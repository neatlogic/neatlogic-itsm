draw2d.shape.node.FlowStateEoaHandle = draw2d.shape.node.FlowBaseImage.extend({
	NAME : "draw2d.shape.node.FlowStateEoaHandle",
	CNNAME : '处理签报',
	allowBack : true,
	allowRefire : false,
	allowAssign : false,
	allowEoa : false,
	allowGrade : false,
	isValid : function() {
		var connections = this.getConnections();
		if (connections.getSize() > 0) {
			var targetcount = 0;
			var connCreate = true;
			var sourcecount = 0;
			var backcount = 0;
			for (var c = 0; c < connections.getSize(); c++) {
				var conn = connections.get(c);
				if (conn.dasharray == '') {
					if (conn.getSource().getParent().getId() == this.getId()) {
						targetcount += 1;
					} else {
						sourcecount += 1;
					}
				} else if (conn.dasharray == '-') {
					if (conn.getSource().getParent().getId() == this.getId()) {
						backcount += 1;
					}
				}
			}

			if (targetcount > 1) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '处理签报节点只能关联一个后置节点';
			}

			if (backcount > 1) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '处理签报节点只允许连接一个回退节点';
			}
		}

		var prevStepList = new draw2d.util.ArrayList();
		var groupIdList = new draw2d.util.ArrayList();
		this.findAllPrevNode(prevStepList, this);
		for (var f = 0; f < prevStepList.getSize(); f++) {
			var figure = prevStepList.get(f);
			if (figure.getName() == 'draw2d.shape.node.FlowStateEoaCreate') {
				groupIdList.add(figure.groupid);
			}else if(figure.getComposite() != null){
				var cl = figure.getComposite().getAssignedFigures();
				for (var di = 0; di < cl.getSize(); di++) {
					var dn = cl.get(di);
					if (dn.getName() == 'draw2d.shape.node.FlowStateEoaCreate') {
						groupIdList.add(dn.groupid);
						break;
					}
				}
			}
		}

		var d = this.getUserData();
		if (d == null || typeof d.name == 'undefined' || d.name == '' || typeof d.policy == 'undefined' || d.policy == '' || typeof d.groupid == 'underfined' || d.groupid == '') {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请设置节点信息。';
		} else if (d.groupid != '') {
			if (!groupIdList.contains(d.groupid)) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '签报审批节点没有关联对应的签报创建节点。';
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
				case "form":
					var that = (this.NAME == 'draw2d.shape.node.FlowStateEoaHandle' ? this : this.getParent());
					var form = $('#dialogForm');
					var name = that.CNNAME, policy = '', groupid;
					var userData = that.getUserData();
					if (userData != null) {
						name = userData.name || that.CNNAME;
						policy = userData.policy;
						groupid = userData.groupid;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.eoahandle.base'));
					var $txtStepName = form.find('#txtStepName');
					var $sltPolicy = form.find('#sltPolicy');
					var $sltGroupId = form.find('#sltGroupId');
					$txtStepName.val(name);
					$sltPolicy.val(policy);

					var prevStepList = new draw2d.util.ArrayList();
					that.findAllPrevNode(prevStepList, that);
					for (var f = 0; f < prevStepList.getSize(); f++) {
						var figure = prevStepList.get(f);
						if (figure.getName() == 'draw2d.shape.node.FlowStateEoaCreate') {
							var opt = $('<option value="' + figure.groupid + '">' + figure.getLabelText() + '</option>');
							if (groupid == figure.groupid) {
								opt.prop('selected', true);
							}
							$sltGroupId.append(opt);
						}else if(figure.getComposite() != null){
							var cl = figure.getComposite().getAssignedFigures();
							for (var d = 0; d < cl.getSize(); d++) {
								var dn = cl.get(d);
								if (dn.getName() == 'draw2d.shape.node.FlowStateEoaCreate') {
									var opt = $('<option value="'+ dn.groupid +'">'+ dn.getLabelText() +'</option>');
									if(groupid == dn.groupid){
										opt.prop('selected', true);
									}
									$sltGroupId.append(opt);
									break;
								}
							}
						}
					}
					break;
				default:
					break;
				}
			}, this),
			x : x,
			y : y,
			items : {
				'form' : {
					name : '基本设置'
				}
			}
		});
	}
});