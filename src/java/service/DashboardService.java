package service;

import dao.DashboardDao;
import java.util.List;
import java.util.Map;
import java.util.ArrayList; // <-- Import
import java.util.Collections; // <-- Import
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp này chứa logic nghiệp vụ để lấy dữ liệu cho Dashboard.
 * Quan trọng nhất: Nó "bọc" các lệnh gọi DAO trong try-catch
 * để đảm bảo trang không bị lỗi nếu có sự cố CSDL.
 */
public class DashboardService {

    private final DashboardDao dashboardDao;
    private static final Logger LOGGER = Logger.getLogger(DashboardService.class.getName());

    // Nhận DAO qua constructor
    public DashboardService(DashboardDao dashboardDao) {
        this.dashboardDao = dashboardDao;
    }

    /**
     * Lấy tổng doanh thu một cách an toàn.
     * @return tổng doanh thu, hoặc 0.0 nếu có lỗi.
     */
    public double getTotalRevenue() {
        try {
            return dashboardDao.getTotalRevenue();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy tổng doanh thu", e);
            return 0.0; // Trả về giá trị mặc định an toàn
        }
    }

    /**
     * Lấy tổng số đơn hàng một cách an toàn.
     * @return tổng số đơn, hoặc 0 nếu có lỗi.
     */
    public int getTotalOrders() {
        try {
            return dashboardDao.getTotalOrders();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy tổng đơn hàng", e);
            return 0; // Trả về giá trị mặc định an toàn
        }
    }

    /**
     * Lấy tổng số người dùng một cách an toàn.
     * @return tổng số người dùng, hoặc 0 nếu có lỗi.
     */
    public int getTotalUsers() {
        try {
            return dashboardDao.getTotalUsers();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy tổng người dùng", e);
            return 0; // Trả về giá trị mặc định an toàn
        }
    }

    /**
     * Lấy sản phẩm bán chạy nhất một cách an toàn.
     * @return danh sách sản phẩm, hoặc danh sách rỗng nếu có lỗi.
     */
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        try {
            return dashboardDao.getTopSellingProducts(limit);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy sản phẩm bán chạy", e);
            return Collections.emptyList(); // Trả về danh sách rỗng an toàn
        }
    }
}