;
(function($) {

	var formbuilder = function(target, options) {
		var that = this;
		options = !options ? {} : options;
		this.dashboard = $(target).dashboard({
			minBlockRow : 3,
			minBlockColumn : 4,
			readOnly : options.readOnly || false,
			onReady : function(portlet) {
				var scriptList = new Array();
				portlet.getBody().find('script.xdotScript').each(function() {
					scriptList.push($(this).html());
				});
				for (var s = 0; s < scriptList.length; s++) {
					try {
						var fn = null, loadFn = null;
						eval(scriptList[s]);
						if (typeof (fn) == 'object') {
							for ( var dom in fn) {
								var fucList = fn[dom];
								for ( var fuc in fucList) {
									portlet.getBody().find(dom).off(fuc).on(fuc, fucList[fuc]);
								}
							}
						}
						fn = null;
					} catch (e) {
						console.error(e);
					}
				}
			},
			onEdit : function(portlet) {
				var container = portlet.getBody().find('.customControlContainer');
				var type = container.data('type');
				var conf = {
					type : type,
					name : container.data('name'),
					title : container.data('title'),
					mustinput : container.data('mustinput')
				};
				if (container.data('mutiline')) {
					conf.multiple = 'true';
				} else {
					conf.multiple = 'false';
				}
				var controller = $('.customControl', container);
				if (type == 'text' || type == 'textarea') {
					conf.placeholder = controller.attr('placeholder') || '';
				} else if (type == 'time') {
					conf.timeformat = controller.data('timeformat') || '';
				} else if (type == 'select') {
					var optList = new Array();
					controller.find('option').each(function() {
						var opt = {
							'value' : $(this).val(),
							'text' : $(this).text()
						};
						if ($(this).prop('selected')) {
							opt.selected = true;
						}
						optList.push(opt);
					});
					conf.multiple = controller.attr('multiple') ? "true" : "false";
					conf.valueList = JSON.stringify(optList, null, 2);
				} else if (type == 'radio' || type == 'checkbox') {
					var optList = new Array();
					controller.each(function() {
						var opt = {
							'value' : $(this).val(),
							'text' : $(this).data('text')
						};
						if ($(this).prop('checked')) {
							opt.selected = true;
						}
						optList.push(opt);
					});
					conf.valueList = JSON.stringify(optList, null, 2);
				} else if (type == 'inputselect') {
					conf.url = controller.data('url') || '';
					conf.root = controller.data('root') || '';
					conf.multiple = controller.attr('multiple') ? "true" : "false";
				}
				var scriptContainer = container.find('.xdotScript');
				var script = $.trim(scriptContainer.html());
				if (script) {
					var fn = null;
					try {
						eval(script);
					} catch (e) {

					}
					if (fn) {
						for ( var target in fn) {
							if (fn[target]) {
								for ( var event in fn[target]) {
									if (fn[target][event]) {
										var s = fn[target][event].toString();
										var body = s.slice(s.indexOf("{") + 1, s.lastIndexOf("}"));
										if ($.trim(body)) {
											conf.script = $.trim(body);
											conf.action = event;
											break;
										}
									}
								}
							}
						}
					}
				}
				var html = xdoT.render('balantflow.formbuilder-new.controller.config', conf);
				var dialog = createSlideDialog({
					title : '编辑控件',
					content : html,
					width : 800,
					successFuc : function() {
						var editor = $('#txtScript').data('editor');
						if (editor) {
							editor.save();
						}
						if ($('#formConfigControl').valid()) {
							var json = $('#formConfigControl').toJson();
							if (json.valueList) {
								try {
									json.valueList = JSON.parse(json.valueList);
								} catch (e) {
									json.valueList = null;
								}
							}
							var node = that.createNode(json);
							portlet.getBody().empty().html(node.getContent());
							portlet.setTitle(json.title);
							dialog.hide();
						}
						return false;
					}
				});
			}
		});
		this.nodeList = [];
	}

	this.formbuilder = formbuilder;
	formbuilder.prototype = {
		getUuid : function() {
			var chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
			var num = chars.length;
			var uuid = '';
			for (var i = 0; i < 6; i++) {
				uuid += chars[parseInt(Math.random() * (num - 1 + 1), 10)];
			}
			return uuid;
		},
		addNode : function(data) {
			var that = this;
			var n = that.createNode(data);
			var portlet = that.dashboard.addPortlet({
				title : n.getTitle(),
				content : n.getContent(),
				width : 12,
				height : 5,
				editable : true
			});
		},
		createNode : function(data) {
			var that = this;
			if (!data.valueList) {
				data.valueList = [ {
					value : "选项一",
					text : "选项一"
				}, {
					value : "选项二",
					text : "选项二"
				} ];
			}
			if (!data.uuid) {
				data.uuid = that.getUuid();
			}
			var nodecontent = xdoT.render('balantflow.formbuilder-new.controller.' + data.type, data);
			var n = new formbuilder.node(data, nodecontent);
			return n;
		}
	};

	formbuilder.node = function(config, content) {
		this.config = config;
		this.title = config.title || '';
		this.content = content || '';
	};

	formbuilder.node.prototype = {
		getTitle : function() {
			return this.title;
		},
		getContent : function() {
			return this.content;
		}
	};

	$.fn.formbuilder = function(options) {
		var $target = $(this);
		d = new formbuilder($target, options);
		return d;
	};

})(jQuery);
