<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_attribute_{{=it.attributeUuid}}" class="form_attribute" data-uuid="{{=it.attributeUuid}}">
	<select name="attribute_{{=it.attributeUuid}}" id="attribute_{{=it.attributeUuid}}">
		{{?it.data && it.data['value']}}
		<option value="{{=it.data['value']}}" selected>{{=it.data['text']}}</option>
		{{?}}
	</select>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			$('#attribute_{{=it.attributeUuid}}', that).inputselect({
				url : '${pageContext.request.contextPath}/module/process/attribute/{{=it.attributeUuid}}/data',
				param : 'name'
			});
		};
		var fn = {
			'this' : {
				'getData' : function() {
					var that = this;
					var returnVal = {};
					$('#attribute_{{=it.attributeUuid}}', that).find('option').each(function() {
						if ($(this).prop('selected')) {
							returnVal['value'] = $(this).val();
							returnVal['text'] = $(this).text();
						}
					});
					return returnVal;
				},
				'getValue' : function() {
					var that = this;
					var returnValue = '';
					$('#attribute_{{=it.attributeUuid}}', that).find('option').each(function() {
						if ($(this).prop('selected')) {
							returnValue =  $(this).val();
							return false;
						}
					});
					return returnValue;
				}
			}
		};
	</script>
</form>