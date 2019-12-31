$(function() {
	$('[data-toggle="tooltip"]').tooltip();
	$(document).bind("ajaxComplete", function(e, xhr, settings) {
		$('[data-toggle="tooltip"]').tooltip();
	});

	$(document).on('xdoTRender', function(e, content) {
		$('[data-toggle="tooltip"]').tooltip();
	});
});
