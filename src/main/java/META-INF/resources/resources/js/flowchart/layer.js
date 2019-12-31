var Layer = {
  layerObjects: [], 
  layerDragObject: ''
};

Layer.object = Class.create();

// make keys: x, y, width, height, zindex, vis, parent
Layer.object.prototype.initialize=function(obj, make) {
  if(!window.bw)
  return false;
  
  this.name = obj;
  this.hier = new Array();
  this.keylistener = new Key.listener();
  this.create(make);
  
  if(this.element) {
    // layer object properties
    this.name = this.element.id;
    this.css = this.element.style;
    this.doc = self.document;
    this.clipWidth = this.element.offsetWidth;
    this.clipHeight = this.element.offsetHeight;
    this.width = this.element.offsetWidth;
    this.height = this.element.offsetHeight;
    this.x = this.element.offsetLeft;
    this.y = this.element.offsetTop;
    this.visible = (this.css.visibility=='visible' || this.css.visibility=='show' || this.css.visibility=='inherit') ? true : false;
    this.win = self;
    this.id = Layer.layerObjects.length;
    
    // global layer object
    Layer.layerObjects[this.id] = this;
    
    if(this.css && !this.css.setAttribute) {
      this.css.setAttribute = function(attribute, property) {
	this[attribute] = property;
      }
    }
    
    // set opacity
    if(make.opacity)
      this.setOpacity(make.opacity);
    
    // write
    if(make.html)
      this.write(make.html);
    
    // bgcolor
    if(make.bgcolor)
      this.setBgColor(make.bgcolor);
  }
};
  
  
// create a new layer in the current document
Layer.object.prototype.create = function(arg) {
  var obj = document.createElement('div');

  with(obj) {
    // set properties
    id = this.name;
    
    with(style) {
      position = 'absolute';
      visibility = (arg.vis) ? (arg.vis==2) ? 'inherit' : 'visible' : 'hidden';
      left = (arg.x ? arg.x : 0)+'px';
      top  = (arg.y ? arg.y : 0)+'px';

      if(arg.width)  width  = arg.width+'px';
      if(arg.height) height = arg.height+'px';
      if(arg.zindex) zIndex = arg.zindex;
    }
    
    try{
    	obj.setAttribute("class", "abc");
    }catch(e){
    	obj.className = 'abc'; 
    }
    
  }

  // append object
  ((arg.parent) ? arg.parent : document.body).appendChild(obj);  

  // save object
  this.element = obj;
};


// public layer object methods
// go through the layer hierarchy and return the absolute position
Layer.object.prototype.absolute = function() {
  var elm  = this.element.offsetParent;
  var posX = this.x;
  var posY = this.y;  

  while(elm) {
    posX += elm.offsetLeft;
    posY += elm.offsetTop;
    elm = elm.offsetParent;
  }
  
  return {x:posX, y:posY};
};



// check if x,y is within the layer
Layer.object.prototype.within = function(x, y, absolute) {
  var objX = 0;
  var objY = 0;
  var abs;

  if(absolute) {
    abs = this.absolute();
    objX = abs.x;
    objY = abs.y;
  }
  else {
    objX = this.x;
    objY = this.y;
  }
  
  return (this.checkWithin(x, y, objX, objX+this.width, objY, objY+this.height)) ? true : false;
};


// move the layer to a specific position
Layer.object.prototype.move = function(x, y) {
  this.x = typeof(x) == 'number' ? Math.round(x) : 0;
  this.y = typeof(y) == 'number' ? Math.round(y) : 0;
  
  this.css.setAttribute('left', this.x+'px');
  this.css.setAttribute('top', this.y+'px');
  return false;
};


// move the layer for a specific step
Layer.object.prototype.shift = function(x, y) {
  x = Math.round(x*100)/100;
  y = Math.round(y*100)/100;

  this.move(this.x+x, this.y+y);
  return false;
};


// change the layers width and height
Layer.object.prototype.resize = function(width, height) {
  this.width = typeof(width) == 'number' ? Math.round(width) : 0;
  this.height = typeof(height) == 'number' ? Math.round(height) : 0;

  this.css.setAttribute('width', this.width+'px');
  this.css.setAttribute('height', this.height+'px');
  return false;
};


// cut the layer (top, width, height, left)
Layer.object.prototype.clip = function(t, w, h, l) {
  this.css.setAttribute('clip', 'rect('+t+' '+w+' '+h+' '+l+')');

  this.clipHeight = h;
  this.clipWidth = w;
  return false;
};

// toggle object visibility
Layer.object.prototype.toggle = function() {
  return this.show((this.visible ? 0 : 1));
};

// show or hide the layer
Layer.object.prototype.show = function(mode) {
  if(mode==2) {
    this.css.setAttribute('visibility', 'inherit');
    this.visible = true;
  }
  else if(mode) {
    this.css.setAttribute('visibility', 'visible');
    this.visible = true;
  }
  else {
    this.css.setAttribute('visibility', 'hidden');
    this.visible = false;
  }
  
  return false;
};


// write new content into a layer
Layer.object.prototype.write = function(cont) {
  if(typeof cont == 'string') {
    this.element.innerHTML = cont;
  }
  else if(window.Builder) {
    this.element.appendChild(cont);
  }
  else {
    throw "run the file system is missing!";
  }
  return false;
};


// set the given color to the layer background
Layer.object.prototype.setBgColor = function(c) {
  this.css.setAttribute('backgroundColor', (!c || c=='#') ? 'transparent' : c);
  return false;
};

// set the given image to the layer background
Layer.object.prototype.setBgImage = function(i, r) {
  if(!r) this.css.setAttribute('backgroundRepeat', 'no-repeat');
  this.css.setAttribute('backgroundImage', 'url('+i+')');
  return false;
};


// set the opacity of a layer to the given ammount (in %)
Layer.object.prototype.setOpacity = function(v) {  
  var op = v<=1 ? Math.round(v*100) : parseInt(v);
  
  if(bw.ie) {
    this.css.setAttribute('filter', 'alpha(opacity:'+op+')');
  }
  else if(bw.safari) {
    this.css.setAttribute('opacity', op/100);
    this.css.setAttribute('KhtmlOpacity', op/100);
  }
  else if(bw.mz) {
    this.css.setAttribute('MozOpacity', op/100);
  }
  return false;
};


// fade a layer in or out (if browser supports opacity)
Layer.object.prototype.fade = function(time) {
  if(!time) {
    return this.toggle();
  }

  var steps = time/20;
  var oStep = 0;
  var tStep = time/steps;
  
  if(this.visible) {
    // fade layer out
    oStep = 1/steps;

    steps.times(function(n) {
      setTimeout(function() {
	this.setOpacity(1-(n*oStep));
      }.bind(this), Math.round(n*tStep));
    }.bind(this));
    
    setTimeout(function() {
      this.show(0);
      this.setOpacity(1);
    }.bind(this), time);
  }
  else {
    // fade layer in 
    oStep = 0.98/steps;
    
    this.show(1);
    this.setOpacity(oStep);
    
    steps.times(function(n) {
      setTimeout(function() {
	this.setOpacity(n*oStep);
      }.bind(this), Math.round(n*tStep));
    }.bind(this));
  }

  return false;
};


// move the layer to a specific position with a linear animation
Layer.object.prototype.slide = function(endX, endY, step, speed, action) {
  step  = (!step)  ? 10 : step;
  speed = (!speed) ? 20 : speed;
  
  var distX = endX-this.x;
  var distY = endY-this.y;
  var num   = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2))/step;
  var incX  = distX/num;
  var incY  = distY/num;
  
  if(num==0) { 
    if(action) {
      action.apply(null, {x: this.x, y: this.y});
    }
  }
  else {    
    this.slideActive = true;
    this.slideInt = 1;
    
    window.clearInterval(this.intvl);
    
    this.intvl = setInterval(function() {
      this.doSlide(incX, incY, endX, endY, num, action);
    }.bind(this), speed);
  }
  
  return false;
};

// sliding animation step (animation frame)
Layer.object.prototype.doSlide = function(incX, incY, endX, endY, num, action) {
  if(this.slideActive) {
    if(this.slideInt++ < num) {
      this.shift(incX, incY);
    }
    else {
      window.clearInterval(this.intvl);
      this.slideActive = false;
      this.move(endX, endY);
      
      if(action) {
	action.apply(null, {x:incX, y:incY});
      }
    }
  }
  
  return false;
};


// move the layer to a specific position with an accelerated animation
// step: 5 - 10 (degrees)
// speedStart/speedEnd: 'fast' or 'slow'
Layer.object.prototype.glide = function(endX, endY, step, speed, action, stepAction) {
  var centerX  = this.x;
  var centerY  = this.y;
  var amplitude = 0;
  var endangle  = 90;
  var distX = endX-this.x;
  var distY = endY-this.y;
  var doStep = step ? step : 3;
  var amplitude = Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
  var slantangle = (endX==this.x) ? 90 : (endY==this.y) ? 0 : Math.abs(Math.atan(distY/distX)*180/Math.PI);
  slantangle = ((endX>=this.x) ? ((endY>this.y) ? 360-slantangle : slantangle) : (endY>this.y) ? 180+slantangle : 180-slantangle) * Math.PI/180;
  
  this.glideActive = true;
  this.glideAngle = 0;

  // do first step
  this.glideStep(amplitude, centerX, centerY, slantangle, doStep);

  // start interval
  window.clearInterval(this.intvl);

  this.intvl = window.setInterval(function() {
    this.doGlide(amplitude, endangle, centerX, centerY, slantangle, endX, endY, distX, distY, doStep, action, stepAction);
  }.bind(this), !speed ? speed : 20);
};


// sliding animation step (animation frame)
Layer.object.prototype.doGlide = function(amplitude, endangle, centerX, centerY, slantangle, endX, endY, distX, distY, angleinc, action, stepAction) {
  if(this.glideActive) {
    if(this.glideAngle < endangle && this.glideActive) {
      this.glideStep(amplitude, centerX, centerY, slantangle, angleinc, stepAction);
    }
    else {
      window.clearInterval(this.intvl);

      // finish gliding
      this.glideActive = false;
      this.move(endX, endY);
      
      if(action) {
	action.apply(null, {x: endX, y:endY});
      }
    }
  }
  return false;
};

// execute one step in the gliding process
Layer.object.prototype.glideStep = function(amplitude, centerX, centerY, slantangle, angleinc, stepAction) {
  if(this.glideActive) {
    this.glideAngle += angleinc;
    var u = amplitude*Math.sin(this.glideAngle*Math.PI/180);
    var x = Math.round(Number(centerX) + u*Math.cos(slantangle));
    var y = Math.round(Number(centerY) - u*Math.sin(slantangle));
    
    this.move(x, y);
  
    if(stepAction) {
      stepAction.apply(null, {x:x, y:y});
    }
  }

  return false;
};


// set the layer above another
Layer.object.prototype.above = function(sub) {
  if(sub!=this) {
    if(sub.css.zIndex=='') {
      sub.css.zIndex = 0;
    }

    this.css.setAttribute('zIndex', parseInt(sub.css.zIndex)+1);
    
    if(this.element.moveAbove) {
      this.element.moveAbove(sub.element);
    }
  }

  return false;
};


// enable/disable a layer to be draggable
Layer.object.prototype.draggable = function(a, prior, actions) {
  if(a) {
    // enable dragging 
    this.dragActive = false;
    
    this.layerDragMouseDownFnc = this.layerDragMouseDownFnc ? this.layerDragMouseDownFnc : this.layerDragMouseDown.bindAsEventListener(this);
    this.layerDragMouseUpFnc = this.layerDragMouseUpFnc ? this.layerDragMouseUpFnc : this.layerDragMouseUp.bindAsEventListener(this);

    Event.observe(this.element, 'mousedown', this.layerDragMouseDownFnc);
    this.element.onmouseup = this.layerDragMouseUpFnc;
    this.element.instance = Layer.layerObjects[this.id];

    this.prior = (prior) ? true : false;
    this.dragActions = (actions) ? actions : false;
  }
  else {
    // disable dragging
    this.dragActive = null;
    Event.stopObserving(this.element, 'mousedown', this.layerDragMouseDownFnc, false);
    this.element.onmouseup = null;

    this.prior = false;
    this.dragActions = false;
  }
};


// eventhandler for dragging mousedown
Layer.object.prototype.layerDragMouseDown = function(e) {
  var obj = this.element.instance;
  var x = Event.pointerX(e);
  var y = Event.pointerY(e);
  var exec = false;
  var button = (e && e.which) ? e.which : (window.event) ? event.button : 0;
  
  obj.abs = obj.absolute();
  if(button>1 || ((button==1 || button==0) && this.keylistener.alt(e)))
    exec = true;
  
  else if(this.checkWithin(x, y, obj.abs.x, obj.abs.x+obj.width, obj.abs.y, obj.abs.y+obj.height)) {
    obj.dragActive = true;
    obj.xOffset = x-obj.x;
    obj.yOffset = y-obj.y;
    
    if(document.onmousemove) {
      obj.eventStacMove = document.onmousemove;
    }

    document.onmousemove = function(e) {
      this.layerDragMouseMove(e);
    }.bindAsEventListener(this);
    
    if(document.onmouseup)
      obj.eventStackUp = document.onmouseup;
    
    document.onmouseup = function(e) {
      if(Layer.layerDragObject)
      this.layerDragMouseUp(e, this.element, true);
    }.bindAsEventListener(this);
    

    if(obj.prior) {
      // make layer appear on top 
      var z = (obj.css.zIndex!='') ? obj.css.zIndex : 0;
      var sub = null;
      
      for(var key in Layer.layerObjects) {
        if(Layer.layerObjects[key].dragActive!=null && Layer.layerObjects[key]!=obj && Layer.layerObjects[key].css.zIndex>=z) {
	  z = Layer.layerObjects[key].css.zIndex;
          sub = Layer.layerObjects[key];
	}
      }
      
      if(sub!=null) {
	obj.above(sub);
      }
      
      Layer.layerDragObject = Layer.layerObjects[obj.id];
    }
    
    exec = true;
    Layer.layerDragObject = Layer.layerObjects[obj.id];
  }
  
  // execute action for this event
  if(exec) {
    if(obj.dragActions && obj.dragActions.down) {
      return obj.dragActions.down(obj, x, y, e);
    }
    return false;
  }    
  
  return true;
}


// eventhandler for dragging mousemove
Layer.object.prototype.layerDragMouseUp = function(e, fromGlobal) {
  var exec = false;
  var obj = fromGlobal ? Layer.layerDragObject : this.element.instance;
  var button = (e && e.which) ? e.which : (window.event) ? event.button : 0;
  
  if(button>1 || ((button==1 || button==0) && this.keylistener.alt(e)))
    exec = true;
  else if(obj && obj==Layer.layerDragObject && obj.dragActive) {
    obj.dragActive = false;
    Layer.layerDragObject = null;
    exec = true;
  }
  
  // reset the mousemove event handler
  document.onmousemove = (obj.eventStacMove) ? obj.eventStacMove : null;
  document.onmouseup = (obj.eventStackUp) ? obj.eventStackUp : null;
  
  // execute action for this event
  if(exec) {
    if(obj.dragActions && obj.dragActions.up) {
      return obj.dragActions.up(obj, obj.x, obj.y, e);
    }
    
    return false;
  }
  
  return true;
}

// compare x/y values if within a rectangle defined by left/right/top/bottom
Layer.object.prototype.checkWithin = function(x, y, left, right, top, bottom) {
  return (x>=left && x<right && y>=top && y<bottom) ? true : false;
}  



// eventhandler for dragging mousemove
Layer.object.prototype.layerDragMouseMove = function(e) {
  var mX = Event.pointerX(e);
  var mY = Event.pointerY(e);
  var state = true;

  // have an active layer to move arround
  if(Layer.layerDragObject && Layer.layerDragObject.dragActive) {
    Layer.layerDragObject.move(mX-Layer.layerDragObject.xOffset, mY-Layer.layerDragObject.yOffset);
    
    // execute action for this event
    if(Layer.layerDragObject.dragActions && Layer.layerDragObject.dragActions.move) {
      state = Layer.layerDragObject.dragActions.move(Layer.layerDragObject, mX, mY, e);
    }
    
    state = false;
  }
  
  return state;
}
