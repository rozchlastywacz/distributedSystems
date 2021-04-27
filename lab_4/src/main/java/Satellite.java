import akka.actor.typed.Behavior;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class Satellite extends AbstractBehavior<Messages.Message> {
    final int satelliteID;
    private final Executor ec;

    public Satellite(ActorContext<Messages.Message> context, int satelliteID) {
        super(context);
        this.satelliteID = satelliteID;
        ec = context
                .getSystem()
                .dispatchers()
                .lookup(DispatcherSelector.fromConfig("my-dispatcher"));
    }

    public static Behavior<Messages.Message> create(int satelliteID) {
        return Behaviors.setup(context -> new Satellite(context, satelliteID));
    }

    @Override
    public Receive<Messages.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Messages.StatusMessage.class, this::onStatusMessage)
                .build();
    }

    private Behavior<Messages.Message> onStatusMessage(Messages.StatusMessage sm) {
        CompletableFuture
                .supplyAsync(
                        () -> SatelliteAPI.getStatus(satelliteID), ec)
                .orTimeout(sm.timeout, TimeUnit.MILLISECONDS)
                .whenComplete((result, exception) -> {
                    Messages.StatusResponseMessage srm = new Messages.StatusResponseMessage(sm.msID, sm.queryID, satelliteID, result);
                    sm.replyTo.tell(srm);
                });
        return this;
    }
}
