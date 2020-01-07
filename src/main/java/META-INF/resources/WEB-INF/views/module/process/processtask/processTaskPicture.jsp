<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java"%>
<%@include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE HTML>
<html>
<head>
<title>工单流程图：${processTaskId}</title>
<jsp:include page="/resources/res-include-new.jsp?module=process&exclude=bootstrap,skincss&need=base,bootstrap-validation,form,util,inputselect,tojson,codemirror,xdot,title,slidedialog,json,userinfo,checkselect,customcheckbox,ckeditor,slidebar,select,quartz,wdatepicker,scrollbar,octopuscss,d3arrange" />

<script type="text/javascript">
	var STEPSTATUS_TIMMER = null;
	var JOBSTATUS_TIMMER = null;
	var PREDICTION_TIMMER = null;
	$(function() {

		$("#completeBtn").on('click', function(e) {
			var submitData = {};

			var stepId = $("#completestepId").val();
			var nextStepId = $('#nextStepId').val();
			submitData['stepId'] = stepId;
			submitData['nextStepId'] = nextStepId;
			$.ajax({
				url : "${pageContext.request.contextPath}/module/process/processtask/processtaskstep/" + stepId + "/complete",
				dataType : 'json',
				type : 'POST',
				data : JSON.stringify(submitData, null, 2),
				contentType : "application/json",
				success : function(data) {
					showPopMsg.success('操作成功 ');
				}
			});
		})
		$("#startBtn").on('click', function(e) {
			var submitData = {};

			var stepId = $("#completestepId").val();
			var nextStepId = $('#nextStepId').val();
			submitData['stepId'] = stepId;
			submitData['nextStepId'] = nextStepId;
			$.ajax({
				url : "${pageContext.request.contextPath}/module/process/processtask/processtaskstep/" + stepId + "/start",
				dataType : 'json',
				type : 'POST',
				data : JSON.stringify(submitData, null, 2),
				contentType : "application/json",
				success : function(data) {
					showPopMsg.success('操作成功 ');
				}
			});
		});
		$('#acceptBtn').on('click', function() {
			var submitData = {};

			var stepId = $("#completestepId").val();
			var nextStepId = $('#nextStepId').val();
			submitData['stepId'] = stepId;
			submitData['nextStepId'] = nextStepId;
			$.ajax({
				url : "${pageContext.request.contextPath}/module/process/processtask/processtaskstep/" + stepId + "/accept",
				dataType : 'json',
				type : 'POST',
				data : JSON.stringify(submitData, null, 2),
				contentType : "application/json",
				success : function(data) {
					showPopMsg.success('操作成功 ');
				}
			});
		});

		$('#backBtn').on('click', function() {
			var submitData = {};
			var stepId = $("#completestepId").val();
			var nextStepId = $('#nextStepId').val();
			submitData['stepId'] = stepId;
			submitData['nextStepId'] = nextStepId;
			$.ajax({
				url : "${pageContext.request.contextPath}/module/process/processtask/processtaskstep/" + stepId + "/back",
				dataType : 'json',
				type : 'POST',
				data : JSON.stringify(submitData, null, 2),
				contentType : "application/json",
				success : function(data) {
					showPopMsg.success('操作成功 ');
				}
			});
		});
		$('#abortBtn').on('click', function() {
			var submitData = {};
			var processTaskId = $("#hidJobId").val();
			submitData['processTaskId'] = processTaskId;
			$.ajax({
				url : "${pageContext.request.contextPath}/module/process/processtask/" + processTaskId + "/abort",
				dataType : 'json',
				type : 'POST',
				data : JSON.stringify(submitData, null, 2),
				contentType : "application/json",
				success : function(data) {
					showPopMsg.success('操作成功 ');
				}
			});
		});
		$('#recoverBtn').on('click', function() {
			var submitData = {};
			var processTaskId = $("#hidJobId").val();
			submitData['processTaskId'] = processTaskId;
			$.ajax({
				url : "${pageContext.request.contextPath}/module/process/processtask/" + processTaskId + "/recover",
				dataType : 'json',
				type : 'POST',
				data : JSON.stringify(submitData, null, 2),
				contentType : "application/json",
				success : function(data) {
					showPopMsg.success('操作成功 ');
				}
			});
		});
		
		$('a[data-toggle="tab"]').on('shown.bs.tab', function(e) {
			if (e.target.toString().indexOf('tabFlow') > -1) {
				resetFlowDivHeight();
				if (!$("#divPaper").html()) {
					var flowJobId = $('#hidJobId').val();
					console.log(flowJobId);
					$.getJSON('${pageContext.request.contextPath}/module/process/processtask/flowjobconfig/' + flowJobId, function(data) {
						var paper = TsFlowChart($("#divPaper")[0], {
							readOnly : true
						});
						for (var i = 0; i < data.elementList.length; i++) {
							var element = data.elementList[i];
							if (element.type && element.type != 'start' && element.type != 'end') {
								element.onClick = function(node) {
									$.getJSON('${pageContext.request.contextPath}/module/octopus/flowjob/' + flowJobId + '/step/' + node.getId(), function(data) {
										var html = xdoT.render('octopus.flowjobstep.' + node.getType() + '.showstepdetail', {
											step : data
										});
										createSlideDialog({
											content : html,
											title : '步骤信息',
											width : '90%',
											showclose : false,
											blurclose : true
										});
									});
								}
							}
						}
						paper.fromJson(data);
						if (flowJobId) {
							paper.setData('flowJobId', flowJobId);
						}
						var nodeList = paper.getAllNode();
						for (var i = 0; i < nodeList.length; i++) {
							var node = nodeList[i];
							if (node.getUserData() && node.getUserData()['isStartNode'] == '1') {
								node.setStyle({
									fill : '#70BC82',
									stroke : '#009688',
									fillopacity : 1,
									fontcolor : '#ffffff',
									fontsize : 12,
									iconcolor : '#ffffff',
									strokewidth : 1,
									strokedasharray : 0
								});
								node.setIsStart(true);
								node.setNeedIn('optional');
								break;
							}
						}
						$('#divPaper').data('paper', paper);
					});
					getFlowJobStepStatus();
				}
			}
		});

		$("#flowTab").click();

		$(window).on('resize', function() {
			resetFlowDivHeight();
		});

		getFlowJobStepStatus();
		getFlowJobStatus();
	});

	function resetFlowDivHeight() {
		var height = $(window).height() - $("#divPaper").offset().top - 40;
		$("#divPaper").css('height', height + 'px');
	}

	function getFlowJobStatus() {
		if (JOBSTATUS_TIMMER) {
			clearTimeout(JOBSTATUS_TIMMER);
			JOBSTATUS_TIMMER = null;
		}
		var flowJobId = $('#hidJobId').val();
		$.getJSON('${pageContext.request.contextPath}/module/octopus/flowjob/' + flowJobId + '/jobstatus', function(data) {
			$('#divJobStatus').empty().html('<span class="textstatus-'+data.status+'">' + data.statusText + '</span>');
			$('#divJobStartTime').empty().html(data.startTime);
			$('#divJobEndTime').empty().html(data.endTime);
			$('#divJobTimeCost').empty().html(data.timeCostStr);
			$('#hidJobStatus').val(data.status);
			if (data.error) {
				$('#divJobError').show();
				$('#divJobError').empty().html(data.error);
			} else {
				$('#divJobError').hide();
				$('#divJobError').empty()
			}
			if (data.status == 'pending' || data.status == 'running') {
				JOBSTATUS_TIMMER = setTimeout(function() {
					getFlowJobStatus();
				}, 3000);
			}
		});
	}

	var STEPSTATUS_RETRY_TIME = 0;

	var BTN_MAP = function(nodeType, status) {
		return [];
	};

	var STYLE_MAP = {
		'succeed' : {
			fill : '#70BC82',
			stroke : '#009688',
			fillopacity : 1,
			fontcolor : '#ffffff',
			fontsize : 12,
			iconcolor : '#ffffff',
			strokewidth : 1,
			strokedasharray : 0
		},
		'failed' : {
			fill : '#DE5045',
			stroke : '#DE5045',
			fillopacity : 1,
			fontcolor : '#ffffff',
			iconcolor : '#ffffff',
			fontsize : 12,
			strokewidth : 1,
			strokedasharray : 0
		},
		'pending' : {
			fill : '#ffffff',
			stroke : '#999999',
			fillopacity : 1,
			fontcolor : '#999999',
			iconcolor : '#999999',
			fontsize : 12,
			strokewidth : 1,
			strokedasharray : "3 3"
		},
		'hang' : {
			fill : '#eeeeee',
			stroke : '#999999',
			fillopacity : 1,
			fontcolor : '#999999',
			iconcolor : '#999999',
			fontsize : 12,
			strokewidth : 1,
			strokedasharray : 0
		},
		'back' : {
			fill : '#F7B538',
			stroke : '#F7B538',
			fillopacity : 1,
			fontcolor : '#ffffff',
			iconcolor : '#ffffff',
			fontsize : 12,
			strokewidth : 1,
			strokedasharray : 0
		},
		'locked' : {
			fill : '#ffffff',
			stroke : '#999999',
			fillopacity : 1,
			fontcolor : '#999999',
			iconcolor : '#999999',
			fontsize : 12,
			strokewidth : 1,
			strokedasharray : 0
		},
		'running' : {
			fill : '#E5F9FC',
			stroke : '#00C1DE',
			fillopacity : 1,
			fontcolor : '#00C1DE',
			iconcolor : '#00C1DE',
			fontsize : 12,
			strokewidth : 1,
			strokedasharray : 0
		},
		'ignored' : {
			fill : '#efefef',
			stroke : '#ddd',
			fillopacity : 1,
			fontcolor : '#555',
			iconcolor : '#555',
			fontsize : 12,
			strokewidth : 1,
			strokedasharray : 0
		}
	}

	function updateFlowJobChartStatus(paper, data) {
		if (paper) {
			if (data.stepList && data.stepList.length > 0) {
				for (var i = 0; i < data.stepList.length; i++) {
					var step = data.stepList[i];
					var nodeList = paper.getAllNode();
					for (var n = 0; n < nodeList.length; n++) {
						var node = nodeList[n];
						if (node.getId() == step['uid']) {
							if (!node.getData('status') || node.getData('status') != step['status']) {
								if (step['status'] == 'running') {
									node.setProgress({
										color : '#00C1DE',
										opacity : 0.5,
										progress : 0.5,
										enable : true
									});
								} else {
									node.setProgress({
										enable : false
									});
								}
								if (step['status'] == 'locked') {
									node.setStatusIcon([ {
										name : 'locked'
									} ]);
								} else {
									node.setStatusIcon([]);
								}
								node.setStyle(STYLE_MAP[step['status']]);
								if ((step['handleMode'] == 'at' && step['hasRunRole']) || (step['status'] == 'doing' && step['canSolve'])) {
									node.setStatusBtn([]);
									node.setStatusBtn(BTN_MAP(node.getType(), step['status']));
								} else {
									node.setStatusBtn([]);
								}
								node.setData('status', step['status']);
							}
							break;
						}
					}
				}
			}
			if (data.relList && data.relList.length > 0) {
				for (var i = 0; i < data.relList.length; i++) {
					var link = paper.getPathById(data.relList[i]['uid']);
					if (data.relList[i]['status'] == '1') {
						link.setStyle({
							stroke : '#009688'
						});
					} else if (data.relList[i]['status'] == '-1') {
						link.setStyle({
							stroke : '#F48FB1'
						});
					} else {
						link.setStyle({
							stroke : '#9b9b9b'
						});
					}
				}
			}
		}
	}

	function getFlowJobStepStatus() {
		if (STEPSTATUS_TIMMER) {
			clearTimeout(STEPSTATUS_TIMMER);
			STEPSTATUS_TIMMER = null;
		}
		var flowJobId = $('#hidJobId').val();
		$.getJSON('${pageContext.request.contextPath}/module/process/processtask/' + flowJobId + '/stepstatus', function(data) {
			var hasRunning = false;

			updateFlowJobChartStatus($('#divPaper').data('paper'), data);
			if (data.stepList && data.stepList.length > 0) {
				for (var i = 0; i < data.stepList.length; i++) {
					var step = data.stepList[i];
					if (step['status'] == 'running') {
						hasRunning = true;
					}
				}
			}
			if (hasRunning || STEPSTATUS_RETRY_TIME <= 3) {
				STEPSTATUS_TIMMER = setTimeout(function() {
					getFlowJobStepStatus();
				}, 3000);
				if (!hasRunning) {
					STEPSTATUS_RETRY_TIME += 1;
				} else {
					STEPSTATUS_RETRY_TIME = 0;
				}
			} else {
				STEPSTATUS_TIMMER = null;
			}
		});
	}
</script>
<style type="text/css">
.divSort {
	position: absolute;
	top: 0px;
	left: 0px;
	border-radius: 100%;
	border: 3px solid #23C6C8;
	color: #23C6C8;
	text-align: center;
	font-size: 20px;
	font-size: 1.4285714285714286rem;
	height: 34px;
	width: 34px;
	font-weight: bold;
	background: #fff;
	z-index: 2;
	display: inline-block;
}

.divSort.succeed, .divSortLine.succeed {
	border-color: #1BB394;
	color: #1BB394;
}

.divSort.failed, .divSortLine.failed {
	border-color: #ED5666;
	color: #ED5666;
}

.divSort.waitconfirm, .divSortLine.waitconfirm {
	border-color: #F8AC5A;
	color: #F8AC5A;
}

.divSort.running, .divSortLine.running {
	border-color: #1D84C6;
	color: #1D84C6;
}

.divSort.pending, .divSortLine.pending {
	border-color: #cccccc;
	color: #cccccc;
}

.divSortLine {
	position: absolute;
	z-index: 1;
	width: 19px;
	height: 100%;
	border-right: 3px solid #23C6C8;
	top: 0px;
	left: 0px;
}

.trStep:last-child .divSortLine {
	display: none
}

.trStep.trError td {
	background-color: #ffd;
}

.trStep.trError td:last-child {
	border-right: 3px solid #D9534F;
}

.tabJobStep {
	width: 100%
}

.tabJobStep td, .tabJobStep th {
	padding: 5px 0px 20px 0px;
	vertical-align: top;
}

.divTitle, .divContent {
	margin: 10px 0px;
}

.divContent {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap
}

.divTitle {
	text-align: right;
}

pre {
	border: none;
}

td.tar {
	text-align: right;
	color: #666;
}

.tdTimeCost .ts-m-clock {
	color: #999;
}

.CodeMirror-scroll {
	min-height: 500px
}

.CodeMirror {
	border: 1px solid #eee;
	height: auto;
}

.trlead .tracount {
	border-radius: 50%;
	width: 25px;
	height: 25px;
	font-size: 14px;
	color: #336eff;
	text-align: center;
	line-height: 30px;
	border: 1px solid;
	position: relative;
	background: #fff;
}

.tracount span {
	position: absolute;
	width: 25px;
	height: 25px;
	margin-left: -13px;
	margin-top: -15px;
	top: 50%;
	left: 50%;
}

td.trlead {
	position: relative;
}

.trStep:not (:last-of-type ) td.trlead:before {
	position: absolute;
	content: "";
	width: 1px;
	top: 50%;
	bottom: -50%;
	background: #336eff;
	left: 19px;
}

.timeline {
	background: #ddd;
	position: relative;
}

.timeline .percent {
	position: absolute;
	top: 0;
	left: 0;
	background: #81a4fb;
	width: 0;
	height: 20px;
	transition: all .3s;
}

.timeline .percent.failed {
	background: #e9626c;
}

.timeline .percent.succeed {
	background: #8af1b5;
}

.lh36 {
	height: 36px;
	line-height: 36px;
	padding-left: 8px;
}
</style>
</head>
<body class="bg-grey">

	<div class="block-main" style="position: relative;">
		<input id="hidJobId" type="hidden" value="${processTaskId}">
		<ul class="nav-bdbottom nav-tabs" role="tablist">
			<li role="presentation" class="active">
				<a href="#tabMain" aria-controls="home" role="tab" data-toggle="tab">作业信息</a>
			</li>
			<li role="presentation">
				<a href="#tabFlow" id="flowTab" aria-controls="profile" role="tab" data-toggle="tab">流程图</a>
			</li>
		</ul>
		<div class="tab-content">
			<div role="tabpanel" class="tab-pane active" id="tabMain">


				<div id="scrollDiv">
					<br> <br> <br> 完成步骤ID:
					<input type="text" name="aa" id="completestepId">
					<br> <br> <br> 下一步:
					<input type="text" name="aa" id="nextStepId">
					<br> <br> <br>
					<input type="button" id="startBtn" class="btn btn-primary" value="开始" />
					<input type="button" id="acceptBtn" class="btn btn-primary" value="接管" />
					<input type="button" id="backBtn" class="btn btn-danger" value="回退" />
					<input type="button" id="abortBtn" class="btn btn-danger" value="终止" />
					<input type="button" id="recoverBtn" class="btn btn-danger" value="恢复" />
					<input type="button" id="completeBtn" class="btn btn-success" value="完成" />
				</div>
			</div>
			<div role="tabpanel" class="tab-pane" id="tabFlow">

				<div id="divPaper"></div>
			</div>
		</div>
	</div>
</body>
</html>



