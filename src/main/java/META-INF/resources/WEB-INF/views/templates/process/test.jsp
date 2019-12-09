<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<div>
	<select plugin-checkselect id="sltEnv" multiple>
		<option value="dev" {{?it&&it['dev']}}selected{{?}}>开发环境</option>
		<option value="stg" {{?it&&it['stg']}}selected{{?}}>测试环境</option>
		<option value="dr" {{?it&&it['dr']}}selected{{?}}>容灾环境</option>
		<option value="prd" {{?it&&it['prd']}}selected{{?}}>生产环境</option>
	</select>
	<script class="xdotScript">
		var fn = {
			'this' : {
				'getConfig' : function() {
					var that = this;
					var datas = {};
					$('#sltEnv', that).find('option').each(function() {
						if ($(this).prop('selected')) {
							datas[$(this).val()] = true;
						}
					});
					return datas;
				},
				'getValue' : function() {
					var values = '';
					$('#sltEnv', that).find('option').each(function() {
						if ($(this).prop('selected')) {
							values += $(this).val() + ',';
						}
					});
					return values;
				}
			}
		};
	</script>
</div>