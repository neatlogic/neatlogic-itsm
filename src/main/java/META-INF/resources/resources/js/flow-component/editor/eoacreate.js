draw2d.shape.node.FlowStateEoaCreate = draw2d.shape.node.FlowBaseImage.extend({
	COMPONENT_ID : 14,
	NAME : "draw2d.shape.node.FlowStateEoaCreate",
	CNNAME : '创建签报',
	allowBack : true,
	allowRefire : true,
	allowAssign : true,
	allowEoa : false,
	allowGrade : false,
	isValid : function() {
		var connections = this.getConnections();
		if (connections.getSize() > 0) {
			var connHandle = true;
			var targetcount = 0;
			for (var c = 0; c < connections.getSize(); c++) {
				var conn = connections.get(c);
				if (conn.dasharray == '') {
					if (conn.getSource().getParent().getId() == this.getId()) {
						targetcount += 1;
					}
				}
			}

			if (targetcount > 1) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '签报创建节点只能关联一个后置节点';
			}
		}

		var d = this.getUserData();
		if (d == null || typeof d.name == 'undefined' || d.name == '' || typeof d.templateId == 'undefined' || d.templateId == '' || ((d.team == null || d.team == '') && (d.user == null || d.user == ''))) {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请设置节点信息';
		}

		var nextStepList = new draw2d.util.ArrayList();
		var hasHandler = false;
		if (this.getComposite() != null) {// 检查自己是不是被组合节点
			var cl = this.getComposite().getAssignedFigures();
			for (var di = 0; di < cl.getSize(); di++) {
				var dn = cl.get(di);
				if (dn.getName() == 'draw2d.shape.node.FlowStart') {
					this.findAllNextNode(nextStepList, dn);
				}
			}
		} else {
			this.findAllNextNode(nextStepList, this);
		}
		for (var f = 0; f < nextStepList.getSize(); f++) {
			var figure = nextStepList.get(f);
			if (figure.getName() == 'draw2d.shape.node.FlowStateEoaHandle' && figure.getUserData() && figure.getUserData() != null) {
				if (this.groupid == figure.getUserData().groupid) {
					hasHandler = true;
				}
			}
		}
		if (!hasHandler) {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '签报创建节点没有关联对应的签报审批节点。';
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
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateEoaCreate' ? this : this.getParent());
					var name = that.CNNAME, templateId = '', targetStep = null;
					var userData = that.getUserData();
					if (userData) {
						templateId = userData.templateId;
						targetStep = userData.targetStep;
					} else {
						userData = {
							name : name,
							groupid : that.groupid
						};
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.eoacreate.base', userData));

					// var $divTemplatePreview =
					// form.find('#divTemplatePreview');
					var $txtName = form.find('#txtStepName');
					var $sltTemplate = form.find('#sltTemplate');
					var $sltTargetStep = form.find('#sltTargetStep');

					$txtName.val(name);
					$sltTemplate.data('value', templateId);
					$.getJSON('/balantflow/eoatemplate/getTemplateListSelectJson.do', function(data) {
						$sltTemplate.loadSelect(data);
						$sltTemplate.checkselect();
					});

					var prevStepList = new draw2d.util.ArrayList();
					that.findAllPrevNode(prevStepList, that);
					for (var f = 0; f < prevStepList.getSize(); f++) {
						var figure = prevStepList.get(f);
						if (figure.allowEoa) {
							var opt = $('<option value="' + figure.id + '">' + figure.getLabelText() + '</option>');
							$sltTargetStep.append(opt);
						}
					}
					$sltTargetStep.val(targetStep);

					$sltTargetStep.on('change', function() {
						//$(this).find('option').each(function() {
							if ($(this).prop('selected')) {
								$sltTargetStep.append('<option value="' + $(this).val() + '">' + $(this).text() + '</option>');
							}
						//});
					});
					$sltTargetStep.checkselect();
					break;
				case 'dispatch':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateEoaCreate' ? this : this.getParent());
					var userData = that.getUserData();
					if (userData == null) {
						userData = {
							name : that.CNNAME
						};
					}
					var team = userData.team;
					var user = userData.user;
					var assignstep = userData.assignstep;

					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.eoacreate.dispatch', userData));

					var sltTeam = form.find('#sltTeam');
					var sltTeamDispatcher = form.find('#sltTeamDispatcher');
					var sltUser = form.find('#sltUser');
					var sltAssignStep = form.find('#sltAssignStep');
					var sltUserDispatcher = form.find('#sltUserDispatcher');
					var sltCopyUserStep = form.find('#sltCopyUserStep');

					$.getJSON('/balantflow/dispatch/getUserDispatchTemplateListByComponentId.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltUserDispatcher.loadSelectWithDefault(data);
						// sltUserDispatcher.val(userdispatcher);
						sltUserDispatcher.checkselect();
					});

					sltUser.on('change', function() {
						if ($(this).val()) {
							sltTeam.removeAttr('check-type');
						} else {
							sltTeam.attr('check-type', 'required');
						}
					});
					sltUser.checkselect();

					$.getJSON('/balantflow/team/getComponentTeamListJson.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltTeam.loadSelect(data);
						sltTeam.val(team);
						// sltTeam[0].checkselect.reload();
						sltTeam.checkselect();
						getTeamUser(user, true);
					});

					$.getJSON('/balantflow/dispatch/getTeamDispatcher.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltTeamDispatcher.loadSelectWithDefault(data);
						// sltTeamDispatcher.val(teamdispatcher);
						sltTeamDispatcher.checkselect();
					});

					var nextStepList = new draw2d.util.ArrayList();
					var composite = that.getComposite();
					var cs;
					if (composite != null) {
						var cl = composite.getAssignedFigures();
						for (var d = 0; d < cl.getSize(); d++) {
							if (cl.get(d).getName() == 'draw2d.shape.node.FlowStart') {
								cs = cl.get(d);
							}
						}
					} else {
						cs = that;
					}
					cs.findAllNextNode(nextStepList, cs);
					sltAssignStep.children().remove();
					// sltAssignStep.append('<option value="">不选择</option>');
					for (var i = 0; i < nextStepList.getSize(); i++) {
						if (nextStepList.get(i).allowAssign) {
							sltAssignStep.append('<option value="' + nextStepList.get(i).getId() + '">' + nextStepList.get(i).getLabelText() + '</option>');
						}
					}
					sltAssignStep.val(assignstep);
					sltAssignStep.checkselect();

					var prevStepList = new draw2d.util.ArrayList();
					that.findAllPrevNode(prevStepList, that);
					var opt = $('<option value="">不选择</option>');
					sltCopyUserStep.append(opt);
					for (var f = 0; f < prevStepList.getSize(); f++) {
						var figure = prevStepList.get(f);
						if (figure.allowCopyUser) {
							var opt = $('<option value="' + figure.id + '">' + figure.getLabelText() + '</option>');
							sltCopyUserStep.append(opt);
						}
					}
					sltCopyUserStep.val(sltCopyUserStep.data('value'));
					sltCopyUserStep.checkselect();

					sltTeam.change(function() {
						sltTeam.attr('check-type', 'required');
						getTeamUser(null, true);
					});
					if (typeof user == 'object') {
						for (var u = 0; u < user.length; u++) {
							if (user[u]) {
								sltTeam.removeAttr('check-type');
								break;
							}
						}
					}
					break;
				default:
					break;
				}
				;
			}, this),
			x : x,
			y : y,
			items : {
				'form' : {
					name : '基本设置'
				},
				'dispatch' : {
					name : '分派设置'
				}
			}
		});
	}
});