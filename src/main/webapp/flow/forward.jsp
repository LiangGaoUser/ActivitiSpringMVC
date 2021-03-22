<%@ page pageEncoding="UTF-8" %>
<html>
<head>
    <meta http-equiv=Content-Type CONTENT="text/html; charset=gbk" />
    <title>Itcast OA</title>
    <link href="../style/blue/login.css" type=text/css rel=stylesheet />
    <link type="text/css" rel="stylesheet" href="../style/blue/pageCommon.css"/>
</head>

<body LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0 CLASS=PageBody >
<form action="/ActivitiSpringMVC_war/DangerTaskApplyMan/ForwardTask.do" method="post">
    <table cellpadding="0" cellspacing="0" class="mainForm">
        <tr>
            <td>实例ID</td>
            <td><input name="InstanceId" value="${InstanceId}"></td>
        </tr>
        <tr>
            <td>转发人对应的id或者姓名</td>
            <td><input name="userid"></td>
        </tr>
    </table>
    <input type="submit" value="转发">
</form>
</body>

</html>

