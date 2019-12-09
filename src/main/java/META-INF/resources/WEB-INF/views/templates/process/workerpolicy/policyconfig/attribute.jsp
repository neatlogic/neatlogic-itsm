<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_workerpolicy_{{=it.policy}}">
	<div class="help-block">${tk:lang("支持类型是单个处理人、多个处理人、单个处理组和多个处理组的属性","") }</div>
	<div>
		<select class="sltAssignAttribute" plugin-checkselect multiple>
		</select>
		{{?it.config && it.config.length >0}} {{~it.config:uuid:index}}
		<input type="hidden" class="hidAttrUuid" value="{{=uuid}}">
		{{~}} {{?}}
	</div>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var userData = Paper.getUserData();
			if (userData['attributeList'] && userData['attributeList'].length > 0) {
				for (var i = 0; i < userData['attributeList'].length; i++) {
					var attr = userData['attributeList'][i];
					if (attr.handler == 'user' || attr.handler == 'muser' || attr.handler == 'team' || attr.handler == 'mteam') {
						var option = $('<option value="'+attr.uuid+'">' + attr.label + '</option>');
						$('.hidAttrUuid', that).each(function() {
							if (attr.uuid == $(this).val()) {
								option.prop('selected', true);
							}
						});
						$('.sltAssignAttribute', that).append(option);
					}
				}
				if ($('.sltAssignAttribute', that)[0].checkselect) {
					$('.sltAssignAttribute', that)[0].checkselect.reload();
				}
			}
		};
		var fn = {
			'this' : {
				'getConfig' : function() {
					var that = this.root;
					var assign = new Array();
					$('.sltAssignAttribute', that).find('option:selected').each(function() {
						assign.push($(this).val());
					});
					return assign;
				}
			}
		};
	</script>
</form>