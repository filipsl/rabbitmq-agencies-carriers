package model;

import com.rabbitmq.client.*;
import services.ServiceType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static model.Admin.ADMIN_EXCHANGE_NAME;
import static model.Carrier.CARRIER_EXCHANGE_NAME;

public class Agency extends AbstractUser {
    public static final String AGENCY_EXCHANGE_NAME = "agency_exchange";

    private final String name;

    private final Channel messageChannel;
    private final String messageQueueName;
    private int taskCount = 0;


    public Agency(String name) throws Exception {
        super();
        this.name = name;
        this.messageChannel = connection.createChannel();
        this.messageQueueName = name.toLowerCase() + "_agency";
        declareBindQueues();
        start();
    }

    private void declareBindQueues() throws IOException {
        declareBindQueue(messageChannel, messageQueueName, ADMIN_EXCHANGE_NAME, "a.#");
        declareBindQueue(messageChannel, messageQueueName, AGENCY_EXCHANGE_NAME, name.toLowerCase());
        basicChannel.basicQos(1, false);
    }

    private void declareBindQueue(Channel channel, String queueName, String exchangeName, String key) throws IOException {
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, exchangeName, key);
    }

    public void start() throws Exception {
        printSynchronized("Starting agency " + name);
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

    public void addTask(ServiceType serviceType) throws IOException {
        String message = name + "#" + taskCount;
        basicChannel.basicPublish(CARRIER_EXCHANGE_NAME, serviceType.name().toLowerCase(),
                null, message.getBytes(StandardCharsets.UTF_8));
        taskCount++;
    }

}
