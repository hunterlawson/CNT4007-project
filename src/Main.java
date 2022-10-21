import controllers.App;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println(System.getProperty("user.dir"));
        App app = App.getApp();

        app.run();
    }
}