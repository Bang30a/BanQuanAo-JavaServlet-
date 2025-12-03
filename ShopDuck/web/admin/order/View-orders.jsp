<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Danh sách Đơn hàng</title>
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
            padding: 30px;
            border-radius: 16px;
            margin: 40px auto;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
        }
        h2 {
            font-weight: 700;
            color: #0f172a;
            margin-bottom: 25px;
            text-align: center;
        }

        /* Filter Section */
        .filter-box {
            background: #f8fafc;
            padding: 15px;
            border-radius: 12px;
            border: 1px solid #e2e8f0;
            display: flex;
            align-items: center;
            gap: 15px;
            margin-bottom: 20px;
        }
        .form-select-filter {
            max-width: 200px;
            border-radius: 8px;
        }
        .btn-filter {
            background-color: #3b82f6;
            color: white;
            border-radius: 8px;
            padding: 6px 20px;
            border: none;
            font-weight: 500;
            transition: 0.2s;
        }
        .btn-filter:hover { background-color: #2563eb; }

        /* Table */
        .table thead th {
            background-color: #1e293b;
            color: #fff;
            font-weight: 600;
            border: none;
            padding: 12px;
            font-size: 0.9rem;
        }
        .table tbody td {
            vertical-align: middle;
            font-size: 0.95rem;
            padding: 12px;
        }
        .table-hover tbody tr:hover { background-color: #f1f5f9; }

        /* Status Colors */
        .status-select {
            border-radius: 6px;
            font-size: 0.85rem;
            padding: 4px 8px;
            font-weight: 500;
            border: 1px solid #cbd5e1;
            cursor: pointer;
        }
        .status-select:focus { border-color: #3b82f6; outline: none; }

        /* Action Buttons */
        .btn-action {
            padding: 5px 12px;
            border-radius: 6px;
            font-size: 0.85rem;
            text-decoration: none;
            font-weight: 500;
            display: inline-block;
        }
        .btn-view { background-color: #0ea5e9; color: white; }
        .btn-view:hover { background-color: #0284c7; color: white; }
        .btn-delete { background-color: #ef4444; color: white; }
        .btn-delete:hover { background-color: #dc2626; color: white; }
    </style>
</head>
<body>

    <c:set var="context" value="${pageContext.request.contextPath}" />

    <div class="container container-box">
        <h2>📋 Quản lý Đơn hàng</h2>

        <form method="get" action="${context}/admin/OrdersManagerServlet">
            <input type="hidden" name="action" value="List">
            <div class="filter-box">
                <label class="fw-bold text-muted">Lọc theo trạng thái:</label>
                <select name="status" class="form-select form-select-filter">
                    <option value="">-- Tất cả --</option>
                    <option value="Chờ xử lý" ${param.status == 'Chờ xử lý' ? 'selected' : ''}>Chờ xử lý</option>
                    <option value="Đang giao hàng" ${param.status == 'Đang giao hàng' ? 'selected' : ''}>Đang giao hàng</option>
                    <option value="Đã giao" ${param.status == 'Đã giao' ? 'selected' : ''}>Đã giao</option>
                    <option value="Đã hủy" ${param.status == 'Đã hủy' ? 'selected' : ''}>Đã hủy</option>
                </select>
                <button type="submit" class="btn-filter">Lọc ngay</button>
            </div>
        </form>

        <div class="table-responsive">
            <table class="table table-hover table-bordered text-center">
                <thead>
                    <tr>
                        <th>Mã đơn</th>
                        <th>User ID</th>
                        <th>Ngày đặt</th>
                        <th>Tổng tiền</th>
                        <th>Địa chỉ</th>
                        <th>SĐT</th>
                        <th>Trạng thái (Cập nhật nhanh)</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty requestScope.ORDERS}">
                            <c:forEach var="item" items="${requestScope.ORDERS}">
                                <tr>
                                    <td><strong>#${item.id}</strong></td>
                                    <td>${item.userId}</td>
                                    <td>${item.orderDate}</td>
                                    <td class="text-primary fw-bold">
                                        <fmt:formatNumber value="${item.total}" type="number" groupingUsed="true"/> đ
                                    </td>
                                    <td class="text-start" style="max-width: 200px;">${item.address}</td>
                                    <td>${item.phone}</td>
                                    
                                    <td style="width: 180px;">
                                        <form action="${context}/admin/OrdersManagerServlet" method="post" class="m-0">
                                            <input type="hidden" name="action" value="UpdateStatus">
                                            <input type="hidden" name="id" value="${item.id}">
                                            
                                            <select name="status" class="status-select w-100" onchange="this.form.submit()"
                                                style="
                                                background-color: 
                                                    ${item.status == 'Đã giao' ? '#dcfce7' : 
                                                      item.status == 'Đã hủy' ? '#fee2e2' : 
                                                      item.status == 'Đang giao hàng' ? '#e0f2fe' : '#f1f5f9'};
                                                color: 
                                                    ${item.status == 'Đã giao' ? '#166534' : 
                                                      item.status == 'Đã hủy' ? '#991b1b' : 
                                                      item.status == 'Đang giao hàng' ? '#075985' : '#475569'};
                                                ">
                                                <option value="Chờ xử lý" ${item.status == 'Chờ xử lý' ? 'selected' : ''}>⏳ Chờ xử lý</option>
                                                <option value="Đang giao hàng" ${item.status == 'Đang giao hàng' ? 'selected' : ''}>🚚 Đang giao hàng</option>
                                                <option value="Đã giao" ${item.status == 'Đã giao' ? 'selected' : ''}>✅ Đã giao</option>
                                                <option value="Đã hủy" ${item.status == 'Đã hủy' ? 'selected' : ''}>❌ Đã hủy</option>
                                            </select>
                                        </form>
                                    </td>
                                    
                                    <td>
                                        <div class="d-flex justify-content-center gap-2">
                                            <a href="${context}/admin/OrderDetailManagerServlet?action=List&orderId=${item.id}" 
                                               class="btn-action btn-view" title="Xem chi tiết">
                                                👁️
                                            </a>
                                            
                                            <a href="${context}/admin/OrdersManagerServlet?action=Delete&id=${item.id}" 
                                               class="btn-action btn-delete" 
                                               onclick="return confirm('⚠️ Cảnh báo: Bạn có chắc chắn muốn xoá đơn hàng #${item.id} không? Hành động này không thể hoàn tác!')"
                                               title="Xóa đơn hàng">
                                                🗑️
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8" class="text-center py-5 text-muted">
                                    <div style="font-size: 40px; margin-bottom: 10px;">📭</div>
                                    Không tìm thấy đơn hàng nào.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>