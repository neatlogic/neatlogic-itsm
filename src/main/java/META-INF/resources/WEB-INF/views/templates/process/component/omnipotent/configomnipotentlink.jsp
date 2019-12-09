<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="formConfigVerifyLink" class="form-horizontal">
	<div class="form-block form-block-10 ">
		<span class="block-left">${tk:lang("流转动作","") }：</span>
		<div class="block-right">
			<input type="text" class="input-large" id="txtAction" value="{{?it.text}}{{=it.text||''}}{{?}}">
			<div class="help-block">${tk:lang("帮助","") }：${tk:lang("流转动作为空，代表无条件流转；如指定了流转动作，则触发了相应动作才会流转到相应路径","") }</div>
		</div>
	</div>
</form>

