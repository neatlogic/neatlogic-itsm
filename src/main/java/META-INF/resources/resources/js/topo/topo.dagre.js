(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {})));
}(this, (function (exports) {
    'use strict';
    var opts = {
        type: 'dagre',
    };
    // graph options
    var graphOpts = {
        multigraph: true,
        compound: true
    };
    // dagre options
    var dagreOpts = {
        rankdir: 'TB',  // TB, BT, LR, RL
        // align: 'DR', // UL, UR, DL, or DR, where U = up, D = down, L = left, and R = right.
        nodesep: 100,
        edgesep: 20,
        ranksep: 130,
        marginx: 0,
        marginy: 0,
        // acyclicer: 'greedy',
        ranker: 'network-simplex', // network-simplex || tight-tree || longest-path
    };
    // 节点相关配置
    var nodeopts = {
        width: 50,
        height: 50
    };
    // 连线相关配置
    var edgeopts = {
        minlen: 1,
        labeloffset: 0,
        // labelpos: 'c', // Where to place the label relative to the edge. l = left, c = center r = right.
    };

    // 处理dagre布局
    var Tag = exports[opts.type] = exports.Base.extend();
    Tag.inherit = 'Base';
    Tag.inject({
        init: function (vm) {
            var that = this;
            this
                .inherit(vm)
                .subset(vm)
                .instanceInject({
                    options: Object.assign({}, vm.options, opts),
                    container: vm.el
                });

            Object.defineProperty(this, 'el', {
                get: function () {
                    return that.container.selectAll('g.' + that.options.nodeClass);
                }
            });

            // 加载模块
            this.Util = this.getVm('util');
            // this.Node = this.getVm('node');

            // 添加布局算法模块
            this.dagre = dagreD3.dagre;
            this.graphlib = dagreD3.graphlib;
            if (!this.dagre) this.consoleLog('log', 'dagre插件未引入！');

            // 创建实例
            this.g = new this.graphlib.Graph(graphOpts);

        },
        dataFn: function (json) {
            var that = this;
            var nodes = json.nodes, links = json.links, groups = json.groups;

            var gopts = Object.assign({}, dagreOpts, json.layout);

            if (!nodes || !links) {
                this.consoleLog('node data为空！', 'warn');
                return;
            }

            // 创建实例
            this.g = new this.graphlib.Graph(graphOpts);

            this.g.setGraph(gopts);

            // 添加节点
            this.addNodes(nodes);
            // children
            this.addParentsByChildren(nodes)

            // 添加线条
            this.addLinks(links);

            // 添加group
            this.addParents(groups);

            // 添加层级布局
            var virtualNodes = [];
            if (gopts.ranker === 'depth') this.hierarchical(nodes);
            
            // 添加虚拟节点(删除跨级节点连线、创建虚拟节点、创建虚拟节点连线)
            // var aa = this.forwardAppendVirtualNode();
            // var aa1 = this.backwardsAppendVirtualNode();
            // console.log(aa, aa1);

            // 计算布局（核心）
            this.dagre.layout(this.g);

            // 虚拟节点合并(添加跨级节点连线、合并虚拟节点、删除创建的虚拟节点)
            this.addVirtualNodes(virtualNodes);

            // test
            // this.g.setParent('D', 'E');

            // console.log(this.g.nodes(), this.g.parent('D'));

            // 返回数据
            var nodesData = nodes.map(function (d) {
                return Object.assign({uuid: d.uuid}, that.g.node(d.uuid));
            });

            var linksData = links.map(function (d) {
                return that.g.edge(d.source, d.target, d.name);
            });
            var groupsData = groups.map(function (d) {
                return that.g.node(d.uuid);
            });

            // console.log(11111, this.g.nodes().map((v)=>this.g.node(v)));
            // console.log(11111, this.graphlib.alg.topsort(this.g).map((v)=>this.g.node(v)));
            // var aa = this.graphlib.alg.floydWarshall(this.g, function(e) { console.log(e); return  1 || g.edge(e); });
            var bb = this.graphlib.alg.components(this.g);

            // console.log(nodesData, linksData, groups, groupsData, that.g.edges());

            return {
                nodes: nodesData,
                links: linksData,
                groups: groupsData,
                g: this.g
            }
        },
        getComponents: function (g) {
            return this.graphlib.alg.components(g || this.g);
        },
        getNext: function (v, g) {
            var G = g || this.g;
            return G.successors(v).map(function (d) {
                return G.node(d);
            });
        },
        getNode: function (v, g) {
            return (g || this.g).node(v);
        },
        getSource: function (g) {
            return (g || this.g).sources();
        },
        getOutEdges: function (v, g) {
            var that = this;
            var G = g || this.g;
            return G.outEdges(v).map(function (d) {
                return G.edge(d.v, d.w, d.name);
            });
        },
        addNodes: function (nodes, dagre) {
            var that = this;
            var G = dagre || this.g;
            if (Array.isArray(nodes)) {
                nodes.forEach(function (d) {
                    G.setNode(d.uuid, Object.assign({}, nodeopts, d));
                });
            } else {
                G.setNode(nodes.uuid, Object.assign({}, nodeopts, nodes));
            }
            return G;
        },
        addLinks: function (links) {
            var that = this;
            if (Array.isArray(links)) {
                links.forEach(function (d) {
                    that.g.setEdge(d.source, d.target, Object.assign({}, edgeopts, d), d.name);
                });
            } else {
                that.g.setEdge(links.source, links.target, Object.assign({}, edgeopts, links), links.name);
            }
        },
        addParents: function (groups) {
            var that = this;
            if (Array.isArray(groups)) {
                groups.forEach(function (d) {
                    if (Array.isArray(d.contain)) {
                        that.g.setNode(d.uuid, Object.assign({}, nodeopts, d));
                        d.contain.forEach(function (a) {
                            that.g.setParent(a, d.uuid);
                        })
                    }
                });
            }
        },
        addParentsByDepth: function (nodes) {
            var that = this;
            var depthGroups = {};
            if (Array.isArray(nodes)) {
                nodes.forEach(function (d) {
                    if (d.hasOwnProperty('depth')) {
                        if (!Array.isArray(depthGroups['depth' + d.depth])) depthGroups['depth' + d.depth] = [];
                        depthGroups['depth' + d.depth].push(d)
                    }
                })
            }
            Object.keys(depthGroups).forEach(function (k) {
                that.addParents([{
                    uuid: k,
                    contain: depthGroups[k].map(function (d) {
                        return d.uuid;
                    })
                }])
            })
        },
        addParentsByChildren: function (nodes) {
            var that = this;
            nodes.forEach(function (d) {
                d.children && console.log(d.children, d)
                Array.isArray(d.children) && d.children.length && that.addParents([{
                    uuid: 'children' + d.uuid,
                    contain: d.children.map(function (a) {
                        return a.uuid;
                    }).push(d.uuid)
                }])
            })
        },
        hierarchical: function (nodes) {
            var that = this;
            if (Array.isArray(nodes)) {
                // 层级条件判断
                var isDepth = nodes.some(function (d) {
                    return !d.hasOwnProperty('depth');
                })
                var G = this.addNodes(nodes, new this.graphlib.Graph(graphOpts));
                var tarjan = that.graphlib.alg.tarjan(G);
                if (tarjan.length && isDepth) {
                    console.log('层级布局条件不成立——存在回环结构！');
                    return false;
                } else {
                    that.addParentsByDepth(nodes);
                }
            }
        },
        getNodes: function (dagre, nodes) {
            var G = dagre || this.g;
            var nodes = nodes || G.nodes();
            return nodes.map(function (d) {
                return G.getNode(d.uuid);
            })
        },
        addVirtualNodes: function (vNodes, dagre) {
            var that = this;
            var G =  dagre || this.g;
            if (Array.isArray(vNodes)) {
                vNodes.forEach(function (o) {
                    if (o.sedge) {
                        // 添加跨级节点连线
                        G.setEdge(o.source.uuid, o.target.uuid, Object.assign({}, edgeopts, o.sedge), o.sedge.name);
                        // 合并虚拟节点
                        var points = [];
                        o.vedges.forEach(function (d) {
                            points = points.concat(G.edge(d.source, d.target, d.name).points)
                        });
                        G.edge(o.source.uuid, o.target.uuid, o.sedge.name).points = points;
                    }
                    // 删除创建的虚拟节点
                    o.vnodes.forEach(function (d) {
                        G.removeNode(d.uuid);
                    });
                    o.vedges.forEach(function (d) {
                        G.removeEdge(d.source, d.target, d.name);
                    })
                });
            }
        },
        forwardAppendVirtualNode: function () {
            var that = this;
            // 添加虚拟节点(删除跨级节点连线、创建虚拟节点、创建虚拟节点连线)
            var vNodes = [], allNodes = this.g.nodes().map(function (e) {
                return that.g.node(e);
            }).filter(function (a) {
                return a;
            });

            var minDepth = d3.min(allNodes, function (a) {
                return a.depth;
            });

            allNodes.forEach(function (n) {
                if (!n.hasOwnProperty('depth')) return; // 节点不存在层级配置

                var sNode = n;
                var sNodePredecessors = that.g.predecessors(sNode.uuid).map(function (e1) {
                    return that.g.node(e1);
                });
                var preNode = sNodePredecessors.length && sNodePredecessors.reduce(function (a, b) {
                    return a.depth > b.depth ? b : a;
                });

                var vnodes = [], vedges = [];

                // 判断是否有必要添加虚拟节点
                // 起始节点和结束节点之间最短的层级差，大于2才添加
                // 向后添加原则

                var preDepth = preNode ? preNode.depth : (minDepth - 1);

                if (sNode.depth - preDepth > 1) {
                    for (var i = sNode.depth - 1; i > preDepth; i--) {
                        var o = {
                            uuid: 'V-' + sNode.uuid + '-' + that.guid(5),
                            width: 0,
                            height: 0,
                            depth: i
                        };
                        vedges.push(Object.assign({}, edgeopts, {
                            target: i === sNode.depth - 1 ? sNode.uuid : vnodes[vnodes.length - 1].uuid,
                            source: o.uuid,
                        }));
                        vnodes.push(o);
                    }
                    if (preNode) {
                        vedges.push(Object.assign({}, edgeopts, {
                            source: preNode.uuid,
                            target: vnodes[vnodes.length - 1].uuid,
                        }));
                    }

                    // 添加虚拟节点数据、虚拟连线数据

                    var o = {
                        source: preNode,
                        target: sNode,
                        sedge: that.g.outEdges(preNode.uuid, sNode.uuid)[0],
                        vnodes: vnodes,
                        vedges: vedges
                    };

                    vNodes.push(o);

                    // 删除当前连接线数据
                    o.sedge && that.g.removeEdge(o.sedge.v, o.sedge.w, o.sedge.name);

                    //添加虚拟节点
                    vnodes.forEach(function (d) {
                        that.g.setNode(d.uuid, Object.assign({}, nodeopts, d));
                    });
                    //添加虚拟连线
                    vedges.forEach(function (d) {
                        that.g.setEdge(d.source, d.target, Object.assign({}, edgeopts, d), d.source + '-' + d.target);
                    });
                }
            });
            return vNodes;
        },
        backwardsAppendVirtualNode: function () {
            var that = this;
            var vNodes = [], allNodes = this.g.nodes().map(function (e) {
                return that.g.node(e);
            }).filter(function (a) {
                return a;
            });

            var maxDepth = d3.max(allNodes, function (a) {
                return a.depth || 0;
            });
            var minDepth = d3.min(allNodes, function (a) {
                return a.depth;
            });
            allNodes.forEach(function (n) {
                if (!n.hasOwnProperty('depth')) return; // 节点不存在层级配置

                var sNode = n;
                var sNodeSuccessors = that.g.successors(sNode.uuid).map(function (e1) {
                    return that.g.node(e1);
                });
                var tNode = sNodeSuccessors.length && sNodeSuccessors.reduce(function (a, b) {
                    return a.depth > b.depth ? b : a;
                });

                var vnodes = [], vedges = [];

                // 判断是否有必要添加虚拟节点
                // 起始节点和结束节点之间最短的层级差，大于2才添加
                // 向后添加原则

                var tDepth = tNode ? tNode.depth : (maxDepth + 1);

                if (tDepth - sNode.depth > 1) {
                    for (var i = sNode.depth + 1; i < tDepth; i++) { // 向后添加
                        var o = {
                            uuid: 'V-' + sNode.uuid + '-' + that.guid(5),
                            width: 0,
                            height: 0,
                            depth: i
                        };

                        vedges.push(Object.assign({}, edgeopts, {
                            source: vnodes.length ? vnodes[vnodes.length - 1].uuid : sNode.uuid,
                            target: o.uuid,
                            name: 'v',
                        }));
                        vnodes.push(o);
                    }
                    if (tNode) { // 存在目标节点
                        vedges.push(Object.assign({}, edgeopts, {
                            source: vnodes[vnodes.length - 1].uuid,
                            target: tNode.uuid,
                            name: 'v',
                        }));
                    } else { // 判断是否存在前置节点
                        if (sNode.depth === minDepth) return;
                        var sNodeInEdges = that.g.inEdges(sNode.uuid);
                        if (!sNodeInEdges.length) {
                            for (var i = sNode.depth - 1; i >= minDepth; i--) {
                                var o = {
                                    uuid: 'V-' + sNode.uuid + '-' + that.guid(5),
                                    width: 0,
                                    height: 0,
                                    depth: i
                                };
                                vedges.push(Object.assign({}, edgeopts, {
                                    target: i === sNode.depth - 1 ? sNode.uuid : vnodes[vnodes.length - 1].uuid,
                                    source: o.uuid,
                                    name: 'v',
                                }));
                                vnodes.push(o);
                            }
                        }
                    }

                    // 添加虚拟节点数据、虚拟连线数据

                    var o = {
                        source: sNode,
                        target: tNode,
                        sedge: that.g.outEdges(sNode.uuid, tNode.uuid)[0],
                        vnodes: vnodes,
                        vedges: vedges
                    };

                    vNodes.push(o);

                    // 删除当前连接线数据
                    o.sedge && that.g.removeEdge(o.sedge.v, o.sedge.w, o.sedge.name);

                    //添加虚拟节点
                    that.addNodes(vnodes);
                    //添加虚拟连线
                    that.addLinks(vedges);

                }
            });
            return vNodes;
        },
        draw: function (data) {
            var that = this;

        },
    });

})));