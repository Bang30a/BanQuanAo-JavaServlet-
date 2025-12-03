package unit;

// === IMPORT LOGIC ===
import entity.CartBean;
import entity.ProductVariants;
import util.ExcelTestExporter; // Class tiện ích xuất Excel

// === IMPORT JUNIT ===
import org.junit.Assert;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class CartBeanTest {

    // === CẤU HÌNH BÁO CÁO (REPORTING VARS) ===
    private static String currentId = "";
    private static String currentName = "";
    private static String currentSteps = "";
    private static String currentData = "";
    private static String currentExpected = "";

    // Hàm helper để set thông tin cho từng Test Case
    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        currentId = id;
        currentName = name;
        currentSteps = steps;
        currentData = data;
        currentExpected = expected;
    }

    // ==========================================
    // CÁC TEST CASE (Đã thêm thông tin báo cáo)
    // ==========================================

    // UT_01: Test Constructor với số lượng hợp lệ
    @Test
    public void testConstructor_ValidQuantity() {
        setTestCaseInfo("BEAN_01", "Constructor: Số lượng hợp lệ", 
                "New CartBean(Product, 5)", "Qty=5", "Qty=5");

        ProductVariants p = new ProductVariants();
        int inputQty = 5;
        
        CartBean item = new CartBean(p, inputQty);
        
        Assert.assertEquals(5, item.getQuantity());
    }

    // UT_02: Test Constructor với số lượng không hợp lệ (Số âm hoặc 0)
    @Test
    public void testConstructor_InvalidQuantity() {
        setTestCaseInfo("BEAN_02", "Constructor: Số lượng lỗi (<=0)", 
                "New CartBean với Qty=-5 và Qty=0", "Qty=-5, 0", "Qty tự về 1");

        ProductVariants p = new ProductVariants();
        
        // Thử nhập số âm
        CartBean itemNegative = new CartBean(p, -5);
        Assert.assertEquals("Số lượng âm phải tự về 1", 1, itemNegative.getQuantity());

        // Thử nhập số 0
        CartBean itemZero = new CartBean(p, 0);
        Assert.assertEquals("Số lượng 0 phải tự về 1", 1, itemZero.getQuantity());
    }

    // UT_03: Test Setter bảo vệ số lượng
    @Test
    public void testSetQuantity_Protection() {
        setTestCaseInfo("BEAN_03", "Setter: Bảo vệ số lượng", 
                "setQuantity(10) rồi setQuantity(0)", "Set 10, Set 0", "Qty=10, Qty=1");

        CartBean item = new CartBean();
        
        // Set số hợp lệ
        item.setQuantity(10);
        Assert.assertEquals(10, item.getQuantity());
        
        // Set số lỗi (số 0) -> Mong đợi nhảy về 1
        item.setQuantity(0);
        Assert.assertEquals(1, item.getQuantity());
    }

    // UT_04: Test tính tổng tiền (Happy Case)
    @Test
    public void testGetTotalPrice_Normal() {
        setTestCaseInfo("BEAN_04", "Tính tổng tiền: Bình thường", 
                "Price=100k * Qty=2", "P=100k, Q=2", "Total=200k");

        // 1. Giả lập sản phẩm
        ProductVariants p = new ProductVariants();
        p.setPrice(100000.0);
        
        // 2. Tạo CartBean
        CartBean item = new CartBean(p, 2);
        
        // 3. Tính toán
        double expected = 200000.0;
        double actual = item.getTotalPrice();
        
        Assert.assertEquals(expected, actual, 0.001);
    }

    // UT_05: Test tính tổng tiền khi Product bị null
    @Test
    public void testGetTotalPrice_NullProduct() {
        setTestCaseInfo("BEAN_05", "Tính tổng tiền: Product Null", 
                "CartBean chưa gán Product", "Product=null", "Total=0.0");

        CartBean item = new CartBean();
        item.setProductVariant(null);
        item.setQuantity(5);
        
        double actual = item.getTotalPrice();
        Assert.assertEquals(0.0, actual, 0.001);
    }

    // ==========================================================
    // === CẤU HÌNH XUẤT EXCEL (TEST WATCHER) ===
    // ==========================================================

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
        // Xuất ra file Excel riêng cho CartBean
        ExcelTestExporter.exportToExcel("KetQuaTest_CartBean.xlsx");
        System.out.println(">> Đã xuất file báo cáo: KetQuaTest_CartBean.xlsx");
    }
}