package rabbit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class Admin {
    final static String HOSTNAME = "localhost";
    final static String EXCHANGE_NAME = "exchange_topic";
    final static String RABBIT_KEY = "rabbit.#";
    final static String RABBIT_PROMPT = "<Rabbit> ";
    final static List<String> TYPES = Arrays.asList("ekipa", "dostawca", "oba");
    final static String ADMIN_KEY = "admin.";
    public static void main(String[] args) throws Exception{
        printRabbit("Hello there, Admin!");
        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOSTNAME);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // exchange
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC, false, true, false, null);

        // queue & bind
        String queueName = channel.queueDeclare().getQueue();
        String key = RABBIT_KEY;
        channel.queueBind(queueName, EXCHANGE_NAME, key);
        printRabbit("created queue: " + queueName + " with key " + key);

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
        printRabbit("Where do you want to send your message? " + "[" + String.join(", ", TYPES) + ", exit]");
        while (true) {
            // read what kind of product they want to buy
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String receiver = br.readLine();
            if(TYPES.contains(receiver)){
                if(receiver.equals("oba")){
                    TYPES.stream().filter(s -> !s.equals("oba")).forEach(s -> {
                        try {
                            sendTo(channel, s);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }else{
                    sendTo(channel, receiver);
                }

            }
            // break condition
            if ("exit".equals(receiver)) {
                break;
            }

        }
        channel.close();
        connection.close();
        printRabbit("Admin is done");
    }

    private static void sendTo(Channel channel, String receiver) throws IOException, UnsupportedEncodingException {
        String receiver_key = ADMIN_KEY + receiver;
        String message = "<ADMIN> " + "hey, you " + receiver + ", wazzup?";
        // publish
        channel.basicPublish(EXCHANGE_NAME, receiver_key, null, message.getBytes("UTF-8"));
        // printRabbit("Sent: " + message +" via " + EXCHANGE_NAME + " to " + receiver_key);
    }
    
    public static void printRabbit(String msg){
        System.out.println(RABBIT_PROMPT + msg);
    }
}
