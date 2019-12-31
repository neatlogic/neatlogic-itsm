
function formValidate() {

	
	var flag = true;
	try{
		$.each(arrEditor, function(i, editor) {
			editor.sync();
		});
		$(".ke-container-default").addClass('noneed');
	}catch(e){}
	
	//校验mustinput
	var textboxs = $('.mustinput:text');
	textboxs
			.each(function() {
				var item = $(this);
				item.val($.trim(item.val()));
				if (item.parents().is('[style*="none"]')
						|| item.is('[style*="none"]')) {
					item.css('background-color', '');

				} else {
					if ($.trim(item.val()) == '') {
						item.css('background-color', 'yellow');
						flag = false;
					} else {
						item.css('background-color', '');
					}
				}
			});

	var textareas = $('textarea.mustinput');
	textareas
			.each(function() {
				var item = $(this);
				if (item.parents().is('[style*="none"]')
						|| item.is('[style*="none"]')) {
					item.css('background-color', '');
				} else {
					if ($.trim(item.val()) == '') {
						item.css('background-color', 'yellow');
						flag = false;
					} else {
						item.css('background-color', '');
					}
				}
			});

	var selects = $('select.mustinput');
	selects
			.each(function() {
				var item = $(this);
				if (item.parents().is('[style*="none"]')
						|| item.is('[style*="none"]')) {
					item.css('background-color', '');
				} else {
					if ($.trim(item.val()) == '') {
						item.css('background-color', 'yellow');
						flag = false;
					} else {
						item.css('background-color', '');
					}
				}
			});
	
	//校验number
	var textboxs = $('.number:text');
	textboxs
			.each(function() {
				var item = $(this);
				item.val($.trim(item.val()));
				if (item.parents().is('[style*="none"]')
						|| item.is('[style*="none"]')) {
					item.css('background-color', '');

				} else {
					if ($.trim(item.val()) != '') {
						if(isNaN($.trim(item.val()))){
							item.css('background-color', 'yellow');
							flag = false;
						}else{
							item.css('background-color', '');
						}
					}
				}
			});

	var textareas = $('textarea.number');
	textareas
			.each(function() {
				var item = $(this);
				if (item.parents().is('[style*="none"]')
						|| item.is('[style*="none"]')) {
					item.css('background-color', '');
				} else {
					if ($.trim(item.val()) != '') {
						if(isNaN($.trim(item.val()))){
							item.css('background-color', 'yellow');
							flag = false;
						}else{
							item.css('background-color', '');
						}
					}
				}
			});

	var selects = $('select.number');
	selects
			.each(function() {
				var item = $(this);
				if (item.parents().is('[style*="none"]')
						|| item.is('[style*="none"]')) {
					item.css('background-color', '');
				} else {
					if ($.trim(item.val()) != '') {
						if(isNaN($.trim(item.val()))){
							item.css('background-color', 'yellow');
							flag = false;
						}else{
							item.css('background-color', '');
						}
					}
				}
			});
	
	return flag;
}
