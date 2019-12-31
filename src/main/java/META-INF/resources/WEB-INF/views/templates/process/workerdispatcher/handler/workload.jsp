<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form>
	<div class="help-block">${tk:lang("请选择分配范围","") }</div>
	<div>
		<select class="sltTeamUser" multiple plugin-inputselect data-url="${pageContext.request.contextPath}/user/searchuserandteam">
			{{?it.config && it.config.length > 0}} {{~it.config:c:index}}
			<option value="{{=c.value}}" selected>{{=c.text}}</option>
			{{~}} {{?}}
		</select>
	</div>
	<script class="xdotScript">
		var fn = {
			'this' : {
				'getConfig' : function() {
					var that = this;
					var returnList = new Array();
					$('.sltTeamUser', that).find('option:selected').each(function() {
						var data = {};
						data.value = $(this).val();
						data.text = $(this).text();
						returnList.push(data);
					});
					return returnList;
				}
			}
		}
	</script>
</form>