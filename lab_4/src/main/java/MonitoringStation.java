import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MonitoringStation extends AbstractBehavior<Messages.Message> {
    private final ActorRef<Messages.Message> dispatcher;
    private final int stationID;
    private AtomicInteger queryID;
    private final Map<Integer, Long> queriesTimestamps;
    private final Executor ec;
    private final ActorRef<Messages.Message> db;

    public MonitoringStation(ActorContext<Messages.Message> context, ActorRef<Messages.Message> dispatcher, int stationID, ActorRef<Messages.Message> db) {
        super(context);
        this.dispatcher = dispatcher;
        this.stationID = stationID;
        this.queryID = new AtomicInteger(0);
        this.queriesTimestamps = new HashMap<>();
        ec = context
                .getSystem()
                .dispatchers()
                .lookup(DispatcherSelector.fromConfig("my-dispatcher"));
        this.db = db;
    }

    public static Behavior<Messages.Message> create(int ID, ActorRef<Messages.Message> dispatcher, ActorRef<Messages.Message> db) {
        return Behaviors
                .setup((context) -> new MonitoringStation(context, dispatcher, ID, db));
    }

    @Override
    public Receive<Messages.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Messages.StatusRequest.class, this::onStatusRequest)
                .onMessage(Messages.StatusQueryResponse.class, this::onStatusQueryResponse)
                .onMessage(Messages.MainToMS.class, this::onMainToMS)
                .onMessage(Messages.DatabaseToMS.class, this::onDatabaseToMS)
                .build();
    }



    // handle request from main
    private Behavior<Messages.Message> onStatusRequest(Messages.StatusRequest sr) {
        Messages.StatusQuery sq = new Messages.StatusQuery(
                sr.firstSatID,
                sr.range,
                sr.timeout,
                stationID,
                queryID.incrementAndGet(),
                getContext().getSelf()
        );
        dispatcher.tell(sq);
        queriesTimestamps.put(queryID.get(), System.currentTimeMillis());
        return this;
    }

    // handle response from dispatcher
    private Behavior<Messages.Message> onStatusQueryResponse(Messages.StatusQueryResponse sqr) {
        long elapsed = System.currentTimeMillis() - queriesTimestamps.remove(sqr.queryID);
        ec.execute(() -> {
            synchronized (System.out) {
                String border = "- - - - - - - - - - - - - - -";
                System.out.println(border);
                System.out.println("Monitoring station name: " + getContext().getSelf().path().name());
                System.out.println("Elapsed time: " + elapsed + " [ms]");
                System.out.println("Response percentage: " + String.format("%.0f", sqr.responsesPercentage * 100) + "%");
                System.out.println("Number of errors: " + sqr.errors.size());
                System.out.println("    > > > > Error list < < < < ");
                for (int key : sqr.errors.keySet().stream().sorted().collect(Collectors.toList())) {
                    System.out.println("    Satellite " + key + ": " + sqr.errors.get(key));
                }
                System.out.println("    < < < < Error list > > > >");
            }
        });

        return this;
    }

    // handle db request
    private Behavior<Messages.Message> onMainToMS(Messages.MainToMS mtms) {
        db.tell(new Messages.MSToDatabase(getContext().getSelf(), mtms.satelliteID));
        return this;
    }

    private Behavior<Messages.Message> onDatabaseToMS(Messages.DatabaseToMS dbtms) {
        System.out.println("---Satellite " + dbtms.satelliteID + " had " + dbtms.errors + " reported---");
        return this;
    }

}
