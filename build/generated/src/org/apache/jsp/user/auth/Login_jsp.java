package org.apache.jsp.user.auth;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class Login_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List<String> _jspx_dependants;

  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_set_var_value_nobody;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_remove_var_scope_nobody;
  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_if_test;

  private org.glassfish.jsp.api.ResourceInjector _jspx_resourceInjector;

  public java.util.List<String> getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _jspx_tagPool_c_set_var_value_nobody = org.apache.jasper.runtime.TagHandlerPool.getTagHandlerPool(getServletConfig());
    _jspx_tagPool_c_remove_var_scope_nobody = org.apache.jasper.runtime.TagHandlerPool.getTagHandlerPool(getServletConfig());
    _jspx_tagPool_c_if_test = org.apache.jasper.runtime.TagHandlerPool.getTagHandlerPool(getServletConfig());
  }

  public void _jspDestroy() {
    _jspx_tagPool_c_set_var_value_nobody.release();
    _jspx_tagPool_c_remove_var_scope_nobody.release();
    _jspx_tagPool_c_if_test.release();
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;

    try {
      response.setContentType("text/html;charset=UTF-8");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;
      _jspx_resourceInjector = (org.glassfish.jsp.api.ResourceInjector) application.getAttribute("com.sun.appserv.jsp.resource.injector");

      out.write("\n");
      out.write("\n");
      out.write("\n");
      out.write("<!DOCTYPE html>\n");
      out.write("<html lang=\"vi\">\n");
      out.write("<head>\n");
      out.write("    <meta charset=\"UTF-8\">\n");
      out.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
      out.write("    <title>Đăng nhập - ShopDuck</title>\n");
      out.write("\n");
      out.write("    <!-- 1. BOOTSTRAP 5 & FONT (Đồng bộ với toàn trang) -->\n");
      out.write("    <link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap\" rel=\"stylesheet\">\n");
      out.write("    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n");
      out.write("    <link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css\">\n");
      out.write("    \n");
      out.write("    <!-- Link CSS riêng (nếu có) -->\n");
      out.write("    <link rel=\"stylesheet\" href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${pageContext.request.contextPath}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/user/assets/css/style.css\">\n");
      out.write("\n");
      out.write("    <style>\n");
      out.write("        body {\n");
      out.write("            background-color: #f8fafc;\n");
      out.write("            font-family: 'Inter', sans-serif;\n");
      out.write("            display: flex;\n");
      out.write("            flex-direction: column;\n");
      out.write("            min-height: 100vh;\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        main {\n");
      out.write("            flex: 1; /* Đẩy footer xuống đáy */\n");
      out.write("            display: flex;\n");
      out.write("            align-items: center;\n");
      out.write("            justify-content: center;\n");
      out.write("            padding: 40px 20px;\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        .login-card {\n");
      out.write("            background: white;\n");
      out.write("            padding: 40px;\n");
      out.write("            border-radius: 16px;\n");
      out.write("            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);\n");
      out.write("            width: 100%;\n");
      out.write("            max-width: 450px;\n");
      out.write("            border: 1px solid #e2e8f0;\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        .login-title {\n");
      out.write("            font-weight: 700;\n");
      out.write("            color: #1e293b;\n");
      out.write("            text-align: center;\n");
      out.write("            margin-bottom: 10px;\n");
      out.write("        }\n");
      out.write("        \n");
      out.write("        .login-subtitle {\n");
      out.write("            text-align: center;\n");
      out.write("            color: #64748b;\n");
      out.write("            font-size: 14px;\n");
      out.write("            margin-bottom: 30px;\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        .form-label {\n");
      out.write("            font-weight: 600;\n");
      out.write("            color: #334155;\n");
      out.write("            font-size: 14px;\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        .form-control {\n");
      out.write("            border-radius: 8px;\n");
      out.write("            padding: 10px 15px;\n");
      out.write("            border: 1px solid #cbd5e1;\n");
      out.write("        }\n");
      out.write("        .form-control:focus {\n");
      out.write("            border-color: #3b82f6;\n");
      out.write("            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        .btn-login {\n");
      out.write("            background-color: #2563eb;\n");
      out.write("            color: white;\n");
      out.write("            font-weight: 600;\n");
      out.write("            padding: 12px;\n");
      out.write("            border-radius: 8px;\n");
      out.write("            width: 100%;\n");
      out.write("            border: none;\n");
      out.write("            transition: 0.2s;\n");
      out.write("        }\n");
      out.write("        .btn-login:hover {\n");
      out.write("            background-color: #1d4ed8;\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        .alert-custom {\n");
      out.write("            border-radius: 8px;\n");
      out.write("            font-size: 14px;\n");
      out.write("            display: flex;\n");
      out.write("            align-items: center;\n");
      out.write("            gap: 10px;\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        .register-link {\n");
      out.write("            text-align: center;\n");
      out.write("            margin-top: 25px;\n");
      out.write("            font-size: 14px;\n");
      out.write("            color: #64748b;\n");
      out.write("        }\n");
      out.write("        .register-link a {\n");
      out.write("            color: #2563eb;\n");
      out.write("            text-decoration: none;\n");
      out.write("            font-weight: 600;\n");
      out.write("        }\n");
      out.write("        .register-link a:hover { text-decoration: underline; }\n");
      out.write("    </style>\n");
      out.write("</head>\n");
      out.write("\n");
      out.write("<body>\n");
      out.write("    ");
      if (_jspx_meth_c_set_0(_jspx_page_context))
        return;
      out.write("\n");
      out.write("\n");
      out.write("    <!-- 2. INCLUDE HEADER -->\n");
      out.write("    ");
      org.apache.jasper.runtime.JspRuntimeLibrary.include(request, response, "../includes/header.jsp", out, false);
      out.write("\n");
      out.write("\n");
      out.write("    <main>\n");
      out.write("        <div class=\"login-card\">\n");
      out.write("            <h3 class=\"login-title\">Chào mừng trở lại!</h3>\n");
      out.write("            <p class=\"login-subtitle\">Vui lòng đăng nhập để tiếp tục</p>\n");
      out.write("\n");
      out.write("            <!-- Thông báo lỗi (JSTL) -->\n");
      out.write("            ");
      if (_jspx_meth_c_if_0(_jspx_page_context))
        return;
      out.write("\n");
      out.write("\n");
      out.write("            <form action=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/LoginServlet\" method=\"post\">\n");
      out.write("                <div class=\"mb-3\">\n");
      out.write("                    <label for=\"username\" class=\"form-label\">Tên đăng nhập</label>\n");
      out.write("                    <input type=\"text\" class=\"form-control\" id=\"username\" name=\"username\" \n");
      out.write("                           placeholder=\"Nhập username...\" \n");
      out.write("                           value=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${param.username}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("\" required>\n");
      out.write("                </div>\n");
      out.write("\n");
      out.write("                <div class=\"mb-4\">\n");
      out.write("                    <label for=\"password\" class=\"form-label\">Mật khẩu</label>\n");
      out.write("                    <input type=\"password\" class=\"form-control\" id=\"password\" name=\"password\" \n");
      out.write("                           placeholder=\"••••••••\" required>\n");
      out.write("                </div>\n");
      out.write("\n");
      out.write("                <button type=\"submit\" class=\"btn-login\">Đăng nhập</button>\n");
      out.write("            </form>\n");
      out.write("\n");
      out.write("            <div class=\"register-link\">\n");
      out.write("                Chưa có tài khoản? \n");
      out.write("                <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/user/auth/Register.jsp\">Đăng ký ngay</a>\n");
      out.write("            </div>\n");
      out.write("        </div>\n");
      out.write("    </main>\n");
      out.write("\n");
      out.write("    <!-- 3. INCLUDE FOOTER -->\n");
      out.write("    ");
      org.apache.jasper.runtime.JspRuntimeLibrary.include(request, response, "../includes/footer.jsp", out, false);
      out.write("\n");
      out.write("\n");
      out.write("    <!-- Bootstrap JS -->\n");
      out.write("    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js\"></script>\n");
      out.write("</body>\n");
      out.write("</html>");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          out.clearBuffer();
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }

  private boolean _jspx_meth_c_set_0(PageContext _jspx_page_context)
          throws Throwable {
    PageContext pageContext = _jspx_page_context;
    JspWriter out = _jspx_page_context.getOut();
    //  c:set
    org.apache.taglibs.standard.tag.rt.core.SetTag _jspx_th_c_set_0 = (org.apache.taglibs.standard.tag.rt.core.SetTag) _jspx_tagPool_c_set_var_value_nobody.get(org.apache.taglibs.standard.tag.rt.core.SetTag.class);
    _jspx_th_c_set_0.setPageContext(_jspx_page_context);
    _jspx_th_c_set_0.setParent(null);
    _jspx_th_c_set_0.setVar("context");
    _jspx_th_c_set_0.setValue((java.lang.Object) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${pageContext.request.contextPath}", java.lang.Object.class, (PageContext)_jspx_page_context, null));
    int _jspx_eval_c_set_0 = _jspx_th_c_set_0.doStartTag();
    if (_jspx_th_c_set_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
      _jspx_tagPool_c_set_var_value_nobody.reuse(_jspx_th_c_set_0);
      return true;
    }
    _jspx_tagPool_c_set_var_value_nobody.reuse(_jspx_th_c_set_0);
    return false;
  }

  private boolean _jspx_meth_c_if_0(PageContext _jspx_page_context)
          throws Throwable {
    PageContext pageContext = _jspx_page_context;
    JspWriter out = _jspx_page_context.getOut();
    //  c:if
    org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_if_0 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _jspx_tagPool_c_if_test.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
    _jspx_th_c_if_0.setPageContext(_jspx_page_context);
    _jspx_th_c_if_0.setParent(null);
    _jspx_th_c_if_0.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${not empty sessionScope.loginError}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null)).booleanValue());
    int _jspx_eval_c_if_0 = _jspx_th_c_if_0.doStartTag();
    if (_jspx_eval_c_if_0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
      do {
        out.write("\n");
        out.write("                <div class=\"alert alert-danger alert-custom mb-4\" role=\"alert\">\n");
        out.write("                    <i class=\"bi bi-exclamation-triangle-fill\"></i>\n");
        out.write("                    ");
        out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${sessionScope.loginError}", java.lang.String.class, (PageContext)_jspx_page_context, null));
        out.write("\n");
        out.write("                </div>\n");
        out.write("                <!-- Xóa session error sau khi hiện (Cần xử lý trong Servlet, hoặc dùng JSTL remove nếu server hỗ trợ) -->\n");
        out.write("                ");
        if (_jspx_meth_c_remove_0((javax.servlet.jsp.tagext.JspTag) _jspx_th_c_if_0, _jspx_page_context))
          return true;
        out.write("\n");
        out.write("            ");
        int evalDoAfterBody = _jspx_th_c_if_0.doAfterBody();
        if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
          break;
      } while (true);
    }
    if (_jspx_th_c_if_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
      _jspx_tagPool_c_if_test.reuse(_jspx_th_c_if_0);
      return true;
    }
    _jspx_tagPool_c_if_test.reuse(_jspx_th_c_if_0);
    return false;
  }

  private boolean _jspx_meth_c_remove_0(javax.servlet.jsp.tagext.JspTag _jspx_th_c_if_0, PageContext _jspx_page_context)
          throws Throwable {
    PageContext pageContext = _jspx_page_context;
    JspWriter out = _jspx_page_context.getOut();
    //  c:remove
    org.apache.taglibs.standard.tag.common.core.RemoveTag _jspx_th_c_remove_0 = (org.apache.taglibs.standard.tag.common.core.RemoveTag) _jspx_tagPool_c_remove_var_scope_nobody.get(org.apache.taglibs.standard.tag.common.core.RemoveTag.class);
    _jspx_th_c_remove_0.setPageContext(_jspx_page_context);
    _jspx_th_c_remove_0.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_if_0);
    _jspx_th_c_remove_0.setVar("loginError");
    _jspx_th_c_remove_0.setScope("session");
    int _jspx_eval_c_remove_0 = _jspx_th_c_remove_0.doStartTag();
    if (_jspx_th_c_remove_0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
      _jspx_tagPool_c_remove_var_scope_nobody.reuse(_jspx_th_c_remove_0);
      return true;
    }
    _jspx_tagPool_c_remove_var_scope_nobody.reuse(_jspx_th_c_remove_0);
    return false;
  }
}
