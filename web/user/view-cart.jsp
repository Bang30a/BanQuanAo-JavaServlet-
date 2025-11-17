<%@ page import="java.util.List" %>
<%@ page import="entity.CartBean" %>
<%@ page import="entity.ProductVariants" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%
    if (session.getAttribute("user") == null) {
        response.sendRedirect("Login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>🛒 Giỏ hàng của bạn</title>

    <!-- Bootstrap -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/css/bootstrap.min.css">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <!-- Layout (footer, header) -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/user/layout.css">

  <style>
/* === CART PAGE STYLE (namespace: .cart-page) === */
.cart-page {
    background-color: #f3f6fa;
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    font-family: 'Segoe UI', sans-serif;
}

/* === Nội dung chính === */
.cart-page main {
    flex: 1;
    padding: 50px 0;
}

/* === Tiêu đề === */
.cart-page h2 {
    text-align: center;
    margin-bottom: 35px;
    color: #0d6efd;
    font-weight: 700;
    letter-spacing: 0.3px;
}

/* === Thẻ card (bao quanh bảng) === */
.cart-page .card {
    border: none;
    border-radius: 14px;
    padding: 28px;
    background: #fff;
    box-shadow: 0 6px 20px rgba(0,0,0,0.08);
}

/* === Bảng sản phẩm === */
.cart-page .table {
    border-radius: 8px;
    overflow: hidden;
}

.cart-page .table thead {
    background-color: #0d6efd;
    color: #fff;
}

.cart-page .table td, 
.cart-page .table th {
    vertical-align: middle !important;
    text-align: center;
    font-size: 15px;
}

.cart-page .total-row {
    background: #e9f5ff;
    font-weight: 700;
    color: #007bff;
}

/* === Thanh hành động (2 nút thêm & thanh toán) === */
.cart-page .cart-action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 22px;
    gap: 12px;
}

.cart-page .cart-btn {
    padding: 10px 22px;
    border-radius: 30px;
    font-weight: 600;
    font-size: 15px;
    transition: all 0.25s ease;
    box-shadow: 0 2px 8px rgba(0,0,0,0.08);
    text-decoration: none;
}

.cart-page .cart-btn i {
    margin-right: 8px;
}

/* Nút thêm sản phẩm */
.cart-page .cart-btn-primary {
    background-color: #007bff;
    color: white;
    border: none;
}
.cart-page .cart-btn-primary:hover {
    background-color: #0056b3;
    transform: translateY(-2px);
}

/* Nút thanh toán */
.cart-page .cart-btn-success {
    background-color: #28a745;
    color: white;
    border: none;
}
.cart-page .cart-btn-success:hover {
    background-color: #1e7e34;
    transform: translateY(-2px);
}

/* === Khi giỏ hàng trống === */
.cart-page .empty-cart {
    text-align: center;
    padding: 60px 0;
    font-size: 18px;
    color: #6c757d;
}

/* === Footer an toàn === */
.cart-page footer {
    margin-top: 60px;
}
</style>

</head>

<body class="cart-page">
    <%@ include file="header.jsp" %>

    <main>
        <div class="container">
            <div class="card">
                <h2>🛍️ Giỏ hàng của bạn</h2>

                <div class="cart-action-bar d-flex justify-content-between align-items-center mb-4">
                    <a href="${pageContext.request.contextPath}/user/view-products"
                       class="btn cart-btn cart-btn-primary">
                        <i class="fas fa-arrow-left"></i> Tiếp tục mua sắm
                    </a>

                    <a href="${pageContext.request.contextPath}/user/checkout"
                       class="btn cart-btn cart-btn-success">
                        <i class="fas fa-credit-card"></i> Thanh toán
                    </a>
                </div>
                <table class="table table-bordered">
                    <thead>
                        <tr>
                            <th>STT</th>
                            <th>Mã Biến Thể</th>
                            <th>Tên Sản Phẩm</th>
                            <th>Giá</th>
                            <th>Số lượng</th>
                            <th>Thành tiền</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        List<CartBean> cart = (List<CartBean>) session.getAttribute("cart");
                        if (cart != null && !cart.isEmpty()) {
                            double total = 0;
                            for (int i = 0; i < cart.size(); i++) {
                                CartBean item = cart.get(i);
                                ProductVariants variant = item.getProductVariant();
                                double price = variant.getPrice(); 
                                int quantity = item.getQuantity();
                                double subtotal = price * quantity;
                                total += subtotal;
                    %>
                        <tr>
                            <td><%= i + 1 %></td>
                            <td><%= variant.getId() %></td>
                            <td><%= variant.getProductName() %></td>
                            <td><%= String.format("%,.0f ₫", price) %></td>
                            <td><%= quantity %></td>
                            <td><%= String.format("%,.0f ₫", subtotal) %></td>
                            <td>
                                <a class="btn btn-danger btn-sm" 
                                   href="${pageContext.request.contextPath}/user/remove-from-cart?index=<%= i %>">
                                   <i class="fas fa-trash-alt"></i> Xóa
                                </a>
                            </td>
                        </tr>
                    <%  } %>
                        <tr class="total-row">
                            <td colspan="5" class="text-right"><strong>Tổng:</strong></td>
                            <td colspan="2"><strong><%= String.format("%,.0f ₫", total) %></strong></td>
                        </tr>
                    <%
                        } else {
                    %>
                        <tr>
                            <td colspan="7" class="empty-cart">🛒 Giỏ hàng của bạn đang trống</td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <%@ include file="footer.jsp" %>
</body>
</html>
