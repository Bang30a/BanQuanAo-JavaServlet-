<%-- 
    Document   : footer
    Created on : Jun 2, 2025, 1:38:03 AM
    Author     : phant
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<style>
    /* CSS FOOTER ĐỘC LẬP, KHÔNG ẢNH HƯỞNG FILE KHÁC */
.layout-root .site-footer {
    background-color: #f9f9f9;
    font-family: Arial, sans-serif;
    padding: 30px 20px;
    color: #333;
    border-top: 1px solid #ddd;
    width: 100%;
}

/* Dàn footer nằm ngang đều */
.layout-root .footer-top {
    display: flex;
    flex-wrap: nowrap;         /* Không tự xuống dòng */
    justify-content: space-between;
    align-items: flex-start;
    gap: 40px;
}

/* Kích thước từng cột */
.layout-root .footer-column {
    flex: 1;
    min-width: 220px;
}

/* Text và list */
.layout-root .footer-column h3 {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 15px;
}

.layout-root .footer-column ul {
    list-style: none;
    padding: 0;
    margin: 0;
    line-height: 1.8;
    font-size: 14px;
}

/* Icon mạng xã hội */
.layout-root .social-icons a {
    display: inline-block;
    margin-right: 8px;
    font-size: 22px;
    color: #333;
    transition: 0.3s;
}

.layout-root .social-icons a:hover {
    color: #0073e6;
}

/* Fanpage */
.layout-root .fanpage iframe {
    width: 100%;
    border-radius: 6px;
}

/* Dòng cuối footer */
.layout-root .footer-bottom {
    margin-top: 25px;
    border-top: 1px solid #ccc;
    padding-top: 12px;
    font-size: 13px;
    text-align: center;
    color: #666;
}

</style>
<div class="layout-root">
    <footer class="site-footer">
        <div class="footer-top">
            <!-- Cột liên hệ -->
            <div class="footer-column contact-info">
                <p><i class="fa fa-map-marker"></i> Berlin & Herlin, TT. Thanh Sơn, Đông Anh, Việt Nam</p>
                <p><i class="fa fa-phone"></i> 0999xxxy</p>
                <p><i class="fa fa-envelope"></i> notareal@gmail.com</p>

                <div class="social-icons">
                    <a href="#"><i class="fab fa-facebook"></i></a>
                    <a href="#"><i class="fab fa-instagram"></i></a>
                    <a href="#"><i class="fab fa-tiktok"></i></a>
                    <a href="#"><i class="fa fa-envelope"></i></a>
                    <a href="#"><i class="fab fa-facebook-messenger"></i></a>
                    <a href="#"><i class="fab fa-youtube"></i></a>
                </div>
            </div>

            <!-- Cột hỗ trợ -->
            <div class="footer-column">
                <h3>HỖ TRỢ KHÁCH HÀNG</h3>
                <ul>
                    <li>Hướng dẫn mua hàng trực tuyến</li>
                    <li>Hướng dẫn thanh toán</li>
                    <li>Góp ý, khiếu nại</li>
                </ul>
            </div>

            <!-- Cột chính sách -->
            <div class="footer-column">
                <h3>CHÍNH SÁCH CHUNG</h3>
                <ul>
                    <li>Chính sách, quy định chung</li>
                    <li>Chính sách vận chuyển</li>
                    <li>Chính sách bảo hành</li>
                    <li>Chính sách đổi trả và hoàn tiền</li>
                    <li>Chính sách xử lý khiếu nại</li>
                    <li>Bảo mật thông tin khách hàng</li>
                </ul>
            </div>

            <!-- Cột fanpage -->
            <div class="footer-column">
                <h3>FANPAGE FACEBOOK</h3>
                <div class="fanpage">
                    <!-- Fanpage -->
                    <iframe
                        src="https://www.facebook.com/plugins/page.php?href=https://www.facebook.com/groups/717489453756639&tabs&width=300&height=160"
                        width="300" height="160" style="border:none;overflow:hidden" scrolling="no" frameborder="0"
                        allowfullscreen="true" allow="autoplay; clipboard-write; encrypted-media; picture-in-picture; web-share">
                    </iframe>

                    <!-- Nhóm Facebook -->
                    <div class="fanpage-group-box">
                        <p>👉 Tham gia nhóm của chúng tôi:</p>
                        <a href="https://www.facebook.com/groups/717489453756639" target="_blank"
                           class="fanpage-link">Group Facebook</a>
                    </div>
                </div>
            </div>
        </div>

        <div class="footer-bottom">
            <p>
                Công ty TNHH MAIU1MINHIEM<br>
                Địa chỉ: Phía Bắc Miền Trung - Miền Bắc<br>
                Chủ sở hữu: Phan Tứ Trường
            </p>
        </div>
    </footer>
</div>
