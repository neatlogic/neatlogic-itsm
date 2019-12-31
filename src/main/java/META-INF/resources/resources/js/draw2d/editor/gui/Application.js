// declare the namespace for this editor
var editor = {
	command : {},
	shape : {},
	propertypane : {},
	backend : {},
	dialog : {},
	chartType : {}
};

/**
 * 
 * The **GraphicalEditor** is responsible for layout and dialog handling.
 * 
 * @author Andreas Herz
 * @extends draw2d.ui.parts.GraphicalEditor
 */
editor.Application = Class.extend({
	NAME : "editor.Application",

	/**
	 * @constructor
	 * 
	 * @param {String}
	 *            canvasId the id of the DOM element to use as paint container
	 */
	init : function(flowId) {
		this.flowId = flowId;
		this.flowName = '';
		this.readonly = false;
		this.view = new editor.View("canvas");
		this.toolbar = new editor.Toolbar("toolbar", this.view);
		/*
		 * this.propertyPane = new editor.PropertyPane("property", this.view);
		 */
		// this.router = new
		// draw2d.layout.connection.SplineConnectionRouter();//曲线
		// this.router = new draw2d.layout.connection.FanConnectionRouter();
		// //折线
		// this.router = new draw2d.layout.connection.VertexRouter();
		// this.router = new
		// draw2d.layout.connection.SketchBridgedConnectionRouter();
		this.router = new draw2d.layout.connection.ManhattanConnectionRouter();
		this.targetDecorator = new draw2d.decoration.connection.ArrowDecorator(10, 7);
		this.sourceDecorator = null;
		this.connColor = "999999";
		this.connStroke = 1;
		this.connDynamic = 0;// 默认静态线
		this.lineStyle = 1; // 默认实线

		var size = {
			width : 2000,
			height : 2000
		};
		this.view.setDimension(size);

		var v = this.view;
		var c = this;
		if (flowId && !isNaN(parseInt(flowId))) {
			$.getJSON('getFlowByIdJson.do?id=' + flowId, function(data) {
				c.setName(data.flowName);
				c.setType(data.flowType);
				v.setFlowId(data.flowId);
				$('#txtFlowName').val(data.flowName);
				$('#sltFlowType').val(data.flowType);
				var reader = new draw2d.io.json.Reader();
				reader.unmarshal(v, data.chart);
			});
		}
	},
	getView : function() {
		return this.view;
	},
	setRouterByClassName : function(routerClassName) {
		this.routerClassName = routerClassName;
		this.router = eval("new " + this.routerClassName + "()");
	},
	setLineTargetDecorator : function(className) {
		this.targetDecorator = eval("new " + className + "(10,7)");
	},
	setLineSourceDecorator : function(className) {
		this.sourceDecorator = eval("new " + className + "(10,7)");
	},
	createConnectionAuto : function(sourcePort, targetPort) {
		var conn = new MyConnection();
		conn.setRouter(this.router);
		if (this.targetDecorator != null) {
			conn.setTargetDecorator(this.targetDecorator);
		}
		if (this.sourceDecorator != null) {
			conn.setSourceDecorator(this.sourceDecorator)
		}
		conn.setStroke(this.connStroke);
		conn.setColor(this.connColor);
		conn.setSource(sourcePort);
		conn.setTarget(targetPort);
		conn.setStatic();
		return conn;
	},
	createConnection : function(sourcePort, targetPort) {
		var sourceNode = sourcePort.getParent();
		var targetNode = targetPort.getParent();
		if ((sourceNode.getName() != 'draw2d.shape.node.FlowStart' && this.lineStyle == 1) && sourceNode.getComposite() != null) {
			return;
		}

		if ((targetNode.getName() != 'draw2d.shape.node.FlowStart' && this.lineStyle == 2) && targetNode.getComposite() != null) {
			return;
		}

		if (this.lineStyle != 3 && targetNode.allowCallin) {
			showPopMsg.info('接口节点只能用调用路径关联。');
			return;
		}

		if (this.lineStyle == 3 && (!targetNode.allowCallin || !sourceNode.allowCallout)) {
			showPopMsg.info('无法建立<b>' + sourceNode.CNNAME + '</b>节点到<b>' + targetNode.CNNAME + '</b>节点的调用路径。');
			return;
		}

		if (sourceNode.getName() == 'draw2d.shape.node.FlowStart' && targetNode.getName() == 'draw2d.shape.node.FlowEnd') {
			showPopMsg.info('开始节点不能与结束节点关联。');
			return;
		}

		for (var c = 0; c < sourceNode.getConnections().getSize(); c++) {
			if (sourceNode.getConnections().get(c).getTarget().getParent().getId() == targetNode.getId()) {
				showPopMsg.info('请勿重复连线。');
				return;
			}
		}

		if (this.lineStyle == 2 && (!sourceNode.allowBack || !targetNode.allowRefire)) {
			showPopMsg.info('无法建立<b>' + sourceNode.CNNAME + '</b>节点到<b>' + targetNode.CNNAME + '</b>节点的回退路径。');
			return;
		}
		var conn = new MyConnection();

		switch (this.lineStyle) {
		case 1:
			conn.setStatic();// 静态实线
			break;
		case 2:
			conn.setDashArray("-");// 虚线
			break;
		case 3:
			conn.setDashArray("--..");// 动态虚线
			break;
		default:
			conn.setDashArray("");
			break;
		}

		conn.setRouter(this.router);
		if (this.targetDecorator != null) {
			conn.setTargetDecorator(this.targetDecorator);
		}
		if (this.sourceDecorator != null) {
			conn.setSourceDecorator(this.sourceDecorator)
		}
		conn.setStroke(this.connStroke);
		conn.setColor(this.connColor);
		conn.setSource(sourcePort);
		conn.setTarget(targetPort);
		return conn;
	},
	checkFlow : function() {
		var error = $('<ul></ul>');
		var isValid = true;
		var errorList = new draw2d.util.ArrayList();
		if (this.getName() == null || this.getName() == '') {
			error.append('<li>请填写流程名称。</li>');
			isValid = false;
		}

		var figureList = this.view.getFigures();
		for (var f = 0; f < figureList.getSize(); f++) {
			var figure = figureList.get(f);
			if (figure.getCssClass() == 'draw2d_shape_composite_Group') {
				continue;
			}
			var nextNodes = new draw2d.util.ArrayList();
			this.findAllNextNode(nextNodes, figure);
			figure.setNextNodes(nextNodes);

			var prevNodes = new draw2d.util.ArrayList();
			this.findAllPrevNode(prevNodes, figure);
			figure.setPrevNodes(prevNodes);
		}

		var hasStart = false, hasEnd = false, hasProcess = false, errinfo;
		for (var i = 0; i < figureList.getSize(); i++) {
			var that = figureList.get(i);
			if (that.getCssClass() == 'draw2d_shape_composite_Group') {
				continue;
			}
			if (that.getName() == 'draw2d.shape.node.FlowStart') {
				hasStart = true;
			} else if (that.getName() == 'draw2d.shape.node.FlowEnd') {
				hasEnd = true;
			} else if (that.getName().indexOf('draw2d.shape.node.FlowState') == 0) {
				hasProcess = true;
			}

			if (that.isValid) {
				errinfo = that.isValid();
				if (errinfo != true) {
					isValid = false;
					if (!errorList.contains(errinfo)) {
						errorList.add(errinfo);
					}
				}
			}

			// 校验节点关联合法性START
			var e1 = '发现孤立节点。';
			var e2 = '发现循环关联。';
			var e3 = '回退路径不能指向非前置步骤。';
			var nextNodes = that.getNextNodes();
			var prevNodes = that.getPrevNodes();
			var connEnd = false;
			var connStart = false;
			// console.log("id:" + that.getId());
			for (var n = 0; n < nextNodes.getSize(); n++) {
				var node = nextNodes.get(n);
				if (that.getId() == node.getId()) {
					isValid = false;
					that.addFigure(that.stateFigure, new draw2d.layout.locator.RightLocator(that));
					if (!errorList.contains(e2)) {
						errorList.add(e2);
					}
				}
				if (!connEnd && node.getName() == 'draw2d.shape.node.FlowEnd') {
					connEnd = true;
				}
				// console.log("next:" + node.getId());
			}

			for (var n = 0; n < prevNodes.getSize(); n++) {
				var node = prevNodes.get(n);
				if (that.getId() == node.getId()) {
					isValid = false;
					that.addFigure(that.stateFigure, new draw2d.layout.locator.RightLocator(that));
					if (!errorList.contains(e2)) {
						errorList.add(e2);
					}
				}
				if (!connStart && node.getName() == 'draw2d.shape.node.FlowStart') {
					connStart = true;
				}
				// console.log("prev:" + node.getId());
			}

			if ((!connEnd && that.getName() != 'draw2d.shape.node.FlowEnd' && that.TYPE != 2) || (!connStart && that.getName() != 'draw2d.shape.node.FlowStart')) {
				if ((that.getComposite() != null && that.getName() != 'draw2d.shape.node.FlowStart') || that.getName() == 'draw2d.shape.node.FlowStateChildflow') {

				} else {
					if (!errorList.contains(e1)) {
						errorList.add(e1);
					}
					isValid = false;
					that.addFigure(that.stateFigure, new draw2d.layout.locator.RightLocator(that));
				}
			}
			// 校验节点关联合法性END
		}

		var lines = this.view.getLines();
		var hasSame = false;

		var color = new draw2d.util.Color("#ff0000");

		for (var l = 0; l < lines.getSize(); l++) {
			var line = lines.get(l);
			if (line.dasharray == '-') {
				var backsource = line.getSource().getParent();
				var backtarget = line.getTarget().getParent();
				if (!backsource.allowBack || !backtarget.allowRefire) {
					isValid = false;
					line.setColor(color);
					if (!errorList.contains('无法建立<b>' + backsource.CNNAME + '</b>节点到<b>' + backtarget.CNNAME + '</b>节点的回退路径。')) {
						errorList.add('无法建立<b>' + backsource.CNNAME + '</b>节点到<b>' + backtarget.CNNAME + '</b>节点的回退路径。');
					}
				}
				var isexists = false;
				for (var b = 0; b < backtarget.getNextNodes().getSize(); b++) {
					if (backtarget.getNextNodes().get(b).getId() == backsource.getId()) {
						isexists = true;
					}
				}
				if (!isexists) {
					isValid = false;
					line.setColor(color);
					if (!errorList.contains(e3)) {
						errorList.add(e3);
					}
				}
			}
		}

		if (!hasStart) {
			error.append('<li>请添加一个开始节点。</li>');
			isValid = false;
		}

		if (!hasEnd) {
			error.append('<li>请添加一个结束节点。</li>');
			isValid = false;
		}

		if (!hasProcess) {
			error.append('<li>请至少添加一个过程节点。</li>');
			isValid = false;
		}

		if (!isValid) {
			for (var e = 0; e < errorList.getSize(); e++) {
				error.append('<li>' + errorList.get(e) + '</li>');
			}
			isValid = false;
		}

		if (!isValid) {
			showPopMsg.warn(error, 10);
		}
		return isValid;
	},
	findAllNextNode : function(nodeList, figure) {
		var connections = figure.getConnections();
		for (var c = 0; c < connections.getSize(); c++) {
			var conn = connections.get(c);
			if (conn.dasharray == '' && conn.getSource().getParent().getId() == figure.getId() && !nodeList.contains(conn.getTarget().getParent())) {
				nodeList.add(conn.getTarget().getParent());
				this.findAllNextNode(nodeList, conn.getTarget().getParent());
			}
		}
	},
	findAllPrevNode : function(nodeList, figure) {
		var connections = figure.getConnections();
		for (var c = 0; c < connections.getSize(); c++) {
			var conn = connections.get(c);
			if ((conn.dasharray == '' || conn.dasharray == '--..') && conn.getTarget().getParent().getId() == figure.getId() && !nodeList.contains(conn.getSource().getParent())) {
				nodeList.add(conn.getSource().getParent());
				this.findAllPrevNode(nodeList, conn.getSource().getParent());
			}
		}
	},
	saveFlow : function(btn) {
		var writer = new draw2d.io.json.Writer();
		var v = this.view;
		var c = this;
		/*
		 * writer.marshal(v, function(json) { var jsonTxt = JSON.stringify(json,
		 * null, 2); console.info(jsonTxt); });
		 */
		if (this.checkFlow()) {
			// var svgwriter = new draw2d.io.svg.Writer();
			// svgwriter.marshal(v, function(svg) {
			// console.log(svg);
			// });

			writer.marshal(v, function(json) {
				var j = {};
				j.flowId = c.flowId;
				j.chart = json;
				j.flowName = c.getName();
				j.flowType = c.getType();
				var jsonTxt = JSON.stringify(j, null, 2);
				$.ajax({
					type : 'POST',
					dataType : 'json',
					url : 'saveFlowAjax.do',
					data : {
						'id' : this.flowId,
						'flow' : jsonTxt
					},
					success : function(data) {
						if (data.Status == 'OK') {
							showPopMsg.success('操作成功', function() {
								if (data.flowid) {
									window.location.href = '/balantflow/flow/editFlow.do?flowId=' + data.flowid;
								}
							});
						} else {
							showPopMsg.error('保存失败，异常：<br>' + data.Message);
						}
						btn.removeAttr('disabled').html('保存');
						try{
							updateflowCanvas();//20100920_zqp for update table in listFlowManage
						}catch(e){
							
						}
					}
				});
			});
			if (btn) {
				btn.attr('disabled', 'disabled');
			}
		}
	},

	setData : function(figure, userData) {
		figure.setUserData(userData);
		// console.info(figure.getUserData());
	},

	setName : function(n) {
		this.flowName = n;
	},

	getName : function() {
		return this.flowName;
	},

	setType : function(n) {
		this.flowType = n;
	},

	getType : function() {
		return this.flowType;
	},

	setAlertConnectionColor : function(figure, color) {
		figure.getConnections().each(function(i, line) {
			line.setColor(color);
		});
	},

	resetConnectionColor : function(figure) {
		figure.getConnections().each(function(i, line) {
			line.resetColor();
		});
	},

	showInfo : function(userData) {

	}
});
