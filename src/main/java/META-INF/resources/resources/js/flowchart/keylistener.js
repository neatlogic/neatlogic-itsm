 
var Key = {
  Version: '0.1',
  prototypeVersion: parseFloat(Prototype.Version.split(".")[0] + "." + Prototype.Version.split(".")[1])
};

Key.listener = Class.create();
Key.listener.prototype.initialize=function() {

  this.objects={down:{}, up:{}};
  
  // listen to keys
  Event.observe(window.document, "keydown", function(e) {
    // get keyCode
    var key=(e.which) ? e.which : e.keyCode;
    
    // kick function if needed
    if(this.objects.down[key])
      return this.objects.down[key](e, 'down');
  }.bindAsEventListener(this), false);
  
  
  // listen to keys
  Event.observe(window.document, "keyup", function(e) {
    // get keyCode
    var key=(e.which) ? e.which : e.keyCode;
    
    // kick function if needed
    if(this.objects.up[key])    
      return this.objects.up[key](e, 'up');
  }.bindAsEventListener(this), false);
};

Key.listener.prototype.add=function(mode, key, func) {
  this.objects[mode][key]=func;
};

Key.listener.prototype.alt=function(e) {
  if(!e && window.event)
  e = window.event;
  
  return ((bw.linux && bw.ns4 && e.modifiers) || (bw.opera && e.ctrlKey) || ((bw.ns4 && e.modifiers & Event.ALT_MASK) || e.altKey)) ? true : false;
}








