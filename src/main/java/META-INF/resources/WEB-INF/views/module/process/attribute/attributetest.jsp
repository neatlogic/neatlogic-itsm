<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE HTML>
<html lang="en">
<head>
<title><c:choose>
		<c:when test="${empty flowVo }">
${tk:lang("创建流程","") }
</c:when>
		<c:otherwise>${tk:lang("编辑编排","") }：${flowVo.name }</c:otherwise>
	</c:choose></title>
<jsp:include page="/resources/res-include-new.jsp?module=process&exclude=bootstrap,skincss,tsicon&need=base,jquery,bootstrap,bootstrap-validation,tsicon,customcheckbox,ckeditor,treeselect,codemirror,slidedialog,xdot,util,title,snapmessage,form,json,quartz,tojson,d3arrange,draggable,checkselect,select,scrollbar,wdatepicker,inputselect" />

<script type="text/javascript">
	$(function(){
		
	});
</script>
<style type="text/css">
</style>
</head>
<body>
	<div>
		<select plugin-checkselect id="prop_team" data-datacubeid="1">
			<option value=""></option>
		</select>
	</div>
	<div>
		<select plugin-checkselect id="prop_user"></select>
	</div>
</body>
</html>