draw2d.shape.node.FlowState = draw2d.shape.node.FlowBaseImage.extend({
	COMPONENT_ID : 2,
	NAME : "draw2d.shape.node.FlowState",
	CNNAME : '通用节点',
	allowBack : true,
	allowRefire : true,
	allowAssign : true,
	allowEoa : true,
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
		if (d == null || d.name == null || d.name == '') {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请完成基本设置。';
		}
		if((d.team == null || d.team == '') && (d.user == null || d.user == '')){
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
					var that = (this.NAME == 'draw2d.shape.node.FlowState' ? this : this.getParent());
					var name = that.CNNAME, proptype = '', formid = '', propid = '', noteContent = '', attachment = null; // votetype
					var userData = that.getUserData();
					if (userData != null) {
						name = userData.name || that.CNNAME;
						proptype = userData.proptype;
						propid = userData.propid;
						formid = userData.form;
						noteContent = userData.noteContent ? userData.noteContent : '';
						attachment = userData.attachment;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.process.base'));
					var txtStepName = form.find('#txtStepName');
					var sltProp = form.find('#sltProp');
					var sltPropType = form.find('#sltPropType');
					var sltForm = form.find('#sltForm');
					var sltAttachment = form.find('#sltAttachment');

					txtStepName.val(name);

					$.getJSON('/balantflow/file/listallfiletype', function(data) {
						sltAttachment.loadSelect(data, {
							valuekey : 'id',
							textkey : 'name'
						});
						sltAttachment.val(attachment);
						sltAttachment.checkselect();
					});

					$.getJSON('getAllEnableFormJson.do', function(data) {
						sltForm.loadSelectWithDefault(data);
						sltForm.val(formid);
						if (sltForm[0].checkselect) {
							sltForm[0].checkselect.reload();
						}
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
						if (sltPropType[0].checkselect) {
							sltPropType[0].checkselect.reload();
						}
					});
					sltPropType.change(function() {
						$.getJSON('/balantflow/channel/getPropDefListByType.do?type=' + sltPropType.val(), function(data) {
							sltProp.loadSelectWithDefault(data);
							if (sltProp[0].checkselect) {
								sltProp[0].checkselect.reload();
							}
						});
					});
					if (proptype) {
						$.getJSON('/balantflow/channel/getPropDefListByType.do?type=' + proptype, function(data) {
							sltProp.loadSelectWithDefault(data);
							sltProp.val(propid);
							sltProp[0].checkselect.reload();
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
				case 'param':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowState' ? this : this.getParent());
					var paramList = null;
					var userData = that.getUserData();
					var paramMap = {};
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					var prevStepList = new draw2d.util.ArrayList();
					that.findAllPrevNode(prevStepList, that);
					var formDataList = new Array();
					var json = {};
					json.userData = userData;
					for (var f = 0; f < prevStepList.getSize(); f++) {
						var figure = prevStepList.get(f);
						var figureData = figure.getUserData();
						var formList = new Array();
						var stepObj = {};
						stepObj.stepId = figure.getId();
						stepObj.stepName = figure.CNNAME;
						stepObj.formList = new Array();
						if (figureData && figureData.form) {
							if (figureData.form instanceof Array) {
								for ( var formindex in figureData.form) {
									if (figureData.form[formindex]) {
										formList.push(figureData.form[formindex]);
									}
								}
							} else {
								formList.push(figureData.form);
							}
							for ( var formindex in formList) {
								if (formList[formindex]) {
									$.ajax({
										async : false,
										data : 'json',
										type : 'GET',
										url : '/balantflow/form/listformparam/' + formList[formindex],
										success : function(data) {
											if (!$.isEmptyObject(data)) {
												stepObj.formList.push(data);
											}
										}
									});
								}
							}
							formDataList.push(stepObj);
						}
					}
					json.formDataList = formDataList;
					var html = xdoT.render('balantflow.editflow.process.param', json);
					form.html(html);
					break;
				case 'dispatch':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowState' ? this : this.getParent());
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
					form.html(xdoT.render('balantflow.editflow.process.dispatch', userData));

					var sltTeam = form.find('#sltTeam');
					var sltTeamDispatcher = form.find('#sltTeamDispatcher');
					var sltUser = form.find('#sltUser');
					var sltAssignStep = form.find('#sltAssignStep');
					var sltUserDispatcher = form.find('#sltUserDispatcher');
					var sltCopyUserStep = form.find('#sltCopyUserStep');

					$.getJSON('/balantflow/dispatch/getUserDispatchTemplateListByComponentId.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltUserDispatcher.loadSelectWithDefault(data);
						//sltUserDispatcher.val(userdispatcher);
						sltUserDispatcher.checkselect();
					});

					sltUser.on('change', function() {
						if ($(this).val() == '#{OWNER}' || $(this).val() == '#{REPORT}') {
							sltTeam.removeAttr('check-type');
						} else {
							sltTeam.attr('check-type', 'required');
						}
					});
					sltUser.checkselect();
					
					$.getJSON('/balantflow/team/getComponentTeamListJson.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltTeam.loadSelect(data);
						sltTeam.val(team);
						//sltTeam[0].checkselect.reload();
						sltTeam.checkselect();
						getTeamUser(user, true);
					});
					
					if(user == '#{OWNER}' || user == '#{REPORT}'){
						sltTeam.removeAttr('check-type');
					}else{
						sltTeam.attr('check-type', 'required');
					}

					$.getJSON('/balantflow/dispatch/getTeamDispatcher.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltTeamDispatcher.loadSelectWithDefault(data);
						//sltTeamDispatcher.val(teamdispatcher);
						sltTeamDispatcher.checkselect();
					});

					var nextStepList = new draw2d.util.ArrayList();
					that.findAllNextNode(nextStepList, that);
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
				case 'script':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowState' ? this : this.getParent());
					var script = '', propid = '';
					var userData = that.getUserData();
					if (userData != null) {
						script = userData.script;
						propid = userData.propid;
					}
					$('#divDialog').modal('show');
					form.html(xdoT.render('balantflow.editflow.common.script'));
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					var scriptEditor;
					var txtScript = form.find('#txtScript');
					$('#divDialog').on('shown.bs.modal', function (e) {
						
						if(!scriptEditor){
							scriptEditor= CodeMirror.fromTextArea(txtScript[0], {
								mode : "htmlmixed",
								lineNumbers : true
							});		
							scriptEditor.setSize('100%', 300);
						}
						if (script) {
							scriptEditor.setValue(script);
						}
					})
					
					if (propid == '' || !propid) {
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

					$('#divDialog').unbind('beforeSave').bind('beforeSave', function() {
						txtScript.val(scriptEditor.getValue());
					});
					break;
				case 'urgency':
					var that = (this.NAME == 'draw2d.shape.node.FlowState' ? this : this.getParent());
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
										+ '<td style="text-align:left"><input check-type="integer_p" integer_p-message="请输入正整数" style="display:inline;width:150px" type="text" class="form-control input-sm" value="' + utime
										+ '" name="urgencytime">分钟</td></tr>');
								tbody.append($tr);
							}
						},
						error : function(ajax, error, errorThrown) {
							showPopMsg.error('获取紧急程度列表失败：<br/>请检查服务端或者网络是否存在问题。' + '<br/>' + errorThrown, 10);
						}
					});
					break;
				case 'mail':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowState' ? this : this.getParent());
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
				case 'transfer':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowState' ? this : this.getParent());
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
					var html = xdoT.render('balantflow.editflow.common.transfer',{});
					form.html(html);
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
				'form' : {
					name : '基本设置'
				},
				'param' : {
					name : '前置表单'
				},
				'dispatch' : {
					name : '分派设置'
				},
				'script' : {
					name : '脚本设置'
				},
				'urgency' : {
					name : '时限设置'
				},
				'mail' : {
					name : '通知设置'
				},
				'transfer' : {
					name : '转报设置'
				}
			}
		});
	}
});