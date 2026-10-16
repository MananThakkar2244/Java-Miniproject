package miniproject;

import java.sql.Connection;
import java.sql.DriverManager;

class DBoperations {
    public void connectDB() {
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/collecttrack_demo",
                    "root",
                    "");
            System.out.println("DB connected");
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
