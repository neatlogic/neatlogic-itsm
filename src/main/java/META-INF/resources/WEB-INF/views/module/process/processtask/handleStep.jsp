<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE HTML>
<html lang="en">
<head>
<title>${processTaskStepVo.name }</title>
<jsp:include page="/resources/res-include-new.jsp?module=process&exclude=bootstrap,skincss,tsicon&need=base,uploader,userinfo,jquery,bootstrap,sheet,bootstrap-validation,tsicon,customcheckbox,ckeditor,treeselect,codemirror,slidedialog,xdot,util,title,snapmessage,form,json,quartz,tojson,d3arrange,draggable,checkselect,select,scrollbar,wdatepicker,inputselect" />
<script>
	$(function() {
		getProcessTaskStep($('#hidProcessTaskStepId').val());

		$('#btnSave').on('click', function() {
			if ($('#form_process').valid()) {
				$(this).attr('disabled', true);
				var processId = $('#hidProcessUuid').val();
				var submitData = {};
				var attributeValueList = new Array();
				$('.form_step').each(function() {
					$(this).on('getConfig', function(event) {
						var step = event.result;
						step['uuid'] = $(this).data('uuid');
						submitData['step'] = step;
						event.stopPropagation();
					});
					$(this).trigger('getConfig');
				});
				$('.form_attribute').each(function() {
					var attr = {};
					attr['uuid'] = $(this).data('uuid');
					$(this).on('getValue', function(event) {
						attr['value'] = event.result;
						event.stopPropagation();
					}).trigger('getValue');
					$(this).on('getData', function(event) {
						attr['data'] = event.result;
						event.stopPropagation();
					}).trigger('getData');
					attributeValueList.push(attr);
				});
				submitData['attributeValueList'] = attributeValueList;
				console.log(JSON.stringify(submitData, null, 2));

				submitData['title'] = $('#txtTitle').val();
				$.ajax({
					url : '${pageContext.request.contextPath}/module/process/processtask/' + processId + '/startnewtask',
					dataType : 'json',
					type : 'POST',
					data : JSON.stringify(submitData, null, 2),
					contentType : "application/json",
					success : function(data) {
						if (data.Status == 'OK') {
							showPopMsg.success('${tk:lang("操作成功","")}', function() {
								top.addTab('${pageContext.request.contextPath}/module/process/processtask/getProcessTaskDetail.do?processTaskId=' + data.processTaskId);
								top.closeCurrent(window);
							});
						} else {
							showPopMsg.error('${tk:lang("操作失败，异常","")}：<br>' + data.Message);
						}
						$('#btnRun').removeAttr('disabled');
					}
				});
			}
		});
	});

	function getProcessTaskStep(id) {
		$.getJSON('${pageContext.request.contextPath}/module/process/processtask/processtaskstep/' + id, function(data) {
			if (data && data.editPage) {
				var html = xdoT.render(data.editPage, data);
				$('#divAttribute').html(html);
			}
		});
	}
</script>
</head>
<body>
	<form id="form_process" method="POST">
		<input type="hidden" id="hidProcessTaskStepId" value="${param.id }">
		<div id="divAttribute"></div>
		<button type="button" id="btnSave">${tk:lang("提交","")}</button>
	</form>
</body>
</html>