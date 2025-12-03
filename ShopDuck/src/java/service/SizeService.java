package service;

import dao.SizeDao;
import entity.Size;
import java.util.List;
import java.util.Collections; // Dùng để trả về danh sách rỗng
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
            return sizeDao.getAllSizes();
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
            Size size = sizeDao.getSizeById(sizeId);
            return (size != null) ? size : new Size();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy size theo ID: " + sizeId, e);
            return new Size(); // Trả về rỗng nếu có lỗi
        }
    }

    /**
     * Lưu (Thêm mới) hoặc Cập nhật một kích thước.
     * Trả về true nếu thành công.
     */
    public boolean saveOrUpdateSize(Size size) {
        try {
            // Logic "upsert" (update/insert)
            if (size.getId() == 0 || sizeDao.getSizeById(size.getId()) == null) {
                sizeDao.insertSize(size);
            } else {
                sizeDao.updateSize(size);
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu/cập nhật size", e);
            return false;
        }
    }

    /**
     * Xóa một kích thước theo ID.
     * Trả về true nếu thành công.
     */
    public boolean deleteSize(int sizeId) {
        try {
            sizeDao.deleteSize(sizeId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa size: " + sizeId, e);
            return false;
        }
    }
}