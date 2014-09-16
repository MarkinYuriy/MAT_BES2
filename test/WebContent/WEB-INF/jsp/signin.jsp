<%--
  Created by IntelliJ IDEA.
  User: broleg
  Date: 9/13/14
  Time: 12:49
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Google Sign In</title>

    <script type="text/javascript">
        (function() {
            var po = document.createElement('script'); po.type = 'text/javascript'; po.async = true;
            po.src = 'https://apis.google.com/js/client.js?onload=onLoadCallback';
            var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(po, s);
        })();
    </script>
</head>

<body>

<input type="button"  value="Login" onclick="login()" />
<input type="button"  value="Logout" onclick="logout()" />

<form id="result_form" action="login" method="post" style="display:none;">
    <input type="hidden" name="token" id="result_form_token">
</form>

<form id="contacts_form" action="contacts">
    <input type="submit" value="Get Contacts">
</form>

<div id="profile"></div>
<script type="text/javascript">

    function logout()
    {
        gapi.auth.signOut();
        location.reload();
    }

    function login()
    {
        var params = {
            'clientid' : '830872460833-bq38m67qbe2iqk60pjlab70oih7vld8v.apps.googleusercontent.com',
            'client_secret' : 'BEH6NhFjH_KFRxY7N0BspfNY',
            'cookiepolicy' : 'single_host_origin',
            'callback' : 'loginCallback',
            'accesstype' : 'offline',
            'approvalprompt' : 'force',
            'scope' : 'https://www.googleapis.com/auth/plus.login https://www.google.com/m8/feeds',
        };
        gapi.auth.signIn(params);
    }

    function loginCallback(result)
    {
//        Received Json file structure
//        {
//            "state": "",
//                "access_token": "XXXXXXXXXXXXXXXX",
//                "token_type": "Bearer",
//                "expires_in": "3600",
//                "code": "XXXXXXXX",
//                "scope": "",
//                "id_token": "XXXXXXXXXXXX",
//                "authuser": "0",
//                "num_sessions": "1",
//                "prompt": "consent",
//                "session_state": "XXXXXXXXX",
//                "client_id": "XXXXXX.apps.googleusercontent.com",
//                "g_user_cookie_policy": "single_host_origin",
//                "cookie_policy": "single_host_origin",
//                "response_type": "code token id_token gsession",
//                "issued_at": "1395298062",
//                "expires_at": "1395301662",
//                "g-oauth-window": {},
//            "status": {
//            "google_logged_in": true,
//                    "signed_in": true,
//                    "method": "PROMPT"
//        }
//        }

        if(result['status']['signed_in'])
        {

            document.getElementById('result_form_token').value=result['code'];
            document.getElementById('result_form').submit();
        }

    }

    function onLoadCallback()
    {
        gapi.client.setApiKey('AIzaSyCQQ_w-Axmjz4dNqdaXe5x0oezcnLvAtjI');
        gapi.client.load('plus', 'v1', function(){});
    }

</script>
</body>
</html>
