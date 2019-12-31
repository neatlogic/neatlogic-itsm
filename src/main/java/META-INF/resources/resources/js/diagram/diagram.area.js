Tsdiagram.area = Tsdiagram.Figures.extend({
    init: function (container, diagramData, opts) {
        var that = this;
        this._super(container, diagramData, opts);

        // padding
        if (this.diagramData.legend && this.diagramData.legend.data) {
            // this.options.padding[1] = this.options.padding[3];
            this.spadl = this.options.padding[3] + 10;
            this.options.padding[3] = this.options.padding[3] + this.options.flexlegendw;
        } else {
            this.options.padding[1] = this.options.padding[3];
        }
        if (this.diagramData.hasOwnProperty('title') && this.diagramData.title.name) {
            this.options.padding[0] = 30;
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

        this.svg.classed('area', true);
        // axis
        this.xAxis();
        this.yAxis();

        // legend data
        this.getColor();
        this.barbgFn();
        this.dividedLine();
        this.drawArea(700);

        this.brush();
        if (this.diagramData.legend && this.diagramData.legend.data) {
            this.legendFn(this.legend);
        }
        if (this.diagramData.hasOwnProperty('title')) {
            this.title(this.diagramData.title);
        }
        this.svg
            .on('mousemove', function () {
                that.auxiliaryline();
            })
            .on('mouseout', function () {
                that.auxiliaryline();
            });

        if (this.diagramData.legend && this.diagramData.legend.show) {
            this.legendopen();
        } else {
            this.legendclose();
        }

        d3.select(this.container).on('mousemove', function () {
            that.hidedragarrow();
        });
        that.hidedragarrow();
        this.adjustment(700);
    },
    scale: function (type, domain, range) {
        if (type === 'category') {
            var scale = d3.scalePoint().domain(domain).range(range);
            return scale;
        }
        else return this._super(type, domain, range);
    },
    yAxis: function () {
        var that = this;
        this.yScaleArr = [];
        this.yAxisLArr = [];
        this._super('scale');
        this.seriesData.forEach(function (d) {
            if (d.data.length > that.diagramData.xAxis.data.length && !that.seriesStyle) d.data.splice(that.diagramData.xAxis.data.length);
            if (!d || !d.data.length) return false;
            if (d.type.indexOf('Compare') !== -1) {
                var domain = d.rangey ? d.rangey : [d.min, d.max];
                var ticks = d3.ticks(domain[0], domain[1], 2);
                if (ticks[ticks.length - 1] < domain[1]) {
                    domain[1] = ticks[ticks.length - 1] + ticks[1] - ticks[0];
                    ticks.push(ticks[ticks.length - 1] + ticks[1] - ticks[0]);
                }
                var count = d.data[0][3];
                var interval = (that.conHeight - that.options.padding[0] - that.options.padding[2] - that.options.areaComparePadding * (count - 1)) / count;
                var bottom = that.conHeight - that.options.padding[2] - (that.options.areaComparePadding + interval) * d.data[0][2];
                var range = [bottom, bottom - interval];
                var scale = that.scale('value', domain, range);
                that.yScaleArr.push(scale);
                var yAxisL = d3.axisLeft(scale).tickSizeInner([-that.conWidth + that.options.padding[3] + that.options.padding[1]])
                    .tickPadding([that.options.tickPadding]).ticks(2);
                that.yAxisLArr.push(yAxisL);
            }
        });
        if (this.yAxisLArr.length) {
            this.yAxisLG = that.svg.selectAll('.yAxis').data(this.yAxisLArr);
            var enter = this.yAxisLG.enter().append('g').attr('class', 'yAxis L y').lower();
            this.yAxisLG = this.yAxisLG.merge(enter).attr('transform', `translate(${this.options.padding[3]}, 0)`)
                .each(function (d) {
                    d3.select(this).call(d);
                });
        } else {
            this._super();
        }
    },
    drawArea: function (tsti) {
        tsti = tsti !== undefined ? tsti : 700;
        var that = this;
        if (!this.area) {
            this.area = d3.area()
                .x(function (d, i) {
                    if (that.seriesStyle) return that.xScale(d[0]);
                    return that.xScale(that.domainx[i]);
                })
                .y0(function (d) {
                    if (that.seriesStyle) return that.yScale(d[1]);
                    if (d.length === 4) return that.yScaleArr[d[2]](d[0]);
                    return that.yScale(d[0]);
                })
                .y1(function (d) {
                    if (that.seriesStyle) return that.yScale(d[2]);
                    if (d.length === 4) return that.yScaleArr[d[2]](d[1]);
                    return that.yScale(d[1]);
                });
            if (this.diagramData.xAxis && this.diagramData.xAxis.curve) {
                this.area.curve(d3.curveCatmullRom.alpha(0.5));
            }
            this.line0 = d3.line()
                .x(function (d, i) {
                    // console.log(that.seriesStyle,new Date(d[0]), that.xScale(new Date(d[0])))/
                    if (that.seriesStyle) return that.xScale(d[0]);
                    return that.xScale(that.domainx[i]);
                })
                .y(function (d) {
                    if (that.seriesStyle) return that.yScale(d[1]);
                    if (d.length === 4) return that.yScaleArr[d[2]](d[0]);
                    return that.yScale(d[0]);
                });
            if (this.diagramData.xAxis && this.diagramData.xAxis.curve) {
                this.line0.curve(d3.curveCatmullRom.alpha(0.5));
            }
        }
        if (!this.area1) {
            this.area1 = d3.area()
                .x(function (d, i) {
                    if (that.seriesStyle) return that.xScale(d[0]);
                    return that.xScale(that.domainx[i]);
                })
                .y(function (d) {
                    if (that.seriesStyle) return that.yScale(d[1]);
                    if (d.length === 4) return that.yScaleArr[d[2]](d[0]);
                    return that.yScale(d[0]);
                });

            if (this.diagramData.xAxis && this.diagramData.xAxis.curve) {
                this.area1.curve(d3.curveCatmullRom.alpha(0.5));
            }
            this.line1 = d3.line()
                .x(function (d, i) {
                    if (that.seriesStyle) return that.xScale(d[0]);
                    return that.xScale(that.domainx[i]);
                })
                .y(function (d) {
                    if (that.seriesStyle) return that.yScale(d[2]);
                    if (d.length === 4) return that.yScaleArr[d[2]](d[1]);
                    return that.yScale(d[1]);
                });
            if (this.diagramData.xAxis && this.diagramData.xAxis.curve) {
                this.line1.curve(d3.curveCatmullRom.alpha(0.5));
            }
        }
        var areas = that.graphG.selectAll('g.areaG').data(['areaG']);
        var areassenter = areas.enter().append('g')
            .attr('class', 'areaG');

        var paths = areas.merge(areassenter).selectAll('.pathG').data(that.dividePoints);
        var pathsenter = paths.enter().append('g').attr('class', 'pathG');
        paths.exit().remove();
        var line = areas.merge(areassenter).selectAll('.lineG').data(that.dividePoints);
        var lineenter = paths.enter().append('g').attr('class', 'lineG');
        line.exit().remove();
        paths.merge(pathsenter)
            .each(function (d) {
                var path = d3.select(this).selectAll('path').data(d);
                var pathenter = path.enter().append('path');
                path.exit().remove();
                path.merge(pathenter).attr('d', function (d) {
                    if (d.key === 'k') return '';
                    return that.area1(d.points);
                })
                    .transition()
                    .duration(tsti)
                    .attr('d', function (d) {
                        if (d.key === 'k') return '';
                        return that.area(d.points);
                    })
                    .attr('fill', function (d) {
                        var color = that.legend.find(function (dd) {
                            return dd.name === d.name;
                        });
                        d3.select(this).classed('clr' + color.index, true);
                        return color.color ? color.color : 'black';
                    });
            });
        line.merge(lineenter)
            .each(function (d) {
                var path = d3.select(this).selectAll('path').data(d);
                var pathenter = path.enter().append('path').style('fill', 'none');
                path.exit().remove();
                path.merge(pathenter)
                    .attr('d', function (d) {
                        if (d.key === 'k') return '';
                        return that.line0(d.points);
                    })
                    .transition()
                    .duration(tsti)
                    .attr('d', function (d) {
                        if (d.key === 'k') return '';
                        return that.line1(d.points);
                    })
                    .attr('stroke', function (d) {
                        var color = that.legend.find(function (dd) {
                            return dd.name === d.name;
                        });
                        d3.select(this).classed('clr' + color.index, true);
                        return color.color ? color.color : 'black';
                    });
            });

    },
    drawCompareArea: function () {

    },
    dragupdate: function (tsti) {
        var that = this;
        this._super();
        that.drawArea(tsti);
    },
    resize: function (tsti) {
        this._super();
        this.dragupdate(0);
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
        if (this.diagramData.legend && this.diagramData.legend.data) {
            this.legendFn(this.legend);
        }
        this.adjustment();
    },

});