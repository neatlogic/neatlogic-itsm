draw2d.shape.node.FlowStateCaseConfirm = draw2d.shape.node.FlowBaseImage.extend({
	NAME : "draw2d.shape.node.FlowStateCaseConfirm",
	CNNAME : '事件确认',
	allowBack : true,
	allowRefire : false,
	allowAssign : false,
	allowEoa : false,
	allowGrade : false,
	isValid : function() {
		var connections = this.getConnections();
		var color = new draw2d.util.Color("#ff0000");
		if (connections.getSize() > 0) {
			var sourcecount = 0;
			var targetcount = 0;
			var hascase = true;
			for (var c = 0; c < connections.getSize(); c++) {
				var conn = connections.get(c);
				if (conn.dasharray == '') {
					if (conn.getTarget().getParent().getId() == this.getId()) {
						sourcecount += 1;
						if (conn.getSource().getParent().getName() != 'draw2d.shape.node.FlowStateCase') {
							hascase = false;
						}
					} else {
						targetcount += 1;
					}
				}
			}

			if (targetcount > 1) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '事件确认节点只能关联一个后置节点。';
			}
		}

		var prevStepList = new draw2d.util.ArrayList();
		var groupIdList = new draw2d.util.ArrayList();
		this.findAllPrevNode(prevStepList, this);
		for (var f = 0; f < prevStepList.getSize(); f++) {
			var figure = prevStepList.get(f);
			if (figure.getName() == 'draw2d.shape.node.FlowStateCase') {
				groupIdList.add(figure.groupid);
			}
		}

		var d = this.getUserData();
		if (d == null || d.name == '' || typeof d.finishuser == 'underfined' || d.finishuser == '' || typeof d.groupid == 'underfined' || d.groupid == '') {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请设置节点信息。';
		}else if(d.groupid != ''){
			if(!groupIdList.contains(d.groupid)){
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '事件确认节点没有关联对应的事件处理节点。';
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
				case "base":
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateCaseConfirm' ? this : this.getParent());
					var name = that.CNNAME, finishuser = '', groupid = '', noteContent = '';
					var userData = that.getUserData();
					if (userData != null) {
						name = userData.name || that.CNNAME;
						finishuser = userData.finishuser;
						groupid = userData.groupid;
						noteContent = userData.noteContent;
					}
					$('#divDialog').modal('show');
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.caseconfirm.base'));
					var txtStepName = form.find('#txtStepName');
					var sltFinishUser = form.find('#sltFinishUser');
					var sltGroupId = form.find('#sltGroupId');

					txtStepName.val(name);
					sltFinishUser.val(finishuser);

					var prevStepList = new draw2d.util.ArrayList();
					that.findAllPrevNode(prevStepList, that);
					for (var f = 0; f < prevStepList.getSize(); f++) {
						var figure = prevStepList.get(f);
						if (figure.getName() == 'draw2d.shape.node.FlowStateCase') {
							var opt = $('<option value="' + figure.groupid + '">' + figure.getLabelText() + '</option>');
							if (groupid == figure.groupid) {
								opt.prop('selected', true);
							}
							sltGroupId.append(opt);
						}
					}

					var editor = CKEDITOR.replace('txtNoteContent', {
						extraPlugins : ''
					});
					editor.setData(noteContent);
					$('#divDialog').unbind().bind('beforeSave', function() {
						if (editor) {
							editor.updateElement();
						}
					});
					break;
				default:
					break;
				}
				;
			}, this),
			x : x,
			y : y,
			items : {
				'base' : {
					name : '基本设置'
				}
			}
		});
	}
});