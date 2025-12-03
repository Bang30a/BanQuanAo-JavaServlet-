<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý đơn hàng</title>
    <!-- Font Inter & Bootstrap -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <style>
        body {
            background-color: #f1f5f9;
            font-family: 'Inter', sans-serif;
            color: #334155;
        }
        .form-container {
            max-width: 800px; /* Rộng hơn chút để chia cột */
            margin: 40px auto;
            padding: 40px;
            background: white;
            border-radius: 16px;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
        }
        h2 {
            text-align: center;
            margin-bottom: 30px;
            font-weight: 700;
            color: #0f172a;
        }
        .form-label {
            font-weight: 600;
            font-size: 0.9rem;
            color: #475569;
            margin-bottom: 0.5rem;
        }
        .form-control {
            border-radius: 8px;
            padding: 10px 15px;
            border: 1px solid #cbd5e1;
        }
        .form-control:focus {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
        }
        .btn-custom {
            padding: 10px 24px;
            border-radius: 8px;
            font-weight: 600;
        }
    </style>
</head>
<body>

    <!-- Lấy Context Path -->
    <c:set var="context" value="${pageContext.request.contextPath}" />
    
    <!-- Lấy dữ liệu từ Servlet -->
    <c:set var="order" value="${requestScope.ORDER}" />
    <c:set var="action" value="${empty requestScope.ACTION ? 'SaveOrUpdate' : requestScope.ACTION}" />

    <div class="container">
        <div class="form-container">
            <h2>
                <c:choose>
                    <c:when test="${not empty order.orderId}">📝 Cập nhật đơn hàng</c:when>
                    <c:otherwise>✨ Tạo đơn hàng mới</c:otherwise>
                </c:choose>
            </h2>

            <form action="${context}/admin/OrdersManager" method="post">
                <input type="hidden" name="action" value="${action}">
                <!-- Nếu update thì cần gửi kèm ID đơn hàng (ẩn) -->
                <c:if test="${not empty order.orderId}">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                </c:if>

                <div class="row">
                    <!-- Cột trái -->
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">ID Người dùng (User ID)</label>
                            <!-- Nếu đang sửa thì không cho đổi User ID để tránh lỗi logic -->
                            <input type="text" class="form-control ${not empty order.orderId ? 'bg-light' : ''}" 
                                   name="userId" value="${order.userId}" 
                                   ${not empty order.orderId ? 'readonly' : 'required'}>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Số điện thoại</label>
                            <input type="tel" class="form-control" name="phone" value="${order.phone}" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Tổng tiền</label>
                            <div class="input-group">
                                <input type="number" step="0.01" class="form-control" name="total" value="${order.total}" required>
                                <span class="input-group-text">VNĐ</span>
                            </div>
                        </div>
                    </div>

                    <!-- Cột phải -->
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">Ngày đặt hàng</label>
                            <input type="text" class="form-control" name="orderDate" 
                                   value="${order.orderDate}" required 
                                   placeholder="yyyy-mm-dd hh:mm:ss">
                            <div class="form-text text-muted">Định dạng: Năm-Tháng-Ngày Giờ:Phút:Giây</div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Địa chỉ giao hàng</label>
                            <textarea class="form-control" name="address" rows="4" required>${order.address}</textarea>
                        </div>
                    </div>
                </div>

                <!-- Nút bấm -->
                <div class="d-flex justify-content-center gap-3 mt-4">
                    <a href="View-orders.jsp" class="btn btn-outline-secondary btn-custom">
                        Hủy / Xem danh sách
                    </a>
                    <button type="submit" class="btn btn-primary btn-custom px-5">
                        Lưu Đơn Hàng
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Bootstrap 5 JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>