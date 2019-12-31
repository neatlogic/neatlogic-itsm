draw2d.shape.node.FlowStateRelease = draw2d.shape.node.FlowBaseImage.extend({
	NAME : "draw2d.shape.node.FlowStateRelease",
	CNNAME : '版本发布',
	allowBack : true,
	allowRefire : true,
	allowAssign : true,
	allowEoa : false,
	allowGrade : true,
	isValid : function() {
		var connections = this.getConnections();
		if (connections.getSize() > 0 && this.getComposite() == null) {
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
		if (d == null || d.name == '' || ((d.team == null || d.team == '') && (d.user == null || d.user == ''))) {
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
				case "form":
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateRelease' ? this : this.getParent());
					var name = that.CNNAME, team = null, user = '', assign = '', proptype = '', propid = '', teamdispatcher = '', allowcancel = '', assignstep = '', plantype = null;
					var userData = that.getUserData();
					if (userData != null) {
						name = userData.name || that.CNNAME;
						team = userData.team;
						user = userData.user;
						assign = userData.assign;
						allowcancel = userData.allowcancel;
						proptype = userData.proptype;
						propid = userData.propid;
						teamdispatcher = userData.teamdispatcher;
						filegroup = userData.filegroup;
						plantype = userData.plantype;
						assignstep = userData.assignstep;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.release.base'));

					var txtStepName = form.find('#txtStepName');
					var sltTeam = form.find('#sltTeam');
					var sltUser = form.find('#sltUser');
					var sltAssign = form.find('#sltAssign');
					var sltAllowCancel = form.find('#sltAllowCancel');
					var hidGroupId = form.find('#hidGroupId');
					var sltProp = form.find('#sltProp');
					var sltPropType = form.find('#sltPropType');
					var sltTeamDispatcher = form.find('#sltTeamDispatcher');
					var divPlan = form.find('#divPlan');
					var sltAssignStep = form.find('#sltAssignStep');

					txtStepName.val(name);
					sltAllowCancel.val(allowcancel);
					sltAssign.val(assign);
					hidGroupId.val(that.groupid);

					sltUser.checkselect();

					$.getJSON('/balantflow/module/balantcase/task/getAllTeam.do', function(data) {
						sltTeam.loadSelectWithDefault(data);
						sltTeam.val(team);
						getTeamUser(user, true);
					});

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

					$.getJSON('/balantflow/dispatch/getTeamDispatcher.do?componentId=13', function(data) {
						sltTeamDispatcher.loadSelectWithDefault(data);
						sltTeamDispatcher.val(teamdispatcher);
					});

					$.getJSON('/balantflow/module/balantrelease/plan/getAllPlanTypeListJson.do', function(data) {
						for (var i = 0; i < data.length; i++) {
							var $div = $('<div></div>');
							var $chk = $('<input data-makeup="checkbox" data-style="square" type="checkbox" value="' + data[i].value + '" name="plantype">');
							$div.append($chk).append('<div style="display:inline-block;vertical-align: top">' + data[i].text + '</div>');
							divPlan.append($div);
							if (plantype && plantype != null && plantype.length > 0) {
								for ( var p in plantype) {
									if (plantype[p] == data[i].value) {
										$chk.prop('checked', true);
									}
								}
							}
						}
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

					sltTeam.change(function() {
						sltTeam.attr('check-type', 'required');
						getTeamUser(null, true);
					});

					if (propid) {
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
					for (var i = 0; i < nextStepList.getSize(); i++) {
						if (nextStepList.get(i).allowAssign) {
							sltAssignStep.append('<option value="' + nextStepList.get(i).getId() + '">' + nextStepList.get(i).getLabelText() + '</option>');
						}
					}
					sltAssignStep.val(assignstep);
					sltAssignStep.checkselect();
					break;
				case 'file':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateRelease' ? this : this.getParent());
					var userData = that.getUserData();
					var filegroup = null, filegroupname = null, filegroupneed = null;
					if (userData != null) {
						filegroup = userData.filegroup;
						filegroupname = userData.filegroupname;
						filegroupneed = userData.filegroupneed;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.release.file'));

					var tabFileGroup = form.find('#tabFileGroup');

					var filegrouplist = null;
					$.ajax({
						type : 'GET',
						async : false,
						url : '/balantflow/module/balantrelease/filegroup/getAllActiveFileGroupListJson.do',
						dataType : 'json',
						success : function(data) {
							filegrouplist = data;
						}
					});

					if (filegroup != null && filegroupname != null && filegroupneed != null && filegroup.length > 0) {
						for (var i = 0; i < filegroup.length; i++) {
							if (filegroup[i] != '') {
								var json = {};
								json.filegroup = filegroup[i];
								json.filegroupname = filegroupname[i];
								json.filegroupneed = filegroupneed[i];
								json.fileGroupList = filegrouplist;
								tabFileGroup.append(xdoT.render('balantflow.editflow.release.tmpfile', json));
							}
						}
					}

					$(document).on('click', '.btnAddFileGroup', function() {
						var json = {};
						json.fileGroupList = filegrouplist;
						tabFileGroup.append(xdoT.render('balantflow.editflow.release.tmpfile', json));
					});

					$(document).on('click', '.btnDelFileGroup', function() {
						$(this).closest('tr').remove();
					});

					$('#divDialog').unbind().bind('beforeSave', function() {
						var checkjson = {};
						$('.filegroup').each(function() {
							if ($(this).val() == '') {
								showPopMsg.info('请选择文件类型。');
								$('#divDialog').data('closable', 0);
								return false;
							} else {
								if (checkjson[$(this).val()]) {
									showPopMsg.info('请不要选择重复的文件类型。');
									$('#divDialog').data('closable', 0);
									return false;
								} else {
									checkjson[$(this).val()] = true;
									$('#divDialog').data('closable', 1);
								}
							}
						});
						if ($('#divDialog').data('closable') != 0) {
							$(document).off('click', '.btnAddFileGroup');
							$(document).off('click', '.btnDelFileGroup');
						}
					});

					break;
				case 'script':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateRelease' ? this : this.getParent());
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
					scriptEditor.setSize('100%', 300);

					if (script) {
						scriptEditor.setValue(script);
					}

					$('#divDialog').unbind().bind('beforeSave', function() {
						txtScript.val(scriptEditor.getValue());
					});
					break;
				case 'mail':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateRelease' ? this : this.getParent());
					var mailtemplate;
					var userData = that.getUserData();
					if (userData != null) {
						mailtemplate = userData.mailtemplate;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.common.mail'));

					$.getJSON('/balantflow/mailtemplate/getAllActiveMailtemplateJson.do', function(data) {
						if (data && data.length > 0) {
							for (var i = 0; i < data.length; i++) {
								var chk = $('<input type="checkbox" data-makeup="checkbox" data-style="square" name="mailtemplate" value="' + data[i].value + '">');
								if (mailtemplate && mailtemplate.length > 0) {
									for (var j = 0; j < mailtemplate.length; j++) {
										if (mailtemplate[j] == data[i].value) {
											chk.prop('checked', true);
											break;
										}
									}
								}
								var div = $('<div style="margin:2px"></div>');
								var span1 = $('<span style="display:inline-block;vertical-align:middle;margin-right:5px"></span>');
								var span2 = $('<span style="display:inline-block"></span>');
								span1.append(chk);
								span2.append(data[i].text);
								div.append(span1).append(span2);
								$('#divMailtemplate').append(div);
							}
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
				'file' : {
					name : '附件设置'
				},
				'script' : {
					name : '脚本设置'
				},
				'mail' : {
					name : '通知设置'
				}
			}
		});
	}
});