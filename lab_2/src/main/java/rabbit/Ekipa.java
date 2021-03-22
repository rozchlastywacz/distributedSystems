package rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Ekipa {
    final static AtomicInteger teamNumber = new AtomicInteger(0);
    final static String HOSTNAME = "localhost";
    final static String EXCHANGE_NAME = "exchange_topic";
    final static String TEAM_KEY = "rabbit.ekipa.";
    final static String SUPPLIER_KEY = "rabbit.dostawca.";
    final static String ADMIN_KEY = "admin.ekipa";
    final static List<String> TYPES = Arrays.asList("buty", "tlen", "plecak");
    final static String RABBIT_PROMPT = "<Rabbit> ";
    
    
    public static void main(String[] argv) throws Exception {
        
        // info
        printRabbit("Say your name");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String name = br.readLine();
        String teamName = name;
        printRabbit("Hello there " + teamName + "!");
        final String TEAM_PROMPT = "<" + teamName + "> "; 
        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOSTNAME);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, false, true, false, null);

        // queue & bind
        String queueName = channel.queueDeclare().getQueue();
        String key = TEAM_KEY + teamName;
        channel.queueBind(queueName, EXCHANGE_NAME, key);
        printRabbit("created queue: " + queueName + " with key " + key);
        
        // for admin
        channel.queueBind(queueName, EXCHANGE_NAME, ADMIN_KEY);
        printRabbit("updated queue: " + queueName + " with key " + ADMIN_KEY);
        

        // consumer (message handling)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(message);
            }
        };

        // start listening
        printRabbit("Ready for receiving messages!");
        channel.basicConsume(queueName, true, consumer);

        // start reading from terminal and sending orders 
        printRabbit("What do you want to buy? " + "[" + String.join(", ", TYPES) + ", exit]");
        while (true) {
            // read what kind of product they want to buy
            String product = br.readLine();
            if(TYPES.contains(product)){
                String supplier_key = SUPPLIER_KEY + product;
                String message = TEAM_PROMPT + "Hello, we want to order " + product;
                // publish
                channel.basicPublish(EXCHANGE_NAME, supplier_key, null, message.getBytes("UTF-8"));
                // printRabbit("Sent: " + message +" via " + EXCHANGE_NAME + " to " + supplier_key);
            }
            // break condition
            if ("exit".equals(product)) {
                break;
            }

        }
        channel.close();
        connection.close();
        printRabbit("Team " + teamName + " is done");
    }
    public static void printRabbit(String msg){
        System.out.println(RABBIT_PROMPT + msg);
    }
}
