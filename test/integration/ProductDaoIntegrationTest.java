package integration;

import dao.ProductDao;
import entity.Products;
import util.ExcelTestExporter; // <-- Import class tiện ích

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

// Import JUNIT
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class ProductDaoIntegrationTest {

    private Connection connection;
    private ProductDao productDao;

    // --- CẤU HÌNH DB ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String instance = "";
    private final String userID = "sa";
    private final String password = "123456";
    private final String dbName = "shopduck_test"; 

    // === CẤU HÌNH BÁO CÁO (Biến instance) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    // Đã xóa list finalReportData

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        // 1. Kết nối SQL Server
        String url;
        if (instance == null || instance.trim().isEmpty()) {
            url = "jdbc:sqlserver://" + serverName + ":" + portNumber + ";databaseName=" + dbName;
        } else {
            url = "jdbc:sqlserver://" + serverName + ":" + portNumber + "\\" + instance + ";databaseName=" + dbName;
        }
        url += ";encrypt=false;trustServerCertificate=true;loginTimeout=30";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(url, userID, password);

        // 2. Dọn dẹp dữ liệu (Quan trọng: Xóa bảng con trước, bảng cha sau)
        try (Statement stmt = connection.createStatement()) {
            // Xóa biến thể trước (vì nó tham chiếu đến Product)
            // Lưu ý: Nếu bảng ProductVariants chưa tạo thì lệnh này có thể lỗi, ta dùng try-catch lờ đi nếu cần
            try { stmt.execute("DELETE FROM ProductVariants"); } catch (Exception e) {}
            
            // Xóa sản phẩm
            stmt.execute("DELETE FROM Products");
            
            // Reset ID về 0
            try { stmt.execute("DBCC CHECKIDENT ('Products', RESEED, 0)"); } catch (Exception e) {}
        }

        // 3. Khởi tạo DAO
        productDao = new ProductDao(connection);
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testInsertAndFindById() {
        setTestCaseInfo("PROD_DAO_01", "Thêm và Tìm kiếm theo ID", 
                "1. Insert Product\n2. Find by generated ID", 
                "Name: Ao Thun", "Trả về object Product đúng tên");

        // 1. Insert
        Products p = new Products(0, "Ao Thun", "Chat lieu cotton", 100000, "img.jpg");
        int row = productDao.insert(p); // Hàm insert của bạn tự cập nhật ID vào object p
        
        Assert.assertTrue("Insert phải trả về > 0", row > 0);
        Assert.assertTrue("ID phải được sinh ra (>0)", p.getId() > 0);

        // 2. Find
        Products found = productDao.findById(p.getId());
        
        Assert.assertNotNull("Phải tìm thấy sản phẩm", found);
        Assert.assertEquals("Ao Thun", found.getName());
        Assert.assertEquals(100000, found.getPrice(), 0.01);
    }

    @Test
    public void testSearchByKeyword() {
        setTestCaseInfo("PROD_DAO_02", "Tìm kiếm sản phẩm (Tên & Mô tả)", 
                "1. Insert 3 SP\n2. Tìm 'jean' (có trong tên)\n3. Tìm 'cotton' (có trong mô tả)", 
                "Data: Quan Jean, Ao Thun...", "Kết quả list trả về đúng số lượng");

        // Chuẩn bị dữ liệu
        productDao.insert(new Products(0, "Quan Jean Xanh", "Vai bo xin", 200000, "1.jpg"));
        productDao.insert(new Products(0, "Ao Thun Trang", "100% Cotton", 90000, "2.jpg"));
        productDao.insert(new Products(0, "Ao Khoac", "Jean den", 300000, "3.jpg"));

        // Test 1: Tìm theo tên "Jean" -> Mong đợi: Quan Jean Xanh, Ao Khoac (có chữ Jean trong mô tả)
        List<Products> list1 = productDao.searchByKeyword("Jean");
        Assert.assertEquals("Phải tìm thấy 2 sản phẩm có chữ Jean", 2, list1.size());

        // Test 2: Tìm theo mô tả "Cotton" -> Mong đợi: Ao Thun Trang
        List<Products> list2 = productDao.searchByKeyword("cotton"); // Test chữ thường
        Assert.assertEquals("Phải tìm thấy 1 sản phẩm Cotton", 1, list2.size());
        Assert.assertEquals("Ao Thun Trang", list2.get(0).getName());
        
        // Test 3: Tìm không thấy
        List<Products> list3 = productDao.searchByKeyword("XYZ123");
        Assert.assertEquals("Không tìm thấy gì", 0, list3.size());
    }

    @Test
    public void testUpdateProduct() {
        setTestCaseInfo("PROD_DAO_03", "Cập nhật sản phẩm", 
                "1. Insert\n2. Update giá & tên\n3. Find lại", 
                "Old: 100k, New: 150k", "Dữ liệu mới được lưu");

        Products p = new Products(0, "Ao Cu", "Mo ta cu", 100000, "old.jpg");
        productDao.insert(p);

        // Update
        p.setName("Ao Moi");
        p.setPrice(150000);
        int result = productDao.update(p);

        Assert.assertTrue("Update trả về > 0", result > 0);

        // Verify
        Products updated = productDao.findById(p.getId());
        Assert.assertEquals("Ao Moi", updated.getName());
        Assert.assertEquals(150000, updated.getPrice(), 0.01);
    }

    @Test
    public void testDeleteProduct() {
        setTestCaseInfo("PROD_DAO_04", "Xóa sản phẩm", 
                "1. Insert\n2. Delete\n3. Find lại", 
                "ID vừa tạo", "Find trả về null");

        Products p = new Products(0, "Ao Xoa", "...", 50000, "del.jpg");
        productDao.insert(p);

        // Delete
        int result = productDao.delete(p.getId());
        Assert.assertTrue("Delete trả về > 0", result > 0);

        // Verify
        Products deleted = productDao.findById(p.getId());
        Assert.assertNull("Sản phẩm đã xóa phải null", deleted);
    }

    // === XUẤT EXCEL MỚI ===
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "OK", "PASS");
        }
        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, e.getMessage(), "FAIL");
        }
    };

    @AfterClass
    public static void exportReport() {
        // Xuất ra file .xlsx
        ExcelTestExporter.exportToExcel("KetQuaTest_ProductDao.xlsx");
    }
}