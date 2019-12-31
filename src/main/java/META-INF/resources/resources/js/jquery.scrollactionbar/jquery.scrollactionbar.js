;
(function($) {
	var scrollactionbar = function(target) {
		var that = this;
		that.$target = $(target);
		that.$target.wrap('<div class="jquery-scrollactionbar-container"></div>');
		that.$container = that.$target.closest('.jquery-scrollactionbar-container');
		that.$target.addClass('jquery-scrollactionbar');
		that.init();
	};
	this.scrollactionbar = scrollactionbar;
	scrollactionbar.prototype = {
		init:function(){
			var that = this;
			$(window).on('scroll',function(){
				that.$container.css('padding-top',that.$target.height()+20);
				if($(window).scrollTop()>that.$container.offset().top){
					that.$target.addClass('fixtop-scrollactionbar');
				}else{
					that.$target.removeClass('fixtop-scrollactionbar');
					that.$container.css('padding-top',0);
				}	
			});			
		}
	};
	$.fn.scrollactionbar = function() {
		var $target = $(this);
		if (!$target.attr('bind-scrollactionbar')) {
			var c = new scrollactionbar($target);
			$target.attr('bind-scrollactionbar', true);
		}
		return this;
	};

	$(function() {
		$('[plugin-scrollactionbar]').each(function() {
			$(this).scrollactionbar();
		});

		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('[plugin-scrollactionbar]').each(function() {
				$(this).scrollactionbar();
			});
		});

		$(document).on('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('[plugin-scrollactionbar]').each(function() {
				$(this).scrollactionbar();
			});
		});
	});

})(jQuery);



