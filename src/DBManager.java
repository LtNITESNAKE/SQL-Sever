
import java.sql.*;
import com.microsoft.sqlserver.jdbc.*;

public class DBManager {
    Connection con = null; // build connection
    Statement stmt = null; // execute query



    public DBManager() throws SQLException {
        DriverManager.registerDriver(new SQLServerDriver());
        // initialization of connection class object
        String url = "jdbc:sqlserver://DESKTOP-DVPP6KP\\SQLEXPRESS;databaseName=SQLErrors;integratedSecurity=true;encrypt=false;";
        con = DriverManager.getConnection(url);
        stmt = con.createStatement();
        System.out.println("Connection established");
    }

    public void insertUpdateDelete(String query) throws SqlException {
        try {
            stmt.execute(query);
        } catch (SQLException e) {
            logError(query, e.getMessage());
            throw new SqlException("Error executing query: " + query, e);
        }
    }

    public ResultSet select(String query) throws SqlException {
        try {
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            logError(query, e.getMessage());
            throw new SqlException("Error executing query: " + query, e);
        }
    }

    public void logError(String query, String errorMessage) {
        String logQuery = "INSERT INTO ErrorLogs (query, error_message, timestamp) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(logQuery)) {
            pstmt.setString(1, query);
            pstmt.setString(2, errorMessage);
            pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error done!");
        }
    }

    public void close() {
        try {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
