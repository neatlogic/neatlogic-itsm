;
window.flowChartNode.start = {
	label : '开始',
	type : 'start',
	fill : '#00CCCC',
	fontcolor : '#fff',
	icon : '\ue81b',
	stroke : '#8c8c8c',
	shape : 'L-rectangle-50%:R-rectangle-50%',
	iconcolor : '#fff',
	isMultiple : 0,
	onEdit : function(node) {
		var json = {};
		json.userData = node.getUserData();
		var html = xdoT.render('process.component.start.configomnipotent', json);
		var editor;
		createSlideDialog({
			title : '编辑通用节点',
			content : html,
			width : '90%',
			successFuc : function() {
			},
			shownFuc : function() {
				editor = CKEDITOR.replace('txtDescription', {
					extraPlugins : ''
				});
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
	userData : {
		name : '开始'
	}
};