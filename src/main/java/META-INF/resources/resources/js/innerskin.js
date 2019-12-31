$(function(){
	if($('.innerskin')){
		var myskin;
		if($.cookie('skin') !=null){
			myskin = $.cookie('skin');
		}else{
			myskin = 'skin3';
		}
		$('.innerskin').attr('href','/balantflow/resources/css/index/innerskin/'+myskin+'.css');		
	}

});


