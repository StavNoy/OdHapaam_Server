import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) {
        try {
            HttpServer s = HttpServer.create(new InetSocketAddress(9999),0);
            s.createContext("/login",new UserHandler(UserHandler.LOGIN));
            s.createContext("/save",new UserHandler(UserHandler.SAVE));
            s.createContext("/signup",new UserHandler(UserHandler.SIGNUP));
            s.createContext("/highscore",new AllScoreHandler());
            s.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
