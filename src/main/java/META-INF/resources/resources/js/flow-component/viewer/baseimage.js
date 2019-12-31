draw2d.shape.node.FlowBaseImage = draw2d.shape.basic.Image.extend({
	init : function(path, width, height) {
		this._super(path, width, height);
		this.setResizeable(false);
		this.label = new draw2d.shape.basic.Label('');
		this.label.setStroke(0);
		this.label.onContextMenu = this.onContextMenu;
		this.setCssClass("deviceNode");
		this.addFigure(this.label, new draw2d.layout.locator.BottomLocator(this));

		// 4个连接点，混合型
		this.createPort("hybrid", new draw2d.layout.locator.BottomLocator(this));
		this.createPort("hybrid", new draw2d.layout.locator.MyLeftLocator(this));
		this.createPort("hybrid", new draw2d.layout.locator.TopLocator(this));
		this.createPort("hybrid", new draw2d.layout.locator.MyRightLocator(this));
	},
	getName : function() {
		return this.NAME;
	},
	onDoubleClick : function() {
	},
	onMouseEnter : function() {
		this.isin = true;
		if (this.getTaskId() && this.getStepId()) {
			var zoom = this.getCanvas().getZoom();
			var x = this.getX() / zoom;
			var y = this.getY() / zoom;
			var w = this.getWidth() / zoom;
			var h = this.getHeight();
			showStepInfo(this, this.getTaskId(), this.getStepId(), x + w + 20, y + h);
		}
	},
	onMouseLeave : function() {
		this.isin = false;
		if (this.getTaskId() && this.getStepId()) {
			hideStepInfo();
		}
	},
	setStepId : function(stepid) {
		this.stepid = stepid;
	},
	getStepId : function() {
		return this.stepid;
	},
	setTaskId : function(taskid) {
		this.taskid = taskid;
	},
	getTaskId : function() {
		return this.taskid;
	},
	setPersistentAttributes : function(memento) {
		this._super(memento);
		if (typeof memento.labels.text !== "undefined") {
			this.setLabelText(memento.labels.text);
		}
		if (typeof memento.stepid != 'undefined') {
			this.setStepId(memento.stepid);
		}
		if (typeof memento.taskid != 'undefined') {
			this.setTaskId(memento.taskid);
		}
		if (typeof memento.status != 'undefined' && !memento.composite) {
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