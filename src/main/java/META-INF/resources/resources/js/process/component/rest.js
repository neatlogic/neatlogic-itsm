;
window.flowChartNode.rest = {
	stroke : '#8c8c8c',
	onLinkEdit : function(link) {
		var json = link.getUserData() || {};
		var html = xdoT.render('octopus.flow.link.confignodelink', json);
		createModalDialog({
			msgtitle : '条件编辑',
			msgcontent : html,
			msgwidth : 700,
			successFuc : function() {
				var form = $('#formConfigNodeLink');
				if (form.valid()) {
					var finalJson = form.toJson();
					link.setUserData(finalJson);
				}
			}
		});
	},
	onEdit : function(node) {
		var json = {};
		json.userData = node.getUserData();
		
		var nodeList = node.getAllPrevNode('node');
		var restList = node.getAllPrevNode('rest');
		var verifyList = node.getAllPrevNode('verify');
		var childFlowList = node.getAllPrevNode('childflow');
		nodeList = nodeList.concat(restList);
		nodeList = nodeList.concat(verifyList);
		nodeList = nodeList.concat(childFlowList);
		var prevNodeIds = '';
		for (var i = 0; i < nodeList.length; i++) {
			var prevNode = nodeList[i];
			if (prevNodeIds) {
				prevNodeIds += ',';
			}
			prevNodeIds += prevNode.getId();
		}
		json.prevNodeIds = prevNodeIds;

		var html = xdoT.render('octopus.flow.rest.configrest', json);
		createSlideDialog({
			title : '编辑接口节点',
			content : html,
			width : '90%',
			successFuc : function() {
				var form = $('#formConfigNode');
				if (form.valid()) {
					var finalJson = $('#formConfigNode').toJson();
					var returnJson = {};
					var count = form.find('.hidActionParamId').length;
					for (var i = 0; i < count; i++) {
						var actionParamId = form.find('.hidActionParamId:eq(' + i + ')').val();
						var inputType = form.find('.sltInputType:eq(' + i + ')').val();
						var value = form.find('.' + inputType + 'Value:eq(' + i + ')').val();
						var config = form.find('.txtConfig:eq(' + i + ')').val();
						returnJson[actionParamId] = {
							inputtype : inputType,
							value : value,
							config : $.trim(config)
						};
					}
					finalJson.setting = returnJson;

					if (typeof finalJson.status != 'object') {
						finalJson.status = [ finalJson.status ];
					}

					var sltT = form.find('.sltT');
					var txtTimeRange = form.find('.txtTimeRange');
					var timeWindow = '';
					if ($.trim(txtTimeRange.val())) {
						var timeRange = $.trim(txtTimeRange.val());
						var regex = /^([0-1]?[\d]|[2][0-3]):[0-5]?[\d]-([0-1]?[\d]|[2][0-3]):[0-5]?[\d]$/ig;
						if (!regex.test(timeRange)) {
							showPopMsg.info('时间窗口不符合格式要求');
							return false;
						} else {
							timeWindow = 'T+' + sltT.val() + ' ' + timeRange;
						}
					}
					finalJson.timeWindow = timeWindow;
					node.setUserData(finalJson);
					node.setLabel(finalJson.name || '');

					return true;
				}
				return false;
			}
		});
	},
	onValid : function(node) {
		var userData = node.getUserData();
		if (userData) {
			if (userData['name'] && userData['actionId']) {
				return true;
			} else {
				var msg = '请设置以下属性：\n';
				if (!userData['name']) {
					msg += '步骤名称\n';
				}
				if (!userData['execUser']) {
					msg += '执行用户\n';
				}
				node.showMessage({
					msg : msg,
					fill : '#f4f4f4',
					fontcolor : '#999'
				});
			}
		}
		return false;
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
	}
};