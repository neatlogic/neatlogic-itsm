
Flowchart = Class.create();

Flowchart.prototype.initialize=function(conf) {
  this.conf=conf;
  window.flowchart=this;
  
  // initialize variables
  this.objects={};
  this.objectsConnection=[];
  this.editObjectIndex=false;

  this.language();
  this.initKeylistener();
  
  
  Event.observe(window.document, 'click', this.clickListener.bindAsEventListener(this));
  Event.observe(window.document, 'contextmenu', this.customContext.bindAsEventListener(this));
  
  Event.observe(window.document, "dblclick", function (e) {
    var target=(e.target) ? e.target : e.srcElement;
    
    if(target.tagName.toLowerCase()=="body" || target.tagName.toLowerCase()=="html")
      this.addObject(1, {x: Event.pointerX(e), y: Event.pointerY(e)});
  }.bindAsEventListener(this), false);

  Event.observe(window, "load", function () {
    this.loadImages();
    this.loadText();
  }.bind(this), false);
};

Flowchart.prototype.loadText=function() {
  var objs=document.getElementsByClassName('trans');
  objs.each(function(obj, index) {
    obj.parentNode.innerHTML= this.getText(obj.innerHTML) ;
  }.bind(this));
};

Flowchart.prototype.getObjectIndex=function() {
  return 'fj_'+String((new Date()).getTime()).substr(8, 5);
};

Flowchart.prototype.getObjectsLength=function() {
  var counter = 0;
  for(var key in this.objects) {
    counter++;
  }

  return counter;
};

Flowchart.prototype.language=function(lang) {
  this.lang = typeof(this.language[bw.lang]) == 'object' ? bw.lang : 'zh';
};

Flowchart.prototype.loadImages=function() {
  var img=[];
  var imgPath=['arrow_down.gif',
	       'arrow_left.gif',
	       'arrow_right.gif',
	       'arrow_up.gif',
	       'corners.gif',
	       'corners_on.gif',
	       'mouse_connect.gif',
	       'mouse_connect_on.gif']
  
  imgPath.each(function(path) {
    img.push(new Image());
    img.last().src="/balantflow/resources/images/flowchart/"+path;
  });
};



Flowchart.prototype.customContext=function(e) {
  var target=(e.target) ? e.target : e.srcElement;
  
  if(target.tagName.toLowerCase()==(bw.ie ? 'body' : 'html')) {    
    // show flowchart menu
    this.showContextMenu(false, e);
  }
  
  e.cancelBubble=true;
  e.returnValue=false;
};



Flowchart.prototype.clickListener=function(e) {
  var target=(e.target) ? e.target : e.srcElement;
  
  // nothing clicked
  if(target.tagName.toLowerCase()==(bw.ie ? 'body' : 'html')) {
    this.setMouseMode();
    
    // unmark all flowchart objects
    if(!e.ctrlKey)
      this.unmarkAllObjects();
    
    // hide flowchart menu
    this.hideContextMenu();
    this.disableEdit();
  }
  else if((target.index || target.index===0) && this.mouseMode=="connect") {
    this.setMouseMode("connect");
  }
};



Flowchart.prototype.initKeylistener=function() {
  // new keylistener object
  this.keyListener = new Key.listener();
  
  // moving objects with up/down/left/right
  this.keyListener.add('down', 37, function(e, direction) { 
    this.moveObjectByKey(e, 'left'); 
  }.bindAsEventListener(this));

  this.keyListener.add('down', 39, function(e, direction) { 
    this.moveObjectByKey(e, 'right'); 
  }.bindAsEventListener(this));

  this.keyListener.add('down', 38, function(e, direction) {
    this.moveObjectByKey(e, 'up');
  }.bindAsEventListener(this));
  
  this.keyListener.add('down', 40, function(e, direction) { 
    this.moveObjectByKey(e, 'down');
  }.bindAsEventListener(this));
  
  // delete object
  this.keyListener.add('down', 46, this.removeMarkedObjects.bind(this));
};


Flowchart.prototype.addObject=function(type, pos) {
  var index=this.getObjectIndex();
  
  if($("grid").checked) {
    pos=this.getSnapPos(pos);
  }
  else if(!pos) {
    pos=this.getFlowchartObjectStartPosition();
  }
  
  // create new layer object
  this.objects[index]=new Layer.object(index, {x:pos.x, y:pos.y, width:160, height:70, vis:1});
  
  if(bw.ie)
  this.objects[index].element.style.overflow="hidden";
  
  // save index
  this.objects[index].index=index;
  
  this.objects[index].write('<div class="top_left" id="flowchart_top_left_'+index+'"></div>'+
			    '<div class="top_right" id="flowchart_top_right_'+index+'"></div>'+
			    '<div class="inside" id="flowchart_inside_'+index+'"></div>'+
			    '<div class="resize" id="flowchart_resize_'+index+'"></div>'+
			    '<div class="bottom_left" id="flowchart_bottom_left_'+index+'"></div>'+
			    '<div class="bottom_right" id="flowchart_bottom_right_'+index+'"></div>');
  
  
  var resizeDiv=$("flowchart_resize_"+index);
  resizeDiv.index=index;

  Event.observe(resizeDiv, "mousedown", this.enableResize.bindAsEventListener(this), false);

  
  // add write method
  this.objects[index].write=function(content){this.element.childNodes[2].innerHTML=content};
  
  // add mark method
  this.objects[index].setStyle=function(mode) {
    var inside=$('flowchart_inside_'+this.index);
    var top_left=$('flowchart_top_left_'+this.index);
    var top_right=$('flowchart_top_right_'+this.index);	
    var bottom_left=$('flowchart_bottom_left_'+this.index);
    var bottom_right=$('flowchart_bottom_right_'+this.index);
    
    if(mode=='marked' && inside.className!='inside_on') {
      inside.className='inside_on';
      top_left.className='top_left_on';
      top_right.className='top_right_on';
      bottom_left.className='bottom_left_on';
      bottom_right.className='bottom_right_on';
    }
    else if(mode!='marked' && inside.className=='inside_on') {
      inside.className='inside';
      top_left.className='top_left';
      top_right.className='top_right';
      bottom_left.className='bottom_left';
      bottom_right.className='bottom_right';
    }
  };
  
  var inside=$('flowchart_inside_'+index);
  inside.index=index;
  
  Event.observe(inside, "dblclick", function(e){
    var target=(e.target) ? e.target : e.srcElement;    
    if(target.index || target.index===0)
      this.enableEdit(e, target.index);  
  }.bindAsEventListener(this), false);
  
  // make object draggable
  this.setDraggable(index, true);
  return false;
};


Flowchart.prototype.enableResize=function(e) {  
  var target=e.target ? e.target : e.srcElement;
  
  this.disableEdit();

  // set dragging off
  this.setDraggable(target.index, false);

  this.resize=[];
  this.resize.index=target.index;
  this.resize.startPosition={x: Event.pointerX(e), y: Event.pointerY(e)};
  this.resize.startSize={width: this.objects[target.index].width, height: this.objects[target.index].height}
  
  this.mouseMoveListenerFnc = this.mouseMoveListenerFnc ? this.mouseMoveListenerFnc : this.mouseMoveListener.bindAsEventListener(this);
  this.mouseUpListenerFnc = this.mouseUpListenerFnc ? this.mouseUpListenerFnc : this.disableResize.bindAsEventListener(this);

  Event.observe(window.document, "mouseup", this.mouseUpListenerFnc, false);
  Event.observe(window.document, "mousemove", this.mouseMoveListenerFnc, false);
};


Flowchart.prototype.disableResize=function(e) {
  this.setDraggable(this.resize.index, true);
  Event.stopObserving(window.document, 'mousemove', this.mouseMoveListenerFnc, false);
  Event.stopObserving(window.document, "mouseup", this.mouseUpListenerFnc, false);
}

Flowchart.prototype.mouseMoveListener=function(e) {
  var width=Math.round(this.resize.startSize.width+Event.pointerX(e)-this.resize.startPosition.x);
  var height=Math.round(this.resize.startSize.height+Event.pointerY(e)-this.resize.startPosition.y);
  
  if(height < 40)
  height=40;
  else if(height > 400)
  height=400;
  
  if(width < 50)
  width=50;
  else if(width > 600)
  width=600;
  
  // resize object
  this.objects[this.resize.index].resize(width, height);
  this.objects[this.resize.index].element.childNodes[2].style.height=(height-20)+'px';
  
  // adjust connected objects
  this.connectAllObjects();
  return false;
};


Flowchart.prototype.setDraggable=function(index, mode) {
  if(!mode) {
    this.objects[index].draggable();
  }
  else {
    // set draggable
    this.objects[index].draggable(true, true, {down:function(obj, x, y, e) {
      if(!e)
      e=window.event;
      
      if(!this.checkDragTarget(e)) {
	if((e.button ? e.button : e.which)==2)
	  this.showContextMenu(obj, e);
	else
	  {
	    this.startDrag(e, obj);
	    this.hideContextMenu();
	  }
      }

      return false;
    }.bind(this), move: function(obj, x, y, e) {
      if(!e)
      e=window.event;
      
      if(!this.checkDragTarget(e))
      this.drag(obj);
      
      return false;
    }.bind(this), up:function(obj) {
      return this.endDrag(obj);
    }.bind(this)});
  }
};



Flowchart.prototype.createContextMenu=function() {
  this.contextMenu=new Layer.object('flowchart_context_menu', {x: -100, y:-100, width:120, height:80, zindex: 100});
  this.contextMenu.setOpacity(95);
}


Flowchart.prototype.hideContextMenu=function() {
  if(!this.contextMenu) {
    this.createContextMenu();
  }
  
  this.contextMenu.show();
  this.contextMenu.write('');
  this.contextMenu.move(-100, -100);
}


Flowchart.prototype.showContextMenu=function(obj, e) {     
  var html='';
  var markedObjects=this.getMarkedObjects();
  
  if(!this.contextMenu) {
    this.createContextMenu();
  }
  
  // 2 objects are connected
  if(markedObjects.length==2) {
    if(this.checkConnection(markedObjects[0], markedObjects[1])) {
      html+='<li onclick="return flowchart.command(event, \'unconnect\', \''+obj.index+'\', this)">'+this.getText('c9')+'</li>';
      this.contextMenu.resize(160, 80);				
    }
    else
      html+='<li onclick="return flowchart.command(event, \'connect\', \''+obj.index+'\', this)">'+this.getText('c8')+'</li>';
    
    html+='<li onclick="return flowchart.command(event, \'remove\', \''+obj.index+'\', this)">'+this.getText('c4')+'</li>';
  }
  
  // more than 2 connected
  else if(markedObjects.length>2) {
    html+='<li onclick="return flowchart.command(event, \'remove\', \''+obj.index+'\', this)">'+this.getText('c4')+'</li>';
    /* html+='<li onclick="return flowchart.command(event, \'color\', \''+obj.index+'\', this)">'+this.getText('c7')+'</li>'; */
  }
  
  // clicked on arrows
  else if(obj && obj.id && (String(obj.id).indexOf("snap")!=-1 || String(obj.id).indexOf("arrow")!=-1)) {	
    html+='<li onclick="return flowchart.command(event, \'changeArrow\', \''+obj.index+'\', this)">'+this.getText('c6')+'</li>';
    html+='<li onclick="return flowchart.command(event, \'drag_arrow\', \''+obj.index+'\', this)">'+this.getText('c5')+'</li>';

    this.contextMenu.resize(160, 80);
    }
  
  // 1 object selected
  else if(obj) {
    html+='<li onclick="return flowchart.command(event, \'remove\', \''+obj.index+'\', this)">'+this.getText('c4')+'</li>';
    html+='<li onclick="return flowchart.command(event, \'connect\', \''+obj.index+'\', this)">'+this.getText('c8')+'</li>';
    /* html+='<li onclick="return flowchart.command(event, \'edit\', \''+obj.index+'\', this)">'+this.getText('c3')+'</li>'; */
    /* html+='<li onclick="return flowchart.command(event, \'color\', \''+obj.index+'\', this)">'+this.getText('c7')+'</li>'; */
  }
  
  // nothing selected
  else {
    html+='<li onclick="return flowchart.command(event, \'add\', \''+obj.index+'\', this)">'+this.getText('c10')+'</li>';
  }
  
  this.contextMenu.write('<ul>'+html+'</ul>');
  this.contextMenu.move(Event.pointerX(e),   Event.pointerY(e)); 
  this.contextMenu.show(true);

  this.checkClickedObject(e, obj.index);
};


Flowchart.prototype.checkClickedObject=function(e, index) {
  var markedObjects = this.getMarkedObjects();
  
  // if no object is marked then mark the just clickt object
  if(markedObjects.length==0 && this.objects[index]) {
    this.markObject(e, this.objects[index]);
  }
  else if(markedObjects.length==1 && markedObjects[0] != this.objects[index]) {
    this.unmarkAllObjects();
    this.markObject(e, this.objects[index]);
  }
}



Flowchart.prototype.command=function(e, action, index, obj) {
  if(!e)
	  e=window.event;
  
  if(obj)
	  obj.blur();
  if(action=='remove')
	  this.removeMarkedObjects();
  else if(action=='edit')
	  this.enableEdit(e, index);
  else if(action=='add') {
    var pos=false;
    if(this.contextMenu && this.contextMenu.visible)
    pos={x: this.contextMenu.x, y: this.contextMenu.y};
    
    this.addObject(1, pos);
  }
  else if(action=='connect')
	  this.connect();
  else if(action=='unconnect')
	  this.unconnectMarkedObjects();
  else if(action=='grid')
	  this.setGrid();
  else if(action=='changeArrow')
	  this.swapSnappoint(index);
  else if(action=='export')
	  this.exportAll();
  else if(action=='save')
	  this.saveContent();
  else if(action=='build'){
	  var pos=false;
	    if(this.contextMenu && this.contextMenu.visible)
	    pos={x: this.contextMenu.x, y: this.contextMenu.y};
	    this.buildObject(1, pos , obj);
  }
  this.hideContextMenu();
  return false;
};

Flowchart.prototype.buildObject=function(type, pos , obj) {
	  var statusId = obj.attributes.getNamedItem("statusId").nodeValue ; 
	  var statusName = obj.attributes.getNamedItem("statusName").nodeValue; 

	  // var index=this.getObjectIndex();
	  var index = statusId ; 
	  if(this.objects[index]){
		  alert('object status is exits!');
		  return false;
	  }
	  
	  if($("grid").checked) {
	    pos=this.getSnapPos(pos);
	  }
	  else if(!pos) {
	    pos=this.getFlowchartObjectStartPosition();
	  }
	  
	  // create new layer object
	  this.objects[index]=new Layer.object(index, {x:pos.x, y:pos.y, width:160, height:70, vis:1});
	  
	  if(bw.ie)
	  this.objects[index].element.style.overflow="hidden";
	  
	  // save index
	  this.objects[index].index=index;
	  
	  this.objects[index].write('<div class="top_left" id="flowchart_top_left_'+index+'"></div>'+
				    '<div class="top_right" id="flowchart_top_right_'+index+'"></div>'+
				    '<div class="inside buildInside" id="flowchart_inside_'+index+'">' + statusName +"/"+ index +  '</div>'+
				    '<div class="resize" id="flowchart_resize_'+index+'"></div>'+
				    '<div class="bottom_left" id="flowchart_bottom_left_'+index+'"></div>'+
				    '<div class="bottom_right" id="flowchart_bottom_right_'+index+'"></div>');
	  
	  
	  var resizeDiv=$("flowchart_resize_"+index);
	  resizeDiv.index=index;

	  Event.observe(resizeDiv, "mousedown", this.enableResize.bindAsEventListener(this), false);

	  
	  // add write method
	  this.objects[index].write=function(content){this.element.childNodes[2].innerHTML=content};
	  
	  // add mark method
	  this.objects[index].setStyle=function(mode) {
	    var inside=$('flowchart_inside_'+this.index);
	    var top_left=$('flowchart_top_left_'+this.index);
	    var top_right=$('flowchart_top_right_'+this.index);	
	    var bottom_left=$('flowchart_bottom_left_'+this.index);
	    var bottom_right=$('flowchart_bottom_right_'+this.index);
	    
	    if(mode=='marked' && inside.className!='inside_on') {
	      inside.className='inside_on';
	      top_left.className='top_left_on';
	      top_right.className='top_right_on';
	      bottom_left.className='bottom_left_on';
	      bottom_right.className='bottom_right_on';
	    }
	    else if(mode!='marked' && inside.className=='inside_on') {
	      inside.className='inside';
	      top_left.className='top_left';
	      top_right.className='top_right';
	      bottom_left.className='bottom_left';
	      bottom_right.className='bottom_right';
	    }
	  };
	  
	  var inside=$('flowchart_inside_'+index);
	  inside.index=index;
	  
	  Event.observe(inside, "dblclick", function(e){
	    var target=(e.target) ? e.target : e.srcElement;    
	    if(target.index || target.index===0)
	      this.buildEdit(e, target.index);  
	  }.bindAsEventListener(this), false);
	  
	  // make object draggable
	  this.setDraggable(index, true);
	  return false;
}

// export
Flowchart.prototype.exportAll=function() {
  for(var key in this.objects) {
	  console.log(key +"\t" + JSON.stringify(this.exportObject(this.objects[key])));
  };
};


Flowchart.prototype.exportObject=function(object) {
	var direction = new Array();
	var count = 0 ; 
	this.arrowParts.each(function(part, index) {
		    direction[count] = (part[3].x > part[4].x && part.arrowDirection == 1) || (part[3].x < part[4].x && part.arrowDirection == 2) ? 'right' : 'left';
		    count ++ ;
	}.bind(object));
	 
  return {width:object.width, height:object.height, x:object.x, y:object.y, id: object.id, connections: this.exportObjectConnections(object), direct : direction }; 
};

Flowchart.prototype.exportObjectConnections=function(object) {
  var objects = [];
  this.objectsConnection.each(function(connection) {
    if(connection.objectA == object)
      objects.push(connection.objectB.id);
    else if(connection.objectB == object)
      objects.push(connection.objectA.id);
  }.bind(this));
  return objects;
};

Flowchart.prototype.saveContent = function() {
	var json = {} , html = "" , exp = {} , temp = "";
	var obj = document.getElementsByTagName("div");
	for ( var i = 0; i < obj.length; i++) {
		if (obj[i].className == 'abc') {
			var getObj = obj[i];
			html  =  html + getObj.innerHTML;
		}
	}
	for(var key in this.objects) {
		temp =  JSON.stringify(this.exportObject(this.objects[key])) ;
		console.info("fuck : " + temp);
	};
	
	exp[key] = temp ; 
	json["exp"] = exp ;
	json["html"] = html ;
	
	if(html){
		try {
			if (confirm('是否需要保存?')) {
				var url = "insertFlow.do?json=" + JSON.stringify(json);
				new Ajax.Request(url, {
					method : 'post',
					asynchronous : 'false',
					onComplete:function(response){
					var msg = eval('('+ response.responseText + ')');
					if(msg.status == 'ok'){
						alert('保存成功！');
					}else{
						alert('操作异常！');
					}
				 }
			});
		  }
		} catch (e) {
			alert('操作异常！');
		}
	}else{
		alert('请先定义流程图！');
	}
};


Flowchart.prototype.checkDragTarget=function(e) {
  var target=(e.target) ? e.target : e.srcElement;   
  return (target && target.getAttribute('ignore')) ? true : false;
};


Flowchart.prototype.buildEdit=function(e, index) {
	  this.disableEdit();
	  this.unmarkAllObjects();
	  this.markObject(e, this.objects[index]);
	 
	  var resizeDiv=$('flowchart_resize_'+index);
	  
	  this.enableResizeFnc = this.enableResizeFnc ? this.enableResizeFnc : this.enableResize.bindAsEventListener(this);

	  if(this.editObjectIndex!==index) {// build method can edit?
// this.editObjectIndex=index;
// var value=this.objects[index].element.childNodes[2].innerHTML;
// this.objects[index].write('<textarea ignore="true" id="edit_'+index+'"
// class="inputarea">'+this.parseText(value)+'</textarea>');
//	    
// var textarea=$('edit_'+index);
// textarea.style.width=(this.objects[index].element.offsetWidth-22)+'px';
// textarea.style.height=(this.objects[index].element.offsetHeight-22)+'px';
// textarea.focus();
//
// setTimeout(function() {
// $('edit_'+this.editObjectIndex).focus();
// }.bind(this), 100);
	    
	    resizeDiv.style.cursor='default';
	    Event.stopObserving(resizeDiv, 'mousedown', this.enableResizeFnc, false);
	  }
};


Flowchart.prototype.enableEdit=function(e, index) {
  this.disableEdit();
  this.unmarkAllObjects();
  this.markObject(e, this.objects[index]);
 
  var resizeDiv=$('flowchart_resize_'+index);
  
  this.enableResizeFnc = this.enableResizeFnc ? this.enableResizeFnc : this.enableResize.bindAsEventListener(this);

  if(this.editObjectIndex!==index) {// textarea can edit view value
    this.editObjectIndex=index;
    var value=this.objects[index].element.childNodes[2].innerHTML;
    this.objects[index].write('<textarea ignore="true" id="edit_'+index+'" class="inputarea">'+this.parseText(value)+'</textarea>');
    
    var textarea=$('edit_'+index);
    textarea.style.width=(this.objects[index].element.offsetWidth-22)+'px';
    textarea.style.height=(this.objects[index].element.offsetHeight-22)+'px';
    textarea.focus();

    setTimeout(function() {
      $('edit_'+this.editObjectIndex).focus();
    }.bind(this), 100);
    
    resizeDiv.style.cursor='default';
    Event.stopObserving(resizeDiv, 'mousedown', this.enableResizeFnc, false);
  }
};


Flowchart.prototype.disableEdit=function(index) {
  if(this.editObjectIndex!==false) {
    var editObject = $('edit_'+this.editObjectIndex);
    var resizeDiv  = $('flowchart_resize_'+this.editObjectIndex);
    
    if(editObject) {
      this.objects[this.editObjectIndex].write(this.parseText(editObject.value, true));
      
      // is draggable again
      this.setDraggable(this.editObjectIndex, true);
      
      Event.observe(resizeDiv, 'mousedown', this.enableResizeFnc, false);
      resizeDiv.style.cursor='se-resize';

      this.editObjectIndex=false;
    }
  }
}


Flowchart.prototype.parseText=function(str, mode) {
  return (mode) ? str.replace(/\n/gi, '<br>') : str.replace(/<br>/gi, '\n');
};


Flowchart.prototype.removeMarkedObjects=function() {
  var markedObjects=this.getMarkedObjects();
  var ask;
  
  if(markedObjects.length==1)
  this.removeObject(markedObjects[0].index);
  else {  
    ask=confirm(this.getText('c1')+' '+markedObjects.length+' '+this.getText('c2'));
    
    if(ask) {
      markedObjects.each(function(object) {
	this.removeObject(object.index);
      }.bind(this));
    }
  }
}


Flowchart.prototype.removeObject=function(index) {
  this.unmarkAllObjects();
  this.objects[index].show(0);
  
  // loop connected objects
  this.objectsConnection.each(function(object, counter) {
    if(object.objectA==this.objects[index] || object.objectB==this.objects[index]) {
      this.removeConnectionByIndex(counter);
    }
  }.bind(this));
};


Flowchart.prototype.createConnectionObjects=function(markedObjects) {
  if(!this.arrowParts)
  this.arrowParts=[];

  var index=this.arrowParts.length;
  
  // new array
  this.arrowParts[index]=[];
  this.arrowParts[index][0]=new Layer.object('arrow_part_0_'+index, {x:0, y:0, width:9, height:1});
  this.arrowParts[index][0].write("<div style='position:absolute;left:4px;width:1px;height:100%;background-color:black'></div>");
  this.arrowParts[index][0].element.style.overflow='hidden';
  this.arrowParts[index][0].element.style.cursor='w-resize';
  
  this.arrowParts[index][0].draggable(true, true, {down: function(obj) {
    this.startMoveLine(obj); return false; }.bind(this), move: function(obj)  { 
      this.moveLine(obj); return false;
    }.bind(this)});
  
  this.arrowParts[index][1]=new Layer.object('arrow_part_1_'+index, {x:0, y:0, width:1, height:1});
  this.arrowParts[index][1].setBgColor('black');
  
  this.arrowParts[index][2]=new Layer.object('arrow_part_2_'+index, {x:0, y:0, width:1, height:1});
  this.arrowParts[index][2].setBgColor('black');
 
  this.arrowParts[index][3]=new Layer.object('snap_1_'+index, {x:0, y:0, width:13, height:13, parent:this.arrowParts[index][1].element});
  this.arrowParts[index][3].write('<img src="/balantflow/resources/images/flowchart/0.gif" id="arrow_3_'+index+'" />');
  this.arrowParts[index][3].element.style.overflow='hidden';
  this.arrowParts[index][3].element.index=index;
  this.arrowParts[index][3].element.snap_index=1;


  Event.observe(this.arrowParts[index][3].element, 'mousedown', function(e) {
    if((e.button ? e.button : e.which) == 2)
      this.showContextMenu(e.target ? e.target : e.srcElement, e);
  }.bindAsEventListener(this), false);
  

  Event.observe(this.arrowParts[index][3].element, 'dblclick', function(e) {
    var target=e.target ? e.target : e.srcElement;
    
    if(!target.index && target.index!==0)
      target=target.parentNode;    
    
    this.swapSnappoint(target.index);
  }.bindAsEventListener(this), false);

  
  $('arrow_3_'+index).index=index;

  this.arrowParts[index][4]=new Layer.object('snap_2_'+index, {x:0, y:0, width:13, height:13, parent:this.arrowParts[index][2].element});
  this.arrowParts[index][4].write('<img src="/balantflow/resources/images/flowchart/0.gif" id="arrow_4_'+index+'" />');
  this.arrowParts[index][4].element.style.overflow='hidden';
  this.arrowParts[index][4].element.index=index;
  this.arrowParts[index][4].element.snap_index=2;

  Event.observe(this.arrowParts[index][4].element, 'mousedown', function(e) {    
    if((e.button ? e.button : e.which) == 2){
    	this.showContextMenu(e.target ? e.target : e.srcElement, e);
    }
  }.bindAsEventListener(this), false);
  
  Event.observe(this.arrowParts[index][4].element, 'dblclick', function(e) {
    var target=e.target ? e.target : e.srcElement;
    if(!target.index && target.index!==0)
      target=target.parentNode;    
    this.swapSnappoint(target.index);
  }.bindAsEventListener(this), false);
  

  $('arrow_4_'+index).index=index;
  
  this.swapSnappoint(index);
  
  // save connected objects
  this.objectsConnection.push({objectB: markedObjects[0], objectA: markedObjects[1], arrow: this.arrowParts[index]});

  // save twin elementents to draggable line
  this.arrowParts[index][0].lines=[this.arrowParts[index][1], this.arrowParts[index][2]];
};



Flowchart.prototype.checkConnection=function(objectsA, objectsB) {
  var foundObject = false;
  
  this.objectsConnection.each(function(object) {
    if(object) {
      if((objectsA==object.objectA && objectsB==object.objectB) || (objectsA==object.objectB && objectsB==object.objectA)) {
	foundObject = true;
	throw $break;
      }
    }
  }.bind(this));
  
  return foundObject;
};



Flowchart.prototype.unconnectMarkedObjects=function() {
  var markedObjects=this.getMarkedObjects();
  
  if(markedObjects.length==2) {
    this.objectsConnection.each(function(object, index) {
      if(this.checkConnection(markedObjects[0], markedObjects[1])) {
	this.removeConnectionByIndex(index);
	throw $break;
      }
    }.bind(this));
  }
};


// to remove empty layers
Flowchart.prototype.removeConnectionByIndex=function(index) {
  if(this.objectsConnection[index] && this.objectsConnection[index].arrow) {
    (5).times(function(i) {
      this.objectsConnection[index].arrow[i].show();
    }.bind(this));
    
    if(!this.garbageCollection)
    this.garbageCollection=[];
    
    if(this.garbageCollection.indexOf(index)<0) {
      this.garbageCollection.push(index);
    }
  }

  clearTimeout(this.clearGarbageTimeout);
  
  this.clearGarbageTimeout = setTimeout(function() {
    this.clearGarbage();
  }.bind(this), 100);
};


// remove unused layers
Flowchart.prototype.clearGarbage=function() {
  if(this.garbageCollection) {
    var newConnectedObjects = [];

    for(var i=0; i<this.garbageCollection.length; i++) {
      if(this.objectsConnection[this.garbageCollection[i]]) {
	for(var key in this.objectsConnection[this.garbageCollection[i]])
	  delete this.objectsConnection[this.garbageCollection[i]][key];
	
	delete this.objectsConnection[this.garbageCollection[i]];
      }
    }
    
    this.objectsConnection.each(function(connection, index) {
      if(this.objectsConnection[index]) {
	newConnectedObjects.push(this.objectsConnection[index]);
      }
    }.bind(this));
    
    delete this.objectsConnection;
    this.objectsConnection = newConnectedObjects;
  }
};



Flowchart.prototype.startDrag=function(e, obj) {
  if(e.ctrlKey && obj.marked) {
    // ctrl key pressed, click on a marked object -> unmark object
    this.unmarkObject(obj);
  }
  else if((e.ctrlKey && !obj.marked) || this.mouseMode=='connect') {
    // ctrl key pressed, click on a not marked object -> mark object
    this.markObject(e, obj);
  }
  else if(!e.ctrlKey) {
    var markedObjects = this.getMarkedObjects()
    if(markedObjects.length != 1 || markedObjects[0] != obj) {
      this.unmarkAllObjects();
    }
    this.markObject(e, obj);
  }
  
  var markedObjects=this.getMarkedObjects();
  this.startPosition={x:obj.x, y:obj.y};
  
  if(this.mouseMode=='connect') {
    if(markedObjects.length==2)
      this.connect();
    
    if(markedObjects.length>=2) {
      this.setMouseMode();
      this.unmarkAllObjects();
    }
  }
};


Flowchart.prototype.drag=function(dragObject) {
  dragObject.setOpacity(80);
  dragObject.marked=true;
  dragObject.setStyle('marked');
  
  var x=(dragObject.x-this.startPosition.x);
  var y=(dragObject.y-this.startPosition.y);
  
  var markedObjects=this.getMarkedObjects();
  
  // move all connected objects
  markedObjects.each(function(markedObject) {
    if(dragObject!=markedObject)
      markedObject.move(markedObject.startPosition.x+x, markedObject.startPosition.y+y);
  }.bind(this));
  
  this.connectAllObjects();
};


Flowchart.prototype.endDrag=function(obj) {
  if($("grid").checked) {
    this.snapToGrid(obj);

    // ajust connected objects
    this.connectAllObjects();
  }

  return obj.setOpacity(100);
};

Flowchart.prototype.snapAllToGrid=function() {
  for(var key in this.objects) {
    this.snapToGrid(this.objects[key]);
  }
  
  // ajust connected objects
  this.connectAllObjects();
};


Flowchart.prototype.snapToGrid=function(obj) {
  var pos = this.getSnapPos({x: obj.x, y:obj.y});
  return obj.move(pos.x, pos.y);
};

Flowchart.prototype.getSnapPos=function(pos) {
  for(var x=i=0; true; i++) {
    x = i*30;
    if(x >= pos.x-15) {
      break;
    }
  }
  
  for(var y=i=0; true; i++) {
    y = i*30;
    if(y >= pos.y-15) {
      break;
    }
  }
  
  return {x: x, y: y};
}

Flowchart.prototype.getFlowchartObjectStartPosition=function() {
  var pos={x: 10, y: 30};
  
  // loop all objects
  for(var key in this.objects) {
    if(pos.x==this.objects[key].x && pos.y==this.objects[key].y && this.objects[key].visible) {
      pos.x+=10;
      pos.y+=10;
    }
  }
  
  return pos;
};

// connect 2 objects together

Flowchart.prototype.connect=function() {
  markedObjects=this.getMarkedObjects();
  
  if(this.checkConnection(markedObjects[0], markedObjects[1])) {
    alert(this.getText('c11'));
  }
  else {
    if(markedObjects.length!=2) {
      if(this.getObjectsLength()>=2) {
    	  this.setMouseMode("connect");
      }
      else {
    	  alert(this.getText('c12'));
      }
    }
    else {
      this.createConnectionObjects(markedObjects);
    }
    
    this.connectAllObjects();
  }
};


Flowchart.prototype.setMouseMode=function(mode) {
  this.mouseMode=mode;
  
  if(!this.mouseFollow) {
    this.mouseFollow=new Layer.object('mouseFollow', {x:0, y:0, width:16, height:13, zindex: 1000000});
  }
  
  this.followMouseFnc = this.followMouseFnc ? this.followMouseFnc : this.followMouse.bindAsEventListener(this)
  
  if(mode) { 
    var markedObjects=this.getMarkedObjects();
    
    if(markedObjects.length==1) {
      var on=true;
      this.firstMarked=markedObjects[0];
    }
    
    this.mouseFollow.write('<img src="/balantflow/resources/images/flowchart/mouse_'+mode+(on ? '_on' : '')+'.gif" width="16" height="13" id="mouseFollow_img" />');
    Event.observe(window.document, 'mousemove', this.followMouseFnc, false);
  }
  else {
    this.mouseFollow.show(0);
    Event.stopObserving(window.document, 'mousemove', this.followMouseFnc, false);
  }
};


Flowchart.prototype.followMouse=function(e) {  
  if(this.mouseMode) {
    var pos={x: Event.pointerX(e), y: Event.pointerY(e)};
    var winHeight=(window.innerHeight) ? window.innerHeight : document.body.offsetHeight;
    
    if(pos.x+10>0 && pos.y-10>0)
    this.mouseFollow.move(pos.x+10, pos.y-10);
    
    this.mouseFollow.show(pos.x <= 11 || pos.y <= 20 || pos.y+15 > winHeight ? 0 : 1);
  }
};




Flowchart.prototype.swapSnappoint=function(index) {
  var markedObjects=this.getMarkedObjects();

  if(!this.arrowParts[index].arrowDirection && this.firstMarked) {
    this.secondMarked=(markedObjects[0].index==this.firstMarked.index) ? markedObjects[1] : markedObjects[0];    
    this.arrowParts[index].arrowDirection=(this.firstMarked.x<this.secondMarked.x) ? 1 : 2;
  }
  else	 
	  this.arrowParts[index].arrowDirection = (this.arrowParts[index].arrowDirection==1) ? 2 : 1;

  this.setArrowDirections();	 
};


Flowchart.prototype.setArrowDirections=function() {
  // set arrows
  this.arrowParts.each(function(part, index) {
    var direction = (part[3].x > part[4].x && part.arrowDirection == 1) || (part[3].x < part[4].x && part.arrowDirection == 2) ? 'right' : 'left';
    
    if(part.arrowDirection == 1) {
      $('arrow_3_'+index).src='/balantflow/resources/images/flowchart/arrow_'+direction+'.gif';
      $('arrow_4_'+index).src='/balantflow/resources/images/flowchart/0.gif';
    }
    else {
      $('arrow_3_'+index).src='/balantflow/resources/images/flowchart/0.gif';
      $('arrow_4_'+index).src='/balantflow/resources/images/flowchart/arrow_'+direction+'.gif';
    }
  }.bind(this));
};


Flowchart.prototype.startMoveLine=function(obj) {
  obj.startPosition={y: obj.y, x: obj.x};
  obj.lines[0].startSize={width: obj.lines[0].width, height: obj.lines[0].height};
  obj.lines[1].startSize={width: obj.lines[1].width, height: obj.lines[1].height};
  obj.lines[0].startPosition={x: obj.lines[0].x, y: obj.lines[0].y};
  obj.lines[1].startPosition={x: obj.lines[1].x, y: obj.lines[1].y};
};


Flowchart.prototype.moveLine=function(obj) {
  if(obj.lines[0].startSize.width-(obj.startPosition.x-obj.x) <= 5 || obj.lines[0].startSize.width+(obj.startPosition.x-obj.x) <= 5) {
    obj.move(obj.lastPos, obj.startPosition.y);
    return false;
  }
  
  obj.move(obj.x, obj.startPosition.y);
  obj.lastPosX=obj.x;
  
  if(obj.lines[1].startPosition.x < obj.lines[0].startPosition.x) {
    obj.lines[0].resize(obj.lines[0].startSize.width+(obj.startPosition.x-obj.x), 1);
    obj.lines[0].move(obj.lines[0].startPosition.x-(obj.startPosition.x-obj.x), obj.lines[0].y);
    obj.lines[1].resize(obj.lines[1].startSize.width-(obj.startPosition.x-obj.x), 1);
  }
  else {
    obj.lines[0].resize(obj.lines[0].startSize.width-(obj.startPosition.x-obj.x), 1);
    obj.lines[1].resize(obj.lines[1].startSize.width+(obj.startPosition.x-obj.x), 1);
    obj.lines[1].move(obj.lines[1].startPosition.x-(obj.startPosition.x-obj.x), obj.lines[1].y);
  }
};



Flowchart.prototype.moveObjectByKey=function(e, direction) {
  if(this.editObjectIndex!==false)
  return false;
  
  var markedObjects=this.getMarkedObjects();
  var speed=(e.ctrlKey) ? 10 : 1;

  // move all connected objects
  if(direction=='left') {
    markedObjects.each(function(markedObject) {
      markedObject.shift(-speed,0);
    }.bind(this));
  }
  else if(direction=='right') {
    markedObjects.each(function(markedObject) {
      markedObject.shift(speed,0);
    }.bind(this));
  }
  else if(direction=='up') {
    markedObjects.each(function(markedObject) {
      markedObject.shift(0,-speed);
    }.bind(this));
  }
  else if(direction=='down') {
    markedObjects.each(function(markedObject) {
      markedObject.shift(0,speed);
    }.bind(this));
  }
  
  // ajust connected objects
  this.connectAllObjects();
  
  return false;
};



Flowchart.prototype.connectAllObjects=function() {
  this.objectsConnection.each(function(object) {
    this.connectObject(object);
  }.bind(this));
};


Flowchart.prototype.connectObject=function(obj) {
  var width=0;
  var height=0;
  var x_0=0;
  var y_0=0;
  var x_1=0;
  var y_1=0;
  var x_2=0;
  var y_2=0;
  var diff=2;
  
  if(obj.objectA.x>obj.objectB.x) {
    width=obj.objectA.x-(obj.objectB.x+obj.objectB.width);
    x_1=obj.objectA.x-Math.floor(width/diff);
    x_2=obj.objectB.x+obj.objectB.width;
    
    y_1=obj.objectA.y+Math.floor(obj.objectA.height/2);
    y_2=obj.objectB.y+Math.floor(obj.objectB.height/2);
    
    obj.arrow[3].move(Math.floor(width/diff)-12, -6);
    obj.arrow[3].show(1);
    
    obj.arrow[4].move(0, -6);
    obj.arrow[4].show(1);
  }
  else {	
    width=obj.objectB.x-(obj.objectA.x+obj.objectA.width);
    x_2=obj.objectB.x-Math.floor(width/diff);
    x_1=obj.objectA.x+obj.objectA.width;
    
    y_1=obj.objectA.y+Math.floor(obj.objectA.height/2);
    y_2=obj.objectB.y+Math.floor(obj.objectB.height/2);
    
    obj.arrow[3].move(0, -6);
    obj.arrow[3].show(1);
    
    obj.arrow[4].move(Math.floor(width/diff)-12, -6);
    obj.arrow[4].show(1);
  }

  x_0= (x_2>x_1 ? x_1 : x_2)+Math.floor(width/diff)-4;
  y_0= y_2>y_1 ? y_1 : y_2;
  height=(y_2>y_1 ? y_2-y_1 : y_1-y_2)+1;
  
  obj.arrow[0].resize(9, height);
  obj.arrow[0].move(x_0, y_0);
  obj.arrow[0].show(1);
  
  obj.arrow[1].resize(Math.floor(width/diff), 1);
  obj.arrow[1].move(x_1, y_1);
  obj.arrow[1].show(1);
  
  obj.arrow[2].resize(Math.floor(width/diff), 1);	 
  obj.arrow[2].move(x_2, y_2);
  obj.arrow[2].show(1);
  
  this.setArrowDirections();
};


Flowchart.prototype.markObject=function(e, obj) {
  if(obj) {
    obj.setStyle('marked');
    obj.marked=true;
  }

  return false;
};


Flowchart.prototype.unmarkObject=function(obj) {
  obj.setStyle('unmarked');
  obj.marked=false;
  return false;
};


Flowchart.prototype.unmarkAllObjects=function() {
  for(var key in this.objects) {
    this.unmarkObject(this.objects[key]);
  }
};


Flowchart.prototype.getMarkedObjects=function() {
  var markedObjects=[];

  for(var key in this.objects) {
    if(this.objects[key].marked) {
      this.objects[key].startPosition={x:this.objects[key].x, y:this.objects[key].y};
      markedObjects.push(this.objects[key]);
    }
  }
  
  return markedObjects;
};


Flowchart.prototype.setGrid=function() {
  if($("grid").checked) {
    this.snapAllToGrid();
  }
};


Flowchart.prototype.error=function(error) {
  alert(error);
  return false;
};




