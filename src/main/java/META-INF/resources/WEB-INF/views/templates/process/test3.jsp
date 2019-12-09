<form id="form_attributeconfig_{{=it.attributeUuid}}" class="form_attribute" data-uuid="{{=it.attributeUuid}}">
	<input type="checkbox" class="chkConfig" value="sip"{{?it.config && it.config.sip}}checked{{?}}>
	源ip
	<input type="checkbox" class="chkConfig" value="sport"{{?it.config && it.config.sport}}checked{{?}}>
	源端口
	<input type="checkbox" class="chkConfig" value="tip"{{?it.config && it.config.tip}}checked{{?}}>
	目标ip
	<input type="checkbox" class="chkConfig" value="tport"{{?it.config && it.config.tport}}checked{{?}}>
	目标端口
	<script class="xdotScript">
		var fn = {
			'this' : {
				'getConfig' : function() {
					console.log('asdfadf');
					var that = this.root;
					var configList = {};
					$('.chkConfig:checked', that).each(function() {
						configList[$(this).val()] = true;
					});
					return configList;
				}
			}
		}
	</script>
</form>