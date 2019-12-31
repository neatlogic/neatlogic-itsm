(function($) {
	var ZTREE = null;
	$.fn.treeselect = function(options) {

		var html = '<span class="spnSelectedItem" style="display:inline-block;' + 'border-right:1px solid #bbb;' + 'border-top:1px solid #ddd; ' + 'border-bottom:1px solid #bbb;' + 'border-left:1px solid #ddd;background:#fcfcfc;border-radius:2px;padding:2px 15px 2px 5px;margin:1px"></span>';
		var $target = $(this);
		if ($target.children('input[type=hidden]').length > 0) {
			var valueList = new Array();
			$target.children('input[type=hidden]').each(function() {
				if ($(this).val() != '') {
					valueList.push($(this).val());
				}
			});
			options.value = valueList;
			$target.children('input[type=hidden]').remove();

		}

		$target.unbind();
		if (typeof $target.data('treeDiv') != 'undefined' && $target.data('treeDiv') != null) {
			$target.data('treeDiv').remove();
			$target.data('treeDiv', null).data('tree', null);
		}
		;
		$target.html('');

		this.getValue = function() {
			var returnVal = new Array();
			var nodes = ZTREE.getCheckedNodes(true);
			for ( var n = 0; n < nodes.length; n++) {
				var exists = false;
				var node = nodes[n];
				for ( var r = 0; r < returnVal.length; r++) {
					if (returnVal[r].id == node.id) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					var returnObj = {};
					for ( var i = 0; i < arguments.length; i++) {
						var arg = arguments[i];
						if (typeof arg == 'string' && typeof node[arg] != 'undefined' && node[arg] != null) {
							returnObj[arg] = node[arg];
						}
					}
					returnVal.push(returnObj);
				}
			}
			return returnVal;
		}

		$target.css({
			'padding' : '5px 15px 5px 5px',
			/*
			 * 'border-top-left-radius' : '4px', 'border-bottom-left-radius' :
			 * '4px', 'border-bottom-right-radius' : '4px',
			 * 'border-top-right-radius' : '4px',
			 */
			'border-top' : '1px solid #ccc',
			'border-left' : '1px solid #ccc',
			'border-bottom' : '1px solid #ccc',
			'border-right' : '1px solid #ccc',
			'min-height' : '30px',
			'position' : 'relative',
			'vertical-align' : 'middle',
			'display' : 'inline-block',
			'min-width' : '200px',
			'text-align' : 'left'
		});

		$target.append('<i style="position:absolute;top:2px;right:2px" class="jquery-ztreeselect-up"></i>');

		if (options.value != null && options.value.length > 1) {// 如果选项多余一个，强制切回checkbox模式
			options.chkstyle = 'checkbox';
		}

		var opts = $.extend(true, {}, $.fn.treeselect.defaultopts, options);

		var setting = {
			check : {
				enable : true,
				radioType : "all",
				chkStyle : opts.chkstyle,
				chkboxType : opts.chkboxType
			},
			view : {
				showIcon : true,
				dblClickExpand : true,
				showLine : true,
				fontCss : setFontCss
			},
			data : {
				simpleData : {
					enable : true,
					idKey : "id",
					pIdKey : "pId",
					rootPId : 0
				}
			},
			callback : {
				onCheck : function zTreeOnCheck(event, treeId, treeNode) {
					if (opts.chkstyle == 'radio' && treeNode.checked) {
						var h = html;// .replace('#{id}', treeNode.id);
						var $h = $(h);
						$h.append(treeNode.name);
						if ($target.attr('name')) {
							$h.append('<input type="hidden" name="' + $target.attr('name') + '" value="' + treeNode.id + '">');
						} else {
							$h.append('<input type="hidden" value="' + treeNode.id + '">');
						}
						$target.children('.spnSelectedItem').remove();
						$target.append($h).trigger('sizechange');
						var $div = $target.data('treeDiv');
						$div.slideUp();
					} else {
						$target.children('.spnSelectedItem').remove();
						$target.trigger('sizechange');
						var zTree = $target.data('tree');
						var nodes = zTree.getCheckedNodes(true);
						for ( var i = 0; i < nodes.length; i++) {
							var h = html;
							var $h = $(h);
							var exists = false;
							$target.find('input[type=hidden]').each(function() {
								if ($(this).val() == nodes[i].id) {
									exists = true;
									return false;
								}
							});
							if (!exists) {
								$h.append(nodes[i].name);
								if ($target.attr('name')) {
									$h.append('<input type="hidden" name="' + $target.attr('name') + '" value="' + nodes[i].id + '">');
								} else {
									$h.append('<input type="hidden" value="' + nodes[i].id + '">');
								}
								$target.append($h).trigger('sizechange');
							}
						}
					}
					if ($target.attr('check-type') && $target.find('input[type=hidden]').length <= 0 && $target.data('validator') == null) {
						var validator = $('<input type="text" readonly class="ztreeselect_placeholder" style="border:0px;width:0px;" check-type="' + $target.attr('check-type') + '" ' + ($target.attr('required-message') ? ('required-message="' + $target.attr('required-message') + '"') : '') + '>');
						$target.after(validator);
						$target.data('validator', validator);
					} else if ((!$target.attr('check-type') || $target.find('input[type=hidden]').length > 0) && $target.data('validator') != null) {
						$target.data('validator').remove();
						$target.data('validator', null);
					}
					if (opts.onChoose != null) {
						opts.onChoose($target);
					}
				}
			}
		};
		var json = null;
		if (opts.url != '') {
			$.ajax({
				type : "GET",
				url : opts.url,
				dataType : "json",
				async : false,
				success : function(data) {
					json = data;
					if (opts.value != null && opts.value.length > 0) {
						for ( var d = 0; d < data.length; d++) {
							for ( var v = 0; v < opts.value.length; v++) {
								if (data[d].id == opts.value[v]) {
									opts.value.splice(v, 1);
									data[d].checked = true;
									var h = html;
									var $h = $(h);
									$h.append(data[d].name);
									if ($target.attr('name')) {
										$h.append('<input type="hidden" name="' + $target.attr('name') + '" value="' + data[d].id + '">');
									}
									$target.append($h);
								}
							}
						}
					}
				}
			});

			if ($target.attr('check-type') && $target.find('input[type=hidden]').length <= 0 && $target.data('validator') == null) {
				var validator = $('<input type="text" readonly class="ztreeselect_placeholder" style="border:0px;width:0px;" check-type="' + $target.attr('check-type') + '" ' + ($target.attr('required-message') ? ('required-message="' + $target.attr('required-message') + '"') : '') + '>');
				$target.after(validator);
				$target.data('validator', validator);
			}

			if (json != null) {
				$target.on('sizechange', function() {
					var $div = $target.data('treeDiv');
					if ((typeof $div != 'undefined' && $div != null) && $div.is(':visible')) {
						var top = $target.position().top + $target.outerHeight(true) - ($target.outerHeight(true) - $target.height()) / 2;
						var left = $target.position().left;
						$div.css({
							'top' : top,
							'left' : left
						});
					}
					if ($div.outerWidth(true) > $target.outerWidth(true)) {
						$target.css('width', $div.outerWidth(true));
					} else {
						$div.css('width', $target.outerWidth(true));
					}
				});

				$target.on('click', function() {
					var $div = $target.data('treeDiv');
					if (typeof $div == 'undefined' || $div == null) {
						$div = $('<div class="treeselect" style="position:absolute;height:auto;background:#fefefe;border-top:0px;border-left:1px solid #ddd;border-right:1px solid #ccc;border-bottom:1px solid #ccc;border-bottom-left-radius:4px;border-bottom-right-radius:4px;display:none;z-index:' + opts.zindex + '"></div>');
						var $searchdiv = $('<div style="padding:3px"></div>');
						var $searcher = $('<input type="text" class="form-control input-sm" style="width:100%;" placeholder="输入关键字">');
						var $close = $('<div style="text-align:right"><i class="pic-icon-close pic-icon-small" style="cursor:pointer"></i></div>');
						var $ul = $('<ul id="' + new Date().getTime() + parseInt(Math.random() * 10000000) + '" class="ztree"></ul>');
						var top = $target.position().top + $target.outerHeight(true) - ($target.outerHeight(true) - $target.height()) / 2;
						var left = $target.position().left;
						$div.css({
							'top' : top,
							'left' : left
						});
						$searchdiv.append($searcher);
						$div.append($close).append($searchdiv).append($ul);
						$target.after($div);
						var zTree = $.fn.zTree.init($ul, setting, json);
						ZTREE = zTree;
						if (opts.selectmode != 'all') {
							var nodes = zTree.getNodes();
							disabledCheck(nodes, opts.selectmode);
						}
						if (opts.extendall) {
							zTree.expandAll(true);
						} else {
							zTree.expandAll(false);
						}
						$close.click(function() {
							if ($div.is(':visible')) {
								$div.slideUp(function() {
									$target.css('border-bottom', '1px solid #ccc');
								});
							}
						});

						$searcher.keyup(function() {
							var oldnodes = zTree.getNodesByParam("found", 1, null);
							for ( var n = 0; n < oldnodes.length; n++) {
								oldnodes[n].found = 0;
								zTree.updateNode(oldnodes[n]);
							}
							if ($.trim($(this).val()) != '') {
								var nodes = zTree.getNodesByParamFuzzy("name", $.trim($(this).val()), null);
								for ( var n = 0; n < nodes.length; n++) {
									nodes[n].found = 1;
									zTree.updateNode(nodes[n]);
									if (!nodes[n].isParent) {
										zTree.expandNode(nodes[n].getParentNode(), true, true, false);
									} else {
										zTree.expandNode(nodes[n], true, true, true);
									}
								}
							}
						});

						$div.slideDown('fast', function() {
							$target.trigger('sizechange');
						});
						$target.data('tree', zTree).data('treeDiv', $div);
					} else {
						if ($div.is(':visible')) {
							$div.slideUp();
						} else {
							var top = $target.position().top + $target.outerHeight(true) - ($target.outerHeight(true) - $target.height()) / 2;
							var left = $target.position().left;
							$div.css({
								'top' : top,
								'left' : left
							});
							$div.slideDown('fast', function() {
								$target.trigger('sizechange');
							});
						}
					}
				});
			}
		}
		return this;
	};

	function setFontCss(treeId, treeNode) {
		return treeNode.found == 1 ? {
			"color" : "red",
			"font-weight" : "bold"
		} : {
			"color" : "#333",
			"font-weight" : "normal"
		};
	}

	function disabledCheck(nodes, selectmode) {
		var zTree = ZTREE;
		for ( var n = 0; n < nodes.length; n++) {
			if (selectmode == 'child') {
				if (nodes[n].isParent) {
					zTree.setChkDisabled(nodes[n], true);
					disabledCheck(nodes[n].children, selectmode);
				}
			} else if (selectmode == 'parent') {
				if (!nodes[n].isParent) {
					zTree.setChkDisabled(nodes[n], true);
				} else {
					disabledCheck(nodes[n].children, selectmode);
				}
			}
		}
	}

	$.fn.treeselect.defaultopts = {
		url : '',// 返回json数据的超链接
		selectmode : 'child', // child:只能选子节点,parent:只能选父节点,all:所有
		extendall : false, // 是否展开所有节点
		chkstyle : 'radio', // 可选：radio或checkbox
		zindex : 3000, // zindex,
		chkboxType : {
			"Y" : "s",
			"N" : "s"
		},
		value : [],
		onChoose : null
	// 默认值
	};

})(jQuery);