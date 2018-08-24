<%--
  Created by IntelliJ IDEA.
  User: liuyandong
  Date: 2018-08-14
  Time: 15:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
    <script type="text/javascript">
        function login() {
            window.location.href = "${url}";
        }
    </script>
</head>
<body>
<button onclick="login();">登录</button>
</body>
</html>
