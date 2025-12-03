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

// QUAN TRỌNG: Chạy theo thứ tự test01 -> test02 -> test03
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductVariantSystemTest {

    WebDriver driver;
    String loginUrl = "http://localhost:8080/ShopDuck/user/auth/Login.jsp";
    String dashboardUrl = "http://localhost:8080/ShopDuck/admin/dashboard/index.jsp";

    // --- CẤU HÌNH TEST DATA ---
    String targetProductId = "35"; 
    String targetSizeId = "1"; 
    
    static int uniqueSeed = new Random().nextInt(9000) + 1000;
    
    // Dữ liệu ban đầu (Create)
    String initialStock = String.valueOf(uniqueSeed);      
    
    // Dữ liệu sau khi sửa (Update)
    String updatedStock = String.valueOf(uniqueSeed + 1);  

    // Tốc độ test (ms)
    final int SLOW_SPEED = 1500; 

    // --- BIẾN BÁO CÁO EXCEL ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    private String currentActual = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
        this.currentActual = "Chưa hoàn thành";
    }

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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Variant_NoPrice.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Variant_NoPrice.xlsx");
    }

    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        
        // 1. Đăng nhập
        driver.get(loginUrl);
        slowDown();
        
        driver.findElement(By.name("username")).sendKeys("admin");
        slowDown();
        driver.findElement(By.name("password")).sendKeys("admin123");
        slowDown();
        
        try {
            driver.findElement(By.xpath("//form[contains(@action, 'Login')]//button")).click();
        } catch (Exception e) {
            driver.findElement(By.className("btn-login")).click();
        }
        slowDown();
        
        if (!driver.getCurrentUrl().contains("admin")) {
            driver.get(dashboardUrl);
            slowDown();
        }
    }

    // --- TEST 01: CREATE ---
    @Test
    public void test01_Variant_Create() {
        setTestCaseInfo(
            "ST_VAR_01", 
            "Thêm biến thể mới (Không sửa giá)", 
            "1. Menu SP -> Biến thể\n2. Thêm mới\n3. Nhập Stock=" + initialStock + "\n4. Lưu", 
            "Stock: " + initialStock, 
            "Biến thể mới xuất hiện với đúng số lượng tồn kho"
        );

        navigateToVariantPage(); 

        driver.findElement(By.className("btn-add")).click();
        slowDown();

        fillInput("productId", targetProductId);
        fillInput("sizeId", targetSizeId);
        fillInput("stock", initialStock);
        // [ĐÃ SỬA] Không nhập giá vì trường này đã bị disable/readonly
        
        slowDown();

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        slowDown(); 

        // Kiểm tra kết quả
        boolean found = findRowByText(initialStock);
        this.currentActual = "Tìm thấy Stock=" + initialStock + ": " + found;
        Assert.assertTrue("Lỗi: Thêm xong nhưng không tìm thấy!", found);
    }

    // --- TEST 02: UPDATE ---
    @Test
    public void test02_Variant_Update() {
        setTestCaseInfo(
            "ST_VAR_02", 
            "Sửa tồn kho biến thể (Chỉ sửa Stock)", 
            "1. Tìm Stock=" + initialStock + "\n2. Sửa thành Stock=" + updatedStock + "\n3. Lưu", 
            "Old Stock: " + initialStock + " -> New Stock: " + updatedStock, 
            "Tồn kho được cập nhật thành công"
        );

        navigateToVariantPage(); 

        boolean clickEditSuccess = findAndClickButton(initialStock, "btn-edit");
        if (!clickEditSuccess) {
            clickEditSuccess = findAndClickLinkByText(initialStock, "Sửa");
        }
        Assert.assertTrue("Không tìm thấy nút Sửa cho item Stock=" + initialStock, clickEditSuccess);
        
        slowDown(); 

        // [ĐÃ SỬA] Chỉ sửa Stock, không đụng vào Price
        fillInput("stock", updatedStock);
        slowDown();

        driver.findElement(By.cssSelector("button[type='submit']")).click();
        slowDown();

        boolean foundNew = findRowByText(updatedStock);
        this.currentActual = "Tìm thấy Stock mới (" + updatedStock + "): " + foundNew;
        Assert.assertTrue("Lỗi: Cập nhật xong nhưng không thấy dữ liệu mới!", foundNew);
    }

    // --- TEST 03: DELETE ---
    @Test
    public void test03_Variant_Delete() {
        setTestCaseInfo(
            "ST_VAR_03", 
            "Xóa biến thể", 
            "1. Tìm Stock=" + updatedStock + "\n2. Xóa\n3. Check biến mất", 
            "Target: Stock " + updatedStock, 
            "Biến thể bị xóa khỏi danh sách"
        );

        navigateToVariantPage(); 

        boolean clickDeleteSuccess = findAndClickButton(updatedStock, "btn-delete");
        if (!clickDeleteSuccess) {
             clickDeleteSuccess = findAndClickLinkByText(updatedStock, "Xóa");
        }
        Assert.assertTrue("Không tìm thấy nút Xóa cho item Stock=" + updatedStock, clickDeleteSuccess);

        try {
            slowDown(); 
            driver.switchTo().alert().accept();
            slowDown(); 
        } catch (Exception e) {}

        boolean found = findRowByText(updatedStock);
        this.currentActual = "Vẫn tìm thấy Stock=" + updatedStock + ": " + found;
        Assert.assertFalse("Lỗi: Xóa xong nhưng vẫn tìm thấy dữ liệu!", found);
    }

    // --- UTILITIES ---

    private void navigateToVariantPage() {
        try {
            driver.findElement(By.xpath("//a[contains(text(), 'Quản lý sản phẩm')]")).click();
            slowDown();
            WebElement link = driver.findElement(By.linkText("Biến thể"));
            link.click(); 
            slowDown();
        } catch (Exception e) {
        }
        driver.switchTo().frame("mainFrame");
    }

    private void fillInput(String nameAttr, String value) {
        try {
            WebElement input = driver.findElement(By.name(nameAttr));
            // Kiểm tra nếu input không phải readonly mới nhập
            if (input.getAttribute("readonly") == null) {
                input.click();
                input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                input.sendKeys(Keys.BACK_SPACE);
                slowDown();
                input.sendKeys(value);
            }
        } catch (Exception e) {
            System.out.println("Không thể nhập liệu vào: " + nameAttr);
        }
    }

    private boolean findRowByText(String textValue) {
        while (true) {
            int count = driver.findElements(By.xpath("//td[contains(., '" + textValue + "')]")).size();
            if (count > 0) return true; 

            if (!goToNextPage()) break; 
        }
        return false;
    }

    private boolean findAndClickButton(String textToFind, String buttonClass) {
        while (true) {
            List<WebElement> rows = driver.findElements(By.xpath("//tr[td[contains(., '" + textToFind + "')]]"));
            
            if (!rows.isEmpty()) {
                System.out.println(">> Đã tìm thấy '" + textToFind + "'. Click nút: " + buttonClass);
                try {
                    WebElement btn = rows.get(0).findElement(By.xpath(".//a[contains(@class, '" + buttonClass + "')]"));
                    btn.click();
                } catch (Exception e) {
                    rows.get(0).findElement(By.tagName("a")).click(); 
                }
                return true;
            }

            if (!goToNextPage()) return false; 
        }
    }

    private boolean findAndClickLinkByText(String textToFind, String linkText) {
        while (true) {
            List<WebElement> rows = driver.findElements(By.xpath("//tr[td[contains(., '" + textToFind + "')]]"));
            if (!rows.isEmpty()) {
                WebElement btn = rows.get(0).findElement(By.xpath(".//a[contains(text(), '" + linkText + "')]"));
                btn.click();
                return true;
            }
            if (!goToNextPage()) return false;
        }
    }

    private boolean goToNextPage() {
        List<WebElement> nextBtns = driver.findElements(By.xpath("//li[not(contains(@class, 'disabled'))]/a[contains(text(), 'Sau') or contains(text(), 'Next') or contains(text(), '»')]"));
        if (!nextBtns.isEmpty()) {
            System.out.println(">> Chuyển trang kế tiếp...");
            nextBtns.get(0).click();
            slowDown(); 
            return true;
        }
        return false;
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}