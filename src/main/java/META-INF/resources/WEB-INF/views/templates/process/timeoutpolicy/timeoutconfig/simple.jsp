<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form class="form_timeoutpolicy">
	<div>
		<span style="margin-right: 10px">
			<select class="sltProcessTaskAttribute" plugin-checkselect check-type="required" data-value="{{?it.config}}{{=it.config.uuid||''}}{{?}}">
			</select>
		</span>
		<span style="margin-right: 10px">${tk:lang("等于","") }</span>
		<span>
			<input type="text" class="txtTargetValue" check-type="required" value="{{?it.config}}{{=it.config.targetvalue||''}}{{?}}">
		</span>
	</div>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			var userData = Paper.getUserData();
			if (userData && userData['attributeList']) {
				var defaultValue = $('.sltProcessTaskAttribute', that).data('value');
				for (var i = 0; i < userData['attributeList'].length; i++) {
					var attr = userData['attributeList'][i];
					var option = $('<option value="'+attr['uuid']+'">' + attr['label'] + '</option>');
					if (attr['uuid'] == defaultValue) {
						option.prop('selected', true);
					}
					$('.sltProcessTaskAttribute', that).append(option);
				}
				if ($('.sltProcessTaskAttribute', that)[0].checkselect) {
					$('.sltProcessTaskAttribute', that)[0].checkselect.reload();
				}
			}
		};
		var fn = {
			'this' : {
				'getConfig':function() {
					var that = this.root;
					var returnValue = {};
					returnValue['uuid'] = $('.sltProcessTaskAttribute', that).val();
					returnValue['targetvalue'] = $('.txtTargetValue', that).val();
					return returnValue;
				}
			}
		}
	</script>
</form>