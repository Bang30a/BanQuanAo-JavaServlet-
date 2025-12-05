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

/**
 * System Test (Selenium) cho chức năng Tìm kiếm sản phẩm (Search).
 * Kiểm thử các kịch bản tìm kiếm cơ bản, không có kết quả, bỏ trống và xử lý ký tự đặc biệt.
 */
public class SearchSystemTest {

    WebDriver driver;
    // URL trang chủ (điểm bắt đầu của tìm kiếm)
    private final String homeUrl = "http://localhost:8080/ShopDuck/user/view-products";

    // [CẤU HÌNH] Tốc độ làm chậm (milliseconds) giữa các bước Selenium để dễ dàng quan sát
    private final int SLOW_SPEED = 3000;

    // --- BIẾN GHI LOG TEST CASE (Dùng cho ExcelTestExporter) ---
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    private String currentActual = "";

    /**
     * Thiết lập thông tin chi tiết cho Test Case hiện tại.
     *
     * @param id ID của Test Case.
     * @param name Tên của Test Case.
     * @param steps Các bước thực hiện Selenium.
     * @param data Dữ liệu/điều kiện đầu vào.
     * @param expected Kết quả mong đợi.
     */
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
        this.currentActual = "Chưa hoàn thành";
    }

    /**
     * Rule giúp ghi kết quả Test Case (PASS/FAIL) vào Excel sau khi mỗi @Test hoàn thành.
     */
    @Rule
    public TestWatcher watcher = new TestWatcher() {
        @Override
        protected void succeeded(Description description) {
            // Ghi kết quả thành công
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, currentActual, "PASS");
        }
        @Override
        protected void failed(Throwable e, Description description) {
            // Ghi kết quả thất bại, sử dụng thông báo lỗi của Exception
            ExcelTestExporter.addResult(currentId, currentName, currentSteps, currentData, currentExpected, "Lỗi: " + e.getMessage(), "FAIL");
        }
    };

    /**
     * Phương thức được gọi một lần sau khi tất cả các Test Case hoàn thành.
     * Dùng để xuất dữ liệu đã thu thập được ra file Excel cuối cùng.
     */
    @AfterClass
    public static void exportReport() {
        ExcelTestExporter.exportToExcel("BaoCao_SystemTest_Search.xlsx");
        System.out.println(">> Xuất Excel thành công: BaoCao_SystemTest_Search.xlsx");
    }

    /**
     * Helper: Làm chậm tiến trình test bằng cách tạm dừng luồng.
     */
    public void slowDown() {
        try { Thread.sleep(SLOW_SPEED); } catch (InterruptedException e) {}
    }

    /**
     * Thiết lập môi trường trước mỗi Test Case: Khởi tạo WebDriver.
     */
    @Before
    public void setUp() {
        System.setProperty("webdriver.chrome.driver", "C:\\WebDrivers\\chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    // ================================================================
    // CÁC TEST CASE TÌM KIẾM
    // ================================================================

    // --- CASE 1: TÌM KIẾM CÓ KẾT QUẢ (Nhập keyword) ---
    @Test
    public void testSearch_Found() {
        setTestCaseInfo(
            "ST_SEARCH_01", 
            "Tìm kiếm có kết quả (Gõ phím)", 
            "1. Nhập keyword 'Áo' vào ô search\n2. Enter\n3. Check URL và Số lượng SP", 
            "Keyword: Áo", 
            "Hiển thị danh sách sản phẩm liên quan đến 'Áo' (Số lượng > 0)"
        );

        driver.get(homeUrl);
        slowDown();

        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.clear();
        searchInput.sendKeys("Áo");
        slowDown();
        
        // Submit form bằng phím Enter
        searchInput.sendKeys(Keys.ENTER);
        slowDown(); 

        String currentUrl = driver.getCurrentUrl();
        this.currentActual = "URL: " + currentUrl;
        
        // Verify 1: URL phải chứa tham số tìm kiếm
        Assert.assertTrue("URL không chứa tham số keyword!", currentUrl.contains("keyword="));

        // Verify 2: Phải tìm thấy ít nhất 1 sản phẩm
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
            "1. Nhập từ khóa rác 'xyz123'\n2. Enter\n3. Check thông báo lỗi và danh sách rỗng", 
            "Keyword: xyz123_khong_co_dau", 
            "Hiển thị thông báo 'Không tìm thấy' / Danh sách rỗng"
        );

        driver.get(homeUrl);
        slowDown();

        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.sendKeys("xyz123_khong_co_dau");
        slowDown();
        
        // Submit
        searchInput.sendKeys(Keys.ENTER);
        slowDown();

        // Verify 1: Danh sách sản phẩm phải rỗng
        int productCount = driver.findElements(By.className("product-card")).size();
        Assert.assertEquals("Lỗi: Lẽ ra không được hiện sản phẩm nào!", 0, productCount);

        // Verify 2: Phải hiện thông báo lỗi
        boolean hasMessage = false;
        try {
            WebElement msg = driver.findElement(By.xpath("//*[contains(text(), 'Không tìm thấy') or contains(text(), '0 kết quả')]"));
            hasMessage = msg.isDisplayed();
            this.currentActual = "Thông báo: " + msg.getText();
        } catch (Exception e) {
            this.currentActual = "Không thấy thông báo 'Không tìm thấy'";
        }
        
        Assert.assertTrue("Lỗi: Không hiện thông báo 'Không tìm thấy sản phẩm'!", hasMessage);
    }

    // --- CASE 3: BỎ TRỐNG TỪ KHÓA (VALIDATION) ---
    @Test
    public void testSearch_EmptyKeyword() {
        setTestCaseInfo(
            "ST_SEARCH_03", 
            "Bỏ trống từ khóa (Validation)", 
            "1. Để trống ô search\n2. Nhấn nút Tìm (button)\n3. Check URL không đổi", 
            "Keyword: (rỗng)", 
            "Trình duyệt chặn submit, URL không thay đổi"
        );

        driver.get(homeUrl);
        slowDown();

        String urlBefore = driver.getCurrentUrl();
        
        // Click nút Tìm (giả định input có thuộc tính required hoặc validation JS/HTML5)
        WebElement searchBtn = driver.findElement(By.cssSelector(".header-search button"));
        searchBtn.click();
        
        slowDown(); 
        
        String urlAfter = driver.getCurrentUrl();
        this.currentActual = "URL sau khi click: " + urlAfter;

        // Verify: URL không chứa tham số keyword (chặn submit rỗng) và không chuyển trang
        Assert.assertFalse("Lỗi: Form cho phép submit rỗng (URL chứa keyword=)!", urlAfter.contains("keyword="));
        Assert.assertEquals("Lỗi: Trang đã bị chuyển đi!", urlBefore, urlAfter);
    }

    // --- CASE 4: TÌM KIẾM BẰNG MENU CÓ SẴN ---
    @Test
    public void testSearch_ViaMenu() {
        setTestCaseInfo(
            "ST_SEARCH_04", 
            "Tìm kiếm qua Menu Mục lục", 
            "1. Click Menu 'BỘ SƯU TẬP'\n2. Click mục con 'Áo Hoodie'\n3. Check kết quả", 
            "Chọn: Áo Hoodie", 
            "Chuyển trang tìm kiếm & Hiện sản phẩm liên quan (Số lượng > 0)"
        );

        driver.get(homeUrl);
        slowDown();

        try {
            // 1. Click menu cha (nếu cần)
            WebElement parentMenu = driver.findElement(By.partialLinkText("BỘ SƯU TẬP"));
            parentMenu.click();
            System.out.println(">> Đã click menu cha: BỘ SƯU TẬP");
            slowDown(); 
            
            // 2. Click menu con (Áo Hoodie)
            WebElement childMenu = driver.findElement(By.partialLinkText("Áo Hoodie"));
            childMenu.click();
            System.out.println(">> Đã click menu con: Áo Hoodie");
            slowDown(); 

            String currentUrl = driver.getCurrentUrl();
            this.currentActual = "URL hiện tại: " + currentUrl;

            // Verify 1: URL chứa tham số tìm kiếm
            Assert.assertTrue("URL không đúng format search!", currentUrl.contains("keyword="));
            
            // Verify 2: Tìm thấy sản phẩm
            int count = driver.findElements(By.className("product-card")).size();
            this.currentActual += " | Số SP tìm thấy: " + count;
            
            Assert.assertTrue("Lỗi: Không tìm thấy sản phẩm nào khi click menu 'Áo Hoodie'!", count > 0);

        } catch (Exception e) {
            this.currentActual = "Lỗi thao tác menu: " + e.getMessage();
            Assert.fail("Không tìm thấy menu hoặc không click được: " + e.getMessage());
        }
    }

    // --- [MỚI] CASE 5: TÌM KIẾM KHÔNG PHÂN BIỆT HOA THƯỜNG ---
    @Test
    public void testSearch_CaseInsensitive() {
        setTestCaseInfo(
            "ST_SEARCH_05", 
            "Tìm kiếm không phân biệt hoa thường", 
            "1. Nhập 'áo hoodie' (chữ thường)\n2. Enter\n3. Check kết quả", 
            "Keyword: áo hoodie", 
            "Vẫn tìm thấy sản phẩm (Số lượng > 0)"
        );

        driver.get(homeUrl);
        slowDown();

        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.clear();
        searchInput.sendKeys("áo hoodie"); // Nhập chữ thường toàn bộ
        slowDown();
        searchInput.sendKeys(Keys.ENTER);
        slowDown();

        // Verify: Phải tìm thấy sản phẩm (chứng tỏ DB/Logic không phân biệt hoa thường)
        int count = driver.findElements(By.className("product-card")).size();
        this.currentActual = "Tìm thấy: " + count + " sản phẩm.";
        
        Assert.assertTrue("Lỗi: Tìm kiếm bị phân biệt hoa thường!", count > 0);
    }

    // --- [MỚI] CASE 6: TÌM KIẾM KÝ TỰ ĐẶC BIỆT (ROBUSTNESS) ---
    @Test
    public void testSearch_SpecialChars() {
        setTestCaseInfo(
            "ST_SEARCH_06", 
            "Tìm kiếm ký tự đặc biệt", 
            "1. Nhập '@#$'\n2. Enter\n3. Check không bị lỗi Server (500)", 
            "Keyword: @#$", 
            "Hệ thống xử lý an toàn (Không bị Crash/Lỗi Server)"
        );

        driver.get(homeUrl);
        slowDown();

        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.sendKeys("@#$"); 
        slowDown();
        searchInput.sendKeys(Keys.ENTER);
        slowDown();

        String pageSource = driver.getPageSource();
        this.currentActual = "Kiểm tra lỗi Server...";

        // Verify 1: Đảm bảo không hiện lỗi Server
        Assert.assertFalse("Lỗi: Hệ thống bị Crash (HTTP Status 500)!", pageSource.contains("HTTP Status 500"));
        Assert.assertFalse("Lỗi: Hệ thống hiện Exception!", pageSource.contains("Exception"));
        
        // Verify 2: Hiện thông báo không tìm thấy (xử lý an toàn)
        boolean hasMessage = driver.findElements(By.xpath("//*[contains(text(), 'Không tìm thấy')]")).size() > 0;
        Assert.assertTrue("Lỗi: Không hiện thông báo 'Không tìm thấy' (xử lý an toàn thất bại).", hasMessage);
    }

    /**
     * Dọn dẹp tài nguyên sau mỗi Test Case Class: Đóng trình duyệt (WebDriver).
     */
    @After
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}