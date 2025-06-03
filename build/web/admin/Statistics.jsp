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
            margin: 20px;
            background-color: #f8fafc;
            color: #1e293b;
        }
        .stat-box {
            display: flex;
            gap: 20px;
            margin-bottom: 30px;
        }
        .card {
            background: #ffffff;
            padding: 20px;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
            flex: 1;
            text-align: center;
        }
        .card h2 {
            margin: 0;
            font-size: 24px;
            color: #0f172a;
        }
        .card p {
            margin-top: 8px;
            color: #64748b;
            font-size: 14px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            background: #fff;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }
        th, td {
            padding: 14px 18px;
            border-bottom: 1px solid #e2e8f0;
            text-align: left;
        }
        th {
            background-color: #f1f5f9;
            color: #1e293b;
        }
    </style>
</head>
<body>

    <h1>📊 Trang thống kê</h1>

    <div class="stat-box">
        <div class="card">
            <h2><%= nf.format(totalRevenue) %> đ</h2>
            <p>Tổng doanh thu</p>
        </div>
        <div class="card">
            <h2><%= totalOrders %></h2>
            <p>Tổng đơn hàng</p>
        </div>
        <div class="card">
            <h2><%= totalUsers %></h2>
            <p>Tổng khách hàng</p>
        </div>
    </div>

    <h2>🔥 Top sản phẩm bán chạy</h2>
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
        <h2>📈 Biểu đồ sản phẩm bán chạy</h2>
        <canvas id="topSellingChart" width="600" height="300"></canvas>
<script>
    const productLabels = [];
    const productQuantities = [];

    <% if (topProducts != null) {
           for (Map<String, Object> p : topProducts) {
    %>
        productLabels.push("<%= p.get("name").toString().replace("\"", "\\\"") %>");
        productQuantities.push(<%= p.get("total_sold") %>);
    <%   }
       }
    %>

    const ctx = document.getElementById('topSellingChart').getContext('2d');
    const topSellingChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: productLabels,
            datasets: [{
                label: 'Số lượng đã bán',
                data: productQuantities,
                backgroundColor: 'rgba(54, 162, 235, 0.5)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1,
                borderRadius: 5
            }]
        },
        options: {
            plugins: {
                title: {
                    display: true,
                    text: 'Top sản phẩm bán chạy',
                    font: {
                        size: 18
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Số lượng'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Sản phẩm'
                    }
                }
            }
        }
    });
</script>

</body>
</html>
