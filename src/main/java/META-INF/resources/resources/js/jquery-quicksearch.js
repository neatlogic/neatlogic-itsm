$(function(){
	$(document).on("keydown", "input", function(event) {
		//输入框回车执行事件的触发为aim元素 
		var myTrigger = $(this).attr('aim');
		var keycode = event.keyCode ?event.keyCode:event.which;
		if(myTrigger && keycode == "13"){
			//去掉首尾空格
			event.preventDefault();
			$(this).val($.trim($(this).val()));
		    $('#' + myTrigger).click();
		}
	});
	$(document).on("change", "select", function(event) {
		var myTrigger = $(this).attr('aim');
		if(myTrigger){
		     $('#' + myTrigger).click();
		}
	});	
});


