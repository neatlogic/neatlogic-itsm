<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_attribute_{{=it.attributeUuid}}" class="form_attribute" data-uuid="{{=it.attributeUuid}}">
	<select name="attribute_{{=it.attributeUuid}}" id="attribute_{{=it.attributeUuid}}" multiple>
		{{?it.data && it.data['value']}}
		<option value="{{=it.data['value']}}" selected>{{=it.data['text']}}</option>
		{{?}}
	</select>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			$.getJSON('${pageContext.request.contextPath}/module/process/attribute/{{=it.attributeUuid}}/data', function(data) {
				$('#attribute_{{=it.attributeUuid}}', that).loadselect(data);
			});
		};
		var fn = {
			'this' : {
				'getData' : function() {
					var that = this;
					var returnVal = new Array();
					$('#attribute_{{=it.attributeUuid}}', that).find('option').each(function() {
						if ($(this).prop('selected')) {
							var obj = {};
							obj['value'] = $(this).val();
							obj['text'] = $(this).text();
							returnVal.push(obj);
						}
					});
					return returnVal;
				},
				'getValue' : function() {
					var that = this;
					var returnVal = new Array();
					$('#attribute_{{=it.attributeUuid}}', that).find('option').each(function() {
						if ($(this).prop('selected')) {
							returnVal.push($(this).val());
						}
					});
					return returnVal;
				}
			}
		};
	</script>
</form>