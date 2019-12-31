<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/x-template" id="tmpCatalogPath">
<div>
	{{?typeof it.parents != 'undefined' && it.parents != null && it.parents.length > 0}}
	<ol class="breadcrumb">
		{{~it.parents : item : index}} {{?it.parents.length - 1 == index}}
		<li class="active">{{=item.name}}</li>
		{{??}}
		<li>
			<a href="javascript:void(0);" class="catalog" catalog_id="{{=item.id}}">{{=item.name}}</a>
		</li>
		{{?}} {{~}}
	</ol>
	{{?}}
</div>
</script>