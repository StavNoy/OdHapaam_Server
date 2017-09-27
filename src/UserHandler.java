import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserHandler implements HttpHandler {
    private Connection dbConect;
    private int status = 400; //Default BAD REQUEST
    private byte[] response;
    private final Boolean login;

    public UserHandler(final Boolean login) {
        this.login = login;
    }

    @Override
    public void handle(final HttpExchange httpEx) throws IOException {
        try {
            dbConect = DBManager.INSTANCE.dbConect;
            final JSONObject recievedUser = recievedUser(httpEx);
            final JSONObject uPoints;
            if (login) {
                uPoints = foundUser(recievedUser);
            } else {
                uPoints = savePoints(recievedUser);
            }
            //If not exception thrown by now, request is not attack
            status = 200;
            //If correct credentials
            if(uPoints != null){
                response = uPoints.toString().getBytes();
            } else {
                response = new JSONObject().put("error", "wrong credentials").toString().getBytes();
            }
        } catch (SQLException | JSONException e) {
            e.printStackTrace();
        }
        httpEx.sendResponseHeaders(status, response.length);
        httpEx.getResponseBody().write(response);
    }

    JSONObject foundUser(final JSONObject recievedUser) throws JSONException, SQLException {
        final String NAME = recievedUser.getString("name"), PASS = recievedUser.getString("pass");
        //TODO - shield from SQL injections
        if (validName(NAME) && validPass(PASS)) {
            PreparedStatement pStmt = dbConect.prepareStatement("SELECT points FROM users WHERE name = '"+NAME+"' AND pass = '"+PASS+"' LIMIT 1");// TODO: make DataBase table
            ResultSet results = pStmt.executeQuery();
            results.first();
            if (results.getRow()==1){
                return new JSONObject().put("points",results.getInt("points"));
            }
        }
        return null;
    }

    private JSONObject savePoints(final JSONObject recievedUser) throws JSONException, SQLException {
        final String NAME = recievedUser.getString("name"), PASS = recievedUser.getString("pass");
        final int POINTS = recievedUser.getInt("points");
        //TODO - shield from SQL injections
        if (validName(NAME) && validPass(PASS)) {
            PreparedStatement pStmt = dbConect.prepareStatement("SELECT id FROM users WHERE name = '"+NAME+"' AND pass = '"+PASS+"' LIMIT 1");// TODO: make DataBase table
            ResultSet results = pStmt.executeQuery();
            results.first();
            if (results.getRow()==1){
                final int ID = results.getInt("id");
                dbConect.prepareStatement("UPDATE users SET points = "+POINTS+" WHERE id = "+ID).executeUpdate();
                return new JSONObject().put("save",true);
            }
        }
        return null;
    }

    //todo add GET of all scores

    //todo separate request for user and for password
    private JSONObject recievedUser(HttpExchange httpEx) throws IOException, JSONException {
        return new JSONObject(new BufferedReader(new InputStreamReader(httpEx.getRequestBody())).readLine());
    }

    private boolean validPass(final String pswrd){
        return pswrd.matches("(?=.*[A-Z]+)(?=.*[a-z]+)(?=.*\\d+)^.{8,}$");
    }
    private boolean validName(final String name){//username must be at least 3 char long with no spaces
        return name.matches("^[^\\s]{3,}$");
    }
}
