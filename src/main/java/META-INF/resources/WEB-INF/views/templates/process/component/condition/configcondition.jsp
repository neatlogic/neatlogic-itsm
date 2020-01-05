<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/commons/taglibs.jsp"%>
<form id="formConfigNode" class="form-horizontal">
	<div class="form-block form-block-10 ">
		<label class="block-left">步骤名称：</label>
		<div class="block-right">
			<input type="text" class="input-large" maxlength="20" name="name" value="{{=it.userData.name||' 条件判断'}}" check-type="required">
			<input type="hidden" id="hidPrevNodeIds" value="{{=it.prevNodeIds||''}}">
			<input type="hidden" id="hidNextNodeIds" value="{{=it.nextNodeIds||''}}">
			<input type="hidden" id="hidNextPathIds" value="{{=it.nextPathIds||''}}">
			{{?it.userData && it.userData.path}} {{?typeof(it.userData.path) == 'object'}} {{~it.userData.path:path:index}}
			<input type="hidden" class="hidOldPath" value="{{=path}}">
			{{~}} {{??}}
			<input type="hidden" class="hidOldPath" value="{{=it.userData.path}}">
			{{?}} {{?}} {{?it.userData && it.userData.condition}} {{?typeof(it.userData.condition) == 'object'}} {{~it.userData.condition:condition:index}}
			<textarea style="display: none" class="hidOldCondition">{{=condition}}</textarea>
			{{~}} {{??}}
			<input type="hidden" class="hidOldCondition" value="{{=it.userData.condition}}">
			{{?}} {{?}}
		</div>
	</div>
	<div class="form-block form-block-10 text ">
		<label class="block-left">前置变量：</label>
		<div class="block-right" id="divParameter">
			<p class="form-control-static">前置节点没有任何返回值</p>
		</div>
	</div>
	<div class="form-block form-block-10 text ">
		<label class="block-left">流转设置：</label>
		<div class="block-right" id="divNextNode"></div>
	</div>
	<script class="xdotScript">
		var loadFn = function(that) {
			var outputParamList = new Array();
			var nextNodeIds = $('#hidNextNodeIds', that).val();
			var nextPathIds = $('#hidNextPathIds', that).val();
			if (nextNodeIds && nextPathIds) {
				var newNodeList = new Array();
				var nextNodeIdList = nextNodeIds.split(',');
				var nextPathIdList = nextPathIds.split(',');
				for (var i = 0; i < nextNodeIdList.length; i++) {
					var node = Paper.getNodeById(nextNodeIdList[i]);
					var userData = node.getUserData();
					var jsonObj = {
						id : nextNodeIdList[i],
						name : userData.name,
						path : nextPathIdList[i]
					};
					$('.hidOldPath', that).each(function(pi, pk) {
						if ($(this).val() == nextPathIdList[i]) {
							jsonObj['condition'] = $('.hidOldCondition:eq(' + pi + ')', that).val();
						}
					});
					newNodeList.push(jsonObj);
				}
				var html = xdoT.render('process.component.condition.listnode', newNodeList);
				$('#divNextNode', that).empty().html(html);
			} else {
				$('#divNextNode', that).empty().html('<p class="form-control-static">没有后续步骤</p>');
			}
			var json = {};
			var prevNodeIds = $('#hidPrevNodeIds', that).val();
			if (prevNodeIds) {
				var prevNodeIdList = prevNodeIds.split(',');
				var callArray = new Array();
				for (var i = 0; i < prevNodeIdList.length; i++) {
					var node = Paper.getNodeById(prevNodeIdList[i]);
					if (node) {
						var userData = node.getUserData();
						var stepName = userData.name;
						if (node.getType() == 'node') {
							var caid = userData.actionId;
							if (caid) {
								callArray.push($.getJSON('${pageContext.request.contextPath}/module/octopus/action/' + caid + '/activedversion', (function(node, stepName) {
									return function(data) {
										var hasOutput = false;
										if (data.actionParamList && data.actionParamList.length > 0) {
											for (var p = 0; p < data.actionParamList.length; p++) {
												if (data.actionParamList[p].mode == 'output') {
													hasOutput = true;
													break;
												}
											}
										}
										if (hasOutput) {
											var stepObj = {};
											stepObj.name = stepName;
											stepObj.id = node.getId();
											stepObj.paramList = new Array();
											for (var p = 0; p < data.actionParamList.length; p++) {
												if (data.actionParamList[p].mode == 'output') {
													stepObj.paramList.push(data.actionParamList[p]);
												}
											}
											outputParamList.push(stepObj);
										}
									};
								}(node, stepName))));
							}
						} else if (node.getType() == 'verify') {
							if (userData.paramList) {
								var stepObj = {};
								stepObj.name = stepName;
								stepObj.id = node.getId();
								stepObj.paramList = new Array();

								for (var p = 0; p < userData.paramList.length; p++) {
									userData.paramList[p].mode = 'output';/*转换人工节点的参数类型为输出参数*/
									stepObj.paramList.push(userData.paramList[p]);
								}
								outputParamList.push(stepObj);
							}
						} else if (node.getType() == 'childflow') {
							var flowid = userData.flowId;
							if (flowid) {
								callArray.push($.getJSON('${pageContext.request.contextPath}/module/octopus/flow/param/search', {
									flowId : flowid,
									mode : 'output'
								}, (function(node, stepName) {
									return function(data) {
										if (data.length > 0) {
											var actionObj = {
												name : stepName,
												paramList : data,
												id : node.getId()
											};
											outputParamList.push(actionObj);
										}
									};
								}(node, stepName))));
							}
						}
					}
				}
				$.when.apply($, callArray).done(function() {
					var html = xdoT.render('process.component.listparam', outputParamList);
					$('#divParameter', that).empty().html(html);
				});
			}
		};
	</script>
</form>