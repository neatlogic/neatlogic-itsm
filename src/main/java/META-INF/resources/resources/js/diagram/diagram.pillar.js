Tsdiagram.pillar = Tsdiagram.Figures.extend({
    init: function (container, diagramData, opts) {
        var that = this;
        this.diagramType = 'pillar';
        
        if (!this.pillarG) {
            this._super(container, diagramData, opts);
            this.svg.classed('pillar', true);
            this.pillarG = this.graphG.append('g').classed('pillarG', true);
            this.tsti = 500;

            this.seriesSort = this.diagramData.legend && this.diagramData.legend.sort || false;
            var last = JSON.parse(JSON.stringify(that.seriesData[that.seriesData.length - 1]));
            this.showTitle = (this.diagramData.legend && this.diagramData.legend.showTitle !="undefined")? this.diagramData.legend.showTitle: true;

            var isStack = that.seriesData.every(function (d) {
                return d.type === 'stackbar';
            });
            if (isStack && this.seriesSort) {
                if (this.diagramData.xAxis && Array.isArray(this.diagramData.xAxis.data)) {
                    this.diagramData.xAxis.data = sortByOtherArr(this.diagramData.xAxis.data, last.data);
                }
                if (this.diagramData.yAxis && Array.isArray(this.diagramData.yAxis.data)) {
                    this.diagramData.yAxis.data = sortByOtherArr(this.diagramData.yAxis.data, last.data);
                }
                this.seriesData.forEach(function (dd, i) {
                    diagramData.series[i].data = sortByOtherArr(diagramData.series[i].data, last.data);
                    dd.data = sortByOtherArr(dd.data, last.data);
                });
            }
        } else {
            this.dealSeries();
        }

        function sortByOtherArr (sortArr, otherArr) {
            var axisArr = sortArr.map(function (d, i) {
                return {
                    num: otherArr[i][1],
                    str: d
                }
            });
            return axisArr.sort(function (a, b) {
                if (that.seriesSort === 'asc') return a.num - b.num;
                else if (that.seriesSort === 'desc') return b.num - a.num;
                else return 0;
            }).map(function (d) {
                return d.str;
            });
        }


        if (this.diagramData.hasOwnProperty('title')) {
            this.title(this.diagramData.title);
        }
        if (this.diagramData.legend && this.diagramData.legend.data) {
            if (this.diagramData.legend.style === 'style1') {
                this.pillarStyle = 'style1';
                this.options.padding[0] = this.options.pillarlegendheight;
            } else {
                // delete this.diagramData.legend.data;
                this.options.padding[0] = this.options.pillarlegendheight1 + 14;
            }
        }
        if (that.diagramData.yAxis && Array.isArray(that.diagramData.yAxis.data)) {
            this.options.padding[0] = this.options.pillarlegendheight1;
        }
        if (this.conHeight < this.options.padding[0] + this.options.padding[2]) {
            this.conHeight = this.options.padding[0] + this.options.padding[2] + 10;
        }

        if (this.diagramData.xAxis && this.diagramData.xAxis.unit) this.axisUnit = this.diagramData.xAxis.unit;
        else if (this.diagramData.yAxis && this.diagramData.yAxis.unit) this.axisUnit = this.diagramData.yAxis.unit;

        this.xAxis();
        this.yAxis();
        this.adjTickx();
        this.getColor();

        var isStyle2 = this.seriesData.every(function (d) {
            var arr = d.type.split('-');
            return ['all', 'already', 'line'].includes(arr[1]);
        });
        if (isStyle2) this.pillarStyle = 'style2';
        if (this.pillarStyle === 'style2') {
            d3.select(this.container).classed('customclr', true);
            if (Array.isArray(this.legend)) {
                var line = this.legend.find(function (d) {
                    return d.type.indexOf('line') !== -1;
                });
                line.color = '#f7b437';
                var all = this.legend.find(function (d) {
                    return d.type.indexOf('all') !== -1;
                });
                all.color = '#e6f5ff';
                var already = this.legend.find(function (d) {
                    return d.type.indexOf('already') !== -1;
                });
                already.color = '#26a0da';
            }
        }
        if (that.diagramData.hasOwnProperty('legend') && that.diagramData.legend.data) {
            if (that.pillarStyle !== 'style1') {
                that.legendFn2(that.legend);
            }
        }
        if (!this.hoverbg) {
            this.hoverbg = this.svg.append('g').lower()
                .attr('class', 'hoverbg')
                .append('rect');
        }
        if (this.yAxisType === 'category') {
            this.hoverbg
                .attr('width', that.conWidth - that.options.padding[1] - that.options.padding[3])
                .attr('x', that.options.padding[3])
                .attr('class', 'trans');
        } else {
            this.hoverbg
                .attr('height', that.conHeight - that.options.padding[0] - that.options.padding[2])
                .attr('y', that.options.padding[0])
                .attr('class', 'trans');
        }

        this.drawPillar(this.tsti);

        this.dispatch = d3.dispatch('tooltip');
        this.dispatch.on('tooltip.a', function (hoverIndex) {
            var mousePos;
            if (hoverIndex !== undefined) {
                mousePos = that.bargroupArr[hoverIndex];
                if (mousePos) {
                    mousePos = [(mousePos[0] + mousePos[1]) / 2, that.options.padding[0] + 10];
                }
            } else mousePos = d3.mouse(that.svg.node());
            if (!mousePos) return false;
            if (!((mousePos[0] < that.options.padding[3] || mousePos[0] > that.conWidth - that.options.padding[1]) || (mousePos[1] < that.options.padding[0] || mousePos[1] > that.conHeight - that.options.padding[2]))) {
                var currIndex = 0;
                var hoversection = that.bargroupArr.map(function (d) {
                    return [(d[1] + d[0]) / 2 - (d[1] - d[0]) / 4, (d[1] + d[0]) / 2 + (d[1] - d[0]) / 4];
                });
                var section = hoversection.find(function (d, i) {
                    if (that.yAxisType === 'category') {
                        if (d[0] < mousePos[1] && mousePos[1] < d[1]) {
                            currIndex = i;
                            return true;
                        } else return false;
                    }
                    if (d[0] < mousePos[0] && mousePos[0] < d[1]) {
                        currIndex = i;
                        return true;
                    } else return false;
                });
                if (!section) return;
                if (section1 === section) return;
                else section1 = section;
                if (that.yAxisType === 'category') {
                    that.hoverbg
                        .attr('y', function () {
                            return section[0];
                        })
                        .attr('height', section[1] - section[0]);
                } else {
                    that.hoverbg
                        .attr('x', function () {
                            return section[0];
                        })
                        .attr('width', section[1] - section[0]);
                }

                if (Array.isArray(that.diagramData.series)) {
                    for (var i = that.diagramData.series.length - 1; i >= 0; i--) {
                        var legendc = that.legend.find(function (d) {
                            return d.name === that.diagramData.series[i].name;
                        });
                        legendc.value = that.diagramData.series[i].data[currIndex] instanceof Object ? that.diagramData.series[i].data[currIndex].value : that.diagramData.series[i].data[currIndex];
                    }
                }
                that.hoverIndex = currIndex;
                if (that.diagramData.hasOwnProperty('legend') && that.diagramData.legend.data && that.pillarStyle === 'style1') {
                    if (that.pillarStyle === 'style1') {
                        that.legendFn(that.diagramData.legend.data);
                    }
                } else {
                    that.tooltipFn(section[1], section[0]);
                }
            } else {
                section1 = [];
            }
        });
        var section1;
        this.svg
            .on('mousemove', function () {
                if (that.diagramData.hasOwnProperty('legend') && that.diagramData.legend.data && that.pillarStyle === 'style1') {
                    that.dispatch.call('tooltip');
                } else {
                    // if (that.yAxisType === 'category') return;
                    var mouse = d3.mouse(that.svg.node());
                    if (((mouse[0] < that.options.padding[3] || mouse[0] > that.conWidth - that.options.padding[1]) || (mouse[1] < that.options.padding[0] || mouse[1] > that.conHeight - that.options.padding[2]))) {
                        if (that.hoverbg) that.hoverbg.style('display', 'none');
                        if (that.tooltip) that.tooltip.style('display', 'none');
                    } else {
                        if (that.hoverbg) that.hoverbg.style('display', 'inline');
                        if (that.tooltip) that.tooltip.style('display', 'inline');
                        if (that.pillarBar === 'stackbar' || that.pillarStyle === 'style2') that.dispatch.call('tooltip');
                    }
                }
            });
        d3.select(this.container).on('mouseleave', function () {
            if (!that.diagramData.legend || !that.diagramData.legend.data || that.pillarStyle !== 'style1') {
                if (that.hoverbg) that.hoverbg.style('display', 'none');
                if (that.tooltip) that.tooltip.style('display', 'none');
            }
        });

        if (this.hoverIndex === undefined) this.hoverIndex = 0;
        if (this.diagramData.hasOwnProperty('legend') && this.diagramData.legend.data && that.pillarStyle === 'style1') {
            this.dispatch.call('tooltip', this, this.hoverIndex);
        }
        this.adjustment(500);
    },
    drawStyle2: function (tsti) {
        var that = this;

        var pillarData = this.seriesData.filter(function (d) {
            return d.type.indexOf('line') === -1;
        });
        var lineData = this.seriesData.filter(function (d) {
            return d.type.indexOf('line') !== -1;
        });
        var linePoints = [];

        var interval = this.xScale.bandwidth();
        var barStep = this.xScale.paddingInner();
        this.bargroupArr = this.diagramData.xAxis.data.map(function (d) {
            var start = that.xScale(d);
            return [start - barStep * interval, start + interval * (barStep + 1)];
        });
        // 柱宽
        var options = {};
        options.barInterval = interval * 0.6;
        if (options.barInterval > 50) options.barInterval = 50;
        var pillars = this.pillarG.selectAll('.pillar').data(pillarData);
        var pillarenters = pillars.enter()
            .append('g')
            .classed('pillar', true);
        var pillarExit = pillars.exit().remove();
        // 画柱
        pillars.merge(pillarenters).each(function (d, i) {
            // rect
            var rectG = d3.select(this);
            var rect = rectG.selectAll('rect').data(d.data);
            var rectenter = rect.enter().append('rect');
            var recteixt = rect.exit().remove();
            rect.merge(rectenter)
                .attr('transform', function (d, i) {
                    var currTicksPosx = that.xScale(that.diagramData.xAxis.data[i]);
                    return `translate(${currTicksPosx}, 0)`;
                })
                .attr('fill', function (d2) {
                    var color = that.legend.find(function (d2) {
                        return d2.name === d.name;
                    });
                    rectG.classed('clr' + color.index, true);
                    if (d.type.indexOf('all') !== -1) {
                        d3.select(this).classed('fillNone', false).attr('stroke', '#bcbcbc');
                        return '#e6e6e6'
                    }
                    return color.color;
                })
                .attr('stroke-width', function () {
                    if (d.type.indexOf('already') !== -1) {
                        return 0;
                    }
                    return 1;
                })
                .transition().duration(tsti)
                .attr('width', function (b) {
                    if (d.type.indexOf('already') !== -1) return options.barInterval - 2
                    return options.barInterval;
                })
                .attr('x', function (b) {
                    if (d.type.indexOf('already') !== -1) return interval / 2 - options.barInterval / 2 + 1
                    return interval / 2 - options.barInterval / 2;
                })
                .attr('y', function (b) {
                    return that.yScale(b[1]);
                })
                .attr('height', function (d2) {
                    if (d.type.indexOf('already') !== -1) return that.yScale(d2[0]) - that.yScale(d2[1]) - 1
                    return that.yScale(d2[0]) - that.yScale(d2[1]);
                });
        });
        // 画线
        lineData[0].data.forEach(function (d, i) {
            var currTicksPosx = that.xScale(that.diagramData.xAxis.data[i]);
            var step = interval / 2 - options.barInterval / 2;
            var arr1 = [currTicksPosx + step, d[1]];
            var arr2 = [currTicksPosx + options.barInterval + step, d[1]];
            linePoints.push([arr1, arr2]);
        });

        if (!this.line) {
            this.line = d3.line()
                .x(function (d, i) {
                    return d[0];
                })
                .y(function (d) {
                    return that.yScale(d[1]);
                });
        }
        var pillarsLine = this.pillarG.selectAll('g.pillar-line').data(linePoints);
        var pillarentersLine = pillarsLine.enter()
            .append('g')
            .classed('pillar-line', true);
        pillarentersLine.append('path')
            .attr('fill', 'none')
            .attr('stroke', function () {
                var color = that.legend.find(function (d2) {
                    return d2.name === lineData[0].name;
                });
                pillarentersLine.classed('clr' + color.index, true);
                return color.color;
            })
            .attr('stroke-width', '2');
        var pillarExitLine = pillars.exit().remove();
        // return `M${d1[0]},${that.yScale(d1[1])}L${d1[0] + d1[2]},${that.yScale(d1[1])}`;
        pillarsLine.merge(pillarentersLine).each(function (d1) {
            d3.select(this).selectAll('path').attr('d', function () {
                return that.line(d1)
            })
        });
    },
    drawLine: function () {
        console.log(111);
    },
    drawPillar: function (tsti) {
        var that = this;
        var interval;
        var barStep;
        if (this.xAxisType === 'category') {
            interval = this.xScale.bandwidth();
            barStep = this.xScale.paddingInner();
            this.bargroupArr = this.diagramData.xAxis.data.map(function (d) {
                var start = that.xScale(d);
                return [start - barStep * interval, start + interval * (barStep + 1)];
            });
        } else if (this.yAxisType === 'category') {
            interval = this.yScale.bandwidth();
            barStep = this.yScale.paddingInner();
            this.bargroupArr = this.diagramData.yAxis.data.map(function (d) {
                var start = that.yScale(d);
                return [start - barStep * interval, start + interval * (barStep + 1)];
            });
        }
        var options = this.diagramData.series.find(function (d) {
            return d.hasOwnProperty('itemStyle');
        });
        if (options) options = options.itemStyle;
        else options = {};
        options.tileBarWidth = options.tileBarWidth ? options.tileBarWidth : that.options.tileBarWidth;
        options.stackBarWidth = options.stackBarWidth ? options.stackBarWidth : that.options.stackBarWidth;
        options.barInterval = options.barInterval ? options.barInterval : that.options.barInterval;
        options.barInterval = this.percentTranDecimal(options.barInterval);
        if (that.pillarStyle !== 'style!') {
            options.barInterval = 0;
        }
        if (that.pillarStyle === 'style2') {
            this.drawStyle2();
            return false;
        }
        var pillars = this.pillarG.selectAll('.pillar').data(this.seriesData);
        var pillarenters = pillars.enter()
            .append('g')
            .classed('pillar', true);
        if (that.pillarStyle === 'style1') pillarenters.lower();

        var pillarexit = pillars.exit().remove();
        pillars.merge(pillarenters)
            .each(function (d, i, arr) {
                var rectG = d3.select(this).selectAll('g.rectG').data(d.data);
                var rectGenter = rectG.enter().append('g').attr('class', 'rectG');
                rectG.merge(rectGenter)
                    .each(function (d1, i1) {
                        if (Array.isArray(d1) && d1.length === 2) that.pillarBar = 'stackbar';
                        if (that.pillarStyle !== 'style1') {
                            if (Array.isArray(d1) && d1.length === 4) options.tileBarWidth = interval / d1[3];
                            else if (d1.length === 2) {
                                options.stackBarWidth = interval * 0.6;
                            }
                            if (options.tileBarWidth > 50) options.tileBarWidth = 50;
                            if (options.stackBarWidth > 50) options.stackBarWidth = 50;
                        }
                        var currTicksPosx;
                        if (that.diagramData.xAxis && that.diagramData.xAxis.data[i1]) {
                            currTicksPosx = that.xScale(that.diagramData.xAxis.data[i1]);
                        } else if (that.diagramData.yAxis && that.diagramData.yAxis.data[i1]) {
                            currTicksPosx = that.yScale(that.diagramData.yAxis.data[i1]);
                        }
                        // rect
                        var rectG = d3.select(this);
                        var rect = rectG.selectAll('rect').data([d1]);
                        var text = rectG.selectAll('text').data([d1]);
                        if (that.yAxisType === 'category') {
                            d3.select(this).attr('transform', `translate(0,${currTicksPosx})`);
                        } else {
                            d3.select(this).attr('transform', `translate(${currTicksPosx},0)`);
                        }
                        var rectenter = rect.enter().append('rect');
                        if (that.yAxisType === 'category') {
                            rect
                                .attr('fill', function (d2) {
                                    var color = that.legend.find(function (d2) {
                                        return d2.name === d.name;
                                    });
                                    rectG.classed('clr' + color.index, true);
                                    if (!Array.isArray(d2)) {
                                        if (d2.hasOwnProperty('itemStyle') && d2.itemStyle.color) {
                                            return d2.itemStyle.color;
                                        } else {
                                            return color.color;
                                        }
                                    } else {
                                        return color.color;
                                    }
                                })
                                .attr('rx', function (d) {
                                    if (that.pillarStyle === 'style1') {
                                        if (d.length === 2) return options.stackBarWidth / 2;
                                        if (d.length === 4) return options.tileBarWidth / 2;
                                    } else return 0;
                                })
                                .attr('ry', function (d) {
                                    if (that.pillarStyle === 'style1') {
                                        if (d.length === 2) return options.stackBarWidth / 2;
                                        if (d.length === 4) return options.tileBarWidth / 2;
                                    } else return 0;
                                })
                                .transition().duration(tsti)
                                .attr('height', function (b) {
                                    if (b.length === 4) {
                                        return Math.abs(options.tileBarWidth);
                                    }
                                    return Math.abs(options.stackBarWidth);
                                })
                                .attr('y', function (b) {
                                    if (b.length === 4) {
                                        var center = (b[3] * options.tileBarWidth + (b[3] - 1) * options.barInterval * interval) / 2;
                                        return interval / 2 - center + (options.tileBarWidth + options.barInterval * interval) * b[2];
                                    }
                                    return interval / 2 - options.stackBarWidth / 2;
                                })
                                .attr('x', function (b) {
                                    if (!Array.isArray(b)) {
                                        return that.xScale(b.value[1]);
                                    }
                                    if (b[1] < 0) return that.xScale(b[1]);
                                    return that.xScale(b[0]);
                                })
                                .attr('width', function (d2) {
                                    if (!Array.isArray(d2)) {
                                        return Math.abs(that.xScale(d2.value[1]) - that.xScale(d2.value[0]));
                                    }
                                    if (d2[0] !== 0) {
                                        if (that.pillarStyle === 'style1') return Math.abs(that.xScale(d2[1]) - that.xScale(d2[0]) + options.stackBarWidth / 2);
                                    }
                                    return Math.abs(that.xScale(d2[1]) - that.xScale(d2[0]));
                                });
                            rectenter
                                .attr('rx', function (d) {
                                    if (that.pillarStyle === 'style1') {
                                        if (d.length === 2) return options.stackBarWidth / 2;
                                        if (d.length === 4) return options.tileBarWidth / 2;
                                    } else return 0;
                                })
                                .attr('ry', function (d) {
                                    if (that.pillarStyle === 'style1') {
                                        if (d.length === 2) return options.stackBarWidth / 2;
                                        if (d.length === 4) return options.tileBarWidth / 2;
                                    } else return 0;
                                })
                                .attr('height', function (b) {
                                    if (b.length === 4) {
                                        return Math.abs(options.tileBarWidth);
                                    }
                                    return Math.abs(options.stackBarWidth);
                                })
                                .attr('y', function (b) {
                                    if (b.length === 4) {
                                        var center = (b[3] * options.tileBarWidth + (b[3] - 1) * options.barInterval * interval) / 2;
                                        return interval / 2 - center + (options.tileBarWidth + options.barInterval * interval) * b[2];
                                    }
                                    return interval / 2 - options.stackBarWidth / 2;
                                })
                                .attr('fill', function (d2) {
                                    var color = that.legend.find(function (d2) {
                                        return d2.name === d.name;
                                    });
                                    rectG.classed('clr' + color.index, true);
                                    if (!Array.isArray(d2)) {
                                        if (d2.hasOwnProperty('itemStyle') && d2.itemStyle.color) {
                                            return d2.itemStyle.color;
                                        } else {
                                            return color.color;
                                        }
                                    } else {
                                        return color.color;
                                    }
                                })
                                // .attr('fill-opacity', function () {
                                // 	var color = that.legend.find(function (d2) {
                                // 		return d2.name === d.name;
                                // 	});
                                // 	return color.opacity;
                                // })
                                .attr('x', function (b) {
                                    if (!Array.isArray(b)) {
                                        return that.xScale(b.value[1]);
                                    }
                                    return that.xScale(0);
                                })
                                .attr('width', function (d2) {
                                    if (!Array.isArray(d2)) {
                                        return Math.abs(that.xScale(d2.value[0]) - that.xScale(d2.value[1]));
                                    }
                                    if (d2[0] !== 0) return 0;
                                    return 0;
                                })
                                .transition()
                                .duration(tsti)
                                // .attr('y', function (b) {
                                //   if (!Array.isArray(b)) {
                                //     return that.xScale(b.value[1])
                                //   }
                                //   return that.xScale(b[0])
                                // })
                                .attr('x', function (b) {
                                    if (!Array.isArray(b)) {
                                        return that.xScale(b.value[1]);
                                    }
                                    return that.xScale(0);
                                })
                                .attr('width', function (d2) {
                                    if (!Array.isArray(d2)) {
                                        return Math.abs(that.xScale(d2.value[0]) - that.xScale(d2.value[1]));
                                    }
                                    if (d2[0] !== 0) {
                                        if (that.pillarStyle === 'style1') return Math.abs(that.xScale(d2[0]) - that.xScale(d2[1]) + options.stackBarWidth / 2);
                                    }
                                    return Math.abs(that.xScale(d2[1]) - that.xScale(d2[0]));
                                });
                            if (that.pillarStyle === 'style1') return false;
                            if (i !== arr.length - 1 && d1.length === 2) return false;
 
                            var textenter = text.enter().append('text')
                                .text(function (d1) {
                                    return (that.showTitle != false && d1[1]) ? ((/\d+\.\d{2,}/.test(d1[1]))?d1[1].toFixed(2):d1[1]) + (d.unit ? d.unit : '') : '';
                                }).attr('text-anchor', 'start');
                            text.merge(textenter).attr('x', function (d) {
                            	if ((that.xScale(d1[1]) + this.getBBox().width ) >= that.xScale(that.domainx[1]) + 12) return that.xScale(d1[1]) - this.getBBox().width - 3;
                                return that.xScale(d[1]) + 6;
                            }).attr('y', function (b) {
                                if (b.length === 4) {
                                    var center = (b[3] * options.tileBarWidth + (b[3] - 1) * options.barInterval * interval) / 2;
                                    return interval / 2 - center + (options.tileBarWidth + options.barInterval * interval) * b[2] + options.tileBarWidth / 2;
                                }
                                return interval / 2;
                            })
                                .attr('text-anchor', 'start')
                                .attr('dominant-baseline', 'middle');
                            return false;
                        }
                        rect
                            .attr('fill', function (d2) {
                                var color = that.legend.find(function (d2) {
                                    return d2.name === d.name;
                                });
                                rectG.classed('clr' + color.index, true);
                                if (!Array.isArray(d2)) {
                                    if (d2.hasOwnProperty('itemStyle') && d2.itemStyle.color) {
                                        return d2.itemStyle.color;
                                    } else {
                                        return color.color;
                                    }
                                } else {
                                    return color.color;
                                }
                            })
                            .transition().duration(tsti)
                            .attr('rx', function (d) {
                                if (that.pillarStyle === 'style1') {
                                    if (d.length === 2) return options.stackBarWidth / 2;
                                    if (d.length === 4) return options.tileBarWidth / 2;
                                } else return 0;

                            })
                            .attr('ry', function (d) {
                                if (that.pillarStyle === 'style1') {
                                    if (d.length === 2) return options.stackBarWidth / 2;
                                    if (d.length === 4) return options.tileBarWidth / 2;
                                } else return 0;
                            })
                            .attr('width', function (b) {
                                if (b.length === 4) {
                                    return options.tileBarWidth;
                                }
                                return options.stackBarWidth;
                            })
                            .attr('x', function (b) {
                                if (b.length === 4) {
                                    var center = (b[3] * options.tileBarWidth + (b[3] - 1) * options.barInterval * interval) / 2;
                                    return interval / 2 - center + (options.tileBarWidth + options.barInterval * interval) * b[2];
                                }
                                return interval / 2 - options.stackBarWidth / 2;
                            })
                            .attr('y', function (b) {
                                if (!Array.isArray(b)) {
                                    return that.yScale(b.value[1]);
                                }
                                if (b[1] < 0) return that.yScale(0);
                                return that.yScale(b[1]);
                            })
                            .attr('height', function (d2) {
                                if (!Array.isArray(d2)) {
                                    return Math.abs(that.yScale(d2.value[0]) - that.yScale(d2.value[1]));
                                }
                                if (d2[0] !== 0) {
                                    if (that.pillarStyle === 'style') return Math.abs(that.yScale(d2[0]) - that.yScale(d2[1]) + options.stackBarWidth / 2);
                                }
                                return Math.abs(that.yScale(d2[0]) - that.yScale(d2[1]));
                            });
                        rectenter
                            .attr('rx', function (d) {
                                if (that.pillarStyle === 'style1') {
                                    if (d.length === 2) return options.stackBarWidth / 2;
                                    if (d.length === 4) return options.tileBarWidth / 2;
                                } else return 0;
                            })
                            .attr('ry', function (d) {
                                if (that.pillarStyle === 'style1') {
                                    if (d.length === 2) return options.stackBarWidth / 2;
                                    if (d.length === 4) return options.tileBarWidth / 2;
                                } else return 0;
                            })
                            .attr('width', function (b) {
                                if (b.length === 4) {
                                    return Math.abs(options.tileBarWidth);
                                }
                                return Math.abs(options.stackBarWidth);
                            })
                            .attr('x', function (b) {
                                if (b.length === 4) {
                                    var center = (b[3] * options.tileBarWidth + (b[3] - 1) * options.barInterval * interval) / 2;
                                    return interval / 2 - center + (options.tileBarWidth + options.barInterval * interval) * b[2];
                                }
                                return interval / 2 - options.stackBarWidth / 2;
                            })
                            .attr('fill', function (d2) {
                                var color = that.legend.find(function (d2) {
                                    return d2.name === d.name;
                                });
                                rectG.classed('clr' + color.index, true);
                                if (!Array.isArray(d2)) {
                                    if (d2.hasOwnProperty('itemStyle') && d2.itemStyle.color) {
                                        return d2.itemStyle.color;
                                    } else {
                                        return color.color;
                                    }
                                } else {
                                    return color.color;
                                }
                            })
                            // .attr('fill-opacity', function () {
                            // 	var color = that.legend.find(function (d2) {
                            // 		return d2.name === d.name
                            // 	});
                            // 	return color.opacity;
                            // })
                            .attr('y', function (b) {
                                if (!Array.isArray(b)) {
                                    return that.yScale(b.value[1]);
                                }
                                return that.yScale(0);
                            })
                            .attr('height', function (d2) {
                                if (!Array.isArray(d2)) {
                                    return Math.abs(that.yScale(d2.value[0]) - that.yScale(d2.value[1]));
                                }
                                if (d2[0] !== 0) return 0;
                                return 0;
                            })
                            .transition()
                            .duration(tsti)
                            .attr('y', function (b) {
                                if (!Array.isArray(b)) {
                                    return that.yScale(b.value[1]);
                                }
                                if (b[1] < 0) return that.yScale(0);
                                return that.yScale(b[1]);
                            })
                            .attr('height', function (d2) {
                                if (!Array.isArray(d2)) {
                                    return Math.abs(that.yScale(d2.value[0]) - that.yScale(d2.value[1]));
                                }
                                if (d2[0] !== 0) {
                                    if (that.pillarStyle === 'style1') return Math.abs(that.yScale(d2[0]) - that.yScale(d2[1]) + options.stackBarWidth / 2);
                                }
                                return Math.abs(that.yScale(d2[0]) - that.yScale(d2[1]));
                            });
                        if (that.pillarStyle === 'style1') return false;
                        if (i !== arr.length - 1 && d1.length === 2) return false;
                        var textenter = text.enter().append('text');
                        text.merge(textenter).attr('y', function (d1) {
                            // if (that.yScale(d1[1]) <= that.yScale(that.domainy[1]) + 12) return that.yScale(d1[1]) + 15;
                        	
                            return that.yScale(d1[1]) - 6;
                        })
                            .attr('x', function (b) {
                                if (b.length === 4) {
                                    var center = (b[3] * options.tileBarWidth + (b[3] - 1) * options.barInterval * interval) / 2;
                                    return interval / 2 - center + (options.tileBarWidth + options.barInterval * interval) * b[2] + options.tileBarWidth / 2;
                                }
                                return interval / 2;
                            })
                            .text(function (d1) {
                            	
                                return (that.showTitle != false && d1[1])  ? ((/\d+\.\d{2,}/.test(d1[1]))?d1[1].toFixed(2):d1[1]) + (d.unit ? d.unit : '') : '';
                            })
                            .attr('text-anchor', 'middle');
                    });
            })
            .on('mouseover', function () {
                var target = d3.event.target;
                var currData = d3.select(this).datum();
                var currIndex = 0;
                for (var i = 0; i < this.children.length; i++) {
                    if (this.children[i] === target.parentNode) {
                        currIndex = i;
                        break;
                    }
                }
            });
    },
    tooltipFn: function (x, x1) {
        var that = this;
        this.tooltip = d3.select(this.container).selectAll('div.tooltip1').data(['tooltip1']);
        var tooltipenter = this.tooltip.enter().append('xhtml:div').attr('class', 'tooltip1');

        this.tooltip = this.tooltip.merge(tooltipenter)
            .html(function (d) {
                var str = '';
                that.legend.forEach(function (d, i) {
                    if (d.value === '') return false;
                    var color = that.legend.find(function (d1) {
                        return d1.name === d.name;
                    });
                    // if (color.type.indexOf('all') !== -1) {
                    //     str += `<div class="item"><div class="colorblock clr${color.index} fillNone" style="border: 2px solid ${color.color};"></div><div class="tit">${d.name}</div><div class="num">${d.value}</div></div>`
                    // } else
                    if (color.type.indexOf('line') !== -1) {
                        str += `<div class="item"><div class="colorblock clr${color.index}" style="background: ${color.color}; height: 4px;"></div><div class="tit">${d.name}</div><div class="num">${d.value}${d.unit ? d.unit : ''}</div></div>`;
                    } else str += `<div class="item"><div class="colorblock clr${color.index}" style="background: ${color.color}"></div><div class="tit">${d.name}</div><div class="num">${d.value}${d.unit ? d.unit : ''}</div></div>`;
                });
                return `<div class="legend3">${str}</div>`;
            });
        if (that.xAxisType === 'category') {
            this.tooltip
                .style('top', this.options.padding[0] + 10 + 'px')
                .style('left', x + 10 + 'px');
            var right = this.tooltip.node().offsetWidth;
            if (right + x > this.conWidth - this.options.padding[1]) {
                this.tooltip.style('left', x1 - right - 10 + 'px');
            }
        } else if (that.yAxisType === 'category') {
            this.tooltip
                .style('right', this.options.padding[1] + 10 + 'px')
                .style('top', x + 10 + 'px');
            var right = this.tooltip.node().offsetHeight;
            if (right + x > this.conHeight / 2) {
                this.tooltip.style('top', x1 - right - 10 + 'px');
            }
        }

    },
    legendFn: function (legendData) {
        if (!legendData) {
            console.log('请提供相应的legend data');
            return false;
        }
        var that = this;
        this.legendG = this.svg.selectAll('.legendG').data(['legendG']);
        var enter = this.legendG.enter().append('g').attr('class', 'legendG')
            .attr('transform', `translate(${this.options.padding[3] / 2},0)`);
        var foreignObject = enter.append('foreignObject');
        this.legendG = this.legendG.merge(enter);
        this.legendG.selectAll('foreignObject').attr('width', this.conWidth - (this.options.padding[1] + this.options.padding[3]) / 2).attr('height', this.options.padding[0]);

        var items = foreignObject.append('xhtml:div').attr('class', 'legend')
            .each(function () {
                if (that.titlebt) {
                    d3.select(this).style('padding-top', that.titlebt - 16 + 'px');
                }
            })
            .selectAll('div.item').data(legendData).enter().append('xhtml:div').attr('class', 'item');

        if (!items.size()) items = this.legendG.selectAll('foreignObject > div.legend').selectAll('div.item').data(legendData);
        var enterItems = items.enter().append('xhtml:div').attr('class', 'item');

        var item = items.merge(enterItems).html(function (d) {
            var html = '';
            var value = that.legend.find(function (dd) {
                return dd.name === d || dd.name === d.name;
            });

            if (that.isObject(d)) {
                if (d.type === 'sum') {
                    var sum = that.legend.reduce(function (a, b) {
                        return a + b.value;
                    }, 0);
                    d3.select(this).classed('sum', true);
                    html += `<div class="num">${sum}</div><div class="tit">${d.name}</div>`;
                } else html += `<div class="num">312</div><div class="tit">${d.name}</div>`;
            } else {
                if (!value) return;
                html += `<div class="num">${value.value}</div><div class="tit" title="${d}"><div class="colorbar" style="background:${value.color}; opacity:${value.opacity};"></div><div class="name">${d}</div></div>`;
            }
            return html;
        });
        items.exit().remove();
        var legwidth = this.conWidth - (this.options.padding[1] + this.options.padding[3]) / 2;
        var itemwidth = item.node().offsetWidth * item.size();
        if (itemwidth > legwidth) {
            this.legendG.selectAll('foreignObject div.legend').style('width', itemwidth + 'px');
        }
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
    resize: function (tsti) {
        tsti = tsti ? tsti : 0;
        this._super();
        this.tsti = tsti;
        this.init(this.container, this.diagramData, this.options);
    },
    redraw: function (reData, tsti) { // 增量和全量
        // var transionTime = tsti !== undefined ? tsti : 300
        this.diagramData = reData; // 全量
        this.tsti = 500;
        this.init(this.container, this.diagramData, this.options);

    },
    drawTset: function () {
        console.log('drawTset', this.legend);
    }
});