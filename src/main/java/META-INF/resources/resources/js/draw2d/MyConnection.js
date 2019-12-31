/*****************************************
 *   Library is under GPL License (GPL)
 *   Copyright (c) 2013 Techsure.com
 ****************************************/
/**
 * @class MyConnection
 * 
 * A simple Connection with a label wehich sticks in the middle of the
 * connection..
 * 
 * @author wangtc
 * @extend draw2d.Connection
 */
var MyConnection = draw2d.Connection.extend({
	NAME : "MyConnection",
	init : function(route, arrow) {
		this._super();
		this.label = new draw2d.shape.basic.Label("");
		this.label.setStroke(0);
		var color = new draw2d.util.Color('#666666');
		this.label.setFontColor(color);
		this.addFigure(this.label, new draw2d.layout.locator.ManhattanMidpointLocator(this));
	},
	getName : function() {
		return this.NAME;
	},
	resetColor : function() {
		if (this.originalColor != undefined) {
			this.setColor(this.originalColor);
		}
	},
	setOriginalColor : function(color) {
		this.originalColor = color;
	},
	getPersistentAttributes : function() {
		if (typeof this.groupid != 'undefined' && this.groupid != null) {
			var d = {};
			d.groupid = this.groupid;
			this.setUserData($.extend(this.getUserData(), d));
		}
		var memento = this._super();
		if (typeof this.dasharray === "string") {
			memento.dasharray = this.dasharray;
		}

		if (this.dynamicLine == 1) {
			memento.dynamicLine = this.dynamicLine;
		}
		if (this.sourceDecorator != null) {
			memento.source.sourceDecorator = this.sourceDecorator.NAME;
		}
		if (this.targetDecorator != null) {
			memento.target.targetDecorator = this.targetDecorator.NAME;
		}
		memento.labels = this.label.getPersistentAttributes();
		return memento;
	},
	setPersistentAttributes : function(memento) {
		this._super(memento);

		if (typeof memento.dasharray !== "undefined") {
			this.setDashArray(memento.dasharray);
		}

		if (typeof memento.dynamicLine !== "undefined") {
			this.setDynamic(memento.dynamicLine);
		}

		if (typeof memento.color !== "undefined") {
			this.setOriginalColor(memento.color);
		}

		if (typeof memento.source.sourceDecorator !== "undefined") {
			this.setSourceDecorator(eval("new " + memento.source.sourceDecorator + "(10,7)"));
		}
		if (typeof memento.target.targetDecorator !== "undefined") {
			this.setTargetDecorator(eval("new " + memento.target.targetDecorator + "(10,7)"));
		}
		if (typeof memento.labels.text !== "undefined") {
			this.setLabelText(memento.labels.text);
		}
		if (typeof memento.userData.groupid != 'underfined') {
			this.groupid = memento.userData.groupid;
		}
	},
	setDynamic : function() {
		this.dynamicLine = 1;

		// 虚线动态效果

		this.dasharray = '-';
		this.setLineAnimation();

	},
	setStatic : function() {
		this.setDashArray("");
		if (this.dynamicLineInterval != null) {
			clearInterval(this.dynamicLineInterval);
		}
	},
	/**
	 * 重置setInterval，使其可接受参数
	 * 
	 * @param dom
	 */
	setLineAnimation : function() {
		// var itv = setInterval("this.changeDasharray(line)", 500);

		var mySetInterval = setInterval;
		window.setInterval = function(callback, interval) {
			var args = Array.prototype.slice.call(arguments, 2);
			function callFn() {
				callback.apply(null, args);
			}
			return mySetInterval(callFn, interval);
		}
		this.dynamicLineInterval = window.setInterval(this.changeDasharray, 200, this);
	},
	/**
	 * 切换虚线样式
	 * 
	 * @param line
	 */
	changeDasharray : function(line) {
		if (line.dasharray == "-") {
			line.dasharray = "-.";
			line.repaint();
		} else if (line.dasharray == "-.") {
			line.dasharray = "-";
			line.repaint();
		}
	},
	/**
	 * 设置虚线
	 * 
	 * @param d
	 */
	setDashLine : function(ds) {
		if (ds != null && typeof ds === "string") {
			this.dasharray = ds;
			// this.repaint();
			console.info("set line 属性:" + this.dasharray);
		}
	},
	// 取消虚线
	cannelDashLine : function() {
		this.dasharray = "";
		// this.repaint();
	},
	setData : function(d) {
		this.setUserData(d);
		if (d.name != null && d.name != '') {
			this.setLabelText(d.name);
		} else {
			this.setLabelText('');
		}
	},
	setLabelText : function(txt) {
		this.labelText = txt;
		this.label.setText(this.labelText);
	},
	onDragStart : function(x, y, shiftKey, ctrlKey) {
		var color = new draw2d.util.Color('#999999');
		this.setColor(color);
	},
	onContextMenu : function(x, y) {
		$.contextMenu({
			selector : 'body',
			events : {
				hide : function() {
					$.contextMenu('destroy');
				}
			},
			callback : $.proxy(function(key, options) {
				switch (key) {
				case 'info':
					var form = $('#dialogForm');
					var name = '';
					if (this.getUserData() != null) {
						name = this.getUserData().name;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', this);
					$('#dialogTitle').html('编辑信息');
					var html = xdoT.render('balantflow.editflow.line', this.getUserData() || {});
					form.html(html);
					break;
				default:
					break;
				}
				;
			}, this),
			x : x,
			y : y,
			items : {
				'info' : {
					name : "编辑信息"
				}
			// "rule": {name: "编辑流转规则"}
			}
		});
	}
});
