tsgauge = function() {
    var tsgauge = {
        gauge:null,
        displayNum:null,
        displayTitle:null
    };

    tsgauge.init = function(config){
        var svg = d3.select(config.id)
        .append("svg:svg")
                .attr("width", config.width||600)
                .attr("height", config.height||400);

         gauge = tsgauge.arcslider()
                .radius(config.radius||200)
                .events(false)
                .indicator(tsgauge.defaultGaugeIndicator);
        gauge.axis().orient("in")
                .normalize(true)
                .ticks(12)
                .tickSubdivide(3)
                .tickSize(10, 8, 10)
                .tickPadding(5)
                .scale(d3.scale.linear()
                        .domain([0, 100])
                        .range([-Math.PI/2, Math.PI/2]));

       displayNum = tsgauge.displayNum();
         displayTitle = tsgauge.displayTitle();

       
        svg.append("g")
                .attr("class", "gauge")
                .call(gauge);
        displayTitle.title(config.title||'');

        return tsgauge;
        

    }
    tsgauge.setGaugeValue = function(val){
       gauge.value(val);
        displayNum.value(val);
    };
    tsgauge.arcslider = function()
    {
        var radius = 100, minEventInterval = 100, enableFlick = true, transitionDuration = 500, arcFactor = 0.95, moveToTouch = true, ease = "cubic-out";
        var margin = {"top": 50, "left": 50, "bottom": 50, "right": 50};
        var axis = tsgauge.arcaxis()
            .scale(d3.scale.linear()
                .range([- 3* Math.PI / 4, 3* Math.PI / 4]))
            .orient("out")
            .outerRadius(radius)
            .innerRadius(radius-20);
        var bands = [];
        var _range, _extent, _invert, _comp, _indicator, _cursorArc, _pointerUpdate, _cursorUpdate;
        var _slide, _currentValue, _currentRad, _lastEvent, _onValueChanged, _lastAngle, _delta = 0;
        
        function arcslider(g)
        {
            _range = tsgauge_scaleRange(axis.scale());
            _extent = tsgauge_scaleExtent(axis.scale());
            _invert = (_range[0] > _range[1]) ? true : false;
            _comp = 2*Math.PI - Math.abs(_range[1] - _range[0]);
            _comp < 0 ? 0 : _comp;

            g.each(function() {
                var g = d3.select(this);

                g.on("pointerleave", function(){_slide=false; return false;})
                    .on("pointerup", function(){_slide=false; return false;});

                var arc = g.selectAll(".arc").data([ 0 ])
                    , arcUpdate = (arc.enter().append("g").attr("class", "arc"), d3.transition(arc));
                 g.append("g")
                .attr("class", "number unselectable")
                .attr("transform", "translate("+ (radius + margin.left)+", " + (margin.top+0.8*radius)+")")
                .call(displayNum);

                    g.append("g")
                .attr("class", "title unselectable")
                .attr("transform", "translate("+ (radius + margin.left)+"," + (margin.top+radius/2)+")")
                .style('font-size','1.5em')
                .call(displayTitle);

                arcUpdate.attr("transform", "translate(" + (radius + margin.left) + ", " + (radius + margin.top) + ")");
                
                arcUpdate.append("path")
                    .attr("class", "lane")
                    .attr("d", d3.svg.arc()
                        .startAngle(_extent[0])
                        .endAngle(_extent[1])
                        .innerRadius(arcFactor * radius)
                        .outerRadius(radius));

                _cursorArc = d3.svg.arc()
                    .startAngle(_range[0])
                    .endAngle(_range[1])
                    .innerRadius(arcFactor * radius)
                    .outerRadius(radius);

                arcUpdate.append("path")
                    .attr("class", "cursor")
                    .attr("d", _cursorArc);
                
                _cursorUpdate = arcUpdate.selectAll(".cursor");


                var b = g.selectAll(".band").data(bands)
                    , bandsUpdate = (b.enter().append("g").attr("class", "band"), d3.transition(b))
                    , bandsExit = d3.transition(b.exit()).style("opacity", 1e-6).remove();

                bandsUpdate.attr("transform", "translate(" + (radius + margin.left) + ", " + (radius + margin.top) + ")")
                    .each(function(band) {d3.select(this).append("path")
                                .attr("class", band.class)
                                .attr("d", d3.svg.arc()
                                            .startAngle(tsgauge_convert(axis.scale(), band.domain[0]))
                                            .endAngle(tsgauge_convert(axis.scale(), band.domain[1]))
                                            .innerRadius(band.span[0] * radius)
                                            .outerRadius(band.span[1] * radius))});

                var a = g.selectAll(".axis").data([ 0 ])
                    , axisUpdate = (a.enter().append("g").attr("class", "g_axis"), d3.transition(a));

                axisUpdate.attr("transform", "translate(" + (radius + margin.left) + ", " + (radius + margin.top) + ")")
                    .call(axis);

                var indicator = g.selectAll(".indicator").data([ 0 ])
                    , indicatorUpdate = (indicator.enter().append("g").attr("class", "indicator"), d3.transition(indicator));

                _pointerUpdate=indicatorUpdate.attr("transform", "translate(" + (radius + margin.left) + ", " + (radius + margin.top) + ")")
                    .append("g")
                    .attr("class", "pointer")
                    .attr("transform", "rotate("+ 180 * _range[0] / Math.PI +")");
                
                if(undefined != _indicator) {
                    _pointerUpdate.call(_indicator, radius)
                }
                    
                redraw(tsgauge_invert(axis.scale(), _range[0]));
            });
        }
        
        function redraw(value, td)
        {
            if(value == _currentValue) return;
            _delta = 0;

            var rad = tsgauge_convert(axis.scale(), value);
            var startRad = (typeof _currentRad == "undefined" || isNaN(_currentRad)) ?  _range[0] : _currentRad;
            
            _cursorUpdate.transition()
                .duration(td)
                .delay(0)
                .ease(ease)
                .attrTween("d", function() {
                    
                    return function(step) {
                        _currentRad = startRad + (rad - startRad) * step;
                        _currentValue = tsgauge_invert(axis.scale(), _currentRad);
                        
                        var now = new Date().getTime();
                        if(_onValueChanged && (step==1 || (_lastEvent || 0) + minEventInterval < now)) {
                            _onValueChanged(_currentValue, step==1);
                            _lastEvent=now;
                        }
                        
                        if(_comp != 0) {
                            _pointerUpdate.attr("transform", "rotate(" + 180 * _currentRad / Math.PI + ")");
                        }
                        return _cursorArc.endAngle(_currentRad)();
                    }
                })
                .each(function() {
                    if(_comp == 0) {
                        d3.transition(_pointerUpdate)
                            .duration(td)
                            .delay(0)
                            .ease(ease)
                            .attr("transform", "rotate(" + 180 * rad / Math.PI + ")");
                    }
                });
        }

        arcslider.value = function(x) {
            if (!arguments.length) return _currentValue;
            redraw(x, transitionDuration);
            return arcslider;
        };
        arcslider.scale = function(x) {
            if (!arguments.length) return axis.scale();
            axis.scale(x);
            return arcslider;
        };
        arcslider.axis = function(x) {
            if (!arguments.length) return axis;
            axis = x;
            return arcslider;
        };
        arcslider.bands = function(x) {
            if (!arguments.length) return bands;
            bands = x;
            return arcslider;
        };
        arcslider.radius = function(x) {
            if (!arguments.length) return radius;
            radius = x;
            axis.outerRadius(x);
            axis.innerRadius(x);
            return arcslider;
        };
        arcslider.arcFactor = function(x) {
            if (!arguments.length) return arcFactor;
            arcFactor = x;
            return arcslider;
        };
        arcslider.moveToTouch = function(x) {
            if (!arguments.length) return moveToTouch;
            moveToTouch = x;
            return arcslider;
        };
        arcslider.transitionDuration = function(x) {
            if (!arguments.length) return transitionDuration;
            transitionDuration = x;
            return arcslider;
        };
        arcslider.ease = function(x) {
            if (!arguments.length) return ease;
            ease = x;
            return arcslider;
        };
        arcslider.indicator = function(x) {
            if (!arguments.length) return _indicator;
            _indicator = x;
            return arcslider;
        };
        arcslider.events = function(x) {
            if (!arguments.length) return events;
            events = x;
            return arcslider;
        };
        arcslider.onValueChanged = function(x) {
            if (!arguments.length) return _onValueChanged;
            _onValueChanged = x;
            return arcslider;
        };
        function pointToRad(x, y, cont) {
            var r = Math.sqrt(x * x + y * y);
            var omega = Math.atan2(x, -y);
            
            if(_invert) {
                if(omega - 2*Math.PI > (_range[1] - _comp / 2)) omega -= 2*Math.PI;
                else if(omega + 2*Math.PI < (_range[0] + _comp / 2)) omega += 2*Math.PI;
            }
            else {
                if(omega - 2*Math.PI > (_range[0] - _comp / 2)) omega -= 2*Math.PI;
                else if(omega + 2*Math.PI < (_range[1] + _comp / 2)) omega += 2*Math.PI;
            }
            
            if(cont && _comp > 0) {
                if(omega - _lastAngle < -Math.PI) omega += 2*Math.PI;
                else if(omega - _lastAngle > Math.PI) omega -= 2*Math.PI;
            }
            _delta += omega - (_lastAngle || omega);
            _lastAngle = omega;    
            return {"r": r, "omega": omega, "delta": _delta};
        }
        function radToValue(omega) {
            var min = ((!_invert && omega < _range[0]) || (_invert && omega > _range[0]));
            var max = ((!_invert && omega > _range[1]) || (_invert && omega < _range[1]));
            return tsgauge_invert(axis.scale(), min ? _range[0] : max ? _range[1] : omega);
        }
        return arcslider;
    };
    tsgauge.defaultArcSliderIndicator = function(g, r) {
        //g.append("circle").attr("r", 0.95 * r).attr("class", "knob");
        g.append("path").attr("d", "M0 " + -0.7 * r + " S 0 " + -0.98 * r + "");
    };
    tsgauge.defaultGaugeIndicator = function(g, r) {
        g.append("path").attr("d", "M"+ -0.08*r + " 0 L 0 " + -0.76 * r + "L"+0.08*r+" 0");
        g.append("circle").attr("r", 0.08 * r);
    };
    tsgauge.displayTitle = function(){
        var _gtitle = null;
        function displayTitle(g){
            g.each(function() {
                _gtitle = g.append('text')
                .attr('dominant-baseline','central')
                .attr('text-anchor','middle');
            });
        }
        displayTitle.title = function(val){
            _gtitle.text(val);
            return displayTitle;
        };
        return displayTitle;
    }

    tsgauge.displayNum = function(){
        var _gtext= null;
        function displayNum(g){
            g.each(function(){
                _gtext =  g.append('text')
                .attr('dominant-baseline','central')
                .attr('text-anchor','middle');
                
            });

        }
        displayNum.value = function(val){
            _gtext.text(val);
            return displayNum;
        };
       
        return displayNum;
    }
    
    tsgauge.arcaxis = function() {
        var scale = d3.scale.linear(), outerRadius = 100, innerRadius = 100, orient = "out", tickMajorSize = 6, tickMinorSize = 4, tickEndSize = 6, tickPadding = 3, tickArguments_ = [ 10 ], tickValues = null, tickFormat_, tickSubdivide = 0, normalize = true;
        function arcaxis(g) {
            g.each(function() {
                var g = d3.select(this);
                var ticks = tickValues == null ? scale.ticks ? scale.ticks.apply(scale, tickArguments_) : scale.domain() : tickValues
                    , tickFormat = tickFormat_ == null ? scale.tickFormat ? scale.tickFormat.apply(scale, tickArguments_) : String : tickFormat_;
                ticks = tickArguments_[0]<3 ? d3.extent(ticks) : ticks;
                
                var subticks = tsgauge_axisSubdivide(scale, ticks, tickSubdivide)
                    , subtick = g.selectAll(".tick.minor").data(subticks, String)
                    , subtickEnter = subtick.enter().insert("line", ".tick").attr("class", "tick minor").style("opacity", 1e-6).style("stroke",function(d){
                    	if(d<=30){
                    		return "#3fb0ac";
                    	}
                    	else if(d<=70){
                    		return "#fae596";
                    	}
                    	else{
                    		return "#e62739";
                    	}
                    })
                    , subtickExit = d3.transition(subtick.exit()).style("opacity", 1e-6).remove()
                    , subtickUpdate = d3.transition(subtick).style("opacity", 1);
                
                var tick = g.selectAll(".tick.major").data(ticks, String).style("opacity", 1e-6)
                    , tickEnter = tick.enter().insert("g", ".domain").attr("class", "tick major").style("opacity", 1e-6).style("fill",function(d){
                    	if(d<=30){
                    		return "#3fb0ac";
                    		}
                    	else if(d<=70){
                    		return "#fae596";
                    		}
                    	else{
                    		return "#e62739";
                    		}
                        })
                    , tickExit = d3.transition(tick.exit()).style("opacity", 1e-6).remove()
                    , tickUpdate = d3.transition(tick).style("opacity", 1), tickTransform;
                
                var extent = tsgauge_scaleExtent(scale)
                    , path = g.selectAll(".domain").data([ 0 ])
                    , pathUpdate = (path.enter().append("path").attr("class", "domain"), d3.transition(path));
                
                var scale1 = scale.copy(), scale0 = this.__chart__ || scale1;
                this.__chart__ = scale1;
                tickEnter.append("line");
                tickEnter.append("text");
                var lineEnter = tickEnter.select("line")
                    , lineUpdate = tickUpdate.select("line")
                    , text = tick.select("text").text(tickFormat)
                    , textEnter = tickEnter.select("text")
                    , textUpdate = tickUpdate.select("text")
                    , textTransform;
                switch (orient) {
                    case "out":
                    {
                        tickTransform = tsgauge_axis_transform;
                        subtickEnter.attr("y2", -tickMinorSize);
                        subtickUpdate.attr("x2", 0).attr("y2", -tickMinorSize);
                        lineEnter.attr("y2", -tickMajorSize).style("stroke",function(d){
                        	if(d<=30){
                        		return "#3fb0ac";
                        		}
                        	else if(d<=70){
                        		return "#fae596";
                        		}
                        	else{
                        		return "#e62739";
                        		}
                            });
                        lineUpdate.attr("x2", 0).attr("y2", -tickMajorSize);
                        pathUpdate.attr("d", d3.svg.arc()
                            .startAngle(extent[0])
                            .endAngle(extent[1])
                            .innerRadius(innerRadius)
                            .outerRadius(outerRadius));
                        
                        if(normalize) {
                            textTransform = tsgauge_text_transform_normalize_out;
                            textEnter.call(textTransform, scale0, -(Math.max(tickMajorSize, 0) + tickPadding));
                            textUpdate.call(textTransform, scale1, -(Math.max(tickMajorSize, 0) + tickPadding));
                            text.attr("class", "unselectable").attr("dy", "0em");
                        }
                        else {
                            textEnter.attr("y", -(Math.max(tickMajorSize, 0) + tickPadding));
                            textUpdate.attr("x", 0).attr("y", -(Math.max(tickMajorSize, 0) + tickPadding));
                            text.attr("class", "unselectable").attr("dy", "0em").style("text-anchor", "middle");
                        }

                        break;
                    }
                    case "in":
                    {
                        var t = outerRadius - innerRadius;
                        tickTransform = tsgauge_axis_transform;
                        subtickEnter.attr("y2", tickMinorSize + t);
                        subtickUpdate.attr("x2", 0).attr("y2", tickMinorSize + t);
                        lineEnter.attr("y2", tickMajorSize + t).style("stroke",function(d){
                        	if(d<=30){
                        		return "#3fb0ac";
                        		}
                        	else if(d<=70){
                        		return "#fae596";
                        		}
                        	else{
                        		return "#e62739";
                        		}
                            });
                        lineUpdate.attr("x2", 0).attr("y2", tickMajorSize + t);
                        pathUpdate.attr("d", d3.svg.arc()
                            .startAngle(extent[0])
                            .endAngle(extent[1])
                            .innerRadius(innerRadius)
                            .outerRadius(outerRadius));

                        if(normalize) {
                            textTransform = tsgauge_text_transform_normalize_in;
                            textEnter.call(textTransform, scale0, (Math.max(tickMajorSize, 0) + tickPadding + t), 0.71);
                            textUpdate.call(textTransform, scale1, (Math.max(tickMajorSize, 0) + tickPadding + t), 0.71);
                            text.attr("class", "unselectable");
                        }
                        else {
                            textEnter.attr("y", Math.max(tickMajorSize, 0) + tickPadding + t);
                            textUpdate.attr("x", 0).attr("y", Math.max(tickMajorSize, 0) + tickPadding + t);
                            text.attr("class", "unselectable").attr("dy", ".71em").style("text-anchor", "middle");
                        }
                        break;
                    }
                }
                
                var r = outerRadius;
                if (scale.rangeBand) {
                    var dx = scale1.rangeBand() / 2, x = function(d) {
                        return scale1(d) + dx;
                    };
                    tickEnter.call(tickTransform, x, r);
                    tickUpdate.call(tickTransform, x, r);
                } else {
                    tickEnter.call(tickTransform, scale0, r);
                    tickUpdate.call(tickTransform, scale1, r);
                    tickExit.call(tickTransform, scale1, r);
                    subtickEnter.call(tickTransform, scale0, r);
                    subtickUpdate.call(tickTransform, scale1, r);
                    subtickExit.call(tickTransform, scale1, r);
                }
            });
        }
        arcaxis.scale = function(x) {
            if (!arguments.length) return scale;
            scale = x;
            return arcaxis;
        };
        arcaxis.innerRadius = function(x) {
            if (!arguments.length) return innerRadius;
            innerRadius = x;
            return arcaxis;
        };
        arcaxis.outerRadius = function(x) {
            if (!arguments.length) return outerRadius;
            outerRadius = x;
            return arcaxis;
        };
        arcaxis.orient = function(x) {
            if (!arguments.length) return orient;
            orient = x in tsgauge_axisOrients ? x + "" : tsgauge_axisDefaultOrient;
            return arcaxis;
        };
        arcaxis.ticks = function() {
            if (!arguments.length) return tickArguments_;
            tickArguments_ = arguments;
            return arcaxis;
        };
        arcaxis.tickValues = function(x) {
            if (!arguments.length) return tickValues;
            tickValues = x;
            return arcaxis;
        };
        arcaxis.tickFormat = function(x) {
            if (!arguments.length) return tickFormat_;
            tickFormat_ = x;
            return arcaxis;
        };
        arcaxis.tickSize = function(x, y) {
            if (!arguments.length) return tickMajorSize;
            var n = arguments.length - 1;
            tickMajorSize = +x;
            tickMinorSize = n > 1 ? +y : tickMajorSize;
            tickEndSize = n > 0 ? +arguments[n] : tickMajorSize;
            return arcaxis;
        };
        arcaxis.tickPadding = function(x) {
            if (!arguments.length) return tickPadding;
            tickPadding = +x;
            return arcaxis;
        };
        arcaxis.tickSubdivide = function(x) {
            if (!arguments.length) return tickSubdivide;
            tickSubdivide = +x;
            return arcaxis;
        };
        arcaxis.normalize = function(x) {
            if (!arguments.length) return normalize;
            normalize = x;
            return arcaxis;
        };

        return arcaxis;
    };
    function tsgauge_extent(domain) {
        var start = domain[0], stop = domain[domain.length - 1];
        return start < stop ? [ start, stop ] : [ stop, start ];
    }
    function tsgauge_scaleExtent(scale) {
        return scale.rangeExtent ? scale.rangeExtent() : tsgauge_extent(scale.range());
    }
    function tsgauge_scaleRange(scale) {
        var extent = tsgauge_scaleExtent(scale);
        var range = scale.range();
        return range[0] < range[range.length - 1] ? [ extent[0], extent[1] ] : [ extent[1], extent[0] ];
    }
    function tsgauge_convert(scale, x) {
        var d = scale(x);
        isNaN(d) ? d = tsgauge_scaleRange(scale)[0] : d;
        return scale.rangeBand ? d + scale.rangeBand() / 2 : d;
    }
    function tsgauge_invert(scale, x) {
        if(scale.invert) return scale.invert(x);

        var l = scale.domain().length;
        var range = tsgauge_scaleRange(scale);
        var band = (range[1] - range[0]) / l;
        var index = Math.floor((x - range[0]) / band);
        return scale.domain()[index < l ? index : l-1];

    }
    var tsgauge_axisDefaultOrient = "out", tsgauge_axisOrients = {
        in: 1,
        out: 1
    };
    function tsgauge_axis_transform(selection, x, r) {
        selection.attr("transform", function(d) {
            return "translate(" + r * Math.sin(x(d)) + "," + -r * Math.cos(x(d)) + ") rotate("+ 180 / Math.PI * x(d) +")";
        });
    }
    function tsgauge_text_transform_normalize_out(selection, x, dr) {
        selection.attr("transform", function(d) {
                var a = x(d) + (x.rangeBand ? x.rangeBand() / 2 : 0);
                return "rotate("+ -180 / Math.PI * a + ")" + "translate(" + -dr * Math.sin(a) + "," + dr * Math.cos(a) + ")";
            })
            .style("text-anchor", function(d) {
                var a = x(d) + (x.rangeBand ? x.rangeBand() / 2 : 0);
                a = a < -Math.PI ? a += 2*Math.PI : a > Math.PI ? a -= 2*Math.PI : a;
                return a > -19*Math.PI/20 && a < -Math.PI/20 ? "end" : a < 19*Math.PI/20 && a > Math.PI/20 ? "start" : "middle";
            })
            .style("baseline-shift", function(d) {
                var a = x(d) + (x.rangeBand ? x.rangeBand() / 2 : 0);
                return -80 * Math.pow(Math.sin(Math.abs(a/2)), 2) + "%";
            });
    }
    function tsgauge_text_transform_normalize_in(selection, x, dr, em) {
        selection.attr("transform", function(d) {
            var a = x(d) + (x.rangeBand ? x.rangeBand() / 2 : 0);
            return "rotate("+ -180 / Math.PI * a + ")" + "translate(" + -dr * Math.sin(a) + "," + dr * Math.cos(a) + ")";
            })
            .style("text-anchor", function(d) {
                var a = x(d) + (x.rangeBand ? x.rangeBand() / 2 : 0);
                a = a < -Math.PI ? a += 2*Math.PI : a > Math.PI ? a -= 2*Math.PI : a;
                return a > -7*Math.PI/8 && a < -Math.PI/8 ? "start" : a < 7*Math.PI/8 && a > Math.PI/8 ? "end" : "middle";
            })
             .style("baseline-shift", function(d) {
                var a = x(d) + (x.rangeBand ? x.rangeBand() / 2 : 0);
                return -100 * Math.pow(Math.cos(Math.abs(a/2)), 3) + "%";
            });
    }
    function tsgauge_axisSubdivide(scale, ticks, m) {
        subticks = [];
        if (m && ticks.length > 1) {
            var extent = tsgauge_extent(scale.domain()), subticks, i = -1, n = ticks.length, d = (ticks[1] - ticks[0]) / ++m, j, v;
            while (++i < n) {
                for (j = m; --j > 0; ) {
                    if ((v = +ticks[i] - j * d) >= extent[0]) {
                        subticks.push(v);
                    }
                }
            }
            for (--i, j = 0; ++j < m && (v = +ticks[i] + j * d) < extent[1]; ) {
                subticks.push(v);
            }
        }
        return subticks;
    }
    
    return tsgauge;
}();




