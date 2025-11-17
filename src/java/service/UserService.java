package service;

import dao.UsersDao;
import entity.Users;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp này chứa toàn bộ logic nghiệp vụ để quản lý Người dùng (CRUD).
 */
public class UserService {

    private final UsersDao usersDao;
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    // Nhận DAO qua constructor
    public UserService(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    /**
     * Lấy tất cả người dùng. Trả về danh sách rỗng nếu có lỗi.
     */
    public List<Users> getAllUsers() {
        try {
            return usersDao.getAllUsers();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy tất cả người dùng", e);
            return new ArrayList<>(); // Trả về rỗng an toàn
        }
    }

    /**
     * Lấy một người dùng để chỉnh sửa.
     * Nếu ID = 0 hoặc không tìm thấy, trả về một đối tượng User mới (rỗng).
     */
    public Users getUserForEdit(int userId) {
        try {
            if (userId == 0) {
                return new Users(); // Cho trường hợp "Thêm mới"
            }
            Users user = usersDao.getUserById(userId);
            if (user == null) {
                return new Users(); // Không tìm thấy, trả về rỗng
            }
            return user;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy người dùng theo ID: " + userId, e);
            return new Users(); // Trả về rỗng nếu có lỗi
        }
    }

    /**
     * Lưu (Thêm mới) hoặc Cập nhật một người dùng.
     * Trả về true nếu thành công, false nếu thất bại.
     */
    public boolean saveOrUpdateUser(Users user) {
        try {
            // Logic "upsert" (update/insert) giống hệt trong servlet gốc
            if (user.getId() == 0 || usersDao.getUserById(user.getId()) == null) {
                usersDao.insert(user);
            } else {
                usersDao.updateUser(user);
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu/cập nhật người dùng", e);
            return false;
        }
    }

    /**
     * Xóa một người dùng theo ID.
     * Trả về true nếu thành công, false nếu thất bại.
     */
    public boolean deleteUser(int userId) {
        try {
            usersDao.deleteUser(userId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa người dùng: " + userId, e);
            return false;
        }
    }
}