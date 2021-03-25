<%@ page pageEncoding="UTF-8" %>
<HTML>
<HEAD>
    <META http-equiv=Content-Type CONTENT="text/html; charset=gbk" />
    <TITLE>Itcast OA</TITLE>
    <LINK HREF="/ActivitiWeb_war/style/blue/login.css" type=text/css rel=stylesheet />
</HEAD>

<BODY LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0 CLASS=PageBody >
<form action="/ActivitiSpringMVC_war/DangerTaskApplyMan/AddApproval.do" method="post">
    <tr>
        实例ID：<input type="text" name="InstanceId", value="${InstanceId}">
    </tr>
    <tr>
        添加的申请人：<input type="text" name="approvelist">
    </tr>
    <input type="submit" value="提交">
</form>
</BODY>

</HTML>

