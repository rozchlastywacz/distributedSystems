package zoo;

import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;

public class ChildrenWatcher implements Watcher {
    private String nodePath;
    private ZooKeeper zooKeeper;

    public ChildrenWatcher(String nodePath, ZooKeeper zooKeeper) {
        this.nodePath = nodePath;
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() != EventType.None) {
            try {
                if (zooKeeper.exists(nodePath, null) != null) {
                    zooKeeper.addWatch(nodePath, this, AddWatchMode.PERSISTENT_RECURSIVE);
                    System.out.println("All children number: " + zooKeeper.getAllChildrenNumber(nodePath));
                }
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
