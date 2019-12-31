<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" deferredSyntaxAllowedAsLiteral="true"%>
<script type="text/x-template" id="tmpTaskRelation">
{{?it && it != null && it.length > 0}}
<div  style="margin:0;">
	{{~it:relation:index}}
	<div style="text-align:left;width:100%;border-top:1px solid #ccc;height:30px;line-height:30px;">
		<div style="padding-left:10px;float:left;width:200px;">{{=relation.name}} {{?relation.taskCount >0}}<img title="查看关联请求" class="imgSlide" src="/balantflow/resources/images/down.gif"  style="cursor:pointer;" mode="del" rtype="{{=relation.id}}" onclick="addRelation(this);"/>{{?}}</div>
		<div style="float:left;width:100px;"><span style="font-weight:normal;" class="span_{{=relation.id}} label label-primary">{{=relation.taskCount}}</span></div>
		<div style="text-align:left;float:left;"><button title="添加关联请求" type="button" class="btn btn-xs btn-success"  mode="add" rtype="{{=relation.id}}" onclick="addRelation(this);">添加</button></div>
	</div>
	<div style="display:none;width:100%;" id="trDel_{{=relation.id}}" class="trDel">
		<div id="tdDel_{{=relation.id}}"></div>
	</div>
	<div style="display:none;width:100%;border-top:1px solid #ccc;padding:2px 2px;" id="trAdd_{{=relation.id}}" class="trAdd">
	  			<input type="text"  name="keyword" id="keyword_{{=relation.id}}" class="form-control input-sm" placeholder="输入id或标题" value=""/> 
        		<div class="btn-group" role="group"><button class="btn btn-default btn-sm btnSearchRelation" type="button" onclick="searchRelation(this);" relationType="{{=relation.id}}"><i class="icon-search icon-grey"></i></button>
      			<button type="button"  class="btn btn-default btn-sm btnSaveRelation" onclick="saveRelation(this);" relationType="{{=relation.id}}"><i class="icon-ok icon-grey"></i></button>
</div>
			<div id="taskDiv_{{=relation.id}}"></div>
	</div>
	{{~}}
</div>
{{??}}
<div class="alert alert-default"><b>提示：</b>请增加可用的关联类型。</div>
{{?}}
</script>