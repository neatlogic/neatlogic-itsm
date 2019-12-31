// declare the namespace for this editor
var viewer = {
	command : {},
	shape : {},
	propertypane : {},
	backend : {},
	dialog : {},
	conn : {},
	canvasBg : {}
};

viewer.Application = Class.extend({
	NAME : "editor.Application",

	/**
	 * @constructor
	 * 
	 * @param {String}
	 *            canvasId the id of the DOM element to use as paint container
	 */
	init : function(json) {
		// this.flowId = flowId;
		this.flowName = '';
		this.readonly = false;
		this.view = new viewer.View("canvas");
		this.toolbar = new viewer.Toolbar("toolbar", this.view);
		/*
		 * this.propertyPane = new editor.PropertyPane("property", this.view);
		 */
		// this.router = new
		// draw2d.layout.connection.ManhattanBridgedConnectionRouter(); //折线
		this.router = new draw2d.layout.connection.VertexRouter();
		this.targetDecorator = new draw2d.decoration.connection.ArrowDecorator();
		this.sourceDecorator = null;
		this.connColor = "999999";
		this.connStroke = 1;
		this.connDynamic = 0;// 默认静态线
		this.lineStyle = 1; // 默认实线

		this.view.installEditPolicy(new draw2d.policy.canvas.MyPolicy());
		// this.view.installEditPolicy(new
		// draw2d.policy.canvas.CoronaDecorationPolicy());
		var v = this.view;
		this.resetAndPaint(json);
		/*var g = new draw2d.shape.composite.Group( );
		this.view.groupid = g.getId();
		for(var f = 0; f < this.view.getFigures().getSize(); f++){
			g.assignFigure(this.view.getFigures().get(f));
		}
		this.view.getCommandStack().execute(new draw2d.command.CommandAdd(this.view, g, 0, 0));
		//this.view.getCommandStack().execute(new draw2d.command.CommandGroup(this.view, this.view.getFigures()));*/
	},

	/**
	 * @method Return the view or canvas of the application. Required to access
	 *         the document itself
	 * 
	 */
	getView : function() {
		return this.view;
	},

	/**
	 * @method Return the backend data storage for this application
	 * 
	 */
	getBackend : function() {
		return this.backend;
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

	createConnection : function(sourcePort, targetPort) {
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
		// if (this.connDasharray != undefined) {
		// conn.setDashArray(this.connDasharray);
		// } else {
		// conn.setDashArray(null);
		// }
		// conn.onContextMenu = null;//取消右键菜单
		return conn;
	},

	saveDefinition : function(updateType) {

		var writer = new draw2d.io.json.Writer();
		writer.marshal(this.view, $.proxy(function(data) {
			this.backend.save(this.loadedDefinitionId, this.canvasBg, data, updateType, $.proxy(function() {
				// do nothing
			}, this));
		}, this));

	},

	loadDefinition : function(definitionId) {
		// $("#loadedFileName").text("loading...");
		this.view.clear();
		this.backend.load(definitionId, $.proxy(function(jsonDocument) {
			this.loadedDefinitionId = definitionId;
			this.canvasBg = jsonDocument.bg;
			this.setCanvasBackground(this.canvasBg);
			this.resetAndPaint(jsonDocument.chartConf);
		}, this));
	},

	/**
	 * 
	 * @param chartConf
	 */
	resetAndPaint : function(json) {
		var reader = new draw2d.io.json.Reader();
		reader.unmarshal(this.view, json);
		// 隐藏所有连接点，设置透明度0
		this.view.getAllPorts().each($.proxy(function(i, p) {
			p.setAlpha(0);
		}, this));

		var w = 2000;
		var h = 2000;
		// 找出最大边长
		/*this.view.getFigures().each($.proxy(function(i, f) {
			if (f.x > w) {
				w = f.x;
			}
			if (f.y > h) {
				h = f.y;
			}
		}, this));*/
		var size = {
			width : w,
			height : h
		};
		this.view.setDimension(size);
		// $(".main-container").width(w + 20).height(h + 20);
		/*var cw = $("#content").width();
		var ch = $("#content").height();
		var s = Math.max(cw / w, ch / h).toFixed(2);
		s = s > 2 ? 2 : s; // 最多缩小2倍
		this.view.setZoom(s, false);*/
		// 连接线重置
		// this.view.getLines().each($.proxy(function (i,line){
		// line.onContextMenu = function(){};//清除右键菜单
		// },this));

	}
});
