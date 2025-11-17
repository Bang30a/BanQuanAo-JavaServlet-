<%@ page import="entity.Users" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    Users user = (Users) session.getAttribute("user");
%>

<!-- 💅 STYLE HEADER NÂNG CẤP -->
<style>
    header {
        background: #ffffff;
        border-bottom: 2px solid #f0f0f0;
        position: sticky;
        top: 0;
        z-index: 1000;
    }

    /* --- MENU CHÍNH --- */
    .nav > .nav-item > .nav-link {
        color: #333;
        font-weight: 600;
        padding: 12px 18px;
        position: relative;
        transition: all 0.3s ease;
        text-transform: uppercase;
        font-size: 14px;
        letter-spacing: 0.3px;
    }

    .nav > .nav-item > .nav-link:hover {
        color: #28a745;
        background-color: #f8f9fa;
    }

    /* --- GẠCH CHÂN CHUYỂN ĐỘNG --- */
    .nav > .nav-item > .nav-link::after {
        content: "";
        position: absolute;
        bottom: 6px;
        left: 0;
        width: 0%;
        height: 2px;
        background-color: #28a745;
        transition: all 0.3s ease-in-out;
    }

    .nav > .nav-item > .nav-link:hover::after {
        width: 100%;
    }

    /* --- DROPDOWN MENU --- */
    .dropdown-menu {
        border-radius: 8px;
        border: 1px solid #eee;
        box-shadow: 0 4px 10px rgba(0,0,0,0.1);
        margin-top: 5px;
    }
    .dropdown-item {
        padding: 10px 20px;
        font-weight: 500;
        color: #555;
        transition: background 0.3s;
    }
    .dropdown-item:hover {
        background-color: #e9f7ef;
        color: #28a745;
    }

    /* --- NÚT ĐẸP HƠN --- */
    .btn-custom {
        border-radius: 30px;
        font-weight: 600;
        padding: 6px 14px;
        transition: all 0.3s ease;
    }

    .btn-outline-primary.btn-custom:hover {
        background-color: #007bff;
        color: white;
        box-shadow: 0 3px 10px rgba(0,123,255,0.3);
    }

    .btn-outline-danger.btn-custom:hover {
        background-color: #dc3545;
        color: white;
        box-shadow: 0 3px 10px rgba(220,53,69,0.3);
    }

    .btn-outline-info.btn-custom:hover {
        background-color: #17a2b8;
        color: white;
        box-shadow: 0 3px 10px rgba(23,162,184,0.3);
    }

    .btn-outline-secondary.btn-custom:hover {
        background-color: #6c757d;
        color: white;
        box-shadow: 0 3px 10px rgba(108,117,125,0.3);
    }

    /* --- TÌM KIẾM --- */
    .form-inline input {
        border-radius: 20px;
        padding: 4px 12px;
    }

    .btn-outline-success {
        border-radius: 20px;
        font-weight: 600;
        padding: 4px 12px;
    }

    .btn-outline-success:hover {
        background-color: #28a745;
        color: #fff;
        box-shadow: 0 3px 8px rgba(40,167,69,0.3);
    }
</style>

<header class="p-3 mb-4 shadow-sm">
    <div class="container d-flex justify-content-between align-items-center">
        <!-- Logo -->
        <a href="<%= request.getContextPath() %>/user/view-products">
            <img src="<%= request.getContextPath() %>/user/logo.png" alt="ShopDuck Logo" style="height: 80px;">
        </a>

        <!-- Navbar -->
        <nav>
            <ul class="nav">
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="#">HƯỚNG DẪN</a> 
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/size-guide-shirt.jsp">Chọn Size Áo</a></li>
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/size-guide-pants.jsp">Chọn Size Quần</a></li>
                    </ul>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="#">BỘ SƯU TẬP</a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Áo Hoodie">Áo Hoodie</a></li>
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Áo Khoác">Áo Khoác</a></li>
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Áo Polo">Áo Polo</a></li>
                    </ul>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="#">ÁO NAM</a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Áo Sơ Mi">Áo Sơ mi</a></li>
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Áo Thun">Áo Thun</a></li>
                    </ul>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="#">QUẦN NAM</a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Short">Quần Short</a></li>
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Quần Dài">Quần Dài</a></li>
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Jeans">Quần Jeans</a></li>
                    </ul>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="#">PHỤ KIỆN</a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Kính">Kính</a></li>
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Cà Vạt">Cà Vạt</a></li>
                        <li><a class="dropdown-item" href="<%= request.getContextPath() %>/user/search-products?keyword=Túi Xách">Túi Xách</a></li>
                    </ul>
                </li>
            </ul>
        </nav>

        <!-- Right side -->
        <div class="d-flex align-items-center">
            <% if (user != null) { %>
                <span class="mr-2 font-weight-bold text-success">👋 Xin chào, <%= user.getUsername() %></span>
                <a href="<%= request.getContextPath() %>/user/order-history" class="btn btn-outline-info btn-sm btn-custom mr-2">Theo dõi đơn</a>
                <a href="<%= request.getContextPath() %>/LogoutServlet" class="btn btn-outline-danger btn-sm btn-custom">Đăng xuất</a>
            <% } else { %>
                <a href="<%= request.getContextPath() %>/user/Login.jsp" class="btn btn-outline-primary btn-sm btn-custom d-flex align-items-center">
                    <i class="bi bi-person mr-2"></i> Đăng nhập
                </a>
            <% } %>

            <form action="<%= request.getContextPath() %>/user/search-products" method="get" class="form-inline ml-3">
                <input type="text" name="keyword" placeholder="Tìm kiếm..." class="form-control form-control-sm mr-2" />
                <button type="submit" class="btn btn-sm btn-outline-success btn-custom">Tìm</button>
            </form>

            <a href="<%= request.getContextPath() %>/user/view-cart.jsp" class="btn btn-outline-secondary btn-sm btn-custom ml-3">
                <i class="bi bi-cart3"></i> Giỏ hàng
            </a>
        </div>
    </div>
</header>
