draw2d.shape.node.FlowServiceRest = draw2d.shape.node.FlowBaseService.extend({
	NAME : "draw2d.shape.node.FlowServiceRest",
	CNNAME : 'RESTFul接口',
	isValid : function() {
		var connections = this.getConnections();
		var color = new draw2d.util.Color("#ff0000");
		var d = this.getUserData();
		if (connections.getSize() > 0) {
			var sourcecount = 0;
			var targetcount = 0;
			var hasgradestep = false;
			for (var c = 0; c < connections.getSize(); c++) {
				var conn = connections.get(c);
				if (conn.getTarget().getParent().getId() == this.getId()) {
					sourcecount += 1;
				} else {
					targetcount += 1;
				}
			}

			if (sourcecount > 1) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return 'RESTFul接口节点只能关联一个前置节点。';
			}

			if (targetcount > 0) {
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return 'RESTFul接口节点不能关联后置节点。';
			}
		}

		if (d == null || d.name == '' || !d.url) {
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
	findAllPrevNode : function(nodeList, figure) {
		var connections = figure.getConnections();
		for (var c = 0; c < connections.getSize(); c++) {
			var conn = connections.get(c);
			if ((conn.dasharray == '' || conn.dasharray == '--..') && conn.getTarget().getParent().getId() == figure.getId() && !nodeList.contains(conn.getSource().getParent())) {
				nodeList.add(conn.getSource().getParent());
				this.findAllPrevNode(nodeList, conn.getSource().getParent());
			}
		}
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
					var that = (this.NAME == 'draw2d.shape.node.FlowServiceRest' ? this : this.getParent());
					var userData = that.getUserData();
					var targetId = '';
					if (userData == null) {
						userData = {
							name : that.CNNAME
						};
					}
					targetId = userData.targetid;
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
					form.html(xdoT.render('balantflow.editflow.rest.base', userData));

					var txtParamValue = form.find('#txtParamValue');
					var divParamContainer = form.find('#divParamContainer');
					var sltTargetId = form.find('#sltTargetId');
					var sltAuthtype = form.find('#sltAuthType');
					var sltMethod = form.find('#sltMethod');

					var getProp = function(targetid, container) {
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
									if(preNode != null && preNode.getComposite() != null){// 如果是组合节点，则取组合节点的信息
										var cl = preNode.getComposite().getAssignedFigures();
										for (var di = 0; di < cl.getSize(); di++) {
											var dn = cl.get(di);
											if (dn.getName() != 'draw2d.shape.node.FlowStart') {
												preNode = dn;
												break;
											}
										}
									}
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
						propparams = propparams.replace(/,/g, "");
						$.ajax({
							type : "GET",
							url : '/balantflow/flow/getAllParameterJson.do?' + propparams,
							async : false,
							dataType : 'json',
							success : function(data) {
								container.empty().html(xdoT.render('balantflow.editflow.rest.prop', data));
							}
						});
					};

					getProp(targetId, divParamContainer);

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

					sltTargetId.on('change', function() {
						getProp($(this).val(), divParamContainer);
					});

					sltMethod.on('change', function() {
						if ($(this).val() == 'GET') {
							$('#sltIsTransfer').val('1').attr('disabled', 'disabled');
						} else {
							$('#sltIsTransfer').removeAttr('disabled');
						}
					});

					var scriptEditor = CodeMirror.fromTextArea(txtParamValue[0], {
						mode : "htmlmixed",
						lineNumbers : true
					});
					scriptEditor.setSize('100%', 180);

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
				}
			}
		});
	}
});