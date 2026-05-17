import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class TestDb {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/custify", "root", "");
            Statement stmt = conn.createStatement();
            stmt.execute("SET FOREIGN_KEY_CHECKS=0");
            stmt.execute("DROP TABLE IF EXISTS reunion");
            stmt.execute("SET FOREIGN_KEY_CHECKS=1");
            System.out.println("Table dropped successfully!");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
