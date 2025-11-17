<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap" rel="stylesheet">
    <style>
* {
    box-sizing: border-box;
}

body {
    margin: 0;
    font-family: 'Inter', sans-serif;
    background-color: #f1f5f9;
    color: #333;
    display: flex;
}

/* ===== SIDEBAR ===== */
.sidebar {
    width: 260px;
    background: #1e293b;
    height: 100vh;
    position: fixed;
    display: flex;
    flex-direction: column;
    padding-top: 25px;
    color: white;
    transition: 0.3s;
    box-shadow: 4px 0 12px rgba(0,0,0,0.15);
}

.sidebar h3 {
    text-align: center;
    margin-bottom: 25px;
    font-size: 21px;
    font-weight: 600;
    color: #e2e8f0;
}

/* Nút Xem trang user */
.view-site-link {
    display: block;
    margin: 0 28px 18px;
    padding: 10px;
    background: #0284c7;
    color: white;
    text-decoration: none;
    text-align: center;
    border-radius: 8px;
    font-weight: 600;
    transition: 0.2s;
}
.view-site-link:hover {
    background: #0369a1;
}

.sidebar hr {
    border: none;
    border-top: 1px solid #334155;
    margin: 18px 28px;
}

/* Menu items */
.sidebar a {
    display: block;
    padding: 14px 30px;
    color: #cbd5e1;
    text-decoration: none;
    font-size: 15px;
    transition: 0.2s;
    border-left: 4px solid transparent;
}

.sidebar a:hover,
.sidebar a.active-link {
    background: #334155;
    color: white;
    border-left: 4px solid #0ea5e9;
}

/* Submenu */
.submenu {
    background: #263447;
    display: none;
}
.submenu a {
    padding-left: 50px;
    font-size: 14px;
}

/* Arrow icons */
.has-submenu::after {
    content: '▾';
    float: right;
    font-size: 12px;
}
.has-submenu.active::after {
    content: '▴';
}

/* ===== MAIN CONTENT ===== */
.main-content {
    margin-left: 260px;
    width: calc(100% - 260px);
    transition: 0.3s;
}

iframe {
    width: 100%;
    height: 100vh;
    border: none;
    background: white;
    border-radius: 0 0 0 0;
}

/* Logout bottom */
.logout {
    margin-top: auto;
    margin-bottom: 25px;
}

.logout a {
    padding-left: 30px;
    padding-right: 30px;
    color: #fca5a5;
    font-weight: 600;
}
.logout a:hover {
    color: #ef4444;
    background-color: #334155;
    border-left-color: #ef4444;
}

    </style>
</head>
<body>
    <div class="sidebar">
        <h3>Admin Panel</h3>
        
        <!-- === NÚT MỚI: XEM TRANG USER (Mở tab mới) === -->
        <a href="<%= request.getContextPath() %>/user/view-products" 
           target="_blank" 
           class="view-site-link">
            🌐 Xem trang User
        </a>
        <hr class="separator">
        <!-- === KẾT THÚC NÚT MỚI === -->
        
        <a href="${pageContext.request.contextPath}/admin/StatisticsServlet" target="mainFrame" class="admin-link active-link" id="link-stats">📊 Thống kê</a>
        <a href="${pageContext.request.contextPath}/admin/UsersManagerServlet?action=List" target="mainFrame" class="admin-link" id="link-users">👤 Quản lý người dùng</a>

        <a href="#" class="has-submenu" onclick="toggleSubmenu(event)">🛒 Quản lý sản phẩm</a>
        <div class="submenu" id="productSubmenu">
            <a href="${pageContext.request.contextPath}/admin/ProductsManager?action=List" target="mainFrame" class="admin-link" id="link-products">Sản phẩm</a>
            <a href="${pageContext.request.contextPath}/admin/ProductVariantsManagerServlet?action=List" target="mainFrame" class="admin-link" id="link-variants">Biến thể sản phẩm</a>
            <a href="${pageContext.request.contextPath}/admin/SizesManagerServlet?action=List" target="mainFrame" class="admin-link" id="link-sizes">Kích cỡ</a>
        </div>

        <a href="${pageContext.request.contextPath}/admin/OrdersManagerServlet?action=List" target="mainFrame" class="admin-link" id="link-orders">🧾 Quản lý hóa đơn</a>

        <div class="logout">
            <!-- Sửa lỗi: Trỏ đến /LogoutServlet (theo @WebServlet hoặc web.xml) -->
            <a href="<%= request.getContextPath() %>/LogoutServlet">🚪 Đăng xuất</a>
        </div>
    </div>

    <div class="main-content">
        <iframe name="mainFrame" src="${pageContext.request.contextPath}/admin/StatisticsServlet"></iframe>
    </div>

    <script>
    function toggleSubmenu(e) {
        e.preventDefault();
        const link = e.target;
        link.classList.toggle('active');
        const submenu = document.getElementById('productSubmenu');
        submenu.style.display = submenu.style.display === 'block' ? 'none' : 'block';
    }

    // Giữ highlight menu đang chọn
    const sidebarLinks = document.querySelectorAll('.sidebar a:not(.view-site-link)');
    sidebarLinks.forEach(link => {
        link.addEventListener('click', () => {
            sidebarLinks.forEach(l => l.classList.remove('active-link'));
            link.classList.add('active-link');
        });
    });
</script>

</body>
</html>