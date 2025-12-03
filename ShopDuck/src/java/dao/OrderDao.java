package dao;

import context.DBContext;
import entity.Orders;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    // Nếu mockConn != null => đang chạy Integration Test
    private Connection mockConn;

    public OrderDao() {}

    public OrderDao(Connection conn) {
        this.mockConn = conn;
    }

    protected Connection getConnection() throws Exception {
        if (mockConn != null) return mockConn;
        return new DBContext().getConnection();
    }

    public Connection getMockConnection() {
        return this.mockConn;
    }

    // Giữ lại để tránh lỗi tương thích
    public Connection getInjectedConnection() {
        return this.mockConn;
    }

    // Đóng stmt, rs, nhưng KHÔNG đóng connection test
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (Exception ignored) {}
        try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}

        // chỉ đóng connection thật (web), không đóng connection test
        if (mockConn == null && conn != null) {
            try { conn.close(); } catch (Exception ignored) {}
        }
    }

    private Orders extractOrder(ResultSet rs) throws SQLException {
        return new Orders(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getTimestamp("order_date"),
            rs.getDouble("total"),
            rs.getString("address"),
            rs.getString("phone"),
            rs.getString("status")
        );
    }

    // ================= SELECT =================

    public List<Orders> getAllOrders() {
        List<Orders> ordersList = new ArrayList<>();
        String sql = "SELECT * FROM Orders";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) ordersList.add(extractOrder(rs));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }

        return ordersList;
    }

    public List<Orders> getOrdersByStatus(String status) {
        List<Orders> list = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE TRIM(status) = TRIM(?)";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            rs = ps.executeQuery();
            while (rs.next()) list.add(extractOrder(rs));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }

        return list;
    }

    // =============== INSERT (transaction mode) ===============

    public int addOrder(Connection conn, Orders order) throws Exception {
        String sql = "INSERT INTO Orders (user_id, order_date, total, address, phone, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getUserId());
            stmt.setTimestamp(2, order.getOrderDate());
            stmt.setDouble(3, order.getTotal());
            stmt.setString(4, order.getAddress());
            stmt.setString(5, order.getPhone());
            stmt.setString(6, order.getStatus());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    // =============== INSERT (web mode) ===============

    public int addOrder(Orders order) {
        Connection conn = null;

        try {
            conn = getConnection();
            return addOrder(conn, order);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, null, null);
        }

        return -1;
    }

    // ================= UPDATE =================

    public void updateOrder(Orders order) {
        String sql = "UPDATE Orders SET user_id = ?, order_date = ?, total = ?, address = ?, phone = ?, status = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);

            ps.setInt(1, order.getUserId());
            ps.setTimestamp(2, order.getOrderDate());
            ps.setDouble(3, order.getTotal());
            ps.setString(4, order.getAddress());
            ps.setString(5, order.getPhone());
            ps.setString(6, order.getStatus());
            ps.setInt(7, order.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }
    }

    // ================= DELETE =================

    public void deleteOrder(int id) {
        String sql = "DELETE FROM Orders WHERE id = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }
    }

    // ================= GET BY ID =================

    public Orders getOrderById(int id) {
        String sql = "SELECT * FROM Orders WHERE id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) return extractOrder(rs);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }

        return null;
    }

    public List<Orders> getOrdersByUserId(int userId) throws Exception {
        List<Orders> list = new ArrayList<>();

        String sql = "SELECT * FROM Orders WHERE user_id = ? ORDER BY order_date DESC";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);

            ps.setInt(1, userId);
            rs = ps.executeQuery();

            while (rs.next()) list.add(extractOrder(rs));

        } finally {
            closeResources(conn, ps, rs);
        }

        return list;
    }

    public Orders getOrderByIdAndUserId(int orderId, int userId) throws Exception {
        String sql = "SELECT * FROM Orders WHERE id = ? AND user_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);

            ps.setInt(1, orderId);
            ps.setInt(2, userId);

            rs = ps.executeQuery();

            if (rs.next()) return extractOrder(rs);

        } finally {
            closeResources(conn, ps, rs);
        }

        return null;
    }
}
