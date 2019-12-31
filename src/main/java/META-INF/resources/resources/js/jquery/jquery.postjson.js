(function($) {
	$.postJSON = function() {
		var url = null, data = null, callback = null, async = true;
		for (var i = 0; i < arguments.length; i++) {
			if (i == 0) {
				url = arguments[i];
			} else if (typeof arguments[i] == 'boolean') {
				async = arguments[i];
			} else if (typeof arguments[i] == 'string' || typeof arguments[i] == 'object') {
				data = arguments[i];
			} else if (typeof arguments[i] == 'function') {
				callback = arguments[i];
			}
		}
		return $.ajax({
			type : 'POST',
			url : url,
			async : async,
			contentType : 'application/json;charset=utf-8',
			data : data,
			dataType : 'json',
			processData : false,
			success : callback
		});
	};
}(jQuery));
