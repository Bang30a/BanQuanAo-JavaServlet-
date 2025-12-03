package service;

import entity.Users;

/**
 * Chứa kết quả trả về của LoginService.
 * Gồm TRẠNG THÁI (Status) và thông tin Users (nếu đăng nhập thành công).
 */
public class LoginResult {

    public enum Status {
        SUCCESS_ADMIN,       // Đăng nhập thành công -> Admin
        SUCCESS_USER,        // Đăng nhập thành công -> User
        FAILED_CREDENTIALS,  // Sai username hoặc password
        FAILED_INVALID_ROLE, // Đăng nhập thành công nhưng role không hợp lệ
        FAILED_SYSTEM_ERROR  // Lỗi hệ thống (DAO lỗi, SQL lỗi...)
    }

    private final Status status;
    private final Users user;   // null nếu thất bại

    public LoginResult(Status status, Users user) {
        this.status = status;
        this.user = user;
    }

    public Status getStatus() {
        return status;
    }

    public Users getUser() {
        return user;
    }
}
