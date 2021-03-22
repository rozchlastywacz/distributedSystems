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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Dostawca {
    final static AtomicInteger supplierNumber = new AtomicInteger(0);
    final static String HOSTNAME = "localhost";
    final static String EXCHANGE_NAME = "exchange_topic";
    final static String TEAM_KEY = "rabbit.ekipa.";
    final static String SUPPLIER_KEY = "rabbit.dostawca.";
    final static String ADMIN_KEY = "admin.dostawca";
    final static List<String> TYPES = Arrays.asList("buty", "tlen", "plecak");
    final static String RABBIT_PROMPT = "<Rabbit> ";
    public static void main(String[] argv) throws Exception {

        // info
        printRabbit("Say your name");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String name = br.readLine();
        String supplierName = name;
        printRabbit("Hello there " + supplierName + "!");
        final String SUPPLIER_PROMPT = "<" + supplierName + "> ";

        // atomic integer for order number
        AtomicInteger orderNumber = new AtomicInteger(0);

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOSTNAME);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, false, true, false, null);

        // check what to sell and create queue & bind
        printRabbit("What do you want to sell? "  + "[" + String.join(", ", TYPES) + ", done]");
        List<String> alreadyUsed = new ArrayList<>();
        while(true){
            String product = br.readLine();
            if(TYPES.contains(product)){
                if(alreadyUsed.contains(product)){
                    printRabbit("You are selling that already!");
                }else{
                    alreadyUsed.add(product);
                    // bind proper queue
                    String queueName = product;
                    String key = SUPPLIER_KEY + product;
                    channel.queueDeclare(queueName, false, false, true, null);
                    printRabbit("declared queue: " + queueName);
                    channel.queueBind(queueName, EXCHANGE_NAME, key);
                    printRabbit("binded queue: " + queueName + " with key " + key);
                }
            }
            if(product.equals("done")){
                break;
            }
            if(alreadyUsed.size() == TYPES.size()){
                break;
            }
        }
        // queue for admin
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, ADMIN_KEY);
        printRabbit("created queue: " + queueName + " with key " + ADMIN_KEY);
        // channel.basicQos(1);
        // consumer (message handling)
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(message);
                String[] parsedMessage = message.split(">");
                String teamName = parsedMessage[0].replace("<", "");
                String[] parsedMessageSecond = parsedMessage[1].split(" ");
                String product = parsedMessageSecond[parsedMessageSecond.length-1];
                if(!teamName.equals("ADMIN")){
                    int orderID = orderNumber.incrementAndGet();
                    String teamKey = TEAM_KEY + teamName;
                    String response = SUPPLIER_PROMPT + "Hello " + teamName + ", your order number for " + product + " is " + orderID;
                    channel.basicPublish(EXCHANGE_NAME, teamKey, null, response.getBytes());
                }
            }
        };

        // start listening
        printRabbit("Waiting for messages...");
        channel.basicConsume(queueName, true, consumer);
        for(String q: alreadyUsed){
            channel.basicConsume(q, true, consumer);
        }

        if(br.readLine().equals("exit")){
            channel.close();
            connection.close();
            printRabbit("Supplier " + supplierName + " is done");
        }
    }
    public static void printRabbit(String msg){
        System.out.println(RABBIT_PROMPT + msg);
    }
}
