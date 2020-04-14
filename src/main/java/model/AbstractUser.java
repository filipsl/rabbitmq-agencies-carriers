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
    ConnectionFactory factory;
    Connection connection;
    Channel basicChannel;

    AbstractUser() throws IOException, TimeoutException {
        initChannel();
        declareExchanges();
    }

    void initChannel() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost("localhost");
        connection = factory.newConnection();
        basicChannel = connection.createChannel();
    }

    void declareExchanges() throws IOException {
        basicChannel.exchangeDeclare(ADMIN_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        basicChannel.exchangeDeclare(AGENCY_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        basicChannel.exchangeDeclare(CARRIER_EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
    }

    synchronized void printSynchronized(String string) {
        System.out.println(string);
    }
}
