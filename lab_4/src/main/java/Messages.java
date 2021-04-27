import akka.actor.typed.ActorRef;

import java.util.List;
import java.util.Map;

public class Messages {
    // basic interface
    interface Message {
    }

    // satellite status request from main to monitoring station
    public static class StatusRequest implements Message {
        final int firstSatID;
        final int range;
        final int timeout;

        public StatusRequest(int firstSatID, int range, int timeout) {
            this.firstSatID = firstSatID;
            this.range = range;
            this.timeout = timeout;
        }
    }

    // satellite status query from monitoring station to dispatcher
    public static class StatusQuery extends StatusRequest {
        final int msID;
        final int queryID;
        final ActorRef<Message> replyTo;

        public StatusQuery(int firstSatID, int range, int timeout, int msID, int queryID, ActorRef<Message> replyTo) {
            super(firstSatID, range, timeout);
            this.msID = msID;
            this.queryID = queryID;
            this.replyTo = replyTo;
        }
    }

    // satellite status message from dispatcher to satellite actor
    public static class StatusMessage implements Message{
        final int msID;
        final int queryID;
        final int timeout;
        final ActorRef<Message> replyTo;

        public StatusMessage(int msID, int queryID, int timeout, ActorRef<Message> replyTo) {
            this.msID = msID;
            this.queryID = queryID;
            this.timeout = timeout;
            this.replyTo = replyTo;
        }
    }

    // satellite status response message from satellite actor to dispatcher
    public static class StatusResponseMessage implements Message{
        final int msID;
        final int queryID;
        final int satelliteID;
        final SatelliteAPI.Status result;

        public StatusResponseMessage(int msID, int queryID, int satelliteID, SatelliteAPI.Status result) {
            this.msID = msID;
            this.queryID = queryID;
            this.satelliteID = satelliteID;
            this.result = result;
        }

        public boolean isSuccessful(){
            return result != null;
        }
    }

    // satellite status response from dispatcher to monitoring station
    public static class StatusQueryResponse implements Message{
        final int queryID;
        final Map<Integer, SatelliteAPI.Status> errors;
        final double responsesPercentage;

        public StatusQueryResponse(int queryID, Map<Integer, SatelliteAPI.Status> errors, double responsesPercentage) {
            this.queryID = queryID;
            this.errors = errors;
            this.responsesPercentage = responsesPercentage;
        }
    }

    // from dispatcher to database
    public static class DispatcherToDatabase implements Message{
        final List<Integer> stationsWithError;

        public DispatcherToDatabase(List<Integer> stationsWithError) {
            this.stationsWithError = stationsWithError;
        }
    }

    // from main to monitoring station

    public static class MainToMS implements Message{
        final int satelliteID;

        public MainToMS(int satelliteID) {
            this.satelliteID = satelliteID;
        }
    }

    // from monitoring station to database
    public static class MSToDatabase implements Message{
        final ActorRef<Message> replyTo;
        final int satelliteID;

        public MSToDatabase(ActorRef<Message> replyTo, int satelliteID) {
            this.replyTo = replyTo;
            this.satelliteID = satelliteID;
        }
    }

    // from database to monitoring station
    public static class DatabaseToMS implements Message{
        final int satelliteID;
        final int errors;

        public DatabaseToMS(int satelliteID, int errors) {
            this.satelliteID = satelliteID;
            this.errors = errors;
        }
    }

}
