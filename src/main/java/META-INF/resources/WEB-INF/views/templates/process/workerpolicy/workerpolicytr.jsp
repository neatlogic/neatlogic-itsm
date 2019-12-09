<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<tr>
	<td>
		<input type="hidden" class="hidWorkerPolicy workerpolicy_policy" value="{{=it.policy}}">
		<input type="hidden" class="hidWorkerPolicy workerpolicy_policyname" value="{{=it.policyName}}">
		<input type="hidden" class="hidPrevNodeIds" value="{{=it.prevNodeIds}}">
		{{=it.policyName}}
	</td>
	<td class="tdPolicyConfig">
		<textarea class="txtWorkerPolicyConfig workerpolicy_config" style="display: none">{{?it.config}}{{=JSON.stringify(it.config,null,2)}}{{?}}</textarea>
	</td>
	<td>
		<button type="button" class="btn btn-xs btn-default btnDelPolicy">
			<i class="ts-trash"></i>
		</button>
	</td>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var policy = $('.hidWorkerPolicy', that).val();
			var config = $('.txtWorkerPolicyConfig', that).val();
			if ($.trim(config)) {
				config = JSON.parse(config);
			} else {
				config = {};
			}
			var data = {};
			data.prevNodeIds = $('.hidPrevNodeIds', that).val();
			data.config = config;
			data.policy = policy;
			var html = xdoT.render('process.workerpolicy.policyconfig.' + policy, data);
			$('.tdPolicyConfig', that).append(html);
		};
		var fn = {
			'.btnDelPolicy' : {
				'click' : function() {
					var tbody = $(this).closest('tbody');
					var table = $(this).closest('table');
					$(this).closest('tr').remove();
					if (tbody.children().length == 0) {
						table.hide();
					}
				}
			}
		}
	</script>
</tr>