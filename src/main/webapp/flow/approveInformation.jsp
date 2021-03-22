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
        <td width="115">审批人</td>
        <td width="115">审批意见</td>
        <td width="115">审批完成时间</td>

    </tr>
    </thead>
    <!--显示数据列表-->
    <tbody id="TableData" class="dataContainer" datakey="formList">
    <c:if test="${applicationInstance != null}">
        <c:forEach  var="i" begin="0" end="${applicationInstance.approveList.size()-1}">
            <tr class="TableDetail1 template">
                <td>${applicationInstance.approveList.get(i).userName}&nbsp;</td>
                <c:if test="${i<applicationInstance.commentList.size()}">
                    <td>${applicationInstance.commentList.get(i)}&nbsp;</td>
                    <td>${applicationInstance.timeList.get(i)}&nbsp;</td>

                </c:if>
                    <%--                <td>${item.application.applicant}&nbsp;</td>--%>
            </tr>
        </c:forEach>
    </c:if>
    <h2>批复记录</h2>
<%--    <Button><a href="../home/main.jsp">返回主页面</a></Button>--%>
    <a href="javascript:history.go(-1);">返回上一页</a>

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

