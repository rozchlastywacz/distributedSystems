package zoo;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

public class ZKConnection {
    private final int SESSION_TIMEOUT = 2000;
    private ZooKeeper zooKeeper;
    private CountDownLatch connectionLatch = new CountDownLatch(1);

    public ZooKeeper connect(String connectString) throws IOException, InterruptedException {
        this.zooKeeper = new ZooKeeper(connectString, SESSION_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == KeeperState.SyncConnected) {
                    connectionLatch.countDown();
                }
            }

        });
        connectionLatch.await();
        return this.zooKeeper;
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }
}
