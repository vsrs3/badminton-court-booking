<%--
  Created by IntelliJ IDEA.
  User: Nguyen Minh Duc
  Date: 11/02/2026
  Time: 3:24 CH
  To change this template use File | Settings | File Templates.
--%>
<% String error = (String) request.getAttribute("error"); %>

<h3 style="color:red;"><%= error %></h3>

<a href="<%= request.getContextPath() %>/google-link">
    Chọn lại tài khoản Google
</a>