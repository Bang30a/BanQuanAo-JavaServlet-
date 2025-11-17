package service;

import dao.OrderDao;
import dao.OrderDetailDao;
import dao.ProductVariantDao;
import entity.OrderDetails;
import entity.Orders;
import entity.ProductVariants;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lớp này chứa toàn bộ logic nghiệp vụ của Admin để quản lý Đơn hàng.
 */
public class OrderAdminService {

    private final OrderDao orderDao;
    private final OrderDetailDao detailDao;
    private final ProductVariantDao variantDao;
    private static final Logger LOGGER = Logger.getLogger(OrderAdminService.class.getName());

    // Nhận cả 3 DAO qua constructor
    public OrderAdminService(OrderDao orderDao, OrderDetailDao detailDao, ProductVariantDao variantDao) {
        this.orderDao = orderDao;
        this.detailDao = detailDao;
        this.variantDao = variantDao;
    }

    /**
     * Lấy tất cả đơn hàng một cách an toàn.
     */
    public List<Orders> getAllOrders() {
        try {
            return orderDao.getAllOrders();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy tất cả đơn hàng", e);
            return Collections.emptyList();
        }
    }

    /**
     * Lấy một đơn hàng để chỉnh sửa.
     * Nếu ID = 0 hoặc không tìm thấy, trả về một đối tượng mới (rỗng).
     */
    public Orders getOrderForEdit(int orderId) {
        try {
            if (orderId == 0) {
                return new Orders(); // Cho trường hợp "Thêm mới"
            }
            Orders order = orderDao.getOrderById(orderId);
            return (order != null) ? order : new Orders();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy đơn hàng theo ID: " + orderId, e);
            return new Orders(); // Trả về rỗng an toàn
        }
    }

    /**
     * Lưu (Thêm mới) hoặc Cập nhật một đơn hàng.
     * (Không bao gồm logic cập nhật kho).
     */
    public boolean saveOrUpdateOrder(Orders order) {
        try {
            // Logic "upsert" (update/insert)
            if (order.getId() == 0 || orderDao.getOrderById(order.getId()) == null) {
                orderDao.addOrder(order); // Giả sử addOrder này không cần Connection
            } else {
                orderDao.updateOrder(order);
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lưu/cập nhật đơn hàng", e);
            return false;
        }
    }

    /**
     * Xóa một đơn hàng.
     * (Giả sử DB đã có "ON DELETE CASCADE" để xóa details).
     */
    public boolean deleteOrder(int orderId) {
        try {
            orderDao.deleteOrder(orderId);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi xóa đơn hàng: " + orderId, e);
            return false;
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng VÀ xử lý logic cập nhật kho.
     * Đây là nghiệp vụ quan trọng nhất.
     */
    public boolean updateOrderStatus(int orderId, String newStatus) {
        try {
            Orders order = orderDao.getOrderById(orderId);
            if (order == null) {
                return false; // Không tìm thấy đơn hàng
            }

            String oldStatus = order.getStatus();
            
            // 1. Cập nhật trạng thái đơn hàng
            order.setStatus(newStatus);
            orderDao.updateOrder(order);

            // 2. Xử lý logic nghiệp vụ: Trừ kho khi "Đã giao"
            // (Code này sao y logic của servlet - nó không có transaction,
            // nếu update kho lỗi thì status vẫn bị đổi)
            if (!"Đã giao".equalsIgnoreCase(oldStatus) && "Đã giao".equalsIgnoreCase(newStatus)) {
                
                List<OrderDetails> details = detailDao.getDetailsByOrderId(orderId);
                for (OrderDetails detail : details) {
                    ProductVariants variant = variantDao.findById(detail.getProductVariantId());
                    if (variant != null) {
                        int newStock = variant.getStock() - detail.getQuantity();
                        variant.setStock(Math.max(newStock, 0)); // Đảm bảo không âm
                        variantDao.updateVariant(variant);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi cập nhật trạng thái đơn hàng: " + orderId, e);
            return false;
        }
    }
    /**
     * [Admin] Lấy chi tiết của một đơn hàng một cách an toàn.
     * Trả về danh sách rỗng nếu có lỗi.
     */
    public List<OrderDetails> getDetailsForOrder(int orderId) {
        try {
            return detailDao.getDetailsByOrderId(orderId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi lấy chi tiết cho order ID: " + orderId, e);
            return Collections.emptyList(); // Trả về rỗng an toàn
        }
    }
    public List<Orders> getOrdersByStatus(String status) {
    try {
        return orderDao.getOrdersByStatus(status);
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Lỗi khi lọc đơn hàng theo trạng thái", e);
        return Collections.emptyList();
    }
}

}