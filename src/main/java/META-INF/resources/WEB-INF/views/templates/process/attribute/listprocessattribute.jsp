<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div>
	{{?it.attrList && it.attrList.length > 0}} {{~it.attrList:attr:index}}
	<div {{?attr.isDisplay==0}}style="display:none"{{?}}>
		<h5>{{=attr.attribute.label}}</h5>
		<div>
			{{?attr.attribute.handler=='text'}}
			<input type="text" {{?attr.isEditable==0}}disabled{{?}} value="">
			{{??attr.attribute.handler=='select'}}
			<select plugin-checkselect>
				{{?attr.attribute.datacube && attr.attribute.datacube.datalist}} {{~attr.attribute.datacube.datalist:data:dindex}}
				<option value="{{=data.value}}">{{=data.text}}</option>
				{{~}} {{?}}
			</select>
			{{?}}
		</div>
	</div>
	{{~}} {{?}}
</div>