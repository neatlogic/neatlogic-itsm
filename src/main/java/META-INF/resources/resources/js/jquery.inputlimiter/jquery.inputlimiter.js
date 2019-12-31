;
(function($) {

	var inputlimiter = function(target, options) {
		var blacklist;
		if(options.blacklist){
			blacklist = options.blacklist;
			blacklist = blacklist.replace('&amp;','&');
			blacklist = blacklist.replace('&lt;','<');
			blacklist = blacklist.replace('&gt;','>');
			blacklist = blacklist.replace('&quot;','"');
		}
		$(target).on('input', function(e) {
			var v = $(this).val();
			if (blacklist) {
				var strs = blacklist.toString().split('');
				for ( var i in strs) {
					v = v.replace(strs[i], '');
				}
			}
			$(this).val(v);
		});
	}

	this.inputlimiter = inputlimiter;

	$.fn.inputlimiter = function(options) {
		var $target = $(this);
		if (!$target.attr('bind-inputlimiter')) {
			var c = new inputlimiter($target, options);
			$target.attr('bind-inputlimiter', true);
		}
		return this;
	};

	$(function() {
		$(document).bind("ajaxComplete", function(e, xhr, settings) {
			$('input[plugin-inputlimiter]').each(function() {
				var $target = $(this);
				if (!$target.attr('bind-inputlimiter') && ($target.data('blacklist') || $target.data('whitelist'))) {
					var options = {};
					if ($target.data('blacklist')) {
						options.blacklist = $target.data('blacklist');
					}
					if ($target.data('whitelist')) {
						options.whitelist = $target.data('whitelist');
					}
					$target.inputlimiter(options);
					$target.attr('bind-inputlimiter', true);
				}
			});
		});

		$(document).on('xdoTRender', function(e, content) {
			var $content = $(content);
			$content.find('input[plugin-inputlimiter]').each(function() {
				var $target = $(this);
				if (!$target.attr('bind-inputlimiter') && ($target.data('blacklist') || $target.data('whitelist'))) {
					var options = {};
					if ($target.data('blacklist')) {
						options.blacklist = $target.data('blacklist');
					}
					if ($target.data('whitelist')) {
						options.whitelist = $target.data('whitelist');
					}
					$target.inputlimiter(options);
					$target.attr('bind-inputlimiter', true);
				}
			});
		});

		$('input[plugin-inputlimiter]').each(function() {
			var $target = $(this);
			if (!$target.attr('bind-inputlimiter') && ($target.data('blacklist') || $target.data('whitelist'))) {
				var options = {};
				if ($target.data('blacklist')) {
					options.blacklist = $target.data('blacklist');
				}
				if ($target.data('whitelist')) {
					options.whitelist = $target.data('whitelist');
				}
				$target.inputlimiter(options);
				$target.attr('bind-inputlimiter', true);
			}
		});
	});

})(jQuery);