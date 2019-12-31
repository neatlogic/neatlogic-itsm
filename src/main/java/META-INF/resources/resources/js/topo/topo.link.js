(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {})));
}(this, (function (exports) {
    'use strict';
    var opts = {
        type: 'link',
        className: 'linksG',
        linkClass: 'linkG',
        strokeHoverWidth: 5,
        hierTransAble: true,
    };
    var Link = exports[opts.type] = exports.Base.extend();
    Link.inherit = 'Base';
    Link.inject({
        init: function (vm) {
            this
                .inherit(vm)
                .subset(vm)
                .instanceInject({
                    options: Object.assign({}, vm.options, opts),
                    container: this.getVm('svg').zoomG,
                    linkStart: [],
                    linkEnd: []
                });
            this.el = this.container.append('g').attr('class', this.options.className).lower();
            // 存放画布中的node节点
            this.nodes = Array.isArray(this.nodes) ? this.nodes : this.gatherNodes();

            // 加载模块
            this.Svg = this.getVm('svg');
            this.Node = this.getVm('node');
            this.Group = this.getVm('group');
            this.Util = this.getVm('util');
            this.intersect = dagreD3.intersect;

            // 添加一个path计算totalLength
            this.totalLengthPath = this.Svg.defs.append('path');
        },
        dataFn: function (data, reuuid) {
            var that = this;
            if (!data) {
                this.consoleLog('link data为空！', 'warn');
                return;
            }

            var nodeData = this.Node.nodesData.concat(this.Group.nodesData);

            return data.map(function (d) {
                if (!d.source || !d.target) {
                    that.consoleLog('有无效连线数据！', 'log');
                    return false;
                }
                var sourceArr = d.source.toString().split('+');
                var targetArr = d.target.toString().split('+');
                var sourceNodeData = nodeData.find(function (d) {
                    return d.uuid === sourceArr[0];
                });
                var targetNodeData = nodeData.find(function (d) {
                    return d.uuid === targetArr[0];
                });

                if (!sourceNodeData || !targetNodeData) {
					that.consoleLog('没有source node或者target node！', 'log');
                    return false;
				}

				var contextMenuData = Array.isArray(that.options.linkContextMenu) && that.options.linkContextMenu.map(function (item) {
				    return Object.assign({}, item);
                })

                var db =  {
                    el: '',
                    data: d,
                    uuid: d.uuid && d.uuid.toString() || (d.uuid = that.guid(that.options.uuidmode)),
                    reuuid: reuuid || 'reuuid',
                    source: sourceNodeData,
                    target: targetNodeData,
                    sAnchor: d.sAnchor,
                    tAnchor: d.tAnchor,
                    type: d.type,
                    mode: d.mode || that.options.linkDefaultMode || 'curve',
                    relation: d.relation || [],
                    points: d.points || [],
                    name: d.name || '',
                    textAnchor: d.textAnchor || 'middle',
                    startOffset: d.startOffset || '50%',
                    icon: d.icon,
                    stroke: d.stroke || that.options.linkStroke || 'rgba(140, 140, 140, 0.56)',
                    strokeWidth: d.strokeWidth || 1,
                    strokeDasharray: d.strokeDasharray || '4,5',
                    arrow: 'single',
                    animation: d.animation || false,
                    nameSize: d.nameSize || 12,
                    nameColor: d.nameColor || that.options.linkNameFill || '#999999',
                    fontFamily: d.fontFamily || 'ts',
                    nameLength: d.name ? that.calculateTextLen(d.name).width : 0,
                    selectStroke: that.options.selectStroke,
                    selectWidth: that.options.strokeHoverWidth || 6,
					contextMenu: d.contextMenu || contextMenuData || [],
                    getSet: {}, // 专门给get,set提供数值存放的位置
                    status: {hide: false},
                    offset: d.offset || 0,
                    info: {},
                };

                sourceNodeData.out.push(db);
                targetNodeData.in.push(db);
                // sourceNodeData.children.push(db.target);
                // targetNodeData.parents.push(db.source);

                // nameColor不存在就取stroke
                !db.nameColor && (db.nameColor = db.stroke);
                if (!db.selectStroke) {
                    var color = d3.color(db.stroke);
                    color.opacity = 0.7;
                    db.selectStroke = color.toString();
                }

                // 添加线的标记
                db.marker = {
                    name: d.markerName || '',
                    color: d.markerColor || 'red',
                    size: d.markerSize || 10,
                    dy: '-0.9em',
                    startOffset: d.markerOffset || '50%',
                }

                // 添加节点信息展示字典
                Object.assign(db.info, { // key为db中的属性，值为展示的detail
                    type: 'link',
                    label: '线条',
                    name: {
                        label: '名称',
                        value: db.name,
                        editable: true,
                        type: 'input'
                    },
                    stroke: {
                        label: '颜色',
                        value: db.stroke,
                        editable: true,
                        type: 'color'
                    }
                });
                return db;
            }).filter(function (d) {
                return d;
            });
        },
        position: function (links) {
            var that = this;
            if (Array.isArray(links)) {
                links.forEach(function (d) {
                    d.data.points = d.points;
                })
            }
        },
        linkMode: function (d, points) {
            var that = this;
            var offset = 3;
            if (that.options.layoutOpts.rankdir === 'LR') {
                if (d.mode === 'vertical') {
                    points[0].x = d.source.x > d.target.x ? d.points[0].x - offset : points[0].x + offset;
                    points[d.points.length - 1].x = d.source.x > d.target.x ? points[points.length - 1].x - offset : points[points.length - 1].x + offset;
                }
            } else { // 默认上下
                if (d.mode === 'vertical') {
                    points[0].y = d.source.y > d.target.y ? points[0].y - offset : points[0].y + offset;
                    points[points.length - 1].y = d.source.y > d.target.y ? points[points.length - 1].y + offset : points[points.length - 1].y - offset;
                }
            }
        },
        linkMargin: function (d, points) {
            var that = this;
            var offset = 3;
            if (that.options.layoutOpts.rankdir === 'LR') {
                if (d.mode === 'vertical') {
                    points[0].x = d.source.x > d.target.x ? d.points[0].x - offset : points[0].x + offset;
                    points[d.points.length - 1].x = d.source.x > d.target.x ? points[points.length - 1].x - offset : points[points.length - 1].x + offset;
                }
            } else { // 默认上下
                if (d.mode === 'vertical') {
                    points[0].y = d.source.y > d.target.y ? points[0].y - d.source.margin[0] : points[0].y + d.source.margin[2];
                    points[points.length - 1].y = d.source.y > d.target.y ? (points[points.length - 1].y + d.target.margin[2]) : (points[points.length - 1].y - d.target.margin[0]);
                    var points0 = that.Util.shapeEdge({
                        x: points[0].x,
                        y: points[0].y,
                        width: d.source.width,
                        height: 10,
                        shape: d.source.shape
                    }, points[1]);
                    var points1 = that.Util.shapeEdge({
                        x: points[points.length - 1].x,
                        y: points[points.length - 1].y,
                        width: d.target.width,
                        height: 10,
                        shape: d.target.shape
                    }, points[points.length - 2]);
                    points[0].x = points0.x;
                    points[0].y = points0.y;
                    points[points.length - 1].x = points1.x;
                    points[points.length - 1].y = points1.y;
                } else if (d.mode === 'horVertical') {

				}
            }
        },
        shapeEdge: function (data, point) {
            var that = this;
            var point1 = point;
            switch (data.shape) {
                case 'circle':
                    point1 = that.intersect[data.shape]({
                        x: data.x,
                        y: data.y
                    }, data.size, point);
                    break;
                case 'ellipse':
                    point1 = that.intersect[data.shape]({
                        x: data.x,
                        y: data.y
                    }, data.width / 2, data.height / 2, point);
                    break;
                case 'polygon':
                    point1 = that.intersect[data.shape]({
                        x: data.x,
                        y: data.y,
                        width: data.width,
                        height: data.height
                    }, data.polyPoints, point);
                    break;
                case 'rect':
                    point1 = that.intersect[data.shape]({
                        x: data.x,
                        y: data.y,
                        width: data.width,
                        height: data.height
                    }, point);
                    break;
            }
            return point1;
        },
        toJson: function () {
            var datas = this.getAll('data');
            var data = datas.map(function (d) {
                d.data.source = d.source.uuid;
                d.data.target = d.target.uuid;
                // d.data.sAnchor = d.sAnchor;
                // d.data.tAnchor = d.tAnchor;
                d.data.points = d.points;
                return d.data;
            });
            return {
                links: data
            };
        },
        draw: function (data, action) {
            var that = this;
            var typeData = {};

            var links = this.el.selectAll('g.' + this.options.linkClass).data(data, function (d) {
                return d.uuid + d.reuuid;
            });

            // enter
            var linksEnter = links.enter().append('g')
                .attr('class', this.options.linkClass)
                .attr('fill', 'none');

            // exit
            var linksExit = links.exit();
            if (action === 'init') {
                linksExit.dispatch('remove');
            } else if (action === 'relation') {
				// links.classed('hide', false);
				// linksExit.classed('hide', true)
            } else if (action === 'remove') {
				// linksExit.dispatch('remove');
            }

            // 节点类型归纳
            linksEnter.each(function (d) {
                d.el = this;
                typeData[d.mode] && Array.isArray(typeData[d.mode]) ? typeData[d.mode].push(d) : typeData[d.mode] = [d];
            });
            // 存放节点
            this.nodes.add(linksEnter.nodes());

            // 计算link的偏移量
            // this.linkOffset(data);

            // 节点添加
            // Object.keys(typeData).forEach(function (key) {
            //     that.getVm(key).draw(typeData[key]);
            // });

            // update
            // links = links.data(updateData, function (d) {
            //     return d.uuid;
            // });
            // this.update(links);

            // append link
            this.appendLink(linksEnter, action);
            // append name
            this.appendName(linksEnter);
            // append name
            this.appendMarker(linksEnter);

            // 处理线条path数据
            // this.disposePath(linksEnter, action)
            linksEnter.on('mouseover', function () {
                var link = d3.select(this);
                link.raise();
            })

            links.merge(linksEnter).each(function (d) {
                if (d.status.hide) d3.select(this).classed('hide', true);
                else d3.select(this).classed('hide', false);
            })

            // 添加节点接口实例
			this.createPort(linksEnter, opts.type);

            // 动画
            linksEnter.dispatch('add');

			linksEnter.on('contextmenu', function (d) {
				d3.event.stopPropagation();
				d3.event.preventDefault();
			    that.hook('contextmenu', d);
            });
			linksEnter.each(function (d) {
				if (typeof that.options.linkAppend === 'function') that.options.linkAppend(d.port);
			});

        },
        update: function (links) {
            var that = this;
            links.each(function (d) {
                d.transition = d3.event && d3.event.type === 'drag' ? false : true;
                d.pathStr = that.getPathStr(d);
                d.transition = false;
            })
        },
        transformTransition: function (data) {
			if (!data || !data.nodes) return false;
			var transNodes = data.nodes;
			var time = data.t || this.options.duration;
			data.hasOwnProperty('o0') && transNodes.attr('opacity', data.o0);
			data.hasOwnProperty('strokeDasharray0') && transNodes.attr('stroke-dasharray', data['strokeDasharray0']);
			data.hasOwnProperty('d0') && transNodes.attr('d', data.d0);
			transNodes = transNodes.transition(data.name || 'node').duration(time).ease(d3.easeLinear);
			data.hasOwnProperty('d1') && transNodes.attr('d', data.d1);
			data.hasOwnProperty('o1') && transNodes.attr('opacity', data.o1);
			data.hasOwnProperty('strokeDasharray1') && transNodes.attr('stroke-dasharray', data['strokeDasharray1']);
			transNodes
				.on('start', function () {
					data.start && data.start(data.nodes);
				}).on('end', function () {
				data.end && data.end(data.nodes);
			});
			return transNodes;
        },
        fnData: function (data) {
            if (typeof data === 'function') return data(arguments[1]);
        },
        render: function (data, action) {
            if (!data || !Array.isArray(data) || !data.length) return false;
            var vm = this.getVm(opts.type);
            // 数据处理
            data = vm.dataFn(data);
            // 添加节点（分配增删改）
            vm.draw(data, action);
        },
        linkOffset: function (data) {
            // 是否有相同节点的连线
            var sourceTargetList = {};
            var allData = this.getAll('data');
            data.forEach(function (d) {
                var isExit = Object.keys(sourceTargetList).some(function (key) {
                    var uuidArr = key.split('+');
                    return (uuidArr[0] === d.source.uuid && uuidArr[1] === d.target.uuid) || (uuidArr[1] === d.source.uuid && uuidArr[0] === d.target.uuid);
                });
                if (isExit) return;
                var sameSourceAndTarget = allData.filter(function (d1, i) {
                    return (((d.source === d1.source) && (d.target === d1.target)) || ((d.target === d1.source) && (d.source === d1.target))) && d.uuid !== d1.uuid;
                });
                if (sameSourceAndTarget.length) {
                    sameSourceAndTarget.push(d);
                    sourceTargetList[d.source.uuid + '+' + d.target.uuid] = sameSourceAndTarget;
                }
            });
            // 把相同节点实现偏移
            var offset = 30;
            Object.keys(sourceTargetList).forEach(function (key) {
                var uuidArr = key.split('+');
                var first = offset / 2;
                if (sourceTargetList[key].length % 2 === 0) { // 偶数个
                    first = offset / 2;
                } else { // 奇数个
                    first = 0;
                }
                var dur;
                sourceTargetList[key].forEach(function (d, i) {
                    // 大小
                    if (first) {
                        dur = first + Math.floor(i / 2) * offset;
                    } else {
                        dur = first + Math.floor((i + 1) / 2) * offset;
                    }
                    // 正负
                    if (first) { // 方向相同取异号，方向相反取同号
                        dur = d.target.uuid === uuidArr[0] ? dur : -dur;
                        dur = i % 2 ? -dur : dur;
                        d.offset = dur;
                    } else {
                        dur = d.target.uuid === uuidArr[0] ? dur : -dur;
                        dur = i % 2 ? dur : -dur;
                        d.offset = dur;
                    }
                });
            });
        },
        appendLink: function (links, action) {
            var that = this;
            links.each(function (d) {
                var el = d3.select(this);
                el.attr('relation', JSON.stringify(d.relation));
                el
                    .on('selected', function () {
                        if (d.status.selected) return false; // 防止统一状态反复触发，提高性能
                        d.status.selected = true;
                        hover.dispatch('select');
                        if (typeof that.options.linkSelected === 'function') that.options.linkSelected(d.port);
                    })
                    .on('unselect', function () {
                        if (!d.status.selected) return false; // 防止统一状态反复触发，提高性能
                        d.status.selected = false;
                        hover.dispatch('unselect');
						if (typeof that.options.linkUnselected === 'function') that.options.linkUnselected(d.port);
                    })
                    .on('click', function () {
                        d3.event.stopPropagation();
                        if (!that.options.linkSelectable) return;
                        if (!d3.event.ctrlKey &&  that.options.linkRadio) {
                            ['node', 'group', 'link'].forEach(function (d) {
                                if (that.options.linkCancleSelected.includes(d)) {
                                    that.getVm(d).getAll('condition', 'selected').dispatch('unselect');
                                }
                            });
                        }
                        if (d.status.selected) {
                            el.dispatch('unselect');
                        } else {
                            el.dispatch('selected');
                        }

                        that.hook('click', d)
                    })
                    .on('updatePath', function () {
                        var detail = d3.event.detail;
                        d.transition = detail && detail.transition  !==undefined ? detail.transition : true;
                        d.pathStr = that.getPathStr(d);
                        d.transition = false;
                    });
                // hover
                var hover = el.append('path')
                    .attr('stroke', 'transparent')
                    .attr('fill', 'none')
                    .attr('stroke-width', d.selectWidth)
                    .attr('class', 'hover')
                    .attr('mask', `url(#link-mask${that.options.id + '-' + d.uuid})`)
                    .on('select', function (d1) {
                        var color = d3.color(d.stroke);
                        color.opacity = 0.5;
                        // hover.attr('stroke', d.selectStroke);
                        hover.attr('stroke', color.toString());
                    })
                    .on('unselect', function (d1) {
                        hover.attr('stroke', 'transparent');
                    });
                // link
                var link = el.append('path')
                    .attr('stroke', 'blue')
                    .attr('class', 'linkPath')
                    .attr('stroke-linecap', 'round')
                    .attr('stroke-linejoin', 'round')
                    .attr('fill', 'none')
					.attr('stroke-opacity', '0.9')
                    .attr('id', `link${that.options.id}-${d.uuid}`)
                    .attr('mask', `url(#link-mask${that.options.id + '-' + d.uuid})`);

                // title
                var title = el.append('title').text('');

                // traction
                var traction = el.append('path')
                    .attr('stroke', 'transparent')
                    .attr('class', 'traction')
                    .attr('stroke-linecap', 'round')
                    .attr('stroke-linejoin', 'round')
                    .attr('id', `link-traction${that.options.id}-${d.uuid}`);
                // text fill
                // var nameBg = el.append('path')
                    // .attr('stroke', 'blue')
                    // .attr('class', 'nameBg')
                    // .attr('stroke-width', 16)
                    // .on('click',function () {
                    //     console.log(1111);
                    // })
                    // .attr('mask', `url(#link-mask-name${that.options.id + '-' + d.uuid})`)
                    // .attr('clip-path', `url(#link-mask-name-clip${that.options.id + '-' + d.uuid})`);


                // mask
                var mark = el.append('mask').lower()
                    .attr('x', '-500000')
                    .attr('y', '-500000')
                    .attr('width', '1000000')
                    .attr('height', '1000000')
                    .attr('maskUnits', 'userSpaceOnUse') // 必须要加 (解决水平、垂直时的line不可见情况) 需要配合mask的边际范围
                    .attr('id', 'link-mask' + that.options.id + '-' + d.uuid);

                var maskWhite = mark.append('path')
                    .attr('stroke', 'white')
                    .attr('stroke-width', 16)
                    // .attr('stroke-linecap', 'round')
                    // .attr('stroke-linejoin', 'round')
                    .attr('fill', 'none');

                var maskBlack = mark.append('path')
                    .attr('class', 'black')
                    .attr('stroke', 'white')
                    // .attr('stroke-linecap', 'round')
                    // .attr('stroke-linejoin', 'round')
                    .attr('stroke-width', 16)
                    .attr('fill', 'none');

                // var mark2 = el.append('mask').lower()
                //     .attr('x', '-500000')
                //     .attr('y', '-500000')
                //     .attr('width', '1000000')
                //     .attr('height', '1000000')
                //     .attr('maskUnits', 'userSpaceOnUse') // 必须要加 (解决水平、垂直时的line不可见情况) 需要配合mask的边际范围
                //     // .attr('id', 'link-mask-name' + that.options.id + '-' + d.uuid);
                //
                // var maskWhite2 = el.append('clipPath').lower()
                //     .attr('id', 'link-mask-name-clip' + that.options.id + '-' + d.uuid)
                //     .append('path')
                //     .attr('stroke', 'black')
                //     .attr('stroke-width', 18)
                //     .attr('stroke-linecap', 'round')
                //     .attr('stroke-linejoin', 'round')
                //     .attr('fill', 'none');
                //
                // var maskBlack2 = mark2.append('path')
                //     .attr('class', 'black')
                //     .attr('stroke', 'white')
                //     .attr('stroke-linecap', 'round')
                //     .attr('stroke-linejoin', 'round')
                //     .attr('stroke-width', 14)
                //     .attr('fill', 'none');


                if (d.pathStr) {
                    console.warn(d);
                }
                // 相关变量修改操作
                Object.defineProperties(d, {
                    pathStr: {
                        get: that.objectGet('pathStr', ''),
                        set: function (value) {
                            if (this.getSet.pathStr !== value) {
                                d.totalLength = that.totalLengthPath.attr('d', value).node().getTotalLength();
                                var transformData = {
                                    name: 'aa',
                                    nodes: d3.selectAll([link.node(), traction.node(), hover.node(), maskWhite.node(), maskBlack.node()]),
                                    d1: function () {
                                        if (this === traction.node() && (d.source.x > d.target.x)) {
                                            return that.getPathStr(d, 'reverse');
                                        }
                                        // else if (this === maskWhite2.node()) {
                                        //     return that.getPathStr(d, 'close');
                                        // }
                                        else {
                                            return value;
                                        }
                                    }
                                };
                                if (!d.pathStr) {
                                    var xy0 = d.source.xyChange.split('+');
                                    var source = Object.assign({},d.source, {
                                        x: +xy0[0],
                                        y: +xy0[1],
                                    });
                                    transformData.d0 = that.getPathStr(Object.assign({}, d, {source: source}));
                                } else {

                                }

                                if (d3.event && d3.event.type === 'drag' || d.transitionType === 'css') {
                                    link.attr('d', value);
                                    hover.attr('d', value);
                                    // nameBg.attr('d', value);
                                    maskWhite.attr('d', value);
                                    maskBlack.attr('d', value);
                                    // maskWhite2.attr('d', that.getPathStr(d, 'close'));
                                    // maskBlack2.attr('d', value);
                                        // .attr('stroke-dasharray', blackDasharray())
                                    if (d.source.x > d.target.x) {
                                        traction.attr('d', that.getPathStr(d, 'reverse'));
                                    } else {
                                        traction.attr('d', value);
                                    }
                                } else {
                                    that.transformTransition(transformData);
                                    if (d.pathStr) {
                                        that.transformTransition(Object.assign({}, transformData, {
                                            nodes: maskBlack,
                                            name: 'bb',
                                            strokeDasharray1: blackDasharray()
                                        }));
                                    }
                                }
                                el.dispatch('nameMask');
                                this.getSet.pathStr = value;
                            }
                        }
                    },
                    pathTractionStr: {
                        get: that.objectGet('pathTractionStr', ''),
                        set: function (value) {
                            if (this.getSet.pathTractionStr !== value) {
                                traction.attr('d', value);
                                this.getSet.pathTractionStr = value;
                            }
                        }
                    },
                    stroke: {
                        get: that.objectGet('stroke', d.stroke),
                        set: function (value) {
                            if (this.getSet.stroke !== value) {
                                link.attr('stroke', value);
                                this.getSet.stroke = value;
								d.arrow = d.arrow;
                            }
                        }
                    },
                    strokeWidth: {
                        get: that.objectGet('strokeWidth', d.strokeWidth),
                        set: function (value) {
                            if (this.getSet.strokeWidth !== value) {
                                link.attr('stroke-width', value);
                                this.getSet.strokeWidth = value;
                            }
                        }
                    },
					title: {
						get: that.objectGet('title', ''),
						set: function (value) {
							if (this.getSet.title !== value) {
								title.text(value);
								this.getSet.title = value;
							}
						}
					},
                    strokeDasharray: {
                        get: that.objectGet('strokeDasharray', d.strokeDasharray),
                        set: function (value) {
                            if (this.getSet.strokeDasharray !== value) {
                                link.attr('stroke-dasharray', value);
                                this.getSet.strokeDasharray = value;
                            }
                        }
                    },
                    arrow: {
						get: that.objectGet('arrow', d.arrow),
						set: function (value) {
						    if (this.getSet.arrow !== value + '-' + d.stroke) {
						        value = value.split('-')[0];
								if (value === 'none') {
									link.attr('marker-end', 'none');
									link.attr('marker-start', 'none');
								} else if (value === 'both') {
									link.attr('marker-end', `url(#${that.getArrowColor(d.stroke)})`);
									link.attr('marker-start', `url(#${that.getArrowColor(d.stroke, 'start')})`);
								} else if (value === 'single') {
									link.attr('marker-end', `url(#${that.getArrowColor(d.stroke)})`);
									if (that.options.linkArrow === 'startDot') link.attr('marker-start', `url(#dot${that.getArrowColor(d.stroke)})`);
								}
								this.getSet.arrow = value + '-' + d.stroke;
                            }
						}
                    },
					animation: {
						get: that.objectGet('animation', d.animation),
						set: function (value) {
							if (this.getSet.animation !== value) { // value: Boolean
							    if (value) { // 为true时
									linkAnimate('start')
                                } else {
									linkAnimate('end')
								}
								this.getSet.animation = value;
							}
						}
                    },
                    // totalLength: {
                    //     get: that.objectGet('totalLength', link.node().getTotalLength()),
                    //     set: function (value) {
                    //         if (this.getSet.totalLength !== value) {
                    //             // if (!d.transition) {
                    //             //     maskBlack
                    //             //         .attr('stroke-dasharray', blackDasharray(value));
                    //             // }
                    //             this.getSet.totalLength = value;
                    //         }
                    //     }
                    // },
                    mode: {
                        get: that.objectGet('mode', d.mode),
                        set: function (value) {
                            if (this.getSet.mode !== value) {
                                d.pathStr = that.getPathStr(d);
                                this.getSet.mode = value;
                            }
                        }
                    }
                });
                // 初始化相关数据操作
                d.pathStr = that.getPathStr(d);
                d.stroke = d.stroke;
                d.strokeWidth = d.strokeWidth;
                d.strokeDasharray = d.strokeDasharray;
                d.animation = d.animation;

                function blackDasharray (action) {
                    var totalLen = d.totalLength;
                    var nameLength = d.nameLength;
                    var padding = nameLength ? 3 : 0;
                    if (nameLength) return `0 ${totalLen / 2 - nameLength / 2 - padding} ${nameLength + padding * 2} ${totalLen / 2 - nameLength / 2 - padding}`;
                    else return `0 ${totalLen / 2} 0 ${totalLen / 2}`;
                }

                function linkAnimate (action) {
                    if (action === 'start') {
						maskWhite
							.attr('stroke-dasharray', `${d.totalLength} ${d.totalLength / 2}`)
							.attr('stroke-dashoffset', `${d.totalLength * 3 / 2}`)
                            .transition('animation')
							.duration(700)
							.ease(d3.easeLinear)
							.attr('stroke-dashoffset', `0`)
                            .delay(300)
							.on('end', function () {
								linkAnimate('start');
							})
                    } else if (action === 'end') {
						maskWhite
							.interrupt('animation')
                            .attr('stroke-dasharray', `${d.totalLength} 0`)
                    }
                }

                // 添加remove动画
                var arrow = d.arrow;
                el.on('add.link', function (){
                    that.transformTransition({
                        nodes: link,
                        name: 'pathAnimate',
                        strokeDasharray0: `0 ${d.totalLength}`,
                        strokeDasharray1: `${d.totalLength} 0`,
                        start: function () {
                            d.arrow = 'none';
                        },
                        end: function () {
                            d.arrow = arrow;
                            that.options.hierTransAble && that.transitionEnd && that.transitionEnd()
                        }
                    });
                });
                el.on('remove.link', function (d) {

                    that.transformTransition({
                        nodes: d3.selectAll([link.node(), hover.node()]),
                        name: 'pathAnimate',
                        strokeDasharray0: `${d.totalLength} 0`,
                        strokeDasharray1: `0 ${d.totalLength}`,
                        start: function () {
                            d.arrow = 'none';
                        },
                        end: function () {
                            el.remove();
                        }
                    });
					that.nodes.remove(d);
					// 删除相关联in和out
                    var sIndex = d.source.out.indexOf(d);
                    var tIndex = d.target.in.indexOf(d);
                    if (sIndex !== -1) d.source.out.splice(sIndex, 1);
                    if (tIndex !== -1) d.target.in.splice(tIndex, 1);
					if (typeof that.options.linkRemove === 'function') that.options.linkRemove(d.port);
                });
                el.on('nameMask', function () {
                    if (d.nameLength) {
                        maskBlack.attr('stroke', 'black')
                            .attr('stroke-dasharray', blackDasharray());
                        // maskWhite2.attr('stroke-dasharray', blackDasharray());
                        // maskBlack2.attr('stroke', 'white')
                        //     .attr('stroke-dasharray', blackDasharray())
                    } else {
                        maskBlack.attr('stroke', 'white');
                        // maskBlack2.attr('stroke', 'black');
                    }
                })

            });
            return this;
        },
        appendName: function (links) {
            var that = this;
            links.each(function (d) {
                var el = d3.select(this);
                // link
                var name = el.append('text')
                    .attr('class', 'name')
                    .attr('font-family', d.fontFamily)
                    .attr('dy', '0.32em')
                    .attr('text-anchor', d.textAnchor);
                // text
                var textPath = name.append('textPath')
                    .attr('startOffset', d.startOffset)
                    .attr('xlink:href', `#link-traction${that.options.id}-${d.uuid}`)
                    .append('tspan');
                // mask
                // var maskBlack = el.select('mask path.black');

                // 相关变量修改操作
                Object.defineProperties(d, {
                    nameSize: {
                        get: that.objectGet('nameSize', d.nameSize),
                        set: function (value) {
                            if (this.getSet.nameSize !== value) {
                                name.attr('font-size', value);
                                this.getSet.nameSize = value;
                            }
                        }
                    },
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
                                d.nameLength = that.calculateTextLen(value, d.nameSize).width;
                                textPath.text(value);
                                el.dispatch('nameMask');
                                this.getSet.name = value;
                            }
                        }
                    },
                    // nameLength: {
                    //     get: that.objectGet('nameLength', 0),
                    //     set: function (val) {
                    //         val = val === undefined ? 0 : val;
                    //         if (this.getSet.nameLength !== val) {
                    //             var totalLen = d.totalLength;
                    //             // var nameLength = val;
                    //             // var padding = 0;
                    //             //
                    //             // if (val) {
                    //             //     maskBlack
                    //             //         .attr('stroke-dasharray', `0 ${totalLen / 2 - nameLength / 2 - padding} ${nameLength + padding * 2} ${totalLen / 2 - nameLength / 2 - padding}`);
                    //             // } else {
                    //             //     maskBlack
                    //             //         .attr('stroke-dasharray', `0 ${totalLen / 2} 0 ${totalLen / 2}`)
                    //             // }
                    //             this.getSet.nameLength = val;
                    //
                    //         }
                    //     },
                    // }
                });

                // 初始化相关数据操作
                d.name = d.name;
                d.nameSize = d.nameSize;
                d.nameColor = d.nameColor;

                // 添加append动画
                el.on('add.name', function () {
                    that.transformTransition({
                        nodes: name,
                        o0: 0,
                        o1: 1,
                    });
                    // name
                    //     .attr('opacity', 0)
                    //     .transition()
                    //     .duration(time)
                    //     .ease(d3.easeLinear)
                    //     .attr('opacity', 1)
                });
                // 添加remove动画
                el.on('remove.name', function () {
                    // var time = d.totalLength * 1.5;
                    that.transformTransition({
                        nodes: name,
                        o0: 1,
                        o1: 0,
                    }, 'rmeove');
                    // name
                    //     .transition()
                    //     .duration(time)
                    //     .ease(d3.easeLinear)
                    //     .attr('opacity', 0)
                    //     .on('end', function () {
                    //         name.remove();
                    //     })
                })
            });
        },
        appendMarker: function (links) {
            var that = this;
            links.each(function (d) {
                var el = d3.select(this);
                // link
                var name = el.append('text')
                    .attr('class', 'marker')
                    .attr('font-family', d.fontFamily)
                    .attr('dy', d.marker.dy)
                    .attr('text-anchor', d.textAnchor);
                // text
                var textPath = name.append('textPath')
                    .attr('startOffset', d.marker.startOffset)
                    .attr('xlink:href', `#link-traction${that.options.id}-${d.uuid}`)
                    .append('tspan');

                // 相关变量修改操作
                Object.defineProperties(d.marker, {
                    size: {
                        get: that.objectGet('markerSize', d.marker.size, d).bind(d),
                        set: function (value) {
                            if (d.getSet.nameSize !== value) {
                                name.attr('font-size', value);
                                d.getSet.markerSize = value;
                            }
                        }
                    },
                    color: {
                        get: that.objectGet('markerColor', d.marker.color).bind(d),
                        set: function (value) {
                            if (d.getSet.nameColor !== value) {
                                name.attr('fill', d.nameColor);
                                d.getSet.markerColor = value;
                            }
                        }
                    },
                    name: {
                        get: that.objectGet('markerName', d.marker.name).bind(d),
                        set: function (value) {
                            if (d.getSet.markerName !== value) {
                                textPath.text(value);
                                d.getSet.markerName = value;
                            }
                        }
                    }
                });

                // 初始化相关数据操作
                d.marker.name = d.marker.name;
                d.marker.size = d.marker.size;
                d.marker.color = d.marker.color;

            });
        },
        intersectRect: function (node, point) {
            return;
            var x = node.x;
            var y = node.y;

            // Rectangle intersection algorithm from:
            // http://math.stackexchange.com/questions/108113/find-edge-between-two-boxes
            var dx = point.x - x;
            var dy = point.y - y;
            var w = node.width / 2;
            var h = node.height / 2;

            var sx, sy;
            if (Math.abs(dy) * w > Math.abs(dx) * h) {
                // Intersection is top or bottom of rect.
                if (dy < 0) {
                    h = -h;
                }
                sx = dy === 0 ? 0 : h * dx / dy;
                sy = h;
            } else {
                // Intersection is left or right of rect.
                if (dx < 0) {
                    w = -w;
                }
                sx = w;
                sy = dx === 0 ? 0 : w * dy / dx;
            }

            return {x: x + sx, y: y + sy};
        },
        getArrowColor: function (color, dir) {
            dir = dir ? dir : 'end';
            var that = this;
            color = d3.color(color).toString();
            if (!this.Svg.marker) this.Svg.marker = this.Svg.defs.append('g').attr('class', 'marker');
            var arrowMarker = this.Svg.marker.selectAll('marker.arrow' + dir).filter(function (d) {
                return d === color;
            });
            if (arrowMarker.size()) return arrowMarker.attr('id');
            else {
                var colorId = that.guid(6);
                if (dir === 'start') {
                    var arrow_path_right = 'M10,8 L2,4 L10,0 L6,4 L10,8';
                    var arrowMarkerS = this.Svg.marker.append('marker').datum(color)
                        .attr('class', 'arrow' + dir)
                        .attr('id', `arrow${this.options.id}-${colorId}`)
                        .attr('markerUnits', 'strokeWidth')
                        .attr('markerWidth', '12')
                        .attr('markerHeight', '12')
                        .attr('viewBox', '0 0 12 12')
                        .attr('refX', '2')
                        .attr('refY', '4')
                        .attr('orient', 'auto');
                    arrowMarkerS.append('path')
                        .attr('d', arrow_path_right)
                        .attr('fill', color)
                        .attr('stroke', color);

                    var arrowMarkerD = this.Svg.marker.append('marker').datum(color)
                        .attr('class', 'arrow' + dir)
                        .attr('id', `dotarrow${this.options.id}-${colorId}`)
                        .attr('markerUnits', 'userSpaceOnUse')
                        .attr('markerWidth', '12')
                        .attr('markerHeight', '12')
                        .attr('viewBox', '0 0 12 12')
                        .attr('refX', '3')
                        .attr('refY', '6')
                        .attr('orient', 'auto');
                    arrowMarkerD.append('circle')
                        .attr('r', 2)
                        .attr('cx', 3)
                        .attr('cy', 6)
                        .attr('fill', color)
                        .attr('stroke', color);
                } else {
                    var arrow_path_right = 'M0,2 L8,6 L0,10 L4,6 L0,2';
                    var arrowMarkerS = this.Svg.marker.append('marker').datum(color)
                        .attr('class', 'arrow' + dir)
                        .attr('id', `arrow${this.options.id}-${colorId}`)
                        .attr('markerUnits', 'strokeWidth')
                        .attr('markerWidth', '12')
                        .attr('markerHeight', '12')
                        .attr('viewBox', '0 0 12 12')
                        .attr('refX', '8')
                        .attr('refY', '6')
                        .attr('orient', 'auto');
                    arrowMarkerS.append('path')
                        .attr('d', arrow_path_right)
                        .attr('fill', color)
                        .attr('stroke', color);
                    var arrowMarkerD = this.Svg.marker.append('marker').datum(color)
                        .attr('class', 'arrow' + dir)
                        .attr('id', `dotarrow${this.options.id}-${colorId}`)
                        .attr('markerUnits', 'userSpaceOnUse')
                        .attr('markerWidth', '12')
                        .attr('markerHeight', '12')
                        .attr('viewBox', '0 0 12 12')
                        .attr('refX', '3')
                        .attr('refY', '6')
                        .attr('orient', 'auto');
                    arrowMarkerD.append('circle')
                        .attr('r', 2)
                        .attr('cx', 3)
                        .attr('cy', 6)
                        .attr('fill', color)
                        .attr('stroke', color);
                }

                return `arrow${this.options.id}-${colorId}`;
            }
        },
        getPathStr: function (data, action) {
            var that = this;
            var points = data.points.map(function (d) {
                return d;
            });

            if (!points.length) {
                points.push(
                    {
                        x: data.source.x,
                        y: data.source.y
                    },
                    {
                        x: data.target.x,
                        y: data.target.y
                    }
                )
            }
            // ?
            this.linkMode(data, points);
            // 找到最近两个连接锚点
            var repoints = this.linkRegulation(data, points);
            points[0] = repoints[0];
            points[points.length - 1] = repoints[1];

			// 处理node margin
			this.linkMargin(data, points);

            // 算坐标
            if (data.mode === 'horVertical') {
                that.horVertical(points, data);
            }

            if (action === 'reverse') {
                points = points.concat([]).reverse();
            } else if (action === 'animation') {
                // points = points.map(function (d) {
                //     return points[0];
                // })
            }


            // if (action === 'close') {
            //     var revepoints =  points.concat([]).reverse();
            //     points = points.concat(revepoints);
            // }
            var linkFn = d3.line()
                .x(function (d) {
                    return d.x;
                })
                .y(function (d) {
                    return d.y;
                });
            switch (data.mode || action) {
                case 'straight':
                    points.splice(1, points.length - 2);
                    break;
                case 'curve':
                    linkFn.curve(d3.curveBasis);
                    break;
                case 'vertical':
                    linkFn = function (points) {
                        var path = d3.path();
                        var points0 = {}, pointsl = {};

                        path.moveTo(points0.x || points[0].x, points0.y || points[0].y);
                        points.reduce(function (a, b) {
                            var cpx1 = a.x + (b.x - a.x) * 0.1;
                            var cpy1 = (a.y + b.y) / 2;
                            var cpx2 = b.x - (b.x - a.x) * 0.1;
                            var cpy2 = (a.y + b.y) / 2;
                            path.bezierCurveTo(cpx1, cpy1, cpx2, cpy2, b.x, b.y);
                            return b;
                        });
                        return path.toString();
                    };
                    break;
                case 'horizontal':
                    linkFn = function (points) {
                        var path = d3.path();

                        path.moveTo(points[0].x, points[0].y);
                        points.reduce(function (a, b) {
                            var cpx1 = a.x + (b.x - a.x) * 0.1;
                            var cpy1 = (a.y + b.y) / 2;
                            var cpx2 = b.x - (b.x - a.x) * 0.1;
                            var cpy2 = (a.y + b.y) / 2;
                            path.bezierCurveTo(cpx1, cpy1, cpx2, cpy2, b.x, b.y);
                            return b;
                        });
                        return path.toString();
                    };
                    break;
                case 'horVertical':
                    linkFn = function (points) {
                        data.transitionType = 'css';
                        // path string
                        var path = d3.path();
                        var arcRadius = 6;
                        path.moveTo(points[0].x, points[0].y);

                        points.reduce(function (a, b, i, arr) {
                            if (a.x == b.x && a.y == b.y || i === 1) return b;
                            var c = points[i - 2];
                            var rx1 = Infinity;
                            var ry1 = Infinity;
                            var rx2 = Infinity;
                            var ry2 = Infinity;
                            if (Math.abs(b.x - a.x)) rx1 = Math.abs(b.x - a.x) / 2;
                            if (Math.abs(b.y - a.y)) ry1 = Math.abs(b.y - a.y) / 2;
                            if (c && Math.abs(c.x - a.x)) rx2 = Math.abs(c.x - a.x) / 2;
                            if (c && Math.abs(c.y - a.y)) ry2 = Math.abs(c.y - a.y) / 2;
                            var minr = Math.min(rx1, ry1, rx2, ry2);
                            minr = minr / 2 < arcRadius ? minr / 2 : arcRadius;

                            path.arcTo(a.x, a.y, b.x, b.y, minr);
                            return b;
                        });
                        path.lineTo(points[points.length - 1].x, points[points.length - 1].y);
                        return path.toString();
                    };
                    break;
                default:
                    break;
            }
            return linkFn(points);
        },
        horVertical: function (points, data) {
            var p1 = points[0], p2 = points[points.length - 1];
            var dirObj = {T: -1, B: 1, L: -2, R: 2};
            var offset = 20;
            var sIndex = data.source.groups[0].contain.indexOf(data.source);
            var tIndex = data.target.groups[0].contain.indexOf(data.target);
            // 单位方向
            var dirP1 = dirObj[p1.dir] / Math.abs(dirObj[p1.dir]);
            var dirP2 = dirObj[p2.dir] / Math.abs(dirObj[p2.dir]);
            // 反向
            var p1o = dirP1 * (offset + sIndex * 5);
            var p2o = dirP2 * (offset + tIndex * 5);
            // 错开
            p1o += dirP1 * 6;
            p2o += dirP2 * 6;

            // 方向组合
            var isDirOp = dirObj[p1.dir] + dirObj[p2.dir] === 0; // 反向
            var isDirSa = dirObj[p1.dir] === dirObj[p2.dir]; // 同向
            var isHor = Math.abs(dirObj[p1.dir]) === 2; // 水平
            var isVer = Math.abs(dirObj[p1.dir]) === 1; // 垂直
            // 起始点牵引点 p3,p4
            var p3, p4;
            if (Math.abs(dirObj[p1.dir]) === 2) p3 = {x: p1.x + p1o, y: p1.y};
            else if (Math.abs(dirObj[p1.dir]) === 1) p3 = {x: p1.x, y: p1.y + p1o};
            if (Math.abs(dirObj[p2.dir]) === 2) p4 = {x: p2.x + p2o, y: p2.y};
            else if (Math.abs(dirObj[p2.dir]) === 1) p4 = {x: p2.x, y: p2.y + p2o};

            // 中间拐点arr p5,p6
            var p5,p6;
            var p5o = {x: (p3.x + p4.x) / 2, y: (p3.y + p4.y) / 2};
            if (isDirOp && isHor) { // x水平异向
                var isZ = ((dirObj[p2.dir] === 2) && (p3.x < p4.x)) || ((dirObj[p2.dir] === -2) && (p3.x > p4.x));
                if (isZ) {
                    p5 = {x: p3.x, y: p5o.y};
                    p6 = {x: p4.x, y: p5o.y};
                } else {
                    p5 = {x: p5o.x, y: p3.y};
                    p6 = {x: p5o.x, y: p4.y};
                }
            } else if (isDirSa && isHor) {// x水平同向
                var x = dirObj[p1.dir] === -2 ? Math.min(p3.x, p4.x) : Math.max(p3.x, p4.x);
                p5 = {x: x, y: p3.y};
                p6 = {x: x, y: p4.y};
            }
            points.splice(1, 0, p3, p5, p6,  p4);
        },
        linkRegulation: function (data, points) {
            var that = this;
            var point1 = {}, point2 = {};
            var source = data.source, target = data.target;
            if (this.options.linkRegulation === 'recently') {
                var sourcePoints = source.anchor;
                var targetPoints = target.anchor;
                var s1, s2;
                var minLen = Infinity;
                var offset = 30;
                sourcePoints.forEach(function (p1, i1) {
                    targetPoints.forEach(function (p2) {
                        var dist = Math.sqrt(Math.pow((p2.x1 + target.x) - (p1.x1 + source.x), 2) + Math.pow((p2.y1 + target.y) - (p1.y1 + source.y), 2));
                        if (minLen > dist) {
                            minLen = dist;
                            s1 = p1;
                            s2 = p2;
                        }
                    })
                });
                // x,y
                point1 = {
                    x: s1.x1,
                    y: s1.y1,
                    dir: s1.dir,
                    o: offset
                };
                point2 = {
                    x: s2.x1,
                    y: s2.y1,
                    dir: s2.dir,
                    o: offset
                };
                // 如果不在起点范围
                // 1. 如何是异向则取反

                if (point1.hasOwnProperty('x') && point1.hasOwnProperty('y')) {
                    point1.x += source.x;
                    point1.y += source.y;
                }
                if (point2.hasOwnProperty('x') && point2.hasOwnProperty('y')) {
                    point2.x += target.x;
                    point2.y += target.y;
                }

            } else {
                point1 = that.shapeEdge(data.source, points[1]);
                point2 = that.shapeEdge(data.target, points[points.length - 2]);
            }
            return [point1, point2];
        },
        getLinkByNode: function (data) {
            return this.getAll('selection').filter(function (d) {
                return d.source === data || d.target === data;
            });
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
            return this.el.selectAll('.' + this.options.linkClass).data();
        }
    });

})));