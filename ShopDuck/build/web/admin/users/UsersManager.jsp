<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý Người dùng</title>
    <!-- Font Inter & Bootstrap -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background-color: #f1f5f9;
            font-family: 'Inter', sans-serif;
            color: #334155;
        }
        .form-container {
            max-width: 800px; /* Rộng hơn để chia 2 cột */
            margin: 50px auto;
            background: #ffffff;
            border-radius: 16px;
            padding: 40px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        }
        h2 {
            font-weight: 700;
            text-align: center;
            margin-bottom: 30px;
            color: #0f172a;
        }
        .form-label {
            font-weight: 600;
            color: #475569;
            margin-bottom: 8px;
        }
        .form-control, .form-select {
            border-radius: 8px;
            padding: 10px 15px;
            border: 1px solid #cbd5e1;
            height: 45px;
        }
        .form-control:focus, .form-select:focus {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
        }
        
        /* Password Toggle */
        .password-group {
            position: relative;
        }
        .toggle-password {
            position: absolute;
            right: 15px;
            top: 50%;
            transform: translateY(-50%);
            cursor: pointer;
            color: #64748b;
            font-size: 0.9rem;
            user-select: none;
        }
        .toggle-password:hover { color: #3b82f6; }

        .btn-custom {
            padding: 10px 24px;
            border-radius: 8px;
            font-weight: 600;
            transition: 0.2s;
        }
        
        .bg-readonly {
            background-color: #f8fafc;
            color: #94a3b8;
        }
    </style>
</head>
<body>

    <!-- Context Path -->
    <c:set var="context" value="${pageContext.request.contextPath}" />
    
    <!-- Lấy dữ liệu -->
    <c:set var="user" value="${requestScope.USER}" />
    <c:set var="action" value="${empty requestScope.ACTION ? 'SaveOrUpdate' : requestScope.ACTION}" />

    <div class="container">
        <div class="form-container">
            <h2>
                <c:choose>
                    <c:when test="${not empty user.id && user.id != 0}">👤 Cập nhật User</c:when>
                    <c:otherwise>✨ Thêm User mới</c:otherwise>
                </c:choose>
            </h2>

            <form action="${context}/admin/UsersManagerServlet" method="post">
                <input type="hidden" name="action" value="${action}">

                <div class="row">
                    <!-- CỘT TRÁI -->
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">User ID</label>
                            <input type="text" class="form-control bg-readonly" name="id" 
                                   value="${(not empty user.id && user.id != 0) ? user.id : ''}" 
                                   readonly placeholder="Tự động tạo">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Tên đăng nhập (Username) <span class="text-danger">*</span></label>
                            <!-- Nếu đang sửa thì không cho đổi username (thường là khóa chính) -->
                            <input type="text" class="form-control" name="username" 
                                   value="${user.username}" required
                                   ${not empty user.id && user.id != 0 ? 'readonly style="background:#f8fafc"' : ''}>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Vai trò (Role)</label>
                            <select class="form-select" name="role">
                                <option value="user" ${user.role == 'user' ? 'selected' : ''}>User (Khách hàng)</option>
                                <option value="admin" ${user.role == 'admin' ? 'selected' : ''}>Admin (Quản trị)</option>
                            </select>
                        </div>
                    </div>

                    <!-- CỘT PHẢI -->
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">Họ và tên <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="fullname" 
                                   value="${user.fullname}" placeholder="Nhập họ tên đầy đủ" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Email <span class="text-danger">*</span></label>
                            <input type="email" class="form-control" name="email" 
                                   value="${user.email}" placeholder="example@gmail.com" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Mật khẩu <span class="text-danger">*</span></label>
                            <div class="password-group">
                                <input type="password" class="form-control" name="password" id="passInput"
                                       value="${user.password}" required>
                                <span class="toggle-password" onclick="togglePass()">👁️</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- BUTTONS -->
                <div class="d-flex justify-content-center gap-3 mt-4 pt-3 border-top">
                    <a href="${context}/admin/UsersManagerServlet?action=List" class="btn btn-outline-secondary btn-custom">
                        Hủy / Xem danh sách
                    </a>
                    
                    <button type="submit" class="btn btn-primary btn-custom px-5">
                        Lưu thông tin
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Script Ẩn/Hiện mật khẩu -->
    <script>
        function togglePass() {
            const passInput = document.getElementById("passInput");
            if (passInput.type === "password") {
                passInput.type = "text";
            } else {
                passInput.type = "password";
            }
        }
    </script>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>