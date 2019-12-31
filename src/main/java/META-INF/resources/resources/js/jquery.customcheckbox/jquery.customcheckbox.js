(function($) {
	$.fn.customcheckbox = function(options) {
		var target = $(this);
		var style = target.data('style') || 'square';
		if (!target.data('isbind')) {
			var id;
			if (target.attr('id')) {
				id = target.attr('id');
			} else {
				id = new Date().getTime() + parseInt(Math.random() * 10000000);
				target.attr('id', id);
			}
			var disabledStyle = $(this).attr('disabled') == 'disabled' ? 'customcheckbox_disabled' : '';
			target.wrap('<div class="customcheckbox_' + style + ' ' + disabledStyle + ' "></div>');
			var label = $('<label for="' + id + '"></label>');
			label.on('click', function(e) {
				e.stopPropagation();
			});
			target.after(label);
			target.hide();
			target.attr('data-isbind', true);
		}
		//只更新状态
		target.on('reload',function(){
			if(target.attr('disabled')){
				target.parent().addClass('customcheckbox_disabled');
			}else{
				target.parent().removeClass('customcheckbox_disabled');
			}			
		});
		
		return this;
	};

	$.fn.customradio = function(options) {
		var target = $(this);
		if (!target.data('isbind')) {
			var id;
			if (target.attr('id')) {
				id = target.attr('id');
			} else {
				id = new Date().getTime() + parseInt(Math.random() * 10000000);
				target.attr('id', id);
			}
			target.wrap('<div class="customradio_square"></div>');
			if(target.attr('disabled')){
				target.parent().addClass('customradio_disabled');
			}else{
				target.parent().removeClass('customradio_disabled');
			}
			var label = $('<label for="' + id + '"></label>');
			label.on('click', function(e) {
				e.stopPropagation();
			});
			target.after(label);
			target.hide();
			target.attr('data-isbind', true);
		}
		//只更新状态
		target.on('reload',function(){
			if(target.attr('disabled')){
				target.parent().addClass('customradio_disabled');
			}else{
				target.parent().removeClass('customradio_disabled');
			}			
		});
		return this;
	};
	$.fn.togglestatus = function() {
		var target = $(this);
		var valList = {
				"on":1,
				"off":0
		};
		if(target.data('on')){
			valList.on = target.data('on');
		}
		if(target.data('off')){
			valList.off = target.data('off');
		}
		if (!target.data('isbind')) {
			target.wrap('<span class="tsradio_toggle"></span>');
			target.$wrapper = target.closest('.tsradio_toggle');
			if(target.val() == valList.on){
				target.$wrapper.addClass('active');
				target.val('1');
			}else{
				target.$wrapper.removeClass('active');
				target.val('0');
			}
			if(target.prop('disabled')){
				target.$wrapper.addClass('disabled');
			}else{
				target.$wrapper.on('click', function(e) {
					e.stopPropagation();
					if(target.val() == valList.on){
						target.$wrapper.removeClass('active');
						target.val('0');
					}else{
						target.$wrapper.addClass('active');
						target.val('1');
					}
				});				
			}
			target.on('reload',function(){
				if(target.prop(':disabled')){
					target.$wrapper.addClass('disabled');
					target.$wrapper.off('click');
				}else{
					target.$wrapper.removeClass('disabled');
					target.$wrapper.on('click', function(e) {
						e.stopPropagation();
						if(target.val() == 1){
							target.$wrapper.removeClass('active');
							target.val('0');
						}else{
							target.$wrapper.addClass('active');
							target.val('1');
						}
					});
				}				
			});
			target.hide();
			target.attr('data-isbind', true);
		}
		return this;
	};

	$(function() {
		if ('-webkit-border-radius' in document.body.style || '-moz-border-radius' in document.body.style || 'border-radius' in document.body.style) {// check
			// css3
			// is
			// support
			$(document).bind("ajaxComplete", function(e, xhr, settings) {
				$('input[data-makeup="checkbox"]').each(function() {
					if (!$(this).data('isbind')) {
						$(this).customcheckbox();
					}
				});
			});

			$('input[data-makeup="checkbox"]').each(function() {
				if (!$(this).data('isbind')) {
					$(this).customcheckbox();
				}
			});

			$(document).on('xdoTRender', function(e, content) {
				var $content = $(content);
				$content.find('input[data-makeup="checkbox"]').each(function() {
					if (!$(this).data('isbind')) {
						$(this).customcheckbox();
					}
				});
			});

			$(document).bind("ajaxComplete", function(e, xhr, settings) {
				$('input[data-makeup="radio"]').each(function() {
					if (!$(this).data('isbind')) {
						$(this).customradio();
					}
				});
			});

			$('input[data-makeup="radio"]').each(function() {
				if (!$(this).data('isbind')) {
					$(this).customradio();
				}
			});

			$(document).on('xdoTRender', function(e, content) {
				var $content = $(content);
				$content.find('input[data-makeup="radio"]').each(function() {
					if (!$(this).data('isbind')) {
						$(this).customradio();
					}
				});
			});
			
			$(document).bind("ajaxComplete", function(e, xhr, settings) {
				$('input[data-makeup="togglestatus"]').each(function() {
					if (!$(this).data('isbind')) {
						$(this).togglestatus();
					}
				});
			});

			$('input[data-makeup="togglestatus"]').each(function() {
				if (!$(this).data('isbind')) {
					$(this).togglestatus();
				}
			});

			$(document).on('xdoTRender', function(e, content) {
				var $content = $(content);
				$content.find('input[data-makeup="togglestatus"]').each(function() {
					if (!$(this).data('isbind')) {
						$(this).togglestatus();
					}
				});
			});
		}
	});
})(jQuery);