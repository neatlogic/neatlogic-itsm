draw2d.shape.node.FlowBaseCircle = draw2d.shape.basic.Circle.extend({
	TYPE : 0,
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
			if (conn.dasharray == '' && conn.getTarget().getParent().getId() == figure.getId() && !nodeList.contains(conn.getSource().getParent())) {
				nodeList.add(conn.getSource().getParent());
				this.findAllPrevNode(nodeList, conn.getSource().getParent());
			}
		}
	},
	init : function(id, width, height, needport) {
		this._super();
		this.stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/warn.png', 16, 16);
		this.setResizeable(false);
		this.label = new draw2d.shape.basic.Label(this.CNNAME);
		this.label.setStroke(0);
		this.label.onContextMenu = this.onContextMenu;
		var color = new draw2d.util.Color(this.COLOR);
		this.setBackgroundColor(color);
		this.addFigure(this.label, new draw2d.layout.locator.BottomLocator(this));
		if (typeof needport == 'undefined' || needport) {
			this.createPort(this.PORTTYPE, new draw2d.layout.locator.BottomLocator(this));
			this.createPort(this.PORTTYPE, new draw2d.layout.locator.MyLeftLocator(this));
			this.createPort(this.PORTTYPE, new draw2d.layout.locator.TopLocator(this));
			this.createPort(this.PORTTYPE, new draw2d.layout.locator.MyRightLocator(this));
		}
	},
	getName : function() {
		return this.NAME;
	},
	setData : function(d) {
		this.setUserData(d);
	},
	getLabelText : function() {
		return this.label.getText();
	},
	onDoubleClick : function() {
	},
	setPersistentAttributes : function(memento) {
		this._super(memento);
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
	getLabelText : function() {
		return this.label.getText();
	}
});