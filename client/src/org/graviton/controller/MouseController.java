package org.graviton.controller;

import org.graviton.network.Connector;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class MouseController {

    private static final AtomicBoolean locked = new AtomicBoolean(false);
    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (Exception e) {
            Connector.send("e" + e.getMessage());
        }
    }

    public static void switchLock(boolean lock) {
        if (lock) {
            locked.set(true);
            new Thread(() -> {
                while (locked.get())
                    robot.mouseMove(0, 0);
            }).start();
        } else {
            locked.set(false);
        }
    }

    public static void move(int x, int y) {
        robot.mouseMove(x, y);
    }

    public static void click(boolean left) {
        robot.mousePress(left ? InputEvent.BUTTON1_MASK : InputEvent.BUTTON3_MASK);
        robot.mouseRelease(left ? InputEvent.BUTTON1_MASK : InputEvent.BUTTON3_MASK);
    }

}
