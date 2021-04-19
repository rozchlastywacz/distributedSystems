package zoo;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.data.Stat;

public class NodeWatcher implements Watcher {
    private String nodePath;
    private ZooKeeper zooKeeper;
    private ChildrenWatcher childrenWatcher;
    private String execPath;
    private Process process;

    public NodeWatcher(String nodePath, ZooKeeper zooKeeper, ChildrenWatcher childrenWatcher, String execPath) {
        this.nodePath = nodePath;
        this.zooKeeper = zooKeeper;
        this.childrenWatcher = childrenWatcher;
        this.execPath = execPath;
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() != EventType.None) {
            try {
                printInfo(event);
                switch (event.getType()) {
                case NodeCreated:
                    nodeCreated(event);
                    break;
                case NodeDeleted:
                    nodeDeleted(event);
                    break;
                default:
                    break;
                }
            } catch (KeeperException | InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void printInfo(WatchedEvent event) throws KeeperException, InterruptedException {
        Stat stat = zooKeeper.exists(event.getPath(), this);
        if (stat != null) {
            System.out.println("node | path: " + event.getPath() + " eventType: " + event.getType());
        }
    }

    private void nodeDeleted(WatchedEvent event) {
        System.out.println("Node deleted, path: " + event.getPath());
        if (process != null) {
            process.destroy();
            System.out.println("Process killed");
        } else {
            System.out.println("No process to kill");
        }
    }

    private void nodeCreated(WatchedEvent event) throws KeeperException, InterruptedException, IOException {
        System.out.println("Node created, path: " + event.getPath());
        zooKeeper.getChildren(nodePath, childrenWatcher);
        process = Runtime.getRuntime().exec(execPath);
    }

}
