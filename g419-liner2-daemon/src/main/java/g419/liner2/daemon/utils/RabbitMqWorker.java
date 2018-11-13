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

    final String queueName;
    final Connection connection;
    final Channel channel;
    final Liner2 liner2;
    final String inputFormat;

    public RabbitMqWorker(final String queueName, final String modelPath, final String inputFormat)
            throws IOException, TimeoutException, Exception {
        this.queueName = queueName;
        this.inputFormat = inputFormat;
        final ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(queueName, false, false, false, null);
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
            doWork(message);
        } catch (final Exception ex) {
            getLogger().error("Exception", ex);
        } finally {
            getLogger().info("Request processing done");
        }
    }

    @Override
    public void run() {
        try {
            getLogger().info("Listing to RabbitMQ on channel {} ...", queueName);
            channel.basicConsume(queueName, true, this);
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void doWork(final String path) throws Exception {
        getLogger().info("Received path: '{}'", path);

        final Document document = ReaderFactory.get().getStreamReader(path, inputFormat).next();
        liner2.chunkInPlace(document);

        final String outputPath = path + "-ner.xml";
        final AbstractDocumentWriter writer = WriterFactory.get().getStreamWriter(outputPath, "ccl");
        writer.writeDocument(document);
        writer.close();
        getLogger().info("Output saved to {}", path);
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

