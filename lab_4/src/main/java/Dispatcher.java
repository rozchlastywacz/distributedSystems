import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Dispatcher extends AbstractBehavior<Messages.Message> {
    private final int FIRST_SAT_ID = 100;
    private final int SAT_ID_RANGE = 100;
    private final Map<IDPair, ActiveQuery> activeQueries;
    private final Map<Integer, ActorRef<Messages.Message>> satellites;
    private final ActorRef<Messages.Message> db;

    public Dispatcher(ActorContext<Messages.Message> context, ActorRef<Messages.Message> db) {
        super(context);
        this.activeQueries = new HashMap<>();
        this.satellites = new HashMap<>();
        for (int i = FIRST_SAT_ID; i < FIRST_SAT_ID + SAT_ID_RANGE; i++) {
            ActorRef<Messages.Message> satellite = getContext().spawn(
                    Behaviors.supervise(Satellite.create(i)).onFailure(Exception.class, SupervisorStrategy.resume()),
                    "satellite_" + i
            );
            satellites.put(i, satellite);
        }
        this.db = db;
    }

    public static Behavior<Messages.Message> create(ActorRef<Messages.Message> db) {
        return Behaviors.setup(context -> new Dispatcher(context, db));
    }

    @Override
    public Receive<Messages.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Messages.StatusQuery.class, this::onStatusQuery)
                .onMessage(Messages.StatusResponseMessage.class, this::onStatusResponseMessage)
                .build();
    }

    // handle query from monitoring station
    private Behavior<Messages.Message> onStatusQuery(Messages.StatusQuery sq) {
        IDPair ids = new IDPair(sq.msID, sq.queryID);
        ActiveQuery activeQuery = new ActiveQuery(sq.replyTo, sq.queryID, sq.range);
        activeQueries.put(ids, activeQuery);
        for (int i = sq.firstSatID; i < sq.firstSatID + sq.range; i++) {
            Messages.StatusMessage sm = new Messages.StatusMessage(sq.msID, sq.queryID, sq.timeout, getContext().getSelf());
            satellites.get(i).tell(sm);
        }
        return this;
    }

    // handle response from satellite
    private Behavior<Messages.Message> onStatusResponseMessage(Messages.StatusResponseMessage srm) {
        IDPair ids = new IDPair(srm.msID, srm.queryID);
        ActiveQuery activeQuery = activeQueries.get(ids);
        activeQuery.numberOfResponses += 1;
        if (srm.isSuccessful()) {
            activeQuery.numberOfSuccessfulResponses += 1;
            if (!srm.result.equals(SatelliteAPI.Status.OK)) {
                activeQuery.errors.put(srm.satelliteID, srm.result);
            }
        }
        if (activeQuery.numberOfRequests == activeQuery.numberOfResponses) {
            activeQueries.remove(ids);
            double percentage = (double) activeQuery.numberOfSuccessfulResponses / (double) activeQuery.numberOfResponses;
            Messages.StatusQueryResponse sqr = new Messages.StatusQueryResponse(activeQuery.queryID, activeQuery.errors, percentage);
            activeQuery.replyTo.tell(sqr);
            db.tell(new Messages.DispatcherToDatabase(activeQuery.errors.keySet().stream().toList()));
        }
        return this;
    }


    private static class IDPair {
        final int msID;
        final int queryID;

        private IDPair(int msID, int queryID) {
            this.msID = msID;
            this.queryID = queryID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IDPair idPair = (IDPair) o;
            return msID == idPair.msID && queryID == idPair.queryID;
        }

        @Override
        public int hashCode() {
            return Objects.hash(msID, queryID);
        }
    }

    private static class ActiveQuery {
        final ActorRef<Messages.Message> replyTo;
        final int queryID;
        final int numberOfRequests;
        final Map<Integer, SatelliteAPI.Status> errors;
        int numberOfResponses;
        int numberOfSuccessfulResponses;

        private ActiveQuery(ActorRef<Messages.Message> replyTo, int queryID, int numberOfRequests) {
            this.replyTo = replyTo;
            this.queryID = queryID;
            this.numberOfRequests = numberOfRequests;
            this.errors = new HashMap<>();
            numberOfResponses = 0;
            numberOfSuccessfulResponses = 0;
        }
    }
}