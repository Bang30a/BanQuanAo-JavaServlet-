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
            background-color: #f5f7fa;
            color: #333;
        }

        .sidebar {
            width: 260px;
            background-color: #1e293b;
            height: 100vh;
            position: fixed;
            padding-top: 30px;
            color: white;
            overflow-y: auto;
            box-shadow: 2px 0 5px rgba(0,0,0,0.1);
        }

        .sidebar h3 {
            text-align: center;
            margin-bottom: 40px;
            font-size: 22px;
            font-weight: 600;
            color: #f1f5f9;
        }

        .sidebar a {
            display: block;
            padding: 14px 28px;
            color: #cbd5e1;
            text-decoration: none;
            font-size: 15px;
            transition: background-color 0.2s, color 0.2s;
            border-left: 4px solid transparent;
        }

        .sidebar a:hover,
        .sidebar a:focus {
            background-color: #334155;
            color: #ffffff;
            border-left: 4px solid #0ea5e9;
        }

        .submenu {
            background-color: #2c3e50;
            display: none;
        }

        .submenu a {
            padding-left: 45px;
            font-size: 14px;
        }

        .has-submenu::after {
            content: '▼';
            float: right;
            font-size: 12px;
            margin-top: 4px;
        }

        .has-submenu.active::after {
            content: '▲';
        }

        .main-content {
            margin-left: 260px;
            padding: 0;
        }

        iframe {
            width: 100%;
            height: 100vh;
            border: none;
        }

        .logout {
            position: absolute;
            bottom: 30px;
            width: 100%;
            text-align: center;
        }

        .logout a {
            color: #f87171;
            font-weight: 600;
            text-decoration: none;
        }

        .logout a:hover {
            color: #ef4444;
        }
    </style>
</head>
<body>
    <div class="sidebar">
        <h3>Admin Panel</h3>
        <a href="View-users.jsp" target="mainFrame">👤 Quản lý người dùng</a>

        <a href="#" class="has-submenu" onclick="toggleSubmenu(event)">🛒 Quản lý sản phẩm</a>
        <div class="submenu" id="productSubmenu">
            <a href="View-products.jsp" target="mainFrame">Sản phẩm</a>
            <a href="View-product-variants.jsp" target="mainFrame">Biến thể sản phẩm</a>
            <a href="View-sizes.jsp" target="mainFrame">Kích cỡ</a>
        </div>

        <a href="View-orders.jsp" target="mainFrame">🧾 Quản lý hóa đơn</a>
        <a href="${pageContext.request.contextPath}/admin/StatisticsServlet" target="mainFrame">📊 Thống kê</a>

        <div class="logout">
            <a href="<%= request.getContextPath() %>/logout">🚪 Đăng xuất</a>
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
    </script>
</body>
</html>
