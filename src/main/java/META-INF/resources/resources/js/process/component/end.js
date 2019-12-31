;
window.flowChartNode.end = {
	label : '结束',
	type : 'end',
	fill : '#FF9999',
	icon : '\ue8b4',
	stroke : '#8c8c8c',
	isEnd : true,
	shape : 'L-rectangle-50%:R-rectangle-50%',
	fontcolor : '#fff',
	iconcolor : '#fff',
	isMultiple : 0,
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
	handleMode : 'at',
	userData : {
		name : '结束'
	}
};