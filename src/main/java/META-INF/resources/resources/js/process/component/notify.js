;
window.flowChartNode.notify = {
	onEdit : function(node) {
		var json = {};
		json.userData = node.getUserData();

		var callArray = new Array();
		callArray.push($.getJSON('/balantflow/module/octopus/notify/listall', function(data) {
			json.notifyList = data;
		}));
		$.when.apply($, callArray).done(function() {
			var html = xdoT.render('octopus.flow.notify.confignotify', json);
			createSlideDialog({
				title : '编辑通知节点',
				content : html,
				width : '90%',
				successFuc : function() {
					var form = $('#formConfigNotify');
					if (form.valid()) {
						var finalJson = $('#formConfigNotify').toJson();
						node.setUserData(finalJson);
						node.setLabel(finalJson.name || '');
						return true;
					}
					return false;
				}
			});
		});
	},
	onValid : function(node) {
		var userData = node.getUserData();
		if (userData) {
			if (userData['name'] && userData['fromName'] && userData['userIdList'] && userData['subject'] && userData['content']) {
				return true;
			} else {
				var msg = '请设置以下属性：\n';
				if (!userData['name']) {
					msg += '步骤名称\n';
				}
				if (!userData['fromName']) {
					msg += '发件人名称\n';
				}
				if (!userData['userIdList']) {
					msg += '收件人\n';
				}
				if (!userData['subject']) {
					msg += '主题\n';
				}
				node.showMessage({
					msg : msg,
					fill : '#f4f4f4',
					fontcolor : '#aaa'
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
	},
	needOut : false,
	label : '通知',
	icon : '\uea6c',
	type : 'notify',
	handleMode : 'at',
	stroke : '#8c8c8c',
	shape : 'L-rectangle:R-hexagon',
	userData : {
		name : '通知'
	}
};