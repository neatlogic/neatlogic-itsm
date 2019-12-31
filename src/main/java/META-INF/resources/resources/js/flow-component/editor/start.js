draw2d.shape.node.FlowStart = draw2d.shape.node.FlowBaseCircle.extend({
	NAME : "draw2d.shape.node.FlowStart",
	CNNAME : '开始',
	COLOR : "#3CBD17",
	PORTTYPE : 'hybrid',
	allowBack : false,
	allowRefire : true,
	allowAssign : false,
	allowEoa : true,
	allowGrade : false,
	allowCallout : true,
	allowCondition : true,
	isValid : function() {
		var color = new draw2d.util.Color("#ff0000");
		var connections = this.getConnections();
		var connCount = 0;
		for (var i = 0; i < connections.getSize(); i++) {
			var conn = connections.get(i);
			if (conn.dasharray == '' && conn.getTarget().getParent().getId() == this.getId()) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '开始节点不能作为后续节点。';
			} else if (conn.dasharray == '' && conn.getSource().getParent().getId() == this.getId()) {
				connCount += 1;
			}
		}
		if (connCount > 1) {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '开始节点只能关联一个后续节点。';
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
				case 'form':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStart' ? this : this.getParent());
					var name = that.CNNAME, proptype = '', formid = '', propid = ''
					var userData = that.getUserData();
					if (userData != null) {
						formid = userData.form;
						proptype = userData.proptype;
						propid = userData.propid;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', this.NAME == 'draw2d.shape.node.FlowStart' ? this : this.getParent());
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.start.form'));

					var sltProp = form.find('#sltProp');
					var sltPropType = form.find('#sltPropType');
					var sltForm = form.find('#sltForm');

					$.getJSON('getAllEnableFormJson.do', function(data) {
						sltForm.loadSelect(data);
						sltForm.val(formid);
						sltForm.checkselect();
						/*if(sltForm[0].checkselect){
							
						}else{
						sltForm.checkselect();
						}*/
					});

					$.getJSON('/balantflow/channel/getAllChannelTypeJson.do', function(data) {
						sltPropType.loadSelectWithDefault(data, {
							'textkey' : 'name',
							'valuekey' : 'id'
						});
						sltPropType.val(proptype);
						sltPropType[0].checkselect.reload();
					});

					sltPropType.change(function() {
						$.getJSON('/balantflow/channel/getPropDefListByType.do?type=' + sltPropType.val(), function(data) {
							sltProp.loadSelectWithDefault(data);
							sltProp[0].checkselect.reload();
						});
					});

					if (proptype) {
						$.getJSON('/balantflow/channel/getPropDefListByType.do?type=' + proptype, function(data) {
							sltProp.loadSelectWithDefault(data);
							sltProp.val(propid);
							sltProp[0].checkselect.reload();
						});
					}
					break;
				case 'dispatch':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStart' ? this : this.getParent());
					var name = that.CNNAME, team = null, user = '', allowcancel = '', allowrepeat = '', userdispatcher = '', assignstep = '', assign = '', teamdispatcher = '', allowtaskover = '';
					var isretrybacktime = '', retrytime = '';
					var userData = that.getUserData();
					if (userData != null) {
						assignstep = userData.assignstep;
					}
					$('#divDialog').modal('show');
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.start.dispatch', userData));

					var sltAssignStep = form.find('#sltAssignStep');

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
					break;
				case 'script':
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStart' ? this : this.getParent());
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
				case 'component':
					var form = $('#dialogForm');
					var component = '';
					var that = (this.NAME == 'draw2d.shape.node.FlowStart' ? this : this.getParent());
					var userData = that.getUserData();
					if (userData != null) {
						component = userData.component;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', this.NAME == 'draw2d.shape.node.FlowStart' ? this : this.getParent());
					$('#dialogTitle').html(that.CNNAME);
					form.html(xdoT.render('balantflow.editflow.start.composite'));

					var sltComponent = form.find('#sltComponent');
					$.getJSON('/balantflow/component/getCompositeComponentJson.do', function(data) {
						sltComponent.loadSelectWithDefault(data);
						sltComponent.val(component);
					});

					$('#divDialog').unbind().bind('beforeSave', function() {
						if (sltComponent.val() == component) {
							return;
						}
						var canvas = that.getCanvas();
						canvas.getCommandStack().startTransaction();
						var composite = that.getComposite();
						if (composite != null) {
							var cl = composite.getAssignedFigures();
							var unGroupList = new draw2d.util.ArrayList();
							for (var d = 0; d < cl.getSize(); d++) {
								var dn = cl.get(d);
								if (dn.getName() != 'draw2d.shape.node.FlowStart') {
									unGroupList.push(dn);
								}
							}

							canvas.getCommandStack().execute(new draw2d.command.CommandUngroup(canvas, composite));
							for (var d = 0; d < unGroupList.getSize(); d++) {
								var dn = unGroupList.get(d);
								canvas.getCommandStack().execute(new draw2d.command.CommandDelete(dn));
							}

						}
						canvas.getCommandStack().commitTransaction();
						canvas.getCommandStack().startTransaction();
						if (sltComponent.val() != '') {
							if (!that.groupid) {
								var groupid = draw2d.util.UUID.create();
								that.groupid = groupid;
							}
							var figureList = new draw2d.util.ArrayList();
							figureList.push(that);
							var shapeid = 0;
							sltComponent.children('option').each(function() {
								if ($(this).prop('selected')) {
									shapeid = $(this).data('id');
								}
							});

							var x = that.getX() + (38 - 15) / 2;
							var y = that.getY() + 38 + 25;
							var f0 = eval('new ' + sltComponent.val() + '("icon_' + shapeid + '", ' + 15 + ', ' + 15 + ', false)');
							f0.groupid = that.groupid;
							figureList.push(f0);
							canvas.getCommandStack().execute(new draw2d.command.CommandAdd(canvas, f0, x, y));
							canvas.getCommandStack().execute(new draw2d.command.CommandGroup(canvas, figureList));
						}
						canvas.getCommandStack().commitTransaction();
					});
				default:
					break;
				}
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
				'script' : {
					name : '脚本设置'
				},
				'component' : {
					name : '合并节点'
				}
			}
		});
	}
});