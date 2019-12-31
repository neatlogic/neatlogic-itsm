<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" deferredSyntaxAllowedAsLiteral="true"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<script type="text/x-template" id="tmpUpRelation">
{{?it.relation && it.relation.length > 0}}
<div style="margin: 0;">
	{{~it.relation:relation:index}} {{?relation.taskCount >0}}
	<div style="text-align: left; width: 100%; height: 25px; line-height: 25px;">
		<div style="padding-left: 10px; float: left; width: 200px;">
			{{=relation.name}} [{{=relation.taskCount}}]
			<img src="/balantflow/resources/images/up.gif" style="cursor: pointer;" class="imgUpLoadRelation" rtype="{{=relation.id}}" onclick="showRelation(this);" />
		</div>
	</div>
	<div id="trRelation_{{=relation.id}}" class="trRelation_{{=relation.id}}">
		<table class="table" style="margin: 0;">
			<tr style="text-align: center;">
				<td style="vertical-align: middle; width: 200px; text-align: left">
					<span class="channelType" style="background-color:{{=it.taskVo.channelTypeColor}}">{{=it.taskVo.channelTypeName.substring(0,1)}}</span>
					<a rel="tab" href="${pageContext.request.contextPath}/task/getTaskDetail.do?taskId={{=it.taskVo.id}}" style="word-break: break-all"> [{{=!it.taskVo.seqNumber ? it.taskVo.id : it.taskVo.seqNumber}}]{{=it.taskVo.title}} </a>
				</td>
				<td id="tdRelation_{{=relation.id}}"></td>
			</tr>
		</table>
	</div>
	{{?}} {{~}}
</div>
{{??}}
<div class="well">
	<b>${tk:lang("提示","")}：</b>${tk:lang("没有关联任何请求","")}</div>
{{?}}
</script>