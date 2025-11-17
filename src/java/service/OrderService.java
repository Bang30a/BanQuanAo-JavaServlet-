package service;

import dao.*;
import entity.*;
import context.DBContext;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OrderService: xử lý transaction khi tạo đơn hàng.
 * - Test: DAO đã inject connection -> service KHÔNG tự mở/đóng connection.
 * - Production: mở connection từ DBContext và tự đóng sau commit/rollback.
 */
public class OrderService {

    private final OrderDao orderDao;
    private final OrderDetailDao detailDao;
    private final ProductDao productDao;
    private final ProductVariantDao variantDao;
    private final SizeDao sizeDao;

    private final DBContext dbContext;
    private final boolean isTestEnvironment;

    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    // ====================== TEST MODE ============================
    public OrderService(
            OrderDao orderDao,
            OrderDetailDao detailDao,
            ProductDao productDao,
            ProductVariantDao variantDao,
            SizeDao sizeDao,
            DBContext ignoredDbContext      // test không dùng
    ) {
        this.orderDao = orderDao;
        this.detailDao = detailDao;
        this.productDao = productDao;
        this.variantDao = variantDao;
        this.sizeDao = sizeDao;

        this.dbContext = null;
        this.isTestEnvironment = true;
    }

    // ====================== PRODUCTION MODE ============================
    public OrderService(DBContext dbContext) {
        this.dbContext = dbContext;

        this.orderDao = new OrderDao();
        this.detailDao = new OrderDetailDao();
        this.productDao = new ProductDao();
        this.variantDao = new ProductVariantDao();
        this.sizeDao = new SizeDao();

        this.isTestEnvironment = false;
    }

    // ====================== PRODUCTION ONLY ============================
    private Connection getConnection() throws Exception {
        if (isTestEnvironment) {
            throw new Exception("Test mode: không được gọi getConnection().");
        }
        return dbContext.getConnection();
    }

    // ======================= PLACE ORDER ===============================
    public OrderResult placeOrder(Users user, List<CartBean> cart, String address, String phone) {

        if (user == null) return OrderResult.NOT_LOGGED_IN;
        if (cart == null || cart.isEmpty()) return OrderResult.EMPTY_CART;
        if (address == null || address.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty())
            return OrderResult.MISSING_INFO;

        double total = 0;
        for (CartBean item : cart) {
            total += item.getProductVariant().getPrice() * item.getQuantity();
        }

        Orders order = new Orders();
        order.setUserId(user.getId());
        order.setOrderDate(Timestamp.valueOf(LocalDateTime.now()));
        order.setTotal(total);
        order.setAddress(address.trim());
        order.setPhone(phone.trim());
        order.setStatus("Chờ xử lý");

        Connection conn = null;

        try {
            // lấy connection
            conn = isTestEnvironment
                    ? orderDao.getMockConnection()
                    : getConnection();

            if (conn == null) {
                throw new IllegalStateException("Không có connection để thực hiện transaction");
            }

            conn.setAutoCommit(false);

            // insert order
            int orderId = orderDao.addOrder(conn, order);
            if (orderId == -1) {
                conn.rollback();
                return OrderResult.ORDER_FAILED;
            }

            // insert details
            for (CartBean item : cart) {
                ProductVariants variant = item.getProductVariant();

                OrderDetails detail = new OrderDetails();
                detail.setOrderId(orderId);
                detail.setProductVariantId(variant.getId());
                detail.setQuantity(item.getQuantity());
                detail.setPrice(variant.getPrice());

                boolean ok = detailDao.addDetail(conn, detail);
                if (!ok) {
                    conn.rollback();
                    return OrderResult.DETAIL_FAILED;
                }
            }

            conn.commit();
            return OrderResult.SUCCESS;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Transaction error", e);
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return OrderResult.EXCEPTION;

        } finally {
            // test KHÔNG đóng connection
            if (!isTestEnvironment) {
                try { if (conn != null) conn.close(); } catch (Exception ignored) {}
            }
        }
    }

    // =================== API khác =======================
    public List<Orders> getOrdersForUser(int userId) throws Exception {
        return orderDao.getOrdersByUserId(userId);
    }

    public Orders getSecuredOrder(int orderId, int userId) throws Exception {
        return orderDao.getOrderByIdAndUserId(orderId, userId);
    }

    public List<Map<String, Object>> getRichOrderDetails(int orderId) throws Exception {

        List<Map<String, Object>> richDetails = new ArrayList<>();
        List<OrderDetails> details = detailDao.getDetailsByOrderId(orderId);

        Map<Integer, String> sizeMap = new HashMap<>();
        for (Size s : sizeDao.getAllSizes()) {
            sizeMap.put(s.getId(), s.getSizeLabel());
        }

        for (OrderDetails detail : details) {
            Map<String, Object> item = new HashMap<>();

            ProductVariants variant = variantDao.findById(detail.getProductVariantId());
            if (variant != null) {
                Products product = productDao.findById(variant.getProductId());

                item.put("productName", product != null ? product.getName() : "Không tìm thấy");
                item.put("productImage", product != null ? product.getImage() : "images/default.jpg");
                item.put("sizeLabel", sizeMap.getOrDefault(variant.getSizeId(), "N/A"));
            } else {
                item.put("productName", "Sản phẩm không còn tồn tại");
                item.put("productImage", "images/default.jpg");
                item.put("sizeLabel", "N/A");
            }

            item.put("quantity", detail.getQuantity());
            item.put("price", detail.getPrice());

            richDetails.add(item);
        }

        return richDetails;
    }
}
