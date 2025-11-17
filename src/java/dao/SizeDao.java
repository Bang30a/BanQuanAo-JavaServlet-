package dao;

import context.DBContext;
import entity.Size;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SizeDao {

    private Connection externalConn; // Connection được inject từ test

    // Constructor mặc định
    public SizeDao() {}

    // Constructor cho test
    public SizeDao(Connection conn) {
        this.externalConn = conn;
    }

    // Lấy connection: dùng external nếu test inject
    protected Connection getConnection() throws Exception {
        if (externalConn != null) return externalConn;
        return new DBContext().getConnection();
    }

    // Đóng tài nguyên an toàn
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
        try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}

        // Chỉ đóng connection nếu không phải external
        if (externalConn == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    // Map dữ liệu từ ResultSet
    private Size extractSize(ResultSet rs) throws SQLException {
        return new Size(
            rs.getInt("id"),
            rs.getString("size_label")
        );
    }

    // Lấy tất cả size
    public List<Size> getAllSizes() {
        List<Size> list = new ArrayList<>();
        String sql = "SELECT * FROM Sizes";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(extractSize(rs));
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách size:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    // Lấy size theo ID
    public Size getSizeById(int id) {
        String sql = "SELECT * FROM Sizes WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) return extractSize(rs);

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy size theo ID:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    // Thêm size
    public boolean insertSize(Size size) {
        String sql = "INSERT INTO Sizes (size_label) VALUES (?)";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, size.getSizeLabel());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi thêm size:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }

    // Cập nhật size
    public boolean updateSize(Size size) {
        String sql = "UPDATE Sizes SET size_label = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, size.getSizeLabel());
            ps.setInt(2, size.getId());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi cập nhật size:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }

    // Xóa size
    public boolean deleteSize(int id) {
        String sql = "DELETE FROM Sizes WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xoá size:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }
        return false;
    }
}
