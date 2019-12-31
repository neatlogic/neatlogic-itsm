Tsdiagram.numerical = Tsdiagram.Figures.extend({
    init: function (container, diagramData, options) {
        var that = this;
        this._super(container, diagramData, options);
        this.svg.classed('gauge', true);
        this.options.padding = [0, 0, 0, 0];
        if (this.diagramData.xAxis && Array.isArray(this.diagramData.xAxis.data)) {
            this.domainx = this.diagramData.xAxis.data;
            this.domainx = this.domainx.map(function (d) {
                return new Date(d).getTime();
            });
            this.domainy = [0, d3.max(this.seriesData[0].data) * 2];
            this.appendDefs();
            this.drawCurve();
            return false;
        } else if (this.diagramData.series.length) {
            this.appendNumber();
            return;
        }
        this.dishradius = d3.min([this.conWidth, this.conHeight]) * 0.8 / 2;
        try {
            this.gaugevalue = Object.assign({}, this.diagramData.series[0].data[0]);
            that.gaugevalue.value = that.gaugevalue.value < 1 ? that.gaugevalue.value < 0 ? 0 : that.gaugevalue.value : that.gaugevalue.value <= 100 ? that.gaugevalue.value / 100 : 1;
        } catch (e) {
            this.gaugevalue = {value: 0, name: '完成率'};
            console.log('gauge value 不存在！');
            return;
        }

        this.arc = d3.arc();
        this.dishG = this.graphG.append('g').attr('class', 'dishG');

        this.dishdata = {
            innerRadius: this.dishradius - 20,
            outerRadius: this.dishradius,
            startAngle: -Math.PI / 2 - Math.PI / 8,
            endAngle: Math.PI / 2 + Math.PI / 8
        };
        this.colorinter = d3.interpolateLab('#5779f5', '#e0576c');
        this.appenddefs();
        this.drawtick();
        this.drawgauge();
    },
    appendDefs: function () {
        this.defs = this.svg.append('defs');
        var colors = this.defs.selectAll('.color').data([this.options.color[0]]);
        var enter = colors.enter().append('linearGradient')
            .attr('id', function (d, i) {
                return 'clrNumBg' + i;
            })
            .attr('x1', '0%')
            .attr('x2', '0%')
            .attr('y1', '0%')
            .attr('y2', '100%')
            .html(function (d) {
                return `<stop offset="0%" stop-color="${d}" stop-opacity="0.8"></stop><stop offset="100%" stop-color="${d}" stop-opacity="0.2"></stop>`;
            });
    },
    drawCurve: function () {
        this.rangex = [0, this.conWidth];
        this.xScale = this.scale('time', this.domainx, this.rangex);
        this.rangey = [this.conHeight, 0];
        this.yScale = this.scale('value', this.domainy, this.rangey);
        var that = this;
        if (!this.line) {
            // line
            this.line = d3.line()
                .x(function (d, i) {
                    return that.xScale(that.domainx[i]);
                })
                .y(function (d) {
                    return that.yScale(d);
                })
                .curve(d3.curveCatmullRom.alpha(0.5));
        }
        if (!this.area1) {
            this.area1 = d3.area()
                .x(function (d, i) {
                    return that.xScale(that.domainx[i]);
                })
                .y0(function (d) {
                    return that.yScale(0);
                })
                .y1(function (d) {
                    return that.yScale(d);
                })
                .curve(d3.curveCatmullRom.alpha(0.5));
        }
        var curveG = this.graphG.selectAll('.curveG').data(this.seriesData);
        var center = curveG.enter().append('g').attr('class', 'curveG');
        center.append('path').attr('class', 'curve');
        center.append('path').attr('class', 'area');
        center.append('rect').attr('class', 'overlay');
        curveG.merge(center).select('path.curve')
            .attr('d', function (d) {
                return that.line(d.data);
            })
            .attr('stroke', this.options.color[1]);
        curveG.merge(center).select('path.area')
            .attr('d', function (d) {
                return that.area1(d.data);
            })
            .attr('fill', 'url(#clrNumBg0)');
        curveG.merge(center).select('rect.overlay')
            .attr('width', that.conWidth)
            .attr('height', that.conHeight);
        this.appendNumber();
    },
    appendNumber: function () {
        var curveG = this.graphG.selectAll('.numberG').data(this.seriesData);
        var center = curveG.enter().append('g').attr('class', 'numberG');
        center
            .attr('cursor', 'pointer')
            .on('click', function (d) {
                if (d.url && (typeof top.addTab === 'function')) {
                    top.addTab(d.url);
                }
            });
        center.append('text')
            .attr('class', function (d) {
                if (d.color) return 'number custom';
                else return 'number';
            })
            .attr('x', 0)
            .attr('y', 0)
            .attr('dominant-baseline', 'central')
            .attr('text-anchor', 'middle')
            .attr('font-size', '35')
            .attr('fill', function (d) {
                if (d.color) return d.color;
                else return '#3166e6';
            });
        curveG.merge(center).select('text.number').text(function (d) {
            return d.value;
        });
        center.append('text')
            .attr('class', function (d) {
                if (d.color) return 'text custom';
                else return 'text';
            })
            .attr('x', 0)
            .attr('y', 0)
            .attr('dominant-baseline', 'middle')
            .attr('text-anchor', 'middle')
            .attr('font-size', '12')
            .attr('fill', function (d) {
                if (d.color) return d.color;
                else return '#999999';
            });
        curveG.merge(center).select('text.text').text(function (d) {
            return d.text;
        });
        curveG.merge(center).attr('transform', `translate(${this.conWidth / 2}, ${this.conHeight / 2})`)
            .attr('fill', 'white');
        var number = curveG.merge(center).select('text.number');
        var text = curveG.merge(center).select('text.text');
        number.attr('y', function () {
            var textbb = text.node().getBBox();
            if (textbb.height) return -textbb.height / 2;
            else return 0;
        });
        text.attr('y', function () {
            var textbb = number.node().getBBox();
            if (textbb.height) return textbb.height / 2;
            else return 0;
        });

        this.separateLineG = this.graphG.selectAll('g.lineG').data(['lineG']).enter().append('g').attr('class', 'lineG');
        var position = this.adjustScale();

    },
    adjustScale: function () {
        var that = this;
        var numG = this.graphG.selectAll('.numberG');

        var size = numG.size();
        var padding = 0.1;
        var width = that.conWidth * (1 - padding);
        var height = that.conHeight * (1 - padding);
        var paddingleft = this.conWidth * padding / 2;
        var paddingtop = this.conHeight * padding / 2;
        var positionArr = [];

        if (size === 1) {
            positionArr.push([width / 2 + paddingleft, paddingtop + height / 2]);
        } else if (size === 2) {
            positionArr.push(
                [width / 4 + paddingleft, paddingtop + height / 2],
                [width / 4 * 3 + paddingleft, paddingtop + height / 2]
            );
        } else if (size === 3) {
            var position1 = [width / 4 + paddingleft, paddingtop + height / 2];
            var position2 = [width / 4 * 3 + paddingleft, height / 4 + paddingtop];
            var position3 = [width / 4 * 3 + paddingleft, height / 4 * 3 + paddingtop];
            positionArr.push(position1, position2, position3);
        } else if (size === 4) {
            var position1 = [width / 4 + paddingleft, height / 4 + paddingtop];
            var position2 = [width / 4 * 3 + paddingleft, height / 4 + paddingtop];
            var position3 = [width / 4 + paddingleft, height / 4 * 3 + paddingtop];
            var position4 = [width / 4 * 3 + paddingleft, height / 4 * 3 + paddingtop];
            positionArr.push(position1, position2, position3, position4);
        } else if (size % 2 === 1) { // 奇数
            for (var i = 0; i < size; i++) {
                if (i === 0) positionArr.push([width / (size + 1) + paddingleft, paddingtop + height / 2]);
                else {
                    positionArr.push([width / (size + 1) * (Math.ceil(i / 2) * 2 + 1) + paddingleft, paddingtop + height / 4 * ((i + 1) % 2 ? 3 : 1) ]);
                }
            }
        } else if (size % 2 === 0) { // 偶数
            for (var i = 0; i < size; i++) {
                positionArr.push([width / (size) * (Math.ceil((i + 1) / 2) * 2 - 1) + paddingleft, paddingtop + height / 4 * ((i) % 2 ? 3 : 1) ]);
            }
        }

        var linePosition = [];
        var zLineCount = Math.ceil(positionArr.length / 2);
        for (var i = 1; i < zLineCount; i++) {
            linePosition.push({
                x1: paddingleft + width / zLineCount * i,
                x2: paddingleft + width / zLineCount * i,
                y1: paddingtop,
                y2: height + paddingtop
            })
        }
        if (size > 2) {
            if (size % 2 === 1) {
                linePosition.push({
                    x1: paddingleft + width / zLineCount,
                    x2: paddingleft + width,
                    y1: paddingtop + height / 2,
                    y2: paddingtop + height / 2,
                });
            } else {
                linePosition.push({
                    x1: paddingleft,
                    x2: paddingleft + width,
                    y1: paddingtop + height / 2,
                    y2: paddingtop + height / 2,
                });
            }
        } else if (size === 2) {
            linePosition.push({
                x1: paddingleft + width / 2,
                x2: paddingleft + width / 2,
                y1: paddingtop,
                y2: paddingtop + height,
            });
        }
        var line = this.separateLineG.selectAll('.borderLine').data(linePosition)
            .enter().append('line').attr('class', 'broderLine')
            .attr('stroke', '#ececec').attr('stroke-width', 0.8);

        line.each(function (d) {
            d3.select(this)
                .attr('x1', d.x1)
                .attr('x2', d.x2)
                .attr('y1', d.y1)
                .attr('y2', d.y2);
        });
        var minscale = 100;
        numG.each(function (d, i) {
            var bbox = this.getBBox();
            var scaleW = width / (size != 2 ? Math.ceil(size / 2) : 2) / bbox.width;
            var scaleH = height / (size != 2 ? Math.ceil(size / 2) : 2) / bbox.height;
            // scaleH = height / bbox.height;
            var scale = d3.min([scaleW, scaleH]);
            minscale = minscale > scale ? scale : minscale;
        });
        numG.each(function (d, i) {
            d3.select(this).attr('transform', `translate(${positionArr[i][0]},${positionArr[i][1]})scale(${minscale * 0.8})`);
        });
        var numBb = numG.node().getBBox();
        var scale1 = 1, scale2 = 1;
        if (numBb.width + 100 > this.conWidth) {
            scale1 = this.conWidth / (numBb.width + 100);
        }
        if (numBb.height + 40 > this.conHeight) {
            scale2 = this.conHeight / (numBb.height + 40);
        }
        if (scale1 < 1 || scale2 < 1) {
            numG.attr('transform', `translate(${this.conWidth / 2},${this.conHeight / 2})scale(${d3.min([scale1, scale2])})`);
        }

        return positionArr;
    },
    drawgauge: function () {
        // 画仪表盘
        var that = this;
        this.dishG.attr('transform', `translate(${this.conWidth / 2},${this.conHeight / 2})`);
        this.dish = this.dishG.selectAll('path.dish').data(['dish']);
        var enterpath = this.dish.enter().append('path').attr('class', 'dish').attr('fill', `url(#${this.dishgradualid})`);
        this.dish = this.dish.merge(enterpath).attr('d', this.arc(this.dishdata));
        // 画辅助盘
        var assistdishd = Object.assign({}, this.dishdata);
        assistdishd.innerRadius = assistdishd.innerRadius - 0.2;
        assistdishd.outerRadius = assistdishd.outerRadius + 0.2;
        assistdishd.startAngle = that.dishdata.startAngle + (-that.dishdata.startAngle + that.dishdata.endAngle) * that.gaugevalue.value;

        var assistpath = this.dishG.selectAll('path.assistpath').data(['assistpath']);
        var assistpathenter = assistpath.enter().append('path').attr('class', 'assistpath').attr('fill', '#e4e5ed');
        assistpath.merge(assistpathenter).attr('d', this.arc(assistdishd));

        // 圆角处理
        var circlebead = this.dishG.selectAll('circle.bead').data(['left', 'right', 'center']);
        var beadenter = circlebead.enter().append('circle').attr('class', function (d) {
            return `bead ${d}`;
        }).attr('r', 10);
        circlebead.merge(beadenter).style('transform', function (d) {
            if (d === 'left') return `rotate(${that.dishdata.startAngle - Math.PI / 2}rad)translate(${(that.dishdata.innerRadius + that.dishdata.outerRadius) / 2}px,0)`;
            if (d === 'right') return `rotate(${that.dishdata.endAngle - Math.PI / 2}rad)translate(${(that.dishdata.innerRadius + that.dishdata.outerRadius) / 2}px,0)`;
            if (d === 'center') return `rotate(${assistdishd.startAngle - Math.PI / 2}rad)translate(${(that.dishdata.innerRadius + that.dishdata.outerRadius) / 2}px,0)`;
        })
            .attr('fill', function (d) {
                if (d === 'left') return '#5779f5';
                if (d === 'right') return '#e4e5ed';
                var colorinter = that.colorinter(that.gaugevalue.value);
                if (d === 'center') return colorinter;
            })
            .filter('.center');
        // .attr('clip-path', `url(#${this.dishclipid})`)
        var currspoint = this.dishG.selectAll('circle.currspoint').data(['currspoint']);
        var currspointenter = currspoint.enter().append('circle').attr('class', 'currspoint').attr('r', 2).attr('fill', '#ffffff');
        currspoint.merge(currspointenter).style('transform', function () {
            return `rotate(${assistdishd.startAngle - Math.PI / 2}rad)translate(${(that.dishdata.innerRadius + that.dishdata.outerRadius) / 2}px,0)`;
        });
        // 纠正指针的角度
        this.dirpointer.transition().duration(300).style('transform', function () {
            var angle = that.dishdata.startAngle - Math.PI / 2 + (-that.dishdata.startAngle + that.dishdata.endAngle) * that.gaugevalue.value;
            return `rotate(${angle}rad)`;
        });

        // 画百分比
        this.gaugetext = this.dishG.selectAll('.gaugetext').data([1]);
        var enter = this.gaugetext.enter().append('text').attr('class', 'gaugetext').attr('transform', `translate(0,${this.dishdata.innerRadius})`);
        this.gaugetext.attr('transform', `translate(0,${this.dishdata.innerRadius})`);
        var tspan = this.gaugetext.merge(enter).selectAll('tspan').data([this.diagramData.series[0].data[0].value, '%']);
        var tspanenter = tspan.enter().append('tspan');
        tspan.merge(tspanenter)
            .text(function (d) {
                return d;
            })
            .attr('class', function (d) {
                if (d === '%') return 'stext';
                return 'btext';
            })
            .attr('dy', function (d) {
                if (d === '%') return -10;
                return 0;
            });
    },
    drawtick: function () {
        var that = this;
        var tickpad = Math.PI / 21;
        var tickarr = [];
        for (var i = 0; i < 22; i++) {
            tickarr.push(-Math.PI + tickpad * i);
        }
        // 画中心圆点
        this.cenpoint = this.dishG.selectAll('.cenpoint').data([1])
            .enter()
            .append('circle')
            .attr('class', 'cenpoint')
            .attr('r', 10)
            .attr('rx', this.conWidth / 2)
            .attr('ry', this.conHeight / 2);
        // 画刻度
        var tick = this.dishG.selectAll('g.ticks').data(['ticks']);
        var enter = tick.enter().append('g').attr('class', 'ticks');
        var tickrect = tick.merge(enter).selectAll('.tick').data(tickarr);
        var tickrectenter = tickrect.enter().append('rect').attr('class', 'tick').attr('width', function (d, i) {
            return i % 3 === 0 ? 9 : 5;
        })
            .attr('height', function (d, i) {
                return i % 3 === 0 ? 3 : 2;
            });
        tickrect.merge(tickrectenter).style('transform', function (d) {
            return `rotate(${d}rad)translate(${that.dishdata.innerRadius}px,0)`;
        })
            .style('opacity', 0)
            .transition()
            .duration(function (d, i) {
                if (that.state === 'resizestart') return 0;
                return 500 + i * 20;
            })
            .style('transform', function (d, i) {
                if (i % 3 === 0) return `rotate(${d}rad)translate(${that.dishdata.innerRadius - 18}px,0)`;
                return `rotate(${d}rad)translate(${that.dishdata.innerRadius - 15}px,0)`;
            })
            .style('opacity', 1);
        // 画指针
        this.dirpointer = this.dishG.selectAll('.dirpointer').data(['dirpointer']);
        var dirpointerenter = this.dirpointer.enter().append('polygon')
            .attr('class', 'dirpointer');
        this.dirpointer = this.dirpointer.merge(dirpointerenter).attr('points', function () {
            if (that.dishdata.innerRadius - 34 <= 0) return `-6,0`;
            var points = `-6,0 -4,5, ${that.dishdata.innerRadius - 34},3 ${that.dishdata.innerRadius - 30},0 ${that.dishdata.innerRadius - 34},-3 -4,-5`;
            return points;
        })
            .attr('transform', function () {
                return `rotate(-180)`;
            });
        // 画点
        var cenpoint1 = this.dishG.selectAll('.cenpoint1').data(['cenpoint1']);
        var enter1 = cenpoint1.enter().append('circle').attr('class', 'cenpoint1');
        cenpoint1.merge(enter1).attr('r', 2).attr('fill', '#e4e5ed');

    },
    appenddefs: function () {
        this.dishgradualid = this.getuuid();
        this.dishclipid = this.getuuid();
        var defs = this.svg.selectAll('defs').data([1]);
        var enter = defs.enter().append('defs');
        enter.append('linearGradient')
            .attr('id', this.dishgradualid)
            .attr('x1', '0%')
            .attr('x2', '100%')
            .attr('y1', '0%')
            .attr('y2', '0%')
            .html(function () {
                return '<stop offset="0%" stop-color="#5779f5"/><stop offset="100%" stop-color="#e0576c"/>';
            });
        enter.append('clipPath')
            .attr('id', this.dishclipid)
            .append('rect')
            .attr('x', -10)
            .attr('y', -1)
            .attr('width', 20)
            .attr('height', 11);
    },
    getuuid: function () {
        return 'xxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    },
    resize: function () {
        this.conWidth = this.container.offsetWidth;
        this.conHeight = this.container.offsetHeight;
        if (this.diagramData.xAxis && Array.isArray(this.diagramData.xAxis.data)) {
            this.drawCurve();
            return false;
        }
        this.state = 'resizestart';
        this.redraw(this.diagramData);
        this.state = 'resizeend';
    },
    redraw: function (redata) {
        if (!redata) return;
        this.diagramData = redata;
        if (redata && redata.xAxis && redata.xAxis.data) {
            this.dealSeries();
            this.domainx = this.diagramData.xAxis.data;
            this.domainx = this.domainx.map(function (d) {
                return new Date(d).getTime();
            });
            this.drawCurve();
            return false;
        } else if (redata.series && redata.series.length > 2) {
            this.appendNumber();
            return false;
        }
        var that = this;
        this.dishradius = d3.min([this.conWidth, this.conHeight]) * 0.8 / 2;
        try {
            this.gaugevalue = Object.assign({}, this.diagramData.series[0].data[0]);
            that.gaugevalue.value = that.gaugevalue.value < 1 ? that.gaugevalue.value < 0 ? 0 : that.gaugevalue.value : that.gaugevalue.value <= 100 ? that.gaugevalue.value / 100 : 1;
        } catch (e) {
            this.gaugevalue = {value: 0, name: '完成率'};
            console.log('gauge value 不存在！');
            return;
        }
        this.dishdata = {
            innerRadius: this.dishradius - 20,
            outerRadius: this.dishradius,
            startAngle: -Math.PI / 2 - Math.PI / 8,
            endAngle: Math.PI / 2 + Math.PI / 8
        };
        this.drawtick();
        this.drawgauge();
    },
});