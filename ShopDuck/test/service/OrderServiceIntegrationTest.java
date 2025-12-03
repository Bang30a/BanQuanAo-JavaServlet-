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

// === IMPORT MOCKITO (Chúng ta cần SPY nếu muốn mock connection trong DAO, 
// nhưng ở đây ta dùng constructor injection nên không nhất thiết phải spy nếu DAO hỗ trợ) ===
import org.mockito.Mockito;
import static org.mockito.Mockito.doReturn;

// === IMPORT ĐỂ TẠO BÁO CÁO EXCEL/CSV ===
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * INTEGRATION TEST cho OrderService.
 * Kiểm tra logic TRANSACTION (Commit/Rollback) trên CSDL H2 thật.
 * Xuất kết quả ra file Excel (CSV).
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
    private static UsersDao usersDao;
    
    // 3. Dữ liệu "giả" dùng chung
    private Users testUser;
    private ProductVariants testVariant;

    // === CÁC BIẾN HỖ TRỢ BÁO CÁO ===
    private static String currentId = "";
    private static String currentName = "";
    private static String currentSteps = "";
    private static String currentData = "";
    private static String currentExpected = "";
    
    // Danh sách lưu kết quả để xuất Excel
    private static final List<String[]> finalReportData = new ArrayList<>();

    // === CHẠY MỘT LẦN DUY NHẤT KHI BẮT ĐẦU ===
    @BeforeClass
    public static void setUpClass() throws Exception {
        // 1. Nạp driver H2
        Class.forName("org.h2.Driver");
        
        // 2. Tạo kết nối H2 (BẮT BUỘC AUTOCOMMIT=OFF để kiểm soát transaction)
        String dbUrl = "jdbc:h2:mem:order_testdb;DB_CLOSE_DELAY=-1;AUTOCOMMIT=OFF";
        h2Connection = DriverManager.getConnection(dbUrl, "sa", ""); 
        
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
        orderDao = new OrderDao(h2Connection); 

        // 5. Khởi tạo Service (Dùng constructor test đã inject DAO)
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
        
        // Commit việc xóa
        h2Connection.commit();
        
        // Chuẩn bị dữ liệu cơ bản cho mỗi test
        // 1. Tạo User (ID=1)
        usersDao.register(new Users(0, "testuser", "pass", "Test User", "test@g.com", "user"));
        testUser = usersDao.getUserById(1);
        
        // 2. Tạo Product (ID=1)
        productDao.insert(new Products(0, "Áo Thun", "Áo", 100.0, "img.jpg"));
        
        // 3. Tạo Size (ID=1)
        sizeDao.insertSize(new Size(0, "M"));
        
        // 4. Tạo Variant (ID=1)
        variantDao.insertVariant(new ProductVariants(0, 1, 1, 50)); // (id, prod_id, size_id, stock)
        
        // Lấy lại variant THẬT từ H2 DB
        testVariant = variantDao.findById(1);
        
        // Commit (lưu) luôn cả dữ liệu "mồi"
        h2Connection.commit();
    }

    // Hàm điền thông tin Test Case
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        currentId = id;
        currentName = name;
        currentSteps = steps;
        currentData = data;
        currentExpected = expected;
    }

    // -- BẮT ĐẦU CÁC TEST CASE --

    @Test
    public void testPlaceOrder_Success_ShouldCommit() throws Exception {
        setTestCaseInfo(
            "INT_ORD_01", 
            "Đặt hàng thành công & Commit DB", 
            "1. User mua 2 áo (ID=1)\n2. PlaceOrder\n3. Check DB", 
            "Cart: 2 x Variant(1)", 
            "Result: SUCCESS\nDB: Có Order & Detail"
        );

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
        setTestCaseInfo(
            "INT_ORD_02", 
            "Rollback khi lỗi thêm chi tiết (Detail)", 
            "1. Cart chứa SP ma (ID=999)\n2. PlaceOrder\n3. Check DB (phải rỗng)", 
            "Cart: Variant(999)", 
            "Result: EXCEPTION\nDB: Không có Order (Rollback)"
        );

        // --- ARRANGE ---
        ProductVariants nonExistVariant = new ProductVariants();
        nonExistVariant.setId(999); 
        nonExistVariant.setPrice(10.0);
        
        List<CartBean> cart = new ArrayList<>();
        cart.add(new CartBean(nonExistVariant, 1));
        
        // --- ACT ---
        OrderResult result = orderService.placeOrder(testUser, cart, "123 Street", "09090909");

        // --- ASSERT ---
        // Mong đợi EXCEPTION (hoặc DETAIL_FAILED tùy logic DAO), ở đây giả định ném Exception do FK constraint
        assertTrue(result == OrderResult.EXCEPTION || result == OrderResult.DETAIL_FAILED);
        
        // Kiểm tra CSDL H2 (Quan trọng nhất: Rollback có hoạt động không?)
        try (Statement stmt = h2Connection.createStatement()) {
            ResultSet rsOrders = stmt.executeQuery("SELECT COUNT(*) FROM Orders");
            rsOrders.next();
            // Nếu rollback đúng, lệnh insert Order trước đó phải bị hủy
            assertEquals("Bảng Orders phải rỗng (đã rollback)", 0, rsOrders.getInt(1));

            ResultSet rsDetails = stmt.executeQuery("SELECT COUNT(*) FROM OrderDetails");
            rsDetails.next();
            assertEquals("Bảng OrderDetails phải rỗng", 0, rsDetails.getInt(1));
        }
    }
    
    @Test
    public void testPlaceOrder_EmptyCart() throws Exception {
        setTestCaseInfo(
            "INT_ORD_03", 
            "Đặt hàng thất bại (Giỏ rỗng)", 
            "1. Cart rỗng\n2. PlaceOrder", 
            "Cart: Empty", 
            "Result: EMPTY_CART\nDB: Không đổi"
        );

        List<CartBean> emptyCart = new ArrayList<>();
        OrderResult result = orderService.placeOrder(testUser, emptyCart, "123 Street", "09090909");
        
        assertEquals(OrderResult.EMPTY_CART, result);
        
        try (Statement stmt = h2Connection.createStatement()) {
            ResultSet rsOrders = stmt.executeQuery("SELECT COUNT(*) FROM Orders");
            rsOrders.next();
            assertEquals("Bảng Orders phải rỗng", 0, rsOrders.getInt(1));
        }
    }

    // ==========================================================
    // === CẤU HÌNH XUẤT FILE EXCEL (CSV) ===
    // ==========================================================

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            finalReportData.add(new String[]{
                currentId, currentName, currentSteps, currentData, currentExpected, currentExpected, "PASS"
            });
        }

        @Override
        protected void failed(Throwable e, Description description) {
            String errorMsg = (e != null) ? e.getMessage() : "Unknown Error";
            finalReportData.add(new String[]{
                currentId, currentName, currentSteps, currentData, currentExpected, errorMsg, "FAIL"
            });
        }
    };
    
    @AfterClass
    public static void tearDownClass() throws Exception {
        // 1. Đóng kết nối
        if (h2Connection != null) {
            h2Connection.close();
        }
        
        // 2. Xuất file CSV
        String fileName = "KetQuaTest_OrderServiceIntegrationTest.csv";
        
        System.out.println("\n-------------------------------------------------------");
        System.out.println("Đang xuất báo cáo OrderService Integration ra file (" + fileName + ")...");

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8))) {
            
            writer.write('\ufeff'); // BOM

            writer.println("ID,Tên Test Case,Các bước thực hiện,Dữ liệu đầu vào,Kết quả mong đợi,Kết quả thực tế,Trạng thái");

            for (String[] row : finalReportData) {
                String line = String.format("%s,%s,%s,%s,%s,%s,%s",
                        escapeSpecialChars(row[0]),
                        escapeSpecialChars(row[1]),
                        escapeSpecialChars(row[2]),
                        escapeSpecialChars(row[3]),
                        escapeSpecialChars(row[4]),
                        escapeSpecialChars(row[5]),
                        escapeSpecialChars(row[6])
                );
                writer.println(line);
            }
            
            System.out.println("XONG! File '" + fileName + "' đã được tạo.");
            System.out.println("-------------------------------------------------------");

        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file: " + e.getMessage());
        }
    }
    
    private static String escapeSpecialChars(String data) {
        if (data == null) return "\"\"";
        String escapedData = data.replaceAll("\"", "\"\"");
        return "\"" + escapedData + "\"";
    }
}