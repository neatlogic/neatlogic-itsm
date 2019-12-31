draw2d.shape.node.FlowBaseDiamond = draw2d.shape.basic.Diamond.extend({
	init : function(width, height) {
		this._super(width, height);
		var color = new draw2d.util.Color(this.COLOR);
		this.setBackgroundColor(color);
		this.label = new draw2d.shape.basic.Label('');
		this.label.setStroke(0);
		this.label.onContextMenu = this.onContextMenu;
		this.addFigure(this.label, new draw2d.layout.locator.BottomLocator(this));

		this.createPort("hybrid", new draw2d.layout.locator.BottomLocator(this));
		this.createPort("hybrid", new draw2d.layout.locator.MyLeftLocator(this));
		this.createPort("hybrid", new draw2d.layout.locator.TopLocator(this));
		this.createPort("hybrid", new draw2d.layout.locator.MyRightLocator(this));
	},
	onDoubleClick: function(){},
	setLabelText : function(txt) {
		this.labelText = txt;
		this.label.setText(this.labelText);
	},
	setPersistentAttributes : function(memento) {
		this._super(memento);
		if (typeof memento.labels.text !== "undefined") {
			this.setLabelText(memento.labels.text);
		}
		if (typeof memento.status != 'undefined') {
			var stateFigure = null;
			switch (memento.status) {
			case 1020:
				if (memento.token == 0) {
					stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/doing.gif', 16, 11);
				}
				break;
			case 1021:
				stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/doing.gif', 16, 11);
				break;
			case 1022:
				stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/ok.png', 16, 16);
				break;
			case 1023:
				stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/pause.png', 16, 16);
				break;
			case 1024:
				stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/abort.png', 16, 16);
				break;
			case 1025:
				stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/confirm.png', 16, 16);
				break;
			case 1026:
				stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/hang.png', 16, 16);
				break;
			case 1027:
				stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/back.png', 16, 16);
				break;
			}
			if (stateFigure != null) {
				this.addFigure(stateFigure, new draw2d.layout.locator.TopLocator(this));
			}
		}
	}
});