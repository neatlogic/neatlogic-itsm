;
(function($) {
	var titletips = function(target) {
		var that = this;
		that.$target = $(target);
		if (that.$target.attr('data-title') && !that.$target.attr('data-url')) {
			that.$target.addClass('jquery-titletips-allTxt');
			that.showall();
			that.$container.on('mouseenter', function() {
				that.getPos();
			});
		} else if (that.$target.attr('data-url') && that.$target.attr('data-template')) {
			that.$target.addClass('jquery-titletips-detailTxt');
			that.showdetail();
			var timer = null;
			that.$target.on('click', function() {
				that.getdetail();
			});
			that.$container.on('mouseenter', function() {
				clearTimeout(timer);
			});
			that.$container.on('mouseleave', function() {
				if (that.$container.hasClass('onshow')) {
					timer = setTimeout(function() {
						that.$container.removeClass('onshow');
					}, 2000);
				}
			});
		}
		$(window).on('resize', function() {
			that.getPos();
		});
		$(document).scroll(function() {
			that.getPos();
		});

	};
	this.titletips = titletips;
	titletips.prototype = {
		showall : function() {
			var that = this;
			that.$target.wrap('<div class="jquery-titletips-allBox"></div>');
			that.$container = that.$target.closest('.jquery-titletips-allBox');
			var innerHtml = that.$target.data('title');
			var innerWidth = that.$target.data('width');
			if (that.$target.data('skin')) {
				that.$container.addClass('black');
			}
			if (innerHtml) {
				that.$content = $('<div class="jquery-titletips-allCont">' + innerHtml + '</div>');
				if (innerWidth) {
					that.$content.css('width', innerWidth);
				}
				that.$target.closest('.jquery-titletips-allBox').append(that.$content);
			} else {
				return;
			}
		},
		showdetail : function() {
			var that = this;
			that.$target.wrap('<div class="jquery-titletips-detailBox"></div>');
			that.$container = that.$target.closest('.jquery-titletips-detailBox');
			that.$content = $('<div class="jquery-titletips-detailCont"></div>');
			that.$container.append(that.$content);
			that.getPos();
		},
		getdetail : function() {
			var that = this;
			that.url = that.$target.data('url');
			that.template = that.$target.data('template');
			that.title = that.$target.data('title');
			if (that.url && that.template) {
				$.post(that.url, function(data) {
					var html = xdoT.render(that.template, data);
					that.$content.empty();
					if (that.title) {
						that.$content.append('<div class="showmoreTitl"><span>' + that.title + '</span><span class="close"><i class="ts-remove"></i></span></div>');
						that.$content.addClass('hastitl');
					}
					that.$innertxt = $('<div class="showmoreDeta"></div>');
					that.$innertxt.html(html);
					that.$content.append(that.$innertxt);
					that.$container.toggleClass('onshow');
					that.getPos();

					that.$content.find('.close').on('click', function() {
						that.$container.removeClass('onshow');
					})
				});
			}

		},
		getPos : function() {
			var that = this;
			if (that.$content) {
				var myX = that.$container.offset().left - $(window).scrollLeft();
				var myY = that.$container.offset().top - $(window).scrollTop();
				var posX = ((myX + that.$content.outerWidth()) < $(window).width());
				var posY = ((myY + that.$content.outerHeight()) < $(window).height());
				var myPos = that.$target.data('position');
				var isFix = that.$target.attr('position');
				that.$content.removeClass('bottom').removeClass('top');
				if (isFix == 'fixed') {
					that.$content.css('position', 'fixed');
					that.$content.css({
						'left' : posX ? myX : myX + that.$container.outerWidth() - that.$content.outerWidth(),
						'top' : posY ? (that.$container.height() + myY) : (myY - that.$content.outerHeight())
					});
					if (posY) {
						that.$content.addClass('bottom');
					} else {
						that.$content.addClass('top');
					}
					if (that.$container.css('text-align') == 'right') {
						that.$content.addClass('right');
						that.$content.css({
							'left' : myX + that.$container.outerWidth() - that.$content.outerWidth()
						});
					}
					if (myPos == "center") {
						that.$content.css({
							'left' : myX + (that.$container.outerWidth() - that.$content.outerWidth()) / 2
						});
						that.$content.addClass('center');
					}
				} else {
					that.$content.css({
						'left' : posX ? 0 : that.$container.outerWidth() - that.$content.outerWidth(),
						'top' : posY ? that.$target.outerHeight() + 10 : -that.$content.outerHeight() - 10
					});
					if (posY) {
						that.$content.addClass('bottom');
					} else {
						that.$content.addClass('top');
					}
					if (that.$container.css('text-align') == 'right') {
						that.$content.addClass('right');
						that.$content.css({
							'left' : that.$container.outerWidth() - that.$content.outerWidth()
						});
					}
					if (myPos == "center") {
						that.$content.css({
							'left' : (that.$container.outerWidth() - that.$content.outerWidth()) / 2
						});
						that.$content.addClass('center');
					}
				}
			} else {
				return;
			}
		}
	};
	$.fn.titletips = function() {
		var $target = $(this);
		if (!$target.attr('bind-haspartent')) {
			var c = new titletips($target);
			$target.attr('bind-haspartent', true);
		}
		return this;
	};
	$(function() {
		$('[plugin-titletips]').each(function() {
			$(this).titletips();
		});
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('[plugin-titletips]').each(function() {
				$(this).titletips();
			});
		});
		$(document).on('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('[plugin-titletips]').each(function() {
				$(this).titletips();
			});
		});
	});
})(jQuery);