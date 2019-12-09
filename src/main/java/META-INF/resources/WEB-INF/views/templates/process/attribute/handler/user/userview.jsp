<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_attribute_{{=it.uuid}}" class="form_attribute" data-uuid="{{=it.uuid}}">
	{{?it.data && it.data['value']}}
	<span class="userInfo" userid="{{=it.data['value']}}">{{=it.data['text']}}</span>
	{{?}}
</form>