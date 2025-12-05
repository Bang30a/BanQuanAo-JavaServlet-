package integration;

import dao.SizeDao;
import entity.Size;
import util.ExcelTestExporter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Random;

/**
 * Kiểm tra tích hợp (Integration Test) cho lớp SizeDao.
 * Class này kiểm thử các nghiệp vụ CRUD cơ bản của bảng Sizes.
 */
public class SizeDaoIntegrationTest {

    private Connection connection;
    private SizeDao dao;

    // --- CẤU HÌNH KẾT NỐI DATABASE TEST ---
    private final String fullDbUrl = "jdbc:sqlserver://BANGGG:1433;databaseName=shopduck_test;encrypt=false;trustServerCertificate=true;loginTimeout=30";
    private final String userID = "sa";
    private final String password = "123456";

    /** ID của Size được tạo trong mỗi Test Case, dùng để cleanup trong tearDown(). */
    private int createdSizeId = 0;

    // --- BIẾN GHI LOG TEST CASE (Dùng cho ExcelTestExporter) ---
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    /**
     * Thiết lập thông tin chi tiết cho Test Case hiện tại.
     *
     * @param id ID của Test Case.
     * @param name Tên của Test Case.
     * @param steps Các bước thực hiện.
     * @param data Dữ liệu đầu vào/chuẩn bị.
     * @param expected Kết quả mong đợi.
     */
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case: Mở kết nối DB và khởi tạo DAO.
     * @throws Exception nếu có lỗi kết nối.
     */
    @Before
    public void setUp() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(fullDbUrl, userID, password);

        // Khởi tạo DAO với Connection thật
        dao = new SizeDao(connection);
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case: Xóa Size vừa được tạo và đóng kết nối DB.
     * @throws Exception nếu có lỗi đóng kết nối.
     */
    @After
    public void tearDown() throws Exception {
        // CLEANUP: Chỉ xóa size nếu đã được tạo thành công trong Test Case (createdSizeId > 0)
        if (createdSizeId > 0) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DELETE FROM Sizes WHERE id = " + createdSizeId);
            } catch (Exception ignored) {
                System.err.println("WARN: Không thể xóa ID " + createdSizeId + " sau test.");
            }
            createdSizeId = 0; // Reset ID để không bị xóa lại
        }
        
        if (connection != null && !connection.isClosed()) connection.close();
    }

    /**
     * Helper: Lấy ID lớn nhất (ID mới nhất) từ bảng Sizes.
     * Dùng để kiểm tra ID tự tăng sau khi INSERT.
     * @return ID mới nhất, hoặc -1 nếu lỗi/bảng trống.
     */
    private int getLatestSizeId() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 1 id FROM Sizes ORDER BY id DESC")) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return -1;
    }

    // ========================== TEST CASES ===============================

    // TEST 1 - INSERT (CREATE)
    @Test
    public void testInsertSize() throws Exception {
        setTestCaseInfo("SIZE_DAO_01", "Thêm Size mới",
                "Insert size_label='TXX'",
                "Label='TXX'", 
                "Return true & ID > 0");

        Size size = new Size(0, "TXX"); 
        boolean result = dao.insertSize(size);
        Assert.assertTrue("Insert phải thành công", result);

        createdSizeId = getLatestSizeId();
        Assert.assertTrue("ID size mới phải lớn hơn 0", createdSizeId > 0);
    }

    // TEST 2 - FIND BY ID (READ)
    @Test
    public void testFindById() throws Exception {
        setTestCaseInfo("SIZE_DAO_02", "Tìm theo ID",
                "Insert tạm -> findById",
                "ID tạo tạm",
                "Return đúng Size");

        // Insert tạm thời để lấy ID
        Size s = new Size(0, "FIN"); 
        dao.insertSize(s);
        createdSizeId = getLatestSizeId();

        Size found = dao.getSizeById(createdSizeId);

        Assert.assertNotNull("Phải tìm thấy Size vừa insert", found);
        Assert.assertEquals("Kiểm tra label của Size", "FIN", found.getSizeLabel());
    }

    // TEST 3 - UPDATE
    @Test
    public void testUpdateSize() throws Exception {
        setTestCaseInfo("SIZE_DAO_03", "Update Size",
                "Insert tạm -> Update label",
                "New label='UP1'", 
                "Update thành công");

        // 1. Insert tạm
        Size s = new Size(0, "TMP"); 
        dao.insertSize(s);
        createdSizeId = getLatestSizeId();

        // 2. Update object
        s.setId(createdSizeId);
        s.setSizeLabel("UP1"); 

        boolean result = dao.updateSize(s);
        Assert.assertTrue("Update phải trả về true", result);

        // 3. Verify
        Size updated = dao.getSizeById(createdSizeId);
        Assert.assertEquals("Label phải được cập nhật thành 'UP1'", "UP1", updated.getSizeLabel());
    }

    // TEST 4 - GET ALL
    @Test
    public void testGetAllSizes() throws Exception {
        setTestCaseInfo("SIZE_DAO_04", "Get tất cả Size",
                "Insert 2 items (S1, M2) -> getAllSizes()",
                "Không tham số",
                "List chứa 2 items vừa thêm");
        
        // --- CHUẨN BỊ DỮ LIỆU ĐẶC BIỆT CHO TEST NÀY ---
        // Lưu ID để cleanup thủ công, vì logic cleanup tự động không áp dụng cho nhiều item
        int id1 = -1, id2 = -1;
        try (Statement stmt = connection.createStatement()) {
            // Xóa toàn bộ dữ liệu (cần thiết để kiểm tra kích thước chính xác)
            stmt.execute("DELETE FROM Sizes"); 
        }

        // 1. Insert 2 items
        dao.insertSize(new Size(0, "S1"));
        dao.insertSize(new Size(0, "M2"));

        // 2. Lấy lại ID để Cleanup TAY (Bắt 2 ID lớn nhất vừa được sinh ra)
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT TOP 2 id FROM Sizes ORDER BY id DESC");
            if (rs.next()) id1 = rs.getInt(1);
            if (rs.next()) id2 = rs.getInt(1);
        }

        // 3. Thực hiện test
        List<Size> list = dao.getAllSizes();
        Assert.assertEquals("Tổng số Size phải là 2 sau khi dọn dẹp và thêm 2 item", 2, list.size());
        
        // Kiểm tra label (ví dụ)
        Assert.assertTrue("Danh sách phải chứa item 'S1'", list.stream().anyMatch(s -> s.getSizeLabel().equals("S1")));
        
        // --- Cleanup thủ công cho các item vừa tạo ---
        try (Statement stmt = connection.createStatement()) {
            if (id1 > 0) stmt.execute("DELETE FROM Sizes WHERE id = " + id1);
            if (id2 > 0) stmt.execute("DELETE FROM Sizes WHERE id = " + id2);
        }
        // Đảm bảo After() không chạy lại cleanup (vì createdSizeId không được set)
    }

    // TEST 5 - DELETE
    @Test
    public void testDeleteSize() throws Exception {
        setTestCaseInfo("SIZE_DAO_05", "Xóa Size",
                "Insert -> Delete -> find lại",
                "Temp Size Id",
                "findById trả null");

        // 1. Insert tạm
        Size s = new Size(0, "DEL"); 
        dao.insertSize(s);
        int deleteId = getLatestSizeId();

        // 2. Delete
        boolean deleted = dao.deleteSize(deleteId);
        Assert.assertTrue("Delete phải thành công", deleted);

        // 3. Verify
        Size result = dao.getSizeById(deleteId);
        Assert.assertNull("Sau khi xóa, findById phải trả về null", result);

        createdSizeId = 0; // Đánh dấu đã xóa thành công, không cần cleanup trong tearDown()
    }
    
    // TEST 6 - GET BY ID (Không tìm thấy)
    @Test
    public void testFindById_NotFound() throws Exception {
        setTestCaseInfo("SIZE_DAO_06", "Tìm ID không tồn tại",
                "findById(999999)",
                "ID=999999",
                "Return null");

        Size found = dao.getSizeById(999999);
        Assert.assertNull("Tìm ID không tồn tại phải trả về null", found);
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
        ExcelTestExporter.exportToExcel("KetQuaTest_SizeDao.xlsx");
    }
}