<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký tài khoản - ShopDuck</title>

    <!-- 1. BOOTSTRAP 5 & FONT -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <!-- CSS Riêng -->
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

        .register-card {
            background: white;
            padding: 40px;
            border-radius: 16px;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
            width: 100%;
            max-width: 500px; /* Rộng hơn Login xíu vì nhiều trường hơn */
            border: 1px solid #e2e8f0;
        }

        .register-title {
            font-weight: 700;
            color: #1e293b;
            text-align: center;
            margin-bottom: 10px;
        }
        
        .register-subtitle {
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

        .btn-register {
            background-color: #2563eb;
            color: white;
            font-weight: 600;
            padding: 12px;
            border-radius: 8px;
            width: 100%;
            border: none;
            transition: 0.2s;
            margin-top: 10px;
        }
        .btn-register:hover {
            background-color: #1d4ed8;
        }

        .login-link {
            text-align: center;
            margin-top: 25px;
            font-size: 14px;
            color: #64748b;
        }
        .login-link a {
            color: #2563eb;
            text-decoration: none;
            font-weight: 600;
        }
        .login-link a:hover { text-decoration: underline; }
        
        /* Alert styles */
        .alert-custom {
            border-radius: 8px;
            font-size: 14px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <!-- 2. INCLUDE HEADER -->
    <jsp:include page="../includes/header.jsp" />

    <main>
        <div class="register-card">
            <h3 class="register-title">Tạo tài khoản mới</h3>
            <p class="register-subtitle">Điền thông tin để tham gia cộng đồng ShopDuck</p>

            <!-- THÔNG BÁO LỖI (JSTL) -->
            <c:if test="${not empty sessionScope.registerError}">
                <div class="alert alert-danger alert-custom mb-4">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                    ${sessionScope.registerError}
                </div>
                <c:remove var="registerError" scope="session" />
            </c:if>

            <!-- THÔNG BÁO THÀNH CÔNG (JSTL) -->
            <c:if test="${not empty sessionScope.registerSuccess}">
                <div class="alert alert-success alert-custom mb-4">
                    <i class="bi bi-check-circle-fill"></i>
                    ${sessionScope.registerSuccess}
                </div>
                <c:remove var="registerSuccess" scope="session" />
            </c:if>

            <form action="${context}/RegisterServlet" method="post">
                
                <div class="mb-3">
                    <label for="username" class="form-label">Tên đăng nhập <span class="text-danger">*</span></label>
                    <input type="text" class="form-control" id="username" name="username" 
                           value="${param.username}" placeholder="VD: nguyenvanan" required>
                </div>

                <div class="mb-3">
                    <label for="fullname" class="form-label">Họ và tên</label>
                    <input type="text" class="form-control" id="fullname" name="fullname" 
                           value="${param.fullname}" placeholder="Nhập họ tên đầy đủ">
                </div>

                <div class="mb-3">
                    <label for="email" class="form-label">Email <span class="text-danger">*</span></label>
                    <input type="email" class="form-control" id="email" name="email" 
                           value="${param.email}" placeholder="example@gmail.com" required>
                </div>

                <div class="mb-4">
                    <label for="password" class="form-label">Mật khẩu <span class="text-danger">*</span></label>
                    <input type="password" class="form-control" id="password" name="password" 
                           placeholder="Ít nhất 6 ký tự" required>
                </div>

                <button type="submit" class="btn-register">Đăng ký ngay</button>
            </form>

            <div class="login-link">
                Đã có tài khoản? 
                <a href="${context}/user/auth/Login.jsp">Đăng nhập</a>
            </div>
        </div>
    </main>

    <!-- 3. INCLUDE FOOTER -->
    <jsp:include page="../includes/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>