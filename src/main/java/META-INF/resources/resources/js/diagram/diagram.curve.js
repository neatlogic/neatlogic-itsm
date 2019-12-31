Tsdiagram.curve = Tsdiagram.Figures.extend({
    init: function (container, diagramData, opts) {

        var that = this;
        this._super(container, diagramData, opts);
        this.svg.classed('curve', true);
        // padding
        if (this.diagramData.legend && this.diagramData.legend.position === 'top') {
            this.options.padding[0] = 30;
        } else if (this.diagramData.legend && this.diagramData.legend.data) {
            this.spadl = this.options.padding[3];
            this.options.padding[3] = this.options.padding[3] + this.options.flexlegendw;
        } else {
            this.options.padding[1] = this.options.padding[3];
        }
        if (this.diagramData.hasOwnProperty('title') && this.diagramData.title.name) {
            this.options.padding[0] = 30;
        }
        if (this.diagramData.xAxis && this.diagramData.xAxis.fill) {
            this.fill = true;
        }
        if (this.diagramData.xAxis && this.diagramData.xAxis.bar) {
            this.bar = true;
        }
        if (this.diagramData.legend && this.diagramData.legend.show) {
            this.legendShow = true;
        }
        if (this.diagramData.xAxis && this.diagramData.xAxis.break) {
            this.break = true;
        }
        if (that.diagramData.legend && that.diagramData.legend.key) {
            this.legendKey = that.diagramData.legend.key;
        }
        if (that.diagramData.legend && that.diagramData.legend.adaption) {
            this.legendAdaption = true;
        }
        if (that.diagramData.legend && that.diagramData.legend.sort) {
            this.legendSort = that.diagramData.legend.sort;
        }
        // aixs
        this.xAxis();
        this.yAxis();
        // legend
        this.getColor();
        // condition curve
        this.dividedLine();
        this.barbgFn();

        that.drawCurve();
        if (this.diagramData.xAxis && this.diagramData.xAxis.point) {
            this.drawPoint();
        }
        if (this.diagramData.xAxis && this.diagramData.xAxis.brush) {
            this.brush();
        }
        if (this.diagramData.legend && this.diagramData.legend.position === 'top') {
            this.formatTime = d3.timeFormat('%Y-%m-%d');
            this.legendTopFn(this.legend);
        } else if (this.diagramData.legend && this.diagramData.legend.data) {
            this.legendFn(this.legend);
        }
        if (this.diagramData.hasOwnProperty('title')) {
            this.title(this.diagramData.title);
        }
        this.svg
            .on('mousemove', function () {
                that.auxiliaryline();
            });
        d3.select(this.container)
            .on('mousemove', function () {
                that.auxiliaryline();
            })
            .on('mouseout', function () {
                that.auxiliaryline();
            })
            .on('mouseleave', function () {
                that.auxiliaryline();
            });

        d3.select(this.container).on('mousemove', function () {
            that.hidedragarrow();
        });
        if (this.diagramData.legend && this.diagramData.legend.show) {
            // this.legendopen()
        } else {
            this.legendclose();
        }
        this.hidedragarrow();
        if (this.fill) {
            this.appendDefs();
        }
        this.adjustment();

    },
    appendDefs: function () {
        this.defs = this.svg.append('defs');
        var colors = this.defs.selectAll('.color').data(this.options.color);
        var enter = colors.enter().append('linearGradient')
            .attr('id', function (d, i) {
                return 'clr' + i;
            })
            .attr('x1', '0%')
            .attr('x2', '0%')
            .attr('y1', '0%')
            .attr('y2', '100%')
            .html(function (d) {
                return `<stop offset="0%" stop-color="${d}" stop-opacity="0.4"></stop><stop offset="100%" stop-color="${d}" stop-opacity="0"></stop>`;
            });
    },
    drawPoint: function () {
        var that = this;
        if (that.diagramData.xAxis && !that.diagramData.xAxis.point) return false;
        var pointG = that.graphG.selectAll('.pointG').data(this.seriesData);
        var pointenterG = pointG.enter().append('g').attr('class', 'pointG');
        pointG.exit().remove();
        pointG = pointG.merge(pointenterG);

        pointG.each(function (d, i) {
            var G = d3.select(this);
            var color = that.legendKey === 'index' ? that.legend[i] : that.legend.find(function (d1) {
                return d1.name === d.name;
            });
            var point = G.selectAll('circle.point').data(d.data);
            var pointEnter = point.enter().append('circle').attr('class', `point clr${color.index}`);
            point.exit().remove();
            point = point.merge(pointEnter);
            point
                .attr('r', 3)
                .attr('cx', function (d) {
                    if (that.xAxisType === 'category') {
                        if (typeof d[0] === 'string') return that.xScale(d[0]) + that.interval / 2;
                        return d[0];
                    }
                    else {
                        return that.xScale(d[0]);
                    }
                })
                .attr('cy', function (d) {
                    return that.yScale(d[1]);
                })
                .attr('fill', color.color);
            var thresholdArr = d.data.filter(function (d1) {
                return d1[1] >= color.threshold && color.threshold;
            });
            var thresholdPoint = G.selectAll('g.threshold').data(thresholdArr);
            var thresholdPointEnter = thresholdPoint.enter().append('g').attr('class', 'threshold');
            thresholdPoint.exit().remove();
            thresholdPoint = thresholdPoint.merge(thresholdPointEnter);
            thresholdPoint.attr('transform', function (d1) {
                return `translate(${that.xScale(d1[0])},${that.yScale(d1[1])})`;
            });

            thresholdPointEnter
                .append('circle')
                .attr('class', `clr${color.index}`)
                .attr('r', 6)
                .attr('fill', color.color)
                .style('stroke', 'none')
                .attr('fill-opacity', 0.4);

            thresholdPointEnter
                .append('circle')
                .attr('class', `clr${color.index}`)
                .attr('r', 9)
                .attr('fill', color.color)
                .style('stroke', 'none')
                .attr('fill-opacity', 0.2);
            // 添加阈值线
            if (color.threshold && color.threshold <= d.max) {
                var thresholdLine = G.selectAll('line.thresholdLine').data([color.threshold]);
                var thresholdLineEnter = thresholdLine.enter().append('line').attr('class', `thresholdLine clr${color.index}`);//.lower();
                thresholdLine.exit().remove();
                thresholdLine = thresholdLine.merge(thresholdLineEnter);
                thresholdLine
                    .attr('x1', that.options.padding[3])
                    .attr('x2', that.conWidth - that.options.padding[1])
                    .attr('y1', that.yScale(color.threshold))
                    .attr('y2', that.yScale(color.threshold))
                    .attr('stroke', color.color)
                    .attr('stroke-width', 0.5)
                    .attr('stroke-dasharray', '3 4');
            }

        });
    },
    drawCurve: function (tsti) {
        tsti = tsti ? tsti : 700;
        var that = this;
        if (!this.line) {
            // line
            this.line = d3.line()
                .x(function (d, i) {
                    if (that.xAxisType === 'category') {
                        if (typeof d[0] === 'string') return that.xScale(d[0]) + that.interval / 2;
                        return d[0];
                    }
                    else {
                        return that.xScale(d[0]);
                    }
                })
                .y(function (d) {
                    return that.yScale(d[1]);
                });
            if (this.diagramData.xAxis && this.diagramData.xAxis.curve) {
                this.line.curve(d3.curveCatmullRom.alpha(0.5));
            }
            // area
            this.area = d3.area()
                .x(function (d, i) {
                    return that.xScale(d[0]);
                })
                .y0(function (d) {
                    return that.yScale(0);
                })
                .y1(function (d) {
                    return that.yScale(d[1]);
                });
            if (this.diagramData.xAxis && this.diagramData.xAxis.curve) {
                this.area.curve(d3.curveCatmullRom.alpha(0.5));
            }
        }
        var curveG = that.graphG.selectAll('.curveG').data(this.dividePoints);
        var curveenter = curveG.enter().append('g').attr('class', 'curveG');
        curveG.exit().remove();
        var curves = curveG.merge(curveenter).attr('stroke', function (d, i) {
            var curveName = that.diagramData.series[i];
            d[0].name = curveName.name;
            var color = that.legendKey === 'index' ? that.legend[i] : that.legend.find(function (d, i1) {
                return d.name === curveName.name;
            });
            d3.select(this).classed('clr' + color.index, true).attr('fill', 'url(#clr' + color.index + ')');
            return color.color ? color.color : 'black';
        })
            .each(function (d) {
                var path = d3.select(this).selectAll('path.path').data(d);
                var enter = path.enter().append('path').attr('class', 'path');
                path.merge(enter)
                    .attr('d', function (d) {
                        d3.select(this).classed('k', false).classed('fk', false);
                        d3.select(this).classed(d.key, true);
                        if (d.key === 'k') {
                            if (d.points[0][1] === '' || d.points[d.points.length - 1][1] === '' || that.break) return '';
                            return that.line([d.points[0], d.points[d.points.length - 1]]);
                        }
                        return that.line(d.points);
                    });
                path.exit().remove();
                if (!that.fill) return;
                var area = d3.select(this).selectAll('path.area').data(d);
                var areaenter = path.enter().append('path').attr('class', 'area');
                area.merge(areaenter)
                    .attr('d', function (d) {
                        d3.select(this).classed('k', false).classed('fk', false);
                        d3.select(this).classed(d.key, true);
                        if (d.key === 'k') {
                            if (!d.points[0][1] && !d.points[d.points.length - 1][1]) return '';
                            return '';
                            //return that.area([d.points[0], d.points[d.points.length - 1]]);
                        }
                        return that.area(d.points);
                    });
                area.exit().remove();
            })
            .on('click', function (d, i) {
                if (!that.legendDiv) return false;
                var curr = this;
                d3.select(curr).attr('opacity', 1).classed('active1', true);
                d3.event.stopPropagation();
                d3.event.preventDefault();
                curves.filter(function () {
                    return this !== curr;
                }).attr('opacity', 0.2).classed('active1', false);
                var pathData = d3.select(this).datum();
                var items = that.legendDiv.selectAll('div.item');
                var curritem = items.filter(function (d1,i1) {
                    if (that.legendKey === 'index') return d3.select(this).attr('key') == i;
                    var name = d3.select(this).select('.tit').text();
                    return name === pathData[0].name;
                });
                curritem.style('opacity', 1);
                var other = items.filter(function () {
                    return this !== curritem.node();
                });
                other.style('opacity', 0.2);
                return false;
            });
    },
    dragupdate: function () {
        var that = this;
        this._super();
        that.drawCurve();
        that.drawPoint();
    },
    tooltipFn: function (x, index, activename, y) {
        this._super(x, index, activename, y);
        this.tooltip.selectAll('.item').each(function (d) {
            var colorblock = d3.select(this).select('.colorblock');
            if (colorblock.size()) {
                var clrindex = colorblock.attr('clr');
                colorblock.classed(clrindex, true);
            }
        });
    },
    legendFn: function (legendData) {
        var that = this;
        this._super(legendData);
        this.legendDiv.selectAll('.item').each(function (d) {
            var colorblock = d3.select(this).select('.colorblock');
            var clrindex = colorblock.attr('clr');
            colorblock.classed(clrindex, true);
        });
        this.legendDiv.on('click', function (d, i) {
            if (this === d3.event.target) return;
            var curritem = that.getparents(d3.event.target, 'item');
            if (!curritem) return false;
            var currname = d3.select(curritem).select('div.tit').text();
            var itemIndex = that.legendDiv.selectAll('.item').nodes().indexOf(curritem);
            var currcurve = that.graphG.selectAll('.curveG').filter(function (d1, i1) {
                if (that.legendKey === 'index') return i1 == d3.select(curritem).attr('key');
                var name = d1[0].name;
                return name === currname;
            });
            var othercurve = that.graphG.selectAll('.curveG').filter(function (d) {
                return this !== currcurve.node();
            });
            var otheritem = that.legendDiv.selectAll('.item').filter(function () {
                return this !== curritem;
            });
            var isshadow = otheritem.nodes().some(function (ele) {
                return parseFloat(d3.select(ele).style('opacity')) < 1;
            });
            if (isshadow && parseFloat(d3.select(curritem).style('opacity')) === 1) {
                otheritem.style('opacity', 1);
                othercurve.attr('opacity', 1);
            }
            else {
                otheritem.style('opacity', 0.2);
                othercurve.attr('opacity', 0.1).classed('active1', false);
            }
            currcurve.attr('opacity', 1).classed('active1', true);//.raise();
            d3.select(curritem).style('opacity', 1);
        });
    },
    legendTopFn: function (legendData) {
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
                .attr('y', -1.5)
                .attr('width', 10)
                .attr('height', 3)
                .attr('fill', function (d) {
                    return d.color;
                })
                .each(function (d) {
                    // if (d.type.indexOf('all') !== -1) d3.select(this).classed('fillNone', true).attr('stroke', d.color)
                    if (d.type.indexOf('line') !== -1) d3.select(this).attr('height', 2).attr('y', -1);
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
        this._super();
        this.dragupdate();
    },
    redraw: function (redata) {
        this.diagramData = redata;
        this.xAxis();
        this.yAxis();
        this.dealSeries();
        this.getColor();
        // return
        this.dividedLine();
        this.dragupdate();
        if (this.diagramData.legend && this.diagramData.legend.position === 'top') {
            this.legendTopFn(this.legend);
        } else if (this.diagramData.legend && this.diagramData.legend.data) {
            this.legendFn(this.legend);
        }
        this.adjustment();
    },
});