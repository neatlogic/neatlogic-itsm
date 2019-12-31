<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<tr>
	<td nowrap>
		<input type="hidden" class="hidTimeoutPolicy timeoutpolicy_policy" value="{{=it.policy}}">
		<input type="hidden" class="hidTimeoutPolicy timeoutpolicy_policyname" value="{{=it.policyName}}">
		<textarea class="txtTimeoutPolicyConfig timeoutpolicy_config" style="display: none">{{?it.config}}{{=JSON.stringify(it.config,null,2)}}{{?}}</textarea>

		{{=it.policyName}}
	</td>
	<td nowrap class="tdPolicyConfig"><%-- {{?it.policy == 'simple'}} {{#def.include("process.timeoutpolicy.timeoutconfig.simple")}} {{??it.policy == 'advanced'}} {{#def.include("process.timeoutpolicy.timeoutconfig.advanced")}} {{?}}--%></td>
	<td nowrap>
		<input type="number" class="form-control input-sm timeoutpolicy_time" value="{{=it.time||''}}" check-type="required">
	</td>
	<td nowrap>
		<button type="button" class="btn btn-xs btn-default btnDelPolicy">
			<i class="ts-trash"></i>
		</button>
	</td>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var policy = $('.hidTimeoutPolicy', that).val();
			var config = $('.txtTimeoutPolicyConfig', that).val();
			if ($.trim(config)) {
				config = JSON.parse(config);
			} else {
				config = {};
			}
			var data = {};
			data.config = config;
			data.policy = policy;
			var html = xdoT.render('process.timeoutpolicy.timeoutconfig.' + policy, data);
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