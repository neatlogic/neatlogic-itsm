<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div>
	{{?it && it.length > 0}}
	<table class="table table-condensed" style="margin: 0px">
		<thead>
			<tr class="th-left noborder">
				<th nowrap>步骤名称</th>
				<th nowrap>变量类型</th>
				<th nowrap>变量名称</th>
			</tr>
		</thead>
		<tbody>
			{{~it:step:sindex}} {{~step.paramList:param:pindex}} {{?pindex==0}}
			<tr>
				<td rowspan="{{=step.paramList.length}}">{{=step.name}}</td>
				{{??pindex>0}}
			<tr>
				{{?}}
				<td>{{=param.type}}</td>
				<td>
					<span class="spnParam">
						<c:out value="\${" />
						{{=step.id}}.{{=param.key}}
						<c:out value="}" />
					</span>
				</td>

				{{~}}
			</tr>
			{{~}}
		</tbody>
	</table>
	<div class="help-block">帮助：点击变量名称可以复制</div>
	<script class="xdotScript">
		var fn = {
			".spnParam" : {
				"click" : function() {
					var $temp = $('<input>');
					$("body").append($temp);
					$temp.val($(this).text()).select();
					document.execCommand("copy");
					$temp.remove();
					showSnapMessage('复制成功');
				}
			}
		}
	</script>
	{{??}}
	<p class="form-control-static">前置节点没有任何返回值</p>
	{{?}}
</div>