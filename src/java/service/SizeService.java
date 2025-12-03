package service;

import dao.SizeDao;
import entity.Size;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp này chứa toàn bộ logic nghiệp vụ để quản lý Kích thước (CRUD).
 */
public class SizeService {

    private final SizeDao sizeDao;
    private static final Logger LOGGER = Logger.getLogger(SizeService.class.getName());

    // Nhận DAO qua constructor
    public SizeService(SizeDao sizeDao) {
        this.sizeDao = sizeDao;
    }

    /**
     * Lấy tất cả kích thước một cách an toàn.
     */
    public List<Size> getAllSizes() {
        try {
            return sizeDao.getAllSizes(); // Giữ nguyên hàm của bạn
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy tất cả kích thước", e);
            return Collections.emptyList(); // Trả về danh sách rỗng
        }
    }

    /**
     * Lấy một kích thước để chỉnh sửa.
     * Nếu ID = 0 hoặc không tìm thấy, trả về một đối tượng Size mới (rỗng).
     */
    public Size getSizeForEdit(int sizeId) {
        try {
            if (sizeId == 0) {
                return new Size(); // Cho trường hợp "Thêm mới"
            }
            Size size = sizeDao.getSizeById(sizeId); // Giữ nguyên hàm của bạn
            return (size != null) ? size : new Size();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy size theo ID: " + sizeId, e);
            return new Size(); // Trả về rỗng nếu có lỗi
        }
    }

    /**
     * CẬP NHẬT LOGIC: Trả về String để báo lỗi cụ thể (Thay vì boolean)
     * Đã tích hợp logic kiểm tra Rỗng và Trùng lặp.
     * SỬA QUAN TRỌNG: Dùng getSizeLabel() thay vì name/sizeLabel() sai cú pháp
     */
    public String saveOrUpdateSize(Size size) {
        try {
            // 1. VALIDATION: Kiểm tra rỗng (Sửa thành getSizeLabel)
            if (size.getSizeLabel() == null || size.getSizeLabel().trim().isEmpty()) {
                return "Tên Size không được để trống!";
            }

            // 2. VALIDATION: Kiểm tra trùng lặp (Logic thủ công)
            // Lấy tất cả size lên để so sánh
            List<Size> allSizes = getAllSizes();
            for (Size existing : allSizes) {
                // Sửa thành getSizeLabel()
                if (existing.getSizeLabel().equalsIgnoreCase(size.getSizeLabel())) {
                    
                    // Nếu là thêm mới (id=0) -> Lỗi
                    if (size.getId() == 0) {
                        return "Size '" + size.getSizeLabel() + "' đã tồn tại!";
                    }
                    
                    // Nếu là update (id!=0) mà id khác nhau -> Lỗi trùng tên người khác
                    if (existing.getId() != size.getId()) {
                        return "Size '" + size.getSizeLabel() + "' đã được sử dụng!";
                    }
                }
            }

            // 3. Logic lưu xuống DB (Sử dụng đúng tên hàm insertSize/updateSize của bạn)
            boolean success;
            if (size.getId() == 0) {
                success = sizeDao.insertSize(size);
            } else {
                success = sizeDao.updateSize(size);
            }
            return success ? "SUCCESS" : "Lỗi hệ thống (DB)";

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu/cập nhật size", e);
            return "Exception: " + e.getMessage();
        }
    }

    /**
     * Xóa một kích thước theo ID.
     */
    public boolean deleteSize(int sizeId) {
        try {
            return sizeDao.deleteSize(sizeId); // Giữ nguyên hàm của bạn
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa size: " + sizeId, e);
            return false;
        }
    }
}