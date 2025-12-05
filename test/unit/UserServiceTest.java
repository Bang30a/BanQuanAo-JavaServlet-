package unit;

import dao.UsersDao;
import entity.Users;
import service.UserService;
import util.ExcelTestExporter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock private UsersDao usersDao;
    @InjectMocks private UserService userService;

    // === CẤU HÌNH BÁO CÁO ===
    private String currentId = "", currentName = "", currentSteps = "", currentData = "", currentExpected = "";

    private void setTestCaseInfo(String id, String name, String steps, String data, String expected) {
        this.currentId = id; this.currentName = name; this.currentSteps = steps; 
        this.currentData = data; this.currentExpected = expected;
    }

    // --- [MỚI] CASE 1: LẤY TẤT CẢ USER ---
    @Test
    public void testGetAllUsers() {
        setTestCaseInfo("USER_SVC_01", "Lấy danh sách User", 
                "Call DAO getAll", "DB has 1 user", "Return List size 1");

        List<Users> list = new ArrayList<>();
        list.add(new Users());
        when(usersDao.getAllUsers()).thenReturn(list);

        List<Users> result = userService.getAllUsers();
        assertEquals(1, result.size());
    }

    // --- [MỚI] CASE 2: LẤY USER ĐỂ SỬA (ID > 0) ---
    @Test
    public void testGetUserForEdit_Existing() {
        setTestCaseInfo("USER_SVC_02", "Lấy User để sửa", 
                "ID=5", "ID=5", "Return User Obj");

        Users u = new Users(); u.setId(5);
        when(usersDao.getUserById(5)).thenReturn(u);

        Users result = userService.getUserForEdit(5);
        assertNotNull(result);
        assertEquals(5, result.getId());
    }

    // --- [MỚI] CASE 3: LẤY FORM THÊM MỚI (ID = 0) ---
    @Test
    public void testGetUserForEdit_New() {
        setTestCaseInfo("USER_SVC_03", "Lấy form thêm mới", 
                "ID=0", "ID=0", "Return Empty User");

        Users result = userService.getUserForEdit(0);
        assertNotNull(result);
        assertEquals(0, result.getId());
        verify(usersDao, never()).getUserById(anyInt());
    }

    // --- CASE 4: LƯU USER MỚI THÀNH CÔNG ---
    @Test
    public void testSave_NewUser_Success() {
        setTestCaseInfo("USER_SVC_04", "Lưu User mới", 
                "1. ID=0, Valid\n2. CheckExists=false", "User: new", "SUCCESS");

        Users u = new Users(0, "newuser", "123", "New Name", "new@mail.com", "user");
        
        when(usersDao.checkUserExists("newuser")).thenReturn(false);
        when(usersDao.insert(u)).thenReturn(true);

        String result = userService.saveOrUpdateUser(u);
        assertEquals("SUCCESS", result);
    }

    // --- CASE 5: CẬP NHẬT USER THÀNH CÔNG ---
    @Test
    public void testSave_UpdateUser_Success() {
        setTestCaseInfo("USER_SVC_05", "Cập nhật User", 
                "1. ID=5\n2. Update=true", "User ID: 5", "SUCCESS");

        Users u = new Users(5, "olduser", "123", "Updated Name", "old@mail.com", "admin");
        // Giả lập tìm thấy user cũ trong DB để lọt vào nhánh update
        when(usersDao.getUserById(5)).thenReturn(new Users()); 
        when(usersDao.updateUser(u)).thenReturn(true);

        String result = userService.saveOrUpdateUser(u);
        assertEquals("SUCCESS", result);
    }

    // --- CASE 6: LỖI VALIDATION ---
    @Test
    public void testSave_ValidationErrors() {
        setTestCaseInfo("USER_SVC_06", "Lỗi Validation (Rỗng)", 
                "Username/Pass empty", "Empty", "Error Msg");

        Users u1 = new Users(0, "", "123", "Name", "m", "u");
        assertEquals("Vui lòng nhập Username!", userService.saveOrUpdateUser(u1));

        Users u2 = new Users(0, "user", "", "Name", "m", "u");
        assertEquals("Vui lòng nhập Password!", userService.saveOrUpdateUser(u2));
    }

    // --- CASE 7: LỖI TRÙNG USERNAME ---
    @Test
    public void testSave_DuplicateUsername() {
        setTestCaseInfo("USER_SVC_07", "Lỗi trùng Username", 
                "1. ID=0\n2. CheckExists=true", "User: admin", "Error: Đã tồn tại");

        Users u = new Users(0, "admin", "123", "Admin", "mail", "admin");
        when(usersDao.checkUserExists("admin")).thenReturn(true);

        String result = userService.saveOrUpdateUser(u);
        assertEquals("Username đã tồn tại!", result);
    }

    // --- CASE 8: XÓA USER ---
    @Test
    public void testDeleteUser() {
        setTestCaseInfo("USER_SVC_08", "Xóa User", "Call DAO delete", "ID=10", "True");

        when(usersDao.deleteUser(10)).thenReturn(true);
        boolean res = userService.deleteUser(10);
        assertTrue(res);
    }

    // --- CASE 9: EXCEPTION HANDLING ---
    @Test
    public void testExceptionHandling() {
        setTestCaseInfo("USER_SVC_09", "Xử lý lỗi hệ thống", 
                "DAO throw Exception", "Error", "Catch & Return default");

        // Test getAll exception
        when(usersDao.getAllUsers()).thenThrow(new RuntimeException("DB Error"));
        assertTrue(userService.getAllUsers().isEmpty());

        // Test save exception
        Users u = new Users(0, "u", "p", "n", "e", "r");
        when(usersDao.checkUserExists("u")).thenThrow(new RuntimeException("DB Error"));
        assertTrue(userService.saveOrUpdateUser(u).startsWith("Exception:"));
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

    @AfterClass public static void exportReport() { ExcelTestExporter.exportToExcel("KetQuaTest_UserService.xlsx"); }
}