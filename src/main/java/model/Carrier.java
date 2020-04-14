package model;

import com.rabbitmq.client.*;
import services.ServiceType;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static model.Admin.ADMIN_EXCHANGE_NAME;
import static model.Agency.AGENCY_EXCHANGE_NAME;


public class Carrier extends AbstractUser {

    public static final String CARRIER_EXCHANGE_NAME = "carrier_exchange";

    private final String name;
    private final ServiceType serviceType1;
    private final ServiceType serviceType2;
    private final Channel replyChannel;
    private final String queue1Name;
    private final String queue2Name;
    private final String queueAdminName;


    public Carrier(String name, ServiceType serviceType1, ServiceType serviceType2) throws IOException, TimeoutException {
        super();
        this.name = name;
        this.serviceType1 = serviceType1;
        this.serviceType2 = serviceType2;
        this.replyChannel = connection.createChannel();
        this.queue1Name = serviceType1.name().toLowerCase();
        this.queue2Name = serviceType2.name().toLowerCase();
        this.queueAdminName = name.toLowerCase() + "_carrier";
        declareBindQueues();
    }

    private void declareBindQueues() throws IOException {
        declareBindQueue(basicChannel, queue1Name, CARRIER_EXCHANGE_NAME, queue1Name);
        declareBindQueue(basicChannel, queue2Name, CARRIER_EXCHANGE_NAME, queue2Name);
        declareBindQueue(basicChannel, queueAdminName, ADMIN_EXCHANGE_NAME, "#.c");
        basicChannel.basicQos(1, false);
    }

    private void declareBindQueue(Channel channel, String queueName, String exchangeName, String key) throws IOException {
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, exchangeName, key);
    }


    private synchronized void handleReply(String agencyName, String message) throws IOException {
        replyChannel.basicPublish(AGENCY_EXCHANGE_NAME, agencyName.toLowerCase() + "_agency", null, message.getBytes("UTF-8"));
    }

    private void handleService(byte[] body, ServiceType serviceType) throws IOException {
        String message = new String(body, "UTF-8");
        String[] parts = message.split("#");
        String agencyName = parts[0];
        String taskNumberString = parts[1];
        printSynchronized("[New task assigned] agency:" + agencyName + "; task no. "
                + taskNumberString + "; type: " + serviceType.name());
        //Do some work...
        printSynchronized("[Task done]");
        String replyMessage = "Task no. " + taskNumberString + "type: " + serviceType.name()
                + " has been done by " + name;
        handleReply(agencyName, replyMessage);
    }


    public void start() throws Exception {

        printSynchronized("Starting carrier " + name);

        Consumer serviceConsumer1 = new DefaultConsumer(basicChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                handleService(body, serviceType1);
                basicChannel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        Consumer serviceConsumer2 = new DefaultConsumer(basicChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                handleService(body, serviceType2);
                basicChannel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        Consumer adminConsumer = new DefaultConsumer(basicChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                printSynchronized("Received Admin message: " + message);
                basicChannel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        // start listening
        System.out.println("Waiting for messages...");
        basicChannel.basicConsume(queue1Name, false, serviceConsumer1);
        basicChannel.basicConsume(queue2Name, false, serviceConsumer2);
        basicChannel.basicConsume(queueAdminName, false, adminConsumer);
    }
}
