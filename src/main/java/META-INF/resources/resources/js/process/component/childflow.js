;window.flowChartNode.childflow = {
	onEdit : function(node) {
		var json = {};
		json.userData = node.getUserData();
		var nodeList = node.getAllPrevNode('node');
		var verifyList = node.getAllPrevNode('verify');
		var childFlowList = node.getAllPrevNode('childflow');
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
		var html = xdoT.render('octopus.flow.childflow.configflow', json);
		createSlideDialog({
			title : '编辑子流程节点',
			content : html,
			width : '90%',
			successFuc : function() {
				var form = $('#formConfigFlow');
				if (form.valid()) {
					var finalJson = $('#formConfigFlow').toJson();
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
			if (userData['name'] && userData['flowId']) {
				return true;
			} else {
				node.showMessage({
					msg : '请设置子编排',
					fill : '#f4f4f4',
					fontcolor : '#999'
				});
			}
		}
		return false;
	},
	onLinkEdit : function(link) {
		var json = link.getUserData() || {};
		var html = xdoT.render('octopus.flow.link.configchildflowlink', json);
		createModalDialog({
			msgtitle : '条件编辑',
			msgcontent : html,
			msgwidth : 700,
			successFuc : function() {
				var form = $('#formConfigChildflowLink');
				if (form.valid()) {
					var finalJson = form.toJson();
					link.setUserData(finalJson);
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
	label : '子流程',
	icon : '\ue878',
	type : 'childflow',
	handleMode : 'at',
	stroke : '#8c8c8c',
	shape : 'L-trapezoid:R-invertedTrapezoid',
	userData : {
		name : '子流程'
	}
};