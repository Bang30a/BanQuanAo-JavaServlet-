package unit;

// === IMPORT LOGIC ===
import control.admin.SizesManagerServlet;
import entity.Size;
import service.SizeService;
import util.ExcelTestExporter; // <-- Import class tiện ích

// === IMPORT TEST & MOCKITO ===
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// === IMPORT SERVLET & REFLECTION ===
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// === IMPORT JUNIT ===
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class SizesManagerServletTest {

    private SizesManagerServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;
    @Mock private SizeService sizeService;

    // === CẤU HÌNH BÁO CÁO (Biến instance) ===
    private String currentId = "";
    private String currentName = "";
    private String currentSteps = "";
    private String currentData = "";
    private String currentExpected = "";
    // Đã xóa list finalReportData

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id;
        this.currentName = name;
        this.currentSteps = steps;
        this.currentData = data;
        this.currentExpected = expected;
    }

    @Before
    public void setUp() throws Exception {
        servlet = new SizesManagerServlet();

        // Inject Mock Service vào Servlet bằng Reflection
        try {
            Field serviceField = SizesManagerServlet.class.getDeclaredField("sizeService");
            serviceField.setAccessible(true);
            serviceField.set(servlet, sizeService);
        } catch (NoSuchFieldException e) {
            System.err.println("Cảnh báo: Không tìm thấy biến 'sizeService' trong Servlet");
        }
    }

    // ==========================================
    // 1. TEST LIST (Xem danh sách Size)
    // ==========================================
    @Test
    public void testDoGet_ListSizes() throws Exception {
        setTestCaseInfo("SIZE_MGR_01", "Xem danh sách Size", 
                "1. Action='List'\n2. Service trả list", 
                "List size=2", "Forward View-sizes.jsp");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("List");
        when(request.getRequestDispatcher(contains("View-sizes.jsp"))).thenReturn(dispatcher);

        // 2. Mock Service
        List<Size> mockList = new ArrayList<>();
        mockList.add(new Size(1, "S"));
        mockList.add(new Size(2, "M"));
        when(sizeService.getAllSizes()).thenReturn(mockList);

        // 3. Run (Reflection calling doGet)
        Method doGet = SizesManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 4. Verify
        verify(request).setAttribute(eq("list"), eq(mockList)); // Kiểm tra setAttribute "list"
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 2. TEST PREPARE ADD/EDIT (Hiện form)
    // ==========================================
    @Test
    public void testDoGet_PrepareEdit() throws Exception {
        setTestCaseInfo("SIZE_MGR_02", "Hiện form sửa Size", 
                "1. Action='AddOrEdit', ID=1\n2. Service getById", 
                "ID=1", "Forward SizesManager.jsp");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("AddOrEdit");
        when(request.getParameter("id")).thenReturn("1");
        when(request.getRequestDispatcher(contains("SizesManager.jsp"))).thenReturn(dispatcher);

        // 2. Mock Service
        Size s = new Size(1, "S");
        when(sizeService.getSizeForEdit(1)).thenReturn(s);

        // 3. Run
        Method doGet = SizesManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 4. Verify
        verify(request).setAttribute(eq("SIZE"), eq(s));
        verify(request).setAttribute(eq("ACTION"), eq("SaveOrUpdate"));
        verify(dispatcher).forward(request, response);
    }

    // ==========================================
    // 3. TEST SAVE/UPDATE (Lưu)
    // ==========================================
    @Test
    public void testDoPost_SaveNew() throws Exception {
        setTestCaseInfo("SIZE_MGR_03", "Lưu Size mới", 
                "1. Action='SaveOrUpdate'\n2. Label='XL'", 
                "Label='XL'", "Call Service Save -> Redirect");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("SaveOrUpdate");
        when(request.getParameter("sizeLabel")).thenReturn("XL");
        // ID null hoặc rỗng -> Thêm mới (0)
        when(request.getParameter("id")).thenReturn(""); 

        // 2. Run
        Method doPost = SizesManagerServlet.class.getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
        doPost.setAccessible(true);
        doPost.invoke(servlet, request, response);

        // 3. Verify
        verify(sizeService).saveOrUpdateSize(any(Size.class)); // Kiểm tra service được gọi
        verify(response).sendRedirect(contains("SizesManagerServlet?action=List"));
    }

    // ==========================================
    // 4. TEST DELETE (Xóa)
    // ==========================================
    @Test
    public void testDoGet_Delete() throws Exception {
        setTestCaseInfo("SIZE_MGR_04", "Xóa Size", 
                "1. Action='Delete'\n2. ID=5", 
                "ID=5", "Call Service Delete -> Redirect");

        // 1. Mock Input
        when(request.getParameter("action")).thenReturn("Delete");
        when(request.getParameter("id")).thenReturn("5");

        // 2. Run
        Method doGet = SizesManagerServlet.class.getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
        doGet.setAccessible(true);
        doGet.invoke(servlet, request, response);

        // 3. Verify
        verify(sizeService).deleteSize(5); // Phải gọi hàm xóa với ID 5
        verify(response).sendRedirect(contains("SizesManagerServlet?action=List"));
    }

    // === XUẤT EXCEL MỚI ===
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
        // Xuất ra file .xlsx
        ExcelTestExporter.exportToExcel("KetQuaTest_SizesManagerServlet.xlsx");
    }
}