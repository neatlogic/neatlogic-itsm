;(function (window) {
    var TsFlowChart = window.TsFlowChart || {};

    TsFlowChart.prototype.extend({
        getAnchorPointPosition: function (anchor) {
            var that = this;
            if (this.consoleLog(anchor, 'getAnchorPointPosition anchor 参数 is not exist!')) return false;
            var nodeG, nodeData, direction, offset = that.options.anchorPointOffset || 0;
            direction = d3.select(anchor).datum();
            if (direction) direction = direction.position;
            else return false;
            nodeG = this.getParents(anchor, 'nodeG');
            if (nodeG) nodeData = d3.select(nodeG).datum();
            if (this.consoleLog(nodeData, 'getAnchorPointPosition nodeData is not exist!')) return false;
            if (direction === 'right') {
                return {
                    x: nodeData.x + nodeData.width + offset,
                    y: nodeData.y + nodeData.height / 2
                };
            } else if (direction === 'top') {
                return {
                    x: nodeData.x + nodeData.width / 2,
                    y: nodeData.y - offset
                };
            } else if (direction === 'left') {
                return {
                    x: nodeData.x - offset,
                    y: nodeData.y + nodeData.height / 2
                };
            } else if (direction === 'bottom') {
                return {
                    x: nodeData.x + nodeData.width / 2,
                    y: nodeData.y + nodeData.height + offset
                };
            }
        },
        getNodeAnchorPosition: function (node, dir) {
            var that = this;
            if (!node || !dir) return false;
            var nodeData = node, offset = that.options.anchorPointOffset || 0;
            if (dir === 'right') {
                return {
                    x: nodeData.x + nodeData.width + offset,
                    y: nodeData.y + nodeData.height / 2
                };
            } else if (dir === 'top') {
                return {
                    x: nodeData.x + nodeData.width / 2,
                    y: nodeData.y - offset
                };
            } else if (dir === 'left') {
                return {
                    x: nodeData.x - offset,
                    y: nodeData.y + nodeData.height / 2
                };
            } else if (dir === 'bottom') {
                return {
                    x: nodeData.x + nodeData.width / 2,
                    y: nodeData.y + nodeData.height + offset
                };
            }
        },
        createNode: function (data) {
            if (!data) return false;
            if (!data.id) data.id = this.createId(this.options.uuidmode);
            if (data.x === undefined || data.y === undefined) return false;
            var transform = d3.zoomTransform(this.svg.node());
            data.x = transform.invertX(data.x);
            data.y = transform.invertY(data.y);
            var nodeData = this.nodeDataFn(data);
            // 节点按条件过滤
			var allNodeData = this.getAllNodeData();
			nodeData = nodeData.filter(function (d) {
				var isFind = allNodeData.find(function (d1) {
					return d1.type === d.type;
				});
			    if (d.isMultiple == 0 && isFind) { // 节点的唯一性判断
					typeof d.onInitError === 'function' && d.onInitError(d.data);
					return false;
                } else return true;
            });
			if (nodeData.length === 0) return d3.select(null);
            return this.drawNode(nodeData);
        },
        drawNode: function (nodeData) {
            // console.log(nodeData);
            var that = this;
            if (!Array.isArray(nodeData)) return false;
            var nodeG = this.nodesG.selectAll('.nodeG').data(nodeData, function (d) {
                return d.id;
            });
            var enterNodeG = nodeG.enter()
                .append('g').attr('class', 'nodeG')
                .attr('transform', function (d) {
                    return `translate(${d.x},${d.y})`;
                })
                .attr('cursor', 'pointer');

            if (!that.options.readOnly) {
                enterNodeG.call(this.drag);
            }
            // var exitNode = nodeG.exit().remove()
            if (!enterNodeG.size()) console.log('no enter');

            // 添加节点形状
            that.appendNodeShape(enterNodeG);
            // 添加节点label
            that.appendNodeLabel(enterNodeG);
            // 添加icon
            that.appendNodeIcon(enterNodeG);
            // 添加节点状态
            that.appendNodeStatus(enterNodeG);
            // 添加marker
            that.appendNodeMarker(enterNodeG);
            // 添加锚点
            that.appendNodeAnchor(enterNodeG);
            // 添加节点btn
            that.appendNodeBtn(enterNodeG);
//
            that.appendNodeStatusBtn(enterNodeG);
            // 节点添加事件
            enterNodeG
                .on('mouseenter', function (d) {
                    if (that.options.readOnly) return false;
                    var node = d3.select(this);
                    node.raise();
                    node.selectAll('.anchorG .anchor-point').dispatch('show').dispatch('bigger');
                    node.selectAll('.nodeBtnG').dispatch('show');
                })
                .on('mouseleave', function () {
                    if (that.options.readOnly) return false;
                    var node = d3.select(this);
                    node.selectAll('.anchorG .anchor-point').dispatch('hide').dispatch('smaller');
                    node.selectAll('.nodeBtnG').dispatch('hide');
                })
                .on('click', function (d) {
                    d3.event.stopPropagation();
                    var oNode = that.getNodeObject(this);
                    if (typeof d.data.onClick === 'function') d.data.onClick(oNode);
                });
            // 添加节点操作 remove valid
            enterNodeG
                .on('remove', function (d) {
                    var node = d3.select(this);
                    node
                        .attr('opacity', 1)
                        .transition('remove')
                        .duration(300)
                        .attr('opacity', 0)
                        .on('end', function (d) {
                            node.remove();
                        });
                    // 线remove
                    var linksFrom = that.getLinksByFrom(d.id);
                    var linksTo = that.getLinksByTo(d.id);
                    linksFrom.dispatch('remove');
                    linksTo.dispatch('remove');
                    that.uuidArr.remove(d.id);
                })
                .on('valid', function (d) {
                    var linksData = that.getAllLinkData();
                    var valid = d.valid || {};
                    if (typeof d.valid !== 'object') d.valid = valid;
                    if (typeof d.behavior === 'object') {
                        Object.keys(d.behavior).forEach(function (k) {
                            if (k === 'needIn') {
                                var isIn = linksData.some(function (d1) {
                                    return d1.target === d;
                                });
                                if (isIn) {
                                    valid.needIn = true;
                                } else valid.needIn = false;
                            } else if (k === 'needOut') {
                                var isOut = linksData.some(function (d1) {
                                    return d1.source === d;
                                });
                                if (isOut) {
                                    valid.needOut = true;
                                } else valid.needOut = false;
                            }
                        });
                    }
                });

            // 执行加载完后的callback
            enterNodeG.each(function (d) {
                var oEle = that.getNodeObject(this);
                if (typeof d.data.onLoad === 'function') d.data.onLoad(oEle);
            });

            return enterNodeG;
        },
        appendNodeBtn: function (nodeG) {
            var that = this;
            if (that.options.readOnly) return false;
            if (!nodeG) return false;
            nodeG = this.selectAll(nodeG);
            var nodeBtnG = nodeG.each(function (d) {
                var _this = this;
                var btns = [];
                if (d.behavior) {
                    if (d.behavior.deletable) btns.push('delete');
                    if (d.behavior.editable) btns.push('edit');
                }
                var node = d3.select(this);
                var offset = 3;
                var BtnG = node.selectAll('.nodeBtnG').data([d]);
                var enterBtnG = BtnG.enter().append('g').attr('class', 'nodeBtnG');
                BtnG = BtnG.merge(enterBtnG);
                var foBtnG = BtnG.selectAll('.btnG').data(btns);
                var foBtn = foBtnG.enter().append('g')
                    .attr('class', function (d) {
                        return d + ' btnG';
                    });

                foBtnG = foBtnG.merge(foBtn);
                foBtnG.attr('transform', function (d1, i1) {
                    d3.select(this).attr('width', that.options.nodeBtnWidth).attr('height', that.options.nodeBtnWidth);
                    return `translate(${d.width - i1 * (that.options.nodeBtnWidth + offset) - that.options.nodeBtnWidth},${-that.options.nodeBtnWidth + 2})`;
                });
                foBtnG.exit().remove();
                foBtn.append('circle')
                    .attr('r', that.options.nodeBtnWidth / 2 + 0.3)
                    .attr('cx', that.options.nodeBtnWidth / 2)
                    .attr('cy', that.options.nodeBtnWidth / 2)
                    .attr('stroke', d.stroke)
                    .attr('stroke-width', 1)
                    .attr('fill', d.nodeBtnFill);
                foBtn.append('use')
                    .attr('width', that.options.nodeBtnWidth)
                    .attr('height', that.options.nodeBtnWidth)
                    .attr('xlink:href', function (d) {
                        if (d === 'delete') {
                            d3.select(this).attr('width', that.options.nodeBtnWidth + 1).attr('height', that.options.nodeBtnWidth + 1).attr('x', -0.5).attr('y', -0.5);
                            return '#iconDelete' + that.svgId;
                        } else if (d === 'edit') {
                            return '#iconEdit' + that.svgId;
                        }
                    })
                    .attr('fill', 'white');
                foBtn
                    .on('mousedown', function () {
                        d3.event.stopPropagation();
                    })
                    .on('click', function (d1) {
                        d3.event.stopPropagation();
                        var oEle = that.getNodeObject(_this);
                        if (d1 === 'delete') {
                            node.dispatch('remove');
                            if (typeof d.data.onDelete === 'function') d.data.onDelete(oEle);
                        } else if (d1 === 'edit') {
                            if (typeof d.data.onEdit === 'function') d.data.onEdit(oEle);
                        }
                    });
                foBtn.append('title').text(function (d) {
                    if (d === 'delete') {
                        return '删除';
                    } else if (d === 'edit') {
                        return '编辑';
                    }
                });
                // 添加按钮事件

                // btnG hide and show
                enterBtnG
                    .on('hide', function (d) {
                        enterBtnG
                            .transition()
                            .duration(300)
                            .attr('opacity', 0)
                            .on('end', function () {
                                enterBtnG.classed('hide', true);
                            });
                    })
                    .on('show', function () {
                        enterBtnG
                            .transition()
                            .duration(300)
                            .attr('opacity', 1)
                            .on('start', function () {
                                enterBtnG.classed('hide', false);
                            });
                    })
                    .classed('hide', true);
            });
        },
        appendNodeAnchor: function (nodeG) {
            var that = this;
            if (!nodeG) return false;
            if (nodeG.nodeType === 1) nodeG = d3.select(nodeG);
            nodeG.each(function (d) {
                var node = d3.select(this);
                var offset = that.options.anchorPointOffset;
                // var anchorSort = ['top', 'right', 'bottom', 'left'];
                // anchorSort = anchorSort.map(function (d1) {
                //     return d.data.portList.find(function (d2) {
                //         return d1 === d2.position;
                //     });
                // });
                var anchorG = node.selectAll('.anchorG').data([d]).enter()
                    .append('g').attr('class', 'anchorG').lower();

                var hoverCircle = anchorG.append('circle').attr('class', 'hover')
                    .attr('r', 0)
                    .attr('fill-opacity', 0.2)
                    .attr('fill', '#2ed373')
                    .attr('stroke', '#2ed373')
                    .attr('stroke-opacity', 0.3);
                anchorG.selectAll('.anchor-point').data(d.portList).enter()
                    .append('circle').attr('class', 'anchor-point')
                    .attr('r', that.options.anchorPointRadius)
                    .attr('cx', function (d1) {
                        if (d1.position === 'left') return 0 - offset;
                        else if (d1.position === 'right') return d.width + offset;
                        else if (d1.position === 'top' || d1.position === 'bottom') return d.width / 2;
                    })
                    .attr('cy', function (d1) {
                        if (d1.position === 'left' || d1.position === 'right') return d.height / 2;
                        else if (d1.position === 'top') return 0 - offset;
                        else if (d1.position === 'bottom') return d.height + offset;
                    })
                    .attr('fill', d.anchorFill)
                    .attr('stroke', d.anchorStroke)
                    .attr('cursor', 'pointer')
                    .on('style', function (d1) {
                        if (d1.select.length) return false;
                        var anchor = d3.select(this);
                        anchor
                            .attr('fill', d.anchorFill)
                            .attr('stroke', d.anchorStroke)
                            .attr('stroke-width', 1)
                    })
                    .on('hide', function (d) {
                        if ((d3.event.detail && d3.event.detail.show === false)) delete d.show;
                        if (d.show || (Array.isArray(d.select) && d.select.length !== 0)) return false;
                        var anchor = d3.select(this);
                        anchor
                        // .attr('opacity', 1)
                            .transition()
                            .duration(300)
                            .attr('opacity', 0)
                            .on('end', function () {
                                // anchor.classed('hide', true)
                            });
                    })
                    .on('show', function (d) {
                        if (d3.event.detail && d3.event.detail.show === true) d.show = true;
                        var anchor = d3.select(this);
                        anchor
                        // .attr('opacity', 0)
                            .transition()
                            .duration(300)
                            .attr('opacity', 1)
                            .on('start', function () {
                                anchor.classed('hide', false);
                            });
                    })
                    .on('update', function () {
                        var anchor = d3.select(this);
                        anchor
                            .attr('cx', function (d1) {
                                if (d1.position === 'left') return 0 - offset;
                                else if (d1.position === 'right') return d.width + offset;
                                else if (d1.position === 'top' || d1.position === 'bottom') return d.width / 2;
                            })
                            .attr('cy', function (d1) {
                                if (d1.position === 'left' || d1.position === 'right') return d.height / 2;
                                else if (d1.position === 'top') return 0 - offset;
                                else if (d1.position === 'bottom') return d.height + offset;
                            });
                    })
                    .classed('hide', 'true')
                    .filter(function () {
                        return !that.options.readOnly;
                    })
                    .on('bigger', function () {
                        var offset1  = offset + 3;
                        d3.select(this)
                            .transition('bi')
                            .duration(300)
                            .attr('r', that.options.anchorPointHoverRadius)
                            .attr('cx', function (d1) {
                                if (d1.position === 'left') return 0 - offset1;
                                else if (d1.position === 'right') return d.width + offset1;
                                else if (d1.position === 'top' || d1.position === 'bottom') return d.width / 2;
                            })
                            .attr('cy', function (d1) {
                                if (d1.position === 'left' || d1.position === 'right') return d.height / 2;
                                else if (d1.position === 'top') return 0 - offset1;
                                else if (d1.position === 'bottom') return d.height + offset1;
                            });
                    })
                    .on('smaller', function () {
                        d3.select(this)
                            .transition('sm')
                            .duration(300)
                            .attr('r', that.options.anchorPointRadius)
                            .attr('cx', function (d1) {
                                if (d1.position === 'left') return 0 - offset;
                                else if (d1.position === 'right') return d.width + offset;
                                else if (d1.position === 'top' || d1.position === 'bottom') return d.width / 2;
                            })
                            .attr('cy', function (d1) {
                                if (d1.position === 'left' || d1.position === 'right') return d.height / 2;
                                else if (d1.position === 'top') return 0 - offset;
                                else if (d1.position === 'bottom') return d.height + offset;
                            });
                    })
                    .on('mouseenter', function (d1) {
                        d3.event.stopPropagation();
                        d3.select(this)
                            .classed('active', true)

                        var offset1  = offset + 3;
                        hoverCircle
                            .attr('cx', function () {
                                if (d1.position === 'left') return 0 - offset1;
                                else if (d1.position === 'right') return d.width + offset1;
                                else if (d1.position === 'top' || d1.position === 'bottom') return d.width / 2;
                            })
                            .attr('cy', function () {
                                if (d1.position === 'left' || d1.position === 'right') return d.height / 2;
                                else if (d1.position === 'top') return 0 - offset1;
                                else if (d1.position === 'bottom') return d.height + offset1;
                            })
                            .transition('aa')
                            .duration(300)
                            .attr('r', that.options.anchorPointHoverRadius + 3)

                    })
                    .on('mouseleave', function () {
                        d3.event.stopPropagation();
                        d3.select(this)
                            .classed('active', false)
                            // .attr('r', that.options.anchorPointRadius);
                        hoverCircle
                            .transition('bb')
                            .duration(300)
                            .attr('r', 0)
                    })
                    .call(that.anchorDrag);

            });
        },
        appendNodeShape: function (nodeG) {
            var that = this;
            if (!nodeG) return false;
            nodeG = this.selectAll(nodeG);
            nodeG.each(function (d) {
                var node = d3.select(this);
                var rectG = node.selectAll('.rectG').data([d]);
                var rectEnterG = rectG.enter()
                    .append('g').attr('class', 'rectG');
                rectG = rectG.merge(rectEnterG);
                var rect = rectG.selectAll('.rect').data([d]);
                var rectEnter = rect.enter().append('path').attr('class', 'rect')
                    // .attr('rx', 6)
                    // .attr('ry', 6);
                rect = rect.merge(rectEnter);
                var title = rectG.selectAll('title').data([d.label]);
                title.enter().append('title').text(d.label);
                title.text(d.label);

                node
                    .on('style.shape', function (d) {
                        rect
                            .attr('d', d.shapePath)
                            .attr('fill', d.fill)
                            .attr('stroke', d.stroke)
                            .attr('fill-opacity', d.fillOpacity)
                            .attr('stroke-width', d.strokeWidth)
                            .attr('stroke-dasharray', d.strokeDasharray);
                        var anchor = node.selectAll('.anchor-point');
                        anchor.attr('stroke', d.stroke);
                    })
                    .on('resize.shape', function (d) {
                        rect
                            // .attr('width', d.width)
                            // .attr('height', d.height)
                            .each(function (d) {
                                var linkfrom = that.getLinksByFrom(d.id);
                                var linkto = that.getLinksByTo(d.id);
                                var anchor = node.selectAll('.anchor-point');
                                linkfrom.dispatch('update');
                                linkto.dispatch('update');
                                anchor.dispatch('update');
                            });
                    })
                    .dispatch('resize')
                    .dispatch('style');
            })
        },
        appendNodeLabel: function (nodeG) {
            var that = this;
            if (!nodeG) return false;
            nodeG = this.selectAll(nodeG);

            nodeG.each(function (d) {
                var node = d3.select(this);
                var label = node.selectAll('text.nodeName').data([d]);
                var labelEnter = label.enter().append('text').attr('class', 'nodeName')
                    .attr('stroke', 'none');
                    // .attr('font-family', 'Arial');

                label = label.merge(labelEnter);

                // 换行处理
                label
                    .attr('transform', function (d) {
                        if (d.type === 'start' || d.type === 'end') return `translate(${d.width / 2 + d.fontIconWidth / 2 - d.labelWidth / 2 + 2},${d.labelHeight / 2})`;
                        return `translate(${d.fontIconWidth + d.padding + d.fontIconPadding},${d.labelHeight / 2})`;
                    })
                    // .attr('x', that.options.nodeFontIconWidth)
                    // .attr('y', d.labelHeight / 2)
                    .text(d.label)
                    .each(function () {
                        var labelEle = d3.select(this);
                        var labelMaxWidth = d.width - d.padding * 2 - d.fontIconWidth - d.fontIconPadding;
                        var labelArr = [];
						if (d.lineClamp > 1) {
							labelArr = that.textLineExceed({
								text: d.label,
								len: labelMaxWidth,
								line: 2,
								size: d.fontsize || 12,
								exceed: false
							});
						} else labelArr.push(d.label);
                        var tspan = labelEle.text('').selectAll('tspan').data(labelArr);
                        var tspanEnter = tspan.enter().append('tspan');
                        tspan.exit().remove();
                        tspan = tspan.merge(tspanEnter);
                        tspan
                            .attr('dy', '0.37em')
                            .attr('x', 0)
                            .text(function (d) {
                                return d;
                            })
                            .attr('font-size', d.fontSize)
                            .attr('y', function (d1, i) {
                                if (labelArr.length <= 1) return 0;
                                if (i === 0) return '-0.8em';
                                return '0.8em';
                            });
                        if (labelArr.length > 1) {
                            var labelBox = this.getBBox();
                            if (labelBox.width > labelMaxWidth) {
                                var scale =  labelMaxWidth / labelBox.width;
                                // tspan.attr('font-size', d.fontSize * scale);
                                // tspan.attr('transform', `scale(${scale})`);
                                label.attr('transform', `translate(${d.fontIconWidth + d.padding + d.fontIconPadding},${d.labelHeight / 2})scale(${scale})`)
                                    // .style('transform', `translate(${that.options.nodeFontIconWidth}px,${d.labelHeight / 2}px)scale(${scale})`);
                            }
                        }
                    })

                node
                    .on('style.lable', function (d) {
                        label
                            .attr('fill', d.fontColor)
                            .attr('font-size', d.fontSize);
                        node.selectAll('.Icon')
                            .attr('fill', d.fontIconColor)
                            .attr('font-size', d.fontIconSize);
                    })
                    .on('resize.label', function (d) {

                    })
                    .dispatch('resize')
                    .dispatch('style');

            });
        },
		textLineExceed: function (opts) {
			var that = this;
			var exceedStr = opts.exceed ? opts.exceed : '';
			var exceedLen = opts.exceed ? this.getTextLabelLen({text: opts.exceed, fontSize: opts.size}).width : 0;
			var strArr = [];
			var str = '';
			var strSplit = opts.text.split('');
			var totalLen = that.getTextLabelLen({text: opts.text, fontSize: opts.size}).width;
			if ((opts.exceed === false) && (totalLen > opts.len * opts.line)) {
				for (var i = 0; i < opts.text.length; i++) {
					str += strSplit[i];
					if (that.getTextLabelLen({text: str, fontSize: opts.size}).width >= totalLen / opts.line) {
						strArr.push(str);
						str = '';
					}
					if (strArr.length === opts.line - 1) {
						strArr.push(strSplit.slice(i+1).join(''));
						break;
					}
				}
			} else {
				for (var i = 0; i < opts.text.length; i++) {
					str += strSplit[i];
					var len = that.getTextLabelLen({text: str, fontSize: opts.size}).width;
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
        appendNodeWareEffect: function (nodeG) {
            var that = this;
            var scaley = 0.6;
            var scalex = that.options.nodeWidth / 560 * 4;
            nodeG = this.selectAll(nodeG);
            nodeG.each(function (d) {
                var node = d3.select(this);
                var effectG = node.selectAll('.effectG').data([d]);
                var effectEnterG = effectG.enter().append('g').attr('class', 'effectG');
                effectG = effectG.merge(effectEnterG);
                var progressHeight = d.progress.progress !== undefined ? d.labelHeight * d.progress.progress : d.labelHeight * 0.4;
                effectEnterG
                    .attr('clip-path', function (d) {
                        return `url(#${that.getNodeFilter(d.shapePath)})`;
                        // if (node.selectAll('.statusBtnG .btnG').size()) {
                        //     if (d.lineClamp > 1) return `url(#clipPath5${that.svgId})`;
                        //     else return `url(#clipPath4${that.svgId})`;
                        // } else {
                        //     if (d.lineClamp > 1) return `url(#clipPath2${that.svgId})`;
                        //     else return `url(#clipPath1${that.svgId})`;
                        // }
                    });
                var ware = effectG.selectAll('.ware').data([d.progress, d.progress]);
                var wareEnter = ware.enter().append('use').attr('class', 'ware').attr('xlink:href', `#ware${that.svgId}`);
                ware = ware.merge(wareEnter);

                // progress
                var progress = effectG.selectAll('rect').data([d.progress]);
                var progressEnter = progress.enter()
                    .append('rect');
                progress = progress.merge(progressEnter);
                progress
                    .attr('x', 0);

                node
                    .on('progress', function (d) {
                        // ware
                        ware.each(function (d1, i) {
                            var wareUse = d3.select(this);
                            if (i === 0) {
                                wareUse
                                    .attr('transform', function () {
                                        return `translate(0,${d.labelHeight - 20 * scaley})scale(${scalex},${scaley})`;
                                    })
                                    .attr('fill', function (d1) {
                                        return d1.color;
                                    })
                                    .attr('fill-opacity', d1.opacity)
                                    .attr('y', -progressHeight / scaley)
                                    .on('running', function () {
                                        // var use = d3.select(this);
                                        var durationTime = 800;
                                        var time = d3.event.detail && d3.event.detail.time ? d3.event.detail.time : durationTime;
                                        transitionUse();

                                        function transitionUse() {
                                            wareUse
                                                .attr('transform', function () {
                                                    return `translate(${-that.options.nodeWidth},${d.labelHeight - 20 * scaley})scale(${scalex},${scaley})`;
                                                })
                                                .transition('ware')
                                                .duration(time)
                                                .ease(d3.easeLinear)
                                                .attr('transform', function () {
                                                    return `translate(${-that.options.nodeWidth * 3},${d.labelHeight - 20 * scaley})scale(${scalex},${scaley})`;
                                                })
                                                .on('end', function () {
                                                    transitionUse();
                                                });
                                        }
                                    });
                            } else if (i === 1) {
                                wareUse
                                    .attr('transform', function () {
                                        return `translate(${-that.options.nodeWidth * 3},${d.labelHeight - 20 * scaley})scale(${scalex},${scaley})`;
                                    })
                                    .attr('fill', function (d1) {
                                        return d1.color;
                                    })
                                    .attr('fill-opacity', d1.opacity - 0.2)
                                    .attr('y', -progressHeight / scaley)
                                    .on('running', function () {
                                        // var use = d3.select(this);
                                        var durationTime = 1000;
                                        var time = d3.event.detail && d3.event.detail.time ? d3.event.detail.time : durationTime;
                                        transitionUse();

                                        function transitionUse() {
                                            wareUse
                                                .attr('transform', function () {
                                                    return `translate(${-that.options.nodeWidth * 2},${d.labelHeight - 20 * scaley})scale(${scalex},${scaley})`;
                                                })
                                                .transition('ware')
                                                .duration(time)
                                                .ease(d3.easeLinear)
                                                .attr('transform', function () {
                                                    return `translate(${0},${d.labelHeight - 20 * scaley})scale(${scalex},${scaley})`;
                                                })
                                                .on('end', function (d) {
                                                    transitionUse();
                                                });
                                        }
                                    });
                            }
                        });
                        // progress
                        progress
                            .attr('width', d.width)
                            .attr('height', progressHeight)
                            .attr('fill-opacity', function (d1) {
                                return d1.opacity + 0.1;
                            })
                            .attr('fill', function (d1) {
                                return d1.color;
                            })
                            .attr('y', -progressHeight)
                            .attr('transform', `translate(0,${d.labelHeight})`);

                        if (d.progress.enable === true) {
                            ware.dispatch('running');
                        } else if (d.progress.enable === false) {
                            ware.remove();
                            progress.remove();
                        } else if (d.progress.enable === 'paused') {
                            var transform = ware.attr('transform').split(',');
                            transform.pop();
                            transform.push('0)');
                            ware.interrupt('ware');
                            ware.transition()
                                .duration(1000)
                                .ease(d3.easeLinear)
                                .attr('transform', transform.join(','));
                        }
                    })
                    .dispatch('progress');
            });
            nodeG.selectAll('.Icon,.nodeName,.markerG').raise();
            nodeG.selectAll('.statusIconG').raise();
            nodeG.selectAll('.statusBtnG').raise();
            return scaley;
        },
        appendNodeIcon: function (nodeG) {
            var that = this;
            if (!nodeG) return false;
            nodeG = this.selectAll(nodeG);
            nodeG.each(function (d) {
                if (!d.icon) return false;
                var node = d3.select(this);
                if (d.icon === 'start' || d.icon === 'end') {
                    node.selectAll('.Icon').data([d]).enter()
                        .append('image')
                        .attr('class', 'Icon')
                        .attr('x', function () {
                            var name = node.select('text.nodeName');
                            var nameBB = name.node().getBBox();
                            name
                                .attr('text-anchor', 'middle')
                                .attr('transform', function (d1) {
                                    return `translate(${d.width / 2},${d.labelHeight / 2})`;
                                })
                                .attr('x', that.options.nodeIconWidth / 2 + 3);
                            return -nameBB.width / 2 - that.options.nodeIconWidth / 2 - 3;
                        })
                        .attr('y', function (d) {
                            return -that.options.nodeIconWidth / 2;
                        })
                        .attr('width', that.options.nodeIconWidth)
                        .attr('height', that.options.nodeIconWidth)
                        .attr('transform', `translate(${d.width / 2},${d.labelHeight / 2})`)
                        .attr('xlink:href', function (d) {
                            return that.getIcon(d.icon);
                        });
                } else {
                    // var name = node.select('.name');
                    // var nameBox = name.node().getBBox();
                    var icon = node.selectAll('.Icon').data([d]);
                    var iconEnter = icon.enter()
                        .append('text')
                        .attr('class', 'Icon')
                        .attr('font-size', d.iconFontSize)
                        .attr('text-anchor', function () {
                            if (d.type === 'start' || d.type === 'end') return 'start';
                            return 'middle';
                        })
                        .attr('dy', '0.36em')
                        .attr('stroke', 'none')
                        .attr('fill', d.fontIconColor)
                        .attr('font-size', d.fontIconSize)
                        .attr('font-family', d.fontIconFamily)
                        .text(d.icon);
                    var iconBox = icon.merge(iconEnter)
                        .attr('x', function () {
                            if (d.data.type === 'start' || d.data.type === 'end') {
                                return d.width / 2 - d.labelWidth / 2 - d.fontIconWidth / 2  - 2;
                            } else return d.fontIconWidth / 2 + d.padding;
                        })
                        .attr('y', d.labelHeight / 2);
                    // name.attr('transform', `translate(${d.width / 2 + iconBox.width / 2},${d.labelHeight / 2})`)
                    //     .selectAll('tspan').attr('dx', '0.3em');
                }
            });
        },
        appendNodeStatusIcon: function (nodeG, status) {
            var that = this;
            if (!nodeG) return false;
            nodeG = this.selectAll(nodeG);
            nodeG.each(function (d) {
                var node = d3.select(this);
                var _this = this;
                var localStatusIconArr = ['locked', 'paused', 'waitconfirm', 'aborted']
                var statusIcon = node.selectAll('.statusIconG').data([d]);
                var enterStatusIcon = statusIcon.enter()
                    .append('g').attr('class', 'statusIconG');
                statusIcon = statusIcon.merge(enterStatusIcon);
                statusIcon.html('');
                var iconG = statusIcon.selectAll('.iconG').data(d.statusIcon);
                var iconEnterG = iconG.enter().append('g').attr('class', 'iconG');
                iconG.exit().remove();
                iconG = iconG.merge(iconEnterG);
                var localIcon = iconG
                    .filter(function (d1) {
                        // return localStatusIconArr.includes(d1.name);
                        return that.include(localStatusIconArr, d1.name);
                    }).each(function (d1) {
                        var iconGele = d3.select(this);
                        if (d1.name === 'locked') {
                            var rect = iconGele.append('rect')
                                .attr('class', 'locked')
                                .attr('width', d.width)
                                .attr('height', d.labelHeight)
                                .attr('clip-path', function () {
                                    return `url(#${that.getNodeFilter(d.shapePath)})`;
                                    // if (node.selectAll('.statusBtnG .btnG').size()) {
                                    //     if (d.lineClamp > 1) return `url(#clipPath5${that.svgId})`;
                                    //     else return `url(#clipPath4${that.svgId})`;
                                    // } else {
                                    //     if (d.lineClamp > 1) return `url(#clipPath2${that.svgId})`;
                                    //     else return `url(#clipPath1${that.svgId})`;
                                    // }
                                })
                                .attr('fill', '#595959')
                                .attr('fill-opacity', d1.bgFillOpacity || 0.8)
                                .attr('stroke', 'transparent')
                                .attr('stroke-width', 1)
                                .attr('x', 0)
                                .attr('y', 0);
                            iconGele.append('use')
                                .attr('x', d.width / 2 - 8)
                                .attr('y', d.labelHeight / 2 - 8)
                                .attr('width', 16)
                                .attr('height', 16)
                                .attr('fill', 'white')
                                .attr('xlink:href', '#iconLocked' + that.svgId);
                            iconGele.append('title')
                                .text(function (d1) {
                                    return d1.title || d1.name;
                                })
                        } else {
                            iconGele.append('use')
                                .attr('x', d.width + 4)
                                .attr('y', -1)
                                .attr('width', 16)
                                .attr('height', 16)
                                .attr('stroke', function () {
                                    if (status === 'aborted') return 'red';
                                    else return '#a1a1a1';
                                })
                                .attr('fill', function () {
                                    if (status === 'aborted') return 'red';
                                    else return '#a1a1a1';
                                })
                                .attr('xlink:href', function () {
                                    if (status === 'paused') return '#iconPaused' + that.svgId;
                                    else if (status === 'waitconfirm') return '#iconWait' + that.svgId;
                                    else if (status === 'aborted') return '#iconAborted' + that.svgId;
                                });
                            var circle = iconGele.append('circle')
                                .attr('r', 14 / 2)
                                .attr('cx', d.width + 5 + 14 / 2)
                                .attr('cy', 14 / 2)
                                .attr('fill', 'transparent')
                                .attr('stroke', 'transparent').lower();
                        }
                    });
                var customIcon = iconG
                    .filter(function (d1) {
                        // return !localStatusIconArr.includes(d1.name);
                        return !that.include(localStatusIconArr, d1.name);
                    })
                    .attr('transform', function (d1, i1) {
                        if (i1 === 0) return `translate(${d.width + that.options.nodeStatusIconWidth / 2 + 4},${that.options.nodeStatusIconWidth / 2})`;
                        if (i1 === 1) return `translate(${d.width + that.options.nodeStatusIconWidth / 2 + 4},${d.height - that.options.nodeStatusIconWidth / 2})`;
                        if (i1 === 2) return `translate(${-that.options.nodeStatusIconWidth / 2 - 4},${that.options.nodeStatusIconWidth / 2})`;
                        if (i1 === 3) return `translate(${-that.options.nodeStatusIconWidth / 2 - 4},${d.height - that.options.nodeStatusIconWidth / 2})`;
                    });
                customIcon.append('title');
                customIcon.append('text')
                    .attr('text-anchor', 'middle')
                    .attr('y', that.options.nodeStatusIconWidth / 2)
                    .attr('dy', '-0.16em');

                customIcon.append('circle')
                    .attr('r', that.options.nodeStatusIconWidth / 2);

                customIcon.selectAll('title')
                    .text(function (d1) {
                        return d1.title;
                    });
                customIcon.selectAll('text')
                    .text(function (d1) {
                        return d1.icon;
                    })
                    .attr('fill', function (d1) {
                        return d1.fontIconColor;
                    })
                    .attr('font-size', function (d1) {
                        return d1.fontIconSize;
                    })
                    .attr('font-family', function (d1) {
                        return d1.fontIconFamily;
                    });
                customIcon.selectAll('circle')
                    .attr('stroke', function (d1) {
                        return d1.bgStroke;
                    })
                    .attr('fill', function (d1) {
                        return d1.bgFill;
                    });
                // 执行callback
                iconG.each(function (d1) {
                    if (typeof d1.cb === 'function') {
                        var oNode = that.getNodeObject(_this);
                        d1.cb(oNode);
                    }
                });

                iconG
                    .on('click', function (d1) {
                        var oNode = that.getNodeObject(_this);
                        if (typeof d1.onclick === 'function') d1.onclick(oNode);
                    });
                node.selectAll('.statusBtnG').raise();
            });
        },
        appendNodeMarker: function (nodeG) {
            return;
            var that = this;
            nodeG = this.selectAll(nodeG);
            nodeG.each(function (d) {
                var node = d3.select(this);
                var markerG = node.selectAll('.markerG').data(d.marker);
                var markerEnterG = markerG.enter().append('g').attr('class', 'markerG');
                markerG.exit().remove();
                markerG = markerG.merge(markerEnterG);
                markerG.attr('clip-path', function () {
                    if (d.lineClamp > 1) return `url(#clipPath7${that.svgId})`;
                    else return `url(#clipPath6${that.svgId})`;
                });
                var item = markerEnterG.append('g');
                item.append('path')
                    .attr('d', function (d1) {
                        if (d1.position === 'upleft') {
                            return `M0,0L${d1.width},0L0,${d1.width}Z`;
                        } else if (d1.position === 'upright') {
                            return `M0,0L${-d1.width},0L0,${d1.width}Z`;
                        } else if (d1.position === 'bottomleft') {
                            return `M0,0L${d1.width},0L0,${-d1.width}Z`;
                        } else if (d1.position === 'bottomright') {
                            return `M0,0L${-d1.width},0L0,${-d1.width}Z`;
                        }
                    })
                    .attr('fill', function (d1) {
                        return d1.fill;
                    })
                    .attr('stroke', function (d1) {
                        return d1.stroke || 'none';
                    })
                    .attr('transform', function (d1) {
                        if (d1.position === 'upleft') {
                            return `translate(0,0)`;
                        } else if (d1.position === 'upright') {
                            return `translate(${d.width},0)`;
                        } else if (d1.position === 'bottomleft') {
                            return `translate(0,${d.labelHeight})`;
                        } else if (d1.position === 'bottomright') {
                            return `translate(${d.width},${d.labelHeight})`;
                        }
                    });
                item.append('title').text(function (d1) {
                    return d1.title;
                });
            })
        },
        appendNodeStatusBtn1: function (nodeG) {
            var that = this;
            if (!nodeG) return false;
            nodeG = this.selectAll(nodeG);
            nodeG.each(function (d) {
                var _this = this;
                if (!d.statusBtn) return false;
                var node = d3.select(this);
                var interval = 0;
                var btnLen = d.statusBtn.length;
                var btnWidth = (d.width - (btnLen - 1) * interval) / btnLen;
                var btnG = node.selectAll('.statusBtnG').data([d]);
                var btnEnterG = btnG.enter().append('g').attr('class', 'statusBtnG')
                    .attr('transform', function () {
                        return `translate(${0}, ${d.labelHeight - 6})`;
                    }).attr('clip-path', `url(#clipPath3${that.svgId})`);
                btnG = btnG.merge(btnEnterG);
                var btnChild = btnG.selectAll('.btnG').data(d.statusBtn);
                var btnChildEnter = btnChild.enter().append('g').attr('class', 'btnG');
                btnChild.exit().remove();
                btnChildEnter.attr('transform', function (d1, i1) {
                    return `translate(${(d.width / btnLen + interval / 2) * i1},6)`;
                });
                btnChild = btnChild.merge(btnChildEnter);
                btnChildEnter.append('rect');
                btnChildEnter.append('text');
                btnChildEnter.append('title');
                btnChildEnter.append('line').attr('class', 'segmentation');
                btnChild
                    .each(function (d1, i1) {
                        var G = d3.select(this);
                        G.selectAll('rect')
                            .attr('width', btnWidth)
                            .attr('height', that.options.nodeStatusBtnHeight)
                            .attr('fill', d1.bgFill)
                            .attr('stroke', d1.bgStroke)
                            .attr('stroke-opacity', d1.bgFillOpacity)
                            .attr('fill-opacity', d1.bgFillOpacity);
                        G.selectAll('text')
                            .attr('x', btnWidth / 2)
                            .attr('y', that.options.nodeStatusBtnHeight / 2)
                            .attr('dy', '0.36em')
                            .attr('text-anchor', 'middle')
                            .text(d1.icon)
                            .attr('font-size', d1.fontIconSize)
                            .attr('font-family', d1.fontIconFamily)
                            .attr('fill', d1.fontIconColor);
                        G.selectAll('title').text(d1.title);
                        G.selectAll('line.segmentation')
                            .attr('x1', 0)
                            .attr('x2', btnWidth)
                            .attr('y1', 0)
                            .attr('y2', 0)
                            .attr('stroke-width', d1.segmentationWidth)
                            .attr('stroke', d1.segmentation);
                    });

                var slitArr = d.statusBtn.filter(function (d1, i1, arr1) {
                    return i1 < arr1.length - 1;
                });
                var split = btnG.selectAll('line.split').data(slitArr);
                var splitEnter = split.enter().append('line').attr('class', 'split');
                split.exit().remove();
                split = split.merge(splitEnter);
                split
                    .attr('x1', function (d1, i1) {
                        return (d.width / btnLen + interval / 2) * (i1 + 1);
                    })
                    .attr('x2', function (d1, i1) {
                        return (d.width / btnLen + interval / 2) * (i1 + 1);
                    })
                    .attr('y1', 6)
                    .attr('y2', that.options.nodeStatusBtnHeight + 6)
                    .attr('stroke-width', function (d1) {
                        return d1.segmentationWidth;
                    })
                    .attr('stroke-dasharray', function (d1) {
                        return d1.splitDasharray;
                    })
                    .attr('stroke', function (d1) {
                        return d1.split;
                    });
                btnChild.on('click', function (d) {
                    d3.event.stopPropagation();
                    if (typeof d.onClick === 'function') {
                        var oEle = that.getNodeObject(_this);
                        d.onClick(oEle);
                    }
                });

                if (btnChild.size()) {
                    node.selectAll('.effectG,.statusIconG .locked').attr('clip-path', function () {
                        if (d.lineClamp > 1) return `url(#clipPath5${that.svgId})`;
                        else return `url(#clipPath4${that.svgId})`;
                    })
                }
            });
        },
        appendNodeStatusBtn: function (nodeG) {
            var that = this;
            if (!nodeG) return false;
            nodeG = this.selectAll(nodeG);
            nodeG.each(function (d) {
                var _this = this;
                if (!d.statusBtn) return false;
                var node = d3.select(this);
                var interval = 4;
                var radius = 8;
                var btnLen = d.statusBtn.length;
                var btnWidth = (d.width - (btnLen - 1) * interval) / btnLen;
                var btnG = node.selectAll('.statusBtnG').data([d]);
                var btnEnterG = btnG.enter().append('g').attr('class', 'statusBtnG')
                    .attr('transform', function () {
                        return `translate(${0}, ${0})`;
                    })
                    // .attr('clip-path', `url(#clipPath3${that.svgId})`);
                btnG = btnG.merge(btnEnterG);
                var btnChild = btnG.selectAll('.btnG').data(d.statusBtn);
                var btnChildEnter = btnChild.enter().append('g').attr('class', 'btnG');
                btnChild.exit().remove();
                btnChildEnter.attr('transform', function (d1, i1) {
                    return `translate(${(radius * 2 + interval) * i1 + radius},-4)`;
                });
                btnChild = btnChild.merge(btnChildEnter);
                btnChildEnter.append('circle');
                btnChildEnter.append('text');
                btnChildEnter.append('title');
                // btnChildEnter.append('line').attr('class', 'segmentation');
                btnChild
                    .each(function (d1, i1) {
                        var G = d3.select(this);
                        G.selectAll('circle')
                            .attr('r', radius)
                            // .attr('height', that.options.nodeStatusBtnHeight)
                            .attr('fill', '#FBFBFB')
                            .attr('stroke', d1.fontIconColor)
                            // .attr('stroke', d.stroke)
                            // .attr('stroke-dasharray', d.strokeDasharray)
                            .attr('stroke-opacity', 0.8);
                            // .attr('clip-path', function (d1, i1) {
                            //     if (i1 === 0) return `url(#clipPath8${that.svgId})`;
                            //     else return `url(#clipPath9${that.svgId})`;
                            // })
                            // .attr('fill-opacity', d1.bgFillOpacity);
                        G.selectAll('text')
                            .attr('x', 0)
                            .attr('y',  0)
                            .attr('dy', '0.36em')
                            .attr('text-anchor', 'middle')
                            .text(d1.icon)
                            .attr('font-size', d1.fontIconSize - 4)
                            .attr('font-family', d1.fontIconFamily)
                            .attr('fill', d1.fontIconColor);
                        G.selectAll('title').text(d1.title);
                        // G.selectAll('line.segmentation')
                        //     .attr('x1', 0)
                        //     .attr('x2', btnWidth)
                        //     .attr('y1', 0)
                        //     .attr('y2', 0)
                        //     .attr('stroke-width', d1.segmentationWidth)
                        //     .attr('stroke', d1.segmentation);
                    });

                // var slitArr = d.statusBtn.filter(function (d1, i1, arr1) {
                //     return i1 < arr1.length - 1;
                // });
                // var split = btnG.selectAll('line.split').data(slitArr);
                // var splitEnter = split.enter().append('line').attr('class', 'split');
                // split.exit().remove();
                // split = split.merge(splitEnter);
                // split
                //     .attr('x1', function (d1, i1) {
                //         return (d.width / btnLen + interval / 2) * (i1 + 1);
                //     })
                //     .attr('x2', function (d1, i1) {
                //         return (d.width / btnLen + interval / 2) * (i1 + 1);
                //     })
                //     .attr('y1', 6)
                //     .attr('y2', that.options.nodeStatusBtnHeight + 6)
                //     .attr('stroke-width', function (d1) {
                //         return d1.segmentationWidth;
                //     })
                //     .attr('stroke-dasharray', function (d1) {
                //         return d1.splitDasharray;
                //     })
                //     .attr('stroke', function (d1) {
                //         return d1.split;
                //     });
                btnChild.on('click', function (d) {
                    d3.event.stopPropagation();
                    if (typeof d.onClick === 'function') {
                        var oEle = that.getNodeObject(_this);
                        d.onClick(oEle);
                    }
                });

                // if (btnChild.size()) {
                //     node.selectAll('.effectG,.statusIconG .locked').attr('clip-path', function () {
                //         if (d.lineClamp > 1) return `url(#clipPath5${that.svgId})`;
                //         else return `url(#clipPath4${that.svgId})`;
                //     })
                // }
            });
        },
        appendNodeStatus: function (nodeG) {
            var that = this;
            if (!nodeG) return false;
            if (nodeG.nodeType === 1) nodeG = d3.selectAll(nodeG);
            nodeG
                .on('error', function (d) { // 错误节点
                    d.status = 'error';
                    var node = d3.select(this);
                    var nodeData = node.datum();
                    nodeData.stroke = '#e42332';
                    nodeData.strokeDasharray = '4 5';
                    node.dispatch('style')
                })
                .on('default', function (d) { // 恢复节点
                    d.status = 'default';
                    var node = d3.select(this);
                    var nodeData = node.datum();
                    nodeData.stroke = '#4e93fa';
                    nodeData.strokeDasharray = 0;
                    node.dispatch('style');
                })
                .on('hide', function (d) { // 失败状态的运行中
                    d.status = 'hide';
                    var node = d3.select(this);
                    node
                        .attr('opacity', 1)
                        .transition('remove')
                        .duration(300)
                        .attr('opacity', 0)
                        .on('end', function (d) {
                            node.classed('hide', true);
                        });
                    // 线remove
                    var linksFrom = that.getLinksByFrom(d.id);
                    var linksTo = that.getLinksByTo(d.id);
                    linksFrom.dispatch('hide');
                    linksTo.dispatch('hide');
                })
                .on('show', function (d) { // 失败状态的运行中
                    d.status = 'show';
                    var node = d3.select(this);
                    node
                        .attr('opacity', 0)
                        .transition('remove')
                        .duration(300)
                        .attr('opacity', 1)
                        .on('start', function (d) {
                            node.classed('hide', false);
                        });
                    // 线remove
                    var linksFrom = that.getLinksByFrom(d.id);
                    var linksTo = that.getLinksByTo(d.id);
                    linksFrom.dispatch('show');
                    linksTo.dispatch('show');
                });
        },
        nodeDataFn: function (nodeData) {
            var that = this;
            if (!nodeData) return false;
            if (!Array.isArray(nodeData)) nodeData = [nodeData];
            return nodeData.map(function (data) {
                if (data.type) {
                    if (flowChartNode && flowChartNode[data.type]) {
                        var data1 = data;
                        data = Object.assign({}, data, flowChartNode[data.type]);
                        if (data1 && data1['userData']) {
                            if (!data['userData']) data['userData'] = {};
                            data['userData'] = Object.assign({}, data['userData'], data1['userData']);
                        }
                    } // else console.log('没有相应的flowChartNode数据！', flowChartNode, data.type);
                }
                var label = data.userData ? data.userData.name || data.label : data.label;
                var labelWidth = that.getTextLabelLen({text: label, fontSize: data.fontsize || 12});
                labelWidth = labelWidth ? labelWidth.width : 10;
                // 补充锚点
                var arr = ['top', 'right', 'bottom', 'left'];
                arr.forEach(function (d) {
                    if (!(data.portList && Array.isArray(data.portList))) data.portList = [];
                    var is = data.portList.some(function (d1) {
                        return d1.position === d;
                    });
                    if (!is) {
                        data.portList.push({
                            position: d,
                            id: that.createId(that.options.uuidmode)
                        });
                    }
                });

                // node data
                var NODE  = {
                    data: data,
                    id: data.id || that.createId(that.options.uuidmode),
                    x: data.x,
                    y: data.y,
                    tmp: {x: data.x, y: data.y},
                    type: data.type,
                    label: label,
                    shape: data.shape || 'L-rectangle:R-rectangle', // 矩形
                    shapePath: 'M0,0',
                    borderRadius: data.borderradius || 3,
                    width: that.options.nodeWidth,
                    height: that.options.nodeHeight,
                    labelHeight: that.options.nodeHeight,
                    padding: that.options.nodePadding,
                    stroke: data.stroke || '#8c8c8c',
                    strokeWidth: data.strokewidth || 1,
                    strokeDasharray: data.strokedasharray || 0,
                    fill: data.fill || 'white',
                    fillOpacity: data.fillopacity || 1,
                    fontColor: data.fontcolor || '#8c8c8c',
                    fontSize: data.fontsize || 12,
                    labelWidth: labelWidth,
                    lineClamp: 1,
                    icon: data.icon ? eval('\'' + data.icon + '\'') : '\ue92a',
                    fontIconSize: data.iconsize || 16,
                    fontIconFamily: data.iconfamily || 'ts',
                    fontIconColor: data.iconcolor || '#8c8c8c',
                    fontIconPadding: 5,
                    fontIconWidth: 30,
                    statusIcon: [],
                    statusBtn: [],
                    progress: {},
                    marker: [],
                    anchorStroke: data.stroke || '#8c8c8c',
                    anchorFill: data.anchorfill || 'white',
                    nodeBtnFill: data.btnfill || '#336eff',
                    nodeBtnStroke: data.btnStroke || 'transparent',
                    loop: data.loop || false,
                    behavior: {},
                    tipData: {},
                    userData: {},
                    portList: [],
					isMultiple: data.isMultiple, // 是否允许设置多个节点
					onInitError: data.onInitError,  //
                    isStart: data.isStart || false,
                    isEnd: data.isEnd || false,
                };

                that.uuidArr.add(NODE.id);

                // tip data
                NODE["tipData"] = {
                    error: [],
                    msg: 'hello world!',
                    fill: '#f4f4f4',
                    delay: '#555555',
                    delayOpacitty: 0.6,
                    fillOpacity: 0.8,
                    stroke: 'transparent',
                    fontColor: '#555',
                    fontSize: 12,
                    icon: '', //'\ue8d1',
                    iconSize: 16,
                    iconColor: '#555',
                    deleteIcon: '\ue84d',
                    deleteIconSize: 10,
                    deleteIconColor: 'red',
                    fontFamily: 'ts',
                };
                // node status btn
                if (Array.isArray(data.statusBtn)) {
                    data.statusBtn.forEach(function (btn) {
                        var obj = {
                            icon: btn.icon ? eval('\'' + btn.icon + '\'') : '\ue92a',
                            title: btn.title || '暂停',
                            fontIconSize: btn.iconsize || 14,
                            fontIconColor: btn.iconcolor || '#336FFF',
                            fontIconFamily: btn.iconfamily || 'ts',
                            bgFill: btn.bgfill || '#4E93FA',
                            bgStroke: btn.bgstroke || 'transparent',
                            bgFillOpacity: btn.bgfillopacity || 0.45,
                            segmentation: btn.segmentation || 'red',
                            segmentationWidth: btn.segmentationWidth || 0.5,
                            split: btn.split || 'red',
                            splitDasharray: btn.splitdasharray || '2 2',
                            onClick: btn.onclick || function (o) {
                                // console.log(o);
                            },
                        };
                        NODE.statusBtn.push(obj);
                    });
                }

                // node status icon
                if (Array.isArray(data.statusIcon)) {
                    data.statusIcon.forEach(function (btn) {
                        var obj = {
                            name: btn.name || '',
                            icon: btn.icon ? eval('\'' + btn.icon + '\'') : '\ue92a',
                            title: btn.title || '暂停',
                            fontIconSize: btn.iconsize || 14,
                            fontIconColor: btn.iconcolor || '#336FFF',
                            fontIconFamily: btn.iconfamily || 'ts',
                        };
                        NODE.statusIcon.push(obj);
                    });
                }

                // node progress
                NODE.progress.color = data.progress ? data.progress.color || 'red' : 'red';
                NODE.progress.opacity = data.progress ? data.progress.opacity || 1 : 1;
                NODE.progress.progress = data.progress ? data.progress.progress || 0.4 : 0.4;
                NODE.progress.enable = data.progress ? data.progress.enable || false : false;

                // node marker
                if (Array.isArray(data.marker)) {
                    data.marker.forEach(function (d) {
                        var obj = {
                            fill: d.fill || '#F7B538',
                            stroke: d.stroke || 'none',
                            title: d.title || '',
                            position: d.position || 'upleft',
                            width: d.width || 16
                        };
                        NODE.marker.push(obj)
                    })
                }

                // start node , end node
                if (data.type === 'start' || data.type === 'end') {
                    NODE["fontColor"] = 'white';
                    NODE["width"] = that.options.startNodeWidth;
                    NODE["height"] = that.options.startNodeHeight;
                    NODE["labelHeight"] = that.options.startNodeHeight;
                    NODE["fontSize"] = data.fontsize || 14;
                    NODE["behavior"] = {
                        deletable: data.deletable !== undefined ? data.deletable : true,
                        editable: data.editable !== undefined ? data.editable : false,
                        needOut: data.type === 'start',
                        needIn: data.type === 'end'
                    };
                } else { // 其它节点
                    NODE["fontSize"] = data.fontsize || 12;
                    NODE["behavior"] = {
                        deletable: data.deletable !== undefined ? data.deletable : true,
                        editable: data.editable !== undefined ? data.editable : true,
                        needOut: data.needOut !== undefined ? data.needOut : true,
                        needIn: data.needIn !== undefined ? data.needIn : true,
                    };
                }
                // icon width
                NODE['fontIconWidth'] = that.getTextLabelLen({text: NODE['icon'], fontFamily: NODE['fontIconFamily'], fontSize: NODE['fontIconSize']});
                NODE['fontIconWidth'] = NODE['fontIconWidth'].width;

                // node height , node lineClamp , node labelHeight
                if (NODE.width - NODE.padding * 2 - NODE.fontIconWidth - NODE.fontIconPadding >= labelWidth) {
                    NODE.height = that.options.nodeHeight;
                } else {
                    NODE.height = that.options.nodeHeight1;
                    NODE.lineClamp = 2;
                }
                NODE.labelHeight = NODE.height;
                // if (NODE.statusBtn.length) NODE.height += that.options.nodeStatusBtnHeight;
                // 添加节点形状
                NODE.shapePath = that.getShapePath(NODE);

                //  锚点
                var anchorSort = ['top', 'right', 'bottom', 'left'];
                NODE['portList'] = anchorSort.map(function (d1) {
                    var port = data.portList.find(function (d2) {
                        return d1 === d2.position;
                    });
                    return {
                        data: port,
                        id: port.id,
                        position: port.position,
                        coordinate: that.getNodeAnchorPosition(NODE, port.position),
                        select: [],
                        show: false,
                    };
                });

                return NODE;
            });
        },
        getShapePath: function (data) {
            if (!data) return false;
            var path = d3.path();
            var shapePoint = [];
            var shapeArr = data.shape.split(':') || 'rect';
            var shapeRight = shapeArr.find(function (d) {
                return d.indexOf('R') !== -1;
            });
            var shapeLeft = shapeArr.find(function (d) {
                return d.indexOf('L') !== -1;
            });
            shapeRight = shapeRight ? shapeRight.split('-') : 'rectangle';
            shapeLeft = shapeLeft ? shapeLeft.split('-') : 'rectangle';

            var shapeRightR = Array.isArray(shapeRight) && shapeRight.length === 3 ? shapeRight[2] : data.borderRadius;
            var shapeLeftR = Array.isArray(shapeLeft) && shapeLeft.length === 3 ? shapeLeft[2] : data.borderRadius;
            shapeRight = Array.isArray(shapeRight) && shapeRight[1] ? shapeRight[1] : 'rectangle';
            shapeLeft = Array.isArray(shapeLeft) && shapeRight[1] ? shapeLeft[1] : 'rectangle';
            // 处理百分比
            if (typeof shapeRightR === 'string' && shapeRightR.indexOf('%') !== -1) {
                shapeRightR = Number(shapeRightR.split('%')[0]) / 100;
                if (shapeRightR) shapeRightR = shapeRightR * data.height;
                else shapeRightR = data.borderRadius;
            }
            if (typeof shapeLeftR === 'string' && shapeLeftR.indexOf('%') !== -1) {
                shapeLeftR = Number(shapeLeftR.split('%')[0]) / 100;
                if (shapeLeftR) shapeLeftR = shapeLeftR * data.height;
                else shapeRightR = data.borderRadius;
            }

            shapePoint.push([data.width / 2,0]);
            if (shapeRight === 'rectangle') { // 矩形
                shapePoint.push([data.width, 0, shapeRightR]);
                shapePoint.push([data.width, data.height, shapeRightR]);
            } else if (shapeRight === 'hexagon') { // 六边形
                var unit = data.height / Math.sqrt(3) / 3;
                shapePoint.push([data.width - unit, 0, shapeRightR]);
                shapePoint.push([data.width, data.height / 2, shapeRightR]);
                shapePoint.push([data.width - unit, data.height, shapeRightR]);
            } else if (shapeRight === 'octagon') { // 八边形
                var unit = data.height / (2 + Math.sqrt(2));
                var side = unit * Math.sqrt(2);
                shapePoint.push([data.width - unit, 0, shapeRightR]);
                shapePoint.push([data.width, unit, shapeRightR]);
                shapePoint.push([data.width, unit + side, shapeRightR]);
                shapePoint.push([data.width - unit, data.height, shapeRightR]);
            } else if (shapeRight === 'trapezoid') { // 梯形
                shapePoint.push([data.width - data.padding, 0, shapeRightR]);
                shapePoint.push([data.width, data.height, shapeRightR]);
            } else if (shapeRight === 'invertedTrapezoid') { // 倒梯形
                shapePoint.push([data.width, 0, shapeRightR]);
                shapePoint.push([data.width - data.padding, data.height, shapeRightR]);
            }

            if (shapeLeft === 'rectangle') {
                shapePoint.push([0, data.height, shapeLeftR]);
                shapePoint.push([0, 0, shapeLeftR]);
            } else if (shapeLeft === 'hexagon') {
                var unit = data.height / Math.sqrt(3) / 3;
                shapePoint.push([unit, data.height, shapeLeftR]);
                shapePoint.push([0, data.height / 2, shapeLeftR]);
                shapePoint.push([unit, 0, shapeLeftR]);
            } else if (shapeLeft === 'octagon') {
                var unit = data.height / (2 + Math.sqrt(2));
                var side = unit * Math.sqrt(2);
                shapePoint.push([unit, data.height, shapeLeftR]);
                shapePoint.push([0, unit + side, shapeLeftR]);
                shapePoint.push([0, unit, shapeLeftR]);
                shapePoint.push([unit, 0, shapeLeftR]);
            } else if (shapeLeft === 'trapezoid') {
                shapePoint.push([0, data.height, shapeLeftR]);
                shapePoint.push([data.padding, 0, shapeLeftR]);
            } else if (shapeLeft === 'invertedTrapezoid') {
                shapePoint.push([data.padding, data.height, shapeLeftR]);
                shapePoint.push([0, 0, shapeLeftR]);
            }

            shapePoint.forEach(function (d, i, arr) {
                if (i === 0) path.moveTo(d[0], d[1]);
                else if (i < arr.length - 1) {
                    path.arcTo(d[0], d[1], arr[i + 1][0], arr[i + 1][1], d[2])
                }
                else path.arcTo(d[0], d[1], arr[0][0], arr[0][1], d[2])
            });
            path.closePath();
            return path.toString();
        },
        getNodeAnchor: function (node, direction) {
            if (!node || !direction) return false;
            if (node.nodeType === 1) node = d3.selectAll(node);
            if (node.hasOwnProperty('id')) node = this.getNodeByUid(node.id);
            return node.selectAll('.anchor-point').filter(function (d) {
                return d.position === direction;
            });
        },
        getNodesByType: function (type) {
            if (this.consoleLog(type, '不存在该type节点')) return false;
            return this.nodesG.selectAll('.nodeG').filter(function (d) {
                return d.data.type === type;
            });
        },
        getNodeByUid: function (uid) {
            if (this.consoleLog(uid, '不存在该uid节点')) return false;
            return this.nodesG.selectAll('.nodeG').filter(function (d) {
                return d.data.id === uid;
            });
        },
        getPrevNodes: function (node) {
            var that = this;
            if (!node) return false;
            var nodeData;
            if (typeof node.node === 'function' && node.size()) nodeData = node.datum();
            else if (node.data) nodeData = node;
            else if (node.nodeType === 1) nodeData = d3.select(node).datum();
            if (!nodeData) return false;
            var prevNodesId = this.getLinksByTo(nodeData.data.id).data().map(function (d) {
                return d.data.from;
            });
            var prevNodes = this.nodesG.selectAll('.nodeG').filter(function (d) {
                // return prevNodesId.includes(d.data.id);
                return that.include(prevNodesId, d.data.id);
            });
            return prevNodes;
        },
        getNextNodes: function (node) {
            var that = this;
            if (!node) return false;
            var nodeData;
            if (typeof node.node === 'function' && node.size()) nodeData = node.datum();
            else if (node.data) nodeData = node;
            else if (node.nodeType === 1) nodeData = d3.select(node).datum();
            if (!nodeData) return false;
            var nextNodesId = this.getLinksByFrom(nodeData.data.id).data().map(function (d) {
                return d.data.to;
            });
            var nextNodes = this.nodesG.selectAll('.nodeG').filter(function (d) {
                // return nextNodesId.includes(d.data.id);
                return that.include(nextNodesId, d.data.id);
            });
            return nextNodes;
        },
        getAllPrevTypeNode: function (node, type) {
            var that = this;
            if (!node) return false;
            if (node.nodeType === 1) node = d3.select(node);
            if (typeof node.node === 'function' && node.size() && type) {
                var nodeData = node.datum();
                var linkDatas = this.getAllLinkData(),
                    prevLinkDatas = this.getLinksByTo(nodeData.data.id).data(),
                    prevTypeNode = [],
                    pathNode = []; //防死循环
                prevLinkDatas.forEach(function (d) {
                    findTypeNode(d);
                });

                function findTypeNode(linkData) {
                    // if (pathNode.includes(linkData)) return false;
                    if (that.include(pathNode, linkData)) return false;
                    if (linkData.source.data.type === type) {
                        // if (prevTypeNode.includes(linkData.source)) return false;
                        if (that.include(prevTypeNode, linkData.source)) return false;
                        else prevTypeNode.push(linkData.source);
                    }
                    var prev = linkDatas.filter(function (d1) {
                        return d1.target === linkData.source;
                    });
                    if (prev.length) {
                        prev.forEach(function (d) {
                            pathNode.push(linkData);
                            findTypeNode(d);
                        });
                    }
                }

                if (prevTypeNode.length) {
                    return this.nodesG.selectAll('.nodeG').filter(function (d) {
                        // return prevTypeNode.includes(d);
                        return that.include(prevTypeNode, d);
                    });
                } else prevTypeNode;
            }
        },
        getAllNextTypeNode: function (node, type) {
            var that = this;
            if (!node) return false;
            if (node.nodeType === 1) node = d3.select(node);
            if (typeof node.node === 'function' && node.size() && type) {
                var nodeData = node.datum();
                var linkDatas = this.getAllLinkData(),
                    prevLinkDatas = this.getLinksByFrom(nodeData.data.id).data(),
                    prevTypeNode = [],
                    pathNode = []; //防死循环
                prevLinkDatas.forEach(function (d) {
                    findTypeNode(d);
                });

                function findTypeNode(linkData) {
                    // if (pathNode.includes(linkData)) return false;
                    if (that.include(pathNode, linkData)) return false;
                    if (linkData.target.data.type === type) {
                        // if (prevTypeNode.includes(linkData.target)) return false;
                        if (that.include(prevTypeNode, linkData.target)) return false;
                        else prevTypeNode.push(linkData.target);
                    }
                    var prev = linkDatas.filter(function (d1) {
                        return d1.source === linkData.target;
                    });
                    if (prev.length) {
                        prev.forEach(function (d) {
                            pathNode.push(linkData);
                            findTypeNode(d);
                        });
                    }
                }

                if (prevTypeNode.length) {
                    return this.nodesG.selectAll('.nodeG').filter(function (d) {
                        // return prevTypeNode.includes(d);
                        return that.include(prevTypeNode, d);
                    });
                } else prevTypeNode;
            }
        },
        getNodeData: function (node) {
            if (!node) return false;
            var nodeData;
            if (typeof node.node === 'function' && node.size()) nodeData = node.datum();
            else if (node.data) nodeData = node;
            else if (node.nodeType === 1) nodeData = d3.select(node).datum();
            if (!nodeData) return false;
            this.setNodeData(nodeData);
            return nodeData.data;
        },
        setNodeData: function (node) {
            if (!node) return false;
            var nodeData;
            if (typeof node.node === 'function' && node.size()) nodeData = node.datum();
            else if (node.data) nodeData = node;
            else if (node.nodeType === 1) nodeData = d3.select(node).datum();
            if (!nodeData) return false;
            nodeData.data.x = nodeData.x;
            nodeData.data.y = nodeData.y;
            nodeData.data.label = nodeData.label;
            return nodeData;
        },
        setNodeLabel: function (node, text) {
            var that = this;
            if (!node) return false;
            if (node.nodeType === 1) node = d3.select(node);
            if (typeof node.node === 'function' && text) {
                var nodeData = node.datum();
                nodeData.label = nodeData.data.label = text;
                nodeData.labelWidth = that.getTextLabelLen({text: text, fontSize: nodeData.fontSize});
                nodeData.labelWidth = nodeData.labelWidth ? nodeData.labelWidth.width : 10;
                if (nodeData.labelWidth.width) nodeData.labelWidth = nodeData.labelWidth.width;
                if (nodeData.width - nodeData.padding * 2 - nodeData.fontIconWidth - nodeData.fontIconPadding >= nodeData.labelWidth) {
                    nodeData.labelHeight = that.options.nodeHeight;
                    nodeData.height = that.options.nodeHeight;
                    nodeData.lineClamp = 1;
                } else {
                    nodeData.labelHeight = that.options.nodeHeight1;
                    nodeData.height = that.options.nodeHeight1;
                    nodeData.lineClamp = 2;
                }
                nodeData.shapePath = that.getShapePath(nodeData);
                that.appendNodeLabel(node);
                that.appendNodeIcon(node);
                node.dispatch('style');
            }
        },
        nodeAlignLine: function (currData, allData, alignValue, alignLine) {
            alignValue.mark = false;
            var deviation = 10;
            var cx = 0, cy = 0;
            var ccx = currData.x + currData.width / 2, ccy = currData.y + currData.height / 2;
            alignValue.horizon = '';
            alignValue.vertical = '';
            allData.forEach(function (d) {
                if (d === currData) return false;
                cx = d.x + d.width / 2;
                cy = d.y + d.height / 2;
                if ((cx <= (ccx + deviation)) && (cx >= (ccx - deviation))) { // 'vertical'
                    alignValue.vertical = cx;
                }
                if ((cy <= (ccy + deviation)) && (cy >= (ccy - deviation))) { // 'level'
                    alignValue.horizon = cy;
                }
            });
            alignLine.attr('d', function (d) {
                if (d === 'level') {
                    if (alignValue.horizon) {
                        return `M-9999,${alignValue.horizon}H9999`;
                    }
                    else return '';
                } else if (d === 'vertical') {
                    if (alignValue.vertical) {
                        return `M${alignValue.vertical},-9999V9999`;
                    }
                    else return '';
                }
            });
            alignValue.mark = true;
        }
    });

})(window);