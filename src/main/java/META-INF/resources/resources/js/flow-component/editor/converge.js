draw2d.shape.node.FlowConverge = draw2d.shape.node.FlowBaseDiamond.extend({
	NAME : 'draw2d.shape.node.FlowConverge',
	CNNAME : '汇聚/分流',
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
				if (conn.getSource().getParent().getName() == 'draw2d.shape.node.FlowConverge') {
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
					var that = (this.NAME == 'draw2d.shape.node.FlowConverge' ? this : this.getParent());
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
					form.html(xdoT.render('balantflow.editflow.converge.base'));
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