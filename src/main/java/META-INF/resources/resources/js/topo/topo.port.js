(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {}), global));
}(this, (function (exports, global) {
    function port (VM) {
        VM.options.port = this;
        var baseVm = VM.getRoom();
        var svgVm = VM.getVm('svg');
        var Tip = VM.getVm('tip');

        this.svg = function () {}
        this.svg.prototype = {
			constructor: this.svg,
			base: baseVm,
			fromJson: function (data) {
				baseVm.fromJson(data);
				return this;
			},
			toJson: function () {
				return baseVm.toJson();
			},
			getConWidthHeight: function () {
				return {
					width: svgVm.container.node().offsetWidth,
					height: svgVm.container.node().offsetHeight,
				}
			},
			// create: function (data) {
			//     if (data.hasOwnProperty('x') && data.hasOwnProperty('y')) {
			//         var position = svgVm.positionTransform([data.x, data.y], 'invert');
			//         data.x = position[0];
			//         data.y = position[1];
             //    }
			// 	baseVm.append(data);
			// 	return this;
			// },
			positionTransform: function (data) {
				if (data.hasOwnProperty('x') && data.hasOwnProperty('y')) {
					var position = svgVm.positionTransform([data.x, data.y], 'invert');
					data.x = position[0];
					data.y = position[1];
			   }
			   return data;
			},
			create: function (data) {
				var nodes = data.nodes;
				var groups = data.groups;
				var links = data.links;
				var reuuid = baseVm.guid(5);
				var nodes1 = baseVm.getVm('node').dataFn(nodes || [], reuuid);
				var groups1 = baseVm.getVm('group').dataFn(groups || [], reuuid);
				var links1 = baseVm.getVm('link').dataFn(links || [], reuuid);
				var topoData = {
                    nodes: nodes1 || [],
                    groups: groups1 || [],
					links: links1 || [],
                };
				baseVm.gatherLayout(topoData);
				baseVm.drawData(topoData, 'append');
				return {
					nodes: topoData.nodes.map(d => d.port),
					links: topoData.links.map(d => d.port),
					groups: topoData.groups.map(d => d.port),
				}
			},
			getAlertNodes: function () {
				return baseVm.getAll('nodeData', 'alert').data().map(function (d) {
					return d.port;
				});
			},
			getNodeByConfig: function (obj, action) {
				if (obj && typeof obj === 'object') {
					return baseVm.getAll('fields', obj, action).data().map(function (d) {
						return d.port;
					});
				}
			},
			getSelected: function () {
                return baseVm.getAll('condition', 'selected').data().map(function (d) {
                    return d.port;
				});
			},
            getConfig: function (fields) {
				if (fields) return svgVm.el.datum()[fields];
                return svgVm.el.datum().data;
			},
			setConfig: function (data, action) {
				var svgData = svgVm.el.datum();
				if (typeof data === "object" && !Array.isArray(data)) {
					if (!action) Object.assign(svgData.data, data);
					Object.keys(data).forEach(function (key) {
						if (key === 'name') return false;
						if (svgData.hasOwnProperty(key) && key !== 'data') {
							svgData[key] = data[key];
						}
					})
				}
				return this;
			},
			onEvent: function (key, fn) {
				var that = this;
				svgVm.el.on(key, function () {
					var detail = d3.event.detail;
					fn(detail && detail.data().map(function (d) {
						return d.port;
					}));
				})
				return this;
			},
			center: function (time) {
				svgVm.center(time);
				return this;
			},
			getAlertNodes: function () {
				return baseVm.getAll('nodeData', 'alert').data().map(function (d) {
					return d.port;
				});
			},
			getNodeByKey: function (obj) {
				if (obj && typeof obj === 'object') {
					return baseVm.getAll('fields', obj).data().map(function (d) {
						return d.port;
					});
				}
			},
			getNodes: function (arr) {
				var nodes = baseVm.getVm('node').getAll('data');
				return nodes.filter(function (d) {
					if (Array.isArray(arr)) return arr.some(function (a) {
						return d.uuid === a;
                    });
					return true;
                }).map(function (d) {
                    return d.port;
                });
			},
			setRelationList: function (data) {
				baseVm.getVm('tree').setFilterRelation(data);
			},
			renderByRelation: function (relation) {
				if (Array.isArray(relation)) {
					baseVm.getVm('tree').setFilterRelation(relation);
                    var dagreDatas = baseVm.layout('relation');

                    var nodes = dagreDatas.nodes.map(function (d) {
						return d.data;
					}).filter(function (d) {
						return d;
					});

					var links = dagreDatas.links.map(function (d) {
						return d.data;
					}).filter(function (d) {
						return d;
					});
                    var drawData = {
                        nodes: nodes.concat(dagreDatas.hideNodes),
                        links: links.concat(dagreDatas.hideLinks)
                    };

					baseVm.drawData(drawData, 'relation');
				}
            },
			getRelationList: function (action) {
				var arr = [];
				var Tree = baseVm.getVm('tree');
				var links = Tree.sourceData.links.map(function (d) {
					return d.data;
                });
				var relation = baseVm.options.relation;
				if (action === 'unrelation') return relation;
				var relationList = [];
				links.forEach(function (d) {
					d.relation.forEach(function (d1) {
						if (!relationList.includes(d1)) relationList.push(d1);
					})
				});
				return {
					unrelation: relation,
					relationList: relationList
				};
            },
            getGroups: function () {
				return baseVm.getVm('group').getAll('data').map(function (d) {
					return d.port;
                })
            },
			getGroupByUUid: function (uuid) {
				var group = baseVm.getVm('group').getAll('data').find(function (d) {
					return d.uuid === uuid;
				});
				return group && group.port;
			},
			isExistLink: function (data) {
				var links = baseVm.getVm('link').getAll('data');
				return links.some(function (d) {
					return d.source.uuid === data.source && d.target.uuid === data.target;
				})
			},
        };
        // node
        this.node = function (dom, d) {
            this.el = dom;
            this.data = d;
            this.type = 'node';
        }
        this.node.prototype = {
            constructor: this.node,
            remove: function () {
				d3.select(this.el).dispatch('remove');
				return this;
			},
			getConfig: function (fields) {
            	if (fields) return this.data[fields];
				return this.data.data;
			},
			getGroups: function () {
            	return this.data.groups.map(function (d) {
					return d.port;
            	});
			},
			storageConfig: function (config) {
            	if (!config) return this;
				Object.assign(this.data.storage, config);
				return this;
			},
			getStorageConfig: function (fields) {
            	if (fields) return this.data.storage[fields];
            	return this.data.storage;
			},
			setConfig: function (data, action) {
            	var that = this;
            	// console.log('node config:', data);
            	if (typeof data === "object" && !Array.isArray(data)) {
            		if (!action) Object.assign(this.data.data, data);
					Object.keys(data).forEach(function (key) {
						if (that.data.hasOwnProperty(key) && key !== 'data') {
							that.data[key] = data[key];
						}
					})
				}
				return this;
			},
			getLinks: function () {
				return baseVm.getVm('link').getLinkByNode(this.data).data().map(function (d) {
					return d.port;
				});
			},
			getInLinks: function () {
            	return this.data.in.slice(0).map(function (d) {
            		return d.port;
				});
			},
			getOutLinks: function () {
				return this.data.out.slice(0).map(function (d) {
					return d.port;
				});
			},
			appendParents: function (data) {
            	var that = this;
            	if (!data) return;
				baseVm.getVm('node').appendParents(this.data, data);
			},
			removeParents: function (data, actionData) {
            	// console.log('remove parents');
				baseVm.getVm('node').removeParents(this.data);
			},
			appendChildren: function (data, actionData) {
				if (!data) return this;
				baseVm.getVm('node').appendChildren(this.data, data);
            },
			removeChildren: function (data, actionData) {
				baseVm.getVm('node').removeChildren(this.data);
			},
			showTip: function (action, fn) {
            	var that = this;
            	var actionData = ['ru', 'rd'].includes(action) && d3.select(this.el).selectAll('.'+action).datum() || action;
				new Promise(function (resolve, reject) {
					Tip.current = that;
					Tip.tip(that.data, actionData);
					(typeof fn === 'function') && fn(resolve, reject)
                }).then(function (data) {
                    Tip.show(data);
                })
            },
            hideTip: function () {
            	if (Tip.current === this) Tip.hide();
            },
			getStatus: function () {
				return this.data.state;
			},
			setStatus: function (data) {
				d3.select(this.el).dispatch('setStatus', {detail: data});
            },
			setSelectable: function (b) {
				this.data.selected.selectable = Boolean(b);
				return this;
			},
			isSelected: function () {
            	return this.data.status.selected;
			},
			isShow: function () {
            	return !this.data.status.hide;
			},
			selected: function () {
				d3.select(this.el).dispatch('selected');
			},
			unselected: function () {
				d3.select(this.el).dispatch('unselect');
			},
            getChildren: function () {
				return this.data.out.map(function (d) {
					return d.target;
                });
            },
            highlight: function (action) {
				if (action === 'rd') {
					d3.select(this.el).dispatch('highlight', {detail: {action:'rd'}});
				} else if (action === 'ru') {
                    d3.select(this.el).dispatch('highlight', {detail: {action: 'ru'}});
                }
            },
            canHighlight: function (action) {
                if (action === 'rd') {
                    d3.select(this.el).dispatch('canHighlight', {detail: {action:'rd'}});
                } else if (action === 'ru') {
                    d3.select(this.el).dispatch('canHighlight', {detail: {action: 'ru'}});
                }
            },
            getGroup: function () {
				return this.data.groups.map(function (group) {
					return group.port;
                })
            },
			setBtns: function (btns) {
				d3.select(this.el).dispatch('setBtns', {detail: {btns: btns || []}});
			},
			showBtns: function () {
				d3.select(this.el).dispatch('showBtns');
			},
			hideBtns: function () {
				d3.select(this.el).dispatch('hideBtns');
			},
			setInfoBtns: function (btns) {
                d3.select(this.el).dispatch('setInfoBtns', {detail: {btns: btns || []}});
			},
        };

        // link
        this.link = function (dom, d) {
			this.el = dom;
			this.data = d;
			this.type = 'link';
        }
        this.link.prototype = {
            constructor: this.link,
			remove: function () {
				d3.select(this.el).dispatch('remove');
				return this;
			},
			getConfig: function () {
				return this.data.data;
			},
			setConfig: function (data, action) {
				var that = this;
				if (typeof data === "object" && !Array.isArray(data)) {
					if (!action) Object.assign(this.data.data, data);
					Object.keys(data).forEach(function (key) {
						if (key === 'dasharray') {
							if (data[key] === 'true') {
								that.data.strokeDasharray = '4,3'
							} else {
								that.data.strokeDasharray = '0'
							}
						} else if (key === 'width') {
							that.data.strokeWidth = data.width;
						} else if (key === 'animation') {
							if (data[key] === 'true') that.data.animation = true;
							else that.data.animation = false;
						} else if (that.data.hasOwnProperty(key) && key !== 'data' && key !== 'source' && key !== 'target') {
							that.data[key] = data[key];
						}
					})
				}
				return this;
			},
			setMarker: function (data) {
            	var that = this;
            	Object.keys(data).forEach(function (key) {
					if (that.data.marker.hasOwnProperty(key)) that.data.marker[key] = data[key];
                });
			},
            getSource: function () {
				return this.data.source.port;
            },
			getTarget: function () {
                return this.data.target.port;
            }
        }

		// group
		this.group = function (dom, d) {
			this.el = dom;
			this.data = d;
			this.type = 'group';
		};
		this.group.prototype = {
			constructor: this.group,
			remove: function () {
				d3.select(this.el).dispatch('remove');
				return this;
			},
			getConfig: function () {
				return this.data.data;
			},
			setConfig: function (data) {
				var that = this;
				// console.log('group config:', data);
				if (typeof data === "object" && !Array.isArray(data)) {
					Object.assign(this.data.data, data);
					Object.keys(data).forEach(function (key) {
						if (that.data.hasOwnProperty(key) && key !== 'data') {
							that.data[key] = data[key];
						}
					})
				}
				return this;
			},
            isSelected: function () {
                return this.data.status.selected;
            },
            selected: function () {
                d3.select(this.el).dispatch('selected');
            },
            unselected: function () {
                d3.select(this.el).dispatch('unselect');
            },
			getContainNodes: function () {
				return this.data.contain.map(function (d) {
					return d.port;
                })
            },
			getPosition: function () {
				return {
					x: this.data.x,
					y: this.data.y
				}
			},
			addContain: function (data, action) {
				var that = this;
				var nodes = data.nodes || [];
				if (!nodes.length) {
					console.log('add contain 数组为空！');
					return;
				}

				var nodes1 = baseVm.getVm('node').dataFn(nodes, baseVm.guid(5));
				nodes1.forEach(function (node) {
                    that.data.contain.push(node);
                    node.groups.push(that.data);
                });


				this.data.data.x = this.data.x;
				this.data.data.y = this.data.y;
                var topoData = {
                    groups: [this.data],
                };

                baseVm.gatherLayout(topoData);
                baseVm.drawData({nodes: that.data.contain, groups: topoData.groups}, 'append');
            },
			remind: function (data) {
				d3.select(this.el).dispatch('remind', {detail: Object.assign({}, data || {})});
			},
		};

        svgVm.port = new this.svg();

        return svgVm.port;
    }

    exports.port = port;
})));