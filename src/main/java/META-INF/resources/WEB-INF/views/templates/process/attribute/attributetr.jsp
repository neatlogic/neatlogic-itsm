<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<tr>
	<td>
		{{=it.label}}[{{=it.name}}]{{?it.typeName}}
		<span style="border: 1px solid #ddd">{{=it.typeName}}</span>
		{{?}}
		<input type="hidden" class="attributeName" value="{{=it.name}}">
	</td>
	<td>{{=it.handlerName}}</td>
	<td>
		<select class="attributeIsRequired" plugin-checkselect data-width="60px" data-searchable="false">
			<option value="1" {{=it.isRequired=='1'?'selected':''}}>${tk:lang("是","")}</option>
			<option value="0" {{=!it.isRequired || it.isRequired=='0'?'selected':''}}>${tk:lang("否","")}</option>
		</select>
	</td>
	<td>
		<div class="divAttributeHandler" data-attributename="{{=it.name}}"></div>
		<textarea style="display: none" class="txtAttributeValue">{{=it.value||''}}</textarea>
	</td>

	<td>
		<i class="ts-long-arrow-up"></i><i class="ts-long-arrow-down"></i>
	</td>
	<td>
		<button type="button" class="btnAttributeScript btn btn-xs" data-attributename="{{=it.name}}">${tk:lang("脚本","") }</button>
		<textarea class="txtAttributeScript" style="display: none"></textarea>
	</td>
	<td></td>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			$('.divAttributeHandler', that).each(function() {
				var attrname = $(this).data('attributename');
				$.getJSON('${pageContext.request.contextPath}/module/process/attribute/get/' + attrname, (function(container) {
					return function(data) {
						data.value = $('.txtAttributeValue', that).val();
						var html = xdoT.render('process.attribute.handler.' + data.handler, data);
						container.empty().html(html);
					};
				}($(this))));
			});
		};
		var fn = {
			'.btnAttributeScript' : {
				'click' : function() {
					
				}
			}
		};
	</script>
</tr>