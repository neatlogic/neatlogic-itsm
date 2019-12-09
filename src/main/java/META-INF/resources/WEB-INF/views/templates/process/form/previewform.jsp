<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div>
	<ul class="nav-bdbottom nav-tabs" role="tablist">
		<li role="presentation" class="active">
			<a href="#divMain" class="tabMain" aria-controls="divMain" role="tab" data-toggle="tab">${tk:lang("填写表单","") }</a>
		</li>
		<li role="presentation">
			<a href="#divTest" class="tabMain" aria-controls="divTest" role="tab" data-toggle="tab">${tk:lang("测试","") }</a>
		</li>
	</ul>
	<div class="tab-content">
		<div role="tabpanel" id="divMain" class="tab-pane active">
			<textarea id="txtConfig" style="display: none">{{=it.config||''}}</textarea>
		</div>
		<div role="tabpanel" class="tab-pane" id="divTest"></div>
	</div>


	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var sheetbuilder = $('#txtConfig', that).SheetBuilder({
				cols : 10,
				rows : 5,
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
	</script>
</div>