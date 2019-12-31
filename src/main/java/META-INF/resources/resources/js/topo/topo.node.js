(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {})));
}(this, (function (exports) {
    'use strict';
    var opts = {
        type: 'node',
        shape: 'circle',
        className: 'nodesG',
        nodeClass: 'nodeG',
        selectable: true,
        size: 26,
        nameWidth: 90,
        remarkWidth: 52,
        nameHeight: 32,
        minWidth: 52,
        minHeight: 36,
        padding: 10,
        selectStroke: '#336eff',
        selectStrokeWidth: 1,
        hierTransAble: true,
    };

    var Node = exports[opts.type] = exports.Base.extend();
    Node.inherit = 'Base';
    Node.inject({
        init: function (vm) {
            this
                .inherit(vm)
                .subset(vm)
                .instanceInject({
                    options: Object.assign({}, vm.options, opts),
                    container: this.getVm('svg').zoomG
                });
            // 添加初始化标签
            this.el = this.container.classed(this.options.className, true);
            // 存放画布中的node节点
            this.nodes = Array.isArray(this.nodes) ? this.nodes : this.gatherNodes();
            this.nodesData = Array.isArray(this.nodesData) ? this.nodesData : this.gatherNodes();

            // 引入需要的模块
            this.Anchor = this.getVm('anchor');
            this.Link = this.getVm('link');
            this.Group = this.getVm('group');
            this.Util = this.getVm('util');
            this.Tree = this.getVm('tree');
            this.Tip = this.getVm('tip');

        },
        dataFn: function (data, reuuid) {
            var that = this;
            if (!data) {
                this.consoleLog('node data为空！', 'warn');
                return;
            }

            var dagreData = this.parent.dagreData;

            var nodesDataArr = data.map(function (d) { // 节点默认以圆形模型处理

                if (d.hasOwnProperty(d.uuid) && that.isReuuid(d.uuid)) {
                    that.consoleLog('节点uuid重复了！', 'warn');
                    return;
                }

                var db = {
                    el: '', // 存储数据对应的dom
                    data: d,
					ciEntityId: d.ciEntityId,
                    uuid: d.uuid && (d.uuid = String(d.uuid)) || (d.uuid = that.guid(that.options.uuidmode)),
                    reuuid: reuuid || 'uuid',
                    type: d.type,
                    name: d.name || '',
                    url: d.url,
                    nameColor: d.nameColor || that.options.nodeNameColor || '#666666',
                    nameSize: d.nameSize || 12,
                    remark: d.remark || '',
                    remarkColor: d.nameColor || '#336eff',
                    remarkSize: d.nameSize || 10,
                    icon: d.icon || '\ue832',
                    x: d.x || 0,
                    y: d.y || 0,
                    cx: 0,
                    cy: 0,
                    anchor: [],
                    groups: [], // 分组
                    in: [], // 记录连入的线
                    out: [], // 记录连出的线
                    children: [], // 树形布局用
                    treeChildren: [], // 树形布局用
                    reChildren: [],  //做树形relation过滤
                    parents: [],  // 树形布局
                    shape: d.shape || that.options.shape,
                    fill: d.fill || that.options.nodeFill || 'transparent',
                    stroke: d.stroke || that.options.nodeStroke || '#336eff',
                    strokeWidth: d.strokeWidth || 1,
                    strokedasharray: d.strokedasharray || '0 0',
                    iconColor: d.iconColor || that.options.nodeIconColor || '#336eff',
                    iconSize: d.iconSize || that.options.nodeIconSize || 16,
                    iconWeight: d.iconWeight || that.options.iconWeight || 'normal',
                    fontFamily: d.fontFamily || 'ts',
                    marker: d.marker || false,
                    markerColor: d.markerColor || '#b2d235',
                    // selectable: true,
                    // selectMargin: true,
                    // selectFill: 'transparent' || that.options.selectFill,
                    // selectStroke: 'transparent' || that.options.selectStroke,
                    // selectStrokeWidth: that.options.selectStrokeWidth,
                    // selectStrokedasharray: that.options.selectStrokedasharray,
                    getSet: {}, // 专门给get,set提供数值存放的位置
                    storage: {}, // 专供存放恢复数据
                    status: {
                        hide: false, // 是否隐藏
                    }, // 存储节点的状态信息
                    contextMenu: d.contextMenu || that.options.nodeContextMenu || [],
                    info: {},
                    margin: [0, 0, 0, 0]
                };

                // 节点标记
                db.sign = {
                    disable: !d.hasOwnProperty('ru') && !d.hasOwnProperty('rd'),
                    ratio: 0.4,
                    ru: d.hasOwnProperty('ru') ? d.ru : 11,
                    ruColor: d.ruColor || '#666666',
                    ruSize: d.ruSize || 12,
                    ruBg: d.ruBg || db.fill || 'transparent',
                    ruStroke: d.ruStroke || db.stroke,
                    rd: d.hasOwnProperty('rd') ? d.rd : 12,
                    rdColor: d.rdColor || '#666666',
                    rdSize: d.rdSize || 13,
                    rdBg: d.rdBg || db.fill || 'transparent',
                    rdStroke: d.rdStroke || db.stroke,
                };

                // btns
                db.btns = [

                ];

                // info btns
                db.infoBtns = [
                ];

                // 节点状态
                db.state = {
                    name: '',
                };

                // 节点的selected
                db.selected = {
                    selectable: that.options.hasOwnProperty('nodeSelectable') ? that.options.nodeSelectable : true,
                    selectMargin: that.options.selectMargin || true,
                    fill: d.selectedFill || 'transparent',
                    stroke: d.selectedStroke || that.options.nodeSelectedStroke || '#336eff',
                    strokeWidth: that.options.selectStrokeWidth,
                    strokedasharray: that.options.selectStrokedasharray || '3,5',
                };

                // x,y
                if (db.x === undefined || db.y === undefined) {
                    var xy = dagreData && dagreData.nodes.find(function (a) {
                        return a.uuid === db.uuid;
                    });
                    if (xy) {
                        db.x = xy.x;
                        db.y = xy.y;
                    }
                }
                // 处理形状
                that.shapeHandle({
                    action: 'size',
                    resize: that.options.nodeWidthResize || false,
                    data: db
                });

                // 想关数据变化
                db.icon = eval('\'' + db.icon + '\'');
                Array.isArray(db.contextMenu) && db.contextMenu.forEach(function (d1) {
                    d1.icon = eval('\'' + d1.icon + '\'');
                });

                // 图标转换
                if(db.icon =='#ts-dev-3cube'){
                    d.icon = db.icon ='#ts-cubes';
                }else if(db.icon =='#ts-dev-cube'){
					d.icon = db.icon ='#ts-cube';
                }else if(db.icon =='#ts-dev-database'){
					d.icon = db.icon = '#ts-database';
                }else if(db.icon =='#ts-dev-gear'){
					d.icon = db.icon ='#ts-setting';
                }else if(db.icon =='#ts-dev-gears'){
					d.icon = db.icon ='#ts-cogs';
                }else if(db.icon =='#ts-dev-location'){
					d.icon = db.icon ='#ts-location';
                }else if(db.icon =='#ts-dev-network'){
					d.icon = db.icon ='#ts-internet';
                }else if(db.icon =='#ts-dev-team'){
					d.icon = db.icon ='#ts-team';
                }else if(db.icon =='#ts-dev-user'){
					d.icon = db.icon ='#ts-user';
                }else if(db.icon =='#ts-dev-websphere'){
					d.icon = db.icon ='#ts-internet';
                }

                // !db.stroke && (db.stroke = db.fill);
                // 添加节点信息展示字典
                Object.assign(db.info, { // key为db中的属性，值为展示的detail
                    type: 'node',
                    label: '节点',
                    name: {
                        label: '名称',
                        value: db.name,
                        editable: true,
                        type: 'input'
                    },
                    stroke: {
                        label: '边框',
                        value: db.stroke,
                        editable: true,
                        type: 'color'
                    },
                    fill: {
                        label: '填充',
                        value: db.fill,
                        editable: true,
                        type: 'color'
                    }
                });
                return db;
            });

            this.nodesData.add(nodesDataArr);

            return nodesDataArr;
        },
        toJson: function () {
            var datas = this.getAll('data');
            var data = datas.map(function (d) {
                var o = Object.assign({}, d.data);
                o.x = d.x;
                o.y = d.y;
                return o;
            });
            return {
                nodes: data
            };
        },
        draw: function (data, action) {
            var that = this;
            var typeData = {}; // 节点类型分类

            // filter出需要update数据
            var updateData = d3.selectAll(this.nodes).data().filter(function (d) {
                var fd = data.find(function (d1) {
                    return d1.uuid === d.uuid;
                });
                fd && (d.data = fd.data);
                return fd;
            });

            var nodes = this.el.selectAll('g.' + this.options.nodeClass).data(data, function (d) {
                return d.uuid + d.reuuid;
            });

            // enter
            var nodesEnter = nodes.enter().append('g')
                .attr('class', this.options.nodeClass)
                .attr('cursor', 'pointer')
                .attr('transform', function (d) {
                    return `translate(${d.x},${d.y})`;
                });
            nodesEnter.dispatch('add');

            // exit
            var nodesExit = nodes.exit();
            if (action === 'init') {
                nodesExit.dispatch('remove');
			} else if (action === 'relation') {
				// nodesExit.each(function (d) {
				// 	var links = that.Link.getLinkByNode(d);
				// 	links.dispatch('hide');
				// });
				// nodes.classed('hide', false);
				// nodesExit.classed('hide', true);
            } else if (action === 'remove') {
				// this.update(nodes);
				// nodesExit.dispatch('remove');
            }

            nodes.merge(nodesEnter).each(function (d) {
                if (d.status.hide) d3.select(this).classed('hide', true);
                else d3.select(this).classed('hide', false);
            });

            // 节点类型type归纳
            nodesEnter.each(function (d) {
                d.el = this;
                if (d.type && that.getVm(d.type)) {
                    Array.isArray(typeData[d.type]) ? typeData[d.type].push(d) : typeData[d.type] = [d];
                } else { // 直接添加节点

                }
            });

            // 置顶
            nodesEnter.on('raise',function (d) {
                d.groups.forEach(function (d1) {
                    d3.select(d1.el).dispatch('raise');
                });
                d3.select(this).raise();
            });

            var renderNodes = nodesEnter.filter(function (d) {
                return !typeData.hasOwnProperty(d.type);
            });

            // 添加形状
            that.appendShape(renderNodes);//.setShape(renderNodes);
            // 添加icon
            that.appendIcon(renderNodes);
            // 添加name
            that.appendName(renderNodes);
            // 添加remark
            that.appendRemark(renderNodes);
            // 添加remark
            that.appendSign(renderNodes);
            // 添加节点状态
            that.appendStatus(renderNodes);
            // 添加节点control
            that.appendBtns(renderNodes);
            // 添加节点alert icon
            that.appendAlertIcon(renderNodes);

            // 节点添加
            Object.keys(typeData).forEach(function (key) {
                that.getVm(key) && that.getVm(key).draw(typeData[key]);
            });

            // 存放节点
            this.nodes.add(nodesEnter.nodes());

            // update
            nodes = nodes.data(updateData, function (d) {
                return d.uuid;
            });
            this.update(nodes);

            // 添加drag
            this.setDrag(nodesEnter);

            // 添加marker
            this.appendMarker(nodesEnter);

            // 锚点数据
            var anchorData = {};

            if (this.options.nodeAnchorable) {
                nodesEnter.each(function (d) {
                    that.Anchor.constructor.call(that, {
                        shape: that.options.nodeAnchorShape || 'circle',
                        offset: 0, // 锚点离中心点的偏移量
                        isTop: that.options.nodeAnchorIsTop || true, // 锚点是否要显示在节点的上面， 默认是显示在节点后面
                        effect: that.options.nodeAnchorShadow || 'assistNode', // 锚点牵连效果
                        size: that.options.nodeAnchorSize || 4, // 锚点大小
                        amount: that.options.nodeAnchorAmount || 8, // 锚点数量
                        defaultDir: that.options.nodeAnchorDefaultDir || 'LR', // 锚点默认开始的方向
                        fill: that.options.nodeAnchorFill || 'white',
                        stroke: that.options.nodeAnchorStroke || '#336EFF',
                        mouseOver: 'assist',
                        disable: d.anchorDisable || typeof that.options.anchorDisable === 'function' && that.options.anchorDisable(d.data), // 是否禁用anchor
                    }, d3.select(this));
                })
            }

            // 节点添加动画
            nodesEnter.on('add', function (d) {
                var node = d3.select(this);
                that.transformTransition({
                    nodes: node,
                    name: 'enter',
                    trans: {
                        x0: d.x,
                        y0: d.y,
                        k0: 0,
                        x1: d.x,
                        y1: d.y,
                        k1: 1
                    },
                    o0: 0,
                    o1: 1,
                    end: function () {
                        that.options.hierTransAble && that.transitionEnd && that.transitionEnd();
                    }
                });
            });
            nodesEnter.dispatch('add');

            // 节点remove动画

            nodesEnter
                .on('remove', function (d) {
                    var node = d3.select(this);
                    that.transformTransition({
                        nodes: node,
                        name: 'exit',
                        trans: {
                            x0: d.x,
                            y0: d.y,
                            k0: 1,
                            x1: d.x,
                            y1: d.y,
                            k1: 0
                        },
                        o0: 1,
                        o1: 0,
                        end: function () {
                            node.remove();
                        }
                    });
					that.nodesData.remove(d);
					that.nodes.remove(d);
					var links = that.Link.getLinkByNode(d);
					links.dispatch('remove');
					// 改变groups
                    var group = d.groups.forEach(function (d1) {
                        var index = d1.contain.indexOf(d);
                        if (index !== -1) {
                            d1.contain.splice(index, 1);
                        }
                        if (d1.contain.length === 0) {
                            d3.select(d1.el).dispatch('remove');
                        } else {
                            that.gatherLayout({groups: [d1]});
                            d1.whChange = d1.width + '+' + d1.height;
                            d1.contain.forEach(function (d2) {
                                d2.xyChange = d2.x + '+' + d2.y;
							});
                        }
                    });
                })
				.on('mousemove', function () {
					event.stopPropagation();
				})
				.on('mouseover', function () {
					event.stopPropagation();
				})
                .on('mouseenter', function (d) {
                	event.stopPropagation();
                    var node = d3.select(this);
                    node.dispatch('raise');
					that.options.nodeMouseover(d.port, 'node');
                })
				.on('mouseleave', function (d) {
					event.stopPropagation();
					that.Tip.hide();
					typeof that.options.nodeMouseout === 'function' && that.options.nodeMouseout(d.port, 'node');
				})
				.on('mouseout', function (d) {
					event.stopPropagation();
					// that.Tip.hide();
				})
                .on('dblclick', function (d) {
                    d3.event.stopPropagation();
                    if (typeof d.data.ondblclick === 'function') {
                        d.data.ondblclick(d.port);
                    }
                })
                .on('contextmenu', function (d) {
                    d3.event.stopPropagation();
                    d3.event.preventDefault();
                    that.hook('contextmenu', d);
                });

            // 添加节点接口实例
            this.createPort(nodesEnter, opts.type);

            // chlidren sort
            nodesEnter.each(function () {
                that.elementChildrenSort(this);
            });

            return this;

        },
        update: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                Object.keys(d.data).forEach(function (key) {
                    if (key === 'status') {

                    } else if (d.hasOwnProperty(key)) {
                        d[key] = d.data[key];
                    }
                });
                if (d.hasOwnProperty('x') || d.hasOwnProperty('y')) {
                    d.xyChange = d.x + '+' + d.y;
                }
            });
        },
        render: function (data, action) {
            if (!data || !Array.isArray(data) || !data.length) return false;
            var vm = this.getVm(opts.type);
            // 数据处理z
            data = vm.dataFn(data);
            // 添加节点（分配增删改）
            vm.draw(data, action);
        },
        transformTransition: function (data) {
            if (!data || !data.nodes) return false;
            var transNodes = data.nodes;
            var durationTime = data.hasOwnProperty('t') ? data.t : this.options.duration;
            var zoomIdentity0 = d3.zoomIdentity;
            var zoomIdentity1 = d3.zoomIdentity;

            if (data.hasOwnProperty('trans')) {
                var b0 = data.trans.hasOwnProperty('x0') && data.trans.hasOwnProperty('y0') && (zoomIdentity0 = zoomIdentity0.translate(data.trans.x0, data.trans.y0));
                var b1 = data.trans.hasOwnProperty('k0') && (zoomIdentity0 = zoomIdentity0.scale(data.trans.k0));
                (b0 || b1) && transNodes.attr('transform', zoomIdentity0);
            }
            durationTime && (transNodes = transNodes.transition(data.hasOwnProperty('name') ? data.name : 'node').duration(durationTime)).ease(d3.easeLinear);
            data.hasOwnProperty('o0') && transNodes.attr('opacity', data.o0);
            if (data.hasOwnProperty('trans')) {
                var b0 = data.trans.hasOwnProperty('x1') && data.trans.hasOwnProperty('y1') && (zoomIdentity1 = zoomIdentity1.translate(data.trans.x1, data.trans.y1));
                var b1 = data.trans.hasOwnProperty('k1') && (zoomIdentity1 = zoomIdentity1.scale(data.trans.k1));
                (b0 || b1) && transNodes.attr('transform', zoomIdentity1);
            }
            data.hasOwnProperty('o1') && transNodes.attr('opacity', data.o1);
            transNodes
                .on('start', function () {
                    data.start && data.start(data.nodes);
                }).on('end', function () {
                data.end && data.end(data.nodes);
            });
            return transNodes;
        },
        shapeHandle: function (data) {
            var that = this;
            switch (data.action) {
                case 'size': // 大小
                    if (data.data.shape === 'circle') { // 圆形
                        data.data.size = data.data.data.size || that.options.size;
                        data.data.width = data.data.size * 2;
                        data.data.height = data.data.size * 2;
                    } else {
                        data.data.width = that.options.nodeWidth || that.options.minWidth;
                        data.data.height = that.options.nodeHeight || that.options.minHeight;
                    }
                    if (data.resize) {
                        var textbbox = that.calculateTextLen(data.data.icon, data.data.iconSize);
                        var w = textbbox.width + that.options.padding * 2;
                        var h = textbbox.height + that.options.padding * 2;
                        data.data.width = w > that.options.minWidth ? w : that.options.minWidth;
                        data.data.height = h > that.options.minHeight ? h : that.options.minHeight;
                    }
                    break;
                default:
            }

        },
        appendShape: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                var node = d3.select(this);
                var shapeEle = node.append(function () {
                    return that.Util.shape(d, -1).node();
                }).attr('class', 'shape')
                    .attr('data-sort', 10)
                // .attr('filter', `url(#node-shadow-${that.options.id})`);

                var shapeSelect = node.append(function () {
                    return that.Util.shape(d, 3).node();
                }).attr('class', 'select')
                    .attr('data-sort', 2)
                    .attr('fill', 'transparent');

                // 告警效果添加
                var wave1 = node.append(function () {
                    return that.Util.shape(d, -2).node();
                }).attr('stroke-width', 1.5)
                    .attr('fill', 'none')
                    .attr('data-sort', 0)
                    .attr('class', 'alert');
                var wave2 = node.append(function () {
                    return that.Util.shape(d, -2).node();
                }).attr('stroke-width', 1.5)
                    .attr('fill', 'none')
                    .attr('data-sort', 0)
                    .attr('class', 'alert');

                // 设置节点
                var shapeUse = node.selectAll('use.shape'); // 有待考虑

                var alert = d3.selectAll([wave1.node(), wave2.node()]);

                if (d.hasOwnProperty('xyChange')) {
                    console.warn(d);
                    // return;
                }

                Object.defineProperties(d, {
                    fill: {
                        get: that.objectGet('fill', d.fill),
                        set: function (value) {
                            if (this.getSet.fill !== value) {
                                shapeEle.attr('fill', value);
                                // alert.attr('stroke', value);
                                this.getSet.fill = value;
                            }
                        }
                    },
                    stroke: {
                        get: that.objectGet('stroke', d.stroke),
                        set: function (value) {
                            if (this.getSet.stroke !== value) {
                                shapeEle.attr('stroke', value);
                                this.getSet.stroke = value;
                            }
                        }
                    },
                    strokeWidth: {
                        get: that.objectGet('strokeWidth', d.fill),
                        set: function (value) {
                            if (this.getSet.strokeWidth !== value) {
                                shapeEle.attr('stroke-width', d.strokeWidth);
                                this.getSet.strokeWidth = value;
                            }
                        }
                    },
                    strokedasharray: {
                        get: that.objectGet('strokedasharray', d.strokedasharray),
                        set: function (val) {
                            if (this.getSet.strokedasharray !== val) {
                                shapeEle.attr('stroke-dasharray', val);
                                this.getSet.strokedasharray = val;
                            }
                        }
                    },
                    // selectStroke: {
                    //     get: that.objectGet('selectStroke', d.selectStroke),
                    //     set: function (val) {
                    //         if (this.getSet.selectStroke !== val) {
                    //             shapeSelect.attr('stroke', val);
                    //             this.getSet.selectStroke = val;
                    //         }
                    //     }
                    // },
                    // selectStrokeWidth: {
                    //     get: that.objectGet('selectStrokeWidth', d.selectStrokeWidth),
                    //     set: function (val) {
                    //         if (this.getSet.selectStrokeWidth !== val) {
                    //             shapeSelect.attr('stroke-width', val);
                    //             this.getSet.selectStrokeWidth = val;
                    //         }
                    //     }
                    // },
                    // selectStrokedasharray: {
                    //     get: that.objectGet('selectStrokedasharray', d.selectStrokedasharray),
                    //     set: function (val) {
                    //         if (this.getSet.selectStrokedasharray !== val) {
                    //             shapeSelect.attr('stroke-dasharray', val);
                    //             this.getSet.selectStrokedasharray = val;
                    //         }
                    //     }
                    // },
                    xyChange: {
                        get: that.objectGet('xyChange', d.x + '+' + d.y),
                        set: function (val) {
                            if (this.getSet.xyChange !== val) {
                                if (d3.event && d3.event.type === 'drag') {
                                    that.transformTransition({
                                        nodes: node,
                                        t: 0,
                                        trans: {
                                            x1: d.x,
                                            y1: d.y
                                        }
                                    });
                                    d.in.concat(d.out).forEach(function (d1) {
                                        d1.points.splice(1, d1.points.length - 2);
                                        if (d1.points.length > 1) {
											d1.points[0].x = d1.source.x;
											d1.points[0].y = d1.source.y;
											d1.points[1].x = d1.target.x;
											d1.points[1].y = d1.target.y;
                                        }
                                    })
                                }
                                else {
                                    that.transformTransition({
                                        nodes: node,
                                        name: d.uuid + d.reuuid,
                                        trans: {
                                            x1: d.x,
                                            y1: d.y
                                        },
                                        end: function () {

                                        }
                                    });
									d.getSet.xyChange = val;
                                }
                                var links = d.in.concat(d.out);
                                links = links.map(function (d1) {
                                    return d1.el;
                                });
                                that.Link.update(d3.selectAll(links));
                            }
                        }
                    },
                    alert: {
                        get: that.objectGet('alert', false),
                        set: function (val) {
                            if (this.getSet.alert !== val) {
                                alert.each(function (d1, i1) {
                                    if (val) {
                                        d3.select(this)
                                            .attr('stroke', d.stroke)
                                            .classed('alert-wave' + (i1 + 1), true);
                                    } else {
                                        d3.select(this)
											.attr('stroke', 'none')
                                            .classed('alert-wave' + (i1 + 1), false);
                                    }
                                });
                                this.getSet.alert = val;
                            }
                        }
                    }
                });

                // 数据初始化
                d.fill = d.fill;
                d.stroke = d.stroke;
                d.strokeWidth = d.strokeWidth;
                d.strokedasharray = d.strokedasharray;
                // d.selectStroke = d.selectStroke;
                // d.selectStrokeWidth = d.selectStrokeWidth;
                // d.selectStrokedasharray = d.selectStrokedasharray;

                // 操作
                node
                    .on('selected', function (d) {
                        if (d.status.selected || !d.selected.selectable) return false; // 防止统一状态反复触发，提高性能
                        d.status.selected = true;
                        // d.selectStroke = that.options.selectStroke;

                        if (typeof that.options.nodeSelected === 'function') {
                            that.options.nodeSelected(d.port);
                        } else {
                            shapeSelect
                                .attr('stroke', d.selected.stroke)
                                .attr('fill', d.selected.fill)
                                .attr('stroke-width', d.selected.strokeWidth)
                                .attr('stroke-dasharray', d.selected.strokedasharray)
                                .attr('transform', `scale(1)`)

                            if (d.selected.selectMargin) {
                                shapeSelect
                                    .attr('width', d.width + d.margin[1] + d.margin[3] + 9)
                                    .attr('height', d.height + d.margin[0] + d.margin[2] + 9)
                                    .attr('transform', `translate(${-d.margin[3] - 3},${-d.margin[0] - 3})`)
                            }
                        }
                    })
                    .on('unselect', function (d) {
                        if (!d.status.selected || !d.selected.selectable) return false; // 防止统一状态反复触发，提高性能
                        d.status.selected = false;

                        if (typeof that.options.nodeUnselected === 'function') {
                            that.options.nodeUnselected(d.port);
                        } else {
                            shapeSelect
                                .attr('stroke', 'transparent')
                                .attr('fill', d.selected.fill)
                                .attr('stroke-width', d.selected.strokeWidth)
                                .attr('stroke-dasharray', d.selected.strokedasharray)
                                .attr('transform', `scale(0)`)
                        }
                    })
                    .on('click', function () {
                        d3.event.stopPropagation();
                        if (!d3.event.ctrlKey && that.options.nodeRadio) { // 多选
                            that.getRoom().getAll('condition', 'selected').filter(function (d1) {
                                return d1 !== d;
                            }).dispatch('unselect');
                        }

                        if (d.status.selected) {
                            node.dispatch('unselect');
                        } else {
                            node.dispatch('selected');
                        }

                        // 处理点击
                        that.options.nodeClick(d.port);

                        that.hook('click', d);
                    })
                    .on('signWidth.shape', function (d) {
                        if (!d.sign.disable) {
                            shapeEle
                                .attr('width', (1 - d.sign.ratio) * d.width - 2);
                        } else {
                            shapeEle
                                .attr('width', d.width - 2);
                        }
                    });

				shapeEle.on('mouseenter', function () {
					that.options.nodeMouseover(d.port, 'node');
                }).on('mosueleave', function () {
					that.Tip.hide();
                })
            });

            return this;
        },
        appendStatus: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                var node = d3.select(this);

                var statusG = node.append('g').attr('class', 'status')
                    .attr('data-sort', 13);

                var nodeChildren = Array.prototype.slice.call(node.node().childNodes);
                var shadeRect, statusIcon

                node
                    .on('setStatus', function () {
                        var data = d3.event.detail;
                        var _this = this;
                        if (data) {
                            if (data.name === 'lock') {
                                Object.assign(data, {
                                    shade: true,
                                    icon: data.icon || '#ts-lock',
                                    size: d.iconSize + 4 || 14,
                                    color: 'white'
                                })
                            } else if (data.name === 'abort') {
                                Object.assign(data, {
									shade: true,
									shadeColor: 'transparent',
                                    icon: data.icon || '#ts-stop',
                                    size: d.iconSize + 4 || 14,
                                    color: 'rgb(153, 153, 153)',
                                    opacity: 0.3,
                                })
                            }
                            Object.assign(d.state, data);
                            if (data.shade) {
								shadeRect = shadeRect || statusG.append('rect')
								shadeRect.attr('width', d.width)
									.attr('height', d.height)
									.attr('x', -d.width / 2)
									.attr('y', -d.height / 2)
									.attr('fill-opacity', 0.7)
									.attr('fill', data.shadeColor || 'black');
                            }
                            if (data.icon) {
								statusIcon = statusIcon || statusG.append('use')
								statusIcon.attr('xlink:href', data.icon)
                                    .attr('fill', data.color)
                                    .attr('x', -data.size / 2)
                                    .attr('y', -data.size / 2)
                                    .attr('width', data.size)
                                    .attr('height', data.size);
                            }
                            if (data.opacity) {
                                d3.selectAll(nodeChildren).filter(function () {
                                    return this !== statusG.node();
                                }).attr('opacity', data.opacity);
                            } else {
                                d3.selectAll(nodeChildren).attr('opacity', null)
                            }
                        } else {
                            statusG.html('');
							shadeRect = null;
							statusIcon = null;
							d3.selectAll(nodeChildren).attr('opacity', null)
                        }
                    })
                // 相关事件阻止
				statusG
                    .on('click', function () {
                        // d3.event.stopPropagation();
                    })
                    .call(d3.drag());

                node.dispatch('setStatus', {detail: {name: d.status.name}});

            });
            return this;
        },
        appendBtns: function (nodes, btns) {
            var that = this;
            var btnsData = {
                remove: {
                    name: 'remove',
                    title: '删除',
                    width: 16,
                    height: 16,
                    shape: 'circle',
                    icon: eval('\'' + '#ts-remove' + '\''),
                    iconSize: 10,
                    iconColor: 'white',
                    stroke: '#595959',
                    fill: '#595959',
                    position: 'TR',
                },
                defaultOpt: {
                    name: '',
                    title: '',
                    width: 16,
                    height: 16,
                    shape: 'circle',
                    icon: eval('\'' + '' + '\''),
                    iconSize: 10,
                    iconColor: 'white',
                    stroke: '#595959',
                    fill: '#595959',
                    position: 'TR',
                }
            };
            nodes.each(function (d) {
                var node = d3.select(this);

                var btnG = node.append('g').attr('class', 'btnG').attr('data-sort', 14).attr('opacity', 1);

                node.on('setBtns', function () {
                    var btns = d3.event.detail && d3.event.detail.btns || [];
                    btns = btns.map(function (d1, i1) {
                        var opts = btnsData.hasOwnProperty(d1.name) ? btnsData[d1.name] : btnsData.defaultOpt;
                        var o = Object.assign(opts || {}, d1);
						if (o.position === 'TR') {
							o.x = d.width / 2;
							o.y = -d.height / 2;
						}
                        return o;
                    });
                    var G = btnG.selectAll('g.btn').data(btns, function (d) {
                            return d.name;
                        });
                    var btnEnter = G.enter().append('g').attr('class', 'btn')
                        .attr('transform', (d1) => `translate(${d1.x},${d1.y})scale(0)`);

                    var btnExit = btnG.exit().remove();

                    var shape = btnEnter
                        .append(function (d1) {
                            return that.Util.shape(d1, 0).node();
                        })
                        .attr('stroke', d1 => d1.stroke)
                        .attr('fill', d1 => d1.fill);

                    var title = btnEnter.append('title').text(d => d.title);

                    btnEnter.each(function (d1) {
                        var node = d3.select(this);
                        var isUse = d1.icon[0] === '#';
                        var icon = node.append(isUse ? 'use' : 'text');
                        if (isUse) {
                            icon
                                .attr('x', -d1.iconSize / 2)
                                .attr('y', -d1.iconSize / 2)
                                .attr('width', d1.iconSize)
                                .attr('height', d1.iconSize)
                                .attr('xlink:href', d1.icon);
                        } else {
                            icon
                                .attr('text-anchor', 'middle')
                                .attr('font-size', d1.iconSize)
                                .attr('x', 0)
                                .attr('y', 0)
                                .attr('dy', '0.32em')
                                .attr('font-family', d.fontFamily)
                                .text(d1.icon)
                        }
                        icon.attr('fill', d1.iconColor);
                    }).on('click', function (d1) {
                        d3.event.stopPropagation();
                        if (typeof d1.clickCb === 'function') {
                            d1.clickCb(d.port);
                        } else if (d1.name === 'remove') {
                            node.dispatch('remove');
                        }

                    });

                    node.on('hideBtns', function () {
						btnEnter.each(function (d1) {
							that.transformTransition({
								nodes: d3.select(this),
								t: 300,
								trans: {
									x1: d1.x,
									y1: d1.y,
									k1: 0,
								},
								end: function () {
									// btnG.classed('hide', true);
								}
							})
                        })
					}).on('showBtns', function () {
						btnEnter.each(function (d1) {
							that.transformTransition({
								nodes: d3.select(this),
                                t: 300,
								trans: {
									x1: d1.x,
									y1: d1.y,
									k1: 1,
								},
								end: function () {
									// btnG.classed('hide', true);
								}
							})
						});
					})
                }).on('mouseenter.btn', function () {
                    node.dispatch('showBtns');
                }).on('mouseleave.btn', function () {
                    node.dispatch('hideBtns');
                });
                node.dispatch('setBtns', {detail: {btns: d.btns}});
            });
            return this;
        },
        appendAlertIcon: function (nodes, btns) {
            var that = this;
			var btnsData = {
				defaultOpt: {
					name: 'default',
					title: '告警',
					width: 16,
					height: 16,
					shape: 'rect',
					icon: eval('\'' + '#ts-m-monitor' + '\''),
					iconSize: 14,
					iconColor: 'red',
					stroke: 'transparent',
					fill: 'transparent',
					position: 'RT',
				}
			};
            nodes.each(function (d) {
                var node = d3.select(this);

                var btnG = node.append('g').attr('class', 'infobtnG').attr('data-sort', 14).attr('opacity', 1);

                node.on('setInfoBtns', function () {
                    var btns = d3.event.detail && d3.event.detail.btns || [];
                    btns = btns.map(function (d1, i1) {
                        var opts = btnsData.hasOwnProperty(d1.name) ? btnsData[d1.name] : btnsData.defaultOpt;
                        var o = Object.assign({}, opts || {}, d1);
                        if (o.position === 'RT') {
                            o.x = -d.width / 2 - o.width / 2 - d.margin[3] - 4;
                            o.y = -d.height / 2 + o.height / 2;
                        }
                        return o;
                    });
                    var G = btnG.selectAll('g.btn').data(btns, function (d) {
                        return d.name;
                    });
                    var btnEnter = G.enter().append('g').attr('class', 'btn')
                        .attr('transform', (d1) => `translate(${d1.x},${d1.y})scale(1)`);

                    var btnExit = G.exit().remove();

					G.each(function (d) {
                        var d1 = btns.find(function (d2) {
                            return d.name === d2.name;
                        });
                        Object.assign(d, d1);
					});

                    var shape = btnEnter
                        .append(function (d1) {
                            return that.Util.shape(d1, 0).node();
                        })
                        .attr('stroke', d1 => d1.stroke)
                        .attr('fill', d1 => d1.fill);

                    btnEnter.each(function (d1) {
                        // d.infoBtns.push(d1);
                        var o = Object.assign({}, d1);
                        var node = d3.select(this);
                        var isUse = d1.icon[0] === '#';
                        var icon = node.append(isUse ? 'use' : 'text');
                        var iconText = '';
                        var iconColor = '';

						var title = node.append('title').text(d => d.title);

                        Object.defineProperties(d1, {
                            icon: {
                                get: function () {
                                    return iconText;
                                },
                                set: function (value) {
                                    console.log(value, iconText);
                                    if (value === iconText) return;
                                    if (isUse) {
                                        icon
                                            .attr('x', -d1.iconSize / 2)
                                            .attr('y', -d1.iconSize / 2)
                                            .attr('width', d1.iconSize)
                                            .attr('height', d1.iconSize)
                                            .attr('xlink:href', value);
                                    } else {
                                        icon
                                            .attr('text-anchor', 'middle')
                                            .attr('font-size', d1.iconSize)
                                            .attr('x', 0)
                                            .attr('y', 0)
                                            .attr('dy', '0.32em')
                                            .attr('font-family', d.fontFamily)
                                            .text(value)
                                    }
                                    iconText = value;
                                }
                            },
                            iconColor: {
                                get: function () {
                                    return iconColor;
                                },
                                set: function (value) {
                                    if (value === iconColor) return;
                                    icon.attr('fill', value);
                                    iconColor = value;
                                }
                            }
                        });

                        // icon style
                        d1.icon = o.icon;
                        d1.iconColor = o.iconColor;

                    }).on('click', function (d1) {
                        d3.event.stopPropagation();
                        if (typeof d1.clickCb === 'function') {
                            d1.clickCb(d.port, d1.data);
                        }
                    }).on('mouseenter', function () {
						d3.event.stopPropagation();
					}).on('mouseover', function () {
						d3.event.stopPropagation();
					});

                    node.on('hideInfoBtns', function () {
                        btnEnter.each(function (d1) {
                            that.transformTransition({
                                nodes: d3.select(this),
                                t: 300,
                                trans: {
                                    x1: d1.x,
                                    y1: d1.y,
                                    k1: 0,
                                },
                                end: function () {
                                    // btnG.classed('hide', true);
                                }
                            })
                        })
                    }).on('showInfoBtns', function () {
                        btnEnter.each(function (d1) {
                            that.transformTransition({
                                nodes: d3.select(this),
                                t: 300,
                                trans: {
                                    x1: d1.x,
                                    y1: d1.y,
                                    k1: 1,
                                },
                                end: function () {
                                    // btnG.classed('hide', true);
                                }
                            })
                        });
                    })
                });
				node.dispatch('setInfoBtns', {detail: {btns: d.infoBtns}}).dispatch('showInfoBtns');
            });
            return this;
        },
        appendParents: function (data, children) {
            var that = this;
            var nodes0 = this.getAll('data');
            var links0 = this.Link.getAll('data');
            var groups0 = this.Group.getAll('data');
            if (!children || (!Array.isArray(children.nodes) || !Array.isArray(children.links))) return;

            // 节点去重
            var childrenNodes = children.nodes.filter(function (d) {
                return !nodes0.some(function (a) {
                    return a.uuid === d.uuid;
                }) && data.children.every(function (a) {
                    return a.uuid !== d.uuid;
                });
            });

            // 连线去重
            var childrenLinks = children.links.filter(function (d) {
                return !links0.some(function (a) {
                    return (a.source.uuid === d.source) && (a.target.uuid === d.target);
                }) && (data.uuid === d.target && data.uuid !== d.source);
            });

			if (!childrenNodes.length && !childrenLinks.length) return;
			data.children = data.children.concat(this.dataFn(childrenNodes));
            childrenLinks = this.Link.dataFn(childrenLinks);

            data.children.forEach(function (a) {
                var is = nodes0.some(function (b) {
                    return a.uuid === b.uuid;
                });
                if (!is) return nodes0.push(a);
            });
            var links = links0.concat(childrenLinks);

            if (this.options.layoutOpts.ranker === 'network-tree') {
                var layoutData = {
                    links: links,
                };
                this.Tree.setSourceData({links: links});
                that.options.hierTransAble = false;
                that.Link.options.hierTransAble = false;

                var dagreData = this.parent.layout('append');
                var nodes = dagreData.nodes.map(function (d) {
                    return d.data;
                }).concat(dagreData.hideNodes);

                layoutData.nodes = nodes;

                this.Tree.setSourceData({nodes: nodes});
                that.Tree.setAssistDepthNodes(nodes);
                this.parent.drawData(layoutData, 'init');
            } else {
                this.parent.layout(layoutData, 'click');
            }
        },
        removeParents: function (data) {
            var that = this;

            for (var i = 0; i < data.in.length; i++) {
                var i1 = data.children.indexOf(data.in[i].source);
                var i2 = data.reChildren.indexOf(data.in[i].source);
                if (i1 !== -1) {
                    data.in.splice(i, 1);
                    data.children.splice(i1, 1);
                    i--;
                }
                if (i2 !== -1) {
                    data.in.splice(i, 1);
                    data.reChildren.splice(i2, 1);
                    i--;
                }
            }

            if (this.options.layoutOpts.ranker === 'network-tree') {
                var dagreDatas = this.parent.layout('remove');
                // nodes
                var nodes = dagreDatas.nodes.map(function (d) {
                    return d.data;
                });
                nodes = nodes.concat(dagreDatas.hideNodes);
                // links
                var links = dagreDatas.links
                    .map(function (d) {
                        return d.data;
                    });

                var parent = dagreDatas.nodes.concat(dagreDatas.hideNodes).find(function (d) {
                    return d.data === data;
                });
                links = links.concat(dagreDatas.hideLinks);
                // remove links
                dagreDatas.removeLinks.forEach(function (d) {
                    var i = links.indexOf(d);
                    if (i !== -1) {
                        links.splice(i, 1);
                    }
                });
                // remove nodes
				dagreDatas.removeNodes.forEach(function (d) {
					var i = nodes.indexOf(d);
					if (i !== -1) {
						nodes.splice(i, 1);
					}
				});

                var removeLinks = links.filter(function (d) {
                    return (parent && parent.parent && parent.parent.data !== d.source) && (d.target === data);
                });

                removeLinks.forEach(function (d) {
                    var i = links.indexOf(d);
                    if (i !== -1) {
                        links.splice(i, 1);
                    }
                });

                var layoutData = {
                    nodes: nodes,
                    links: links,
                };

                this.Tree.setSourceData(layoutData);
                that.Tree.setAssistDepthNodes(layoutData.nodes);

                this.parent.drawData(layoutData, 'init');
            } else {
                // var layoutData = {
                // 	nodes: that.getAll(),
                // 	links: links,
                // };
                // this.parent.layout(layoutData, 'click');
            }
        },
        appendChildren: function (data, children) {
            var that = this;
            var nodes0 = this.getAll('data');
            var links0 = this.Link.getAll('data');
            var groups0 = this.Group.getAll('data');
            if (!children || (!Array.isArray(children.nodes) || !Array.isArray(children.links))) return;

            // 节点去重
            var childrenNodes = children.nodes.filter(function (d) {
                return !nodes0.some(function (a) {
                    return a.uuid === d.uuid;
                }) && !data.children.concat(data.reChildren).some(function (a) {
                    return a.uuid === d.uuid;
                });
            });

            // 连线去重
            var childrenLinks = children.links.filter(function (d) {
                var isExit = links0.find(function (a) {
                    return (a.source.uuid === d.source) && (a.target.uuid === d.target);
                });
                return !isExit && (data.uuid !== d.target && data.uuid === d.source);
            });
			if (!childrenNodes.length && !childrenLinks.length) return;
			// 添加新节点
			data.children = data.children.concat(this.dataFn(childrenNodes));
            childrenLinks = this.Link.dataFn(childrenLinks);

            var links = links0.concat(childrenLinks);

            if (this.options.layoutOpts.ranker === 'network-tree') {
                var layoutData = {
                    links: links,
                };
                this.Tree.setSourceData({links: links});
                that.options.hierTransAble = false;
                that.Link.options.hierTransAble = false;

                var dagreData = this.parent.layout('append');
                var nodes = dagreData.nodes.map(function (d) {
                    return d.data;
                }).concat(dagreData.hideNodes);

                layoutData.nodes = nodes;

                this.Tree.setSourceData({nodes: nodes});
                that.Tree.setAssistDepthNodes(nodes);

                this.parent.drawData(layoutData, 'init');
            } else {
                // 添加到nodes0中
                var layoutData = {
                    nodes: nodes0.concat(data.children),
                    links: links,
                    groups: groups0
                };
                console.log(layoutData);
                this.parent.layout(layoutData, 'click');
                this.parent.drawData(layoutData, 'init');
            }
        },
        removeChildren: function (data) {
            var that = this;
            var removeChildren = [];
            for (var i = 0; i < data.out.length; i++) {
                var i1 = data.children.indexOf(data.out[i].target);
                var i2 = data.reChildren.indexOf(data.out[i].target);
                if (i1 !== -1) {
                    removeChildren.push(data.children[i1]);
                    data.out.splice(i, 1);
                    data.children.splice(i1, 1);
                    i--;
                }
                if (i2 !== -1) {
                    removeChildren.push(data.reChildren[i2]);
					data.out.splice(i, 1);
					data.reChildren.splice(i2, 1);
					i--;
                }
            }
            if (this.options.layoutOpts.ranker === 'network-tree') {
                var dagreDatas = this.parent.layout('remove');
                // nodes
                var nodes = dagreDatas.nodes.map(function (d) {
                    return d.data;
                });
				nodes = nodes.concat(dagreDatas.hideNodes);
				// links
                var links = dagreDatas.links
                    .map(function (d) {
                        return d.data;
                    });
                var parent = dagreDatas.nodes.concat(dagreDatas.hideNodes).find(function (d) {
                    return d.data === data;
                });
                links = links.concat(dagreDatas.hideLinks);

                var removeLinks = links.filter(function (d) {
                    return (parent && parent.parent && parent.parent.data !== d.target) && (d.source === data);
                });

                // remove links
                dagreDatas.removeLinks.forEach(function (d) {
                    var i = links.indexOf(d);
                    if (i !== -1) {
                        links.splice(i, 1);
                    }
                });
                // rmeove nodes
				dagreDatas.removeNodes.forEach(function (d) {
					var i = nodes.indexOf(d);
					if (i !== -1) {
						nodes.splice(i, 1);
					}
				});

                removeLinks.forEach(function (d) {
                    var i = links.indexOf(d);
                    if (i !== -1) {
                        links.splice(i, 1);
                    }
                });

                var layoutData = {
                    nodes: nodes,
                    links: links,
                };

                this.Tree.setSourceData(layoutData);
                that.Tree.setAssistDepthNodes(layoutData.nodes);

                this.parent.drawData(layoutData, 'init');
            } else {
                // 添加到nodes0中
                var nodes = this.getAll('data');
                var links = this.Link.getAll('data');
                removeChildren.forEach(function (d) {
                    var i = nodes.indexOf(d);
                    if (i !== -1) nodes.splice(i, 1);
                    // 删除线
                    var link = links.find(function (d1) {
                        return d1.target.uuid === d.uuid && d1.source.uuid === data.uuid;
                    });
                    if (link) {
                        var i2 = links.indexOf(link);
                        links.splice(i2, 1);
                    }
                });
                var layoutData = {
                    nodes: nodes,
                    links: links,
                    groups: this.Group.getAll('data')
                };
                this.parent.layout(layoutData, 'click');
                this.parent.drawData(layoutData, 'init');
            }
        },
        position: function (nodes) {
            if (Array.isArray(nodes)) {
                nodes.forEach(function (d) {
                    d.data.x = d.x;
                    d.data.y = d.y;
                });
            }
        },
        appendIcon: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                var node = d3.select(this);
                var isUse = d.icon[0] === '#';
                var icon = node.append(isUse ? 'use' : 'text')
                    .attr('data-sort', 11)
                    .attr('class', 'icon');
                var title = node.append('title');

                if (isUse) {
                    icon
                        .attr('x', -d.iconSize / 2)
                        .attr('y', -d.iconSize / 2)
                        .attr('width', d.iconSize)
                        .attr('height', d.iconSize);
                } else {
                    icon
                        .attr('text-anchor', 'middle')
                        .attr('font-size', d.iconSize)
                        .attr('x', 0)
                        .attr('y', 0)
                        .attr('dy', '0.32em')
                        .attr('font-family', d.fontFamily);
                }

                Object.defineProperties(d, {
                    icon: {
                        get: that.objectGet('icon', d.icon),
                        set: function (value) {
                            value = eval('\'' + value + '\'');
                            if (this.getSet.icon !== value) {
                                if (isUse) icon.attr('xlink:href', value);
                                else {
									var text = value;
                                    if (that.options.nodeWidthResize === false) {
										text = that.textLineExceed({
											text: value,
											len: d.width - 10,
											line: 1,
											size: d.iconSize,
											exceed: '...',
										})[0];
                                    }

                                    if (text.indexOf('...') !== -1) title.text(value);
                                    else title.text('');
                                    icon.text(text);
                                }
                                this.getSet.icon = value;
                            }
                        }
                    },
                    iconColor: {
                        get: that.objectGet('iconColor', d.iconColor),
                        set: function (value) {
                            if (this.getSet.iconColor !== value) {
                                icon.attr('fill', value);
                                this.getSet.iconColor = value;
                            }
                        }
                    },
                    iconSize: {
                        get: that.objectGet('iconSize', d.iconSize),
                        set: function (value) {
                            if (this.getSet.iconSize !== value) {
                                if (isUse) {
                                    icon
                                        .attr('width', value)
                                        .attr('height', value);
                                } else {
                                    icon
                                        .attr('font-size', value);

                                    var text = d.icon;
                                    if (that.options.nodeWidthResize === false) {
										text = that.textLineExceed({
											text: d.icon,
											len: d.width - 10,
											line: 1,
											size: value,
											exceed: '...',
										})[0];
                                    }

                                    if (text.indexOf('...') !== -1) title.text(d.icon);
                                    else title.text('');
                                    icon.text(text);
                                }
                                this.getSet.iconSize = value;
                            }
                        }
                    },
                    iconWeight: {
                        get: that.objectGet('iconWeight', d.iconWeight),
                        set: function (value) {
                            if (this.getSet.iconWeight !== value) {
                                icon
                                    .attr('font-weight', value);
                                this.getSet.iconWeight = value;
                            }
                        }
                    },
                });

                // 变量初始化
                d.icon = d.icon;
                d.iconColor = d.iconColor;
                d.iconSize = d.iconSize;
                d.iconWeight = d.iconWeight;

                // 相关操作
                node.on('signWidth.icon', function () {
                    if (!d.sign.disable) {
                        icon
                            .attr('transform', `translate(${-(d.width / 2 - (1 - d.sign.ratio) * d.width / 2)},0)`);
                    } else {
                        icon
                            .attr('transform', null);
                    }
                });
            });
            return this;
        },
        appendName: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                var node = d3.select(this);

                var text = node.append('text')
                    .attr('data-sort', 1)
                    .attr('class', 'name')
                    .attr('font-size', d.nameSize)
                    .attr('y', d.size || d.height / 2);

                if (that.options.nodeNameAlign === 'left') {
                    text.attr('transform', `translate(${-d.width / 2},0)`)
                } else {
                    text.attr('text-anchor', 'middle').attr('x', 0)
                }

                var title = text.append('title')
                    .text(d.name);

                Object.defineProperties(d, {
                    nameColor: {
                        get: that.objectGet('nameColor', d.nameColor),
                        set: function (value) {
                            if (this.getSet.nameColor !== value) {
                                text.attr('fill', d.nameColor);
                                this.getSet.nameColor = value;
                            }
                        }
                    },
                    name: {
                        get: that.objectGet('name', d.name),
                        set: function (value) {
                            if (this.getSet.name !== value) {
                                text.selectAll('tspan').remove();
                                that.textLineExceed({
                                    text: value,
                                    len: that.options.nameWidth,
                                    line: 2,
                                    size: d.nameSize,
                                    exceed: '...',
                                }).forEach(function (str, i1) {
                                    text
                                        .append('tspan')
                                        .text(str)
                                        .attr('x', 0)
                                        .attr('dy', 1 + i1 * .3 + 'em');
                                });
                                title.text(value);
                                this.getSet.name = value;
                            }
                        }
                    }
                });
                d.nameColor = d.nameColor;
                d.name = d.name;

                text
                    .on('click', function () {
                        d3.event.stopPropagation();
                        that.options.nodeClick(d.port, 'name');
                    })
                    .call(d3.drag());

                if (d.url) {
                    text.style('text-decoration', 'underline')
                }

                // 相关数据
                d.margin[2] = text.node().getBBox().height;
            });
        },
        appendRemark: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                var node = d3.select(this);

                var text = node.append('text')
                    .attr('data-sort', 1)
                    .attr('class', 'remark')
                    .attr('font-size', d.remarkSize)
                    .attr('x', (-d.size || -d.width / 2))
                    .attr('y', (-d.size || -d.height / 2));

                var title = text.append('title')
                    .text(d.remark);

                if (that.options.nodeRemarkAlign === 'left') {
                    text.attr('transform', `translate(${-d.width / 2},0)`)
                } else {
                    text.attr('text-anchor', 'middle').attr('x', 0)
                }

                Object.defineProperties(d, {
                    remarkColor: {
                        get: that.objectGet('remarkColor', d.remarkColor),
                        set: function (value) {
                            if (this.getSet.remarkColor !== value) {
                                text.attr('fill', d.remarkColor);
                                this.getSet.remarkColor = value;
                            }
                        }
                    },
                    remark: {
                        get: that.objectGet('remark', d.remark),
                        set: function (value) {
                            if (this.getSet.remark !== value) {
                                text.selectAll('tspan').remove();
                                that.textLineExceed({
                                    text: d.remark,
                                    len: d.width || that.options.remarkWidth,
                                    line: 1,
                                    size: d.remarkSize,
                                    exceed: '...',
                                }).forEach(function (str, i1) {
                                    text
                                        .append('tspan')
                                        .text(str)
                                        .attr('x', 0)
                                        .attr('dy', -0.36 - i1 * .3 + 'em');
                                });
                                title.text(value);
                                this.getSet.remark = value;

                            }
                        }
                    }
                });
                d.remarkColor = d.remarkColor;
                d.remark = d.remark;

                text
                    .on('click', function () {
                        d3.event.stopPropagation();
                    })
                    .call(d3.drag());

                // 相关数据关联
                d.margin[0] = text.node().getBBox().height;
            });
        },
        appendSign: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                if (d.sign.disable) return;
                var node = d3.select(this);

                node.dispatch('signWidth');

                var signG = node.append('g').attr('data-sort', 12)
                    .attr('class', 'signG');
                var sign = d.sign;
                var offset = 1;
                var signWidth = sign.ratio * d.width - offset;
                var signHeight = d.height / 2;
                var textRuG = sign.hasOwnProperty('ru') && signG.append('g')
                    .attr('class', 'ru')
                    .attr('text-anchor', 'middle')
                    .attr('transform', `translate(${d.width / 2 - signWidth / 2 - 0.5},${-signHeight / 2})`)
                    .datum({
                        action: 'ru',
                        x: d.width / 2 - signWidth / 2 - 0.5,
                        y: -signHeight / 2,
                        width: signWidth,
                        height: signHeight,
                        fill: sign.ruBg
                    });

                textRuG.append('rect')
                    .attr('x', -signWidth / 2)
                    .attr('y', -signHeight / 2 + 0.5)
                    .attr('width', signWidth)
                    .attr('height', signHeight - 1.5)
                    .attr('fill', sign.ruBg)
                    .attr('rx', d.rx || 0)
                    .attr('ry', d.rx || 0)
                    .attr('stroke', sign.ruStroke);

                var ruNum = sign.ru > 99 ? '99' : sign.ru;
                var textRu = textRuG.append('text')
                    .attr('fill', sign.ruColor)
                    .attr('dy', '0.37em')
                    .attr('font-size', sign.ruSize)
                    .text(ruNum);
                if (sign.ru > 99) {
                    textRu.attr('font-size', sign.ruSize * 0.8);
                    textRu.append('tspan')
                        .text('+')
                        .attr('dx', '0')
                        .attr('dy', '-3');
                }

                var textRdG = sign.hasOwnProperty('rd') && signG.append('g')
                    .attr('class', 'rd')
                    .attr('text-anchor', 'middle')
                    .attr('transform', `translate(${d.width / 2 - signWidth / 2 - 0.5},${d.height / 4})`)
                    .datum({
                        action: 'rd',
                        x: d.width / 2 - signWidth / 2 - 0.5,
                        y: -signHeight / 2,
                        width: signWidth,
                        height: signHeight,
                        fill: sign.rdBg
                    });

                textRdG.append('rect')
                    .attr('x', -signWidth / 2)
                    .attr('y', -signHeight / 2 + 1)
                    .attr('width', signWidth)
                    .attr('height', signHeight - 1.5)
                    .attr('fill', sign.rdBg)
                    .attr('rx', d.rx || 0)
                    .attr('ry', d.rx || 0)
                    .attr('stroke', sign.rdStroke);

                var rdNum = sign.rd > 99 ? '99' : sign.rd;
                var textRd = textRdG.append('text')
                    .attr('fill', sign.rdColor)
                    .attr('dy', '0.37em')
                    .attr('font-size', sign.rdSize)
                    .text(rdNum);

                if (sign.rd > 99) {
                    textRd.attr('font-size', sign.rdSize * 0.8);
                    textRd.append('tspan')
                        .text('+')
                        .attr('dx', '0')
                        .attr('dy', '-3');
                }

                d3.selectAll([textRuG.node(), textRdG.node()])
                    .on('mouseover', function () {
                        d3.event.stopPropagation();
                        d3.event.preventDefault();
                    })
					.on('mouseout', function () {
						d3.event.stopPropagation();
						d3.event.preventDefault();
					})
                    .on('mouseenter', function (d1) {
                        d3.event.stopPropagation();
                        var dir = 'ru';
                        if (d3.select(this).classed('ru')) dir = 'ru';
                        else if (d3.select(this).classed('rd')) dir = 'rd';
                        that.options.nodeMouseover(d.port, dir);
                    })
                    .on('mouseleave', function () {
                        d3.event.stopPropagation();
                        var dir = 'ru';
                        if (d3.select(this).classed('ru')) dir = 'ru';
                        else if (d3.select(this).classed('rd')) dir = 'rd';
                        that.options.nodeMouseout(d.port, dir);
                    })
                    .on('click', function (d1) {
                        d3.event.stopPropagation();
                        var dir = 'ru';
                        if (d3.select(this).classed('ru')) dir = 'ru';
                        else if (d3.select(this).classed('rd')) dir = 'rd';
                        that.options.nodeClick(d.port, d1);

                        // if (d1.done) d3.select(this).dispatch('unselected');
                        // else d3.select(this).dispatch('selected');
                    })
                    .on('selected', function (d) {
                        d3.select(this).classed('done', true)
                            .select('rect')
                            .attr('fill', 'rgb(210, 215, 229)');
                        d.done = true;
                    })
                    .on('unselected', function (d) {
                        d3.select(this).classed('done', false)
                            .select('rect')
                            .attr('fill', d.fill);
                        d.done = false;
                    });
                node.on('highlight.sign', function () {
                    var detail = d3.event.detail;
                    if (detail.action === 'rd') {
                        textRdG.dispatch('selected');
                    } else if (detail.action === 'ru') {
                        textRuG.dispatch('selected')
                    }
                }).on('canHighlight.sign', function () {
                    var detail = d3.event.detail;
                    if (detail.action === 'rd') {
                        textRdG.dispatch('unselected');
                    } else if (detail.action === 'ru') {
                        textRuG.dispatch('unselected')
                    }
                })

            });
        },
        textLineExceed: function (opts) {
            var that = this;
            var exceedStr = opts.exceed ? opts.exceed : '';
            var exceedLen = opts.exceed ? this.calculateTextLen(opts.exceed, opts.size).width : 0;
            var strArr = [];
            var str = '';
            var strSplit = opts.text.split('');
            var totalLen = that.calculateTextLen(opts.text, opts.size).width;
            if ((opts.exceed === false) && (totalLen > opts.len * opts.line)) {

                for (var i = 0; i < opts.text.length; i++) {
                    str += strSplit[i];
                    if (that.calculateTextLen(str, opts.size).width > totalLen / opts.line) {
                        strArr.push(str);
                        str = '';
                    }
                    if (strArr.length === opts.line - 1) {
                        strArr.push(strSplit.slice(i + 1).join(''));
                        break;
                    }
                }
            } else {
                for (var i = 0; i < opts.text.length; i++) {
                    str += strSplit[i];
                    var len = that.calculateTextLen(str, opts.size).width;
                    var isExceed = (strArr.length === opts.line - 1) && (len + exceedLen > opts.len);
                    if ((len > opts.len) || isExceed) {
                        if (isExceed) {
                            strArr.push(str.substring(0, str.length - 1) + exceedStr);
                            break;
                        }
                        else strArr.push(str.substring(0, str.length - 1));
                        str = strSplit[i];
                    }
                    if (strArr.length === opts.line) break;
                    if (i === opts.text.length - 1) strArr.push(str);
                }
            }
            return strArr;
        },
        move: function (data) {
            var d = data.data;
            d.x += data.dx;
            d.y += data.dy;
            d.xyChange = d.x + '+' + d.y;
            // groups
            if (!Array.isArray(d.groups)) return;
            var groups = this.getVm('group').getAll('data');
            groups.forEach(function (group) {
                var gx = group.x, gy = group.y, gw = group.width, gh = group.height;
                var isIn = gx <= d.x - d.width / 2 && gy <= d.y - d.height / 2 && gw + gx >= d.x + d.width / 2 && gy + gh > d.y + d.height / 2;
                if (isIn) {
                    if (!d.groups.includes(group)) {
                        d.groups.push(group);
                        group.contain.push(d);
                    }
                } else {
                    if (d.groups.includes(group)) {
                        var ix = d.groups.indexOf(group);
                        if (ix !== -1) d.groups.splice(ix, 1);
                        var ix1 = group.contain.indexOf(d);
                        if (ix1 !== -1) group.contain.splice(ix1, 1);
                    }
                }
            })
        },
        setDrag: function (nodes) {
            var that = this;
            if (!that.options.nodeDragable) return;
            nodes.call(d3.drag()
                .on('start', function (d) {
                    d3.select(this).dispatch('dragStart');
                    that.hook('dragStart', d);
                })
                .on('drag', function (d) {
                    if (!d.status.selected && that.options.nodeRadio && that.options.nodeDragCancleSelected) {
                        // 取消所有节点的选中状态
                        that.cancelSelected();
                    }
                    if (that.options.nodeGroupsPosition === 'static') {
                        d.groups.forEach(function (group) {
                            that.Group.move({
                                data: group,
                                dx: d3.event.dx,
                                dy: d3.event.dy
                            });
                        });
                    } else {
                        that.move({
                            data: d,
                            dx: d3.event.dx,
                            dy: d3.event.dy
                        });
                    }
                    that.selectionDrag(d3.event.dx, d3.event.dy, d);
                    d3.select(this).dispatch('dragging');
                })
                .on('end', function (d) {
                    d3.select(this).dispatch('dragEnd');
                }));
        },
        appendMarker: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                var node = d3.select(this);
                var markerG = node.append('g').attr('class', 'markerG').attr('data-sort', 2);

                var marker = markerG.append('path')
                    .attr('d', function () {
                        return d3.arc()
                            .innerRadius(d.size + 3)
                            .outerRadius(d.size + 5)
                            .startAngle(-Math.PI / 4)
                            .endAngle(Math.PI / 4)();
                    })
                    .attr('fill', d.marker.color)
                    .attr('stroke', d.marker.color)
                    .attr('transform', `translate(0,0)`);
                markerG.append('title').text(function (d1) {
                    return d1.title;
                });

                Object.defineProperties(d, {
                    marker: {
                        get: that.objectGet('marker', d.marker),
                        set: function (value) {
                            if (this.getSet.marker !== value) {
                                if (value) {
                                    markerG.style('display', null);
                                } else markerG.style('display', 'none');
                                this.getSet.marker = value;
                            }
                        }
                    },
                    markerColor: {
                        get: that.objectGet('markerColor', d.markerColor),
                        set: function (value) {
                            if (this.getSet.markerColor !== value) {
                                marker.attr('fill', value)
                                    .attr('stroke', value);
                                this.getSet.markerColor = value;
                            }
                        }
                    },
                });

                d.marker = d.marker;
                d.markerColor = d.markerColor;

            });
        },
        getNodesByClass: function (className) {
            return this.container.selectAll('.' + className);
        },
        getAll: function (type, condition) {
            var res = this.nodes;
            if (type === 'data') res = d3.selectAll(res).data();
            else if (type === 'selection') res = d3.selectAll(res);
            else if (type === 'condition') res = d3.selectAll(res).filter(function (d) {
                return d.status && d.status[condition];
            });
            return res;
        },
        getDatas: function () {
            return this.el.selectAll('g.' + this.options.nodeClass).data();
        },
        getNodeById: function (id) {
            return d3.selectAll(this.nodes).filter(function (d) {
                return d.uuid === id;
            });
        },
        getNodeDataById: function (id) {
            return this.nodesData.find(function (d) {
                return d.uuid === id;
            });
        },
        brushIn: function (section) { // section: [[x0, y0], [x1, y1]]
            var that = this;
            if (!section || !Array.isArray(section)) return [];
            return d3.selectAll(this.nodes).filter(function (d) {
                return section[0][0] <= d.x - that.options.size && section[1][0] >= d.x + that.options.size && section[0][1] <= d.y - that.options.size && section[1][1] >= d.y + that.options.size;
            }).nodes();
        },
        elementChildrenSort: function (node) {
            if (!node) return;
            if (typeof node.node === 'function') node = node.node();
            var childrenDom = Array.prototype.slice.call(node.children);
            childrenDom.sort(function (a, b) {
                return a.getAttribute('data-sort') - b.getAttribute('data-sort');
            });
            d3.selectAll(childrenDom).order();
        },
    });

})));