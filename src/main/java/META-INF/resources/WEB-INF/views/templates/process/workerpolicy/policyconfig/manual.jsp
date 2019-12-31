<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_workerpolicy_{{=it.policy}}">
	<div class="help-block">请选择处理人范围</div>
	<div>
		<select plugin-inputselect class="sltManualUser" data-url="${pageContext.request.contextPath}/module/process/process/searchuserandteam" multiple>
			{{?it.config && it.config.length >0}} {{~it.config:user:index}}
			<option value="{{=user.userid}}" selected>{{=user.username}}</option>
			{{~}} {{?}}
		</select>
	</div>
	<script class="xdotScript">
		var fn = {
			'this' : {
				'getConfig' : function() {
					var that = this.root;
					var assign = new Array();
					$('.sltManualUser', that).find('option:selected').each(function() {
						var user = {};
						user['userid'] = $(this).val();
						user['username'] = $(this).text();
						assign.push(user);
					});
					return assign;
				}
			}
		};
	</script>
</form>