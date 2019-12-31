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
		
		$("#completeBtn").on('click', function(e){
			var submitData = {};
			
			var stepId = $("#completestepId").val();
			submitData['stepId'] = stepId;
			$.ajax({
				url :  "${pageContext.request.contextPath}/module/process/processtask/processtaskstep/"+stepId+"/complete",
				dataType : 'json',
				type : 'POST',
				data : JSON.stringify(submitData, null, 2),
				contentType : "application/json",
				success : function(data) {
					showPopMsg.success('操作成功 ');
				}
			});
		})
		$("#startBtn").on('click', function(e){
			var submitData = {};
			
			var stepId = $("#completestepId").val();
			submitData['stepId'] = stepId;
			$.ajax({
				url :  "${pageContext.request.contextPath}/module/process/processtask/processtaskstep/"+stepId+"/start",
				dataType : 'json',
				type : 'POST',
				data : JSON.stringify(submitData, null, 2),
				contentType : "application/json",
				success : function(data) {
					showPopMsg.success('操作成功 ');
				}
			});
		})
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
		if (status == 'pending') {
			if (nodeType == 'node') {
				return [ {
					icon : '\ue86c',
					title : '锁定',
					iconsize : 14,
					iconcolor : '#555',
					iconfamily : 'ts',
					bgfill : '#ffffff',
					bgfillopacity : 0,
					segmentation : '#969696',
					onclick : function(node) {
						var flowJobId = node.getSvg().getData('flowJobId');
						var uid = node.getId();
						createModalDialog({
							msgtitle : '锁定确认',
							msgcontent : '是否确认锁定当前步骤？',
							successFuc : function() {
								$.post('${pageContext.request.contextPath}/module/octopus/flowjob/' + flowJobId + '/step/' + uid + '/lock', function(data) {
									if (data.Status == 'OK') {
										showPopMsg.success();
										//node.setStatusBtn(BTN_MAP(nodeType, 'locked'));
										//node.setStyle(STYLE_MAP['locked']);
										getFlowJobStepStatus();
										getFlowJobStatus();
										getFlowJobPrediction();
									} else {
										showPopMsg.error(data);
									}
								}, 'json');
							}
						});
					}
				} ];
			} else {
				return [];
			}
		} else if (status == 'running') {
			if (nodeType == 'node') {
				return [ {
					icon : '\ue84d',
					title : '终止',
					iconsize : 14,
					iconcolor : '#00C1DE',
					iconfamily : 'ts',
					bgfill : '#fff',
					bgfillopacity : 1,
					segmentation : '#00C1DE',
					onclick : function(node) {
						var flowJobId = node.getSvg().getData('flowJobId');
						var uid = node.getId();
						createModalDialog({
							msgtitle : '终止确认',
							msgcontent : '是否确认终止当前步骤？',
							successFuc : function() {
								$.post('${pageContext.request.contextPath}/module/octopus/flowjob/' + flowJobId + '/step/' + uid + '/abort', function(data) {
									if (data.Status == 'OK') {
										showPopMsg.success();
										//node.setProgress({
										//	enable : false
										//});
										//node.setStatusBtn(BTN_MAP(nodeType, 'aborted'));
										//node.setStyle(STYLE_MAP['aborted']);
										getFlowJobStepStatus();
										getFlowJobStatus();
										getFlowJobPrediction();
									} else {
										showPopMsg.error(data);
									}
								}, 'json');
							}
						});
					}
				} ];
			} else {
				return [];
			}
		} else if (status == 'waitconfirm') {
			return [ {
				icon : '\ueaac',
				title : '确认',
				iconsize : 14,
				iconcolor : '#87B2EC',
				iconfamily : 'ts',
				bgfill : '#ffffff',
				bgstroke : '#87B2EC',
				bgfillopacity : 1,
				segmentation : '#87B2EC',
				onclick : function(node) {
					var flowJobId = node.getSvg().getData('flowJobId');
					var uid = node.getId();
					createModalDialog({
						msgtitle : '确认提示',
						msgcontent : '是否确认当前步骤执行成功？',
						successFuc : function() {
							$.post('${pageContext.request.contextPath}/module/octopus/flowjob/' + flowJobId + '/step/' + uid + '/confirm', function(data) {
								if (data.Status == 'OK') {
									showPopMsg.success('操作成功');
									getFlowJobStepStatus();
									getFlowJobStatus();
									getFlowJobPrediction();
								} else {
									showPopMsg.error(data);
								}
							}, 'json');
						}
					});
				}
			} ];
		} else if (status == 'locked') {
			return [ {
				icon : '\ue8d6',
				title : '解锁',
				iconsize : 14,
				iconcolor : '#555',
				iconfamily : 'ts',
				bgfill : '#fff',
				bgstroke : 'transparent',
				bgfillopacity : 1,
				segmentation : '#969696',
				onclick : function(node) {
					var flowJobId = node.getSvg().getData('flowJobId');
					var uid = node.getId();
					createModalDialog({
						msgtitle : '锁定确认',
						msgcontent : '是否确认解除当前步骤锁定？',
						successFuc : function() {
							$.post('${pageContext.request.contextPath}/module/octopus/flowjob/' + flowJobId + '/step/' + uid + '/unlock', function(data) {
								if (data.Status == 'OK') {
									showPopMsg.success();
									getFlowJobStepStatus();
									getFlowJobStatus();
									getFlowJobPrediction();
								} else {
									showPopMsg.error(data);
								}
							}, 'json');
						}
					});
				}
			} ];
		} else if (status == 'aborted' || status == 'failed') {
			var color = '';
			if (status == 'aborted') {
				color = '#F7B538';
			} else {
				color = '#DE5045';
			}
			return [ {
				icon : '\uea89',
				title : '重做',
				iconsize : 14,
				iconcolor : color,
				iconfamily : 'ts',
				bgfill : '#fff',
				bgstroke : 'transparent',
				bgfillopacity : 1,
				segmentation : color,
				onclick : function(node) {
					var flowJobId = node.getSvg().getData('flowJobId');
					var uid = node.getId();
					createModalDialog({
						msgtitle : '执行确认',
						msgcontent : '是否确认重新执行当前步骤？',
						successFuc : function() {
							$.post('${pageContext.request.contextPath}/module/octopus/flowjob/' + flowJobId + '/step/' + uid + '/run', function(data) {
								if (data.Status == 'OK') {
									showPopMsg.success();
									getFlowJobStepStatus();
									getFlowJobStatus();
									getFlowJobPrediction();
								} else {
									showPopMsg.error(data);
								}
							});
						}
					});
				}
			}, {
				icon : '\ue855',
				title : '忽略',
				iconsize : 14,
				iconcolor : color,
				iconfamily : 'ts',
				bgfill : '#fff',
				bgstroke : 'transparent',
				bgfillopacity : 1,
				segmentation : color,
				onclick : function(node) {
					var flowJobId = node.getSvg().getData('flowJobId');
					var uid = node.getId();
					$.getJSON('${pageContext.request.contextPath}/module/octopus/flowjob/' + flowJobId + '/step/' + uid + '/nextsteplist', function(data) {
						var html = xdoT.render('octopus.flowjob.listnextflowjobstep', data);
						createModalDialog({
							msgtitle : '忽略确认',
							msgcontent : html,
							msgwidth : 600,
							successFuc : function() {
								var hasCheck = false;
								var flowJobStepIdList = new Array();
								$('.chkFlowJobStep').each(function() {
									if ($(this).prop('checked')) {
										flowJobStepIdList.push($(this).val());
									}
								});
								if (flowJobStepIdList.length > 0) {
									$.post('${pageContext.request.contextPath}/module/octopus/flowjob/' + flowJobId + '/step/' + uid + '/ignore', {
										nextStepIdList : flowJobStepIdList
									}, function(data) {
										if (data.Status == 'OK') {
											getFlowJobStepStatus();
											getFlowJobStatus();
											getFlowJobPrediction();
											showPopMsg.success();
										} else {
											showPopMsg.error(data);
										}
									});
								} else {
									showPopMsg.info('请选择需要流转的路径');
								}
							}
						});
					});
				}
			} ];
		} else if (status == 'doing') {
			return [ {
				icon : '\ue85f',
				title : '处理',
				iconsize : 14,
				iconcolor : '#0BC4E0',
				iconfamily : 'ts',
				bgfill : '#ffffff',
				bgstroke : '#0BC4E0',
				bgfillopacity : 1,
				segmentation : '#0BC4E0',
				onclick : function(node) {
					var flowJobId = node.getSvg().getData('flowJobId');
					var uid = node.getId();
					$.getJSON('${pageContext.request.contextPath}/module/octopus/flowjob/' + flowJobId + '/step/' + uid, function(data) {
						var btnList = new Array();
						var editor = null;
						var slideDialog = null;
						var submitFuc = function(action) {
							$('#hidAction').val(action);
							if (editor) {
								$('#txtContent').val(editor.getData());
							}
							if ($('#formVerify').valid()) {
								$.ajax({
									url : '${pageContext.request.contextPath}/module/octopus/flowjob/step/' + data.id + '/solve',
									dataType : 'json',
									type : 'POST',
									data : JSON.stringify($('#formVerify').toJson()),
									contentType : "application/json",
									success : function(d) {
										if (d.Status == 'OK') {
											if (slideDialog) {
												slideDialog.hide();
											}
											showPopMsg.success();
											getFlowJobStepStatus();
											getFlowJobStatus();
											getFlowJobPrediction();
										} else {
											showPopMsg.error(d);
										}
									}
								});
							}
							return false;
						};
						var config = JSON.parse(data.paramConfig);
						data.config = config;
						btnList.push({
							classname : 'btn-primary',
							text : '同意',
							click : function() {
								submitFuc('agree');
							}
						});
						btnList.push({
							classname : 'btn-danger',
							text : '不同意',
							click : function() {
								submitFuc('disagree');
							}
						});
						var html = xdoT.render('octopus.flowjobstep.verify.handlestep', data);
						slideDialog = createSlideDialog({
							title : data.name,
							content : html,
							width : '85%',
							showclose : false,
							blurclose : true,
							customButtons : btnList,
							shownFuc : function() {
								editor = CKEDITOR.replace('txtContent', {
									extraPlugins : '',
									height : 500
								});
							}
						});
					});
				}
			} ];
		} else {
			return [];
		}
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
		'done' : {
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
		'timing' : {
			fill : '#ffffff',
			stroke : '#999999',
			fillopacity : 1,
			fontcolor : '#999999',
			iconcolor : '#999999',
			fontsize : 12,
			strokewidth : 1,
			strokedasharray : 0
		},
		'aborted' : {
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
		'doing' : {
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
			/* if ($.trim($('#divFlowJobStep').html()) == '') {
				var html = xdoT.render('octopus.flowjob.listflowjobstep', data);
				$('#divFlowJobStep').empty().html(html);
				for (var s = 0; s < data.stepList.length; s++) {
					var stepData = data.stepList[s];
					var btnhtml = xdoT.render('octopus.flowjob.listflowjobstepbtn', stepData);
					$('#divFlowJobStep').find('.tdStepBtn' + stepData.id).empty().html(btnhtml);
				}
			} else {
				for (var s = 0; s < data.stepList.length; s++) {
					var stepData = data.stepList[s];
					var tdBtn = $('#divFlowJobStep').find('.tdStepBtn' + stepData.id);
					var tdStepTimeCost = $('#divFlowJobStep').find('.tdStepTimeCost' + stepData.id);
					var tdStepStatus = $('#divFlowJobStep').find('.tdStepStatus' + stepData.id);
					var tdStepProcess = $('#divFlowJobStep').find('.tdStepProcess' + stepData.id);
					var tdStartTime = $('#divFlowJobStep').find('.tdStartTime' + stepData.id);
					var tdEndTime = $('#divFlowJobStep').find('.tdEndTime' + stepData.id);
					var tdChildFlow = $('#divFlowJobStep').find('.tdChildFlow' + stepData.id);
					tdEndTime.text(stepData.endTime || '-');
					tdStartTime.text(stepData.startTime || '-');
					var timehtml = xdoT.render('octopus.flowjob.listflowjobsteptimecost', stepData);
					tdStepTimeCost.empty().html(timehtml);
					var processhtml = xdoT.render('octopus.flowjob.listflowjobstepprocess', stepData);
					tdStepProcess.empty().html(processhtml);
					if (tdChildFlow.data('status') != stepData.status) {
						var childhtml = xdoT.render('octopus.flowjob.listflowjobchildflow', stepData);
						tdChildFlow.empty().html(childhtml);
						tdChildFlow.data('status', stepData.status);
					}
					if (tdBtn.data('status') != stepData.status) {
						var btnhtml = xdoT.render('octopus.flowjob.listflowjobstepbtn', stepData);
						tdBtn.empty().html(btnhtml);
						tdBtn.data('status', stepData.status);
					}
					if (tdStepStatus.data('status') != stepData.status) {
						var statushtml = xdoT.render('octopus.flowjob.listflowjobstepstatus', stepData);
						tdStepStatus.empty().html(statushtml);
						tdStepStatus.data('status', stepData.status);
					}

				}
			} */
			updateFlowJobChartStatus($('#divPaper').data('paper'), data);
			if (data.stepList && data.stepList.length > 0) {
				for (var i = 0; i < data.stepList.length; i++) {
					var step = data.stepList[i];
					if (step['status'] == 'running' || step['status'] == 'doing' || step['status'] == 'timing') {
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
			<li role="presentation" >
				<a href="#tabFlow" id="flowTab" aria-controls="profile" role="tab" data-toggle="tab">流程图</a>
			</li>
		</ul>
		<div class="tab-content">
			<div role="tabpanel" class="tab-pane active" id="tabMain">
		

				<div id="scrollDiv">
					<br><br><br>
					完成步骤ID:<input type="text" name="aa" id="completestepId">
					<br><br><br>
					<input type="button" id="startBtn" class="btn btn-primary" value="开始"/>
				
					<input type="button" id="completeBtn" class="btn btn-success" value="完成"/>
				</div> 
			</div>
			<div role="tabpanel" class="tab-pane" id="tabFlow">
			
				<div id="divPaper"></div>   
			</div>
		</div>
	</div> 
</body>
</html>  



