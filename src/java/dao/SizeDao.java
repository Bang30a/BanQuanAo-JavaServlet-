package dao;

import context.DBContext;
import entity.Size;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SizeDao {

    // Lấy danh sách tất cả các size
    public List<Size> getAllSizes() {
        List<Size> list = new ArrayList<>();
        String sql = "SELECT * FROM Sizes";

        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(extractSize(rs));
            }
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi lấy danh sách size:");
            e.printStackTrace();
        }

        return list;
    }

    // Lấy size theo ID
    public Size getSizeById(int id) {
        String sql = "SELECT * FROM Sizes WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractSize(rs);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Lỗi khi lấy size theo ID:");
            e.printStackTrace();
        }

        return null;
    }

    // Thêm size mới
    public boolean insertSize(Size size) {
        String sql = "INSERT INTO Sizes (size_label) VALUES (?)";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, size.getSizeLabel());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("❌ Lỗi khi thêm size:");
            e.printStackTrace();
        }
        return false;
    }

    // Cập nhật size
    public boolean updateSize(Size size) {
        String sql = "UPDATE Sizes SET size_label = ? WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, size.getSizeLabel());
            ps.setInt(2, size.getId());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("❌ Lỗi khi cập nhật size:");
            e.printStackTrace();
        }
        return false;
    }

    // Xoá size
    public boolean deleteSize(int id) {
        String sql = "DELETE FROM Sizes WHERE id = ?";
        try (Connection conn = new DBContext().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.out.println("❌ Lỗi khi xoá size:");
            e.printStackTrace();
        }
        return false;
    }

    // Phương thức dùng chung
    private Size extractSize(ResultSet rs) throws SQLException {
        return new Size(rs.getInt("id"), rs.getString("size_label"));
    }
}
