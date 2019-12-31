<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div>
	<form id="formProcessForm">
		<select name="formId" id="sltForm">
			<option value="">${tk:lang("请选择表单","") }...</option>
			{{?it.formId}}
			<option value="{{=it.formId}}" selected></option>
			{{?}}
		</select>
		<div id="divSheet"></div>
		<script class="xdotScript">
			var loadFn = function() {
				var that = this;
				$('#sltForm', that).inputselect({
					url : "${pageContext.request.contextPath}/module/process/form/searchactiveform",
					param : "name",
					valueKey : "uuid",
					textKey : "name"
				});
			};
			var fn = {
				'#sltForm' : {
					'change' : function() {
						var that = this.root;
						var uuid = $(this).val();
						if (uuid) {
							$.getJSON('${pageContext.request.contextPath}/module/process/form/' + uuid, function(data) {
								if (data.content) {
									var formbuilder = $('#divSheet', that).SheetBuilder({
										readonly : true,
										data : JSON.parse(data.content),
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
							});
						} 
					}
				}
			};
		</script>
	</form>
</div>