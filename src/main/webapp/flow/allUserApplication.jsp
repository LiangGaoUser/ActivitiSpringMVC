<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page pageEncoding="UTF-8" %>
<html>
<head>
    <title>待我审批</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <script language="javascript" src="../script/jquery.js"></script>
    <script language="javascript" src="../script/pageCommon.js" charset="utf-8"></script>
    <script language="javascript" src="../script/PageUtils.js" charset="utf-8"></script>
    <link type="text/css" rel="stylesheet" href="../style/blue/pageCommon.css"/>
    <script type="text/javascript">
    </script>
</head>

<body>
<table cellspacing="0" cellpadding="0" class="TableStyle">
    <!-- 表头-->
    <thead>
    <tr align="CENTER" valign="MIDDLE" id="TableTitle">
        <td width="115">危险作业id</td>
        <td width="115">危险作业名称</td>
        <td width="115">危险作业申请人id</td>
        <td width="115">开始时间</td>
        <td width="115">结束时间</td>
        <td width="115">文件名称</td>
        <td width="115">状态</td>
    </tr>
    </thead>
    <!--显示数据列表-->
    <tbody id="TableData" class="dataContainer" datakey="formList">
    <c:if test="${userApplicationList != null}">
        <c:forEach items="${userApplicationList}" var="item">
            <tr class="TableDetail1 template">
                <td><a href="approveUI.html">${item.dangerTaskId}</a></td>
                <td>${item.dangerTaskName}&nbsp;</td>
                <td>${item.applicant}&nbsp;</td>
                <td>${item.startTime}&nbsp;</td>
                <td>${item.endTime}&nbsp;</td>
                <td>${item.fileName}&nbsp;</td>
                <td>${item.state}&nbsp;</td>
            </tr>
        </c:forEach>
    </c:if>
    <h2>我的申请列表</h2>
    <Button><a href="../home/main.jsp">返回主页面</a></Button>

    </tbody>
</table>
<%--<table>
    <tr>table</tr>
    <c:forEach items="${list}" var="item">
        <tr >
            <td><a href="approveUI.html">${item.fileName}</a></td>
        </tr>
    </c:forEach>
</table>--%>
</body>

</html>

