import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AllScoreHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpEx) throws IOException {
       try {
            byte[] response;
            final Connection dbConect = DBManager.INSTANCE.dbConect;
            final PreparedStatement pStmt = dbConect.prepareStatement("SELECT name,points FROM users");// TODO: make DataBase table
            final ResultSet results = pStmt.executeQuery();
            results.first();
            JSONObject scores = new JSONObject();
            for (results.first(); !results.isAfterLast(); results.next()){
                scores.put(results.getString("name"),results.getInt("points"));
            }
            response = scores.toString().getBytes();
            httpEx.sendResponseHeaders(200, response.length);
            httpEx.getResponseBody().write(response);
        }catch ( JSONException | SQLException e){
           e.printStackTrace();
       }
    }
}
