package unit;

import dao.SizeDao;
import entity.Size;
import service.SizeService;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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
public class SizeServiceTest {

    @Mock private SizeDao sizeDao;
    @InjectMocks private SizeService sizeService;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    // --- CASE 1: THÊM MỚI THÀNH CÔNG ---
    @Test
    public void testSave_NewSize_Success() {
        setTestCaseInfo("SIZE_SVC_01", "Thêm Size mới", 
                "1. Check Duplicate (OK)\n2. Insert", "Size: XL", "SUCCESS");

        // Mock list hiện tại chưa có XL
        List<Size> existing = new ArrayList<>();
        existing.add(new Size(1, "S"));
        when(sizeDao.getAllSizes()).thenReturn(existing);
        
        Size newSize = new Size(0, "XL");
        when(sizeDao.insertSize(newSize)).thenReturn(true);

        String result = sizeService.saveOrUpdateSize(newSize);
        assertEquals("SUCCESS", result);
    }

    // --- CASE 2: LỖI TÊN RỖNG ---
    @Test
    public void testSave_EmptyLabel() {
        setTestCaseInfo("SIZE_SVC_02", "Lỗi tên Size rỗng", 
                "Label = empty", "Size: ''", "Error Msg");

        Size size = new Size(0, "   ");
        String result = sizeService.saveOrUpdateSize(size);
        assertEquals("Tên Size không được để trống!", result);
    }

    // --- CASE 3: LỖI TRÙNG TÊN (THÊM MỚI) ---
    @Test
    public void testSave_Duplicate_New() {
        setTestCaseInfo("SIZE_SVC_03", "Lỗi trùng tên (Thêm mới)", 
                "List có 'S', thêm 's'", "Size: s", "Error: Đã tồn tại");

        List<Size> existing = new ArrayList<>();
        existing.add(new Size(1, "S"));
        when(sizeDao.getAllSizes()).thenReturn(existing);

        Size newSize = new Size(0, "s"); // Test case insensitive
        String result = sizeService.saveOrUpdateSize(newSize);
        
        assertTrue(result.contains("đã tồn tại"));
    }

    // --- CASE 4: LỖI TRÙNG TÊN (CẬP NHẬT) ---
    @Test
    public void testSave_Duplicate_Edit() {
        setTestCaseInfo("SIZE_SVC_04", "Lỗi trùng tên (Sửa)", 
                "List có S(1), M(2). Sửa M(2) -> S", "ID=2, Label=S", "Error: Đã sử dụng");

        List<Size> existing = new ArrayList<>();
        existing.add(new Size(1, "S"));
        existing.add(new Size(2, "M"));
        when(sizeDao.getAllSizes()).thenReturn(existing);

        // Sửa ID=2 thành tên "S" (đã thuộc về ID=1)
        Size updateSize = new Size(2, "S");
        String result = sizeService.saveOrUpdateSize(updateSize);

        assertTrue(result.contains("đã được sử dụng"));
    }

    // --- CASE 5: CẬP NHẬT THÀNH CÔNG ---
    @Test
    public void testSave_Update_Success() {
        setTestCaseInfo("SIZE_SVC_05", "Sửa Size thành công", 
                "Sửa M(2) -> L (chưa có)", "ID=2, Label=L", "SUCCESS");

        List<Size> existing = new ArrayList<>();
        existing.add(new Size(1, "S"));
        existing.add(new Size(2, "M"));
        when(sizeDao.getAllSizes()).thenReturn(existing);

        Size updateSize = new Size(2, "L");
        when(sizeDao.updateSize(updateSize)).thenReturn(true);

        String result = sizeService.saveOrUpdateSize(updateSize);
        assertEquals("SUCCESS", result);
    }

    // --- CASE 6: CÁC HÀM GET/DELETE/EXCEPTION ---
    @Test
    public void testOtherMethods() {
        setTestCaseInfo("SIZE_SVC_06", "Các hàm Get/Delete/Exception", 
                "GetEdit, Delete, Exception", "Mixed", "Pass all");

        // Test GetEdit (New)
        assertEquals(0, sizeService.getSizeForEdit(0).getId());

        // Test GetEdit (Existing)
        Size s = new Size(1, "S");
        when(sizeDao.getSizeById(1)).thenReturn(s);
        assertEquals(1, sizeService.getSizeForEdit(1).getId());

        // Test Delete
        when(sizeDao.deleteSize(1)).thenReturn(true);
        assertTrue(sizeService.deleteSize(1));

        // Test Exception (getAll)
        when(sizeDao.getAllSizes()).thenThrow(new RuntimeException("DB Error"));
        assertTrue(sizeService.getAllSizes().isEmpty());
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_SizeService.xlsx"); }
}