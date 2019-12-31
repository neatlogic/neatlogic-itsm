(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {})));
}(this, (function (exports) {
    'use strict';
    var opts = {
        type: 'group',
        shape: 'rect',
        className: 'groupsG',
        nodeClass: 'groupG',
        width: 26,
        height: 80,
        selectStroke: '#336eff',
        selectStrokeWidth: 1,
        selectStrokedasharray: '4,3',
        anchorNum: 8,
        anchorSize: 10,
        anchorHoverSize: 10 + 5,
        marker: 20,
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

            // 添加模块
            this.Node = this.getVm('node');
            this.Link = this.getVm('link');
            this.Anchor = this.getVm('anchor');

            this.Link && this.Link.el.lower();
        },
        dataFn: function (data, reuuid) {
            var that = this;
            if (!data) {
                this.consoleLog('group data为空！', 'warn');
                return;
            }
            var anchorSize = that.options.anchorSize || 6;
            var anchorHoverSize = that.options.anchorHoverSize || 9;

            return data.map(function (d) { // 节点默认以矩形模型处理
                var db =  {
                    el: '',
                    data: d,
                    uuid: d.uuid && d.uuid.toString() || (d.uuid = that.guid(that.options.uuidmode)),
                    reuuid: reuuid || 'reuuid',
                    type: d.type,
                    name: d.name,
                    icon: d.icon,
                    x: d.x,
                    y: d.y,
                    rx: d.rx || 0,
                    ry: d.ry || 0,
                    cx: d.width / 2,
                    cy: d.height / 2,
                    width: d.width || 50,
                    height: d.height || 50,
                    shape: that.options.shape,
                    contain: d.contain || [],
                    fill: d.fill || 'transparent',
                    stroke: d.stroke || that.options.groupStroke || '#336EFF',
                    strokeWidth: d.strokeWidth || that.options.groupStrokeWidth || 1.0000002,
                    strokedasharray: d.strokedasharray || '0 0',
                    iconColor: d.iconColor || 'white',
                    iconSize: 13,
                    fontFamily: d.fontFamily || 'ts',
                    nameColor: d.nameColor || that.options.groupNameColor || '#336EFF',
                    nameSize: d.nameSize || 12,
                    marker: d.marker || false,
                    markerColor: d.markerColor || '#b2d235',
                    selectFill: that.options.selectFill,
                    selectStroke: that.options.groupSelectedStroke || that.options.selectStroke,
                    selectStrokeWidth: that.options.selectStrokeWidth,
                    selectStrokedasharray: that.options.selectStrokedasharray,
                    getSet: {}, // 专门给get,set提供数值存放的位置
                    status: {},
                    contextMenu: d.contextMenu || [],
                    info: {}, // 添加节点信息展示内容

                };

                // 包含节点
                db.contain = db.contain.map(function (d) {
                    var node = that.Node.getNodeDataById(d);
                    node.groups.push(db);
                    return node;
                });

                // 添加节点信息展示字典
                Object.assign(db.info, { // key为db中的属性，值为展示的detail
                    type: 'group',
                    label: '组',
                    name: {
                        label: '名称',
                        value: db.name,
                        editable: true,
                        type: 'input'
                    },
                    widthHeight: {
                        label: '尺寸',
                        value: db.width + "-" + db.height,
                        editable: true,
                        type: 'mulInput',
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
        },
        position: function (data) {
            var offset = 20;
            if (Array.isArray(data)) {
                data.forEach(function (d) {
                    var x0 = d3.min(d.data.contain, function (a) {
                        return a.x - a.width / 2 - offset;
                    });
                    var y0 = d3.min(d.data.contain, function (a) {
                        return a.y - a.height / 2 - offset;
                    });
                    var x1 = d3.max(d.data.contain, function (a) {
                        return a.x + a.width / 2 + offset;
                    });
                    var y1 = d3.max(d.data.contain, function (a) {
                        return a.y + a.height / 2 + offset;
                    });

                    d.data.x = x0;
                    d.data.y = y0;
                    d.data.width = x1 - x0;
                    d.data.height = y1 - y0;

                    // d.data.x = d.x - d.width / 2;
                    // d.data.y = d.y - d.height / 2;
                    // d.data.width = d.width;
                    // d.data.height = d.height;
                })
                // 处理包含中包含关系
                data.forEach(function (d) {
                    var contain = [];
                    data.forEach(function (a) {
                        if (d === a) return;
                        var is = a.contain.every(function (b) {
                            return d.contain.some(function (c) {
                                return c === b;
                            })
                        });
                        if (is) contain.push(a);
                    });
                    var x0 = d3.min(contain, function (a) {
                        return a.data.x - offset;
                    });
                    var y0 = d3.min(contain, function (a) {
                        return a.data.y - offset;
                    });
                    var x1 = d3.max(contain, function (a) {
                        return a.data.x + a.data.width + offset;
                    });
                    var y1 = d3.max(contain, function (a) {
                        return a.data.y + a.data.height + offset;
                    });
                    if (d.data.x > x0) d.data.x = x0;
                    if (d.data.y > y0) d.data.y = y0;
                    if ((d.data.x + d.data.width) < x1) d.data.width = x1 - d.data.x;
                    if ((d.data.y + d.data.height) < y1) d.data.height = y1 - d.data.y;
                })
            }
        },
        toJson: function () {
            var datas = this.getAll('data');
            var data = datas.map(function (d) {
                var o = Object.assign({},d.data);
				o.width = d.width;
				o.height = d.height;
                o.x = d.x;
                o.y = d.y;
                o.contain.splice(0);
                d.contain.forEach(function (d1) {
                    o.contain.push(d1.uuid);
                });
                return o;
            });
            return {
                groups: data
            };
        },
        draw: function (data, action) {
            var that = this;
            var typeData = {};

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
                .each(function (d) {
                    d.el = this;
                });
            // .attr('transform', function (d) {
            //     return `translate(${d.x},${d.y})`;
            // });

            // exit
            var nodesExit = nodes.exit();
            if (action === 'init') nodesExit.dispatch('remove');

            // 存储节点
            this.nodes.add(nodesEnter.nodes());
            // update
            nodes = nodes.data(updateData, function (d) {
                return d.uuid;
            });
            this.update(nodes);

            // 添加drag
            this.setDrag(nodesEnter);

            // 添加group
            this.appendShape(nodesEnter);

            // 添加name
            this.appendName(nodesEnter);

            // 添加锚点
            if (this.options.groupAnchorable) {
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
                });
            }

            // // 添加contextMenu
            // this.appendCxtMenu(nodesEnter);

            // 添加marker
            this.appendMarker(nodesEnter);

			// 添加remind
			this.appendRemind(nodesEnter);

            // 置顶
            nodesEnter.on('raise',function (d) {
                d3.select(this).raise();
                d.contain.forEach(function (d1) {
                    d3.select(d1.el).raise();
                });
            });
            nodesEnter.dispatch('raise');


            // 添加节点remove操作
            nodesEnter
                .on('remove.node', function (d) {
                    var node = d3.select(this);
                    var links = that.Link.getLinkByNode(d);
                    node
                        .attr('opacity', 1)
                        .transition('remove')
                        .attr('opacity', 0)
                        .on('end', function () {
                            node.remove();
                        });
					that.nodesData.remove(d);
					that.nodes.remove(d);
                    links.dispatch('remove');
                    // remove contain nodes
					var nodes = d.contain.map(function (d1) {
						return d1.el;
					});
					nodes = d3.selectAll(nodes);
					nodes.size() && nodes.dispatch('remove');
                    // cb
                    if (typeof that.options.groupRemove === 'function') that.options.groupRemove(d.port);
                })
                .on('mouseover', function () {
                    var node = d3.select(this);
                    node.dispatch('raise');
                })
                .on('dblclick', function (d) {
                    d3.event.stopPropagation();
                    if (typeof d.data.ondblclick === "function") {
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

            return this;

        },
        update: function (node) {
            var that = this;
            node.each(function (d) {
                Object.keys(d.data).forEach(function (key) {
                    if (d.hasOwnProperty(key) && typeof d[key] !== 'object') {
                        d[key] = d.data[key];
                    }
                });
                if (d.hasOwnProperty('width') || d.hasOwnProperty('height')) {
                    d.whChange = d.width + '-' + d.height;
                }
                if (d.hasOwnProperty('x') || d.hasOwnProperty('y')) {
                    d.xyChange = d.x + '+' + d.y;
                }
            });
        },
        render: function (data) {
            if (!data || !Array.isArray(data) || !data.length) return false;
            var vm = this.getVm(opts.type);
            // 数据处理
            data = vm.dataFn(data);
            // 添加节点（分配增删改）
            vm.draw(data);
        },
        move: function (data) {
            var that = this;
            var d = data.data;
            d.x += data.dx;
            d.y += data.dy;
            d.xyChange = d.x + '+' + d.y;
            // contain
            if (!Array.isArray(d.contain)) return;
            d.contain.forEach(function (node) {
                if (node.status.selected) return;
                node.x += data.dx;
                node.y += data.dy;
                node.xyChange = node.x + '+' + node.y;
            })
        },
        appendShape: function (nodes) {
            var that = this;
            var minWidth = 30, minHeight = 30;
            var handleSize = 10;
            var brushAssist = d3.brush().handleSize([handleSize]);
            var dx = 0, dy = 0, selectionArr;
            var offset = that.options.hasOwnProperty('groupSelectedOffset') ? that.options.groupSelectedOffset : 4;

            nodes.each(function (d) {
                var group = d3.select(this);
                var brush = d3.brush()
                    .extent([[-99999, -99999], [99999, 99999]])
                    .handleSize([handleSize])
                    .filter(function () {
                        if (!that.options.groupBrushable) return false;
                        return true;
                    })
                    .on('start', function (d) {
                        selectionArr = d3.event.selection;
                        dx = 0;
                        dy = 0;
                        group.dispatch('unselect');
                        group.dispatch('brushStart');
                    })
                    .on('brush', function () {
                        var selection = d3.event.selection;
                        // 处理框最小 width 和 height
                        var rw = selection[1][0] - selection[0][0];
                        var rh = selection[1][1] - selection[0][1];
                        var isMove = rw < minWidth || rh < minHeight;
                        rw = rw < minWidth ? minWidth : rw;
                        rh = rh < minHeight ? minHeight : rh;

                        // group的中心点的位置
                        d.x += (selection[0][0] - selectionArr[0][0]);
                        d.y += (selection[0][1] - selectionArr[0][1]);
                        selectionArr = selection;
                        d.cx = rw / 2;
                        d.cy = rh / 2;
                        d.width = rw;
                        d.height = rh;

                        // 更新连线
                        that.Link.getLinkByNode(d).dispatch('updatePath');
                        // mask
                        mark.dispatch('resize', {
                            detail: {
                                x: selection[0][0] - 10,
                                y: selection[0][1] - 10,
                                width: d.width + 20,
                                height: d.height + 20
                            }
                        });

                        // name
                        d.namePosition = {
                            x: (selection[1][0] + selection[0][0]) / 2,
                            y: (selection[1][1] + selection[0][1]) / 2 + d.height / 2
                        };

                        // 四个角 resize
                        corHandle.dispatch('resize');
                        // marker
                        d.markerPosition = {x: selection[0][0], y: selection[0][1]};
                        // 执行该元素自定义事件
                        group.dispatch('brushing');
                    })
                    .on('end', function () {
                        var selection = d3.event.selection;
                        // 改变group的起点位置
                        d.xyChange = d.x + '+' + d.y;
                        // 防止selectionRect过小
                        var w = selection[1][0] - selection[0][0];
                        var h = selection[1][1] - selection[0][1];
                        if (w < 30) w = 30;
                        if (h < 30) h = 30;
                        d.width = w;
                        d.height = h;
                        d.whChange = w + '-' + h;
                        // mask
                        mark.dispatch('resize', {
                            detail: {
                                x: -10,
                                y: -10
                            }
                        });
                        // 四个角 resize
                        corHandle.dispatch('resize');
                        // marker
                        d.markerPosition = {x: 0, y: 0};

                        group.dispatch('brushEnd');
                    });
                // 添加 mask
                var mark = group.append('mask')
                // .attr('maskUnits', 'userSpaceOnUse') // 必须要加
                    .attr('id', 'group' + that.options.id + '-' + d.uuid)
                    .on('resize.white', function () {
                        if (d3.event.detail) {
                            Object.keys(d3.event.detail).forEach(function (key) {
                                maskWhite.attr(key, d3.event.detail[key]);
                            });
                        }
                    });
                var maskWhite = mark.append('rect')
                    .attr('fill', 'white');
                var maskBlack = mark.append('rect')
                    .attr('x', 0)
                    .attr('y', 0)
                    .attr('width', 0)
                    .attr('height', 0)
                    .attr('fill', 'black');

                mark.dispatch('resize', {
                    detail: {
                        x: -10,
                        y: -10,
                        width: d.width + 20,
                        height: d.height + 20
                    }
                });
                // 添加group
                group.call(brush)
                    .selectAll('.overlay').remove();
                // rect
                var selectionRect = group.selectAll('.selection')
                    .attr('rx', d.rx)
                    .attr('ry', d.rx)
					.attr('stroke-linecap', 'round')
					.attr('stroke-linejoin', 'round')
                    .attr('mask', `url(#group${that.options.id + '-' + d.uuid})`);

                // handle
                if (!that.options.groupHandle) {
                    group.selectAll('.handle--nw,.handle--ne,.handle--se,.handle--sw').remove();
                }
                var handleOffset = 8;
                var corHandle = group.selectAll('.handle--nw,.handle--ne,.handle--se,.handle--sw').raise()
                    .attr('fill', d.stroke)
                    .attr('stroke', d.stroke)
                    .attr('transform', function (d1) {
                        if (d1.type === 'nw') {
                            d1.dx = handleOffset;
                            d1.dy = handleOffset;
                        } else if (d1.type === 'ne') {
                            d1.dx = -handleOffset;
                            d1.dy = handleOffset;
                        } else if (d1.type === 'se') {
                            d1.dx = -handleOffset;
                            d1.dy = -handleOffset;
                        } else if (d1.type === 'sw') {
                            d1.dx = handleOffset;
                            d1.dy = -handleOffset;
                        }
                        return `translate(${d1.dx},${d1.dy})`;
                    })
                    .attr('clip-path', function (d1) {
                        return `url(#group-corHandleClipPath-${d1.type + that.options.id + '-' + d.uuid})`;
                    })
                    .on('resize', function (d1) {
                        var corner = d3.select(this);
                        corClipPath.filter(function (d2) {
                            return d2.type === d1.type;
                        }).attr('transform', `translate(${corner.attr('x')},${corner.attr('y')})`)
                    });

                var handleWidth = 2;
                var handleRadius = 3.6;
                var corClipPath = group.selectAll('clipPath.corHandle').data(corHandle.data()).enter().append('clipPath').lower()
                    .attr('id', function (d1) {
                        return 'group-corHandleClipPath-' + d1.type + that.options.id + '-' + d.uuid;
                    })
                    .append('path')
                    .attr('fill', 'none')
                    .attr('stroke', 'red')
                    .attr('d', function (d1) {
                        var pathStr = 1;
                        var path = d3.path();
                        if (d1.type === 'nw') {
                            path.moveTo(handleSize, 0)
                            path.arcTo(0,0,0,handleSize,handleRadius)
                            path.lineTo(0, handleSize)
                            path.lineTo(handleWidth, handleSize)
                            path.arcTo(handleWidth,handleWidth,handleSize,handleWidth,handleRadius - 1)
                            path.lineTo(handleSize, handleWidth)
                            path.closePath()
                            return path.toString();
                            // pathStr = `M${handleSize},0L0,0L0,${handleSize}L${handleWidth},${handleSize}L${handleWidth},${handleWidth}L${handleSize},${handleWidth}Z`;
                        } else if (d1.type === 'ne') {
                            path.moveTo(0, 0)
                            path.arcTo(handleSize, 0, handleSize, handleSize, handleRadius)
                            path.lineTo(handleSize, handleSize)
                            path.lineTo(handleSize-handleWidth, handleSize)
                            path.arcTo(handleSize-handleWidth, handleWidth, 0, handleWidth, handleRadius - 1)
                            path.lineTo(0, handleWidth)
                            path.closePath()
                            return path.toString();
                            // pathStr = `M0,0L${handleSize},0L${handleSize},${handleSize}L${handleSize-handleWidth},${handleSize}L${handleSize-handleWidth},${handleWidth}L0,${handleWidth}Z`;
                        } else if (d1.type === 'se') {
                            path.moveTo(handleSize, 0);
                            path.arcTo(handleSize, handleSize, 0, handleSize, handleRadius)
                            path.lineTo(0, handleSize)
                            path.lineTo(0, handleSize-handleWidth)
                            path.arcTo(handleSize-handleWidth, handleSize-handleWidth, handleSize-handleWidth, 0, handleRadius - 1)
                            path.lineTo(handleSize-handleWidth, 0)
                            path.closePath()
                            return path.toString();
                            // pathStr = `M${handleSize},0L${handleSize},${handleSize}L0,${handleSize}L0,${handleSize-handleWidth}L${handleSize-handleWidth},${handleSize-handleWidth}L${handleSize-handleWidth},0Z`;
                        } else if (d1.type === 'sw') {
                            path.moveTo(0, 0);
                            path.arcTo(0, handleSize, handleSize, handleSize, handleRadius)
                            path.lineTo(handleSize, handleSize)
                            path.lineTo(handleSize, handleSize-handleWidth)
                            path.arcTo(handleWidth, handleSize-handleWidth,handleWidth, 0, handleRadius - 1)
                            path.lineTo(handleWidth, 0)
                            path.closePath()
                            return path.toString();
                            // pathStr = `M0,0L0,${handleSize}L${handleSize},${handleSize}L${handleSize},${handleSize-handleWidth}L${handleWidth},${handleSize-handleWidth}L${handleWidth},0Z`;
                        }
                    });

                // 添加select
                var selectRect = group.append('rect').lower()
                    .attr('class', 'select')
                    .attr('mask', `url(#group${that.options.id + '-' + d.uuid})`)
                    .attr('fill', 'transparent')
                    .attr('x', -offset)
                    .attr('y', -offset)
                    .attr('rx', d.rx || 0)
                    .attr('ry', d.rx || 0)
                    .attr('width', 0)
                    .attr('height', 0)
                    .attr('stroke', d.selectStroke)
                    .attr('stroke-width', d.selectStrokeWidth || 6)
                    .attr('stroke-dasharray','0' || d.selectStrokedasharray)
                    .style('display', 'none');

                if (that.options.groupSelectedShadow) {
                    selectRect.attr('filter', 'url(#group-shadow-' + that.options.id);
                }

                // 操作
                group
                    .on('click', function () {
                        d3.event.stopPropagation();
                        // if (!d3.event.ctrlKey && that.options.groupRadio) { // 多选
                        //     that.getRoom().getAll('condition', 'selected').filter(function (d1) {
                        //         return d1 !== d;
                        //     }).dispatch('unselect');
                        // }
                        if (d.status.selected) group.dispatch('unselect', {detail: {ctrlKey: d3.event.ctrlKey}});
                        else group.dispatch('selected', {detail: {ctrlKey: d3.event.ctrlKey}});

                        that.hook('click', d);
                    })
                    .on('selected', function () {
                        var detail = d3.event.detail;
                        if (d.status.selected) return false; // 防止统一状态反复触发，提高性能
                        // 取消其他选中节点
                        if ((!detail || !detail.ctrlKey) && that.options.groupRadio) { // 多选
                            that.getRoom().getAll('condition', 'selected').filter(function (d1) {
                                return d1 !== d;
                            }).dispatch('unselect');
                        }
                        d.status.selected = true;
                        selectRect.style('display', null);
                        typeof that.options.groupSelected === 'function' && that.options.groupSelected(d.port);
                    })
                    .on('unselect', function () {
                        if (!d.status.selected) return false; // 防止统一状态反复触发，提高性能
                        d.status.selected = false;
                        selectRect. style('display', 'none');
                        typeof that.options.groupUnselected === 'function' && that.options.groupUnselected(d.port);
                    })
                    .on('nameMask', function () {
                        var detail = d3.event.detail;
                        if (detail) {
                            Object.keys(detail).forEach(function (key) {
                                maskBlack.attr(key, detail[key]);
                            })
                        }
                    })

                // mv
                Object.defineProperties(d, {
                    fill: {
                        get: that.objectGet('fill', d.fill),
                        set: function (value) {
                            if (this.getSet.fill !== value) {
                                selectionRect.attr('fill', value);
                                this.getSet.fill = value;
                            }
                        }
                    },
                    stroke: {
                        get: that.objectGet('stroke', d.stroke),
                        set: function (value) {
                            if (this.getSet.stroke !== value) {
                                selectionRect.attr('stroke', value);
                                this.getSet.stroke = value;
                            }
                        }
                    },
                    strokeWidth: {
                        get: that.objectGet('strokeWidth', d.strokeWidth),
                        set: function (value) {
                            if (this.getSet.strokeWidth !== value) {
                                selectionRect.attr('stroke-width', value);
                                this.getSet.strokeWidth = value;
                            }
                        }
                    },
                    strokedasharray: {
                        get: that.objectGet('strokedasharray', d.strokedasharray),
                        set: function (val) {
                            if (this.getSet.strokedasharray !== val) {
                                selectionRect.attr('stroke-dasharray', val);
                                this.getSet.strokedasharray = val;
                            }
                        }
                    },
                    whChange: {
                        get: that.objectGet('whChange', 0),
                        set: function (val) {
                            if (this.getSet.whChange !== val) {
                                // mask
                                mark.dispatch('resize', {
                                    detail: {
                                        width: d.width + 20,
                                        height: d.height + 20
                                    }
                                });
                                // group
                                brushAssist.move(group, [[0, 0], [d.width, d.height]]);
                                // name
                                d.namePosition = {
                                    x: d.width / 2,
                                    y: d.height
                                };
                                // 锚点resize
                                group.dispatch('anchorResize');
                                // select
                                selectRect
                                    .attr('width', d.width + offset * 2)
                                    .attr('height', d.height + offset * 2);

                                this.getSet.whChange = val;
                            }
                        }
                    },
                    xyChange: {
                        get: that.objectGet('xyChange', 0),
                        set: function (val) {
                            if (this.getSet.xyChange !== val) {
                                group.attr('transform', `translate(${d.x},${d.y})`);
                                this.getSet.xyChange = val;
                                that.Link.getLinkByNode(d).dispatch('updatePath');
                            }
                        }
                    },

                });

                // 数据初始化渲染
                d.fill = d.fill;
                d.stroke = d.stroke;
                d.strokeWidth = d.strokeWidth;
                d.strokedasharray = d.strokedasharray;
                d.whChange = d.width + '-' + d.height; // 初始化节点的width和height
                d.xyChange = d.width + '+' + d.height; // 初始化节点的位置
                corHandle.dispatch('resize');
            });

            return this;
        },
        appendIcon: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                var node = d3.select(this);
                var icon = node.append('text')
                    .attr('class', 'icon')
                    .attr('text-anchor', 'middle')
                    .attr('font-size', d.iconSize)
                    .attr('dominant-baseline', 'central')
                    .attr('font-family', d.fontFamily)
                    .text(d.icon)
                    .attr('fill', d.iconColor);

                Object.defineProperties(d, {
                    icon: {
                        get: that.objectGet('icon', d.icon),
                        set: function (value) {
                            if (d.icon !== value) {
                                icon.text(value);
                                this.getSet.icon = value;
                            }
                        }
                    },
                    iconColor: {
                        get: that.objectGet('iconColor', d.iconColor),
                        set: function (value) {
                            if (d.iconColor !== value) {
                                icon.attr('fill', d.iconColor);
                                this.getSet.iconColor = value;
                            }
                        }
                    }
                });
            });
            return this;
        },
        appendName: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                var node = d3.select(this);

                var name = node.append('text')
                    .attr('class', 'name')
                    .attr('font-size', d.nameSize)
                    .attr('text-anchor', 'middle')
                    .attr('dy', '0.32em');

                Object.defineProperties(d, {
                    nameColor: {
                        get: that.objectGet('nameColor', d.nameColor),
                        set: function (value) {
                            if (this.getSet.nameColor !== value) {
                                name.attr('fill', d.nameColor);
                                this.getSet.nameColor = value;
                            }
                        }
                    },
                    name: {
                        get: that.objectGet('name', d.name),
                        set: function (value) {
                            if (this.getSet.name !== value) {
                                name.text(value);
                                // 添加mask
                                var box = name.node().getBBox();
                                var w = box.width ? box.width + 8 : 0;
                                var h = box.height ? box.height + 6 : 0;

                                node.dispatch('nameMask', {
                                    detail: {
                                        width: w,
                                        height: h,
                                        transform: `translate(${-w / 2},${-h / 2})`
                                    }
                                });

                                this.getSet.name = value;
                            }
                        }

                    },
                    namePosition: {
                        get: that.objectGet('namePosition', {
                            x: d.width / 2,
                            y: d.height
                        }),
                        set: function (value) { // 文字中心点的坐标
                            if (value && typeof value === 'object') {
                                if (!this.getSet.namePosition) this.getSet.namePosition = {};
                                if (this.getSet.namePosition.x !== value.x || this.getSet.namePosition.y !== value.y) {
                                    name.attr('x', value.x)
                                        .attr('y', value.y);

                                    node.dispatch('nameMask', {
                                        detail: {
                                            x: value.x,
                                            y: value.y
                                        }
                                    });
                                    this.getSet.namePosition.x === value.x;
                                    this.getSet.namePosition.y === value.y;
                                }
                            }
                        }
                    },
                });
                // 数据初始化
                d.nameColor = d.nameColor;
                d.name = d.name;
                d.namePosition = {
                    x: d.width / 2,
                    y: d.height
                };
            });
            return this;
        },
        appendMarker: function (nodes) {
            var that = this;
            nodes.each(function (d) {
                var node = d3.select(this);
                var markerG = node.append('g').attr('class', 'markerG').lower();
                markerG.attr('clip-path', `url(#marker-${that.options.id})`);

                var marker = markerG.append('path')
                    .attr('d', `M0,0L${that.options.marker},0L0,${that.options.marker}Z`)
                    .attr('transform', `translate(0,0)`);
                markerG.append('title').text(function (d1) {
                    return d1.title;
                });

                Object.defineProperties(d, {
                    markerPosition: {
                        get: that.objectGet('markerPosition', {x: 0, y:0}),
                        set: function (value) {
                            markerG.attr('transform', `translate(${value.x},${value.y})`)
                        }
                    },
                    marker: {
                        get: that.objectGet('marker', d.marker),
                        set: function (value) {
                            if (this.getSet.marker !== value) {
                                if (value) {
                                    markerG.style('display', null)
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
                                    .attr('stroke', value)
                                this.getSet.markerColor = value;
                            }
                        }
                    },
                })

                d.marker = d.marker;
                d.markerColor = d.markerColor;
            });
        },
		appendRemind: function (nodes) {
			var that = this;
			nodes.each(function (d) {
				var node = d3.select(this);
				var styleObj = {
					normal: {
						stroke: d.stroke,
						strokeOpacity: d.strokeOpacity || 1,
						fill: d.fill,
						fillOpacity: d.fillOpacity || 1,
					},
					warn: {
						stroke: '#F7B538',
						strokeOpacity: d.strokeOpacity || 1,
						strokeWidth: 6,
						fill: d.fill,
						fillOpacity: d.fillOpacity || 1,
					}
				};
				node.on('remind', function () {
					var detail = d3.event.detail;
					var style = styleObj.hasOwnProperty(detail.name) ? styleObj[detail.name] : styleObj.normal;
					Object.keys(style).forEach(function (k) {
						if (d.hasOwnProperty(k)) {
							d[k] = style[k];
						}
					})
				});
            })




        },
        setDrag: function (nodes) {
            var that = this;
            if (!that.options.groupDragable) return;
            nodes.call(d3.drag()
                .filter(function () {
                    if (d3.select(d3.event.target).classed('handle')) return false;
                    return true;
                })
                .on('start', function (d) {
                    var node = d3.select(this);
                })
                .on('drag', function (d) {
                    var action = '';
                    if (!d.status.selected) {
                        // 取消所有节点的选中状态
                        that.parent.getAll('condition', 'selected').dispatch('unselect');
                    } else action = 'selection';
                    that.move({
                        data: d,
                        dx: d3.event.dx,
                        dy: d3.event.dy
                    });
                    that.selectionDrag(d3.event.dx, d3.event.dy, d);
                })
                .on('end', function (d) {

                }));
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
        brushIn: function (section) { // section: [[x0, y0], [x1, y1]]
            if (!section || !Array.isArray(section)) return [];
            if (Array.isArray(this.children)) {
                return this.children.reduce(function (a, b) {
                    return a.concat(b.brushNodes ? b.brushNodes(section) : []);
                }, []);
            } else {
                return this.el.selectAll('g.' + this.options.nodeClass).filter(function (d) {
                    return section[0][0] <= d.x && section[1][0] >= d.x + d.width && section[0][1] <= d.y && section[1][1] >= d.y + d.height;
                }).nodes();
            }
        },
        brushNodes: function (section) {
            var that = this;
            if (!section || !Array.isArray(section)) return [];
            return this.el.filter(function (d) {
                return section[0][0] <= d.x - d.width && section[1][0] >= d.x + d.width && section[0][1] <= d.y - d.height && section[1][1] >= d.y + d.height;
            }).nodes();
        },
    });

})));