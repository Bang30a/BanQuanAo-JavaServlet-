<%@page import="entity.Users"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Users Manager</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
        }
        .form-container {
            max-width: 600px;
            margin: 40px auto;
            background: white;
            border-radius: 15px;
            padding: 30px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        h2 {
            margin-bottom: 25px;
        }
    </style>
</head>
<body>
<%
    String action = (String) request.getAttribute("ACTION");
    Users user = (Users) request.getAttribute("USER");
    if (action == null) action = "SaveOrUpdate";
    if (user == null) user = new Users();
%>

<div class="container">
    <div class="form-container">
        <h2 class="text-center">Quản lý người dùng</h2>
        <form action="<%=request.getContextPath()%>/admin/UsersManagerServlet" method="post">
            <input type="hidden" name="action" value="<%= action %>">

            <div class="mb-3">
                <label class="form-label">ID</label>
                <input type="text" class="form-control" name="id" value="<%= user.getId() %>" readonly>
            </div>

            <div class="mb-3">
                <label class="form-label">Tên đăng nhập</label>
                <input type="text" class="form-control" name="username" value="<%= user.getUsername() != null ? user.getUsername() : "" %>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Họ và tên</label>
                <input type="text" class="form-control" name="fullname" value="<%= user.getFullname() != null ? user.getFullname() : "" %>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" name="email" value="<%= user.getEmail() != null ? user.getEmail() : "" %>" required>
            </div>

            <div class="mb-3">
                <label class="form-label">Mật khẩu</label>
                <input type="password" class="form-control" name="password" value="<%= user.getPassword() != null ? user.getPassword() : "" %>" required>
            </div>

            <div class="mb-4">
                <label class="form-label">Vai trò</label>
                <select class="form-select" name="role">
                    <option value="user" <%= "user".equals(user.getRole()) ? "selected" : "" %>>User</option>
                    <option value="admin" <%= "admin".equals(user.getRole()) ? "selected" : "" %>>Admin</option>
                </select>
            </div>

            <div class="d-flex justify-content-between">
                <button type="submit" class="btn btn-primary w-45">Lưu</button>
                <a href="View-users.jsp" class="btn btn-outline-secondary w-45">Danh sách người dùng</a>
            </div>
        </form>
    </div>
</div>

<!-- Bootstrap 5 JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
