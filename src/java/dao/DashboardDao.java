package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import context.DBContext;

public class DashboardDao extends DBContext {

    // ==========================================================
    // 1. CÁC HÀM CHO TRANG DASHBOARD TỔNG QUAN (KHÔNG LỌC)
    // ==========================================================

    // Tổng doanh thu (Toàn thời gian)
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

    // Tổng số đơn hàng (Toàn thời gian)
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

    // Sản phẩm bán chạy (Top N - Toàn thời gian)
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

    // ==========================================================
    // 2. CÁC HÀM CHO BIỂU ĐỒ DASHBOARD (LINE CHART & PIE CHART)
    // ==========================================================

    /**
     * Lấy doanh thu 7 ngày gần nhất (để vẽ biểu đồ đường)
     */
    public List<Map<String, Object>> getRevenueLast7Days() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT FORMAT(order_date, 'dd/MM') AS order_day, SUM(total) AS daily_revenue " +
                     "FROM Orders " +
                     "WHERE order_date >= DATEADD(DAY, -6, CAST(GETDATE() AS DATE)) " +
                     "AND status = N'Đã giao' " +
                     "GROUP BY FORMAT(order_date, 'dd/MM'), CAST(order_date AS DATE) " +
                     "ORDER BY CAST(order_date AS DATE)";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("date", rs.getString("order_day"));
                item.put("revenue", rs.getDouble("daily_revenue"));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy thống kê trạng thái đơn hàng (để vẽ biểu đồ tròn)
     */
    public List<Map<String, Object>> getOrderStatusStats() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT status, COUNT(*) AS count FROM Orders GROUP BY status";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("status", rs.getString("status"));
                item.put("count", rs.getInt("count"));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // ==========================================================
    // 3. CÁC HÀM CHO TRANG BÁO CÁO CHI TIẾT (CÓ LỌC NGÀY)
    // ==========================================================

    public double getRevenueByDate(String startDate, String endDate) {
        double total = 0;
        String sql = "SELECT SUM(od.price * od.quantity) " +
                     "FROM Orders o " +
                     "JOIN OrderDetails od ON o.id = od.order_id " +
                     "WHERE o.status = N'Đã giao' AND o.order_date BETWEEN ? AND ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, startDate + " 00:00:00"); 
            ps.setString(2, endDate + " 23:59:59");
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    public int getOrderCountByDate(String startDate, String endDate) {
        String sql = "SELECT COUNT(*) FROM Orders WHERE status = N'Đã giao' AND order_date BETWEEN ? AND ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, startDate + " 00:00:00");
            ps.setString(2, endDate + " 23:59:59");
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    // [QUAN TRỌNG] Hàm này lấy dữ liệu bảng chi tiết cho trang Statistics
    public List<Map<String, Object>> getTopSellingProductsByDate(String startDate, String endDate) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT TOP 10 p.name, SUM(od.quantity) AS total_sold, SUM(od.quantity * od.price) as revenue " +
                     "FROM OrderDetails od " +
                     "JOIN Orders o ON od.order_id = o.id " +
                     "JOIN ProductVariants pv ON od.product_variant_id = pv.id " +
                     "JOIN Products p ON pv.product_id = p.id " +
                     "WHERE o.status = N'Đã giao' AND o.order_date BETWEEN ? AND ? " +
                     "GROUP BY p.name " +
                     "ORDER BY total_sold DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, startDate + " 00:00:00");
            ps.setString(2, endDate + " 23:59:59");
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("name", rs.getString("name"));
                row.put("total_sold", rs.getInt("total_sold"));
                row.put("revenue", rs.getDouble("revenue"));
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}