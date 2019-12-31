editor.View = draw2d.Canvas.extend({
	init : function(id) {
		this._super(id);
		this.setScrollArea("#" + id);
		// 鼠标移上节点才显示连接点
		this.installEditPolicy(new draw2d.policy.canvas.CoronaDecorationPolicy());
		this.uninstallEditPolicy(new draw2d.policy.canvas.DefaultKeyboardPolicy());// 屏蔽键盘删除
	},
	onDrag : function(droppedDomNode, x, y) {

	},
	onDrop : function(droppedDomNode, x, y) {
		this.getCommandStack().startTransaction();
		x = x - 20;
		y = y - 20;
		var shape = $(droppedDomNode).data("shape");
		var width = $(droppedDomNode).data("width");
		var height = $(droppedDomNode).data("height");
		var id = $(droppedDomNode).data("id");
		var figures = this.getFigures();
		var hasStart = false;
		var hasEnd = false;
		for (var i = 0; i < figures.getSize(); i++) {
			if (figures.get(i).NAME == 'draw2d.shape.node.FlowStart') {
				hasStart = true;
			}
			if (figures.get(i).NAME == 'draw2d.shape.node.FlowEnd') {
				hasEnd = true;
			}
			if (hasStart && hasEnd) {
				break;
			}
		}

		if (shape == 'draw2d.shape.node.FlowStart') {
			if (hasStart) {
				showPopMsg.error('只能有一个开始节点。');
				return;
			}
		} else if (shape == 'draw2d.shape.node.FlowEnd') {
			if (hasEnd) {
				showPopMsg.error('只能有一个结束节点。');
				return;
			}
		}

		var commandList = new draw2d.util.ArrayList();
		var groupid = draw2d.util.UUID.create();
		try {
			var f0 = eval('new ' + shape + '(' + id + ', ' + width + ', ' + height + ')');
			f0.groupid = groupid;
			commandList.push(new draw2d.command.CommandAdd(this, f0, x, y));
			for (var c = 0; c < commandList.getSize(); c++) {
				this.getCommandStack().execute(commandList.get(c));
			}
			this.getCommandStack().commitTransaction();
		} catch (e) {
			showPopMsg.error('无法创建流程节点：' + shape + '，异常：<br>' + e);
			return false;
		}
	},

	/**
	 * 删除字符串最后的字符
	 * 
	 * @param str
	 * @param c
	 * @returns {*}
	 */
	deleteLastChar : function(str, c) {
		// 判断当前字符串是否以str结束
		var s = str;
		if (typeof String.prototype.endsWith != 'function') {
			String.prototype.endsWith = function(str) {
				return this.slice(-str.length) == str;
			};
		}

		if (s.endsWith(c)) {
			s = s.substr(0, str.length - c.length);

		}
		return s;
	},
	setFlowId : function(flowId) {
		this.flowId = flowId;
	},

	getFlowId : function() {
		return this.flowId;
	},
	flowId : ''
});
