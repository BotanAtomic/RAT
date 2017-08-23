package org.graviton.core;

import org.graviton.build.Build;
import org.graviton.network.Connector;

public class Main {

    public static void main(String[] args) throws Exception {
        buildHeader();
        Connector.connect();
    }

    private static void buildHeader() {
        System.out.println("                 _____                     _  _                \n                / ____|                   (_)| |               \n               | |  __  _ __  __ _ __   __ _ | |_  ___   _ __  \n               | | |_ || '__|/ _` |\\ \\ / /| || __|/ _ \\ | '_ \\ \n               | |__| || |  | (_| | \\ V / | || |_| (_) || | | |\n                \\_____||_|   \\__,_|  \\_/  |_| \\__|\\___/ |_| |_|\n");
        System.out.println("Client build : " + Build.version + "\n");
    }

}
