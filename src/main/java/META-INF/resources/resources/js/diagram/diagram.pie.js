(function () {
    Tsdiagram.pie = Tsdiagram.Figures.extend({
        init: function (container, diagramData, opts) {
            var that = this;
            this._super(container, diagramData, opts);

            this.svg.classed('pie', true);
            // center point
            this.options.padding = [0, 0, 0, 0];
            that.options.flexlegendw = that.conWidth * 0.382;
            if (that.options.flexlegendw < 60) that.options.flexlegendw = 60;

            if (this.diagramData.hasOwnProperty('legend')) {
                if (this.diagramData.legend.hasOwnProperty('data')) {
                    this.spadl = this.options.padding[3];
                    this.options.padding[3] = this.options.padding[3] + this.options.flexlegendw;
                }
            }
            if (this.diagramData.legend && this.diagramData.legend.straw) this.straw = true;
            if (this.diagramData.hasOwnProperty('title') && this.diagramData.title.name) {
                this.title(this.diagramData.title);
            }
         
            this.strawText = (this.diagramData.legend && this.diagramData.legend.strawText  !="undefined")  ? this.diagramData.legend.strawText : true; // label是否显示
            this.strawValue = this.diagramData.legend && this.diagramData.legend.strawValue; // label显示百分比
            this.strawRatio = this.diagramData.legend && this.diagramData.legend.strawRatio; // label显示百分比
            
            if (this.strawText === false && this.strawRatio === false && this.strawValue === false) this.straw = false;

            this.periphery = this.diagramData.legend && this.diagramData.legend.periphery || 1; // 圆周大小

            if (this.diagramData.legend && this.diagramData.legend.padAngle) {
                this.padAngle = this.diagramData.legend.padAngle || 0; // 圆padding angle
            }

            this.pieDataHandle();
            // legend
            if (this.diagramData.hasOwnProperty('legend')) {
                if (this.diagramData.legend.hasOwnProperty('data')) {
                    this.legendFn(that.legend);
                    this.options.flexlegendw = this.legendDiv.node().offsetWidth;
                    this.options.padding[3] = this.spadl + this.options.flexlegendw;
                    this.hidedragarrow();
                }
            }
            // draw
            that.drawLoop(that.pieData, 600);

            d3.select(this.container).on('mousemove', function () {
                that.hidedragarrow();
            });
            if (this.diagramData.legend && this.diagramData.legend.show) {
                this.legendopen();
            } else {
                this.legendclose();
            }
            this.adjustment();
            if (that.legendDiv && that.legendDiv.node()) {
                var bar = that.legendDiv.select('.legscroll');
                if (bar.size()) {
                    bar.classed('hide', true);
                }
                d3.select(this.container)
                    .on('mouseenter', function () {
                        if (bar.size()) {
                            bar.classed('hide', false);
                        }
                    })
                    .on('mouseleave', function () {
                        if (bar.size()) {
                            bar.classed('hide', true);
                        }
                    });
            }
        },
        adjustment: function (tsti) {
            tsti = tsti !== undefined ? 0 : 0;
            var that = this;
            var width = this.container.offsetWidth;
            var height = this.container.offsetHeight;
            var scalex = 1, scaley = 1;
            if ((width - this.options.padding[3] - this.options.padding[1]) * 1.25 < that.options.width) {
                if (this.options.padding[3] === 0) scalex = (width - this.options.padding[3] - this.options.padding[1]) / that.options.width;
                else scalex = (width - this.options.padding[3] - this.options.padding[1]) * 1.25 / that.options.width;
            }
            if ((height - this.options.padding[0] - this.options.padding[2]) * 1.25 < that.options.height) {
                if (this.options.padding[3] === 0) scaley = (height - this.options.padding[0] - this.options.padding[2]) / that.options.height;
                else scaley = (height - this.options.padding[0] - this.options.padding[2]) * 1.25 / that.options.height;
            }

            var bbox = this.pieG.node().getBBox();
            var bboxScaleW = 1, bboxScaleH = 1;
            this.pieG.selectAll('.strawlegend .label').each(function () {
                var labelbbox = this.getBBox();
                if (-bbox.x > width / 2) bboxScaleW = width / 2 / -bbox.x * 0.96;
                else if (bbox.x + bbox.width > width / 2) bboxScaleW = width / 2 / (bbox.x + bbox.width) * 0.96;
                if (-bbox.y > height / 2) bboxScaleH = height / 2 / -bbox.y * 0.96;
                else if (bbox.y + bbox.height > width / 2) bboxScaleH = height / 2 / (bbox.y + bbox.height) * 0.96;
            });

            var minscale = d3.min([scalex, scaley, bboxScaleW, bboxScaleH]);

            if (minscale < 1) {
                this.pieG.attr('transform', `translate(${this.cx},${this.cy})scale(${minscale})`)
                //     .selectAll('.strawlegend text').style('font-size', function () {
                //     if (that.diagramData.legend && that.diagramData.legend.strawFont) {
                //         if (typeof that.diagramData.legend.strawFont === 'number') return that.diagramData.legend.strawFont / minscale + 'px';
                //         else if (typeof that.diagramData.legend.strawFont === 'string') return that.diagramData.legend.strawFont;
                //     } else return 12 / minscale + 'px';
                //     return 12 / minscale + 'px';
                // });
            } else {
                this.pieG
                    .selectAll('.strawlegend text').style('font-size', function () {
                    if (that.diagramData.legend && that.diagramData.legend.strawFont) {
                        if (typeof that.diagramData.legend.strawFont === 'number') return that.diagramData.legend.strawFont + 'px';
                        else if (typeof that.diagramData.legend.strawFont === 'string') return that.diagramData.legend.strawFont;
                    } else return '12px';
                });
            }
        },
        pieDataHandle: function () {
            var that = this;
            if (!this.pie) {
                // pie
                this.pie = d3.pie()
                    .padAngle((this.padAngle || 0) / 360 * 2 * Math.PI)
                    .startAngle(-Math.PI * (this.periphery || 1))
                    .endAngle(Math.PI * (this.periphery || 1))
                    .sort(function () {
                        return false;
                    })
                    .value(function (d) {
                        return d.value;
                    });
            }
            var width = this.conWidth < this.options.width ? this.options.width : this.conWidth;
            var height = this.conHeight < this.options.height ? this.options.height : this.conHeight;
            var options = this.diagramData.series.find(function (d) {
                return d.hasOwnProperty('itemStyle');
            });
            if (options) options = options.itemStyle;
            else options = {};
            options.outerLoopRadius = options.outerLoopRadius ? options.outerLoopRadius : that.options.outerLoopRadius;
            options.loopInterval = options.loopInterval ? options.loopInterval : that.options.loopInterval;
            options.loopWidth = options.loopWidth ? options.loopWidth : that.options.loopWidth;
            options.piePadding = options.piePadding ? options.piePadding : that.options.piePadding;
            options.outerLoopRadius = this.percentTranDecimal(options.outerLoopRadius);
            options.loopInterval = this.percentTranDecimal(options.loopInterval);
            options.piePadding = this.percentTranDecimal(options.piePadding);

            var loopCount = this.diagramData.series.length;
            this.pieData = [];
            var maxRadius = d3.min([(width - that.options.padding[3]) * 0.8, height + height / 2 * (1 - Math.cos(Math.PI * (1 - this.periphery)))]) / 2 * options.outerLoopRadius;
            var colorIndex = 0;
            var deopacity = 1;
            this.diagramData.series.forEach(function (pie, i) {
                var outRadius = maxRadius;
                var loopWidth = options.loopWidth;
                var loopInterval = outRadius * options.loopInterval;
                var j = loopCount - 1 - i;

                while (j > 0) {
                    loopWidth = loopWidth * 0.9;
                    loopInterval = loopInterval * 0.9;
                    outRadius -= (loopWidth + loopInterval);
                    j--;
                }
                var obj = {
                    pie: that.pie(pie.data),
                    outRadius: outRadius,
                    innerRadius: outRadius - loopWidth,
                    total: pie.data.reduce(function (a, b) {
                        return a + b.value;
                    }, 0)
                };
                if (pie.radius) {
                    obj.outRadius = d3.min([width - that.options.padding[3], height]) / 2 * that.percentTranDecimal(pie.radius);
                    obj.innerRadius = 0;
                }
                if (pie.hasOwnProperty('sum')) {
                    obj.sum = pie.sum;
                }
                if (i === 0) {
                    obj.pie.forEach(function (d) {
                        if (colorIndex > that.options.color.length - 1) {
                            colorIndex = 0;
                            deopacity -= 0.1;
                        }
                        var steelblue = d3.color(that.options.color[colorIndex]);
                        steelblue.opacity = deopacity;
                        d.color = that.options.color[colorIndex];
                        d.opacity = deopacity;
                        colorIndex++;
                    });
                    that.pieData.push(obj);
                } else { // i === 1
                    obj.pie.forEach(function (d) {
                        var pieIndex = that.pieData[0].pie.find(function (dd) {
                            return (d.startAngle + d.endAngle) / 2 < dd.endAngle && (d.startAngle + d.endAngle) / 2 > dd.startAngle;
                        });
                        d.color = pieIndex.color;
                        d.opacity = 1;
                        d.cindex = pieIndex.index;
                    });
                    that.pieData.push(obj);
                }
            });
            if (loopCount === 2) {
                // opacity
                that.pieData[0].pie.forEach(function (d) {
                    var index = d.index;
                    var ab = that.pieData[1].pie.reduce(function (a, b) {
                        if (b.cindex === index) return a += 1;
                        else return a;
                    }, 0);
                    d.cindex = ab;
                });
                var sum = [0, 0];
                that.pieData[1].pie.forEach(function (d, i) {
                    var ab = that.pieData[0].pie.find(function (dd) {
                        return d.cindex === dd.index;
                    });
                    if (ab) {
                        var cindex = ab.cindex;
                        if (sum[0] !== cindex) {
                            sum[1] += cindex;
                            sum[0] = cindex;
                        }
                        if (sum[0] !== 1) {
                            d.opacity = 1 - 0.4 / (sum[0] - 1) * (sum[1] - i); // (sum[1] - 1 - i)
                        }
                    }
                });
            }
            // legend
            this.legend = [];
            // var deopacity = 1
            that.pieData.forEach(function (d) {
                d.pie.forEach(function (d1) {
                    var obj = {
                        name: d1.data.name,
                        color: d1.color,
                        opacity: d1.opacity,
                        value: d1.data.value,
                        ratio: d1.value / d.total,
                        index: d1.index
                    };
                    d1.ratio = obj.ratio;
                    var is = that.legend.find(function (d2) {
                        return d2.name === obj.name;
                    });
                    if (!is) that.legend.push(obj);
                });
            });
        },
        drawLoop: function (data, tsti) {

            var that = this;
            this.cx = (this.conWidth - this.options.padding[1] - this.options.padding[3]) / 2 + this.options.padding[3];
            this.cy = (this.conHeight - this.options.padding[0] - this.options.padding[2]) / 2  + data[data.length - 1].outRadius * (1 - Math.cos((1 - this.periphery) * Math.PI)) / 2 + this.options.padding[0];
            // pieG
            var pieG = that.graphG.selectAll('g.pieG').data(['pieG']);
            var pieGenter = pieG.enter().append('g')
                .attr('class', 'pieG');
            this.pieG = pieG.merge(pieGenter).attr('transform', 'translate(' + that.cx + ',' + that.cy + ')');
            // loop g
            var gPie = this.pieG.selectAll('g.loop').data(data);
            var enterloop = gPie.enter().append('g')
                .attr('class', 'loop');
            gPie.exit().remove();

            gPie.merge(enterloop)
                .each(function (pie) {
                    var or = pie.outRadius;
                    var ir = pie.innerRadius;
                    var pathvalue = pie.pie.filter(function (d) {
                        return d.value !== 0;
                    });
                    var pathsG = d3.select(this).selectAll('g.pathG').data(pathvalue);
                    var enterpaths = pathsG.enter()
                        .append('g').attr('class', 'pathG');
                    enterpaths.append('path');
                    pathsG.exit().remove();

                    function arcTween (a) {
                        var i = d3.interpolate(this._current, a);
                        this._current = i(0);
                        var x = ir / or;
                        return function (t) {
                            return that.pieTop(i(t), or, or, x);
                        };
                    }

                    pathsG.merge(enterpaths).each(function (d1, i1, arr1) {
                        var pieg = d3.select(this);
                        pieg.select('path').datum(d1).attr('fill', function (d, i) {
                            var color = that.legend.find(function (dd) {
                                return dd.name === d.data.name;
                            });
                            pieg.classed('clr' + color.index % that.options.color.length, true);
                            return color.color;
                        })
                            .attr('d', function (d) {
                                return 'M 0 0';
                            })
                            .attr('opacity', function (d) {
                                return d.opacity;
                            })
                            .each(function (d) {
                                this._current = d;
                            })
                            .on('mouseover', function () {
                                d3.select(this).classed('trans', true).attr('d', function (d) {
                                    var x = ir / (or + 8);
                                    return that.pieTop(d, or + 6, or + 6, x);
                                });
                                var currData = d3.select(this).datum();
                                that.tooltipData = [currData.data];
                                if (that.tooltip) that.tooltip.style('display', 'inline');
                                if (that.legendDiv) {
                                    var curritem = that.legendDiv.selectAll('div.item').filter(function () {
                                        var name = d3.select(this).select('div.tit').text();
                                        return name === currData.data.name;
                                    });
                                    if (curritem) curritem.classed('active', true);
                                }
                            })
                            .on('mouseout', function (d) {
                                if (d3.select(this.parentNode).classed('cative')) return false;
                                d3.select(this).attr('d', function (d) {
                                    var x = ir / or;
                                    return that.pieTop(d, or, or, x);
                                }).classed('trans', false);
                                if (that.tooltip) that.tooltip.style('display', 'none');
                                if (that.legendDiv) {
                                    that.legendDiv.selectAll('div.item').classed('active', false);
                                }
                            })
                            .on('mousemove', function () {
                                that.tooltipFn();
                            })
                            .transition().duration(tsti)
                            .attrTween('d', function (d) {
                                return arcTween(d);
                            })
                            .on('end', function (d, i, arr) {
                                if (i1 === arr1.length - 1) {
                                    if (that.straw) {
                                        that.strawLegend(that.legend);
                                    }
                                    that.adjustment();
                                }
                            });
                    });
                });
            var sum = data.find(function (d) {
                return d.hasOwnProperty('sum');
            });
            if (sum) {
                var textsum = this.pieG.selectAll('.sum').data([sum.sum]);
                var textsumenter = textsum.enter().append('g').attr('class', 'sum');
                textsumenter.append('text').text(function (d) {
                    return d.value;
                }).attr('text-anchor', 'middle').attr('dy', '0.32em');
                textsum.merge(textsumenter)
                    .attr('font-size', function (d) {
                        return d.fontSize || 46;
                    })
                    .each(function (d) {
                        if (d.color) {
                            d3.select(this).selectAll('text').style('fill', function (d) {
                                return d.color || 'black';
                            });
                        }
                    })
            }
        },
        strawLegend: function () {
            var that = this;
            var strawdata = this.pieData[this.pieData.length - 1];
            var gPieLabel = this.pieG.selectAll('g.strawlegend').data(['strawG']);
            var gPieLabelenter = gPieLabel.enter().append('g').attr('class', 'strawlegend');

            gPieLabel = gPieLabel.merge(gPieLabelenter);
            // gPieLabel.attr("transform", "translate(" + this.cx + "," + this.cy + ")");
            var arc1 = d3.arc().outerRadius(strawdata.outRadius)
                .innerRadius(strawdata.outRadius)
                .startAngle(function (d) {
                    return d.startAngle;
                })
                .endAngle(function (d) {
                    return d.endAngle;
                });
            var arc2 = d3.arc().outerRadius(strawdata.outRadius + 16 * 2)
                .innerRadius(strawdata.outRadius)
                .startAngle(function (d) {
                    return d.startAngle;
                })
                .endAngle(function (d) {
                    return d.endAngle;
                });
            var strawvalue = strawdata.pie.filter(function (d) {
                return d.value !== 0;
            });

            if (this.diagramData.legend && this.diagramData.legend.strawCount && this.diagramData.legend.strawCount < strawvalue.length) {
                strawvalue = strawvalue.sort(function ( a, b ) {
                    return b.data.value - a.data.value;
                }).filter(function (d, i) {
                    return i < that.diagramData.legend.strawCount;
                });
            }
            var labels = gPieLabel.selectAll('g.label').data(strawvalue);
            var labelsenter = labels.enter().append('g').attr('class', 'label');
            labelsenter.append('path');
            labelsenter.append('text').attr('class', 'name')
                .style('font-size', function () {
                    if (that.diagramData.legend && that.diagramData.legend.strawFont) {
                        if (typeof that.diagramData.legend.strawFont === 'number') return that.diagramData.legend.strawFont + 'px';
                        else if (typeof that.diagramData.legend.strawFont === 'string') return that.diagramData.legend.strawFont;
                    }
                    else return 12 + 'px';
                });
            // .text(function (d) {
            //   return d.data.name + d.data.value;
            // });
            // labelsenter.append('text').attr('class', 'value').text(function (d) {
            //   return d.data.value;
            // })
            labels = labels.merge(labelsenter);
            labels.each(function (d) {
                var centerPoint = arc1.centroid(d);
                var centerPoint2 = arc2.centroid(d);
                var isRight = centerPoint[0] < centerPoint2[0];
                var endPoint = isRight ? [centerPoint2[0] + 10, centerPoint2[1]] : [centerPoint2[0] - 10, centerPoint2[1]];
                var isLevel = Math.abs(centerPoint2[1] - centerPoint[1]) < 10;
                var g = d3.select(this);
                g.select('path')
                    .attr('d', function () {
                        if (isLevel) return `M${centerPoint.join(',')} L${centerPoint2.join(',')}`;
                        return `M${centerPoint.join(',')} L${centerPoint2.join(',')} L${endPoint.join(',')}`;
                    })
                    .attr('stroke', function (d) {
                        g.classed('clr' + d.index % that.options.color.length, true);
                        return d.color;
                    });
                var text = d3.select(this).select('text.name')
                    .text(function (d) {
                        // if (!isRight) return  (that.strawValue ? d.data.value + (d.data.unit || '') : '') + ' ' + d.data.name;
                        return (that.strawText != false ? d.data.name : '') + ' ' + (that.strawValue != false ? d.data.value + (d.data.unit || '') : '');
                    })
                    .each(function (d) {
                        var _this = this;
                        // d3.select(this).append('tspan')
                        //     .attr('x', 0)
                        //     .attr('y', 0)
                        //     .attr('dx', 0)
                        //     .attr('dy', 0)
                        //     .text(function () {
                        //         // if (!isRight) return  (that.strawValue ? d.data.value + (d.data.unit || '') : '') + ' ' + d.data.name;
                        //         return d.data.name + ' ' + (that.strawValue ? d.data.value + (d.data.unit || '') : '');
                        //     })
                        if (that.strawRatio) {
                        	var ratio = d.ratio * 100;
                        	var reg = /\d+\.\d{2,}/;
                        	ratio = (reg.test(ratio))?ratio.toFixed(2):ratio;
                        	
                            if (!that.strawText && !that.strawValue) d3.select(this).text(ratio + '%');
                            else d3.select(this).append('tspan')
                                .attr('x', 0)
                                .attr('y', 0)
                                .attr('dy', '1.33em')
                                .attr('dx', function () {
                                    if (!isRight) return -5;
                                    d3.select(this).style('text-anchor', 'end');
                                    return _this.getBBox().width - 5;
                                })
                                .text(ratio + '%')
                        }
                    })
                    .attr('transform', function () {
                        var classname = isRight ? 'textSta' : 'textEnd';
                        d3.select(this).classed(classname, true);
                        if (isLevel) return `translate(${isRight ? centerPoint2[0] + 2 : centerPoint2[0] - 2},${endPoint[1]})`;
                        return `translate(${isRight ? endPoint[0] + 2 : endPoint[0] - 2},${endPoint[1]})`;
                    });
                if (text.size()) {
                    var textBBox = text.node().getBBox();
                    var str = d.data.name + ' ' + d.data.value;
                    // if (textBBox.width > 70 && isLevel) {
                    //     var textArr = str.split('');
                    //     text.text('').append('tspan').text(d.data.name).attr('x', 0);
                    //     text.append('tspan').text(d.data.value).attr('x', 0).attr('y', '1.37em')
                    // }
                }

                // .style('fill', function (d) {
                //   return d.color;
                // })

                // d3.select(this).select('text.value')
                //   .attr('x', function () {
                //     var classname = isRight ? 'textSta' : 'textEnd';
                //     d3.select(this).classed(classname, true);
                //     return isRight ? endPoint[0] + 4 : endPoint[0] - 4;
                //   })
                //   .attr('y', endPoint[1] - 16)
            });

            if (!this.legendDiv) return false;
            var dragarrow = this.legendDiv.select('.dragarrow');
            if (dragarrow.size()) {
                if (dragarrow.classed('open')) {
                    gPieLabel.classed('hide', true);
                } else {
                    gPieLabel.classed('hide', false);
                }
            }
        },
        pieTop: function (d, rx, ry, ir) {
            if (d.endAngle - d.startAngle == 0) return 'M 0 0';
            // var sx = rx * Math.cos(d.startAngle),
            //     sy = ry * Math.sin(d.startAngle),
            //     ex = rx * Math.cos(d.endAngle),
            //     ey = ry * Math.sin(d.endAngle);
			//
            // var ret = [];
            // ret.push('M', sx, sy, 'A', rx, ry, '0', (d.endAngle - d.startAngle > Math.PI ? 1 : 0), '1', ex, ey, 'L', ir * ex, ir * ey);
            // ret.push('A', ir * rx, ir * ry, '0', (d.endAngle - d.startAngle > Math.PI ? 1 : 0), '0', ir * sx, ir * sy, 'z');
            // return ret.join(' ');
            if (!this.pieArc) this.pieArc = d3.arc();
			return this.pieArc({
				innerRadius: ir * rx,
				outerRadius: rx,
				startAngle: d.startAngle,
				endAngle: d.endAngle,
                padAngle: d.padAngle || 0,
			})
        },
        transition: function (data, r, ir, i) {
            var that = this;

            function arcTween (a) {
                var i = d3.interpolate(this._current, a);
                this._current = i(0);
                var x = ir / r;
                return function (t) {
                    return that.pieTop(i(t), r, r, x);
                };
            }

            this.pieG.selectAll('.loop').filter(function (d, i1) {
                return i1 === i;
            }).selectAll('path').data(data)
                .transition().duration(3000).attrTween('d', arcTween);

            this.pieG.selectAll('.loop').filter(function (d, i1) {
                return i1 === i;
            }).selectAll('path').data(data).exit().transition().duration(3000).attrTween('d', function (d) {
                var a = JSON.parse(JSON.stringify(d));
                a.startAngle = d.endAngle;
                a.endAngle = d.endAngle;
                return arcTween(a);
            });
        },
        dragupdate: function () {
            var that = this;
            this.cx = (this.conWidth - this.options.padding[1] - this.options.padding[3]) / 2 + this.options.padding[3];
            this.cy = (this.conHeight - this.options.padding[0] - this.options.padding[2]) / 2 * (1 + Math.cos((1 - this.periphery) * Math.PI)) + this.options.padding[0];
            if (this.diagramData.hasOwnProperty('title') && this.diagramData.title.name) {
                this.title(this.diagramData.title);
            }
            this.redraw(this.diagramData, 0);
        },
        legendFn: function (legendData) {
            var that = this;
            this.legendDiv = d3.select(this.container).selectAll('div.legendDiv').data(['legend']);
            var legendenter = this.legendDiv.enter().append('xhtml:div').attr('class', 'legendDiv pie')
                .style('width', 'auto')
                // .style('width', this.options.padding[3] - this.spadl - 14 + 'px')
                .style('padding-top', 10 + 'px')
                .style('padding-bottom', 10 + 'px')
                .on('click', function () {
                    if (this === d3.event.target) return;
                    var curritem = that.getparents(d3.event.target, 'item');
                    if (!curritem) return false;
                    var piename = d3.select(curritem).select('div.tit').text();
                    var allpies = that.pieG.selectAll('.pathG');
                    var currpie = allpies.filter(function (d) {
                        return d.data.name === piename;
                    });
                    var otheritem = that.legendDiv.selectAll('.item').filter(function () {
                        return this !== curritem;
                    });

                    var isshadow = otheritem.nodes().some(function (ele) {
                        return parseFloat(d3.select(ele).style('opacity')) < 1;
                    });
                    if (isshadow && parseFloat(d3.select(curritem).style('opacity')) === 1) {
                        otheritem.style('opacity', 1);
                    }
                    else {
                        otheritem.style('opacity', 0.2);
                    }
                    allpies.filter('.cative').classed('cative', false).selectAll('path').dispatch('mouseout');
                    currpie.classed('cative', true);
                    currpie.dispatch('mouseover');
                    d3.select(curritem).style('opacity', 1);
                })
                .on('wheel', function () {
                    if (!that.legendDiv.select('.legscroll').size()) return;
                    d3.event.preventDefault();
                    var conpdt = parseFloat(d3.select(this).style('padding-top'));
                    var conpdb = parseFloat(d3.select(this).style('padding-bottom'));
                    var conHeight = this.offsetHeight - conpdb - conpdt;
                    var legendHeight = d3.select(this).select('div.legend2').node().offsetHeight;
                    var legendMgt = parseFloat(d3.select(this).select('div.legend2').style('margin-top'));
                    if (d3.event.wheelDelta < 0) { // 向下
                        legendMgt -= 32;
                    } else {
                        legendMgt += 32;
                    }
                    if (legendMgt > 0) legendMgt = 0;
                    else if (legendMgt < conHeight - legendHeight) legendMgt = conHeight - legendHeight;
                    var scroMgt = (-legendMgt / legendHeight) * (that.conHeight - that.options.padding[0] - that.options.padding[2]);
                    d3.select(this).select('div.legend2').style('margin-top', legendMgt + 'px');
                    that.legendDiv.select('.legscroll > div').style('margin-top', scroMgt + 'px');
                });

            // 给legend套个容器
            legendenter.append('xhtml:div').attr('class', 'legendcon').append('xhtml:div').attr('class', 'legend2');
            // 添加legend item
            if (legendData) {
                this.legendDiv = this.legendDiv.merge(legendenter);
                this.legendDiv.select('div.legend2')
                    .html(function (d) {
                        var str = '';
                        legendData.sort(function (a, b) {
                            if (that.diagramData.legend && that.diagramData.legend.sort === 'asc') {
                                return a.value - b.value;
                            } else if (that.diagramData.legend && that.diagramData.legend.sort === 'desc') {
                                return b.value - a.value;
                            }
                            return 0;
                        });
                        legendData.forEach(function (d) {
                            var color = that.legend.find(function (d1) {
                                return d1.name === d.name;
                            });
                            var num = 0;
                            if (that.diagramData.legend && that.diagramData.legend.ratio) {
                                num = (color.ratio * 100).toFixed(0) + '%';
                            } else {
                                num = color.value;
                            }
                            str += `<div class="item"><div class="colorblock clr${color.index % that.options.color.length}" style="border-color: ${color.color}; opacity:${color.opacity}"></div><div class="num">${num}</div><div class="tit">${color.name}</div></div>`;
                        });
                        return str;
                    })
                    .each(function () {
                        var itemNumMaxWidth = 0;
                        d3.select(this).selectAll('.item .num').each(function () {
                            var w = this.offsetWidth;
                            if (itemNumMaxWidth < w) itemNumMaxWidth = w;
                        }).style('min-width', itemNumMaxWidth + 'px');
                    });
            }
            this.legendDiv.each(function () {
                var conpdt = parseFloat(d3.select(this).style('padding-top'));
                var conpdb = parseFloat(d3.select(this).style('padding-bottom'));
                var conHeight = this.offsetHeight - conpdb - conpdt;
                var legendHeight = d3.select(this).select('div.legend2').node().offsetHeight;
                var legendMgt = parseFloat(d3.select(this).select('div.legend2').style('margin-top'));
                if (legendHeight > conHeight) {
                    d3.select(this).style('padding-right', '10px');
                    d3.select(this).select('div.legendcon').style('display', 'block');
                    var ratio = conHeight / legendHeight;
                    var maxLegendMgt = legendHeight - conHeight;
                    var scroll = d3.select(this).selectAll('div.legscroll').data(['legscroll']);
                    var enter = scroll.enter().append('xhtml:div').attr('class', 'legscroll');
                    if (maxLegendMgt < -legendMgt) {
                        d3.select(this).select('div.legend2').style('margin-top', -maxLegendMgt + 'px');
                    }
                    enter.append('xhtml:div')
                        .attr('class', 'scrollbar');
                    scroll.merge(enter)
                        .style('height', conHeight + 'px')
                        .style('top', 10 + 'px')
                        .select('.scrollbar')
                        .style('margin-top', (-legendMgt / legendHeight) * (that.conHeight - that.options.padding[0] - that.options.padding[2]) + 'px')
                        .style('height', ratio * (conHeight) + 'px');
                } else {
                    d3.select(this).selectAll('div.legscroll').remove();
                    d3.select(this).style('padding-right', 0);
                    d3.select(this).select('div.legendcon').style('display', 'flex');
                }
            })
                .selectAll('.item')
                .on('mouseenter', function () {
                    var divitem = that.getparents(d3.event.target, 'item');
                    if (!divitem) return false;
                    var classname = d3.select(divitem).select('div.tit').text();
                    var paths = that.pieG.selectAll('.pathG path');
                    var currpath = paths.filter(function (d) {
                        return d.data.name === classname;
                    });
                    currpath.dispatch('mouseover');
                    if (that.tooltip) that.tooltip.style('display', 'none');
                })
                .on('mouseleave', function () {
                    var divitem = that.getparents(d3.event.target, 'item');
                    if (!divitem) return false;
                    var classname = d3.select(divitem).select('div.tit').text();
                    var paths = that.pieG.selectAll('.pathG path');
                    var currpath = paths.filter(function (d) {
                        return d.data.name === classname;
                    });
                    currpath.dispatch('mouseout');
                });

            if (!this.drag) {
                this.drag = d3.drag()
                    .on('drag', function () {
                        var event = d3.event;
                        that.adaptiveLegend(this, event);
                    });
                this.adaptiveLegend = function (node, event) {
                    if (event.x < 60 || event.x > that.conWidth - 60) return;
                    d3.select(node).attr('x1', event.x).attr('x2', event.x);
                    that.legendDiv.style('width', event.x + 'px');
                    that.options.padding[3] = event.x + that.spadl;
                    that.dragupdate();
                    that.hidedragarrow();
                };
            }

            this.dragbar = this.legendDiv.selectAll('div.dragbar').data(['dragbar']);
            var dragbarEnter = this.dragbar.enter().append('xhtml:div').attr('class', 'dragbar').call(this.drag);
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
                        that.legendDiv.style('width', 'auto').style('left', 0 + 'px').style('overflow', '');
                        d3.select(this).classed('open', true);
                        that.conbar.style('display', 'none').style('opacity', 0);
                    } else {
                        that.options.padding[3] = that.spadl;
                        that.legendDiv.style('width', 0 + 'px').style('left', -that.options.flexlegendw + 'px').style('overflow', 'hidden');
                        d3.select(this).classed('open', false);
                        that.conbar.style('display', 'inline').style('opacity', 1);
                    }
                    that.dragupdate();
                });
            if (legendenter.size()) {
                this.svg.on('click', function () {
                    that.legendDiv.selectAll('.item').style('opacity', 1);
                    that.pieG.selectAll('.pathG').filter('.cative').classed('cative', false).selectAll('path').dispatch('mouseout');
                });
            }
        },
        tooltipFn: function () {
            var that = this;
            var mouse = d3.mouse(this.svg.node());
            this.tooltip = d3.select(this.container).selectAll('div.tooltip1').data(['tooltip1']);
            var tooltipenter = this.tooltip.enter().append('xhtml:div').attr('class', 'tooltip1 pie');
            this.tooltip = this.tooltip.merge(tooltipenter).style('top', this.options.padding[0] + 10 + 'px')
                .html(function (d) {
                    var str = '';
                    that.tooltipData.forEach(function (d) {
                        str += `<div class="item"><div class="tit">${d.name}</div><div class="num">${d.value}</div></div>`;
                    });
                    return `<div class="legend3">${str}</div>`;
                });
            var tpw = this.tooltip.node().offsetWidth;
            var tph = this.tooltip.node().offsetHeight;
            if (mouse[0] - tpw / 2 < this.options.padding[3]) mouse[0] = this.options.padding[3] + this.spadl;
            if (mouse[0] + tpw / 2 > this.conWidth - this.options.padding[1]) mouse[0] = this.conWidth - this.options.padding[1] - tpw / 2;
            this.tooltip.style('left', mouse[0] - tpw / 2 + 'px');
            this.tooltip.style('top', mouse[1] - tph - 10 + 'px');
            this.tooltip.selectAll('.arrow').data(['arrow']).enter().append('xhtml:div').attr('class', 'arrow');
        },
        resize: function (tsti) {
            tsti = tsti ? tsti : 0;
            this._super();
            // if (this.legendDiv && this.legendDiv.select('.open').size()) {
            //   // this.adaptiveLegend(this.dragbar.node(), {x: this.conWidth / 3})
            // } else
            this.dragupdate(0);
        },
        redraw: function (reData, tsti) {
            var that = this;
            tsti = tsti !== undefined ? tsti : 600;
            this.diagramData = reData;
            this.pieDataHandle();
            if (this.diagramData.hasOwnProperty('legend')) {
                if (this.diagramData.legend.hasOwnProperty('data')) {
                    this.legendFn(that.legend);
                }
            }
            this.drawLoop(this.pieData, tsti);
            that.adjustment();
        },
    });
})();
