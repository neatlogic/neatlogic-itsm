
function countTime(et,id){
	
	var st = new Date().getTime();
	//alert("now:" + st);
	//return;
	var strStart;
	var strEnd="</span>";
	var timeSpan = et - st;
	var label = "";
	if(timeSpan > 0){
		strStart="<span style='color:green;font-size:12px;font-weight:normal'>" ;
		label = "离超时还有：";
	}else{
		label = "已经超时：";
		strStart="<span style='color:red;font-size:12px;font-weight:normal'>";
	}
	var day = parseInt(timeSpan / 1000 / 60 /60/24);
	timeSpan = timeSpan - day  * 1000 * 60 * 60 * 24;
	var hour = parseInt(timeSpan / 1000 /60 / 60);
	timeSpan = timeSpan - hour * 1000 * 60 * 60;
	var min =  parseInt(timeSpan / 1000 /60) ;
	timeSpan = timeSpan - min * 1000 * 60
	var ss = parseInt(timeSpan / 1000) ;
	
	var temp=label +  Math.abs(day) + "天" + Math.abs(hour) +"小时" + Math.abs(min) + "分" + Math.abs(ss) + "秒";
	if(day==0){
		temp=label +  Math.abs(hour) +"小时" + Math.abs(min) + "分" + Math.abs(ss) + "秒";
		if(hour==0){
			temp=label +  Math.abs(min) + "分" + Math.abs(ss) + "秒";
			if(min==0){
				temp=label + Math.abs(ss);
			}
		}
	}
	$('#' + id).html(strStart + temp + strEnd);
}

