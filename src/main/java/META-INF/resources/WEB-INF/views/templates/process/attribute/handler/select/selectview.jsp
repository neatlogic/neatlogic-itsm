<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_attribute_{{=it.attributeUuid}}" class="form_attribute" data-uuid="{{=it.attributeUuid}}">
	{{?it.data && it.data['value']}}
	<span style="margin-rigth: 10px">{{=it.data['text']}}</span>
	{{?}}
</form>