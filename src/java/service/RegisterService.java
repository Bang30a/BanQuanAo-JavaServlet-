package service;

import dao.UsersDao;
import entity.Users;

/**
 * Lớp này chứa TOÀN BỘ logic nghiệp vụ cho việc đăng ký.
 */
public class RegisterService {

    // Biến này sẽ chứa Mock DAO khi test, hoặc DAO thật khi chạy web
    private final UsersDao usersDao;

    // Constructor: Nhận DAO từ bên ngoài vào (Dependency Injection)
    public RegisterService(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    /**
     * Xử lý logic đăng ký người dùng.
     * @return Enum RegisterResult cho biết kết quả.
     */
    public RegisterResult registerUser(String username, String password, String fullname, String email) {

        // 0. Kiểm tra dữ liệu đầu vào (Validation)
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

        // 0.6 Kiểm tra định dạng email
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            return RegisterResult.INVALID_INPUT;
        }

        // 1. Kiểm tra tồn tại
        // [QUAN TRỌNG] Dùng this.usersDao (đã được tiêm vào) chứ KHÔNG new UsersDao()
        if (this.usersDao.checkUserExists(username)) {
            return RegisterResult.USERNAME_EXISTS;
        }

        // 2. Tạo đối tượng người dùng mới
        // (Lưu ý: Trong thực tế bạn nên mã hóa password tại đây, ví dụ dùng BCrypt)
        Users newUser = new Users(username, password, fullname, email, "user");

        // 3. Gọi DAO để lưu vào database
        // [QUAN TRỌNG] Dùng this.usersDao
        boolean success = this.usersDao.register(newUser);

        // 4. Trả về kết quả dựa trên phản hồi của DAO
        if (success) {
            return RegisterResult.SUCCESS;
        } else {
            return RegisterResult.REGISTRATION_FAILED;
        }
    }
}