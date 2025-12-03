<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý chi tiết đơn hàng</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <style>
        body {
            background-color: #f1f5f9; /* Màu nền xám xanh hiện đại */
            font-family: 'Inter', sans-serif;
            color: #334155;
        }
        .form-container {
            max-width: 700px;
            margin: 50px auto;
            padding: 40px;
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
        }
        h2 {
            text-align: center;
            margin-bottom: 30px;
            font-weight: 700;
            color: #0f172a;
        }
        .form-label {
            font-weight: 600;
            font-size: 0.95rem;
            color: #475569;
        }
        .form-control {
            border-radius: 8px;
            padding: 10px 15px;
            border: 1px solid #cbd5e1;
        }
        .form-control:focus {
            border-color: #0ea5e9;
            box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.15);
        }
        .btn-custom {
            padding: 10px 20px;
            border-radius: 8px;
            font-weight: 600;
        }
        .bg-readonly {
            background-color: #f8fafc;
            color: #64748b;
        }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />
    
    <c:set var="detail" value="${requestScope.DETAIL}" />
    <c:set var="action" value="${empty requestScope.ACTION ? 'SaveOrUpdate' : requestScope.ACTION}" />

    <div class="container">
        <div class="form-container">
            <h2>
                <c:choose>
                    <c:when test="${not empty detail.id}">🖊️ Cập nhật chi tiết</c:when>
                    <c:otherwise>✨ Thêm chi tiết mới</c:otherwise>
                </c:choose>
            </h2>

            <form action="${context}/admin/OrderDetailManagerServlet" method="post">
                <input type="hidden" name="action" value="${action}">

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Mã chi tiết (ID)</label>
                        <input type="text" class="form-control bg-readonly" name="id" value="${detail.id}" readonly placeholder="Tự động tạo">
                    </div>

                    <div class="col-md-6 mb-3">
                        <label class="form-label">Mã đơn hàng (Order ID)</label>
                        <input type="text" class="form-control" name="orderId" value="${detail.orderId}" required>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Mã phiên bản sản phẩm (Variant ID)</label>
                    <input type="text" class="form-control" name="productVariantId" value="${detail.productVariantId}" required>
                    <div class="form-text">Nhập ID của biến thể sản phẩm (VD: Màu sắc/Size).</div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Tên sản phẩm (Hiển thị)</label>
                    <input type="text" class="form-control bg-readonly" value="${empty detail.productName ? 'Chưa xác định' : detail.productName}" readonly>
                </div>

                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Số lượng</label>
                        <input type="number" class="form-control" name="quantity" value="${detail.quantity}" min="1" required>
                    </div>

                    <div class="col-md-6 mb-3">
                        <label class="form-label">Đơn giá</label>
                        <div class="input-group">
                            <input type="number" step="0.01" class="form-control" name="price" value="${detail.price}" required>
                            <span class="input-group-text">VNĐ</span>
                        </div>
                    </div>
                </div>

                <div class="d-flex justify-content-between mt-4">
                    <a href="${context}/admin/OrdersManagerServlet?action=Detail&id=${detail.orderId}" class="btn btn-outline-secondary btn-custom">
                        ⬅ Quay lại đơn hàng
                    </a>
                    
                    <button type="submit" class="btn btn-primary btn-custom px-5">
                        💾 Lưu thông tin
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>