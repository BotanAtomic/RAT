package org.graviton.network.media;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.graviton.network.Client;
import org.graviton.network.Server;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;

public class MediaServer {
    private static final int PORT = 2999;
    private static MediaServer instance;
    private final Stage stage;
    public boolean mouseControl;
    private Socket currentSocket;
    private HBox container = new HBox();
    private Scene scene;
    private boolean onRecord;

    public MediaServer(Stage stage) throws IOException {
        instance = this;
        this.stage = stage;

        Platform.setImplicitExit(false);
        stage.setScene(this.scene = new Scene(container, 1280, 720) {{
            setFill(null);
        }});
        stage.setOnCloseRequest((r) -> {
            onRecord = false;
            closeSocket();
        });

        container.setOnMouseClicked(event -> {
            Server.getInstance().send("m" + (int) event.getX() + ":" + (int) event.getY() + "!");
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                Server.getInstance().send("e" + e.getMessage());
            } finally {
                Server.getInstance().send("c" + (event.getButton() == MouseButton.SECONDARY ? "2" : "1"));
            }
        });

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(2999));
        System.out.println("Media server listen to port " + PORT + " ...");

        new Thread(() -> {
            while (true) {
                try {
                    currentSocket = serverSocket.accept();
                    BufferedInputStream stream = new BufferedInputStream(currentSocket.getInputStream());

                    while (currentSocket.isConnected()) {
                        stream.mark(50 * 1024 * 1024);

                        ImageInputStream imgStream = ImageIO.createImageInputStream(stream);

                        Iterator<ImageReader> i = ImageIO.getImageReaders(imgStream);
                        if (!i.hasNext()) {
                            break;
                        }

                        ImageReader reader = i.next();
                        reader.setInput(imgStream);

                        BufferedImage image;
                        try {
                            image = reader.read(0);
                        } catch (Exception e) {
                            break;
                        }

                        if (image == null) {
                            break;
                        }

                        updateImage(image);

                        long bytesRead = imgStream.getStreamPosition();

                        stream.reset();
                        stream.skip(bytesRead);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    init(null);
                }
            }
        }).start();


    }

    public static MediaServer getInstance() {
        return instance;
    }

    public void init(Client client) {
        if (onRecord || client == null) {
            stage.hide();
            onRecord = false;
            closeSocket();
        } else {
            stage.setWidth(client.getScreenWidth());
            stage.setHeight(client.getScreenHeight());
            stage.setResizable(false);
            stage.show();
            onRecord = true;
            Server.getInstance().send("3");
        }

    }

    private void closeSocket() {
        try {
            if (currentSocket != null)
                currentSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateImage(BufferedImage bf) {
        if (bf != null)
            container.setBackground(new Background(new BackgroundImage(SwingFXUtils.toFXImage(bf, null), null, null, null, null)));
    }

}
