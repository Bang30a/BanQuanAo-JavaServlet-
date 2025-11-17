<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="entity.OrderDetails"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chi tiết đơn hàng</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

    <style>
        body {
            background-color: #f1f5f9;
            font-family: "Segoe UI", sans-serif;
        }
        .container-box {
            background: #ffffff;
            padding: 32px;
            border-radius: 16px;
            margin-top: 48px;
            box-shadow: 0 6px 25px rgba(0,0,0,0.08);
            animation: fadeIn .4s ease;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        h2 {
            font-weight: 700;
            color: #1e293b;
        }
        .btn-back {
            background: linear-gradient(135deg, #64748b, #475569);
            border: none;
            padding: 8px 18px;
            border-radius: 8px;
            color: white;
            text-decoration: none;
            transition: 0.25s;
        }
        .btn-back:hover {
            opacity: 0.85;
        }
        thead {
            background: linear-gradient(135deg, #334155, #1e293b) !important;
            color: #fff;
        }
    </style>

</head>
<body>

<div class="container container-box">

    <h2 class="mb-4 text-center">
        Chi tiết đơn hàng #<%= request.getAttribute("ORDER_ID") %>
    </h2>

    <div class="table-responsive">
        <table class="table table-hover table-bordered align-middle text-center">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Product Variant ID</th>
                    <th>Tên sản phẩm</th>
                    <th>Số lượng</th>
                    <th>Giá (VNĐ)</th>
                </tr>
            </thead>
            <tbody>
            <%
                List<OrderDetails> list = (List<OrderDetails>) request.getAttribute("DETAILS");
                if (list != null && !list.isEmpty()) {
                    for (OrderDetails od : list) {
            %>
                <tr>
                    <td><%= od.getId() %></td>
                    <td><%= od.getProductVariantId() %></td>
                    <td class="text-start"><%= od.getProductName() != null ? od.getProductName() : "Không rõ" %></td>
                    <td><%= od.getQuantity() %></td>
                    <td class="text-end"><%= String.format("%,.0f", od.getPrice()) %></td>
                </tr>
            <%
                    }
                } else {
            %>
                <tr>
                    <td colspan="5" class="text-center text-muted py-3">Không có chi tiết đơn hàng nào.</td>
                </tr>
            <%
                }
            %>
            </tbody>
        </table>
    </div>

    <div class="text-center mt-4">
        <a href="<%=request.getContextPath()%>/admin/OrdersManagerServlet" class="btn-back">
            ← Quay lại danh sách
        </a>
    </div>

</div>

</body>
</html>
