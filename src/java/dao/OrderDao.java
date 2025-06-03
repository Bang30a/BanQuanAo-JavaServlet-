package dao;

import context.DBContext;
import entity.Orders;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    // Tạo đối tượng Orders từ ResultSet
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

    // Lấy toàn bộ đơn hàng
    public List<Orders> getAllOrders() {
        List<Orders> ordersList = new ArrayList<>();
        String sql = "SELECT * FROM Orders";

        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                ordersList.add(extractOrder(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ordersList;
    }

    // Lấy đơn hàng theo trạng thái
    public List<Orders> getOrdersByStatus(String status) {
        List<Orders> list = new ArrayList<>();
        String sql = "SELECT * FROM Orders WHERE status = ?";

        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setNString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractOrder(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Thêm đơn hàng (kèm kết nối truyền vào)
    public int addOrder(Connection conn, Orders order) {
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
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Thêm đơn hàng (tự tạo kết nối)
    public int addOrder(Orders order) {
        try (Connection conn = new DBContext().getConnection()) {
            return addOrder(conn, order);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Cập nhật đơn hàng
    public void updateOrder(Orders order) {
        String sql = "UPDATE Orders SET user_id = ?, order_date = ?, total = ?, address = ?, phone = ?, status = ? WHERE id = ?";

        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, order.getUserId());
            ps.setTimestamp(2, order.getOrderDate());
            ps.setDouble(3, order.getTotal());
            ps.setString(4, order.getAddress());
            ps.setString(5, order.getPhone());
            ps.setNString(6, order.getStatus());
            ps.setInt(7, order.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Xoá đơn hàng
    public void deleteOrder(int id) {
        String sql = "DELETE FROM Orders WHERE id = ?";

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

    // Lấy đơn hàng theo ID
    public Orders getOrderById(int id) {
        String sql = "SELECT * FROM Orders WHERE id = ?";

        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractOrder(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
