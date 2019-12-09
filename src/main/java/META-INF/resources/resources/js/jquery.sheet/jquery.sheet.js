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
		},
		cols : 10,// 默认列数
		rows : 10,// 默认行数
		onCellDrop : null
	};

	var SheetBuilder = function(target, options) {
		var that = this;
		that.$target = $(target);
		that.config = $.extend(true, {}, defaultoptions, options);
		that.init();
	}

	this.SheetBuilder = SheetBuilder;

	SheetBuilder.prototype = {
		init : function() {
			var that = this;
			that.cellMap = {};
			that.leftMap = {};
			// that.$target.hide();
			// that.$target.wrap('<div class="sheetbuilder-wraper"></div>');
			that.$target.addClass('sheetbuilder-wraper');
			// that.$wraper = that.$target.closest('.sheetbuilder-wraper');
			that.$window = $(window);

			that.$editor = $('<div class="sheetbuilder-container"></div>');

			that.$editor.bind("cut copy paste", function(e) {
				e.preventDefault();
			});

			// body不接受拖拽
			that.$editor.on('drop', function(e) {
				e.preventDefault();
				e.stopPropagation();
			});

			that.$editor.on('blur', function(e) {
				alert('a');
			});
			
			that.$target.append(that.$editor);
			if (that.config.data) {
				var json = that.config.data;
				if (that.config.readonly) {
					that.drawTableReadonly(json);
				} else {
					that.drawTable(json);
				}
			} else {
				that.drawTable(that.config.rows, that.config.cols);
			}

		},
		destory : function() {
			var that = this;
			that.$editor.remove();
			that.$target.removeClass('sheetbuilder-wraper');
		},
		toJson : function() {
			var that = this;
			var cellList = {};
			var colCount = that.$table.find('.header').length - 1;
			var rowCount = that.$table.find('.lefter').length - 1;
			for (var i = 0; i < that.getCellList().length; i++) {
				var cell = that.getCellList()[i];
				var cellObj = {};
				if (cell.getData()) {
					cellObj['data'] = cell.getData();
				}
				if (cell.getText()) {
					cellObj['text'] = cell.getText();
				}
				if (cell.getCssJson()) {
					cellObj['css'] = cell.getCssJson();
				}
				if (cell.$td.attr('colspan')) {
					cellObj['colspan'] = cell.$td.attr('colspan');
				}
				if (cell.$td.attr('rowspan')) {
					cellObj['rowspan'] = cell.$td.attr('rowspan');
				}
				// if (cellObj['data'] || cellObj['text'] || cellObj['css']) {
				cellList[cell.config.x + ":" + cell.config.y] = cellObj;
				// }

			}
			return {
				'colcount' : colCount,
				'rowcount' : rowCount,
				'cells' : cellList
			};
		},
		addLeft : function(left) {
			this.leftMap[left.config.y] = left;
		},
		getLeft : function(y) {
			return this.leftMap[y];
		},
		addCell : function(td) {
			this.cellMap[td.config.x + ':' + td.config.y] = td;
		},
		getCell : function(x, y) {
			return this.cellMap[x + ":" + y];
		},
		addColumn : function() {
			var that = this;
			var x = 0;
			var lasttd = null;
			if ($('.header', that.$table).length > 0) {
				// x = parseInt($td.prev().data('x'), 10);
				lasttd = $('.header:last', that.$table);
				x = parseInt(lasttd.data('x'), 10);
				x += 1;
			} else {
				lasttd = $('.root', that.$table);
			}
			var td = that.createHeaderTd(x);
			lasttd.after(td);
			that.$table.find('.bodytr').each(function(i, k) {
				var st = new SheetTd({
					x : x,
					y : i
				}, that);
				$(this).append(st.getDom());
			});
		},
		addRow : function() {
			var that = this;
			var y = 0;
			if ($('.lefter', that.$table).length > 1) {
				y = parseInt($('.lefter:last', that.$table).data('y'), 10);
				y += 1;
			}
			var newtr = $('<tr class="maintr bodytr"></tr>');
			var stleft = new SheetLeft({
				y : y
			}, that);
			newtr.append(stleft.getDom());
			$('.header', that.$table).each(function(i, k) {
				if (typeof $(this).data('x') != 'undefined') {
					var st = new SheetTd({
						x : i,
						y : y
					}, that);
					newtr.append(st.getDom());
				}
			});
			$('.lefter:last', that.$table).closest('tr').after(newtr);
		},
		removeCell : function(td) {
			td.$td.remove();
			delete this.cellMap[td.config.x + ':' + td.config.y];
		},
		getCellList : function() {
			var cellList = new Array();
			for ( var td in this.cellMap) {
				cellList.push(this.cellMap[td]);
			}
			return cellList;
		},
		reload : function(jsonObj) {
			var that = this;
			that.$editor.empty();
			that.init(jsonObj);
		},
		getOriginalHtml : function() {
			var that = this;
			return that.$editor.html();
		},
		isEmpty : function() {
			var that = this;
			var isEmpty = true;
			that.$table.find('.bodytd').each(function() {
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
			that.$table.find('.trickController').each(function() {
				if (!$(this).data('prop') || $.isEmptyObject($(this).data('prop'))) {
					isValid = false;
					$(this).addClass('error');
				} else {
					$(this).removeClass('error');
				}
			});
			return isValid;
		},
		createRootTd : function() {
			var that = this;
			var $td = $('<td class="root"></td>');
			return $td;
		},
		createHeaderTd : function(x) {
			var that = this;
			var c = x + 65;
			var $td = $('<td class="header" data-x="' + x + '">' + String.fromCharCode(c) + '</td>');
			return $td;
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
		createTd : function(x, y) {
			var that = this;
			var $td = $('<td class="bodytd" data-x="' + x + '" data-y="' + y + '"></td>');
			that.initTdEvent($td);
			return $td;
		},
		drawTable : function() {
			var rows, cols, config;
			if (arguments.length == 2) {
				rows = arguments[0];
				cols = arguments[1];
			} else if (arguments.length == 1) {
				config = arguments[0];
			}
			var that = this;
			if (that.$table) {
				that.$table.remove();
			}
			that.cellMap = {};
			var cells = null;
			if (config) {
				cells = config.cells;
			}
			// 如果是只读模式，去掉没用的单元格
			if (cells) {
				var minX = Number.MAX_VALUE, minY = Number.MAX_VALUE, maxX = 0, maxY = 0;
				for ( var c in cells) {
					if (cells[c].data || cells[c].text || !that.config.readonly) {
						var x = parseInt(c.split(':')[0], 10);
						var y = parseInt(c.split(':')[1], 10);

						if (minX > x) {
							minX = x;
						}
						if (minY > y) {
							minY = y;
						}
						if (cells[c]['colspan']) {
							if (maxX < x + parseInt(cells[c]['colspan'], 10) - 1) {
								maxX = x + parseInt(cells[c]['colspan'], 10) - 1;
							}
						} else {
							if (maxX < x) {
								maxX = x;
							}
						}
						if (cells[c]['rowspan']) {
							if (maxY < y + parseInt(cells[c]['rowspan'], 10) - 1) {
								maxY = y + parseInt(cells[c]['colspan'], 10) - 1;
							}
						} else {
							if (maxY < y) {
								maxY = y;
							}
						}
					}
				}
				rows = maxY + 1;
				cols = maxX + 1;
				if (that.config.readonly) {
					rows -= minY;
					cols -= minX;
					var newCells = {};
					for ( var c in cells) {
						var x = parseInt(c.split(':')[0], 10);
						var y = parseInt(c.split(':')[1], 10);
						if (x >= minX && y >= minY && x <= maxX && y <= maxY) {
							newCells[(x - minX) + ':' + (y - minY)] = cells[c];
						}
					}
					cells = newCells;
				}
			}
			that.$table = $('<table class="sheetbuilder-maintable"></table>');
			var x = 0, y = 0;
			for (var r = 0; r <= rows; r++) {
				var tr = $('<tr></tr>');
				if (r > 0) {
					tr.addClass('maintr bodytr');
				} else {
					tr.addClass('maintr');
				}
				for (var c = 0; c <= cols; c++) {
					var td = null;
					if (r == 0) {
						if (c == 0) {
							td = that.createRootTd();
							x = 0;
						} else {
							td = that.createHeaderTd(x);
							x += 1;
						}
					} else {
						if (c == 0) {
							var stleft = new SheetLeft({
								y : y
							}, that);
							td = stleft.getDom();
							x = 0;
						} else {
							var tdconfig = {
								x : x,
								y : y
							};
							if (cells) {
								var cd = cells[x + ':' + y];
								if (cd) {
									if (cd['data']) {
										tdconfig['data'] = cd['data'];
									}
									if (cd['css']) {
										tdconfig['css'] = cd['css'];
									}
									if (cd['text']) {
										tdconfig['text'] = cd['text'];
									}
									if (cd['colspan']) {
										tdconfig['colspan'] = cd['colspan'];
									}
									if (cd['rowspan']) {
										tdconfig['rowspan'] = cd['rowspan'];
									}
									var st = new SheetTd(tdconfig, that);
									td = st.getDom();
								}
							} else {
								var st = new SheetTd(tdconfig, that);
								td = st.getDom();
							}
							x += 1;
						}
					}
					tr.append(td);
				}

				if (r > 0) {
					y += 1;
				}
				that.$table.append(tr);
			}
			that.$editor.empty().html(that.$table);
		},
		drawTableReadonly : function(config) {
			var that = this;
			var rows = 0, cols = 0;
			var cells = null;
			if (config) {
				cells = config.cells;
			}
			if (that.$table) {
				that.$table.remove();
			}
			that.cellMap = {};
			// 如果是只读模式，去掉没用的单元格
			if (cells) {
				var minX = Number.MAX_VALUE, minY = Number.MAX_VALUE, maxX = 0, maxY = 0;
				for ( var c in cells) {
					if (cells[c].data || cells[c].text || !that.config.readonly) {
						var x = parseInt(c.split(':')[0], 10);
						var y = parseInt(c.split(':')[1], 10);

						if (minX > x) {
							minX = x;
						}
						if (minY > y) {
							minY = y;
						}
						if (cells[c]['colspan']) {
							if (maxX < x + parseInt(cells[c]['colspan'], 10) - 1) {
								maxX = x + parseInt(cells[c]['colspan'], 10) - 1;
							}
						} else {
							if (maxX < x) {
								maxX = x;
							}
						}
						if (cells[c]['rowspan']) {
							if (maxY < y + parseInt(cells[c]['rowspan'], 10) - 1) {
								maxY = y + parseInt(cells[c]['colspan'], 10) - 1;
							}
						} else {
							if (maxY < y) {
								maxY = y;
							}
						}
					}
				}
				rows = maxY + 1;
				cols = maxX + 1;
				rows -= minY;
				cols -= minX;
				var newCells = {};
				for ( var c in cells) {
					var x = parseInt(c.split(':')[0], 10);
					var y = parseInt(c.split(':')[1], 10);
					if (x >= minX && y >= minY && x <= maxX && y <= maxY) {
						newCells[(x - minX) + ':' + (y - minY)] = cells[c];
					}
				}
				cells = newCells;
			}
			that.$table = $('<table class="sheetbuilder-maintable"></table>');
			for (var r = 0; r < rows; r++) {
				var tr = $('<tr></tr>');
				tr.addClass('maintr bodytr');
				for (var c = 0; c < cols; c++) {
					var td = null;
					var tdconfig = {
						x : c,
						y : r
					};
					var cd = cells[c + ':' + r];
					if (cd) {
						if (cd['data']) {
							tdconfig['data'] = cd['data'];
						}
						if (cd['css']) {
							tdconfig['css'] = cd['css'];
						}
						if (cd['text']) {
							tdconfig['text'] = cd['text'];
						}
						if (cd['colspan']) {
							tdconfig['colspan'] = cd['colspan'];
						}
						if (cd['rowspan']) {
							tdconfig['rowspan'] = cd['rowspan'];
						}
						var st = new SheetTd(tdconfig, that);
						td = st.getDom();
					}

					tr.append(td);
				}
				that.$table.append(tr);
			}
			that.$editor.empty().html(that.$table);
		},
		selectRange : function() {
			var that = this;
			var ftd = that.firsttd;
			var ltd = that.lasttd;
			var minX = 9999, minY = 9999, maxX = 0, maxY = 0;
			if (ftd && ltd) {
				minX = Math.min(minX, ftd.config.x);
				minY = Math.min(minY, ftd.config.y);
				if (ftd.$td.attr('colspan')) {
					maxX = Math.max(maxX, ftd.config.x + parseInt(ftd.$td.attr('colspan'), 10) - 1);
				} else {
					maxX = Math.max(maxX, ftd.config.x);
				}
				if (ftd.$td.attr('rowspan')) {
					maxY = Math.max(maxY, ftd.config.y + parseInt(ftd.$td.attr('rowspan'), 10) - 1);
				} else {
					maxY = Math.max(maxY, ftd.config.y);
				}

				minX = Math.min(minX, ltd.config.x);
				minY = Math.min(minY, ltd.config.y);
				if (ltd.$td.attr('colspan')) {
					maxX = Math.max(maxX, ltd.config.x + parseInt(ltd.$td.attr('colspan'), 10) - 1);
				} else {
					maxX = Math.max(maxX, ltd.config.x);
				}
				if (ltd.$td.attr('rowspan')) {
					maxY = Math.max(maxY, ltd.config.y + parseInt(ltd.$td.attr('rowspan'), 10) - 1);
				} else {
					maxY = Math.max(maxY, ltd.config.y);
				}
			}
			for (var i = 0; i < that.getCellList().length; i++) {
				var cell = that.getCellList()[i];
				cell.$td.removeClass
				cell.isselected = false;
				if (ftd && ltd && cell.config.x >= minX && cell.config.y >= minY && cell.config.x <= maxX && cell.config.y <= maxY) {
					cell.isselected = true;
					if (cell.config.x == minX) {
						cell.$td.addClass('selected-left');
					} else {
						cell.$td.removeClass('selected-left');
					}
					if (cell.config.y == minY) {
						cell.$td.addClass('selected-top');
					} else {
						cell.$td.removeClass('selected-top');
					}
					if (cell.config.x == maxX || (cell.config.x + parseInt(cell.$td.attr('colspan'), 10) - 1) == maxX) {
						cell.$td.addClass('selected-right');
					} else {
						cell.$td.removeClass('selected-right');
					}
					if (cell.config.y == maxY || (cell.config.y + parseInt(cell.$td.attr('rowspan'), 10) - 1) == maxY) {
						cell.$td.addClass('selected-bottom');
					} else {
						cell.$td.removeClass('selected-bottom');
					}
				}
				if (cell.isselected) {
					cell.$td.addClass('selected');
				} else {
					cell.$td.removeClass('selected').removeClass('selected-top').removeClass('selected-left').removeClass('selected-right').removeClass('selected-bottom');
				}
			}
		},
		mergeRange : function() {
			var that = this;
			that.separateRange();
			var ftd = that.firsttd;
			var ltd = that.lasttd;
			if (ftd && ltd && ftd != ltd) {
				var minX = 9999, minY = 9999, maxX = 0, maxY = 0;
				minX = Math.min(minX, ftd.config.x);
				minY = Math.min(minY, ftd.config.y);
				maxX = Math.max(maxX, ftd.config.x);
				maxY = Math.max(maxY, ftd.config.y);

				minX = Math.min(minX, ltd.config.x);
				minY = Math.min(minY, ltd.config.y);
				maxX = Math.max(maxX, ltd.config.x);
				maxY = Math.max(maxY, ltd.config.y);

				var start, end;
				for (var i = 0; i < that.getCellList().length; i++) {
					var cell = that.getCellList()[i];
					if (minX == cell.config.x && minY == cell.config.y) {
						start = cell;
					}
					if (start) {
						break;
					}
				}
				if (start) {
					var colspan = maxX - minX + 1;
					var rowspan = maxY - minY + 1;
					if (colspan > 0) {
						start.$td.attr('colspan', colspan);
					}
					if (rowspan > 0) {
						start.$td.attr('rowspan', rowspan);
					}
					var delList = new Array();
					for (var i = 0; i < that.getCellList().length; i++) {
						var cell = that.getCellList()[i];
						if ((cell.config.x >= minX && cell.config.y >= minY) && (cell.config.x <= maxX && cell.config.y <= maxY)) {
							if (cell.config.x != minX || cell.config.y != minY) {
								delList.push(cell);
							}
						}
					}
					for (var i = 0; i < delList.length; i++) {
						that.removeCell(delList[i]);
					}
					that.firsttd = start;
					that.lasttd = start;
					that.selectRange();
				}
			}
		},
		separateRange : function() {
			var that = this;
			var hasSeparated = false;
			for (var i = 0; i < that.getCellList().length; i++) {
				var cell = that.getCellList()[i];
				if (cell.isselected && (cell.$td.attr('colspan') || cell.$td.attr('rowspan'))) {
					hasSeparated = true;
					var currentX = cell.config.x;
					var currentY = cell.config.y;
					var trIndex = cell.$td.closest('tr').index();
					var tdIndex = cell.$td.index();
					var rowspan = parseInt(cell.$td.attr('rowspan'), 10) || 1;
					var colspan = parseInt(cell.$td.attr('colspan'), 10) || 1;
					for (var r = 0; r < rowspan; r++) {
						var currentTr = that.$table.find('.maintr').eq(trIndex + r);
						// 如果行存在
						if (currentTr.length > 0) {
							var baseTd = currentTr.children('td:eq(0)');
							var baseX = currentX;
							for (var c = 0; c < colspan; c++) {
								// 查找前一个td
								currentTr.children('td').each(function() {
									if (typeof $(this).data('x') != 'undefined') {
										if (parseInt($(this).data('x'), 10) < (currentX + c)) {
											baseTd = $(this);
										} else {
											return false;
										}
									}
								});

								if (!that.getCell(currentX + c, currentY + r)) {
									var newst = new SheetTd({
										x : currentX + c,
										y : currentY + r
									}, that);
									baseTd.after(newst.getDom());
								}
							}
						}

					}
					cell.$td.removeAttr('colspan').removeAttr('rowspan');
				}
			}
			if (hasSeparated) {
				that.selectRange();
			}
		}
	};

	var SheetLeft = function(options, builder) {
		var that = this;
		that.config = options;
		that.builder = builder;
		that.$td = $('<td class="lefter" data-y="' + (that.config.y) + '">' + (that.config.y + 1) + '</td>');
		builder.addLeft(that);
	};
	this.SheetLeft = SheetLeft;
	SheetLeft.prototype = {
		getDom : function() {
			return this.$td;
		}
	};

	var SheetTd = function(options, builder, isReadonly) {
		var that = this;
		that.config = options;
		that.builder = builder;
		that.init();
		builder.addCell(that);
	};
	this.SheetTd = SheetTd;
	SheetTd.prototype = {
		init : function() {
			var that = this;
			that.isempty = true;
			that.isselected = false;
			that.$td = $('<td class="bodytd" data-x="' + that.config.x + '" data-y="' + that.config.y + '"></td>');
			if (that.config.css) {
				that.$td.css(that.config.css);
			}

			if (that.config.text) {
				that.setText(that.config.text);
			}

			if (that.config.data) {
				that.data = that.config.data;
				if (that.builder.config.onCellRender && typeof that.builder.config.onCellRender == 'function') {
					that.builder.config.onCellRender(that);
				}
			}

			if (that.config.colspan) {
				that.$td.attr('colspan', that.config.colspan);
			}

			if (that.config.rowspan) {
				that.$td.attr('rowspan', that.config.rowspan);
			}

			/** 必须加这个，否则drop事件不生效* */
			if (!that.builder.config.readonly) {
				that.$td.on('dragover', function(event) {
					event.preventDefault();
					event.stopPropagation();
				});

				that.$td.on('drop', function(event) {
					var td = $(this);
					event.preventDefault();
					event.stopPropagation();
					try {
						if (that.getBuilder().config.onCellDrop) {
							that.getBuilder().config.onCellDrop(that, event);
						}
					} catch (ex) {
						console.log(ex);
					}
					td.removeClass('enter');
				});

				that.$td.on('dragenter', function(event) {
					$(this).addClass('enter');
				});

				that.$td.on('dragleave', function(e) {
					e.preventDefault();
					e.stopPropagation();
					$(this).removeClass('enter');
				});

				that.$td.on('mousedown', function(e) {
					if (!that.isTexting) {
						var firstTd = that;
						if (!e.shiftKey) {
							that.builder.firsttd = that;
							that.builder.lasttd = that;
							that.builder.isRanging = true;
						} else {
							if (!that.isselected) {
								that.builder.isRanging = true;
								that.builder.lasttd = that;
								if (!that.builder.firsttd) {
									that.builder.firsttd = that;
								}
							} else {
								that.builder.lasttd = that;
							}
						}
						that.builder.selectRange();
					}
				});

				that.$td.on('mouseup', function() {
					that.builder.isRanging = false;
				});

				that.$td.on('mouseenter', function(e) {
					if (that.builder.isRanging) {
						that.builder.lasttd = that;
						that.builder.selectRange();
					} else {
						if (!that.isEmpty() || that.getText()) {
							var clearBtn = $('<div class="tdbtn" style="top:-8px;right:-8px"><i class="ts-remove"></i><div>');
							clearBtn.on('click', function() {
								that.empty();
								$(this).remove();
							});
							var editBtn = $('<div class="tdbtn" style="top:-8px;right:13px"><i class="ts-edit"></i><div>');
							editBtn.on('click', function(e) {
								e.stopPropagation();
								var cssjson = that.getCssJson();
								var html = xdoT.render('balantflow.sheetbuilder.tdprop', cssjson || {});
								createSlideDialog({
									width : 700,
									title : '编辑单元格',
									content : html,
									successFuc : function() {
										if ($('#formProp').valid()) {
											var fd = $('#formProp').serializeArray();
											var css = {}
											for (var i = 0; i < fd.length; i++) {
												css[fd[i].name] = fd[i].value;
											}
											that.$td.css(css);
											return true;
										} else {
											return false;
										}
									}
								});
							});
							$(this).append(editBtn).append(clearBtn);
						}
					}
				});

				that.$td.on('mouseleave', function(e) {
					$(this).find('.tdbtn').remove();
					$(this).removeClass('enter');
				});

				that.$td.on('dblclick', function(e) {
					if (that.isEmpty()) {
						that.setEditMode(true);
					}
				});
			}
		},
		setData : function(data) {
			this.data = data;
		},
		getData : function() {
			if (this.getDataFuc && typeof this.getDataFuc == 'function') {
				this.getDataFuc(this);
			}
			return this.data;
		},
		getCssJson : function() {
			var css;
			var target = this.$td;
			if (target.attr('style')) {
				css = {};
				var style = target.attr('style');
				var styles = style.split(';');
				for (var i = 0; i < styles.length; i++) {
					var s = styles[i];
					var key = $.trim(s.split(':')[0]);
					var value = $.trim(s.split(':')[1]);
					if (key) {
						css[key] = value;
					}
				}
			}
			return css;
		},
		select : function() {
			var that = this;
			if (!this.isselected) {
				// this.$td.addClass('selected');
				this.isselected = true;

			}
		},
		empty : function() {
			var that = this;
			that.$td.empty();
			that.isempty = true;
			that.text = null;
			that.data = null;
			that.getDataFuc = null;
		},
		setContent : function(content) {
			this.$td.empty().html(content);
			this.isempty = false;
			this.text = null;
		},
		getDom : function() {
			var that = this;
			return that.$td;
		},
		getBuilder : function() {
			return this.builder;
		},
		isEmpty : function() {
			return this.isempty;
		},
		setUserData : function(userData) {
			this.userData = userData;
		},
		getUserData : function() {
			return this.userData;
		},
		setEditMode : function(bool) {
			var that = this;
			if (bool) {
				that.isTexting = true;
				that.builder.isRanging = false;
				that.$td.empty();
				var input = $('<input type="text" class="sheetbuilder-inputer" autocomplete="off" style="width:100%">');
				input.val(that.getText());
				input.on('blur', function() {
					that.setText($(this).val());
					that.isTexting = false;
				});
				that.$td.append(input);
				input.trigger('focus');
			} else {
			}
		},
		setText : function(text) {
			this.text = text;
			this.$td.empty().text(this.text);
		},
		getText : function() {
			return this.text || '';
		}
	};

	$.fn.SheetBuilder = function(options) {
		var $target = $(this);
		var fb = null;
		if ($target[0].tagName.toLowerCase() == 'div') {
			if (!$target.attr('bind-sheetbuilder')) {
				fb = new SheetBuilder($target, options);
				$target.attr('bind-sheetbuilder', true);
			}
		}
		return fb;
	};

})(jQuery);