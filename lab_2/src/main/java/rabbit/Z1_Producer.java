package rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;

public class Z1_Producer {

    public static void main(String[] args) throws Exception {

        // info
        System.out.println("Z1 PRODUCER");

        // connection & channel
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // queue
        String QUEUE_NAME = "queue1";
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        // producer (publish msg)
//        String message = "Hello world!";
//        int i = 10;
//        while(i-->0) {
//            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//            String message = br.readLine();
//            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
//            System.out.println("Sent: " + message);
//        }
        int[] dane = {2,5,1,5,1,5,1,5,1,5};
        for(int i : dane){
            channel.basicPublish("", QUEUE_NAME, null, String.valueOf(i).getBytes());
            System.out.println("Sent: " + i);
        }
        // close
        channel.close();
        connection.close();
    }
}
