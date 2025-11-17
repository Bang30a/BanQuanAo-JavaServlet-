<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lịch sử đơn hàng</title>
    
    <!-- HEAD CHUẨN (Dùng Bootstrap 4) -->
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/css/bootstrap.min.css">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/layout.css">
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath() %>/user/style.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.4.0/js/bootstrap.min.js"></script>
</head>
<body>
    <jsp:include page="header.jsp" />
    
    <main>
        <div class="container mt-4">
            <h2 class="mb-4">Lịch sử đơn hàng</h2>
            
            <c:choose>
                <c:when test="${not empty orderList}">
                    <div class="table-responsive">
                        <table class="table table-hover table-bordered">
                            <thead class="thead-light">
                                <tr>
                                    <th>Mã Đơn</th>
                                    <th>Ngày Đặt</th>
                                    <th>Địa chỉ</th>
                                    <th>Tổng tiền</th>
                                    <th>Trạng thái</th>
                                    <th>Chi tiết</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="order" items="${orderList}">
                                    <tr>
                                        <td>#${order.id}</td>
                                        <td>
                                            <fmt:formatDate value="${order.orderDate}" pattern="HH:mm dd/MM/yyyy" />
                                        </td>
                                        <td>${order.address}</td>
                                        <td>
                                            <fmt:formatNumber value="${order.total}" type="number" groupingUsed="true" /> VNĐ
                                        </td>
                                        <td>
                                            <span class="badge 
                                                <c:if test='${order.status == "Đã giao"}'>badge-success</c:if>
                                                <c:if test='${order.status == "Đang giao"}'>badge-info</c:if>
                                                <c:if test='${order.status == "Chờ xử lý"}'>badge-warning</c:if>
                                                <c:if test='${order.status == "Đã hủy"}'>badge-danger</c:if>
                                            ">
                                                ${order.status}
                                            </span>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/user/order-history?action=detail&id=${order.id}" class="btn btn-primary btn-sm">
                                                Xem
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-info text-center">Bạn chưa có đơn hàng nào.</div>
                </c:otherwise>
            </c:choose>
        </div>
    </main>
    
    <jsp:include page="footer.jsp" />
</body>
</html>