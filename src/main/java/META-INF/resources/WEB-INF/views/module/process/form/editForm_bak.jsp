<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<!DOCTYPE HTML>
<html lang="en">
<head>
<title>编辑表单</title>
<jsp:include page="/resources/res-include-new.jsp?module=process&exclude=bootstrap,skincss,tsicon&need=base,jquery,bootstrap,bootstrap-validation,tsicon,uploader,customcheckbox,slidedialog,sheet,xdot,util,title,snapmessage,form,json,tojson,checkselect,select,scrollbar,wdatepicker,inputselect" />
<script type="text/javascript">
	$(function() {
		var formbuilder = $('#divContent').SheetBuilder({
			cols : 5,
			rows : 10,
			onCellDrop : function(td, event) {
				var e;
				if (event.isTrigger) {
					e = triggerEvent.originalEvent;
				} else {
					e = event.originalEvent;
				}
				var uuid = e.dataTransfer.getData('uuid');
				uuid = $.trim(uuid);
				if (uuid) {
					var config = td.builder.toJson();
					var isExists = false;
					for ( var cc in config['cells']) {
						var cell = config['cells'][cc];
						if (cell.data && cell.data['uuid']) {
							if (cell.data['uuid'] == uuid) {
								isExists = true;
								break;
							}
						}
					}
					if (!isExists) {
						$.getJSON('${pageContext.request.contextPath}/module/process/attribute/get/' + uuid, (function(uuid) {
							return function(data) {
								if (data) {
									var html;
									if (data.editTemplate) {
										html = xdoT.renderWithHtml(data.editTemplate, data);
										td.setContent(html);
									} else {
										html = xdoT.render(data.inputPage, data);
										td.setContent(html);
									}
									td.getDataFuc = function(td) {
										var config = {
											'uuid' : data.attributeUuid
										};
										//不保存值，统一在流程节点中配置
										/*html.on('getData', function(event) {
											config.data = event.result;
										}).trigger('getData');
										html.on('getValue', function(event) {
											config.value = event.result;
										}).trigger('getValue');*/
										td.setData(config);
									};
								}
							};
						}(uuid)));
					} else {
						showPopMsg.info('${tk:lang("属性已存在","")}');
					}
				}
			},
			onCellRender : function(td) {
				if (td.data && td.data['uuid']) {
					var uuid = td.data['uuid'];
					if (uuid) {
						$.getJSON('${pageContext.request.contextPath}/module/process/attribute/get/' + uuid, (function(uuid) {
							return function(data) {
								if (data) {
									var html;
									data.data = td.data['data'];
									data.value = td.data['value'];
									data.config = td.data['config'];
									if (data.editTemplate) {
										html = xdoT.renderWithHtml(data.editTemplate, data);
										td.setContent(html);
									} else {
										html = xdoT.render(data.inputPage, data);
										td.setContent(html);
									}
									td.getDataFuc = function(td) {
										var config = {
											'uuid' : data.attributeUuid
										};
										html.on('getData', function(event) {
											config.data = event.result;
										}).trigger('getData');
										html.on('getValue', function(event) {
											config.value = event.result;
										}).trigger('getValue');
										td.setData(config);
									};
								}
							};
						}(uuid)));
					}
				}
			}
		});
		$('.drag').on('dragstart', function(e) {
			e.originalEvent.dataTransfer.setData("uuid", $(this).data('uuid'));
		});

		$('#btnPreview').on('click', function() {
			var config = {};
			config['config'] = JSON.stringify(formbuilder.toJson(), null, 2);
			var html = xdoT.render('process.form.previewform', config);
			createSlideDialog({
				title : '预览',
				content : html,
				width : '90%'
			});
		});

		$('#btnMerge').on('click', function() {
			formbuilder.mergeRange();
		});

		$('#btnUnMerge').on('click', function() {
			formbuilder.separateRange();
		});

		$('#btnAddColumn').on('click', function() {
			formbuilder.addColumn();
		});

		$('#btnAddRow').on('click', function() {
			formbuilder.addRow();
		});

		$('#btnSave').on('click', function() {
			if ($('#mainForm').valid()) {
				$('#txtContent').val(JSON.stringify(formbuilder.toJson(), null, 2));
				console.log(JSON.stringify(formbuilder.toJson(), null, 2));
				$('#mainForm').ajaxSubmit({
					url : '${pageContext.request.contextPath}/module/process/form/save',
					type : 'POST',
					dataType : 'json',
					success : function(data) {
						if (data.Status == 'OK') {
							showPopMsg.success();
						} else {
							showPopMsg.error(data);
						}
					}
				})
			}
		});

		$('.radVersion').on('change', function() {
			if ($(this).prop('checked')) {
				if ($(this).val()) {
					$.getJSON('${pageContext.request.contextPath}/module/process/form/version/' + $(this).val(), function(data) {
						if (data && data.content) {
							formbuilder.drawTable(JSON.parse(data.content));
						}
					});
				} else {
					formbuilder.drawTable(10, 5);
				}
			}
		});

		$('.radVersion').each(function() {
			if ($(this).prop('checked') && $(this).val()) {
				$('.radVersion').trigger('change');
				return false;
			}
		});

		$('.tabForm').on('shown.bs.tab', function(e) {
			var target = e.target.toString().substr(e.target.toString().indexOf('#') + 1);
			if (target == 'tabPreview') {
				var formpreviewer = $('#divPreviewContent').data('sheet');
				if (!formpreviewer) {
					formpreviewer = $('#divPreviewContent').SheetBuilder({
						readonly : true,
						onCellRender : function(td) {
							if (td.data && td.data['uuid']) {
								var uuid = td.data['uuid'];
								if (uuid) {
									$.getJSON('${pageContext.request.contextPath}/module/process/attribute/get/' + uuid, (function(uuid) {
										return function(data) {
											if (data) {
												var html;
												data.data = td.data['data'];
												data.value = td.data['value'];
												if (data.editTemplate) {
													html = xdoT.renderWithHtml(data.editTemplate, data);
													td.setContent(html);
												} else {
													html = xdoT.render(data.inputPage, data);
													td.setContent(html);
												}
												td.getDataFuc = function(td) {
													var config = {
														'uuid' : data.attributeUuid
													};
													html.on('getData', function(event) {
														config.data = event.result;
													}).trigger('getData');
													html.on('getValue', function(event) {
														config.value = event.result;
													}).trigger('getValue');
													td.setData(config);
												};
											}
										};
									}(uuid)));
								}
							}
						}
					});
				}
				$('#divPreviewContent').data('sheet', formpreviewer);
				formpreviewer.drawTableReadonly(formbuilder.toJson());
			}
		});
	});
</script>
</head>
<body class="bg-grey">
	<div class="block-main form-container step-panel">
		<h6>${tk:lang("基本信息","") }</h6>
		<form method="post" id="mainForm" role="form" class="form-detail">
			<input type="hidden" name="uuid" value="${formVo.uuid}">
			<div class="form-block">
				<div class="block-left">${tk:lang("名称","") }：</div>
				<div class="block-right">
					<input type="text" class="input-xxlarge" name="name" check-type="required" value="${formVo.name}">
				</div>
			</div>
			<div class="form-block">
				<div class="block-left">${tk:lang("是否激活","") }：</div>
				<div class="block-right">
					<input type="radio" data-makeup="radio" class="radisActive" id="radisActive1" name="isActive" value="1" ${formVo.isActive == 1 || formVo.isActive == null ? "checked" : ""}>
					${tk:lang("激活","") }
					<input type="radio" data-makeup="radio" class="radisActive" id="radisActive0" name="isActive" value="0" ${formVo.isActive == 0 ? "checked" : ""}>
					${tk:lang("禁用","") }
				</div>
			</div>
			<div class="form-block">
				<div class="form-block">
					<div class="block-left">${tk:lang("选择版本","") }：</div>
					<div class="block-right">
						<c:set var="hasActiveVersion" value="false"></c:set>
						<c:forEach items="${formVo.versionList}" var="version">
							<if test="${version.isActive == 1}"> <c:set var="hasActiveVersion" value="true"></c:set></if>
							<input class="radVersion" name="activeVersionUuid" ${version.isActive == 1 ?"checked":""} type="radio" data-makeup="radio" value="${version.uuid}">
									Ver.${version.version}
							</c:forEach>
						<input class="radVersion" name="activeVersionUuid" type="radio" ${!hasActiveVersion?"checked":"" } data-makeup="radio" value="">
						${tk:lang("新建版本","") }
					</div>
				</div>
			</div>
			<h6>${tk:lang("编辑表单","") }</h6>
			<table style="width: 100%">
				<tr>
					<td style="vertical-align: top; width: 120px">
						<c:forEach items="${attributeList}" var="attribute">
							<div class="drag" draggable="true" data-uuid="${attribute.uuid }">
								<i class="glyphicon glyphicon-font"></i>${attribute.label }
							</div>
						</c:forEach>
					</td>
					<td>
						<div>
							<ul class="nav-bdbottom nav-tabs">
								<li class="active">
									<a href="#tabEdit" data-toggle="tab" class="tabForm" id="linkEdit">${tk:lang("编辑","") }</a>
								</li>
								<li>
									<a href="#tabPreview" data-toggle="tab" class="tabForm" id="linkPreview">${tk:lang("预览","") }</a>
								</li>
							</ul>
							<div class="tab-content">
								<div class="tab-pane active in" id="tabEdit">
									<textarea id="txtContent" name="content" style="display: none"></textarea>
									<div id="divContent"></div>
									<div class="btn-group" style="margin-top: 10px">
										<button id="btnAddColumn" class="btn btn-default" type="button">${tk:lang("添加列","") }</button>
										<button id="btnAddRow" class="btn btn-default" type="button">${tk:lang("添加行","") }</button>
										<button id="btnMerge" class="btn btn-default" type="button">${tk:lang("合并单元格","") }</button>
										<button id="btnUnMerge" class="btn btn-default" type="button">${tk:lang("分拆单元格","") }</button>
									</div>
								</div>
								<div class="tab-pane" id="tabPreview">
									<div id="divPreviewContent"></div>
								</div>
							</div>
						</div>

					</td>
				</tr>
			</table>
	</div>
	<div class="btn-bar" style="z-index: 800;">
		<div class="btn-group">
			<button id="btnPreview" class="btn btn-default" type="button">${tk:lang("预览","") }</button>
			<c:if test="${param.formId != null}">
				<button type="button" class="btn btn-default" id="btnSave">${tk:lang("覆盖当前版本","")}</button>
				<button type="button" class="btn btn-default" id="btnNewSave">${tk:lang("另存为新版本","")}</button>
			</c:if>
			<c:if test="${param.formId == null}">
				<button type="button" class="btn btn-primary" id="btnSave">${tk:lang("保存","")}</button>
			</c:if>
		</div>
	</div>
</body>
</html>