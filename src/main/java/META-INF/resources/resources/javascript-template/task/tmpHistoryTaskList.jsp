<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/x-template" id="tmpHistoryTaskList">
<div>查看最近<input type="text" value="{{=it.pagesize}}" maxlength="2" class="form-control input-sm input-mini" id="txtHistoryCount">条记录<button type="button" id="btnSearchHistory" class="btn btn-sm btn-default"><i class="icon-search"></i>搜索</button></div>
{{?it.tasklist.length > 0}}
<table class="table table-condensed table-hover" style="margin-top:5px">
<thead>
	<tr>
		<th>标题</th>
		<th>优先级</th>
		<th>状态</th>
		<th>通道</th>
		<th>提交时间</th>
		<th>完成时间</th>
	</tr>
</thead>
<tbody>
	{{~it.tasklist : task : index}}
	<tr style="text-align:center">
		<td style="text-align:left" class="tdtitleMax">
		<span class="channelType" style="background-color:{{=task.channelTypeColor}}">{{=task.channelTypeName.substring(0,1)}}</span>
		<a rel="tab" href="${pageContext.request.contextPath}/task/getTaskDetail.do?taskId={{=task.id}}">[{{=task.seqNumber}}]{{=task.title}}</a></td>
		<td>{{=task.urgencyText||"-"}}</td>
		<td><span class="task-status-{{=task.status}}">{{=task.statusText}}</span></td>
		<td>{{=task.channelName}}</td>
		<td style="font-size:12px">{{=task.createDate}}</td>
		<td style="font-size:12px">{{=task.finishDate||"-"}}</td>
	</tr>
	{{~}}
</tbody>
</table>
{{??}}
<div class="alert alert-default">
	<b>提示：</b>找不到上报历史。
</div>
{{?}}
</script>