<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chi tiết đơn hàng</title>

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background-color: #f1f5f9;
            font-family: 'Inter', sans-serif;
            color: #334155;
        }
        .container-box {
            background: #ffffff;
            padding: 40px;
            border-radius: 16px;
            margin: 40px auto;
            max-width: 900px; /* Rộng hơn chút để bảng đẹp */
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
        }
        h2 {
            font-weight: 700;
            color: #0f172a;
            margin-bottom: 30px;
        }
        /* Custom Table */
        .table {
            margin-bottom: 0;
            border-collapse: separate; 
            border-spacing: 0;
        }
        .table thead th {
            background-color: #f8fafc;
            color: #475569;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.85rem;
            border-bottom: 2px solid #e2e8f0;
            padding: 15px;
        }
        .table tbody td {
            padding: 15px;
            vertical-align: middle;
            border-bottom: 1px solid #f1f5f9;
            color: #1e293b;
        }
        .table-hover tbody tr:hover {
            background-color: #f8fafc;
        }
        
        /* Price Column */
        .price-text {
            font-weight: 600;
            color: #2563eb;
        }

        /* Back Button */
        .btn-back {
            background-color: #fff;
            color: #64748b;
            border: 1px solid #cbd5e1;
            padding: 10px 24px;
            border-radius: 8px;
            font-weight: 600;
            text-decoration: none;
            transition: all 0.2s;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }
        .btn-back:hover {
            background-color: #f1f5f9;
            color: #0f172a;
            border-color: #94a3b8;
        }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />

    <div class="container container-box">

        <h2 class="text-center">
            📦 Chi tiết đơn hàng #${requestScope.ORDER_ID}
        </h2>

        <div class="table-responsive mt-4">
            <table class="table table-hover align-middle">
                <thead>
                    <tr>
                        <th class="text-center" width="80">ID</th>
                        <th class="text-center">Variant ID</th>
                        <th>Tên sản phẩm</th>
                        <th class="text-center">Số lượng</th>
                        <th class="text-end">Đơn giá</th>
                        <th class="text-end">Thành tiền</th> </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty requestScope.DETAILS}">
                            <c:forEach var="od" items="${requestScope.DETAILS}">
                                <tr>
                                    <td class="text-center text-muted">#${od.id}</td>
                                    <td class="text-center">
                                        <span class="badge bg-light text-dark border">
                                            ${od.productVariantId}
                                        </span>
                                    </td>
                                    <td>
                                        <strong>${empty od.productName ? 'Sản phẩm không xác định' : od.productName}</strong>
                                    </td>
                                    <td class="text-center">${od.quantity}</td>
                                    <td class="text-end text-muted">
                                        <fmt:formatNumber value="${od.price}" type="number" groupingUsed="true"/> đ
                                    </td>
                                    <td class="text-end price-text">
                                        <fmt:formatNumber value="${od.price * od.quantity}" type="number" groupingUsed="true"/> đ
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="6" class="text-center py-5 text-muted">
                                    <img src="https://cdn-icons-png.flaticon.com/512/4076/4076432.png" width="64" style="opacity: 0.5; margin-bottom: 10px;"><br>
                                    Chưa có sản phẩm nào trong đơn hàng này.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>

        <div class="text-center mt-5">
            <a href="${context}/admin/OrdersManagerServlet?action=List" class="btn-back">
                ⬅ Quay lại danh sách
            </a>
            
            <a href="${context}/admin/OrderDetailManagerServlet?action=Create&orderId=${requestScope.ORDER_ID}" class="btn btn-primary px-4 py-2 ms-3" style="border-radius: 8px;">
                + Thêm sản phẩm
            </a>
        </div>

    </div>

</body>
</html>