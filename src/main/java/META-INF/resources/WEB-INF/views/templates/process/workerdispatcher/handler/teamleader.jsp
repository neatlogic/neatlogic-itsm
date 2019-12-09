<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form>
	<div class="help-block">${tk:lang("请选择分组","") }</div>
	<div>
		<select plugin-inputselect class="sltTeam" data-url="${pageContext.request.contextPath}/team/searchTeamByName.do">
			{{?it.config && it.config.value}}
			<option value="{{=it.config.value}}" selected>{{=it.config.text}}</option>
			{{?}}
		</select>
	</div>
	<script class="xdotScript">
		var fn = {
			'this' : {
				'getConfig' : function() {
					var that = this;
					var data = {};
					$('.sltTeam', that).find('option:selected').each(function() {
						data.value = $(this).val();
						data.text = $(this).text();
						return false;
					});
					return data;
				}
			}
		}
	</script>
</form>