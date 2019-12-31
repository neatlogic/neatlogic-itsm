draw2d.shape.node.FlowStateCase = draw2d.shape.node.FlowBaseImage.extend({
	COMPONENT_ID : 6,
	NAME : "draw2d.shape.node.FlowStateCase",
	CNNAME : '事件处理',
	allowBack : true,
	allowRefire : true,
	allowAssign : true,
	allowEoa : false,
	allowGrade : true,
	allowCopyUser : true,
	isValid : function() {
		var connections = this.getConnections();
		if (connections.getSize() > 0) {
			var targetconvergecount = 0;
			var sourceconvergecount = 0;
			for (var c = 0; c < connections.getSize(); c++) {
				var conn = connections.get(c);
				if (conn.getTarget().getParent().getId() == this.getId()) {
					if (conn.getSource().getParent().getName() == 'draw2d.shape.node.FlowConverge' || conn.getSource().getParent().getName() == 'draw2d.shape.node.FlowCondition') {
						sourceconvergecount += 1;
					}
				} else {
					if (conn.getTarget().getParent().getName() == 'draw2d.shape.node.FlowConverge' || conn.getTarget().getParent().getName() == 'draw2d.shape.node.FlowCondition') {
						targetconvergecount += 1;
					}
				}
			}

			if (targetconvergecount > 1 || sourceconvergecount > 1) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '同一节点只能连接一个汇聚型节点。';
			}
		}

		var d = this.getUserData();
		if (d == null || d.name == '') {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请完成基本设置。';
		}
		if (((d.team == null || d.team == '') && (d.user == null || d.user == ''))) {
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
				case "base":
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateCase' ? this : this.getParent());
					var name = that.CNNAME, proptype = '', propid = '';
					var userData = that.getUserData();
					var allowtaskover = '', needworktime = '', worktimedispatcher = '', noteContent = '';
					if (userData != null) {
						name = userData.name || that.CNNAME;
						proptype = userData.proptype;
						propid = userData.propid;
						noteContent = userData.noteContent ? userData.noteContent : '';
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.casehandle.base'));

					var txtStepName = form.find('#txtStepName');
					var sltProp = form.find('#sltProp');
					var sltPropType = form.find('#sltPropType');
					txtStepName.val(name);

					$.getJSON('/balantflow/channel/getAllChannelTypeJson.do', function(data) {
						var n = $.map(data, function(v, i) {
							return {
								"text" : v.name,
								"value" : v.id
							};
						});
						sltPropType.appendSelect(n);
						sltPropType.val(proptype);
					});

					sltPropType.change(function() {
						$.getJSON('/balantflow/channel/getPropDefListByType.do?type=' + sltPropType.val(), function(data) {
							sltProp.loadSelectWithDefault(data);
						});
					});

					if (proptype) {
						$.getJSON('/balantflow/channel/getPropDefListByType.do?type=' + proptype, function(data) {
							sltProp.loadSelectWithDefault(data);
							sltProp.val(propid);
						});
					}

					editor = CKEDITOR.replace('txtNoteContent', {
						extraPlugins : ''
					});
					editor.setData(noteContent);
					$('#divDialog').unbind().bind('beforeSave', function() {
						if (editor) {
							editor.updateElement();
						}
					});
					break;
				case 'dispatch':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateCase' ? this : this.getParent());
					var userData = that.getUserData();
					if (userData == null) {
						userData = {
							name : that.CNNAME
						};
					}
					var team = userData.team;
					var user = userData.user;

					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.casehandle.dispatch', userData));

					var sltTeam = form.find('#sltTeam');
					var sltTeamDispatcher = form.find('#sltTeamDispatcher');
					var sltUser = form.find('#sltUser');
					var sltUserDispatcher = form.find('#sltUserDispatcher');
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
							if (user[u]) {
								sltTeam.removeAttr('check-type');
								break;
							}
						}
					}
					break;
				case 'seniorfunc':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateCase' ? this : this.getParent());
					var userData = that.getUserData();
					var worktime = '', worktimedispatcher = '';
					if (userData != null) {
						worktime = userData.worktime;
						worktimedispatcher = userData.worktimedispatcher;
					}
					$('#divDialog').modal('show');
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);

					form.html(xdoT.render('balantflow.editflow.casehandle.transfer'));

					var sltAllowWorkTime = form.find('#sltAllowWorkTime');
					var sltWorktimedispatcher = form.find('#sltWorktimedispatcher');

					$.getJSON('/balantflow/worktime/getWorktime.json', function(data) {
						sltAllowWorkTime.loadSelect(data);
						sltAllowWorkTime.val(worktime);
						sltAllowWorkTime[0].checkselect.reload();
					});

					$.getJSON('/balantflow/dispatch/getWorktimeDispatch.json?componentId=' + that.COMPONENT_ID, function(data) {
						sltWorktimedispatcher.loadSelectWithDefault(data);
						sltWorktimedispatcher.val(worktimedispatcher);
					});

					sltAllowWorkTime.checkselect();
					break;
				case 'script':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateCase' ? this : this.getParent());
					var script = '', propid = '';
					var userData = that.getUserData();
					if (userData != null) {
						script = userData.script;
						propid = userData.propid;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);

					form.html(xdoT.render('balantflow.editflow.common.script'));

					$('#divDefaultMethodItem').html('<ul><li>隐藏事件归档类型:hideCaseType()</li><li>隐藏事件关闭代码:hideCloseCode()</li><li>隐藏事件解决方案:hideSolution()</li></ul>');

					if (!propid) {
						$('#divCustomItem').text('暂无可控制的自定义属性。');
					} else {
						$.getJSON('/balantflow/prop/getPropByTemplateIdJson.do?templateId=' + propid, function(data) {
							var str = '';
							if (data && data.length > 0) {
								for ( var i in data) {
									str += '<span style="background:#f4f4f4;display:inline-block;padding:2px;margin:2px;border:1px solid #ccc">id：custom_' + data[i].propid + ';名称：' + data[i].propname + ';类型：' + data[i].proptype + '</span>';
								}
							} else {
								str = '没有找到可控制的自定义属性。'
							}
							$('#divCustomItem').html(str);
						});
					}

					var txtScript = form.find('#txtScript');

					var scriptEditor = CodeMirror.fromTextArea($('#txtScript')[0], {
						mode : "htmlmixed",
						lineNumbers : true
					});
					if (script) {
						scriptEditor.setValue(script);
					}
					scriptEditor.setSize('100%', 300);
					$('#divDialog').unbind().bind('beforeSave', function() {
						txtScript.val(scriptEditor.getValue());
					});
					break;
				case 'urgency':
					var userData = (this.NAME == 'draw2d.shape.node.FlowStateCase' ? this.getUserData() : this.getParent().getUserData());
					var that = (this.NAME == 'draw2d.shape.node.FlowStateCase' ? this : this.getParent());
					var urgency, urgencytime, expiredispatcher;
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
					$('#divDialog').data('obj', this.NAME == 'draw2d.shape.node.FlowStateCase' ? this : this.getParent());
					$('#dialogTitle').html('时限设置');
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
										+ '" name="urgencytime">&nbsp;分钟</td></tr>');
								tbody.append($tr);
							}
						},
						error : function(ajax, error, errorThrown) {
							showPopMsg.error('获取紧急程度列表失败：<br/>请检查服务端或者网络是否存在问题。' + '<br/>' + errorThrown, 10);
						}
					});
					break;
				case 'transfer':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateCase' ? this : this.getParent());
					var channel = [];
					var userData = that.getUserData();
					if (userData != null) {
						channel = userData.channel;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.common.transfer'));

					var sltChannel = form.find('#sltChannel');
					$.getJSON('/balantflow/channel/getActiveChannelListJson.do', function(data) {
						sltChannel.loadSelect(data);
						sltChannel.val(channel);
						sltChannel[0].checkselect.reload();
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
				},
				'dispatch' : {
					name : '分派设置'
				},
				'seniorfunc' : {
					name : '转交设置'
				},
				'script' : {
					name : '脚本设置'
				},
				'urgency' : {
					name : '时限设置'
				},
				'transfer' : {
					name : '转报设置'
				}
			}
		});
	}
});