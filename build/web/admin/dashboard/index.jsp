<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap" rel="stylesheet">
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: 'Inter', sans-serif; background-color: #f1f5f9; color: #333; display: flex; height: 100vh; overflow: hidden; }
        
        /* ===== SIDEBAR (THANH MENU) ===== */
        .sidebar {
            width: 260px;
            background: #1e293b;
            height: 100%;
            display: flex;
            flex-direction: column;
            padding-top: 25px;
            color: white;
            box-shadow: 4px 0 12px rgba(0,0,0,0.15);
            flex-shrink: 0;
        }

        .sidebar h3 { text-align: center; margin-bottom: 25px; font-size: 21px; font-weight: 600; color: #e2e8f0; }

        .view-site-link {
            display: block; margin: 0 28px 18px; padding: 10px;
            background: #0284c7; color: white; text-decoration: none;
            text-align: center; border-radius: 8px; font-weight: 600; transition: 0.2s;
        }
        .view-site-link:hover { background: #0369a1; }
        .sidebar hr { border: none; border-top: 1px solid #334155; margin: 18px 28px; }

        /* Menu items */
        .sidebar a.admin-link, .sidebar a.has-submenu {
            display: block; padding: 14px 30px; color: #cbd5e1;
            text-decoration: none; font-size: 15px; transition: 0.2s;
            border-left: 4px solid transparent; cursor: pointer;
        }
        .sidebar a:hover, .sidebar a.active-link {
            background: #334155; color: white; border-left: 4px solid #0ea5e9;
        }

        /* Submenu */
        .submenu { background: #0f172a; display: none; }
        .submenu a { display: block; padding: 12px 0 12px 50px; font-size: 14px; color: #94a3b8; text-decoration: none; transition: 0.2s; }
        .submenu a:hover { color: #fff; background: #334155; }

        .has-submenu::after { content: '▾'; float: right; font-size: 12px; }
        .has-submenu.active::after { content: '▴'; }

        /* ===== MAIN CONTENT (KHUNG CHỨA) ===== */
        .main-content { flex-grow: 1; height: 100%; transition: 0.3s; }
        iframe { width: 100%; height: 100%; border: none; background: white; }

        /* Logout */
        .logout { margin-top: auto; margin-bottom: 25px; }
        .logout a { display: block; padding: 14px 30px; color: #fca5a5; font-weight: 600; text-decoration: none; }
        .logout a:hover { color: #ef4444; background-color: #334155; }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />

    <div class="sidebar">
        <h3>Admin Panel</h3>
        
        <a href="${context}/user/view-products" target="_blank" class="view-site-link">
            🌐 Xem trang User
        </a>
        <hr class="separator">
        
        <a href="${context}/admin/dashboard" target="mainFrame" class="admin-link active-link">
            🏠 Dashboard
        </a>

        <a href="${context}/admin/statistics" target="mainFrame" class="admin-link">
            📊 Báo cáo chi tiết
        </a>

        <a href="${context}/admin/UsersManagerServlet?action=List" target="mainFrame" class="admin-link">
            👤 Quản lý người dùng
        </a>

        <a class="has-submenu" onclick="toggleSubmenu(event)">
            🛒 Quản lý sản phẩm
        </a>
        <div class="submenu" id="productSubmenu">
            <a href="${context}/admin/ProductsManagerServlet?action=List" target="mainFrame">Sản phẩm</a>
            <a href="${context}/admin/ProductVariantsManagerServlet?action=List" target="mainFrame">Biến thể</a>
            <a href="${context}/admin/SizesManagerServlet?action=List" target="mainFrame">Kích cỡ</a>
        </div>

        <a href="${context}/admin/OrdersManagerServlet?action=List" target="mainFrame" class="admin-link">
            ? Quản lý hóa đơn
        </a>

        <div class="logout">
            <a href="${context}/LogoutServlet">🚪 Đăng xuất</a>
        </div>
    </div>

    <div class="main-content">
        <iframe name="mainFrame" src="${context}/admin/dashboard"></iframe>
    </div>

    <script>
    function toggleSubmenu(e) {
        e.preventDefault();
        let link = e.target.closest('.has-submenu'); 
        if (!link) return;
        link.classList.toggle('active');
        const submenu = document.getElementById('productSubmenu');
        submenu.style.display = (submenu.style.display === 'block') ? 'none' : 'block';
    }

    const allLinks = document.querySelectorAll('.sidebar a[target="mainFrame"]');
    allLinks.forEach(link => {
        link.addEventListener('click', function() {
            document.querySelectorAll('.sidebar a').forEach(l => l.classList.remove('active-link'));
            this.classList.add('active-link');
            const parentSubmenu = this.closest('.submenu');
            if (parentSubmenu) {
                const parentLink = parentSubmenu.previousElementSibling; 
                if(parentLink) parentLink.classList.add('active-link');
            }
        });
    });
    </script>
</body>
</html>