<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- [QUAN TRỌNG] Thêm dòng này để dùng hàm fn:length đếm giỏ hàng --%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<style>
    /* Sticky Header */
    header.site-header {
        background: #ffffff;
        border-bottom: 1px solid #e5e7eb;
        position: sticky;
        top: 0;
        z-index: 1020;
        box-shadow: 0 2px 10px rgba(0,0,0,0.05);
    }

    /* Logo */
    .header-logo img {
        height: 50px; 
        transition: transform 0.3s;
    }
    .header-logo:hover img {
        transform: scale(1.05);
    }

    /* Navbar Links */
    .navbar-nav .nav-link {
        color: #374151;
        font-weight: 600;
        font-size: 14px;
        text-transform: uppercase;
        padding: 10px 15px !important;
        position: relative;
        transition: color 0.3s;
    }

    .navbar-nav .nav-link:hover, 
    .navbar-nav .nav-link.active {
        color: #2563eb; 
    }

    .navbar-nav .nav-link::after {
        content: '';
        position: absolute;
        width: 0;
        height: 2px;
        bottom: 5px;
        left: 50%;
        background-color: #2563eb;
        transition: all 0.3s ease;
        transform: translateX(-50%);
    }

    .navbar-nav .nav-link:hover::after {
        width: 80%;
    }

    /* Dropdown Menu */
    .dropdown-menu {
        border: none;
        border-radius: 8px;
        box-shadow: 0 10px 25px rgba(0,0,0,0.1);
        margin-top: 10px;
        animation: fadeIn 0.2s ease-in-out;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }

    .dropdown-item {
        font-size: 14px;
        padding: 8px 20px;
        font-weight: 500;
        color: #4b5563;
    }
    .dropdown-item:hover {
        background-color: #eff6ff;
        color: #2563eb;
    }

    /* Search Form */
    .header-search {
        position: relative;
    }
    .header-search input {
        border-radius: 20px;
        padding-left: 15px;
        padding-right: 40px;
        border: 1px solid #d1d5db;
        font-size: 14px;
        width: 200px;
        transition: width 0.3s;
    }
    .header-search input:focus {
        width: 260px; 
        border-color: #2563eb;
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
    }
    .header-search button {
        position: absolute;
        right: 5px;
        top: 50%;
        transform: translateY(-50%);
        border: none;
        background: none;
        color: #6b7280;
        padding: 5px;
    }
    .header-search button:hover { color: #2563eb; }

    /* Action Buttons */
    .btn-header {
        font-size: 14px;
        font-weight: 600;
        border-radius: 20px;
        padding: 6px 16px;
        transition: 0.2s;
    }
    
    /* Badge Cart Style (Nút đỏ thông báo số lượng) */
    .btn-cart {
        position: relative;
        display: inline-flex; 
        align-items: center;
        justify-content: center;
    }
    .badge-cart {
        position: absolute;
        top: -5px;
        right: -5px;
        background: #ef4444; /* Màu đỏ */
        color: white;
        border-radius: 50%;
        font-size: 11px;
        font-weight: 700;
        min-width: 18px;
        height: 18px;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2px 5px rgba(239, 68, 68, 0.3);
        border: 2px solid white;
        z-index: 10;
    }
</style>

<c:set var="context" value="${pageContext.request.contextPath}" />

<header class="site-header">
    <nav class="navbar navbar-expand-lg navbar-light">
        <div class="container">
            <!-- 1. Logo -->
            <a class="navbar-brand header-logo" href="${context}/user/view-products">
                <img src="${context}/user/assets/images/ui/logo.png" alt="ShopDuck" 
                     onerror="this.src='https://placehold.co/100x40?text=LOGO'">
            </a>

            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavbar">
                <span class="navbar-toggler-icon"></span>
            </button>

            <!-- 2. Main Menu -->
            <div class="collapse navbar-collapse" id="mainNavbar">
                <ul class="navbar-nav mx-auto mb-2 mb-lg-0">
                    
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">HƯỚNG DẪN</a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${context}/user/includes/size-guide-shirt.jsp">Chọn Size Áo</a></li>
                            <li><a class="dropdown-item" href="${context}/user/includes/size-guide-pants.jsp">Chọn Size Quần</a></li>
                        </ul>
                    </li>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">BỘ SƯU TẬP</a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${context}/user/search-products?keyword=Áo Hoodie">Áo Hoodie</a></li>
                            <li><a class="dropdown-item" href="${context}/user/search-products?keyword=Áo Khoác">Áo Khoác</a></li>
                            <li><a class="dropdown-item" href="${context}/user/search-products?keyword=Áo Polo">Áo Polo</a></li>
                        </ul>
                    </li>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">ÁO NAM</a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${context}/user/search-products?keyword=Áo Sơ Mi">Áo Sơ mi</a></li>
                            <li><a class="dropdown-item" href="${context}/user/search-products?keyword=Áo Thun">Áo Thun</a></li>
                        </ul>
                    </li>

                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">QUẦN NAM</a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${context}/user/search-products?keyword=Short">Quần Short</a></li>
                            <li><a class="dropdown-item" href="${context}/user/search-products?keyword=Quần Dài">Quần Dài</a></li>
                            <li><a class="dropdown-item" href="${context}/user/search-products?keyword=Jeans">Quần Jeans</a></li>
                        </ul>
                    </li>
                </ul>

                <!-- 3. Right Actions -->
                <div class="d-flex align-items-center gap-3">
                    
                    <form action="${context}/user/search-products" method="get" class="header-search d-none d-md-block">
                        <input type="text" name="keyword" placeholder="Tìm sản phẩm..." required>
                        <button type="submit"><i class="bi bi-search"></i></button>
                    </form>

                    <!-- GIỎ HÀNG (Có Badge Số lượng) -->
                    <a href="${context}/user/order/view-cart.jsp" class="btn btn-outline-dark btn-header btn-cart border-0">
                        <i class="bi bi-bag" style="font-size: 1.3rem;"></i>
                        
                        <!-- Logic hiển thị số lượng -->
                        <c:if test="${not empty sessionScope.cart && fn:length(sessionScope.cart) > 0}">
                            <span class="badge-cart">${fn:length(sessionScope.cart)}</span>
                        </c:if>
                    </a>

                    <!-- User Actions -->
                    <c:choose>
                        <c:when test="${not empty sessionScope.user}">
                            <div class="dropdown">
                                <a class="btn btn-light btn-header dropdown-toggle d-flex align-items-center gap-2" href="#" role="button" data-bs-toggle="dropdown">
                                    <img src="${context}/user/assets/images/ui/user-icon.png" width="24" height="24" class="rounded-circle" onerror="this.src='https://placehold.co/24x24?text=U'">
                                    <span>${sessionScope.user.username}</span>
                                </a>
                                <ul class="dropdown-menu dropdown-menu-end">
                                    <li><a class="dropdown-item" href="${context}/user/order-history">📦 Đơn hàng của tôi</a></li>
                                    <li><hr class="dropdown-divider"></li>
                                    <li><a class="dropdown-item text-danger" href="${context}/LogoutServlet">🚪 Đăng xuất</a></li>
                                </ul>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <a href="${context}/user/auth/Login.jsp" class="btn btn-primary btn-header px-4">Đăng nhập</a>
                        </c:otherwise>
                    </c:choose>

                </div>
            </div>
        </div>
    </nav>
</header>