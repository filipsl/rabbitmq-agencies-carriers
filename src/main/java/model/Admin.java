package model;

import com.rabbitmq.client.*;
import services.ServiceType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static model.Agency.AGENCY_EXCHANGE_NAME;
import static model.Carrier.CARRIER_EXCHANGE_NAME;

public class Admin extends AbstractUser {
    static final String ADMIN_EXCHANGE_NAME = "admin_exchange";

    private final Channel messageChannel;
    private final String messageQueueName = "admin";


    public Admin() throws Exception {
        super();
        this.messageChannel = connection.createChannel();
        declareBindQueues();
        start();
    }

    private void declareBindQueues() throws IOException {
        declareBindQueue(messageChannel, messageQueueName, AGENCY_EXCHANGE_NAME, "#");
        declareBindQueue(messageChannel, messageQueueName, CARRIER_EXCHANGE_NAME, "#");
    }


    private void start() throws Exception {
        printSynchronized("Starting Admin");
        Consumer messageConsumer = new DefaultConsumer(messageChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                System.out.println("Received message: " + message);
                messageChannel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        messageChannel.basicConsume(messageQueueName, false, messageConsumer);
    }

    private void sendMessage(String message, String key) throws IOException {
        message = "ADMIN: " + message;
        basicChannel.basicPublish(ADMIN_EXCHANGE_NAME, key,
                null, message.getBytes(StandardCharsets.UTF_8));
    }

    public void sendMessageToCarriers(String message) throws IOException {
        sendMessage(message, ".c");
    }

    public void sendMessageToAgencies(String message) throws IOException {
        sendMessage(message, "a.");
    }

    public void sendMessageToAll(String message) throws IOException {
        sendMessage(message, "a.c");
    }

    public void close() throws IOException, TimeoutException {
        basicChannel.close();
        messageChannel.close();
        connection.close();
    }

}
