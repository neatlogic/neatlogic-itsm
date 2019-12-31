
tsareachart = function(){

  var tsareachart ={
    generate:null,
    init:null
  };

    tsareachart.init = function(id,width,height){
      

      var data = [
        {time: '10:01', upload: 200, download: 500, total: 1000},
        {time: '10:02', upload: 620, download: 600, total: 1000},
        {time: '10:03', upload: 300, download: 800, total: 1000},
        {time: '10:04', upload: 440, download: 700, total: 1000},
        {time: '10:05', upload: 900, download: 900, total: 1000},
        {time: '10:06', upload: 300, download: 500, total: 1000},
        {time: '10:07', upload: 50, download: 300, total: 1000},
        {time: '10:08', upload: 350, download: 70, total: 1000},
        {time: '10:09', upload: 750, download: 200, total: 1000}
      ];

      var category = ['upload', 'download'];

      var hAxis = 10, mAxis = 10;

      var sca = new generate(data, category,id, "linear", 6,width,height);
       //dynamic data and chart update
      setInterval(function() {
        //update donut data
        data.push({time: hAxis + ":" + mAxis, upload: Math.random()*200+400, download: Math.random()*400+200, total: 1000});

        // console.log(tAxis);
        if(mAxis === 59) {
          hAxis++;
          mAxis=0;
        }
        else {
          mAxis++;
        }

        if (Object.keys(data).length === 20) data.shift();

        redraw(data, category,id, sca.getOpt()['x'], sca.getOpt()['y'], sca.getOpt()['xAxis'], sca.getSvg()['svg'], sca.getSvg()['area'], sca.getSvg()['path'], sca.getSvg()['points'], sca.getSvg()['legendColor'], sca.getOpt()['height'], 6);
      }, 3500);
      return tsareachart;

    };


    //generation function
      function generate(data,category, id, lineType, axisNum,width,height) {
        var margin = {top: 20, right: 18, bottom: 35, left: 28},
            width = width - margin.left - margin.right,
            height = height - margin.top - margin.bottom;

        var parseDate = d3.time.format("%H:%M").parse;

        var legendSize = 10,
            legendColor = {'upload': 'rgba(0, 160, 233, 1)', 'download': 'rgba(34, 172, 56, 1)'};

        var x = d3.time.scale()
            .range([0, width]);

        var y = d3.scale.linear()
            .range([height, 0]);

        //data.length/10 is set for the garantte of timeseries's fitting effect in svg chart
        var xAxis = d3.svg.axis()
            .scale(x)
            .ticks(d3.time.minutes, Math.floor(data.length / axisNum))
            .tickSize(-height)
            .tickPadding([6])
            .orient("bottom");

        var yAxis = d3.svg.axis()
            .scale(y)
            .ticks(10)
            .tickSize(-width)
            .orient("left");

        var ddata = (function() {
          var temp = {}, seriesArr = [];

          category.forEach(function (name) {
            temp[name] = {category: name, values:[]};
            seriesArr.push(temp[name]);
          });

          data.forEach(function (d) {
            category.map(function (name) {
              temp[name].values.push({'category': name, 'time': parseDate(d['time']), 'num': d[name]});
            });
          });

          return seriesArr;
        })();

        // q = ddata;
        // console.log(ddata);

        x.domain( d3.extent(data, function(d) { return parseDate(d['time']); }) );

        y.domain([
          0,
          d3.max(ddata, function(c) { return d3.max(c.values, function(v) { return v['num']; }); })+100
        ]);

        var area = d3.svg.area()
            .x(function(d) { return x(d['time']); })
            .y0(height)
            .y1(function(d) { return y(d['num']); })
            .interpolate(lineType);

        d3.select('#svg-net').remove();

        var svg = d3.select(id).append("svg")
            .attr("id", "svg-net").classed('tsareachart',true)
            .attr("width", width+margin.right+margin.left)
            .attr("height", height+margin.top+margin.bottom)
            .append("g")
            .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

//        svg.append("g")
//            .attr("class", "x axis")
//            .attr("id", "net-x-axis")
//            .attr("transform", "translate(0," + height + ")")
//            .call(xAxis);

        svg.append("g")
            .attr("class", "y axis")
            .call(yAxis);

        var path = svg.selectAll(".gPath")
            .data(ddata)
            .enter().append("g")
            .attr("class", "gPath");

        path.append("path")
            .attr("d", function(d) { return area(d['values']); })
            .attr("class", function(d) {
              if (d['category'] === 'upload')
                return 'areaU';
              else
                return 'areaD';
            });

        var legend = svg.selectAll('.legend')
            .data(category)
            .enter()
            .append('g')
            .attr('class', 'legend')
            .attr('transform', function(d, i) {
              return 'translate(' + (i * 10 * legendSize) + ',' + (height + margin.bottom - legendSize * 1.2) + ')';
            });

        legend.append('rect')
            .attr('width', legendSize)
            .attr('height', legendSize)
            .style('fill', function(d) { return legendColor[d]});

        legend.append('text')
            .data(category)
            .attr('x', legendSize*1.2)
            .attr('y', legendSize/1.1)
            .text(function(d) {
              return d;
            });

        var points = svg.selectAll(".seriesPoints")
            .data(ddata)
            .enter().append("g")
            .attr("class", "seriesPoints");

        points.selectAll(".tipNetPoints")
            .data(function (d) { return d['values']; })
            .enter().append("circle")
            .attr("class", "tipNetPoints")
            .attr("cx", function (d) { return x(d['time']); })
            .attr("cy", function (d) { return y(d['num']); })
            .text(function (d) { return d['num']; })
            .attr("r", "6px")
            .style("fill",function (d) { return legendColor[d['category']]; })
            .on("mouseover", function (d) {
              // console.log();
              var currentX = $(this)[0]['cx']['animVal']['value'],
                  currentY = $(this)[0]['cy']['animVal']['value'];

              d3.select(this).transition().duration(100).style("opacity", 1);

              var ret = $('.tipNetPoints').filter(function(index) {
                return ($(this)[0]['cx']['animVal']['value'] === currentX && $(this)[0]['cy']['animVal']['value'] !== currentY);
              });

              //to adjust tooltip'x content if upload and download data are the same
              var jud = ret.length;

              // console.log(ret.length);

              var mainCate = (function() {
                if (jud === 0)
                  return 'upload/download';
                else
                  return d['category'];
              })();

              var viceCate = (function() {
                if (category[0] === d['category'])
                  return category[1];
                else
                  return category[0];
              })();

              $.each(ret, function(index, val) {
                // console.log(mainCate + ' | ' + viceCate);

                $(val).animate({
                  opacity: "1"
                }, 100);

                $(val).tooltip({
                  'container': 'body',
                  'placement': 'left',
                  'title': viceCate + ' | ' + $(this)[0]['textContent'],
                  'trigger': 'hover'
                })
                    .tooltip('show');
              });

              svg.append("g")
                .attr("class", "tipDot")
                .append("line")
                .attr("class", "tipDot")
                .transition()
                .duration(50)
                .attr("x1", $(this)[0]['cx']['animVal']['value'])
                .attr("x2", $(this)[0]['cx']['animVal']['value'])
                .attr("y2", height);

              svg.append("polyline")
                .attr("class", "tipDot")
                .style("fill", "black")
                .attr("points", ($(this)[0]['cx']['animVal']['value']-3.5)+","+(0-2.5)+","+$(this)[0]['cx']['animVal']['value']+","+(0+6)+","+($(this)[0]['cx']['animVal']['value']+3.5)+","+(0-2.5));

              svg.append("polyline")
                .attr("class", "tipDot")
                .style("fill", "black")
                .attr("points", ($(this)[0]['cx']['animVal']['value']-3.5)+","+(y(0)+2.5)+","+$(this)[0]['cx']['animVal']['value']+","+(y(0)-6)+","+($(this)[0]['cx']['animVal']['value']+3.5)+","+(y(0)+2.5));

              $(this).tooltip({
                'container': 'body',
                'placement': 'left',
                'title': mainCate + ' | ' + d['num'],
                'trigger': 'hover'
              })
              .tooltip('show');
            })
            .on("mouseout",  function (d) {
              var currentX = $(this)[0]['cx']['animVal']['value'];

              d3.select(this).transition().duration(100).style("opacity", 0);

              var ret = $('.tipNetPoints').filter(function(index) {
                return ($(this)[0]['cx']['animVal']['value'] === currentX);
              });

              $.each(ret, function(index, val) {
                $(val).animate({
                  opacity: "0"
                }, 100);

                $(val).tooltip('destroy');
              });

              d3.selectAll('.tipDot').transition().duration(100).remove();

              $(this).tooltip('destroy');
            });

        this.getOpt = function() {
          var axisOpt = new Object();
          axisOpt['x'] = x;
          axisOpt['y'] = y;
          axisOpt['xAxis'] = xAxis;
          axisOpt['width'] = width;
          axisOpt['height'] = height;

          return axisOpt;
        }

        this.getSvg = function() {
          var svgD = new Object();
          svgD['svg'] = svg;
          svgD['points'] = points;
          svgD['area'] = area;
          svgD['path'] = path;
          svgD['legendColor'] = legendColor;

          return svgD;
        }
      }

      //end generate function

      //redraw function
      function redraw(data, category,id, x, y, xAxis, svg, area, path, points, legendColor, height, axisNum) {
        //format of time data
        var parseDate = d3.time.format("%H:%M").parse;

        var ddata = (function() {
          var temp = {}, seriesArr = [];

          category.forEach(function (name) {
            temp[name] = {category: name, values:[]};
            seriesArr.push(temp[name]);
          });

          data.forEach(function (d) {
            category.map(function (name) {
              temp[name].values.push({'category': name, 'time': parseDate(d['time']), 'num': d[name]});
            });
          });

          return seriesArr;
        })();

        x.domain( d3.extent(data, function(d) { return parseDate(d['time']); }) );
        xAxis.ticks(d3.time.minutes, Math.floor(data.length / axisNum));

//        svg.select("#net-x-axis")
//            .transition()
//            .duration(200)
//            .ease("sin-in-out")
//            .call(xAxis);

        //different area line updating

        path.select("path")
            .data(ddata)
            .transition()
            .duration(200)
            .attr("d", function(d) { return area(d['values']); })
            .attr("class", function(d) {
              if (d['category'] === 'upload')
                return 'areaU';
              else
                return 'areaD';
            });

        //circle updating

        points.selectAll(".tipNetPoints")
            .data(function (d) { return d['values']; })
            .attr("class", "tipNetPoints")
            .attr("cx", function (d) { return x(d['time']); })
            .attr("cy", function (d) { return y(d['num']); })
            .attr("r", "6px")
            .style("fill",function (d) { return legendColor[d['category']]; });

        // //draw new dot

        // console.log(ddata);
        points.data(ddata);

        points.selectAll(".tipNetPoints")
            .data(function (d) {
              return d['values'];
            })
            .enter().append("circle")
            .attr("class", "tipNetPoints")
            .attr("cx", function (d) { return x(d['time']); })
            .attr("cy", function (d) { return y(d['num']); })
            .text(function (d) { return d['num']; })
            .attr("r", "6px")
            .style("fill",function (d) { return legendColor[d['category']]; })
            .on("mouseover", function (d) {
              // console.log();
              var currentX = $(this)[0]['cx']['animVal']['value'],
                  currentY = $(this)[0]['cy']['animVal']['value'];

              d3.select(this).transition().duration(100).style("opacity", 1);

              var ret = $('.tipNetPoints').filter(function(index) {
                return ($(this)[0]['cx']['animVal']['value'] === currentX && $(this)[0]['cy']['animVal']['value'] !== currentY);
              });

              //to adjust tooltip'x content if upload and download data are the same
              var jud = ret.length;

              var mainCate = (function() {
                if (jud === 0)
                  return 'upload/download';
                else
                  return d['category'];
              })();

              var viceCate = (function() {
                if (category[0] === d['category'])
                  return category[1];
                else
                  return category[0];
              })();

              $.each(ret, function(index, val) {
                $(val).animate({
                  opacity: "1"
                }, 100);

                $(val).tooltip({
                  'container': 'body',
                  'placement': 'left',
                  'title': viceCate + ' | ' + $(this)[0]['textContent'],
                  'trigger': 'hover'
                })
                    .tooltip('show');
              });

              // console.log("the correct xaxis is: ", currentX, 'but the output is ', x(d['time']), '$this object is: ', $(this)[0]['cx']['animVal']['value']);

              svg.append("g")
                  .attr("class", "tipDot")
                  .append("line")
                  .attr("class", "tipDot")
                  .transition()
                  .duration(50)
                  .attr("x1", $(this)[0]['cx']['animVal']['value'])
                  .attr("x2", $(this)[0]['cx']['animVal']['value'])
                  .attr("y2", height);

              svg.append("polyline")
                  .attr("class", "tipDot")
                  .style("fill", "black")
                  .attr("points", ($(this)[0]['cx']['animVal']['value']-3.5)+","+(0-2.5)+","+$(this)[0]['cx']['animVal']['value']+","+(0+6)+","+($(this)[0]['cx']['animVal']['value']+3.5)+","+(0-2.5));

              svg.append("polyline")
                  .attr("class", "tipDot")
                  .style("fill", "black")
                  .attr("points", ($(this)[0]['cx']['animVal']['value']-3.5)+","+(y(0)+2.5)+","+$(this)[0]['cx']['animVal']['value']+","+(y(0)-6)+","+($(this)[0]['cx']['animVal']['value']+3.5)+","+(y(0)+2.5));

              $(this).tooltip({
                'container': 'body',
                'placement': 'left',
                'title': mainCate + ' | ' + d['num'],
                'trigger': 'hover'
              })
                  .tooltip('show');
            })
            .on("mouseout",  function (d) {
              var currentX = $(this)[0]['cx']['animVal']['value'];

              d3.select(this).transition().duration(100).style("opacity", 0);

              var ret = $('.tipNetPoints').filter(function(index) {
                return ($(this)[0]['cx']['animVal']['value'] === currentX);
              });

              $.each(ret, function(index, val) {
                $(val).animate({
                  opacity: "0"
                }, 100);

                $(val).tooltip('destroy');
              });

              d3.selectAll('.tipDot').transition().duration(100).remove();

              $(this).tooltip('destroy');
            });

        //remove old dot
        points.selectAll(".tipNetPoints")
            .data(function (d) { return d['values']; })
            .exit()
            .transition()
            .duration(200)
            .remove();

      }

      //end redraw function
      return tsareachart;
}();