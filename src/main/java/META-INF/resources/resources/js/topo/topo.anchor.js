(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {})));
}(this, (function (exports) {
    'use strict';

    var opts = {
        type: 'anchor', // 继承关系，不要动

    };

    var Ar = exports[opts.type] = exports.Base.extend();
    Ar.inherit = 'Base';
    Ar.inject({
        init: function (vm) {
            var that = this;
            this
                .instanceInject({
                    options: Object.assign({}, vm.options, opts),
                });

            // 加载模块
            this.Svg = this.getVm('svg');
            this.Link = this.getVm('link');
            this.Util = this.getVm('util');
            this.Node = this.getVm('node');

            // 添加颜色渐变
            var gradientId = this.Svg.options.id + '-' + 'gradient';
            var gradient = this.Svg.defs.append('radialGradient').attr('id', gradientId)
                .attr('cx', '50%')
                .attr('cy', '50%')
                .attr('fx', '50%')
                .attr('fy', '50%')
                .attr('r', '50%');
            var gradientStart = gradient.append('stop')
                .attr('offset', '0%')
                .attr('stop-color', 'red');
            var gradientEnd = gradient.append('stop')
                .attr('offset', '100%')
                .attr('stop-color', 'blue');

            this.constructor = Anchor;

            // svg
            var assistPath = this.Svg.zoomG.append('path').attr('class', 'assistPath').attr('stroke', 'red');

            this.constructor.init.prototype.extend({
                gradient: function (startColor, endColor) {
                    startColor && gradientStart.attr('stop-color', startColor);
                    endColor && gradientEnd.attr('stop-color', endColor);
                    return gradientId;
                },
                assistLink: function (data) {
                    if (data) {
                        var angle = Math.acos((data.x1 - data.x0) / Math.sqrt(Math.pow(data.x1 - data.x0, 2) + Math.pow(data.y1 - data.y0, 2)));
                        var offset = 5;
                        if (data.y1 < data.y0) angle = Math.PI * 2 - angle;
                        var x1 = data.x1 + offset * Math.cos(angle + Math.PI);
                        var y1 = data.y1 + offset * Math.sin(angle + Math.PI);
                        var pathStr = `M${data.x0},${data.y0}L${x1},${y1}`;
                        assistPath.attr('d', pathStr);
                    } else {
                        assistPath.attr('d', 'M0,0');
                    }
                },
                createLink: function (data) {
                    if (typeof that.options.createLinkBefore === 'function') {
						new Promise(function (resolve, reject) {
						    var sourceVm = that.Node.getNodeDataById(data.source);
						    var targetVm = that.Node.getNodeDataById(data.target);
							that.options.createLinkBefore({
                                data: data,
                                source: sourceVm.port,
                                target: targetVm.port
                            }, resolve, reject)
						}).then(function (data1) {
						    if (data1) that.append(data1);
						}).catch(function (error) {
						    console.log(error);
                        });
                    } else {
                        console.log(data);
						that.append(data);
                    }
                },
                // 创建锚点起始和结束
                sAnchor: {
                    source: null,
                    index: null
                },
                tAnchor: {
                    target: null,
                    index: null
                },

            });
        }
    });

    function Anchor (opts, nodes) {
        var that = this;
        nodes = nodes ? nodes : this.el;
        var anchorG = nodes.selectAll('.anchorG');

        nodes.each(function (d) {
            var node = d3.select(this);
            var anchorG = node.append('g').attr('class', 'anchorG').attr('data-sort', opts.isTop ? 15 : 3);
            that.nodeG = node;
            // 创建一个控制器
            var anchorRender = new Anchor.init(anchorG, opts, that);

            // 事件
            node
                .on('mouseenter.anchor', function () {
                    if (opts.disable) return false;
                    anchorRender.anchorShow();
                })
                .on('mouseleave.anchor', function () {
                    anchorRender.anchorHide();
                });

            if (that.type === 'group') {
                node.on('brushStart', function () {
                    anchorRender.anchorHide(0);
                });

                node.on('brushing', function () {

                });
                node.on('brushEnd', function (d) {
                    anchorRender.position.forEach(function (a, i) {
                        Object.assign(a, anchorRender.rectAnchorPositon(i));
                    });
                    anchorRender.anchorShow(300);
                });
            }
        });

        // return new anchor.init(this, opts, nodes);
    }

    Anchor.init = function (anchorG, opts, vm) {
        this.nodeG = vm.nodeG;
        this.el = anchorG;
        this.options = Object.assign({
            amount: 8,
            fillOpacity: 1,
        }, opts);
        this.Util = vm.getVm('util');
        this.data = this.el.datum();
        this.position = this.positionTransform();
        this.data.anchor = this.position;
        this.append();
    };

    Anchor.init.prototype = {
        constructor: Anchor,
        shapeCreator: function () {
            var that = this;
            var shapeCreator;
            switch (this.options.shape) {
                case 'rect':
                    shapeCreator = d3.create('svg:rect')
                        .attr('width', this.options.width)
                        .attr('height', this.options.height)
                        .attr('x', -this.options.width / 2)
                        .attr('y', -this.options.height / 2)
                        .attr('stroke', this.options.stroke)
                        .attr('fill-opacity', this.options.fillOpacity)
                        .attr('fill', this.options.fill);
                    break;
                case 'circle':
                    shapeCreator = d3.create('svg:circle')
                        .attr('r', this.options.size)
                        .attr('cx', this.options.cx || 0)
                        .attr('cy', this.options.cy || 0)
                        .attr('stroke', this.options.stroke)
                        .attr('fill-opacity', this.options.fillOpacity)
                        .attr('fill', this.options.fill);
                    break;
            }
            return shapeCreator;
        },
        positionTransform: function () {
            var that = this;
            var position;
            var offset = 6;

            switch (this.data.shape) {
                case 'rect':
                    position = Array.apply(null, {length: this.options.amount || 8}).map(function (d1, i1) {
                        return that.rectAnchorPositon(i1, that.data.width, that.data.height);
                    });
                    break;
                case 'circle':
                    position = Array.apply(null, {length: this.options.amount}).map(function (d1, i1, arr1) {
                        var angle = i1 * Math.PI * 2 / arr1.length;
                        return {
                            x0: Math.cos(angle) * that.data.size,
                            y0: Math.cos(angle) * that.data.size,
                            x1: Math.cos(angle) * (that.data.size + offset),
                            y1: Math.sin(angle) * (that.data.size + offset)
                        };
                    });
                    break;
                default:
                    position = [];
            }
            return position;
        },
        rectAnchorPositon: function (index, width, height, x0, y0) {
            var w = width || this.data.width, h = height || this.data.height;
            var dx = 0, dy = 0, offset = this.options.offset || 0;
            var cx = this.options.cx !== undefined ? this.options.cx : -w / 2;
            var cy = this.options.cy !== undefined ? this.options.cy : -h / 2;
            var dir = '';
            var sortArr = [0, 1, 2, 3, 4, 5, 6, 7];
            if (this.options.defaultDir === 'LR') sortArr = [2, 3, 0, 1, 4, 5, 6, 7];
            index = sortArr.indexOf(index);
            switch (index) {
                case 0: // top
                    dx = w / 2;
                    dy = 0 - offset;
                    dir = 'T';
                    break;
                case 1: // bottom
                    dx = w / 2;
                    dy = h + offset;
                    dir = 'B';
                    break;
                case 2: // left
                    dx = 0 - offset;
                    dy = h / 2;
                    dir = 'L';
                    break;
                case 3: // right
                    dx = w + offset;
                    dy = h / 2;
                    dir = 'R';
                    break;
                case 4: // left top
                    dx = 0 - offset;
                    dy = 0 - offset;
                    dir = 'LT';
                    break;
                case 5: // right bottom
                    dx = w + offset;
                    dy = h + offset;
                    dir = 'RB';
                    break;
                case 6: // left bottom
                    dx = 0 - offset;
                    dy = h + offset;
                    dir = 'LB';
                    break;
                case 7: // right top
                    dx = w + offset;
                    dy = 0 - offset;
                    dir = 'RT';
                    break;
                default:
                    break;
            }
            return {
                x1: dx + (x0 || 0) + cx,
                y1: dy + (y0 || 0) + cy,
                dir: dir
            };
        },
        append: function () {
            var that = this;
            var anchor = this.el.selectAll('.anG').data(this.position);
            var anchorEnterG = anchor.enter().append('g').attr('class', 'anG');
            // var anchorEnter = anchorEnterG.append(function () {
            //     return that.shapeCreator().node();
            // }).attr('class', 'anchor');
            var anchorExit = anchor.exit().remove();

            //
            var assistCircle = d3.select();
            var durationTime = 700;
            //
            // if (this.options.effect !== false) {
            //     assistCircle = this.el.append(function () {
            //         return that.Util.shape(that.data, -2).node();
            //     }).attr('class', 'shape')
            //         .attr('data-sort', 0)
            //         .attr('fill', this.data.fill)
            //         .attr('fill-opacity', 1)
            //     this.el.attr('filter', `url("#goo-shadow")`);
            // } else if (this.options.effect === 'assistNode') {
            //
            // }

            this.nodeG.on('mouseInNode.anchor', function () {

            });

            if (that.options.effect === 'gooShadow') {
                assistCircle = that.nodeG.append(function () {
                    return that.Util.shape(that.data, -2).node();
                }).attr('class', 'assistShape').lower()
                    .attr('data-sort', 0)
                    .attr('fill', that.data.fill)
                    .attr('stroke', that.data.stroke)
                    .attr('fill-opacity', 1);
                assistCircle.attr('filter', `url("#goo-shadow")`);
                this.el.attr('filter', `url("#goo-shadow")`);
            } else if (this.options.effect === 'assistNode') {
                anchorEnterG.append();
            }

            if (that.options.effect === 'gooShadow') {
                assistCircle = that.nodeG.append(function () {
                    return that.Util.shape(that.data, -2).node();
                }).attr('class', 'assistShape').lower()
                    .attr('data-sort', 0)
                    .attr('fill', that.data.fill)
                    .attr('stroke', that.data.stroke)
                    .attr('fill-opacity', 1);
                assistCircle.attr('filter', `url("#goo-shadow")`);
                that.el.attr('filter', `url("#goo-shadow")`);
            }

            anchorEnterG.each(function (d) {
                var g = d3.select(this);
                var circle = g.append(function () {
                    return that.shapeCreator().node();
                }).attr('class', 'anchor');

                var assistCircle = d3.select();
                if (that.options.effect === 'assistNode') {
                    assistCircle = g.append(function () {
                        return that.shapeCreator().node();
                    }).attr('class', 'assistAnchor').lower()
                        .attr('fill', that.options.stroke)
                        .attr('stroke', 'transparent')
                        .attr('x', d.x1)
                        .attr('y', d.y1)
                        .attr('fill-opacity', 0.4);
                }

                g
                    .attr('transform', function (d) {
                        return `translate(${d.x1},${d.y1})scale(0)`;
                    })
                    .on('mouseenter', function (d, i) {
                        var color1 = d3.color(that.options.fill).brighter();
                        if (that.options.mouseOver === 'assist') {
                            d3.select(this).dispatch('mouseInAnchor');
                        } else {
                            circle
                                .attr('stroke', 'none')
                                .attr('fill', `url(#${that.gradient(color1, that.options.fill)})`);
                            assistCircle && assistCircle
                                .transition()
                                .duration(durationTime)
                                .attr('r', that.data.size - 3);
                        }
                        that.tAnchor.target = that.data.uuid;
                        that.tAnchor.index = i;
                    })
                    .on('mouseleave', function () {
                        if (that.options.mouseOver === 'assist') {
                            d3.select(this).dispatch('mouseOutAnchor');
                        } else {
                            d3.select(this)
                                .attr('stroke', that.options.stroke)
                                .attr('fill', that.options.fill);
                            assistCircle && assistCircle
                                .transition()
                                .duration(durationTime)
                                .attr('r', that.data.size);
                        }
                        that.tAnchor.target = null;
                        that.tAnchor.index = null;
                    })
                    .on('mouseInAnchor', function (d) {
                        // assistAnchor = that.el.append(function () {
                        //     return that.shapeCreator().node();
                        // }).attr('class', 'assistShape').lower()
                        //     .datum(d)
                        //     .attr('fill', that.options.stroke)
                        //     .attr('stroke', 'transparent')
                        //     .attr('x', d.x1)
                        //     .attr('y', d.y1)
                        //     .attr('fill-opacity', 0.4);
                        assistCircle
                            .attr('transform', `scale(1)`)
                            .transition()
                            .duration(durationTime)
                            .attr('transform', `scale(2.2)`);
                    })
                    .on('mouseOutAnchor', function (d) {
                        assistCircle
                            .transition()
                            .duration(durationTime)
                            .attr('transform', `scale(1)`)
                            // .on('end', function () {
                            //     assistAnchor.remove();
                            // })
                    })
                    .call(d3.drag()
                        .on('start', function (d, i) {
                            that.sAnchor.source = that.data.uuid;
                            that.sAnchor.index = i;
                        })
                        .on('drag', function (d) {
                            // 画辅助线
                            that.assistLink({
                                x0: d.x1 + that.data.x,
                                y0: d.y1 + that.data.y,
                                x1: that.data.x + d3.event.x,
                                y1: that.data.y + d3.event.y,
                            });
                        })
                        .on('end', function () {
                            console.log('end');
                            // 辅助线消失
                            that.assistLink();
                            // 创建线条
                            if (that.tAnchor.target && that.tAnchor.target !== that.sAnchor.source) {
                                that.createLink({
                                    type: 'link',
                                    source: that.sAnchor.source,
                                    target: that.tAnchor.target,
                                });
                            }
                            // 清除Anchor相关数据
                            that.sAnchor.source = that.sAnchor.index = null;
                            that.tAnchor.target = that.tAnchor.index = null;
                        }));
            });
            this.anchor = anchorEnterG;
        },
        anchorShow: function (time) {
            this.anchor
                .transition()
                .duration(time === undefined ? 700 : time)
                .attr('transform', function (d) {
                    return `translate(${d.x1},${d.y1})scale(1)`;
                });
        },
        anchorHide: function (time) {
            this.anchor
                .interrupt()
                .transition()
                .duration(time === undefined ? 700 : time)
                .attr('transform', function (d) {
                    return `translate(${d.x1},${d.y1})scale(0)`;
                });
        },
        extend: function (obj) {
            for (var attr in obj) {
                this[attr] = obj[attr];
            }
        },
        draw: function (nodes) {
            var that = this.that;
            var offset = 6;

            var link = that.getVm('link');

            var anchorConnectLine; // 添加锚点链接线
            nodes.each(function (d) {
                // node上相关锚点的操作
                var node = d3.select(this);

                var nodeHoverPath = node.append('path')
                    .attr('d', d.shape)
                    .attr('stroke', 'transparent')
                    .attr('fill', 'transparent')
                    .attr('transform', `scale(${(d.size + offset + d.anchorSize) / d.size})`);

                node
                    .on('anchorShow', function (d) {
                        // 添加锚点block, 防止hover锚点是抖动
                        nodeHoverPath.style('display', null);
                        pathEnter.dispatch('show');
                    })
                    .on('anchorHide', function (d) {
                        pathEnter.dispatch('hide');
                        nodeHoverPath.style('display', 'none');
                    });

                // 添加锚点G
                var anchor = node.append('g')
                    .attr('class', 'anchor')
                    .classed('hide', true);

                // 锚点hover效果
                var anchorHoverPath = anchor.append('path')
                    .attr('class', 'anchorHover')
                    .attr('fill', d.anchorBgFill)
                    .attr('stroke', d.anchorBgStroke)
                    .attr('opacity', 0)
                    .classed('hide', true)
                    .on('show', function () {
                        var d1 = d3.event.detail;
                        anchorHoverPath
                            .transition('show')
                            .duration(500)
                            .ease(d3.easeLinear)
                            .attr('d', d.anchorBgHoverShape)
                            .attr('opacity', 1)
                            .on('start', function () {
                                anchorHoverPath
                                    .attr('transform', `translate(${d1.dx},${d1.dy})`)
                                    .attr('d', d.anchorBgShape)
                                    .classed('hide', false);
                            });
                    })
                    .on('hide', function () {
                        anchorHoverPath
                            .transition('show')
                            .duration(500)
                            .ease(d3.easeLinear)
                            .attr('d', d.anchorBgShape)
                            .attr('opacity', 0)
                            .on('end', function () {
                                anchorHoverPath.classed('hide', true);
                            });
                    });
                // 添加锚点
                var path = anchor.selectAll('g').data(d.anchor);
                var pathEnterG = path.enter()
                    .append('g')
                    .attr('transform', function (d) {
                        return `rotate(${d.angle})translate(${that.options.size - offset + that.options.anchorSize / 2},0)`;
                    });
                var pathEnter = pathEnterG
                    .append('path')
                    .attr('fill', d.anchorFill)
                    .attr('stroke', d.anchorStroke)
                    .attr('d', d.anchorShape)
                    .attr('transform', `translate(0,0)`)
                    .on('show', function (d1, i1, arr1) {
                        var el = d3.select(this);
                        el
                            .transition('anchor')
                            .duration(d1.angle + 150)
                            .ease(d3.easeLinear)
                            .attr('opacity', 1)
                            .attr('d', d.anchorHoverShape)
                            .attr('transform', `translate(${offset * 2},0)`)
                            .on('start', function (d2, i2, arr2) {
                                anchor.classed('hide', false);
                            });
                    })
                    .on('hide', function (d1, i1, arr1) {
                        var el = d3.select(this);
                        el
                            .transition('anchor')
                            .duration(d1.angle + 150)
                            .ease(d3.easeLinear)
                            .attr('transform', `translate(${0},0)`)
                            .attr('d', d.anchorShape)
                            .attr('opacity', 0)
                            .on('end', function (d2, i2, arr2) {
                                if (i1 === arr1.length - 1) {
                                    anchor.classed('hide', true);
                                }
                            });
                    })
                    .on('mouseover', function (d1) {
                        var el = d3.select(this);
                        anchorHoverPath.dispatch('show', {detail: d1});
                        // 记录连线的结束锚点
                        link.linkEnd[0] = d;
                        link.linkEnd[1] = d1;
                    })
                    .on('mouseout', function (d1) {
                        var el = d3.select(this);
                        anchorHoverPath.dispatch('hide');
                        // 清空
                        link.linkEnd.length = 0;
                    });
                pathEnterG.call(d3.drag()
                    .on('start', function (d1) {
                        // 初始化拖动的偏移量
                        ax = 0;
                        ay = 0;
                        // 记录连线的起始节点
                        link.linkStart[0] = d;
                        link.linkStart[1] = d1;

                        // 创建锚点辅助链接线
                        anchorConnectLine = that.container.append('path').attr('stroke', 'red').attr('fill', 'none');
                    })
                    .on('drag', function (d1) {
                        // 累计拖动的偏移量
                        ax += d3.event.dx;
                        ay += d3.event.dy;
                        // 计算辅助线的起始点和结束点
                        var x0 = d.x + d1.dx, y0 = d.y + d1.dy;
                        var x = x0 + ax, y = y0 + ay;
                        var offset = 5;
                        dragAnchor(x0, x, y0, y, offset);

                    })
                    .on('end', function () {
                        // 清除锚点的辅助连接线
                        anchorConnectLine.remove();
                        // 创建连接线
                        if (link.linkStart.length && link.linkEnd.length && link.linkStart[0] !== link.linkEnd[0]) {
                            console.log('create line');
                            that.append({
                                type: 'link',
                                mode: 'straight',
                                source: link.linkStart[0].uuid,
                                target: link.linkEnd[0].uuid,
                            });
                        }
                        // 链接线创建节点，数组清空
                        link.linkStart.length = 0;
                        link.linkEnd.length = 0;
                    }));
                var ax = 0, ay = 0;
                var dragAnchor = _.throttle(function (x0, x, y0, y, offset) {
                    var angle = Math.acos((x - x0) / Math.sqrt(Math.pow(x - x0, 2) + Math.pow(y - y0, 2)));
                    if (y < y0) angle = Math.PI * 2 - angle;
                    if (angle) {
                        x = x + offset * Math.cos(angle + Math.PI);
                        y = y + offset * Math.sin(angle + Math.PI);
                    }

                    var anchorConPathStr = `M${x0},${y0}L${x},${y}`;
                    anchorConnectLine.attr('d', anchorConPathStr);

                }, 0);
            });

        },
    };

})));