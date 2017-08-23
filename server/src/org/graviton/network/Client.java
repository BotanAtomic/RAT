package org.graviton.network;

import org.apache.mina.core.session.IoSession;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Client {
    private final IoSession session;

    private Map<String, String> params = new HashMap<>();

    Client(IoSession session) {
        this.session = session;
    }

    void parse(String data) {
        switch (data.charAt(0)) {
            case '0':
                Stream.of(data.substring(1).split(";")).forEach(info -> params.put(info.split(":")[0], info.split(":")[1]));
                break;
            case 'p':
                System.out.print(data.substring(1));
                break;
        }
    }


    public IoSession getSession() {
        return session;
    }

    public Map<String, String> params() {
        return this.params;
    }

    public String address() {
        return ((InetSocketAddress) session.getRemoteAddress()).getAddress().getHostAddress();
    }

    public int getScreenWidth() {
        return (int) Double.parseDouble(params.get("screen_width"));
    }

    public int getScreenHeight() {
        return (int) Double.parseDouble(params.get("screen_height"));
    }
}
