<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản lý biến thể sản phẩm</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <style>
        body {
            background-color: #f1f5f9;
            font-family: 'Inter', sans-serif;
            color: #334155;
        }
        .form-container {
            max-width: 750px; /* Rộng hơn chút để chia cột đẹp */
            margin: 50px auto;
            padding: 40px;
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
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
        .bg-readonly {
            background-color: #f8fafc;
            color: #94a3b8;
        }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />
    
    <c:set var="pv" value="${requestScope.VARIANT}" />
    <c:set var="action" value="${empty requestScope.ACTION ? 'SaveOrUpdate' : requestScope.ACTION}" />

    <div class="container">
        <div class="form-container">
            <h2>
                <c:choose>
                    <c:when test="${not empty pv.id}">🛠️ Cập nhật biến thể</c:when>
                    <c:otherwise>✨ Thêm biến thể mới</c:otherwise>
                </c:choose>
            </h2>

            <form action="${context}/admin/ProductVariantsManagerServlet" method="post">
                <input type="hidden" name="action" value="${action}">

                <div class="row">
                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">ID Biến thể</label>
                            <input type="text" class="form-control bg-readonly" name="id" value="${pv.id}" readonly placeholder="Tự động tạo">
                        </div>

                        <div class="mb-3">
                            <label class="form-label">ID Sản phẩm (Product ID)</label>
                            <input type="text" class="form-control" name="productId" value="${pv.productId}" required>
                            <div class="form-text">Nhập ID của sản phẩm chính.</div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">ID Kích thước (Size ID)</label>
                            <input type="text" class="form-control" name="sizeId" value="${pv.sizeId}" required>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="mb-3">
                            <label class="form-label">Tồn kho</label>
                            <input type="number" class="form-control" name="stock" value="${pv.stock}" min="0" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Giá bán</label>
                            <div class="input-group">
                                <input type="number" step="0.01" class="form-control" name="price" value="${pv.price}" min="0" required>
                                <span class="input-group-text">VNĐ</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="d-flex justify-content-center gap-3 mt-4">
                    <a href="${context}/admin/ProductVariantsManagerServlet?action=List" class="btn btn-outline-secondary btn-custom">
                        Hủy / Xem danh sách
                    </a>
                    <button type="submit" class="btn btn-success btn-custom px-5">
                        Lưu thông tin
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>