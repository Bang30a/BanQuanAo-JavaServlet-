package dao;

import entity.Users;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import context.DBContext;

/**
 * UsersDao – hỗ trợ inject 1 connection duy nhất từ OrderService/test.
 * Không tự mở hoặc đóng connection trong mọi trường hợp.
 */
public class UsersDao {

    private Connection conn; // connection được inject từ ngoài

    // Constructor mặc định (production)
    public UsersDao() {}

    // Constructor cho test
    public UsersDao(Connection conn) {
        this.conn = conn;
    }

    /** 
     * Inject connection (Test sẽ dùng)
     */
    public void setConnection(Connection c) {
        this.conn = c;
    }

    /**
     * Lấy connection.
     * - Nếu test: dùng conn đã inject
     * - Nếu production: tự mở DBContext
     */
    private Connection getConnection() throws Exception {
        if (conn != null) return conn; // test mode
        return new DBContext().getConnection(); // production mode
    }

    /**
     * Không bao giờ đóng connection trong DAO.
     */
    private void close(Statement st, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (Exception ignored) {}
        try { if (st != null) st.close(); } catch (Exception ignored) {}
    }

    // ================================================================================
    // CÁC HÀM CRUD
    // ================================================================================

    public boolean insert(Users user) {
        String sql = "INSERT INTO Users (username, password, fullname, email, role) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullname());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            close(ps, null);
        }
    }

    public boolean register(Users user) {
        return insert(user);
    }

    public List<Users> getAllUsers() {
        List<Users> list = new ArrayList<>();
        String sql = "SELECT * FROM Users";
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) list.add(extract(rs));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(ps, rs);
        }
        return list;
    }

    public Users getUserById(int id) {
        String sql = "SELECT * FROM Users WHERE id = ?";
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) return extract(rs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(ps, rs);
        }
        return null;
    }

    public Users login(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) return extract(rs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(ps, rs);
        }
        return null;
    }

    public boolean updateUser(Users user) {
        String sql = "UPDATE Users SET username=?, password=?, fullname=?, email=?, role=? WHERE id=?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getFullname());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getRole());
            ps.setInt(6, user.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(ps, null);
        }
        return false;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM Users WHERE id=?";
        PreparedStatement ps = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(ps, null);
        }
        return false;
    }

    public boolean checkUserExists(String username) {
        String sql = "SELECT id FROM Users WHERE username = ?";
        PreparedStatement ps = null; ResultSet rs = null;
        try {
            ps = getConnection().prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(ps, rs);
        }
        return false;
    }

    // ================================================================================
    // Helper
    // ================================================================================
    private Users extract(ResultSet rs) throws Exception {
        return new Users(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("fullname"),
                rs.getString("email"),
                rs.getString("role")
        );
    }
}
