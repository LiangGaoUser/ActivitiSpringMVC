<%@ page pageEncoding="UTF-8" %>
<HTML>
<HEAD>
    <META http-equiv=Content-Type CONTENT="text/html; charset=gbk" />
    <TITLE>Itcast OA</TITLE>
    <LINK HREF="/ActivitiWeb_war/style/blue/login.css" type=text/css rel=stylesheet />
</HEAD>

<BODY LEFTMARGIN=0 TOPMARGIN=0 MARGINWIDTH=0 MARGINHEIGHT=0 CLASS=PageBody >
<form action="/ActivitiSpringMVC_war/DangerTaskApplyMan/SubmitAdd.do" method="post">
    <tr>
        dangertaskname：<input type="text" name="dangertaskname">
    </tr>
    <tr>
        starttime：<input type="text" name="starttime">
    </tr>
    <tr>
        endtime：<input type="text" name="endtime">
    </tr>
    <tr>
        filename：<input type="text" name="filename">
    </tr>
    <tr>
        approvelist：<input type="text" name="approvelist">
    </tr>
    <input type="submit" value="提交">
</form>
</BODY>

</HTML>

