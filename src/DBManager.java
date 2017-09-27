import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum DBManager {
    INSTANCE;

    // TODO: configure
    private final String IP = "127.0.0.1";
    private final String URL = "jdbc:mysql://"+IP+"3306/";
    private final String DB_NAME = "RasheyTevot";
    public static final String UNAME = "root";
    public static final String UPASS ="";
    public Connection dbConect;

    DBManager(){
        try {
            this.dbConect = DriverManager.getConnection(URL+DB_NAME, UNAME, UPASS);
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }
}
