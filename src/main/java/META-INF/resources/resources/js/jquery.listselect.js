(function($) {
	
	$.fn.listselect = function(options) {
		var html = '<span class="spnSelectedItem" style="position:relative;display:inline-block;' + 'border-right:1px solid #bbb;' + 'border-top:1px solid #ddd; ' + 'border-bottom:1px solid #bbb;' + 'border-left:1px solid #ddd;background:#fcfcfc;border-radius:2px;padding:2px 15px 2px 5px;margin:1px"><i class="delitem jquery-listselect-close" style="position:absolute;top:0px;right:0px"></i></span>';
		var itemclose = '<i class="delitem jquery-listselect-close" style="position:absolute;top:0px;right:0px"></i>';
		var $target = $(this);
		$target.unbind();
		if(typeof $target.data('treeDiv') != 'undefined' && $target.data('treeDiv') != null){
			$target.data('treeDiv').remove();
			$target.data('treeDiv', null);
		};
		$target.html('');
		
		function itemClick(that){
			var $item = $(that);
			var value = $item.data('value');
			var text = $item.text();
			var $hidden = $('<input type="hidden">');
			var h = html;
			var $h = $(h);
			$h.find('.delitem').click(function(event) {
				$(this).parent().remove();
				if ($target.attr('check-type') && $target.find('input[type=hidden]').length <= 0) {
					$target.append('<input class="blankinput" type="text" readonly style="border:0px;width:100%;height:100%" check-type="' + $target.attr('check-type') + '" ' + ($target.attr('required-message') ? ('required-message="' + $target.attr('required-message') + '"') : '') + '>');
				}
				if(opts.callBack != null && opts.callBack  != undefined){
					opts.callBack();
				}
				$target.trigger('sizechange');
				event.stopPropagation();
			});
			if (typeof value != 'undefined') { // 存在value
				if (opts.mode == 'single') {
					$h.append(text);
					$hidden.val(value);
					if ($target.attr('name')) {
						$hidden.attr('name', $target.attr('name'));
					}
					for ( var vv in $item.data()) {
						$hidden.data(vv, $item.data()[vv]);
					}
					$h.append($hidden);
					$target.children('.spnSelectedItem').remove();
					$target.children('.blankinput').remove();
					$target.append($h).trigger('sizechange');
				} else {
					var exists = false;
					$target.find('input[type=hidden]').each(function() {
						if ($(this).val() == value) {
							exists = true;
							return false;
						}
					});
					if (!exists) {
						$h.append(text);
						$hidden.val(value);
						if ($target.attr('name')) {
							$hidden.attr('name', $target.attr('name'));
						}
						for ( var vv in $item.data()) {
							$hidden.data(vv, $item.data()[vv]);
						}
						$h.append($hidden);
						$target.children('.blankinput').remove();
						$target.append($h).trigger('sizechange');
					}
				}
				
				var $div = $target.data('treeDiv');
				$div.hide();
			} else {
				showPopMsg.error('选中的值没有value值。');
			}
			if(opts.callBack != null && opts.callBack  != undefined){
				opts.callBack();
			}
		}

		this.getValue = function() {
			var returnVal = new Array();
			$target.find('input[type=hidden]').each(function(){
				var data = {};
				for(var k in $(this).data()){
					data[k] = $(this).data()[k];
				}
				returnVal.push(data);
			});
			return returnVal;
		}

		$target.css({
			'padding' : '5px',
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

		var opts = $.extend(true, {}, $.fn.listselect.defaultopts, options);
		if (opts.values != null && opts.values.length > 0) {
			for ( var v in opts.values) {
				var vv = opts.values[v];
				var text = vv.text;
				var value = vv.value;
				if (typeof text != 'undefined' && typeof value != 'undefined') {
					var h = html;
					var $h = $(h);
					$h.find('.delitem').click(function(event) {
						$(this).parent().remove();
						if ($target.attr('check-type') && $target.find('input[type=hidden]').length <= 0) {
							$target.append('<input class="blankinput" type="text" readonly style="border:0px;width:100%;height:100%" check-type="' + $target.attr('check-type') + '" ' + ($target.attr('required-message') ? ('required-message="' + $target.attr('required-message') + '"') : '') + '>');
						}
						$target.trigger('sizechange');
						event.stopPropagation();
					});
					var $hidden = $('<input type="hidden">');
					$hidden.val(value);
					$h.append(text);
					if (typeof value != 'undefined') {
						if ($target.attr('name')) {
							$hidden.attr('name', $target.attr('name'));
						}
					}
					for ( var vk in vv) {
						$hidden.data(vk, vv[vk]);
					}
					$h.append($hidden);
					$target.append($h).trigger('sizechange');
				}
			}
		}else if ($target.attr('check-type') && $target.find('input[type=hidden]').length <= 0) {
			$target.append('<input class="blankinput" type="text" readonly style="border:0px;width:100%;height:100%" check-type="' + $target.attr('check-type') + '" ' + ($target.attr('required-message') ? ('required-message="' + $target.attr('required-message') + '"') : '') + '>').trigger('sizechange');
		}

		if (opts.url != '') {

			$target.on('sizechange', function() {
				var $div = $target.data('treeDiv');
				// $afterdiv.height(Math.max(30, $target.outerHeight(true)));
				if ((typeof $div != 'undefined' && $div != null) && $div.is(':visible')) {
					var top = $target.position().top + $target.outerHeight(true) - 1;
					var left = $target.position().left;
					$div.css({
						'top' : top,
						'left' : left
					});
					if ($div.outerWidth(true) > $target.outerWidth(true)) {
						$target.css('width', $div.outerWidth(true));
					} else {
						$div.css('width', $target.outerWidth(true));
					}
				}
			});

			$target.click(function() {
				var $div = $target.data('treeDiv');
				if (typeof $div == 'undefined' || $div == null) {
					$div = $('<div class="treeselect" style="height:auto;background:#fefefe;border-top:0px;border-left:1px solid #ddd;border-right:1px solid #ccc;border-bottom:1px solid #ccc;border-bottom-left-radius:4px;border-bottom-right-radius:4px;display:none;position:absolute;z-index:' + opts.zindex + '"></div>');
					var $searchdiv = $('<div style="padding:3px"></div>');
					var $searcher = $('<input type="text" class="form-control input-sm" style="width:100%;" placeholder="输入关键字">');
					//var $close = $('<div style="text-align:right"><i class="pic-icon-close pic-icon-small" style="cursor:pointer"></i></div>');
					var $itemlist = $('<div></div>');
					var $list = $('<div></div>');
					var top = $target.position().top + $target.outerHeight(true) - 1;
					var left = $target.position().left;

					/*$close.click(function() {
						if ($div.is(':visible')) {
							$div.slideUp();
						}
					});*/

					$div.css({
						'top' : top,
						'left' : left
					});
					
					if(opts.items != null && opts.items.length > 0){
						for(var n in opts.items){
							var it = opts.items[n];
							var $itemdiv = $('<div class="jquery-listselect-listitem"></div>');
							for ( var uu in it) {
								if (uu.toLowerCase() == 'text') {
									$itemdiv.text(it[uu]);
								}
								$itemdiv.data(uu.toLowerCase(), it[uu]);
							}
							$itemdiv.click(function() {
								itemClick(this);
							});
							$itemlist.append($itemdiv);
						}
					}
					$searchdiv.append($searcher);
					$div.append($searchdiv).append($itemlist).append($list);
					$target.after($div);

					$searcher.keyup(function() {
						if ($.trim($(this).val()).length >= opts.minlength) {
							var keyword = $.trim($(this).val());
							$.getJSON(opts.url + '?limit='+ opts.limit +'&k=' + encodeURIComponent(keyword), function(data) {
								$list.html('');
								for ( var u = 0; u < data.length; u++) {
									var user = data[u];
									var $itemdiv = $('<div class="jquery-listselect-listitem"></div>');
									for ( var uu in user) {
										if (uu.toLowerCase() == 'text') {
											$itemdiv.text(user[uu]);
										}
										$itemdiv.data(uu.toLowerCase(), user[uu]);
									}

									$itemdiv.click(function() {
										itemClick(this);
									});
									if (typeof user.category != 'undefined') {
										var $categoryitem = $list.find('.jquery-listselect-categoryitem:contains("'+ user.category+ '")');
										if ($categoryitem.length == 0) {
											var $categorydiv = $('<div class="jquery-listselect-categoryitem"></div>');
											$categorydiv.append(user.category);
											$list.append($categorydiv).append($itemdiv);
										} else {
											$categoryitem.after($itemdiv);
										}
									} else {
										$list.append($itemdiv);
									}
								}
							});
						}
					});
					$div.slideDown('fast', function(){
						$target.trigger('sizechange');
					});
					$target.data('treeDiv', $div);
				} else {
					if ($div.is(':visible')) {
						$div.slideUp();
					} else {
						var top = $target.position().top + $target.outerHeight(true) - 1;
						var left = $target.position().left;
						$div.css({
							'top' : top,
							'left' : left
						});
						$div.slideDown('fast', function(){
							$target.trigger('sizechange');
						});
					}
				}
			});
		}
		return this;
	};
	
	
	$.fn.listselect.defaultopts = {
		url : '',// 返回json数据的超链接
		minlength : 3,// 搜索时最小字符数
		mode : 'single', // single or multi
		zindex : 3000, // zindex,
		limit : 20, //<=0代表没限制
		items: [],//静态选项
		values : [],// 每个元素都应该是包含value和text属性的json
		callBack : null  //回调函数
	};

})(jQuery);