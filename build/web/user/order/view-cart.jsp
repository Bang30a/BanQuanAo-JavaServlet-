<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- Kiểm tra đăng nhập --%>
<c:if test="${empty sessionScope.user}">
    <%-- Redirect đúng về trang login --%>
    <c:redirect url="/user/auth/Login.jsp" />
</c:if>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giỏ hàng của bạn - ShopDuck</title>

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/user/assets/css/style.css">

    <style>
        body {
            background-color: #f8fafc;
            font-family: 'Inter', sans-serif;
            color: #334155;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        main { flex: 1; padding: 40px 0; }

        .cart-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            overflow: hidden;
            border: 1px solid #e2e8f0;
            padding: 30px;
        }

        h2 {
            font-weight: 700;
            color: #0f172a;
            margin-bottom: 25px;
            text-align: center;
        }

        /* Table Styles */
        .table thead th {
            background-color: #f1f5f9;
            color: #64748b;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.85rem;
            border-bottom: 2px solid #e2e8f0;
            padding: 15px;
            text-align: center;
        }
        .table tbody td {
            vertical-align: middle;
            padding: 15px;
            text-align: center;
            color: #334155;
        }
        
        .product-name {
            font-weight: 600;
            color: #1e293b;
            text-align: left;
        }
        
        .total-row td {
            background-color: #f8fafc;
            font-weight: 700;
            font-size: 1.1rem;
            color: #0f172a;
            text-align: right;
        }
        .total-amount { color: #2563eb; font-size: 1.2rem; }

        /* Buttons */
        .btn-custom {
            border-radius: 8px;
            font-weight: 600;
            padding: 8px 20px;
            transition: 0.2s;
        }
        .btn-continue {
            background-color: #fff;
            color: #2563eb;
            border: 1px solid #bfdbfe;
        }
        .btn-continue:hover {
            background-color: #eff6ff;
            color: #1d4ed8;
        }
        .btn-checkout {
            background-color: #10b981;
            color: white;
            border: none;
        }
        .btn-checkout:hover {
            background-color: #059669;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
        }
        .btn-remove {
            color: #ef4444;
            background: #fee2e2;
            border: none;
            padding: 6px 12px;
            border-radius: 6px;
        }
        .btn-remove:hover {
            background: #fecaca;
            color: #dc2626;
        }

        /* Empty State */
        .empty-state {
            text-align: center;
            padding: 60px 20px;
        }
        .empty-icon {
            font-size: 4rem;
            color: #cbd5e1;
            margin-bottom: 20px;
        }
        
        /* Ẩn nút mũi tên tăng giảm mặc định của input number */
        input[type=number]::-webkit-inner-spin-button, 
        input[type=number]::-webkit-outer-spin-button { 
            -webkit-appearance: none; 
            margin: 0; 
        }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <jsp:include page="../includes/header.jsp" />

    <main>
        <div class="container" style="max-width: 1000px;">
            <div class="cart-card">
                <h2>🛍️ Giỏ hàng của bạn</h2>

                <div class="d-flex justify-content-between align-items-center mb-4">
                    <a href="${context}/user/view-products" class="btn btn-custom btn-continue text-decoration-none">
                        <i class="bi bi-arrow-left"></i> Tiếp tục mua sắm
                    </a>
                    
                    <c:if test="${not empty sessionScope.cart && sessionScope.cart.size() > 0}">
                        <a href="${context}/user/checkout" class="btn btn-custom btn-checkout text-decoration-none">
                            Thanh toán <i class="bi bi-credit-card-2-front ms-1"></i>
                        </a>
                    </c:if>
                </div>

                <c:choose>
                    <c:when test="${not empty sessionScope.cart && sessionScope.cart.size() > 0}">
                        <div class="table-responsive">
                            <table class="table table-hover mb-0">
                                <thead>
                                    <tr>
                                        <th width="50">STT</th>
                                        <th>Sản phẩm</th>
                                        <th>Đơn giá</th>
                                        <th width="140">Số lượng</th> <th>Thành tiền</th>
                                        <th width="80">Xóa</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:set var="grandTotal" value="0" />
                                    
                                    <c:forEach var="item" items="${sessionScope.cart}" varStatus="status">
                                        <c:set var="subtotal" value="${item.productVariant.price * item.quantity}" />
                                        <c:set var="grandTotal" value="${grandTotal + subtotal}" />

                                        <tr>
                                            <td>${status.count}</td>
                                            <td>
                                                <div class="product-name">${item.productVariant.productName}</div>
                                                <small class="text-muted">
                                                    Phân loại: ${item.productVariant.sizeName} 
                                                    (Mã: #${item.productVariant.id})
                                                </small>
                                            </td>
                                            <td>
                                                <fmt:formatNumber value="${item.productVariant.price}" type="number"/> đ
                                            </td>
                                            
                                            <td>
                                                <form action="${context}/user/update-cart" method="post" class="d-flex align-items-center justify-content-center">
                                                    <input type="hidden" name="variantId" value="${item.productVariant.id}" />
                                                    
                                                    <input type="number" name="quantity" value="${item.quantity}" min="1" 
                                                           class="form-control form-control-sm text-center me-1" 
                                                           style="width: 60px; border-radius: 6px;"
                                                           onchange="this.form.submit()" /> 

                                                    <button type="submit" class="btn btn-sm text-primary border-0" title="Cập nhật" style="background: none;">
                                                        <i class="bi bi-arrow-clockwise"></i>
                                                    </button>
                                                </form>
                                            </td>
                                            <td class="fw-bold text-primary">
                                                <fmt:formatNumber value="${subtotal}" type="number"/> đ
                                            </td>
                                            <td>
                                                <a href="${context}/user/remove-from-cart?index=${status.index}" 
                                                   class="btn-remove" title="Xóa khỏi giỏ"
                                                   onclick="return confirm('Bạn muốn xóa sản phẩm này?')">
                                                    <i class="bi bi-trash"></i>
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>

                                    <tr class="total-row">
                                        <td colspan="4">TỔNG CỘNG:</td>
                                        <td class="total-amount">
                                            <fmt:formatNumber value="${grandTotal}" type="number"/> đ
                                        </td>
                                        <td></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    
                    <c:otherwise>
                        <div class="empty-state">
                            <i class="bi bi-cart-x empty-icon"></i>
                            <h4 class="text-muted">Giỏ hàng của bạn đang trống</h4>
                            <p class="text-secondary mb-4">Hãy chọn những món đồ yêu thích để lấp đầy giỏ hàng nhé!</p>
                            
                            <a href="${context}/user/view-products" class="btn btn-primary px-4 py-2 rounded-pill">
                                Xem sản phẩm ngay
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>

            </div>
        </div>
    </main>

    <jsp:include page="../includes/footer.jsp" />

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>