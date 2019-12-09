<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div class="clearfix">
	{{?it.attributeList && it.attributeList.length > 0}} {{~it.attributeList:attr:index}}
	<div style="width: 25%; margin: 5px; border: 1px solid #ddd; border-radius: 3px" class="d_f">
		<input type="checkbox" class="chkProcessAttribute" data-makeup="checkbox" value="{{=attr.uuid}}">
		{{=attr.label}}
	</div>
	{{~}} {{??}}
	<div style="text-align: center;">
		<div class="nodatatips">
			<div class="nodataImg">
				<i class="ts ts-nodata"></i>
			</div>
			<div class="nodataTxt">${tk:lang('请现在流程中添加自定义属性','') }</div>
		</div>
	</div>
	{{?}}
</div>