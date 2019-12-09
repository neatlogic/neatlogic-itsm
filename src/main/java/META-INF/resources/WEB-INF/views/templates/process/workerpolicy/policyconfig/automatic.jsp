<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="form_workerpolicy_{{=it.policy}}">
	<div class="help-block">${tk:lang("请选择分配器","") }</div>
	<div>
		<select class="sltWorkerDispatcher" plugin-checkselect data-searchable="false" {{?it.config && it.config.handler}}data-value="{{=it.config.handler}}"{{?}}>
		</select>
		<textarea class="txtWorkerDispatcherConfig" style="display: none">{{?it.config && it.config.config}}{{=JSON.stringify(it.config.config, null, 2)}}{{?}}</textarea>
	</div>
	<div class="divWorkerDispatcherConfig"></div>
	<script class="xdotScript">
		var loadFn = function() {
			var that = this;
			$.getJSON('${pageContext.request.contextPath}/module/process/process/workerdispatcher/list', function(data) {
				$('.sltWorkerDispatcher', that).loadSelect(data, {
					valuekey : 'handler',
					textkey : 'name'
				});
				if ($('.sltWorkerDispatcher', that)[0].checkselect) {
					$('.sltWorkerDispatcher', that)[0].checkselect.reload();
				}
				$('.sltWorkerDispatcher', that).trigger('change');
			});
		};
		var fn = {
			'.sltWorkerDispatcher' : {
				'change' : function() {
					var that = this.root;
					if ($(this).val()) {
						$(this).find('option:selected').each(function() {
							var configpage = $(this).attr('configpage');
							var data = {};
							if ($.trim($('.txtWorkerDispatcherConfig', that).val())) {
								data['config'] = JSON.parse($.trim($('.txtWorkerDispatcherConfig', that).val()));
							}
							var html = xdoT.render(configpage, data);
							$('.divWorkerDispatcherConfig', that).html(html);
						});
					} else {
						$('.divWorkerDispatcherConfig', that).empty();
					}
				}
			},
			'this' : {
				'getConfig' : function() {
					var that = this;
					var form = $('.divWorkerDispatcherConfig', that).find('form');
					var data = {};
					if (form.length == 1) {
						form.on('getConfig', function(event) {
							data['config'] = event.result;
							/*必须加，不然会无限递归*/
							event.stopPropagation();
						});
						form.trigger('getConfig');
					}
					data['handler'] = $('.sltWorkerDispatcher', that).val();
					return data;
				}
			}
		}
	</script>
</form>