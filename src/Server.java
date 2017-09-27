import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) {
        try {
            HttpServer s = HttpServer.create(new InetSocketAddress(9999),0);
            s.createContext("/login",new UserHandler(true));
            s.createContext("/save",new UserHandler(false));
            s.createContext("/highscore",new AllScoreHandler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
