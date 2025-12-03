<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Nội dung Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        body { font-family: 'Inter', sans-serif; margin: 30px; background-color: #ffffff; color: #1e293b; }
        .dashboard-title { font-size: 24px; font-weight: 700; margin-bottom: 25px; color: #0f172a; }
        
        .stat-row { display: flex; gap: 20px; margin-bottom: 30px; }
        .stat-card { flex: 1; background: white; padding: 25px; border-radius: 12px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); }
        .stat-value { font-size: 28px; font-weight: 700; margin-bottom: 5px; }
        .stat-label { font-size: 14px; color: #64748b; font-weight: 500; }
        .text-blue { color: #2563eb; } .text-green { color: #16a34a; } .text-purple { color: #9333ea; }

        .section-wrap { background: white; border-radius: 12px; border: 1px solid #e2e8f0; padding: 20px; margin-bottom: 30px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
        .section-title { margin: 0 0 20px; font-size: 18px; font-weight: 600; }
        
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
        th { background-color: #f1f5f9; color: #475569; }
        .col-number { text-align: right; }
    </style>
</head>
<body>

    <div class="dashboard-title">🏠 Tổng quan cửa hàng</div>

    <div class="stat-row">
        <div class="stat-card">
            <div class="stat-value text-blue">
                <fmt:formatNumber value="${totalRevenue}" type="number" groupingUsed="true"/> đ
            </div>
            <div class="stat-label">Tổng doanh thu</div>
        </div>
        <div class="stat-card">
            <div class="stat-value text-green">${totalOrders}</div>
            <div class="stat-label">Tổng đơn hàng</div>
        </div>
        <div class="stat-card">
            <div class="stat-value text-purple">${totalUsers}</div>
            <div class="stat-label">Tổng khách hàng</div>
        </div>
    </div>

    <div style="display: flex; gap: 20px;">
        <div style="flex: 2; display: flex; flex-direction: column; gap: 20px;">
            <div class="section-wrap">
                <h2 class="section-title">📈 Doanh thu 7 ngày qua</h2>
                <div style="height: 300px; position: relative;">
                    <canvas id="revenueChart"></canvas>
                </div>
            </div>
            <div class="section-wrap">
                <h2 class="section-title">📦 Tỷ lệ đơn hàng</h2>
                <div style="height: 250px; position: relative; display: flex; justify-content: center;">
                    <canvas id="statusChart"></canvas>
                </div>
            </div>
        </div>

        <div style="flex: 1;">
            <div class="section-wrap" style="height: 100%;">
                <h2 class="section-title">🔥 Top bán chạy</h2>
                <c:choose>
                    <c:when test="${not empty topSellingProducts}">
                        <table>
                            <thead><tr><th>Sản phẩm</th><th class="col-number">SL</th></tr></thead>
                            <tbody>
                                <c:forEach var="p" items="${topSellingProducts}">
                                    <tr>
                                        <td>${p.name}</td>
                                        <td class="col-number"><strong>${p.total_sold}</strong></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </c:when>
                    <c:otherwise><p style="color:#64748b; text-align:center;">Chưa có dữ liệu.</p></c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <script>
        const revLabels = []; const revData = [];
        <c:forEach var="item" items="${revenueChartData}">
            revLabels.push("${item.date}");
            revData.push(${item.revenue});
        </c:forEach>

        const statusLabels = []; const statusData = [];
        <c:forEach var="item" items="${statusChartData}">
            statusLabels.push("${item.status}");
            statusData.push(${item.count});
        </c:forEach>

        new Chart(document.getElementById('revenueChart'), {
            type: 'line',
            data: {
                labels: revLabels,
                datasets: [{ label: 'Doanh thu', data: revData, borderColor: '#2563eb', backgroundColor: 'rgba(37, 99, 235, 0.1)', fill: true }]
            },
            options: { responsive: true, maintainAspectRatio: false, scales: { y: { beginAtZero: true } } }
        });

        new Chart(document.getElementById('statusChart'), {
            type: 'doughnut',
            data: {
                labels: statusLabels,
                datasets: [{ data: statusData, backgroundColor: ['#10b981', '#f59e0b', '#ef4444', '#3b82f6', '#64748b'] }]
            },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' } } }
        });
    </script>
</body>
</html>