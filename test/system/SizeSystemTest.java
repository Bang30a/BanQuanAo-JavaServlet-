package system;

import util.ExcelTestExporter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;
import java.util.Random;

// QUAN TRỌNG: Chạy theo thứ tự Create -> Update -> Delete
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SizeSystemTest {

    WebDriver driver;
    
    // Cấu hình đường dẫn
    String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    String dashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp";

    // --- CẤU HÌNH TEST DATA ---
    // Tạo số ngẫu nhiên để đảm bảo mỗi lần chạy là một size XXL mới
    static int uniqueSeed = new Random().nextInt(9000) + 1000;
    
    String initialSizeName = "XXL_" + uniqueSeed;       
    String updatedSizeName = "XXL_Updated_" + uniqueSeed;

    // Tốc độ test (ms)
    final int SLOW_SPEED = 1500; 

    // --- BIẾN BÁO CÁO EXCEL ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    private String currentActual = "";

    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, currentActual, "PASS");
        }
        @Override
        protected void failed(Throwable e, Description description) {
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "Lỗi: " + e.getMessage(), "FAIL");
        }
    };

    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Sizes.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Sizes.xlsx");
    }

    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        
        // --- 1. LUỒNG ĐĂNG NHẬP ---
        driver.get(loginUrl);
        slowDown();
        
        driver.findElement(By.name("username")).sendKeys("admin");
        slowDown();
        driver.findElement(By.name("password")).sendKeys("admin123");
        slowDown();
        
        try {
            WebElement loginBtn;
            try {
                loginBtn = driver.findElement(By.xpath("//form[contains(@action, 'Login')]//button"));
            } catch (Exception e) {
                loginBtn = driver.findElement(By.className("btn-login"));
            }
            loginBtn.click();
        } catch (Exception e) {
            System.out.println("Không click được nút login: " + e.getMessage());
        }
        slowDown();
        
        // Đảm bảo đã vào Dashboard
        if (!driver.getCurrentUrl().contains("admin") && !driver.getCurrentUrl().contains("dashboard")) {
            driver.get(dashboardUrl);
            slowDown();
        }
    }

    // --- TEST 01: CREATE SIZE XXL ---
    @Test
    public void test01_Size_Create() {
        setTestCaseInfo(
            "ST_SIZE_01", 
            "Thêm Size XXL Mới", 
            "1. Dashboard -> Menu Quản lý SP -> Kích cỡ\n2. Click Thêm Size\n3. Nhập '" + initialSizeName + "'\n4. Lưu", 
            "Input: " + initialSizeName, 
            "Size mới xuất hiện trong danh sách"
        );

        navigateToSizePageViaMenu(); 

        driver.findElement(By.className("btn-add")).click();
        slowDown();

        fillInput("sizeLabel", initialSizeName);
        slowDown();

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        slowDown(); 

        boolean found = findRowByText(initialSizeName);
        this.currentActual = "Tìm thấy size '" + initialSizeName + "': " + found;
        Assert.assertTrue("Lỗi: Thêm xong nhưng không tìm thấy trong danh sách!", found);
    }

    // --- TEST 02: UPDATE SIZE ---
    @Test
    public void test02_Size_Update() {
        setTestCaseInfo(
            "ST_SIZE_02", 
            "Sửa tên Size", 
            "1. Tìm size '" + initialSizeName + "'\n2. Click Sửa\n3. Đổi thành '" + updatedSizeName + "'\n4. Lưu", 
            "Old: " + initialSizeName + " -> New: " + updatedSizeName, 
            "Tên size được cập nhật thành công"
        );

        navigateToSizePageViaMenu(); 

        boolean clickEditSuccess = findAndClickButton(initialSizeName, "btn-edit");
        Assert.assertTrue("Không tìm thấy nút Sửa cho size: " + initialSizeName, clickEditSuccess);
        
        slowDown(); 

        fillInput("sizeLabel", updatedSizeName);
        slowDown();

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        slowDown();

        boolean foundNew = findRowByText(updatedSizeName);
        boolean foundOld = findRowByText(initialSizeName);
        
        this.currentActual = "Tìm thấy mới: " + foundNew + " | Tìm thấy cũ: " + foundOld;
        Assert.assertTrue("Lỗi: Không tìm thấy tên size mới sau khi sửa!", foundNew);
        Assert.assertFalse("Lỗi: Tên size cũ vẫn còn tồn tại!", foundOld);
    }

    // --- TEST 03: DELETE SIZE ---
    @Test
    public void test03_Size_Delete() {
        setTestCaseInfo(
            "ST_SIZE_03", 
            "Xóa Size", 
            "1. Tìm size '" + updatedSizeName + "'\n2. Click Xóa\n3. Xác nhận Alert\n4. Kiểm tra biến mất", 
            "Target: " + updatedSizeName, 
            "Size bị xóa khỏi danh sách"
        );

        navigateToSizePageViaMenu(); 

        boolean clickDeleteSuccess = findAndClickButton(updatedSizeName, "btn-delete");
        Assert.assertTrue("Không tìm thấy nút Xóa cho size: " + updatedSizeName, clickDeleteSuccess);

        try {
            slowDown(); 
            driver.switchTo().alert().accept(); 
            slowDown(); 
        } catch (Exception e) {}

        boolean found = findRowByText(updatedSizeName);
        this.currentActual = "Vẫn tìm thấy size '" + updatedSizeName + "': " + found;
        Assert.assertFalse("Lỗi: Xóa xong nhưng vẫn tìm thấy dữ liệu trong bảng!", found);
    }

    // --- CÁC HÀM HỖ TRỢ (UTILITIES) ---

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
        this.currentActual = "Chưa hoàn thành";
    }

    // [QUAN TRỌNG] Hàm điều hướng dùng Menu Sidebar và Iframe
    private void navigateToSizePageViaMenu() {
        // 1. Luôn thoát ra khỏi iframe (nếu đang ở trong) để tương tác với menu bên trái
        driver.switchTo().defaultContent();

        // Kiểm tra nếu chưa ở Dashboard thì quay về
        if (!driver.getCurrentUrl().contains("admin") && !driver.getCurrentUrl().contains("dashboard")) {
             driver.get(dashboardUrl);
             slowDown();
        }

        try {
            // 2. Click menu cha "Quản lý sản phẩm" (Dựa trên text hiển thị)
            WebElement productMenu = driver.findElement(By.xpath("//a[contains(text(), 'Quản lý sản phẩm')]"));
            productMenu.click();
            slowDown();

            // 3. Click menu con "Kích cỡ" (Theo HTML bạn cung cấp là 'Kích cỡ')
            WebElement sizeLink = driver.findElement(By.xpath("//a[contains(text(), 'Kích cỡ')]"));
            sizeLink.click(); 
            slowDown();

            // 4. QUAN TRỌNG: Chuyển driver vào iframe "mainFrame" để tương tác với nội dung bên trong
            driver.switchTo().frame("mainFrame");

        } catch (Exception e) {
            System.out.println("Lỗi điều hướng menu: " + e.getMessage());
            // Fallback: Nếu không bấm được menu thì thử vào trực tiếp URL
            driver.get("http://localhost:8080/ShopDuck/admin/SizesManagerServlet?action=List");
        }
    }

    private void fillInput(String nameAttr, String value) {
        try {
            WebElement input = driver.findElement(By.name(nameAttr));
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            input.sendKeys(Keys.BACK_SPACE);
            slowDown();
            input.sendKeys(value);
        } catch (Exception e) {
            System.out.println("Lỗi nhập liệu vào field " + nameAttr + ": " + e.getMessage());
        }
    }

    private boolean findRowByText(String textValue) {
        try {
            // Tìm trong bảng có id="sizeTable"
            int count = driver.findElements(By.xpath("//table[@id='sizeTable']//td[contains(., '" + textValue + "')]")).size();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean findAndClickButton(String textToFind, String buttonClass) {
        try {
            List<WebElement> rows = driver.findElements(By.xpath("//tr[td[contains(., '" + textToFind + "')]]"));
            
            if (!rows.isEmpty()) {
                System.out.println(">> Đã tìm thấy row chứa '" + textToFind + "'. Click nút class='" + buttonClass + "'");
                WebElement btn = rows.get(0).findElement(By.xpath(".//a[contains(@class, '" + buttonClass + "')]"));
                btn.click();
                return true;
            }
        } catch (Exception e) {
            System.out.println("Lỗi tìm nút: " + e.getMessage());
        }
        return false;
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}