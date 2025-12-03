<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán & Đặt hàng - ShopDuck</title>

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
            color: #334155;
            display: flex;
            flex-direction: column;
            min-height: 100vh;
        }

        main { flex: 1; padding: 40px 0; }

        .checkout-container {
            max-width: 900px;
            margin: 0 auto;
        }

        .checkout-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
            overflow: hidden;
            border: 1px solid #e2e8f0;
        }

        .checkout-header {
            background-color: #fff;
            padding: 25px 30px;
            border-bottom: 1px solid #f1f5f9;
        }
        .checkout-header h3 {
            margin: 0;
            font-weight: 700;
            color: #0f172a;
            font-size: 1.5rem;
        }

        .checkout-body { padding: 30px; }

        /* Table Styles */
        .table thead th {
            background-color: #f8fafc;
            color: #64748b;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.85rem;
            border-bottom: 2px solid #e2e8f0;
        }
        .table tbody td {
            vertical-align: middle;
            color: #334155;
            padding: 15px;
        }
        
        .product-name {
            font-weight: 600;
            color: #1e293b;
        }
        
        .total-row td {
            background-color: #f8fafc;
            font-weight: 700;
            font-size: 1.1rem;
            color: #0f172a;
        }
        .total-price { color: #2563eb; }

        /* Form Styles */
        .form-label {
            font-weight: 600;
            color: #475569;
            font-size: 0.95rem;
        }
        .form-control {
            border-radius: 8px;
            padding: 12px;
            border: 1px solid #cbd5e1;
        }
        .form-control:focus {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
        }

        .btn-confirm {
            background-color: #10b981;
            color: white;
            font-weight: 700;
            padding: 15px;
            border-radius: 10px;
            width: 100%;
            border: none;
            font-size: 1.1rem;
            transition: 0.2s;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .btn-confirm:hover {
            background-color: #059669;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
        }

        .back-link {
            display: inline-flex;
            align-items: center;
            gap: 5px;
            color: #64748b;
            text-decoration: none;
            font-weight: 500;
            margin-top: 20px;
            transition: 0.2s;
        }
        .back-link:hover { color: #2563eb; }
        
        /* Empty State */
        .empty-cart {
            text-align: center;
            padding: 50px 20px;
        }
        .empty-icon {
            font-size: 4rem;
            color: #cbd5e1;
            margin-bottom: 20px;
        }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <!-- 2. INCLUDE HEADER -->
    <jsp:include page="../includes/header.jsp" />

    <main>
        <div class="container checkout-container">
            
            <!-- KIỂM TRA GIỎ HÀNG -->
            <c:choose>
                <c:when test="${empty sessionScope.cart}">
                    <!-- GIỎ HÀNG TRỐNG -->
                    <div class="checkout-card empty-cart">
                        <i class="bi bi-cart-x empty-icon"></i>
                        <h3>Giỏ hàng của bạn đang trống!</h3>
                        <p class="text-muted mb-4">Hãy chọn thêm sản phẩm để tiến hành thanh toán.</p>
                        <a href="${context}/user/product/view-products.jsp" class="btn btn-primary px-4 py-2 rounded-pill">
                            Quay lại cửa hàng
                        </a>
                    </div>
                </c:when>

                <c:otherwise>
                    <!-- CÓ SẢN PHẨM -->
                    <form action="${context}/user/confirm-checkout" method="post">
                        <div class="checkout-card">
                            <div class="checkout-header d-flex justify-content-between align-items-center">
                                <h3><i class="bi bi-bag-check-fill text-primary me-2"></i> Xác nhận đơn hàng</h3>
                                <span class="badge bg-light text-dark border">
                                    ${sessionScope.cart.size()} sản phẩm
                                </span>
                            </div>

                            <div class="checkout-body">
                                
                                <!-- 1. DANH SÁCH SẢN PHẨM -->
                                <h5 class="mb-3 text-uppercase text-muted" style="font-size: 0.85rem; font-weight: 700; letter-spacing: 1px;">
                                    Thông tin sản phẩm
                                </h5>
                                <div class="table-responsive mb-5">
                                    <table class="table table-bordered mb-0">
                                        <thead>
                                            <tr>
                                                <th>Sản phẩm</th>
                                                <th class="text-center">Số lượng</th>
                                                <th class="text-end">Đơn giá</th>
                                                <th class="text-end">Thành tiền</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:set var="grandTotal" value="0" />
                                            
                                            <c:forEach var="item" items="${sessionScope.cart}">
                                                <!-- Tính tổng từng món -->
                                                <c:set var="itemTotal" value="${item.productVariant.price * item.quantity}" />
                                                <!-- Cộng dồn vào tổng đơn -->
                                                <c:set var="grandTotal" value="${grandTotal + itemTotal}" />

                                                <tr>
                                                    <td>
                                                        <div class="product-name">${item.productVariant.productName}</div>
                                                        <small class="text-muted">Phân loại: ${item.productVariant.sizeName}</small>
                                                    </td>
                                                    <td class="text-center">${item.quantity}</td>
                                                    <td class="text-end">
                                                        <fmt:formatNumber value="${item.productVariant.price}" type="number"/> đ
                                                    </td>
                                                    <td class="text-end fw-bold">
                                                        <fmt:formatNumber value="${itemTotal}" type="number"/> đ
                                                    </td>
                                                </tr>
                                            </c:forEach>

                                            <!-- HÀNG TỔNG CỘNG -->
                                            <tr class="total-row">
                                                <td colspan="3" class="text-end">TỔNG THANH TOÁN:</td>
                                                <td class="text-end total-price">
                                                    <fmt:formatNumber value="${grandTotal}" type="number"/> đ
                                                </td>
                                            </tr>
                                        </tbody>
                                    </table>
                                </div>

                                <!-- 2. THÔNG TIN GIAO HÀNG -->
                                <h5 class="mb-3 text-uppercase text-muted" style="font-size: 0.85rem; font-weight: 700; letter-spacing: 1px;">
                                    Thông tin giao hàng
                                </h5>
                                
                                <!-- Hiển thị lỗi nếu có -->
                                <c:if test="${not empty requestScope.error}">
                                    <div class="alert alert-danger d-flex align-items-center mb-4">
                                        <i class="bi bi-exclamation-circle-fill me-2"></i>
                                        ${requestScope.error}
                                    </div>
                                </c:if>

                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="fullname" class="form-label">Người nhận</label>
                                        <!-- Tự điền tên user nếu đã đăng nhập -->
                                        <input type="text" class="form-control" value="${sessionScope.user.fullname}" readonly style="background-color: #f8fafc;">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="phone" class="form-label">Số điện thoại <span class="text-danger">*</span></label>
                                        <input type="tel" class="form-control" id="phone" name="phone" 
                                               placeholder="09xx..." required pattern="[0-9]{10}">
                                    </div>
                                    <div class="col-12 mb-4">
                                        <label for="address" class="form-label">Địa chỉ nhận hàng <span class="text-danger">*</span></label>
                                        <textarea class="form-control" id="address" name="address" rows="3" 
                                                  placeholder="Số nhà, tên đường, phường/xã, quận/huyện..." required></textarea>
                                    </div>
                                </div>

                                <button type="submit" class="btn-confirm">
                                    <i class="bi bi-bag-check me-2"></i> Đặt hàng ngay
                                </button>
                                
                                <div class="text-center">
                                    <a href="${context}/user/order/view-cart.jsp" class="back-link">
                                        <i class="bi bi-arrow-left"></i> Quay lại giỏ hàng
                                    </a>
                                </div>

                            </div>
                        </div>
                    </form>
                </c:otherwise>
            </c:choose>
            
        </div>
    </main>

    <!-- 3. INCLUDE FOOTER -->
    <jsp:include page="../includes/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>