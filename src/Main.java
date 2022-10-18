import models.messages.HandshakeMessage;

public class Main {
    public static void main(String[] args) throws Exception {
        HandshakeMessage hsMessage = new HandshakeMessage(25);

        System.out.println(hsMessage);
    }
}