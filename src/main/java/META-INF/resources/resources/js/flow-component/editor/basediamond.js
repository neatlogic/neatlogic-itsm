draw2d.shape.node.FlowBaseDiamond = draw2d.shape.basic.Diamond.extend({
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
	stateFigure : null,
	getName : function() {
		return this.NAME;
	},
	isValid : null,
	init : function(id, width, height, needport) {
		this._super(width, height);
		this.stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/warn.png', 16, 16);
		this.setResizeable(false);
		var color = new draw2d.util.Color('#CCFFCC');
		this.setBackgroundColor(color);
		this.label = new draw2d.shape.basic.Label(this.CNNAME);
		this.label.setStroke(0);
		this.label.onContextMenu = this.onContextMenu;
		this.addFigure(this.label, new draw2d.layout.locator.BottomLocator(this));
		if (typeof needport == 'undefined' || needport) {
			this.createPort("hybrid", new draw2d.layout.locator.BottomLocator(this));
			this.createPort("hybrid", new draw2d.layout.locator.MyLeftLocator(this));
			this.createPort("hybrid", new draw2d.layout.locator.TopLocator(this));
			this.createPort("hybrid", new draw2d.layout.locator.MyRightLocator(this));
		}
	},
	setData : function(d) {
		this.setUserData(d);
		if (d != null && d.name != null && d.name != '') {
			this.setLabelText(d.name);
		}
	},
	setLabelText : function(txt) {
		this.labelText = txt;
		this.label.setText(this.labelText);
	},
	setPersistentAttributes : function(memento) {
		this._super(memento);
		if (typeof memento.labels.text !== "undefined") {
			this.setLabelText(memento.labels.text);
		}
	},
	getPersistentAttributes : function() {
		var memento = this._super();
		memento.labels = this.label.getPersistentAttributes();
		return memento;
	},
	onDoubleClick : function() {
	},
	onContextMenu : null,
	getLabelText : function() {
		return this.label.getText();
	}
});