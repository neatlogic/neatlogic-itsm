;
(function($) {
	$(function() {
		$(document).on("keypress", function(e) {
			if (e.keyCode == 13) {
				var btns = $('button[plugin-entertrigger]');
				btns.each(function() {
					$(this).trigger('click');
				});
			}
		});
	});
})(jQuery);