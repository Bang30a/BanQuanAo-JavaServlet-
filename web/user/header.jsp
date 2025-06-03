<%@ page import="entity.Users" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    Users user = (Users) session.getAttribute("user");
%>

<header class="bg-light p-3 mb-4 shadow-sm">
    <div class="container d-flex justify-content-between align-items-center">
        <!-- Logo -->
        <a href="View-products.jsp">
            <img src="logo.png" alt="ShopDuck Logo" style="height: 80px;">
        </a>

        <!-- Navbar -->
        <nav>
            <ul class="nav">
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="#">Hướng Dẫn</a>
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

        <!-- Right side: user, search, cart -->
        <div class="d-flex align-items-center gap-3">
            <% if (user != null) { %>
                <span class="mr-2 font-weight-bold">👋 Xin chào, <%= user.getUsername() %></span>
                <a href="<%= request.getContextPath() %>/LogoutServlet" class="btn btn-outline-danger btn-sm">Đăng xuất</a>
            <% } else { %>
                <a href="Login.jsp" class="btn btn-outline-primary btn-sm d-flex align-items-center">
                    <img src="user-icon.png" alt="Login Icon" style="width: 20px; height: 20px; margin-right: 6px;">
                    Đăng nhập
                </a>
            <% } %>

            <!-- Form search -->
            <form action="<%= request.getContextPath() %>/user/search-products" method="get" class="form-inline ml-3">
                <input type="text" name="keyword" placeholder="Tìm kiếm..." class="form-control form-control-sm mr-2" />
                <button type="submit" class="btn btn-sm btn-outline-success">Tìm</button>
            </form>

            <a href="view-cart.jsp" class="btn btn-outline-secondary btn-sm ml-3">
                <i class="bi bi-cart3"></i> Giỏ hàng
            </a>
        </div>
    </div>
</header>