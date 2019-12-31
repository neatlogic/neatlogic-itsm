draw2d.shape.node.FlowBaseService = draw2d.shape.basic.Image.extend({
	NAME : null,
	CNNAME : null,
	TYPE : 2,
	getPrevNodes : function() {
		if (!this.pn || this.pn == null) {
			this.pn = new draw2d.util.ArrayList();
		}
		return this.pn;
	},
	getNextNodes : function() {
		if (!this.nn || this.nn == null) {
			this.nn = new draw2d.util.ArrayList();
		}
		return this.nn;
	},
	setNextNodes : function(nnlist) {
		this.nn = nnlist;
	},
	setPrevNodes : function(nnlist) {
		this.pn = nnlist;
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
			if (conn.dasharray == '-.' && conn.getTarget().getParent().getId() == figure.getId() && !nodeList.contains(conn.getSource().getParent())) {
				nodeList.add(conn.getSource().getParent());
				this.findAllPrevNode(nodeList, conn.getSource().getParent());
			}
		}
	},
	stateFigure : null,
	allowCallin : true,
	isValid : null,
	init : function(componentid, width, height, needport) {
		this._super("../resources/images/draw2d/" + componentid + ".png", width, height);
		this.stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/warn.png', 16, 16);
		this.setResizeable(false);
		this.label = new draw2d.shape.basic.Label(this.CNNAME);
		this.label.setStroke(0);
		this.label.onContextMenu = this.onContextMenu;
		this.setCssClass("deviceNode");
		this.addFigure(this.label, new draw2d.layout.locator.BottomLocator(this));
		// 4个连接点，混合型
		if (typeof needport == 'undefined' || needport) {
			this.createPort("input", new draw2d.layout.locator.BottomLocator(this));
			this.createPort("input", new draw2d.layout.locator.MyLeftLocator(this));
			this.createPort("input", new draw2d.layout.locator.TopLocator(this));
			this.createPort("input", new draw2d.layout.locator.MyRightLocator(this));
		}
	},
	getName : function() {
		return this.NAME;
	},
	setData : function(d) {
		this.setUserData(d);
		if (d != null && d.name != null && d.name != '') {
			this.setLabelText(d.name);
		}
	},
	onDoubleClick : function() {
	},
	onContextMenu : null,
	setPersistentAttributes : function(memento) {
		this._super(memento);
		if (typeof memento.labels.text !== "undefined") {
			this.setLabelText(memento.labels.text);
		}
		if (typeof memento.userData.groupid != 'undefined') {
			this.groupid = memento.userData.groupid;
		}
	},
	getPersistentAttributes : function() {
		var memento = this._super();
		memento.labels = this.label.getPersistentAttributes();
		if (this.groupid && !memento.userData.groupid) {
			memento.userData.groupid = this.groupid;
		}
		return memento;
	},
	setLabelText : function(txt) {
		this.labelText = txt;
		this.label.setText(this.labelText);
	},
	getLabelText : function() {
		return this.label.getText();
	}
});