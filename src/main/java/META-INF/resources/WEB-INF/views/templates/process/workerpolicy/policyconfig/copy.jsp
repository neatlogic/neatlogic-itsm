<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_workerpolicy_{{=it.policy}}">
	<div class="divCopyContainer">
		<div class="help-block">${tk:lang("请选择需要复制处理人的前置步骤","") }</div>
		<div>
			<select class="sltPrevStep" plugin-checkselect data-searchable="false" data-value="{{?it.config}}{{=it.config.prevId||''}}{{?}}"></select>
			<input type="hidden" class="hidPrevNodeIds" value="{{=it.prevNodeIds}}">
		</div>
	</div>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var prevNodeIds = $('.hidPrevNodeIds', that).val();
			if (prevNodeIds) {
				var nodeIds = prevNodeIds.split(',');
				for (var i = 0; i < nodeIds.length; i++) {
					if (nodeIds[i]) {
						var node = Paper.getNodeById(nodeIds[i]);
						if (node) {
							var option = $('<option value="' + nodeIds[i] + '">' + node.getLabel() + '</option>');
							if (nodeIds[i] == $('.sltPrevStep', that).data('value')) {
								option.prop('selected', true);
							}
							$('.sltPrevStep', that).append(option);
						}
					}
				}
				if ($('.sltPrevStep', that)[0].checkselect) {
					$('.sltPrevStep', that)[0].checkselect.reload();
				}
			} else {
				$('.divCopyContainer', that).empty().text('没有找到任何前置步骤');
			}
		};
		var fn = {
			'this' : {
				'getConfig' : function() {
					var that = this.root;
					var prev = {};
					$('.sltPrevStep', that).find('option:selected').each(function() {
						prev['prevId'] = $(this).val();
						return false;
					});
					return prev;
				}
			}
		}
	</script>
</form>