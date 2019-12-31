;
(function($) {
	var defaultoptions = {
		dict : {
			'joinright' : '向右合并单元格',
			'splitvertical' : '垂直拆分单元格',
			'joindown' : '向下合并单元格',
			'splithorizontal' : '水平拆分单元格',
			'addcol' : '增加列',
			'delcol' : '删除列',
			'addrow' : '增加行',
			'delrow' : '删除行',
			'tdprop' : '单元格属性',
			'garbagecan' : '回收站'
		},
		icon : {
			'text' : 'glyphicon glyphicon-font',
			'select' : 'fa fa-toggle-down',
			'mselect' : 'fa fa-toggle-down',
			'textarea' : 'glyphicon glyphicon-text-size',
			'form' : 'glyphicon glyphicon-list-alt',
			'radio' : 'fa fa-dot-circle-o',
			'checkbox' : 'fa fa-check-square-o',
			'datetime' : 'glyphicon glyphicon-time',
			'file' : 'glyphicon glyphicon-cloud-upload'
		}
	};

	var formviewer = function(target, options, readonly) {
		var that = this;
		var outputHtml = options.outputHtml;
		var formData = options.formData;
		var regex = /\#\{([^\}]+?)\}/g;
		var result = null;
		while ((result = regex.exec(outputHtml)) != null) {
			var fieldName = result[1];
			var confObj = null;
			for (var i = 0; i < formData.length; i++) {
				if (fieldName == formData[i].name) {
					confObj = formData[i];
					break;
				}
			}
			if (confObj) {
				outputHtml = outputHtml.replace(result[0], '<div class="divTrick" data-fieldtype="' + confObj.controlType + '" data-fieldname="' + confObj.name + '"></div>');
			}
		}
		var $output = $(outputHtml);
		var dataid = options._dataid || that.createDataId();
		that.dataId = dataid;
		$output.find('.divTrick').each(function() {
			var fieldname = $(this).data('fieldname');
			var fieldtype = $(this).data('fieldtype');
			var confObj = {};
			// confObj.uuid = uuid;
			for (var i = 0; i < formData.length; i++) {
				if (fieldname == formData[i].name) {
					confObj = formData[i];
				}
			}
			if (options && options.formId) {
				confObj.formId = options.formId;
			}
			if (options && options.fieldList) {
				for (var i = 0; i < options.fieldList.length; i++) {
					if (options.fieldList[i].name == fieldname) {
						confObj.value = options.fieldList[i].value;
						break;
					}
				}
			}

			confObj.dataid = dataid;
			if (readonly) {
				var html = xdoT.render('balantflow.form.controller.readonly.' + fieldtype, confObj);
				$(this).replaceWith(html);
			} else {
				var html = xdoT.render('balantflow.form.controller.' + fieldtype, confObj);
				$(this).replaceWith(html);
			}
		});
		$output.find('.bodytd').on('mouseenter', function() {
			$(this).removeClass('error');
		});
		that.output = $output;
		if (target) {
			$(target).empty().html($output);
		}
	};

	this.formviewer = formviewer;

	formviewer.prototype = {
		getOutput : function() {
			return this.output;
		},
		getDataId : function() {
			return this.dataId;
		},
		createDataId : function() {
			return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
				var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
				return v.toString(16);
			});
		},
		setError : function(fieldName) {
			var that = this;
			var item = that.output.find('[name=' + fieldName + ']');
			if (item.length > 0) {
				item.closest('.bodytd').addClass('error');
			}
		}
	};

	var formbuilder = function(target, options) {
		var that = this;
		that.config = $.extend(true, {}, defaultoptions, options);
		that.$target = $(target);
		that.$target.hide();
		that.$target.wrap('<div class="formbuilder-wraper"></div>');
		that.$wraper = that.$target.closest('.formbuilder-wraper');

		that.$btnbar = $('<div class="formbuilder-btnbar"></div>');
		// that.$btnforward = $('<button class="btn btn-sm btn-default"><i
		// class="fa fa-mail-forward"></i></button>');
		// that.$btnreplay = $('<button class="btn btn-sm btn-default"><i
		// class="fa fa-mail-reply"></i></button>');
		that.$garbagecan = $('<div class="formbuilder-garbagecan"><i class="glyphicon glyphicon-trash"></i>拖到这里删除</div>');

		that.$btnbar.append(that.$garbagecan);
		// that.$btnbar.append(that.$btnreplay);
		// that.$btnbar.append(that.$btnforward);

		that.$wraper.append(that.$btnbar);

		that.$editoriframe = $('<iframe src="/balantflow/form/formEditor.do" frameborder="0" class="formbuilder-editoriframe" allowTransparency="true" style="width:100%;height:100%"></iframe>');
		that.$wraper.append(that.$editoriframe);

		that.$garbagecan.on('dragenter', function(e) {
			e.preventDefault();
			e.stopPropagation();
		});

		that.$garbagecan.on('dragover', function(e) {
			e.preventDefault();
			e.stopPropagation();
		})

		that.$garbagecan.bind('drop', function(event) {
			event.preventDefault();
			event.stopPropagation();
			that.$garbagecan.removeClass('actived');
			var e;
			if (event.isTrigger) {
				e = triggerEvent.originalEvent;
			} else {
				var e = event.originalEvent;
			}
			var type = e.dataTransfer.getData('text');
			if (type.indexOf('uuid') == 0) {
				var uuid = type.substring(5);
				that.$editor.find('.trickController').each(function() {
					if (uuid == $(this).data('uuid')) {
						$(this).remove();
						return false;
					}
				});
			}
		});

		that.$editoriframe.on('load', function() {
			that.$window = $(that.$editoriframe[0].contentWindow);
			that.$document = $(that.$editoriframe[0].contentWindow.document);
			that.$editor = $(that.$editoriframe[0].contentWindow.document.body);
			that.$header = $(that.$editoriframe[0].contentWindow.document.head);
			that.$editor[0].contentEditable = "true";
			if ($.trim(that.$target.val()) != '') {
				that.init(JSON.parse(that.$target.val()));
			} else {
				that.drawTable(20, 20);
			}

			/*
			 * that.$editor.keydown(function(e) { var key = e.keyCode ||
			 * e.charCode; if (key == 8 || key == 46) { e.preventDefault();
			 * //e.stopPropagation(); } });
			 */

			that.$editor.on('click', function(e) {
				e.stopPropagation();
				if (!that.isInContextMenu) {
					that.hideContextMenu();
				}
			});

			that.$editor.bind("cut copy paste", function(e) {
				e.preventDefault();
			});

			// body不接受拖拽
			that.$editor.on('drop', function(e) {
				e.preventDefault();
				e.stopPropagation();
			});

			that.$editor.on('focus', function(e) {

			});

		});

	}

	this.formbuilder = formbuilder;

	formbuilder.prototype = {
		init : function(jsonObj) {
			var that = this;
			that.$editor.html(that.decodeHtml(jsonObj.originalHtml));
			that.$maintable = $('.formbuilder-maintable', that.$editor);
			$('td.root', that.$editor).each(function() {
				that.initRootTdEvent($(this));
			});
			$('td.header', that.$editor).each(function() {
				that.initHeaderTdEvent($(this));
			});
			$('td.lefter', that.$editor).each(function() {
				that.initLefterTdEvent($(this));
			});
			$('td.bodytd', that.$editor).each(function() {
				that.initTdEvent($(this));
			});
			$('.trickController', that.$editor).each(function() {
				that.initTrickControllerEvent($(this));
				if (jsonObj.formData && jsonObj.formData.length > 0) {
					for (var i = 0; i < jsonObj.formData.length; i++) {
						if (jsonObj.formData[i].uuid == $(this).data('uuid')) {
							$(this).data('prop', jsonObj.formData[i]);
						}
					}
				}
			});
		},
		reload : function(jsonObj) {
			var that = this;
			that.$editor.empty();
			that.init(jsonObj);
		},
		sync : function() {
			var that = this;
			var json = {};
			json.outputHtml = that.getHtml();
			json.originalHtml = that.getOriginalHtml();
			json.formData = that.getData();
			var jsonStr = JSON.stringify(json);
			that.$target.val(jsonStr);
			return jsonStr;
		},
		getOriginalHtml : function() {
			var that = this;
			return that.$editor.html();
		},
		getHtml : function() {
			var that = this;
			var returnTable = $('<table class="formbuilder-outputtable"></table>');
			var minX = Number.MAX_VALUE, minY = Number.MAX_VALUE, maxX = 0, maxY = 0;
			that.$maintable.find('tr').each(function() {
				$(this).find('.bodytd').each(function() {
					if ($(this).html() != '') {
						var pos = that.getPosition($(this));
						minX = minX > pos.X1 ? pos.X1 : minX;
						minY = minY > pos.Y1 ? pos.Y1 : minY;
						maxX = maxX < pos.X2 ? pos.X2 : maxX;
						maxY = maxY < pos.Y2 ? pos.Y2 : maxY;
					}
				});
			});
			that.$maintable.find('tr').each(function() {
				var tr = $('<tr></tr>');
				$(this).find('.bodytd').each(function() {
					var pos = that.getPosition($(this));
					if (minX <= pos.X1 && minY <= pos.Y1 && maxX >= pos.X2 && maxY >= pos.Y2) {
						var td = $(this).clone();
						td.find('.trickController').each(function() {
							var name = $(this).find('label').text();
							$(this).replaceWith(name);
						});
						tr.append(td);
					}
				});
				if (tr.html() != '') {
					returnTable.append(tr);
				}
			});
			var div = $('<div></div>');
			div.append(returnTable);
			return div.html();
		},
		isEmpty : function() {
			var that = this;
			var isEmpty = true;
			that.$maintable.find('.bodytd').each(function() {
				if ($(this).html() != '') {
					isEmpty = false;
					return false;
				}
			});
			return isEmpty;
		},
		valid : function() {
			var that = this;
			var isValid = true;
			that.$maintable.find('.trickController').each(function() {
				if (!$(this).data('prop') || $.isEmptyObject($(this).data('prop'))) {
					isValid = false;
					$(this).addClass('error');
				} else {
					$(this).removeClass('error');
				}
			});
			return isValid;
		},
		getData : function() {
			var that = this;
			var propList = new Array();
			that.$maintable.find('.trickController').each(function() {
				if ($(this).data('prop') && !$.isEmptyObject($(this).data('prop'))) {
					propList.push($(this).data('prop'));
				}
			});
			return propList;
		},
		getPosition : function(item) {
			var position = {};
			position.X1 = item.offset().left;
			position.X2 = item.offset().left + item.outerWidth();
			position.Y1 = item.offset().top;
			position.Y2 = item.offset().top + item.outerHeight();
			return position;
		},
		startDrag : function(item) {
			var that = this;
			if (!that.$dragItem) {
				that.$dragItem = $(item).clone();
				var top = $(item).offset().top;
				var left = $(item).offset().left;
				that.$dragItem.css({
					'position' : 'absolute',
					'opacity' : 0.5,
					'top' : top + 'px',
					'left' : left + 'px'
				});
				$(document).on('mouseup', function() {
					that.endDrag();
				});
				$('body').append(that.$dragItem);
			}
		},
		doDrag : function(e) {
			var that = this;
			if (that.$dragItem) {
				var x = that.$window.scrollLeft() + e.clientX - that.$dragItem.outerWidth() / 2;
				var y = that.$window.scrollTop() + e.clientY - that.$dragItem.outerHeight() / 2;
				if (!that.dragtimer) {
					that.dragtimer = window.setTimeout(function() {
						that.$dragItem.css({
							'top' : y + 'px',
							'left' : x + 'px'
						});
						that.dragtimer = null;
					}, 25);
				}
			}
		},
		endDrag : function() {
			var that = this;
			if (that.$dragItem) {
				that.$dragItem.remove();
				that.$dragItem = null;
			}
		},
		encodeHtml : function(str) {
			var s = "";
			if (typeof str != 'undefined' && str != null) {
				if (str.length == 0)
					return "";
				s = str.replace(/&/g, "&amp;");
				s = s.replace(/</g, "&lt;");
				s = s.replace(/>/g, "&gt;");
				s = s.replace(/\'/g, "&#39;");
				s = s.replace(/\"/g, "&quot;");
			}
			return s;
		},
		decodeHtml : function(str) {
			var s = "";
			if (typeof str != 'undefined' && str != null) {
				if (str.length == 0)
					return "";
				s = str.replace(/&quot;/g, "\"");
				s = s.replace(/&#39;/g, "\'");
				s = s.replace(/&gt;/g, ">");
				s = s.replace(/&lt;/g, "<");
				s = s.replace(/&amp;/g, "&");
			}
			return s;
		},
		hideContextMenu : function() {
			var that = this;
			if (that.$contextmenu) {
				that.$contextmenu.slideUp('fast', function() {
					that.$contextmenu.remove();
					that.$contextmenu = null;
				});
			}
		},
		showContextMenu : function(mouseX, mouseY, optionList) {
			var that = this;
			if (that.$contextmenu) {
				that.$contextmenu.remove();
			}
			that.$contextmenu = $('<div class="formbuilder-contextmenu"></div>');
			var x = mouseX + that.$window.scrollLeft();
			var y = mouseY + that.$window.scrollTop();

			that.$contextmenu.css({
				'top' : y + 'px',
				'left' : x + 'px'
			});
			var ul = $('<ul></ul>');
			for (var i = 0; i < optionList.length; i++) {
				ul.append(optionList[i]);
			}
			that.$contextmenu.append(ul);
			that.$editor.append(that.$contextmenu);
			var width = that.$contextmenu.outerWidth();
			if (x + width > that.$window.width() + that.$window.scrollLeft()) {
				that.$contextmenu.css('left', ((x - width) + 'px'));
			}
			that.$contextmenu.slideDown('fast');
			that.$contextmenu.on('mouseenter', function() {
				that.isInContextMenu = true;
			});
			that.$contextmenu.on('mouseleave', function() {
				that.isInContextMenu = false;
			});
		},
		createLefterTd : function() {
			var that = this;
			if (!that.rownum) {
				that.rownum = 1;
			} else {
				that.rownum += 1;
			}

			var $td = $('<td class="lefter">' + that.rownum + '</td>');
			that.initLefterTdEvent($td);
			return $td;
		},
		initLefterTdEvent : function($td) {
			var that = this;
			$td.attr('unselectable', 'on');
			$td.css('user-select', 'none');
			$td.on('selectstart', false);
			$td.bind('contextmenu', function(e) {
				var optionList = new Array();
				var $menu = $('<li>' + that.config.dict.addrow + '</li>');
				$menu.addClass('actived');
				$menu.on('click', function() {
					var $tr = $('<tr></tr>');
					that.$maintable.find('.header').each(function(i, k) {
						if (i == 0) {
							$tr.append(that.createLefterTd());
						} else {
							$tr.append(that.createTd());
						}
					});
					that.$maintable.append($tr);
					that.hideContextMenu();
				});
				optionList.push($menu);

				var $menu = $('<li>' + that.config.dict.delrow + '</li>');
				$menu.addClass('actived');
				$menu.on('click', function() {
					$td.closest('tr').remove();
					that.hideContextMenu();
				});
				optionList.push($menu);

				if (optionList.length > 0) {
					that.showContextMenu(e.clientX, e.clientY, optionList);
				}
				return false;
			});
		},
		createRootTd : function() {
			var that = this;
			var $td = $('<td class="root"></td>');
			that.initRootTdEvent($td);
			return $td;
		},
		initRootTdEvent : function($td) {
			var that = this;
			$td.attr('unselectable', 'on');
			$td.css('user-select', 'none');
			$td.on('selectstart', false);
			$td.bind('contextmenu', function(e) {
				var optionList = new Array();
				var $menu = $('<li>' + that.config.dict.addcol + '</li>');
				$menu.addClass('actived');
				$menu.on('click', function() {
					that.$maintable.find('tr').each(function(i, k) {
						if (i == 0) {
							$(this).append(that.createHeaderTd());
						} else {
							$(this).append(that.createTd());
						}
					});
					that.hideContextMenu();
				});
				optionList.push($menu);

				var $menu = $('<li>' + that.config.dict.addrow + '</li>');
				$menu.addClass('actived');
				$menu.on('click', function() {
					var $tr = $('<tr></tr>');
					that.$maintable.find('.header').each(function(i, k) {
						if (i == 0) {
							$tr.append(that.createLefterTd());
						} else {
							$tr.append(that.createTd());
						}
					});
					that.$maintable.append($tr);
					that.hideContextMenu();
				});
				optionList.push($menu);

				if (optionList.length > 0) {
					that.showContextMenu(e.clientX, e.clientY, optionList);
				}
				return false;
			});
		},
		createHeaderTd : function() {
			var that = this;
			if (!that.colnum) {
				that.colnum = 65;
			} else {
				if (that.colnum == 90) {
					that.colnum = 65;
				} else {
					that.colnum += 1;
				}
			}

			var $td = $('<td class="header">' + String.fromCharCode(that.colnum) + '</td>');
			that.initHeaderTdEvent($td);
			return $td;
		},
		initHeaderTdEvent : function($td) {
			var that = this;
			$td.attr('unselectable', 'on');
			$td.css('user-select', 'none');
			$td.on('selectstart', false);
			$td.bind('contextmenu', function(e) {
				var optionList = new Array();
				var $menu = $('<li>' + that.config.dict.addcol + '</li>');
				$menu.addClass('actived');
				$menu.on('click', function() {
					that.$maintable.find('tr').each(function(i, k) {
						if (i == 0) {
							$(this).append(that.createHeaderTd());
						} else {
							$(this).append(that.createTd());
						}
					});
					that.hideContextMenu();
				});
				optionList.push($menu);

				var $menu = $('<li>' + that.config.dict.delcol + '</li>');
				$menu.addClass('actived');
				$menu.on('click', function() {
					var index = $td.index();
					that.$maintable.find('tr').each(function(i, k) {
						var deltd = $(this).find('td:eq(' + index + ')');
						if (deltd.length > 0) {
							deltd.remove();
						}
					});
					that.hideContextMenu();
				});
				optionList.push($menu);

				if (optionList.length > 0) {
					that.showContextMenu(e.clientX, e.clientY, optionList);
				}
				return false;
			});
		},
		getEditor : function() {
			return this.$editor;
		},
		getFormData : function(form) {
			var formData = {};
			var json = form.serializeArray();
			if (json && !$.isEmptyObject(json)) {
				for (var i = 0; i < json.length; i++) {
					if (formData[json[i].name]) {
						if (formData[json[i].name] instanceof Array) {
							formData[json[i].name].push(json[i].value);
						} else {
							var tmp = formData[json[i].name];
							formData[json[i].name] = new Array();
							formData[json[i].name].push(tmp);
						}
					} else {
						formData[json[i].name] = json[i].value;
					}
				}
			}
			return formData;
		},
		getUuid : function() {
			return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
				var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
				return v.toString(16);
			});
		},
		initTrickControllerEvent : function(item) {
			var that = this;
			item.on('click', function(ev) {
				$(this).removeClass('error');
				var type = item.data('type');
				var html = xdoT.render('balantflow.formbuilder.' + type + 'prop', $(this).data('prop') || {
					uuid : item.data('uuid')
				});
				createSlideDialog({
					title : '编辑控件：' + type,
					content : html,
					width : 700,
					successFuc : function() {
						if ($('#formProp').valid()) {
							var fd = that.getFormData($('#formProp'));
							var name = fd['name'];
							isExists = false;
							that.$editor.find('.trickController').each(function() {
								if (!$(this).is(item)) {
									var cfd = $(this).data('prop');
									if (cfd && name == cfd['name']) {
										isExists = true;
									}
								}
							});
							if (isExists) {
								showPopMsg.info('控件id：' + name + '已存在');
								return false;
							}
							item.data('prop', fd);
							item.find('label').text('#{' + name + '}');
							return true;
						} else {
							return false;
						}
					}
				});
			});
			item.on('dragstart', function(e) {
				e.originalEvent.dataTransfer.setData('text', 'uuid:' + item.data('uuid'));
				that.$garbagecan.addClass('actived');
			});

			item.on('dragend', function(e) {
				that.$garbagecan.removeClass('actived');
			});
		},
		createTrickController : function(type) {
			var that = this;
			var uuid = that.getUuid();
			var item = $('<div class="trickController" draggable="true" contentEditable="false" data-uuid="' + uuid + '" data-type="' + type + '"><i class="' + that.config.icon[type] + '"></i><label>#{' + type + '}</label></div>');
			that.initTrickControllerEvent(item);
			return item;
		},
		createTd : function() {
			var that = this;
			var $td = $('<td class="bodytd"></td>');
			that.initTdEvent($td);
			return $td;
		},
		initTdEvent : function($td) {
			var that = this;

			/*
			 * $td.bind('copy', function(e) {
			 * window.clipboardData.setData('Text', $(this).text());
			 * e.preventDefault(); alert('done'); });
			 * 
			 * $td.bind('paste', function(e) { var data =
			 * window.clipboardData.getData('Text'); alert(data); //
			 * console.log(data); // return false; });
			 */
			/*
			 * $td.on('keydown', function(e) { alert('aaa'); var key = e.keyCode ||
			 * e.charCode; if (key == 8 || key == 46) { alert(key);
			 * e.preventDefault(); // e.stopPropagation(); } });
			 */

			$td.bind('drop', function(event) {
				var td = $(this);
				event.preventDefault();
				event.stopPropagation();
				var e;
				if (event.isTrigger) {
					e = triggerEvent.originalEvent;
				} else {
					var e = event.originalEvent;
				}
				try {
					var type = e.dataTransfer.getData('text');
					if (type.indexOf('uuid') == 0) {
						var uuid = type.substring(5);
						that.$editor.find('.trickController').each(function() {
							if (uuid == $(this).data('uuid')) {
								td.append($(this));
								return false;
							}
						});
					} else {
						td.append(that.createTrickController(type));
					}
				} catch (ex) {
					console.log(ex);
				}
				td.removeClass('enter');
			});
			$td.bind('dragenter', function(e) {
				e.preventDefault();
				e.stopPropagation();
				$(this).addClass('enter');
			});
			$td.bind('dragleave', function(e) {
				e.preventDefault();
				e.stopPropagation();
				$(this).removeClass('enter');
			});
			$td.bind('contextmenu', function(e) {
				var optionList = new Array();
				var td = $(this);
				var tdpos = that.getPosition(td);
				var table = $(this).closest('table');
				var tr = $(this).closest('tr');
				var trindex = tr.index();
				var tdindex = td.index();
				var $menu = $('<li>' + that.config.dict.joinright + '</li>');
				var rowspan = 1, colspan = 1;
				if (td.attr('colspan')) {
					colspan = parseInt(td.attr('colspan'), 10);
				}
				if (td.attr('rowspan')) {
					rowspan = parseInt(td.attr('rowspan'), 10);
				}
				if (td.next().length > 0) {
					var nextcolspan = 1, nextrowspan = 1;
					var tdnext = td.next();
					var tmppos = that.getPosition(tdnext);

					if (tdpos.X2 == tmppos.X1 && tdpos.Y1 == tmppos.Y1 && tdpos.Y2 == tmppos.Y2) {
						if (tdnext.attr('colspan')) {
							nextcolspan = parseInt(tdnext.attr('colspan'), 10);
						}
						$menu.addClass('actived');
						$menu.on('click', (function(tdnext, finalcolspan) {
							return function(e) {
								e.stopPropagation();
								td.attr('colspan', finalcolspan);
								tdnext.remove();
								that.hideContextMenu();
							}
						}(tdnext, colspan + nextcolspan)));
					}
				}
				optionList.push($menu);

				var $menu = $('<li>' + that.config.dict.joindown + '</li>');
				var trnext = table.find('tr:eq(' + (rowspan + trindex) + ')');
				if (trnext.length > 0) {
					var nextcolspan = 1, nextrowspan = 1;
					trnext.find('.bodytd').each(function() {
						var tmppos = that.getPosition($(this));
						if (tdpos.X1 == tmppos.X1 && tdpos.Y2 == tmppos.Y1 && tdpos.X2 == tmppos.X2) {
							tdnext = $(this);
							if (tdnext.attr('rowspan')) {
								nextrowspan = parseInt(tdnext.attr('rowspan'), 10);
							}
							$menu.addClass('actived');
							$menu.on('click', (function(tdnext, finalrowspan) {
								return function(e) {
									e.stopPropagation();
									td.attr('rowspan', finalrowspan);
									tdnext.remove();
									that.hideContextMenu();
								}
							}(tdnext, rowspan + nextrowspan)));
							return false;
						}
					});

				}
				optionList.push($menu);

				var $menu = $('<li>' + that.config.dict.splitvertical + '</li>');
				if (colspan > 1) {
					$menu.addClass('actived');
					$menu.on('click', function(e) {
						e.stopPropagation();
						td.attr('colspan', colspan - 1);
						for (var i = 0; i < rowspan; i++) {
							table.find('tr:eq(' + (trindex + i) + ')').append(that.createTd());
						}
						that.hideContextMenu();
					});
				}
				optionList.push($menu);

				var $menu = $('<li>' + that.config.dict.splithorizontal + '</li>');
				if (rowspan > 1) {
					$menu.addClass('actived');
					$menu.on('click', function(e) {
						e.stopPropagation();
						td.attr('rowspan', rowspan - 1);
						for (var i = 0; i < colspan; i++) {
							table.find('tr:eq(' + (trindex + rowspan - 1) + ')').append(that.createTd());
						}
						that.hideContextMenu();
					});
				}
				optionList.push($menu);

				var $menu = $('<li>' + that.config.dict.tdprop + '</li>');
				$menu.addClass('actived');
				$menu.on('click', function(e) {
					e.stopPropagation();
					var cssjson = that.getCssJson(td);
					var html = xdoT.render('balantflow.formbuilder.tdprop', cssjson || {});
					createSlideDialog({
						width : 700,
						title : that.config.dict.tdprop,
						content : html,
						successFuc : function() {
							if ($('#formProp').valid()) {
								var fd = $('#formProp').serializeArray();
								var css = {}
								for (var i = 0; i < fd.length; i++) {
									css[fd[i].name] = fd[i].value;
								}
								td.css(css);
								return true;
							} else {
								return false;
							}
						}
					});
					that.hideContextMenu();
				});
				optionList.push($menu);

				if (optionList.length > 0) {
					that.showContextMenu(e.clientX, e.clientY, optionList);
				}
				return false;
			});
		},
		drawTable : function(rows, cols) {
			var that = this;
			if (that.$maintable) {
				that.$maintable.remove();
			}
			that.$maintable = $('<table class="formbuilder-maintable"></table>');
			for (var r = 0; r <= rows; r++) {
				var tr = $('<tr></tr>');
				for (var c = 0; c <= cols; c++) {
					var td = null;
					if (r == 0) {
						if (c == 0) {
							td = that.createRootTd();
						} else {
							td = that.createHeaderTd();
						}
					} else {
						if (c == 0) {
							td = that.createLefterTd();
						} else {
							td = that.createTd();
						}

					}
					tr.append(td);
				}
				that.$maintable.append(tr);
			}
			that.$editor.empty().html(that.$maintable);
		},
		getCssJson : function(target) {
			var css = {};
			if (target.attr('style')) {
				var style = target.attr('style');
				var styles = style.split(';');
				for (var i = 0; i < styles.length; i++) {
					var s = styles[i];
					var key = $.trim(s.split(':')[0]);
					var value = $.trim(s.split(':')[1]);
					css[key] = value;
				}
			}
			return css;
		}
	};

	$.fn.formviewer = function(options, readonly) {
		var $target = $(this);
		var fv = new formviewer($target, options, readonly);
		return fv;
	};

	$.formviewer = function(options, readonly) {
		var fv = new formviewer(null, options, readonly);
		return fv;
	}

	$.fn.formbuilder = function(options) {
		var $target = $(this);
		var fb = null;
		if ($target[0].tagName.toLowerCase() == 'textarea') {
			if (!$target.attr('bind-formbuilder')) {
				fb = new formbuilder($target, options);
				$target.attr('bind-formbuilder', true);
			}
		}
		return fb;
	};

})(jQuery);