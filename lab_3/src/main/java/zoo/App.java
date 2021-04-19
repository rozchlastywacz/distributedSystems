package zoo;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

public class App {
    private static ZooKeeper zooKeeper;
    private static ZKConnection zkConnection;
    private final String DEFAULT_EXEC_PATH = "C:\\WINDOWS\\system32\\mspaint.exe";
    private final String NODE_PATH = "/z";
    private String execPath;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        System.out.println("Starting ZooKeeper App");
        new App().start();
    }

    public App() throws IOException, InterruptedException {
        zkConnection = new ZKConnection();
        zooKeeper = zkConnection.connect("localhost:2181");
    }

    public void start() throws KeeperException, InterruptedException {
        printInfo();
        setupExecPath();
        setupZNode();
        AtomicBoolean running = new AtomicBoolean(true);
        Thread consoleThread = new Thread(() -> {
            while (running.get()) {
                String command = System.console().readLine("Type T to show tree, Q to quit:\n");
                if (command.equals("T")) {
                    showTree(NODE_PATH);
                } else if (command.equals("Q")) {
                    running.set(false);
                    countDownLatch.countDown();
                }
            }
        });
        consoleThread.start();
        countDownLatch.await();
        consoleThread.join();
        zkConnection.close();
    }

    private void showTree(String parent) {
        List<String> children;
        String path = parent;
        if (path.equals("/")) {
            path = "";
        }
        try {
            if (zooKeeper.exists(NODE_PATH, null) != null) {
                children = zooKeeper.getChildren(parent, null);
                Collections.sort(children);
                for (String child : children) {
                    System.out.println(path + "/" + child);
                    showTree(path + "/" + child);
                }
            }else{
                System.out.println("End of children");
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setupZNode() throws KeeperException, InterruptedException {
        ChildrenWatcher childrenWatcher = new ChildrenWatcher(NODE_PATH, zooKeeper);
        NodeWatcher nodeWatcher = new NodeWatcher(NODE_PATH, zooKeeper, childrenWatcher, execPath);
        zooKeeper.addWatch(NODE_PATH, childrenWatcher, AddWatchMode.PERSISTENT_RECURSIVE);
        if (zooKeeper.exists(NODE_PATH, nodeWatcher) != null) {
            printChildrenInfo(NODE_PATH, zooKeeper.getChildren(NODE_PATH, childrenWatcher));
        }
    }

    private void setupExecPath() {
        execPath = System.console().readLine("Set path to executable:\n");
        if (execPath.equals("")) {
            execPath = DEFAULT_EXEC_PATH;
        }
    }

    private void printChildrenInfo(String nodePath, List<String> children) {
        if (children != null) {
            System.out.println("path: " + nodePath + " children count: " + children.size());
            children.forEach(System.out::println);
        } else {
            System.out.println("path: " + nodePath + " children count: 0");
        }
    }

    public void printInfo() {
        System.out.println("Default path to executable: " + DEFAULT_EXEC_PATH);
        System.out.println("Press ENTER to use default path");
    }
}
