draw2d.shape.node.FlowStateProblemCreate = draw2d.shape.node.FlowBaseImage.extend({
	COMPONENT_ID : 11,
	NAME : "draw2d.shape.node.FlowStateProblemCreate",
	CNNAME : '问题创建',
	allowBack : false,
	allowRefire : true,
	allowAssign : true,
	allowEoa : false,
	allowGrade : true,
	isValid : function() {
		var connections = this.getConnections();
		if (connections.getSize() > 0) {
			var connHandle = true;
			var targetcount = 0;
			var sourcecount = 0;
			for (var c = 0; c < connections.getSize(); c++) {
				var conn = connections.get(c);
				if (conn.dasharray == '') {
					if (conn.getTarget().getParent().getId() == this.getId()) {
						sourcecount += 1;
					} else {
						targetcount += 1;
						if (conn.getTarget().getParent().getName() != 'draw2d.shape.node.FlowStateProblemHandle') {
							connHandle = false;
						}
					}
				}
			}

		}

		var d = this.getUserData();
		if (d == null || d.name == null || d.name == '' || ((d.team == null || d.team == '') && (d.user == null || d.user == ''))) {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请设置节点信息。';
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
					var that = (this.NAME == 'draw2d.shape.node.FlowStateProblemCreate' ? this : this.getParent());
					var name = that.CNNAME, proptype = '', propid = '', noteContent = '';
					var userData = that.getUserData();
					var assignstep = null;
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
					form.html(xdoT.render('balantflow.editflow.problemcreate.base'));

					var txtStepName = form.find('#txtStepName');
					var hidGroupId = form.find('#hidGroupId');
					var sltPropType = form.find('#sltPropType');
					var sltProp = form.find('#sltProp');

					txtStepName.val(name);
					hidGroupId.val(that.groupid);

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
				case 'urgency':
					var userData = (this.NAME == 'draw2d.shape.node.FlowStateProblemCreate' ? this.getUserData() : this.getParent().getUserData());
					var urgency, urgencytime;
					if (userData != null) {
						urgency = userData.urgency;
						urgencytime = userData.urgencytime;
					}

					var form = $('#dialogForm');
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', this.NAME == 'draw2d.shape.node.FlowStateProblemCreate' ? this : this.getParent());
					$('#dialogTitle').html('编辑处理时限');
					form.html(xdoT.render('balantflow.editflow.common.urgency'));

					var tbody = form.find('tbody:first');
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
										+ '<td style="text-align:center"><input check-type="integer_p" integer_p-message="请输入正整数" style="display:inline;width:100px" type="text" class="form-control input-sm" value="' + utime
										+ '" name="urgencytime">分钟</td></tr>');
								tbody.append($tr);
							}
						},
						error : function(ajax, error, errorThrown) {
							showPopMsg.error('获取紧急程度列表失败：<br/>请检查服务端或者网络是否存在问题。' + '<br/>' + errorThrown, 10);
						}
					});
					break;
				case 'dispatch':
					var that = (this.NAME == 'draw2d.shape.node.FlowStateProblemCreate' ? this : this.getParent());
					var userData = that.getUserData();
					var team = null, user = '', assign = '', teamdispatcher = '', userdispatcher = '', assignstep = null;
					if (userData != null) {
						team = userData.team;
						user = userData.user;
						assign = userData.assign;
						teamdispatcher = userData.teamdispatcher;
						userdispatcher = userData.userdispatcher;
						assignstep = userData.assignstep;
					}

					var form = $('#dialogForm');
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', this.NAME == 'draw2d.shape.node.FlowStateProblemCreate' ? this : this.getParent());
					$('#dialogTitle').html('分派设置');
					form.html(xdoT.render('balantflow.editflow.problemcreate.dispatch'));

					var sltTeam = form.find('#sltTeam');
					var sltAssign = form.find('#sltAssign');
					var sltUser = form.find('#sltUser');
					var sltTeamDispatcher = form.find('#sltTeamDispatcher');
					var sltUserDispatcher = form.find('#sltUserDispatcher');
					var sltAssignStep = form.find('#sltAssignStep');

					var nextStepList = new draw2d.util.ArrayList();
					this.findAllNextNode(nextStepList, this);
					sltAssignStep.children().remove();
					for (var i = 0; i < nextStepList.getSize(); i++) {
						if (nextStepList.get(i).allowAssign) {
							sltAssignStep.append('<option value="' + nextStepList.get(i).getId() + '">' + nextStepList.get(i).getLabelText() + '</option>');
						}
					}
					sltAssignStep.val(assignstep);
					sltAssignStep.checkselect();

					$.getJSON('/balantflow/dispatch/getTeamDispatcher.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltTeamDispatcher.loadSelectWithDefault(data);
						sltTeamDispatcher.val(teamdispatcher);
						sltTeamDispatcher.checkselect();
					});

					$.getJSON('/balantflow/dispatch/getUserDispatchTemplateListByComponentId.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltUserDispatcher.loadSelectWithDefault(data);
						sltUserDispatcher.val(userdispatcher);
						sltUserDispatcher.checkselect();
					});

					sltTeam.change(function() {
						getTeamUser(null, true);
					});

					$.getJSON('/balantflow/module/balantproblem/problem/getProblemTeam.do', function(data) {
						sltTeam.loadSelectWithDefault(data);
						sltTeam.val(team);
						getTeamUser(user, true);
					}).fail(function() {
						showPopMsg.error('获取处理组列表失败：<br/>请检查服务端或者网络是否存在问题。');
					});
					sltUser.on('change', function() {
						if ($(this).val() == '#{OWNER}') {
							sltTeam.removeAttr('check-type');
						} else {
							sltTeam.attr('check-type', 'required');
						}
					});
					if (typeof user == 'object') {
						for (var u = 0; u < user.length; u++) {
							if (user[u] == '#{OWNER}') {
								sltTeam.removeAttr('check-type');
							}
						}
					}
					sltUser.checkselect();
					break;
				case 'script':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateProblemCreate' ? this : this.getParent());
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

					scriptEditor = CodeMirror.fromTextArea($('#txtScript')[0], {
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

				'script' : {
					name : '脚本设置'
				},
				'urgency' : {
					name : '时限设置'
				}
			}
		});
	}
});