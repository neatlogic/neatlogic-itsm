;
window.flowChartNode.condition = {
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
		var nextPathList = node.getNextPath();
		var nextPathIds = '', nextNodeIds = '';
		for (var i = 0; i < nextPathList.length; i++) {
			var nextPath = nextPathList[i];
			if (nextPathIds) {
				nextPathIds += ',';
			}
			if (nextNodeIds) {
				nextNodeIds += ',';
			}
			nextPathIds += nextPath.getId();
			nextNodeIds += nextPath.getEndNode().getId();
		}
		json.nextPathIds = nextPathIds;
		json.nextNodeIds = nextNodeIds;
		var html = xdoT.render('process.component.condition.configcondition', json);
		createSlideDialog({
			title : '编辑条件判断节点',
			content : html,
			width : '90%',
			successFuc : function() {
				var form = $('#formConfigNode');
				if (form.valid()) {
					var finalJson = $('#formConfigNode').toJson();
					$('.txtCondition').each(function() {
						var pathId = $(this).data('path');
						if ($.trim($(this).val())) {
							Paper.getPathById(pathId).setProperty('condition', $.trim($(this).val()));
						} else {
							Paper.getPathById(pathId).setProperty('condition', '');
						}
					});
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
		var pathList = node.getNextPath();
		if (userData) {
			for (var i = 0; i < pathList.length; i++) {
				var path = pathList[i];
				var hasRule = false;
				if (userData['path']) {
					if (typeof userData['path'] == 'string') {
						if (path.getId() == userData['path'] && userData['condition']) {
							hasRule = true;
						}
					} else {
						for (var u = 0; u < userData['path'].length; u++) {
							var p = userData['path'][u];
							var c = userData['condition'][u];
							if (p == path.getId() && c) {
								hasRule = true;
								break;
							}
						}
					}
				}
				if (!hasRule) {
					node.showMessage({
						msg : '请设置流转条件',
						fill : '#f4f4f4',
						fontcolor : '#999'
					});
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
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
	needOut : true,
	multiOut : true,
	label : '条件判断',
	icon : '\ue98b',
	type : 'condition',
	handleMode : 'at',
	stroke : '#8c8c8c',
	shape : 'L-hexagon:R-hexagon',
	userData : {
		name : '条件判断'
	}
};