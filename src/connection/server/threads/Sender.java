package connection.server.threads;

import logic.Packet;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class Sender implements Runnable {
    private final String message;
    private final ObjectOutputStream out;

    Sender(String answer, ObjectOutputStream out) {
        this.message = answer;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            Packet packet = new Packet(null, message, null);
            out.writeObject(packet);
            out.flush();
            Thread.sleep(70);
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}
