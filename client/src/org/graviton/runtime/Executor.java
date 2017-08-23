package org.graviton.runtime;

import org.graviton.network.Connector;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Executor {

    public static void execute(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    Connector.send("p" + line + "\n");
                }
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            Connector.send("pException in execution of command {" + command + "} -> " + e.getMessage() + "\n");
        }
    }

}
