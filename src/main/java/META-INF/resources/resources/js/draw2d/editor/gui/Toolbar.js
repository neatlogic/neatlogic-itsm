editor.Toolbar = Class.extend({

	init : function(elementId, view) {
		this.html = $("#" + elementId);
		this.view = view;

		// register this class as event listener for the canvas
		// CommandStack. This is required to update the state of
		// the Undo/Redo Buttons.
		//
		view.getCommandStack().addEventListener(this);

		// Register a Selection listener for the state hnadling
		// of the Delete Button
		//
		view.addSelectionListener(this);

	},

	setRedo : function() {
		this.view.getCommandStack().redo();
	},

	setUndo : function() {
		this.view.getCommandStack().undo();
	},

	deleteItem : function() {
		this.view.getCommandStack().startTransaction();
		var nodes = this.view.getSelection().getAll();
		if (nodes != null && nodes.getSize() > 0) {
			for ( var n = 0; n < nodes.getSize(); n++) {
				var node = nodes.get(n);
				var delList = new draw2d.util.ArrayList();
				if (typeof node.groupid != 'undefined' && node.groupid != '') {
					var figures = this.view.getFigures();
					for ( var f = 0; f < figures.getSize(); f++) {
						var figure = figures.get(f);
						if (figure.groupid && figure.groupid == node.groupid) {
							delList.add(figure);
						}
					}
				} else {
					delList.add(node);
				}
				for ( var d = 0; d < delList.getSize(); d++) {
					var dn = delList.get(d);
					if (this.view.getFigure(dn.getId()) != null || this.view.getLine(dn.getId())) {
						var command = new draw2d.command.CommandDelete(dn);
						this.view.getCommandStack().execute(command);
					}
				}
			}
		}
		this.view.getCommandStack().commitTransaction();
	},

	saveCanvas : function(btn) {
		app.saveFlow(btn);
	},
	/**
	 * 画板背景
	 * 
	 * @param t
	 */
	setCanvasBg : function(t) {
		app.setCanvasBackground(t);
	},

	/**
	 * 箭头样式
	 * 
	 * @param arrowStyle
	 */
	setConnectionArrow : function(arrowStyle) {
		switch (arrowStyle) {
		case 1:
			app.sourceDecorator = null;
			app.setLineTargetDecorator("draw2d.decoration.connection.ArrowDecorator");
			break;
		case 2:
			app.sourceDecorator = null;
			app.targetDecorator = null;
			break;
		case 3:
			app.setLineTargetDecorator("draw2d.decoration.connection.ArrowDecorator");
			app.setLineSourceDecorator("draw2d.decoration.connection.ArrowDecorator");
			break;
		}
	},

	setConnectionStyle : function(s) {
		if (s != null) {
			app.lineStyle = s;
		}
	},

	/**
	 * 放大缩小
	 * 
	 * @param t
	 */
	setViewZoom : function(t) {
		switch (t) {
		case 1:
			app.view.setZoom(app.view.getZoom() * 1.2, true);
			break;
		case 2:
			app.view.setZoom(1.0, true);
			break;
		case 3:
			app.view.setZoom(app.view.getZoom() * 0.8, true);
			break;
		}
	},
	/**
	 * 线条颜色
	 * 
	 * @param color
	 */
	setConnectionColor : function(color) {
		app.connColor = color;
		this.resetConnection();
	},

	resetConnection : function() {
		draw2d.Connection.createConnection = function(sourcePort, targetPort) {
			return app.createConnection(sourcePort, targetPort);
		};
	},

	enableSaveButton : function() {
		this.saveButton.attr("disabled", false);
	},

	/**
	 * @method Called if the selection in the cnavas has been changed. You must
	 *         register this class on the canvas to receive this event.
	 * 
	 * @param {draw2d.Figure}
	 *            figure
	 */
	onSelectionChanged : function(figure) {
		// this.deleteButton.attr( "disabled", figure===null );
	},

	/**
	 * @method Sent when an event occurs on the command stack.
	 *         draw2d.command.CommandStackEvent.getDetail() can be used to
	 *         identify the type of event which has occurred.
	 * 
	 * @template
	 * 
	 * @param {draw2d.command.CommandStackEvent}
	 *            event
	 */
	stackChanged : function(event) {
		/*
		 * this.undoButton.attr("disabled", !event.getStack().canUndo() );
		 * this.redoButton.attr("disabled", !event.getStack().canRedo() );
		 * 
		 * this.saveButton.attr("disabled", !event.getStack().canUndo() &&
		 * !event.getStack().canRedo() );
		 */
	}
});