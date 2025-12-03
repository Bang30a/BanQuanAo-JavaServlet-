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
        
        /* ===== SIDEBAR ===== */
        .sidebar {
            width: 260px;
            background: #1e293b;
            height: 100%;
            display: flex;
            flex-direction: column;
            padding-top: 25px;
            color: white;
            box-shadow: 4px 0 12px rgba(0,0,0,0.15);
            flex-shrink: 0; /* Không bị co lại */
        }

        .sidebar h3 { text-align: center; margin-bottom: 25px; font-size: 21px; font-weight: 600; color: #e2e8f0; }

        /* Nút Xem trang user */
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
        .submenu { background: #0f172a; display: none; /* Màu tối hơn chút */ }
        .submenu a { display: block; padding: 12px 0 12px 50px; font-size: 14px; color: #94a3b8; text-decoration: none; transition: 0.2s; }
        .submenu a:hover { color: #fff; background: #334155; }

        /* Arrow icons */
        .has-submenu::after { content: '▾'; float: right; font-size: 12px; }
        .has-submenu.active::after { content: '▴'; }

        /* ===== MAIN CONTENT ===== */
        .main-content { flex-grow: 1; height: 100%; transition: 0.3s; }
        iframe { width: 100%; height: 100%; border: none; background: white; }

        /* Logout bottom */
        .logout { margin-top: auto; margin-bottom: 25px; }
        .logout a { display: block; padding: 14px 30px; color: #fca5a5; font-weight: 600; text-decoration: none; }
        .logout a:hover { color: #ef4444; background-color: #334155; }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />

    <div class="sidebar">
        <h3>Admin Panel</h3>
        
        <a href="${context}/index.jsp" target="_blank" class="view-site-link">
            🌐 Xem trang User
        </a>
        <hr class="separator">
        
        <a href="${context}/admin/dashboard/Statistics.jsp" target="mainFrame" class="admin-link active-link">
            📊 Thống kê
        </a>

        <a href="${context}/admin/users/View-users.jsp" target="mainFrame" class="admin-link">
            👤 Quản lý người dùng
        </a>

        <a class="has-submenu" onclick="toggleSubmenu(event)">
            🛒 Quản lý sản phẩm
        </a>
        <div class="submenu" id="productSubmenu">
            <a href="${context}/admin/products/View-products.jsp" target="mainFrame">Sản phẩm</a>
            <a href="${context}/admin/products/View-product-variants.jsp" target="mainFrame">Biến thể</a>
            <a href="${context}/admin/products/View-sizes.jsp" target="mainFrame">Kích cỡ</a>
        </div>

        <a href="${context}/admin/order/View-orders.jsp" target="mainFrame" class="admin-link">
            🧾 Quản lý hóa đơn
        </a>

        <div class="logout">
            <a href="${context}/LogoutServlet">🚪 Đăng xuất</a>
        </div>
    </div>

    <div class="main-content">
        <iframe name="mainFrame" src="${context}/admin/dashboard/Statistics.jsp"></iframe>
    </div>

    <script>
    function toggleSubmenu(e) {
        e.preventDefault();
        const link = e.target; // Thẻ a click vào
        link.classList.toggle('active'); // Đổi icon mũi tên
        
        const submenu = document.getElementById('productSubmenu');
        // Hiệu ứng đóng mở
        if (submenu.style.display === 'block') {
            submenu.style.display = 'none';
        } else {
            submenu.style.display = 'block';
        }
    }

    // Script Highlight menu đang chọn
    const sidebarLinks = document.querySelectorAll('.sidebar a.admin-link, .submenu a');
    
    sidebarLinks.forEach(link => {
        link.addEventListener('click', function() {
            // 1. Bỏ active cũ
            sidebarLinks.forEach(l => l.classList.remove('active-link'));
            document.querySelectorAll('.view-site-link, .logout a').forEach(l => l.classList.remove('active-link'));
            
            // 2. Thêm active mới cho link vừa click
            this.classList.add('active-link');

            // 3. Nếu click vào link con, giữ active cho menu cha
            if (this.parentElement.classList.contains('submenu')) {
                 // Logic giữ sáng menu cha nếu cần (tuỳ chọn)
            }
        });
    });
    </script>

</body>
</html>