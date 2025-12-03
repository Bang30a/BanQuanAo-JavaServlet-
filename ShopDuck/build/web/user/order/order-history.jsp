<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lịch sử đơn hàng - ShopDuck</title>

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

        .history-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            overflow: hidden;
            border: 1px solid #e2e8f0;
        }

        .card-header-custom {
            background-color: #fff;
            padding: 20px 30px;
            border-bottom: 1px solid #f1f5f9;
        }
        
        h2 {
            font-weight: 700;
            color: #0f172a;
            margin: 0;
            font-size: 1.5rem;
        }

        /* Table Styles */
        .table thead th {
            background-color: #f8fafc;
            color: #64748b;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.85rem;
            border-bottom: 2px solid #e2e8f0;
            padding: 15px;
        }
        .table tbody td {
            vertical-align: middle;
            padding: 15px;
            font-size: 0.95rem;
        }

        /* Status Badges */
        .badge-status {
            font-size: 0.85rem;
            padding: 6px 12px;
            border-radius: 6px;
            font-weight: 600;
            display: inline-block;
        }
        .status-pending { background-color: #fff7ed; color: #c2410c; border: 1px solid #ffedd5; }
        .status-shipping { background-color: #eff6ff; color: #1d4ed8; border: 1px solid #dbeafe; }
        .status-success { background-color: #f0fdf4; color: #15803d; border: 1px solid #dcfce7; }
        .status-cancel { background-color: #fef2f2; color: #b91c1c; border: 1px solid #fee2e2; }

        .btn-view {
            background-color: #fff;
            color: #2563eb;
            border: 1px solid #bfdbfe;
            padding: 6px 16px;
            border-radius: 8px;
            font-weight: 600;
            font-size: 0.9rem;
            transition: 0.2s;
            text-decoration: none;
            display: inline-block;
        }
        .btn-view:hover {
            background-color: #2563eb;
            color: white;
        }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <!-- 2. INCLUDE HEADER -->
    <jsp:include page="../includes/header.jsp" />

    <main>
        <div class="container" style="max-width: 1000px;">
            
            <div class="history-card">
                <div class="card-header-custom">
                    <h2><i class="bi bi-clock-history text-primary me-2"></i> Lịch sử đơn hàng</h2>
                </div>

                <div class="card-body p-0">
                    <c:choose>
                        <c:when test="${not empty orderList}">
                            <div class="table-responsive">
                                <table class="table table-hover mb-0">
                                    <thead>
                                        <tr>
                                            <th class="ps-4">Mã Đơn</th>
                                            <th>Ngày đặt</th>
                                            <th>Địa chỉ nhận hàng</th>
                                            <th class="text-end">Tổng tiền</th>
                                            <th class="text-center">Trạng thái</th>
                                            <th class="text-center pe-4">Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="order" items="${orderList}">
                                            <tr>
                                                <td class="ps-4 fw-bold text-muted">#${order.id}</td>
                                                <td>
                                                    <fmt:formatDate value="${order.orderDate}" pattern="dd/MM/yyyy HH:mm" />
                                                </td>
                                                <td style="max-width: 250px;" class="text-truncate" title="${order.address}">
                                                    ${order.address}
                                                </td>
                                                <td class="text-end fw-bold text-dark">
                                                    <fmt:formatNumber value="${order.total}" type="number" /> đ
                                                </td>
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${order.status == 'Đã giao'}">
                                                            <span class="badge-status status-success">Đã giao</span>
                                                        </c:when>
                                                        <c:when test="${order.status == 'Đang giao hàng'}">
                                                            <span class="badge-status status-shipping">Đang giao</span>
                                                        </c:when>
                                                        <c:when test="${order.status == 'Đã hủy'}">
                                                            <span class="badge-status status-cancel">Đã hủy</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge-status status-pending">${order.status}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="text-center pe-4">
                                                    <!-- Chú ý: Link này có thể cần sửa tùy theo Servlet của bạn -->
                                                    <!-- Ví dụ: OrderHistoryServlet?action=detail&id=... -->
                                                    <a href="${context}/user/order/order-detail.jsp?id=${order.id}" class="btn-view">
                                                        Chi tiết
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center py-5">
                                <div style="font-size: 3rem; margin-bottom: 15px; opacity: 0.3;">📦</div>
                                <h5 class="text-muted">Bạn chưa có đơn hàng nào</h5>
                                <a href="${context}/user/product/view-products.jsp" class="btn btn-primary mt-3 px-4 rounded-pill">
                                    Mua sắm ngay
                                </a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

        </div>
    </main>

    <!-- 3. INCLUDE FOOTER -->
    <jsp:include page="../includes/footer.jsp" />

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>