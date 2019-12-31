(function($) {
    $.fn.userlink = function(options) {
    	var item = $(this);
    	if($.trim(item.text()) == ''){
			return this;
		}
    	if(item.attr('userid') && !item.attr('binded') && item.attr('userid').toLowerCase() != 'system'){
    		if(item.children().length <= 0){
    			item.css({'padding':'0px 5px 0px 0px', 'text-decoration':'underline','cursor':'pointer', 'background' : 'url(/balantflow/resources/images/icons/out.png) no-repeat', 'background-position':'top right'});
    		}else{
    			item.css('cursor', 'pointer');
    		}
    		item.click(function(){
    			$(top.document.body).find('#rightContent').attr('src', '/balantflow/module/balantface/viewUserLayout.do?uid=' + item.attr('userid'));
    		});
    		item.attr('binded', 'true');
    	}
    	return this;
    };
    
    $(function(){
    	$(document).bind("ajaxComplete", function(e, xhr, settings){
    		$('.spnUser').each(function(){
        		$(this).userlink();
        	});
    	});
    	$('.spnUser').each(function(){
    		$(this).userlink();
    	});
    });
    
})(jQuery);