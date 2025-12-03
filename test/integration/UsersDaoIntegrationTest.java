package integration;

import dao.UsersDao;
import entity.Users;
import util.ExcelTestExporter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class UsersDaoIntegrationTest {

    private Connection connection;
    private UsersDao usersDao;

    // CẤU HÌNH DB TEST (Thay đổi nếu cần)
    private final String dbUrl = "jdbc:sqlserver://BANGGG:1433;databaseName=shopduck_test;encrypt=false;trustServerCertificate=true";
    private final String dbUser = "sa";
    private final String dbPass = "123456";

    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        connection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        
        // Clean DB trước khi test
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Users"); 
        }
        usersDao = new UsersDao(connection);
    }

    @After
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    @Test
    public void testLogin_Success() {
        setTestCaseInfo("DAO_01", "Login thành công", "Insert -> Login", "User: test, Pass: 123", "User Object");
        usersDao.insert(new Users(0, "test", "123", "Test", "t@m.c", "user"));
        Assert.assertNotNull(usersDao.login("test", "123"));
    }

    @Test
    public void testLogin_Fail() {
        setTestCaseInfo("DAO_02", "Login thất bại", "Login sai pass", "Pass: WRONG", "NULL");
        usersDao.insert(new Users(0, "test", "123", "Test", "t@m.c", "user"));
        Assert.assertNull(usersDao.login("test", "WRONG"));
    }

    @Test
    public void testCheckUserExists() {
        setTestCaseInfo("DAO_03", "Check tồn tại", "Insert A -> Check A", "User: A", "TRUE");
        usersDao.insert(new Users(0, "A", "1", "A", "a@m.c", "user"));
        Assert.assertTrue(usersDao.checkUserExists("A"));
    }

    @Test
    public void testCRUD_Flow() {
        setTestCaseInfo("DAO_04", "Full Flow CRUD", "Insert -> Update -> Delete", "Data Change", "Success");
        
        // Insert
        Users u = new Users(0, "crud", "1", "Original", "c@m.c", "user");
        usersDao.insert(u);
        Users saved = usersDao.login("crud", "1");
        
        // Update
        saved.setFullname("Changed");
        usersDao.updateUser(saved);
        Assert.assertEquals("Changed", usersDao.login("crud", "1").getFullname());

        // Delete
        usersDao.deleteUser(saved.getId());
        Assert.assertNull(usersDao.login("crud", "1"));
    }
    
    @Rule public TestWatcher watcher = new TestWatcher() {
        @Override protected void succeeded(Description d) { ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "OK", "PASS"); }
        @Override protected void failed(Throwable e, Description d) { ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, e.getMessage(), "FAIL"); }
    };

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_UsersDao.xlsx"); }
}