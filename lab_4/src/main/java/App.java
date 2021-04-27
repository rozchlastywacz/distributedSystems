import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class App {
    private final static int FIRST_SAT_ID = 100;
    private final static int SAT_ID_RANGE = 100;
    private static final String DB_URL = "src/main/resources/database.db";
    private static final Random random = new Random();

    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    // create db manager
                    ActorRef<Messages.Message> dbManager = context.spawn(DatabaseManager.create("jdbc:sqlite:" + DB_URL), "dbManager");
                    // create dispatcher
                    ActorRef<Messages.Message> dispatcher = context.spawn(Dispatcher.create(dbManager), "dispatcher");
                    // create monit stations
                    List<ActorRef<Messages.Message>> msList = new LinkedList<>();
                    for(int i = 0; i < 3; i++){
                        msList.add(context.spawn(MonitoringStation.create(i, dispatcher, dbManager), "ms_"+i));
                    }
                    // send requests
                    for(ActorRef<Messages.Message> ms : msList){
                        int timeout = 300;
                        ms.tell(new Messages.StatusRequest(FIRST_SAT_ID+random.nextInt(50), 50, timeout));
                        ms.tell(new Messages.StatusRequest(FIRST_SAT_ID+random.nextInt(50), 50, timeout));
                    }
                    Thread.sleep(1000);
                    System.out.println("---       DATA FROM DATABASE    ---");
                    for(int i = FIRST_SAT_ID; i < FIRST_SAT_ID+SAT_ID_RANGE; i++){
                        msList.get(0).tell(new Messages.MainToMS(i));
                    }

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    public static void main(String[] args) {
        File configFile = new File("src/main/resources/dispatcher.conf");
        Config config = ConfigFactory.parseFile(configFile);
        ActorSystem.create(App.create(), "app", config);
    }
}
