package model;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static model.Admin.ADMIN_EXCHANGE_NAME;
import static model.Agency.AGENCY_EXCHANGE_NAME;
import static model.Carrier.CARRIER_EXCHANGE_NAME;

abstract class AbstractUser {
    Connection connection;
    Channel basicChannel;

    AbstractUser() throws IOException, TimeoutException {
        initChannel();
        declareExchanges();
    }

    private void initChannel() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        basicChannel = connection.createChannel();
    }

    private void declareExchanges() throws IOException {
        basicChannel.exchangeDeclare(ADMIN_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        basicChannel.exchangeDeclare(AGENCY_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        basicChannel.exchangeDeclare(CARRIER_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
    }

    void declareBindQueue(Channel channel, String queueName, String exchangeName, String key) throws IOException {
        channel.queueDeclare(queueName, false, false, false, null);
        channel.queueBind(queueName, exchangeName, key);
    }

    synchronized void printSynchronized(String string) {
        System.out.println(string);
    }
}
