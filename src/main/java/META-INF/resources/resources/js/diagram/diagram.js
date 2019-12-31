(function (window) {
    // diagram 核心文件
    var Tsdiagram = window.Tsdiagram || {};

    var formatMillisecond = d3.timeFormat('.%L'),
        formatSecond = d3.timeFormat('%H:%M:%S'),
        formatMinute = d3.timeFormat('%H:%M:%S'),
        formatHour = d3.timeFormat('%H:%M:%S'),
        formatDay = d3.timeFormat('%m-%d'),
        formatWeek = d3.timeFormat('%m-%d'),
        formatMonth = d3.timeFormat('%m-%d'),
        formatYear = d3.timeFormat('%Y-%m-%d');

    function multiFormat (date) {
        return (d3.timeSecond(date) < date ? formatMillisecond
            : d3.timeMinute(date) < date ? formatSecond
                : d3.timeHour(date) < date ? formatMinute
                    : d3.timeDay(date) < date ? formatHour
                        : d3.timeMonth(date) < date ? (d3.timeWeek(date) < date ? formatDay : formatWeek)
                            : d3.timeYear(date) < date ? formatMonth
                                : formatYear)(date);
    }

    Tsdiagram.setting = {
        width: 500,
        height: 300,
        zoom: [0.5, 2],
        padding: [20, 30, 30, 20], // 图表padding
        flexlegendw: 200, // legend 宽度
        tickPadding: 4, // 刻度的padding
        scatterRadius: 4, // 点图点的半径
        stackBarWidth: 10, // 堆叠柱形图的宽度
        tileBarWidth: 5, // 平铺柱形图的宽度
        barInterval: '10%',
        outerLoopRadius: '80%', // 饼图外环半径
        loopInterval: '14%', // 饼图环之间的距离
        loopWidth: 30, // 环宽度
        pillarlegendheight: 100, // 柱形图legend宽度
        pillarlegendheight1: 30, // 柱形图legend宽度
        piePadding: '10%',
        areaComparePadding: 20,
        color1: ['#00fff9', '#49bdca', '#1e5f6f', '#ffffff'],
        color3: ['#336eff', 'rgba(51, 110, 255, 0.65)', 'rgba(51, 110, 255, 0.25)', 'rgba(51, 110, 255, 0.9)', 'rgba(51, 110, 255, 0.43)'],
        color: ['#00bcd4', '#4e8ec6', '#009688', '#f3a46e', '#62bcfa', '#677077', '#4253af', '#81d553'],
        color2: ['#2dc649', '#336eff', '#e42232', '#f7b437', '#a977ff'],
        title: {
            color: '#3a3a3a',
            fontsize: 26,
        },
    };

    Tsdiagram.Figures = Class.extend({
        init: function (container, diagramData, opts) {
            d3.select(container).selectAll('div.graph-container').remove();
            this.options = Object.assign({}, JSON.parse(JSON.stringify(Tsdiagram.setting)), opts);
            this.content = container;
            this.container = d3.select(container).append('xhtml:div').attr('class', 'graph-container').node();
            this.conWidth = this.content.offsetWidth || this.options.width;
            this.conHeight = this.content.offsetHeight || this.options.height;
            this.diagramData = diagramData;
            this.sdiagramData = JSON.parse(JSON.stringify(diagramData));
            this.conWidth = this.conWidth > this.options.padding[1] + this.options.padding[3] ? this.conWidth : this.options.padding[1] + this.options.padding[3];
            this.conHeight = this.conHeight > this.options.padding[0] + this.options.padding[2] ? this.conHeight : this.options.padding[0] + this.options.padding[2];
            // 用户自定义颜色
            if (opts && Array.isArray(opts.color)) d3.select(this.container).classed('customclr', true);
            // 阻止文本选中
            d3.select(this.container).attr('onselectstart', 'return false');
            // graph
            this.svg = d3.select(this.container).selectAll('svg.graph').data(['svg']).enter().append('svg').attr('class', 'graph');
            this.graphG = this.svg.append('g').attr('class', 'graphG');
            // diagram data series
            this.dealSeries();
            var dashboardPortvar = this.getparents(this.content, 'jquery-dashboard-portlet');
            if (dashboardPortvar) {
                this.dashboardPortvar = d3.select(dashboardPortvar)
                    .on('mouseenter', function () {
                        d3.select(this).classed('zIndex', true);
                    })
                    .on('mouseleave', function () {
                        d3.select(this).classed('zIndex', false).select('.tooltip1').style('display', 'none');
                    });
            }
            this.jqueryDashboard = this.getparents(this.content, 'jquery-dashboard');
            // 阻止事件冒泡
            d3.select(this.content)
              .on('mousedown', function () {
                  d3.event.stopPropagation();
              })
              .on('mouseup', function () {
                  d3.event.stopPropagation();
              })
              .on('mousemove', function () {
								  d3.event.stopPropagation();
              })
        },
        title: function (titleData) {
            if (!titleData && !titleData.name) return;
            var that = this;
            this.titleG = this.svg.selectAll('g.titleG').data(['title']);
            var enter = this.titleG.enter().append('g')
                .attr('class', 'titleG')
                .attr('fill', function () {
                    if (titleData.hasOwnProperty('itemStyle') && titleData.itemStyle.hasOwnProperty('color')) return titleData.itemStyle.color;
                    else return 'black';
                })
                .attr('font-size', function () {
                    if (titleData.hasOwnProperty('itemStyle') && titleData.itemStyle.hasOwnProperty('fontSize')) return titleData.itemStyle.fontSize;
                    else return 20;
                });
            enter.append('text')
                .text(function () {
                    if (titleData.hasOwnProperty('name')) return titleData.name;
                    else return '';
                });
            var titleText = this.titleG.merge(enter).node().getBBox();
            if (titleText) {
                this.titlebt = titleText.height + 12;
            } else {
                this.titlebt = 0;
            }
            this.titleG.merge(enter).each(function () {
                var currText = d3.select(this);
                if (titleData.hasOwnProperty('itemStyle') && titleData.itemStyle.hasOwnProperty('position')) {
                    if (titleData.itemStyle.position === 'center') {
                        currText
                            .attr('transform', `translate(${(that.conWidth - that.options.padding[3] - that.options.padding[1]) / 2 + that.options.padding[3]},${6})`)
                            .attr('text-anchor', 'middle');
                    } else if (titleData.itemStyle.position === 'right') {
                        currText
                            .attr('transform', `translate(${that.conWidth - that.options.padding[1] - 4},${6})`)
                            .attr('text-anchor', 'end');
                    } else {
                        if (that.svg.classed('pillar')) {
                            currText
                                .attr('transform', `translate(${8},${6})`);
                        } else {
                            if (that.spadl) {
                                currText
                                    .attr('transform', `translate(${that.options.padding[3] - that.spadl + 8},${6})`);
                            } else {
                                currText
                                    .attr('transform', `translate(${that.options.padding[3]},${6})`);
                            }
                        }
                        currText
                            .attr('text-anchor', 'start');
                    }
                    currText
                        .attr('font-weight', function () {
                            if (titleData.itemStyle.fontWeight) return titleData.itemStyle.fontWeight;
                            else return 'normal';
                        })
                        .attr('dominant-baseline', 'hanging');
                }
            });
            this.options.padding[0] = this.titlebt > this.options.padding[0] ? this.titlebt : this.options.padding[0];
        },
        brush: function (selectionarr) {
            var that = this;
            if (this.xAxisType !== 'time') return false;
            this.brushG = this.svg.selectAll('g.brushG').data(['brush']);
            var brushenter = this.brushG.enter().append('g').attr('class', 'brushG');
            this.brushG = this.brushG.merge(brushenter);
            var selection = this.brushG.selectAll('rect.selection').data(['selection']);
            var selectionenter = selection.enter().append('rect').attr('class', 'selection');
            this.brushselection = selection.merge(selectionenter);
            this.brushselection.attr('width', function () {
                if (!selectionarr) return 0;
                var width = selectionarr[1] - selectionarr[0];
                if (width < 0) width = 0;
                return width;
            })
                .attr('height', function () {
                    return that.rangey[0] - that.rangey[1];
                })
                .attr('transform', `translate(${this.rangex[0]},${this.rangey[1]})`);

            var startx = this.rangex[0];
            var drag = d3.drag()
                .on('start', function () {
									  d3.event.sourceEvent && d3.event.sourceEvent.stopPropagation();
                    if (d3.event.x < that.rangex[0] || d3.event.x > that.rangex[1]) { // 水平左边
                        that.brushselection.attr('width', 0);
                        startx = false;
                        return false;
                    }
                    startx = d3.event.x;
                    that.brushselection.attr('height', that.rangey[0] - that.rangey[1]).attr('transform', `translate(${startx},${that.rangey[1]})`);
                })
                .on('drag', function () {
									  d3.event.sourceEvent && d3.event.sourceEvent.stopPropagation();
                    if (!startx) return false;
                    if (d3.event.x < that.rangex[0]) {
                        that.brushselection.attr('transform', `translate(${that.rangex[0]},${that.rangey[1]})`);
                        that.brushselection.attr('width', Math.abs(that.rangex[0] - startx));
                        return false;
                    } else if (d3.event.x > that.rangex[1]) {
                        that.brushselection.attr('width', Math.abs(that.rangex[1] - startx));
                        return false;
                    }
                    if (startx > d3.event.x) that.brushselection.attr('transform', `translate(${d3.event.x},${that.rangey[1]})`);
                    that.brushselection.attr('width', Math.abs(d3.event.x - startx));
                })
                .on('end', function () {
									  d3.event.sourceEvent && d3.event.sourceEvent.stopPropagation();
                    that.brushselection.attr('width', 0);
                    // 获取起点位置和终点位置
                    if (!startx) return false;
                    var start = d3.min([startx, d3.event.x]);
                    var end = d3.max([startx, d3.event.x]);
                    if (start < that.rangex[0]) start = that.rangex[0];
                    if (end > that.rangex[1]) end = that.rangex[1];
                    if (end - start < 10) return false;
                    that.brushselectionFn([start, end]);
                });
            this.svg.call(drag);
        },
        brushselectionFn: function (selection) {
            var that = this;
            var starttime = this.xScale.invert(selection[0]);
            var endtime = this.xScale.invert(selection[1]);
            starttime = new Date(starttime).getTime();
            endtime = new Date(endtime).getTime();
            if (this.seriesStyle === 'style1') {
                var newArr = this.diagramData.xAxis.data.filter(function (d, i) {
                    return d >= starttime && d <= endtime;
                });
                if (newArr.length < 6) return false;
                this.diagramData.xAxis.data = newArr;
                this.diagramData.series.forEach(function (d, i) {
                    d.data = that.sdiagramData.series[i].data.filter(function (d1, i1) {
                        return d1[0] >= starttime && d1[0] <= endtime;
                    });
                });
            } else {
                var startindex;
                var endindex;
                this.sdiagramData.xAxis.data.forEach(function (d, i) {
                    var mtime = that.xAxisType === 'time' && typeof d === 'string' ? new Date(d).getTime() : d;
                    if (mtime > starttime && !startindex) startindex = i;
                    if (mtime > endtime && !endindex) endindex = i - 1;
                });
                if (starttime === undefined) startindex = 0;
                if (endindex === undefined) endindex = this.sdiagramData.xAxis.data.length - 1;

                var newArr = this.sdiagramData.xAxis.data.filter(function (d, i) {
                    return i >= startindex && i <= endindex;
                });
                if (newArr.length < 6) return false;
                this.diagramData.xAxis.data = newArr;
                this.diagramData.series.forEach(function (d, i) {
                    d.data = that.sdiagramData.series[i].data.filter(function (d1, i1) {
                        return i1 >= startindex && i1 <= endindex;
                    });
                });
            }
            if (this.auxiliarylineG) {
                var legendline = this.auxiliarylineG.selectAll('.legendline');
                if (legendline.size()) {
                    that.auxiliarylineG.selectAll('.legendline,.legendpoints').remove();
                }
            }
            this.dealSeries('brush');
            this.dividedLine();
            this.dragupdate();
        },
        getColor: function () {
            var that = this;
            this.legend = [];
            var deopacity = 1;
            if (Array.isArray(this.diagramData.series)) {
                var colorIndex = 0;
                this.diagramData.series.forEach(function (d, i, arr) {
                    if (d.hasOwnProperty('type') && d.type.indexOf('pie') !== -1) { // pie
                        if (Array.isArray(d.data)) {
                            d.data.forEach(function (d) {
                                var is = that.legend.find(function (d1) {
                                    return d1.name === d.name;
                                });
                                if (d.hasOwnProperty('itemStyle')) {
                                    if (d.itemStyle.color) {
                                        if (is) {
                                            is.color = d.itemStyle.color;
                                        } else {
                                            that.legend.push({
                                                name: d.name,
                                                color: d.itemStyle.color
                                            });
                                        }
                                    } else {
                                        that.legend.push({
                                            name: d.name,
                                            color: that.options.color[colorIndex]
                                        });
                                        colorIndex++;
                                    }
                                } else if (!is) {
                                    that.legend.push({
                                        name: d.name,
                                        color: that.options.color[colorIndex]
                                    });
                                    colorIndex++;
                                }
                            });
                        }
                    } else if (d.hasOwnProperty('type') && (d.type.indexOf('curve') !== -1 || d.type.indexOf('area') !== -1)) { // curve
                        var is = that.legend.find(function (d1) {
                            if (that.diagramData.legend && that.diagramData.legend.key === 'index') return false;
                            return d1.name === d.name;
                        });
                        if (d.hasOwnProperty('itemStyle')) {
                            if (d.itemStyle.color) {
                                if (is) {
                                    is.color = d.itemStyle.color;
                                } else {
                                    that.legend.push({
                                        name: d.name,
                                        color: d.itemStyle.color
                                    });
                                }
                            } else {
                                that.legend.push({
                                    name: d.name,
                                    color: that.options.color[colorIndex]
                                });
                                colorIndex++;
                            }
                        } else if (!is) {
                            var color;
                            if (colorIndex >= that.options.color.length) {
                                if (d.type.indexOf('curve') !== -1) {
                                    colorIndex = 0;
                                    color = that.options.color[colorIndex];
                                } else {
                                    colorIndex = 0;
                                    deopacity -= 0.4;
                                }
                            } else {
                                color = that.options.color[colorIndex];
                            }
                            var formatfloat = d3.format('.1f');
                            var obj = {
                                name: d.name,
                                key: that.diagramData.legend && that.diagramData.legend.key === 'index' ? i : i,
                                value: that.diagramData.legend && that.diagramData.legend.value === 'newest' ? d.data[d.data.length - 1][1] : undefined,
                                color: color,
                                opacity: deopacity,
                                index: colorIndex,
                                type: d.type,
                                unit: d.unit ? d.unit : '',
                                threshold: d.threshold || ''
                            };
                            that.legend.push(obj);
                            colorIndex++;
                        }
                    } else { // 非pie
                        var is = that.legend.find(function (d1) {
                            return d1.name === d.name;
                        });
                        if (d.hasOwnProperty('itemStyle')) {
                            if (d.itemStyle.color) {
                                if (is) {
                                    is.color = d.itemStyle.color;
                                } else {
                                    that.legend.push({
                                        name: d.name,
                                        color: d.itemStyle.color
                                    });
                                }
                            } else {
                                that.legend.push({
                                    name: d.name,
                                    color: that.options.color[colorIndex]
                                });
                                colorIndex++;
                            }
                        } else if (!is) {
                            var color;
                            if (colorIndex >= that.options.color.length) {
                                if (d.type.indexOf('curve') !== -1) {
                                    // var cool = d3.interpolateCool(colorIndex / arr.length);
                                    // var dark2 = d3.schemeCategory20;
                                    // if (dark2 && dark2[colorIndex - that.options.color.length]) cool = dark2[colorIndex];
                                    // color = `${cool}`;
                                    colorIndex = 0;
                                    color = that.options.color[colorIndex];
                                } else {
                                    colorIndex = 0;
                                    deopacity -= 0.4;
                                }
                            } else {
                                color = that.options.color[colorIndex];
                            }
                            var formatfloat = d3.format('.1f');
                            !color && (color = that.options.color[colorIndex])
                            var obj = {
                                name: d.name,
                                color: color,
                                opacity: deopacity,
                                index: colorIndex,
                                type: d.type,
                                unit: d.unit ? d.unit : '',
                                threshold: d.threshold || ''
                            };
                            that.legend.push(obj);
                            colorIndex++;
                        }
                    }
                });
            }
        },
        randomvalue: function (a, b) {
            return Math.floor(Math.random() * (b - a) + a);
        },
        scale: function (type, domain, range) {
            var scale;
            if (type === 'time') {
                scale = d3.scaleTime();
                scale.domain([domain[0], domain[domain.length - 1]]).range(range);
            } else if (type === 'value') {
                scale = d3.scaleLinear();
                scale.domain([domain[0], domain[domain.length - 1]]).range(range);
            } else if (type === 'category') {
                if (this.diagramType === 'pillar' && this.pillarStyle !== 'style1') {
                    scale = d3.scaleBand().paddingOuter(0.2).paddingInner(0.2);
                } else {
                    scale = d3.scaleBand().paddingOuter(0.05).paddingInner(0.5);
                }
                scale.domain(domain).range(range);
            }
            return scale;
        },
        xAxis: function (action) {
            var that = this;
            // range
            this.rangex = [this.options.padding[3], this.conWidth - this.options.padding[1]];
            if (this.diagramData.hasOwnProperty('xAxis')) { // xAxis data
                this.xAxisdata = this.diagramData.xAxis;
                // domain
                this.domainx = this.xAxisdata.data;
                // xaxis type
                if (this.xAxisdata.brush) {
                    this.xAxisType = 'time';
                } else {
                    this.xAxisType = 'category';
                }
            } else { // 纵向
                this.domainx = [0, 0];
                // type
                this.xAxisType = 'value';
                // domain
                this.seriesData.forEach(function (d) {
                    if (d.max > that.domainx[1]) that.domainx[1] = d.max;
                    if (d.min < that.domainx[0]) that.domainx[0] = d.min;
                });
                // ticks
                this.ticksx = d3.ticks(this.domainx[0], this.domainx[1], 5);
                if (this.ticksx[this.ticksx.length - 1] < this.domainx[1]) {
                    this.domainx[1] = this.ticksx[this.ticksx.length - 1] + this.ticksx[1] - this.ticksx[0];
                    this.ticksx.push(this.ticksx[this.ticksx.length - 1] + this.ticksx[1] - this.ticksx[0]);
                }
            }
            if (this.xAxisType === 'time') {
                this.domainx = this.domainx.map(function (d) {
                    return new Date(d).getTime();
                });
            }

            if (this.xAxisType === 'value') {
                if (this.domainx[this.domainx.length - 1] === 0) {
                    this.domainx[this.domainx.length - 1] = 10;
                }
            }
            // scale
            this.xScale = this.scale(this.xAxisType, this.domainx, this.rangex);
            if (action === 'NoG') {
                return false;
            }
            this.xAxisB = d3.axisBottom(this.xScale)
                .tickSizeOuter([0])
                .tickPadding([this.options.tickPadding]);
            if (this.xAxisType === 'time') {
                this.xAxisB.ticks(5).tickFormat(multiFormat);
            } else if (this.xAxisType === 'value') {
                this.xAxisB
                    .tickSizeInner([-this.conHeight + this.options.padding[2] + this.options.padding[0]])
                    .ticks(5);
            }
            if (this.xAxisType === 'value' &&  this.axisUnit) {
                this.xAxisB.tickFormat(function (d) {
                    if (!d) return d;
                    return d + that.axisUnit;
                });
            }
            this.xAxisBG = this.svg.selectAll('.timeAxis').data([this.xAxisB]);
            var enter = this.xAxisBG.enter().append('g').lower()
                .classed('timeAxis', true);
            this.xAxisBG = this.xAxisBG.merge(enter).attr('transform', `translate(0,${this.conHeight - this.options.padding[2]})`)
                .call(this.xAxisB);
            if (this.xAxisType === 'value') {
                this.xAxisBG.classed('y', true);
            } else {
                this.xAxisBG.classed('y', false);
            }
            // 调节时间刻度数量
            if (this.xAxisType === 'time') {
                this.adjTickx('reduce');
            }
        },
        adjTickx: function (action) {
            var ticksx = this.xAxisBG.selectAll('g.tick text');
            if (ticksx.size() < 2) return false;
            var interval = 0;
            var barStep = 0;
            if (this.xAxisType === 'category') {
                interval = this.xScale.bandwidth();
                barStep = this.xScale.paddingInner();
            }
            var maxlen = 0;
            var isCrash = false;
            ticksx.each(function (d, i, arr) {
                var pre;
                var b = this.getBBox();
                if (i > 0) pre = arr[i - 1];
                if (pre) {
                    var preWidth = pre.getBBox().width;
                    if (interval && (b.width + preWidth) / 2 > interval * (1 + barStep)) isCrash = true;
                }
                if (b.width > maxlen) maxlen = b.width;
            });
            if (action === 'reduce') {
                if ((maxlen + 2) * (ticksx.size() - ticksx.filter('.hide').size()) > (this.rangex[1] - this.rangex[0])) {
                    var arr = ticksx
                        .filter(':not(.hide)')
                        .filter(function (d, i) {
                            return i % 2 === 1;
                        }).classed('hide', true);
                    if (!this.hideticks) this.hideticks = [];
                    this.hideticks.push(arr);
                } else {
                    if ((ticksx.size() - ticksx.filter('.hide').size()) * maxlen * 2 < (this.rangex[1] - this.rangex[0])) {
                        if (this.hideticks && this.hideticks.length > 0) {
                            var hide = this.hideticks.pop();
                            if (hide) hide.classed('hide', false);
                        }
                    }
                }
            } else {
                if (isCrash) {
                    var dy = maxlen / 2 + 10;
                    this.options.padding[2] = dy;
                    ticksx.style('transform', `translate(${0}px,${dy / 3}px)rotate(-30deg)`);
                } else {
                    ticksx.style('transform', 'rotate(0deg)');
                    this.options.padding[2] = 30;
                }
            }
        },
        adjustment: function (tsti) {
            var ticksele = this.yAxisLG.selectAll('g.tick text:not(.mul)');
            var maxlen = 0;
            ticksele.each(function () {
                var bbox = this.getBBox();
                if (bbox.width > maxlen) maxlen = bbox.width;
            });
            var paddingleft = 8;
            if (this.conbar) paddingleft = 16;
            if (maxlen + this.options.tickPadding + paddingleft > this.options.padding[3]) {
                this.spadl = this.options.padding[3] = maxlen + this.options.tickPadding + paddingleft;
                this.resize(tsti);
                if (this.titleG) {
                    if (this.diagramData.hasOwnProperty('title')) {
                        this.title(this.diagramData.title);
                    }
                }
            } else if (this.legendMaxLength + 70 + this.spadl> this.options.flexlegendw && this.legendAdaption) {
                this.options.flexlegendw = this.spadl + this.legendMaxLength + 70;
                if (this.options.flexlegendw > this.container.offsetWidth / 2) this.options.flexlegendw = this.container.offsetWidth / 2;
                this.options.padding[3] = this.options.flexlegendw;
                this.resize(tsti);
            } else {
                if (this.diagramType === 'pillar') {
                    this.yAxis();
                    this.xAxis();
                    this.drawPillar(this.tsti);
                }
            }
        },
        yAxis: function (action) {
            var that = this;
            // range
            this.rangey = [this.conHeight - this.options.padding[2], this.options.padding[0]];
            if (this.diagramData.hasOwnProperty('yAxis')) {
                if (Array.isArray(this.diagramData.yAxis.data)) {
                    this.yAxisdata = this.diagramData.yAxis;
                    this.domainy = this.yAxisdata.data;
                    this.yAxisType = 'category';
                } else if (Array.isArray(this.diagramData.yAxis.ticks)) {
                    this.domainy = this.diagramData.yAxis.ticks;
                } else if (this.diagramData.yAxis.hasOwnProperty('max') && this.diagramData.yAxis.hasOwnProperty('min')) {
                    this.domainy = [this.diagramData.yAxis.min, this.diagramData.yAxis.max];
                }
            } else {
                this.domainy = [0, 0];
                // type
                this.yAxisType = 'value';
                // domain
                this.seriesData.forEach(function (d) {
                    if (d.max > that.domainy[1]) that.domainy[1] = d.max;
                    if (d.min < that.domainy[0]) that.domainy[0] = d.min;
                });
                // ticks
                var ticks = d3.ticks(this.domainy[0], this.domainy[1], 3);
                if (ticks[ticks.length - 1] < this.domainy[1]) {
                    this.domainy[1] = ticks[ticks.length - 1] + ticks[1] - ticks[0];
                    ticks.push(ticks[ticks.length - 1] + ticks[1] - ticks[0]);
                }
                this.ticksy = ticks;
            }
            if (!this.yAxisType) {
                this.yAxisType = 'value';
            }
            if (this.yAxisType === 'value') {
                if (this.domainy[this.domainy.length - 1] === 0) {
                    this.domainy[this.domainy.length - 1] = 10;
                }
            }
            // scale
            this.yScale = this.scale(this.yAxisType, this.domainy, this.rangey);
            if (action === 'scale') return false;
            // axis
            this.yAxisL = d3.axisLeft(this.yScale);
            if (this.yAxisType === 'value') {
                this.yAxisL.tickSizeInner([-this.conWidth + this.options.padding[3] + this.options.padding[1]]).tickSizeOuter([100])
                    .tickPadding([this.options.tickPadding]);
                if (this.diagramData.yAxis && this.diagramData.yAxis.ticks) {
                    this.yAxisL.tickValues(this.domainy);
                } else {
                    this.yAxisL.ticks(3);
                }
            } else if (this.yAxisType === 'category') {
                this.yAxisL
                    .tickSizeOuter([0])
                    .tickPadding([this.options.tickPadding]);
            }

            // 刻度倍数
            // var tickvalue = this.yScale.tickValues();
            this.yAxisLG = this.svg.selectAll('.yAxis').data([this.yAxisL]);
            var enter = this.yAxisLG.enter().append('g')
                .attr('class', 'yAxis L y').lower();
            if (this.yAxisType === 'value' &&  this.axisUnit) {
                this.yAxisL.tickFormat(function (d) {
                    if (!d) return d;
                    return d + that.axisUnit;
                });
            }
            this.yAxisLG = this.yAxisLG.merge(enter).attr('transform', `translate(${this.options.padding[3]}, 0)`)
                .call(this.yAxisL);
            if (this.yAxisType === 'category') {
                this.yAxisLG.classed('y', false);
            } else {
                this.yAxisLG.classed('y', true);
            }
            if (!this.ticksy || this.ticksy.length < 2) return false;
            this.multipleY = 0;
            var multiple = this.ticksy[1] / 1000;
            if (multiple > 1) this.multipleY = 1000;
            if (multiple >= 1000) this.multipleY = 1000000;
            if (this.multipleY) {
                this.yAxisLG.selectAll('.tick text').text(function (d, i, arr) {
                    var text = d3.select(this).text();
                    var textArr = text.split(',');
                    if (that.multipleY === 1000 && textArr.length > 1) textArr.pop();
                    if (that.multipleY === 1000000 && textArr.length > 1) {
                        textArr.pop();
                        textArr.pop();
                    }
                    if (arr.length - 1 === i && that.multipleY) {
                        var parent = that.getparents(this, 'tick');
                        var dy = d3.select(this).attr('dy');
                        d3.select(parent)
                            .selectAll('.mul').data([1]).enter()
                            .append('text').classed('mul', true)
                            .attr('dy', dy)
                            .attr('x', '0')
                            .attr('text-anchor', 'start')
                            .text(`（ x${that.multipleY} ）`);
                    }
                    return textArr.join(',');
                });
            } else {
                this.yAxisLG.selectAll('.mul').remove();
            }
        },
        isObject: function (data) {
            if (data instanceof Object && !Array.isArray(data)) return true;
        },
        dealSeries: function (action) {
            var that = this;
            this.seriesData = [];
            if (Array.isArray(this.diagramData.series)) {
                var sum = [0, 0];
                var obj = {};
                var tilesCount = 0;
                var tilesIndex = 0;
                var max = 0;
                var min = 0;
                var xAxisData = [];
                var isEmpty = that.diagramData.xAxis && that.diagramData.xAxis.startTime && that.diagramData.xAxis.endTime;
                if (isEmpty) this.seriesStyle = 'style1';
                this.diagramData.series.forEach(function (d) {
                    if (d.type.indexOf('tile') !== -1 || d.type.indexOf('Compare') !== -1) tilesCount++;
                });
                // 组织xAxis data
                if (isEmpty) {
                    this.diagramData.series.forEach(function (d) {
                        if (that.diagramData.xAxis && that.diagramData.xAxis.startTime && that.diagramData.xAxis.endTime) {
                            that.startTime = that.diagramData.xAxis.startTime;
                            that.endTime = that.diagramData.xAxis.endTime;
                            //console.log(that.startTime, that.endTime)
                            if (typeof that.diagramData.startTime === 'string') that.startTime = new Date(that.startTime).getTime();
                            if (typeof that.diagramData.endTime === 'string') that.endTime = new Date(that.endTime).getTime();
                        }
                        if (that.startTime && that.endTime) {
                            d.data = d.data.filter(function (d) {
                                return d[0] >= that.startTime && d[0] <= that.endTime;
                            });
                        }
                        d.data.forEach(function (d) {
                            if (typeof d[0] === 'string') d[0] = new Date(d).getTime();
                            if (!xAxisData.includes(d[0])) {
                                xAxisData.push(d[0]);
                            }
                        });
                    });
                    that.diagramData.xAxis.data = xAxisData;
                    that.diagramData.xAxis.data.sort(function (a, b) {
                        return a - b;
                    });
                    if (action !== 'brush') {
                        if (that.diagramData.xAxis.data[0] !== that.diagramData.xAxis.startTime) that.diagramData.xAxis.data.unshift(that.diagramData.xAxis.startTime);
                        if (that.diagramData.xAxis.data[that.diagramData.xAxis.data.length - 1] !== that.diagramData.xAxis.endTime) that.diagramData.xAxis.data.push(that.diagramData.xAxis.endTime);
                    }
                }
                this.diagramData.series.forEach(function (d, i) {
                    if (Array.isArray(d.data)) {
                        var isArr = d.data.every(function (d) {
                            return Array.isArray(d);
                        });
                        if (isArr) {
                            // 面积图
                            if ((d.type.indexOf('area') !== -1 && d.type.indexOf('Compare') === -1)) { // 堆叠
                                var arr = [];
                                var arr1 = [];
                                d.data.forEach(function (dd) {
                                    var index = xAxisData.indexOf(dd[0]);
                                    arr1[index] = dd[1];
                                });
                                var arr3 = [];
                                xAxisData.forEach(function (a, i1) {
                                    if (arr1[i1] === undefined) arr1[i1] = '';
                                    arr1[i1] = Number(arr1[i1]);
                                    if (sum[i1]) {
                                        arr = [a, sum[i1], sum[i1] + arr1[i1]];
                                        sum[i1] += arr1[i1];
                                        if (arr[2] > max) max = arr[2];
                                        arr3.push(arr);
                                        return false;
                                    }
                                    sum[i1] = arr1[i1];
                                    if (sum[i] > max) max = sum[i1];
                                    arr3.push([a, 0, arr1[i1]]);
                                    return false;
                                });
                                max = d3.max(sum);
                                that.seriesData.push({
                                    data: arr3,
                                    max: max,
                                    min: 0,
                                    type: d.type,
                                    name: d.name
                                });
                                return false;
                            }
                            d.data.forEach(function (d) {
                                if (typeof d[1] === 'string') d[1] = Number(d[1]);
                                if (d[1] > max) {
                                    max = d[1];
                                }
                            });

                            that.seriesData.push({
                                data: d.data,
                                max: max,
                                min: 0,
                                type: d.type,
                                name: d.name
                            });
                            return false;
                        }
                    }
                    if (d.type.indexOf('tile') !== -1) tilesIndex++;
                    obj = JSON.parse(JSON.stringify(d));
                    if ((d.type.indexOf('stack') !== -1) || (d.type.indexOf('area') !== -1 && d.type.indexOf('Compare') === -1)) { // 堆叠
                        if (d.type.indexOf('area') !== -1) {
                            if (obj.data.length > that.diagramData.xAxis.data.length) obj.data.splice(that.diagramData.xAxis.data.length);
                        }
                        obj.data = obj.data.map(function (a, i1) {
                            a = Number(a);
                            if (sum[i1]) {
                                var arr;
                                if (a.value !== undefined) {
                                    var b = a.value;
                                    arr = [sum[i1], sum[i1] + a.value];
                                    sum[i1] += b;
                                } else {
                                    arr = [sum[i1], sum[i1] + a];
                                    sum[i1] += a;
                                }
                                if (arr[1] > max) max = arr[1];
                                return arr;
                            }
                            sum[i1] = a;
                            if (sum[i] > max) max = sum[i1];
                            if (sum[i] < min) min = sum[i1];
                            return [0, a];
                        });
                        max = d3.max(sum);
                    } else if (d.type.indexOf('tile') !== -1 || d.type.indexOf('Compare') !== -1) {
                        tilesIndex = i;
                        obj.data = d.data.map(function (a) {
                            if (a.value !== undefined) {
                                if (a.value > max) max = a.value;
                                if (a.value < min) min = a.value;
                                return [0, a.value, tilesIndex, tilesCount];
                            }
                            if (a > max) max = Number(a);
                            return [0, a, tilesIndex, tilesCount];
                        });
                    } else if (d.type.indexOf('curve') !== -1 || d.type.indexOf('points') !== -1) {
                        obj.data = [];
                        d.data.forEach(function (d, i) {
                            var x = that.diagramData.xAxis.data[i];
                            if (x === undefined) return false;
                            if (that.xAxisType === 'time') {
                                x = new Date(x).getTime();
                            } else if (that.xAxisType === 'category') {
                                // var interval = that.xScale.bandwidth()
                                // x = that.xScale(x) + interval / 2
                            }
                            if (d > max) max = Number(d);
                            obj.data.push([x, d]);
                        });
                    }
                    obj.max = max;
                    obj.min = min;
                    that.seriesData.push(obj);
                });
                if (isEmpty) {

                }
            }
        },
        dragupdate: function () {
            var that = this;
            that.xAxis();
            that.yAxis();
            that.barbgFn();
            if (this.diagramData.legend && this.diagramData.legend.position === 'top') {
                this.legendTopFn(this.legend);
            } else if (this.diagramData.legend && this.diagramData.legend.data) {
                var aa = that.auxiliarylineG.selectAll('.legendpoints').data();
                if (aa.length) {
                    this.legendFn(this.tooltipData);
                } else {
                    this.legendFn(this.legend);
                }
            }
            if (this.diagramData.hasOwnProperty('title')) {
                this.title(this.diagramData.title);
            }
        },
        legendFn: function (legendData) {
            var that = this;
            this.legendDiv = d3.select(this.container).selectAll('div.legendDiv').data(['legend']);
            var legendenter = this.legendDiv.enter().append('xhtml:div').attr('class', 'legendDiv')
                .style('width', this.options.padding[3] - this.spadl - 14 + 'px')
                .style('padding-top', 20 + 'px')
                .style('padding-bottom', 20 + 'px')
                .on('wheel', function () {
                    if (!that.legendDiv.select('.legscroll').size()) return;
                    d3.event.preventDefault();
                    var conpdt = parseFloat(d3.select(this).style('padding-top'));
                    var conpdb = parseFloat(d3.select(this).style('padding-bottom'));
                    var conHeight = this.offsetHeight - conpdb - conpdt;
                    var legendHeight = d3.select(this).select('div.legend3').node().offsetHeight;
                    var legendMgt = parseFloat(d3.select(this).select('div.legend3').style('margin-top'));
                    var itemHeight = d3.select(this).select('div.item').node().offsetHeight;
                    if (d3.event.wheelDelta < 0) { // 向下
                        legendMgt -= itemHeight;
                    } else {
                        legendMgt += itemHeight;
                    }
                    if (legendMgt > 0) legendMgt = 0;
                    else if (legendMgt < conHeight - legendHeight) legendMgt = conHeight - legendHeight;
                    var scroMgt = (-legendMgt / legendHeight) * (that.conHeight - 40);
                    d3.select(this).select('div.legend3').style('margin-top', legendMgt + 'px');
                    that.legendDiv.select('.legscroll > div').style('margin-top', scroMgt + 'px');
                });
            // 自适应最长legend
            this.legendDiv.style('width', this.options.padding[3] - this.spadl - 14 + 'px')
            // 给legend套个容器
            legendenter.append('xhtml:div').attr('class', 'legendcon').append('xhtml:div').attr('class', 'legend3');
            // 添加legend item
            if (legendData) {
                this.legendDiv = this.legendDiv.merge(legendenter);//.data(legendData);
                var legendHtml = this.legendDiv.select('div.legend3')
                    .html(function (d) {
                        var str = '';
                        var arr = JSON.parse(JSON.stringify(legendData)).sort(function (a,b) {
                            if (that.legendSort === 'asc') return a.value - b.value;
                            return b.value - a.value;
                        });
                        arr.forEach(function (d, i) {
                            var color = that.legendKey === 'index' ? that.legend[d.key] : that.legend.find(function (d1) {
                                return d1.name === d.name;
                            });
                            str += `<div class="item" style="position: relative;" key="${color.key}"><div class="nameLength" style="visibility: hidden; white-space: nowrap; position: absolute; top: 0; left: 0;">${d.name}</div><div class="colorblock ${'clr' + color.index}" clr="${'clr' + color.index}" style="background: ${color.color}"></div><div class="tit" title="${d.name}">${d.name}</div><div class="num" style="display: inline-block;">${d.value !== undefined ? (d.value + (d.unit ? d.unit : '')) : ''}</div></div>`;
                        });
                        return str;
                    });
                // 计算最长长度
                var maxLength = 0;
                legendHtml.selectAll('.item').each(function () {
                    var item = d3.select(this);
                    var nameLength = item.select('.nameLength').node().offsetWidth;
                    var numLength = item.select('.num').node().offsetWidth;
                    if (maxLength < (nameLength + numLength)) maxLength = nameLength + numLength;
                });
                if (maxLength) that.legendMaxLength = maxLength;
            }

            this.legendDiv.each(function () {
                var conpdt = parseFloat(d3.select(this).style('padding-top'));
                var conpdb = parseFloat(d3.select(this).style('padding-bottom'));
                var conHeight = this.offsetHeight - conpdb - conpdt;
                var legendMgt = parseFloat(d3.select(this).select('div.legend3').style('margin-top'));
                var legendHeight = d3.select(this).select('div.legend3').node().offsetHeight;
                if (legendHeight > conHeight) {
                    d3.select(this).style('padding-right', '10px');
                    var maxLegendMgt = legendHeight - conHeight;
                    var scroll = d3.select(this).selectAll('div.legscroll').data(['legscroll']);
                    var enter = scroll.enter().append('xhtml:div').attr('class', 'legscroll');
                    if (maxLegendMgt < -legendMgt) {
                        d3.select(this).select('div.legend3').style('margin-top', -maxLegendMgt + 'px');
                    }
                    enter.append('xhtml:div')
                        .attr('class', 'scrollbar');
                    scroll.merge(enter)
                        .style('height', that.conHeight - 40 + 'px')
                        .style('top', 20 + 'px')
                        .select('.scrollbar')
                        .style('margin-top', (-legendMgt / legendHeight) * (that.conHeight - 40) + 'px')
                        .style('height', conHeight / legendHeight * (that.conHeight - 40) + 'px');
                } else {
                    d3.select(this).selectAll('div.legscroll').remove();
                    d3.select(this).style('padding-right', 0);
                }
            });

            if (!this.drag) {
                this.drag = d3.drag()
                    .on('drag', function () {
                        var event = d3.event;
                        if (event.x < 100 || event.x > that.conWidth - 80) return;
                        d3.select(this).attr('x1', event.x).attr('x2', event.x);
                        that.legendDiv.style('left', 0).style('width', event.x + 'px');
                        that.options.padding[3] = event.x + that.spadl + 8;
                        that.dragupdate(0);
                        that.hidedragarrow();
                    });
            }
            this.dragbar = this.legendDiv.selectAll('div.dragbar').data(['dragbar']);
            var dragbarEnter = this.dragbar.enter().append('xhtml:div').attr('class', 'dragbar')
                .call(this.drag);
            dragbarEnter.append('xhtml:div');
            dragbarEnter.append('xhtml:div');
            if (!this.conbar) {
                var graphBar = d3.select(this.content).select('.graph-bar');
                if (graphBar.size()) this.conbar = graphBar;
                else graphBar = d3.select(this.content).style('position', 'relative').append('xhtml:div').attr('class', 'graph-bar');
                this.conbar = graphBar.on('click', function () {
                    openbar.dispatch('click');
                    that.hidedragarrow();
                });
            }

            var openbar = this.dragbar.enter().append('xhtml:div').attr('class', 'dragarrow open')
                .on('click', function () {
                    if (that.options.padding[3] === that.spadl) {
                        that.options.padding[3] = that.spadl + that.options.flexlegendw;
                        that.legendDiv.style('width', that.options.flexlegendw - 8 + 'px').style('left', 0 + 'px').style('overflow', '');
                        d3.select(this).classed('open', true);
                        that.conbar.style('display', 'none').style('opacity', 0);
                    } else {
                        that.options.padding[3] = that.spadl;
                        that.legendDiv.style('width', 0 + 'px').style('left', -that.options.flexlegendw + 'px').style('overflow', 'hidden');
                        d3.select(this).classed('open', false);
                        that.conbar.style('display', 'inline').style('opacity', 1);
                    }
                    that.dragupdate(0);
                });
            // 添加交互
            if (legendenter.size()) {
                this.svg.on('dblclick', function () {
                    if (!that.legendDiv || !that.legendDiv.select('.dragarrow').classed('open')) return;
                    if (that.auxiliarylineG.select('line.hoverline').size() && that.auxiliarylineG.select('line.hoverline').style('display') === 'none') return false;
                    that.auxiliarylineG.selectAll('.legendline,.legendpoints').remove();
                    that.hoverline.attr('class', 'legendline');
                    that.hoverpoints.attr('class', 'legendpoints');
                    that.legendFn(that.tooltipData);
                })
                    .on('click', function (d) {
                        if (that.auxiliarylineG) that.auxiliarylineG.selectAll('.legendline,.legendpoints').remove();
                        that.graphG.selectAll('.curveG').attr('opacity', 1).classed('active1', false);
                        that.legendFn(that.legend);
                        that.hoversection = [];
                    });
            }
        },
        auxiliaryline: function () { // 辅助线
            var that = this;
            var mouse = d3.mouse(this.svg.node());
            if (((mouse[0] < that.options.padding[3] || mouse[0] > that.conWidth - that.options.padding[1]) || (mouse[1] < that.options.padding[0] || mouse[1] > that.conHeight - that.options.padding[2]))) {
                if (this.auxiliarylineG) this.auxiliarylineG.selectAll('line.hoverline,circle.hoverpoints').style('display', 'none');
                if (this.tooltip) this.tooltip.style('display', 'none');
                return false;
            } else {
                if (this.auxiliarylineG) this.auxiliarylineG.selectAll('line.hoverline,circle.hoverpoints').style('display', 'inline');
                if (this.tooltip) this.tooltip.style('display', 'inline');
            }
            var currhover = this.hovergroupArr.find(function (d) {
                return d[0] <= mouse[0] && mouse[0] <= d[1];
            });
            if (!currhover) {
                if (this.auxiliarylineG) this.auxiliarylineG.selectAll('line.hoverline,circle.hoverpoints').style('display', 'none');
                if (this.tooltip) this.tooltip.style('display', 'none');
                return;
            }

            var index = this.hovergroupArr.indexOf(currhover);
            // 判断相同
            if (this.hoversection === currhover) {
                if (mouse[1]) this.tooltipFn(0, index, 'moveY', mouse[1]);
                return;
            }
            else {
                this.hoversection = currhover;
                this.hoverindexpro = index;
            }
            var x;
            if (this.xAxisType === 'time') {
                // if (typeof this.domainx[index]) this.domainx[index] =
                x = this.xScale(new Date(this.domainx[index]));
            } else {
                if (index === 0) x = currhover[0] + this.interval / 2;
                else if (index === this.hovergroupArr.length - 1) x = currhover[1] - this.interval / 2;
                else x = (currhover[0] + currhover[1]) / 2;
            }
            this.hoverline = this.auxiliarylineG.selectAll('.hoverline').data([index]);
            var hoverlineenter = this.hoverline.enter().append('line').attr('class', 'hoverline');
            this.hoverline = this.hoverline.merge(hoverlineenter);
            this.hoverline.exit().remove();

            this.hoverline = this.hoverline.merge(hoverlineenter).attr('transform', `translate(${x},0)`).attr('index', index)
                .attr('y1', this.options.padding[0]).attr('y2', this.conHeight - this.options.padding[2]);
            var xtime = that.domainx[index];
            var pointsData = this.diagramData.series.map(function (d, i) {
                var value = d.data.find(function (d) {
                    return d[0] === xtime;
                });
                // console.log(value);
                // if (value === undefined) {
                //     if (d.data[index] !== undefined) value = d.data[index];
                //     else value = '';
                // }
                // else value = value[1]
                if (value === undefined) value = '';
                var yp = that.seriesData[i].data.find(function (d) {
                    return d[0] === xtime;
                });
                if (yp && Array.isArray(yp)) {
                    if (d.type.indexOf('area') !== -1) yp = yp[2];
                    else yp = yp[1];
                }
                else yp = '';
                var y = that.yScale(yp);

                if (Array.isArray(value) && value.length > 1) {
                    if (value.length === 2) value = value[1];
                    else value = value[2];
                }
                return {name: d.name, point: [x, y], value: value, unit: d.unit === undefined ? '' : d.unit, key: i};
            });
            this.tooltipData = pointsData;
            var activename = that.graphG.selectAll('.curveG.active1');
            if (activename.size()) {
                activename = activename.datum();
                activename = activename[0].name;
            } else activename = '';
            var pointsData1 = pointsData.filter(function (d) {
                if (activename) return activename === d.name && d.value !== '';
                return d.value !== '';
            });

            this.hoverpoints = this.auxiliarylineG.selectAll('.hoverpoints').data(pointsData1);
            var hoverpointsenter = this.hoverpoints.enter().append('circle').attr('class', 'hoverpoints');
            this.hoverpoints.exit().remove();

            this.hoverpoints = this.hoverpoints.merge(hoverpointsenter).attr('transform', `translate(${x},0)`)
                .attr('r', 3)
                .attr('cx', 0)
                .attr('cy', function (d1) {
                    return d1.point[1];
                })
                .attr('value', function (d1) {
                    return d1.value;
                })
                .attr('stroke', function (d1) {
                    var color = that.legendKey === 'index' ? that.legend[d1.key] : that.legend.find(function (d) {
                        return d.name === d1.name;
                    });
                    d3.select(this).classed('clr' + color.index, true);
                    return color.color ? color.color : 'black';
                })
                .on('click', function (d) {
                    d3.event.stopPropagation();
                    var currcurve = that.graphG.selectAll('.curveG').filter(function (dd) {
                        return dd[0].name === d.name;
                    });
                    if (currcurve) {
                        currcurve.dispatch('click');
                    }
                });
            this.tooltipFn(x, index, activename, mouse[1]);
        },
        tooltipFn: function (x, index, activename, y) {
            var that = this;
            this.tooltip = d3.select(this.container).selectAll('div.tooltip1').data(['tooltip1']);
            var tooltipenter = this.tooltip.enter().append('xhtml:div').attr('class', 'tooltip1');
            var isFull = that.getparents(this.content, 'onfull');
            if (activename === 'moveY') {
                this.tooltipTop(y);
                return false;
            }
            this.tooltip = this.tooltip.merge(tooltipenter).style('top', this.options.padding[0] + 10 + 'px')
                .html(function () {
                    var str = '';
                    if (that.xAxisType === 'time') {
                        str += `<div class="item time">${that.formatTime(that.xScale.invert(x))}</div>`;
                    } else if (that.xAxisType === 'category') {
                        str += `<div class="item time">${that.domainx[index]}</div>`;
                    }
                    var showarr = that.tooltipData.filter(function (d) {
                        if (d.value === '') return false;
                        if (activename && d.name !== activename) return false;
                        return true;
                    });
                    var maxlen = 0;
                    var minQuan = 4;
                    if (isFull) minQuan = 9;
                    showarr.sort(function (a, b) {
                        if (a.value === undefined || a.value === undefined) return 1;
                        return Number(b.value) - Number(a.value);
                    });
                    showarr.forEach(function (d, i) {
                        var color = that.legendKey === 'index' ? that.legend[d.key] : that.legend.find(function (d1) {
                            return d1.name === d.name;
                        });
                        if (i > minQuan) {
                            if (i === minQuan + 1) str += '<div class="item more">· · ·</div>';
                            else return;
                        }
                        else {
                            if (24 * (i + 3) > (that.rangey[0] - that.rangey[1])) {
                                if (!maxlen) {
                                    // str += '<div class="item more">· · ·</div>';
                                    maxlen = 24 * (i + 2);
                                }
                                return;
                            }
                            str += `<div class="item"><div class="colorblock ${'clr' + color.index}" clr="${'clr' + color.index}" style="background: ${color.color}"></div><div class="tit" title="${d.name}">${d.name}</div><div class="num">${d.value}${d.unit}</div></div>`;
                        }
                    });
                    if (maxlen) str = `<div class="legend3" style="max-height: ${maxlen}px;" >${str}</div>`;
                    else str = `<div class="legend3">${str}</div>`;
                    return str;
                })
                .style('left', x + 10 + 12 + 'px');
            if (this.jqueryDashboard) {
                this.tooltip.style('position', 'fixed');
            }
            var dashboardTransform = ['a', 'a'];
            this.dashboardPaddingLeft = 0;
            this.dashboardPaddingTop = -10;
            if (this.dashboardPortvar && this.dashboardPortvar.size()) {
                this.dashboardPaddingLeft = 12;
                this.dashboardPaddingTop = 40;
                var transform = this.dashboardPortvar.style('transform');
                var arr = transform.slice(10, transform.length - 1).split(',');
                if (arr.length >= 2) {
                    dashboardTransform[0] = parseFloat(arr[0]);
                    dashboardTransform[1] = parseFloat(arr[1]);
                }
            }
            this.dashboardTransform = dashboardTransform;

            this.tooltipLeft(x);
            this.tooltipTop(y);
        },
        tooltipLeft: function (x) {
            var that = this;
            if (!x) return false;
            var right = that.tooltip.node().offsetWidth;
            var jqueryDashboardWidth = 0;
            if (this.jqueryDashboard) jqueryDashboardWidth = this.jqueryDashboard.offsetWidth;
            if (x > that.conWidth / 2) { // 换向
                if (this.dashboardTransform[0] !== 'a') { // 左边
                    var left = this.dashboardTransform[0] + x + this.dashboardPaddingLeft - right - 10;
                    if (left < 0) {
                        // that.tooltip.style('left', -this.dashboardTransform[0] + 'px')
                        that.tooltip.style('left', x + 10 + 12 + 'px');
                    }
                    else that.tooltip.style('left', x + this.dashboardPaddingLeft - right - 10 + 'px');
                } else that.tooltip.style('left', x + this.dashboardPaddingLeft - right - 10 + 'px');
            } else {
                if (this.dashboardTransform[0] !== 'a' && jqueryDashboardWidth) { // 右边
                    var xright = this.dashboardTransform[0] + x + right;
                    if (xright > jqueryDashboardWidth) {
                        // that.tooltip.style('left', jqueryDashboardWidth - this.dashboardTransform[0] - right + 'px')
                        that.tooltip.style('left', x + this.dashboardPaddingLeft - right - 10 + 'px');
                    }
                }
            }
        },
        tooltipTop: function (posy) {
            var that = this;
            if (!posy) return false;
            var tooltipH = that.tooltip.node().offsetHeight;
            var y = posy - tooltipH + this.dashboardPaddingTop;
            if (this.dashboardTransform[1] !== 'a') {
                if (y + this.dashboardTransform[1] < 0) {
                    that.tooltip.style('top', -this.dashboardTransform[1] + 'px');
                } else that.tooltip.style('top', y + 'px');
            } else that.tooltip.style('top', y + 'px');
        },
        barbgFn: function () {
            var that = this;
            if (this.xAxisdata.brush) {
                var bgbarcount = 12;
                this.interval = (this.rangex[1] - this.rangex[0]) / (bgbarcount - 1);
                this.bargroupArr = [];
                for (var i = 0; i < bgbarcount; i++) {
                    if (i % 2 === 0) {
                        var start = this.interval * i + this.rangex[0];
                        var end = start + this.interval;
                        this.bargroupArr.push([start, end]);
                    }
                }
                this.hovergroupArr = this.domainx.map(function (d, i, arr) {
                    var start = 0;
                    var end = 0;
                    if (i === 0) start = that.xScale(d);
                    else start = (that.xScale(d) + that.xScale(arr[i - 1])) / 2;
                    if (i === arr.length - 1) end = that.xScale(d);
                    else end = (that.xScale(d) + that.xScale(arr[i + 1])) / 2;
                    return [start, end];
                });
            } else {
                this.interval = this.xScale.bandwidth();
                this.bargroupArr = this.domainx.map(function (d) {
                    var start = that.xScale(d);
                    return [start, start + that.interval];
                });
                this.hovergroupArr = this.domainx.map(function (d, i, arr) {
                    var start = 0;
                    var end = 0;
                    if (i === 0) start = that.xScale(d);
                    else start = (that.xScale(d) + that.xScale(arr[i - 1])) / 2 + that.interval / 2;
                    if (i === arr.length - 1) end = that.xScale(d) + that.interval;
                    else end = (that.xScale(d) + that.xScale(arr[i + 1])) / 2 + that.interval / 2;
                    return [start, end];
                });
            }

            this.auxiliarylineG = this.svg.selectAll('g.auxiliarylineG').data(['auxiliaryline']);
            var auxiliaryenter = this.auxiliarylineG.enter().append('g').attr('class', 'auxiliarylineG');
            this.auxiliarylineG = this.auxiliarylineG.merge(auxiliaryenter);

            that.auxiliarylineG.selectAll('line.legendline')
                .each(function () {
                    var index = d3.select(this).attr('index');
                    var x;
                    if (that.xAxisType === 'time') {
                        x = that.xScale(new Date(that.domainx[index]));
                    } else {
                        var x1 = that.hovergroupArr[that.hoverindexpro];
                        x = (x1[0] + x1[1]) / 2;
                    }
                    var classname = d3.select(this).attr('class');
                    d3.select(this).attr('transform', `translate(${x},0)`)
                        .attr('y1', that.options.padding[0]).attr('y2', that.conHeight - that.options.padding[2]);
                    if (classname === 'legendline') {
                        that.auxiliarylineG.selectAll('circle.legendpoints').attr('transform', `translate(${x},0)`);
                        that.auxiliarylineG.selectAll('circle.legendpoints').attr('transform', `translate(${x},0)`).attr('cy', function () {
                            return that.yScale(d3.select(this).attr('value'));
                        });
                    }
                });
            if (!this.diagramData.xAxis || !this.diagramData.xAxis.bar) return false;
            this.barbg = this.svg.selectAll('g.barbgG').data(['barbg']);
            var barbgenter = this.barbg.enter().append('g').attr('class', 'barbgG').lower();
            this.barbg = this.barbg.merge(barbgenter);
            var barbgrect = this.barbg.selectAll('.barbg').data(this.bargroupArr);
            var barbgrectenter = barbgrect.enter().append('rect').attr('class', 'barbg');
            if (this.interval < 0) this.interval = 0;
            barbgrect.merge(barbgrectenter)
                .attr('x', function (d) {
                    return d[0];
                })
                .attr('y', this.options.padding[0])
                .attr('width', this.interval)
                .attr('height', this.conHeight - this.options.padding[0] - this.options.padding[2]);

        },
        percentTranDecimal: function (percent) {
            if (typeof percent === 'string' && percent.indexOf('%') === percent.length - 1) {
                return parseFloat(percent) / 100;
            } else {
                return percent;
            }
        },
        getparents: function (ele, classname) {
            if (d3.select(ele).classed(classname)) return ele;
            var parent = ele.parentNode;
            if (ele === document.documentElement || ele.tagName === "HTML" || !ele || !parent) return undefined;
            if (d3.select(parent).classed(classname)) return parent;
            else return this.getparents(parent, classname);
        },
        formatTime: d3.timeFormat('%Y-%m-%d %H:%M:%S'),
        dividedLine: function () {
            var that = this;
            this.dividePoints = [];
            var curveArr = [];
            this.breakline();
            return false;
            if (Array.isArray(this.seriesData)) {
                this.seriesData.forEach(function (d) {
                    if (Array.isArray(d.data)) {
                        var arr = [];
                        d.data.forEach(function (d1, i, arr1) {
                            if (that.xAxisType === 'time' && typeof d1[0] === 'string') {
                                d1[0] = new Date(d1[0]).getTime();
                            }
                            if (arr1[i - 1] && ((d1[1] === '' && arr1[i - 1][1] !== ''))) {
                                arr.push(arr1[i - 1]);
                            }
                            arr.push(d1);
                            if (arr1[i + 1] && ((d1[1] === '' && arr1[i + 1][1] !== '') || (d1[1] !== '' && arr1[i + 1][1] === ''))) { // 为空条件
                                if (arr1[i + 1][1] !== '') arr.push(arr1[i + 1]);
                                var obj = {};
                                obj.points = arr;
                                obj.name = d.name;
                                if (d1[1] === '') obj.key = 'k';
                                else obj.key = 'fk';
                                curveArr.push(obj);
                                arr = [];
                            }
                            if (i === arr1.length - 1) {
                                var obj = {};
                                obj.points = arr;
                                obj.name = d.name;
                                if (d1[1] === '') obj.key = 'k';
                                else obj.key = 'fk';
                                curveArr.push(obj);
                                that.dividePoints.push(curveArr);
                                curveArr = [];
                            }
                        });
                    }
                });
            }
        },
        breakline: function () {
            var avgInterval = 0, tolerateInterval = 0, dividePoints = [];
            if (Array.isArray(this.seriesData)) {
                this.seriesData.forEach(function (points) {
                    var lineData = [];
                    var dataSegments = {
                        name: points.name,
                        points: [],
                        key: 'fk'
                    };
                    var pointArr = points.data.filter(function (point) {
                        return point[1] !== '';
                    });
                    if (!pointArr.length) return false;
                    avgInterval = (pointArr[pointArr.length - 1][0] - pointArr[0][0]) / pointArr.length;
                    tolerateInterval = avgInterval * 3.7;
                    for (var i = 0; i < pointArr.length; i++) {
                        if (i > 0 && (pointArr[i][0] - pointArr[i - 1][0]) > tolerateInterval) { // 断线
                            if (dataSegments.key === 'k') {
                                dataSegments.points.push(pointArr[i]);
                            } else if (dataSegments.key === 'fk') {
                                lineData.push(dataSegments);
                                dataSegments = {
                                    name: points.name,
                                    points: [pointArr[i - 1], pointArr[i]],
                                    key: 'k'
                                };
                            }
                        } else {
                            if (dataSegments.key === 'fk') {
                                dataSegments.points.push(pointArr[i]);
                            } else if (dataSegments.key === 'k') {
                                lineData.push(dataSegments);
                                dataSegments = {
                                    name: points.name,
                                    points: [pointArr[i - 1], pointArr[i]],
                                    key: 'fk'
                                };
                            }
                        }
                    }
                    if (dataSegments.points.length) {
                        lineData.push(dataSegments);
                    }
                    if (lineData.length) {
                        dividePoints.push(lineData);
                    }
                });
                this.dividePoints = dividePoints;
            }
        },
        hidedragarrow: function () {
            if (!this.legendDiv) return;
            var dragarrow = this.legendDiv.selectAll('.dragarrow');
            var dragline = this.legendDiv.selectAll('.dragbar');
            var graphBar = d3.select(this.content).select('.graph-bar');
            if (dragarrow.size()) {
                dragarrow.classed('hide', false).style('opacity', 1);
                dragline.classed('hide', false).style('opacity', 1);
                if (!dragarrow.classed('open')) graphBar.style('display', 'inline').style('opacity', 1);
                else graphBar.style('display', 'none').style('opacity', 0);
                if (this.dragarrowTime) clearTimeout(this.dragarrowTime);
                this.dragarrowTime = setTimeout(function () {
                    if (!dragarrow.classed('open')) graphBar.transition().duration(500).style('opacity', 0).on('end', function () {
                        graphBar.style('display', 'none').style('opacity', 0);
                    });
                }, 1000);
            }
        },
        resize: function () {
            this.conWidth = this.content.offsetWidth || this.options.width;
            this.conHeight = this.content.offsetHeight || this.options.height;
            this.conWidth = this.conWidth > this.options.padding[1] + this.options.padding[3] ? this.conWidth : this.options.padding[1] + this.options.padding[3];
            this.conHeight = this.conHeight > this.options.padding[0] + this.options.padding[2] ? this.conHeight : this.options.padding[0] + this.options.padding[2];
        },
        legendclose: function () {
            if (this.legendDiv) {
                var dragarrow = this.legendDiv.selectAll('div.dragarrow');
                if (dragarrow.classed('open')) {
                    dragarrow.dispatch('click');
                }
            }
        },
        legendopen: function () {
            if (this.legendDiv) {
                var dragarrow = this.legendDiv.selectAll('div.dragarrow');
                if (!dragarrow.classed('open')) {
                    dragarrow.dispatch('click');
                }
            }
        },
    });
    Tsdiagram.Diagram = Tsdiagram.Figures.extend({
        init: function (container, diagramData, options) {
            var ex;
            if (diagramData && Array.isArray(diagramData.series) && diagramData.series.length) {
                var isempty = diagramData.series.every(function (d) {
                    return (!d.data || !Array.isArray(d.data) || !d.data.length) && d.type.indexOf('gauge') === -1;
                });
                if (isempty) {
                    this.noData(container);
                    return false;
                }
                var isbar = diagramData.series.every(function (d) {
                    return d.type.indexOf('bar') !== -1;
                });
                var ispie = diagramData.series.every(function (d) {
                    return d.type.indexOf('pie') !== -1;
                });
                var iscurve = diagramData.series.every(function (d) {
                    return d.type.indexOf('curve') !== -1;
                });
                var isarea = diagramData.series.every(function (d) {
                    return d.type.indexOf('area') !== -1;
                });
                var isgauge = diagramData.series.every(function (d) {
                    return d.type.indexOf('gauge') !== -1;
                });
                var ispoints = diagramData.series.every(function (d) {
                    return d.type.indexOf('points') !== -1;
                });
                if (isbar) {
                    this.diagramType = 'bar'; // 柱
                    ex = this.pillar = new Tsdiagram.pillar(container, diagramData, options);
                } else if (ispie) {
                    this.diagramType = 'pie'; // 饼
                    ex = this.pie = new Tsdiagram.pie(container, diagramData, options);
                } else if (iscurve) {
                    this.diagramType = 'curve'; // 曲线
                    ex = this.curve = new Tsdiagram.curve(container, diagramData, options);
                } else if (isarea) {
                    this.diagramType = 'area'; // 面积
                    ex = this.area = new Tsdiagram.area(container, diagramData, options);
                } else if (isgauge) {
                    this.diagramType = 'gauge'; // 数值
                    ex = this.gauge = new Tsdiagram.numerical(container, diagramData, options);
                } else if (ispoints) {
                    this.diagramType = 'points'; // 散点图
                    ex = this.points = new Tsdiagram.point(container, diagramData, options);
                }
            } else {
                this.noData(container);
                return false;
            }
            if (container && $) {
                $(container).on('diagramresize', function () {
                    setTimeout(function () {
                        ex.resize();
                    }, 0);
                });
                $(container).on('legendclose', function () {
                    ex.legendclose();
                });
                $(container).on('legendopen', function () {
                    ex.legendopen();
                });
                $(container).on('diagramredraw', function (redata) {
                    ex.redraw(redata);
                });
            }
        },
        noData: function (container) {
            if (container) {
                d3.select(container).html(function () {
                    return `<div class="emptydata-box1"></div>`;
                });
                return false;
            } else {
                d3.select(container).select('.emptydata-box').remove();
            }
        },
        resize: function () {
            if (this.diagramType === 'bar') {
                this.pillar.resize();
            } else if (this.diagramType === 'pie') {
                this.pie.resize();
            } else if (this.diagramType === 'curve') {
                this.curve.resize();
            } else if (this.diagramType === 'gauge') {
                this.gauge.resize();
            } else if (this.diagramType === 'area') {
                this.area.resize();
            } else if (this.diagramType === 'points') {
                this.points.resize();
            }
        },
        redraw: function (reData) {
            if (this.diagramType === 'bar') { // 柱
                this.pillar.redraw(reData);
            } else if (this.diagramType === 'pie') {
                this.pie.redraw(reData);
            } else if (this.diagramType === 'curve') {
                this.curve.redraw(reData);
            } else if (this.diagramType === 'area') {
                this.area.redraw(reData);
            } else if (this.diagramType === 'gauge') {
                this.gauge.redraw(reData);
            } else if (this.diagramType === 'points') {
                this.points.redraw(reData);
            }
        }
    });
    window.Tsdiagram = Tsdiagram;

})(window);
