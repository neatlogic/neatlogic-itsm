<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div>
	{{?it.attributeList && it.attributeList.length > 0}}
	<table class="table table-hover">
		<thead>
			<tr class="noborder">
				<th style="width: 60px"></th>
				<th>${tk:lang("属性名称","") }</th>
				<th>${tk:lang("类型","") }</th>
				<th>${tk:lang("描述","") }</th>
				<th></th>
			</tr>
		</thead>
		<tbody id="tbAttribute">
			{{~it.attributeList:attr:index}}
			<tr>
				<td>
					<input type="checkbox" class="chkAttribute" data-makeup="checkbox" value="{{=attr.uuid}}">
				</td>
				<td>{{=attr.label}}</td>
				<td>{{=attr.handlerName}}</td>
				<td>{{=attr.description||'-'}}</td>
				<td></td>
			</tr>
			{{~}}
		</tbody>
	</table>
	<div style="text-align: right">{{=pagination.number(it, 'javascript:searchAttribute($page)', true)}}</div>
	{{??}}
	<div style="text-align: center;">
		<div class="nodatatips">
			<div class="nodataImg">
				<i class="ts ts-nodata"></i>
			</div>
			<div class="nodataTxt">${tk:lang('找不到符合条件的属性','') }</div>
		</div>
	</div>
	{{?}}
</div>