<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <%-- 
      THAY THẾ HEAD CŨ BẰNG "HEAD CHUẨN" (BOOTSTRAP 4)
      Điều này BẮT BUỘC để header và footer hiển thị đúng.
    --%>
    <meta charset="UTF-8">
    <title>Đăng nhập - ShopDuck</title>

    <!-- 1. BOOTSTRAP 4 (BẮT BUỘC) -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/css/bootstrap.min.css">
    
    <!-- 2. ICON BOOTSTRAP (Cho giỏ hàng) -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    
    <!-- 3. FONT AWESOME (BẮT BUỘC CHO ICON FOOTER) -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    
    <!-- 4. CÁC FILE CSS CỦA BẠN -->
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/layout.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/style.css">

    <!-- 5. SCRIPT BOOTSTRAP 4 (BẮT BUỘC CHO DROPDOWN) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/js/bootstrap.min.js"></script>
</head>

<%-- Giữ lại class "bg-light" của bạn --%>
<body class="bg-light">

<%-- 1. THÊM HEADER --%>
<%@ include file="header.jsp" %>

<%-- 2. BỌC NỘI DUNG TRONG <main> (cho layout.css) --%>
<main>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card shadow">
                    <div class="card-header text-center">
                        <h4>Đăng nhập</h4>
                    </div>
                    <div class="card-body">
                <form id="loginForm" action="${pageContext.request.contextPath}/login" method="post">
                            <div class="form-group">
                                <label for="username">Tên đăng nhập</label>
                                <input type="text" name="username" class="form-control" value="<%= request.getParameter("username") != null ? request.getParameter("username") : "" %>" required>
                            </div>
                            <div class="form-group">
                                <label for="password">Mật khẩu</label>
                                <input type="password" name="password" class="form-control" required>
                            </div>
            <button id="loginButton" type="submit" class="btn btn-success btn-block">Đăng nhập</button>
                        </form>
                            <%
                                String error = (String) session.getAttribute("loginError");
                                if (error != null && !error.isEmpty()) {
                            %>
                                <div class="alert alert-danger mt-3"><%= error %></div>
                            <%
                                    session.removeAttribute("loginError"); // Xóa sau khi hiển thị
                                }
                            %>
                    </div>
                    <div class="card-footer text-center">
                        <%-- SỬA LỖI: Thêm ContextPath cho link Đăng ký --%>
                        Chưa có tài khoản? <a href="<%= request.getContextPath() %>/user/Register.jsp">Đăng ký ngay</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>
<%-- 3. KẾT THÚC <main> --%>


<%-- 4. THÊM FOOTER --%>
<%@ include file="footer.jsp" %>

</body>
</html>