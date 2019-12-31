$(function() {
	$(document).on('change', 'select,input,textarea', function() {
		if (typeof customscript != 'undefined' && typeof customscript.change != 'undefined') {
			customscript.change(this);
		}
	});

	$(document).on('click', 'button,input[type="radio"],input[type="checkbox"]', function() {
		if (typeof customscript != 'undefined' && typeof customscript.click != 'undefined') {
			customscript.click(this);
		}
	});

	$(document).on('mousedown', 'select,input,textarea', function() {
		if (typeof customscript != 'undefined' && typeof customscript.mousedown != 'undefined') {
			customscript.mousedown(this);
		}
	});

	$(document).on('keyup', 'input,textarea', function() {
		if (typeof customscript != 'undefined' && typeof customscript.keyup != 'undefined') {
			customscript.keyup(this);
		}
	});

	$(document).on('submit', 'form', function() {
		if (typeof customscript != 'undefined' && typeof customscript.submit != 'undefined') {
			return customscript.submit(this);
		}
	});

	var customscriptAjaxCount = 0;
	$(document).ajaxSend(function(event, xhr, settings){
		customscriptAjaxCount += 1;
		//console.info("send:"+settings.url);
	});
	$(document).ajaxComplete(function(event, xhr, settings) {
		customscriptAjaxCount -= 1;
		//console.info("done:"+settings.url);
		if (typeof customscript != 'undefined' && typeof customscript.ajaxcomplete != 'undefined') {
			customscript.ajaxcomplete(settings.type, settings.url);
		}
		if(customscriptAjaxCount <= 0 && typeof customscript != 'undefined' && typeof customscript.allajaxcomplete != 'undefined' && !customscript.init && settings.url.indexOf("xdottemplate")<0){
			customscript.init = true;
			customscript.allajaxcomplete();
		}
	});

	if (typeof customscript != 'undefined' && typeof customscript.load != 'undefined') {
		customscript.load();
	}
});