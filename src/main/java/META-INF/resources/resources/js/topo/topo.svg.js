(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {})));
}(this, (function (exports) {
    'use strict';

    var opts = {
        type: 'svg', // 继承关系，不要动
        zoomExtent: [0.2, 10],
        className: 'zxy',
        width: '100%',
        height: '100%'
    };

    var Svg = exports[opts.type] = exports.Base.extend();
    Svg.inherit = 'Base';
    Svg.inject({
        init: function (vm) {
            var that = this;
            this
                .inherit(vm)
                .subset(vm)
                .instanceInject({
                    options: Object.assign({}, vm.options, opts),
                    container: vm.container.append('xhtml:div').attr('class', 'main'),
                });

            // 添加svg
            this.el = this.container.append('svg')
                .attr('class', 'topo')
                .classed(this.options.className, true)
                .attr('cursor', 'pointer')
                .attr('width', this.options.width)
                .attr('height', this.options.height);

            // 添加用于计算的text
            vm.calculateText = this.el.append('text').attr('y', -100).attr('fill', 'none').attr('stroke', 'none');

            // 添加defs
            this.defs = this.el.append('defs');

            this.brushG = this.el.append('g').attr('class', 'brushG');
            // 添加zoomG
            this.zoomG = this.el.append('g').attr('class', 'zoomG');

            this.defsFn();
            this.zoomFn();
            // 添加brush
            this.brushFn();

            // 按键事件
            var keyStatus = {}; // 防止事件高频触发
            d3.select(document)
                .on('keydown', function () {
                    if (d3.event.path && d3.event.path[0].tagName === 'INPUT') return false;
                    if (d3.event.keyCode === 32 && that.options.brushable && !keyStatus['key' + d3.event.keyCode]) {
                        keyStatus['key' + d3.event.keyCode] = true;
                        that.brushG.dispatch('cancel');
                    }
                    // delete
                    if ((d3.event.keyCode === 8 || d3.event.keyCode === 46)) {
                        if (that.data.status.inputting) return false;
                        if (that.options.nodeDeleteable) that.getVm('node').getAll('condition', 'selected').dispatch('remove');
                        if (that.options.linkDeleteable) that.getVm('link').getAll('condition', 'selected').dispatch('remove');
                        if (that.options.groupDeleteable) that.getVm('group').getAll('condition', 'selected').dispatch('remove');
                    }
                })
                .on('keyup', function () {
                    if (d3.event.keyCode === 32 && that.options.brushable && keyStatus['key' + d3.event.keyCode]) {
                        keyStatus['key' + d3.event.keyCode] = false;
                        that.brushG.dispatch('resize');
                    }
                });
            // window的resize事件
            d3.select(window).on('resize', function () {
                if (that.options.brushable) that.brushG.dispatch('resize');
            });
            // svg 添加事件
            this.el.on('click', function () {
                // 取消所有节点的选中状态
                // that.parent.getAll('condition', 'selected').dispatch('unselect');
                ['node', 'group', 'link'].forEach(function (d) {
                    if (that.options.spaceCancleSelected.includes(d)) {
                        that.getVm(d).getAll('condition', 'selected').dispatch('unselect');
                    }
                });
                // 取消节点的右键菜单
                that.parent.getAll('condition', 'contextMenu').dispatch('contextMenuHide');
                // callback
                that.hook('click', this.data)
            });
            // 右键事件
            this.el.on('contextmenu', function () {
				d3.event.preventDefault();
                // that.getRoom().getAll('condition', 'contextMenu').dispatch('contextMenuHide');
                that.hook('contextmenu', that.data);
            });

			this.el.on('mousemove', function () {
				that.getVm('tip').hide();
			});

            // 响应相关配置项
            if (!that.options.brushable) {
                that.brushG.dispatch('cancel');
            }
        },
        dataFn: function (data) {
            if (!data) {
                this.consoleLog('svg data为空！', 'warn');
                return;
            }
            if (data.hasOwnProperty('x') && data.hasOwnProperty('y')) {
                data.zoom = {x: data.x, y: data.y, k: data.k || 1}
            }
            var db = {
                data: data,
                name: '',//data.name || '根视图',
                nameColor: data.nameColor || 'white',
                namePosition: {x: 100, y: 30},
                contextMenu: data.contextMenu || [],
                background: data.background || 'white' || 'linear-gradient(#0d2133, #18334b)',
                getSet: {},
                status: {},
                info: {},
                zoom: data.zoom || {x: 0, y: 0, k: 1}
            };

            // TODO 这个需要移到topo.info.js中
            // 添加节点信息展示字典
            Object.assign(db.info, { // key为db中的属性，值为展示的detail
                type: 'svg',
                label: '画布',
                name: {
                    label: '名称',
                    value: db.name,
                    editable: true,
                    type: 'input'
                },
                background: {
                    label: '背景',
                    value: db.background,
                    editable: true,
                    type: 'color'
                }
            });
            return db;
        },
        draw: function (data) {
            var that = this;
            this.data = data;
            this.el.datum(data);

            Object.defineProperties(data, {
                background: {
                    get: that.objectGet('background', data.background),
                    set: function (value) {
                        if (this.getSet.background !== value) {
                            if (value.indexOf('bg-') !== -1) {
                                that.container.style('background', null);
                                that.container.classed(value, true);
                            } else {
                                that.container.style('background', value);
                            }
                            this.getSet.background = value;
                        }
                    }
                },
            });
            // 初始化
            data.background = data.background;
            this.appendNavigator();
            // this.appendCxtMenu();

            // zoom
            this.zoomTransform(this.data.zoom, 0);

        },
        render: function (data) {
            if (!data) return false;
            var vm = this.getVm(opts.type);
            // 数据处理
            data = vm.dataFn(data);
            // 添加节点（分配增删改）
            vm.draw(data);
        },
        toJson: function () {
            return {
                svg: Object.assign({}, this.data.data, {
                    zoom: d3.zoomTransform(this.el.node())
                })
            };
        },
        defsFn: function () {
            var that = this;
            var defs = this.el.selectAll('defs').data(['defs']);
            var defsEnter = defs.enter().append('defs');
            this.defs = defs.merge(defsEnter);

            // marker剪切
            this.defs.append('clipPath').attr('id', 'marker-' + that.options.id)
                .append('rect')
                .attr('width', 20 + 10)
                .attr('height', 20 + 10)
                .attr('rx', 6)
                .attr('ry', 6);

            this.defs.append('filter').attr('id', 'goo')
                .html(function () {
                    return `<feGaussianBlur in="SourceGraphic" result="blur" stdDeviation="10"></feGaussianBlur><feColorMatrix in="blur" mode="matrix" values="1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 18 -8" result="goo"></feColorMatrix><feGaussianBlur in="goo" stdDeviation="2" result="shadow"></feGaussianBlur><feColorMatrix in="shadow" mode="matrix" values="0 0 0 0 0  0 0 0 0 0  0 0 0 0 0  0 0 0 0 0" result="shadow"></feColorMatrix><feOffset in="shadow" dx="1" dy="1" result="shadow"></feOffset><feComposite in2="shadow" in="goo" result="goo"></feComposite><feComposite in2="goo" in="SourceGraphic" result="mix"></feComposite>`;
                });
            this.defs.append('filter').attr('id', 'goo-shadow')
                .html(function () {
                    return `<feGaussianBlur in="SourceGraphic" stdDeviation="7" result="blur" />
			      <feColorMatrix in="blur" mode="matrix" values="1 0 0 0 0  0 1 0 0 0  0 0 1 0 0  0 0 0 19 -7" result="goo" />
			      <feComposite in="SourceGraphic" in2="goo" operator="atop"/>`;
                });
            // 阴影
            this.defs.append('filter').attr('id', 'node-shadow-' + this.options.id)
                .attr('x', '-0.5')
                .attr('y', '-0.5')
                .attr('width', '200%')
                .attr('height', '200%')
                .html(function () {
                    return `
                       <feOffset result="offOut" in="SourceGraphic" dx="0" dy="0" />
                       <feColorMatrix result="matrixOut" in="offOut" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.6 0" />
                       <feGaussianBlur result="blurOut" in="matrixOut" stdDeviation="5" />
                       <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />
                    `;
                });
            this.defs.append('filter').attr('id', 'group-shadow-' + this.options.id)
                .attr('x', '-0.5')
                .attr('y', '-0.5')
                .attr('width', '200%')
                .attr('height', '200%')
                .html(function () {
                    return `
                       <feOffset result="offOut" in="SourceGraphic" dx="0" dy="0" />
                       <!--<feColorMatrix result="matrixOut" in="offOut" type="matrix"  />-->
                       <feGaussianBlur result="blurOut" in="matrixOut" stdDeviation="3" />
                       <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />
                    `;
                });
            this.defs.append('filter').attr('id', 'link-shadow-' + this.options.id)
                .attr('x', '-0.5')
                .attr('y', '-0.5')
                .attr('width', '200%')
                .attr('height', '200%')
                .html(function () {
                    return `
                       <feOffset result="offOut" in="SourceGraphic" dx="0.5" dy="0.5" />
                       <feColorMatrix result="matrixOut" in="offOut" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.6 0" />
                       <feGaussianBlur result="blurOut" in="matrixOut" stdDeviation="1" />
                       <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />
                    `;
                });
            this.defs.append('filter').attr('id', 'text-shadow-' + this.options.id)
                .attr('x', '-0.5')
                .attr('y', '-0.5')
                .attr('width', '200%')
                .attr('height', '200%')
                .html(function () {
                    return `
                       <feOffset result="offOut" in="SourceGraphic" dx="0.2" dy="0.2" />
                       <feColorMatrix result="matrixOut" in="offOut" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0.6 0" />
                       <feGaussianBlur result="blurOut" in="matrixOut" stdDeviation="0.4" />
                       <feBlend in="SourceGraphic" in2="blurOut" mode="normal" />
                    `;
                });
        },
        zoomFn: function () {
            var that = this;
            this.zoom = d3.zoom()
                .filter(function () {
                    return d3.event.type !== 'dblclick' && d3.event.which !== 3;
                })
                .wheelDelta(function () {
                    return -d3.event.deltaY * (d3.event.deltaMode ? 120 : 1) / 1500;
                })
                .on('start', function () {
                    that.hook('zoomStart')
                })
                .on('zoom', function () {
                    that.zoomG.attr('transform', d3.event.transform);
                })
                .on('end', function () {

                });
            this.el.call(this.zoom);
        },
        zoomTransform: function (transform, time) {
            if (!transform) return false;
            time = time !== undefined ? time : 700;
            return this.el.transition().duration(time).call(this.zoom.transform, typeof transform.translate === 'function' ? transform : d3.zoomIdentity.translate(transform.x, transform.y).scale(transform.k));
        },
        positionTransform: function (position, action) {
            var zoomTransform = d3.zoomTransform(this.el.node());
            return zoomTransform[action](position);
        },
        brushFn: function () {
            var that = this;
            var zoomIdentity;
            var brushAssist = d3.brush();

            var brush = d3.brush()
                .extent([[0, 0], [that.getWidth(), that.getHeight()]])
                .on('start', function () {
                    zoomIdentity = d3.zoomTransform(that.el.node());
                    that.el.dispatch('click');
                    that.hook('brushStart');
                })
                .on('brush', function () {
                    var section = d3.event.selection;
                    // 获取被框选中的节点
                    if (section && that.options.brushSelect) {
                        var selectSection = section.map(function (d) {
                            return zoomIdentity.invert(d);
                        });

                        var gather = that.parent.children.reduce(function (a, b) {
                            return a.concat(b.brushIn ? b.brushIn(selectSection) : []);
                        }, []);
                        var selected = that.parent.getAll('condition', 'selected');
                        var unselect = selected.nodes().filter(function (d) {
                            return !gather.includes(d);
                        });
                        d3.selectAll(gather).dispatch('selected');
                        d3.selectAll(unselect).dispatch('unselect');
                    }
                    that.hook('brush');
                })
                .on('end', function () {
                    brushAssist.move(that.brushG, [[0, 0], [0, 0]]);
                    that.hook('brushEnd');
                });

            this.brushG.call(brush);

            this.brushG
                .on('cancel', function () {
                    // 取消brush框
                    d3.brush().move(that.brushG, null);
                    that.brushG.call(brush.extent([[0, 0], [0, 0]]));
                })
                .on('resize', function () {
                    that.brushG.call(brush.extent([[0, 0], [that.getWidth(), that.getHeight()]]));
                });
        },
        appendNavigator: function () {
            var that = this;
            var data = this.el.datum();
            this.title = [];
            this.titleG = this.el.append('g').attr('class', 'titleG')
                .attr('font-size', 14)
                .attr('fill', data.nameColor)
                .attr('filter', `url(#text-shadow-${that.options.id})`)
                .attr('transform', `translate(${data.namePosition.x},${data.namePosition.y})`)
                .on('click', function () {
                    // 切换视图
                })
                .call(d3.drag());
            this.title.add = function (arr) {
                if (!arr) return false;
                if (Array.isArray(arr)) {
                    arr.forEach(function (item) {
                        that.title.push(item);
                    });
                } else that.title.push(arr);
                that.titleG.dispatch('update');
            };
            this.title.remove = function (item) {
                if (!item) return false;
                if (typeof item === 'number') {
                    that.title.splice(item + 1);
                }
                that.titleG.dispatch('update');
            };

            this.titleG.on('update', function () {
                var text = that.titleG.selectAll('text').data(that.title);
                var textEnter = text.enter().append('text');
                var textExit = text.exit().remove();
                var name = textEnter.append('tspan')
                    .datum('name');
                var aText = text.merge(textEnter);

                var t = aText.filter(function (d, i, arr) {
                    return i < arr.length - 1;
                }).selectAll('tspan').data(['icon'], function (d) {
                    return d;
                }).enter()
                    .append('tspan')
                    .attr('dx', 5)
                    .text('>');

                text.merge(textEnter)
                    .text(function (d) {
                        return d.name;
                    });

                var width = 0;
                aText
                    .attr('x', function () {
                        var x = width;
                        width += this.getBBox().width + 5;
                        return x;
                    });
            });

            // 添加操作器
            Object.defineProperties(data, {
                name: {
                    get: that.objectGet('name', data.name),
                    set: function (val) {
                        if (this.getSet.name !== val) {
                            this.getSet.name = val;
                            that.titleG.dispatch('update');
                        }
                    }
                },
                nameColor: {
                    get: that.objectGet('name', data.nameColor),
                    set: function (val) {
                        if (this.getSet.nameColor !== val) {
                            that.titleG.attr('fill', val);
                            this.getSet.nameColor = val;
                        }
                    }
                }
            });

            // 初始化
            this.title.add(data);
        },
        getAll: function () {
            return [this.el.node()];
        },
        getWidth: function () {
            return this.container.node().offsetWidth;
        },
        getHeight: function () {
            return this.container.node().offsetHeight;
        },
        getZoomInvert: function (data) {
            var zoomData = d3.zoomTransform(this.el.node());
            return zoomData.apply([data.x, data.y])
        },
        center: function (time) {
            var that = this;
            var box = this.zoomG.node().getBBox();
            var width = this.getWidth();
            var height = this.getHeight();
            var k = Math.min(width / box.width, height / box.height) * 0.8;
            k = k > 1.5 ? 1.5 : k;

            this.zoomTransform(d3.zoomIdentity
                .translate(width / 2, height / 2)
                .scale(k)
                .translate(-(box.width / 2 + box.x), -(box.height / 2 + box.y)), time
            )
        },
    });

})));