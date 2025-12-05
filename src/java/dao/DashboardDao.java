package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import context.DBContext;

public class DashboardDao extends DBContext {

    // [MỚI] Biến để lưu connection từ Test truyền vào
    private Connection mockConn;

    // [MỚI] Hàm setter để Test gọi
    public void setConnection(Connection conn) {
        this.mockConn = conn;
    }

    // [MỚI] Override hàm lấy connection
    // Nếu có mockConn (đang test) thì dùng nó, không thì mở mới (chạy thật)
    @Override
    public Connection getConnection() throws Exception {
        if (this.mockConn != null) return this.mockConn;
        return super.getConnection();
    }
    
    // [MỚI] Hàm đóng tài nguyên an toàn (Không đóng connection nếu là mock)
    private void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (Exception e) {}
        try { if (ps != null) ps.close(); } catch (Exception e) {}
        // Chỉ đóng connection nếu nó không phải là connection test
        if (mockConn == null && conn != null) {
            try { conn.close(); } catch (Exception e) {}
        }
    }

    // ==========================================================
    // 1. CÁC HÀM CHO TRANG DASHBOARD TỔNG QUAN (KHÔNG LỌC)
    // ==========================================================

    public double getTotalRevenue() {
        double total = 0;
        String sql = "SELECT SUM(od.price * od.quantity) " +
                     "FROM Orders o " +
                     "JOIN OrderDetails od ON o.id = od.order_id " +
                     "WHERE o.status = N'Đã giao'";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return total;
    }

    public int getTotalOrders() {
        String sql = "SELECT COUNT(*) FROM Orders WHERE status = N'Đã giao'";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return 0;
    }

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

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("name", rs.getString("name"));
                row.put("total_sold", rs.getInt("total_sold"));
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    public int getTotalUsers() {
        String sql = "SELECT COUNT(*) FROM Users WHERE role = 'user'";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return 0;
    }

    // ==========================================================
    // 2. CÁC HÀM CHO BIỂU ĐỒ DASHBOARD
    // ==========================================================

    public List<Map<String, Object>> getRevenueLast7Days() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT FORMAT(order_date, 'dd/MM') AS order_day, SUM(total) AS daily_revenue " +
                     "FROM Orders " +
                     "WHERE order_date >= DATEADD(DAY, -6, CAST(GETDATE() AS DATE)) " +
                     "AND status = N'Đã giao' " +
                     "GROUP BY FORMAT(order_date, 'dd/MM'), CAST(order_date AS DATE) " +
                     "ORDER BY CAST(order_date AS DATE)";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("date", rs.getString("order_day"));
                item.put("revenue", rs.getDouble("daily_revenue"));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    public List<Map<String, Object>> getOrderStatusStats() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT status, COUNT(*) AS count FROM Orders GROUP BY status";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("status", rs.getString("status"));
                item.put("count", rs.getInt("count"));
                list.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    // ==========================================================
    // 3. CÁC HÀM CHO TRANG BÁO CÁO CHI TIẾT
    // ==========================================================

    public double getRevenueByDate(String startDate, String endDate) {
        double total = 0;
        String sql = "SELECT SUM(od.price * od.quantity) " +
                     "FROM Orders o " +
                     "JOIN OrderDetails od ON o.id = od.order_id " +
                     "WHERE o.status = N'Đã giao' AND o.order_date BETWEEN ? AND ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, startDate + " 00:00:00"); 
            ps.setString(2, endDate + " 23:59:59");
            
            rs = ps.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return total;
    }

    public int getOrderCountByDate(String startDate, String endDate) {
        String sql = "SELECT COUNT(*) FROM Orders WHERE status = N'Đã giao' AND order_date BETWEEN ? AND ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, startDate + " 00:00:00");
            ps.setString(2, endDate + " 23:59:59");
            
            rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return 0;
    }

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

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, startDate + " 00:00:00");
            ps.setString(2, endDate + " 23:59:59");
            
            rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("name", rs.getString("name"));
                row.put("total_sold", rs.getInt("total_sold"));
                row.put("revenue", rs.getDouble("revenue"));
                list.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }
}