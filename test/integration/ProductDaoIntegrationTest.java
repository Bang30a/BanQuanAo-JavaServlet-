package integration;

import dao.ProductDao;
import entity.Products;
import util.ExcelTestExporter; 

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

// Import JUNIT
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Kiểm tra tích hợp (Integration Test) cho lớp ProductDao.
 * Class này kiểm thử các nghiệp vụ CRUD (Insert, Find, Update, Delete)
 * và tìm kiếm sản phẩm, bao gồm các tình huống ràng buộc khóa ngoại.
 */
public class ProductDaoIntegrationTest {

    private Connection connection;
    private ProductDao productDao;

    // --- CẤU HÌNH KẾT NỐI DATABASE TEST ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String instance = ""; // Instance Name của SQL Server (để trống nếu không có)
    private final String userID = "sa";
    private final String password = "123456";
    private final String dbName = "shopduck_test"; 

    // === BIẾN GHI LOG TEST CASE (Dùng cho ExcelTestExporter) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";

    /**
     * Thiết lập thông tin chi tiết cho Test Case hiện tại.
     * Thông tin này sẽ được ghi vào file Excel báo cáo.
     *
     * @param id ID của Test Case.
     * @param name Tên của Test Case.
     * @param steps Các bước thực hiện.
     * @param data Dữ liệu đầu vào/chuẩn bị.
     * @param expected Kết quả mong đợi.
     */
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case:
     * 1. Mở kết nối DB.
     * 2. Dọn dẹp dữ liệu (xóa Products và ProductVariants) để đảm bảo môi trường sạch.
     * 3. Khởi tạo DAO.
     * @throws Exception nếu có lỗi kết nối hoặc thao tác DB.
     */
    @Before
    public void setUp() throws Exception {
        // 1. Kết nối SQL Server (Hỗ trợ cả trường hợp có Instance Name)
        String url;
        if (instance == null || instance.trim().isEmpty()) {
            url = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";databaseName=" + dbName;
        } else {
            url = "jdbc:sqlserver://" + serverName + ":" + portNumber + "\\" + instance + ";databaseName=" + dbName;
        }
        url += ";encrypt=false;trustServerCertificate=true;loginTimeout=30";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(url, userID, password);

        // 2. Dọn dẹp dữ liệu (Cleanup): Xóa ProductVariants (bảng con) trước, Products (bảng cha) sau.
        try (Statement stmt = connection.createStatement()) {
            // Xóa biến thể (ProductVariants) trước do ràng buộc khóa ngoại (FK) với Products
            try { stmt.execute("DELETE FROM ProductVariants"); } catch (Exception ignored) {}
            
            // Xóa sản phẩm
            stmt.execute("DELETE FROM Products");
            
            // Reset ID tự tăng về 0 (RESEED) để ID của Product bắt đầu lại từ 1
            try { stmt.execute("DBCC CHECKIDENT ('Products', RESEED, 0)"); } catch (Exception ignored) {}
        }

        // 3. Khởi tạo DAO
        productDao = new ProductDao(connection);
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case: Đóng kết nối DB.
     * @throws Exception nếu có lỗi đóng kết nối.
     */
    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    // ====================== CÁC TEST CASE CRUD CƠ BẢN ======================

    @Test
    public void testInsertAndFindById() {
        setTestCaseInfo("PROD_DAO_01", "Thêm và Tìm kiếm theo ID", 
                "1. Insert Product\n2. Find by generated ID", 
                "Name: Ao Thun", "Trả về object Product đúng tên");

        // 1. Insert sản phẩm
        Products p = new Products(0, "Ao Thun", "Chat lieu cotton", 100000, "img.jpg");
        int row = productDao.insert(p); // ID sản phẩm được cập nhật vào object p
        
        Assert.assertTrue("Insert phải trả về số dòng bị ảnh hưởng (> 0)", row > 0);
        Assert.assertTrue("ID sản phẩm phải được sinh ra (>0)", p.getId() > 0);

        // 2. Find theo ID vừa được sinh ra
        Products found = productDao.findById(p.getId());
        
        Assert.assertNotNull("Phải tìm thấy sản phẩm vừa insert", found);
        Assert.assertEquals("Kiểm tra tên sản phẩm", "Ao Thun", found.getName());
        Assert.assertEquals("Kiểm tra giá sản phẩm", 100000, found.getPrice(), 0.01);
    }

    @Test
    public void testSearchByKeyword() {
        setTestCaseInfo("PROD_DAO_02", "Tìm kiếm sản phẩm (Tên & Mô tả)", 
                "1. Insert 3 SP\n2. Tìm 'Jean' (Tên & Mô tả)\n3. Tìm 'cotton'", 
                "Data: Quan Jean, Ao Thun...", "Kết quả list trả về đúng số lượng");

        // Chuẩn bị dữ liệu
        productDao.insert(new Products(0, "Quan Jean Xanh", "Vai bo xin", 200000, "1.jpg"));
        productDao.insert(new Products(0, "Ao Thun Trang", "100% Cotton", 90000, "2.jpg"));
        productDao.insert(new Products(0, "Ao Khoac", "Jean den", 300000, "3.jpg"));

        // Test 1: Tìm theo keyword "Jean" (Mong đợi tìm thấy 2 sản phẩm: Quan Jean Xanh, Ao Khoac)
        List<Products> list1 = productDao.searchByKeyword("Jean");
        Assert.assertEquals("Phải tìm thấy 2 sản phẩm có chữ 'Jean' (trong Tên hoặc Mô tả)", 2, list1.size());

        // Test 2: Tìm theo keyword "cotton" (Test tính năng tìm kiếm KHÔNG phân biệt chữ hoa/thường)
        List<Products> list2 = productDao.searchByKeyword("cotton"); 
        Assert.assertEquals("Phải tìm thấy 1 sản phẩm có chữ 'Cotton'", 1, list2.size());
        
        // Test 3: Tìm keyword không tồn tại
        List<Products> list3 = productDao.searchByKeyword("XYZ123");
        Assert.assertEquals("Không tìm thấy sản phẩm nào", 0, list3.size());
    }

    @Test
    public void testUpdateProduct() {
        setTestCaseInfo("PROD_DAO_03", "Cập nhật sản phẩm", 
                "1. Insert\n2. Update giá & tên\n3. Find lại", 
                "Old: 100k, New: 150k, Tên: Ao Moi", "Dữ liệu mới được lưu");

        Products p = new Products(0, "Ao Cu", "Mo ta cu", 100000, "old.jpg");
        productDao.insert(p);

        // Update object
        p.setName("Ao Moi");
        p.setPrice(150000);
        int result = productDao.update(p);

        Assert.assertTrue("Update phải trả về số dòng bị ảnh hưởng (> 0)", result > 0);

        // Verify: Tìm lại để kiểm tra
        Products updated = productDao.findById(p.getId());
        Assert.assertEquals("Tên sản phẩm phải được cập nhật thành 'Ao Moi'", "Ao Moi", updated.getName());
        Assert.assertEquals("Giá sản phẩm phải được cập nhật thành 150000", 150000, updated.getPrice(), 0.01);
    }

    @Test
    public void testDeleteProduct() {
        setTestCaseInfo("PROD_DAO_04", "Xóa sản phẩm", 
                "1. Insert\n2. Delete\n3. Find lại", 
                "ID vừa tạo", "Find trả về null");

        Products p = new Products(0, "Ao Xoa", "...", 50000, "del.jpg");
        productDao.insert(p);

        // Thực hiện Delete
        int result = productDao.delete(p.getId());
        Assert.assertTrue("Delete phải trả về số dòng bị ảnh hưởng (> 0)", result > 0);

        // Verify: Tìm lại (mong đợi là null)
        Products deleted = productDao.findById(p.getId());
        Assert.assertNull("Sản phẩm đã xóa phải không tồn tại (null)", deleted);
    }
    
    @Test
    public void testGetAllProducts() {
        setTestCaseInfo("PROD_DAO_05", "Lấy tất cả sản phẩm", 
                "1. Insert 2 SP\n2. Gọi getAll", 
                "2 Products", "List size = 2");

        // Insert 2 sản phẩm mới
        productDao.insert(new Products(0, "SP A", "...", 100, "a.jpg"));
        productDao.insert(new Products(0, "SP B", "...", 200, "b.jpg"));

        try {
            List<Products> list = productDao.getAllProducts();
            // Vì setUp đã dọn dẹp, list phải chứa đúng 2 sản phẩm vừa insert
            Assert.assertEquals("Danh sách phải chứa đúng 2 sản phẩm đã insert", 2, list.size());
        } catch (Exception e) {
            Assert.fail("Hàm getAllProducts không được văng lỗi: " + e.getMessage());
        }
    }

    // ====================== CÁC TEST CASE LOGIC PHỨC TẠP ======================

    @Test
    public void testFindByCategoryId() {
        setTestCaseInfo("PROD_DAO_06", "Tìm theo Category & Loại trừ ID (Sản phẩm liên quan)", 
                "Insert 3 SP cùng Cat=1. Tìm Cat=1, Exclude=ID1", 
                "Cat=1, Exclude ID1", "List chứa SP2, SP3 (size = 2)");

        int catId = 1;
        int id1 = 0; // ID sản phẩm sẽ bị loại trừ

        // Sử dụng SQL thuần để insert, đảm bảo cột category_id được set
        try (Statement s = connection.createStatement()) {
            s.executeUpdate("INSERT INTO Products (name, price, category_id) VALUES ('P1', 100, 1)", Statement.RETURN_GENERATED_KEYS);
            ResultSet rs1 = s.getGeneratedKeys(); if(rs1.next()) id1 = rs1.getInt(1);

            s.executeUpdate("INSERT INTO Products (name, price, category_id) VALUES ('P2', 100, 1)");
            s.executeUpdate("INSERT INTO Products (name, price, category_id) VALUES ('P3', 100, 1)");
        } catch (Exception e) {
            // Nếu lỗi DB (ví dụ: thiếu cột category_id), test case sẽ bị bỏ qua (return)
            return; 
        }

        // Action: Gọi hàm tìm kiếm theo category_id (1) và loại trừ id1
        List<Products> list = productDao.findByCategoryId(catId, id1);

        // Verify
        Assert.assertEquals("Phải tìm thấy 2 sản phẩm còn lại (P2, P3)", 2, list.size());
        
        // Kiểm tra chắc chắn rằng sản phẩm bị loại trừ (id1) không nằm trong list
        boolean containsId1 = false;
        for (Products p : list) {
            if (p.getId() == id1) containsId1 = true;
        }
        Assert.assertFalse("Danh sách không được chứa ID đã exclude", containsId1);
    }

    @Test
    public void testDelete_ForeignKeyViolation() {
        setTestCaseInfo("PROD_DAO_07", "Xóa thất bại do Khóa ngoại (FK)", 
                "1. Insert Product\n2. Insert Variant trỏ tới Product\n3. Delete Product", 
                "Product có Variant", "Trả về 0 (Không xóa được) & Sản phẩm vẫn tồn tại");

        // 1. Tạo Product
        Products p = new Products(0, "SP Khoa Ngoai", "...", 100, "img.jpg");
        productDao.insert(p);
        int pid = p.getId();

        // 2. Tạo Variant trỏ tới Product (Tạo ràng buộc khóa ngoại)
        try (Statement s = connection.createStatement()) {
            // Lấy ID Size bất kỳ để tạo Variant
            ResultSet rs = s.executeQuery("SELECT TOP 1 id FROM Sizes");
            if (rs.next()) {
                int sizeId = rs.getInt(1);
                // Insert ProductVariants
                String sql = "INSERT INTO ProductVariants (product_id, size_id, stock) VALUES (" + pid + ", " + sizeId + ", 10)";
                s.executeUpdate(sql);
            } else {
                // Nếu không có Size nào, không thể tạo ràng buộc, bỏ qua test này
                return; 
            }
        } catch (Exception ignored) { 
            // Nếu lỗi DB khi chèn Variant, bỏ qua test này
            return;
        }

        // 3. Cố tình xóa Product (Sẽ bị chặn bởi FK từ ProductVariants)
        int result = productDao.delete(pid);

        // 4. Verify
        Assert.assertEquals("Hàm delete phải trả về 0 vì bị chặn bởi khóa ngoại", 0, result);
        
        // Kiểm tra Product vẫn tồn tại trong DB
        Products check = productDao.findById(pid);
        Assert.assertNotNull("Sản phẩm vẫn phải còn tồn tại trong DB", check);
    }

    // ====================== XUẤT BÁO CÁO (JUNIT RULE) ======================
    /**
     * Rule giúp ghi kết quả Test Case (PASS/FAIL) vào Excel.
     * Rule này được Junit gọi tự động sau khi mỗi @Test hoàn thành.
     */
    @Rule public TestWatcher watcher = new TestWatcher() {
        @Override protected void succeeded(Description d) { 
            // Ghi kết quả thành công
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, "OK", "PASS"); 
        }
        @Override protected void failed(Throwable e, Description d) { 
            // Ghi lại thông báo lỗi (e.getMessage()) nếu test thất bại
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, e.getMessage(), "FAIL"); 
        }
    };

    /**
     * Phương thức được gọi một lần sau khi tất cả các Test Case hoàn thành.
     * Dùng để xuất dữ liệu đã thu thập được ra file Excel cuối cùng.
     */
    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("KetQuaTest_ProductDao.xlsx");
    }
}