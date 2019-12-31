<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_attributeconfig_{{=it.attributeUuid}}">
	<div class="form-block form-block-12">
		<span class="block-left"> ${tk:lang("是否必填","") }：</span>
		<div class="block-right">
			<input type="checkbox" data-makeup="checkbox" class="chkIsRequired"{{?it.config.isRequired}}checked{{?}}>
		</div>
	</div>
	<script class="xdotScript">
		var fn = {
			'this' : {
				'getConfig' : function() {
					var that = this.root;
					var config = {};
					if ($('.chkIsRequired', that).prop('checked')) {
						config['isRequired'] = true;
					} else {
						config['isRequired'] = false;
					}
					return config;
				}
			}
		}
	</script>
</form>