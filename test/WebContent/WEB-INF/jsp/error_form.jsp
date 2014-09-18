<%--
  Created by IntelliJ IDEA.
  User: broleg
  Date: 9/18/14
  Time: 14:09
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title></title>
</head>
<body>

<form id="error" action="/" style="display:none;">
    <%--<input type="hidden" name="code" id="error_input">--%>
</form>

    <script type="text/javascript">
        alert("${error}");
        document.getElementById('error').submit();
    </script>

</body>
</html>
