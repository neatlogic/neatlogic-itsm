<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div class="d_f divAttributeItem" {{?it.width== '2'}}style="width: 50%" {{??}}style="width:100%"{{?}}>
	<div class="well well-alert well-sm" style="margin: 0 10px 10px 0">
		<input type="hidden" class="attribute_uuid" value="{{=it.uuid}}">
		<div class="clearfix">
			<div class="d_f">
				<h6 class="text-lighten">{{=it.label}}：</h6>
			</div>
			<div class="d_f_r">
				<input type="checkbox" data-makeup="checkbox" class="chkIsEditable" {{?it.isEditable && it.isEditable=='1'}}checked{{?}}>
				<input type="hidden" class="attribute_iseditable" value="{{?it.isEditable && it.isEditable=='1'}}1{{??}}0{{?}}">
				允许编辑
				<%--<input type="checkbox" data-makeup="checkbox" class="chkIsDisplay" {{?it.isDisplay && it.isDisplay=='1'}}checked{{?}}>
				<input type="hidden" class="attribute_isdisplay" value="{{?it.isDisplay && it.isDisplay=='1'}}1{{??}}0{{?}}">
				允许显示 
				<input type="checkbox" data-makeup="checkbox" class="chkIsRequired" {{?it.isRequired && it.isRequired=='1'}}checked{{?}}>
				<input type="hidden" class="attribute_isrequired" value="{{?it.isRequired && it.isRequired=='1'}}1{{??}}0{{?}}">
				是否必填--%>
				<i class="ts-cog btnConfig" id="btnConfig{{=it.uuid}}" data-uuid="{{=it.uuid}}" style="cursor: pointer; display: none"></i> <i class="ts-trash btnDel" data-uuid="{{=it.uuid}}" style="cursor: pointer"></i>
			</div>
		</div>
		<div class="divAttributeHandler" data-uuid="{{=it.uuid}}"></div>
		<input type="hidden" value="{{=it.handler}}" class="hidAttributeHandler">
		<textarea class="txtAttributeData attribute_data" style="display: none">
		{{?it.data}}
		{{?it.data instanceof Object || it.data instanceof Array}}
		{{=JSON.stringify(it.data,null,2)}}
		{{??}}
		{{=it.data}}
		{{?}}
		{{?}}
		</textarea>
		<textarea class="txtAttributeConfig attribute_config" id="txtAttributeConfig{{=it.uuid}}" style="display: none">
		{{?it.config}}
		{{?it.config instanceof Object || it.config instanceof Array}}
		{{=JSON.stringify(it.config,null,2)}}
		{{??}}
		{{=it.config}}
		{{?}}
		{{?}}
		</textarea>
	</div>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var uuid = $('.attribute_uuid', that).val();
			var container = $('.divAttributeHandler', that);
			var isEditable = $('.attribute_iseditable', that).val();
			var attrdata = $('.attribute_data', that).val();
			var attrconfig = $('.attribute_config', that).val();
			$.getJSON('${pageContext.request.contextPath}/module/process/attribute/get/' + uuid, function(data) {
				if (data) {
					if (attrdata && typeof attrdata == 'string') {
						data.data = JSON.parse(attrdata);
					}
					if (attrconfig && typeof attrconfig == 'string') {
						data.config = JSON.parse(attrconfig);
					}
					if (data.configTemplate) {
						$('#btnConfig' + uuid, that).data('configtemplate', data.configTemplate);
						$('#btnConfig' + uuid, that).show();
					} else if (data.configPage) {
						$('#btnConfig' + uuid, that).data('configpage', data.configPage);
						$('#btnConfig' + uuid, that).show();
					}
					if (data.editTemplate) {
						var html = xdoT.renderWithHtml(data.editTemplate, data);
						container.html(html);
					} else {
						var html = xdoT.render(data.inputPage, data);
						container.html(html);
					}
				}
			});
		};
		var fn = {
			'.chkIsEditable' : {
				'change' : function() {
					var that = this.root;
					if ($(this).prop('checked')) {
						$('.attribute_iseditable', that).val(1);
					} else {
						$('.attribute_iseditable', that).val(0);
					}
				}
			},
			'.chkIsDisplay' : {
				'change' : function() {
					var that = this.root;
					if ($(this).prop('checked')) {
						$('.attribute_isdisplay', that).val(1);
					} else {
						$('.attribute_isdisplay', that).val(0);
					}
				}
			},
			'.chkIsRequired' : {
				'change' : function() {
					var that = this.root;
					if ($(this).prop('checked')) {
						$('.attribute_isrequired', that).val(1);
					} else {
						$('.attribute_isrequired', that).val(0);
					}
				}
			},
			'.btnDel' : {
				'click' : function() {
					var attributeUuid = $(this).data('uuid');
					var nodeUuid = $('#hidProcessStepUuid').val();
					var node = Paper.getNodeById(nodeUuid);
					if (node && node.getUserData()) {
						processStepAttributeList = node.getUserData()['attributeList'];
						for (var i = 0; i < processStepAttributeList.length; i++) {
							if (processStepAttributeList[i].uuid == attributeUuid) {
								processStepAttributeList.splice(i, 1);
								break;
							}
						}
					}
					$(this).closest('.divAttributeItem').remove();
				}
			},
			'.btnConfig' : {
				'click' : function() {
					var that = this.root;
					var uuid = $(this).data('uuid');
					var config = $('#txtAttributeConfig' + uuid, that).val();
					var data = {
						'attributeUuid' : uuid
					};
					if ($.trim(config)) {
						data['config'] = JSON.parse($.trim(config));
					}
					var html = null;
					if ($(this).data('configtemplate')) {
						html = xdoT.renderWithHtml($(this).data('configtemplate'), data);
					} else if ($(this).data('configpage')) {
						html = xdoT.render($(this).data('configpage'), data);
					}

					if (html) {
						createSlideDialog({
							title : '${tk:lang("设置","")}',
							content : html,
							successFuc : function() {
								if (html.valid()) {
									html.on('getConfig', function(event) {
										if (event.result) {
											$('#txtAttributeConfig' + uuid, that).val(JSON.stringify(event.result, null, 2));
										} else {
											$('#txtAttributeConfig' + uuid, that).val('');
										}
									}).trigger('getConfig');
								}
							}
						});
					}
				}
			}
		}
	</script>
</div>


