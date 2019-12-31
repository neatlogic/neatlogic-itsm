;
(function($) {
	var defaultoptions = {
		columnCount : 24,
		// rowCount : 30,
		minBlockColumn : 10,
		minBlockRow : 4,
		blockHeight : 20,
		readOnly : false,
		shadowColor : '#e42332',
		backgroundColor : null,
		blockPadding : null,
		onEdit : null,
		onReady : null,
		onDelete : null,
		onEditTitle : null,
		onFullscreen : null
	};

	var dashboard = function(target, options) {
		var that = this;
		that.config = $.extend(true, {}, defaultoptions, options);
		that._width = $(target).width();
		if (!$(target).height()) {
			$(target).css('height', ($(target).offsetParent().height() - $(target).offset().top) + 'px');
		}
		that._height = $(target).height();
		that.$target = $(target);
		if (this.config.backgroundColor) {
			that.$target.css('background', this.config.backgroundColor);
		}
		that.portletList = new Array();
		that.$target.addClass('jquery-dashboard');
		$(window).on('mouseup', function(e) {
			for (var i = 0; i < that.portletList.length; i++) {
				that.portletList[i].endResize();
				that.portletList[i].endMove();
			}
		});

		$(window).on('resize', function() {
			that.resetBlockposition();
		});

		that.$target.find('.jquery-dashboard-portlet').each(function() {
			that.addOldPortlet($(this));
		});

	}

	dashboard.portlet = function(dashboard, options, jqueryObj) {
		var that = this;
		that.dashboard = dashboard;
		if (options) {
			if (options.startcolumn >= 0 && options.endcolumn > options.startcolumn && options.startrow >= 0 && options.endrow > options.startrow) {
				that.config = options;
			} else if (options.width > 0 && options.height > 0) {
				that.config = options;
				options.width = Math.min(that.dashboard.config.columnCount, options.width);
				options.height = options.height;
				var startRow = 0;
				for (var i = 0; i < that.dashboard.portletList.length; i++) {
					startRow = Math.max(startRow, that.dashboard.portletList[i].config.endrow);
				}
				that.config.startrow = startRow;
				that.config.endrow = startRow + options.height;
				that.config.startcolumn = options.startcolumn || 0;
				that.config.endcolumn = options.endcolumn || options.width;
				that.config.pluginId = options.pluginId;
				that.config.datasourceId = options.datasourceId;
				var newStartRow = 0;
				var startColumn = that.config.startcolumn;
				var endColumn = that.config.endcolumn;
				var startRow = that.config.startrow;
				var endRow = that.config.endrow;
				var rowSpan = endRow - startRow;
				for (var i = 0; i < that.dashboard.portletList.length; i++) {
					var p = that.dashboard.portletList[i];
					if (this != p) {
						var pStartColumn = p.config.startcolumn;
						var pEndColumn = p.config.endcolumn;
						var pStartRow = p.config.startrow;
						var pEndRow = p.config.endrow;
						if ((startColumn <= pStartColumn && endColumn > pStartColumn) || (endColumn >= pEndColumn && startColumn < pEndColumn) || (startColumn >= pStartColumn && endColumn <= pEndColumn)) {
							if (startRow >= pStartRow && startRow <= pStartRow + 1) {
								newStartRow = pStartRow;
							} else if (startRow > pStartRow + 1) {
								newStartRow = Math.max(newStartRow, pEndRow);
							}
						}
					}
				}
				that.config.startrow = newStartRow;
				that.config.endrow = that.config.startrow + rowSpan;

			} else {
				console.error('缺少坐标参数或面积参数');
			}
			that.$portlet = $('<div class="jquery-dashboard-portlet"></div>');
			if (!that.config.type || that.config.type != 'blank') {
				that.$container = $('<div class="jquery-dashboard-container"></div>');
				that.$header = $('<div class="jquery-dashboard-header"></div>');
				that.$title = $('<div class="jquery-dashboard-title fz16"></div>');
				that.$title.html(options.title);
				that.$header.append(that.$title);
				that.$body = $('<div class="jquery-dashboard-body tsscroll-container"></div>');
				that.$body.append(options.content);
				that.$container.append(that.$header).append(that.$body);
				that.$portlet.append(that.$container);
			} else if (that.config.type == 'blank') {
				that.$container = $('<div class="jquery-dashboard-container empty-container"></div>');
				that.$header = $('<div class="jquery-dashboard-header"></div>');
				that.$body = $('<div class="jquery-dashboard-body tsscroll-container"></div>');
				that.$container.append(that.$header).append(that.$body);
				that.$portlet.append(that.$container);
			}
			that.dashboard.$target.append(that.$portlet);
		} else if (jqueryObj) {
			var options = {};
			options.startcolumn = jqueryObj.data('sc');
			options.endcolumn = jqueryObj.data('ec');
			options.startrow = jqueryObj.data('sr');
			options.endrow = jqueryObj.data('er');
			that.config = options;
			that.config.editable = true;
			that.$portlet = jqueryObj;
			that.$container = $('.jquery-dashboard-container', jqueryObj);
			that.$header = $('.jquery-dashboard-header', jqueryObj);
			that.$header.css({
				'cursor' : 'default'
			});
			that.$title = $('.jquery-dashboard-title', jqueryObj);
			that.$body = $('.jquery-dashboard-body', jqueryObj);
			$('.jquery-dashboard-resizebox', jqueryObj).remove();
			$('.jquery-dashboard-titleeditor', jqueryObj).remove();
			$('.jquery-dashboard-closer', jqueryObj).remove();
			$('.jquery-dashboard-editor', jqueryObj).remove();
		}

		if (!that.dashboard.config.readOnly) {
			if (!that.config.type || that.config.type != 'blank') {
				// 编辑按钮
				if (that.config.editable && that.dashboard.config.onEdit && typeof that.dashboard.config.onEdit == 'function') {
					that.$editor = $('<i class="ts-pencil jquery-dashboard-editor jquery-dashboard-actionicon"></i>');
					that.$header.append(that.$editor);
					that.$editor.on('click', function(e) {
						e.stopPropagation();
						that.dashboard.config.onEdit(that);
					})
				}
				// 编辑标题按钮
				if (that.dashboard.config.onEditTitle && typeof that.dashboard.config.onEditTitle == 'function') {
					that.$titleeditor = $('<i class="glyphicon glyphicon-option-horizontal jquery-dashboard-titleeditor jquery-dashboard-actionicon"></i>');
					that.$titleeditor.on('click', function() {
						if (!that.$titleinputer) {
							that.$titleinputer = $('<input type="text" class="jquery-dashboard-titleinputer" style="width:100%" value="' + that.config.title + '">');
							that.$titleinputer.on('keypress', function(e) {
								if (e.keyCode == 13) {
									that.config.title = $.trim($(this).val());
									that.$title.empty().text(that.config.title);
									that.dashboard.config.onEditTitle(that);
									that.$titleinputer = null;
								}
							}).on('change blur', function() {
								that.config.title = $.trim($(this).val());
								that.$title.empty().text(that.config.title);
								that.dashboard.config.onEditTitle(that);
								that.$titleinputer = null;
							}).on('mousedown mousemove', function() {
								that._isMoving = false;
							});
							that.$title.empty();
							that.$title.append(that.$titleinputer);
							that.$titleinputer.focus();
						}
					});
					that.$header.append(that.$titleeditor);
				}
			}
			// 删除按钮
			that.$closer = $('<i class="ts-remove jquery-dashboard-closer jquery-dashboard-actionicon"></i>');
			that.$header.append(that.$closer);
			that.$closer.on('click', function(e) {
				e.stopPropagation();
				if (typeof that.dashboard.config.onDelete == 'function') {
					that.dashboard.config.onDelete(that);
				} else {
					that.dashboard.removePortlet(that);
				}

			});

			that.$resizebox = $('<div class="jquery-dashboard-resizebox"></div>');
			that.$container.append(that.$resizebox);
			that.$resizebox.on('mousedown', function(e) {
				that.startResize(e.clientX, e.clientY);
			});
			that.$header.css({
				'cursor' : 'move'
			});
			that.$header.on('mousedown', function(e) {
				if(that.$portlet.hasClass('onfull')){
					return false;
				}
				that.startMove(e.clientX, e.clientY);
				e.stopPropagation();
			});

			that._isResizing = false;
			that._isMoving = false;

			$(window).on('mousemove', function(e) {
				if (that.getIsResizing()) {

					var docHeight = $(document).height();
					var targetHeight = that.dashboard.$target.height();
					var scrollTop = $(window).scrollTop();
					if ((e.clientY + $(window).scrollTop()) > (docHeight - 20)) {
						that.dashboard.$target.height(targetHeight + 5);
						$(window).scrollTop(scrollTop + 5);
					}

					that.doResize(e.clientX, e.clientY);
				}
				if (that.getIsMoving()) {
					that.doMove(e.clientX, e.clientY);
				}
			});
		}

		if (that.dashboard.config.onFullscreen && typeof that.dashboard.config.onFullscreen == 'function' && that.config.type != 'blank') {
			that.$portletfullscreen = $('<i class="ts-fullscreen jquery-dashboard-fullscreen jquery-dashboard-actionicon"></i>');
			that.$header.append(that.$portletfullscreen);
			that.$portletfullscreen.on('click', function() {
				that.dashboard.config.onFullscreen(that);
				if (that.dashboard.config.onResize && typeof that.dashboard.config.onResize == 'function') {
					that.dashboard.config.onResize(that);
				}
			});
		}
		that.setPosition();
	}

	dashboard.portlet.prototype = {
		getId : function() {
			return this.config.id || '';
		},
		getUuid : function() {
			if (!this.config.uuid) {
				var chars = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');
				var num = chars.length;
				var uuid = '';
				for (var i = 0; i < 6; i++) {
					uuid += chars[parseInt(Math.random() * (num - 1 + 1), 10)];
				}
				this.config.uuid = uuid;
			}
			return this.config.uuid;
		},
		getConfig : function() {
			return this.config;
		},
		setConfig : function(config) {
			this.config = config;
		},
		getBody : function() {
			return this.$body;
		},
		setTitle : function(title) {
			var that = this;
			if (that.$title) {
				that.$title.html(title || '');
			}
		},
		getTitle : function() {
			if (this.$title) {
				return this.$title.html();
			} else {
				return "";
			}
		},
		// 绘制阴影
		drawShadow : function(leftX, rightX, topY, bottomY, colSpan, rowSpan) {
			var that = this;
			var blockWidth = this.dashboard.getBlockWidth();
			var blockHeight = this.dashboard.getBlockHeight();
			var startColumn = Math.max(0, Math.round(leftX / blockWidth));
			var endColumn = colSpan ? (startColumn + colSpan) : Math.round(rightX / blockWidth);
			var startRow = Math.max(0, Math.round(topY / blockHeight));
			var endRow = rowSpan ? (startRow + rowSpan) : Math.round(bottomY / blockHeight);
			var rowSpan = endRow - startRow;
			var colSpan = endColumn - startColumn;
			if (endColumn > this.dashboard.config.columnCount) {
				endColumn = this.dashboard.config.columnCount;
				// 移动模式才锁定左边位置
				if (this._isMoving) {
					startColumn = Math.max(0, endColumn - colSpan);
				}
			}
			// 避开所有遮挡block，寻找最大endrow
			for (var i = 0; i < this.dashboard.portletList.length; i++) {
				var p = this.dashboard.portletList[i];
				if (this != p) {
					var pStartColumn = p.config.startcolumn;
					var pEndColumn = p.config.endcolumn;
					var pStartRow = p.config.startrow;
					var pEndRow = p.config.endrow;
					if ((startColumn <= pStartColumn && endColumn > pStartColumn) || (endColumn >= pEndColumn && startColumn < pEndColumn) || (startColumn >= pStartColumn && endColumn <= pEndColumn)) {
						if (startRow >= pStartRow && startRow <= pStartRow + 1) {
							startRow = pStartRow;
							newStartRow3 = pEndRow;
						} else if (startRow > pStartRow + 1) {
							startRow = Math.max(startRow, pEndRow);
						}
					}
				}
			}

			// 重新计算最小endrow，让block自动向上定位
			var maxRow = 0;
			for (var i = 0; i < this.dashboard.portletList.length; i++) {
				var p = this.dashboard.portletList[i];
				if (this != p) {
					var pStartColumn = p.config.startcolumn;
					var pEndColumn = p.config.endcolumn;
					var pStartRow = p.config.startrow;
					var pEndRow = p.config.endrow;
					if ((startColumn <= pStartColumn && endColumn > pStartColumn) || (endColumn >= pEndColumn && startColumn < pEndColumn) || (startColumn >= pStartColumn && endColumn <= pEndColumn)) {
						if (startRow > pStartRow + 1) {
							maxRow = Math.max(maxRow, pEndRow);
						}
					}
				}
			}
			startRow = Math.min(startRow, maxRow);

			endRow = startRow + rowSpan;

			var width = this.dashboard.calculatePortletWidth(startColumn, endColumn);
			var height = this.dashboard.calculatePortletHeight(startRow, endRow);
			var top = this.dashboard.calculatePortletTop(startRow);
			var left = this.dashboard.calculatePortletLeft(startColumn);
			if (!this.dashboard.$shadow) {
				this.dashboard.$shadow = $('<div style="background:' + this.dashboard.config.shadowColor + '" class="jquery-dashboard-shadow"></div>');
				this.dashboard.$target.append(this.dashboard.$shadow);
			}

			this.dashboard.$shadow.css({
				'width' : width + 'px',
				'height' : height + 'px',
				'transform' : 'translate(' + left + 'px, ' + top + 'px)',
				'-ms-transform' : 'translate(' + left + 'px, ' + top + 'px)',
				'-moz-transform' : 'translate(' + left + 'px, ' + top + 'px)',
				'-webkit-transform' : 'translate(' + left + 'px, ' + top + 'px)',
				'-o-transform' : 'translate(' + left + 'px, ' + top + 'px)'
			});
			this.config.startcolumn = startColumn;
			this.config.endcolumn = endColumn;
			this.config.startrow = startRow;
			this.config.endrow = endRow;

			this.dashboard.resetPortletPosition(this);

		},
		setContent : function(content) {
			this.$body.empty();
			this.$body.append(content);
		},
		startMove : function(mouseX, mouseY) {
			this._isMoving = true;
			this._oldTop = this.$portlet.position().top;
			this._oldLeft = this.$portlet.position().left;
			this._startMouseX = mouseX + $(window).scrollLeft();
			this._startMouseY = mouseY + $(window).scrollTop();
			this.$portlet.css('transition', 'none');
			this.$portlet.css('z-index', 3);
			this.$portlet.css('opacity', 0.6);
		},
		doMove : function(mouseX, mouseY) {
			if (this._isMoving) {
				var deltaX = mouseX + $(window).scrollLeft() - this._startMouseX;
				var deltaY = mouseY + $(window).scrollTop() - this._startMouseY;
				this.$portlet.css('transform', 'translate(' + parseInt(this._oldLeft + deltaX) + 'px, ' + parseInt(this._oldTop + deltaY) + 'px)');
				var leftX = this.$portlet.position().left;
				var rightX = leftX + this.$portlet.width();
				var topY = this.$portlet.position().top;
				var bottomY = topY + this.$portlet.height();
				var colSpan = this.config.endcolumn - this.config.startcolumn;
				var rowSpan = this.config.endrow - this.config.startrow;
				this.drawShadow(leftX, rightX, topY, bottomY, colSpan, rowSpan);
			}
		},
		endMove : function() {
			if (this._isMoving) {
				this.setPosition();
				this._isMoving = false;
				if (this.dashboard.$shadow) {
					this.dashboard.$shadow.remove();
					this.dashboard.$shadow = null;
				}
				this._oldTop = null;
				this._oldLeft = null;
				this._startMouseX = null;
				this._startMouseY = null;
				this.$portlet.css('transition', '');
				this.$portlet.css('z-index', '');
				this.$portlet.css('opacity', '');
			}
		},
		startResize : function(mouseX, mouseY) {
			this._isResizing = true;
			this._oldWidth = this.$portlet.width();
			this._oldHeight = this.$portlet.height();
			this._startMouseX = mouseX + $(window).scrollLeft();
			this._startMouseY = mouseY + $(window).scrollTop();
			this.$portlet.css('transition', 'none');
			this.$portlet.css('z-index', 3);
			this.$portlet.css('opacity', 0.6);
		},
		endResize : function() {
			if (this._isResizing) {
				this.setPosition();
				this._isResizing = false;
				if (this.dashboard.$shadow) {
					this.dashboard.$shadow.remove();
					this.dashboard.$shadow = null;
				}
				this._oldWidth = null;
				this._oldHeight = null;
				this._startMouseX = null;
				this._startMouseY = null;
				this.$portlet.css('transition', '');
				this.$portlet.css('z-index', '');
				this.$portlet.css('opacity', '');
			}
		},
		doResize : function(mouseX, mouseY) {
			var that = this;
			if (this._isResizing) {
				var deltaX = mouseX + $(window).scrollLeft() - this._startMouseX;
				var deltaY = mouseY + $(window).scrollTop() - this._startMouseY;

				
				var width = Math.max(this._oldWidth + deltaX, this.dashboard.getBlockWidth() * this.dashboard.config.minBlockColumn);
				var height = Math.max(this._oldHeight + deltaY, this.dashboard.getBlockHeight() * this.dashboard.config.minBlockRow);
				this.$portlet.css({
					'width' : width + 'px',
					'height' : height + 'px'
				});
				var leftX = this.$portlet.position().left;
				var rightX = leftX + this.$portlet.width();
				var topY = this.$portlet.position().top;
				var bottomY = topY + this.$portlet.height();
				this.drawShadow(leftX, rightX, topY, bottomY);
				if (that.dashboard.config.onResize && typeof that.dashboard.config.onResize == 'function') {
					setTimeout(function() {
						that.dashboard.config.onResize(that);
					}, 100);
				}
			}
		},
		getIsResizing : function() {
			return this._isResizing;
		},
		getIsMoving : function() {
			return this._isMoving;
		},
		getPosition : function() {
			var json = {};
			json.startColumn = this.config.startcolumn;
			json.endColumn = this.config.endcolumn;
			json.startRow = this.config.startrow;
			json.endRow = this.config.endrow;
			return json;
		},
		setPosition : function() {
			var that = this;
			that.$portlet.attr('data-sc', that.config.startcolumn);
			that.$portlet.attr('data-sr', that.config.startrow);
			that.$portlet.attr('data-ec', that.config.endcolumn);
			that.$portlet.attr('data-er', that.config.endrow);
			that.$portlet.attr('data-col', that.config.column);
			that.$portlet.attr('data-row', that.config.row);
			var width = that.dashboard.calculatePortletWidth(that.config.startcolumn, that.config.endcolumn);
			var height = that.dashboard.calculatePortletHeight(that.config.startrow, that.config.endrow);
			var top = that.dashboard.calculatePortletTop(that.config.startrow);
			var left = that.dashboard.calculatePortletLeft(that.config.startcolumn);
			that.$portlet.css({
				'width' : width + 'px',
				'height' : height + 'px',
				'transform' : 'translate(' + left + 'px, ' + top + 'px)',
				'-ms-transform' : 'translate(' + left + 'px, ' + top + 'px)',
				'-moz-transform' : 'translate(' + left + 'px, ' + top + 'px)',
				'-webkit-transform' : 'translate(' + left + 'px, ' + top + 'px)',
				'-o-transform' : 'translate(' + left + 'px, ' + top + 'px)'
			});
			that.dashboard.resetDashboardHeight();
			if (that.dashboard.config.onResize && typeof that.dashboard.config.onResize == 'function') {
				setTimeout(function() {
					that.dashboard.config.onResize(that);
				}, 200);
			}
		}
	}

	this.dashboard = dashboard;
	dashboard.prototype = {
		getJson : function() {
			var jsonList = new Array();
			this.portletList.forEach(function(p) {
				var jsonObj = {};
				jsonObj.id = p.getId();
				jsonObj.uuid = p.getUuid();
				jsonObj.startcolumn = p.config.startcolumn;
				jsonObj.endcolumn = p.config.endcolumn;
				jsonObj.startrow = p.config.startrow;
				jsonObj.endrow = p.config.endrow;
				jsonList.push(jsonObj);
			});
			return jsonList;
		},
		calculatePortletTop : function(startRow) {
			return this.getBlockHeight() * startRow;
		},
		calculatePortletLeft : function(startColumn) {
			return this.getBlockWidth() * startColumn;
		},
		calculatePortletWidth : function(startColumn, endColumn) {
			return (endColumn - startColumn) * this.getBlockWidth();
		},
		calculatePortletHeight : function(startRow, endRow) {
			return (endRow - startRow) * this.getBlockHeight();
		},
		resetDashboardHeight : function() {
			var maxRow = 0;
			this.portletList.forEach(function(p) {
				maxRow = Math.max(maxRow, p.config.endrow);
			});
			this.$target.css('height', (maxRow * this.getBlockHeight()) + 'px');
		},
		resetPortletPosition : function(exPortlet) {
			var that = this;
			var portletList = this.getSortedPortletList();
			portletList.forEach(function(p1, idx1) {
				// str += p1.config.title + ',';
				if (typeof exPortlet == 'undefined' || p1 != exPortlet) {
					var newrow = 0;
					var oldrow = p1.config.startrow;
					var rowspan = p1.config.endrow - oldrow;
					var isHitExPortlet = false;
					var index = idx1;
					if (exPortlet) {
						index += 1;
					}
					for (var i = 0; i <= index; i++) {
						var p2 = portletList[i];
						if (p1 == p2) {
							continue;
						}
						if (exPortlet) {
							if (p2 != exPortlet && i > idx1) {
								break;
							} else if (p2 == exPortlet) {
								if (p1.config.startrow + 1 < p2.config.startrow) {
									break;
								}
							}
						}
						if ((p1.config.startcolumn <= p2.config.startcolumn && p1.config.endcolumn > p2.config.startcolumn) || (p1.config.endcolumn >= p2.config.endcolumn && p1.config.startcolumn < p2.config.endcolumn)
								|| (p1.config.startcolumn >= p2.config.startcolumn && p1.config.endcolumn <= p2.config.endcolumn)) {
							newrow = Math.max(newrow, p2.config.endrow);

						}
					}

					if (newrow != oldrow) {
						p1.config.startrow = newrow;
						p1.config.endrow = newrow + rowspan;
						// p1.$portlet.css('transition', 'none');
						p1.setPosition();
						// p1.$portlet.css('transition', '');

					}
				}
			});
			// console.log(str);
		},
		getSortedPortletList : function() {
			var sortedPortList = new Array();
			var minc = 0, minr = 0;
			this.portletList.sort(function(p1, p2) {
				if (p1.config.startrow > p2.config.startrow) {
					return 1;
				} else if (p1.config.startrow < p2.config.startrow) {
					return -1;
				} else {
					if (p1.config.startcolumn > p2.config.startcolumn) {
						return 1;
					} else {
						return -1;
					}
				}
			});
			return this.portletList;
		},
		getPortletList : function() {
			return this.portletList;
		},
		removePortlet : function(portlet) {
			var that = this;
			for (var i = 0; i < that.portletList.length; i++) {
				var p = that.portletList[i];
				if (p == portlet) {
					p.$portlet.remove();
					that.portletList.splice(i, 1);
					that.resetPortletPosition();
					break;
				}
			}
			that.resetBlockposition();
		},
		addOldPortlet : function(jqueryObj) {
			var portlet = new dashboard.portlet(this, null, jqueryObj);
			if (this.config.onReady && typeof this.config.onReady == 'function') {
				this.config.onReady(portlet);
			}
			this.portletList.push(portlet);
			this.resetDashboardHeight();
			return portlet;
		},
		addPortlet : function(json) {
			var portlet = new dashboard.portlet(this, json, null);
			if (this.config.onReady && typeof this.config.onReady == 'function') {
				this.config.onReady(portlet);
			}
			this.portletList.push(portlet);
			this.resetDashboardHeight();
			return portlet;
		},
		fixNumber : function(number) {
			return parseInt(number * 100) / 100;
		},
		getWidth : function() {
			return this._width;
		},
		getHeight : function() {
			return this._height;
		},
		getBlockWidth : function() {
			return parseFloat(this.getWidth() / this.config.columnCount);
		},
		getBlockHeight : function() {
			// return parseInt(this.getHeight() / this.config.rowCount);
			return this.blockHeight || 20;
		},
		resetBlockposition : function() {
			var that = this;
			that._width = that.$target.width();
			that._height = that.$target.height();
			for (var i = 0; i < that.portletList.length; i++) {
				(function(im) {
					that.portletList[im].setPosition();
				})(i);
			}
		}
	};
	$.fn.dashboard = function(options) {
		var $target = $(this);
		var d = null;
		if (!$target.data('dashboard')) {
			d = new dashboard($target, options);
			$target.data('dashboard', d);
		} else {
			d = $target.data('dashboard');
		}
		return d;
	};

})(jQuery);