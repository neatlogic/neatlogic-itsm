draw2d.shape.node.FlowFConverge = draw2d.shape.node.FlowBaseImage.extend({
	NAME : 'draw2d.shape.node.FlowFConverge',
	CNNAME : '优先汇聚',
	allowBack : false,
	allowRefire : false,
	allowAssign : false,
	allowEoa : false,
	allowGrade : false,
	allowCondition : false,
	isValid : function() {
		var color = new draw2d.util.Color("#ff0000");
		var connections = this.getConnections();
		for (var i = 0; i < connections.getSize(); i++) {
			var conn = connections.get(i);
			if (conn.getTarget().getParent().getId() == this.getId()) {
				if (conn.getSource().getParent().getName().indexOf('FlowConverge') > -1 || conn.getSource().getParent().getName().indexOf('FlowFConverge') > -1 || conn.getSource().getParent().getName().indexOf('FlowCondition') > -1) {
					conn.setColor(color);
					return '汇聚/分流节点不能互相关联。';
				}
			}
		}

		for (var c = 0; c < this.getChildren().getSize(); c++) {
			if (this.getChildren().get(c).NAME == 'draw2d.shape.basic.Image') {
				this.removeFigure(this.stateFigure);
			}
		}
		return true;
	},
	init : function(path, width, height, label) {
		this._super(path, width, height);
		this.stateFigure = new draw2d.shape.basic.Image('../resources/images/draw2d/warn.png', 16, 16);
		this.setResizeable(false);
		var txt;
		if (typeof label === "string") {
			txt = label;
		}
		if (typeof txt === "undefined") {
			this.label = new draw2d.shape.basic.Label(this.CNNAME);
		} else {
			this.label = new draw2d.shape.basic.Label(txt);
		}
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
					var that = (this.NAME == 'draw2d.shape.node.FlowFConverge' ? this : this.getParent());
					var userData = that.getUserData();
					var name = that.CNNAME;
					if (userData != null) {
						name = userData.name || that.CNNAME;
					}

					var form = $('#dialogForm');
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.fconverge.base'));
					var txtStepName = form.find('#txtStepName');
					txtStepName.val(name);
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
					name : '编辑信息'
				}
			}
		});
	}
});