// Đặt trong "Test Packages/service/"
package service;

// === IMPORT CƠ BẢN ===
import dao.*;
import entity.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// === IMPORT JUNIT ===
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

// === IMPORT MOCKITO (Chúng ta cần SPY) ===
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;

// === IMPORT ĐỂ TẠO BÁO CÁO ===
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.io.OutputStreamWriter;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
// ===========================================

/**
 * Đây là lớp INTEGRATION TEST (Kiểm thử Tích hợp) cho OrderService.
 * Nó kiểm tra logic TRANSACTION (Commit/Rollback) trên CSDL H2 thật.
 */
public class OrderServiceIntegrationTest {

    // 1. Service và Connection
    private static OrderService orderService;
    private static Connection h2Connection;

    // 2. Các DAO thật (không phải mock)
    private static OrderDao orderDao;
    private static OrderDetailDao detailDao;
    private static ProductDao productDao;
    private static ProductVariantDao variantDao;
    private static SizeDao sizeDao;
    private static UsersDao usersDao; // Cần để tạo user
    
    // 3. Dữ liệu "giả" dùng chung
    private Users testUser;
    private ProductVariants testVariant;

    // === CHẠY MỘT LẦN DUY NHẤT KHI BẮT ĐẦU ===
    @BeforeClass
    public static void setUpClass() throws Exception {
        // 1. Nạp driver H2
        Class.forName("org.h2.Driver");
        
        // 2. Tạo kết nối H2 (BẮT BUỘC AUTOCOMMIT=OFF)
        String dbUrl = "jdbc:h2:mem:order_testdb;DB_CLOSE_DELAY=-1;AUTOCOMMIT=OFF";
        h2Connection = DriverManager.getConnection(dbUrl, "sa", ""); // user/pass H2
        
        // 3. TẠO TẤT CẢ CÁC BẢNG (Schema)
        try (Statement stmt = h2Connection.createStatement()) {
            stmt.execute("CREATE TABLE Users (id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(255) UNIQUE, password VARCHAR(255), fullname VARCHAR(255), email VARCHAR(255), role VARCHAR(50))");
            stmt.execute("CREATE TABLE Products (id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), description VARCHAR(1000), price DOUBLE, image VARCHAR(255), category_id INT)");
            stmt.execute("CREATE TABLE Sizes (id INT PRIMARY KEY AUTO_INCREMENT, size_label VARCHAR(50))");
            stmt.execute("CREATE TABLE ProductVariants (id INT PRIMARY KEY AUTO_INCREMENT, product_id INT, size_id INT, stock INT, FOREIGN KEY (product_id) REFERENCES Products(id), FOREIGN KEY (size_id) REFERENCES Sizes(id))");
            stmt.execute("CREATE TABLE Orders (id INT PRIMARY KEY AUTO_INCREMENT, user_id INT, order_date TIMESTAMP, total DOUBLE, address VARCHAR(255), phone VARCHAR(20), status VARCHAR(50), FOREIGN KEY (user_id) REFERENCES Users(id))");
            stmt.execute("CREATE TABLE OrderDetails (id INT PRIMARY KEY AUTO_INCREMENT, order_id INT, product_variant_id INT, quantity INT, price DOUBLE, FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE, FOREIGN KEY (product_variant_id) REFERENCES ProductVariants(id))");
        }
        
        // 4. Khởi tạo tất cả DAO THẬT, tiêm H2 Connection vào
        detailDao = new OrderDetailDao(h2Connection);
        productDao = new ProductDao(h2Connection);
        variantDao = new ProductVariantDao(h2Connection);
        sizeDao = new SizeDao(h2Connection);
        usersDao = new UsersDao(h2Connection);
        orderDao = new OrderDao(h2Connection); // Dùng constructor đã inject

        // 5. Khởi tạo Service
        // (Giả định OrderService đã được sửa để gọi orderDao.getMockConnection())
        orderService = new OrderService(orderDao, detailDao, productDao, variantDao, sizeDao, null);
        
        // 6. Commit (lưu) việc tạo bảng
        h2Connection.commit();
    }
    
    // === CHẠY TRƯỚC MỖI TEST CASE ===
    @Before
    public void setUp() throws Exception {
        // Dọn dẹp dữ liệu (chạy trước mỗi test)
        try (Statement stmt = h2Connection.createStatement()) {
            // (Phải xóa theo thứ tự ngược lại của Khóa Ngoại)
            stmt.execute("DELETE FROM OrderDetails");
            stmt.execute("DELETE FROM Orders");
            stmt.execute("DELETE FROM ProductVariants");
            stmt.execute("DELETE FROM Products");
            stmt.execute("DELETE FROM Sizes");
            stmt.execute("DELETE FROM Users");
            
            // Reset ID về 1
            stmt.execute("ALTER TABLE Users ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Products ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Sizes ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE ProductVariants ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE Orders ALTER COLUMN id RESTART WITH 1");
            stmt.execute("ALTER TABLE OrderDetails ALTER COLUMN id RESTART WITH 1");
        }
        
        // =================================================================
        // === ĐÃ SỬA LỖI Ở ĐÂY ===
        // =================================================================
        // Vì AUTOCOMMIT=OFF, chúng ta phải "commit" (lưu) các lệnh DELETE ở trên
        h2Connection.commit();
        
        // Chuẩn bị dữ liệu cơ bản cho mỗi test
        // 1. Tạo User (ID sẽ là 1)
        usersDao.register(new Users(0, "testuser", "pass", "Test User", "test@g.com", "user"));
        testUser = usersDao.getUserById(1);
        
        // 2. Tạo Product (ID sẽ là 1)
        productDao.insert(new Products(0, "Áo Thun", "Áo", 100.0, "img.jpg"));
        
        // 3. Tạo Size (ID sẽ là 1)
        sizeDao.insertSize(new Size(0, "M"));
        
        // 4. Tạo Variant (ID sẽ là 1)
        variantDao.insertVariant(new ProductVariants(0, 1, 1, 50)); // (id, prod_id, size_id, stock)
        
        // Lấy lại variant THẬT từ H2 DB
        testVariant = variantDao.findById(1);
        
        // Commit (lưu) luôn cả dữ liệu "mồi"
        h2Connection.commit();
    }

    // -- BẮT ĐẦU CÁC TEST CASE --

    @Test
    public void testPlaceOrder_Success_ShouldCommit() throws Exception {
        List<CartBean> cart = new ArrayList<>();
        cart.add(new CartBean(testVariant, 2));
        
        OrderResult result = orderService.placeOrder(testUser, cart, "123 Street", "09090909");

        assertEquals("Đặt hàng phải thành công", OrderResult.SUCCESS, result);
        
        try (Statement stmt = h2Connection.createStatement()) {
            ResultSet rsOrders = stmt.executeQuery("SELECT * FROM Orders WHERE user_id = 1");
            assertTrue("Phải tìm thấy 1 Order trong CSDL", rsOrders.next());
            assertEquals("Tổng tiền phải là 200.0", 200.0, rsOrders.getDouble("total"), 0.001);
            int orderId = rsOrders.getInt("id");

            ResultSet rsDetails = stmt.executeQuery("SELECT * FROM OrderDetails WHERE order_id = " + orderId);
            assertTrue("Phải tìm thấy 1 OrderDetail trong CSDL", rsDetails.next());
            assertEquals("Số lượng detail phải là 2", 2, rsDetails.getInt("quantity"));
        }
    }

    @Test
    public void testPlaceOrder_RollbackOnDetailFail() throws Exception {
        // --- ARRANGE ---
        ProductVariants nonExistVariant = new ProductVariants();
        nonExistVariant.setId(999); 
        nonExistVariant.setPrice(10.0);
        
        List<CartBean> cart = new ArrayList<>();
        cart.add(new CartBean(nonExistVariant, 1));
        
        // --- ACT ---
        OrderResult result = orderService.placeOrder(testUser, cart, "123 Street", "09090909");

        // --- ASSERT ---
        // Mong đợi EXCEPTION, vì các DAO đã được sửa để "throws Exception"
        assertEquals("Phải trả về EXCEPTION vì DAO đã ném lỗi", OrderResult.EXCEPTION, result);
        
        // Kiểm tra CSDL H2 (Quan trọng nhất)
        try (Statement stmt = h2Connection.createStatement()) {
            ResultSet rsOrders = stmt.executeQuery("SELECT COUNT(*) FROM Orders");
            rsOrders.next();
            // Lần này rollback() sẽ hoạt động vì DAO đã ném lỗi
            assertEquals("Bảng Orders phải rỗng (đã rollback)", 0, rsOrders.getInt(1));

            ResultSet rsDetails = stmt.executeQuery("SELECT COUNT(*) FROM OrderDetails");
            rsDetails.next();
            assertEquals("Bảng OrderDetails phải rỗng", 0, rsDetails.getInt(1));
        }
    }
    
    @Test
    public void testPlaceOrder_EmptyCart() throws Exception {
        List<CartBean> emptyCart = new ArrayList<>();
        OrderResult result = orderService.placeOrder(testUser, emptyCart, "123 Street", "09090909");
        assertEquals(OrderResult.EMPTY_CART, result);
        
        try (Statement stmt = h2Connection.createStatement()) {
            ResultSet rsOrders = stmt.executeQuery("SELECT COUNT(*) FROM Orders");
            rsOrders.next();
            assertEquals("Bảng Orders phải rỗng", 0, rsOrders.getInt(1));
        }
    }


    // =================================================================
    // === PHẦN CODE TẠO BÁO CÁO (GIỮ NGUYÊN) ===
    // =================================================================
    
    private static final List<String[]> testResults = new ArrayList<>();
    
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        private String getModuleName(Description d) {
            String className = d.getClassName();
            return className.substring(className.lastIndexOf('.') + 1);
        }
        @Override
        protected void succeeded(Description description) {
            testResults.add(new String[]{
                getModuleName(description), description.getMethodName(), "PASS", ""
            });
        }
        @Override
        protected void failed(Throwable e, Description description) {
            testResults.add(new String[]{
                getModuleName(description), description.getMethodName(), "FAIL", e.getMessage()
            });
        }
    };
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        if (h2Connection != null) {
            h2Connection.close();
        }
        
        String filePath = "test-report.txt";
        String format = "%-20s | %-45s | %-8s | %s\n";
        String header = String.format(format, "MODULE", "TEST CASE", "STATUS", "ACTUAL RESULT / DETAILS");
        String separator = String.format(format, 
            new String(new char[20]).replace('\0', '-'), 
            new String(new char[45]).replace('\0', '-'), 
            new String(new char[8]).replace('\0', '-'), 
            new String(new char[30]).replace('\0', '-')
        ).replace("|", "+");

        System.out.println("\n\n=============== TEST EXECUTION REPORT ================");
        System.out.print(header);
        System.out.print(separator);

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new java.io.FileOutputStream(filePath, true), StandardCharsets.UTF_8)) {
            
            writer.write("\n--- Kết quả chạy " + OrderServiceIntegrationTest.class.getName() + " ---\n");
            for (String[] result : testResults) {
                String line = String.format(format, (Object[]) result);
                System.out.print(line);
                writer.write(line);
            }
            System.out.println("=======================================================");
            System.out.println("\n==> Báo cáo đã được NỐI VÀO file: " + filePath);
        } catch (IOException e) {
            System.err.println("Không thể ghi file báo cáo: " + e.getMessage());
        }
    }
}