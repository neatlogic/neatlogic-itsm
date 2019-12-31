<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/x-template" id="tmpTransfer">
<form id="formManage" method="post" class="form-horizontal">
	<input type="hidden" value="{{=it.componentid}}" name="componentId">
	<input type="hidden" value="{{=it.taskid}}" name="taskId">
	<input type="hidden" value="{{=it.stepid}}" name="stepId">
	<div class="form-group">
		<label class="col-sm-2 control-label">处理组：</label>
		<div class="col-sm-10">
			<select name="mainteam" class="form-control input-sm" id="sltMainTeam">
			</select>
		</div>
	</div>
	<div class="form-group">
		<label class="col-sm-2 control-label">处理人：</label>
		<div class="col-sm-10">
			<select name="mainworker" class="form-control input-sm" id="sltMainWorker"></select>
		</div>
	</div>
	<c:if test="${!empty worktimeList}">
	<div class="form-group">
		<label class="col-sm-2 control-label">服务窗口：</label>
		<div class="col-sm-10">
			<select name="worktimeId" class="form-control input-sm" id="sltWorkTime">
				<option value="">请选择...</option>
				<c:forEach items="${worktimeList }" var="worktime">
					<option value="${worktime.id }">${worktime.name }</option>
				</c:forEach>
			</select>
			<b>帮助：</b>该选项非必填。
		</div>
	</div>
	</c:if>
	<div class="form-group">
		<label class="col-sm-2 control-label">原因：</label>
		<div class="col-sm-10">
			 <textarea style="width:80%;height:50px;" class="form-control input-sm" check-type="required" required-message="请输入原因" name="reason"></textarea>
		</div>
	</div>
</form>
</script>