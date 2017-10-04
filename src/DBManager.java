import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum DBManager {
    INSTANCE;

    // TODO: configure
    private final String LOCALHOST_IP = "127.0.0.1";
    private final String URL = "jdbc:mysql://"+ LOCALHOST_IP +"3306/";
    private final String DB_NAME = "rasheytevot";
    public static final String UNAME = "root";
    public static final String UPASS ="";
    public Connection dbConnect;

    DBManager(){
        try {
            this.dbConnect = DriverManager.getConnection(URL+DB_NAME, UNAME, UPASS);
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }
}
