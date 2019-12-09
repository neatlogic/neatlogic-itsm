<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_attribute_{{=it.attributeUuid}}" class="form_attribute" data-uuid="{{=it.attributeUuid}}">
	<input type="text" class="input-large attributeValue" id="attribute_{{=it.attributeUuid}}" data-name="{{=it.name}}" value="{{=it.value||''}}">
	<script class="xdotScript">
		var fn = {
			'this' : {
				'getData' : function() {
					var that = this;
					return $('#attribute_{{=it.attributeUuid}}', that).val();
				},
				'getValue' : function() {
					var that = this;
					return $('#attribute_{{=it.attributeUuid}}', that).val();
				}
			}
		};
	</script>
</form>