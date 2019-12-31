(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {}), global));
}(this, (function (exports, global) {
    'use strict';
    var version = '1.0.0';
    var topoOpts = {
        type: 'Base',
        zoomExtent: [0.2, 10],
        pattern: 'edit',   // 设置编辑和只读模式
        anchorable: true, // 设置是否锚点
        initTrans: false,   // 设置初始是否需要开场动画效果
        duration: 500,     // 设置动画过度时间
        uuidmode: 5,       // 默认uuid位数
        layoutOpts: {},    // 布局相关信息
        relation: [],      // 层级结构的过滤条件
        nodeDragable: true, // 节点的拖动
		nodeRadio: true,   // 节点只能单选
        groupRadio: true,   // 节点只能单选
        groupDragable: true,   // group 拖动
        groupBrushable: true,
        nodeDeleteable: true,
		groupDeleteable: true,
		linkDeleteable: true,
        linkSelectable: true,
		brushSelect: true, // 框选
        brushable: true,
        selectedDragable: true,    // 设置节点是否可以拖动
        groupAnchorable: true,
        nodeAnchorable: true,
        linkDefaultMode: 'curve',
        nodeDragCancleSelected: true,
        spaceCancleSelected: ['node', 'link', 'group'],
        linkCancleSelected: ['node', 'group', 'link'],
        hookMap: {
            afterSelected: { // 有目标选中后触发，返回被选中后的目标data
                events: ['click', 'brushEnd'],
                filter: function (fn) {
                    var selected = this.getRoom().getAll('condition', 'selected');
                    if (selected.size()) {
                        fn(selected);
                    }
                }
            },
            noSelected: {
                events: ['click', 'brushEnd'],
                filter: function (fn) {
                    var selected = this.getRoom().getAll('condition', 'selected');
                    if (!selected.size()) {
                        fn();
                    }
                }
            },
            afterClick: {
                events: ['click'],
                filter: function (fn, data) {
                    fn({
                        data: data
                    });
                }
            },
            beforeDrag: {
                events: ['dragStart', 'zoomStart'],
                filter: function (fn, data) {
                    fn();
                }
            },
            context: {
                events: ['contextmenu'],
                filter: function (fn, data) {
                    if (data) {
                        fn({
                            position: d3.mouse(this.getVm('svg').el.node()),
                            data: data
                        });
                    }
                }
            },

            loadEnd: {
                events: ['loadEnd'],
                filter: function (fn, data) {
                    fn(fn, data);
                }
            },
        },
    };

    var eventCb = {
        nodeClick: function (vm) {
            // console.log('node click cb!', vm);
        },
        nodeMouseover: function (vm, action) {
            // console.log('node mouseover ', action);
        },
        nodeMouseout: function (vm, action) {
            // console.log('node mouseout ', action);
        },
		// nodeSelected: function (vm) {
		// 	// console.log('node selected');
		// },
        // nodeUnselected: function (vm) {
		// 	// console.log('node unselected');
		// },
        // loadEnd: function (vm) {
         //    console.log(vm);
        // }
    };

    // 入口
    var topo = function (selector, opts) {
        if (!selector) {
            console.warn('选择器不能为空！');
            return false;
        }

        var domArr = typeof selector === 'string' ? d3.selectAll(selector).nodes()
            : Array.isArray(selector) ? selector
                : selector.nodeType === 1 ? [selector]
                    : undefined;
        if (!domArr) {
            console.warn('传入的第一个参数container有误！');
            return;
        }
        var vmArr = domArr.map(function (dom) {
            return new exports.port(new exports[topoOpts.type](dom, opts));
        });
        if (vmArr.length === 1) return vmArr[0];
        else return vmArr;
    };

    // 核心
    exports[topoOpts.type] = Class.extend({
        init: function (container, opts) {
            var that = this;
            if (!container) {
                this.consoleLog('容器不能為空！', 'error');
                return;
            }
            // 模型
            this
                .instanceInject({
                    options: Object.assign({}, topoOpts, eventCb, opts),
                    content: d3.select(container).classed('topo-container', true),
                    container: d3.select(container).append('xhtml:div').attr('class', 'container'),
                });

            // 画布id
            this.options.id = that.guid(6);

            // 加载模块
            this.Dagre = this.getVm('dagre');
            this.Svg = this.getVm('svg');
            this.Node = this.getVm('node');
            this.Group = this.getVm('group');
            this.Link = this.getVm('link');
            this.Tree = this.getVm('tree');
            this.Context = this.getVm('context');

        },
        instanceInject: function (props) {
            if (props) {
                Object.assign(this, props, {
                    type: props.options.type
                });
            }
            !this.options.vms && (this.options.vms = [this]);
            if (!this.options.vms.includes(this)) this.options.vms.push(this);
            return this;
        },
        consoleLog: function (log, type) {
            switch (type) {
                case 'error':
                    console.error(log);
                    break;
                case 'warn':
                    console.warn(log);
                    break;
                default:
                    console.log(log);
            }
            return this;
        },
        gatherLayout: function (data) {
            var that = this;
            var nodes = data.nodes || [], links = data.links || [], groups = data.groups || [];
            // 给节点定位

            if (this.options.layoutOpts.layout === 'gather') {
				groups.forEach(function (group) {
					// sort
					if (typeof that.options.groupContainSort === 'function') {
						that.options.groupContainSort(group.contain);
					}

					var maxWidth = 0, maxHeight = 0;
					var offset = 0;
					group.contain.forEach(function (node, i) {
						var nh = node.height - 1;
						node.x = group.x + node.width / 2;
						node.y = group.y + node.height / 2 + nh * i;
						if (maxWidth < node.width) maxWidth = node.width;
						if (maxHeight < node.height * i + node.height) maxHeight = nh * i + node.height;
					});
					group.x -= offset;
					group.y -= offset;
					group.width = maxWidth + offset * 2;
					group.height = maxHeight + offset * 2;
				})
            }
        },
        fromJson: function (json) { // 程序入口
            var that = this;
            if (!json) {
                this.consoleLog('json不存在！', 'warn');
                return false;
            }
            var svg = json.svg || json.canvas || {};
            var nodes = json.nodes || [];
            var groups = json.groups || [];
            var links = json.links || [];
            var layout = json.layout || {};

            var dagreData, layoutData;

            // data
            var svg1 = this.Svg.dataFn(svg);
            var nodes1 = this.Node.dataFn(nodes);
            var groups1 = this.Group.dataFn(groups);
            var links1 = this.Link.dataFn(links);

            Object.assign(this.options.layoutOpts || {}, layout);
            // gather layout
            // 1. 计算node的坐标
            this.gatherLayout({
                nodes: nodes1,
                links: links1,
                groups: groups1
            });

            // 初始化是否要重新布局
            var isDagreXy = nodes.concat(groups).some(function (d) {
                return !d.hasOwnProperty('x') || !d.hasOwnProperty('y');
            });
            var isDagrePoints = links.some(function (d) {
                return !d.hasOwnProperty('points');
            });

            this.hierarchyDatas = {
                nodes: [],
                links: [],
                groups: [],
                relation: this.options.relation || []
            };

            if (this.options.layoutOpts.layout === 'gather') {
                layoutData = {
                    svg: svg1,
                    nodes: nodes1,
                    links: links1,
                    groups: groups1,
                    // layout: Object.assign({}, layout),
                };
            } else if (isDagreXy || isDagrePoints) {
                if (layout.ranker === 'network-tree') {
                    layoutData = {
                        svg: svg1,
                        nodes: nodes1,
                        links: links1,
                        groups: groups1,
                        layout: Object.assign({}, layout),
                        rootNodes: this.Tree.getRootNode(nodes1),
                    };

                    // 设置根节点
                    this.Tree.setRootData(nodes1.find(function (d) {
                        return d.data.root;
					}));
                    
                    // 设置原始数据
                    this.Tree.setSourceData({
                        nodes: nodes1,
                        links: links1
                    });
                    // 设置层级数据
					that.Tree.setAssistDepthNodes(nodes1);

                    dagreData = this.layout('init');

                } else {
                    layoutData = {
                        svg: svg1,
                        nodes: nodes1,
                        links: links1,
                        groups: groups1,
                        layout: Object.assign({}, layout),
                    };

                    dagreData = this.layout(layoutData);
                }
            } else {
				layoutData = {
					svg: svg1,
					nodes: nodes1,
					links: links1,
					groups: groups1,
				};
            }

            if (this.options.initTrans && !this.dagreG) {
                if (layout.ranker === 'network-tree') {
                    this.treeNodes = dagreData.nodes;
                    var rootNode = this.treeNodes.filter(function (d) {
                        return d.data.data.root;
                    });
                    this.Node.position(rootNode, 'init');
                    this.hierarchyDatas.nodes.push(rootNode.map(function (d) {
                        return d.data;
                    }));
                    layoutData.nodes = this.hierarchyDatas.nodes[this.hierarchyDatas.nodes.length - 1] || [];
                    layoutData.links = this.hierarchyDatas.links[this.hierarchyDatas.links.length - 1] || [];

                    this.hierarchy(this.hierarchyDatas, 'init');

                } else {
                    this.dagreG = dagreData.g;
                    var nodes2 = this.Dagre.getSource(this.dagreG).map(function (v) {
                        return that.Dagre.getNode(v);
                    });
                    this.hierarchyDatas.nodes.push(nodes2.map(function (d) {
                        return d.data;
                    }));
                    layoutData = {
                        svg: svg1 || {},
                        nodes: this.hierarchyDatas.nodes[this.hierarchyDatas.nodes.length - 1] || [],
                        groups: this.hierarchyDatas.groups[this.hierarchyDatas.groups.length - 1] || [],
                        links: this.hierarchyDatas.links[this.hierarchyDatas.links.length - 1] || [],
                    };
                    this.hierarchy(this.hierarchyDatas, 'init');
                }
            } else {
                // this.Node.position(dagreData.nodes, 'init');
                // this.Link.position(dagreData.links, 'init');
            }

            // layoutData.nodes = layoutData.nodes.filter(function (d) {
            //     return dagreData.nodes.some(function (a) {
            //         return a.data === d;
            //     })
            // });
            // layoutData.links = layoutData.links.filter(function (d) {
            //     return dagreData.links.some(function (a) {
            //         return a.data === d;
            //     })
            // });

            // this.layout(layoutData);
            console.log(layoutData);
            this.drawData(layoutData, 'init');
            // this.Svg.center(0);

            this.hook('loadEnd');
            typeof this.options.fromJsonEnd === 'function' && this.options.fromJsonEnd(this.Svg.port);
            return this;
        },
        hierarchy: function (data, action) {
            var that = this;
            var isOne = false;
            var isOne1 = false;

            if (this.options.layoutOpts.ranker === 'network-tree') {
                this.Node.transitionEnd = function () {
                    if (isOne) return;
                    isOne = true;
                    that.Svg.center();

                    var oldNodes = that.hierarchyDatas.nodes.length && that.hierarchyDatas.nodes.reduce(function (a, b) {
                        return a.concat(b);
                    }) || [];

                    var newNodes = that.hierarchyDatas.nodes[that.hierarchyDatas.nodes.length - 1].reduce(function (a, b) {
                        b.in.forEach(function (c) {
                            if (!oldNodes.includes(c.source) && !a.includes(c.source)) a.push(c.source);
                        });
                        b.out.forEach(function (c) {
                            if (!oldNodes.includes(c.target) && !a.includes(c.target)) a.push(c.target);
                        });
                        return a;
                    }, []);

                    that.Tree.setAssistDepthNodes(oldNodes.concat(newNodes));

                    var dagreData = that.layout(action);

                    var oldLinks = that.hierarchyDatas.links.length && that.hierarchyDatas.links.reduce(function (a, b) {
                        return a.concat(b);
                    }) || [];

                    var newLinks = dagreData.links.map(function (d) {
                        return d.data;
                    }).filter(function (d) {
                        return !oldLinks.includes(d);
                    });

                    // if (newNodes.length === 0 && newLinks.length === 0) {
                    //     return;
                    // }


                    that.hierarchyDatas.links.push(newLinks);

                    var layoutData = {
                        nodes: oldNodes,
                        links: oldLinks.concat(newLinks)
                    };

                    that.drawData(layoutData, 'init');

                    that.hierarchyDatas.nodes.push(newNodes);

                    isOne1 = false;
                };
                this.Link.transitionEnd = function () {
                    if (isOne1) return;
                    isOne1 = true;
                    var oldNodes = that.hierarchyDatas.nodes.length && that.hierarchyDatas.nodes.reduce(function (a, b) {
                        return a.concat(b);
                    }) || [];

                    // 节点连出线
                    var oldLinks = that.hierarchyDatas.links.length && that.hierarchyDatas.links.reduce(function (a, b) {
                        return a.concat(b);
                    }) || [];

                    var layoutData = {
                        nodes: oldNodes,
                        groups: that.hierarchyDatas.groups.length && that.hierarchyDatas.groups.reduce(function (a, b) {
                            return a.concat(b);
                        }) || [],
                        links: oldLinks
                    };
                    that.Svg.center();
                    that.drawData(layoutData, 'init');

                    isOne = false;
                }
            } else {
                this.Node.transitionEnd = function () {
                    if (isOne) return;
                    isOne = true;

                    var oldNodes = that.hierarchyDatas.nodes.reduce(function (a, b) {
                        return a.concat(b);
                    }) || [];
                    var newNodes = that.hierarchyDatas.nodes[that.hierarchyDatas.nodes.length - 1].reduce(function (a, b) {
                        return a.concat(that.Dagre.getNext(b.uuid, that.dagreG));
                    }, []).map(function (d) {
                        return d.data;
                    });

                    // 去重
                    newNodes.reduceRight(function (a, b, i) {
                        var arr = a.concat(oldNodes).filter(function (c) {
                            return c.data.uuid === b.data.uuid;
                        });
                        if (arr.length > 1) a.splice(i, 1);
                        return a;
                    }, newNodes);

                    var oldLinks = that.hierarchyDatas.links.length && that.hierarchyDatas.links.reduce(function (a, b) {
                        return a.concat(b);
                    }) || [];

                    var newLinks = that.hierarchyDatas.nodes[that.hierarchyDatas.nodes.length - 1].reduce(function (a, b) {
                        return a.concat(that.Dagre.getOutEdges(b.uuid, that.dagreG));
                    }, []).map(function (d) {
                        return d.data;
                    });

                    if (!newNodes.length && !newLinks.length) return;

                    that.hierarchyDatas.links.push(newLinks);

                    var layoutData = {
                        nodes: oldNodes.concat(newNodes),
                        groups: that.hierarchyDatas.groups.length && that.hierarchyDatas.groups.reduce(function (a, b) {
                            return a.concat(b);
                        }) || [],
                        links: oldLinks.concat(newLinks)
                    };
                    that.layout(layoutData);
                    layoutData.nodes = oldNodes;
                    that.drawData(layoutData, 'init');
                    that.Svg.center();
                    that.hierarchyDatas.nodes.push(newNodes);
                    isOne1 = false;
                };

                this.Link.transitionEnd = function () {
                    if (isOne1) return;
                    isOne1 = true;
                    var oldNodes = that.hierarchyDatas.nodes.reduce(function (a, b) {
                        return a.concat(b);
                    }) || [];

                    // 节点连出线
                    var oldLinks = that.hierarchyDatas.links.reduce(function (a, b) {
                        return a.concat(b);
                    }) || [];

                    var layoutData = {
                        nodes: oldNodes,
                        groups: that.hierarchyDatas.groups.length && that.hierarchyDatas.groups.reduce(function (a, b) {
                            return a.concat(b);
                        }) || [],
                        links: oldLinks
                    };
                    that.layout(layoutData);
                    that.drawData(layoutData, 'init');
                    that.Svg.center();
                    isOne = false;
                };
            }

        },
        drawData: function (data, action) {
            if (!data) return;
            if (action === 'reuuid') action = this.guid(3);
            // 添加svg
            data.svg && this.Svg.draw(data.svg, action);

            // 添加node
            data.nodes && this.Node.draw(data.nodes, action);

            // 添加groups
            data.groups && this.Group.draw(data.groups, action);

            // 添加link
            data.links && this.Link.draw(data.links, action);
        },
        layout: function (data, action) {
            var that = this;
            if (!data) data = this.hierarchyDatas;

            Object.assign(this.options.layoutOpts || {}, data && data.layout);

            var dagreData = {};

            if (this.options.layoutOpts.ranker === 'network-tree') {
                dagreData = that.Tree.dataFn(data);
            } else {
                var layoutData = {
                    nodes: data.nodes.map(function (d) {
                        return Object.assign({}, d.data, {
                            width: d.width || d.size,
                            height: d.height || d.size,
                            data: d,
                        });
                    }),
                    links: data.links.map(function (d) {
                        return Object.assign({}, {
                            name: d.uuid,
                            source: d.source.uuid,
                            target: d.target.uuid,
                            data: d
                        });
                    }),
                    groups: data.groups.map(function (d) {
                        return Object.assign({}, d.data, {
                            width: d.width || d.size,
                            height: d.height || d.size,
                            data: d
                        });
                    }),
                    layout: this.options.layoutOpts
                };
                dagreData = this.Dagre.dataFn(layoutData);
            }

            // node position
            dagreData.nodes && this.Node.position(dagreData.nodes);

            // group position
            dagreData.groups && this.Group.position(dagreData.groups);

            // link points
            dagreData.links && this.Link.position(dagreData.links);

            return dagreData;
        },
        toJson: function () {
            var json = {};
            var nodes = this.Node.toJson();
            var groups = this.Group.toJson();
            var links = this.Link.toJson();
            var svg = this.Svg.toJson();
            return Object.assign({}, nodes, groups, links, svg);
        },
        append: function (type, data, action) {
            if (!type) return false;
            if (typeof type === 'string') {
                var vm = this.getVm(type);
                vm.render(data, action);
            } else if (type.type) {
                var vm = this.getVm(type.type);
                vm.render([type], action);
            }
            return this;
        },
        isReuuid: function (id) {
            return Array.isArray(this.nodesData) && this.nodesData.some(function (d) {
                return d.uuid === id;
			});
        },
        guid: function (len, radix) {
            var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
            var uuid = [], i;
            radix = radix || chars.length;

            if (len) {
                // Compact form
                for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random() * radix];
            } else {
                // rfc4122, version 4 form
                var r;
                // rfc4122 requires these characters
                uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
                uuid[14] = '4';

                // Fill in random data.  At i==19 set the high bits of clock sequence as
                // per rfc4122, sec. 4.1.5
                for (i = 0; i < 36; i++) {
                    if (!uuid[i]) {
                        r = 0 | Math.random() * 16;
                        uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
                    }
                }
            }

            var uuidStr = uuid.join('');

            // 重复性判断
            if (this.isReuuid(uuidStr)) this.guid(len, radix);

            return uuidStr
        },
        hook: function (action, d) {
            var that = this;
            Object.keys(this.options.hookMap).forEach(function (key) {
                if (that.options.hookMap[key].events.includes(action)) {
                    that.options.hookMap[key].filter.call(that, function (selection) {
                        that.getVm('svg').el.dispatch(key, {detail: selection});
                    }, d);
                }
            });
        },
        cancelSelected: function (vmArr) {
            var that = this;
            ['node', 'group', 'link'].forEach(function (key) {
                if (vmArr && vmArr.length && !vmArr.includes(key)) return;
                that.getVm(key).getAll('condition', 'selected').dispatch('unselect');
            });
        },
        array: function (data) {
            if (!data) return [];
            else if (!Array.isArray(data)) return [data];
            else return data;
        },
        getRoom: function () {
            return this.getVm('Base');
        },
        getVm: function (type) {
            var that = this;
            if (!type) return null;
            if (this.type === type) return this;
            var vms = this.options ? this.options.vms : this.parent.options.vms;
            if (!vms) vms = this.options.vms = [this];
            var vm = vms.find(function (d) {
                return d.type === type;
            });
            if (vm) return vm;
            else {
                if (typeof exports[type] === 'function') {
                    var inherit = that.getVm(exports[type].inherit) || that;
                    if (inherit) {
                        vm = new exports[type](inherit);
                        that.options.vms.push(vm);
                        return vm;
                    } else {
                        // this.consoleLog(`没有找到对应inherit模块，或者未引入该模块！）`, 'warn');
                        return false;
                    }
                } else {
                    // this.consoleLog(`没有找到对应模块（检查是否未创建 ${type} 模块，或者未引入该模块！）`, 'warn');
					return false;
                }
            }
        },
        getAll: function (type, condition) { // return []
            var res;
            if (!Array.isArray(this.children)) return [];
            res = this.children.reduce(function (a, b) {
                return a.concat(b.getAll ? b.getAll() : []);
            }, []);
            if (type === 'data') res = d3.selectAll(res).data();
            else if (type === 'selection') res = d3.selectAll(res);
            else if (type === 'condition') res = d3.selectAll(res).filter(function (d) {
                return d && d.status && d.status[condition];
            });
            else if (type === 'fields') res = d3.selectAll(res).filter(function (d) {
                if (Array.isArray(condition)) {
                    return condition.some(function (obj) {
                        return Object.keys(obj).every(function (k) {
                            return d.data[k] == obj[k];
                        });
                    });
                } else return Object.keys(condition).every(function (k) {
                    return d && d.data[k] == condition[k];
                });
            });
            else if (type === 'nodeData') res = d3.selectAll(res).filter(function (d) {
                return d[condition];
            });
            return res ? res : [];
        },
        inherit: function (vm) {
            this.parent = vm;
            return this;
        },
        getInheritVm: function (type) {
            if (this.parent) return this.parent;
            var vm = this.getVm(type);
            if (!vm) return new exports[type]();
            return vm;
        },
        subset: function (vm) {
            if (vm) Array.isArray(vm.children) ? vm.children.push(this) : vm.children = [this];
            return this;
        },
        objectGet: function (key, val) {
            var that = this;
            return function () {
                if (this.getSet[key] !== undefined && this.getSet[key] !== null && this.getSet[key] !== NaN) return this.getSet[key];
                return this.getSet[key] || val;
            };
        },
        objectSet: function (val) {
            return function (value) {
                this.value = value || val;
            };
        },
        gatherNodes: function () {
            var arr = [];
            arr.remove = function (d) {
                _.remove(arr, function (dom) {
                    if (dom.nodeType === 1) {
                        var data = d3.select(dom).datum();
                        return data.uuid === d.uuid && data.reuuid === d.reuuid;
					}
                    else return dom.uuid === d.uuid && dom.reuuid === d.reuuid;
                });
            };
            arr.add = function (data) {
				// arrFilter(data);
                if (Array.isArray(data)) {
                    data.forEach(function (d) {
                        arr.push(d);
                    });
                } else {
                    arr.push(data);
				}
            };

            function arrFilter (data) {
                data = Array.isArray(data) ? data : [data];
				for (var i = 0; i < arr.length; i++) {
                    var is = data.some(function (d) {
                        console.log(d, arr[i]);
                        return (d === arr[i]) || (d.uuid === arr[i].uuid);
					});
                    console.log(is);
                    if (is) {
                        arr.splice(i, 1);
                        i--;
                    }
				}
            }

            return arr;
        },
        calculateTextLen: function (text, size) {
            if (!text || !size) return 0;
            var text1 = this.getRoom().calculateText.attr('font-size', size)
                .text(text);
            return text1.node().getBBox();
        },
        createPort: function (nodes, action) {
            var that = this;
            nodes.each(function (d) {
                var port = that.getRoom().options.port[action];
                port && (d.port = new port(this, d));
            });
        },
        selectionDrag: function (dx, dy, data) {
            if (!this.options.selectedDragable) return;
            var selection = this.getRoom().getAll('condition', 'selected');
            selection.each(function (d) {
                if (d === data) return;
                d.x += dx;
                d.y += dy;
                d.xyChange = d.x + '+' + d.y;
            });
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

    exports.version = version;

    global.Topo = topo;
})));