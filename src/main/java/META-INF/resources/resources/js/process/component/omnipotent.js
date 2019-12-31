;
window.flowChartNode.omnipotent = {
	onEdit : function(node) {
		var json = {};
		var prevNodeList = node.getAllPrevNode('omnipotent');
		json.userData = node.getUserData();
		json.uuid = node.getId();
		if (prevNodeList.length > 0) {
			var prevNodeIds = '';
			for (var i = 0; i < prevNodeList.length; i++) {
				if (prevNodeIds) {
					prevNodeIds += ',';
				}
				prevNodeIds += prevNodeList[i].getId();
			}
			json.prevNodeIds = prevNodeIds;
		}
		var html = xdoT.render('process.component.omnipotent.configomnipotent', json);

		createSlideDialog({
			title : '编辑通用节点',
			content : html,
			width : '90%',
			successFuc : function() {
				var form = $('#formConfigOmnipotent');
				if (form.valid()) {
					var finalJson = form.toJson();
					// 处理是否开始节点
					if ($('#hidIsStartNode').val() == '1') {
						var nodeList = node.getPaper().getAllNode();
						for (var i = 0; i < nodeList.length; i++) {
							var n = nodeList[i];
							if (n.getId() != node.getId() && n.getUserData()) {
								var userdata = n.getUserData();
								userdata['isStartNode'] = '0';
								n.setUserData(userdata);
								n.setIsStart(false);
								n.setStyle(null);
								n.setNeedIn(true);
							}
						}
						node.setStyle({
							fill : '#70BC82',
							stroke : '#009688',
							fillopacity : 1,
							fontcolor : '#ffffff',
							fontsize : 12,
							iconcolor : '#ffffff',
							strokewidth : 1,
							strokedasharray : 0
						});
						node.setIsStart(true);
						node.setNeedIn('optional');
					} else {
						node.setStyle(null);
						node.setIsStart(false);
						node.setNeedIn(true);
					}

					// 处理自定义属性
					var attributeList = new Array();
					var attrDiv = $('#divProcessStepAttribute');
					$('.attribute_uuid', attrDiv).each(function(i, k) {
						var attr = {};
						var attruuid = $(this).val();
						attr['uuid'] = attruuid;
						attr['isEditable'] = $('.attribute_iseditable:eq(' + i + ')', attrDiv).val();
						attr['config'] = $('.attribute_config:eq(' + i + ')', attrDiv).val();
						if ($('#form_attribute_' + attruuid, attrDiv).length > 0) {
							$('#form_attribute_' + attruuid, attrDiv).on('getData', function(event) {
								attr['data'] = event.result;
								event.stopPropagation();
							});
							$('#form_attribute_' + attruuid, attrDiv).trigger('getData');
						}
						attributeList.push(attr);
					});
					finalJson['attributeList'] = attributeList;
					
					//处理表单属性
					var formAttributeList = new Array();
					var formDiv = $('#divProcessStepFormAttribute');
					$('.attribute_uuid', formDiv).each(function(i, k) {
						var attr = {};
						var attruuid = $(this).val();
						attr['uuid'] = attruuid;
						attr['isEditable'] = $('.attribute_iseditable:eq(' + i + ')', formDiv).val();
						attr['config'] = $('.attribute_config:eq(' + i + ')', formDiv).val();
						if ($('#form_attribute_' + attruuid, formDiv).length > 0) {
							$('#form_attribute_' + attruuid, formDiv).on('getData', function(event) {
								attr['data'] = event.result;
								event.stopPropagation();
							});
							$('#form_attribute_' + attruuid, formDiv).trigger('getData');
						}
						formAttributeList.push(attr);
					});
					finalJson['formAttributeList'] = formAttributeList;

					// 处理分配策略
					var workerPolicyList = new Array();
					$('.workerpolicy_policy').each(function(i, k) {
						var policy = {};
						policy['policy'] = $(this).val();
						policy['policyName'] = $('.workerpolicy_policyname:eq(' + i + ')').val();
						if ($('#form_workerpolicy_' + policy['policy']).length > 0) {
							$('#form_workerpolicy_' + policy['policy']).on('getConfig', function(event) {
								policy['config'] = event.result;
								event.stopPropagation();
							});
							$('#form_workerpolicy_' + policy['policy']).trigger('getConfig');
						}
						workerPolicyList.push(policy);
					});
					finalJson['workerPolicyList'] = workerPolicyList;

					// 超时策略
					var timeoutPolicyList = new Array();
					$('.timeoutpolicy_policy').each(function(i, k) {
						var policy = {};
						policy['policy'] = $(this).val();
						policy['policyName'] = $('.timeoutpolicy_policyname:eq(' + i + ')').val();
						policy['time'] = $('.timeoutpolicy_time:eq(' + i + ')').val();
						$('.form_timeoutpolicy:eq(' + i + ')').on('getConfig', function(event) {
							policy['config'] = event.result;
							event.stopPropagation();
						}).trigger('getConfig');
						timeoutPolicyList.push(policy);
					});
					finalJson['timeoutPolicyList'] = timeoutPolicyList;
					console.log(JSON.stringify(finalJson, null, 2));
					node.setUserData(finalJson);
					node.setLabel(finalJson.name);
					return true;
				}
				return false;
			},
			shownFuc : function() {
				/*
				 * editor = CKEDITOR.replace('txtDescription', { extraPlugins : ''
				 * });
				 */
			}
		});
	},
	onValid : function(node) {
		var userData = node.getUserData();
		if (userData) {
			if (userData['isStartNode'] == '1') {
				return true;
			} else if (!userData['workerPolicyList'] || userData['workerPolicyList'].length == 0) {
				node.showMessage({
					msg : '请设置：处理设置->指派方式',
					fill : '#f4f4f4',
					fontcolor : '#999'
				});
			} else {
				return true;
			}
		}
		return false;
	},
	onLinkEdit : function(link) {
		var json = link.getUserData() || {};
		console.log(JSON.stringify(json, null, 2));
		var html = xdoT.render('process.component.omnipotent.configomnipotentlink', json);
		createModalDialog({
			msgtitle : '条件编辑',
			msgcontent : html,
			msgwidth : 700,
			successFuc : function() {
				var form = html;
				if (form.valid()) {
					var config = {
						text : $.trim($('#txtAction').val())
					};
					link.setUserData(config);
				}
			}
		});
	},
	onDelete : function(node) {
		var textContent = $(".textContent").val();
		var nodeId = node.getId();
		var eventCount = 0;
		if (textContent.length > 0) {
			var textContentJson = JSON.parse(textContent);
			for ( var item in textContentJson) {
				if (item == nodeId) {
					delete textContentJson[item];
				} else {
					for (var i = 0; i < textContentJson[item].length; i++) {
						eventCount++;
					}
				}
			}
		}
		$(".textContent").val(JSON.stringify(textContentJson));
		if (eventCount > 0) {
			$("#spnEventCount").html(" " + eventCount);
		} else {
			$("#spnEventCount").html("");
		}
	},
	label : '通用节点',
	icon : '\uea97',
	handleMode : 'mt',
	stroke : '#8c8c8c',
	type : 'omnipotent',
	loop : true,
	userData : {
		name : '通用节点'
	},
	shape : 'L-octagon:R-octagon'
};