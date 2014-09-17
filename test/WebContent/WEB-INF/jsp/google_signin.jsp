<%--
  Created by Oleg Braginsky on 13/09/14
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
    <input type="hidden" name="code" id="result_form_code">
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

    function login() {
        var params = {
            'clientid': "${id}",
            'client_secret': "${secret}",
            'cookiepolicy': 'single_host_origin',
            'callback': 'loginCallback',
            'accesstype': 'offline',
            'approvalprompt': 'force',
            'scope': "${scopes}"
        };
        gapi.auth.signIn(params);
    }

    function loginCallback(result)
    {
//        Received Json file structure example
//        {
//            "state": "",
//                "access_token": "XXXXXXXXXXXXXXXX",
//                "token_type": "Bearer",
//                "refresh_token": "XXXXXXXXXXXXXXXX",
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

            document.getElementById('result_form_code').value=result['code'];
            document.getElementById('result_form').submit();
        }

    }

    function onLoadCallback()
    {
//        gapi.client.setApiKey('AIzaSyCQQ_w-Axmjz4dNqdaXe5x0oezcnLvAtjI');
//        gapi.client.load('plus', 'v1', function(){});
    }

</script>
</body>
</html>
