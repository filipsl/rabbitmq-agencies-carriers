package model;

import com.rabbitmq.client.*;
import services.ServiceType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import static model.Admin.ADMIN_EXCHANGE_NAME;
import static model.Agency.AGENCY_EXCHANGE_NAME;


public class Carrier extends AbstractUser {

    public static final String CARRIER_EXCHANGE_NAME = "carrier_exchange";

    private final String name;
    private final ServiceType serviceType1;
    private final ServiceType serviceType2;
    private final Channel confirmationChannel;
    private final Channel serviceChannel1;
    private final Channel serviceChannel2;
    private final String queue1Name;
    private final String queue2Name;
    private final String queueAdminName;


    public Carrier(String name, ServiceType serviceType1, ServiceType serviceType2) throws IOException, TimeoutException {
        super();
        this.name = name;
        this.serviceType1 = serviceType1;
        this.serviceType2 = serviceType2;
        this.confirmationChannel = connection.createChannel();
        this.serviceChannel1 = connection.createChannel();
        this.serviceChannel2 = connection.createChannel();
        this.queue1Name = serviceType1.name().toLowerCase();
        this.queue2Name = serviceType2.name().toLowerCase();
        this.queueAdminName = name.toLowerCase() + "_carrier";
        declareBindQueues();
    }

    private void declareBindQueues() throws IOException {
        declareBindQueue(serviceChannel1, queue1Name, CARRIER_EXCHANGE_NAME, queue1Name);
        declareBindQueue(serviceChannel2, queue2Name, CARRIER_EXCHANGE_NAME, queue2Name);
        declareBindQueue(basicChannel, queueAdminName, ADMIN_EXCHANGE_NAME, "#.c");
        serviceChannel1.basicQos(1);
        serviceChannel2.basicQos(1);
    }

    private void declareBindQueue(Channel channel, String queueName, String exchangeName, String key) throws IOException {
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, exchangeName, key);
    }


    private synchronized void handleAgencyConfirmation(String agencyName, String message) throws IOException {
        confirmationChannel.basicPublish(AGENCY_EXCHANGE_NAME, agencyName.toLowerCase(), null, message.getBytes(StandardCharsets.UTF_8));
    }

    private void handleService(byte[] body, ServiceType serviceType) throws IOException {
        long threadId = Thread.currentThread().getId();
        System.out.println("Thread # " + threadId + " is doing this task");

        String message = new String(body, StandardCharsets.UTF_8);
        String[] parts = message.split("#");
        String agencyName = parts[0];
        String taskNumberString = parts[1];
        printSynchronized("[New task assigned] agency: " + agencyName + "; task no. "
                + taskNumberString + "; type: " + serviceType.name());
        //Do some work...
        try {
            Thread.sleep(4 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        printSynchronized("[Task done]");
        String replyMessage = "Task no. " + taskNumberString + " type: " + serviceType.name()
                + " has been done by " + name;
        handleAgencyConfirmation(agencyName, replyMessage);
    }


    public void start() throws Exception {

        printSynchronized("Starting carrier " + name);

        Consumer serviceConsumer1 = new DefaultConsumer(serviceChannel1) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                handleService(body, serviceType1);
                serviceChannel1.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        Consumer serviceConsumer2 = new DefaultConsumer(serviceChannel2) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                handleService(body, serviceType2);
                serviceChannel2.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        Consumer adminConsumer = new DefaultConsumer(basicChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                printSynchronized("Received Admin message: " + message);
                basicChannel.basicAck(envelope.getDeliveryTag(), false);
            }
        };


        System.out.println("Waiting for messages...");
        serviceChannel1.basicConsume(queue1Name, false, serviceConsumer1);
        serviceChannel2.basicConsume(queue2Name, false, serviceConsumer2);
        basicChannel.basicConsume(queueAdminName, false, adminConsumer);
    }
}
