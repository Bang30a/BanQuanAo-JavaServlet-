<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Báo cáo chi tiết</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <style>
        body { font-family: 'Inter', sans-serif; margin: 30px; background-color: #f8fafc; color: #1e293b; }
        .dashboard-title { font-size: 24px; font-weight: 700; margin-bottom: 25px; color: #0f172a; }
        
        /* Form lọc */
        .filter-toolbar {
            background: white; padding: 20px; border-radius: 12px;
            border: 1px solid #e2e8f0; margin-bottom: 30px;
            display: flex; gap: 15px; align-items: end; box-shadow: 0 1px 2px rgba(0,0,0,0.05);
        }
        .form-group label { font-size: 13px; font-weight: 600; color: #64748b; display: block; margin-bottom: 5px; }
        .form-control { padding: 8px 12px; border: 1px solid #cbd5e1; border-radius: 6px; }
        .btn-filter { background: #2563eb; color: white; border: none; padding: 9px 20px; border-radius: 6px; font-weight: 600; cursor: pointer; }
        .btn-excel { background: #10b981; color: white; border: none; padding: 9px 20px; border-radius: 6px; font-weight: 600; cursor: pointer; display: inline-flex; align-items: center; gap: 5px; }

        /* Card thống kê */
        .stat-row { display: flex; gap: 20px; margin-bottom: 30px; }
        .stat-card { flex: 1; background: white; padding: 25px; border-radius: 12px; border: 1px solid #e2e8f0; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
        .stat-value { font-size: 28px; font-weight: 700; margin-bottom: 5px; }
        .text-blue { color: #2563eb; } .text-green { color: #16a34a; }

        /* Bảng dữ liệu */
        .table-wrap { background: white; border-radius: 12px; border: 1px solid #e2e8f0; padding: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
        th { background-color: #f1f5f9; color: #475569; font-weight: 600; }
        .text-right { text-align: right; }
    </style>
</head>
<body>

    <div class="dashboard-title">📊 Báo cáo & Thống kê chi tiết</div>

    <form action="${pageContext.request.contextPath}/admin/statistics" method="GET" class="filter-toolbar">
        <div class="form-group">
            <label>Từ ngày:</label>
            <input type="date" name="startDate" value="${start}" class="form-control" required>
        </div>
        <div class="form-group">
            <label>Đến ngày:</label>
            <input type="date" name="endDate" value="${end}" class="form-control" required>
        </div>
        <button type="submit" class="btn-filter"><i class="bi bi-funnel"></i> Xem kết quả</button>
        
        <button type="submit" formaction="${pageContext.request.contextPath}/admin/export-excel" class="btn-excel">
            <i class="bi bi-file-earmark-excel"></i> Xuất file Excel
        </button>
    </form>

    <div class="stat-row">
        <div class="stat-card">
            <div class="stat-value text-blue">
                <fmt:formatNumber value="${revenue}" type="number" groupingUsed="true"/> đ
            </div>
            <div style="font-size: 14px; color: #64748b;">Doanh thu trong khoảng đã chọn</div>
        </div>
        <div class="stat-card">
            <div class="stat-value text-green">${orders}</div>
            <div style="font-size: 14px; color: #64748b;">Đơn hàng trong khoảng đã chọn</div>
        </div>
    </div>

    <div class="table-wrap">
        <h3 style="margin-top:0; font-size: 18px; color: #0f172a;">📋 Chi tiết sản phẩm bán ra (${start} đến ${end})</h3>
        
        <c:choose>
            <c:when test="${not empty reportData}">
                <table>
                    <thead>
                        <tr>
                            <th width="50">#</th>
                            <th>Tên sản phẩm</th>
                            <th class="text-right">Số lượng bán</th>
                            <th class="text-right">Doanh thu đem lại</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="row" items="${reportData}" varStatus="status">
                            <tr>
                                <td>${status.count}</td>
                                <td>${row.name}</td>
                                <td class="text-right"><strong>${row.total_sold}</strong></td>
                                <td class="text-right text-blue">
                                    <fmt:formatNumber value="${row.revenue}" type="number"/> đ
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <p style="text-align:center; padding: 30px; color: #64748b;">Không có dữ liệu trong khoảng thời gian này.</p>
            </c:otherwise>
        </c:choose>
    </div>

</body>
</html>