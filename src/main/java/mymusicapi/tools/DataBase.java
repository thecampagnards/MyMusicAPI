package mymusicapi.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DataBase {

    private DataBase()
    {}

    private static Connection connection = null;

    public static Connection getConnection() throws ClassNotFoundException, SQLException, IllegalAccessException, InstantiationException {
        if (connection == null){
            ResourceBundle rb = ResourceBundle.getBundle("main");
            String connectionURL = "jdbc:mysql://"+ rb.getString("dbhost") +":"+ rb.getString("dbport") +"/"+ rb.getString("dbname");
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(connectionURL, rb.getString("dbuser"), rb.getString("dbpassword"));
        }
        return connection;
    }
}
