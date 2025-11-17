package service;

import dao.UsersDao;
import entity.Users;

/**
 * Lớp này chứa TOÀN BỘ logic nghiệp vụ cho việc đăng ký.
 */
public class RegisterService {

    private final UsersDao usersDao;

    // Service nhận DAO qua constructor (để tiêm phụ thuộc)
    public RegisterService(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    /**
     * Xử lý logic đăng ký người dùng.
     * @return Enum RegisterResult cho biết kết quả.
     */
    public RegisterResult registerUser(String username, String password, String fullname, String email) {

        // 0. Kiểm tra dữ liệu đầu vào
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            fullname == null || fullname.trim().isEmpty() ||
            email == null || email.trim().isEmpty()) {
            return RegisterResult.INVALID_INPUT;
        }

        // 0.5 Kiểm tra độ dài mật khẩu (tối thiểu 6 ký tự)
        if (password.length() < 6) {
            return RegisterResult.INVALID_INPUT;
        }

        // 0.6 Kiểm tra định dạng email cơ bản
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            return RegisterResult.INVALID_INPUT;
        }

        // 1. Kiểm tra tồn tại
        if (usersDao.checkUserExists(username)) {
            return RegisterResult.USERNAME_EXISTS;
        }

        // 2. Tạo người dùng mới
        // (Trong dự án thật, bạn PHẢI mã hóa mật khẩu ở đây)
        Users newUser = new Users(username, password, fullname, email, "user");

        // 3. Gọi DAO để đăng ký
        boolean success = usersDao.register(newUser);

        // 4. Trả về kết quả
        if (success) {
            return RegisterResult.SUCCESS;
        } else {
            return RegisterResult.REGISTRATION_FAILED;
        }
    }
}
