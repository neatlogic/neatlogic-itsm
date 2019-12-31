;
(function($) {
	var colorvalidator = function(target, options) {
		this.$target = $(target);
		this.init();
	}

	this.colorvalidator = colorvalidator;
	colorvalidator.prototype = {
		init : function() {
			var that = this;
			that.$target.wrap('<div class="jquery-colorvalidator-wraper"></div>');
			that.$colorShower = $('<div class="jquery-colorvalidator-shower"></div>');
			that.$target.closest('.jquery-colorvalidator-wraper').append(that.$colorShower);

			that.setColor($.trim(that.$target.val()));
			that.$target.on('blur change', function() {
				that.setColor($.trim($(this).val()));
			});
			that.$target.attr('data-bind', true);
		},
		setColor : function(stringToTest) {
			var that = this;
			if (stringToTest) {
				that.$colorShower.css("background-color", "rgb(0, 0, 0)");
				that.$colorShower.css("background-color", stringToTest);
				if (that.$colorShower.css("background-color") !== "rgb(0, 0, 0)") {
					that.$target.val(stringToTest);
				} else {
					that.$target.val('#000000');
				}
			} else {
				that.$target.val('#000000');
				that.$colorShower.css("background-color", "rgb(0, 0, 0)");
			}
		}
	};

	$.fn.colorvalidator = function(options) {
		var $target = $(this);
		if (!$target.data('bind')) {
			new colorvalidator($target, options);
		}
		return this;
	};

	$(function() {

		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('input[plugin-colorvalidator]').each(function() {
				$(this).colorvalidator();
			});
		});

		$(document).on('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('input[plugin-colorvalidator]').each(function() {
				$(this).colorvalidator();
			});
		});

		$('input[plugin-colorvalidator]').each(function() {
			$(this).colorvalidator();
		});
	});

})(jQuery);