<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE HTML>
<html>
<head>
<title>${tk:lang("URL异常","") }</title>
<jsp:include page="/resources/res-include-new.jsp?need=base" />
<style>
	.block-main{
		height:70%;
		height: calc(100vh - 32px);
		text-align:center;
	}
	.block-main .table{
		margin-bottom:0;
		height:80%;
	}
	.error-message{
	    font-size: 18px;
	    line-height: 3;
	    color: #999;
	}
</style>
</head>
<body class="bg-grey">
	<div class="block-main">
		<table class="table noborder middle">
			<tbody>
				<tr>
					<td>
						<div>
							<img src="${pageContext.request.contextPath}/resources/images/error/405.svg">
						</div>
						<div class="error-message">${tk:lang("URL不合法","") }</div>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
</body>	
</html>