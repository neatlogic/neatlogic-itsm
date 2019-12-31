<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE HTML>
<html>
<head>
<title>${tk:lang("用户不存在","") }</title>
	<jsp:include page="/resources/res-include-new.jsp?need=bootstrap" />
</head>
<body>
<div style="width:370px;margin:0 auto;">
	<div style="position:absolute;top:25%">
		<div class="alert alert-warning">
			<strong>${tk:lang("提示","") }:</strong>${tk:lang("账号不存在","") }
		</div>
	</div>
</div>
</body>
</html>