<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*" %>
<%@ page import="java.text.NumberFormat" %>
<%
    Double totalRevenueObj = (Double) request.getAttribute("totalRevenue");
    Integer totalOrdersObj = (Integer) request.getAttribute("totalOrders");
    Integer totalUsersObj = (Integer) request.getAttribute("totalUsers");

    double totalRevenue = totalRevenueObj != null ? totalRevenueObj.doubleValue() : 0.0;
    int totalOrders = totalOrdersObj != null ? totalOrdersObj.intValue() : 0;
    int totalUsers = totalUsersObj != null ? totalUsersObj.intValue() : 0;

    List<Map<String, Object>> topProducts = (List<Map<String, Object>>) request.getAttribute("topSellingProducts");
    NumberFormat nf = NumberFormat.getInstance();
%>

<!DOCTYPE html>
<html>
<head>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <meta charset="UTF-8">
    <title>Thống kê</title>

    <style>
        body {
            font-family: 'Inter', sans-serif;
            margin: 30px;
            background-color: #eef2f7;
            color: #1e293b;
        }
        .dashboard-title {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 25px;
            color: #0f172a;
        }

        /* Cards row */
        .stat-row {
            display: flex;
            gap: 25px;
            margin-bottom: 40px;
        }
        .stat-card {
            flex: 1;
            background: #ffffff;
            padding: 30px;
            border-radius: 16px;
            box-shadow: 0 4px 18px rgba(0,0,0,0.06);
            text-align: center;
            transition: .2s;
        }
        .stat-card:hover{
            transform: translateY(-5px);
        }
        .stat-value {
            font-size: 30px;
            font-weight: 700;
            margin-bottom: 6px;
            color: #0f172a;
        }
        .stat-label {
            font-size: 14px;
            color: #6b7280;
        }

        /* Table */
        .section-title {
            margin: 25px 0 15px;
            font-size: 22px;
            font-weight: 600;
        }
        .table-wrap {
            background: white;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 18px rgba(0,0,0,0.05);
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th, td {
            padding: 14px;
            border-bottom: 1px solid #e5e7eb;
        }
        th {
            background-color: #f8fafc;
            text-align: left;
            color: #334155;
            font-weight: 600;
        }
        tr:hover td {
            background-color: #f1f5f9;
        }

        /* Chart Box */
        .chart-card {
            margin-top: 35px;
            background: #fff;
            padding: 25px;
            border-radius: 16px;
            box-shadow: 0 4px 18px rgba(0,0,0,0.05);
        }
    </style>
</head>
<body>

    <h1 class="dashboard-title">📊 Dashboard thống kê</h1>

    <!-- Stats Row -->
    <div class="stat-row">
        <div class="stat-card">
            <div class="stat-value"><%= nf.format(totalRevenue) %> đ</div>
            <div class="stat-label">Tổng doanh thu</div>
        </div>
        <div class="stat-card">
            <div class="stat-value"><%= totalOrders %></div>
            <div class="stat-label">Tổng đơn hàng</div>
        </div>
        <div class="stat-card">
            <div class="stat-value"><%= totalUsers %></div>
            <div class="stat-label">Tổng khách hàng</div>
        </div>
    </div>

    <!-- Top Selling Table -->
    <h2 class="section-title">🔥 Top sản phẩm bán chạy</h2>
    <div class="table-wrap">
        <table>
            <tr>
                <th>#</th>
                <th>Tên sản phẩm</th>
                <th>Số lượng đã bán</th>
            </tr>
            <%
                int index = 1;
                if (topProducts != null) {
                    for (Map<String, Object> p : topProducts) {
            %>
            <tr>
                <td><%= index++ %></td>
                <td><%= p.get("name") %></td>
                <td><%= p.get("total_sold") %></td>
            </tr>
            <%
                    }
                }
            %>
        </table>
    </div>

    <!-- Chart -->
    <div class="chart-card">
        <h2 class="section-title">📈 Biểu đồ sản phẩm bán chạy</h2>
        <canvas id="topSellingChart" height="120"></canvas>
    </div>

<script>
    const productLabels = [];
    const productQuantities = [];

    <% if (topProducts != null) {
           for (Map<String, Object> p : topProducts) { %>
        productLabels.push("<%= p.get("name").toString().replace("\"", "\\\"") %>");
        productQuantities.push(<%= p.get("total_sold") %>);
    <% } } %>

    new Chart(document.getElementById('topSellingChart'), {
        type: 'bar',
        data: {
            labels: productLabels,
            datasets: [{
                label: 'Số lượng đã bán',
                data: productQuantities,
                backgroundColor: 'rgba(59, 130, 246, 0.6)',
                borderColor: 'rgba(37, 99, 235, 1)',
                borderWidth: 1,
                borderRadius: 6
            }]
        },
        options: { responsive: true, scales: { y: { beginAtZero: true } } }
    });
</script>

</body>
</html>
