;
(function($) {
	$.fn.scrollbar = function(options) {
		var $target = $(this);
		if (!$target.data('bind')) {
			$target.css('position', 'relative');
			$target.addClass('jquery-scrollbar-container');
			var $scrollbar = $('<div class="jquery-scrollbar"></div>')
			var $scroller = $('<div class="jquery-scroller"></div>')
			$scrollbar.append($scroller);
			$target.append($scrollbar);
			var runing = false;
			var step = 15;
			$target.bind('mousewheel DOMMouseScroll', function(event, delta, deltaX, deltaY) {
				var container = $(this);
				event.stopPropagation();
				event.preventDefault();
				if(!delta){
					delta = -event.originalEvent.wheelDelta|| event.originalEvent.detail;
				}
				if (container[0].scrollHeight > container.innerHeight()) {
					if (!runing) {
						if (delta >0 && container.scrollTop() != 0) {
							runing = true;
							container.scrollTop(Math.max(0, container.scrollTop() - step*delta));
							runing = false
						} else if (delta <0 && (container[0].scrollHeight - container.innerHeight()) > container.scrollTop()) {
							runing = true;
							container.scrollTop(Math.min(container.scrollTop() - step*delta, container[0].scrollHeight - container.innerHeight()));
							runing = false
						}
					}
				}
				//修改当滚动时input的自动填充导致的问题 如果是input让其失去焦点
				var $inputfocus = $(".jquery-scrollbar-container input:focus");
				if($inputfocus.length>0){
					$inputfocus[0].blur();
				}
				
				if (options && options.callback) {
					options.callback.apply(this);
				}
			});
			$target.on('scroll mouseenter', function(e) {
				if ($(this).css('overflow-y') == 'hidden') {
					var container = $(this);
					var scrollHeight = container[0].scrollHeight;
					var scrollTop = container[0].scrollTop;
					var containerHeight = container.height();
					if (scrollHeight > containerHeight) {
						var st = scrollTop + 5;
						var sh = containerHeight - 10;
						$scrollbar.show();
						$scrollbar.css({
							'top' : scrollTop + 'px',
							'height' : containerHeight + 'px'
						});
						var barHeight = sh * containerHeight / scrollHeight;
						var barTop = st * containerHeight / scrollHeight;
						var newBarHeight = Math.max(20, barHeight);
						if (newBarHeight > barHeight) {
							$scroller.data('diff', newBarHeight - barHeight);
							barTop = Math.max(0, barTop - (newBarHeight - barHeight));
						} else {
							$scroller.data('diff', 0);
						}
						$scroller.css({
							'top' : barTop + 'px',
							'height' : Math.max(20, barHeight) + 'px'
						});
						//step = Math.min(Math.max(100,scrollHeight*80/containerHeight),0.5*containerHeight);
					}else{
						$scrollbar.hide();
					}
				}else{
					$scrollbar.hide();
				}
			});

			$scroller.on('mousedown', function(e) {
				if (!$(this).data('draging')) {
					$(this).data('draging', true);
					$(this).data('mouseY', e.clientY);
					$(this).data('oldTop', $(this).position().top);
					$(this).data('oldTargetTop', $target[0].scrollTop);
					$scrollbar.addClass('active');
				}
			});

			$(document).on('mouseup', function(e) {
				$scroller.data('draging', false);
				$scrollbar.removeClass('active');
			});

			$(document).on('mousemove', function(e) {
				e.stopPropagation();
				if ($scroller.data('draging')) {
					e.preventDefault();
					var oldMouseY = $scroller.data('mouseY');
					var offset = e.clientY - oldMouseY;
					var oldTop = $scroller.data('oldTop');
					var oldTargetTop = $scroller.data('oldTargetTop');
					var scrollHeight = $target[0].scrollHeight;
					var containerHeight = $target.height();
					var diff = $scroller.data('diff');
					if (oldTop + offset >= 0 && oldTop + offset + $scroller.outerHeight() <= $scrollbar.height()) {
						$scroller.css('top', (oldTop + offset - diff) + 'px');
						$target[0].scrollTop = (oldTop + offset) * scrollHeight / containerHeight;
					} else if (offset < 0) {// up
						$scroller.css('top', '0px');
						$target[0].scrollTop = 0;
					} else {// down
						$scroller.css('top', ($scrollbar.height() - $scroller.outerHeight()) + 'px');
						$target[0].scrollTop = ($scrollbar.height() - ($scroller.outerHeight() - diff)) * scrollHeight / containerHeight;
					}
					$scrollbar.addClass('active');
				}
			});

			$(window).resize(function() {
				var container = $target;
				if (!container) {
					return false;
				}
				var scrollHeight = container[0].scrollHeight;
				var scrollTop = container[0].scrollTop;
				var containerHeight = container.height();
				if (scrollHeight > containerHeight) {
					$scrollbar.show();
					$scrollbar.css({
						'top' : scrollTop + 'px',
						'height' : containerHeight + 'px'
					});
					var barHeight = containerHeight * containerHeight / scrollHeight;
					var barTop = scrollTop * containerHeight / scrollHeight;
					$scroller.css({
						'top' : barTop + 'px',
						'height' : barHeight + 'px'
					});
					step = Math.max(100,containerHeight*100/scrollHeight);
				} else {
					$scrollbar.hide();
/*					if ($scroller.is(':visible')) {
						$scroller.fadeOut();
					}*/
				}
			}); 
			$target.find('.dropdown-toggle').on('click', function() {
				var container = $target;
				if (!container) {
					return false;
				}
				var scrollHeight = container[0].scrollHeight;
				var scrollTop = container[0].scrollTop;
				var containerHeight = container.height();
				if (scrollHeight > containerHeight) {
					$scrollbar.css({
						'top' : scrollTop + 'px',
						'height' : containerHeight + 'px'
					});
					var barHeight = containerHeight * containerHeight / scrollHeight;
					var barTop = scrollTop * containerHeight / scrollHeight;
					$scroller.css({
						'top' : barTop + 'px',
						'height' : barHeight + 'px'
					});
				} else {
/*					if ($scroller.is(':visible')) {
						$scroller.fadeOut();
					}*/
				}
			});
			$target.data('bind', true).attr('data-bind', true);
		}
	};

	$.fn.scrollbarX = function(options) {
		var $target = $(this);
		if (!$target.data('bind')) {
			$target.css('position', 'relative');
			$target.addClass('jquery-scrollbar-container');
			var $scrollbar = $('<div class="jquery-scrollbar-x"></div>')
			var $scroller = $('<div class="jquery-scroller-x"></div>')
			$scrollbar.append($scroller);
			$target.append($scrollbar);

			var runing = false;
			$target.bind('mousewheel', function(event, delta, deltaX, deltaY) {
				var container = $(this);
				if (container[0].scrollWidth > container.innerWidth()) {
					if (!runing) {
						//console.log('sw:' + container[0].scrollWidth + ',iw:' + container.innerWidth() + ',sl:' + container.scrollLeft())
						if (delta == 1 && container.scrollLeft() != 0) {
							runing = true;
							container.animate({
								scrollLeft : Math.max(0, container.scrollLeft() - 100)
							}, 200, function() {
								runing = false
							});
						} else if (delta == -1 && (container[0].scrollWidth - container.innerWidth()) > container.scrollLeft()) {
							runing = true;
							container.animate({
								scrollLeft : Math.min(container.scrollLeft() + 100, container[0].scrollWidth - container.innerWidth())
							}, 200, function() {
								runing = false
							});
						}
					}
					event.preventDefault();
				}

			});

			$target.on('scroll mouseenter', function(e) {
				if ($(this).css('overflow-x') == 'hidden') {
					var container = $(this);
					var scrollWidth = container[0].scrollWidth;
					var scrollLeft = container[0].scrollLeft;
					var containerWidth = container.outerWidth();
					if (scrollWidth > containerWidth) {
						$scrollbar.css({
							'left' : scrollLeft + 'px',
							'width' : containerWidth + 'px'
						});
						$scrollbar.show();
						var barWidth = containerWidth * containerWidth / scrollWidth;
						var barLeft = scrollLeft * containerWidth / scrollWidth;
						$scroller.css({
							'left' : barLeft + 'px',
							'width' : barWidth + 'px'
						});
					}else{
						$scrollbar.hide();
					}
				}
			});

			$scroller.on('mousedown', function(e) {
				if (!$(this).data('draging')) {
					$(this).data('draging', true);
					$(this).data('mouseX', e.clientX);
					$(this).data('oldLeft', $(this).position().left);
					$(this).data('oldTargetLeft', $target[0].scrollLeft);
					$scrollbar.addClass('active');
				}
			});

			$(document).on('mouseup', function(e) {
				$scroller.data('draging', false);
				$scrollbar.removeClass('active');
			});

			$(document).on('mousemove', function(e) {
				e.stopPropagation();
				if ($scroller.data('draging')) {
					var oldMouseX = $scroller.data('mouseX');
					var offset = e.clientX - oldMouseX;
					var oldLeft = $scroller.data('oldLeft');
					var oldTargetLeft = $scroller.data('oldTargetLeft');
					var scrollWidth = $target[0].scrollWidth;
					var containerWidth = $target.width();
					if (oldLeft + offset >= 0 && oldLeft + offset + $scroller.outerWidth() <= $scrollbar.width()) {
						$scroller.css('left', (oldLeft + offset) + 'px');
						$target[0].scrollLeft = (oldLeft + offset) * scrollWidth / containerWidth;
					} else if (offset < 0) {// up
						$scroller.css('left', '0px');
						$target[0].scrollLeft = 0;
					} else {// down
						$scroller.css('left', ($scrollbar.width() - $scroller.outerWidth()) + 'px');
						$target[0].scrollLeft = ($scrollbar.width() - $scroller.outerWidth()) * scrollWidth / containerWidth;
					}
				}
			});

			$(window).resize(function() {
				var container = $target;
				var scrollWidth = container[0].scrollWidth;
				var scrollLeft = container[0].scrollLeft;
				var containerWidth = container.width();
				if (scrollWidth > containerWidth) {
					$scrollbar.css({
						'left' : scrollLeft + 'px',
						'width' : containerWidth + 'px'
					});
					var barWidth = containerWidth * containerWidth / scrollWidth;
					var barLeft = scrollLeft * containerWidth / scrollWidth;
					$scroller.css({
						'left' : barLeft + 'px',
						'width' : barWidth + 'px'
					});
				} else {
					if ($scroller.is(':visible')) {
						$scroller.fadeOut();
					}
				}
			});
			$target.data('bind', true).attr('data-bind', true);
		}
	};

	$(function() {
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('div[plugin-scrollbar]').each(function() {
				var item = $(this);
				if (!item.data('bind')) {
					item.scrollbar();
				}
			});
		});

		$('div[plugin-scrollbar]').each(function() {
			var item = $(this);
			if (!item.data('bind')) {
				item.scrollbar();
			}
		});

		$(document).bind('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('div[plugin-scrollbar]').each(function() {
				var item = $(this);
				if (!item.data('bind')) {
					item.scrollbar();
				}
			});
		});

		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('div[plugin-scrollbarX]').each(function() {
				var item = $(this);
				if (!item.data('bind')) {
					item.scrollbarX();
				}
			});
		});

		$('div[plugin-scrollbarX]').each(function() {
			var item = $(this);
			if (!item.data('bind')) {
				item.scrollbarX();
			}
		});

		$(document).bind('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('div[plugin-scrollbarX]').each(function() {
				var item = $(this);
				if (!item.data('bind')) {
					item.scrollbarX();
				}
			});
		});
	});

})(jQuery);