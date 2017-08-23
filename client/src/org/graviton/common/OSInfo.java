package org.graviton.common;


import sun.awt.OSInfo.OSType;

import java.awt.*;

public class OSInfo {

    public static String buildUserInfo(OSType osType) {
        return "OS name: " + osType.name() + ";" +
                "OS architecture: " + System.getProperty("os.arch") + ";" +
                "OS version: " + System.getProperty("os.version") + ";" +
                "username:" + System.getProperty("user.name") + ";" +
                "screen_height:" + Toolkit.getDefaultToolkit().getScreenSize().getHeight() + ";" +
                "screen_width:" + Toolkit.getDefaultToolkit().getScreenSize().getWidth() + ";" +
                "core:" + Runtime.getRuntime().availableProcessors();
    }


}
