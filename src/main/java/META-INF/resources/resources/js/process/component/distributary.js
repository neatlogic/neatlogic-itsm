;
window.flowChartNode.distributary = {
	onEdit : function(node) {

	},
	onValid : function(node) {
		return true;
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
	label : '分流节点',
	icon : '\uea97',
	handleMode : 'at',
	stroke : '#8c8c8c',
	type : 'distributary',
	loop : true,
	userData : {
		name : '分流节点'
	},
	shape : 'L-trapezoid:R-trapezoid'
};