package dao;

import context.DBContext;
import entity.Products;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    // ✅ Hàm phụ để tạo đối tượng sản phẩm từ ResultSet
    private Products extractProduct(ResultSet rs) throws Exception {
        return new Products(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("price"),
            rs.getString("image")
        );
    }

    // ✅ Lấy tất cả sản phẩm
    public List<Products> getAllProducts() throws Exception {
        List<Products> list = new ArrayList<>();
        String query = "SELECT * FROM Products";
        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                list.add(extractProduct(rs));
            }
        }
        return list;
    }

    // ✅ Tìm sản phẩm theo ID
    public Products findById(int id) {
        String query = "SELECT * FROM Products WHERE id = ?";
        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractProduct(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Find error: " + e);
        }
        return null;
    }


    // ✅ Tìm theo tên
    public List<Products> getProductsByKeyword(String keyword) {
        List<Products> list = new ArrayList<>();
        String query = "SELECT * FROM Products WHERE name LIKE ?";
        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractProduct(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("❌ getProductsByKeyword error: " + e);
        }
        return list;
    }

    // ✅ Tìm theo tên hoặc mô tả
    public List<Products> searchByKeyword(String keyword) {
        List<Products> list = new ArrayList<>();
        String sql = "SELECT * FROM Products WHERE name LIKE ? OR description LIKE ?";
        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractProduct(rs));
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Search error: " + e);
        }
        return list;
    }

    // ✅ Thêm sản phẩm
    public int insert(Products p) {
        String query = "INSERT INTO Products (name, description, price, image) VALUES (?, ?, ?, ?)";
        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setString(4, p.getImage());
            return ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("❌ Insert error: " + e);
        }
        return -1;
    }

    // ✅ Cập nhật sản phẩm
    public int update(Products p) {
        String query = "UPDATE Products SET name = ?, description = ?, price = ?, image = ? WHERE id = ?";
        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrice());
            ps.setString(4, p.getImage());
            ps.setInt(5, p.getId());
            return ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("❌ Update error: " + e);
        }
        return -1;
    }

    // ✅ Xoá sản phẩm
    public int delete(int id) {
        String query = "DELETE FROM Products WHERE id = ?";
        try (
            Connection conn = new DBContext().getConnection();
            PreparedStatement ps = conn.prepareStatement(query)
        ) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("❌ Delete error: " + e);
        }
        return -1;
    }
}
