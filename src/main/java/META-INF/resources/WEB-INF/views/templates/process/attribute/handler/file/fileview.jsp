<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_attribute_{{=it.attributeUuid}}" class="form_attribute" data-uuid="{{=it.attributeUuid}}">
	{{?it.data && it.data.length > 0}}
	<div class="clearfix">
		{{~it.data:file:index}}
		<div class="d_f" style="margin: 5px">
			<a href="${pageContext.request.contextPath}/file/getFile.do?fileId={{=file.fileid}}">{{=file.name}}</a>
		</div>
		{{~}}
	</div>
	{{?}}
</form>