package dao;

import context.DBContext;
import entity.Products;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    private Connection mockConn; // dùng cho integration test

    public ProductDao() {}

    public ProductDao(Connection conn) {
        this.mockConn = conn;
    }

    /** Hỗ trợ trích xuất Product từ ResultSet */
    private Products extractProduct(ResultSet rs) throws Exception {
        Products p = new Products(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("price"),
            rs.getString("image")
        );

        // H2 đôi khi không có category_id
        try {
            rs.findColumn("category_id");
            p.setCategoryId(rs.getInt("category_id"));
        } catch (SQLException ignored) {}

        return p;
    }

    /** Lấy connection — nếu test truyền mockConn thì KHÔNG tự đóng */
    protected Connection getConnection() throws Exception {
        if (mockConn != null) return mockConn;
        return new DBContext().getConnection();
    }

    /** Đóng tài nguyên — KHÔNG đóng connection nếu là mockConn */
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
        try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}

        // Chỉ đóng nếu không phải connection test
        if (mockConn == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    // ========================= CRUD ==============================

    public List<Products> getAllProducts() throws Exception {
        List<Products> list = new ArrayList<>();
        String query = "SELECT * FROM Products";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) list.add(extractProduct(rs));
        } finally {
            closeResources(conn, ps, rs);
        }
        return list;
    }

    public Products findById(int id) {
        String query = "SELECT * FROM Products WHERE id = ?";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) return extractProduct(rs);
        } catch (Exception e) {
            System.out.println("❌ Find error: " + e);
        } finally {
            closeResources(conn, ps, rs);
        }

        return null;
    }

    // [ĐÃ SỬA] Dùng LOWER() để tìm không phân biệt hoa thường
    public List<Products> getProductsByKeyword(String keyword) {
        List<Products> list = new ArrayList<>();
        // Sửa câu query thêm hàm LOWER
        String query = "SELECT * FROM Products WHERE LOWER(name) LIKE LOWER(?)";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, '%' + keyword + '%');
            rs = ps.executeQuery();
            while (rs.next()) list.add(extractProduct(rs));
        } catch (Exception e) {
            System.out.println("❌ Keyword search error: " + e);
        } finally {
            closeResources(conn, ps, rs);
        }

        return list;
    }

    public List<Products> searchByKeyword(String keyword) {
        List<Products> list = new ArrayList<>();
        // Sửa câu query dùng LOWER(...) LIKE LOWER(?)
        String sql = "SELECT * FROM Products WHERE LOWER(name) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            rs = ps.executeQuery();
            while (rs.next()) list.add(extractProduct(rs));
        } catch (Exception e) {
            System.out.println("❌ Search error: " + e);
        } finally {
            closeResources(conn, ps, rs);
        }

        return list;
    }

    public int insert(Products p) {
        String query = "INSERT INTO Products (name, description, price, image) VALUES (?, ?, ?, ?)";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setString(4, p.getImage());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) p.setId(rs.getInt(1));
            }

            return rows;
        } catch (Exception e) {
            System.out.println("❌ Insert error: " + e);
            return -1;
        } finally {
            closeResources(conn, ps, rs);
        }
    }

    public int update(Products p) {
        String query = "UPDATE Products SET name = ?, description = ?, price = ?, image = ? WHERE id = ?";
        Connection conn = null; PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setString(4, p.getImage());
            ps.setInt(5, p.getId());
            return ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("❌ Update error: " + e);
            return -1;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public int delete(int id) {
        String query = "DELETE FROM Products WHERE id = ?";
        Connection conn = null; PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {

            // SQL Server violation FK = 547
            if (e.getErrorCode() == 547) {
                System.out.println("❌ Không thể xóa vì khóa ngoại ở ProductVariant.");
                return 0; // Xóa thất bại nhưng không crash
            }

            System.out.println("❌ Delete SQL error: " + e.getMessage());
            return -1;

        } catch (Exception e) {
            System.out.println("❌ Delete error: " + e);
            return -1;
        } finally {
            closeResources(conn, ps, null);
        }
    }

    public List<Products> findByCategoryId(int categoryId, int excludeId) {
        List<Products> list = new ArrayList<>();
        String sql = "SELECT * FROM Products WHERE category_id = ? AND id <> ?";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, categoryId);
            ps.setInt(2, excludeId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(extractProduct(rs));

        } catch (Exception e) {
            System.out.println("❌ findByCategoryId error: " + e);
        } finally {
            closeResources(conn, ps, rs);
        }

        return list;
    }
}