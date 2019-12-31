draw2d.shape.node.FlowStateChildflow = draw2d.shape.node.FlowBaseImage.extend({
	COMPONENT_ID : 5,
	NAME : "draw2d.shape.node.FlowStateChildflow",
	CNNAME : '子流程',
	allowBack : true,
	allowRefire : true,
	allowAssign : true,
	allowEoa : false,
	allowGrade : false,
	isValid : function() {
		var connections = this.getConnections();
		if (connections.getSize() > 0) {
			var targetconvergecount = 0;
			var sourceconvergecount = 0;
			var backcount = 0;
			for (var c = 0; c < connections.getSize(); c++) {
				var conn = connections.get(c);
				if (conn.getTarget().getParent().getId() == this.getId()) {// 连进来
					if (conn.getSource().getParent().getName() == 'draw2d.shape.node.FlowConverge' || conn.getSource().getParent().getName() == 'draw2d.shape.node.FlowCondition') {
						sourceconvergecount += 1;
					}
				} else {// 连出去
					if (conn.dasharray == '-') {
						backcount += 1;
					}
					if (conn.getTarget().getParent().getName() == 'draw2d.shape.node.FlowConverge' || conn.getTarget().getParent().getName() == 'draw2d.shape.node.FlowCondition') {
						targetconvergecount += 1;
					}
				}
			}

			if (targetconvergecount > 1 || sourceconvergecount > 1) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '同一节点只能连接一个汇聚型节点。';
			}

			if (backcount > 1) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '流程子节点只允许连接一个回退节点。';
			}
		}

		var d = this.getUserData();
		if (!d || !d.channel || !d.name) {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请完成基本设置。';
		}
		if ((!d.team && !d.user)) {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请完成分派设置。';
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
					var that = (this.NAME == 'draw2d.shape.node.FlowStateChildflow' ? this : this.getParent());
					var name = that.CNNAME, catalog = '', channel = null, method = '', finishpolicy = '', backpolicy = '';
					var userData = that.getUserData();
					if (userData) {
						channel = userData['channel'];
					} else {
						userData = {
							name : name
						};
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.childflow.base', userData || {}));
					var sltChannel = form.find('#sltChannel');
					var chkFinishPolicy = form.find('#chkFinishPolicy');
					var chkBackPolicy = form.find('#chkBackPolicy');
					var hidFinishPolicy = form.find('#hidFinishPolicy');
					var hidBackPolicy = form.find('#hidBackPolicy');

					chkFinishPolicy.on('click', function() {
						if ($(this).prop('checked')) {
							hidFinishPolicy.val(1);
						} else {
							hidFinishPolicy.val(0);
						}
					});

					chkBackPolicy.on('click', function() {
						if ($(this).prop('checked')) {
							hidBackPolicy.val(1);
						} else {
							hidBackPolicy.val(0);
						}
					});

					$.getJSON('/balantflow/restservices/getChannelApiByServer', function(data) {
						sltChannel.loadSelect(data.Return);
						sltChannel.val(channel);
						sltChannel.checkselect();
					});
					break;
				case 'dispatch':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateChildflow' ? this : this.getParent());
					var name = that.CNNAME, team = null, user = '', relationstep = '', userdispatcher = '', teamdispatcher = '';
					var userData = that.getUserData();
					if (userData != null) {
						team = userData.team;
						user = userData.user;
						userdispatcher = userData.userdispatcher;
						teamdispatcher = userData.teamdispatcher;
						relationstep = userData.relationstep;
					}
					$('#divDialog').modal('show');
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.childflow.dispatch', userData || {}));

					var sltTeam = form.find('#sltTeam');
					var sltTeamDispatcher = form.find('#sltTeamDispatcher');
					var sltUser = form.find('#sltUser');
					var sltUserDispatcher = form.find('#sltUserDispatcher');
					var sltRelationstep = form.find('#sltRelationstep');
					var sltCopyUserStep = form.find('#sltCopyUserStep');

					$.getJSON('/balantflow/dispatch/getUserDispatchTemplateListByComponentId.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltUserDispatcher.loadSelectWithDefault(data);
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
						sltTeam.checkselect();
						getTeamUser(user, true);
					});

					$.getJSON('/balantflow/dispatch/getTeamDispatcher.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltTeamDispatcher.loadSelectWithDefault(data);
						sltTeamDispatcher.checkselect();
					});

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
							if (user[u] == '#{OWNER}') {
								sltTeam.removeAttr('check-type');
							}
						}
					}
					break;
				case 'urgency':
					var that = (this.NAME == 'draw2d.shape.node.FlowStateChildflow' ? this : this.getParent());
					var userData = that.getUserData();
					var urgency, urgencytime, expiredispatcher = '';
					if (userData != null) {
						urgency = userData.urgency;
						urgencytime = userData.urgencytime;
						expiredispatcher = userData.expiredispatcher || '';
					}

					var form = $('#dialogForm');
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.common.urgency'));

					var tbody = form.find('tbody:first');
					var sltExpiredDispatcher = form.find('#sltExpiredDispatcher');

					$.getJSON('/balantflow/dispatch/getExpireDispatcherListByComponentIdJson.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltExpiredDispatcher.loadSelectWithDefault(data);
						sltExpiredDispatcher.val(expiredispatcher);
					});

					$.ajax({
						type : 'GET',
						dataType : 'json',
						url : 'getUrgencyListJson.do',
						success : function(data) {
							tbody.html('');
							for (var u = 0; u < data.length; u++) {
								var utime = '';
								if (urgency != null && urgencytime != null) {
									if (typeof (urgency) == 'object') {
										for (var ur = 0; ur < urgency.length; ur++) {
											if (urgency[ur] == data[u].value) {
												utime = urgencytime[ur];
												break;
											}
										}
									} else {
										if (urgency == data[u].value) {
											utime = urgencytime;
										}
									}
								}
								var $tr = $('<tr><td>' + data[u].text + '<input type="hidden" name="urgency" value="' + data[u].value + '"></td>'
										+ '<td><input check-type="integer_p" integer_p-message="请输入正整数" style="display:inline;width:100px" type="text" class="form-control input-sm" value="' + utime
										+ '" name="urgencytime">分钟</td></tr>');
								tbody.append($tr);
							}
						},
						error : function(ajax, error, errorThrown) {
							showPopMsg.error('获取紧急程度列表失败：<br/>请检查服务端或者网络是否存在问题。' + '<br/>' + errorThrown, 10);
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
				'form' : {
					name : '基本设置'
				},
				'dispatch' : {
					name : '分派设置'
				},
				'urgency' : {
					name : '时限设置'
				}
			}
		});
	}
});