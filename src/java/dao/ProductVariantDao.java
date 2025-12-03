package dao;

import context.DBContext;
import entity.ProductVariants;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantDao {

    private Connection externalConn; // Connection được inject từ test

    // Constructor mặc định
    public ProductVariantDao() {}

    // Constructor cho test
    public ProductVariantDao(Connection conn) {
        this.externalConn = conn;
    }

    // Lấy Connection: dùng external nếu test inject
    protected Connection getConnection() throws Exception {
        if (externalConn != null) return externalConn;
        return new DBContext().getConnection();
    }

    // Đóng tài nguyên an toàn
    private void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try { if (rs != null) rs.close(); } catch (SQLException ignored) {}
        try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}

        // Chỉ đóng conn nếu nó KHÔNG phải external
        if (externalConn == null && conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    // Map dữ liệu từ ResultSet
    private ProductVariants extractVariant(ResultSet rs) throws SQLException {
        ProductVariants v = new ProductVariants();
        v.setId(rs.getInt("id"));
        v.setProductId(rs.getInt("product_id"));
        v.setSizeId(rs.getInt("size_id"));
        v.setStock(rs.getInt("stock"));
        // Check if columns exist before getting them to avoid errors in simple select queries
        try { v.setProductName(rs.getString("productName")); } catch (SQLException e) {}
        try { v.setSizeName(rs.getString("sizeName")); } catch (SQLException e) {}
        try { v.setPrice(rs.getDouble("price")); } catch (SQLException e) {}
        return v;
    }

    // [MỚI] Kiểm tra biến thể tồn tại (dùng cho logic cộng dồn)
    public ProductVariants checkExist(int productId, int sizeId) {
        String sql = "SELECT * FROM ProductVariants WHERE product_id = ? AND size_id = ?";
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, productId);
            ps.setInt(2, sizeId);
            rs = ps.executeQuery();
            
            if (rs.next()) {
                // Chỉ cần lấy thông tin cơ bản để biết ID và Stock cũ
                ProductVariants v = new ProductVariants();
                v.setId(rs.getInt("id"));
                v.setProductId(rs.getInt("product_id"));
                v.setSizeId(rs.getInt("size_id"));
                v.setStock(rs.getInt("stock"));
                return v;
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi check exist:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }
        return null;
    }

    // Lấy tất cả biến thể
    public List<ProductVariants> getAllVariants() {
        List<ProductVariants> list = new ArrayList<>();

        String sql =
            "SELECT pv.id, pv.product_id, pv.size_id, pv.stock, " +
            "p.name AS productName, p.price AS price, s.size_label AS sizeName " +
            "FROM ProductVariants pv " +
            "JOIN Products p ON pv.product_id = p.id " +
            "JOIN Sizes s ON pv.size_id = s.id";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(extractVariant(rs));
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi lấy danh sách biến thể:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }

        return list;
    }

    // Tìm theo ID
    public ProductVariants findById(int id) {
        String sql =
            "SELECT pv.id, pv.product_id, pv.size_id, pv.stock, " +
            "p.name AS productName, p.price AS price, s.size_label AS sizeName " +
            "FROM ProductVariants pv " +
            "JOIN Products p ON pv.product_id = p.id " +
            "JOIN Sizes s ON pv.size_id = s.id " +
            "WHERE pv.id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) return extractVariant(rs);

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tìm biến thể theo ID:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }

        return null;
    }

    // Tìm theo product_id
    public List<ProductVariants> findByProductId(int productId) {
        List<ProductVariants> list = new ArrayList<>();

        String sql =
            "SELECT pv.id, pv.product_id, pv.size_id, pv.stock, " +
            "p.name AS productName, p.price AS price, s.size_label AS sizeName " +
            "FROM ProductVariants pv " +
            "JOIN Products p ON pv.product_id = p.id " +
            "JOIN Sizes s ON pv.size_id = s.id " +
            "WHERE pv.product_id = ?";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, productId);
            rs = ps.executeQuery();

            while (rs.next()) {
                list.add(extractVariant(rs));
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi tìm biến thể theo product_id:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, rs);
        }

        return list;
    }

    // Thêm biến thể
    public boolean insertVariant(ProductVariants variant) {
        String sql =
            "INSERT INTO ProductVariants (product_id, size_id, stock) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);

            ps.setInt(1, variant.getProductId());
            ps.setInt(2, variant.getSizeId());
            ps.setInt(3, variant.getStock());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi thêm biến thể:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }

        return false;
    }

    // Cập nhật biến thể
    public boolean updateVariant(ProductVariants variant) {
        String sql =
            "UPDATE ProductVariants SET product_id = ?, size_id = ?, stock = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);

            ps.setInt(1, variant.getProductId());
            ps.setInt(2, variant.getSizeId());
            ps.setInt(3, variant.getStock());
            ps.setInt(4, variant.getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi cập nhật biến thể:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }

        return false;
    }

    // Xoá biến thể
    public boolean deleteVariant(int id) {
        String sql = "DELETE FROM ProductVariants WHERE id = ?";

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi xoá biến thể:");
            e.printStackTrace();
        } finally {
            closeResources(conn, ps, null);
        }

        return false;
    }
}