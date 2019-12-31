draw2d.shape.node.FlowStateProject = draw2d.shape.node.FlowBaseImage.extend({
	NAME : "draw2d.shape.node.FlowStateProject",
	CNNAME : '项目管理',
	allowBack : true,
	allowRefire : true,
	allowAssign : true,
	allowEoa : false,
	allowGrade : false,
	isValid : function() {
		var connections = this.getConnections();
		if (connections.getSize() == 0) {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return "存在孤立节点，请连线。";
		}else if(connections.getSize() >0){
			var sourceCount = 0;
			var targetCount = 0;
			var hasSource = false;
			var hasTarget = false;
			for(var c = 0; c < connections.getSize(); c++){
				var conn = connections.get(c);
				if (conn.getTarget().getParent().getId() == this.getId()) {
					sourceCount += 1;
					hasSource = true;
				}else{
					targetCount += 1;
				}
			}
			/*if(sourceCount > 1 || !hasSource){
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '只允许一个步骤节点流入项目步骤节点。';
			}
			
			if(targetCount > 1){
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '项目步骤的后置步骤，只能设置一个步骤节点。';
			}else if(targetCount == 0 ){
				this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
				return '请关联项目步骤的后置步骤。';
			}*/
		}
		
		var d = this.getUserData();
		if (d == null || d.name == '' || ((d.team == null || d.team == '') && (d.user == null || d.user == ''))) {
			this.addFigure(this.stateFigure, new draw2d.layout.locator.RightLocator(this));
			return '请设置节点信息。';
		}
		
		for(var c = 0; c < this.getChildren().getSize(); c++){
			if(this.getChildren().get(c).NAME == 'draw2d.shape.basic.Image'){
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
					var that = (this.NAME == 'draw2d.shape.node.FlowStateProject' ? this: this.getParent());
					var name = '', team = null, user = '';
					var userData = that.getUserData();
					if (userData != null) {
						name = userData.name;
						team = userData.team;
						user = userData.user;
					}
					$('#divDialog').modal({
						'backdrop' : false,
						'show' : 'true'
					});
					$('#divDialog').data('obj', that);
					$('#dialogTitle').html(this.CNNAME);
					$.ajax({
						type : "GET",
						url : '/balantflow/resources/js/flow-component/template/project.html',
						async : false,
						success : function(template) {
							form.html(template);
						}
					});
					var txtStepName = form.find('#txtStepName');
					var sltTeam = form.find('#sltTeam');
					txtStepName.val(name);
					
					var sltUser = form.find('#sltUser');
					var tempUser = form.find('#txtTempUser');
					tempUser.val(user);
					
					$.getJSON('getTeamListJson.do', function(data) {
						sltTeam.loadSelect(data);
						sltTeam.val(team);
						getTeamUser(user);
					}).fail(function() {
						showPopMsg.error('获取处理组列表失败：<br/>请检查服务端或者网络是否存在问题。');
					});
					
					sltTeam.change(function() {
						sltTeam.attr('check-type', 'required');
						tempUser.val('');
						getTeamUser(null);
					});

					break;
				case 'urgency':
					var userData = (this.NAME == 'draw2d.shape.node.FlowStateProject' ? this.getUserData() : this.getParent().getUserData());
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
					$('#divDialog').data('obj', this.NAME == 'draw2d.shape.node.FlowStateProject' ? this : this.getParent());
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
				default:
					break;
				}
				;
			}, this),
			x : x,
			y : y,
			items : {
				'form' : {
					name : '编辑步骤信息'
				}/*,
				'urgency' : {
					name : '编辑处理时限'
				}*/
			}
		});
	}
});