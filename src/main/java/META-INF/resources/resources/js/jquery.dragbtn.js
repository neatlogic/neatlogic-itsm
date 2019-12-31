;
(function($) {
	var dragbtn = function(delegation) {
		var that = this;
		that.$delegation = $(delegation);
		that.isDrag = false;
		that.deltaX = 0;
		that.deltaY = 0;
		that.formalY = 0;
		that.maxY = 100;
		that.$delegation.css('cursor', 'move');
		that.$delegation.on('mousedown', function(e) {
			that.isDrag = true;
			that.deltaY = e.clientY;
			that.formalY = $(window).height() - that.$delegation.offset().top- 40 +$(window).scrollTop() ; 
		});
		that.$delegation.on('mousemove', function(e) {
			if (that.isDrag) {
				var ny = Math.min(Math.max(that.formalY - e.clientY + that.deltaY, 0), that.maxY);
				that.$delegation.css({
					'bottom' : ny + 'px'
				});
				//e.preventDefault();
			}
		});
		$(document).on('mouseup', function() {
			that.isDrag = false;
		});
	}
	this.dragbtn = dragbtn;
	$.fn.dragbtn = function() {
		var $delegation = $(this);
		if (!$delegation.data('bind-dragbtn')) {
			var c = new dragbtn($delegation);
			$delegation.attr('bind', true);
		}
		return this;
	};
	$(function() {
		$('.btn-bar').each(function() {
			$(this).dragbtn();
		});
	});
})(jQuery);