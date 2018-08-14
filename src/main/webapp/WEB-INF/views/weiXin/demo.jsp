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
            window.location.href = "https://mp.weixin.qq.com/cgi-bin/componentloginpage?component_appid=wx3465dea1e67a3131&pre_auth_code=preauthcode@@@P78tjr49QX202y4RHd3tX36ijm1AzI6L_4iHYW9ihWHHXwmQHPiCd8UvlgneJmtD&redirect_uri=http://check-local.smartpos.top&auth_type=3";
        }
    </script>
</head>
<body>
<button onclick="login();">登录</button>
</body>
</html>
