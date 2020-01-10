<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE HTML>
<html lang="en">
<head>
<title>上报工单</title>
<jsp:include page="/resources/res-include-new.jsp?module=process&exclude=bootstrap,skincss,tsicon&need=base,jquery,bootstrap,uploader,bootstrap-validation,tsicon,userinfo,customcheckbox,ckeditor,slidedialog,xdot,util,title,snapmessage,form,json,tojson,d3arrange,checkselect,select,scrollbar,wdatepicker,sheet,inputselect" />
<script>
	$(function() {
		getProcessByUuid($('#hidProcessUuid').val());
		$('#btnSave').on('click', function() {
			if ($('#form_process').valid()) {
				$(this).attr('disabled', true);
				var processId = $('#hidProcessUuid').val();
				var submitData = {};

				$('.form_step').each(function() {
					$(this).on('getConfig', function(event) {
						var step = event.result;
						step['uuid'] = $(this).data('uuid');
						submitData['step'] = step;
						event.stopPropagation();
					});
					$(this).trigger('getConfig');
				});

				submitData['title'] = $('#txtTitle').val();
				$.ajax({
					url : '${pageContext.request.contextPath}/module/process/processtask/' + processId + '/startnewtask',
					dataType : 'json',
					type : 'POST',
					data : JSON.stringify(submitData, null, 2),
					contentType : "application/json",
					success : function(data) {
						if (data.Status == 'OK') {
							showPopMsg.success('操作成功 '+data.processTaskId, function() {
								$("#btnSave").next().html("上报成功，工单号："+data.processTaskId);
							});
						} else {
							showPopMsg.error('操作失败，异常：<br>' + data.Message);
						}
						$('#btnSave').removeAttr('disabled');
					}
				});
			}
		});
	});

	function getProcessByUuid(uuid) {
		$.getJSON('${pageContext.request.contextPath}/module/process/process/getstart/' + uuid, function(data) {
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
		<input type="hidden" id="hidProcessUuid" value="${param.uuid }">
		<div>
			<label>标题：</label>
		</div>
		<input type="text" id="txtTitle" class="input-xxlarge">
		<textarea name="content"></textarea>
		<button type="button" id="btnSave">提交</button>
		<h1></h1>
	</form>
</body>
</html>