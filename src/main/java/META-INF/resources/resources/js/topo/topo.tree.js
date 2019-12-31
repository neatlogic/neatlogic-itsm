(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {})));
}(this, (function (exports) {
    'use strict';
    var opts = {
        type: 'tree',
        nodeinterval: 160,
        linelength: 200
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

            this.tree = d3.tree()
                .nodeSize([that.options.nodeinterval / 3, that.options.linelength])
                .separation(function (a, b) {
                    return (a.parent == b.parent ? 3 : 2);
                });

            // 加载模块
            this.Util = this.getVm('util');
            this.data = { // 数据中心
                root: '',
                relation: this.options.relation,
                nodes: [],
                links: []
            };
            // 辅助节点数据
            this.assistData = {
				nodes: [],
				depthNodes: [],
                relationNodes: []
            }
            // 原始节点数据
            this.sourceData = {
                nodes: [],
                links: [],
            }

        },
        setRootData: function (data) {
            if (!data) return;
            this.data.root = data;
        },
        setFilterRelation: function (data) {
            if (!Array.isArray(data)) return;
            var that = this;
			that.data.relation.splice(0);
			data.forEach(function (a) {
				!that.options.relation.includes(a) && that.options.relation.push(a);
			});
        },
        setAssistDepthNodes: function (data) {
            if (!data) return;
            this.assistData.depthNodes = data;
        },
        setSourceData: function (data) {
            if (!data) return;
            if (data.nodes && Array.isArray(data.nodes)) {
                this.sourceData.nodes = data.nodes;
            }
			if (data.links && Array.isArray(data.links)) {
				this.sourceData.links = data.links;
			}
        },
        dataFn: function (action) {
            var that = this;
            if (!this.data.root) {
                console.warn('请先设置根节点！');
                return;
			}

            if (action === 'init') {
                // 建立节点的树形结构children
				this.data.root.children = this.getNodeChildren(this.data.root, 'all');
                this.assistData.nodes = [this.data.root];
                this.data.relation = [];
                this.children([this.data.root]);
                this.data.relation = this.options.relation;
            }

            // relation children
            // this.assistData.relationNodes = [this.data.root];
            // this.relationChilden([{parent: this.data.root, children: this.data.root.children.concat(this.data.root.reChildren)}], action);
            this.relationChildren1(this.data.root);
            // tree layout
            var hierarchy = d3.hierarchy(this.data.root);
            this.tree(hierarchy);
            // nodes
            this.data.nodes = hierarchy.descendants();
            // hide nodes
            var hierarchy1 = d3.hierarchy(this.data.root, function (d) {
                return d.children.concat(d.reChildren);
            });
            var nodes1 = hierarchy1.descendants();
			// console.log(action, nodes1.length);
			if ((nodes1.length < 2) && (action !== 'relation')) this.sourceData.links.splice(0);
            var links1 = this.sourceData.links.filter(function (d) {
                return nodes1.some(function (d1) {
                    return d1.data === d.source || d1.data === d.target;
                })
            });
            var hideNodes = nodes1.filter(function (d) {
                return !that.data.nodes.some(function (d1) {
                    return d1.data === d.data;
                })
            });
			this.data.nodes.forEach(function (d) {
				d.data.status.hide = false;
			});

			hideNodes.forEach(function (d) {
                d.data.status.hide = true;
			});

			// this.hideRechildren(this.data.nodes);
            // virtual nodes
            this.getVirtualNodes();
            // show links
            this.data.links = this.getLinks(this.data.nodes);
            this.data.links = this.data.links.filter(function (d) {
                return !d.data.relation.every(function (val) {
                    return that.options.relation.includes(val);
                })
            });

            // hide links
            var hideLinks = this.sourceData.links.filter(function (d) {
                d.status.hide = false;
                return !that.data.links.some(function (d1) {
                    return d1.data === d;
                })
            });
            hideLinks.forEach(function (d) {
                d.status.hide = true;
            });

            // remove node and links
            var removeNodes = this.sourceData.nodes.filter(function (d) {
                return !nodes1.some(function (d1) {
                    return d1.data === d;
                })
            });
            var removeLinks = this.sourceData.links.filter(function (d) {
                return removeNodes.some(function (d1) {
                    return (d1 === d.source) || (d1 === d.target);
                });
            });

            var allNodes = nodes1.map(function (d) {
                return d.data;
			});
            var allLinks = this.data.links.map(function (d) {
				return d.data;
			}).concat(hideLinks);
            // 添加所有节点
            // if (action !== 'relation') {
				// this.sourceData.nodes = allNodes;
				// this.sourceData.links = allLinks;
            // }

            return {
                nodes: this.data.nodes,
                links: this.data.links,
                hideNodes: hideNodes.map(function (d) {
                    return d.data;
                }),
                hideLinks: hideLinks,
                removeNodes: removeNodes,
                removeLinks: removeLinks,
                allNodes: allNodes,
                allLinks: allLinks
            }
        },
        relationChildren1: function (data) {
            var that = this;
            var children = data.children.concat(data.reChildren);
            var childrenlinks = data.in.concat(data.out);
            children.forEach(function (d) {
                var relinks = childrenlinks.find(function (d1) {
                    return d1.source === d || d1.target === d;
                });
                var isRelation = relinks && relinks.relation.every(function (d1) {
                    return that.options.relation.includes(d1);
                }); // 被rechildren
                var i1 = data.children.indexOf(d);
                var i2 = data.reChildren.indexOf(d);
                if (isRelation && i1 !== -1) {
                    var n = data.children.splice(i1,1)[0];
                    data.reChildren.push(n);
                }
                if (!isRelation && i2 !== -1) {
                    var n = data.reChildren.splice(i2,1)[0];
                    data.children.push(n);
                }
                that.relationChildren1(d);
            })
        },
        relationLinks: function (data) {
            return data.filter(function (d) {
                return !d.relation.every(function (val) {
                    return that.options.relation.includes(val);
                })
            })
        },
        hideRechildren: function (data) {
            data.forEach(function (d) {
                var merge = d.data.children.concat(d.data.reChildren);
                var children = d.data.status.hide ? d.data.children.concat(d.data.reChildren) : d.data.reChildren;
                merge.forEach(function (d1) {
                    if (children.includes(d1)) d1.status.hide = true;
                    else d1.status.hide = false;
                })
            })
        },
        getAllNodes: function (relation) {
			return d3.hierarchy(this.data.root).descendants().map(function (d) {
                return d.data;
			});
        },
        relationChilden: function (dataArr, action) {
            var that = this;
            // dataArr 所有同级children和rechildren，=》[[children1], [children2], ...]
            // 1. 同级过滤  ==》 .1 是否与所有上级节点所关联 .2 是否与所有其它同级节点关联
            // 2. reChildren  => 每组children满足上述条件，push到children内，否则push到reChildren内
            // 3. 循环同级children =>所有同级节点的children打包，循环
            // 所有同级节点
            var allChild = dataArr.length && dataArr.reduce(function (a, b) {
                return a.concat(b.children);
            }, []) || [];

            var loopChildren = [];
            // relation children
            dataArr.forEach(function (obj) {
                obj.children.forEach(function (d) {
                    var relationchildren = that.getNodeChildren(d, action);
                    var is = relationchildren.some(function (a) {
                        return that.assistData.relationNodes.includes(a); // 未考虑同级
                    });
                    var index1 = obj.parent.children.indexOf(d);
                    var index2 = obj.parent.reChildren.indexOf(d);
                    if (is) {
                        if (index2 !== -1) {
                            obj.parent.children.push(d);
                            obj.parent.reChildren.splice(index2, 1);
                        }
                    } else {
                        if (index1 !== -1) {
                            obj.parent.reChildren.push(d);
                            obj.parent.children.splice(index1, 1);
                        }
                    }
                    if (is) {
                        loopChildren.push({
                            parent: d,
                            children: d.children.concat(d.reChildren)
                        })
                    }

                })
            });


            if (loopChildren.length) {
                var devChildren = loopChildren.map(function (d) {
                    return d.parent;
                });
                // 所有上级节点
                this.assistData.relationNodes = this.assistData.relationNodes.concat(devChildren);
                this.relationChilden(loopChildren);
            }


            // function findPrev () {
            //
            // }
        },
        getNodeChildren: function (data, action) {
            var that = this;
            return this.nodeChildren(data, action).concat(this.nodeParents(data, action))
        },
        nodeChildren: function (data, action) {
			var that = this;

			return data.out.filter(function (d) {
				return action === 'all' || !that.data.relation.length || (that.data.relation.length && !d.relation.every(function (a) {
					return that.data.relation.includes(a);
				}));
			}).map(function (d) {
				return d.target;
			})
        },
        nodeParents: function (data, action) {
			var that = this;

			return data.in.filter(function (d) {
				return action === 'all' || !that.data.relation.length || (that.data.relation.length && !d.relation.every(function (a) {
					return that.data.relation.includes(a);
				}));
			}).map(function (d) {
				return d.source;
			})
        },
        children: function (parentArr) {
            var that = this;
            var cArr2 = [];
            that.assistData.nodes = that.assistData.nodes.concat(parentArr.filter(function (d) {
                return !that.assistData.nodes.includes(d)
            }));

            parentArr.forEach(function (d) {
                d.children = d.children.filter(function (a) {
                    return !cArr2.includes(a) && (!that.assistData.depthNodes.length || that.assistData.depthNodes.includes(a)) && !that.assistData.nodes.includes(a) && cArr2.push(a);
                });
                d.children.forEach(function (a) {
                    a.children = that.getNodeChildren(a)
                })
            });
            var parentsChildren = parentArr.length && parentArr.reduce(function (a, b) {
                return a.concat(b.children);
            }, []);
            if (parentsChildren.length) {
                this.children(parentsChildren)
            }

        },
        getRootNode: function (data) {
            return data.filter(function (d) {
                return d.data.root;
            })
        },
        getVirtualNodes: function () {
            var depthObj = {};
            this.data.nodes.forEach(function (d) {
                var key = 'depth' + d.depth;
                if (!depthObj.hasOwnProperty(key)) {
                    depthObj[key]  = [];
                }
                depthObj[key].push(d);
            });
            Object.keys(depthObj).forEach(function (key) {
                var vArr = depthObj['v' + key] = [];
                var marginWidth = 200;
                vArr.push({
                    width: marginWidth,
                    height: depthObj[key][0].data.height,
                    x: depthObj[key][0].x - marginWidth / 2,
                    y: depthObj[key][0].y,
                    shape: depthObj[key][0].data.shape
                });
                depthObj[key].reduce(function (a,b) {
                    vArr.push({
                        width: b.x - a.x - a.data.width / 2 - b.data.width / 2 - 10,
                        height: (b.y - a.y) || a.data.height,
                        x: (b.x + a.x) / 2,
                        y: (b.y + a.y) / 2,
                        shape: a.data.shape
                    });
                    return b;
                });
                vArr.push({
                    width: marginWidth,
                    height: depthObj[key][depthObj[key].length - 1].data.height,
                    x: depthObj[key][depthObj[key].length - 1].x + marginWidth / 2,
                    y: depthObj[key][depthObj[key].length - 1].y,
                    shape: depthObj[key][depthObj[key].length - 1].data.shape
                });
                // 细化区间
                // var l = vArr.length;
                // var offset = 100;
                // for (var i = 0; i < vArr.length; i++) {
                //     var count = Math.floor(vArr[i].width / offset);
                //     if (count >= 2) {
                //         var x0 = vArr[i].x - vArr[i].width / 2;
                //         var y0 = vArr[i].y;
                //         var ox = vArr[i].width / count;
                //         for (var j = 0; j < count; j++) {
                //             vArr.splice(i + j, 0, Object.assign({}, vArr[i], {
                //                 x: x0 + ox / 2 + ox * j,
                //                 y: y0,
                //                 width: ox
                //             }))
                //         }
                //         vArr.splice(i, 1);
                //         i += (count - 1);
                //     }
                // }
            });
            this.data.depth = depthObj;
        },
        getLinks: function (nodes) {

            var that = this;
            return this.sourceData.links.map(function (d) {
                var source = nodes.find(function (a) {
                    return a.data.uuid === d.source.uuid
                });
                var target = nodes.find(function (a) {
                    return a.data.uuid === d.target.uuid
                });

                if (!source || !target) {
                    // console.warn('有未识别的线！！');
                    return false;
                }

                var points = [];
                points.push({
                    x: source.x,
                    y: source.y
                });

                points.push({
                    x: target.x,
                    y: target.y
                });

                if (source.depth === target.depth) {
                    d.mode = 'curve';
                    var sIndex = nodes.indexOf(source);
                    var tIndex = nodes.indexOf(target);
                    var offset = 2 * (tIndex - sIndex) / Math.abs(tIndex - sIndex);
                    if (Math.abs(tIndex - sIndex) === 1) {
                        points[0].x += offset;
                        points[points.length - 1].x -= offset;
                    } else {
                        var offsetx = (target.x - source.x) * 0.1;
                        var offsety = (target.y - source.y) * 0.1;
                        var abs = Math.abs(offsetx);
                        offsetx = abs > 50 ? offsetx : offsetx / abs * 50;
                        offsetx = abs > 100 ? offsetx / abs * 100 : offsetx;
                        points[0].y += offset;
                        points[points.length - 1].y += offset;
                        points.splice(points.length - 1, 0, {
                            x: source.x + offsetx,
                            y: source.y + offsetx
                        });
                        points.splice(points.length - 1, 0, {
                            x: target.x - offsetx,
                            y: target.y + offsetx
                        });
                    }
                } else if (Math.abs(target.depth - source.depth) > 1) {
                    var x0 = source.x;
                    var y0 = source.y;
                    var x1 = target.x;
                    var y1 = source.y;
                    var dx = x1 - x0;
                    var dy = y1 - y0;
                    var dh = (target.depth - source.depth) / Math.abs(target.depth - source.depth);

                    var cx = source.x;
                    var cy = source.y;
                    var ch = source.depth;

                    if (points[0].y > points[1].y) {
                        points[0].y -= 2;
                        points[1].y += 2;
                    } else {
                        points[0].y += 2;
                        points[1].y -= 2;
                    }

                    for (var i = 0; i < Math.abs(target.depth - source.depth) - 1; i++) {
                        ch = ch + dh;
                        var disx = cx * .5 + x0 * .2 + x1 * .3;
                        var arr = that.data.depth['vdepth' + (ch)].map(function (d) {
                            return d;
                        }).sort(function (a, b) {
                            return Math.abs(a.x - disx) - Math.abs(b.x - disx);
                        });
                        var p1 = {};
                        var pt;
                        pt = arr[0];
                        p1.x = arr[0].x;
                        p1.y = arr[0].y;

                        var p2 = that.Util.shapeEdge(pt, source);
                        p1.x = p2.x;
                        p1.y += p1.y - p2.y;
                        points.splice(points.length - 1, 0, p2, p1);
                        cx = p1.x;
                        cy = pt.y;
                    }
                }


                return {
                    data: d,
                    source: source,
                    target: target,
                    points: points
                }
            }).filter(function (d) {
                return d;
            })
        },
        getNext: function (data) {

        }

    });

})));