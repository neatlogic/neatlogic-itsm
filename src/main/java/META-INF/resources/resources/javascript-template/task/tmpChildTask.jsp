<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/x-template" id="tmpChildTask">
{{?it != null && it.length > 0}}
<div>
	<table class="table table-condensed table-hover">
		<thead>
		<tr>
			<th nowrap="nowrap">${tk:lang("标题","")}</th>
			<th nowrap="nowrap">${tk:lang("创建人","")}</th>
			<th nowrap="nowrap">${tk:lang("处理组","")}</th>
			<th nowrap="nowrap">${tk:lang("处理人","")}</th>
			<th nowrap="nowrap">${tk:lang("状态","")}</th>
			<th nowrap="nowrap">${tk:lang("创建时间","")}</th>
			<th nowrap="nowrap">${tk:lang("期望完成时间","")}</th>
			<th nowrap="nowrap">${tk:lang("开始时间","")}</th>
			<th nowrap="nowrap">${tk:lang("完成时间","")}</th>
			<th nowrap="nowrap">${tk:lang("操作","")}</th>
		</tr>
		</thead>
		<tbody>
		{{~it: task: index}}
		<tr style="text-align: center;" id="trChildTask{{=task.id}}">
			<td style="text-align: left;">
				<i class="pic-icon-small pic-icon-plus imgToggle" id="imgToggle{{=task.id}}" rel="0" taskid="{{=task.id}}"></i> {{=task.title}}
			</td>
			<td><span class="userInfo" userid="{{=task.owner}}">{{=task.ownername}}</span></td>
			<td nowrap="nowrap">{{=task.teamname}}</td>
			<td><span class="userInfo" userid="{{=task.userid}}">{{=task.username}}</span></td>
			<td nowrap="nowrap">
				<i class="pic-status pic-status-{{=task.status }}"></i>
			</td>
			<td style="font-size:12px">{{=task.createdate }}</td>
			<td style="font-size:12px">{{=task.targetDate || ""}}</td>
			<td style="font-size:12px">{{=task.startDate || ""}}</td>
			<td style="font-size:12px">{{=task.finishDate || ""}}</td>
			<td nowrap="nowrap">
				<div class="btn-group">
				{{?task.userid == "${myName}" && task.status == 8001}}
				<button type="button" class="btn btn-info btn-xs btnStartChildTask" taskid="{{=task.id}}">
					开始
				</button>
				{{?}} {{?task.owner == "${myName}" && (task.status == 8001 || task.status == 8002)}}
				<button type="button" class="btn btn-danger btn-xs btnAbortChildTask" taskid="{{=task.id}}">
					取消
				</button>
				{{?}}
				</div>
			</td>
		</tr>
		<tr id="trChildTaskDetail_{{=task.id}}" class="trChildTaskDetail" style="display: none;">
			<td colspan="10" id="tdChildTaskDetail_{{=task.id }}">
				<div style="margin-bottom: 5px">
					<div class="d_f" style="width: 25px">
						<span class="face_bg_mini">
							<img src="${pageContext.request.contextPath }/user/getUserFace.do?uid={{=task.owner}}&type=small">
						</span>
					</div>
					<div style="margin-left: 40px">
						<div class="popbox">
							<span class="poparrow"></span>
							<div class="d_f_r" style="color: #555">
								<i class="icon-time icon-grey"></i>{{=task.createdate}}
							</div>
							<div class="d_f_r">
								<i class="icon-user icon-grey"></i>
								<span class="userInfo" userid="{{=task.owner}}">{{=task.ownername}}</span>
							</div>
							<div class="clear"></div>
							<div>{{=task.content}}</div>
						</div>
					</div>
				</div>
				{{?task.replycontentid}}
				<div>
					<div class="d_f" style="width: 25px">
						<span class="face_bg_mini">
							<img src="${pageContext.request.contextPath }/user/getUserFace.do?uid={{=task.userid}}&type=small">
						</span>
					</div>
					<div style="margin-left: 40px">
						<div class="popbox">
							<span class="poparrow"></span>
							<div class="d_f_r" style="color: #555">
								<i class="icon-time icon-grey"></i>{{=task.replycontentcreatedate}}
							</div>
							<div class="d_f_r">
								<i class="icon-user icon-grey"></i>
								<span class="userInfo" userid="{{=task.userid}}">{{=task.username}}</span>
							</div>
							<div class="clear"></div>
							{{?task.userid == "${myName}" && task.status == 8002}}
							<div>
								<textarea class="srtChildTaskReplyContent" id="srtChildTaskReplyContent{{=task.id}}" taskid="{{=task.id}}" name="content" >{{=task.replycontent}}</textarea>
							</div>
							<div style="text-align: right">
								<div class="btn-group">
								<button type="button" taskid="{{=task.id}}" class="btnSaveChildTask btn btn-xs btn-info">暂存</button>
								<button type="button" taskid="{{=task.id}}" class="btnDoneChildTask btn btn-xs btn-success">完成</button>
								</div>
							</div>
							{{??}}
							<div>{{=task.replycontent}}</div>
							{{?}}
						</div>
					</div>
				</div>
				{{?? task.userid == "${myName}" && task.status == 8002}}
				<div>
					<div class="d_f" style="width: 25px">
						<span class="face_bg_mini">
							<img src="${pageContext.request.contextPath }/user/getUserFace.do?uid={{=task.userid}}&type=small">
						</span>
					</div>
					<div style="margin-left: 40px">
						<div class="popbox">
							<span class="poparrow"></span>
							<div>
								<textarea class="srtChildTaskReplyContent" id="srtChildTaskReplyContent{{=task.id}}" taskid="{{=task.id}}" name="content"></textarea>
							</div>
							<div style="text-align: right">
								<div class="btn-group">
								<button type="button" taskid="{{=task.id}}" class="btnSaveChildTask btn btn-xs btn-info">暂存</button>
								<button type="button" taskid="{{=task.id}}" class="btnDoneChildTask btn btn-success btn-xs">完成</button>
								</div>
							</div>
						</div>
					</div>
				</div>
				{{?}}
			</td>
		</tr>
		{{~}}
		</tbody>
	</table>
</div>
{{??}}
<div class="alert alert-default">
	<b>提示：</b>暂无子任务。
</div>
{{?}}
</script>