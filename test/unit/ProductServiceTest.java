package unit;

import dao.ProductDao;
import dao.ProductVariantDao;
import dao.SizeDao;
import entity.Products;
import entity.ProductVariants;
import entity.Size;
import service.ProductService;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class ProductServiceTest {

    @Mock private ProductDao productDao;
    @Mock private ProductVariantDao variantDao;
    @Mock private SizeDao sizeDao;

    private ProductService productService;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    @Before
    public void setUp() {
        productService = new ProductService(productDao, variantDao, sizeDao);
    }

    // ==========================================
    // 1. USER METHODS (TÌM KIẾM & CHI TIẾT)
    // ==========================================

    @Test
    public void testSearchProducts_KeywordValid() throws Exception {
        setTestCaseInfo("PROD_SVC_01", "Tìm kiếm có từ khóa", 
                "1. Key='ao'\n2. Call DAO searchByKeyword", "Key='ao'", "Trả về list kết quả");

        List<Products> mockList = new ArrayList<>();
        mockList.add(new Products());
        when(productDao.searchByKeyword("ao")).thenReturn(mockList);

        List<Products> result = productService.searchProducts("ao");
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchProducts_KeywordEmpty() throws Exception {
        setTestCaseInfo("PROD_SVC_02", "Tìm kiếm rỗng", 
                "1. Key='   '\n2. Call DAO getAllProducts", "Key='   '", "Trả về toàn bộ SP");

        when(productDao.getAllProducts()).thenReturn(new ArrayList<>());
        productService.searchProducts("   "); 
        verify(productDao).getAllProducts();
    }

    @Test
    public void testGetProductDetails_Found() {
        setTestCaseInfo("PROD_SVC_03", "Lấy chi tiết SP (Có)", 
                "1. ID=1\n2. DAO trả về SP", "ID=1", "Trả về Object");

        Products p = new Products(); p.setId(1);
        when(productDao.findById(1)).thenReturn(p);

        Products result = productService.getProductDetails(1);
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    public void testGetVariantsByProductId() {
        setTestCaseInfo("PROD_SVC_04", "Lấy biến thể", 
                "1. ID=1\n2. Call VariantDAO", "ID=1", "Trả về list");

        when(variantDao.findByProductId(1)).thenReturn(new ArrayList<>());
        productService.getVariantsByProductId(1);
        verify(variantDao).findByProductId(1);
    }

    // --- [MỚI] Test lấy Size Map ---
    @Test
    public void testGetSizeMap() throws Exception {
        setTestCaseInfo("PROD_SVC_05", "Lấy Map Size", 
                "1. DAO trả list Size\n2. Convert sang Map", "List=[S, M]", "Map size=2");

        List<Size> sizes = new ArrayList<>();
        sizes.add(new Size(1, "S"));
        sizes.add(new Size(2, "M"));
        when(sizeDao.getAllSizes()).thenReturn(sizes);

        Map<Integer, String> result = productService.getSizeMap();
        
        assertEquals(2, result.size());
        assertEquals("S", result.get(1));
    }

    // ==========================================
    // 2. ADMIN METHODS (CRUD)
    // ==========================================

    // --- [MỚI] Test lấy sản phẩm để sửa (ID=0 -> New) ---
    @Test
    public void testGetProductForEdit_New() {
        setTestCaseInfo("PROD_SVC_06", "Form thêm mới (ID=0)", 
                "ID=0", "ID=0", "Trả về Product rỗng");

        Products p = productService.getProductForEdit(0);
        assertNotNull(p);
        assertEquals(0, p.getId()); // ID mặc định là 0
        verify(productDao, never()).findById(anyInt());
    }

    // --- [MỚI] Test lấy sản phẩm để sửa (ID>0 -> Edit) ---
    @Test
    public void testGetProductForEdit_Existing() {
        setTestCaseInfo("PROD_SVC_07", "Form sửa (ID=5)", 
                "ID=5", "ID=5", "Trả về Product từ DB");

        Products mockP = new Products(); mockP.setId(5);
        when(productDao.findById(5)).thenReturn(mockP);

        Products result = productService.getProductForEdit(5);
        assertEquals(5, result.getId());
    }

    // --- [MỚI] Test Lưu sản phẩm (INSERT) ---
    @Test
    public void testSaveProduct_Insert() throws Exception {
        setTestCaseInfo("PROD_SVC_08", "Lưu mới (Insert)", 
                "Product ID=0", "ID=0", "Call DAO.insert -> True");

        Products newP = new Products(); newP.setId(0);
        
        boolean result = productService.saveOrUpdateProduct(newP);
        
        verify(productDao).insert(newP);
        verify(productDao, never()).update(any());
        assertTrue(result);
    }

    // --- [MỚI] Test Lưu sản phẩm (UPDATE) ---
    @Test
    public void testSaveProduct_Update() throws Exception {
        setTestCaseInfo("PROD_SVC_09", "Cập nhật (Update)", 
                "Product ID=10", "ID=10", "Call DAO.update -> True");

        Products existingP = new Products(); existingP.setId(10);
        // Giả lập tìm thấy ID=10 trong DB
        when(productDao.findById(10)).thenReturn(existingP);

        boolean result = productService.saveOrUpdateProduct(existingP);
        
        verify(productDao).update(existingP);
        verify(productDao, never()).insert(any());
        assertTrue(result);
    }

    // --- [MỚI] Test Xóa sản phẩm (THÀNH CÔNG) ---
    @Test
    public void testDeleteProduct_Success() throws Exception {
        setTestCaseInfo("PROD_SVC_10", "Xóa thành công", 
                "1. Check Variants = Empty\n2. Call Delete", "ID=1, No Var", "DAO Delete -> True");

        // Giả lập không có biến thể
        when(variantDao.findByProductId(1)).thenReturn(new ArrayList<>());
        when(productDao.delete(1)).thenReturn(1); // Xóa được 1 dòng

        boolean result = productService.deleteProduct(1);
        
        assertTrue(result);
        verify(productDao).delete(1);
    }

    // --- [MỚI] Test Xóa sản phẩm (CHẶN DO CÒN BIẾN THỂ) ---
    @Test
    public void testDeleteProduct_Block_HasVariants() {
        setTestCaseInfo("PROD_SVC_11", "Chặn xóa (Còn biến thể)", 
                "1. Check Variants = List\n2. Return False", "ID=1, Has Var", "Không gọi DAO Delete -> False");

        // Giả lập còn biến thể
        List<ProductVariants> vars = new ArrayList<>();
        vars.add(new ProductVariants());
        when(variantDao.findByProductId(1)).thenReturn(vars);

        boolean result = productService.deleteProduct(1);
        
        assertFalse(result);
        verify(productDao, never()).delete(anyInt()); // Quan trọng: Không được gọi hàm xóa
    }

    // ==========================================
    // 3. EXCEPTION HANDLING (Test lỗi hệ thống)
    // ==========================================

    @Test
    public void testSearchProducts_Exception() throws Exception {
        setTestCaseInfo("PROD_EX_01", "Lỗi khi tìm kiếm", "DAO throw Exception", "Error", "Return Empty List");
        when(productDao.searchByKeyword(anyString())).thenThrow(new RuntimeException("DB Error"));
        
        List<Products> res = productService.searchProducts("abc");
        assertTrue(res.isEmpty()); // Không null, trả về rỗng
    }

    @Test
    public void testGetDetails_Exception() {
        setTestCaseInfo("PROD_EX_02", "Lỗi khi lấy chi tiết", "DAO throw Exception", "Error", "Return Null");
        when(productDao.findById(anyInt())).thenThrow(new RuntimeException("DB Error"));
        
        Products res = productService.getProductDetails(1);
        assertNull(res);
    }

    @Test
    public void testSave_Exception() throws Exception {
        setTestCaseInfo("PROD_EX_03", "Lỗi khi lưu", "DAO throw Exception", "Error", "Return False");
        when(productDao.insert(any())).thenThrow(new RuntimeException("DB Error"));
        
        Products p = new Products(); p.setId(0);
        boolean res = productService.saveOrUpdateProduct(p);
        assertFalse(res);
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_ProductService.xlsx"); }
}