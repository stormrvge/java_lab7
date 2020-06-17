package server;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("You have to choose PORT, on which the server will work, user and password on helios");
        }
        else {
            int port = Integer.parseInt(args[0]);
            String user = args[1];
            String password = args[2];
            Server server = new Server(port, user, password);
            server.process();
        }
    }
}
