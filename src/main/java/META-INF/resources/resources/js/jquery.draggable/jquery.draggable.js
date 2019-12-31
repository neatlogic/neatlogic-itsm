;
(function($) {
	var draggable = function(delegation, options) {
		var that = this;
		this.$delegation = $(delegation);
		this.isDrag = false;
		this.deltaX = 0;
		this.deltaY = 0;
		this.config = options || {};
		this.cloneTarget = that.config.target ? $(that.config.target).clone() : that.$delegation.clone();
		this.$delegation.on('mousedown', function(e) {
			if (!that.isDrag) {
				that.isDrag = true;
				that.deltaX = e.clientX + $(window).scrollLeft();
				that.deltaY = e.clientY + $(window).scrollTop();
				that.cloneTarget.css({
					'position' : 'absolute',
					'top' : (that.deltaY - that.cloneTarget.height() / 2) + 'px',
					'left' : (that.deltaX - that.cloneTarget.width() / 2) + 'px',
					'z-index' : 999,
					'opacity' : 0.6
				});
				$('body').append(that.cloneTarget);

				$(document).bind('mousemove', function(e) {
					if (that.isDrag) {
						if (!e) {
							e = window.event;
						}
						var nx = e.clientX + $(window).scrollLeft() - that.cloneTarget.outerWidth() / 2;
						var ny = e.clientY + $(window).scrollTop() - that.cloneTarget.outerHeight() / 2;
						that.cloneTarget.css({
							'top' : ny + 'px',
							'left' : nx + 'px'
						});
					}
				});

				$(document).bind('mouseup', function(e) {
					if (that.isDrag) {
						that.isDrag = false;
						that.cloneTarget.remove();
						$(document).unbind('mousemove').unbind('mouseup');
						var isDrop = false;
						if (that.config.dropHolder) {
							var holder = that.config.dropHolder;
							var top = holder.offset().top;
							var bottom = top + holder.height();
							var left = holder.offset().left;
							var right = left + holder.width();
							if (e.clientX + $(window).scrollLeft() >= left && e.clientX + $(window).scrollLeft() <= right && e.clientY + $(window).scrollTop() >= top && e.clientY + $(window).scrollTop() <= bottom) {
								isDrop = true;
							}
						} else {
							isDrop = true;
						}
						if (isDrop) {
							if (that.config.onDrop) {
								that.config.onDrop(e.clientX + $(window).scrollLeft(), e.clientY + $(window).scrollTop());
							}
						}
					}
				});
			}
		});
	}

	this.draggable = draggable;

	$.fn.draggable = function(options) {
		var $delegation = $(this);
		if (!$delegation.data('bind-draggable')) {
			var c = new draggable($delegation, options);
			$delegation.attr('bind', true);
		}
		return this;
	};

})(jQuery);