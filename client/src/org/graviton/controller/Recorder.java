package org.graviton.controller;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

public class Recorder implements IoHandler {
    private static int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();

    private static int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

    private boolean onRecord;

    public Recorder() {
        NioSocketConnector connector = new NioSocketConnector();
        connector.setHandler(this);
        connector.connect(new InetSocketAddress("192.168.0.6", 2999));

        long time = System.currentTimeMillis();
        while (!connector.isActive() && (System.currentTimeMillis() - time < 5000)) ;
        if (connector.isActive())
            System.out.println("Connected to media server");
        else
            System.out.println("Failed to connect to media server");
    }

    private void record(IoSession session) {
        System.out.println("Record : start");
        onRecord = true;
        Thread recordThread = new Thread() {
            @Override
            public void run() {
                Robot rt;
                try {
                    rt = new Robot();
                    while (onRecord) {
                        BufferedImage image = rt.createScreenCapture(new Rectangle(screenWidth, screenHeight));
                        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
                        ImageOutputStream outputStream = ImageIO.createImageOutputStream(compressed);
                        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
                        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        jpgWriteParam.setCompressionQuality(0.7f);
                        jpgWriter.setOutput(outputStream);
                        jpgWriter.write(null, new IIOImage(image, null, null), jpgWriteParam);
                        jpgWriter.dispose();
                        byte[] bytes2 = compressed.toByteArray();
                        IoBuffer buffer = IoBuffer.allocate(bytes2.length, false);
                        buffer.setAutoExpand(true);
                        buffer.put(bytes2);
                        buffer.flip();
                        session.write(buffer);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        recordThread.start();
    }

    private void stop() {
        onRecord = false;
        System.out.println("Record : stop");
    }

    @Override
    public void sessionCreated(IoSession ioSession) throws Exception {
        if (!onRecord)
            record(ioSession);
    }

    @Override
    public void sessionOpened(IoSession ioSession) throws Exception {

    }

    @Override
    public void sessionClosed(IoSession ioSession) throws Exception {
        if (onRecord)
            stop();
    }

    @Override
    public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {

    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {

    }

    @Override
    public void messageReceived(IoSession ioSession, Object o) throws Exception {

    }

    @Override
    public void messageSent(IoSession ioSession, Object o) throws Exception {

    }

    @Override
    public void inputClosed(IoSession ioSession) throws Exception {

    }
}
