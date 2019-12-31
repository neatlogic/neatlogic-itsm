;
(function($) {
	var defaultoptions = {
		min : 1,
		max : 10,
		step : 1,
		width : 200,
		blockWidth : 15,
		height : 13,
		value : null,
		onSlide : null,
		onChange : null
	};

	var slidebar = function(target, options) {
		var that = this;
		that.$target = $(target);
		that.config = $.extend(true, {}, defaultoptions, options);
		that.$target.wrap('<div class="jquery-slidebar-container"></div>');
		that.$container = this.$target.closest('.jquery-slidebar-container');
		that.$container.css({
			'width' : that.config.width,
			'height' : that.config.height
		});
		that.$slidebar = $('<div class="jquery-slidebar"></div>');
		that.$slideblock = $('<div class="jquery-slideblock"></div>');
		that.$slideover = $('<div class="jquery-slideover"></div>');
		that.$slideblock.css({
			'width' : that.config.blockWidth,
		});
		that.$container.append(that.$slidebar);
		that.$container.append(that.$slideblock);
		that.$container.append(that.$slideover);
		that.isDrag = false;
		that.startX = 0;
		var dotIndex = that.config.step.toString().indexOf('.');
		if (dotIndex > -1) {
			that.fixed = that.config.step.toString().substring(dotIndex + 1).length;
		} else {
			that.fixed = 0;
		}
		that.currentValue = that.config.value || that.config.min;
		if (that.currentValue < that.config.min) {
			that.currentValue = that.config.min;
		} else if (that.currentValue > that.config.max) {
			that.currentValue = that.config.max;
		}
		var rate = (that.config.width - that.config.blockWidth) / (that.config.max - that.config.min);
		var left = (that.currentValue - that.config.min) * rate;
		that.$slideblock.css('left', left + 'px');
		that.$slideover.css("width", left + 'px');

		that.$slideblock.on('mousedown', function(e) {
			that.isDrag = true;
			that.startX = e.originalEvent.x;
			that.oldValue = that.currentValue;
		});

		$(document).on('mousemove', function(e) {
			if (that.isDrag) {
				var left = e.originalEvent.x - that.$container.offset().left - that.config.blockWidth / 2;
				if (left < 0) {
					left = 0;
				}
				if (left > that.config.width - that.config.blockWidth) {
					left = that.config.width - that.config.blockWidth;
				}

				if (that.config.step) {
					var width = that.config.width - that.config.blockWidth;
					var stepWidth = width / (that.config.max - that.config.min) * that.config.step;
					left = parseInt(left / stepWidth, 10) * stepWidth;
				}
				var rate = (that.config.width - that.config.blockWidth) / (that.config.max - that.config.min);
				that.currentValue = (that.config.min + left / rate).toFixed(that.fixed);

				that.$slideblock.css('left', left + 'px');
				that.$slideover.css("width", left + 'px');
				if (that.oldValue != that.currentValue) {
					if (that.config.onChange) {
						that.config.onChange(that.currentValue);
					}
					that.oldValue = that.currentValue;
				}
			}
		});

		$(document).on('mouseup', function() {
			that.isDrag = false;
		});
	}

	this.slidebar = slidebar;
	slidebar.prototype = {
		init : function() {

		}
	};

	$.fn.slidebar = function(options) {
		var $target = $(this);
		if (!$target.data('bind') && $target.attr('type') && $target.attr('type').toLowerCase() == 'hidden') {
			new slidebar($target, options);
		}
		return this;
	};

})(jQuery);