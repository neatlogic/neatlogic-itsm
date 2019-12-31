(function($) {
	$.fn.treeselect = function(options) {
		var opts = $.extend(true, {}, $.fn.treeselect.defaultopts, options);
		var $target = $(this);
		opts.placeholder= $(this).attr("placeholder")?$(this).attr("placeholder"):opts.placeholder;
		$target.wrap('<div class="treeselect-contain"></div>');
		var zTree = null;
		if (!$target.data('bind')) {
			var isSingle = $target.attr('multiple') ? false : true;
			var $selector = $('<span class="treeselect-outspan"></span>');
			if(opts.height){
				$selector.css("line-height",opts.height+"px");
			}
			if(opts.width){
				$selector.css("min-width",opts.width+"px");
			}
			var $downitem = $('<i class="jquery-ztreeselect-up" ></i>');
			var $valuespan = $('<span class="treeselect-valuespan" placeholder="'+opts.placeholder+'"></span>');
			$selector.append($valuespan).append($downitem);
			var $optioncontainer = $('<div class="treeselect-optioncontain" style="z-index:' + opts.zindex + ';min-width:' + opts.width + 'px;"></div>');
			var $treecontainer = $('<ul id="' + new Date().getTime() + parseInt(Math.random() * 10000000) + '" class="ztree"></ul>');
			var $searchdiv = $('<div style="padding:3px"></div>');
			var $searcher = $('<input type="text" style="width:100%;" placeholder="输入关键字">');
			$searchdiv.append($searcher);
			$optioncontainer.append($searchdiv).append($treecontainer);
			$target.after($selector);
			$selector.after($optioncontainer);
			$target.find('option').each(function() {
				if ($(this).prop('selected') && $(this).val() != '') {
					//$valuespan.append('<span class="jquery_treeselect_item" data-value="' + $(this).val() + '"><i>' + $(this).text() + ';</i>&nbsp;</span>');
					opts.value.push($(this).val());
					if (isSingle) {
						return false;
					}
				}
			});
			$target.find('option').remove();

			if ($target.find('option').length == 0) {
				$target.append('<option value=""></option>');
			}

			$target.hide();

			if (isSingle) {// 如果选项多余一个，强制切回checkbox模式
				opts.chkstyle = 'radio';
			} else {
				opts.chkstyle = 'checkbox';
			}

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
					fontCss : function(treeId, treeNode) {
						return treeNode.found == 1 ? {
							"color" : "red",
							"font-weight" : "bold"
						} : {
							"color" : "#666",
							"font-weight" : "normal"
						};
					},
					addDiyDom : function(treeId, treeNode) {
						$("#" + treeNode.tId + "_span").css({
							'vertical-align' : 'middle',
							'text-overflow' : 'ellipsis',
							'white-space' : 'nowrap',
							'overflow' : 'hidden',
							'display' : 'inline-block'
						});
					}
				},
				data : {
					simpleData : {
						enable : true,
						idKey : opts.idKey || "id",
						pIdKey : opts.pIdKey || "pId",
						rootPId : 0
					}
				},
				callback : {
					onCheck : function zTreeOnCheck(event, treeId, treeNode) {
						$target.children().remove();
						$valuespan.find('span').remove();
						if (opts.chkstyle == 'radio' && treeNode.checked) {
							var sltoption = $('<option selected value="' + treeNode.id + '">' + treeNode.name + '</option>');
							for ( var v in treeNode) {
								if (v.indexOf('data-') == 0) {
									sltoption.attr(v, treeNode[v]);
								}
							}
							$target.append(sltoption);
							$valuespan.append('<span class="jquery_treeselect_item" data-value="' + treeNode.id + '"><i class="noneed">' + treeNode.name + ';</i>&nbsp;</span>');
							$optioncontainer.slideUp();
						} else {
							var nodes = zTree.getCheckedNodes(true);
							for (var i = 0; i < nodes.length; i++) {
								var sltoption = $('<option selected value="' + nodes[i].id + '">' + nodes[i].name + '</option>');
								for ( var v in nodes[i]) {
									if (v.indexOf('data-') == 0) {
										sltoption.attr(v, nodes[i][v]);
									}
								}
								$target.append(sltoption);
								$valuespan.append('<span class="jquery_treeselect_item" data-value="' + nodes[i].id + '"><i class="noneed">' + nodes[i].name + ';</i>&nbsp;</span>');
							}
						}
						$target.trigger('change');
						$selector.trigger('sizechange');
					},
					onNodeCreated : function(event, treeId, treeNode) {
						var icon = $("#" + treeNode.tId + "_ico");
						icon.removeClass('button');
					}
				}
			};
			var json = null;
			if (opts.url != '') {
				$.getJSON(opts.url, function(data) {
					if (opts.value != null && opts.value.length > 0) {
						for (var d = 0; d < data.length; d++) {
							for (var v = 0; v < opts.value.length; v++) {
								if (data[d].id == opts.value[v]) {
									data[d].checked = true;
									var sltoption = $('<option selected value="' + data[d].id + '">' + data[d].name + '</option>');
									$target.append(sltoption);
									$valuespan.append('<span class="jquery_treeselect_item" data-value="' + data[d].id + '"><i class="noneed">' + data[d].name + ';</i>&nbsp;</span>');
								}
							}
						}
					}
					zTree = $.fn.zTree.init($treecontainer, setting, data);
					if (opts.selectmode != 'all') {
						var nodes = zTree.getNodes();
						disabledCheck(zTree, nodes, opts.selectmode);
					}
					if (opts.extendall) {
						zTree.expandAll(true);
					} else {
						zTree.expandAll(false);
					}
					if($target.val()){
						$target.trigger('change');
					}
				});
			}

			$selector.on('sizechange', function() {
				// if ($optioncontainer.is(':visible')) {
				$optioncontainer.css('min-width',$selector.width());
				// }
			});

			$selector.on('click', function() {
				if ($optioncontainer.is(':visible')) {
					$optioncontainer.slideUp();
				} else {
					$optioncontainer.slideDown();
				}
			});

			$searcher.on(opts.searchMod, function(event) {
				if ((opts.searchMod == 'keypress' && event.keyCode == "13") || opts.searchMod == 'keyup') {
					zTree.expandAll(false);
					var oldnodes = zTree.getNodesByParam("found", 1, null);
					for (var n = 0; n < oldnodes.length; n++) {
						oldnodes[n].found = 0;
						zTree.updateNode(oldnodes[n]);
					}
					if ($.trim($(this).val()) != '') {
						var nodes = zTree.getNodesByParamFuzzy("name", $.trim($(this).val()), null);
						for (var n = 0; n < nodes.length; n++) {
							nodes[n].found = 1;
							zTree.updateNode(nodes[n]);
							// if (!nodes[n].isParent) {
							zTree.expandNode(nodes[n].getParentNode(), true, false, false);
							// } else {
							// if (opts.searchChildNode) {// 是否寻找最终的叶子节点
							// zTree.expandNode(nodes[n], true, true, true);
							// }
							// }
						}
					}
					event.stopPropagation();
					return false;
				}
			});
			$target.data('bind', true);
		}
		return this;
	};

	function disabledCheck(zTree, nodes, selectmode) {
		for (var n = 0; n < nodes.length; n++) {
			if (selectmode == 'child') {
				if (nodes[n].isParent) {
					zTree.setChkDisabled(nodes[n], true);
					disabledCheck(zTree, nodes[n].children, selectmode);
				}
			} else if (selectmode == 'parent') {
				if (!nodes[n].isParent) {
					zTree.setChkDisabled(nodes[n], true);
				} else {
					disabledCheck(zTree, nodes[n].children, selectmode);
				}
			}
		}
	}

	$.fn.treeselect.defaultopts = {
		width : 180,// 默认为自适应210   180
		height : 30,// 默认设置line-height为30 height为32
		placeholder:"请选择",
		url : '',// 返回json数据的超链接
		selectmode : 'child', // child:只能选子节点,parent:只能选父节点,all:所有
		extendall : false, // 是否展开所有节点
		zindex : 3000, // zindex,
		chkboxType : {
			"Y" : "s",
			"N" : "s"
		},
		idKey : null,
		pIdKey : null,
		value : [],
		searchChildNode : true, // 默认找子集
		searchMod : 'keyup' // 默认是keyup ， 支持keypress 绑定enter事件
	// 默认值
	};

})(jQuery);

