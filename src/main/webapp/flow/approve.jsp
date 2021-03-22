<%@ page pageEncoding="UTF-8" %>
<html>
<head>
    <meta http-equiv=Content-Type CONTENT="text/html; charset=gbk" />
    <title>Itcast OA</title>
    <link href="../style/blue/login.css" type=text/css rel=stylesheet />
    <link type="text/css" rel="stylesheet" href="../style/blue/pageCommon.css"/>
</head>

<body LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0 CLASS=PageBody >
<form action="/ActivitiSpringMVC_war/DangerTaskApplyMan/FinishOneTask.do" method="post">
    <table cellpadding="0" cellspacing="0" class="mainForm">
        <tr>
            <td>实例ID</td>
            <td><input name="InstanceId" value="${InstanceId}"></td>
        </tr>
        <tr>
            <td>审批意见</td>
            <td><textarea name="Comment" class="TextareaStyle" style="width: 100px;"></textarea></td>
        </tr>
        <tr>
            <td>是否同意:输入yes或者no</td>
            <td><textarea name="Approve" class="TextareaStyle" style="width: 100px;"></textarea></td>
        </tr>
    </table>
    <input type="submit" value="提交">
</form>
</body>

</html>

