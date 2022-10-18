public class App {
    private static App instance = null;

    // Private constructor
    private App() {

    }

    // Singleton class static instance is returned instead of constructing a new instance
    public static App getApp() {
        if(instance == null)
            instance = new App();

        return instance;
    }

    public void run() throws Exception {

    }
}
