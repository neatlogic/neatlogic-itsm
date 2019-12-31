(function($) {
	var $container = $('<div class="J-relation J-relation-small batcher-container" id="J-relation-div">' + '<div class="J-relation-item"><div class="baseBg J-L-ico J-relation-pos"></div>' + '<div class="J-relation-main">' + '<div class="J-relation-title">' + '<a href="#" title="" style="white-space:nowrap" class="J-go"><i class="pic-icon-small pic-icon-batch"></i>批量选择池<span style="padding:2px" class="label label-danger J-relation-num">0</span></a>' + '</div>' + '<div class="baseBg J-relation-mx"></div>' + '<div class="J-relation-px"></div>' + '<div class="J-relation-body">' + '<div class="J-relation-buy"><span>最多处理<strong>20</strong>条数据</span><i style="cursor:pointer" title="清空" class="btnClear icon-trash"></i></div>' + '<div style="text-align:right;width:100%;" class="handlerContainer"></div>' + '</div>' + '</div>' + '<div class="baseBg J-R-ico J-relation-pos"></div>' + '</div>' + '</div>');
	var $handlerContainer = $container.find('.handlerContainer');
	var $itemContainer = $container.find('.J-relation-body');
	var $counter = $container.find('.J-relation-num');
	var itemcontainer = '<div class="J-relation-list" data-id="#{id}"><div class="J-relation-list-a"></div><div class="baseBg J-relation-close"></div></div>';
	var $title = $container.find('.J-relation-title');
	var $form = $('<form method="POST"><input type="hidden" name="batchparam"></form>');
	var $clearer = $container.find('.btnClear');
	var opts = {};
	var $target = null;
	$.fn.batcher = function(options) {
		$target = $(this);
		opts = $.extend(true, {}, $.fn.batcher.defaultopts, options);

		var cookie_value = $.cookie(opts.cookiename);
		if (cookie_value) {
			cookie_value = JSON.parse(cookie_value, null);
		}

		if ($target.find('.batcher-container').length <= 0) {
			$target.append($container).append($form);
			// 初始化數據開始
			$.cookie(opts.cookiename, '', {
				path : '/'
			})
			if (cookie_value && typeof cookie_value == 'object' && !$.isEmptyObject(cookie_value)) {
				for ( var c in cookie_value) {
					addItem(cookie_value[c]);
				}
			}
			// 初始化數據結束

			// 初始化按钮开始
			if (opts.handlers && opts.handlers.length > 0) {
				var $btncontainer = $('<div class="btn-group"></div>');
				for ( var h in opts.handlers) {
					var $handler = $('<button type="button" class="' + opts.handlers[h].classname + '">' + opts.handlers[h].text + '</button>');
					$handler.data('url', opts.handlers[h].url);
					$handler.on('click', function(e) {
						e.stopPropagation();
						if ($.cookie(opts.cookiename)) {
							$form.find('input').val($.cookie(opts.cookiename));
							$form.attr('action', $(this).data('url')).submit();
						}
					});
					$btncontainer.prepend($handler);
				}
				$handlerContainer.prepend($btncontainer);
			}
			// 初始化按钮结束

			$title.on('click', function(e) {
				e.stopPropagation();
				var length = $container.find('.J-relation-list').length;
				if (length > 0) {
					if (!$container.hasClass('J-relation-small')) {
						$itemContainer.slideToggle();
					} else {
						$container.find('.J-relation-mx').hide();
						$container.find('.J-relation-px').show();
						$container.animate({
							'width' : 289
						}, 100, function() {
							$container.removeClass('J-relation-small');
							$itemContainer.slideDown();
						});
					}
				}
			});

			$clearer.on('click', function(e) {
				e.stopPropagation();
				$container.find('.J-relation-list').remove();
				$.cookie(opts.cookiename, '', {
					path : '/'
				})
				$counter.text(0);
			});

			$(document).on('click', function() {
				$container.find('.J-relation-px,.J-relation-body').hide();
				$container.find('.J-relation-mx').show();
				$container.animate({
					'width' : 119
				}, 100, function() {
					$container.addClass('J-relation-small');
				});
			});
		}
	};

	var addItem = function(json) {
		var isExists = false;
		var cookie = $.cookie(opts.cookiename);
		var l = null;
		if (cookie && cookie != '') {
			l = JSON.parse(cookie, null);
			for ( var i = 0; i < l.length; i++) {
				if (JSON.stringify(l[i]) == JSON.stringify(json)) {
					showPopMsg.info('请勿重复添加。');
					isExists = true;
				}
			}
		} else {
			l = new Array();
		}
		if (!isExists) {
			var template = opts.template;
			for ( var v in json) {
				template = template.replace(new RegExp('\#\{' + v + '\}', "ig"), json[v]);
			}
			var $itemcontainer = $(itemcontainer);
			$itemcontainer.find('.J-relation-list-a').html(template);
			$itemcontainer.find('.J-relation-close').on('click', function(e) {
				e.stopPropagation();
				$(this).closest('.J-relation-list').remove();
				var c = $.cookie(opts.cookiename);
				var j = json;
				if (c && c != '') {
					var cc = JSON.parse(c, null);
					for ( var i = 0; i < cc.length; i++) {
						if (JSON.stringify(cc[i]) == JSON.stringify(j)) {
							cc.splice(i, 1);
							break;
						}
					}
				}
				$.cookie(opts.cookiename, JSON.stringify(cc), {
					path : '/'
				});
				var count = parseInt($counter.text(), 10) - 1;
				$counter.text(count);
			});
			$itemContainer.prepend($itemcontainer);
			var count = parseInt($counter.text(), 10) + 1;
			$counter.text(count);

			l.push(json);
			cookie = JSON.stringify(l);

			$.cookie(opts.cookiename, cookie, {
				path : '/'
			});
		}
	}

	$.fn.batcheritem = function() {
		var $batcheritem = $(this);
		$batcheritem.data('isbatcheritem', true);
		var isFlying = false;
		$batcheritem.on('click', function(e) {
			e.stopPropagation();
			var json = {};
			var html = opts.template;
			$.each($batcheritem[0].attributes, function() {
				if (this.specified && this.name.indexOf(opts.prefix) == 0) {
					json[this.name.replace(opts.prefix, '')] = this.value;
					html = html.replace(new RegExp('\#\{' + this.name.replace(opts.prefix, '') + '\}', "ig"), this.value);
				}
			});
			var x = $batcheritem.offset().left + 30;
			var y = $batcheritem.offset().top + 10;
			var X = $container.offset().left + $container.width() / 2 - $batcheritem.width() / 2 + 10;
			var Y = $container.offset().top;
			if (!isFlying) {
				var $obj = $('<div>' + html + '</div>');
				$obj.css({
					'position' : 'absolute',
					'z-index' : 890
				});
				$('body').append($obj);
				if (!$obj.is(':animated')) {
					$obj.css({
						'left' : x,
						'top' : y
					}).animate({
						'left' : X,
						'top' : Y - 80
					}, 500, function() {
						$obj.stop(false, false).animate({
							'top' : Y - 20
						}, 500, function() {
							$obj.fadeOut(300, function() {
								isFlying = false;
								$obj.remove();
								addItem(json);
							});
						});
					});
				}
			}
		});
	};

	$.fn.batcher.defaultopts = {
		handlers : [ /*{
			text : '<i class=" icon-retweet icon-white"></i>批量关联',
			classname : 'btn btn-success btn-xs',
			url : '/balantflow/relation/toRelation.do'
		}, */{
			text : '<i class=" icon-retweet icon-white"></i>批量操作',
			classname : 'btn btn-warning btn-xs',
			url : '/balantflow/relation/toBatchOptTaskList.do'
		} ],
		template : '<span class="channelType" style="background-color:#{bg}">#{cutType}</span><a href="/balantflow/task/getTaskDetail.do?taskId=#{id}" ><b>[#{id}]#{title}</b></a>',
		zindex : 20,
		prefix : 'batch_',
		cookiename : 'RELATION_VALUE'
	};

	$(function() {

		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('.batcheritem').each(function() {
				if (!$(this).data('isbatcheritem')) {
					$(this).batcheritem();
				}
			});
		});
		$('.batcheritem').each(function() {
			if (!$(this).data('isbatcheritem')) {
				$(this).batcheritem();
			}
		});

	});
})(jQuery);