<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chi tiết đơn hàng #${order.id} - ShopDuck</title>

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

        .detail-card {
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
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 15px;
        }

        .order-id { font-size: 1.25rem; font-weight: 700; color: #0f172a; margin: 0; }
        .order-date { color: #64748b; font-size: 0.9rem; }

        /* Status Badges */
        .badge-status {
            font-size: 0.85rem;
            padding: 6px 12px;
            border-radius: 6px;
            font-weight: 600;
        }
        .status-pending { background-color: #f1f5f9; color: #475569; }
        .status-shipping { background-color: #dbeafe; color: #2563eb; }
        .status-success { background-color: #dcfce7; color: #16a34a; }
        .status-cancel { background-color: #fee2e2; color: #dc2626; }

        /* Info Section */
        .info-section {
            padding: 30px;
            background-color: #f8fafc;
            border-bottom: 1px solid #e2e8f0;
        }
        .info-label {
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #64748b;
            font-weight: 600;
            margin-bottom: 8px;
        }
        .info-value {
            font-weight: 500;
            color: #1e293b;
            font-size: 1rem;
        }

        /* Product Table */
        .table thead th {
            background-color: #fff;
            color: #64748b;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.8rem;
            border-bottom: 2px solid #e2e8f0;
        }
        .table tbody td {
            vertical-align: middle;
            padding: 15px;
        }
        .product-img {
            width: 60px;
            height: 60px;
            object-fit: cover;
            border-radius: 8px;
            border: 1px solid #e2e8f0;
        }
        
        .total-box {
            padding: 20px 30px;
            text-align: right;
            background-color: #fff;
        }
        .total-label { font-size: 1rem; color: #64748b; margin-right: 15px; }
        .total-amount { font-size: 1.5rem; color: #2563eb; font-weight: 700; }

        .btn-back {
            text-decoration: none;
            color: #64748b;
            font-weight: 500;
            display: inline-flex;
            align-items: center;
            gap: 5px;
            transition: 0.2s;
        }
        .btn-back:hover { color: #2563eb; }
    </style>
</head>

<body>
    <c:set var="context" value="${pageContext.request.contextPath}" />

    <!-- 2. INCLUDE HEADER -->
    <jsp:include page="../includes/header.jsp" />

    <main>
        <div class="container" style="max-width: 900px;">
            
            <!-- Nút Quay lại -->
            <div class="mb-4">
                <a href="${context}/user/order-history" class="btn-back">
                    <i class="bi bi-arrow-left"></i> Quay lại Lịch sử đơn hàng
                </a>
            </div>

            <c:choose>
                <c:when test="${not empty order}">
                    <div class="detail-card">
                        
                        <!-- Header: ID + Ngày + Trạng thái -->
                        <div class="card-header-custom">
                            <div>
                                <h1 class="order-id">Đơn hàng #${order.id}</h1>
                                <span class="order-date">
                                    Ngày đặt: <fmt:formatDate value="${order.orderDate}" pattern="HH:mm dd/MM/yyyy" />
                                </span>
                            </div>
                            <div>
                                <!-- Logic tô màu Badge trạng thái -->
                                <c:choose>
                                    <c:when test="${order.status == 'Đã giao'}">
                                        <span class="badge-status status-success"><i class="bi bi-check-circle-fill"></i> Đã giao thành công</span>
                                    </c:when>
                                    <c:when test="${order.status == 'Đã hủy'}">
                                        <span class="badge-status status-cancel"><i class="bi bi-x-circle-fill"></i> Đã hủy</span>
                                    </c:when>
                                    <c:when test="${order.status == 'Đang giao hàng'}">
                                        <span class="badge-status status-shipping"><i class="bi bi-truck"></i> Đang giao hàng</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-status status-pending"><i class="bi bi-hourglass-split"></i> ${order.status}</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Thông tin người nhận -->
                        <div class="info-section">
                            <div class="row">
                                <div class="col-md-6 mb-3 mb-md-0">
                                    <div class="info-label">Địa chỉ giao hàng</div>
                                    <div class="info-value">
                                        <i class="bi bi-geo-alt-fill text-danger me-1"></i> ${order.address}
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="info-label">Số điện thoại</div>
                                    <div class="info-value">
                                        <i class="bi bi-telephone-fill text-primary me-1"></i> ${order.phone}
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Danh sách sản phẩm -->
                        <div class="table-responsive">
                            <table class="table mb-0">
                                <thead>
                                    <tr>
                                        <th class="ps-4">Sản phẩm</th>
                                        <th class="text-center">Số lượng</th>
                                        <th class="text-end pe-4">Thành tiền</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="item" items="${detailsList}">
                                        <tr>
                                            <td class="ps-4">
                                                <div class="d-flex align-items-center">
                                                    <!-- Ảnh sản phẩm -->
                                                    <img src="${not empty item.productImage ? item.productImage : 'https://placehold.co/60x60?text=No+Img'}" 
                                                         class="product-img me-3" alt="${item.productName}">
                                                    
                                                    <div>
                                                        <div class="fw-bold text-dark">${item.productName}</div>
                                                        <small class="text-muted">Phân loại: ${item.sizeLabel}</small>
                                                    </div>
                                                </div>
                                            </td>
                                            <td class="text-center">x${item.quantity}</td>
                                            <td class="text-end pe-4 fw-bold">
                                                <fmt:formatNumber value="${item.price * item.quantity}" type="number"/> đ
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>

                        <!-- Tổng tiền -->
                        <div class="total-box">
                            <span class="total-label">Tổng thanh toán:</span>
                            <span class="total-amount">
                                <fmt:formatNumber value="${order.total}" type="number"/> đ
                            </span>
                        </div>

                    </div>
                </c:when>
                
                <c:otherwise>
                    <div class="alert alert-warning text-center">
                        Không tìm thấy thông tin đơn hàng này.
                    </div>
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