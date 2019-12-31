<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<tr class="trProcessAttribute">
	<%--<td>
		<input type="text" class="txtProcessAttributeName">
		
	</td>--%>
	<td>
		<input type="text" class="txtProcessAttributeLabel" value="{{=it.label}}">
		<input type="hidden" class="hidProcessAttributeUuid" value="{{=it.attributeUuid}}">
	</td>
	<%-- <td>
		<select class="sltProcessAttributeWidth" plugin-checkselect data-width="80px" data-searchable="false">
			<option value="1">100%</option>
			<option value="2">50%</option>
		</select>
	</td>--%>
	<td>
		{{=it.typeName}}
		<input type="hidden" class="hidProcessAttributeTypeName" value="{{=it.typeName}}">
	</td>
	<td>
		{{=it.handlerName}}
		<input type="hidden" class="hidProcessAttributeHandlerName" value="{{=it.handlerName}}">
		<input type="hidden" class="hidProcessAttributeHandler" value="{{=it.handler}}">
	</td>
	<td>
		<button class="btn btn-sm btn-default btnDelAttribute" type="button">
			<i class="ts-minus"></i>
		</button>
	</td>
	<script class="xdotScript">
		var fn = {
			'.btnDelAttribute' : {
				'click' : function() {
					$(this).closest('tr').remove();
				}
			}
		};
	</script>
</tr>
