<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form class="form_step" data-uuid="{{=it.processStepUuid}}" id="form_{{=it.processStepUuid}}">
	<input type="hidden" value="{{=it.id||''}}" class="hidProcessStepId">
	<input type="hidden" value="{{=it.processStepUuid}}" class="hidProcessStepUuid">
	<input type="hidden" value="{{=it.processTaskId||''}}" class="hidProcessTaskId">
	<input type="hidden" value="{{=it.isActive}}" class="hidProcessStepIsActive">
	{{?it.attributeList && it.attributeList.length >0}}
	<h6>属性：</h6>
	<div id="divProcessStepAttribute" style="padding-top: 8px" class="clearfix">
		{{~it.attributeList:attr:index}}
		<div class="d_f divAttributeItem" {{?attr.width== '2'}}style="width: 50%" {{??}}style="width:100%"{{?}}>
			<div class="well well-alert well-sm" style="margin: 0 10px 10px 0">
				<input type="hidden" class="attribute_uuid" value="{{=attr.attributeUuid}}">
				<div>
					<label>{{=attr.label}}：</label>
				</div>
				<div class="divAttributeHandler" data-attributeuuid="{{=attr.attributeUuid}}"></div>
			</div>
		</div>
		{{~}}
	</div>
	{{?}} {{?it.formUuid}}
	<h6>表单：</h6>
	<div id="divProcessStepForm" style="padding-top: 8px" class="clearfix">
		<input type="hidden" value="{{=it.formUuid}}" id="hidFormUuid">
		<div id="divFormContainer"></div>
	</div>
	{{?}}
	<div>
		<textarea id="txtContent" name="content">{{=it.content||''}}</textarea>
	</div>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var processTaskId = $('.hidProcessTaskId', that).val();
			var processStepId = $('.hidProcessStepId', that).val();
			var processStepUuid = $('.hidProcessStepUuid', that).val();
			var isActive = $('.hidProcessStepIsActive', that).val();
			$('.divAttributeHandler', that).each(function(k, i) {
				var handler = $(this).data('handler');
				var attributeuuid = $(this).data('attributeuuid');
				var url = '';
				if (!processStepId) {
					url = '${pageContext.request.contextPath}/module/process/process/processstep/' + processStepUuid + '/attribute/' + attributeuuid;
				} else {
					url = '${pageContext.request.contextPath}/module/process/processtask/processtaskstep/' + processStepId + '/attribute/' + attributeuuid;
				}
				$.getJSON(url, (function(container) {
					return function(data) {
						if (data) {
							var config = $('#txtAttributeConfig' + container.data('attributeuuid')).val();
							if (data.config && $.trim(data.config)) {
								data.config = JSON.parse(data.config);
							}
							if (data.data && $.trim(data.data)) {
								data.data = JSON.parse(data.data);
							}
							if (isActive == '1' && data.isEditable == 1) {
								if (data.editTemplate) {
									var html = xdoT.renderWithHtml(data.editTemplate, data);
									container.html(html);
								} else {
									var html = xdoT.render(data.inputPage, data);
									container.html(html);
								}
							} else {
								if (data.viewTemplate) {
									var html = xdoT.renderWithHtml(data.viewTemplate, data);
									container.html(html);
								} else {
									var html = xdoT.render(data.viewPage, data);
									container.html(html);
								}
							}
						}
					};
				}($(this))));
			});

			if ($('#hidFormUuid', that).length > 0) {
				var formurl = '';
				if (!processTaskId) {
					formurl = '${pageContext.request.contextPath}/module/process/form/' + $('#hidFormUuid', that).val()
				} else {
					formurl = '${pageContext.request.contextPath}/module/process/processtask/' + processTaskId + '/getform';
				}
				$.getJSON(formurl, function(data) {
					var sheet = $('#divFormContainer', that).SheetBuilder({
						data : JSON.parse(data.content),
						readonly : true,
						onCellRender : function(td) {
							if (td.data && td.data['uuid']) {
								var uuid = td.data['uuid'];
								if (uuid) {
									var url = '';
									if (!processStepId) {
										url = '${pageContext.request.contextPath}/module/process/process/step/' + processStepUuid + '/formattribute/' + uuid;
									} else {
										url = '${pageContext.request.contextPath}/module/process/processtask/step/' + processStepId + '/formattribute/' + uuid;
									}
									$.getJSON(url, (function(uuid) {
										return function(data) {
											if (data && !$.isEmptyObject(data)) {
												var html;
												if (isActive == '1' && data.isEditable) {
													if (data.editTemplate) {
														html = xdoT.renderWithHtml(data.editTemplate, data);
													} else {
														html = xdoT.render(data.inputPage, data);
													}
												} else {
													if (data.viewTemplate) {
														html = xdoT.renderWithHtml(data.viewTemplate, data);
													} else {
														html = xdoT.render(data.viewPage, data);
													}
												}
												td.setContent(html);
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
											} else {
												td.setContent('没有权限查看控件');
											}
										};
									}(uuid)));
								}
							}
						}
					});
				});
			}

			setTimeout(function() {
				$('#txtContent', that).ckeditor({
					imageUploadUrl : '${pageContext.request.contextPath}/file/uploadFile.do?belong=FLOW'
				});
			}, 0);
		};
		var fn = {
			'this' : {
				'getConfig' : function(event) {
					var that = this;
					var attributeValueList = new Array();
					var formAttributeValueList = new Array();
					var returnObj = {};
					$('.form_attribute', $('#divProcessStepAttribute', that)).each(function() {
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

					$('.form_attribute', $('#divProcessStepForm', that)).each(function() {
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
						formAttributeValueList.push(attr);
					});

					returnObj['content'] = $('#txtContent', that).val();
					if (attributeValueList.length > 0) {
						returnObj['attributeValueList'] = attributeValueList;
					}
					if (formAttributeValueList.length > 0) {
						returnObj['formAttributeValueList'] = formAttributeValueList;
					}
					return returnObj;
				}
			}
		};
	</script>
</form>