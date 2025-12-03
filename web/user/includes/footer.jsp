<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!-- 
    LƯU Ý: 
    Để website tải nhanh hơn, bạn nên cắt toàn bộ đoạn <style> bên dưới 
    và dán vào file: user/assets/css/style.css 
-->
<style>
    .site-footer {
        background-color: #f8f9fa; /* Màu nền sáng nhẹ */
        color: #4b5563; /* Màu chữ xám đậm dễ đọc */
        padding: 60px 0 30px;
        font-family: 'Segoe UI', sans-serif;
        border-top: 1px solid #e5e7eb;
        font-size: 14px;
        line-height: 1.6;
    }

    .footer-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 20px;
    }

    .footer-top {
        display: flex;
        flex-wrap: wrap; /* Cho phép xuống dòng trên mobile */
        justify-content: space-between;
        gap: 30px;
    }

    /* Cột Footer */
    .footer-column {
        flex: 1;
        min-width: 220px; /* Đảm bảo không bị co quá nhỏ */
        margin-bottom: 20px;
    }

    .footer-column h3 {
        font-size: 16px;
        font-weight: 700;
        color: #111827; /* Màu tiêu đề đậm */
        margin-bottom: 20px;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }

    .footer-column ul {
        list-style: none;
        padding: 0;
        margin: 0;
    }

    .footer-column ul li {
        margin-bottom: 10px;
    }

    .footer-column a {
        text-decoration: none;
        color: #4b5563;
        transition: color 0.2s;
    }

    .footer-column a:hover {
        color: #2563eb; /* Màu xanh khi hover */
        padding-left: 5px; /* Hiệu ứng dịch chuyển nhẹ */
    }

    /* Social Icons */
    .social-icons {
        margin-top: 20px;
    }
    .social-icons a {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 36px;
        height: 36px;
        background: #e5e7eb;
        color: #374151;
        border-radius: 50%;
        margin-right: 10px;
        font-size: 16px;
        transition: 0.3s;
    }
    .social-icons a:hover {
        background: #2563eb;
        color: #fff;
        padding-left: 0; /* Tắt hiệu ứng padding của thẻ a thường */
        transform: translateY(-3px);
    }

    /* Contact Info Icons */
    .contact-info p {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        margin-bottom: 12px;
    }
    .contact-info i {
        color: #2563eb;
        margin-top: 4px;
    }

    /* Iframe Fanpage Responsive */
    .fanpage iframe {
        max-width: 100%;
        border-radius: 8px;
        box-shadow: 0 4px 6px rgba(0,0,0,0.05);
    }

    .fanpage-group-box {
        margin-top: 15px;
        font-weight: 600;
    }
    .fanpage-link {
        color: #2563eb;
        text-decoration: none;
    }
    .fanpage-link:hover { text-decoration: underline; }

    /* Footer Bottom */
    .footer-bottom {
        border-top: 1px solid #e5e7eb;
        margin-top: 40px;
        padding-top: 25px;
        text-align: center;
        font-size: 13px;
        color: #9ca3af;
    }

    /* Mobile Responsive */
    @media (max-width: 768px) {
        .footer-top {
            flex-direction: column; /* Xếp dọc trên điện thoại */
        }
        .footer-column {
            width: 100%;
            border-bottom: 1px solid #f3f4f6;
            padding-bottom: 20px;
        }
        .footer-column:last-child {
            border-bottom: none;
        }
    }
</style>

<!-- Đảm bảo đã nhúng FontAwesome ở Header. Nếu chưa có thì mở comment dưới đây ra -->
<!-- <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"> -->

<footer class="site-footer">
    <div class="footer-container">
        <div class="footer-top">
            
            <!-- Cột 1: Liên hệ -->
            <div class="footer-column contact-info">
                <h3>Về Chúng Tôi</h3>
                <p><i class="fas fa-map-marker-alt"></i> Berlin & Herlin, TT. Thanh Sơn, Đông Anh, Việt Nam</p>
                <p><i class="fas fa-phone-alt"></i> 0999xxxy</p>
                <p><i class="fas fa-envelope"></i> notareal@gmail.com</p>

                <div class="social-icons">
                    <a href="#" title="Facebook"><i class="fab fa-facebook-f"></i></a>
                    <a href="#" title="Instagram"><i class="fab fa-instagram"></i></a>
                    <a href="#" title="TikTok"><i class="fab fa-tiktok"></i></a>
                    <a href="#" title="Youtube"><i class="fab fa-youtube"></i></a>
                </div>
            </div>

            <!-- Cột 2: Hỗ trợ -->
            <div class="footer-column">
                <h3>Hỗ Trợ Khách Hàng</h3>
                <ul>
                    <li><a href="#">Hướng dẫn mua hàng</a></li>
                    <li><a href="#">Phương thức thanh toán</a></li>
                    <li><a href="#">Tra cứu đơn hàng</a></li>
                    <li><a href="#">Góp ý, khiếu nại</a></li>
                </ul>
            </div>

            <!-- Cột 3: Chính sách -->
            <div class="footer-column">
                <h3>Chính Sách Chung</h3>
                <ul>
                    <li><a href="#">Chính sách vận chuyển</a></li>
                    <li><a href="#">Chính sách bảo hành</a></li>
                    <li><a href="#">Đổi trả và hoàn tiền</a></li>
                    <li><a href="#">Bảo mật thông tin</a></li>
                </ul>
            </div>

            <!-- Cột 4: Fanpage -->
            <div class="footer-column">
                <h3>Kết Nối Facebook</h3>
                <div class="fanpage">
                    <iframe 
                        src="https://www.facebook.com/plugins/page.php?href=https://www.facebook.com/groups/717489453756639&tabs&width=280&height=130&small_header=false&adapt_container_width=true&hide_cover=false&show_facepile=false&appId" 
                        width="280" height="130" style="border:none;overflow:hidden" scrolling="no" frameborder="0" allowfullscreen="true" allow="autoplay; clipboard-write; encrypted-media; picture-in-picture; web-share">
                    </iframe>
                    
                    <div class="fanpage-group-box">
                        <span style="font-weight:normal; font-size: 13px;">Hoặc tham gia nhóm:</span><br>
                        <a href="https://www.facebook.com/groups/717489453756639" target="_blank" class="fanpage-link">
                            <i class="fab fa-facebook-square"></i> Group Cộng Đồng
                        </a>
                    </div>
                </div>
            </div>
            
        </div>

        <div class="footer-bottom">
            <p>
                © 2025 <strong>Công ty TNHH MAIU1MINHIEM</strong>.<br>
                Địa chỉ: Phía Bắc Miền Trung - Miền Bắc<br>
                Người đại diện: Phan Tứ Trường
            </p>
        </div>
    </div>
</footer>