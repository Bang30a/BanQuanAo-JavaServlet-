package context;
import java.sql.Connection;
import java.sql.DriverManager;

public class DBContext {
    private final String userID = "sa";
    private final String password = "123456";
    private final String instance = ""; // hoặc để null
    private final String portNumber = "1433";
    private final String dbName = "shopduck";
    private final String serverName = "BANGGG";
    
    public Connection getConnection() throws Exception {
        String url;
                if (instance == null || instance.trim().isEmpty()) {
            url = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";databaseName=" + dbName;
        } else {
            url = "jdbc:sqlserver://" + serverName + ":" + portNumber + "\\" + instance + ";databaseName=" + dbName;
        }
        
        url += ";encrypt=false;trustServerCertificate=true;loginTimeout=30";
        
        System.out.println("Connection URL: " + url); // Debug log
        
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        return DriverManager.getConnection(url, userID, password);
    }
 
    public static void main(String[] args) {
        try {
            System.out.println("Testing database connection...");
            Connection conn = new DBContext().getConnection();
            if (conn != null) {
                System.out.println("✅ Kết nối thành công!");
                
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Orders");
                if (rs.next()) {
                    System.out.println("✅ Bảng Orders có " + rs.getInt(1) + " records");
                }
                
                conn.close(); 
            }
        } catch (Exception e) {
            System.out.println("❌ Kết nối thất bại: " + e.getMessage());
            e.printStackTrace();
        }
    }
}