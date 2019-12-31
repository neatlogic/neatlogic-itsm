draw2d.shape.node.FlowStateAutomatic = draw2d.shape.node.FlowBaseImage.extend({
	COMPONENT_ID : 24,
	NAME : "draw2d.shape.node.FlowStateAutomatic",
	CNNAME : '自动处理节点',
	allowBack : true,
	allowRefire : true,
	allowAssign : false,
	allowEoa : false,
	allowGrade : false,
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
		if (!d || !d.name || !d.url) {
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
	getProp : function(targetid, container) {
		var that = (this.NAME == 'draw2d.shape.node.FlowStateAutomatic' ? this : this.getParent());
		if (targetid == null || targetid == '' || typeof targetid == 'string') {
			var propparams = '';
			var connections = that.getConnections();
			var formid = '', propid = '';
			for (var i = 0; i < connections.getSize(); i++) {
				var conn = connections.get(i);
				if (conn.getTarget().getParent().getId() == that.getId()) {// 前置步骤
					var preNode = conn.getSource().getParent();
					if (preNode.getComposite() != null) {// 如果是组合节点，则取组合节点的信息
						var cl = preNode.getComposite().getAssignedFigures();
						for (var d = 0; d < cl.getSize(); d++) {
							var dn = cl.get(d);
							if (dn.getName() != 'draw2d.shape.node.FlowStart') {
								preNode = dn;
								break;
							}
						}
					}
					var preData = preNode.getUserData();
					if (preData != null) {
						if (preData.form && preData.form != '') {
							propparams += 'formId=' + preData.form;
							// formid = preData.form;
						}
						if (preData.propid && preData.propid != '') {
							if (propparams != '') {
								propparams += '&';
							}
							propparams += 'propId=' + preData.propid;
							// propid = preData.propid;
						}
					}
				}
			}
		} else {
			var propparams = '';
			var c = that.getCanvas();
			for (var t = 0; t < targetid.length; t++) {
				var tid = targetid[t];
				if (tid != '') {
					var preNode = c.getFigure(tid);
					if (preNode) {
						var preData = preNode.getUserData();
						if (preData != null) {
							if (preData.form && preData.form != '') {
								if (propparams != '') {
									propparams += '&';
								}
								propparams += 'formId=' + preData.form;
							}
							if (preData.propid && preData.propid != '') {
								if (propparams != '') {
									propparams += '&';
								}
								propparams += 'propId=' + preData.propid;
								// propid = preData.propid;
							}
						}
					}
				}
			}

		}
		$.ajax({
			type : "GET",
			url : '/balantflow/flow/getAllParameterJson.do?' + propparams,
			async : false,
			dataType : 'json',
			success : function(data) {
				container.empty().html(xdoT.render('balantflow.editflow.rest.prop', data));
			}
		});
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
                    var that = (this.NAME == 'draw2d.shape.node.FlowStateAutomatic' ? this : this.getParent());
                    var userData = that.getUserData();
                    if (userData == null) {
                        userData = {
                            name : that.CNNAME
                        };
                    }
                    var team = userData.team;
                    var targetId = userData.targetid;
                    $('#divDialog').modal({
                        'backdrop' : false,
                        'show' : 'true'
                    });
                    $('#divDialog').data('obj', that);
                    $('#dialogTitle').html(that.CNNAME);
                    if (userData.paramValue) {
                        if (typeof userData.paramValue == 'object') {
                            userData.paramValue = JSON.stringify(userData.paramValue, null, 2);
                        }
                    }

                    /*                    var loadShowTemplateFn = function(){
                                            var editor = CodeMirror.fromTextArea($('#txtScript', that)[0], {
                                                mode : "htmlmixed",
                                                lineNumbers : true,
                                                theme : "default"
                                            });
                                            $("#txtScript", that).data('editor', editor);
                                            setTimeout(function() {
                                                editor.refresh();
                                            }, 0);
                                        }*/


                    form.html(xdoT.render('balantflow.editflow.automatic.base', userData));
                    console.log(userData);
                    var txtParamValue = form.find('#txtParamValue');
                    var txtTemplateValue = form.find('#textshowtemplate');

                    var divParamContainer = form.find('#divParamContainer');
                    var sltAuthtype = form.find('#sltAuthType');
                    var sltMethod = form.find('#sltMethod');
                    var sltTargetId = form.find('#sltTargetId');

                    this.getProp(targetId, divParamContainer);

                    sltAuthtype.on('change', function() {
                        if ($(this).val() == 'basic') {
                            $('.authcontrol').show();
                        } else {
                            $('.authcontrol').hide();
                        }
                        if ($(this).val() == 'header') {
                            $('.headerControl').show();
                        } else {
                            $('.headerControl').hide();
                        }
                    });

                    sltMethod.on('change', function() {
                        if ($(this).val() == 'GET') {
                            $('#sltIsTransfer').val('1').attr('disabled', 'disabled');
                        } else {
                            $('#sltIsTransfer').removeAttr('disabled');
                        }
                    });
                    
                    $('.btnHeaderAdd').on("click",function(){
                    	$(this).closest("table").find("tr:last").after(xdoT.render('balantflow.editflow.headertr'));
                    });
                    
                    $('.btnHeaderDel').on("click",function(){
                    	if($(this).closest("table").find('tr').length > 2){
                    		$(this).closest("tr").remove();
                    	}else{
                    		$(this).closest("tr").find('input').val("");
                    	}
                    });
                    var scriptEditor = CodeMirror.fromTextArea(txtParamValue[0], {
                        mode : "htmlmixed",
                        lineNumbers : true
                    });
                    scriptEditor.setSize('100%', 180);

                    var templateEditor = CodeMirror.fromTextArea(txtTemplateValue[0], {
                        mode : "htmlmixed",
                        lineNumbers : true
                    });
                    templateEditor.setSize('100%', 180);

                    var prevStepList = new draw2d.util.ArrayList();
                    that.findAllPrevNode(prevStepList, that);
                    for (var f = 0; f < prevStepList.getSize(); f++) {
                        var figure = prevStepList.get(f);
                        if (figure.allowCondition) {
                            var opt = $('<option value="' + figure.id + '">' + figure.getLabelText() + '</option>');
                            sltTargetId.append(opt);
                        }
                    }
                    sltTargetId.val(targetId);
                    sltTargetId.checkselect();

                    $('#divDialog').unbind().bind('beforeSave', function() {
                        txtParamValue.val(encodeHtml(scriptEditor.getValue()));
                        txtTemplateValue.val(encodeHtml(templateEditor.getValue()));
                    });

					break;
				case "callback":
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateAutomatic' ? this : this.getParent());
					var userData = that.getUserData();
					if (userData == null) {
						userData = {
							name : that.CNNAME
						};
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);

					form.html(xdoT.render('balantflow.editflow.automatic.callback', userData));
					var txtParamTemplate = form.find('#txtParamTemplate');
					var sltCallbackType = form.find('#sltCallbackType');
					var sltCallbackAuthtype = form.find('#sltCallbackAuthType');
					
					var scriptEditor = CodeMirror.fromTextArea(txtParamTemplate[0], {
						mode : "htmlmixed",
						lineNumbers : true
					});
					scriptEditor.setSize('100%', 180);

					sltCallbackType.on('change', function() {
						if ($(this).val() == 'pull') {
							$('.pull-item').show();
							sltCallbackAuthtype.trigger('change');
						} else {
							$('.pull-item').hide();
							$('.authcontrol').hide();
							$('.headerControl').hide();
						}
					});

					sltCallbackAuthtype.on('change', function() {
						if ($(this).val() == 'basic') {
							$('.authcontrol').show();
						} else {
							$('.authcontrol').hide();
						}
						if ($(this).val() == 'header') {
							$('.headerControl').show();
						} else {
							$('.headerControl').hide();
						}
					});
					 $('.btnHeaderAdd').on("click",function(){
                    	$(this).closest("table").find("tr:last").after(xdoT.render('balantflow.editflow.headertr'));
                    });
                    
					$('.btnHeaderDel').on("click",function(){
						if($(this).closest("table").find('tr').length > 2){
		            		$(this).closest("tr").remove();
		            	}else{
		            		$(this).closest("tr").find('input').val("");
		            	}
                    });
					$('#divDialog').unbind().bind('beforeSave', function() {
						txtParamTemplate.val(encodeHtml(scriptEditor.getValue()));
					});

					break;
				case "dispatch":
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateAutomatic' ? this : this.getParent());
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

					form.html(xdoT.render('balantflow.editflow.automatic.dispatch', userData));
					var sltTeam = form.find('#sltTeam');
					var sltUser = form.find('#sltUser');
					var sltUserDispatcher = form.find('#sltUserDispatcher');
					var sltTeamDispatcher = form.find('#sltTeamDispatcher');
					var sltCopyUserStep = form.find('#sltCopyUserStep');
					sltUser.checkselect();
					sltTeam.checkselect();
					$.getJSON('/balantflow/team/getComponentTeamListJson.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltTeam.loadSelect(data);
						sltTeam.val(team);
						sltTeam[0].checkselect.reload();
						getTeamUser(user, true);
					});

					$.getJSON('/balantflow/dispatch/getUserDispatchTemplateListByComponentId.do?componentId=' + that.COMPONENT_ID, function(data) {
						sltUserDispatcher.loadSelectWithDefault(data);
						sltUserDispatcher.checkselect();
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

					sltUser.on('change', function() {
						if ($(this).val() == '#{OWNER}') {
							sltTeam.removeAttr('check-type');
						} else {
							sltTeam.attr('check-type', 'required');
						}
					});

					sltTeam.change(function() {
						sltTeam.attr('check-type', 'required');
						getTeamUser(null, true);
					});
					break;
				case "time":
					var form = $('#dialogForm');
					var that = (this.NAME == 'draw2d.shape.node.FlowStateAutomatic' ? this : this.getParent());
					var userData = that.getUserData();
					var targetId = '';
					if (userData == null) {
						userData = {
							name : that.CNNAME
						};
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(that.CNNAME);

					form.html(xdoT.render('balantflow.editflow.automatic.time', userData));
					break;
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
				'callback' : {
					name : '回调设置',
				},
				'dispatch' : {
					name : '分派设置'
				},
				'time' : {
					name : '执行时间'
				}
			}
		});
	}
});