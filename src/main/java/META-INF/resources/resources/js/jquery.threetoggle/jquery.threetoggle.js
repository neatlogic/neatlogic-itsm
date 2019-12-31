;
(function($) {
	var defaultoptions = {
		blockwidth : 'auto',
		blockmargin : 20,
		left : {
			text : '<i class="glyphicon glyphicon-ok"></i>',
			value : '1',
			style : {
				color : '#fff',
				backgroundColor : '#5CB85C'
			}
		},
		mid : {
			text : '<i class="glyphicon glyphicon-unchecked"></i>',
			value : '',
			style : {
				color : '#444',
				backgroundColor : '#ddd'
			}
		},
		right : {
			text : '<i class="glyphicon glyphicon-remove"></i>',
			value : '0',
			style : {
				color : '#fff',
				backgroundColor : '#D9534F'
			}
		}
	};

	var threetoggle = function(target, options) {
		this.config = $.extend(true, {}, defaultoptions, options);
		this.$target = $(target);
		this.$target.wrap('<div class="threetoggle-main"></div>')
		this.$container = this.$target.closest('.threetoggle-main');
		this.$left = $('<div class="threetoggle-left"></div>');
		this.$mid = $('<div class="threetoggle-mid"></div>');
		this.$mid.html(this.config.mid.text);
		if (this.$target.hasClass('btn-xs')) {
			this.$container.addClass('xs');
			this.$mid.addClass('xs');
		}
		if (this.config.mid.style) {
			this.$mid.css(this.config.mid.style);
		}
		if (this.config.blockwidth && !isNaN(this.config.blockwidth)) {
			this.$mid.css('width', this.config.blockwidth + 'px');
		}
		this.$right = $('<div class="threetoggle-right"></div>');
		this.$container.append(this.$mid).append(this.$left).append(this.$right);
		this.$mid.css('left', this.config.blockmargin + 'px');
		
		// this.$mid.css('height', (this.$mid.outerHeight() + 2) + 'px');
		setTimeout((function(that){
			return function(){
				that.$container.width(that.$mid.outerWidth() + that.config.blockmargin * 2);
				that.$mid.css('width', that.$mid.outerWidth() + 'px');
				that.$mid.css('overflow', 'hidden');
			}
		}(this)),100);
		
		this.state = 'mid';
		this.initEvent();
		if (this.$target.val() == this.config.left.value) {
			this.$left.trigger('click');
		} else if (this.$target.val() == this.config.right.value) {
			this.$right.trigger('click');
		}
		/*
		 * if (this.config.left.active) { this.$left.trigger('click'); } else if
		 * (this.config.right.active) { this.$right.trigger('click'); }
		 */

		this.$target.attr('data-bind', true);
	}

	this.threetoggle = threetoggle;
	threetoggle.prototype = {
		initEvent : function() {
			var that = this;
			this.$left.on('click', function() {
				if (!that.isRunning && that.state != 'left') {
					that.isRunning = true;
					var newstyle = that.config.left.style || {};
					newstyle.left = '0px';
					that.$mid.animate(newstyle, 300, function() {
						that.isRunning = false;
						that.$mid.html(that.config.left.text);
						that.$target.val(that.config.left.value);
						that.$target.trigger('change');
						that.state = 'left';
					});
				}
			});
			this.$right.on('click', function() {
				if (!that.isRunning && that.state != 'right') {
					that.isRunning = true;
					var newstyle = that.config.right.style || {};
					newstyle.left = that.config.blockmargin * 2 + 'px';
					that.$mid.animate(newstyle, 300, function() {
						that.isRunning = false;
						that.$mid.html(that.config.right.text);
						that.$target.val(that.config.right.value);
						that.$target.trigger('change');
						that.state = 'right';
					});
				}
			});
			this.$mid.on('click', function() {
				if (!that.isRunning && that.state != 'mid') {
					that.isRunning = true;
					var newstyle = that.config.mid.style || {};
					newstyle.left = that.config.blockmargin + 'px';
					that.$mid.animate(newstyle, 300, function() {
						that.isRunning = false;
						that.$mid.html(that.config.mid.text);
						that.$target.val(that.config.mid.value);
						that.$target.trigger('change');
						that.state = 'mid';
					});
				}
			});
		}
	};

	$.fn.threetoggle = function(options) {
		var $target = $(this);
		if (!$target.data('bind')) {
			new threetoggle($target, options);
		}
		return this;
	};

	$(function() {
		$('input[plugin-threetoggle]').each(function() {
			var config = {
				left : {},
				right : {},
				mid : {}
			};
			if ($(this).attr('blockwidth')) {
				config.blockwidth = $(this).attr('blockwidth');
			}
			if ($(this).attr('blockmargin')) {
				config.blockmargin = $(this).attr('blockmargin');
			}
			if ($(this).attr('left-text')) {
				config.left.text = $(this).attr('left-text');
			}
			if ($(this).attr('left-value')) {
				config.left.value = $(this).attr('left-value');
			}
			if ($(this).attr('left-active')) {
				config.left.active = $(this).attr('left-active');
			}
			if ($(this).attr('right-text')) {
				config.right.text = $(this).attr('right-text');
			}
			if ($(this).attr('right-value')) {
				config.right.value = $(this).attr('right-value');
			}
			if ($(this).attr('right-active')) {
				config.right.active = $(this).attr('right-active');
			}
			if ($(this).attr('mid-text')) {
				config.mid.text = $(this).attr('mid-text');
			}
			if ($(this).attr('mid-value')) {
				config.mid.value = $(this).attr('mid-value');
			}
			if ($(this).attr('mid-active')) {
				config.mid.active = $(this).attr('mid-active');
			}
			$(this).threetoggle(config);
		});
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('input[plugin-threetoggle]').each(function() {
				var config = {
					left : {},
					right : {},
					mid : {}
				};
				if ($(this).attr('blockwidth')) {
					config.blockwidth = $(this).attr('blockwidth');
				}
				if ($(this).attr('blockmargin')) {
					config.blockmargin = $(this).attr('blockmargin');
				}
				if ($(this).attr('left-text')) {
					config.left.text = $(this).attr('left-text');
				}
				if ($(this).attr('left-value')) {
					config.left.value = $(this).attr('left-value');
				}
				if ($(this).attr('left-active')) {
					config.left.active = $(this).attr('left-active');
				}
				if ($(this).attr('right-text')) {
					config.right.text = $(this).attr('right-text');
				}
				if ($(this).attr('right-value')) {
					config.right.value = $(this).attr('right-value');
				}
				if ($(this).attr('right-active')) {
					config.right.active = $(this).attr('right-active');
				}
				if ($(this).attr('mid-text')) {
					config.mid.text = $(this).attr('mid-text');
				}
				if ($(this).attr('mid-value')) {
					config.mid.value = $(this).attr('mid-value');
				}
				if ($(this).attr('mid-active')) {
					config.mid.active = $(this).attr('mid-active');
				}
				$(this).threetoggle(config);
			});
		});

		$(document).on('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('input[plugin-threetoggle]').each(function() {
				var config = {
					left : {},
					right : {},
					mid : {}
				};
				if ($(this).attr('blockwidth')) {
					config.blockwidth = $(this).attr('blockwidth');
				}
				if ($(this).attr('blockmargin')) {
					config.blockmargin = $(this).attr('blockmargin');
				}
				if ($(this).attr('left-text')) {
					config.left.text = $(this).attr('left-text');
				}
				if ($(this).attr('left-value')) {
					config.left.value = $(this).attr('left-value');
				}
				if ($(this).attr('left-active')) {
					config.left.active = $(this).attr('left-active');
				}
				if ($(this).attr('right-text')) {
					config.right.text = $(this).attr('right-text');
				}
				if ($(this).attr('right-value')) {
					config.right.value = $(this).attr('right-value');
				}
				if ($(this).attr('right-active')) {
					config.right.active = $(this).attr('right-active');
				}
				if ($(this).attr('mid-text')) {
					config.mid.text = $(this).attr('mid-text');
				}
				if ($(this).attr('mid-value')) {
					config.mid.value = $(this).attr('mid-value');
				}
				if ($(this).attr('mid-active')) {
					config.mid.active = $(this).attr('mid-active');
				}
				$(this).threetoggle(config);
			});
		});


	});
})(jQuery);