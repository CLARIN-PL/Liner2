package g419.liner2.daemon.utils;


import com.rabbitmq.client.*;
import g419.corpus.HasLogger;
import g419.corpus.io.reader.ReaderFactory;
import g419.corpus.io.writer.AbstractDocumentWriter;
import g419.corpus.io.writer.WriterFactory;
import g419.corpus.structure.Document;
import g419.liner2.core.Liner2;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMqWorker implements Runnable, Consumer, HasLogger {

  final String inputQueueName;
  final String outputQueueName;
  final Connection connection;
  final Channel channel;
  final Liner2 liner2;
  final String inputFormat;
  final String hostname;

  public RabbitMqWorker(final String hostname,
                        final String inputQueueName,
                        final String outputQueueName,
                        final String modelPath,
                        final String inputFormat)
      throws IOException, TimeoutException, Exception {
    this.inputQueueName = inputQueueName;
    this.outputQueueName = outputQueueName;
    this.inputFormat = inputFormat;
    this.hostname = hostname;
    final ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(hostname);
    connection = factory.newConnection();
    channel = connection.createChannel();
    channel.queueDeclare(inputQueueName, false, false, false, null);
    liner2 = new Liner2(modelPath);
  }

  /**
   * Called when consumer is registered.
   */
  @Override
  public void handleConsumeOk(final String consumerTag) {
    System.out.println("Consumer " + consumerTag + " registered");
  }

  @Override
  public void handleCancel(final String consumerTag) {
  }

  @Override
  public void handleCancelOk(final String consumerTag) {
  }

  @Override
  public void handleRecoverOk(final String consumerTag) {
  }

  @Override
  public void handleShutdownSignal(final String consumerTag, final ShutdownSignalException arg1) {
  }

  @Override
  public void handleDelivery(final String consumerTag, final Envelope envelope,
                             final AMQP.BasicProperties properties, final byte[] body)
      throws IOException {
    try {
      final String message = new String(body, "UTF-8");
      final String[] parts = message.split(" ");
      if (parts.length == 2) {
        final String route = parts[0];
        final String path = parts[1];
        try {
          final String outputPath = doWork(path);
          submitWork(route, outputPath);
        } catch (final Exception ex) {
          getLogger().error("An exception occured", ex);
          submitWork(route, "ERROR");
        }
      } else {
        getLogger().error("Invalid format of the message: '{}'. Expecting: 'route path'", message);
      }
    } catch (final Exception ex) {
      getLogger().error("Exception", ex);
    } finally {
      getLogger().info("Request processing done");
    }
  }

  @Override
  public void run() {
    try {
      getLogger().info("Listing to RabbitMQ on channel {} ...", inputQueueName);
      channel.basicConsume(inputQueueName, true, this);
    } catch (final IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private String doWork(final String path) throws Exception {
    getLogger().info("Received path: '{}'", path);

    final Document document = ReaderFactory.get().getStreamReader(path, inputFormat).next();
    liner2.chunkInPlace(document);

    final String outputPath = path + "-ner.xml";
    final AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(outputPath, "ccl");
    writer.writeDocument(document);
    writer.close();
    getLogger().info("Output saved to {}", path);

    return outputPath;
  }

  private void submitWork(final String route, final String message) throws IOException, TimeoutException {
    final ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(hostname);
    try (final Connection connection = factory.newConnection();
         final Channel channel = connection.createChannel()) {
      channel.exchangeDeclare(outputQueueName, "direct");
      channel.basicPublish(outputQueueName, route, null, message.getBytes());
    }
    getLogger().info("Sent {} to {}:{}'", message, outputQueueName, route);
  }

  public void close() throws IOException {
    try {
      channel.close();
      connection.close();
    } catch (final TimeoutException ex) {
      throw new RuntimeException(ex);
    }
  }
}

