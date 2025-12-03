<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- [CODE MỚI THÊM] Kiểm tra nếu chưa có dữ liệu doanh thu (tức là chưa chạy qua Servlet) -->
<!-- Redirect về đúng đường dẫn /admin/StatisticsServlet -->
<c:if test="${empty totalRevenue}">
    <c:redirect url="/admin/StatisticsServlet" />
</c:if>
<!-- [HẾT PHẦN CODE MỚI] -->

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thống kê</title>
    <!-- ChartJS CDN -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <!-- Font -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap" rel="stylesheet">

    <style>
        body {
            font-family: 'Inter', sans-serif;
            margin: 30px;
            background-color: #f8fafc;
            color: #1e293b;
        }
        .dashboard-title {
            font-size: 24px;
            font-weight: 700;
            margin-bottom: 25px;
            color: #0f172a;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        /* Cards row */
        .stat-row {
            display: flex;
            gap: 20px;
            margin-bottom: 30px;
            flex-wrap: wrap; 
        }
        .stat-card {
            flex: 1;
            min-width: 200px;
            background: #ffffff;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            border: 1px solid #e2e8f0;
            transition: .2s;
        }
        .stat-card:hover {
            transform: translateY(-3px);
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
        }
        .stat-value {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 5px;
            color: #0f172a;
        }
        .stat-label {
            font-size: 14px;
            color: #64748b;
            font-weight: 500;
        }
        .text-blue { color: #2563eb; }
        .text-green { color: #16a34a; }
        .text-purple { color: #9333ea; }

        /* Table container */
        .section-wrap {
            background: white;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
            border: 1px solid #e2e8f0;
            padding: 20px;
            margin-bottom: 30px;
        }
        .section-title {
            margin: 0 0 20px;
            font-size: 18px;
            font-weight: 600;
        }

        /* Table Styling */
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
        th { background-color: #f1f5f9; color: #475569; font-weight: 600; border-radius: 6px 6px 0 0; }
        tr:last-child td { border-bottom: none; }
        tr:hover td { background-color: #f8fafc; }
        
        .col-index { width: 50px; text-align: center; }
        .col-number { text-align: right; }
    </style>
</head>
<body>

    <div class="dashboard-title">📊 Dashboard thống kê</div>

    <!-- Stats Row -->
    <div class="stat-row">
        <!-- Card 1: Doanh thu -->
        <div class="stat-card">
            <div class="stat-value text-blue">
                <fmt:formatNumber value="${totalRevenue}" type="number" groupingUsed="true"/> đ
            </div>
            <div class="stat-label">Tổng doanh thu</div>
        </div>

        <!-- Card 2: Đơn hàng -->
        <div class="stat-card">
            <div class="stat-value text-green">
                <c:out value="${totalOrders}" default="0" />
            </div>
            <div class="stat-label">Tổng đơn hàng</div>
        </div>

        <!-- Card 3: Khách hàng -->
        <div class="stat-card">
            <div class="stat-value text-purple">
                <c:out value="${totalUsers}" default="0" />
            </div>
            <div class="stat-label">Tổng khách hàng</div>
        </div>
    </div>

    <!-- Top Selling Table -->
    <div class="section-wrap">
        <h2 class="section-title">🔥 Top sản phẩm bán chạy</h2>
        
        <c:choose>
            <c:when test="${not empty topSellingProducts}">
                <table>
                    <thead>
                        <tr>
                            <th class="col-index">#</th>
                            <th>Tên sản phẩm</th>
                            <th class="col-number">Số lượng đã bán</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${topSellingProducts}" varStatus="status">
                            <tr>
                                <td class="col-index">${status.count}</td>
                                <td>${p.name}</td>
                                <td class="col-number">
                                    <strong>${p.total_sold}</strong>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <p style="text-align:center; color:#64748b; padding: 20px;">Chưa có dữ liệu sản phẩm bán chạy.</p>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Chart -->
    <div class="section-wrap">
        <h2 class="section-title">📈 Biểu đồ xu hướng</h2>
        <div style="height: 300px; position: relative;">
            <canvas id="topSellingChart"></canvas>
        </div>
    </div>

    <script>
        const productLabels = [];
        const productQuantities = [];

        // Dùng JSTL để đổ dữ liệu vào mảng JS
        <c:if test="${not empty topSellingProducts}">
            <c:forEach var="p" items="${topSellingProducts}">
                productLabels.push("${p.name}"); 
                productQuantities.push(${p.total_sold});
            </c:forEach>
        </c:if>

        const ctx = document.getElementById('topSellingChart').getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: productLabels,
                datasets: [{
                    label: 'Số lượng bán ra',
                    data: productQuantities,
                    backgroundColor: 'rgba(59, 130, 246, 0.6)',
                    borderColor: 'rgba(37, 99, 235, 1)',
                    borderWidth: 1,
                    borderRadius: 4,
                    barPercentage: 0.6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: '#f1f5f9' }
                    },
                    x: {
                        grid: { display: false }
                    }
                },
                plugins: {
                    legend: { display: false }
                }
            }
        });
    </script>

</body>
</html>