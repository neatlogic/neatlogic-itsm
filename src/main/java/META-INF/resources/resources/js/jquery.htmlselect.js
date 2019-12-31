(function($) {

	$.fn.htmlselect = function(options) {
		var config = $.extend(true, {}, $.fn.htmlselect.defaultopts, options);
		var $target = $(this);
		if (!$target.data('bind')) {
			$target.wrap('<div style="display:inline-block"></div>');
			var isSingle = $target.attr('multiple') ? false : true;
			var width = $target.outerWidth(true);
			var $selectoption = null;
			var $selector = $('<span class="htmlselect-selector"></span>');
			$selector.width(config.width || width).height(config.height);
			var $downitem = $('<i class="jquery-ztreeselect-up" style="top: 7px; right: 2px; position:absolute"></i>');
			var $valuespan = $('<span class="htmlselect-valuespan"></span>');

			var $optioncontainer = $('<div class="htmlselect-container bottom" style="padding:3px 0px;background:#fff;display:inline-block;text-align:left"></div>');
			if(config.listheight){
				$optioncontainer.css('max-height',config.listheight + 'px');
				$optioncontainer.css('overflow-y','auto');
				$optioncontainer.css('overflow-x','hidden');
			}
			// $optioncontainer.width(config.width || width);
			$optioncontainer.html('<div class="arrow"></div>');

			function init() {
				$optioncontainer.children('.option').remove();
				$optioncontainer.on('blur', function() {
					$(this).slideUp('fast');
				});

				$valuespan.empty();
				$target.children().each(function() {
					if ($(this).prop('selected')) {
						$valuespan.append($(this).text() + ";");
					}
					var icon_url = $(this).attr('icon-url') || '';
					var icon_class = $(this).attr('icon-class') || 'htmlselect-icon-class-default';
					var active_class = $(this).attr('active-class') || 'htmlselect-active-class-default';
					var hover_class = $(this).attr('hover-class') || 'htmlselect-hover-class-default';
					var select_class = $(this).attr('select-class') || 'htmlselect-select-class-default';
					var opt = $(this);
					var color = opt.css('color');
					var $newopt = $('<div style="margin:0px 3px" class="option"></div>');
					$newopt.data('value', opt.val());
					$newopt.data('text', opt.text());
					if (icon_url == '') {
						$newopt.text(opt.text());
					} else {
						var $iconspn = $('<span style="display:inline-block"><img class="' + icon_class + '" src="' + icon_url + '"></span>');
						var $txtspn = $('<span style="margin-left:5px;display:inline-block">' + opt.text() + '</span>');
						$newopt.append($iconspn).append($txtspn);
					}
					$newopt.attr('icon-url', icon_url);
					$newopt.attr('icon-class', icon_class);
					$newopt.attr('active-class', active_class);
					$newopt.attr('hover-class', hover_class);
					$newopt.attr('select-class', select_class);
					if ($(this).prop('selected')) {
						$newopt.addClass(select_class);
					} else {
						$newopt.addClass(active_class);
					}
					$newopt.on('mouseover', function() {
						$(this).addClass(hover_class).removeClass(active_class);
					});
					$newopt.on('mouseout', function() {
						$(this).addClass(active_class).removeClass(hover_class);
					});
					$newopt.on('click', function() {
						if (isSingle) {
							if ($target.val() != $(this).data('value')) {
								$optioncontainer.find('.' + select_class).removeClass(select_class).addClass(active_class);
								$(this).addClass(select_class).removeClass(hover_class).removeClass(active_class);
								$target.val($(this).data('value')).trigger('change');
								$valuespan.text($(this).text());
								$optioncontainer.slideUp('fast');
								if (config.change) {
									config.change();
								}
							} else {
								$optioncontainer.slideUp('fast');
							}
						} else {
							if ($(this).hasClass(select_class)) {
								$(this).removeClass(select_class).addClass(active_class);
								var optitem = $(this);
								var t = $valuespan.text();
								$target.children().each(function() {
									if ($(this).val() == optitem.data('value')) {
										$(this).prop('selected', false);
										t = t.replace(new RegExp(optitem.data('text') + ";", "mg"), "");
									}
								});
								$valuespan.text(t);
							} else {
								$(this).removeClass(active_class).addClass(select_class);
								var optitem = $(this);
								$target.children().each(function() {
									if ($(this).val() == optitem.data('value')) {
										$(this).prop('selected', true);
									}
								});
								var t = $valuespan.text();
								t += optitem.data('text') + ";";
								$valuespan.text(t);
							}
							if (config.change) {
								config.change();
							}
						}
					});

					$optioncontainer.append($newopt);
				});
			}

			init();

			$target.bind('htmlselect:reload', function() {
				init();
			});

			$target.bind('htmlselect:destory', function() {
				if ($target.data('bind')) {
					if ($target.data('selector')) {
						$target.data('selector').remove();
						$target.data('selector', null);
					}
					if ($target.data('optioncontainer')) {
						$target.data('optioncontainer').remove();
						$target.data('optioncontainer', null);
					}
					$target.data('bind', null);
					$target.removeAttr('data-bind');
					$target.unwrap();
					$target.show();
				}
			});

			$target.hide();

			// if (isSingle) {
			$optioncontainer.css({
				'display' : 'none',
				'z-index' : config.zindex
			});
			$selector.append($valuespan).append($downitem);
			$target.after($selector);
			$selector.after($optioncontainer);

			$target.data('selector', $selector);
			$target.data('optioncontainer', $optioncontainer);

			$selector.on('click', function() {
				if ($optioncontainer.is(':visible')) {
					$optioncontainer.slideUp('fast');
				} else {
					var optiontop = $(this).position().top + $(this).outerHeight(true) + 10;
					var optionleft = $(this).position().left;
					$optioncontainer.css({
						'top' : optiontop + 'px',
						'left' : optionleft + 'px'
					});
					// $(this).width($optioncontainer.width());
					$optioncontainer.slideDown('fast');
				}
			});
			/*
			 * } else { $optioncontainer.css({ 'height' : height + 'px',
			 * 'overflow' : 'auto', 'border' : '1px solid #ccc', 'border-radius' :
			 * '4px' }); $target.after($optioncontainer);
			 * $target.data('optioncontainer', $optioncontainer); }
			 */
			$target.data('bind', true);
			$target.attr('data-bind', true);
		}
		return this;
	};

	$.fn.htmlselect.defaultopts = {
		width : 210,
		height : 30,
		listheight:null,
		zindex : 5000,
		change : null
	};

	$(function() {
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('select[plugin-htmlselect]').each(function() {
				if (!$(this).data('bind')) {
					$(this).htmlselect();
				}
			});
		});

		$('select[plugin-htmlselect]').each(function() {
			if (!$(this).data('bind')) {
				$(this).htmlselect();
			}
		});

		$(document).bind('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('select[plugin-htmlselect]').each(function() {
				if (!$(this).data('bind')) {
					$(this).htmlselect();
				}
			});
		});
	});

})(jQuery);
