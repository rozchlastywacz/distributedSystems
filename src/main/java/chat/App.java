package chat;

public final class App {
    private App() {
    }
    public static void main(String[] args) {
        new SRServer().run();
    }
}
