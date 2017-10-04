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
    public static final int LOGIN = 0, SAVE = 1, SIGNUP = 2;
    private final int ACTION;

    public UserHandler(final int action) {
        this.ACTION = action;
    }

    @Override
    public void handle(final HttpExchange httpEx) throws IOException {
        try {
            dbConect = DBManager.INSTANCE.dbConnect;
            final JSONObject receivedUser = recievedUser(httpEx);
            final JSONObject uPoints;
            if (ACTION == LOGIN) {
                uPoints = foundUser(receivedUser);
            } else if (ACTION == SAVE ^ ACTION == SIGNUP) {
                uPoints = upload(receivedUser);
            } else {
                uPoints = null;
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

    JSONObject foundUser(final JSONObject receivedUser) throws JSONException, SQLException {
        final String NAME = receivedUser.getString("name"), PASS = receivedUser.getString("pass");
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

    private JSONObject upload(final JSONObject receivedUser) throws JSONException, SQLException {
        final String NAME = receivedUser.getString("name"), PASS = receivedUser.getString("pass");
        final int POINTS = receivedUser.getInt("points");
        //TODO - shield from SQL injections
        if (validName(NAME) && validPass(PASS)) {
            PreparedStatement pStmt;
            if (ACTION == SAVE) {
                pStmt = dbConect.prepareStatement("SELECT id FROM users WHERE name = '"+NAME+"' AND pass = '"+PASS+"' LIMIT 1");
            } else {
                pStmt = dbConect.prepareStatement("INSERT INTO `users` (`name`,`pass`,`points`) VALUES ("+NAME+","+PASS+","+POINTS+");");

            }
            ResultSet results = pStmt.executeQuery();
            results.first();
            if (results.getRow()==1){
                if (ACTION == SAVE) {
                    final int ID = results.getInt("id");
                    dbConect.prepareStatement("UPDATE users SET points = " + POINTS + " WHERE id = " + ID).executeUpdate();
                }
                return new JSONObject().put("upload",true);
            }
        }
        return null;
    }


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
