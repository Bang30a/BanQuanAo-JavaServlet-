package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import context.DBContext;

public class DashboardDao extends DBContext {

    // Tổng doanh thu
    public double getTotalRevenue() {
        double total = 0;
        String sql = "SELECT SUM(od.price * od.quantity) " +
             "FROM Orders o " +
             "JOIN OrderDetails od ON o.id = od.order_id " +
             "WHERE o.status = N'Đã giao'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    // Tổng số đơn hàng
    public int getTotalOrders() {
    String sql = "SELECT COUNT(*) FROM Orders WHERE status = N'Đã giao'";
        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Sản phẩm bán chạy (Top N)
    public List<Map<String, Object>> getTopSellingProducts(int top) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT TOP " + top + " p.name, SUM(od.quantity) AS total_sold " +
             "FROM OrderDetails od " +
             "JOIN Orders o ON od.order_id = o.id " +
             "JOIN ProductVariants pv ON od.product_variant_id = pv.id " +
             "JOIN Products p ON pv.product_id = p.id " +
             "WHERE o.status = N'Đã giao' " +
             "GROUP BY p.name " +
             "ORDER BY total_sold DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("name", rs.getString("name"));
                row.put("total_sold", rs.getInt("total_sold"));
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Tổng số khách hàng
    public int getTotalUsers() {
        String sql = "SELECT COUNT(*) FROM Users WHERE role = 'user'";
        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
