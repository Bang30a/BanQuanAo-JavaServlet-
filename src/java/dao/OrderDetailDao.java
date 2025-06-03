package dao;

import context.DBContext;
import entity.OrderDetails;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDao {

    // ✅ Sửa hàm extractDetail để lấy cả product_name
    private OrderDetails extractDetail(ResultSet rs) throws SQLException {
        OrderDetails detail = new OrderDetails(
            rs.getInt("id"),
            rs.getInt("order_id"),
            rs.getInt("product_variant_id"),
            rs.getInt("quantity"),
            rs.getDouble("price")
        );

        // Gán thêm tên sản phẩm nếu có
        try {
            detail.setProductName(rs.getString("product_name"));
        } catch (SQLException ignore) {} // tránh lỗi nếu cột không có
        return detail;
    }

    // ✅ Sửa hàm getDetailsByOrderId để JOIN lấy tên sản phẩm
    public List<OrderDetails> getDetailsByOrderId(int orderId) {
        List<OrderDetails> list = new ArrayList<>();
        String sql = 
            "SELECT od.*, p.name AS product_name " +
            "FROM OrderDetails od " +
            "JOIN ProductVariants pv ON od.product_variant_id = pv.id " +
            "JOIN Products p ON pv.product_id = p.id " +
            "WHERE od.order_id = ?";

        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractDetail(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Các hàm còn lại giữ nguyên
    public boolean addDetail(Connection conn, OrderDetails detail) {
        String sql = "INSERT INTO OrderDetails (order_id, product_variant_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detail.getOrderId());
            stmt.setInt(2, detail.getProductVariantId());
            stmt.setInt(3, detail.getQuantity());
            stmt.setDouble(4, detail.getPrice());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addDetail(OrderDetails detail) {
        String sql = "INSERT INTO OrderDetails (order_id, product_variant_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, detail.getOrderId());
            stmt.setInt(2, detail.getProductVariantId());
            stmt.setInt(3, detail.getQuantity());
            stmt.setDouble(4, detail.getPrice());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateDetail(OrderDetails detail) {
        String sql = "UPDATE OrderDetails SET order_id = ?, product_variant_id = ?, quantity = ?, price = ? WHERE id = ?";
        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, detail.getOrderId());
            ps.setInt(2, detail.getProductVariantId());
            ps.setInt(3, detail.getQuantity());
            ps.setDouble(4, detail.getPrice());
            ps.setInt(5, detail.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteDetail(int id) {
        String sql = "DELETE FROM OrderDetails WHERE id = ?";
        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public OrderDetails getDetailById(int id) {
        String sql = 
            "SELECT od.*, p.name AS product_name " +
            "FROM OrderDetails od " +
            "JOIN ProductVariants pv ON od.product_variant_id = pv.id " +
            "JOIN Products p ON pv.product_id = p.id " +
            "WHERE od.id = ?";

        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractDetail(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
