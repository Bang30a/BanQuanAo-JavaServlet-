package system;

import util.ExcelTestExporter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;

public class SearchSystemTest {

    WebDriver driver;
    // URL trang chủ
    String homeUrl = "http://localhost:8080/ShopDuck/user/view-products";

    // [CẤU HÌNH] Tốc độ test chậm (3 giây) để dễ quan sát
    final int SLOW_SPEED = 3000;

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
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Search.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Search.xlsx");
    }

    // Hàm làm chậm
    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    // ================================================================
    // CÁC TEST CASE TÌM KIẾM
    // ================================================================

    // --- CASE 1: TÌM KIẾM CÓ KẾT QUẢ (Nhập tay) ---
    @Test
    public void testSearch_Found() {
        setTestCaseInfo(
            "ST_SEARCH_01", 
            "Tìm kiếm có kết quả (Gõ phím)", 
            "1. Nhập 'Áo'\n2. Enter\n3. Check URL & Số lượng SP > 0", 
            "Keyword: Áo", 
            "Hiển thị danh sách sản phẩm liên quan đến 'Áo'"
        );

        // 1. Truy cập trang chủ
        driver.get(homeUrl);
        slowDown();

        // 2. Nhập từ khóa vào ô tìm kiếm
        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.clear();
        searchInput.sendKeys("Áo");
        slowDown();
        
        // 3. Nhấn Enter để submit form
        searchInput.sendKeys(Keys.ENTER);
        slowDown(); // Chờ trang kết quả load

        // 4. Kiểm tra kết quả
        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;
        
        // Check URL có chứa tham số keyword
        Assert.assertTrue("URL không chứa tham số keyword!", currentUrl.contains("keyword="));

        // Check số lượng sản phẩm hiển thị
        List<WebElement> products = driver.findElements(By.className("product-card"));
        int productCount = products.size();
        this.currentActual += " | Tìm thấy: " + productCount + " sản phẩm.";
        
        Assert.assertTrue("Lỗi: Tìm 'Áo' mà không ra sản phẩm nào!", productCount > 0);
    }

    // --- CASE 2: TÌM KIẾM KHÔNG CÓ KẾT QUẢ ---
    @Test
    public void testSearch_NotFound() {
        setTestCaseInfo(
            "ST_SEARCH_02", 
            "Tìm kiếm không có kết quả", 
            "1. Nhập từ khóa rác 'xyz123'\n2. Enter\n3. Check thông báo lỗi", 
            "Keyword: xyz123_khong_co_dau", 
            "Hiện thông báo 'Không tìm thấy' / Danh sách rỗng"
        );

        // 1. Truy cập trang chủ
        driver.get(homeUrl);
        slowDown();

        // 2. Nhập từ khóa rác
        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.sendKeys("xyz123_khong_co_dau");
        slowDown();
        
        // 3. Nhấn Enter
        searchInput.sendKeys(Keys.ENTER);
        slowDown();

        // 4. Kiểm tra kết quả
        // Đảm bảo không có sản phẩm nào hiện ra
        int productCount = driver.findElements(By.className("product-card")).size();
        Assert.assertEquals("Lỗi: Lẽ ra không được hiện sản phẩm nào!", 0, productCount);

        // Kiểm tra thông báo "Không tìm thấy"
        boolean hasMessage = false;
        try {
            // Tìm theo text hiển thị
            WebElement msg = driver.findElement(By.xpath("//*[contains(text(), 'Không tìm thấy') or contains(text(), '0 kết quả')]"));
            hasMessage = msg.isDisplayed();
            this.currentActual = "Thông báo: " + msg.getText();
        } catch (Exception e) {
            this.currentActual = "Không thấy thông báo 'Không tìm thấy'";
        }
        
        Assert.assertTrue("Lỗi: Không hiện thông báo 'Không tìm thấy sản phẩm'!", hasMessage);
    }

    // --- CASE 3: BỎ TRỐNG TỪ KHÓA ---
    @Test
    public void testSearch_EmptyKeyword() {
        setTestCaseInfo(
            "ST_SEARCH_03", 
            "Bỏ trống từ khóa (Validation)", 
            "1. Để trống ô search\n2. Nhấn nút Tìm\n3. Check URL không đổi", 
            "Keyword: (rỗng)", 
            "Trình duyệt chặn submit, vẫn ở trang cũ"
        );

        // 1. Truy cập trang chủ
        driver.get(homeUrl);
        slowDown();

        String urlBefore = driver.getCurrentUrl();
        
        // 2. Click nút Search (kính lúp) mà không nhập gì
        WebElement searchBtn = driver.findElement(By.cssSelector(".header-search button"));
        searchBtn.click();
        
        slowDown(); // Chờ xem trang có reload không
        
        // 3. Kiểm tra kết quả
        String urlAfter = driver.getCurrentUrl();
        this.currentActual = "URL sau khi click: " + urlAfter;

        // Nếu input có 'required', form sẽ không submit -> URL không thay đổi
        Assert.assertFalse("Lỗi: Form cho phép submit rỗng!", urlAfter.contains("keyword="));
        Assert.assertEquals("Trang đã bị chuyển đi!", urlBefore, urlAfter);
    }

    // --- CASE 4: TÌM KIẾM BẰNG MENU CÓ SẴN (Navigation) ---
    @Test
    public void testSearch_ViaMenu() {
        setTestCaseInfo(
            "ST_SEARCH_04", 
            "Tìm kiếm qua Menu Mục lục", 
            "1. Click Menu 'BỘ SƯU TẬP' (để mở dropdown)\n2. Click mục con 'Áo Hoodie'\n3. Check kết quả", 
            "Chọn: Áo Hoodie", 
            "Chuyển trang tìm kiếm & Hiện sản phẩm Hoodie"
        );

        // 1. Truy cập trang chủ
        driver.get(homeUrl);
        slowDown();

        try {
            // 2. Tìm Menu cha "BỘ SƯU TẬP" và click để nó xổ xuống
            WebElement parentMenu = driver.findElement(By.partialLinkText("BỘ SƯU TẬP"));
            parentMenu.click();
            System.out.println(">> Đã click menu cha: BỘ SƯU TẬP");
            slowDown(); // Chờ hiệu ứng dropdown của Bootstrap
            
            // 3. Tìm Menu con "Áo Hoodie" và click
            WebElement childMenu = driver.findElement(By.partialLinkText("Áo Hoodie"));
            childMenu.click();
            System.out.println(">> Đã click menu con: Áo Hoodie");
            slowDown(); // Chờ load trang kết quả

            // 4. Kiểm tra kết quả
            String currentUrl = driver.getCurrentUrl();
            this.currentActual = "URL hiện tại: " + currentUrl;

            // Kiểm tra URL có chứa từ khóa
            Assert.assertTrue("URL không đúng format search!", currentUrl.contains("keyword="));
            
            // Kiểm tra xem có sản phẩm nào hiện ra không (ít nhất 1 cái)
            int count = driver.findElements(By.className("product-card")).size();
            this.currentActual += " | Số SP tìm thấy: " + count;
            
            Assert.assertTrue("Lỗi: Không tìm thấy sản phẩm nào khi click menu 'Áo Hoodie'!", count > 0);

        } catch (Exception e) {
            this.currentActual = "Lỗi thao tác menu: " + e.getMessage();
            Assert.fail("Không tìm thấy menu hoặc không click được: " + e.getMessage());
        }
    }

    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}