(function(root, factory) {
  "use strict";
  if (typeof define === "function" && typeof define.amd === "object") {
    define([],
    function() {
      return factory(root)
    })
  } else {
    root.SineWaves = factory(root)
  }
})(this,
function() {
  "use strict";
  if (!Function.prototype.bind) {
    Function.prototype.bind = function(oThis) {
      if (typeof this !== "function") {
        throw new TypeError("Function.prototype.bind - what is trying to be bound is not callable")
      }
      var aArgs = Array.prototype.slice.call(arguments, 1);
      var fToBind = this;
      var fNOP = function() {};
      var fBound = function() {
        return fToBind.apply(this instanceof fNOP && oThis ? this: oThis, aArgs.concat(Array.prototype.slice.call(arguments)))
      };
      fNOP.prototype = this.prototype;
      fBound.prototype = new fNOP;
      return fBound
    }
  }
  var vendors = ["ms", "moz", "webkit", "o"];
  for (var x = 0; x < vendors.length && !window.requestAnimationFrame; ++x) {
    window.requestAnimationFrame = window[vendors[x] + "RequestAnimationFrame"];
    window.cancelAnimationFrame = window[vendors[x] + "CancelAnimationFrame"] || window[vendors[x] + "CancelRequestAnimationFrame"]
  }
  if (!window.requestAnimationFrame) {
    var lastFrameTime = 0;
    window.requestAnimationFrame = function(callback) {
      var currTime = (new Date).getTime();
      var timeToCall = Math.max(0, 16 - (currTime - lastFrameTime));
      var id = window.setTimeout(function() {
        callback(currTime + timeToCall)
      },
      timeToCall);
      lastFrameTime = currTime + timeToCall;
      return id
    }
  }
  if (!window.cancelAnimationFrame) {
    window.cancelAnimationFrame = function(id) {
      clearTimeout(id)
    }
  }
  var PI180 = Math.PI / 180;
  var PI2 = Math.PI * 2;
  var HALFPI = Math.PI / 2;
  var Utilities = {};
  var isType = Utilities.isType = function(obj, type) {
    var result = {}.toString.call(obj).toLowerCase();
    return result === "[object " + type.toLowerCase() + "]"
  };
  var isFunction = Utilities.isFunction = function(fn) {
    return isType(fn, "function")
  };
  var isString = Utilities.isString = function(str) {
    return isType(str, "string")
  };
  var isNumber = Utilities.isNumber = function(num) {
    return isType(num, "number")
  };
  var shallowClone = Utilities.shallowClone = function(src) {
    var dest = {};
    for (var i in src) {
      if (src.hasOwnProperty(i)) {
        dest[i] = src[i]
      }
    }
    return dest
  };
  var defaults = Utilities.defaults = function(dest, src) {
    if (!isType(src, "object")) {
      src = {}
    }
    var clone = shallowClone(dest);
    for (var i in src) {
      if (src.hasOwnProperty(i)) {
        clone[i] = src[i]
      }
    }
    return clone
  };
  var degreesToRadians = Utilities.degreesToRadians = function(degrees) {
    if (!isType(degrees, "number")) {
      throw new TypeError("Degrees is not a number")
    }
    return degrees * PI180
  };
  var getFn = Utilities.getFn = function(obj, name, def) {
    if (isFunction(name)) {
      return name
    } else if (isString(name) && isFunction(obj[name.toLowerCase()])) {
      return obj[name.toLowerCase()]
    } else {
      return obj[def]
    }
  };
  var Ease = {};
  Ease.linear = function(percent, amplitude) {
    return amplitude
  };
  Ease.sinein = function(percent, amplitude) {
    return amplitude * (Math.sin(percent * Math.PI - HALFPI) + 1) * .5
  };
  Ease.sineout = function(percent, amplitude) {
    return amplitude * (Math.sin(percent * Math.PI + HALFPI) + 1) * .5
  };
  Ease.sineinout = function(percent, amplitude) {
    return amplitude * (Math.sin(percent * PI2 - HALFPI) + 1) * .5
  };
  var Waves = {};
  Waves.sine = function(x) {
    return Math.sin(x)
  };
  Waves.sin = Waves.sine;
  Waves.sign = function(x) {
    x = +x;
    if (x === 0 || isNaN(x)) {
      return x
    }
    return x > 0 ? 1 : -1
  };
  Waves.square = function(x) {
    return Waves.sign(Math.sin(x * PI2))
  };
  Waves.sawtooth = function(x) {
    return (x - Math.floor(x + .5)) * 2
  };
  Waves.triangle = function(x) {
    return Math.abs(Waves.sawtooth(x))
  };
  function SineWaves(options) {
    this.options = Utilities.defaults(this.options, options);
    this.el = this.options.el;
    delete this.options.el;
    if (!this.el) {
      throw "No Canvas Selected"
    }
    this.ctx = this.el.getContext("2d");
    this.waves = this.options.waves;
    delete this.options.waves;
    if (!this.waves || !this.waves.length) {
      throw "No waves specified"
    }
    this.dpr = window.devicePixelRatio || 1;
    this.updateDimensions();
    window.addEventListener("resize", this.updateDimensions.bind(this));
    this.setupUserFunctions();
    this.easeFn = Utilities.getFn(Ease, this.options.ease, "linear");
    this.rotation = Utilities.degreesToRadians(this.options.rotate);
    if (Utilities.isType(this.options.running, "boolean")) {
      this.running = this.options.running
    }
    this.setupWaveFns();
    this.loop()
  }
  SineWaves.prototype.options = {
    speed: .5,
    rotate: 0,
    ease: "Linear",
    wavesWidth: "95%"
  };
  SineWaves.prototype.setupWaveFns = function() {
    var index = -1;
    var length = this.waves.length;
    while (++index < length) {
      this.waves[index].waveFn = Utilities.getFn(Waves, this.waves[index].type, "sine")
    }
  };
  SineWaves.prototype.setupUserFunctions = function() {
    if (Utilities.isFunction(this.options.resizeEvent)) {
      this.options.resizeEvent.call(this);
      window.addEventListener("resize", this.options.resizeEvent.bind(this))
    }
    if (Utilities.isFunction(this.options.initialize)) {
      this.options.initialize.call(this)
    }
  };
  var defaultWave = {
    timeModifier: 1,
    amplitude: 20,
    wavelength: 50,
    segmentLength: 10,
    lineWidth: 1,
    strokeStyle: "rgba(255, 255, 255, 0.2)",
    type: "Sine"
  };
  function getWaveWidth(value, width) {
    if (Utilities.isType(value, "number")) {
      return value
    }
    value = value.toString();
    if (value.indexOf("%") > -1) {
      value = parseFloat(value);
      if (value > 1) {
        value /= 100
      }
      return width * value
    } else if (value.indexOf("px") > -1) {
      return parseInt(value, 10)
    }
  }
  SineWaves.prototype.getDimension = function(dimension) {
    if (Utilities.isNumber(this.options[dimension])) {
      return this.options[dimension]
    } else if (Utilities.isFunction(this.options[dimension])) {
      return this.options[dimension].call(this, this.el)
    } else if (dimension === "width") {
      return this.el.clientWidth
    } else if (dimension === "height") {
      return this.el.clientHeight
    }
  };
  SineWaves.prototype.updateDimensions = function() {
    var width = this.getDimension("width");
    var height = this.getDimension("height");
    this.width = this.el.width = width * this.dpr;
    this.height = this.el.height = height * this.dpr;
    this.el.style.width = width + "px";
    this.el.style.height = height + "px";
    this.waveWidth = getWaveWidth(this.options.wavesWidth, this.width);
    this.waveLeft = (this.width - this.waveWidth) / 2;
    this.yAxis = this.height / 2
  };
  SineWaves.prototype.clear = function() {
    this.ctx.clearRect(0, 0, this.width, this.height)
  };
  SineWaves.prototype.time = 0;
  SineWaves.prototype.update = function(time) {
    this.time = this.time - .007;
    if (typeof time === "undefined") {
      time = this.time
    }
    var index = -1;
    var length = this.waves.length;
    this.clear();
    this.ctx.save();
    if (this.rotation > 0) {
      this.ctx.translate(this.width / 2, this.height / 2);
      this.ctx.rotate(this.rotation);
      this.ctx.translate( - this.width / 2, -this.height / 2)
    }
    while (++index < length) {
      var timeModifier = this.waves[index].timeModifier || 1;
      this.drawWave(time * timeModifier, this.waves[index])
    }
    this.ctx.restore();
    index = void 0;
    length = void 0
  };
  SineWaves.prototype.getPoint = function(time, position, options) {
    var x = time * this.options.speed + ( - this.yAxis + position) / options.wavelength;
    var y = options.waveFn.call(this, x, Waves);
    var amplitude = this.easeFn.call(this, position / this.waveWidth, options.amplitude);
    x = position + this.waveLeft;
    y = amplitude * y + this.yAxis;
    return {
      x: x,
      y: y
    }
  };
  SineWaves.prototype.drawWave = function(time, options) {
    options = Utilities.defaults(defaultWave, options);
    this.ctx.lineWidth = options.lineWidth * this.dpr;
    this.ctx.strokeStyle = options.strokeStyle;
    this.ctx.lineCap = "butt";
    this.ctx.lineJoin = "round";
    this.ctx.beginPath();
    this.ctx.moveTo(0, this.yAxis);
    this.ctx.lineTo(this.waveLeft, this.yAxis);
    var i;
    var point;
    for (i = 0; i < this.waveWidth; i += options.segmentLength) {
      point = this.getPoint(time, i, options);
      this.ctx.lineTo(point.x, point.y);
      point = void 0
    }
    i = void 0;
    options = void 0;
    this.ctx.lineTo(this.width, this.yAxis);
    this.ctx.stroke()
  };
  SineWaves.prototype.running = true;
  SineWaves.prototype.loop = function() {
    if (this.running === true) {
      this.update()
    }
    window.requestAnimationFrame(this.loop.bind(this))
  };
  SineWaves.prototype.Waves = Waves;
  SineWaves.prototype.Ease = Ease;
  return SineWaves
});
var waves = new SineWaves({
  el: document.getElementById("waves"),
  speed: 3,
  height: 400,
  ease: "SineInOut",
  wavesWidth: "100%",
  waves: [{
    timeModifier: 4,
    lineWidth: 1,
    amplitude: -25,
    wavelength: 25
  },
  {
    timeModifier: 2,
    lineWidth: 2,
    amplitude: -50,
    wavelength: 50
  },
  {
    timeModifier: 1,
    lineWidth: 1,
    amplitude: -100,
    wavelength: 100
  }],
  resizeEvent: function() {
    var gradient = this.ctx.createLinearGradient(0, 0, this.width, 0);
    gradient.addColorStop(0, "rgba(225, 225, 225, 0.2)");
    gradient.addColorStop(.5, "rgba(225, 225, 225, 0.5)");
    gradient.addColorStop(1, "rgba(225, 225, 225, 0.2)");
    var index = -1;
    var length = this.waves.length;
    while (++index < length) {
      this.waves[index].strokeStyle = gradient
    }
    index = void 0;
    length = void 0;
    gradient = void 0
  }
});