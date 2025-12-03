package unit;

import dao.ProductDao;
import dao.ProductVariantDao;
import dao.SizeDao;
import entity.Products;
import entity.ProductVariants;
import service.ProductService;
import util.ExcelTestExporter; // <-- Import class tiện ích mới

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
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

    // === CẤU HÌNH BÁO CÁO (Dùng biến instance cho sạch code) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    // Đã xóa list finalReportData và các thư viện IO cũ

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() {
        // Khởi tạo Service với 3 Mock DAO
        productService = new ProductService(productDao, variantDao, sizeDao);
    }

    @Test
    public void testSearchProducts_KeywordValid() throws Exception {
        setTestCaseInfo("PROD_SVC_01", "Tìm kiếm có từ khóa", 
                "1. Key='ao'\n2. Call DAO searchByKeyword", 
                "Key='ao'", "Trả về list kết quả");

        List<Products> mockList = new ArrayList<>();
        mockList.add(new Products());
        when(productDao.searchByKeyword("ao")).thenReturn(mockList);

        List<Products> result = productService.searchProducts("ao");

        assertEquals(1, result.size());
        verify(productDao).searchByKeyword("ao"); 
    }

    @Test
    public void testSearchProducts_KeywordEmpty() throws Exception {
        setTestCaseInfo("PROD_SVC_02", "Tìm kiếm rỗng", 
                "1. Key='   '\n2. Call DAO getAllProducts", 
                "Key='   '", "Trả về toàn bộ SP");

        when(productDao.getAllProducts()).thenReturn(new ArrayList<>());

        productService.searchProducts("   "); 

        verify(productDao).getAllProducts(); 
        verify(productDao, never()).searchByKeyword(anyString());
    }

    @Test
    public void testGetProductDetails_Found() {
        setTestCaseInfo("PROD_SVC_03", "Lấy chi tiết SP (Có)", 
                "1. ID=1\n2. DAO trả về SP", 
                "ID=1", "Trả về Object Products");

        Products p = new Products(); p.setId(1);
        when(productDao.findById(1)).thenReturn(p);

        Products result = productService.getProductDetails(1);
        
        assertNotNull(result);
        assertEquals(1, result.getId());
    }

    @Test
    public void testGetVariantsByProductId() {
        setTestCaseInfo("PROD_SVC_04", "Lấy biến thể (Size/Màu)", 
                "1. ID=1\n2. Call VariantDAO", 
                "ID=1", "Trả về list variants");

        when(variantDao.findByProductId(1)).thenReturn(new ArrayList<>());

        productService.getVariantsByProductId(1);

        verify(variantDao).findByProductId(1);
    }

    // === CẤU HÌNH XUẤT EXCEL MỚI ===
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
        // Xuất ra file .xlsx thay vì .csv
        ExcelTestExporter.exportToExcel("KetQuaTest_ProductService.xlsx");
    }
}