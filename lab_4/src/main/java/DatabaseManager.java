import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.sql.*;

public class DatabaseManager extends AbstractBehavior<Messages.Message> {
    private final int FIRST_SAT_ID = 100;
    private final int SAT_ID_RANGE = 100;
    private Connection connection;

    public DatabaseManager(ActorContext<Messages.Message> context, String url) {
        super(context);
        try {
            connection = DriverManager.getConnection(url);
            String create = "CREATE TABLE IF NOT EXISTS errors(id integer PRIMARY KEY, errors_count integer);";
            connection.prepareStatement(create).execute();
            String clearDB = "DELETE FROM errors;";
            connection.prepareStatement(clearDB).executeUpdate();

            connection.setAutoCommit(false);
            String insertZeros = "INSERT INTO errors(id, errors_count) VALUES(?, 0);";
            PreparedStatement ps = connection.prepareStatement(insertZeros);
            for (int i = FIRST_SAT_ID; i < FIRST_SAT_ID + SAT_ID_RANGE; i++) {
                ps.setInt(1, i);
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Behavior<Messages.Message> create(String url) {
        return Behaviors.setup(context -> new DatabaseManager(context, url));
    }

    @Override
    public Receive<Messages.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Messages.DispatcherToDatabase.class, this::onDispatcherToDatabase)
                .onMessage(Messages.MSToDatabase.class, this::onMSToDatabase)
                .build();
    }


    private Behavior<Messages.Message> onDispatcherToDatabase(Messages.DispatcherToDatabase dtdb) {
        String q = "UPDATE errors SET errors_count = errors_count + 1 WHERE id = ?;";
        try {
            PreparedStatement ps = connection.prepareStatement(q);
            for (int id : dtdb.stationsWithError) {
                ps.setInt(1, id);
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }

    private Behavior<Messages.Message> onMSToDatabase(Messages.MSToDatabase mstdb) {
        String q = "SELECT errors_count FROM errors WHERE id = ?;";
        int errors= -1;
        try {
            PreparedStatement ps = connection.prepareStatement(q);
            ps.setInt(1, mstdb.satelliteID);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                errors = rs.getInt(1);
            }
            mstdb.replyTo.tell(new Messages.DatabaseToMS(mstdb.satelliteID, errors));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this;
    }
}
