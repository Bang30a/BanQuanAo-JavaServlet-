<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - ShopDuck</title>

    <!-- 1. BOOTSTRAP 5 & FONT -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/user/assets/css/style.css">

    <style>
        body {
            background-color: #f8fafc;
            font-family: 'Inter', sans-serif;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        main {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }

        .login-card {
            background: white;
            padding: 40px;
            border-radius: 16px;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 450px;
            border: 1px solid #e2e8f0;
        }

        .login-title {
            font-weight: 700;
            color: #1e293b;
            text-align: center;
            margin-bottom: 10px;
        }
        
        .login-subtitle {
            text-align: center;
            color: #64748b;
            font-size: 14px;
            margin-bottom: 30px;
        }

        .form-label {
            font-weight: 600;
            color: #334155;
            font-size: 14px;
        }

        .form-control {
            border-radius: 8px;
            padding: 10px 15px;
            border: 1px solid #cbd5e1;
        }
        .form-control:focus {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
        }

        .btn-login {
            background-color: #2563eb;
            color: white;
            font-weight: 600;
            padding: 12px;
            border-radius: 8px;
            width: 100%;
            border: none;
            transition: 0.2s;
        }
        .btn-login:hover {
            background-color: #1d4ed8;
        }

        .alert-custom {
            border-radius: 8px;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .register-link {
            text-align: center;
            margin-top: 25px;
            font-size: 14px;
            color: #64748b;
        }
        .register-link a {
            color: #2563eb;
            text-decoration: none;
            font-weight: 600;
        }
        .register-link a:hover { text-decoration: underline; }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <!-- 2. INCLUDE HEADER -->
    <jsp:include page="../includes/header.jsp" />

    <main>
        <div class="login-card">
            <h3 class="login-title">Chào mừng trở lại!</h3>
            <p class="login-subtitle">Vui lòng đăng nhập để tiếp tục</p>

            <!-- [MỚI] THÔNG BÁO ĐĂNG KÝ THÀNH CÔNG -->
            <c:if test="${not empty sessionScope.registerSuccess}">
                <div class="alert alert-success alert-custom mb-4" role="alert">
                    <i class="bi bi-check-circle-fill"></i>
                    ${sessionScope.registerSuccess}
                </div>
                <!-- Xóa session để không hiện lại khi F5 -->
                <c:remove var="registerSuccess" scope="session" />
            </c:if>

            <!-- Thông báo lỗi Đăng nhập -->
            <c:if test="${not empty sessionScope.loginError}">
                <div class="alert alert-danger alert-custom mb-4" role="alert">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                    ${sessionScope.loginError}
                </div>
                <c:remove var="loginError" scope="session" />
            </c:if>

            <form action="${context}/LoginServlet" method="post">
                <div class="mb-3">
                    <label for="username" class="form-label">Tên đăng nhập</label>
                    <input type="text" class="form-control" id="username" name="username" 
                           placeholder="Nhập username..." 
                           value="${param.username}" required>
                </div>

                <div class="mb-4">
                    <label for="password" class="form-label">Mật khẩu</label>
                    <input type="password" class="form-control" id="password" name="password" 
                           placeholder="••••••••" required>
                </div>

                <button type="submit" class="btn-login">Đăng nhập</button>
            </form>

            <div class="register-link">
                Chưa có tài khoản? 
                <a href="${context}/user/auth/Register.jsp">Đăng ký ngay</a>
            </div>
        </div>
    </main>

    <!-- 3. INCLUDE FOOTER -->
    <jsp:include page="../includes/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>