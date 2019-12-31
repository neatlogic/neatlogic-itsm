draw2d.shape.node.FlowStateProblemAssign = draw2d.shape.node.FlowBaseImage.extend({
	NAME : "draw2d.shape.node.FlowStateProblemAssign",
	CNNAME : '问题分派',
	allowBack : true,
	allowRefire : true,
	allowAssign : true,
	allowEoa : false,
	isValid : function() {
		var connections = this.getConnections();
		if (connections.getSize() > 0) {
			var connHandle = true;
			var targetcount = 0;
			var sourcecount = 0;
			for ( var c = 0; c < connections.getSize(); c++) {
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
					var that = (this.NAME == 'draw2d.shape.node.FlowStateProblemAssign' ? this : this.getParent());
					var name = that.CNNAME , proptype = '' , propid='' ,  assignstep = '' ,noteContent = '';
					var allowtaskover = '' ;
					var userData = that.getUserData();
					if (userData != null) {
						name = userData.name || that.CNNAME;
						assignstep = userData.assignstep;
						proptype = userData.proptype;
						propid = userData.propid;
						noteContent = userData.noteContent ? userData.noteContent : '' ;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);

					form.html(xdoT.render('balantflow.editflow.problemassign.base'));

					var txtStepName = form.find('#txtStepName');
					var sltAssignStep = form.find('#sltAssignStep');
					var sltProp = form.find('#sltProp');
					var sltPropType = form.find('#sltPropType');
					var sltGroupId = form.find('#sltGroupId');

					txtStepName.val(name);
					var prevStepList = new draw2d.util.ArrayList();
					this.findAllPrevNode(prevStepList, that);
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
					var userData = (this.NAME == 'draw2d.shape.node.FlowStateProblemAssign' ? this.getUserData() : this.getParent().getUserData());
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
					$('#divDialog').data('obj', this.NAME == 'draw2d.shape.node.FlowStateProblemAssign' ? this : this.getParent());
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
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateProblemAssign' ? this : this.getParent());
					var team = null, user = '', assign = '',  allowcancel = '', assignstep = '' ,allowrepeat = '' , allowtaskover = '' ;
					var userData = that.getUserData();
					if (userData != null) {
						team = userData.team;
						user = userData.user;
						assign = userData.assign;
						allowcancel = userData.allowcancel;
						assignstep = userData.assignstep;
						allowrepeat = userData.allowRepeat;
						allowtaskover = userData.allowtaskover ;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html('编辑步骤分派信息');
					form.html(xdoT.render('balantflow.editflow.problemassign.dispatch'));
					var sltTeam = form.find('#sltTeam');
					var sltAssign = form.find('#sltAssign');
					var sltAllowCancel = form.find('#sltAllowCancel');
					var sltAssignStep = form.find('#sltAssignStep');
					var sltAllowRepeat = form.find('#sltAllowRepeat');
					var sltAllowTaskover = form.find('#sltAllowTaskover');
					sltAllowTaskover.val(allowtaskover);

					sltAllowCancel.val(allowcancel);
					sltAllowRepeat.val(allowrepeat);
					sltAssign.val(assign);

					var sltUser = form.find('#sltUser');
					sltUser.on('change',function(){
						if ($(this).val() == '#{OWNER}') {
							sltTeam.removeAttr('check-type');
						} else {
							sltTeam.attr('check-type', 'required');
						}
					});
					sltUser.checkselect();
					sltTeam.checkselect();
					$.getJSON('getTeamListJson.do', function(data) {
						sltTeam.loadSelect(data);
						sltTeam.val(team);
						sltTeam[0].checkselect.reload();
						getTeamUser(user, true);
					});

					sltTeam.change(function() {
						getTeamUser(null, true);
						sltTeam.attr('check-type', 'required');
					});
					if(typeof user == 'object'){
						for(var u = 0; u < user.length; u++){
							if(user[u] == '#{OWNER}'){
								sltTeam.removeAttr('check-type');
							}
						}
					}
					var nextStepList = new draw2d.util.ArrayList();
					that.findAllNextNode(nextStepList, that);
					sltAssignStep.children().remove();
					for (var i = 0; i < nextStepList.getSize(); i++) {
						if (nextStepList.get(i).allowAssign) {
							sltAssignStep.append('<option value="' + nextStepList.get(i).getId() + '">' + nextStepList.get(i).getLabelText() + '</option>');
						}
					}
					sltAssignStep.val(assignstep);
					sltAssignStep.checkselect();
					break;
				case 'script':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateProblemAssign' ? this : this.getParent());
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