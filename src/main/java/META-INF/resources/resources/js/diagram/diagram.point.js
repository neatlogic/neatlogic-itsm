Tsdiagram.point = Tsdiagram.Figures.extend({
    init: function (container, diagramData, opts) {
        var that = this;
        this._super(container, diagramData, opts);
        // padding
        this.options.padding[0] = this.options.padding[2];
        // svg scatter
        this.svg.classed('scatter', true);
        this.xAxis();
        this.yAxis();
        this.dealSeries();
        this.getColor();
        this.legendFn2(this.legend);
        that.drawScatter();
        // 调整刻度值padding
        // 隐藏箭头
        this.hidedragarrow();
        // 隐藏箭头
        d3.select(this.container).on('mousemove', function () {
            that.hidedragarrow();
        });
        this.adjustment();
    },
    drawScatter: function (dividePoints, curveName) {
        var that = this;
        // this.scatterData = this.dividePoints;
        this.scatterG = that.graphG.selectAll('g.scatterG').data(this.seriesData);
        var scatterenter = this.scatterG.enter().append('g')
            .attr('class', 'scatterG');
        this.scatterG.exit().remove();
        this.scatterG = this.scatterG.merge(scatterenter).each(function (d) {
            var fd = d.data.filter(function (d) {
                return d[1] !== '';
            });
            var pointsG = d3.select(this).selectAll('g.pointsG').data(fd);
            var pointsGenter = pointsG.enter().append('g').attr('class', 'pointsG');
            pointsG.exit().remove();
            pointsG.merge(pointsGenter)
                .attr('fill', 'transparent')
                .attr('stroke-width', 2)
                .attr('stroke', function () {
                    var color = that.legend.find(function (d1) {
                        return d.name === d1.name;
                    });
                    d3.select(this).classed('clr' + color.index, true);
                    if (color) return color.color;
                    return 'black';
                })
                .attr('cursor', 'pointer')
                .each(function (d1) {
                    var circles = d3.select(this).selectAll('circle').data([d1]);
                    var circlesenter = circles.enter().append('circle');
                    circles.exit().remove();
                    circles.merge(circlesenter)
                        .attr('cx', function (d2) {
                            return that.xScale(d2[0]);
                        })
                        .attr('cy', function (d1) {
                            return that.yScale(d1[1]);
                        })
                        .attr('r', that.options.scatterRadius);
                });
            pointsG.merge(pointsGenter)
                .on('mouseover', function (d1) {
                    var currcircle = d3.select(this).selectAll('circle').attr('r', that.options.scatterRadius + 2);
                    var color = that.legend.find(function (d1) {
                        return d.name === d1.name;
                    });
                    if (!color) return false;
                    var time = that.formatTime(d1[0]);
                    if (time.indexOf('NaN') !== -1) time = d1[0];
                    var tooltipData = {
                        name: d.name,
                        value: d1[1],
                        time: time,
                        color: color.color,
                        index: color.index
                    };
                    if (that.tooltip) that.tooltip.style('display', 'inline');
                    that.tooltipFn(tooltipData);
                })
                .on('mouseout', function () {
                    var currcircle = d3.select(this).selectAll('circle').transition().duration(300).attr('r', that.options.scatterRadius);
                    if (that.tooltip) that.tooltip.style('display', 'none');
                });
        });
    },
    dragupdate: function (tsti) {
        var that = this;
        this._super();
        this.drawScatter();
    },
    tooltipFn: function (tooltipData) {
        var that = this;
        var mouse = d3.mouse(this.svg.node());
        this.tooltip = d3.select(this.container).selectAll('div.tooltip1').data(['tooltip1']);
        var tooltipenter = this.tooltip.enter().append('xhtml:div').attr('class', 'tooltip1 scatter');
        this.tooltip = this.tooltip.merge(tooltipenter).style('top', this.options.padding[0] + 10 + 'px')
            .html(function (d) {
                var str = '';
                [tooltipData].forEach(function (d) {
                    str += `<div class="item">${d.time}</div>`;
                    str += `<div class="item"><div class="colorblock ${'clr' + d.index}" style="background: ${d.color}"></div><div class="tit">${d.name}</div><div class="num">${d.value}</div></div>`;
                });
                return `<div class="legend3">${str}</div>`;
            });
        var tpw = this.tooltip.node().offsetWidth;
        var tph = this.tooltip.node().offsetHeight;
        var left = mouse[0] - tpw / 2 < that.options.padding[3] ? that.options.padding[3] : mouse[0] - tpw / 2;
        if (mouse[0] - tpw / 2 < this.options.padding[3]) left = this.options.padding[3] + this.spadl;
        if (mouse[0] + tpw / 2 > this.conWidth - this.options.padding[1]) left = this.conWidth - this.options.padding[1] - tpw;
        this.tooltip.style('left', left + 'px');
        this.tooltip.style('top', mouse[1] - tph - 10 + 'px');
        //this.tooltip.selectAll('.arrow').data(['arrow']).enter().append('xhtml:div').attr('class', 'arrow')
    },
    legendFn2: function (legendData) {
        var that = this;
        var colorTipL = 0;
        this.legendG = this.svg.selectAll('.legendG').data(['legendG']);
        var enter = this.legendG.enter().append('g').attr('class', 'legendG')
            .attr('transform', `translate(${this.options.padding[3]},${this.options.padding[0] / 2})`)
            .attr('font-size', '12px')
            .attr('dominant-baseline', 'middle');
        var colorLabel = enter
            .selectAll('.colorLabel')
            .data(legendData);
        var colorLabelEnter = colorLabel.enter()
            .append('g')
            .attr('class', 'colorLabel');
        colorLabelEnter.each(function (d) {
            d3.select(this).classed('clr' + d.index, true);
            d3.select(this).append('rect')
                .attr('x', colorTipL)
                .attr('y', -5)
                .attr('width', 10)
                .attr('height', 10)
                .attr('fill', function (d) {
                    return d.color;
                });

            var text = d3.select(this).append('text')
                .attr('x', colorTipL + 10 + 4)
                .attr('y', 1)
                .attr('fill', function (d) {
                    return d.color;
                })
                .text(function (d) {
                    if (d.name) return d.name;
                });
            var bbox = text.node().getBBox();
            colorTipL += (bbox.width + 10 + 16);
            that.legendG = enter.attr('transform', `translate(${that.conWidth - that.options.padding[1] + 16 - colorTipL},${that.options.pillarlegendheight1 / 2})`);
        });
        if (that.legendG.size()) {
            var legendbbox = this.legendG.node().getBBox();
            that.legendG.attr('transform', `translate(${that.conWidth - that.options.padding[1] - legendbbox.width},${that.options.pillarlegendheight1 / 2})`);
        }
        colorLabel.exit().remove();
    },
    resize: function () {
        var that = this;
        this._super();
        this.redraw(this.diagramData);
    },
    redraw: function (redata) {
        var that = this;
        if (!redata) return false;
        this.diagramData = redata;
        this.dealSeries();
        this.xAxis();
        this.yAxis();
        this.getColor();
        this.legendFn2(this.legend);
        this.drawScatter();
        this.adjustment();
    },
});