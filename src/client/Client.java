package client;

import commands.*;
import io.Input;
import logic.Packet;
import logic.User;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class Client {
    private static SocketChannel channel;
    private SocketAddress addr;
    private static Invoker invoker;
    private final int port;
    private final String hostname;
    private static boolean close = false;
    private static Input input;
    private User user;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    void run() {
        invoker = new Invoker();
        input = new Input();
        user = new User();

        try {
            try {
                addr = new InetSocketAddress(hostname, port);
                channel = SocketChannel.open(addr);
                channel.configureBlocking(false);
                System.out.println("Connected to server!");

                registerCommands(invoker);

                while (!close) {
                    input.readCommand();
                    try {
                        handleRequest(input.getNextCommand());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            } catch (SocketException e) {
                System.out.println("Cant connect to the server. Server is down.");
                reconnect();
            }
        } catch (NullPointerException | IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleRequest(String userInput) throws IOException, InterruptedException {
        Command command = invoker.createCommand(userInput);

        if (invoker.getCommandName().equals("exit")) {
            command.execOnClient(this);
        }
        else if (command != null) {
            String[] args = invoker.getArgs();

            if (userInput.contains("execute_script")) {
                CommandExecuteScript cmd = (CommandExecuteScript) command;
                ArrayList<Packet> packets = cmd.execute(userInput.split(" "));
                if (packets != null) {
                    for (Packet packet : packets) {
                        sendPacket(packet);
                        Thread.sleep(70);
                    }
                }
            }
            else {
                Packet packet = command.execOnClient(this, args);
                sendPacket(packet);
            }
        }
    }

    private void sendPacket(Packet packet) throws InterruptedException, IOException {
        if (packet != null) {
            byte[] message = serializeObject(packet);
            ByteBuffer wrap = ByteBuffer.wrap(message);

            try {
                channel.write(wrap);
                System.out.println();
                Thread.sleep(150);
            } catch (IOException e) {
                reconnect();
                System.err.println(e.getMessage());
            }
            readMessage();
        }
    }

    private void readMessage() throws IOException, InterruptedException {
        ByteBuffer msg = ByteBuffer.allocate(4096);
        msg.clear();

        if (channel.isConnected()) {
            channel.read(msg);

            if (msg.position() == 0) {
                System.out.println("Now server is locked. Wait please...");
                try {
                    Thread.sleep(1000);
                    readMessage();
                } catch (InterruptedException e) {
                    System.err.println(e.getMessage());
                }
            }

            try {
                Packet packet = (Packet) deserialize(msg.array());
                String input = (String) packet.getArgument();
                if (input != null) {
                    if (input.equals("U login successfully!") && !user.getLoginState()) {
                        user.setLoginState(true);
                    }
                    System.out.println(input);
                }
            } catch (ClassNotFoundException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static byte[] serializeObject(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream out  = new ObjectOutputStream(b);

        out.writeObject(obj);
        return  b.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(bytes);
        ObjectInputStream objStream = new ObjectInputStream(byteArrayInput);

        return objStream.readObject();
    }

    private void reconnect() {
        System.out.println("Reconnecting...");
        try {
            for (int i = 0; i < 10; i++) {
                try {
                    channel = SocketChannel.open(addr);
                    run();
                    break;
                } catch (Exception e) {
                    System.err.println("No answer from server, trying: " + (i + 1));
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }


    public static void disconnect()  {
        close = true;
        try {
            channel.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        input.closeInput();
        System.out.println("Client was closed...");
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

    private void registerCommands(Invoker invoker) {
        invoker.register("show", new CommandShow());
        invoker.register("execute_script", new CommandExecuteScript( this, invoker));
        invoker.register("update_id", new CommandUpdateId());
        invoker.register("clear", new CommandClear());
        invoker.register("exit", new CommandExit());
        invoker.register("remove_at", new CommandRemoveAt());
        invoker.register("count_by_distance", new CommandCountByDistance());
        invoker.register("print_unique_distance", new CommandPrintUniqueDistance());
        invoker.register("add", new CommandAdd());
        invoker.register("add_if_min", new CommandAddIfMin());
        invoker.register("add_if_max", new CommandAddIfMax());
        invoker.register("remove_by_id", new CommandRemoveById());
        invoker.register("info", new CommandInfo());
        invoker.register("help", new CommandHelp());
        invoker.register("print_field_ascending_distance", new CommandPrintFieldAscendingDistance());
        invoker.register("register", new CommandRegister());
        invoker.register("login", new CommandLogin());
    }
}
