<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div>
	{{?it && it.length > 0}}
	<table class="table table-hover table-condensed" style="margin: 0px">
		<thead>
			<tr class="th-left">
				<th nowrap>判定规则</th>
				<th nowrap>步骤名称</th>
			</tr>
		</thead>
		<tbody>
			{{~it:step:sindex}}
			<tr>
				<th nowrap>
					<input name="path" type="hidden" value="{{=step.path}}">
					<textarea check-type="required" placeholder="表达式为true代表流转当前路径" data-path="{{=step.path}}" name="condition" class="form-control input-sm txtCondition" style="width: 100%">
					{{=step.condition||''}}</textarea>
				</th>
				<th nowrap>{{=step.name}}</th>
			</tr>
			{{~}}
		</tbody>
	</table>
	<div class="help-block">帮助：表达式使用javascript语法，只能返回true或false，例如：true或<c:out value="\${" />
		param1
		<c:out value="}" />
		=='ABC' &&
		<c:out value="\${" />
		param2
		<c:out value="}" />
		== 1
	</div>
	{{??}}
	<p class="form-control-static">没有后续步骤</p>
	{{?}}
</div>