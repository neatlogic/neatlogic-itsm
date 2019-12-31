!function(){
	var ts3Dpie={};
	

	function pieLeft(d,rx,ry,ir,h){

		var startAngle_i = d.startAngle;
		
		var sx_i = ir*(rx+.5)*Math.cos(startAngle_i),
			sy_i = ir*(ry+.5)*Math.sin(startAngle_i);



		var sx_t = rx*Math.cos(d.startAngle),
			sy_t = ry*Math.sin(d.startAngle);

		var ret_l=[];
			ret_l.push("M",sx_t,sy_t,"L",sx_i,sy_i,"L",sx_i,sy_i+h,"L",sx_t,sy_t+h,"z");

			return ret_l.join(" ");

	}
	function pieRight(d,rx,ry,ir,h){

		var endAngle_i =d.endAngle;
		
		var ex_i = ir*rx*Math.cos(endAngle_i),
			ey_i = ir*ry*Math.sin(endAngle_i);


		var ex_t = rx*Math.cos(d.endAngle),
			ey_t = ry*Math.sin(d.endAngle);

		var ret_r=[];
			ret_r.push("M",ex_t,ey_t,"L",ex_i,ey_i,"L",ex_i,ey_i+h,"L",ex_t,ey_t+h,"z");

			return ret_r.join(" ");

	}
	function pieTop(d, rx, ry, ir ){
		if(d.endAngle - d.startAngle == 0 ) return "M 0 0";
		var sx = rx*Math.cos(d.startAngle),
			sy = ry*Math.sin(d.startAngle),
			ex = rx*Math.cos(d.endAngle),
			ey = ry*Math.sin(d.endAngle);
			
		var ret =[];
		ret.push("M",sx,sy,"A",rx,ry,"0",(d.endAngle-d.startAngle > Math.PI? 1: 0),"1",ex,ey,"L",ir*ex,ir*ey);
		ret.push("A",ir*rx,ir*ry,"0",(d.endAngle-d.startAngle > Math.PI? 1: 0), "0",ir*sx,ir*sy,"z");
		return ret.join(" ");



	}

	function pieOuter(d, rx, ry, h ){
		var startAngle = (d.startAngle > Math.PI ? Math.PI : d.startAngle);
		var endAngle = (d.endAngle > Math.PI ? Math.PI : d.endAngle);
		
		var sx = rx*Math.cos(startAngle),
			sy = ry*Math.sin(startAngle),
			ex = rx*Math.cos(endAngle),
			ey = ry*Math.sin(endAngle);
			
			var ret =[];
			ret.push("M",sx,h+sy,"A",rx,ry,"0 0 1",ex,h+ey,"L",ex,ey,"A",rx,ry,"0 0 0",sx,sy,"z");
			return ret.join(" ");
	}

	function pieInner(d, rx, ry, h, ir ){
		var startAngle = (d.startAngle < Math.PI ? Math.PI : d.startAngle);
		var endAngle = (d.endAngle < Math.PI ? Math.PI : d.endAngle);
		
		var sx = ir*rx*Math.cos(startAngle),
			sy = ir*ry*Math.sin(startAngle),
			ex = ir*rx*Math.cos(endAngle),
			ey = ir*ry*Math.sin(endAngle);

			var ret =[];
			ret.push("M",sx, sy,"A",ir*rx,ir*ry,"0 0 1",ex,ey, "L",ex,h+ey,"A",ir*rx, ir*ry,"0 0 0",sx,h+sy,"z");
			return ret.join(" ");
		
	}

	function getPercent(d) {
  return (d.endAngle - d.startAngle > 0 ?
  d.data.label + ': ' + Math.round(1000 * (d.endAngle - d.startAngle) / (Math.PI * 2)) / 10 + '%' : '');
}
	// function getPercent(d){
	// 	return (d.endAngle-d.startAngle > 0.2 ? 
	// 			Math.round(1000*(d.endAngle-d.startAngle)/(Math.PI*2))/10+'%' : '');
	// }	
	
	ts3Dpie.transition = function(id, data, rx, ry, h, ir){
		function arcTweenLeft(a){
			var i = d3.interpolate(this._current, a);
		  this._current = i(0);
		  return function(t) { return pieLeft(i(t), rx, ry, ir,h);  };
		}
		function arcTweenRight(a){
			var i = d3.interpolate(this._current, a);
		  this._current = i(0);
		  return function(t) { return pieRight(i(t), rx, ry, ir,h);  };
		}
		function arcTweenInner(a) {
		  var i = d3.interpolate(this._current, a);
		  this._current = i(0);
		  return function(t) { return pieInner(i(t), rx, ry, h, ir);  };
		}
		function arcTweenTop(a) {
		  var i = d3.interpolate(this._current, a);
		  this._current = i(0);
		  return function(t) { return pieTop(i(t), rx, ry, ir);  };
		}
		function arcTweenOuter(a) {
		  var i = d3.interpolate(this._current, a);
		  this._current = i(0);
		  return function(t) { return pieOuter(i(t), rx, ry, h);  };
		}
		function textTweenX(a) {
		  var i = d3.interpolate(this._current, a);
		  this._current = i(0);
		  return function(t) { return 0.65*rx*Math.cos(0.5*(i(t).startAngle+i(t).endAngle));  };
		}
		function textTweenY(a) {
		  var i = d3.interpolate(this._current, a);
		  this._current = i(0);
		  return function(t) { return 0.65*rx*Math.sin(0.5*(i(t).startAngle+i(t).endAngle));  };
		}
		
		var _data = d3.layout.pie().sort(null).value(function(d) {return d.value;})(data);

		d3.select("#"+id).selectAll(".leftSlice").data(_data)
			.transition().duration(750).attrTween("d", arcTweenLeft); 
		d3.select("#"+id).selectAll(".rightSlice").data(_data)
			.transition().duration(750).attrTween("d", arcTweenRight); 
		
		d3.select("#"+id).selectAll(".innerSlice").data(_data)
			.transition().duration(750).attrTween("d", arcTweenInner); 
			
		d3.select("#"+id).selectAll(".topSlice").data(_data)
			.transition().duration(750).attrTween("d", arcTweenTop); 
			
		d3.select("#"+id).selectAll(".outerSlice").data(_data)
			.transition().duration(750).attrTween("d", arcTweenOuter); 	
			
		d3.select("#"+id).selectAll(".percent text").data(_data).transition().duration(750)
			.attrTween("x",textTweenX).attrTween("y",textTweenY).text(getPercent); 	
	}
	
	ts3Dpie.draw=function(id, data, config){

		var svg = d3.select("#"+id).append("svg").attr("width",config.width).attr("height",config.height);
	    var margin={top:20,right:30,bottom:30,left:20};
		var rx=config.rx||130,ry=config.ry||100,h=config.h||30,ir=config.ir||0.4,width=config.width||300,height=config.height||300;
		
		var x = config.width/2,y=height/2-margin.top;
		var _data = d3.layout.pie().sort(null).value(function(d) {return d.value;})(data);
		
		var slices = svg.append("g").attr("transform", "translate(" + x + "," + y + ")")
			.attr("class", "slices");

		slices.selectAll(".leftSlice").data(_data).enter().append("path").attr("class", "leftSlice").attr('id',function(d){return d.data.label;})
			.style("fill", function(d) { return d3.hsl(d.data.color).darker(0.7); })
			.attr("d",function(d){ return pieLeft(d, rx,ry, ir,h);})
			.each(function(d){this._current=d;});

		slices.selectAll(".rightSlice").data(_data).enter().append("path").attr("class", "rightSlice").attr('id',function(d){return d.data.label;})
			.style("fill", function(d) { return d3.hsl(d.data.color).darker(0.7); })
			.attr("d",function(d){ return pieRight(d, rx,ry, ir,h);})
			.each(function(d){this._current=d;});
			
		slices.selectAll(".innerSlice").data(_data).enter().append("path").attr("class", "innerSlice").attr('id',function(d){return d.data.label;})
			.style("fill", function(d) { return d3.hsl(d.data.color).darker(0.7); })
			.attr("d",function(d){ return pieInner(d, rx,ry, h, ir);})
			.each(function(d){this._current=d;});

		
		slices.selectAll(".outerSlice").data(_data).enter().append("path").attr("class", "outerSlice").attr('id',function(d){return d.data.label;})
			.style("fill", function(d) { return d3.hsl(d.data.color).darker(0.7); })
			.attr("d",function(d){ return pieOuter(d, rx,ry, h);})
			.each(function(d){this._current=d;});

		slices.selectAll(".topSlice").data(_data).enter().append("path").attr("class", "topSlice").attr('id',function(d){return d.data.label;})
			.style("fill", function(d) { return d.data.color; })
			.style("stroke", function(d) { return d.data.color; })
			.attr("d",function(d){ return pieTop(d, rx, ry, ir);})
			.on('click',function(d){
				var item = d3.select(this);//.classed('clicked',true);
				//d3.selectAll('.slices path,.slices g').style('transform','translate(0,0)');
				if(item.classed('clicked')){
					d3.selectAll('#'+d.data.label).style('transform','translate(0,0)');
					item.classed('clicked',false);
				}
				
			 	else{
			 		var arcX = 0.2*rx*Math.cos(0.5*(d.startAngle+d.endAngle));
			 			arcY = 0.2*ry*Math.sin(0.5*(d.startAngle+d.endAngle));
			 		d3.selectAll('#'+d.data.label).style('transform','translate('+arcX+'px,'+arcY+'px)');
			 		item.classed('clicked',true);
			 	}
				
			})
			.each(function(d){this._current=d;});

		slices.selectAll(".percent").data(_data).enter().append('g').attr("class", "percent").attr('id',function(d){
			return d.data.label;
		}).append("text")
			.attr("x",function(d){ return 0.65*rx*Math.cos(0.5*(d.startAngle+d.endAngle));})
			.attr("y",function(d){ return 0.65*ry*Math.sin(0.5*(d.startAngle+d.endAngle));})
			.text(getPercent).each(function(d){
				this._current=d;
			});	
	}
	
	this.ts3Dpie = ts3Dpie;
}();
