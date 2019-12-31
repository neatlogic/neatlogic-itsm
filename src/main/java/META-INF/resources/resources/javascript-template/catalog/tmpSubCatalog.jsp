<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script type="text/x-template" id="tmpSubCatalog">
<div>
	{{?typeof it.catalogs != 'undefined' && it.catalogs != null && it.catalogs.length > 0}}
	<ul class="channelUl">
		{{~it.catalogs : item : index}}
		<li>
			{{?item.type == "catalog"}}
			<a href="javascript:void(0);" style="display:inline-block" class="classTooltip" data-toggle="tooltip" title="{{?item.note}}{{=item.note}}{{?}}">
				<i class="icon-chevron-right icon-grey"></i><span class="catalog" catalog_id="{{=item.id}}">{{=item.name}}</span>
			</a>
			{{??}}
			<a href="${pageContext.request.contextPath}/catalog/taskDispatch.do?channelId={{=item.id}}&fromTaskId={{=it.fromtaskid||''}}" data-placement="right" style="display:inline-block" rel="tab" class="classTooltip" data-toggle="tooltip" title="{{?item.note}}{{=item.note}}{{?}}">
				<i class="pic-icon-small pic-icon-black"></i>{{=item.name}}
			</a>
			{{?}}
		</li>
		{{~}}
	</ul>
	{{??}}
	<div class="alert alert-default">
		<b>提示：</b>此目录为空。
	</div>
	{{?}}
</div>
</script>