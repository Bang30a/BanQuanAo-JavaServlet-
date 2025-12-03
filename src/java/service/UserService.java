package service;

import dao.UsersDao;
import entity.Users;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserService {

    private final UsersDao usersDao;
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    public UserService(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    public List<Users> getAllUsers() {
        try {
            return usersDao.getAllUsers();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy danh sách", e);
            return new ArrayList<>();
        }
    }

    public Users getUserForEdit(int userId) {
        try {
            if (userId == 0) return new Users();
            Users user = usersDao.getUserById(userId);
            return user != null ? user : new Users();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi lấy user", e);
            return new Users();
        }
    }

    /**
     * SỬA ĐỔI: Trả về String (Thông báo lỗi) thay vì boolean
     * Để Servlet có thể hiển thị lỗi cụ thể cho người dùng.
     */
    public String saveOrUpdateUser(Users user) {
        try {
            // 1. VALIDATION: Kiểm tra rỗng
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return "Vui lòng nhập Username!";
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return "Vui lòng nhập Password!";
            }

            // 2. VALIDATION: Kiểm tra trùng Username (Chỉ khi thêm mới)
            if (user.getId() == 0 && usersDao.checkUserExists(user.getUsername())) {
                return "Username đã tồn tại!";
            }

            // 3. Logic lưu xuống DB
            boolean success;
            if (user.getId() == 0 || usersDao.getUserById(user.getId()) == null) {
                success = usersDao.insert(user);
            } else {
                success = usersDao.updateUser(user);
            }
            return success ? "SUCCESS" : "Lỗi hệ thống (DB)";
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi saveOrUpdate", e);
            return "Exception: " + e.getMessage();
        }
    }

    public boolean deleteUser(int userId) {
        try {
            return usersDao.deleteUser(userId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi xóa user", e);
            return false;
        }
    }
}