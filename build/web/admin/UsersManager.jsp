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
            background: linear-gradient(135deg, #e3f2fd, #fff);
            min-height: 100vh;
        }
        .form-container {
            max-width: 600px;
            margin: 60px auto;
            background: #ffffff;
            border-radius: 20px;
            padding: 35px;
            box-shadow: 0px 10px 25px rgba(0,0,0,0.08);
            transition: transform .2s;
        }
        .form-container:hover {
            transform: translateY(-5px);
        }
        .form-control, .form-select {
            border-radius: 10px;
        }
        .btn-primary {
            border-radius: 10px;
            padding: 10px 0;
        }
        .btn-outline-secondary {
            border-radius: 10px;
            padding: 10px 0;
        }
        h2{
            font-weight: 600;
            text-align: center;
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
        <h2>Quản lý Người dùng</h2>

        <form action="<%=request.getContextPath()%>/admin/UsersManagerServlet" method="post">
            <input type="hidden" name="action" value="<%= action %>">

            <div class="mb-3">
                <label class="form-label fw-semibold">ID</label>
                <input type="text" class="form-control" name="id" value="<%= user.getId() %>" readonly>
            </div>

            <div class="mb-3">
                <label class="form-label fw-semibold">Tên đăng nhập</label>
                <input type="text" class="form-control" name="username" value="<%= user.getUsername() != null ? user.getUsername() : "" %>" required>
            </div>

            <div class="mb-3">
                <label class="form-label fw-semibold">Họ và tên</label>
                <input type="text" class="form-control" name="fullname" value="<%= user.getFullname() != null ? user.getFullname() : "" %>" required>
            </div>

            <div class="mb-3">
                <label class="form-label fw-semibold">Email</label>
                <input type="email" class="form-control" name="email" value="<%= user.getEmail() != null ? user.getEmail() : "" %>" required>
            </div>

            <div class="mb-3">
                <label class="form-label fw-semibold">Mật khẩu</label>
                <input type="password" class="form-control" name="password" value="<%= user.getPassword() != null ? user.getPassword() : "" %>" required>
            </div>

            <div class="mb-4">
                <label class="form-label fw-semibold">Vai trò</label>
                <select class="form-select" name="role">
                    <option value="user" <%= "user".equals(user.getRole()) ? "selected" : "" %>>User</option>
                    <option value="admin" <%= "admin".equals(user.getRole()) ? "selected" : "" %>>Admin</option>
                </select>
            </div>

            <div class="d-flex gap-3">
                <button type="submit" class="btn btn-primary w-50">💾 Lưu</button>
                <a href="View-users.jsp" class="btn btn-outline-secondary w-50">📋 Danh sách</a>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
