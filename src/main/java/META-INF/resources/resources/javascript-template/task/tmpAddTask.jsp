<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/x-template" id="tmpAddTask">
<form class="form-horizontal" role="form" method="post" id="formAddTask" style="width:99%;">
	<div class="form-group" >
		<label class="col-sm-2 control-label">${tk:lang("标题","")}：</label>
	    <div class="col-sm-10">
	      <input type="text" class="form-control input-sm" id="txtTitle" name="title" check-type="required" required-message="${tk:lang("请输入标题","")}">
		  <input type="hidden" name="stepId" value="${param.stepId}">
 		  <input type="hidden" name="taskId" value="${param.taskId}">
	    </div>
	</div>
	<div class="form-group" >
		<label class="col-sm-2 control-label">${tk:lang("完成时间","")}：</label>
	    <div class="col-sm-10">
	      <input class="Wdate form-control input-sm notvalid input-medium" name="targetDate" check-type="required" required-message="${tk:lang("请选择期望完成时间","")}"
		    onfocus="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm'})"  >
	    </div>
	</div>
	<div class="form-group">
		<label class="col-sm-2 control-label">${tk:lang("处理组","")}：</label>
		<div class="col-sm-10">
			<select name="teamId" class="mustinput form-control input-sm" id="sltHelpTeam" check-type="required" required-message="${tk:lang("请选择处理组","")}">
			</select>
		</div>
	</div>
	<div class="form-group" >
		<label class="col-sm-2 control-label">${tk:lang("处理人","")}：</label>
	    <div class="col-sm-10">
	      <select name="userId" check-type="required" class="mustinput form-control input-sm" id="sltHelpWorker" required-message="${tk:lang("请选择处理人","")}"></select>
	    </div>
	</div>
	<div class="form-group" >
		<label class="col-sm-2 control-label">${tk:lang("描述","")}：</label>
	    <div class="col-sm-10">
	      	<textarea style="width: 100%;height: 100px;" id="srtChildTaskContent" name="content"></textarea>
	    </div>
	</div>
</form>
</script>