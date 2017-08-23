package org.graviton.core;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.graviton.build.Build;
import org.graviton.cmd.Shell;
import org.graviton.network.Server;
import org.graviton.network.media.MediaServer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;


public class Main extends Application {

    public static void main(String[] args) throws Exception {
        Application.launch(args);
    }

    private static void buildHeader() {
        System.out.println("                 _____                     _  _                \n                / ____|                   (_)| |               \n               | |  __  _ __  __ _ __   __ _ | |_  ___   _ __  \n               | | |_ || '__|/ _` |\\ \\ / /| || __|/ _ \\ | '_ \\ \n               | |__| || |  | (_| | \\ V / | || |_| (_) || | | |\n                \\_____||_|   \\__,_|  \\_/  |_| \\__|\\___/ |_| |_|\n");
        System.out.println("Server build : " + Build.version + "\n");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        buildHeader();
        Shell.start();
        new Server();
        new MediaServer(primaryStage);
    }
}
