<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_attribute_{{=it.attributeUuid}}" class="form_attribute" data-uuid="{{=it.attributeUuid}}">
	<select name="attribute_{{=it.attributeUuid}}" id="attribute_{{=it.attributeUuid}}" plugin-checkselect>
		{{?it.data && it.data['value']}}
		<option value="{{=it.data['value']}}" selected>{{=it.data['text']}}</option>
		{{?}}
	</select>
	{{?it.config && it.config['isRequired']}}<i style="color: red">*</i>{{?}}
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			$.getJSON('${pageContext.request.contextPath}/module/process/attribute/{{=it.attributeUuid}}/data', function(data) {
				var oldValueList = new Array();
				$('#attribute_{{=it.attributeUuid}}', that).find('option:selected').each(function() {
					oldValueList.push({
						'value' : $(this).val(),
						'text' : $(this).text()
					});
				});
				$('#attribute_{{=it.attributeUuid}}', that).loadSelectWithDefault(data);
				$('#attribute_{{=it.attributeUuid}}', that).find('option').each(function() {
					for (var i = 0; i < oldValueList.length; i++) {
						if ($(this).val() == oldValueList[i].value) {
							$(this).prop('selected', true);
							return false;
						}
					}
				});
				if ($('#attribute_{{=it.attributeUuid}}', that)[0].checkselect) {
					$('#attribute_{{=it.attributeUuid}}', that)[0].checkselect.reload();
				}
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
							returnValue = $(this).val();
							return false;
						}
					});
					return returnValue;
				}
			}
		};
	</script>
</form>