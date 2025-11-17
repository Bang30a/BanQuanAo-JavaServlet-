package service;

/**
 * Định nghĩa các kết quả có thể xảy ra khi đặt hàng.
 */
public enum OrderResult {
    SUCCESS,             // Đặt hàng thành công
    NOT_LOGGED_IN,       // Người dùng chưa đăng nhập
    EMPTY_CART,          // Giỏ hàng rỗng
    MISSING_INFO,        // Thiếu địa chỉ hoặc SĐT
    ORDER_FAILED,        // Lỗi khi thêm Order chính (trả về orderId = -1)
    DETAIL_FAILED,       // Lỗi khi thêm chi tiết Order (OrderDetails)
    EXCEPTION            // Lỗi hệ thống (SQL, ... )
}