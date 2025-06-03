<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Đăng ký tài khoản</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body class="bg-light">
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card shadow">
                <div class="card-header text-center">
                    <h4>Đăng ký tài khoản</h4>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/register" method="post">
                        <div class="form-group">
                            <label for="username">Tên đăng nhập</label>
                            <input type="text" name="username" class="form-control" 
                                   value="<%= request.getParameter("username") != null ? request.getParameter("username") : "" %>" required>
                        </div>
                        <div class="form-group">
                            <label for="password">Mật khẩu</label>
                            <input type="password" name="password" class="form-control" required>
                        </div>
                        <div class="form-group">
                            <label for="fullname">Họ tên</label>
                            <input type="text" name="fullname" class="form-control"
                                   value="<%= request.getParameter("fullname") != null ? request.getParameter("fullname") : "" %>">
                        </div>
                        <div class="form-group">
                            <label for="email">Email</label>
                            <input type="email" name="email" class="form-control"
                                   value="<%= request.getParameter("email") != null ? request.getParameter("email") : "" %>">
                        </div>
                        <button type="submit" class="btn btn-primary btn-block">Đăng ký</button>
                    </form>
                    <%
                        String error = (String) request.getAttribute("error");
                        if (error != null && !error.isEmpty()) {
                    %>
                    <div class="alert alert-danger mt-3"><%= error %></div>
                    <%
                        }
                    %>
                </div>
                <div class="card-footer text-center">
                    Đã có tài khoản? <a href="Login.jsp">Đăng nhập</a>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
