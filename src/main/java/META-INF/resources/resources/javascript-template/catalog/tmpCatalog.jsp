<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/x-template" id="tmpCatalog">
<div class="container">
			<div class="col-sm-5 rootCatalogDiv">
				<div class="sidetabs-nav">
{{~it:catalog:index}}
	<a class="sidetabs-nav-tab classTooltip catalog" href="javascript:void(0);" data-toggle="tooltip" title="{{=catalog.desc}}" catalog_id="{{=catalog.id}}">
		<i class="fa fa-angle-double-right"></i>{{=catalog.name}}
	</a>
{{~}}
</div>
			</div>
			<div class="col-sm-7" id="divContainer"></div>
		</div>
</script>
