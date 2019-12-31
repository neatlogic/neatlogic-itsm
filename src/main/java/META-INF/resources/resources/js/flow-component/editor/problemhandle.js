draw2d.shape.node.FlowStateProblemHandle = draw2d.shape.node.FlowBaseImage.extend({
	NAME : 'draw2d.shape.node.FlowStateProblemHandle',
	CNNAME : '问题处理',
	allowBack : true,
	allowRefire : true,
	allowAssign : true,
	allowEoa : false,
	allowGrade : true,
	isValid : function() {
		var connections = this.getConnections();
		if (connections.getSize() > 0) {
			var sourcecount = 0;
			var targetcount = 0;
			var connAssign = true;
			var connHandle = true;
			for ( var c = 0; c < connections.getSize(); c++) {
				var conn = connections.get(c);
				if (conn.dasharray == '') {
					if (conn.getTarget().getParent().getId() == this.getId()) {
						sourcecount += 1;
						if (conn.getSource().getParent().getName() != 'draw2d.shape.node.FlowStateProblemAssign') {
							connAssign = false;
						}
					} else {
						targetcount += 1;
						if (conn.getTarget().getParent().getName() != 'draw2d.shape.node.FlowStateProblemConfirm') {
							connHandle = false;
						}
					}
				}
			}

		}

		var d = this.getUserData();
		if (d == null ||  d.name == null ||  d.name == '' || ((d.team == null || d.team == '') && (d.user == null || d.user == ''))) {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请设置节点信息。';
		}

		for ( var c = 0; c < this.getChildren().getSize(); c++) {
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
					var that = (this.NAME == 'draw2d.shape.node.FlowStateProblemHandle' ? this : this.getParent());
					var name = that.CNNAME, proptype = '' , propid='' , groupid = null , noteContent ='';
					var userData = that.getUserData();
					if (userData != null) {
						groupid = userData.groupid;
						proptype = userData.proptype;
						propid = userData.propid;
						noteContent = userData.noteContent ? userData.noteContent : '' ;
					}
					$('#divDialog').modal('show');
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.problemhandle.base'));

					var txtStepName = form.find('#txtStepName');
					var sltPropType =form.find('#sltPropType');
					var sltProp = form.find('#sltProp');
					var sltGroupId = form.find('#sltGroupId');
					txtStepName.val(name);
					var prevStepList = new draw2d.util.ArrayList();
					that.findAllPrevNode(prevStepList, that);
					for ( var f = 0; f < prevStepList.getSize(); f++) {
						var figure = prevStepList.get(f);
						if(figure.getName() == 'draw2d.shape.node.FlowStateProblemCreate'){
							var opt = $('<option value="'+ figure.groupid +'">'+ figure.getLabelText() +'</option>');
							opt.prop('selected', true);
							sltGroupId.append(opt);
						}
					}

					$.getJSON('/balantflow/channel/getAllChannelTypeJson.do' , function(data){
						var n = $.map(data, function(v,i){
	                        return {"text": v.name , "value": v.id };
	                    });
						sltPropType.appendSelect(n);
						sltPropType.val(proptype);
					});
					sltPropType.change(function(){
						$.getJSON('/balantflow/channel/getPropDefListByType.do?type=' + sltPropType.val() , function(data){
							sltProp.loadSelectWithDefault(data);
						});
					});
					if(proptype){
						$.getJSON('/balantflow/channel/getPropDefListByType.do?type=' + proptype , function(data){
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
					var userData = (this.NAME == 'draw2d.shape.node.FlowStateProblemHandle' ? this.getUserData() : this.getParent().getUserData());
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
					$('#divDialog').data('obj', this.NAME == 'draw2d.shape.node.FlowStateProblemHandle' ? this : this.getParent());
					$('#dialogTitle').html('编辑处理时限');
					form.html(xdoT.render('balantflow.editflow.common.urgency'));

					var tbody = form.find('tbody:first');
					$.ajax({
						type : 'GET',
						dataType : 'json',
						url : 'getUrgencyListJson.do',
						success : function(data) {
							tbody.html('');
							for ( var u = 0; u < data.length; u++) {
								var utime = '';
								if (urgency != null && urgencytime != null) {
									if (typeof (urgency) == 'object') {
										for ( var ur = 0; ur < urgency.length; ur++) {
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
								var $tr = $('<tr><td>' + data[u].text + '<input type="hidden" name="urgency" value="' + data[u].value + '"></td>' + '<td style="text-align:center"><input check-type="integer_p" integer_p-message="请输入正整数" style="display:inline;width:100px" type="text" class="form-control input-sm" value="' + utime + '" name="urgencytime">分钟</td></tr>');
								tbody.append($tr);
							}
						},
						error : function(ajax, error, errorThrown) {
							showPopMsg.error('获取紧急程度列表失败：<br/>请检查服务端或者网络是否存在问题。' + '<br/>' + errorThrown, 10);
						}
					});
					break;
				case 'dispatch':
					var userData = (this.NAME == 'draw2d.shape.node.FlowStateProblemHandle' ? this.getUserData() : this.getParent().getUserData());
					var name = team = null, user = '', assign = '' ;
					if (userData != null) {
						team = userData.team;
						user = userData.user;
						assign = userData.assign;
					}
					var form = $('#dialogForm');
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', this.NAME == 'draw2d.shape.node.FlowStateProblemHandle' ? this : this.getParent());
					$('#dialogTitle').html('编辑步骤分派信息');
					form.html(xdoT.render('balantflow.editflow.problemhandle.dispatch'));
					var sltTeam = form.find('#sltTeam');
					var sltAssign = form.find('#sltAssign');
					var sltUser = form.find('#sltUser');
					sltAssign.val(assign);
					sltTeam.change(function() {
						sltTeam.attr('check-type', 'required');
						getTeamUser(null);
					});
					sltTeam.checkselect();
					$.getJSON('/balantflow/module/balantproblem/problem/getProblemTeam.do', function(data) {
						sltTeam.loadSelect(data);
						sltTeam.val(team);
						sltTeam[0].checkselect.reload();
						getTeamUser(user);
					}).fail(function() {
						showPopMsg.error('获取处理组列表失败：<br/>请检查服务端或者网络是否存在问题。');
					});

					sltUser.checkselect();
					break;
				case 'script':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateProblemHandle' ? this : this.getParent());
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

					$('#divDefaultMethodItem').html('<ul><li>是否需要改进措施:validWorkOrderCount(true/false)</li></ul>');

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

					scriptEditor = CodeMirror.fromTextArea($('#txtScript')[0], {
						mode : "htmlmixed",
						lineNumbers : true
					});

					if (script) {
						scriptEditor.setValue(script);
					}
					$('#divDialog').unbind().bind('beforeSave', function() {
						txtScript.val(scriptEditor.getValue());
					});
					scriptEditor.setSize('100%', 300);
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