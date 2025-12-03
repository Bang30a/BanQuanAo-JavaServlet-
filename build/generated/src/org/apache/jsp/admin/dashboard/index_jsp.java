package org.apache.jsp.admin.dashboard;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class index_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

  private static java.util.List<String> _jspx_dependants;

  private org.apache.jasper.runtime.TagHandlerPool _jspx_tagPool_c_set_var_value_nobody;

  private org.glassfish.jsp.api.ResourceInjector _jspx_resourceInjector;

  public java.util.List<String> getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _jspx_tagPool_c_set_var_value_nobody = org.apache.jasper.runtime.TagHandlerPool.getTagHandlerPool(getServletConfig());
  }

  public void _jspDestroy() {
    _jspx_tagPool_c_set_var_value_nobody.release();
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
      out.write("<!DOCTYPE html>\n");
      out.write("<html>\n");
      out.write("<head>\n");
      out.write("    <meta charset=\"UTF-8\">\n");
      out.write("    <title>Admin Dashboard</title>\n");
      out.write("    <link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;600&display=swap\" rel=\"stylesheet\">\n");
      out.write("    <style>\n");
      out.write("        * { box-sizing: border-box; }\n");
      out.write("        body { margin: 0; font-family: 'Inter', sans-serif; background-color: #f1f5f9; color: #333; display: flex; height: 100vh; overflow: hidden; }\n");
      out.write("        \n");
      out.write("        /* ===== SIDEBAR (THANH MENU) ===== */\n");
      out.write("        .sidebar {\n");
      out.write("            width: 260px;\n");
      out.write("            background: #1e293b;\n");
      out.write("            height: 100%;\n");
      out.write("            display: flex;\n");
      out.write("            flex-direction: column;\n");
      out.write("            padding-top: 25px;\n");
      out.write("            color: white;\n");
      out.write("            box-shadow: 4px 0 12px rgba(0,0,0,0.15);\n");
      out.write("            flex-shrink: 0;\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        .sidebar h3 { text-align: center; margin-bottom: 25px; font-size: 21px; font-weight: 600; color: #e2e8f0; }\n");
      out.write("\n");
      out.write("        .view-site-link {\n");
      out.write("            display: block; margin: 0 28px 18px; padding: 10px;\n");
      out.write("            background: #0284c7; color: white; text-decoration: none;\n");
      out.write("            text-align: center; border-radius: 8px; font-weight: 600; transition: 0.2s;\n");
      out.write("        }\n");
      out.write("        .view-site-link:hover { background: #0369a1; }\n");
      out.write("        .sidebar hr { border: none; border-top: 1px solid #334155; margin: 18px 28px; }\n");
      out.write("\n");
      out.write("        /* Menu items */\n");
      out.write("        .sidebar a.admin-link, .sidebar a.has-submenu {\n");
      out.write("            display: block; padding: 14px 30px; color: #cbd5e1;\n");
      out.write("            text-decoration: none; font-size: 15px; transition: 0.2s;\n");
      out.write("            border-left: 4px solid transparent; cursor: pointer;\n");
      out.write("        }\n");
      out.write("        .sidebar a:hover, .sidebar a.active-link {\n");
      out.write("            background: #334155; color: white; border-left: 4px solid #0ea5e9;\n");
      out.write("        }\n");
      out.write("\n");
      out.write("        /* Submenu */\n");
      out.write("        .submenu { background: #0f172a; display: none; }\n");
      out.write("        .submenu a { display: block; padding: 12px 0 12px 50px; font-size: 14px; color: #94a3b8; text-decoration: none; transition: 0.2s; }\n");
      out.write("        .submenu a:hover { color: #fff; background: #334155; }\n");
      out.write("\n");
      out.write("        .has-submenu::after { content: '▾'; float: right; font-size: 12px; }\n");
      out.write("        .has-submenu.active::after { content: '▴'; }\n");
      out.write("\n");
      out.write("        /* ===== MAIN CONTENT (KHUNG CHỨA) ===== */\n");
      out.write("        .main-content { flex-grow: 1; height: 100%; transition: 0.3s; }\n");
      out.write("        iframe { width: 100%; height: 100%; border: none; background: white; }\n");
      out.write("\n");
      out.write("        /* Logout */\n");
      out.write("        .logout { margin-top: auto; margin-bottom: 25px; }\n");
      out.write("        .logout a { display: block; padding: 14px 30px; color: #fca5a5; font-weight: 600; text-decoration: none; }\n");
      out.write("        .logout a:hover { color: #ef4444; background-color: #334155; }\n");
      out.write("    </style>\n");
      out.write("</head>\n");
      out.write("<body>\n");
      out.write("\n");
      out.write("    ");
      if (_jspx_meth_c_set_0(_jspx_page_context))
        return;
      out.write("\n");
      out.write("\n");
      out.write("    <div class=\"sidebar\">\n");
      out.write("        <h3>Admin Panel</h3>\n");
      out.write("        \n");
      out.write("        <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/user/view-products\" target=\"_blank\" class=\"view-site-link\">\n");
      out.write("            🌐 Xem trang User\n");
      out.write("        </a>\n");
      out.write("        <hr class=\"separator\">\n");
      out.write("        \n");
      out.write("        <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/admin/dashboard\" target=\"mainFrame\" class=\"admin-link active-link\">\n");
      out.write("            🏠 Dashboard\n");
      out.write("        </a>\n");
      out.write("\n");
      out.write("        <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/admin/statistics\" target=\"mainFrame\" class=\"admin-link\">\n");
      out.write("            📊 Báo cáo chi tiết\n");
      out.write("        </a>\n");
      out.write("\n");
      out.write("        <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/admin/UsersManagerServlet?action=List\" target=\"mainFrame\" class=\"admin-link\">\n");
      out.write("            👤 Quản lý người dùng\n");
      out.write("        </a>\n");
      out.write("\n");
      out.write("        <a class=\"has-submenu\" onclick=\"toggleSubmenu(event)\">\n");
      out.write("            🛒 Quản lý sản phẩm\n");
      out.write("        </a>\n");
      out.write("        <div class=\"submenu\" id=\"productSubmenu\">\n");
      out.write("            <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/admin/ProductsManagerServlet?action=List\" target=\"mainFrame\">Sản phẩm</a>\n");
      out.write("            <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/admin/ProductVariantsManagerServlet?action=List\" target=\"mainFrame\">Biến thể</a>\n");
      out.write("            <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/admin/SizesManagerServlet?action=List\" target=\"mainFrame\">Kích cỡ</a>\n");
      out.write("        </div>\n");
      out.write("\n");
      out.write("        <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/admin/OrdersManagerServlet?action=List\" target=\"mainFrame\" class=\"admin-link\">\n");
      out.write("            ? Quản lý hóa đơn\n");
      out.write("        </a>\n");
      out.write("\n");
      out.write("        <div class=\"logout\">\n");
      out.write("            <a href=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/LogoutServlet\">🚪 Đăng xuất</a>\n");
      out.write("        </div>\n");
      out.write("    </div>\n");
      out.write("\n");
      out.write("    <div class=\"main-content\">\n");
      out.write("        <iframe name=\"mainFrame\" src=\"");
      out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${context}", java.lang.String.class, (PageContext)_jspx_page_context, null));
      out.write("/admin/dashboard\"></iframe>\n");
      out.write("    </div>\n");
      out.write("\n");
      out.write("    <script>\n");
      out.write("    function toggleSubmenu(e) {\n");
      out.write("        e.preventDefault();\n");
      out.write("        let link = e.target.closest('.has-submenu'); \n");
      out.write("        if (!link) return;\n");
      out.write("        link.classList.toggle('active');\n");
      out.write("        const submenu = document.getElementById('productSubmenu');\n");
      out.write("        submenu.style.display = (submenu.style.display === 'block') ? 'none' : 'block';\n");
      out.write("    }\n");
      out.write("\n");
      out.write("    const allLinks = document.querySelectorAll('.sidebar a[target=\"mainFrame\"]');\n");
      out.write("    allLinks.forEach(link => {\n");
      out.write("        link.addEventListener('click', function() {\n");
      out.write("            document.querySelectorAll('.sidebar a').forEach(l => l.classList.remove('active-link'));\n");
      out.write("            this.classList.add('active-link');\n");
      out.write("            const parentSubmenu = this.closest('.submenu');\n");
      out.write("            if (parentSubmenu) {\n");
      out.write("                const parentLink = parentSubmenu.previousElementSibling; \n");
      out.write("                if(parentLink) parentLink.classList.add('active-link');\n");
      out.write("            }\n");
      out.write("        });\n");
      out.write("    });\n");
      out.write("    </script>\n");
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
}
