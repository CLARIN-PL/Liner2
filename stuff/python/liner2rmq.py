#!/usr/bin/env python3
import pika
import sys
import random
import string
import codecs
import os
import tempfile


class Liner2Rmq:

    def __init__(self, queue_prefix="liner2"):
        self.input_queue = queue_prefix+"-input"
        self.output_queue = queue_prefix + "-output"
        self.connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))

    def send(self, route, filename):
        channel = self.connection.channel()
        channel.queue_declare(queue=self.input_queue)
        msg = "%s %s" % (route, filename)
        channel.basic_publish(exchange='', routing_key=self.input_queue, body=msg)
        print("[INFO] Sent msg '%s' to %s" % (msg, self.input_queue))

    def receive(self, route):
        channel = self.connection.channel()
        channel.exchange_declare(exchange=self.output_queue, exchange_type='direct')
        result = channel.queue_declare(exclusive=True)
        queue_name = result.method.queue
        channel.queue_bind(exchange=self.output_queue, queue=queue_name, routing_key=route)
        response = next(channel.consume(queue_name))
        return response[2]

    @staticmethod
    def save(text):
        filename = os.path.join(tempfile._get_default_tempdir(), next(tempfile._get_candidate_names()))
        with codecs.open(filename, "w", "utf8") as f:
            f.write(text)
        return filename

    @staticmethod
    def load(filename):
        with codecs.open(filename, "r", "utf8") as f:
            content = f.read()
        return content

    @staticmethod
    def delete(filename):
        try:
            os.remove(filename)
        except OSError:
            pass

    def process(self, text):
        route = "route-" + ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(6))
        print("[INFO] Temp route: %s" % route)
        input_path = self.save(text)
        print("[INFO] Temp input file: %s" % input_path)
        self.send(route, input_path)
        output_path = self.receive(route)
        print("[INFO] Temp output file: %s" % output_path)
        content = self.load(output_path)
        self.delete(input_path)
        self.delete(output_path)
        return content

    def close(self):
        self.connection.close()


if __name__ == "__main__":
    if len(sys.argv) > 2:
        liner2 = Liner2Rmq()
        text = sys.argv[2]
        while True:
            output = liner2.process(text)
            print(output)
    elif len(sys.argv) > 1:
        liner2 = Liner2Rmq()
        xml = liner2.process(sys.argv[1])
        liner2.close()
        print(xml)
    else:
        print("[ERROR] No text to send. Run ./liner2.py 'Text to process'")
