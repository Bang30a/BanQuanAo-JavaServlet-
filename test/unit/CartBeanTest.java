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
    // CÁC TEST CASE
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

    // UT_03: Test Setter bảo vệ số lượng (Số 0)
    @Test
    public void testSetQuantity_Zero() {
        setTestCaseInfo("BEAN_03", "Setter: Nhập 0", 
                "setQuantity(0)", "Input=0", "Qty=1");

        CartBean item = new CartBean();
        item.setQuantity(10); // Gán trước
        
        item.setQuantity(0); // Sửa thành 0
        Assert.assertEquals(1, item.getQuantity());
    }

    // [MỚI] UT_04: Test Setter bảo vệ số lượng (Số Âm)
    @Test
    public void testSetQuantity_Negative() {
        setTestCaseInfo("BEAN_04", "Setter: Nhập số âm", 
                "setQuantity(-10)", "Input=-10", "Qty=1");

        CartBean item = new CartBean();
        item.setQuantity(5);
        
        item.setQuantity(-10); // Sửa thành âm
        Assert.assertEquals(1, item.getQuantity());
    }

    // UT_05: Test tính tổng tiền (Số chẵn)
    @Test
    public void testGetTotalPrice_Normal() {
        setTestCaseInfo("BEAN_05", "Tính tổng tiền: Bình thường", 
                "Price=100k * Qty=2", "P=100k, Q=2", "Total=200k");

        ProductVariants p = new ProductVariants();
        p.setPrice(100000.0);
        
        CartBean item = new CartBean(p, 2);
        
        double expected = 200000.0;
        double actual = item.getTotalPrice();
        
        Assert.assertEquals(expected, actual, 0.001);
    }

    // [MỚI] UT_06: Test tính tổng tiền (Số lẻ/Thập phân)
    @Test
    public void testGetTotalPrice_Decimal() {
        setTestCaseInfo("BEAN_06", "Tính tổng tiền: Số lẻ", 
                "Price=10.5 * Qty=3", "P=10.5, Q=3", "Total=31.5");

        ProductVariants p = new ProductVariants();
        p.setPrice(10.5); // Giá lẻ
        
        CartBean item = new CartBean(p, 3);
        
        double expected = 31.5;
        double actual = item.getTotalPrice();
        
        // Delta 0.001 dùng để so sánh số thực (double)
        Assert.assertEquals(expected, actual, 0.001);
    }

    // UT_07: Test tính tổng tiền khi Product bị null
    @Test
    public void testGetTotalPrice_NullProduct() {
        setTestCaseInfo("BEAN_07", "Tính tổng tiền: Product Null", 
                "CartBean chưa gán Product", "Product=null", "Total=0.0");

        CartBean item = new CartBean();
        item.setProductVariant(null);
        item.setQuantity(5);
        
        double actual = item.getTotalPrice();
        Assert.assertEquals(0.0, actual, 0.001);
    }

    // [MỚI] UT_08: Test Getter/Setter Product
    @Test
    public void testProductGetterSetter() {
        setTestCaseInfo("BEAN_08", "Getter/Setter Product", 
                "Set P -> Get P", "Product Obj", "Obj not null");

        CartBean item = new CartBean();
        ProductVariants p = new ProductVariants();
        p.setId(99);
        
        item.setProductVariant(p);
        
        Assert.assertNotNull(item.getProductVariant());
        Assert.assertEquals(99, item.getProductVariant().getId());
    }

     // === EXCEL EXPORT ===
    @Rule public TestWatcher watcher = new TestWatcher() {
        @Override protected void succeeded(Description d) { 
            // [SỬA] Đảo vị trí currentData và currentSteps để khớp với file Excel
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, "OK", "PASS"); 
        }
        @Override protected void failed(Throwable e, Description d) { 
            // [SỬA] Đảo vị trí currentData và currentSteps
            ExcelTestExporter.addResult(currentId, currentName, currentData, currentSteps, currentExpected, e.getMessage(), "FAIL"); 
        }
    };


    @AfterClass
    public static void exportReport() {
        // Xuất ra file Excel riêng cho CartBean
        ExcelTestExporter.exportToExcel("KetQuaTest_CartBean.xlsx");
        System.out.println(">> Đã xuất file báo cáo: KetQuaTest_CartBean.xlsx");
    }
}