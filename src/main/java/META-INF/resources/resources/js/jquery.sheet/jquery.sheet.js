;
(function($) {
	var defaultoptions = {
		sheets : [ {
			title : '基础表格',
			cols : 5,
			rows : 5
		} ],
		readonly : false,
		onCellDrop : null
	};

	var SheetBuilder = function(target, options) {
		var that = this;
		that.$target = $(target);
		that.$target.addClass('sheetbuilder-wraper');
		that.config = $.extend(true, {}, defaultoptions, options);
		that.sheetList = new Array();
		that.init();
	};

	this.SheetBuilder = SheetBuilder;

	SheetBuilder.prototype = {
		init : function() {
			var that = this;
			$(window).on('resize', function() {
				that.$target.height($(window).height() - that.$target.offset().top);
			});
			that.fromJson();
		},
		toJson : function() {
			var that = this;
			var returnList = new Array();
			var returnObj = {};
			for (var i = 0; i < that.sheetList.length; i++) {
				var sheet = that.sheetList[i];
				var trList = new Array();
				$('.sheetbuilder-maintable-tr', sheet.getDom()).each(function(trindex, tr) {
					var tdList = new Array();
					var trObj = {};
					$('.sheetbuilder-maintable-td', $(this)).each(function(tdindex, td) {
						var cell = $(this).data('cell');
						var cellObj = {};
						if (cell.getData()) {
							cellObj['data'] = cell.getData();
						}
						if (cell.getText()) {
							cellObj['text'] = cell.getText();
						}
						if (cell.getCss()) {
							cellObj['css'] = cell.getCss();
						}
						if (cell.getDom().attr('colspan')) {
							cellObj['colspan'] = cell.getDom().attr('colspan');
						}
						if (cell.getDom().attr('rowspan')) {
							cellObj['rowspan'] = cell.getDom().attr('rowspan');
						}
						// if (!$.isEmptyObject(cellObj)) {
						cellObj['index'] = tdindex;
						tdList.push(cellObj);
						// }
					});
					if (tdList.length > 0) {
						trObj['cols'] = tdList;
					}
					if ($(this).attr('style')) {
						trObj['height'] = $(this).height();
					}
					// if (!$.isEmptyObject(trObj)) {
					trObj['index'] = trindex;
					trList.push(trObj);
					// }
				});

				var thList = new Array();
				$('.sheetbuilder-maintable-th', sheet.getDom()).each(function(thindex, th) {
					var thObj = {};
					if ($(this).attr('style')) {
						thObj['width'] = $(this).width();
					}
					if (!$.isEmptyObject(thObj)) {
						thObj['index'] = thindex;
						thList.push(thObj);
					}
				});

				var sheetObj = {
					'cols' : sheet.getColCount(),
					'rows' : sheet.getRowCount(),
					'width' : sheet.getWidth()
				};
				if (thList.length > 0) {
					sheetObj['head'] = thList;
				}
				if (trList.length > 0) {
					sheetObj['body'] = trList;
				}
				returnList.push(sheetObj);
			}
			returnObj['sheets'] = returnList;
			return returnObj;
		},
		fromJson : function(config) {
			var that = this;
			if (config) {
				that.config = config;
			}
			if (that.sheetList && that.sheetList.length > 0) {
				for (var i = 0; i < that.sheetList.length; i++) {
					that.sheetList[i].destory();
				}
			}
			that.sheetList = new Array();
			if (that.config.sheets && that.config.sheets.length > 0) {
				for (var i = 0; i < that.config.sheets.length; i++) {
					var sheet = new Sheet(that, that.config.sheets[i]);
					that.sheetList.push(sheet);
				}
				// 将第一个sheet设为可视
				that.sheetList[0].active();
			}
		}
	};

	var Sheet = function(builder, options) {
		var that = this;
		that.builder = builder;
		that.config = options;
		if (!that.builder.config.readonly) {
			that.init();
		} else {
			that.initreadonly();
		}
	};

	this.Sheet = Sheet;

	Sheet.prototype = {
		init : function() {
			var that = this;
			that.$sheet = $('<div class="sheetbuilder-sheet"></div>');
			that.$leftcontainer = $('<div class="sheetbuilder-sheet-leftcontainer"></div>');
			that.$topcontainer = $('<div class="sheetbuilder-sheet-topcontainer"></div>');
			that.$rootcontainer = $('<div class="sheetbuilder-sheet-rootcontainer"></div>');
			that.$maincontainer = $('<div class="sheetbuilder-sheet-maincontainer"></div>')
			that.$sheet.append(that.$leftcontainer).append(that.$topcontainer).append(that.$rootcontainer).append(that.$maincontainer);
			that.builder.$target.append(that.$sheet);
			that.drawLeft();
			that.drawTop();
			that.drawRoot();
			that.drawTable();

			that.$sheet.on('click', function() {
				if (that.contextmenu && !that.contextmenu.isHover) {
					that.contextmenu.destory();
				}
			});

			that.$sheet.on('wheel', function(e) {
				var deltaX = e.originalEvent.wheelDeltaX;
				var deltaY = e.originalEvent.wheelDeltaY;
				// 向左右偏移
				if (deltaX < 0 || deltaX > 0) {
					var scrollLeft = that.$maincontainer.scrollLeft();
					that.$maincontainer.scrollLeft(scrollLeft - deltaX);
					that.$topcontainer.scrollLeft(scrollLeft - deltaX);
				}
				// 向上下偏移
				if (deltaY < 0 || deltaY > 0) {
					var scrollTop = that.$maincontainer.scrollTop();
					that.$maincontainer.scrollTop(scrollTop - deltaY);
					that.$leftcontainer.scrollTop(scrollTop - deltaY);
				}

			});
		},
		initreadonly : function() {
			var that = this;
			that.$sheet = $('<div class="sheetbuilder-sheet"></div>');
			that.$maincontainer = $('<div class="sheetbuilder-sheet-maincontainer"></div>')
			that.$sheet.append(that.$maincontainer);
			that.builder.$target.append(that.$sheet);

			/*
			 * // 去掉没用的行 var newRowList = new Array(); for (var i = 0; i <
			 * that.config.body.length; i++) { var row = that.config.body[i];
			 * var keep = false; if (row['cols'] && row['cols'].length > 0) {
			 * for (var j = 0; j < row['cols'].length; j++) { var col =
			 * row['cols'][j]; if (col['css'] || col['data'] || col['text']) {
			 * keep = true; break; } } } if (keep) { newRowList.push(row); } }
			 * //去掉没用的列
			 * 
			 * that.config.body = newRowList;
			 */

			that.drawTable();
		},
		destory : function() {
			this.getDom().remove();
		},
		getWidth : function() {
			return this.getTopTableDom().width();
		},
		getDom : function() {
			return this.$sheet;
		},
		getTableDom : function() {
			return this.$table;
		},
		getTopTableDom : function() {
			return this.$toptable;
		},
		getLeftTableDom : function() {
			return this.$lefttable;
		},
		getColCount : function() {
			return this.getTableDom().find('.sheetbuilder-maintable-th').length;
		},
		getRowCount : function() {
			return this.getTableDom().find('.sheetbuilder-maintable-tr').length;
		},
		active : function() {

		},
		drawRoot : function() {
			var that = this;
			var $roottable = $('<table class="sheetbuilder-roottable"></table>');
			if (that.config.rows && that.config.rows > 0 && that.config.cols && that.config.cols > 0) {
				var tr = $('<tr></tr>');
				var td = $('<td class="sheetbuilder-roottable-td"></td>');
				tr.append(td);
				$roottable.append(tr);
			}
			that.$rootcontainer.append($roottable);
		},
		removeLeft : function(index) {
			var that = this;
			if (!isNaN(parseInt(index, 10))) {
				that.getLeftTableDom().find('tr:eq(' + index + ')').remove();
				that.getLeftTableDom().find('.sheetbuilder-lefttable-td').each(function(i, k) {
					$(this).children('span').text(i + 1);
				});
			}
		},
		appendLeft : function(index) {
			var that = this;
			var tr = $('<tr></tr>');
			var cell = new LeftCell(that);
			tr.append(cell.getDom());
			if (!isNaN(parseInt(index, 10))) {
				var targettr = that.getLeftTableDom().find('tr:eq(' + index + ')');
				if (targettr.length > 0) {
					targettr.after(tr);
				} else {
					that.getLeftTableDom().find('tbody').append(tr);
				}
			} else {
				that.getLeftTableDom().find('tbody').append(tr);
			}
			that.getLeftTableDom().find('.sheetbuilder-lefttable-td').each(function(i, k) {
				$(this).children('span').text(i + 1);
			});
		},
		prependLeft : function(index) {
			var that = this;
			var tr = $('<tr></tr>');
			var cell = new LeftCell(that);
			tr.append(cell.getDom());
			if (!isNaN(parseInt(index, 10))) {
				var targettr = that.getLeftTableDom().find('tr:eq(' + index + ')');
				if (targettr.length > 0) {
					targettr.before(tr);
				} else {
					that.getLeftTableDom().find('tbody').append(tr);
				}
			} else {
				that.getLeftTableDom().find('tbody').append(tr);
			}
			that.getLeftTableDom().find('.sheetbuilder-lefttable-td').each(function(i, k) {
				$(this).children('span').text(i + 1);
			});
		},
		drawLeft : function() {
			var that = this;
			that.$lefttable = $('<table class="sheetbuilder-lefttable"></table>');
			if (that.config.rows && that.config.rows > 0) {
				for (var i = 0; i < that.config.rows; i++) {
					var tr = $('<tr></tr>');
					var leftCell = new LeftCell(that, i);
					tr.append(leftCell.getDom());
					that.getLeftTableDom().append(tr);
				}
			}
			that.$leftcontainer.append(that.$lefttable);
		},
		appendTop : function(index) {
			var that = this;
			var topCell = new TopCell(that);
			if (!isNaN(parseInt(index, 10))) {
				var td = that.getTopTableDom().find('.sheetbuilder-toptable-td:eq(' + index + ')');
				if (td.length > 0) {
					td.after(topCell.getDom());
				} else {
					that.getTopTableDom().find('tr').append(topCell.getDom());
				}
			} else {
				that.getTopTableDom().find('tr').append(topCell.getDom());
			}
			that.getTopTableDom().find('.sheetbuilder-toptable-td').each(function(i, k) {
				$(this).children('span').text(String.fromCharCode(i + 65));
			});
		},
		prependTop : function(index) {
			var that = this;
			var topCell = new TopCell(that);
			if (!isNaN(parseInt(index, 10))) {
				var td = that.getTopTableDom().find('.sheetbuilder-toptable-td:eq(' + index + ')');
				if (td.length > 0) {
					td.before(topCell.getDom());
				} else {
					that.getTopTableDom().find('tr').append(topCell.getDom());
				}
			} else {
				that.getTopTableDom().find('tr').append(topCell.getDom());
			}
			that.getTopTableDom().find('.sheetbuilder-toptable-td').each(function(i, k) {
				$(this).children('span').text(String.fromCharCode(i + 65));
			});
		},
		removeTop : function(index) {
			var that = this;
			if (!isNaN(parseInt(index, 10))) {
				that.getTopTableDom().find('td:eq(' + index + ')').remove();
				that.getTopTableDom().find('.sheetbuilder-toptable-td').each(function(i, k) {
					$(this).children('span').text(String.fromCharCode(i + 65));
				});
			}
		},
		drawTop : function() {
			var that = this;
			that.$toptable = $('<table class="sheetbuilder-toptable"></table>');
			if (that.config.cols && that.config.cols > 0) {
				var tr = $('<tr></tr>');
				for (var i = 0; i < that.config.cols; i++) {
					var conf = {};
					if (that.config.head && that.config.head.length > 0) {
						for (var h = 0; h < that.config.head.length; h++) {
							if (that.config.head[h].index == i) {
								conf['width'] = that.config.head[h].width;
							}
						}
					}
					var topCell = new TopCell(that, i, conf);
					tr.append(topCell.getDom());
				}
				that.$toptable.append(tr);
			}
			that.$topcontainer.append(that.$toptable);
		},
		drawTable : function() {
			var that = this;
			that.$table = $('<table class="sheetbuilder-maintable"></table>');
			if (that.getTopTableDom()) {
				that.$table.width(that.getTopTableDom().width());
			} else {
				that.$table.width(that.config.width);
			}
			if (that.builder.config.readonly) {
				that.$table.addClass('readonly')
			}
			if (that.config.cols && that.config.cols > 0) {
				var thead = $('<thead></thead>');
				var tr = $('<tr style="height: 0px;"></tr>');
				for (var j = 0; j < that.config.cols; j++) {
					var $th = $('<th class="sheetbuilder-maintable-th"></th>');
					if (that.config.head && that.config.head.length > 0) {
						for (var h = 0; h < that.config.head.length; h++) {
							if (that.config.head[h].index == j && that.config.head[h].width) {
								$th.css({
									'min-width' : that.config.head[h].width,
									'width' : that.config.head[h].width
								});
							}
						}
					}
					tr.append($th);
				}
				thead.append(tr);
				that.$table.append(thead);
			}
			if (that.config.rows && that.config.rows > 0 && that.config.cols && that.config.cols > 0) {
				var tbody = $('<tbody></tbody>');
				if (!that.config.body) {
					for (var i = 0; i < that.config.rows; i++) {
						var tr = $('<tr class="sheetbuilder-maintable-tr"></tr>');
						for (var j = 0; j < that.config.cols; j++) {
							var cell = new Cell(that);
							tr.append(cell.getDom());
						}
						tbody.append(tr);
					}
				} else {
					var trconf = {};
					for (var b = 0; b < that.config.body.length; b++) {
						trconf = that.config.body[b];
						var tr = $('<tr class="sheetbuilder-maintable-tr"></tr>');
						if (trconf && trconf.height) {
							tr.css({
								'height' : trconf.height + 'px'
							});
						}
						var tdconf = {};
						if (trconf.cols && trconf.cols.length > 0) {
							for (var c = 0; c < trconf.cols.length; c++) {
								tdconf = trconf.cols[c];
								var cell = new Cell(that, tdconf);
								tr.append(cell.getDom());
							}
						}
						tbody.append(tr);
					}
				}
				that.$table.append(tbody);
			}
			that.$maincontainer.append(that.$table);
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
		getCellList : function() {
			var that = this;
			var cellList = new Array();
			that.getTableDom().find('.sheetbuilder-maintable-td').each(function() {
				cellList.push($(this).data('cell'));
			});
			return cellList;
		},
		getTopCellList : function() {
			var that = this;
			var topCellList = new Array();
			that.getTopTableDom().find('td.sheetbuilder-toptable-td').each(function() {
				topCellList.push($(this).data('cell'));
			});
			return topCellList;
		},
		getLeftCellList : function() {
			var that = this;
			var leftCellList = new Array();
			that.getLeftTableDom().find('td').each(function() {
				leftCellList.push($(this).data('cell'));
			});
			return leftCellList;
		},
		removeCell : function(cell) {
			cell.getDom().remove();
		},
		removeRow : function() {

		},
		removeCol : function() {

		},
		getSelectedCellList : function() {
			var that = this;
			var selectedCellList = new Array();
			that.getTableDom().find('.sheetbuilder-maintable-td.selected').each(function() {
				selectedCellList.push($(this).data('cell'));
			});
			return selectedCellList;
		},
		selectRange : function() {
			var that = this;
			var ftd = that.firsttd;
			var ltd = that.lasttd;
			var minX = Number.MAX_VALUE, minY = Number.MAX_VALUE, maxX = 0, maxY = 0;
			if (ftd && ltd) {
				minX = Math.min(minX, ftd.getX());
				minY = Math.min(minY, ftd.getY());
				maxX = Math.max(maxX, ftd.getX() + ftd.getDom().width());
				maxY = Math.max(maxY, ftd.getY() + ftd.getDom().height());

				minX = Math.min(minX, ltd.getX());
				minY = Math.min(minY, ltd.getY());
				maxX = Math.max(maxX, ltd.getX() + ltd.getDom().width());
				maxY = Math.max(maxY, ltd.getY() + ltd.getDom().height());

				// 重新修正最小和最大坐标
				var cellList = that.getCellList();
				ADJUST: while (true) {
					for (var i = 0; i < cellList.length; i++) {
						var cell = cellList[i];
						var needAdjust = false;
						if ((cell.getX() + cell.getDom().width()) >= minX && (cell.getY() + cell.getDom().height()) >= minY && cell.getX() <= maxX && cell.getY() <= maxY) {
							if (cell.getX() < minX) {
								minX = cell.getX();
								that.firsttd = cell;
								needAdjust = true;
							}
							if (cell.getY() < minY) {
								minY = cell.getY();
								that.firsttd = cell;
								needAdjust = true;
							}
							if (cell.getX() + cell.getDom().width() > maxX) {
								maxX = cell.getX() + cell.getDom().width();
								that.lasttd = cell;
								needAdjust = true;
							}
							if (cell.getY() + cell.getDom().height() > maxY) {
								maxY = cell.getY() + cell.getDom().height();
								that.lasttd = cell;
								needAdjust = true;
							}
							if (needAdjust) {
								continue ADJUST;
							}
						}
					}
					break;
				}
				for (var i = 0; i < cellList.length; i++) {
					var cell = cellList[i];
					cell.isselected = false;
					if (cell.getX() >= minX && cell.getY() >= minY && cell.getX() <= maxX && cell.getY() <= maxY) {
						cell.isselected = true;
					}
					if (cell.isselected) {
						cell.getDom().addClass('selected');
					} else {
						cell.getDom().removeClass('selected');
					}
				}
			} else {
				that.getTableDom().find('.selected').removeClass('selected');
			}
		},
		mergeRange : function() {
			var that = this;
			that.separateRange();
			var ftd = that.firsttd;
			var ltd = that.lasttd;
			if (ftd && ltd && ftd != ltd) {
				var minX = Number.MAX_VALUE, minY = Number.MAX_VALUE, maxX = 0, maxY = 0;
				minX = Math.min(minX, ftd.getX());
				minY = Math.min(minY, ftd.getY());
				maxX = Math.max(maxX, ftd.getX() + ftd.getDom().width());
				maxY = Math.max(maxY, ftd.getY() + ftd.getDom().height());

				minX = Math.min(minX, ltd.getX());
				minY = Math.min(minY, ltd.getY());
				maxX = Math.max(maxX, ltd.getX() + ltd.getDom().width());
				maxY = Math.max(maxY, ltd.getY() + ltd.getDom().height());

				var start, end;
				var cellList = that.getCellList();
				for (var i = 0; i < cellList.length; i++) {
					var cell = cellList[i];
					if (minX == cell.getX() && minY == cell.getY()) {
						start = cell;
					}
					if (start) {
						break;
					}
				}
				if (start) {
					var colspan = 1;
					var rowspan = 1;
					var delList = new Array();
					for (var i = 0; i < cellList.length; i++) {
						var cell = cellList[i];
						if ((cell.getX() >= minX && cell.getY() >= minY) && (cell.getX() <= maxX && cell.getY() <= maxY)) {
							if (start != cell) {
								if (start.getY() == cell.getY()) {
									colspan += cell.getColspan();
								}
								if (start.getX() == cell.getX()) {
									rowspan += cell.getRowspan();
								}
								delList.push(cell);
							}
						}
					}
					for (var i = 0; i < delList.length; i++) {
						that.removeCell(delList[i]);
					}

					if (colspan > 1) {
						start.getDom().attr('colspan', colspan);
					}
					if (rowspan > 1) {
						start.getDom().attr('rowspan', rowspan);
					}

					that.firsttd = start;
					that.lasttd = start;
					that.selectRange();
				}
			}
		},
		separateRange : function() {
			var that = this;
			var separateCellList = new Array();
			var minX = Number.MAX_VALUE, minY = Number.MAX_VALUE, maxX = 0, maxY = 0;
			var selectedCellList = that.getSelectedCellList();
			for (var i = 0; i < selectedCellList.length; i++) {
				var cell = selectedCellList[i];
				minX = Math.min(minX, cell.getX());
				minY = Math.min(minY, cell.getY());
				maxX = Math.max(maxX, cell.getX() + cell.getDom().width());
				maxY = Math.max(maxY, cell.getY() + cell.getDom().height());
				if (cell.getColspan() > 1 || cell.getRowspan() > 1) {
					separateCellList.push(cell);
				}
			}

			if (separateCellList.length > 0) {
				for (var i = 0; i < separateCellList.length; i++) {
					var cell = separateCellList[i];
					var colspan = cell.getColspan();
					var rowspan = cell.getRowspan();
					cell.getDom().removeAttr('colspan').removeAttr('rowspan');
					for (var c = 0; c < colspan - 1; c++) {
						var newCell = new Cell(that);
						cell.getDom().closest('tr').append(newCell.getDom());
					}
					var currentTr = cell.getDom().closest('tr');
					for (var r = 0; r < rowspan - 1; r++) {
						var nextTr = currentTr.next();
						for (var c = 0; c < colspan; c++) {
							var newCell = new Cell(that);
							nextTr.append(newCell.getDom());
						}
						currentTr = nextTr;
					}
				}
				that.firsttd = null;
				that.lasttd = null;

				var start = null, end = null;
				var cellList = that.getCellList();
				for (var i = 0; i < cellList.length; i++) {
					var cell = cellList[i];
					if ((cell.getX() >= minX && cell.getY() >= minY) && (cell.getX() <= maxX && cell.getY() <= maxY)) {
						if (!start || start.getX() > cell.getX() || start.getY() > cell.getY()) {
							start = cell;
						}
						if (!end || end.getX() < cell.getX() || end.getY() < cell.getY()) {
							end = cell;
						}
					}
				}
				that.firsttd = start;
				that.lasttd = end;
				that.selectRange();
			}
		}
	};

	// 上方单元格
	var TopCell = function(sheet, index, config) {
		var that = this;
		that.sheet = sheet;
		that.index = index;
		that.isResizing = false;
		that.config = config;
		that.init();
	};
	this.TopCell = TopCell;
	TopCell.prototype = {
		init : function() {
			var that = this;
			if (typeof that.index == 'undefined') {
				that.index = that.sheet.getTopCellList().length;
			}
			that.$td = $('<td class="sheetbuilder-toptable-td"><span>' + String.fromCharCode(that.index + 65) + '</span></td>');
			if (that.config && that.config.width) {
				that.$td.css({
					'min-width' : that.config.width,
					'width' : that.config.width
				});
			}
			that.$td.on('contextmenu', function(e) {
				if (that.sheet.contextmenu) {
					that.sheet.contextmenu.destory();
				}
				var contextmenu = new ContextMenu(that.sheet, e.pageX, e.pageY);
				that.sheet.contextmenu = contextmenu;
				return false;
			});

			var $resizer = $('<div class="sheetbuilder-top-resizer"></div>');
			$resizer.on('mousedown', function(event) {
				that.isResizing = true;
				var $resizehandler_left = $('<div class="sheetbuilder-topresize-handler"></div>');
				var $resizehandler_right = $('<div class="sheetbuilder-topresize-handler"></div>');
				$resizehandler_right.css({
					top : '0px',
					left : (that.getDom().offset().left - that.sheet.getDom().offset().left + that.getDom().outerWidth()) + 'px',
					height : (that.sheet.getTableDom().height() + that.sheet.getTopTableDom().height()) + 'px'
				});
				$resizehandler_left.css({
					top : '0px',
					left : (that.getDom().offset().left - that.sheet.getDom().offset().left) + 'px',
					height : (that.sheet.getTableDom().height() + that.sheet.getTopTableDom().height()) + 'px'
				});
				that.sheet.getDom().append($resizehandler_left).append($resizehandler_right);
				$(document).on('mousemove', function(e) {
					if (that.isResizing) {
						var x = that.getDom().offset().left;
						var width = Math.max(e.pageX - x, 50);
						that.getDom().css({
							'min-width' : width + 'px',
							'width' : width + 'px'
						});
						var index = that.getDom().index();
						$('.sheetbuilder-maintable-th:eq(' + index + ')').css({
							'min-width' : width + 'px',
							'width' : width + 'px'
						});
						that.sheet.$table.width(that.sheet.$toptable.width());

						$resizehandler_right.css({
							left : (that.getDom().offset().left - that.sheet.getDom().offset().left + that.getDom().outerWidth()) + 'px'
						});
					}
				});
				$(document).on('mouseup', function(e) {
					that.isResizing = false;
					$(document).off('mousemove').off('mouseup');
					$resizehandler_right.remove();
					$resizehandler_left.remove();
				});
			});

			that.$td.append($resizer);
			that.$td.data('cell', that);
		},
		getDom : function() {
			return this.$td;
		}
	};

	// 左边单元格
	var LeftCell = function(sheet, index) {
		var that = this;
		that.sheet = sheet;
		that.index = index;
		that.isResizing = false;
		that.init();
	};
	this.LeftCell = LeftCell;
	LeftCell.prototype = {
		init : function() {
			var that = this;
			if (typeof that.index == 'undefined') {
				that.index = that.sheet.getLeftCellList().length;
			}
			that.$td = $('<td class="sheetbuilder-lefttable-td"><span>' + (that.index + 1) + '</span></td>');
			that.$td.on('contextmenu', function(e) {
				if (that.sheet.contextmenu) {
					that.sheet.contextmenu.destory();
				}
				var contextmenu = new ContextMenu(that.sheet, e.pageX, e.pageY);
				that.sheet.contextmenu = contextmenu;
				return false;
			});
			var $resizer = $('<div class="sheetbuilder-left-resizer"></div>');
			$resizer.on('mousedown', function(event) {
				that.isResizing = true;
				var $resizehandler_top = $('<div class="sheetbuilder-leftresize-handler"></div>');
				var $resizehandler_bottom = $('<div class="sheetbuilder-leftresize-handler"></div>');
				$resizehandler_top.css({
					left : '0px',
					top : (that.getDom().offset().top - that.sheet.getDom().offset().top) + 'px',
					width : (that.sheet.getTableDom().width() + that.sheet.getLeftTableDom().width()) + 'px'
				});
				$resizehandler_bottom.css({
					left : '0px',
					top : (that.getDom().offset().top - that.sheet.getDom().offset().top + that.getDom().outerHeight()) + 'px',
					width : (that.sheet.getTableDom().width() + that.sheet.getTopTableDom().width()) + 'px'
				});
				that.sheet.getDom().append($resizehandler_top).append($resizehandler_bottom);
				$(document).on('mousemove', function(e) {
					if (that.isResizing) {
						var y = that.getDom().offset().top;
						var height = Math.max(e.pageY - y, 47);
						that.getDom().css({
							'min-height' : height + 'px',
							'height' : height + 'px'
						});
						var index = that.getDom().closest('tr').index();
						$('.sheetbuilder-maintable-tr:eq(' + index + ')').css({
							'min-height' : height + 'px',
							'height' : height + 'px'
						});

						$resizehandler_bottom.css({
							top : (that.getDom().offset().top - that.sheet.getDom().offset().top + that.getDom().outerHeight()) + 'px'
						});
					}
				});
				$(document).on('mouseup', function(e) {
					that.isResizing = false;
					$(document).off('mousemove').off('mouseup');
					$resizehandler_top.remove();
					$resizehandler_bottom.remove();
				});
			});

			that.$td.append($resizer);
			that.$td.data('cell', that);
		},
		getDom : function() {
			return this.$td;
		}
	};

	// 主体单元格
	var Cell = function(sheet, config) {
		var that = this;
		that.config = config || {};
		that.sheet = sheet;
		that.init();
	};
	this.Cell = Cell;
	Cell.prototype = {
		init : function() {
			var that = this;
			that.isEmpty = true;
			that.isSelected = false;
			that.$td = $('<td class="sheetbuilder-maintable-td"></td>');
			that.$td.data('cell', that);
			if (!that.sheet.builder.config.readonly) {
				that.$td.on('click', function(e) {
					if (!that.isTexting) {
						var firstTd = that;
						if (!e.shiftKey) {
							that.sheet.firsttd = that;
							that.sheet.lasttd = that;
							that.sheet.isRanging = true;
						} else {
							if (!that.isselected) {
								that.sheet.isRanging = true;
								that.sheet.lasttd = that;
								if (!that.sheet.firsttd) {
									that.sheet.firsttd = that;
								}
							} else {
								that.sheet.lasttd = that;
							}
						}
						that.sheet.selectRange();
					}
				});
				that.$td.on('dblclick', function(e) {
					if (!that.hasController()) {
						that.setEditMode(true);
					}
				});

				that.$td.on('contextmenu', function(e) {
					if (that.sheet.contextmenu) {
						that.sheet.contextmenu.destory();
					}
					var contextmenu = new ContextMenu(that.sheet, e.pageX, e.pageY);
					that.sheet.contextmenu = contextmenu;
					if (!that.isselected) {
						that.select();
					}
					return false;
				});
			}

			if (that.config.colspan) {
				that.$td.attr('colspan', that.config.colspan);
			}

			if (that.config.rowspan) {
				that.$td.attr('rowspan', that.config.rowspan);
			}
			if (that.config.text) {
				that.setText(that.config.text);
			}
		},
		getData : function() {
			return this.data;
		},
		getX : function() {
			return this.getDom().position().left;
		},
		getY : function() {
			return this.getDom().position().top;
		},
		getColspan : function() {
			if (this.getDom().attr('colspan')) {
				return parseInt(this.getDom().attr('colspan'), 10);
			}
			return 1;
		},
		getRowspan : function() {
			if (this.getDom().attr('rowspan')) {
				return parseInt(this.getDom().attr('rowspan'), 10);
			}
			return 1;
		},
		select : function() {
			this.sheet.firsttd = this;
			this.sheet.lasttd = this;
			this.sheet.isRanging = true;
			this.sheet.selectRange();
		},
		getDom : function() {
			return this.$td;
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
		setEditMode : function(bool) {
			var that = this;
			if (bool) {
				that.isTexting = true;
				that.sheet.isRanging = false;
				that.$td.empty();
				var input = $('<div contenteditable="true" class="sheetbuilder-texteditor"></div>');
				input.text(that.getText());
				input.on('blur', function() {
					that.setText($(this).text());
					that.isTexting = false;
				});
				that.getDom().empty().append(input);
				input.trigger('focus');
			} else {
			}
		},
		setText : function(text) {
			this.text = text;
			this.getDom().empty().text(this.text);
		},
		getText : function() {
			return this.text || '';
		},
		getCss : function() {
			if (this.getDom().attr('style')) {
				var css = {};
				css['text-align'] = this.getDom().css('text-align');
				css['vertical-align'] = this.getDom().css('vertical-align');
				return css;
			}
			return null;
		},
		hasController : function() {
			return false;
		}
	}

	var ContextMenu = function(sheet, mouseX, mouseY) {
		this.sheet = sheet;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.marginerror = 10;
		this.init();
	};
	this.ContextMenu = ContextMenu;
	ContextMenu.prototype = {
		init : function() {
			var that = this;
			var menu = $('<div tabindex="0" class="sheetbuilder-contextmenu" style="display:none"></div>');
			var ul = $('<ul></ul>');
			var liMerge = $('<li class="sheetbuilder-contextmenu-item">合并</li>');
			liMerge.on('click', function() {
				if (!$(this).hasClass('disabled')) {
					that.sheet.mergeRange();
					that.destory();
				}
			});
			var liSeparate = $('<li class="sheetbuilder-contextmenu-item">分拆</li>');
			liSeparate.on('click', function() {
				if (!$(this).hasClass('disabled')) {
					that.sheet.separateRange();
					that.destory();
				}
			});
			var liDelRow = $('<li class="sheetbuilder-contextmenu-item">删除行</li>');
			liDelRow.on('click', function() {
				var selectedCellList = that.sheet.getSelectedCellList();
				if (selectedCellList.length > 0) {
					var minY = Number.MAX_VALUE, maxY = 0;
					for (var i = 0; i < selectedCellList.length; i++) {
						var cell = selectedCellList[i];
						minY = Math.min(minY, cell.getY());
						maxY = Math.max(maxY, cell.getY() + cell.getDom().outerHeight());
					}
					var delTrList = new Array();
					var delRowspanTdList = new Array();
					that.sheet.getDom().find('.sheetbuilder-maintable-tr').each(function() {
						if ($(this).position().top >= minY && $(this).position().top < maxY) {
							delTrList.push($(this));
						}
					});

					that.sheet.getDom().find('.sheetbuilder-maintable-td').each(function() {
						if ($(this).attr('rowspan')) {
							if ($(this).position().top < minY && ($(this).position().top + $(this).outerHeight()) > minY) {
								// 缩小rowspan
								for (var dt = 0; dt < delTrList.length; dt++) {
									var delthTop = delTrList[dt].position().top;
									if (delthTop < ($(this).position().top + $(this).outerHeight())) {
										if (!$(this).data('minusrowspan')) {
											$(this).data('minusrowspan', 1);
										} else {
											$(this).data('minusrowspan', $(this).data('minusrowspan') + 1);
										}
									}
								}
								if ($(this).data('minusrowspan')) {
									delRowspanTdList.push($(this));
								}
							} else if ($(this).position().top >= minY && ($(this).position().top + $(this).outerHeight()) > maxY) {
								// 在下面补充td
								var minusrowspan = 0;
								var colspan = $(this).attr('colspan') ? $(this).attr('colspan') : 1;
								var rowspan = parseInt($(this).attr('rowspan'), 10);
								for (var dt = 0; dt < delTrList.length; dt++) {
									var delthTop = delTrList[dt].position().top;
									if (delthTop >= $(this).position().top) {
										minusrowspan += 1;
									}
								}
								var newspan = rowspan - minusrowspan;
								if (minusrowspan > 0) {
									var targetindex = $(this).closest('tr').index() + minusrowspan;
									var targettr = $(this).closest('tbody').find('.sheetbuilder-maintable-tr:eq(' + targetindex + ')');
									var tdright = $(this).position().left + $(this).width();
									if (targettr.length > 0) {
										var cellConf = {};
										if (newspan > 1) {
											cellConf['rowspan'] = newspan;
										}
										if (colspan > 1) {
											cellConf['colspan'] = colspan;
										}
										var cell = new Cell(that.sheet, cellConf);
										var targettd = null;
										targettr.find('.sheetbuilder-maintable-td').each(function() {
											if ($(this).position().left >= tdright) {
												targettd = $(this);
												return false;
											}
										});
										if (targettd) {
											targettd.before(cell.getDom());
										} else {
											targettr.append(cell.getDom());
										}
									}
								}
							}
						}
					});

					if (delRowspanTdList.length > 0) {
						for (var i = 0; i < delRowspanTdList.length; i++) {
							var td = delRowspanTdList[i];
							var rowspan = parseInt(td.attr('rowspan'), 10);
							rowspan = rowspan - td.data('minusrowspan');
							if (rowspan > 1) {
								td.attr('rowspan', rowspan);
							} else {
								td.removeAttr('rowspan');
							}
							td.removeData('minusrowspan');
						}
					}

					if (delTrList.length > 0) {
						for (var i = 0; i < delTrList.length; i++) {
							that.sheet.removeLeft(delTrList[i].index());
							delTrList[i].remove();
						}
					}
				}
				that.destory();
			});
			var liDelCol = $('<li class="sheetbuilder-contextmenu-item">删除列</li>');
			liDelCol.on('click', function() {
				var selectedCellList = that.sheet.getSelectedCellList();
				if (selectedCellList.length > 0) {
					var minX = Number.MAX_VALUE, maxX = 0;
					for (var i = 0; i < selectedCellList.length; i++) {
						var cell = selectedCellList[i];
						minX = Math.min(minX, cell.getX());
						maxX = Math.max(maxX, cell.getX() + cell.getDom().width());
					}
					var delTdList = new Array();
					var delThList = new Array();
					var delColspanTdList = new Array();
					that.sheet.getDom().find('.sheetbuilder-maintable-th').each(function() {
						if ($(this).position().left >= minX && $(this).position().left <= maxX) {
							delThList.push($(this));
						}
					});

					that.sheet.getDom().find('.sheetbuilder-maintable-td').each(function() {
						if ($(this).position().left >= minX && ($(this).position().left + $(this).width()) <= maxX) {
							delTdList.push($(this));
						} else {
							if ($(this).attr('colspan')) {
								if (($(this).position().left <= minX && ($(this).position().left + $(this).width()) > minX) || ($(this).position().left > minX && $(this).position().left < maxX)) {
									for (var dt = 0; dt < delThList.length; dt++) {
										var delthLeft = delThList[dt].position().left;
										if (delthLeft >= $(this).position().left && delthLeft < ($(this).position().left + $(this).width())) {
											if (!$(this).data('minuscolspan')) {
												$(this).data('minuscolspan', 1);
											} else {
												$(this).data('minuscolspan', $(this).data('minuscolspan') + 1);
											}
										}
									}
									if ($(this).data('minuscolspan')) {
										delColspanTdList.push($(this));
									}
								}
							}
						}
					});

					if (delTdList.length > 0) {
						for (var i = 0; i < delTdList.length; i++) {
							delTdList[i].remove();
						}
					}

					if (delColspanTdList.length > 0) {
						for (var i = 0; i < delColspanTdList.length; i++) {
							var td = delColspanTdList[i];
							var colspan = parseInt(td.attr('colspan'), 10);
							colspan = colspan - td.data('minuscolspan');
							if (colspan > 1) {
								td.attr('colspan', colspan);
							} else {
								td.removeAttr('colspan');
							}
							td.removeData('minuscolspan');
						}
					}

					if (delThList.length > 0) {
						for (var i = 0; i < delThList.length; i++) {
							that.sheet.removeTop(delThList[i].index());
							delThList[i].remove();
						}
						that.sheet.$table.width(that.sheet.$toptable.width());
					}

				}
				that.destory();
			});
			var liAddRowBefore = $('<li class="sheetbuilder-contextmenu-item">在前插入<input type="number" onclick="event.stopPropagation();" min="0" max="10" value="1" class="sheetbuilder-numberinput">行</li>');
			liAddRowBefore.on('click', function() {
				var rowcount = $(this).find('input').val();
				var selectedCellList = that.sheet.getSelectedCellList();
				if (rowcount) {
					rowcount = parseInt(rowcount, 10);
					var minY = Number.MAX_VALUE;
					var targettr = null;
					if (selectedCellList.length > 0) {
						for (var i = 0; i < selectedCellList.length; i++) {
							if (minY > selectedCellList[i].getY()) {
								minY = selectedCellList[i].getY();
							}
						}
						$('.sheetbuilder-maintable-tr', that.sheet.getTableDom()).each(function(i, k) {
							if ($(this).position().top == minY) {
								targettr = $(this);
								return false;
							}
						});
					}
					var colcount = that.sheet.getColCount();
					// debugger;
					if (targettr) {
						var rowspanTdList = new Array();
						that.sheet.getTableDom().find('.sheetbuilder-maintable-td').each(function() {
							if (minY < Number.MAX_VALUE) {
								if ($(this).attr('rowspan') && $(this).position().top < minY && ($(this).position().top + $(this).outerHeight()) > minY) {
									rowspanTdList.push($(this));
								}
							}
						});
						var skipcount = 0;
						for (var i = 0; i < rowspanTdList.length; i++) {
							if (rowspanTdList[i].attr('colspan')) {
								skipcount += parseInt(rowspanTdList[i].attr('colspan'), 10);
							} else {
								skipcount += 1;
							}
							var rowspan = parseInt(rowspanTdList[i].attr('rowspan'), 10);
							rowspan += rowcount;
							rowspanTdList[i].attr('rowspan', rowspan);
						}

						for (var i = 0; i < rowcount; i++) {
							var tr = $('<tr class="sheetbuilder-maintable-tr"></tr>');
							for (var j = 0; j < colcount - skipcount; j++) {
								var cell = new Cell(that.sheet);
								tr.append(cell.getDom());
							}
							targettr.before(tr);
							that.sheet.prependLeft(targettr.index());
						}

					} else {
						for (var i = 0; i < rowcount; i++) {
							var tr = $('<tr class="sheetbuilder-maintable-tr"></tr>');
							for (var j = 0; j < colcount; j++) {
								var cell = new Cell(that.sheet);
								tr.append(cell.getDom());
							}
							that.sheet.getTableDom().find('tbody').append(tr);

							that.sheet.appendLeft();
						}
					}
					that.destory();
				}
			});
			var liAddRowAfter = $('<li class="sheetbuilder-contextmenu-item">在后插入<input type="number" onclick="event.stopPropagation();" min="0" max="10" value="1" class="sheetbuilder-numberinput">行</li>');
			liAddRowAfter.on('click', function() {
				var rowcount = $(this).find('input').val();
				var selectedCellList = that.sheet.getSelectedCellList();
				if (rowcount) {
					rowcount = parseInt(rowcount, 10);
					var maxY = -1;
					var targettr = null;
					if (selectedCellList.length > 0) {
						for (var i = 0; i < selectedCellList.length; i++) {
							if (maxY < selectedCellList[i].getY() + selectedCellList[i].getDom().outerHeight()) {
								maxY = selectedCellList[i].getY() + selectedCellList[i].getDom().outerHeight();
							}
						}
						$('.sheetbuilder-maintable-tr', that.sheet.getTableDom()).each(function(i, k) {
							if ($(this).position().top + $(this).outerHeight() == maxY) {
								targettr = $(this);
								return false;
							}
						});
					}
					var colcount = that.sheet.getColCount();
					if (targettr) {
						var rowspanTdList = new Array();
						that.sheet.getTableDom().find('.sheetbuilder-maintable-td').each(function() {
							var rowspanTd = null;
							if (maxY > -1) {
								if ($(this).attr('rowspan') && $(this).position().top < maxY && ($(this).position().top + $(this).outerHeight()) > maxY) {
									rowspanTd = $(this);
									rowspanTdList.push(rowspanTd);
								}
							}
						});
						var skipcount = 0;
						for (var i = 0; i < rowspanTdList.length; i++) {
							if (rowspanTdList[i].attr('colspan')) {
								skipcount += parseInt(rowspanTdList[i].attr('colspan'), 10);
							} else {
								skipcount += 1;
							}
							var rowspan = parseInt(rowspanTdList[i].attr('rowspan'), 10);
							rowspan += rowcount;
							rowspanTdList[i].attr('rowspan', rowspan);
						}

						for (var i = 0; i < rowcount; i++) {
							var tr = $('<tr class="sheetbuilder-maintable-tr"></tr>');
							for (var j = 0; j < colcount - skipcount; j++) {
								var cell = new Cell(that.sheet);
								tr.append(cell.getDom());
							}
							targettr.after(tr);
							that.sheet.appendLeft(targettr.index());
						}

					} else {
						for (var i = 0; i < rowcount; i++) {
							var tr = $('<tr class="sheetbuilder-maintable-tr"></tr>');
							for (var j = 0; j < colcount; j++) {
								var cell = new Cell(that.sheet);
								tr.append(cell.getDom());
							}
							that.sheet.getTableDom().find('tbody').append(tr);

							that.sheet.appendLeft();
						}
					}
					that.destory();
				}
			});

			var liAddColBefore = $('<li class="sheetbuilder-contextmenu-item">在前插入<input type="number" onclick="event.stopPropagation();" min="0" max="10" value="1" class="sheetbuilder-numberinput">列</li>');
			liAddColBefore.on('click', function() {
				var colcount = $(this).find('input').val();
				var selectedCellList = that.sheet.getSelectedCellList();
				if (colcount) {
					colcount = parseInt(colcount, 10);
					var minX = Number.MAX_VALUE;
					var targetth = null;
					if (selectedCellList.length > 0) {
						for (var i = 0; i < selectedCellList.length; i++) {
							if (minX > selectedCellList[i].getX()) {
								minX = selectedCellList[i].getX();
							}
						}
						$('.sheetbuilder-maintable-th', that.sheet.getTableDom()).each(function(i, k) {
							if ($(this).position().left == minX) {
								targetth = $(this);
								return false;
							}
						});
					}

					var colspanTdList = new Array();
					var beforeTdList = new Array();
					var trPrependList = new Array();
					that.sheet.getTableDom().find('.sheetbuilder-maintable-tr').each(function() {
						var afterTd = null;
						var colspanTd = null;
						var beforeTd = null;
						var excludeTr = null;
						for (var i = 0; i < colspanTdList.length; i++) {
							var colTd = colspanTdList[i];
							if ($(this).position().top >= colTd.position().top && $(this).position().top < colTd.position().top + colTd.outerHeight()) {
								excludeTr = $(this);
								break;
							}
						}
						if (minX < Number.MAX_VALUE) {
							$(this).find('.sheetbuilder-maintable-td').each(function() {
								if ($(this).position().left == minX) {
									beforeTd = $(this);
									beforeTdList.push(beforeTd);
									return false;
								} else if ($(this).attr('colspan') && $(this).position().left < minX && ($(this).position().left + $(this).width()) > minX) {
									colspanTd = $(this);
									colspanTdList.push(colspanTd);
									return false;
								}
							});
						}
						if (!colspanTd && !beforeTd && !excludeTr) {
							trPrependList.push($(this));
						}
					});

					for (var i = 0; i < colspanTdList.length; i++) {
						var td = colspanTdList[i];
						var colspan = parseInt(td.attr('colspan'), 10);
						colspan += colcount;
						td.attr('colspan', colspan);
					}

					for (var i = 0; i < colcount; i++) {
						for (var j = 0; j < beforeTdList.length; j++) {
							var cell = new Cell(that.sheet);
							beforeTdList[j].before(cell.getDom());
						}
						for (var j = 0; j < trPrependList.length; j++) {
							var cell = new Cell(that.sheet);
							trPrependList[j].prepend(cell.getDom());
						}
					}

					for (var i = 0; i < colcount; i++) {
						if (targetth) {
							that.sheet.prependTop(targetth.index());
						} else {
							that.sheet.prependTop();
						}
					}

					for (var i = 0; i < colcount; i++) {
						var th = $('<th class="sheetbuilder-maintable-th"></th>');
						if (targetth) {
							targetth.before(th);
						} else {
							that.sheet.getTableDom().find('thead').children('tr').prepend(th);
						}
					}

					that.sheet.getTableDom().width(that.sheet.getTopTableDom().width());
					that.destory();
				}
			});
			var liAddColAfter = $('<li class="sheetbuilder-contextmenu-item">在后插入<input type="number" onclick="event.stopPropagation();" min="0" max="10" value="1" class="sheetbuilder-numberinput">列</li>');
			liAddColAfter.on('click', function() {
				var colcount = $(this).find('input').val();
				var selectedCellList = that.sheet.getSelectedCellList();
				if (colcount) {
					colcount = parseInt(colcount, 10);
					var maxX = -1;
					var targetth = null;
					if (selectedCellList.length > 0) {
						for (var i = 0; i < selectedCellList.length; i++) {
							if (maxX < selectedCellList[i].getX() + selectedCellList[i].getDom().width()) {
								maxX = selectedCellList[i].getX() + selectedCellList[i].getDom().width();
							}
						}
						$('.sheetbuilder-maintable-th', that.sheet.getTableDom()).each(function(i, k) {
							if ($(this).position().left + $(this).width() - maxX < that.marginerror) {// 考虑10像素误差
								targetth = $(this);
							}
						});
					}

					var colspanTdList = new Array();
					var afterTdList = new Array();
					var beforeTdList = new Array();
					var trAppendList = new Array();
					that.sheet.getTableDom().find('.sheetbuilder-maintable-tr').each(function() {
						var afterTd = null;
						var colspanTd = null;
						var beforeTd = null;
						var excludeTr = null;
						for (var i = 0; i < colspanTdList.length; i++) {
							var colTd = colspanTdList[i];
							if ($(this).position().top >= colTd.position().top && $(this).position().top < colTd.position().top + colTd.outerHeight()) {
								excludeTr = $(this);
								break;
							}
						}
						if (maxX > -1 && !excludeTr) {
							$(this).find('.sheetbuilder-maintable-td').each(function() {
								if (($(this).position().left + $(this).width()) == maxX) {
									afterTd = $(this);
									afterTdList.push(afterTd);
									return false;
								} else if ($(this).attr('colspan') && $(this).position().left < maxX && ($(this).position().left + $(this).width()) > maxX) {
									colspanTd = $(this);
									colspanTdList.push(colspanTd);
									return false;
								} else if ($(this).position().left > maxX) {
									beforeTd = $(this);
									beforeTdList.push(beforeTd);
									return false;
								}
							});
						}
						if (!afterTd && !colspanTd && !beforeTd && !excludeTr) {
							trAppendList.push($(this));
						}
					});

					for (var i = 0; i < colspanTdList.length; i++) {
						var td = colspanTdList[i];
						var colspan = parseInt(td.attr('colspan'), 10);
						colspan += colcount;
						td.attr('colspan', colspan);
					}

					for (var i = 0; i < colcount; i++) {
						for (var j = 0; j < afterTdList.length; j++) {
							var cell = new Cell(that.sheet);
							afterTdList[j].after(cell.getDom());
						}
						for (var j = 0; j < beforeTdList.length; j++) {
							var cell = new Cell(that.sheet);
							beforeTdList[j].before(cell.getDom());
						}
						for (var j = 0; j < trAppendList.length; j++) {
							var cell = new Cell(that.sheet);
							trAppendList[j].append(cell.getDom());
						}
					}

					for (var i = 0; i < colcount; i++) {
						if (targetth) {
							that.sheet.appendTop(targetth.index());
						} else {
							that.sheet.appendTop();
						}
					}

					for (var i = 0; i < colcount; i++) {
						var th = $('<th class="sheetbuilder-maintable-th"></th>');
						if (targetth) {
							targetth.after(th);
						} else {
							that.sheet.getTableDom().find('thead').children('tr').append(th);
						}
					}

					that.sheet.getTableDom().width(that.sheet.getTopTableDom().width());
					that.destory();
				}
			});
			var liCancelSelect = $('<li class="sheetbuilder-contextmenu-item">取消选择</li>');
			liCancelSelect.on('click', function() {
				that.sheet.firsttd = null;
				that.sheet.lasttd = null;
				that.sheet.selectRange();
				that.destory();
			});

			var selectedCellList = that.sheet.getSelectedCellList();
			if (selectedCellList.length <= 1) {
				liMerge.addClass('disabled');
			}

			if (selectedCellList.length > 0) {
				var hasMerge = false;
				for (var i = 0; i < selectedCellList.length; i++) {
					if (selectedCellList[i].getColspan() > 1 || selectedCellList[i].getRowspan() > 1) {
						hasMerge = true;
						break;
					}
				}
				if (!hasMerge) {
					liSeparate.addClass('disabled');
				}
			} else {
				liSeparate.addClass('disabled');
			}

			ul.append(liMerge).append(liSeparate).append('<hr>').append(liDelRow).append(liDelCol).append('<hr>').append(liAddRowBefore).append(liAddRowAfter).append(liAddColBefore).append(liAddColAfter).append('<hr>').append(liCancelSelect);
			menu.append(ul);
			that.sheet.getDom().append(menu);
			// 获取窗口尺寸
			var winWidth = that.sheet.getDom().width();
			var winHeight = that.sheet.getDom().height();
			// 鼠标点击位置坐标
			var mouseX = that.mouseX - that.sheet.getDom().offset().left;
			var mouseY = that.mouseY - that.sheet.getDom().offset().top;
			// ul标签的宽高
			var menuWidth = menu.width();
			var menuHeight = menu.height();
			// 最小边缘margin(具体窗口边缘最小的距离)
			var minEdgeMargin = 0;
			// 以下判断用于检测ul标签出现的地方是否超出窗口范围
			// 第一种情况：右下角超出窗口
			if (mouseX + menuWidth + minEdgeMargin >= winWidth && mouseY + menuHeight + minEdgeMargin >= winHeight) {
				menuLeft = mouseX - menuWidth - minEdgeMargin + "px";
				menuTop = mouseY - menuHeight - minEdgeMargin + "px";
			}
			// 第二种情况：右边超出窗口
			else if (mouseX + menuWidth + minEdgeMargin >= winWidth) {
				menuLeft = mouseX - menuWidth - minEdgeMargin + "px";
				menuTop = mouseY + minEdgeMargin + "px";
			}
			// 第三种情况：下边超出窗口
			else if (mouseY + menuHeight + minEdgeMargin >= winHeight) {
				menuLeft = mouseX + minEdgeMargin + "px";
				menuTop = mouseY - menuHeight - minEdgeMargin + "px";
			}
			// 其他情况：未超出窗口
			else {
				menuLeft = mouseX + minEdgeMargin + "px";
				menuTop = mouseY + minEdgeMargin + "px";
			}

			menu.css({
				"left" : menuLeft,
				"top" : menuTop
			}).slideDown('fast');

			menu.on('mouseenter', function() {
				that.isHover = true;
			});

			menu.on('mouseleave', function() {
				that.isHover = false;
			});
			this.$menu = menu;
		},
		destory : function() {
			var that = this;
			that.$menu.remove();
		},
		getDom : function() {
			return this.$menu;
		}
	}

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