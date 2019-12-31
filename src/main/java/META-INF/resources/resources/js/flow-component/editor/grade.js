draw2d.shape.node.FlowStateGrade = draw2d.shape.node.FlowBaseImage.extend({
	NAME : "draw2d.shape.node.FlowStateGrade",
	CNNAME : '用户评分',
	allowBack : true,
	allowRefire : true,
	allowAssign : true,
	allowEoa : false,
	allowGrade : false,
	isValid : function() {
		var connections = this.getConnections();
		var color = new draw2d.util.Color("#ff0000");
		var d = this.getUserData();
		//console.info(d);
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

		if (d == null || d.name == null ||  d.name == '' || d.grademethod == '' || d.stepuid == '' || ((d.team == null || d.team == '') && (d.user == null || d.user == ''))) {
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
					var that = (this.NAME == 'draw2d.shape.node.FlowStateGrade' ? this : this.getParent());
					var name = that.CNNAME, grademethod = '', stepuid = '', gradetemplateid = '', proptype = '', propid = '', noteContent = '', gradeday = '', formid = '';
					var userData = that.getUserData();
					if (userData != null) {
						name = userData.name || that.CNNAME;
						grademethod = userData.grademethod;
						stepuid = userData.stepuid;
						gradetemplateid = userData.gradetemplateid;
						proptype = userData.proptype;
						propid = userData.propid;
						noteContent = userData.noteContent ? userData.noteContent : '';
						gradeday = userData.gradeday;
						formid = userData.form;
					}
					$('#divDialog').modal('show');
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.grade.base'));
					var txtStepName = form.find('#txtStepName');
					var sltGradeMethod = form.find('#sltGradeMethod');
					var sltStepuid = form.find('#sltStepuid');
					var sltGradeTemplate = form.find('#sltGradeTemplate');
					var sltPropType = form.find('#sltPropType');
					var sltProp = form.find('#sltProp');
					var txtGradeday = form.find('#txtGradeday');
					var sltForm = form.find('#sltForm');

					txtStepName.val(name);
					sltGradeMethod.val(grademethod);
					txtGradeday.val(gradeday);

					if (grademethod == '0') {
						$('#autoGradedayDiv').show();
						txtGradeday.attr('check-type', 'required number');
					}

					sltGradeMethod.change(function() {
						if ($(this).val() == '0') {
							$('#autoGradedayDiv').show();
							txtGradeday.attr('check-type', 'required number');
						} else {
							$('#autoGradedayDiv').hide();
							txtGradeday.attr('check-type', '');
						}
					});

					$.getJSON('getAllEnableFormJson.do', function(data) {
						sltForm.loadSelectWithDefault(data);
						sltForm.val(formid);
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

					$.getJSON('/balantflow/grade/getGradeTemplateListJson.do', function(data) {
						var n = new Array();
						n.push({
							"text" : "请选择...",
							"value" : ""
						});
						$.map(data.list, function(v, i) {
							var retVal = {
								"text" : v.templateName,
								"value" : v.templateId
							};
							if (v.templateId == gradetemplateid) {
								retVal['selected'] = 'selected';
							}
							n.push(retVal);
						});
						sltGradeTemplate.appendSelect(n);
					});

					var prevStepList = new draw2d.util.ArrayList();
					that.findAllPrevNode(prevStepList, that);
					for (var f = 0; f < prevStepList.getSize(); f++) {
						var figure = prevStepList.get(f);
						if (figure.allowGrade) {
							var opt = $('<option value="' + figure.id + '">' + figure.getLabelText() + '</option>');
							sltStepuid.append(opt);
						}
					}
					sltStepuid.val(stepuid);

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
					var that = (this.NAME == 'draw2d.shape.node.FlowStateGrade' ? this : this.getParent());
					var name = that.CNNAME, team = null, user = '', allowtaskover = '', allowtransfer = '', allowcancel = '', allowback = '';
					var userData = that.getUserData();
					if (userData != null) {
						team = userData.team;
						user = userData.user;
						allowtransfer = userData.allowtransfer;
						allowtaskover = userData.allowtaskover;
						allowcancel = userData.allowcancel;
					}
					$('#divDialog').modal('show');
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.grade.dispatch'));
					var sltTeam = form.find('#sltTeam');
					var sltUser = form.find('#sltUser');
					var sltAllowTaskover = form.find('#sltAllowTaskover');
					var sltAllowtransfer = form.find('#sltAllowtransfer');
					var sltAllowCancel = form.find('#sltAllowCancel');

					sltAllowTaskover.val(allowtaskover);
					sltAllowtransfer.val(allowtransfer);
					sltAllowCancel.val(allowcancel);

					sltUser.on('change',function(){
						if ($(this).val() == '#{OWNER}') {
							sltTeam.removeAttr('check-type');
						} else {
							sltTeam.attr('check-type', 'required');
						}
					});
					sltUser.checkselect();

					$.getJSON('getTeamListJson.do', function(data) {
						sltTeam.appendSelect(data);
						sltTeam.val(team);
						getTeamUser(user, true);
					});

					sltTeam.change(function() {
						getTeamUser(null, true);
					});
					if(typeof user == 'object'){
						for(var u = 0; u < user.length; u++){
							if(user[u] == '#{OWNER}'){
								sltTeam.removeAttr('check-type');
							}
						}
					}
					break;
				case 'urgency':
					var userData = (this.NAME == 'draw2d.shape.node.FlowStateGrade' ? this.getUserData() : this.getParent().getUserData());
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
					$('#divDialog').data('obj', this.NAME == 'draw2d.shape.node.FlowStateGrade' ? this : this.getParent());
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
								var $tr = $('<tr><td>' + data[u].text + '<input type="hidden" name="urgency" value="' + data[u].value + '"></td>' + '<td><input check-type="integer_p" integer_p-message="请输入正整数" style="display:inline;width:100px" type="text" class="form-control input-sm" value="' + utime + '" name="urgencytime">分钟</td></tr>');
								tbody.append($tr);
							}
						},
						error : function(ajax, error, errorThrown) {
							showPopMsg.error('获取紧急程度列表失败：<br/>请检查服务端或者网络是否存在问题。' + '<br/>' + errorThrown, 10);
						}
					});
					break;
				case 'script':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateGrade' ? this : this.getParent());
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

					if (propid == '') {
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