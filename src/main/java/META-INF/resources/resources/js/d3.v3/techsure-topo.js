;(function(root, factory) {
	if (typeof module === 'object' && module.exports) {
		module.exports = function(d3) {
			TsTopo = factory(d3);
			return TsTopo;
		};
	} else {
		root.TsTopo = factory(root.d3);
	}
	

}(	this, function(d3) {
	
	var topo={
			editor:undefined,
			viewer:undefined
	};
	
	var topoEditor = {
			getTopoId:undefined,
			addNode:undefined,
			updateStatus:undefined,
			addWarningStyle:undefined,
			saveTopo:undefined,
			contextMenu:undefined,
			setLinkMode:undefined,
			setSelectMode:undefined,
			setDragMode:undefined,
			delSelected:undefined,
			cancle:undefined,
			recover:undefined,
			addContextMenu:undefined,
			addClickAction:undefined,
			getStraightLine:undefined,
			getcurveLine:undefined,
			getSinglePolyLine:undefined,
			getDoublePolyLine:undefined,
			addCanvasNode:undefined,
			bindNodeData:undefined,
			setGrid:undefined,
			setLinkMotive:undefined,
			setBackgroundColor:undefined,
			initTopo:undefined,
			showToolTip:undefined,
			setMapBackGround:undefined,
			setNodeColor:undefined,
			scaleNode:undefined,
			ungroupNodes:undefined,
			groupNodes:undefined,
			addShape:undefined,
			clear:undefined,
			showDirection:undefined,
			hideDirection:undefined,
			setImgBackGround:undefined,
		// mergeNodes:undefined,
		// unmergeNodes:undefined,
			setParentTopoId:undefined,
			drawlines:undefined,
			getScaleTrans:undefined,
			getTranslation:undefined,
			getWidth:undefined,
			getSourceId:undefined,
			center:undefined
		//	changeNode:undefined
			
			
	};
	
	var topoViewer = {
			updateStatus:undefined,
			addWarningStyle:undefined,
			initTopo:undefined,
			setGrid:undefined,
			setLinkMotive:undefined,
			setBackgroundColor:undefined,
			setMapBackGround:undefined,
			getTopoId:undefined,
			clear:undefined,
			showDirection:undefined,
			hideDirection:undefined,
			setImgBackGround:undefined,
		// mergeNodes:undefined,
		// unmergeNodes:undefined,
			setParentTopoId:undefined,
			drawlines:undefined,
			getScaleFactor:undefined,
			getTranslation:undefined,
			getWidth:undefined,
			getSourceId:undefined,
			center:undefined
	//		changeNode:undefined
	};
	
	var tooltip,labelBox;
	
	var _topoId = null;
	var _parentTopoId = null;
	var _viewOnly = false;
	var _customBg = false,
			_bgImg = null;
	
	var _widgetView = false;
	
	var lastKeyDown = -1,
		  secondLastKeyDown = -1;
	
	/* record every add/remove step ,include node and link actions */
	var _addSteps=[],
		  _removeSteps=[],
		  _lastStep = null,
		  _restoreMode = false;
	// 用于存储group、shape等删除操作，直到保存动作触发时才被提交到数据库
	var _miscTransactions=[];
	

    var _lineColor  = '#2EA7E0',
    	  _defaultColor = '#fff';
	var styleIndex = 0;
	var scaleFactor = 1,
	translation =[0,0],
	panX = 0,
	panY = 0,

	_itemWidth=30,// 方形图标边长
	_nodeLablePadding = 0.8*_itemWidth,// 节点标签距离节点的距离
	 initiative = true;
	
	
	

	var lineTypes = {straight:0,curve:1,singlepolyline:2,doublepolyline:3},
			 lineType = lineTypes.straight;;
	
	var _width = 800,
	       _height =600,
	       _canvasBorder = 2;
	      
	
	
	var drawLineSelected = false;

	var _defaultNodeContextMenu = null,
	       _defaultNodeClickAction = null,
	       _defaultNodeDoubleClickAction = null,
	       _defaultDoubleClickBackground = null,
	       _defaultClickBackground = null,
		   _defaultLinkClickAction = null;
	
	var _contextMenus ={},
		  _clickActions ={},
		  _nodeIcons ={};

	var xScale = null;
	var yScale = null;
	
	var r = null;
	var zoom = null;
	
	var minScale=0.4;
			maxScale=5;
	// 画布元素
	var canvas = null;
	
	var divContainer = null;
	
	// 连线编辑图标
	var lineEditor = [  {id : 'edit',offset : "45%",y : -10,text : '\uf044'},  {id : 'exit',offset : "55%",y : -10,text : '\uf00d'} ];
	// 节点编辑图标
	var nodeEditor = [ {id : 'fresh',x : -20,y : -25,text : '\uf021'}, {id : 'copy',x : 0,y : -25,text : '\uf0c5'},  {id : 'exit',x : 20,y : -25,text : '\uf00d'} ];
	
	// mouse event vars
	var selected_node = null,
		selected_link=null,
	    mousedown_link = null,
	    mouseover_link = null,
	    mousedown_node = null,
	    mouseup_node = null,
	    mouseover_pan = false, 
	    mouseover_nodelabel = false,
	  // 用于判断当前放大的元素
	    mouseover_node=null,
	   // lastNodeId =0,
	    _links =[],
	    edges = [];

	var force = null;
	
	var background = null;
	
	var xLines = null,
			yLines = null,
			activeAxisXLine = null,
			activeAxisYLine = null;
			
	// 动态画线
	var drag_line = null;
	var addLinkData = false;// 判断是否需要增加线条数据
	var removeGroup = false;// 判断是否移除分组
	// 连线分组
	var path = null;
	
	var pathbg = null;
	
	// 存放node实时数据
	var nodeData=null;
	
	// 拖拽对象
	var drag = null;
	
	// 多选对象
	var brush = null;
	
	// 网格线
	var _grid = 1;
	
	// 初始化的Nodes或lines
	var inited = 0;
	
	// 判断是否为拖拽模式
	var dragMode = false;
	
	// 保存点击node的数据
	var mousedown_clicknode = null;
	// 保存点击node所在的group
	var mousedown_clicknodegroup = null;
	
	var   dispatch = d3.dispatch("onSelected", "onActions","onChanged"),// 自定义元素选中状态事件与撤销/恢复动作事件
			_lastSelectedStatus = -1,// 是否有元素选中状态
		   _lastActionStatus = -1 ,
		   _lastChangedStatus = false;
	
	var parser = new DOMParser();
	
	
	// 处理双击事件参数
	var wait = null;

	// svg 定义对象(mask、marker)
	// var canvasDefs = null;
	
	topo.viewer = function(config,nodes,links){
		tooltip = tooltip || d3.select('.canvas').append("div").attr("class", "tooltip");
        tooltip.style("display", "none");
		
		
		
		divContainer = d3.select('.canvas')[0][0];
		_viewOnly = true;
		_widgetView = config.widgetView || _widgetView;
		_itemWidth=config.itemWidth||_itemWidth;
		_nodeLablePadding = 0.8*_itemWidth;
		_height =divContainer.offsetHeight|_height;
		_width = divContainer.offsetWidth||_width;
		_canvasBorder = config.canvasBorder||_canvasBorder;
		_topoId = config.topoId;
		_parentTopoId = _topoId;
		_grid = config.grid||_grid;
		
		$('.canvas').css({'width':$(document).width(),'height':$(document).height()});
		
		_defaultNodeContextMenu = config.defaultNodeContextMenu||_defaultNodeContextMenu;
		_defaultNodeClickAction = config.defaultNodeClickAction||_defaultNodeClickAction;
		_defaultNodeDoubleClickAction = config.defaultNodeDoubleClickAction||_defaultNodeDoubleClickAction;
		_defaultDoubleClickBackground = config.defaultDoubleClickBackground||_defaultDoubleClickBackground;
		_defaultClickBackground = config.defaultClickBackground || _defaultClickBackground;
		/*
		 * if(config.nodeTypes){ config.nodeTypes.forEach(function(e){
		 * _nodeIcons[e.type]=e.icon; }); }
		 */
		 if(config.canvasNodes){
			 config.canvasNodes.forEach(function(e){
				 topoEditor.addCanvasNode(e);
				// _nodeIcons[e.type]=e.icon;
			 });
		 }
		 
		 xScale = d3.scale.linear()
		.domain([0,_width])
		.range([0,_width]);
		 
		 yScale = d3.scale.linear()
		.domain([0,_height])
		.range([0,_height]);
		 
		 zoom=d3.behavior.zoom()
			.scaleExtent([config.minScale||minScale, config.maxScale||maxScale])
			.x(xScale)
			.y(yScale)
			.on("zoom", config.zoomed||zoomed)
			.on("zoomend",zoomedEnd);
		 
		 
		canvas= d3.select(".canvas").append("svg")
			.attr("width","100%")
			.attr("height","100%")
			.attr("border",_canvasBorder)
			.call(zoom);
		
		force=d3.layout.force()
		.size([_width, _height])
		.charge(-400)
		.gravity(9)
		.linkDistance(200)
		.on("tick", config.tick||tick); 
		
		// define arrow markers for graph links
		canvas.append('svg:defs')
		.append('svg:marker')
	    .attr('id', 'end-arrow')
	    .attr('viewBox', '0 0 12 12')
	    .attr('refX', 8)
	    .attr('refY', 6)
	    .attr('markerWidth', 10)
	    .attr('markerHeight', 10)
	    .attr('orient', 'auto')
	  .append('svg:path')
	    .attr('d', 'M3,3 L10,6 L3,10 L6,6 L3,3')
	    .attr('fill', _lineColor);
		
		
		// 背景层，位于坐标线下层
		 background = canvas;
		
		 if(_grid==1){
			 
		 // X坐标线
		 xLines=background.append("g")
		 .attr("class", "x axis")
		 .selectAll("line")
		 .data(d3.range(15, _width, 15))
		 .enter().append("line")
		 .attr("x1", function(d) { return d; })
		 .attr("y1", 0)
		 .attr("x2", function(d) { return d; })
		 .attr("y2", _height);
		 
		 // Y坐标线
		 yLines=background.append("g")
		 .attr("class", "y axis")
		 .selectAll("line")
		 .data(d3.range(15, _height, 15))
		 .enter().append("line")
		 .attr("x1", 0)
		 .attr("y1", function(d) { return d; })
		 .attr("x2", _width)
		 .attr("y2", function(d) { return d; });
		 }
		
		 
		// 伸缩放大，直接绑定在canvas 的svg对象上
		
		/*
		 * var panCanvas = canvas;
		 * canvas=canvas.call(zoom).on("dblclick.zoom",null).on('mousedown.zoom',null).append('g').attr('class','nodes');
		 * 
		 * 
		 * panCanvas.append('circle').classed('button',true).attr({'cx':(50),'cy':50,'r':16,'opacity':0.85}).on('click',center);
		 * panCanvas.append('path').classed('button',true).attr('d','M50 10 l12
		 * 20 a40, 70 0 0,0 -24, 0z').on('click',function(){pan(0,50);});
		 * panCanvas.append('path').classed('button',true).attr('d','M10 50 l20
		 * -12 a70, 40 0 0,0 0, 24z').on('click',function(){pan(50,0);});
		 * panCanvas.append('path').classed('button',true).attr('d','M50 90 l12
		 * -20 a40, 70 0 0,1 -24, 0z').on('click',function(){pan(0,-50);});
		 * panCanvas.append('path').classed('button',true).attr('d','M90 50 l-20
		 * -12 a70, 40 0 0,1 0, 24z').on('click',function(){pan(-50,0);});
		 */
		
		 pathbg = canvas.append('svg:g').attr('class','linksbg').selectAll('path');
		// 连线分组
		 path = canvas.append('svg:g').attr('class','links').selectAll('path');
		 // viwer mode ,drag actions do nothing!
		// drag = function(){return;};
		   
		 
// if(config.bgImg){
// topoViewer.setBackGround(config.bgImg);
// }
		 

		   
			background.on('contextmenu',function(){
				// prevent rightclick on canvas
				d3.event.preventDefault();
				return;
				});
			initTopo(nodes, links);
			 if(!_widgetView){
				 d3.select(window).on('resize', resize); 
			 }
		return topoViewer;
		
	};
	
	
	 topo.editor = function(config,nodes,links){
		 tooltip = tooltip || d3.select('.canvas').append("div").attr("class", "tooltip");
		 labelBox = labelBox || d3.select('.canvas').append("div").attr("class", "labelBox");
		 labelBox.style('display','none');
		 // 注册自定义事件
		 	dispatch.on('onActions',config.onActions);
		 	dispatch.on('onSelected',config.onSelected);
		 	dispatch.on('onChanged',config.onChanged)
		 	
		 	 divContainer = d3.select('.canvas')[0][0];
		 	// 自适应屏幕
		 	$('.canvas').css({'width':$(document).width(),'height':$(document).height()});
			_itemWidth=config.itemWidth||_itemWidth;
			_nodeLablePadding = 0.8*_itemWidth;
			_height =$(document).height()||_height;// divContainer.offsetHeight|_height;
			_height+=15-_height%15;
			
			_width = $(document).width()||_width;// divContainer.offsetWidth||_width;
			_width +=15-_width%15;
			_canvasBorder = config.canvasBorder||_canvasBorder;
			_topoId = config.topoId;
			_parentTopoId = _topoId;
			
			_viewOnly = config.viewOnly||_viewOnly;
			
			_defaultNodeContextMenu = config.defaultNodeContextMenu||_defaultNodeContextMenu;
			_defaultNodeClickAction = config.defaultNodeClickAction||_defaultNodeClickAction;
			_defaultLinkClickAction = config.defaultLinkClickAction ||_defaultLinkClickAction;
			_defaultNodeDoubleClickAction = config.defaultNodeDoubleClickAction||_defaultNodeDoubleClickAction;
			_defaultDoubleClickBackground = config.defaultDoubleClickBackground||_defaultDoubleClickBackground; 
			_defaultClickBackground = config.defaultClickBackground || _defaultClickBackground;
			if(config.canvasNodes){
				 config.canvasNodes.forEach(function(e){
					 topoEditor.addCanvasNode(e);
					// _nodeIcons[e.type]=e.icon;
				 });
			 }
			 
			 xScale = d3.scale.linear()
			.domain([0,_width])
			.range([0,_width]);
			 
			 yScale = d3.scale.linear()
			.domain([0,_height])
			.range([0,_height]);
			 
			 
			 zoom=d3.behavior.zoom()
				.scaleExtent([config.minScale||minScale, config.maxScale||maxScale])
				.x(xScale)
				.y(yScale)
				.on("zoom", config.zoomed||zoomed)
				.on("zoomend",zoomedEnd(config.zoominEnd,config.zoomoutEnd));
 
	   drag = d3.behavior.drag()
	   .origin(function(d) { return d; })
	   .on('dragstart', function(d){dragStart(d,this)}) 
	    .on('drag', function (d) {
	    	dragged(d,this);
	    })
	    .on('dragend',function(d){
	    	dragEnd(d,this);
	    }); 
	   			 
			canvas= d3.select(".canvas").append("svg")
				.attr("width","100%")
				.attr("height","100%")
				.attr("border",_canvasBorder);

			
			// 背景层，位于坐标线下层
			 background = canvas;
			
			force=d3.layout.force()
			.size([_width, _height])
			.charge(-400)
			.gravity(9)
			.linkDistance(200)
			.on("tick", config.tick||tick); 
			
			// define arrow markers for graph links
			canvas.append('svg:defs')
			.append('svg:marker')
		    .attr('id', 'end-arrow')
		     .attr('viewBox', '0 0 12 12')
	    .attr('refX', 8)
	    .attr('refY', 6)
	    .attr('markerWidth', 10)
	    .attr('markerHeight', 10)
	    .attr('orient', 'auto')
	  .append('svg:path')
	    .attr('d', 'M3,3 L10,6 L3,10 L6,6 L3,3')
		    .attr('fill', _lineColor);
			
			// X坐标线

			 xLines=background.append("g")
			 .attr("class", "x axis")
			 .selectAll("line")
			 .data(d3.range(15, _width-15, 15))
			 .enter().append("line")
			 .attr("x1", function(d) { return d; })
			 .attr("y1", 0)
			 .attr("x2", function(d) { return d; })
			 .attr("y2", _height);

			 


			 // Y坐标线
			 yLines=background.append("g")
			 .attr("class", "y axis")
			 .selectAll("line")
			 .data(d3.range(15, _height-15, 15))
			 .enter().append("line")
			 .attr("x1", 0)
			 .attr("y1", function(d) { return d; })
			 .attr("x2", _width)
			 .attr("y2", function(d) { return d; });

			
			 
			
		    var panCanvas = canvas;
		    
			// 伸缩放大，直接绑定在canvas 的svg对象上
		    
		 
			 setBrush();

			 
			// 动态画线
			 drag_line = canvas.append('svg:path')
				  .attr('class', 'link dragline hidden')
				  .style('stroke',_lineColor)
				  .attr('d', 'M0,0L0,0');
			 pathbg = canvas.append('svg:g').attr('class','linksbg').selectAll('path');	  
			// 连线分组
			 path = canvas.append('svg:g').attr('class','links').selectAll('path');
			 
// //设置地图背景

			 

			 // 在canvas的最上层设置鼠标移动事件
				background.on('mousemove', mousemove)
				.on('click',function(){
					if(!mousedown_node){
						 d3.selectAll('.linkable').remove();
					}
					else if(drawLineSelected){
				 		 drag_line.classed('hidden', true)
				 	        .style('marker-end', '');
				 		 resetMouseVars();
				 	 }	
					clickBackground(_defaultClickBackground);
					
					
					
				})
				.on('dblclick',function(){
					doubleClickBakcground(_defaultDoubleClickBackground);
				})
				.on('mouseup',function(){
					if(!mousedown_node){
						 d3.selectAll('.linkable').remove();
					}
					else if(drawLineSelected){
						
				 		 drag_line.classed('hidden', true)
				 	        .style('marker-end', '');
				 		 resetMouseVars();
				 	 }	
				}).on('contextmenu',function(){
					// prevent rightclick on canvas
					d3.event.preventDefault();
					return;
					});
				initTopo(nodes,links);
				
				// 绑定键盘操作事件
				 d3.select('body')
					.on('keydown', keydown)
				    .on('keyup', keyup);  
				 
				 
			 d3.select(window).on('resize', resize); 
			 
			 

			return topoEditor;
			
		};
	
	function setBrush(){
		if(dragMode){
		}
		else{
			 brush = d3.svg.brush()
				.x(d3.scale.identity().domain([-_width*3, _width*3]))
				.y(d3.scale.identity().domain([-_width*3, _height*3]))
				.on("brushstart", brushStart)
				.on("brush", brushing)
				.on("brushend", brushEnd);
			
		    brushCanvas = d3.select('div.canvas>svg').call(zoom);
		     brushCanvas.append('g').attr( "class", "brush" ).call(brush);
		     //  canvas=canvas.on("dblclick.zoom", null).on('mousedown.zoom',null).append('g').attr('class','nodes').call(zoom);
			    canvas=canvas.on("dblclick.zoom", null).on('mousedown.zoom',null).append('g').attr('class','nodes');
			// 组合框的边界线条
		//	 canvas.append('rect').attr('id','groupborder')
			 // 动态坐标线在nodes上方
			 activeAxisXLine=brushCanvas.append('line')
			 .attr('x1',0)
			 .attr('y1',4)
			 .attr('x2',_width)
			 .attr('y2',4)
			 .classed('coordinate-X',true);
			 
			 
			 activeAxisYLine=brushCanvas.append('line')
			 .attr('x1',4)
			 .attr('y1',0)
			 .attr('x2',4)
			 .attr('y2',_height)
			 .classed('coordinate-Y',true);
		}
	}
	
		
	function resize(){
		d3.selectAll('.canvas .axis').remove();
		 divContainer = d3.select('.canvas')[0][0];
		// X坐标线
		 if(_grid==1&&xLines)
		 xLines=background.insert("g",":first-child")
		 .attr("class", "x axis")
		 .selectAll("line")
		 .data(d3.range(15, $(document).width(), 15))
		 .enter().append("line")
		 .attr("x1", function(d) { return d; })
		 .attr("y1", 0)
		 .attr("x2", function(d) { return d; })
		 .attr("y2", $(document).height());
		 
		 activeAxisXLine=d3.select('.coordinate-X').attr('x2',$(document).width());
		
		 
		 // Y坐标线
		 if(_grid==1&&yLines)
		 yLines=background.insert("g",":first-child")
		 .attr("class", "y axis")
		 .selectAll("line")
		 .data(d3.range(15, $(document).height(), 15))
		 .enter().append("line")
		 .attr("x1", 0)
		 .attr("y1", function(d) { return d; })
		 .attr("x2", $(document).width())
		 .attr("y2", function(d) { return d; });
		 $('.canvas').css({'width':$(document).width(),'height':$(document).height()});
		 activeAxisYLine=d3.select('.coordinate-Y').attr('y2',$(document).height());
		 if(brush)
		 brush.x(d3.scale.identity().domain([0, $(document).width()*3]))
				.y(d3.scale.identity().domain([0, $(document).height()*3]));
		
	}
	
	function ZoomIn(d, i) {
		  var k = r / d.r / 2;
		  xScale.domain([d.x - d.r, d.x + d.r]);
		  yScale.domain([d.y - d.r, d.y + d.r]);

		  var t = canvas.transition()
		      .duration(d3.event.altKey ? 7500 : 750);

		  t.selectAll("circle")
		      .attr("cx", function(d) { return xScale(d.x); })
		      .attr("cy", function(d) { return yScale(d.y); })
		      .attr("r", function(d) { return k * d.r; });

		  t.selectAll("text")
		      .attr("x", function(d) { return xScale(d.x); })
		      .attr("y", function(d) { return yScale(d.y); })
		      .style("opacity", function(d) { return k * d.r > 20 ? 1 : 0; });

		  node = d;
		  d3.event.stopPropagation();
		}
	function resetDrawLine(){
		drawLineSelected  =  false;
	}

	function resetMouseVars() {
		  mousedown_node = null;
		  mouseup_node = null;
		  mousedown_link = null;
		  mouseover_node = null;
		  mouseover_link = null;
		  mouseover_pan = false;
		  mouseover_nodelabel = false;
		  
		}
	
	
	// brush related functions
	
	function brushStart(){
		
		// if (d3.event.defaultPrevented) return;
		
		// d3.event.sourceEvent.stopPropagation();//阻止冒泡事件
			nodeData = d3.selectAll('g.node svg');
			d3.selectAll('g.misc').classed('selected',false).classed('dragged',false);
			initSelectNodeToResize();
	}
	
	function brushing(){
		var extent = d3.event.target.extent();
	      
			nodeData[0].forEach(function (e){
				d3.select(e).each(function(d){
					if (extent[0][0]<= d.x*scaleFactor+translation[0] && d.x*scaleFactor+translation[0]< extent[1][0]&& (extent[0][1]) <= d.y*scaleFactor+translation[1]&& d.y*scaleFactor+translation[1]<extent[1][1])
					{
						if(d3.select(this.parentNode).classed('selected')){
							
						}else{
							d3.select(this.parentNode).classed('selected',true).classed('dragged',true);
							selectNodeToResize(d);
						}

					}
					else{
						d3.select(this.parentNode).classed('selected',false).classed('dragged',false);
					//	initSelectNodeToResize();
					}

				});
			});
			
			d3.select('path.link.selected').classed('selected',false);
			selected_link=null;
	}
	
	function brushEnd(){
		triggerSelectedEvent();
		d3.event.target.clear();
		d3.select(this).call(d3.event.target);
	}
	// end brush related functions
	
	
	// drag related functions
    function dragStart(data,dragObj) {
    	// 防止触发除了drag以外的事件
    	mousedown_clicknode = data;
    	        d3.event.sourceEvent.stopPropagation();
    	        d3.event.sourceEvent.preventDefault();
    	        
    	    	force.stop();
    	    	if(drawLineSelected || dragMode)return;
    	    	 nodeData = d3.selectAll('g .node svg,g .misc svg').data();
    	    	 
    	       if(!d3.select(dragObj.parentNode).classed('dragged') && lastKeyDown != 17)
    	    	   d3.selectAll('.node.selected,.misc.selected').classed('selected',false).classed('dragged',false);
    	       		initSelectNodeToResize();
    	    		 d3.select(dragObj.parentNode).classed('dragging',function(){
    	    			    if(data.nodeType == 'group'){
    	    				 data.ids.map(function(nodeId){
    	    					 var nd = d3.select('#node' + nodeId + ' svg').datum();
    	    					 if(nd.groupId != data.id){
    	    						 data.ids.splice(data.ids.indexOf(nodeId),1);
    	    					 }
    	    					 else{
    	    						 d3.select('#node' + nodeId).classed('dragging',true);
    	    					 }
    	    					
    	    				 });
    	    			 }
    	    			    return true;
    	    		 });
    	}
    
    function dragged(data,dragObj){
    		tooltip.style("display", "none");
    		initSelectNodeToResize()
        	var selectedNodes = d3.selectAll('.node.dragged svg,.node.dragging svg,.misc.dragging svg');
        	selectedNodes[0].forEach(function(e){
             	d3.select(e).each(function(d){
             			d.x += d3.event.dx;
                 		d.y += d3.event.dy;
                 		if(d3.event.dx > 3 || d3.event.dy > 3){
                 			triggerChangedEvent(true);
                 		}
                 		var lx ,ly;// label的x、y
                 		var gd = null;
                 		if(d.groupId){
                 		  gd = d3.select('#group' + d.groupId + ' svg').data()[0];
                 		  mousedown_clicknodegroup = gd;
                 		}
                 		d3.select(e).attr('x',function(){// 判断组合节点是否大于组合图案边界
                 			if(gd){
                 				if(d.x - d.width / 2 < gd.x - gd.width / 2 - 5){
                 					d.x = gd.x - gd.width / 2 + d.width / 2;
                 					lx = gd.x - gd.width / 2 + d.width / 2;
                 					removeToGroup(d,gd);
                 					return gd.x - gd.width / 2;
                 				}
                 				else if(d.x + d.width / 2 > gd.x + gd.width / 2 + 5){
                 					d.x = gd.x + gd.width / 2 - d.width / 2;
                 					lx = gd.x + gd.width / 2 - d.width + d.width / 2;
                 					removeToGroup(d,gd);
                 					return gd.x + gd.width / 2 - d.width;
                 				}
                 			}
                 			lx = d.x;
                 			return d.x-d.width/2;
                 			
                 			})
                 		.attr('y',function(){
                 			// 由于图形长宽不一致，size需改为显示长宽比
                 			if(gd) {
                 			 if(d.y-d.height/2<gd.y-gd.height/2){
                 				 d.y = gd.y-gd.height/2+d.height/2;
                 				 ly = gd.y-gd.height/2+d.height+0.3*d.height;
                 				removeToGroup(d,gd);
                 			 return gd.y-gd.height/2;
                 			 }
                 			 else if(d.y+d.height/2>gd.y+gd.height/2){
                 				 d.y = gd.y+gd.height/2-d.height/2;
                 				 ly = gd.y+gd.height/2+0.3*d.height;
                 				removeToGroup(d,gd);
                 			 return gd.y+gd.height/2-d.height;
                 			 }
                 			} 
                 			ly =d.y+d.height/2+12;
                 			return d.y-d.height/2;
                 		}
                 			);

                     	// 移动标签
                 		if(d3.select(this.parentNode).select('.label')[0].length!=0){
                 		 	d3.select(this.parentNode).select('.label').each(function(d){
                 		  		d.x =lx;// += d3.event.dx;
                 		        d.y =ly;// += d3.event.dy;
                     		var editNode = d3.select(this);
                     		editNode.attr('x',function(d){return d.x;})
                         	.attr('y',function(d){return d.y;});
                     		
                     	});  
                     	} 		
             	});
             	
             });
            
            // 动态X-Y坐标线
        		if(!_viewOnly){
        			  for (e in nodeData ){       	            	
        	            	if(nodeData[e].id!=data.id&&Math.abs(nodeData[e].x-data.x)<=2){
        	        			activeAxisYLine.classed('activeAxis',true);
        	        			activeAxisYLine.attr('x1',data.x*scaleFactor+translation[0]);
        	        	        activeAxisYLine.attr('x2',data.x*scaleFactor+translation[0]);
        	        	        break;
        	        		}else{
        	        			activeAxisYLine.classed('activeAxis',false);
        	        		}
        	            }
        	            for(e in nodeData){
        	            	if(nodeData[e].id!=data.id&&Math.abs(nodeData[e].y-data.y)<=2){
        	        			activeAxisXLine.classed('activeAxis',true);
        	        			 activeAxisXLine.attr('y1',data.y*scaleFactor+translation[1]);
        	        		      activeAxisXLine.attr('y2',data.y*scaleFactor+translation[1]);
        	        		      break;
        	        		}
        	        		else{
        	        			activeAxisXLine.classed('activeAxis',false);
        	        		}
        	            }
        		}
          
            force.start();
        }
    
    function dragEnd(data,dragObj){
    	if(!_viewOnly){
    	 	activeAxisYLine.classed('activeAxis',false);
        	activeAxisXLine.classed('activeAxis',false); 
    	}   	
    	d3.select(dragObj.parentNode).classed('dragging',function(){
    		 if(data.nodeType=='group'){
				 data.ids.map(function(nodeId){
					d3.select('#node'+nodeId).classed('dragging',false);
				 });
    		 }
				 return false;
    	});
   	
   	   // 防止图标重叠
    	 var overlapped=false;
        for(e in nodeData){
        	if(data.type=='node'&&!data.groupId&&nodeData[e].type!='group'&&nodeData[e].id!=data.id&&Math.abs(nodeData[e].x-data.x)<=data.size*data.width&&Math.abs(nodeData[e].y-data.y)<=data.size*data.height){
        		alert('true')
        		overlapped=true;
        		data.x+=data.width;
        		data.y+=data.height;
        		d3.select(dragObj).attr('x',data.x-data.width/2).attr('y',data.y-data.height/2);
    	        }
     	   if(nodeData[e].nodeType=='group'&&data.nodeType!='group'){
     		   if(!data.groupId&&data.x>nodeData[e].x-nodeData[e].width/2+data.width/2&&data.x<nodeData[e].x+nodeData[e].width/2-data.width/2&&data.y>nodeData[e].y-nodeData[e].height/2+data.height/2&&data.y<nodeData[e].y+nodeData[e].height/2-data.height/2){
     			 addToGroup(data,nodeData[e]);
     		   }
     	   }
        }
        
        if(overlapped){
    	// 重新加载标签
    	d3.select(dragObj.parentNode).select('.label').remove();
    	d3.select(dragObj.parentNode).append('text')
       .datum({'x' : data.x,'y' : data.y+data.height/2+12})
       .attr('class','label')
		.attr('text-anchor','middle')
		.attr('x', data.x)
		.attr('y', data.y+data.height/2+12)
		.attr('dominant-baseline','central')
		.attr('font-size','12px')
		.style('fill',_defaultColor).text(data.name)
		.on('mouseover',function(){
			mouseover_nodelabel=true;
			d3.select(this).style('fill','#ff0');
			})
		.on('mouseleave',function(){
			mouseover_nodelabel=false;
			d3.select(this).style('fill',null);
			})
		.on('click',function(){
			
		});
        } 
        selectNodeToResize(data);
    }
    // end drag related functions
    function moveSelected(dx,dy){
    	
    	if(drawLineSelected)return;
    	
    	var selectedNodes = d3.selectAll('.node.selected svg');
    	if(selectedNodes[0].length==0)return;
    	force.stop();
    	 nodeData=d3.selectAll('g .node svg').data();
    	selectedNodes[0].forEach(function(e){
         	d3.select(e).each(function(d){
         			d.x+=dx;
             		d.y+=dy;
             		d3.select(e).attr('x',d.x-d.width/2).attr('y',d.y-d.height/2);
             	
                 	// 移动标签
             		if(d3.select(this.parentNode).select('.label')[0].length!=0){
             		 	d3.select(this.parentNode).select('.label').each(function(d){
             		  		d.x += dx;
             		        d.y += dy;
                 		var editNode = d3.select(this);
                 		editNode.attr('x',function(d){return d.x;})
                     	.attr('y',function(d){return d.y+12;});
                 	});  
                 	}
         		
         	});
         	
         });
        
    	if(selectedNodes[0].length==1){
    	d3.select(selectedNodes[0][0]).each(function(data){
    		 for (e in nodeData ){
    	        	
    	        	if(nodeData[e].id!=data.id&&Math.abs(nodeData[e].x-data.x)<=2){
    	    			activeAxisYLine.classed('activeAxis',true);
    	    			activeAxisYLine.attr('x1',data.x*scaleFactor+translation[0]);
    	    	        activeAxisYLine.attr('x2',data.x*scaleFactor+translation[0]);
    	    	        break;
    	    		}else{
    	    			activeAxisYLine.classed('activeAxis',false);
    	    		}
    	        }
    	        for(e in nodeData){
    	        	if(nodeData[e].id!=data.id&&Math.abs(nodeData[e].y-data.y)<=2){
    	    			activeAxisXLine.classed('activeAxis',true);
    	    			 activeAxisXLine.attr('y1',data.y*scaleFactor+translation[1]);
    	    		      activeAxisXLine.attr('y2',data.y*scaleFactor+translation[1]);
    	    		      break;
    	    		}
    	    		else{
    	    			activeAxisXLine.classed('activeAxis',false);
    	    		}
    	        }
    	});
    	
    	}
        // 动态X-Y坐标线
       
        force.start();
        
    }
    
    function mousemove() {
		if (!mousedown_node)return;
			d3.event.stopPropagation();

			switch (lineType) {
				case lineTypes.straight:
					// 更新画线 直线
					drag_line.attr('d', 'M' + mousedown_node.x + ',' + mousedown_node.y
							+ 'L' + (d3.mouse(this)[0] - translation[0]) / scaleFactor
							+ ',' + (d3.mouse(this)[1] - translation[1]) / scaleFactor); 
					break; 

				case lineTypes.curve:
						var deltax = d3.mouse(this)[0] - mousedown_node.x, 
							deltay = d3.mouse(this)[1]- mousedown_node.y, 
							dist = Math.sqrt(deltax * deltax+ deltay * deltay);
						// 曲线
						if(d3.mouse(this)[0]>mousedown_node.x){
							drag_line.attr('d', 'M' + mousedown_node.x + ',' + mousedown_node.y
									+ 'A' + dist + ',' + dist + ' 0 0 ,1 '
									+ (d3.mouse(this)[0] - translation[0]) / scaleFactor + ','
									+ (d3.mouse(this)[1] - translation[1]) / scaleFactor);
						}
						else if(d3.mouse(this)[0]<mousedown_node.x){
							drag_line.attr('d', 'M' + mousedown_node.x + ',' + mousedown_node.y
									+ 'A' + dist + ',' + dist + ' 0 0 ,0 '
									+ (d3.mouse(this)[0] - translation[0]) / scaleFactor + ','
									+ (d3.mouse(this)[1] - translation[1]) / scaleFactor);
						}
						
						break;
				case lineTypes.singlepolyline:
					if((d3.mouse(this)[1] - translation[1]) / scaleFactor<mousedown_node.y){
						drag_line.attr('d', 'M' + mousedown_node.x + ',' + mousedown_node.y
								+ ' L' + mousedown_node.x + ',' +  ((d3.mouse(this)[1] - translation[1]) / scaleFactor )
								+'L'+ (d3.mouse(this)[0] - translation[0]) / scaleFactor + ','+ (d3.mouse(this)[1] - translation[1]) / scaleFactor);
					}
					else{
						drag_line.attr('d', 'M' + mousedown_node.x + ',' + mousedown_node.y
								+ ' L' + mousedown_node.x + ',' +  ((d3.mouse(this)[1] - translation[1]) / scaleFactor)
								+'L'+ (d3.mouse(this)[0] - translation[0]) / scaleFactor + ','+ (d3.mouse(this)[1] - translation[1]) / scaleFactor);
					}

					break;

				case lineTypes.doublepolyline:
					if((d3.mouse(this)[1] - translation[1]) / scaleFactor<mousedown_node.y){
						drag_line.attr('d', 'M' + mousedown_node.x + ',' + mousedown_node.y
								+ ' L' + mousedown_node.x + ',' +  ((d3.mouse(this)[1] - translation[1]) / scaleFactor -50)
								+ ' L' +(d3.mouse(this)[0] - translation[0]) / scaleFactor+','+ ((d3.mouse(this)[1] - translation[1]) / scaleFactor-50)
								+'L'+ (d3.mouse(this)[0] - translation[0]) / scaleFactor + ','+ (d3.mouse(this)[1] - translation[1]) / scaleFactor);
					}
					else{
						drag_line.attr('d', 'M' + mousedown_node.x + ',' + mousedown_node.y
								+ ' L' + mousedown_node.x + ',' +  ((d3.mouse(this)[1] - translation[1]) / scaleFactor +50)
								+ ' L' +(d3.mouse(this)[0] - translation[0]) / scaleFactor+','+ ((d3.mouse(this)[1] - translation[1]) / scaleFactor+50)
								+'L'+ (d3.mouse(this)[0] - translation[0]) / scaleFactor + ','+ (d3.mouse(this)[1] - translation[1]) / scaleFactor);
					}
					break;
			}
}
    
    function pol( progress,  radius)
    {
    return (Math.sin(progress * Math.PI * 2.0) * radius) + "," 
    + (Math.cos(progress * Math.PI * 2.0) * -radius);
    }
    function calArrowPath(sx,sy,x,y){
    	var deltaX = x - sx,
	     deltaY = y- sy,
	     dist = 20,
	     normX = deltaX/dist,
	     normY = deltaY/dist;
    		
    		return "M"+pol(0.01,100)+'A100,100 0 0,1 '+pol(0.19,100)+' L'+pol(0.21,100)+'L'+pol(0.19,100)+'A100,100 0 0,0 '+pol(0.01,100)+'L'+pol(0.03,100)+'Z';
    }
 // update force layout (called automatically each iteration)
	function tick() {
		// draw directed edges with proper padding from node centers
		path.attr('d',function(d) {
							var deltaX = d.target.x - d.source.x,
							     deltaY = d.target.y- d.source.y, 
							    dist = Math.sqrt(deltaX* deltaX + deltaY * deltaY),
							normX = deltaX / dist,
							tan = deltaY / deltaX, 
							sin = deltaY / dist,
							cos = deltaX / dist,
							normY = deltaY / dist,

							sourceHeightPadding = d.source.height,
							targetHeightPadding = d.target.height,
							sourceWidthPadding = d.source.width,
							targetWidthPadding = d.target.width;
							
							if (d.direct && d.type == lineTypes.straight) {
								var xacos = Math.asin(normX),
									yasin = Math.asin(normY),
									sourceX = d.source.x + (sourceHeightPadding * Math.sin(xacos - 0.5 * d.direct)),
									sourceY = d.source.y + (sourceHeightPadding * Math.sin(yasin - 0.5 * d.direct)),
									targetX = d.target.x - (targetWidthPadding * Math.sin(xacos + 0.5 * d.direct)),
									targetY = d.target.y - (targetWidthPadding * Math.sin(yasin + 0.5 * d.direct));
							} else {
								if (Math.abs(tan) < (sourceHeightPadding / sourceWidthPadding) && d.target.x >= d.source.x && d.target.y <= d.source.y) { // 0-45
									sourceX = d.source.x + sourceWidthPadding / 2,
										sourceY = d.source.y - Math.sqrt((sourceWidthPadding * dist * sourceWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((sourceWidthPadding * sourceWidthPadding) / 4));
									if (Math.abs(tan) < (targetHeightPadding / targetWidthPadding)) { // 180-225
										targetX = d.target.x - targetWidthPadding / 2,
											targetY = d.target.y + Math.sqrt((targetWidthPadding * dist * targetWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((targetWidthPadding * targetWidthPadding) / 4));
									} else { // 225-270
										targetX = d.target.x - Math.sqrt(((targetHeightPadding * dist * targetHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((targetHeightPadding * targetHeightPadding) / 4))),
											targetY = d.target.y + targetHeightPadding / 2;
									}
								} else if (Math.abs(tan) >= (sourceHeightPadding / sourceWidthPadding) && d.target.x >= d.source.x && d.target.y <= d.source.y) { // 45-90
									sourceX = d.source.x + Math.sqrt(((sourceHeightPadding * dist * sourceHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((sourceHeightPadding * sourceHeightPadding) / 4))),
										sourceY = d.source.y - sourceHeightPadding / 2;
									if (Math.abs(tan) < (targetHeightPadding / targetWidthPadding)) { // 180-225
										targetX = d.target.x - targetWidthPadding / 2,
											targetY = d.target.y + Math.sqrt((targetWidthPadding * dist * targetWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((targetWidthPadding * targetWidthPadding) / 4));
									} else { // 225-270
										targetX = d.target.x - Math.sqrt(((targetHeightPadding * dist * targetHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((targetHeightPadding * targetHeightPadding) / 4))),
											targetY = d.target.y + targetHeightPadding / 2;
									}

								} else if (Math.abs(tan) >= (sourceHeightPadding / sourceWidthPadding) && d.target.x <= d.source.x && d.target.y <= d.source.y) { // 90-135
									sourceX = d.source.x - Math.sqrt(((sourceHeightPadding * dist * sourceHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((sourceHeightPadding * sourceHeightPadding) / 4))),
										sourceY = d.source.y - sourceHeightPadding / 2;
									if (Math.abs(tan) >= (targetHeightPadding / targetWidthPadding)) {// 270-315
										targetX = d.target.x + Math.sqrt(((targetHeightPadding * dist * targetHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((targetHeightPadding * targetHeightPadding) / 4))),
										targetY = d.target.y + targetHeightPadding / 2;
									} else{// 315-360
										targetX = d.target.x + targetWidthPadding / 2,
										targetY = d.target.y + Math.sqrt((targetWidthPadding * dist * targetWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((targetWidthPadding * targetWidthPadding) / 4));
									}

								} else if (Math.abs(tan) < (sourceHeightPadding / sourceWidthPadding) && d.target.x <= d.source.x && d.target.y <= d.source.y) { // 135-180
									sourceX = d.source.x - sourceWidthPadding / 2,
										sourceY = d.source.y - Math.sqrt((sourceWidthPadding * dist * sourceWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((sourceWidthPadding * sourceWidthPadding) / 4));
										if (Math.abs(tan) >= (targetHeightPadding / targetWidthPadding)) {// 270-315
										targetX = d.target.x + Math.sqrt(((targetHeightPadding * dist * targetHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((targetHeightPadding * targetHeightPadding) / 4))),
										targetY = d.target.y + targetHeightPadding / 2;
									} else{// 315-360
										targetX = d.target.x + targetWidthPadding / 2,
										targetY = d.target.y + Math.sqrt((targetWidthPadding * dist * targetWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((targetWidthPadding * targetWidthPadding) / 4));
									}

								} else if (Math.abs(tan) < (sourceHeightPadding / sourceWidthPadding) && d.target.x <= d.source.x && d.target.y >= d.source.y) { // 180-225
									sourceX = d.source.x - sourceWidthPadding / 2;
										sourceY = d.source.y + Math.sqrt((sourceWidthPadding * dist * sourceWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((sourceWidthPadding * sourceWidthPadding) / 4));
										if (Math.abs(tan) < (targetHeightPadding / targetWidthPadding)) { // 0-45
										targetX = d.target.x + targetWidthPadding / 2,
											targetY = d.target.y - Math.sqrt((targetWidthPadding * dist * targetWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((targetWidthPadding * targetWidthPadding) / 4));
									} else { // 225-270
										targetX = d.target.x + Math.sqrt(((targetHeightPadding * dist * targetHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((targetHeightPadding * targetHeightPadding) / 4))),
											targetY = d.target.y - targetHeightPadding / 2;
									}
								} else if (Math.abs(tan) >= (sourceHeightPadding / sourceWidthPadding) && d.target.x <= d.source.x && d.target.y >= d.source.y) { // 225-270
									sourceX = d.source.x - Math.sqrt(((sourceHeightPadding * dist * sourceHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((sourceHeightPadding * sourceHeightPadding) / 4))),
										sourceY = d.source.y + sourceHeightPadding / 2;
									if (Math.abs(tan) < (targetHeightPadding / targetWidthPadding)) { // 45-90
										targetX = d.target.x + targetWidthPadding / 2,
											targetY = d.target.y - Math.sqrt((targetWidthPadding * dist * targetWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((targetWidthPadding * targetWidthPadding) / 4));
									} else { // 225-270
										targetX = d.target.x + Math.sqrt(((targetHeightPadding * dist * targetHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((targetHeightPadding * targetHeightPadding) / 4))),
											targetY = d.target.y - targetHeightPadding / 2;
									}
								} else if (Math.abs(tan) >= (sourceHeightPadding / sourceWidthPadding) && d.target.x >= d.source.x && d.target.y >= d.source.y) { // 270-315
									sourceX = d.source.x + Math.sqrt(((sourceHeightPadding * dist * sourceHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((sourceHeightPadding * sourceHeightPadding) / 4))),
										sourceY = d.source.y + sourceHeightPadding / 2;
									if (Math.abs(tan) >= (targetHeightPadding / targetWidthPadding)) {// 270-315
										targetX = d.target.x - Math.sqrt(((targetHeightPadding * dist * targetHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((targetHeightPadding * targetHeightPadding) / 4))),
										targetY = d.target.y - targetHeightPadding / 2;
									} else{// 315-360
										targetX = d.target.x - targetWidthPadding / 2,
										targetY = d.target.y - Math.sqrt((targetWidthPadding * dist * targetWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((targetWidthPadding * targetWidthPadding) / 4));
									}
								} else if (Math.abs(tan) < (sourceHeightPadding / sourceWidthPadding) && d.target.x >= d.source.x && d.target.y >= d.source.y) { // 315-360
									sourceX = d.source.x + sourceWidthPadding / 2,
										sourceY = d.source.y + Math.sqrt((sourceWidthPadding * dist * sourceWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((sourceWidthPadding * sourceWidthPadding) / 4));
									if (Math.abs(tan) >= (targetHeightPadding / targetWidthPadding)) {// 270-315
										targetX = d.target.x - Math.sqrt(((targetHeightPadding * dist * targetHeightPadding * dist) / (deltaY * 2 * deltaY * 2) - ((targetHeightPadding * targetHeightPadding) / 4))),
										targetY = d.target.y - targetHeightPadding / 2;
									} else{// 315-360
										targetX = d.target.x - targetWidthPadding / 2,
										targetY = d.target.y - Math.sqrt((targetWidthPadding * dist * targetWidthPadding * dist) / (deltaX * 2 * deltaX * 2) - ((targetWidthPadding * targetWidthPadding) / 4));
									}
								}

							}
							
							
							var df = null;
						
							switch (d.type) {
							case lineTypes.straight:
								// Ö±Ïß

								if (d.source.x > d.target.x){
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset','30%').attr('transform', 'rotate(180 ' + (sourceX + targetX) / 2 + ' ' + (sourceY + targetY) / 2 + ')');									
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(180 ' + (sourceX + targetX) / 2 + ' ' + (sourceY + targetY) / 2 + ')');									
								}
								else{
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset','30%').attr('transform', 'rotate(0 0 0 )');
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(0 0 0 )');
								}
								df = 'M' + sourceX + ',' + sourceY + 'L' + targetX + ',' + targetY;
								d3.select('#linkbg' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).attr('d', df);
								return df; // +calArrowPath(sourceX,sourceY,targetX,
											// targetY);
							case lineTypes.curve:

								if (d.source.x > d.target.x) {
									d3.select('defs')
									.select('#textpath' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id)
									.attr('d',function(){
										d3.selectAll('#label'+ d.source.nodeType + d.source.id + d.target.nodeType + d.target.id)
										.select('.linklabel')
										.select('.textpath')
										.attr('xlink:href',function(){
										return '#textpath'+ d.source.nodeType + d.source.id + d.target.nodeType + d.target.id ;
									});
										return  "M" +targetX  + "," +targetY  + "A"+ dist + "," + dist + " 0 0,1 "+ sourceX + "," +sourceY ;
										
									});	
									
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath')
									.attr('startOffset','30%')
									
								// d3.selectAll('#label' + d.source.nodeType +
								// d.source.id + d.target.nodeType +
								// d.target.id).select('.linklabel').attr('transform',
								// 'rotate(180 ' +(d.source.x+d.target.x)/2+'
								// '+(d.target.y+d.source.y)/2+' )');
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(0 0 0)');									
								// d3.selectAll('#link' + d.source.nodeType +
								// d.source.id + d.target.nodeType +
								// d.target.id).attr('transform', 'rotate(180 '
								// +(d.source.x+d.target.x)/2+'
								// '+(d.target.y+d.source.y)/2+' )')
									df = "M" + sourceX + "," + sourceY + "A" + dist + "," + dist + " 0 0,0 " + targetX + "," + targetY;
								// df = "M" + targetX + "," + targetY + "A" +
								// dist + "," + dist + " 0 0,0 " + sourceX + ","
								// + sourceY;
										d3.select('#linkbg' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).attr('d', df);
										return df;
								} else {
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset','30%');
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(0 0 0 )');
									df = "M" + sourceX + "," + sourceY + "A" + dist + "," + dist + " 0 0,0 " + targetX + "," + targetY;
								}
								d3.select('#linkbg' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).attr('d', df);
								return df;
								break;
							case lineTypes.singlepolyline:

								if (d.target.y < d.source.y && d.target.x > d.source.x) {
										d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset',100*(1-(d.target.x-d.source.x)/((d.target.x-d.source.x)+(d.source.y-d.target.y)))+'%');
										d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(0 0 0 )');
										df = 'M' + d.source.x + ',' + (d.source.y - d.source.height/2) + ' L' + d.source.x + ',' + (d.target.y) + 'L' + (d.target.x - d.target.width / 2) + ',' + (d.target.y);
										d3.select('#linkbg' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).attr('d', df);
										return df
								} else if (d.target.y <= d.source.y && d.target.x <= d.source.x) {
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset',100*((d.source.y-d.target.y)/((d.source.x-d.target.x)+(d.source.y-d.target.y))+0.05)+'%');
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(-180 '+(d.source.x+d.target.x)/2+' '+d.target.y+')');
							
									df = 'M' + d.source.x + ',' + (d.source.y - d.source.height/2) + ' L' + d.source.x + ',' + d.target.y + 'L' + (d.target.x + d.target.width / 2) + ',' + d.target.y;
									d3.select('#linkbg' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).attr('d', df);
									return df;
								} else if (d.target.y > d.source.y && d.target.x > d.source.x) {
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset',100*((d.target.y-d.source.y)/((d.target.x-d.source.x)+(d.target.y-d.source.y)))+'%');
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(0 0 0 )');

									df = 'M' + d.source.x + ',' + (d.source.y + d.source.height/2) + ' L' + d.source.x + ',' + d.target.y + 'L' + (d.target.x - d.target.width / 2) + ',' + d.target.y;
									d3.select('#linkbg' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).attr('d', df);
									return df;
								} else {
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset',100*((d.target.y-d.source.y)/((d.source.x-d.target.x)+(d.target.y-d.source.y))+0.05)+'%');
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(-180 '+(d.source.x+d.target.x)/2+' '+d.target.y+')');
									df = 'M' + d.source.x + ',' + (d.source.y + d.source.height/2) + ' L' + d.source.x + ',' + d.target.y + 'L' + (d.target.x + d.target.width / 2) + ',' + d.target.y;
									d3.select('#linkbg' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).attr('d', df);
									return df;
								}

								break;

							case lineTypes.doublepolyline:
								if (d.source.x > d.target.x && d.source.y > d.target.y ) {
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset', 100*(((d.source.y-d.target.y)+d.target.height)/((d.source.x-d.target.x)+(d.source.y-d.target.y)+d.target.height*2)+0.05)+'%');
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(-180 '+(d.source.x+d.target.x)/2+' '+(d.target.y-d.target.height*1.5)+')');
							// d3.selectAll('#label' + d.source.nodeType +
							// d.source.id + d.target.nodeType +
							// d.target.id).select('.linklabel').attr('transform',
							// 'rotate(0 0 0)');

								} else if (d.source.x > d.target.x && d.source.y < d.target.y ) {
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset', 100*(((d.target.y-d.source.y)+d.target.height)/((d.source.x-d.target.x)+(d.target.y-d.source.y)+d.target.height*2)+0.05)+'%');
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(-180 '+(d.source.x+d.target.x)/2+' '+(d.target.y+d.target.height*1.5)+')');
							// d3.selectAll('#label' + d.source.nodeType +
							// d.source.id + d.target.nodeType +
							// d.target.id).select('.linklabel').attr('transform',
							// 'rotate(0 0 0)');

								} else if (d.source.x < d.target.x && d.source.y > d.target.y ){
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset', 100*(((d.source.y-d.target.y)+d.target.height)/((d.target.x-d.source.x)+(d.source.y-d.target.y)+d.target.height*2)+0.05)+'%');
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(0 0 0 )');
								} else if (d.source.x < d.target.x && d.source.y < d.target.y ){
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').select('.textpath').attr('startOffset', 100*(((d.target.y-d.source.y)+d.target.height)/((d.target.x-d.source.x)+(d.target.y-d.source.y)+d.target.height*2)+0.05)+'%');
									d3.selectAll('#label' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).select('.linklabel').attr('transform', 'rotate(0 0 0 )');									
								}
								if (d.target.y < d.source.y) {
									df = 'M' + d.source.x + ',' + (d.source.y - d.source.height/2) + ' L' + d.source.x + ',' + (d.target.y - d.target.height * 1.5) + ' L' + d.target.x + ',' + (d.target.y - d.target.height * 1.5) + 'L' + d.target.x + ',' + (d.target.y - d.target.height);
									d3.select('#linkbg' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).attr('d', df);
									return df;
								} else {
									df = 'M' + d.source.x + ',' + (d.source.y + d.source.height) + ' L' + d.source.x + ',' + (d.target.y + d.target.height * 1.5) + ' L' + d.target.x + ',' + (d.target.y + d.target.height * 1.5) + 'L' + d.target.x + ',' + (d.target.y + d.target.height);

									d3.select('#linkbg' + d.source.nodeType + d.source.id + d.target.nodeType + d.target.id).attr('d', df);
									return df;
								}
								
								break;
							
							}
							
							for(var e in path[0]){
								
								d3.select(path[0][e]).each(function(l){
									edges.push(l)
									
									})
									}
						});
	/* edges = []; */
		
			if(path[0].length!=0){
				edges=[];
				for(var i = 0;i<path[0].length;i++){
					d3.select(path[0][i]).each(function(l){
						edges.push(l)
						
						})
						}
			}

		


	}
	
	function spliceLinksForNode(node) {
		var toSplice = _links.filter(function(l) {
			return (l.source.id == node.id || l.target.id == node.id);
		});
		toSplice.map(function(l) {
			
			// 删除线上的标签
			d3.select('#label'+l.source.nodeType+l.source.id+l.target.nodeType+l.target.id).remove();
			// 删除线上的箭头
			d3.select('#end-arrow'+l.source.nodeType+l.source.id+l.target.nodeType+l.target.id).remove();
			_links.splice(_links.indexOf(l), 1);
			
		});
	}

	function showLineAnchor(node,d) {
		var activeNode = d3.select(node.parentNode);
		activeNode.insert('circle',':first-child')
		.attr('class','linkable')
		.datum({'x' : d.x,'y' : d.y})
		.attr('cx',d.x)
		.attr('cy',d.y)
		.attr('r',d.width+5);
		
	}
    topoEditor.showToolTip = function(d){
    	tooltip.style("display", "none");
        tooltip.html('');
    	showToolTip(d,450);
    }
    
    
  function editLabel(d,l,elm){
	  d3.selectAll('.node.selected,.misc.selected').classed('selected',false).classed('dragged',false);
	  selected_link = null;
    		labelBox.style('display','block')
		.style('top',(l.y*scaleFactor+translation[1])-10+'px')
		.style('left',(l.x*scaleFactor+translation[0])-100+'px')
        .append("input").attr("style", "width: 200px;border-radius:5px;")
            .attr("value", function() {
                this.focus();
                return d.name;
            })
            .on("blur", function() {
                var txt = this.value;
                if(txt&&txt.trim()!=''){
                	  d.name = txt;
                  elm.text(txt);
                  triggerChangedEvent(true);
                }
              
                labelBox.style('display','none');
                d3.select(this).remove();
                
            });
    }
	function showToolTip(d,tiplength) {
        var res;
        if (!d) {
            tooltip.style("display", "none");
            tooltip.html('');
            return;
        }
        if (tooltip.style("display") == "none") {
        	tooltip.style("opacity",0);
            res = [];
            res.push((d.x*scaleFactor+translation[0])> _width / 2?"<div class='arrow-right'></div>":"<div class='arrow-left'></div>");
            res.push(d.info);
            tooltip.style("display", "block");
            tooltip.style("width",tiplength?tiplength+"px":"200px");
            if (d) {
                tooltip.style("top",(d.y*scaleFactor-30+translation[1])+"px")
                	   .style("left",(d.x*scaleFactor+translation[0])>_width/2 ? (d.x*scaleFactor-tooltip.node().clientWidth-32)+translation[0]+"px":(d.x*scaleFactor+45+translation[0])+"px");
                
                tooltip.transition().style("opacity",1);
                tooltip.html(res.join(''));
            }
        }
    }
	
	topoViewer.getScaleFactor = function(){
		return scaleFactor;
	};
	
	topoEditor.getScaleFactor = function(){
		return scaleFactor;
	};
	
	topoViewer.getTranslation = function(){
		return translation;
	};
	topoEditor.getTranslation = function(){
		return translation;
	}
	topoViewer.getWidth = function(){
		return _width;
	};
	topoEditor.getWidth = function(){
		return _width;
	}
	topoViewer.getTopoId = function(){
		return getTopoId();
	};
	topoEditor.getTopoId = function(){
		return getTopoId();
	};
	function getTopoId(){
		return _topoId;
	}
	topoEditor.getStraightLine = function(){
		return lineTypes.straight;
	};
	topoEditor.getcurveLine = function(){
		return lineTypes.curve;
	};
	topoEditor.getSinglePolyLine = function(){
		return lineTypes.singlepolyline;
	};
	topoEditor.getDoublePolyLine = function(){
		return lineTypes.doublepolyline;
	};
	
	topoEditor.delSelected = function(callback){
		
		var rmNodes = d3.selectAll('.node.selected svg,.shape.selected svg');
		rmNodes[0].forEach(function(e){
			d3.select(e).each(function(d){
				if(d.nodeType!='view'){
				d3.select(this.parentNode).remove();
				// 删除蒙板mask定义
				if(d.nodeType=='shape'){
					_miscTransactions.push(d);
				}
				
				// 历史记录中的当前节点连线
				var toRmLink = _addSteps.filter(function(l) {
					return (l.node==null&&l.links.length==1&&(l.links[0].source.id==d.id||l.links[0].target.id==d.id));
				});
				// 历史记录中关于当前节点的所有操作
				var toRmNode = _addSteps.filter(function(l){
					return (l.node!=null&&l.node.id==d.id);
				});
				
				// 从历史记录中清除当前节点操作记录
					toRmNode.map(function (n){
						_addSteps.splice(_addSteps.indexOf(n),1);
					});
					
				
				// 从历史记录中清除当前节点连线的操作记录
				toRmLink.map(function(l){
					// var action = {node:null,links:[l]};
					_addSteps.splice(_addSteps.indexOf(l), 1);
					
				});
				// 当前节点的连线
				var toSplice = _links.filter(function(l){
					return ((l.source.id == d.id &&l.source.nodeType==d.nodeType)|| (l.target.id == d.id&&l.target.nodeType==d.nodeType));
				});
				_lastStep = {node:d,links:toSplice};			
				_removeSteps.push(_lastStep);
				spliceLinksForNode(d);
				triggerChangedEvent(true);
				}else{
					
					callback(d);
				}
			});
		});
		
		// 删除选中的连线
		if(selected_link!=null){
			// d3.selectAll('.lineEditable').remove();
			d3.select('#end-arrow'+selected_link.source.nodeType+selected_link.source.id+selected_link.target.nodeType+selected_link.target.id).remove();
			d3.select('#label'+selected_link.source.nodeType+selected_link.source.id+selected_link.target.nodeType+selected_link.target.id).remove();
			  _links.splice(_links.indexOf(selected_link), 1);
			  _removeSteps.push({node:null,links:[selected_link]});
			  _addSteps.splice(_addSteps.indexOf({node:null,links:[selected_link]}), 1);
			  selected_link=null;
			  triggerChangedEvent(true);
		}
		
		  
		triggerSelectedEvent();
		triggerActionEvent();
		drawlines();
		
	};
	
	// 控制触发视图是否发生变化事件，提示用户在退出时保存视图
	function triggerChangedEvent(changed){
			if(changed!=_lastChangedStatus){
				dispatch.onChanged(changed);
				_lastChangedStatus = changed;
			}
			
	}
	// 控制触发是否有元素被选中事件函数
	function triggerSelectedEvent(){
		
		 var selected_nodes = d3.selectAll('.node.selected svg,.misc.selected svg').data();
			
		 	 if(selected_nodes.length==1){
		 		 var nd = selected_nodes[0];
		 	// var scaledown = true;
		 		 var scaledown = false;
		 		 if(nd.size>0.5)scaledown = true;
		 		 if(selected_nodes[0].nodeType=='group'){
		 			dispatch.onSelected({ ungroup: true,group:false,trash:true,color:true,scaleup:true,scaledown:scaledown});
		 		 }
		 		 else if(selected_nodes[0].groupId){
		 			var grpInfo = d3.select('#group'+selected_nodes[0].groupId + ' svg').datum();
		 			if(grpInfo.ids.length==1){
		 				dispatch.onSelected({ ungroup: false,group:false,trash:true,color:true,scaleup:true,scaledown:scaledown});
		 			}
		 			else{
		 				dispatch.onSelected({ ungroup: true,group:false,trash:true,color:true,scaleup:true,scaledown:scaledown});
		 			}
		 		 }
		 		 else{
		 			dispatch.onSelected({ ungroup: false,group:true,trash:true,color:true,scaleup:true,scaledown:scaledown});
		 		 }
		 	 }
		 	 else if(selected_nodes.length>1){
		 		var scaledown = false;
		 		// 判断选择的多个元素中是否有可以缩小的
		 		selected_nodes.forEach(function(e){
		 			if(e.size>0.5){
		 				scaledown=true;
		 				return;
		 			}
		 			
		 		});
		 		dispatch.onSelected({ ungroup: false,group:true,trash:true,color:true,scaleup:true,scaledown:scaledown,merge:true,unmerge:false});
		 	 }
		 	 else{
		 		 if(selected_link!=null){
		 			dispatch.onSelected({ ungroup: false,group:false,trash:true,color:true,scaleup:false,scaledown:false,merge:false,unmerge:false}); 
		 		 }
		 		 else if(_lastSelectedStatus!=0){
		 			dispatch.onSelected({ ungroup: false,group:false,trash:false,color:false,scaleup:false,scaledown:false,merge:false,unmerge:false});
		 		 }
		 		
		 	 }
				
				
				
	}
	
	function triggerActionEvent(){
		if(_addSteps.length>0&&_removeSteps.length>0&&_lastActionStatus != 0){
			dispatch.onActions({detail:{doCancle:true,doRecover:true}});
			_lastActionStatus = 0;
		}
		else if(_addSteps.length>0&&_removeSteps.length==0&&_lastActionStatus != 1){
			dispatch.onActions({detail:{doCancle:true,doRecover:false}});
			_lastActionStatus = 1;
		}
		else if(_addSteps.length==0&&_removeSteps.length>0&&_lastActionStatus != 2){
			dispatch.onActions({detail:{doCancle:false,doRecover:true}});
			_lastActionStatus = 2;
		}
		else if(_addSteps.length==0&&_removeSteps.length==0){
			dispatch.onActions({detail:{doCancle:false,doRecover:false}});
			_lastActionStatus = -1;
		}
		 if(addLinkData == true){
			dispatch.onActions({detail:{mouseup:mouseup_node,mousedown:mousedown_node}});
			addLinkData = false;
		}
	}
	
	
	
/*	topoEditor.changeNode = function(botnode, svgnode, callback) {
		var nodemesg;		
		var changenode = d3.select('.node.selected svg');
		var nodeicon = (botnode == null)? svgnode : botnode;
		changenode[0].forEach(function(e){
			d3.select(e).each(function(d){			
				nodemesg = d;
				$('g#node'+d.id).empty();
				d3.xml(nodeicon, 'image/svg+xml', function(xml) {
					var nodesContainer = d3.select("g#node"+d.id);
					nodesContainer.append(function() {
						return xml.documentElement;
					})
					.datum(function(){
						var dt ={};
						nodemesg.width = $('#node'+nodemesg.id+'>svg').attr('width');
						nodemesg.height = $('#node'+nodemesg.id+'>svg').attr('height')
						nodemesg.x = botnode?nodemesg.x-15:nodemesg.x - nodemesg.width/2;
						nodemesg.y = botnode?nodemesg.y-15:nodemesg.y - nodemesg.height/2;
						for(var v in nodemesg){
							dt[v]=nodemesg[v];
						}
						return dt;}).attr({
						'id': nodemesg.id,
						'x': botnode?nodemesg.x-15:nodemesg.x - nodemesg.width/2,
						'y': botnode?nodemesg.y-15:nodemesg.y - nodemesg.height/2,
						'nodeType': nodemesg.nodeType
					}).on('mousedown',function(d) {// 画线 操作
						if (d3.event.ctrlKey)
							return;
						if (!drawLineSelected || dragMode)
							return;
						// d3.event.stopPropagation();
						// select node
						
						tooltip.style("display", "none");
						tooltip.html('');
						mousedown_node = d;
						if (mousedown_node === selected_node)
							selected_node = null;
						else
							selected_node = mousedown_node;

						// 重绘画线
						drag_line.style('marker-end',function(){
							if(navigator.appVersion.indexOf('Trident')>0)return null;
							else return 'url(#end-arrow)';
						}

						).classed('hidden',false)// .style('marker-end','url(#end-arrow)')
								// 从图标中心开始绘制连线
								.attr('d','M'+ mousedown_node.x+ ','+ mousedown_node.y
												+ 'L'+ mousedown_node.x+ ','+ mousedown_node.y);

					})
			.on('mouseup',function(d) {
	                if (wait) {
	                    window.clearTimeout(wait);
	                    wait = null;
	                    // 双击事件动作
	                    if(_viewOnly){
	                    	topoViewer.doubleClickNode(d,_defaultNodeDoubleClickAction);
	                    }
	                    else{
	                    	topoEditor.doubleClickNode(d,_defaultNodeDoubleClickAction);
	                    }
	                    
	                } else {
	                    wait = window.setTimeout((function(d) {
	                    	// 单击事件
	                        return  topoEditor.clickNode(_defaultNodeClickAction)
	                    })(d3.event), 250,d);
	                }
						if (!mousedown_node)return;
						
						// needed by FF
						drag_line.classed('hidden',true).style('marker-end', '');

						// check for drag-to-self
						mouseup_node = d;
						if (mouseup_node === mousedown_node) {
							resetMouseVars();
							return;
						}
						// add link to graph (update if exists)
						var source, target;
						source = mousedown_node;
						target = mouseup_node;

						var link;
						link = _links
								.filter(function(l) {
									return (l.source.id === source.id &&l.source.nodeType==source.nodeType&& l.target.id === target.id&&l.target.nodeType==target.nodeType);
								})[0];
						var dual;
						dual= _links.filter(function(l){
								   return (l.source.id === target.id&&l.source.nodeType==target.nodeType&& l.target.id === source.id &&l.target.nodeType==source.nodeType);
							  });

						if (!link) {
						
							if(dual&&dual.length>0&&lineType==dual[0].type&&(lineType==lineTypes.straight||lineType==lineTypes.curve)){
								link = {
										source : source,
										target : target,
										type : lineType,
										color:_lineColor,
										direct:1  // 正向连线
									};
								dual[0].direct=-1;// 反向连线
							}
							else{
								link = {
										source : source,
										target : target,
										type : lineType,
										color:_lineColor,
										metricIdList : null
									};
							}
							sourceLinkId = source.id;
							_links.push(link);
							_lastStep = {node:null,links:[link]};
							_addSteps.push(_lastStep);
							addLinkData = true;
							triggerActionEvent();
							triggerChangedEvent(true);
						}
						
						// select new link
						selected_node = null;
						drawlines();
						mouseover_node = null;
						if(mousedown_node&&mousedown_node!=d){
							d3.select(this.parentNode).selectAll('.linkable').remove();
							
						}
					})
			.on('mouseenter',function(d){
				if(!mousedown_node&&d.nodeType!='group'&&d.nodeType!='shape'){
					showToolTip(d);
				}
			})
			.on('mouseover', function(d) {
				mouseover_node = d;
				if(mousedown_node&&mousedown_node!=mouseover_node){
					showLineAnchor(this, d);
				}
				
				
			})
			.on('mouseleave', function(d) {
				mouseover_node = null;
				if(mousedown_node&&mousedown_node!=d){
					d3.select(this.parentNode).selectAll('.linkable').remove();
					
				}
				if(!mousedown_node){
					tooltip.style("display", "none");
					tooltip.html('');
				}
			})
			.on('contextmenu',  topoEditor.contextMenu(_defaultNodeContextMenu))// m=getContextMenu(d.type)?
																				// topoEditor.contextMenu(m):
			.each(function(d) {
				        var activeNode = d3.select(this.parentNode);
				
				        if(!_viewOnly&&d.nodeType!='group'){
				        	   // 防止图标重叠
					     	 var droppedNode = d3.select(this);
					        
					        nodeData=d3.selectAll('g .node svg').data();
				         if(svgnode != null){
					         // 添加标签
						        activeNode.append('text')
						        .datum({'x' : d.x,'y' : d.y+d.height/2+12})// d.type=='group'?d.y-d.height/2+10:d.type=='shape'?d.y:d.y+d.height/2+7})
						        .attr('class','label')
								.attr('text-anchor','middle')
								.attr('x', d.x)
								.attr('y', d.y+d.height/2+12)// d.type=='group'?d.y-d.height/2+10:d.type=='shape'?d.y:d.y+d.height/2+7)
								.attr('dominant-baseline','central')
								.style('font-size','1em')
								.style('fill',_defaultColor)
								.text(d.originalName?d.originalName:d.name)
								.attr('status',d.status)
								.on('mouseover',function(){
									mouseover_nodelabel=true;
									})
								.on('mouseleave',function(){
										mouseover_nodelabel=false;
										})
								.on('mouseup',function(l){
									 if (wait) {
						                    window.clearTimeout(wait);
						                    wait = null;
						                    // 双击事件动作
						                    editLabel(d,l,d3.select(this));
						                } else {
						                    wait = window.setTimeout((function() {
						                    	// 单击事件
						                    })(d3.event), 250);
						                }
									
								});
				         }

						// 恢复模式
					    if(_restoreMode&&edges){
					    	var linkStart = edges.filter(function(l) {
								return (l.source.id === d.id&&l.source.nodeType===d.nodeType);
							});
					    	var linkEnd = edges.filter(function(l) {
								return (l.target.id === d.id&&l.target.nodeType===d.nodeType);
							});
					    	
					    	if(linkStart){
					    		linkStart.map(function(l){
					    			d3.select('#node'+l.target.id).select('svg').each(function(e){
					    				var link = {
						    					source:d,
						    					target:e,
						    					type:l.type,
						    					color:_lineColor
						    			};
						    			_links.push(link);
					    			});
					    			
					    		});
					    	}
					    	if(linkEnd){
					    		linkEnd.map(function(l){
					    			d3.select('#'+l.source.nodeType+l.source.id).select('svg').each(function(e){
					    				var link = {
												source : e,
												target : d,
												type:l.type,
												color:l.color
											};
							    		_links.push(link);
					    			});
					    			
					    		});
					    		
					    	}
					
					    	
					    	drawlines();
					    }
					    if(_restoreMode&&d.nodeType=='group'){
					    	 if(d.ids){
					    		 d.ids.map(function(nodeId){
					    			d3.select('#node'+nodeId+' svg').datum().groupId=d.id;
					    		 })
					    	 }
					    
					    	
					    }
						initiative = true
						// 画出Node所连接的线（如果有的话）
						if (initiative) {
							var linkStart = edges.filter(function(l) {
										return (l.source.id === d.id&&l.source.nodeType===d.nodeType);
									});
							var linkEnd = edges.filter(function(l) {
										return (l.target.id === d.id&&l.target.nodeType===d.nodeType);
									});
							if (linkStart) {
								linkStart.map(function(l) {
										  l.source=d;
										  l.status = nodemesg.status;
										  var dual;
											dual= _links.filter(function(m){
													   return (m.source.id === l.target.id&&m.source.nodeType==l.target.nodeType&& m.target.id === l.source.id &&m.target.nodeType==l.source.nodeType);
												  });
											if(dual&&dual.length>0&&lineType==dual[0].type&&(lineType==lineTypes.straight||lineType==lineTypes.curve)){
												l.direct = 1;// 正向曲线
												dual[0].direct=-1;// 反向连线
											}
											_links.push(l);

										});

							}
							if (linkEnd) {
								linkEnd.map(function(l) {
											l.target = d;
											_links.map(function(m) {
														if (m.target.id === l.target.id&&m.target.nodeType===l.target.nodeType) {
															m.target=d;
															// 连线的终点端在连线生成时还未计算出宽度与高度，因此需要重新设置终点蒙板的宽和高
															// d3.select('#mask'+m.source.nodeType+m.source.id+m.target.nodeType+d.id
															// +'
															// #mask'+d.type+d.id).attr('width',d.width).attr('height',d.height);
														}
													});

										});

							}
							drawlines();

						}
						
							if(callback){
								callback(this,d);
							}
							
					
				        }}).call(drag);
				});
			})
		});
		drawlines();
	}*/
	
	
	
	 function addNode(node,callback) {
		 if(node.icon&&node.icon.indexOf('<svg')==0){
			 var doc = parser.parseFromString(_nodeIcons[node.nodeType], "image/svg+xml");
			 add(doc.documentElement);
		 }
		 else if (node.nodeType&&_nodeIcons[node.nodeType]&&_nodeIcons[node.nodeType].indexOf('<svg')==0){
			 var doc = parser.parseFromString(_nodeIcons[node.nodeType], "image/svg+xml");
			 add(doc.documentElement);
		 }
		 else if(node.nodeType&&_nodeIcons[node.nodeType]){
			 d3.xml(_nodeIcons[node.nodeType],"image/svg+xml",
						function(error,xml) {
			add(xml.documentElement);
			 });

		 }
		 else if(node.icon)
		 {
			 d3.xml(node.icon,"image/svg+xml",
						function(error,xml) {
			add(xml.documentElement);
		});
		 }
		 
		 function add(iconXml){

				var nodeContainer ;
				if(node.nodeType=='group'){
					nodeContainer  = canvas.insert('g','path.dragline').attr("class", "misc group view"+_topoId);
				}
				else if(node.nodeType=='shape'){
					nodeContainer  = canvas.insert('g','path.dragline').attr("class", "misc shape view"+_topoId);
				}
				/*
				 * else if(node.nodeType=='merger'){ nodeContainer =
				 * canvas.insert('g','path.dragline').attr("class", "misc merger
				 * view"+_topoId); }
				 */
				else{
					if(node.status){
						nodeContainer  = canvas.append('g').attr("class", "node view"+_topoId).attr('status',node.status);
					}
					else{
						nodeContainer  = canvas.append('g').attr("class", "node view"+_topoId);
					}
					
				}
					
				
				
				var newItem = nodeContainer.append(function() {
					
					return iconXml;
					
					})
						.datum(function(){
							var dt ={};
							for(var v in node){
								dt[v]=node[v];
							}
							return dt;})
					.attr('x', node.x)
						// x 坐标
						.attr('y', node.y)
						// y 坐标
						.style('fill',function(d){
							return d.color||this.style['fill'];
						})
						.attr('width',function(d){
							d.width = d.size*d3.select(this).attr('width');
							d.x +=d.width/2;
							node.width=d.width;
							return d.width;
						})
						.attr('height',function(d){
							d.height = d.size*d3.select(this).attr('height');
							d.y += d.height/2;
							node.height = d.height;
							return d.height;
						})
						.on('mousedown',function(d) {// 画线 操作
									if (d3.event.ctrlKey)
										return;
									if (!drawLineSelected || dragMode)
										return;
									// d3.event.stopPropagation();
									// select node
									
									tooltip.style("display", "none");
									tooltip.html('');
									mousedown_node = d;
									if (mousedown_node === selected_node)
										selected_node = null;
									else
										selected_node = mousedown_node;

									// 重绘画线
									drag_line.style('marker-end',function(){
										if(navigator.appVersion.indexOf('Trident')>0)return null;
										else return 'url(#end-arrow)';
									}

									).classed('hidden',false)// .style('marker-end','url(#end-arrow)')
											// 从图标中心开始绘制连线
											.attr('d','M'+ mousedown_node.x+ ','+ mousedown_node.y
															+ 'L'+ mousedown_node.x+ ','+ mousedown_node.y);

								})
						.on('mouseup',function(d) {
				                if (wait) {
				                    window.clearTimeout(wait);
				                    wait = null;
				                    // 双击事件动作
				                    if(d.nodeType=='node'){
					                    if(_viewOnly){
					                    	topoViewer.doubleClickNode(d,_defaultNodeDoubleClickAction);
					                    }
					                    else{
					                    	topoEditor.doubleClickNode(d,_defaultNodeDoubleClickAction);
					                    }
				                    }

				                    
				                } else {
				                    wait = window.setTimeout((function(d) {
				                    	// 单击事件
				                        return  topoEditor.clickNode(_defaultNodeClickAction)
				                    })(d3.event), 250,d);
				                }
									if (!mousedown_node)return;
									
									// needed by FF
									drag_line.classed('hidden',true).style('marker-end', '');

									// check for drag-to-self
									mouseup_node = d;
									if (mouseup_node === mousedown_node) {
										resetMouseVars();
										return;
									}
									// add link to graph (update if exists)
									var source, target;
									source = mousedown_node;
									target = mouseup_node;

									var link;
									link = _links
											.filter(function(l) {
												return (l.source.id === source.id &&l.source.nodeType==source.nodeType&& l.target.id === target.id&&l.target.nodeType==target.nodeType);
											})[0];
									var dual;
									dual= _links.filter(function(l){
											   return (l.source.id === target.id&&l.source.nodeType==target.nodeType&& l.target.id === source.id &&l.target.nodeType==source.nodeType);
										  });

									if (!link) {
									
										if(dual&&dual.length>0&&lineType==dual[0].type&&(lineType==lineTypes.straight||lineType==lineTypes.curve)){
											link = {
													source : source,
													target : target,
													type : lineType,
													color:_lineColor,
													direct:1  // 正向连线
												};
											dual[0].direct=-1;// 反向连线
										}
										else{
											link = {
													source : source,
													target : target,
													type : lineType,
													color:_lineColor,
													metricIdList : null
												};
										}
										sourceLinkId = source.id;
										_links.push(link);
										_lastStep = {node:null,links:[link]};
										_addSteps.push(_lastStep);
										addLinkData = true;
										triggerActionEvent();
										triggerChangedEvent(true);
									}
									
									// select new link
									selected_node = null;
									drawlines();
									mouseover_node = null;
									if(mousedown_node&&mousedown_node!=d){
										d3.select(this.parentNode).selectAll('.linkable').remove();
										
									}
								})
						.on('mouseenter',function(d){
							if(!mousedown_node&&d.nodeType!='group'&&d.nodeType!='shape'){
								showToolTip(d);
							}
						})
						.on('mouseover', function(d) {
							mouseover_node = d;
							if(mousedown_node&&mousedown_node!=mouseover_node){
								showLineAnchor(this, d);
							}
							
							
						})
						.on('mouseleave', function(d) {
							mouseover_node = null;
							if(mousedown_node&&mousedown_node!=d){
								d3.select(this.parentNode).selectAll('.linkable').remove();
								
							}
							if(!mousedown_node){
								tooltip.style("display", "none");
								tooltip.html('');
							}
						})
						.on('contextmenu',  topoEditor.contextMenu(_defaultNodeContextMenu))// m=getContextMenu(d.type)?
																							// topoEditor.contextMenu(m):
						.each(function(d) {
							        var activeNode = d3.select(this.parentNode);
							
							        if(!_viewOnly&&d.nodeType!='group'){
							        	   // 防止图标重叠
								     	 var droppedNode = d3.select(this);
								        
								        nodeData=d3.selectAll('g .node svg').data();
									
							         
							         // 添加标签
								        activeNode.append('text')
								        .datum({'x' : d.x,'y' : d.y+d.height/2+12})// d.type=='group'?d.y-d.height/2+10:d.type=='shape'?d.y:d.y+d.height/2+7})
								        .attr('class','label')
										.attr('text-anchor','middle')
										.attr('x', d.x)
										.attr('y', d.y+d.height/2+12)// d.type=='group'?d.y-d.height/2+10:d.type=='shape'?d.y:d.y+d.height/2+7)
										.attr('dominant-baseline','central')
										.style('font-size','12px')
										.style('fill',_defaultColor)
										.text(d.name)
										.attr('status',d.status)
										.on('mouseover',function(){
											mouseover_nodelabel=true;
											})
										.on('mouseleave',function(){
												mouseover_nodelabel=false;
												})
										.on('mouseup',function(l){
											 if (wait) {
								                    window.clearTimeout(wait);
								                    wait = null;
								                    // 双击事件动作
								                    editLabel(d,l,d3.select(this));
								                } else {
								                    wait = window.setTimeout((function() {
								                    	// 单击事件
								                    })(d3.event), 250);
								                }
											
										});
									// 恢复模式
								    if(_restoreMode&&edges){
								    	var linkStart = edges.filter(function(l) {
											return (l.source.id === d.id&&l.source.nodeType===d.nodeType);
										});
								    	var linkEnd = edges.filter(function(l) {
											return (l.target.id === d.id&&l.target.nodeType===d.nodeType);
										});
								    	
								    	if(linkStart){
								    		linkStart.map(function(l){
								    			d3.select('#node'+l.target.id).select('svg').each(function(e){
								    				var link = {
									    					source:d,
									    					target:e,
									    					type:l.type,
									    					color:_lineColor
									    			};
									    			_links.push(link);
								    			});
								    			
								    		});
								    	}
								    	if(linkEnd){
								    		linkEnd.map(function(l){
								    			d3.select('#'+l.source.nodeType+l.source.id).select('svg').each(function(e){
								    				var link = {
															source : e,
															target : d,
															type:l.type,
															color:l.color
														};
										    		_links.push(link);
								    			});
								    			
								    		});
								    		
								    	}
								
								    	
								    	drawlines();
								    }
								    if(_restoreMode&&d.nodeType=='group'){
								    	 if(d.ids){
								    		 d.ids.map(function(nodeId){
								    			d3.select('#node'+nodeId+' svg').datum().groupId=d.id;
								    		 })
								    	 }
								    
								    	
								    }
									// 画出Node所连接的线（如果有的话）
									if (initiative) {
										var linkStart = edges.filter(function(l) {
													return (l.source.id === d.id&&l.source.nodeType===d.nodeType);
												});
										var linkEnd = edges.filter(function(l) {
													return (l.target.id === d.id&&l.target.nodeType===d.nodeType);
												});
										if (linkStart) {
											linkStart.map(function(l) {
													  l.source=d;
													  l.status = node.status;
													  var dual;
														dual= _links.filter(function(m){
																   return (m.source.id === l.target.id&&m.source.nodeType==l.target.nodeType&& m.target.id === l.source.id &&m.target.nodeType==l.source.nodeType);
															  });
														if(dual&&dual.length>0&&lineType==dual[0].type&&(lineType==lineTypes.straight||lineType==lineTypes.curve)){
															l.direct = 1;// 正向曲线
															dual[0].direct=-1;// 反向连线
														}
														_links.push(l);

													});

										}
										if (linkEnd) {
											linkEnd.map(function(l) {
														l.target = d;
														_links.map(function(m) {
																	if (m.target.id === l.target.id&&m.target.nodeType===l.target.nodeType) {
																		m.target=d;
																		// 连线的终点端在连线生成时还未计算出宽度与高度，因此需要重新设置终点蒙板的宽和高
																		// d3.select('#mask'+m.source.nodeType+m.source.id+m.target.nodeType+d.id
																		// +'
																		// #mask'+d.type+d.id).attr('width',d.width).attr('height',d.height);
																	}
																});

													});

										}
										drawlines();

									}
									
										if(callback){
											callback(this,d);
										}
										
								
							        }}).call(drag);

				nodeContainer.attr('id',function(){
						if(node.nodeType=='view'){
							return 'node'+node.id;
						}
						return node.nodeType+node.id;
					});
				if(!_restoreMode){
					if(!initiative){
						_lastStep = {node:node,links:null};
						_addSteps.push(_lastStep);
						triggerActionEvent();
					}
					
				}
				

				force.start();
				_restoreMode = false;
				if(drawLineSelected){
					d3.selectAll('.nodes>.node').classed('connectable',true);
				}

			
		 }
			
	}
	// end function
	 
	 function dist(a, b) {
         return Math.sqrt(Math.pow(a[0] - b[0], 2), Math.pow(a[1] - b[1], 2));
     }
	topoEditor.cancle = function (){
		
		if(_addSteps.length==0)return 0;
		_lastStep = _addSteps.pop();
		
		
			 if(_lastStep.node){
				 _lastStep.node.x+=_lastStep.node.width/2;
				 _lastStep.node.y+=_lastStep.node.height/2;
				 d3.select('#'+_lastStep.node.nodeType+_lastStep.node.id).remove();
				 spliceLinksForNode(_lastStep.node);
				 // 取消节点的groupId
				 if(_lastStep.node.nodeType=='group'){
					 _lastStep.node.ids.map(function(nodeId){
							d3.select('#node'+nodeId+' svg').datum().groupId=null;
						})
				 }
				 drawlines();
				 _removeSteps.push(_lastStep);
				 triggerActionEvent();
				 
				 return  _addSteps.length;
			 }
			 if(_lastStep.links){
				 _lastStep.links.forEach(function(e){
					 d3.select('#end-arrow'+e.source.nodeType+e.source.id+e.target.nodeType+e.target.id).remove();
						d3.select('#label'+e.source.nodeType+e.source.id+e.target.nodeType+e.target.id).remove();
						// 可编辑连线的删除需特殊处理
						// 在此情况下只可能删除一根连线
						var toSplice = _links.filter(function(l){
							return (l.source.id==e.source.id&&l.target.id==e.target.id&&l.type==e.type);
						});
						if(_links.indexOf(toSplice[0])!=-1)
						  _links.splice(_links.indexOf(toSplice[0]), 1);
						  
				 });
				 drawlines();
				 
					  
			 }
			 _removeSteps.push(_lastStep);
			 triggerActionEvent();
		 return _addSteps.length;
	 };
	 topoEditor.recover = function(){	 
		 if(_removeSteps.length==0) return 0;
		 _restoreMode = true;
		 initiative = false;
		 
		 _lastStep = _removeSteps.pop();
			 if(_lastStep.node){
				 if(_lastStep.links)
					 edges = _lastStep.links;
				 else 
					 edges =null;
				 if(_lastStep.node.nodeType=='group'){
					 for(var i = 0; i < _lastStep.node.ids.length; i++){
						d3.select('#node'+_lastStep.node.ids[i]).select('svg').each(function(e){
							e.groupId = _lastStep.node.id;
						}) 
					 }
				 }
				 _lastStep.node.x-=_lastStep.node.width/2;
				 _lastStep.node.y-=_lastStep.node.height/2;
				 addNode(_lastStep.node);
			 }
			 else if(_lastStep.links){
				 	_lastStep.links.map(function(l){
				 		var src = null;
				 		var tgt = null;
				 		if(l.source.nodeType != 'group'){
					 		d3.select('#node'+l.source.id).select('svg').each(function(e){
					 			 src = e;
					 		});
				 		}else{
				 			d3.select('#'+l.source.nodeType+l.source.id).select('svg').each(function(e){
					 			 src = e;
					 		})
				 		}			 		
				 		if(l.target.nodeType != 'group'){
					 		d3.select('#node'+l.target.id).select('svg').each(function(e){
					 			 tgt= e;				 			 
					 		});
				 		}else{
				 			d3.select('#'+l.source.nodeType+l.source.id).select('svg').each(function(e){
					 			 src = e;
					 		})
				 		}

				 		var link ={
				 				source:src,
				 				target:tgt,
				 				type:l.type,
				 				color:l.color,
				 				metricIdList:l.metricIdList,
				 				status:l.status
				 		};
				 		// 判断当前新建的连线是否已经在画布中存在
				 		var duplicated = _links.filter(function(l){
				 			return (l.source.id==link.source.id&&l.target.id==link.target.id&&l.type==link.type);
				 		});
				 		if(duplicated.length==0)
				 		_links.push(link);
				 		else 
				 			return _removeSteps.length;
				 	});
				 	
				  drawlines();
			  }
			  _addSteps.push(_lastStep);
			 triggerActionEvent();
			 return _removeSteps.length;
		 
	 };
	 
	 // start zoom functions
	   // 复位
		  function center(){ translation=[0,0]; scaleFactor = 1; zoom.scale(1);
		  zoom.translate([ 0, 0 ]); canvas.attr("transform", null);
		  d3.selectAll('g.node .label,g.misc .label').style('opacity',1);
		  d3.selectAll('g.nodes').style('opacity',1); }
		 
	 
	 topoViewer.center = function(){
		 translation=[0,0];
		   scaleFactor = 1;
		   zoom.scale(1);
			zoom.translate([ 0, 0 ]);
		   canvas.attr("transform", null);
		   d3.selectAll('g.node .label,g.misc .label').style('opacity',1);
			d3.selectAll('g.nodes').style('opacity',1);
	 }
	 topoEditor.center = function(){
		 translation=[0,0];
		   scaleFactor = 1;
		   zoom.scale(1);
			zoom.translate([ 0, 0 ]);
		   canvas.attr("transform", null);
		   d3.selectAll('g.node .label,g.misc .label').style('opacity',1);
			d3.selectAll('g.nodes').style('opacity',1); 
	 }
	 
	   function pan(dx,dy){
		   translation[0]+=dx;
		   translation[1]+=dy;
		   
		   zoom.translate(translation);
		   canvas.attr("transform", "translate(" + translation + ")scale("
					+ scaleFactor + ")");
		   
	   }
	   
	    topoViewer.customZoom = function(sc,tr){
	    	canvas.attr("transform", "translate(" + tr + ")scale("+sc+")");
	    }
		function zoomed() {
			d3.event.sourceEvent.stopPropagation();
			scaleFactor = d3.event.scale;
			translation=d3.event.translate;
			canvas.attr("transform", "translate(" + translation + ")scale("+scaleFactor+")");

		}

		function zoomedEnd(zoominAction,zoomoutAction) {
			return function(data){
				tooltip.style("display", "none");
				tooltip.html('');
				if(mouseover_node!=null){
					 if (scaleFactor<1.5&&scaleFactor>1){
						//	d3.select('g.nodes').style('opacity',1);
					     }
						else if ((1.5<scaleFactor&&scaleFactor<1.7)){
						//	d3.select('g.nodes').style('opacity',0.5);
						}
						else if (1.7<scaleFactor&&scaleFactor<1.9){
						//	d3.select('g.nodes').style('opacity',0.3);
						}
						else if (1.9<scaleFactor&&scaleFactor<2.3){
						//	d3.select('g.nodes').style('opacity',0.1);
						}
						else if(scaleFactor>=2.3&&mouseover_node.nodeType=='view'){
							scaleFactor = 1;
							translation = [ 0, 0 ];
							
							// 将zoom重新初始化，否则在新的缩放图形过程中会继续沿用前一次的scale和translation值
							zoom.scale(1);
							zoom.translate([ 0, 0 ]);
							// reloadTopo();
							canvas.attr("transform", null);
							if(zoominAction){
								_parentTopoId = _topoId;
								zoominAction(mouseover_node,data);
								
							}
								
							mouseover_node = null;

						}
							

				}else{
					d3.select('g.nodes').style('opacity',1);
				}
			    			     
			     /*if(scaleFactor<0.7&&scaleFactor>0.6){*/
				if(scaleFactor<0.45){
			    	 d3.selectAll('g.node .label,g.misc .label').style('opacity',0);
			    	// d3.select('g.nodes').style('opacity',0.6);
			     }
			     /*else if(scaleFactor>=0.7){*/
				else if(scaleFactor>=0.45){
			    	 d3.selectAll('g.node .label,g.misc .label').style('opacity',1);
			     }
/*
 * else if(scaleFactor<0.5){ scaleFactor = 1; translation = [ 0, 0 ];
 * mouseover_node = null; // 将zoom重新初始化，否则在新的缩放图形过程中会继续沿用前一次的scale和translation值
 * zoom.scale(1); zoom.translate([ 0, 0 ]); canvas.attr("transform", null);
 * if(zoomoutAction&&_parentTopoId!=_topoId) zoomoutAction(_parentTopoId,data);
 * d3.selectAll('g.node .label,g.misc .label').style('opacity',1);
 * d3.selectAll('g.nodes').style('opacity',1); }
 */
			     
				
			}
		}
		// end zoom functions
		
		function updateLineLabel(sourceId,targetId,text,sourceType,targetType){
			addLineLabel(sourceId,targetId,text,sourceType,targetType);
			tick();
		}
		

		function addLineLabel(sourceId,targetId,text,sourceType,targetType){
			var textArr = text.split(",")
			for (var i = 1; i < textArr.length; i++){
				canvas.append("g").attr("class", "linkLabels view"+_topoId)
				 .attr('id','label'+sourceType+sourceId+targetType+targetId)
				 .append("text")
			     .attr("class", "linklabel")
				 .attr("dy", -20*i)
			     .attr('dominant-baseline','central')
				 .append("textPath").attr('class','textpath')
				 .style("letter-spacing", "1px")
				 .style("font-size", "12px")
				 .style("font-weight", "normal")
			    .attr("xlink:href",function(){
			    	return '#link'+sourceType+sourceId+targetType+targetId;
			    	})
			    .style("fill", "white")
			     .text(textArr[i-1])
			}			
		}
		
		function drawlines() {
			path = path.data(_links,function(d){
				return 'link'+d.source.nodeType+d.source.id+d.target.nodeType+d.target.id;
				});
			
			path.enter().append('svg:path').attr('class', 'link linkage view'+_topoId)
			.attr('id',function(d){
				
				d3.select('defs').append('svg:marker')
			    .attr('id', 'bg-end-arrow'+d.source.nodeType+d.source.id+d.target.nodeType+d.target.id)
			    .attr('class','endarrow view'+_topoId)
		  .attr('viewBox', '0 0 12 12')
	    .attr('refX', 8)
	    .attr('refY', 6)
	    .attr('markerWidth', 10)
	    .attr('markerHeight', 10)
	    .attr('orient', 'auto')
	  .append('svg:path')
	    .attr('d', 'M3,3 L10,6 L3,10 L6,6 L3,3')
		    .attr('fill', d.color||_lineColor);
				
				d3.select('g.linksbg').append('svg:path').attr('class','linkbg view'+_topoId)
				.attr('id','linkbg'+d.source.nodeType+d.source.id+d.target.nodeType+d.target.id).attr('d','').style('',function(){
			return d.color;
			})
			.attr('marker-end',function(){
				if(navigator.appVersion.indexOf('Trident')>0)return null;
				else 
				return 'url(#bg-end-arrow'+d.source.nodeType+d.source.id+d.target.nodeType+d.target.id+')';
				});
				
				d3.select('defs').append('svg:marker')
				    .attr('id', 'end-arrow'+d.source.nodeType+d.source.id+d.target.nodeType+d.target.id)
				    .attr('status',d.status)
				    .attr('class','endarrow view'+_topoId)
			     .attr('viewBox', '0 0 12 12')
	    .attr('refX', 8)
	    .attr('refY', 6)
	    .attr('markerWidth', 10)
	    .attr('markerHeight', 10)
	    .attr('orient', 'auto')
	  .append('svg:path')
	    .attr('d', 'M3,3 L10,6 L3,10 L6,6 L3,3')
			    .attr('fill', d.color||_lineColor);// d.color
				if(d.type== lineTypes.curve){
					
				// 增加逆向圆弧路径，用于变换线上文字的方向
				d3.select('defs').append('svg:path')
			    .attr('id', 'textpath'+d.source.nodeType+d.source.id+d.target.nodeType+d.target.id).attr("class", "deftextpath view"+_topoId)
		    .attr('d', '');
				}
				
				return 'link'+d.source.nodeType+d.source.id+d.target.nodeType+d.target.id;
				
			})
			
			.attr('marker-end',function(d){
				if(navigator.appVersion.indexOf('Trident')>0)return null;
				else 
				return 'url(#end-arrow'+d.source.nodeType+d.source.id+d.target.nodeType+d.target.id+')';
				})
			.style('stroke',function(d){
				return d.color;
				})
			.each(function(d,i){
				if(d.status){
					d3.select(this).attr('status',d.status);
				}
			})
			.on('click', topoEditor.clickLink(_defaultLinkClickAction))
			.on('mouseover',function(d){
				mouseover_link = d;
			})
			.on('mouseleave',function(){
				mouseover_link = null;
			});
			// remove old links
			path.exit().remove();
			
			force.start();

		}
		
	topoEditor.updateStatus = function(warnData,linkData){
		updateStatus(warnData,linkData);
	};
	topoViewer.updateStatus = function(warnData,linkData){
		updateStatus(warnData,linkData);
	};
	function updateStatus(warnData,linkData){
		d3.selectAll('.node').attr('status',null);// warnData存在才清空状态
		d3.selectAll('.link').attr('status',null);// linkData存在才清空状态
		d3.selectAll('.endarrow').attr('status',null);// 箭头清空状态
		d3.selectAll('.label').attr('status',null);// label清空状态
		if(linkData){
			linkData.forEach(function(e){
				updateLineLabel(e.source,e.target, e.text,e.sourceType, e.targetType);
				/* updateLineLabel(e); */
			});
		}
		if(warnData){
			
			warnData.forEach(function(e){
				var nd = d3.select('#node'+e.id);
				nd.attr('status',e.status);
				nd.select('text').attr('status',e.status);
				var linkStart=_links.filter(function(l){
					return l.source.id===e.id;
				});
				if(linkStart){
					linkStart.map(function(m){
						if(m.source.nodeType == 'node'){// 判断只有起点为node线条才会变色
							d3.selectAll('#link'+m.source.nodeType+m.source.id+m.target.nodeType+m.target.id).attr('status',e.status);
							d3.selectAll('#end-arrow'+m.source.nodeType+m.source.id+m.target.nodeType+m.target.id).attr('status',e.status);
							d3.selectAll('#label'+m.source.nodeType+m.source.id+m.target.nodeType+m.target.id).attr('status',e.status);
						}
						});
				}
			});
		}
		
		
		};
		topoEditor.setLinkMotive = function(motive){
			if(motive&&motive==1){
				enableLinkFlow();
			}else{
				disableLinksFlow();
			}
		};
		topoViewer.setLinkMotive = function(motive){
			if(motive&&motive==1){
				enableLinkFlow();
			}else{
				disableLinksFlow();
			}
		}
		function enableLinkFlow(){
			d3.select('g.links').classed('links-flow',true);
		}
		/*
		 * function enableLinkFlow(){ var links = d3.select('g.links'); var
		 * $links = $('g.links'); }
		 */
		
		function disableLinksFlow(){
			d3.select('g.links').classed('links-flow',false);
		}
/*
 * function enableLinkBg(){ d3.select('g.links').classed('links-withbg',true);
 * d3.select('g.linksbg').style('display','block'); }
 */
		function disableLinkBg(){
			d3.select('g.links').classed('links-withbg',false);
			d3.select('g.linksbg').style('display','none');
		}
		function setGrid(grid){
			d3.selectAll('.canvas .axis').remove();
			if(grid==1){
				// X坐标线
				 xLines=background.insert("g",":first-child")
				 .attr("class", "x axis")
				 .selectAll("line")
				 .data(d3.range(15, _width, 15))
				 .enter().append("line")
				 .attr("x1", function(d) { return d; })
				 .attr("y1", 0)
				 .attr("x2", function(d) { return d; })
				 .attr("y2", _height);
				
				 
				 // Y坐标线
				 yLines=background.insert("g",":first-child")
				 .attr("class", "y axis")
				 .selectAll("line")
				 .data(d3.range(15, _height, 15))
				 .enter().append("line")
				 .attr("x1", 0)
				 .attr("y1", function(d) { return d; })
				 .attr("x2", _width)
				 .attr("y2", function(d) { return d; });
			}
		}
		topoEditor.setGrid = function(grid){
			_grid = grid;
			setGrid(grid);
		}
		topoViewer.setGrid = function(grid){
			_grid = grid;
			setGrid(grid);
		};
		function setBackgroundColor(color){
			d3.select('.canvas>svg').style('background-color',color);
		}
		topoEditor.setBackgroundColor = function(color){
			setBackgroundColor(color);
		};
		topoViewer.setBackgroundColor = function(color){
			setBackgroundColor(color);
		}

		
		topoEditor.setMapBackGround = function(mapConf){
			d3.select('svg g#background-map').remove();
			if(mapConf)
			setMapBackGround(mapConf);
		}
		topoViewer.setMapBackGround = function(mapConf){
			d3.select('svg g#background-map').remove();
			if(mapConf)
			setMapBackGround(mapConf);
		}
		function setMapBackGround(mapConf){
			var projection = d3.geo.mercator()
		 	.center([mapConf.longi||96,mapConf.lati||35])
		    .scale(mapConf.scale||1250)
		    .translate([_width/2, _height/2]);

		var ph = d3.geo.path()
		    .projection(projection);
		d3.json(mapConf.url, function(error, us) {
			  if (error) throw error;

			  // d3.select('g.nodes').insert("svg:g",':first-child')
			  brushCanvas.insert('svg:g',':first-child')
			      .attr("id", "background-map")
			      .classed('connectable',function(){
			    	  	return drawLineSelected;
			      })
			      .on('click',function(){
			    	  // 点击地图背景时，需要清除选中状态
			    	  d3.selectAll('.node.selected,.misc.selected').classed('selected',false).classed('dragged',false);
			    	  initSelectNodeToResize();
			      })
			    .selectAll("path")
			      .data(us.features)
			    .enter().append("path")
			      .attr("d", ph);
			      
			      // .on("click", clicked);
			});

		}
	topoViewer.setImgBackGround = function(img){
		setImgBackGround(img);
	}
	topoEditor.setImgBackGround = function(img){
		setImgBackGround(img);
	}
	function setImgBackGround (img){
			background.style('background-position','center')//
			.style('background-repeat','no-repeat')
			.style('background-attachment','fixed')
			.style('background-size','100% 100%')
			.style('background-image',function(){
				if(img){
					 xLines.remove();
					 yLines.remove();
					 _customBg = true;
					return 'url('+img+')';
				}
				else
					return null;
				
			}
			);
		
	
			
		};
	topoViewer.addWarningStyle = function(level,color){
		addWarningStyle(level, color);	
	};
	topoEditor.addWarningStyle = function(level,color){
		addWarningStyle(level, color);
	};

		
	function addWarningStyle(level, color){
		var nodeSelector = 'g[status="'+level+'"]>svg'
		var nodeChildSelector='g[status="'+level+'"] svg path[alert=false], g[status="'+level+'"] svg polygon,g[status="'+level+'"] svg ellipse[alert=false],g[status="'+level+'"] svg rect,marker[status="'+level+'"] path';
		// var nodeSelector='.'+level+' svg path,marker.'+level+' path';
		var lineSelector = 'path[status="'+level+'"]';
		var labelSelector = 'g[status="'+level+'"] text textpath';
		var nodeLabelSelector =' .node text[status="'+level+'"]';
		var sheet = document.styleSheets[1];
		var warningAnimation =" -webkit-animation-name: throb;animation-name: throb;-webkit-animation-duration: 2.5s; animation-duration: 2.5s;-webkit-animation-iteration-count: infinite; animation-iteration-count: infinite;  -webkit-animation-timing-function: ease-out;animation-timing-function: ease-out;";
		if("insertRule" in sheet) {
			sheet.insertRule(nodeSelector + "{"+warningAnimation+"}",++styleIndex);
			sheet.insertRule(nodeChildSelector + "{fill:" + color + " !important;}",++styleIndex);
			sheet.insertRule(labelSelector + "{fill:" + color + " !important;}",++styleIndex);
			sheet.insertRule(lineSelector + "{stroke:" + color + " !important;"+"}",++styleIndex);
			sheet.insertRule(nodeLabelSelector + "{fill:" + color + " !important;}",++styleIndex);
		}
		else if("addRule" in sheet) {
			sheet.addRule(nodeSelector , warningAnimation);
			sheet.addRule(nodeChildSelector, "fill:"+color+" !important;",++styleIndex);
			sheet.addRule(labelSelector, "fill:"+color+" !important;",++styleIndex);
			sheet.addRule(lineSelector, "stroke:"+color+" !important;",++styleIndex);
			sheet.addRule(nodeLabelSelector, "fill:"+color+" !important;",++styleIndex);
		}
	};
	
		
/*	topoEditor.scaleNode = function(margin){
		var status = null;
		var node;
		var selected_Nodes = d3.selectAll('.node.selected svg,.shape.selected svg,.group.selected svg');
		selected_Nodes[0].forEach(function(e){
			d3.select(e).each(function(d){
				 var oW = d.width/d.size; 
				 var oH = d.height/d.size;
				 d.size+=margin;
				 d.width=oW*d.size;
				 d.height = oH*d.size;
				 triggerSelectedEvent();
				 if(d.size <= 0.5) {
					 d3.selectAll('.node.selected .label,.misc.selected .label').style('display', 'none');
				 }else{
					 d3.selectAll('.node.selected .label,.misc.selected .label').style('display', 'block'); 
				 }
				 d3.select(this).attr('width',d.width).attr('height',d.height)
				 .attr('x',d.x-d.width/2).attr('y',d.y-d.height/2);
				// 移动标签
	         		if(d3.select(this.parentNode).select('.label')[0].length!=0){
	         		 	d3.select(this.parentNode).select('.label').each(function(){
	             		var editNode = d3.select(this);
	             		editNode.attr('x',d.x).attr('y',d.y+d.height/2+12);// d.nodeType=='group'?d.y-d.height/2+10:d.nodeType=='shape'?d.y:d.y+d.height/2+7);
	             	});  
	             	}
	         	node = d	
	         	triggerChangedEvent(true);
	         	force.start();
			 });
		});
		initSelectNodeToResize();
		return node;
	};*/
	
	topoEditor.scaleNode = function(margin){
		var status = null;
		var node;
		var selected_Nodes = d3.selectAll('.node.selected svg,.shape.selected svg,.group.selected svg');
		selected_Nodes[0].forEach(function(e){
			d3.select(e).each(function(d){
				if (d.size > 0.5 && margin < 0 || margin > 0) {
					 var oW = d.width/d.size; 
					 var oH = d.height/d.size;
					 d.size+=margin;
					 d.width=oW*d.size;
					 d.height = oH*d.size;
				}
				 triggerSelectedEvent();
				 if(d.size <= 0.5) {
					 $("g#node"+d.id+" text").hide();
					// d3.selectAll('.node.selected .label,.misc.selected .label').style('display', 'none');
				 }else{
					 $("g#node"+d.id+" text").show();
					// d3.selectAll('.node.selected .label,.misc.selected .label').style('display', 'block'); 
				 }
				 d3.select(this).attr('width',d.width).attr('height',d.height)
				 .attr('x',d.x-d.width/2).attr('y',d.y-d.height/2);
				// 移动标签
	         		if(d3.select(this.parentNode).select('.label')[0].length!=0){
	         		 	d3.select(this.parentNode).select('.label').each(function(){
	             		var editNode = d3.select(this);
	             		editNode.attr('x',d.x).attr('y',d.y+d.height/2+12);// d.nodeType=='group'?d.y-d.height/2+10:d.nodeType=='shape'?d.y:d.y+d.height/2+7);
	             	});  
	             	}
	         	node = d	
	         	triggerChangedEvent(true);
	         	force.start();
			 });
		});
		initSelectNodeToResize();
		return node;
	};
	
	
	// 将元素加入组合
	function addToGroup(target,group){
		target.groupId = group.id;
		group.ids.push(target.id);
	};
	// 将元素取消组合
	function removeToGroup(target, group){
		var ids = group.ids
		for(var i = 0; i < group.ids.length; i++){
			if(group.ids[i] == target.id){
				ids.splice(i,1)
			}
		}
		delete target.groupId;
		
	};
	
	

	
	
	topoEditor.ungroupNodes = function(callback){
		var selected_node = d3.select('.node.selected svg,.group.selected svg');
		var sd = selected_node.data();
		if(sd[0]){// sd[0]存在，控制删除点的时候触发删除组合
			if(sd[0].groupId&&sd[0].nodeType=='node'){
				// _lastStep = {node:sd[0],action:'group'};
				// _removeSteps.push(_lastStep);
				// triggerActionEvent();
				
				var Id = sd[0].groupId;
				sd[0].groupId=null;
				var gp = d3.select('#group'+Id +' svg').data();
				gp[0].ids.splice(gp[0].ids.indexOf(sd[0].id),1);
				sd[0].x=gp[0].x+gp[0].width/2+sd[0].width+sd[0].width/2;
				selected_node.attr('x',sd[0].x-sd[0].width/2);
				
				// 移动标签
         		if(d3.select(selected_node[0][0].parentNode).select('.label')[0].length!=0){
         		 	d3.select(selected_node[0][0].parentNode).select('.label').each(function(d){
         		  	d.x =sd[0].x+sd[0].width/2;
             		var editNode = d3.select(this);
             		editNode.attr('x',d.x-sd[0].width/2).attr('y',d.y);
             	});  
             	}
			}
			else if(sd[0].nodeType=='group'){
				selected_node.each(function(d){
					d3.select(this.parentNode).remove();
					// 历史记录中的当前节点连线
					var toRmLink = _addSteps.filter(function(l) {
						return (l.node==null&&l.links.length==1&&(l.links[0].source.id==d.id||l.links[0].target.id==d.id));
					});
					// 历史记录中关于当前节点的所有操作
					var toRmNode = _addSteps.filter(function(l){
						return (l.node!=null&&l.node.id==d.id);
					});
					
					// 从历史记录中清除当前节点操作记录
						toRmNode.map(function (n){
							_addSteps.splice(_addSteps.indexOf(n),1);
						});
						
					
					// 从历史记录中清除当前节点连线的操作记录
					toRmLink.map(function(l){
						// var action = {node:null,links:[l]};
						_addSteps.splice(_addSteps.indexOf(l), 1);
						
					});
					// 当前节点的连线
					var toSplice = _links.filter(function(l){
						return ((l.source.id == d.id &&l.source.nodeType==d.type)|| (l.target.id == d.id&&l.target.nodeType==d.type));
					});
					_lastStep = {node:d,links:toSplice};					
					_removeSteps.push(_lastStep);				
					triggerActionEvent();
					d.ids.map(function(nodeId){
						d3.select('#node'+nodeId+' svg').datum().groupId=null;
					});
					
					spliceLinksForNode(d);
				});
				// callback(_topoId,sd[0]);
				_miscTransactions.push(sd[0]);
			}
			drawlines();
		}
		
	};
	topoEditor.addShape = function(snode,callback){
		var sId = callback(_topoId);
		snode.id = sId;
		initiative = false;
		addNode(snode,null);
		triggerChangedEvent(true);
	};
	topoEditor.groupNodes = function(icon,callback){
		var gpNodes = d3.selectAll('.node.selected svg');
		var gx=200000,gy=20000,gw=0,gh=0,ids=[];
		var Id = callback(_topoId);
		gpNodes[0].forEach(function(e){
			d3.select(e).each(function(d){
				ids.push(d.id);
				d.groupId = Id;
			
				gx=gx<d.x-d.width/2?gx:d.x-d.width/2;
				gy=gy<d.y-d.height/2?gy:d.y-d.height/2;
				
				gw=gw>d.x+d.width/2?gw:d.x+d.width/2;
				gh=gh>d.y+d.height/2?gh:d.y+d.height/2;
			});

		});
		
		force.start();
		// initiative = false;
		// inintiative = true//在组合框中改变节点扔可重新定位线条
		var gnode={'id':Id,'x':gx,'y':gy,'size':Math.ceil((gw-gx)/120)>=Math.ceil((gh-gy)/80)?Math.ceil((gw-gx)/120):Math.ceil((gh-gy)/80),'icon':icon,'nodeType':'group','ids':ids};
		addNode(gnode, null);
		triggerChangedEvent(true);
	};
	
	


    
	topoEditor.getSourceId = function(){
		return sourceLinkId;
	};
	var sourceLinkId=null;
		
	topoEditor.saveTopo = function(){	
	   var outPutNodes = d3.selectAll('g .node svg').data();
	   var outPutMisc = d3.selectAll('g .misc svg').data();
	   var outPutLinks=[];
	   var oNodes = [];
	   var mNodes =[];
	   outPutMisc.map(function(m){
		   var n ={};
		   for(var v in m){
				n[v]=m[v];
			}
		   n.x-=n.width/2;
		   n.y-=n.height/2;
		   mNodes.push(n);
	   });
	   
	   outPutNodes.map(function(m){
		   var n ={};
		   for(var v in m){
				n[v]=m[v];
			}
		   n.x-=n.width/2;
		   n.y-=n.height/2;
		   oNodes.push(n);
		   
	   });
	   
	   _links.map(function(l){
		   var tmpLink = {};
			for(var v in l){
				tmpLink[v]=l[v];
			}
			tmpLink.source = l.source.id;
			tmpLink.target = l.target.id;
			tmpLink.sourceType = l.source.nodeType;
			tmpLink.targetType = l.target.nodeType;
		   outPutLinks.push(tmpLink);// {'type':l.nodeType,'source':l.source.id,'target':l.target.id,color:l.color}
	   });
			topoInfo = {
				'id':_topoId,
				'nodes' : oNodes,
				'links' : outPutLinks,
				'misc'	:mNodes,
				'miscTran': _miscTransactions,
				'parent':0
			};
			triggerChangedEvent(false);
			return topoInfo;
			
	};
	
	function openInNewTab(url) {
	    var a = document.createElement("a");
	    a.target = "_blank";
	    a.setAttribute('rel','tab');
	    a.href = url;
	    a.click();
	};
	function afterCleanTopo(topoId){
		d3.selectAll('.nodes g.node.view'+topoId).remove();
		d3.selectAll('.nodes g.misc.view'+topoId).remove();
		d3.selectAll('.links path.link.view'+topoId).remove();
		d3.selectAll('g.linkLabels.view'+topoId).remove();
		d3.selectAll('marker.endarrow.view'+topoId).remove();
		d3.selectAll('path.deftextpath.view'+topoId).remove();
		d3.selectAll('.linksbg path.linkbg.view'+topoId).remove();
		
	};
	
	function hideDirection(){
		d3.selectAll('.button').style('opacity',0);
	}
	
	function showDirection(){
		d3.selectAll('.button').style('opacity',1);
	}
	topoEditor.showDirection = function(){
		showDirection();
	}
	
	topoViewer.showDirection = function(){
		showDirection();
	}
	topoEditor.hideDirection = function(){
		hideDirection();
	}
	
	topoViewer.hideDirection = function(){
		hideDirection();
	}
	
	
	topoEditor.clear = function(){
		cleanTopo();
	}
	
	topoViewer.clear = function(){
		
		cleanTopo();
	}
	function cleanTopo(){
		d3.selectAll('.nodes g.node').remove();
		d3.selectAll('.nodes g.misc').remove();
		d3.selectAll('.links path.link').remove();
		d3.selectAll('g.linkLabels').remove();
		d3.selectAll('marker.endarrow').remove();
		d3.selectAll('path.deftextpath').remove();
		d3.selectAll('.linksbg path.linkbg').remove();
		_links=[];
		_addSteps=[];
		_removeSteps=[];
		  _lastStep = null;
		  _restoreMode = false;
		 edges = [];
		 resetMouseVars();
		 drawlines();
		 triggerActionEvent();
	}
	topoEditor.initTopo = function(topoId,nodes,links,groupNodes){
		// clean all nodes
		oldTopoId = _topoId;
		
		_topoId = topoId;
		cleanTopo();
		initTopo(nodes,links,groupNodes);
		disableLinkBg();
	}
	topoViewer.initTopo = function(topoId,nodes,links){
		oldTopoId = _topoId;
		_topoId = topoId;
		// clean all nodes
		cleanTopo();
		initTopo(nodes,links);
		disableLinkBg();
	}
	topoViewer.setParentTopoId = function(parentId){
		_parentTopoId = parentId;
	}
	topoEditor.setParentTopoId = function(parentId){
		_parentTopoId = parentId;
	}
	function initTopo(nodes,links,groupNodes){
		initiative = true;
		if(!nodes||nodes.length==0) return;
		// lastNodeId = nodes[nodes.length - 1].id;
		// 将由数据库取得的link转为D3可读的link信息
		
		if(links){
			links.forEach(function(e) {
				 var sourceNode = nodes.filter(function(n) {
				 return n.id === e.source&&n.nodeType==e.sourceType;
				 })[0],
				 targetNode = nodes.filter(function(n) {
				 return n.id === e.target&&n.nodeType==e.targetType;
				 })[0];
				 var newlink = {};
				 for(var v in e){
					 newlink[v]=e[v];
					}
				 newlink.source = sourceNode;
				 newlink.target =  targetNode;
				 edges.push(newlink);
				 });
		}
		 /* link数据转换完成 */
/*
 * if(groupNodes&&groupNodes.length>0){ groupNodes.forEach(function(e){
 * addNode(e); }); }
 */
	
			nodes.forEach(function(e) {
				addNode(e);
			});
			triggerChangedEvent(false);
			d3.select('g.nodes').style('opacity',1);
	}
	function keyup() {
		 switch(d3.event.keyCode) {
		 case 37:
		 case 38:
		 case 39:
		 case 40:
			 activeAxisYLine.classed('activeAxis',false);
		        activeAxisXLine.classed('activeAxis',false);
		        break;
		 }
			 
			 
		
		 lastKeyDown = -1;// d3.event.keyCode;
		}
	function keydown(){
		// f12
		if(d3.selectAll('.node.selected svg')[0].length==0&&selected_link==null)return;
		if(d3.event.keyCode==123)return;
		 d3.event.preventDefault();
		 if(secondLastKeyDown==-1)
		 secondLastKeyDown = lastKeyDown;
		 
		 if(lastKeyDown==-1)
		 lastKeyDown = d3.event.keyCode;
		  if(lastKeyDown == -1) return;
		  switch(d3.event.keyCode) {
		    case 8: // backspace
		    case 46: // delete
		      topoEditor.delSelected();
		      return;
		      break;
		    case 17:// ctrl
		    	// lastKeyDown = -1;
		    	secondLastKeyDown =17;
		    	break;
		    case 90:// z
		    	if(secondLastKeyDown==17)
		    	topoEditor.cancle();
		    	break;
		    	
		    case 82:// r
		    	if(secondLastKeyDown==17)
		    		topoEditor.recover();
		    	break;
		    case 37:// left
		    	
		    	moveSelected(-5,0);
		    	break;
		    case 38:// up
		    	moveSelected(0,-5);
		    	break;
		    case 39:// right
		    	moveSelected(5,0);
		    	break;
		    case 40:// down
		    	moveSelected(0,5);
		    	break;
		    	
		     
		  }
		
	}
	
	topoEditor.setLineColor = function(color){
		if(!selected_link)return;
		d3.select('#end-arrow'+selected_link.source.nodeType+selected_link.source.id+selected_link.target.nodeType+selected_link.target.id +' path').attr('fill',color);
		d3.select('#bg-end-arrow'+selected_link.source.nodeType+selected_link.source.id+selected_link.target.nodeType+selected_link.target.id +' path').attr('fill',color);
		d3.select('#link'+selected_link.source.nodeType+selected_link.source.id+selected_link.target.nodeType+selected_link.target.id).style('stroke',color);
		d3.select('#label'+selected_link.source.nodeType+selected_link.source.id+selected_link.target.nodeType+selected_link.target.id+' text').style('fill',color);
		d3.select('#linkbg'+selected_link.source.nodeType+selected_link.source.id+selected_link.target.nodeType+selected_link.target.id).style('stroke',color);
		var links=_links.filter(function(l){
			return (l.source.id==selected_link.source.id&&l.target.id==selected_link.target.id);
		});
		links.map(function(l){
			 _links[_links.indexOf(l)].color=_lineColor;
		});
		triggerChangedEvent(true);
		
	};
	
	topoEditor.setNodeColor = function(color){
		// if(!selected_node)return;
		var selectedNodes = d3.selectAll('.node.selected svg,.misc.selected svg');
		selectedNodes[0].forEach(function(e){
			d3.select(e).style('fill',function(d){
				d.color = color;
				d3.select(this.parentNode).classed('selected',false).classed('dragged',false);
				initSelectNodeToResize();
				return color;
			});
		});
		
		// d3.selectAll('.node.selected').classed('selected',false).classed('dragged',false);
		triggerSelectedEvent();
		triggerChangedEvent(true);		
	};
	
	topoEditor.setLineColorMode = function(color){
		_lineColor= color;
		// 设置动态线的颜色
		drag_line.style('stroke',_lineColor);
		d3.select('#end-arrow  path').attr('fill',color);
		// 设置线条颜色
		topoEditor.setLineColor(_lineColor);
	};
	
	topoEditor.addContextMenu = function(type,contextMenu){
		
		_contextMenus.type = contextMenu;
		
	};
	
	topoEditor.addClickAction = function(type,clickAction){
		// _clickActions.push({type:clickAction});
		_clickActions.type=clickAction;
	};
	
	function getContextMenu(type){
		return  _contextMenus.type;
	}
	function getClickAction(type){
		return _clickActions.type;
	}
	
	topoEditor.addNode = function(node,callback){
		initiative = false;
		var newNode ={};
		for(var v in node){
			newNode[v]=node[v];
		}
		newNode['x']  = (node.x - translation[0]) / scaleFactor;
		newNode['y']  = (node.y  - translation[1]) / scaleFactor;
		var allNodeData = d3.selectAll('g.node svg').data();
		var existed = false;
		allNodeData.map(function(n){
			if(n.id==newNode.id&&n.nodeType==newNode.nodeType){
				callback('exist');
				existed = true;
				return;
			}
		});
		// 如果已经存在此视图中，则不添加
		if(!existed){
			addNode(newNode,callback);
			triggerChangedEvent(true);
		}
		
		
	};
	
	topoEditor.setLineType = function(type){
		// drawLineSelected = true;
		lineType = lineTypes[type];
	};

	topoEditor.setLinkMode = function(){
		dragMode = false;
		cancelDragMode()
		drawLineSelected = true;
		d3.selectAll('.nodes>.node,.nodes>.misc').classed('connectable',true);
// d3.select('.brush rect').classed('connectable',true);
		d3.select('g#background-map').classed('connectable',true);
	};
	
	topoEditor.setSelectMode = function(){
		dragMode = false;
		cancelDragMode();
		drawLineSelected = false;
		d3.selectAll('.nodes >.node,.nodes>.misc').classed('connectable',false);
		d3.select('.brush rect').classed('connectable',false);
		d3.select('g#background-map').classed('connectable',false);
	};
	
	topoEditor.setDragMode = function(){
		 dragMode = true;
		 if(dragMode){
			// cancelBrush();
			 var xpre=null;
			 var ypre=null;
			 $('div.canvas>svg').on('mousemove.dragMode', function(){
				 $(this).css('cursor','-webkit-grab');
			 }).on('mousedown.dragMode',function(d){
				 xpre = d.pageX;
				 ypre = d.pageY;
			 }).on('mouseover.dragMode',function(){
				 $('div.canvas>svg').css('cursor','-webkit-grab');
			 }).on('mouseenter.dragMode', function(){
				 $(this).css('cursor','-webkit-grab');
			 }).on('mouseleave.dragMode',function(){
			
					$(this).css('cursor', 'default');
				})
			 
			 var dragModeDrag = d3.behavior.drag()
			 .origin(null)
			 .on("dragstart", function(d){
				 
			 })
			 .on("dragend", function(d){
				 $('g.brush>rect.extent').css('opacity','')
			 })
			 .on("drag", function(d){
				 $('g.brush>rect.extent').css('opacity',0).css('cursor','default')
				 d3.select(this)
				 .attr("cx", function(){
					 var  dis = d3.event.x - xpre;
					 xpre = d3.event.x ;
					 pan(dis,0);
				 })
				 .attr("cy", function(){
					 var  dis = d3.event.y - ypre;
					 ypre = d3.event.y ;
					 pan(0,dis);
				 })
			 })
			 
			 d3.select('div.canvas>svg').call(dragModeDrag);
		 }
		
	}
	
	function cancelDragMode(){
			$('div.canvas>svg').off('.dragMode')
			d3.select('div.canvas>svg').on('.drag',null)
		
	}
	
	topoEditor.clickLink = function(action,Callback){
		return function(data){
			var elm = this;
			 d3.event.stopPropagation();
				if(!_viewOnly){
					if(drawLineSelected&&mousedown_node)return;
					if(d3.select(this).classed('selected')){
						d3.select('path.link.selected').classed('selected',false);
						selected_link=null;
						triggerSelectedEvent();
						return;
					}
			
						d3.select('path.link.selected').classed('selected',false);
						d3.select(this).classed('selected',true).classed('dragged',true);
						d3.selectAll('.node.selected,.misc.selected').classed('selected',false).classed('dragged',false);
						selected_link=data;
						triggerSelectedEvent();				
							if(!action)return;
							action(elm,data);
							updateLineLabel(data.source.id,data.target.id,data.name,data.source.nodeType,data.target.nodeType);
		}
		
		
	};
	
	};
	
/*
 * topoEditor.doubleClickLink = function(action,Callback){ return
 * function(data){ } }
 */
	
	
	function doubleClickBakcground(action){
			 if(!action||_parentTopoId==_topoId||mouseover_node||mouseover_link||mouseover_pan||mouseover_nodelabel)return;
				action(_parentTopoId);
	} 
	function clickBackground(action){
		if(action){
			action();
		}
		
		event.stopPropagation();
	}
	
/*
 * topoViewer.doubleClickNode = function(data,action){ console.log('in')
 * tooltip.style("display", "none"); tooltip.html(''); if(!action)return;
 * action(data); }
 * 
 * topoEditor.doubleClickNode = function(data,action){ console.log('inin')
 * tooltip.style("display", "none"); tooltip.html('');
 * drag_line.classed('hidden',true).style('marker-end', '');
 * if(data.nodeType=='shape'||data.nodeType=='group'){ var elm=
 * d3.select('#'+data.nodeType+data.id).select('.label'); var l = elm.datum();
 * editLabel(data, l, elm); } if(!action)return; action(data); //
 * event.stopPropagation(); }
 */
	
/*
 * topoViewer.doubleClick
 * 
 * topoViewer.doubleClickLinkLabel = function(data, action){ }
 */
	
	
	topoEditor.clickNode = function(action,Callback){
			
			return function(data){
				tooltip.style("display", "none");
		        tooltip.html('');
		        // 重置单击事件标志触发器
		        wait = null;
		        
				var elm =null;
				if(data.nodeType=='view'){
					elm = d3.select('g#node'+data.id)[0][0];
				}else{
					elm = d3.select('g#'+data.nodeType+data.id)[0][0];
				}
				 
				// 阻止click事件影响到上层画布
				// d3.event.stopPropagation();
				// 判断drag事件默认的click
				// if (d3.event.defaultPrevented) return;
				if(!_viewOnly){
					if(lastKeyDown==17){
						if(d3.select(elm).classed('selected')){// &&!drawLineSelected
							d3.select(elm).classed('selected',false).classed('dragged',false);
							initSelectNodeToResize();
						}
						else{
							d3.select(elm).classed('selected',true).classed('dragged',true);	
							selectNodeToResize(data);
						}
					}
					else{
						d3.select('path.link.selected').classed('selected',false);
						if(d3.select(elm).classed('selected')){// &&!drawLineSelected
							if(d3.selectAll('.node.selected')[0].length>1){// 在已选多个icon情况下点击一个icon
								d3.selectAll('.node.selected').classed('selected',false).classed('dragged',false);
								initSelectNodeToResize();
								d3.select(elm).classed('selected',true).classed('dragged',true);
								selectNodeToResize(data);

							}
							else{
								d3.select(elm).classed('selected',false).classed('dragged',false);
							}
							
						}
						else{// 单独点击一个icon
							d3.selectAll('.node.selected,.misc.selected').classed('selected',false).classed('dragged',false);
							d3.select(elm).classed('selected',true).classed('dragged',true);
						}
					}
					triggerSelectedEvent();
					
					if(!_clickActions[data.nodeType]){
						if(!action)return;
						action(elm,data);
					}
					else{
						_clickActions[data.nodeType](elm, data);
					}
				}
				
				// d3.event.preventDefault();
				
				
			};
		
		
		
	};
	
	var initSelectNodeToResize  =function(){
		$(".selectSize").remove();
	}
	
	var selectNodeToResize = function(data) {
		var nodeType = data.nodeType == "view" ? "node" : data.nodeType;
		var $selectNode = $("g#"+nodeType+data.id+">svg");
		var $selectNodeLabel = $("g#"+nodeType+data.id+" text");
		var d3selectNode = d3.select("g#"+nodeType+data.id+">svg");
		var d3selectNodeLabel = d3.select("g#"+nodeType+data.id+" text");
		var selectNodeAttrX = parseInt($selectNode.attr('x'));
		var selectNodeAttrY = parseInt($selectNode.attr('y'));
		var selectNodeWidth = parseInt($selectNode.attr('width'));
		var origNodeWidth = parseInt($selectNode.attr('width'));
		var origNodeHeight = parseInt($selectNode.attr('height'));
		var selectNodeHeight = parseInt($selectNode.attr('height'));
		var proportion = origNodeWidth / origNodeHeight; //宽与高的比例
		var selectNodeR = 3; 
		var selectNodeColor = 'white';
		var labelPosX = null;
		var minSize = 20;
		var nodeNum = [1, 7, 9, 3, 1];
		var node = d3.select("g#"+nodeType+data.id).append('g').data([{x: selectNodeAttrX + origNodeWidth/2, y: selectNodeAttrY + origNodeHeight/2}]).attr("class", "selectSize");
	//	var node = d3.select("g#" + nodeType + data.id)
		
		
		var dragLeftTop = d3.behavior.drag()
		.origin(Object)
		.on("dragstart", reset)
		.on("drag", dragnw)
		.on("dragend", savePos); 
		
		var dragRightBottom = d3.behavior.drag()
		.origin(Object)
		.on("dragstart", reset)
		.on("drag", dragse)
		.on("dragend", savePos);
		
		var dragLeftBottom = d3.behavior.drag()
		.origin(Object)
		.on("dragstart", reset)
		.on("drag", dragsw)
		.on("dragend", savePos)
		
		var dragRightTop = d3.behavior.drag()
		.origin(Object)
		.on("dragstart", reset)
		.on("drag", dragne)
		.on("dragend", savePos)
		//左上的点		
		var dragOne = node.append('circle')
		.data([{x: selectNodeAttrX, y: selectNodeAttrY}])
		.attr("class", "c1 select selectCircle")
		.attr("id", "c1" + data.id)
		.attr("cx",function(d){
			return selectNodeAttrX;
		})
		.attr("cy", function(d){
			return selectNodeAttrY;
		})
		.attr("r", selectNodeR)
		.attr("fill", selectNodeColor)
		.style("cursor", "nw-resize")
		.call(dragLeftTop);
		//右上的点
		var dragThree = node.append('circle')
		.data([{x: selectNodeAttrX + origNodeWidth, y: selectNodeAttrY}])
		.attr("class", "c3 select selectCircle")
		.attr("id", "c3" + data.id)
		.attr("cx", function(d){
			return selectNodeAttrX+origNodeWidth;
		})
		.attr("cy", function(d){
			return selectNodeAttrY;
		})
		.attr("r", selectNodeR)
		.attr("fill", selectNodeColor)
		.style("cursor", "ne-resize")
		.call(dragRightTop);
		
		//左下的点
		var dragSeven = node.append('circle')
		.data([{x: selectNodeAttrX, y: selectNodeAttrY+origNodeHeight}])
		.attr("id", "c7" + data.id)
		.attr("class", "c7 select selectCircle")
		.attr("cx", function(d){
			return selectNodeAttrX;
		})
		.attr("cy", function(d){
			return selectNodeAttrY+origNodeHeight;
		})
		.attr("r", selectNodeR)
		.attr("fill", selectNodeColor)
		.style("cursor", "sw-resize")
		.call(dragLeftBottom);
		
		//右下的点
		var dragNine = node.append('circle')
		.data([{x:selectNodeAttrX+origNodeWidth,y:selectNodeAttrY+origNodeHeight}])
		.attr("class", "c3 select selectCircle")
		.attr("id", "c9" + data.id)
		.attr("cx", function(d){
			return selectNodeAttrX+origNodeWidth;
		})
		.attr("cy", function(d){
			return selectNodeAttrY+origNodeHeight;
		})
		.attr("r", selectNodeR)
		.attr("fill", selectNodeColor)
		.style("cursor", "se-resize")
		.call(dragRightBottom);
		

		var c1 = $("#c1" + data.id);
		var c3 = $("#c3" + data.id);
		var c7 = $("#c7" + data.id);
		var c9 = $("#c9" + data.id);

		for (var i = 0; i < 4; i++){
			var dragLine = node.append('path')
			.attr("id", "dragBorder" + data.id + "_" + i)
			.attr("class", "select dragBorder")
		    .attr("fill", "none")
		    .attr("stroke", selectNodeColor)
		    .attr("stroke-width", 1)
		    .attr("stroke-dasharray", 10,10)
		}
		dragBorder();
		function dragBorder(){
			for (var i = 0; i < 4; i++){
				$("#dragBorder"+ data.id + "_" + i).attr("d", function(d){
					return "M"+$("#c"+ nodeNum[i] + data.id).attr("cx")+"," + $("#c"+ nodeNum[i] + data.id).attr("cy")+ "L"+$("#c"+ nodeNum[i + 1] + data.id).attr("cx")+","+$("#c"+ nodeNum[i + 1] + data.id).attr("cy")
					}) 
			}

				  
		}
		
		function reset(){//重新获取当前大小和位置
			selectNodeAttrX = parseInt($selectNode.attr('x'));//拖拽动作完成一次更新一次
			selectNodeAttrY = parseInt($selectNode.attr('y'));
			origNodeWidth = selectNodeWidth;
			origNodeHeight = selectNodeHeight;
		}
		
		function savePos(){//保存拖拽完后的坐标
			dragOne.data([{x: c1.attr("cx"), y: c1.attr("cy")}]);
			dragThree.data([{x: c3.attr("cx"), y: c3.attr("cy")}]);
			dragSeven.data([{x: c7.attr("cx"), y: c7.attr("cy")}]);
			dragNine.data([{x: c9.attr("cx"), y: c9.attr("cy")}]);
			d3selectNodeLabel[0].forEach(function(l){
				d3.select(l).each(function(n){
					n.x = parseInt($selectNodeLabel.attr("x"));
					n.y = parseInt($selectNodeLabel.attr("y"));
				});		
			});	
		}

		function dragse(d){
			var dis = parseInt(Math.max(d3.event.x-d.x, d3.event.y-d.y));//每次放大缩小的距离
			selectNodeWidth = dis + origNodeWidth > minSize ? dis + origNodeWidth : minSize;//每次放大缩小后node应该的宽度(实时)
			selectNodeHeight = selectNodeWidth / proportion//每次放大缩小后Node的高度
			labelPosX = dis + origNodeWidth > minSize ? dis : 0;
				dragNine.attr("cx", function(d){
					return (dis + origNodeWidth > minSize ? parseInt(d.x)+dis : selectNodeAttrX + minSize);
				}).attr("cy", function(d){
					return (dis + origNodeWidth > minSize ? parseInt(d.y)+dis / proportion: selectNodeAttrY + minSize / proportion);
				});
				
				dragThree.attr("cx", function(d){
					return (dis + origNodeWidth > minSize ? parseInt(d.x)+dis : selectNodeAttrX + minSize);
				});
				 
				dragSeven.attr("cy", function(d){
					return (dis + origNodeWidth > minSize ? parseInt(d.y)+dis / proportion: selectNodeAttrY + minSize / proportion);
				});
				
				// 改变icon大小
				d3selectNode[0].forEach(function(d){
					d3.select(d).each(function(e){
						e.width = selectNodeWidth; 
						e.height = selectNodeHeight;
						e.x = selectNodeAttrX + selectNodeWidth/2;
						e.y = selectNodeAttrY + selectNodeHeight/2;
						e.size = selectNodeWidth/36;
						d3.select(this)
						.attr("width", selectNodeWidth)
						.attr("height", selectNodeHeight);					
						// 改变text位置
						dis + origNodeWidth > minSize ? $selectNodeLabel.show() : $selectNodeLabel.hide();//缩小成最小隐藏名称
						d3selectNodeLabel[0].forEach(function(l){
									d3.select(l).each(function(n){
										n.y = e.y + selectNodeHeight + 12;
										d3.select(this)
										.attr("x", n.x + labelPosX/2)
										//.attr("y", n.y + 12);
										.attr("y", selectNodeAttrY + selectNodeHeight + 12);
									});		
							});					
						});
				});
				dragBorder();
				drawlines();
			

		}
		
		function dragnw(d){		
			var dis = parseInt(Math.max(d3.event.x-d.x, d3.event.y-d.y));
			selectNodeWidth = -dis + origNodeWidth > minSize ? -dis + origNodeWidth : minSize;//每次放大缩小后node应该的宽度(实时)
			selectNodeHeight = selectNodeWidth / proportion;//每次放大缩小后Node的高度
			labelPosX = dis;
			dragOne.attr("cx", function(d){
				return (-dis + origNodeWidth > minSize ? parseInt(d.x)+dis : selectNodeAttrX + origNodeWidth - minSize);
			}).attr("cy", function(d){
				return (-dis + origNodeWidth > minSize ? parseInt(d.y)+dis / proportion: selectNodeAttrY + origNodeHeight - minSize / proportion);
			});
			
			dragSeven.attr("cx", function(d){
				return (-dis + origNodeWidth > minSize ? parseInt(d.x)+dis : selectNodeAttrX + origNodeWidth - minSize);
			});
			
			dragThree.attr("cy", function(d){
				return (-dis + origNodeWidth > minSize ? parseInt(d.y)+dis / proportion: selectNodeAttrY+ origNodeHeight - minSize / proportion);
			});
			
			// 改变icon大小
			d3selectNode[0].forEach(function(d){
				d3.select(d).each(function(e){
					e.width = selectNodeWidth; 
					e.height = selectNodeHeight;
					e.x = selectNodeAttrX + (-dis + origNodeWidth > minSize ? dis / 2 : 0) + origNodeWidth / 2;//e.x为icon中心
					e.y = selectNodeAttrY + (dis / 2) / proportion + origNodeHeight / 2;//e.y为icon中心
					e.size = selectNodeWidth / 36;
					d3.select(this)
					.attr("width", selectNodeWidth)
					.attr("height", selectNodeHeight)
					.attr("x", (-dis + origNodeWidth > minSize ? selectNodeAttrX+dis : selectNodeAttrX + origNodeWidth - minSize))
					.attr("y", (-dis + origNodeWidth > minSize ? selectNodeAttrY+dis / proportion: selectNodeAttrY + origNodeHeight - minSize / proportion));
					// 改变text位置					
					-dis + origNodeWidth > minSize ? $selectNodeLabel.show() : $selectNodeLabel.hide();//缩小成最小隐藏名称
					d3selectNodeLabel[0].forEach(function(l){
								d3.select(l).each(function(n){
									d3.select(this)
									.attr("x", n.x + labelPosX / 2)
									.attr("y", selectNodeAttrY + origNodeHeight + 12);
								});		
						});					
					});
			});
			dragBorder();
			drawlines();
		}
		
		function dragne(d){
			var dis = parseInt(Math.max(d3.event.x - d.x, d.y - d3.event.y));
			selectNodeWidth = dis + origNodeWidth > minSize ? dis + origNodeWidth : minSize;//每次放大缩小后node应该的宽度(实时)
			selectNodeHeight = selectNodeWidth / proportion;
			labelPosX = dis;
			dragThree.attr("cx", function(d){
				return dis + origNodeWidth > minSize ? parseInt(d.x) + dis : selectNodeAttrX + minSize;
			}).attr("cy", function(d){
				return dis + origNodeWidth > minSize ? parseInt(d.y) - dis / proportion: selectNodeAttrY + origNodeHeight - minSize / proportion;
			});
			
			dragNine.attr("cx", function(d){
				return dis + origNodeWidth > minSize ? parseInt(d.x) + dis : selectNodeAttrX + minSize;
			});
			
			dragOne.attr("cy", function(d){
				return dis + origNodeWidth > minSize ? parseInt(d.y) - dis / proportion: selectNodeAttrY + origNodeHeight - minSize / proportion;
			});
			
			// 改变icon大小
			d3selectNode[0].forEach(function(d){
				d3.select(d).each(function(e){
					e.width = selectNodeWidth; 
					e.height = selectNodeHeight;
					e.x = dis + origNodeWidth > minSize ? selectNodeAttrX + dis / 2 + origNodeWidth / 2 : selectNodeAttrX + minSize / 2 ;
					e.y = dis + origNodeWidth > minSize ? selectNodeAttrY - dis / 2 + origNodeWidth / 2 : selectNodeAttrY + origNodeHeight - minSize/ proportion / 2;
					e.size = selectNodeWidth / 36;
					d3.select(this)
					.attr("width", selectNodeWidth)
					.attr("height", selectNodeHeight)
					.attr("x", selectNodeAttrX)
					.attr("y", dis + origNodeWidth > minSize ? selectNodeAttrY - dis / proportion: selectNodeAttrY + origNodeHeight  - minSize / proportion );

					// 改变text位置	
					dis + origNodeWidth > minSize ? $selectNodeLabel.show() : $selectNodeLabel.hide();//缩小成最小隐藏名称
					d3selectNodeLabel[0].forEach(function(l){
								d3.select(l).each(function(n){
									d3.select(this)
									.attr("x", n.x + labelPosX/2)
									.attr("y", selectNodeAttrY + origNodeHeight + 12);
								});		
						});					
					});
			});
			dragBorder();
			drawlines();
		}
		
		function dragsw(d){
			var dis = parseInt(Math.max(d.x - d3.event.x, d3.event.y - d.y));
			selectNodeWidth = dis + origNodeWidth > minSize ? dis + origNodeWidth : minSize;//每次放大缩小后node应该的宽度(实时)
			selectNodeHeight = selectNodeWidth / proportion;
			labelPosX = dis + origNodeWidth > minSize ? dis : 0;
			dragSeven.attr("cx", function(d){
				return dis + origNodeWidth > minSize ? parseInt(d.x) - dis : selectNodeAttrX + origNodeWidth - minSize;
				
			}).attr("cy", function(d){
				return dis + origNodeWidth > minSize ? parseInt(d.y) + dis / proportion: selectNodeAttrY  + minSize / proportion;
			});
			
			dragOne.attr("cx", function(d){
				return dis + origNodeWidth > minSize ? parseInt(d.x) - dis : selectNodeAttrX + origNodeWidth - minSize;
			});
			
			dragNine.attr("cy", function(d){
				return dis + origNodeWidth > minSize ? parseInt(d.y) + dis / proportion: selectNodeAttrY  + minSize / proportion;
			});
			
			// 改变icon大小
			d3selectNode[0].forEach(function(d){
				d3.select(d).each(function(e){
					e.width = selectNodeWidth; 
					e.height = selectNodeHeight;
					e.x = dis + origNodeWidth > minSize ? selectNodeAttrX - dis / 2 + origNodeWidth / 2 : selectNodeAttrX + origNodeWidth - minSize / 2;
					e.y = dis + origNodeWidth > minSize ? selectNodeAttrY + dis / 2 + origNodeWidth / 2 : selectNodeAttrY  + minSize/ proportion / 2;
					//e.y = selectNodeAttrY + dis/2 + origNodeHeight/2;
					e.size = selectNodeWidth/36;
					d3.select(this)
					.attr("width", selectNodeWidth)
					.attr("height", selectNodeHeight)
					.attr("x", (dis + origNodeWidth > minSize ? selectNodeAttrX - dis : selectNodeAttrX + origNodeWidth - minSize))
					.attr("y", selectNodeAttrY );
					// 改变text位置
					dis + origNodeWidth > minSize ? $selectNodeLabel.show() : $selectNodeLabel.hide();//缩小成最小隐藏名称
					d3selectNodeLabel[0].forEach(function(l){
								d3.select(l).each(function(n){
									d3.select(this)
									.attr("x", n.x - labelPosX/2)
									.attr("y", selectNodeAttrY + selectNodeHeight + 12);
								});		
						});					
					});
			});
			dragBorder();
			drawlines();
		}


	};
	
	
	topoEditor.bindNodeData = function(nodeId,privateData){
		var specificNode = d3.select('#node'+nodeId).select('svg');
		specificNode.each(function(d){
			if((typeof privateData)=="object"){
				for(var x in privateData){
					d[x]=privateData[x];
				}
			}
			else{
				d.privateData = privateData;
			}
		});
	};
	
	topoEditor.contextMenu = function(menu, openCallback){

			

			// this gets executed when a contextmenu event occurs
			return function(data) {
				var privateMenu =null;
				if(!_contextMenus[data.nodeType]){
					if(!menu)return;
					privateMenu = menu;
				}
				else{
					privateMenu=_contextMenus[data.nodeType];
				}
				// create the div element that will hold the context menu
				d3.selectAll('.topo-context-menu').data([1])
					.enter()
					.append('div')
					.attr('class', 'topo-context-menu');

				// close menu
				d3.select('body').on('click.topo-context-menu', function() {
					d3.select('.topo-context-menu').style('display', 'none');
				});
				
				var elm = this;
				d3.selectAll('.topo-context-menu').html('');
				var list = d3.selectAll('.topo-context-menu').append('ul');
				list.selectAll('li').data(typeof menu === 'function' ? privateMenu(data) : privateMenu).enter()
					.append('li')
					.html(function(d) {
						return (typeof d.item === 'string') ? d.item : d.item(data);
					})
					.on('click', function(d) {
						d.action(elm, data);
						d3.select('.topo-context-menu').style('display', 'none');
					});

				// the openCallback allows an action to fire before the menu is
				// displayed
				if (openCallback) {
					if (openCallback(data) === false) {
						return;
					}
				}
				// display context menu
				d3.select('.topo-context-menu')
					.style('left', (d3.event.pageX - 2) + 'px')
					.style('top', (d3.event.pageY - 2) + 'px')
					.style('display', 'block');

				d3.event.preventDefault();
				d3.event.stopPropagation();
			};
		
	};
	
	
	
	topoEditor.addCanvasNode = function(node){
		// var x
		// ={nodeIcon:_nodeIcons,clickAction:_clickActions,contextMenu:_contextMenus};
		_nodeIcons[node.getType()]=node.getIcon();
		_clickActions[node.getType()]=node.getClickAction();
		_contextMenus[node.getType()]=node.getContextMenu();
		/*
		 * var func = {attr:undefined}; func.attr = function(key,value){
		 * x[key][typeName]=value; return func; }; return func;
		 */
	};
	
	
	return  topo;
	
}

));

function CanvasNode() {
    this.icon =null;
    this.clickAction = null;
    this.contextMenu = null;
    this.type = null;
}
CanvasNode.prototype = {
    consturctor: CanvasNode,
 
    setIcon: function(icon) {
    	 this.icon =icon;
    },
    getIcon:function(){
    	return this.icon;
    },
    setClickAction:function(clickAction){
    	this.clickAction = clickAction;
    },
    getClickAction:function(){
    	return this.clickAction;
    },
    setContextMenu:function(contextMenu){
    	this.contextMenu = contextMenu;
    },
    getContextMenu:function(){
    	return this.contextMenu;
    },
    setType: function(type){
    	this.type = type;
    },
    getType:function(){
    	return this.type;
    },
    
};