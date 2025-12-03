package dao;

import context.DBContext;
import entity.OrderDetails;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDao {

    private Connection sharedConn; // dùng cho Integration Test

    // Constructor mặc định (production)
    public OrderDetailDao() {}

    // Constructor test (dùng 1 connection chung)
    public OrderDetailDao(Connection conn) {
        this.sharedConn = conn;
    }

    /**
     * Chỉ dùng khi gọi lẻ (không thuộc transaction).
     * Transaction thật sự phải truyền conn từ OrderService.
     */
    private Connection getConnNonTx() throws Exception {
        if (sharedConn != null) return sharedConn;
        return new DBContext().getConnection();
    }

    // =================================================================
    // ========== TRANSACTION MODE (OrderService truyền conn) ==========
    // =================================================================

    public boolean addDetail(Connection conn, OrderDetails detail) throws Exception {
        String sql = "INSERT INTO OrderDetails (order_id, product_variant_id, quantity, price) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detail.getOrderId());
            stmt.setInt(2, detail.getProductVariantId());
            stmt.setInt(3, detail.getQuantity());
            stmt.setDouble(4, detail.getPrice());
            return stmt.executeUpdate() > 0;
        }
        // không catch lỗi -> để OrderService rollback
    }

    public void deleteDetailsByOrderId(Connection conn, int orderId) throws Exception {
        String sql = "DELETE FROM OrderDetails WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
        // không catch lỗi -> để OrderService rollback
    }

    // =================================================================
    // ========== NON-TRANSACTION MODE (gọi lẻ) ========================
    // =================================================================

    public void addDetail(OrderDetails detail) {
        String sql = "INSERT INTO OrderDetails (order_id, product_variant_id, quantity, price) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = getConnNonTx();
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, detail.getOrderId());
            stmt.setInt(2, detail.getProductVariantId());
            stmt.setInt(3, detail.getQuantity());
            stmt.setDouble(4, detail.getPrice());

            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sharedConn == null) { // chỉ đóng khi không phải test
                try { if (stmt != null) stmt.close(); } catch (Exception ignored) {}
                try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            }
        }
    }

    private OrderDetails extractDetail(ResultSet rs) throws SQLException {
        OrderDetails detail = new OrderDetails(
                rs.getInt("id"),
                rs.getInt("order_id"),
                rs.getInt("product_variant_id"),
                rs.getInt("quantity"),
                rs.getDouble("price")
        );

        try {
            rs.findColumn("product_name");
            detail.setProductName(rs.getString("product_name"));
        } catch (SQLException ignore) {}

        return detail;
    }

    public List<OrderDetails> getDetailsByOrderId(int orderId) {
        List<OrderDetails> list = new ArrayList<>();

        String sql =
                "SELECT od.*, p.name AS product_name " +
                "FROM OrderDetails od " +
                "JOIN ProductVariants pv ON od.product_variant_id = pv.id " +
                "JOIN Products p ON pv.product_id = p.id " +
                "WHERE od.order_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnNonTx();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, orderId);
            rs = ps.executeQuery();

            while (rs.next()) list.add(extractDetail(rs));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sharedConn == null) {
                try { if (rs != null) rs.close(); } catch (Exception ignored) {}
                try { if (ps != null) ps.close(); } catch (Exception ignored) {}
                try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            }
        }

        return list;
    }

    public OrderDetails getDetailById(int id) {

        String sql =
                "SELECT od.*, p.name AS product_name " +
                "FROM OrderDetails od " +
                "JOIN ProductVariants pv ON od.product_variant_id = pv.id " +
                "JOIN Products p ON pv.product_id = p.id " +
                "WHERE od.id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnNonTx();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) return extractDetail(rs);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sharedConn == null) {
                try { if (rs != null) rs.close(); } catch (Exception ignored) {}
                try { if (ps != null) ps.close(); } catch (Exception ignored) {}
                try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            }
        }

        return null;
    }

    public void updateDetail(OrderDetails detail) {
        String sql =
                "UPDATE OrderDetails SET order_id = ?, product_variant_id = ?, quantity = ?, price = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnNonTx();
            ps = conn.prepareStatement(sql);

            ps.setInt(1, detail.getOrderId());
            ps.setInt(2, detail.getProductVariantId());
            ps.setInt(3, detail.getQuantity());
            ps.setDouble(4, detail.getPrice());
            ps.setInt(5, detail.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sharedConn == null) {
                try { if (ps != null) ps.close(); } catch (Exception ignored) {}
                try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            }
        }
    }

    public void deleteDetail(int id) {
        String sql = "DELETE FROM OrderDetails WHERE id = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnNonTx();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sharedConn == null) {
                try { if (ps != null) ps.close(); } catch (Exception ignored) {}
                try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            }
        }
    }
}
