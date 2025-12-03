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

public class SizeDaoIntegrationTest {

    private Connection connection;
    private SizeDao dao;

    // --- CẤU HÌNH DB ---
    private final String serverName = "BANGGG";
    private final String portNumber = "1433";
    private final String dbName = "shopduck";
    private final String userID = "sa";
    private final String password = "123456";

    private String fullDbUrl;

    // ID dùng để cleanup
    private int createdSizeId = 0;

    // --- REPORT VARS ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {

        fullDbUrl = "jdbc:sqlserver://" + serverName + ":" + portNumber +
                ";databaseName=" + dbName +
                ";encrypt=false;trustServerCertificate=true;loginTimeout=30";

        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(fullDbUrl, userID, password);

        dao = new SizeDao(connection);
    }

    @After
    public void tearDown() throws Exception {
        if (createdSizeId > 0) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("DELETE FROM Sizes WHERE id = " + createdSizeId);
            } catch (Exception ignored) {}
            createdSizeId = 0;
        }

        if (connection != null && !connection.isClosed()) connection.close();
    }

    // Lấy id mới nhất trong bảng Size (dùng để hỗ trợ test insert)
    private int getLatestSizeId() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT TOP 1 id FROM Sizes ORDER BY id DESC")) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ignored) {}
        return -1;
    }

    // ========================== TEST CASE ===============================

    // TEST 1 - INSERT
    @Test
    public void testInsertSize() throws Exception {
        setTestCaseInfo("SIZE_DAO_01", "Thêm Size",
                "Insert size_label='Test Size X'",
                "Label='Test Size X'",
                "Return true");

        Size size = new Size(0, "Test Size X");

        boolean result = dao.insertSize(size);
        Assert.assertTrue("Insert phải thành công", result);

        createdSizeId = getLatestSizeId();
        Assert.assertTrue("ID phải > 0", createdSizeId > 0);
    }

    // TEST 2 - FIND BY ID
    @Test
    public void testFindById() throws Exception {
        setTestCaseInfo("SIZE_DAO_02", "Tìm theo ID",
                "Insert tạm -> findById",
                "ID tạo tạm",
                "Return đúng Size");

        // Insert tạm
        Size s = new Size(0, "Find Test Size");
        dao.insertSize(s);
        createdSizeId = getLatestSizeId();

        Size found = dao.getSizeById(createdSizeId);

        Assert.assertNotNull(found);
        Assert.assertEquals("Find Test Size", found.getSizeLabel());
    }

    // TEST 3 - UPDATE
    @Test
    public void testUpdateSize() throws Exception {
        setTestCaseInfo("SIZE_DAO_03", "Update Size",
                "Insert tạm -> Update label",
                "New label='Updated Size'",
                "Update thành công");

        // Insert tạm
        Size s = new Size(0, "Temp Size");
        dao.insertSize(s);
        createdSizeId = getLatestSizeId();

        // Update
        s.setId(createdSizeId);
        s.setSizeLabel("Updated Size");

        boolean result = dao.updateSize(s);
        Assert.assertTrue(result);

        Size updated = dao.getSizeById(createdSizeId);
        Assert.assertEquals("Updated Size", updated.getSizeLabel());
    }

    // TEST 4 - GET ALL
    @Test
    public void testGetAllSizes() throws Exception {
        setTestCaseInfo("SIZE_DAO_04", "Get tất cả Size",
                "getAllSizes()",
                "Không tham số",
                "List > 0");

        List<Size> list = dao.getAllSizes();
        Assert.assertFalse("Danh sách không được rỗng", list.isEmpty());
    }

    // TEST 5 - DELETE
    @Test
    public void testDeleteSize() throws Exception {
        setTestCaseInfo("SIZE_DAO_05", "Xóa Size",
                "Insert -> Delete -> find lại",
                "Temp Size Id",
                "findById trả null");

        // Insert tạm
        Size s = new Size(0, "Delete Test Size");
        dao.insertSize(s);
        int deleteId = getLatestSizeId();

        // Delete
        boolean deleted = dao.deleteSize(deleteId);
        Assert.assertTrue(deleted);

        // Verify
        Size result = dao.getSizeById(deleteId);
        Assert.assertNull(result);

        createdSizeId = 0; // tránh cleanup double
    }

    // ====================== EXCEL EXPORT ============================

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(
                    currentId, currentName, currentSteps, currentData, currentExpected,
                    "OK", "PASS"
            );
        }

        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(
                    currentId, currentName, currentSteps, currentData, currentExpected,
                    e.getMessage(), "FAIL"
            );
        }
    };

    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("KetQuaTest_SizeDao.xlsx");
    }
}
