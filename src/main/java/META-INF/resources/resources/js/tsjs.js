$(function(){
	$(document).on('click', '.tsnavmore', function(event) {
		var tsnavbar = $(this).closest(".tsnavbar");
		if(!tsnavbar.attr('flag')){
			tsnavbar.find('li').eq(0).after('<li role="presentation">'+ tsnavbar.find('li').eq(0).html() +'</li>');
			tsnavbar.attr('flag','true');
		}
		if($(this).hasClass('gohide')){
			$(this).removeClass('gohide');
			$(this).siblings("li").hide();
			$(this).siblings("li.mainshow").show();			
		}else{
			$(this).addClass('gohide');
			$(this).siblings("li").show();
			$(this).siblings("li").each(function(){
				if(!$(this).hasClass('mainshow')){
					if ($(this).find("a").html() == $(this).siblings(".mainshow").find("a").html()) {
						$(this).hide();
					};
				}
			});			
		}
	});
/*	.on('mouseleave', '.tsnavbar', function(event) {
		$(this).find("li").hide();
		$(this).find("li.mainshow").show();
		$(this).find(".tsnavmore").removeClass('gohide');
	})*/
	
	$(document).on('click', '.tsnavbar li', function() {
		if(!$(this).hasClass('mainshow')){
            $(this).parents(".tsnavbar").find("li").each(function() {
				if (!$(this).hasClass('mainshow')) {
					if ($(this).find("a").html() == $(this).siblings(".mainshow").find("a").html()) {
						$(this).hide();
					} else {
						$(this).show();
					}
				}
			});
            $(this).siblings(".mainshow").html($(this).html());
            $(this).hide();
            //$(this).siblings().hide();
            $(this).siblings("li").show();
            //$(this).siblings(".tsnavmore").show();
        }
	});
});


