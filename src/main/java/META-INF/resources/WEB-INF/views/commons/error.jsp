<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true"%>
<%@ page import="org.apache.commons.logging.LogFactory"%>
<%@ page import="codedriver.framework.common.config.Config"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE html>
<html lang="en">
<head>
<title>Error Page</title>
<jsp:include page="/resources/res-include-new.jsp?exclude=tsicon,skincss,bootstrap&need=base" />
<style>

.error-main {
	min-height: calc(100vh - 100px);
	height: auto;
	padding: 10px 16px;
	border-radius: 5px;
	background: #ffffff;
	border: 1px solid #dddddd;
	-moz-box-shadow: 0 0 5px 0 rgba(0, 0, 0, 0.10);
	-webkit-box-shadow: 0 0 5px 0 rgba(0, 0, 0, 0.10);
	box-shadow: 0 0 5px 0 rgba(0, 0, 0, 0.10);
}

.error-title {
	padding: 20px;
}

.error-title img {
	width: 600px;
}

.error-title .btn-danger {
	vertical-align: bottom;
	margin-left: 20px;
}

#detail_system_error_msg {
	text-align: left;
}

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
<script language="javascript">
/* 	function showDetail() {
		$('#detail_system_error_msg').toggle();
	} */
</script>
</head>
<body class="bg-grey">
			<div class="error-main">
			<div class="error-message text-center" style="border-bottom: 1px solid #eee;">服务端错误</div><br/>
				<div class="error-detail">
					<div id="detail_system_error_msg">
						<pre class="text-error"  style="background:#fff;opacity:.8;border:0 none;padding: 0;font-size: 95%;">
					<%
						if (exception != null) {
									exception.printStackTrace(new java.io.PrintWriter(out));
								}
					%>
				</pre>
					</div>
				</div>
			</div>
		
</body>
</html>