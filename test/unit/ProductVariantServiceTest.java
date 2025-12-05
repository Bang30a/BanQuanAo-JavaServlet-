package unit;

import dao.ProductVariantDao;
import entity.ProductVariants;
import service.ProductVariantService;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class ProductVariantServiceTest {

    @Mock private ProductVariantDao variantDao;
    @InjectMocks private ProductVariantService service;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    // ==========================================
    // 1. TEST GET ALL
    // ==========================================
    @Test
    public void testGetAllVariants_Success() {
        setTestCaseInfo("VAR_SVC_01", "Lấy tất cả biến thể", 
                "Call DAO getAll", "DB has 1 item", "Return List size 1");

        List<ProductVariants> list = new ArrayList<>();
        list.add(new ProductVariants());
        when(variantDao.getAllVariants()).thenReturn(list);

        List<ProductVariants> result = service.getAllVariants();
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAllVariants_Exception() {
        setTestCaseInfo("VAR_SVC_02", "Lỗi khi lấy danh sách", 
                "DAO throw Exception", "Error", "Return Empty List");

        when(variantDao.getAllVariants()).thenThrow(new RuntimeException("DB Error"));
        List<ProductVariants> result = service.getAllVariants();
        assertTrue(result.isEmpty());
    }

    // ==========================================
    // 2. TEST GET FOR EDIT
    // ==========================================
    @Test
    public void testGetForEdit_New() {
        setTestCaseInfo("VAR_SVC_03", "Lấy form thêm mới", 
                "ID=0", "ID=0", "Return New Object");

        ProductVariants v = service.getVariantForEdit(0);
        assertEquals(0, v.getId());
        verify(variantDao, never()).findById(anyInt());
    }

    @Test
    public void testGetForEdit_Existing() {
        setTestCaseInfo("VAR_SVC_04", "Lấy biến thể để sửa", 
                "ID=5 -> Found", "ID=5", "Return Object from DB");

        ProductVariants mockV = new ProductVariants(); mockV.setId(5);
        when(variantDao.findById(5)).thenReturn(mockV);

        ProductVariants result = service.getVariantForEdit(5);
        assertEquals(5, result.getId());
    }

    // ==========================================
    // 3. TEST SAVE / UPDATE
    // ==========================================
    @Test
    public void testSave_Insert() {
        setTestCaseInfo("VAR_SVC_05", "Lưu mới (Insert)", 
                "ID=0 -> Call Insert", "ID=0", "Result: True");

        ProductVariants newV = new ProductVariants(); newV.setId(0);
        
        boolean result = service.saveOrUpdateVariant(newV);
        
        verify(variantDao).insertVariant(newV);
        assertTrue(result);
    }

    @Test
    public void testSave_Update() {
        setTestCaseInfo("VAR_SVC_06", "Cập nhật (Update)", 
                "ID=10 -> Call Update", "ID=10", "Result: True");

        ProductVariants existingV = new ProductVariants(); existingV.setId(10);
        // Giả lập tìm thấy ID=10
        when(variantDao.findById(10)).thenReturn(existingV);

        boolean result = service.saveOrUpdateVariant(existingV);
        
        verify(variantDao).updateVariant(existingV);
        assertTrue(result);
    }

    @Test
    public void testSave_Exception() {
        setTestCaseInfo("VAR_SVC_07", "Lỗi khi lưu", 
                "DAO throw Exception", "Error", "Result: False");

        ProductVariants v = new ProductVariants(); v.setId(0);
        doThrow(new RuntimeException("DB Error")).when(variantDao).insertVariant(v);

        boolean result = service.saveOrUpdateVariant(v);
        assertFalse(result);
    }

    // ==========================================
    // 4. TEST DELETE
    // ==========================================
    @Test
    public void testDelete_Success() {
        setTestCaseInfo("VAR_SVC_08", "Xóa thành công", 
                "Call DAO delete", "ID=1", "Result: True");

        boolean result = service.deleteVariant(1);
        verify(variantDao).deleteVariant(1);
        assertTrue(result);
    }

    @Test
    public void testDelete_Exception() {
        setTestCaseInfo("VAR_SVC_09", "Lỗi khi xóa", 
                "DAO throw Exception", "Error", "Result: False");

        doThrow(new RuntimeException("DB Error")).when(variantDao).deleteVariant(1);
        boolean result = service.deleteVariant(1);
        assertFalse(result);
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


    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_ProductVariantService.xlsx"); }
}