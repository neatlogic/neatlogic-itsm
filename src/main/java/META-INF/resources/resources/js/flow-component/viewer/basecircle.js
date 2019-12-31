draw2d.shape.node.FlowBaseCircle = draw2d.shape.basic.Circle.extend({
	init : function() {
		this._super();
		this.label = new draw2d.shape.basic.Label(this.NAME);
		this.label.setStroke(0);
		var color = new draw2d.util.Color(this.COLOR);
		this.setBackgroundColor(color);
		this.addFigure(this.label, new draw2d.layout.locator.BottomLocator(this));
		this.createPort(this.PORTTYPE, new draw2d.layout.locator.BottomLocator(this));
		this.createPort(this.PORTTYPE, new draw2d.layout.locator.MyLeftLocator(this));
		this.createPort(this.PORTTYPE, new draw2d.layout.locator.TopLocator(this));
		this.createPort(this.PORTTYPE, new draw2d.layout.locator.MyRightLocator(this));
	},
	onDoubleClick: function(){},
	setPersistentAttributes : function(memento) {
		this._super(memento);
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
	},
	setLabelText : function(txt) {
		this.labelText = txt;
		this.label.setText(this.labelText);
	}
});