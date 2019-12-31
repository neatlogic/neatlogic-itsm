;(function (window) {
    var TsFlowChart = window.TsFlowChart || {};

    TsFlowChart.prototype.extend({
        linksDataFn: function (linkData) {
            var that = this;
            if (!linkData) return false;
            if (!Array.isArray(linkData)) linkData = [linkData];
            var nodesData = this.getAllNodeData();
            return linkData.map(function (data) {
                var source = nodesData.find(function (d) {
                    return d.id === data.from;
                });
                var target = nodesData.find(function (d) {
                    return d.id === data.to;
                });
                var linkIn, linkOut;
                if (source && target) {
                    var sPortList = [], tPortList = [];
                    if (Array.isArray(source.data.portList)) sPortList = sPortList.concat(source.data.portList);
                    if (Array.isArray(source.data.optionList)) sPortList = sPortList.concat(source.data.optionList);
                    if (Array.isArray(target.data.portList)) tPortList = tPortList.concat(target.data.portList);
                    if (Array.isArray(target.data.optionList)) tPortList = tPortList.concat(target.data.optionList);
                    linkIn = tPortList.find(function (d) {
                        return d && d.id === data.in;
                    });
                    linkOut = sPortList.find(function (d) {
                        return d && d.id === data.out;
                    });
                    if (!linkOut.position) linkOut.position = 'right';
                    if (!linkIn.position) linkIn.position = 'left';
                    // // 出线锚点标记
                    // if (linkOut) linkOut.show = true;
                }
                var tipData = {
                    error: [],
                    msg: '这点发生的发射发点发射点发射的发射发点发射点发射点发',
                    fill: '#f4f4f4',
                    fillOpacity: 0.8,
                    delay: '#555555',
                    delayOpacitty: 0.6,
                    stroke: 'transparent',
                    fontColor: '#555',
                    fontSize: 14,
                    icon: '',//\ue8d1',
                    iconSize: 16,
                    iconColor: '#555',
                    deleteIcon: '\ue84d',
                    deleteIconSize: 10,
                    deleteIconColor: 'red',
                    fontFamily: 'ts',
                };

                that.uuidArr.add(data.id);

                return {
                    data: data,
                    id: data.id,
                    source: source,
                    target: target,
                    in: linkIn,
                    out: linkOut,
                    onEdit: source.data.onLinkEdit,
                    onDelete: source.data.onLinkDelete,
                    path: data.path || [],
                    label: data.userData || {},
                    behavior: {
                        editable: data.eidtable || true,
                        deleteable: data.deleteable || true,
                    },
                    stroke: data.stroke || '#8c8c8c',
                    fill: 'none',
                    strokeWidth: data.strokewidth || 1,
                    strokeDasharray: data.strokedasharray || 0,
                    strokeDashoffset: data.strokedashoffset || 0,
                    linkBtnStroke: data.btnstroke || '#336eff',
                    linkBtnFill: data.btnfill || '#336eff',
                    labelTextLen: 0,
                    lightDot: {
                        icon: data.lightDot && data.lightDot.icon ? eval('\'' + data.lightDot.icon + '\'') : "\ue8d5",
                        iconFamily: data.lightDot ? data.lightDot.iconfamily || 'ts' : 'ts',
                        color: data.lightDot ? data.lightDot.color || '#9f9f9f' : '#9f9f9f',
                        size: data.lightDot ? data.lightDot.size : 14 || 14,
                        speed: data.lightDot ? data.lightDot.speed : 200 || 200
                    },
                    userData: {},
                    tipData: tipData
                };
            });
        },
        getAllLinkGsByNodeG: function (nodeG) {
            if (!nodeG) return false;
            if (nodeG.nodeType === 1) nodeG = d3.select(nodeG);
            var nodeData = nodeG.datum();
            return this.linksG.selectAll('g.linkG').filter(function (d) {
                return d.data.from === nodeData.id || d.data.to === nodeData.id;
            });
        },
        drawLinkOperate: function (linkData, action) {
            var that = this;
            if (!linkData) return false;

            if (linkData.from === linkData.to) { // 自连
                // console.log('自连！');
                return false;
            }
            linkData = that.linksDataFn(linkData);
            var enterLinkG = action === 'switch' ? d3.select('#hello123') : that.drawLink(linkData);
            linkData = linkData[0];
            // 判断连线条件
            var isLineFalse;
            // 1. 判断source节点不愿意连出
            if (linkData.source.behavior.needOut === false) {
                isLineFalse = true;
                var node = that.getNodeByUid(linkData.source.id);
                node.dispatch('reminder');
                linkData.source.tipData.msg = '该节点表示拒绝线连出！';
                that.tooltipFn(linkData.source);
            }
            // 1. 判断target节点不愿意连入
            if (linkData.target.behavior.needIn === false) {
                isLineFalse = true;
                var node = that.getNodeByUid(linkData.source.id);
                node.dispatch('reminder');
                linkData.target.tipData.msg = '该节点表示拒绝线连入！';
                that.tooltipFn(linkData.target);
            }
            if (isLineFalse) {
                enterLinkG.dispatch('remove');
                return false
            }
            // 判断相同连线
            var isSame = that.isLinkSame(linkData);
            if (isSame.length) { // 有相同的连线
                var link = that.getLinksByData(isSame);
                link.dispatch('reminder');
                enterLinkG.dispatch('remove');
                return false;
            }
            // 判断环
            var isLoop = that.isLinkLoop(linkData);
            if (isLoop.is) {
                var link = that.getLinksByData(isLoop.arr);
                link.dispatch('reminder');
                enterLinkG.dispatch('remove', {detail: {time: 1000}});
                return false;
            }
            return true;
        },
        drawLink: function (linkData, action) {
            // console.log(linkData);
            var that = this;
            var linkG = this.linksG.selectAll('g.linkG').data(linkData, function (d) {
                return d.id;
            });
            var enterLinkG = linkG.enter().append('g').attr('class', 'linkG').attr('cursor', 'pointer');

            enterLinkG.each(function (d) {
                // 生成线地数据
                //if (action === 'init' && d.path.length > 0) return;
                that.switchDirection(d);
            });
            // 添加线条
            that.appendLinkPathG(enterLinkG);
            // 添加状态
            if (action !== 'copy') that.appendLinkStatus(enterLinkG);
            // 添加线条label
            that.appendLinkLabel(enterLinkG);

            // 添加事件
            enterLinkG
                .on('click', function () { // 点击操作
                    if (that.options.readOnly) return false;
                    d3.event.stopPropagation();
                    var isActive = d3.select(this).classed('active');
                    if (isActive) {
                        d3.select(this).dispatch('reSelected');
                    } else {
                        d3.select(this).dispatch('selected');
                    }
                })
                .on('mouseenter', function () {
                    var link = d3.select(this);
                    link.raise();
                    link.selectAll('.btn').dispatch('show');
                })
                .on('mouseleave', function () {
                    var link = d3.select(this);
                    link.raise();
                    link.selectAll('.btn').dispatch('hide');
                });

            return enterLinkG;
        },
        appendLinkStatus: function (linkG) { //  link添加操作 click selected reSelected remove
            var that = this;
            if (!linkG) return false;
            var durationTime = that.options.durationTime || 600;
            linkG = that.selectAll(linkG);

            linkG
                .on('selected', function (d) { // 选中状态
                    // console.log('selected');
                    d.selected = 'rgb(0, 102, 153)';
                    var link = d3.select(this);
                    var _this = this;
                    link
                        .classed('active', true)
                        .selectAll('.v-line')
                        .attr('stroke-width', 1.2)
                        .attr('stroke', d.selected)
                        .attr('marker-end', `url(#${that.getArrowColor(d.selected)})`);
                    link.selectAll('.labelG .btn').dispatch('show');
                    // 添加可拖动地
                    that.linkDragAnchor(this);
                    // 获取锚点
                    var sourceAnchor = that.getNodeAnchor(d.source, d.out.position);
                    var targetAnchor = that.getNodeAnchor(d.target, d.in.position);

                    sourceAnchor
                        .each(function (d) {
                            var index = d.select.indexOf(_this);
                            if (index === -1) d.select.push(_this);
                        })
                        .attr('stroke', d.selected);
                    targetAnchor
                        .each(function (d) {
                            var index = d.select.indexOf(_this);
                            if (index === -1) d.select.push(_this);
                        })
                        .attr('stroke', d.selected)
                        .dispatch('show');
                })
                .on('reSelected', function (d) { // 取消选中状态
                    // console.log('reSelected');
                    d.selected = '';
                    var link = d3.select(this);
                    var _this = this;
                    link
                        .classed('active', false)
                        .selectAll('.v-line')
                        .attr('stroke-width', 1)
                        .attr('stroke', d.stroke)
                        .attr('marker-end', `url(#${that.getArrowColor(d.stroke)})`);
                    link.selectAll('.labelG .btn').dispatch('hide');
                    d3.select(this).selectAll('.dragAnchorG').classed('hide', true);
                    // 锚点
                    var sourceAnchor = that.getNodeAnchor(d.source, d.out.position);
                    var targetAnchor = that.getNodeAnchor(d.target, d.in.position);
                    sourceAnchor
                        .each(function (d) {
                            var index = d.select.indexOf(_this);
                            if (index !== -1) d.select.splice(index, 1);
                        })
                        .dispatch('style');
                    targetAnchor
                        .each(function (d) {
                            var index = d.select.indexOf(_this);
                            if (index !== -1) d.select.splice(index, 1);
                        })
                        .dispatch('style')
                        .dispatch('hide');
                })
                .on('error', function (d) { // 错误连线
                    d.status = 'error';
                    var link = d3.select(this);
                    link
                        .dispatch('interrupt')
                        .selectAll('.v-line')
                        .attr('stroke', '#e42332')
                        .attr('marker-end', `url(#arrow_r_e${that.svgId})`);
                })
                .on('reminder', function (d) { // 提示
                    d.status = 'reminder';
                    var link = d3.select(this);
                    var vLine = link.selectAll('.v-line');
                    var time = d3.event.detail && d3.event.detail.time ? d3.event.detail.time : durationTime;
                    link.dispatch('interrupt');
                    // vLine.attr('stroke-dasharray', 0);

                    var count = 0;
                    transition();

                    function transition() {
                        vLine.transition()
                            .duration(time)
                            .attr('stroke', '#e42332')
                            // .attr('marker-end', `url(#arrow_r_e${that.svgId})`)
                            .transition()
                            .duration(time)
                            .attr('stroke', d.selected || d.stroke)
                            // .attr('marker-end', `url(#${that.getArrowColor(d.selected || d.stroke)})`)
                            .on('end', function () {
                                count++;
                                if (count < 3) transition();
                            });
                    }
                })
                .on('running', function (d) {
                    d.status = 'running';
                    var link = d3.select(this);
                    var time = d3.event.detail && d3.event.detail.time ? d3.event.detail.time : 800;
                    var totalLength = 0;
                    link
                        .selectAll('.v-line')
                        .each(function () {
                            totalLength = this.getTotalLength();
                        })
                        .attr('stroke-dasharray', `${totalLength / 2}`);
                    transition();

                    function transition() {
                        link
                            .selectAll('.v-line')
                            .attr('stroke-dashoffset', `${totalLength / 2}`)
                            .transition('running')
                            .duration(time)
                            .ease(d3.easeLinear)
                            .attr('stroke-dashoffset', `${-totalLength / 2}`)
                            .on('end', function () {
                                transition();
                            });
                    }
                })
                .on('interrupt', function (d) {
                    var link = d3.select(this);
                    link.selectAll('.v-line')
                        .interrupt('running')
                        // .attr('stroke-dasharray', 0)
                        // .attr('stroke-dashoffset', 0);
                })
                .on('hide', function (d) { // hide
                    var link = d3.select(this);
                    link
                        .attr('opacity', 1)
                        .transition('hide')
                        .duration(300)
                        .attr('opacity', 0)
                        .on('start', function () {
                            var allLinks = that.getAllLinkData();
                            var is = allLinks.some(function (d1) {
                                return (d1 !== d) && (d1.source === d.source) && (d1.out.position === d.out.position);
                            });
                            if (!is) {
                                var sourceNode = that.getNodeByUid(d.data.from);
                                var nodeAnchor = that.getNodeAnchor(sourceNode, d.out.position);
                                nodeAnchor.dispatch('hide', {detail: {show: false}});
                            }
                        })
                        .on('end', function () {
                            delete d.out.show;
                            link.classed('hide', true);
                        });
                })
                .on('show', function (d) { // show
                    var link = d3.select(this);
                    link
                        .attr('opacity', 0)
                        .transition('show')
                        .duration(300)
                        .attr('opacity', 1)
                        .on('start', function () {
                            link.classed('hide', false);
                            var soruceNode = that.getNodeByUid(d.data.from);
                            var anchor = that.getNodeAnchor(soruceNode, d.out.position);
                            anchor.dispatch('show', {detail: {show: true}});
                        });
                })
                .on('remove', function (d) { // 移除操作
                    var link = d3.select(this);
                    var time = d3.event.detail && d3.event.detail.time ? d3.event.detail.time : durationTime;
                    var totalLength = 0;
                    var _this = this;

                    // link.dispatch('interrupt');
                    link.selectAll('.v-line')
                        .each(function () {
                            totalLength = this.getTotalLength();
                        })
                        .attr('stroke-dashoffset', 0)
                        .attr('stroke-dasharray', totalLength)
                        .attr('marker-end', '')
                        .transition('remove')
                        .duration(time)
                        .attr('stroke-dashoffset', totalLength)
                        .on('start', function () {

                            link.selectAll('.labelG,.dragAnchorG').remove();
                            var allLinks = that.getAllLinkData();
                            var is = allLinks.some(function (d1) {
                                return (d1 !== d) && (d1.source === d.source) && (d1.out.position === d.out.position);
                            });
                            if (!is) {
                                var sourceNode = that.getNodeByUid(d.data.from);
                                var nodeAnchor = that.getNodeAnchor(sourceNode, d.out.position);
                                nodeAnchor.dispatch('hide', {detail: {show: false}});
                            }
                        })
                        .on('end', function () {
                            link.remove();
                        });

                    // target anchor hide
                    var sourceAnchor = that.getNodeAnchor(d.source, d.out.position);
                    var targetAnchor = that.getNodeAnchor(d.target, d.in.position);
                    sourceAnchor
                        .each(function (d) {
                            var index = d.select.indexOf(_this);
                            if (index !== -1) d.select.splice(index, 1);
                        })
                        .dispatch('style');
                    targetAnchor
                        .each(function (d) {
                            var index = d.select.indexOf(_this);
                            if (index !== -1) d.select.splice(index, 1);
                        })
                        .dispatch('style')
                        .dispatch('hide');
                    that.uuidArr.remove(d.id);
                })
                .on('add', function (d) { // 移除操作
                    var link = d3.select(this);
                    var time = d3.event.detail && d3.event.detail.time ? d3.event.detail.time : 800;
                    var totalLength = 0;
                    link.dispatch('interrupt');
                    link.selectAll('.v-line')
                        .each(function () {
                            totalLength = this.getTotalLength();
                        })
                        .attr('stroke-dashoffset', totalLength)
                        .attr('stroke-dasharray', totalLength)
                        .transition('remove')
                        .duration(time)
                        .attr('stroke-dashoffset', 0)
                        .on('start', function () {
                            d3.select(this).attr('marker-end', '');
                            link.selectAll('.labelG').classed('hide', true);
                        })
                        .on('end', function () {
                            d3.select(this).attr('marker-end', `url(#${that.getArrowColor(d.stroke)})`)
                                .attr('stroke-dasharray', d.strokeDasharray);
                            link.selectAll('.labelG').classed('hide', false);
                        });
                })
                .on('update', function (d) { // 线条更新
                    var link = d3.select(this);
                    if (!(d3.event.detail && d3.event.detail.drag)) {
                        that.switchDirection(d);
                    }
                    var pathStr = that.linkPathRadius(that.linkPathFilterDir(d.path));
                    link.select('.pathG').selectAll('path')
                        .attr('d', function () {
                            if (d3.select(this).classed('t-line')) {
                                var dxs = 0, dxt = 0;
                                if (d.out.position === 'top' || d.out.position === 'bottom') {
                                    dxs = d.source.width / 2;
                                } else if (d.out.position === 'right') {
                                    dxs = d.source.width;
                                }
                                if (d.in.position === 'top' || d.in.position === 'bottom') {
                                    dxt = d.target.width / 2;
                                } else if (d.in.position === 'right') {
                                    dxt = d.target.width;
                                }
                                if (d.source.x + dxs > d.target.x + dxt) {
                                    pathStr = that.linkPathRadius(that.linkPathFilterDir(d.path),'reverse');
                                }
                            }
                            return pathStr;
                        });
                        // .each(function (d) {
                        //     var vLink = d3.select(this);
                        //     if (vLink.classed('v-line')) {
                        //         vLink
                        //             .attr('stroke', d.stroke)
                        //             .attr('stroke-width', d.strokeWidth)
                        //             .attr('stroke-dasharray', d.strokeDasharray)
                        //             .attr('stroke-dashoffset', d.strokeDashoffset)
                        //             .attr('fill', d.fill);
                        //     }
                        // });
                    // 更新节点锚点
                    link.select('.dragAnchorG').dispatch('update');
                    // 更新节点label
                    link.select('.labelG').dispatch('update');
                })
                .on('runningDot', function (d) {
                    var link = d3.select(this);
                    var effectG = link.selectAll('.effectG').data([d]);
                    var effectEnterG = effectG.enter().append('g').attr('class', 'effectG');
                    effectG = effectG.merge(effectEnterG);
                    var textPath = effectG.selectAll('.textPath').data([d.lightDot]);
                    var textPathEnter = textPath.enter().append('text').attr('class', 'textPath').attr('dy', "0.34em");
                    textPath = textPath.merge(textPathEnter);
                    textPath.selectAll('textPath').data([d.lightDot]).enter().append('textPath').attr('xlink:href', `#link${d.id}-${that.svgId}`);

                    var dot = textPath.selectAll('textPath')
                        .attr('startOffset', '0')
                        .attr('font-size', function (d1) {
                            return d1.size;
                        })
                        .attr('fill', function (d1) {
                            return d1.color;
                        })
                        .text(function (d1) {
                            return d1.icon;
                        })
                        .attr('font-family', function (d1) {
                            return d1.iconFamily || 'ts';
                        });
                    var totalLength = link.select(`#link${d.id}-${that.svgId}`).node().getTotalLength();
                    var time = totalLength / d.lightDot.speed * 1000;
                    transition();

                    function transition() {
                        dot
                            .attr('startOffset', '0')
                            .transition('running')
                            .duration(time)
                            .ease(d3.easeLinear)
                            .attr('startOffset', '100%')
                            .on('end', function () {
                                transition();
                            });
                    }

                })
                .on('runStop', function () {
                    var link = d3.select(this);
                    link.selectAll('.effectG textPath').interrupt('running').remove();
                })
                .dispatch('add', {detail: {time: 800}});
        },
        reversePath: function (path) {
            if (Array.isArray(path)) {
                return path.map(function (d, i, arr) {
                    return {
                        x0: d.x1,
                        y0: d.y1,
                        x1: d.x0,
                        y1: d.y0,
                        dir: -d.dir
                    }
                }).sort(function () {
                    return -1;
                })
            } else return path;
        },
        appendLinkPathG: function (linkG) {
            var that = this;
            if (!linkG) return false;
            if (linkG.nodeType === 1) linkG = d3.selectAll(linkG);
            linkG.each(function (d) {
                var link = d3.select(this);
                var pathG = link.selectAll('.pathG').data([d]);
                var pathEnterG = pathG.enter().append('g')
                    .attr('class', 'pathG').attr('fill', 'none');
                pathG = pathG.merge(pathEnterG);
                var pathStr = that.linkPathRadius(that.linkPathFilterDir(d.path));
                // 实线
                pathEnterG.selectAll('.v-line').data([d]).enter()
                    .append('path').attr('class', 'v-line')
                    .attr('d', pathStr)
                    .attr('id', function (d) {
                        return `link${d.id}-${that.svgId}`;
                    });

                // 虚线
                pathEnterG.selectAll('.d-line').data([d]).enter()
                    .append('path').attr('class', 'd-line')
                    .attr('stroke-width', that.options.linkHoverWidth)
                    .attr('stroke', 'transparent')
                    .attr('d', pathStr);

                // 牵引线
                pathEnterG.selectAll('.t-line').data([d]).enter()
                    .append('path').attr('class', 't-line')
                    .attr('stroke-width', 1)
                    .attr('stroke', 'transparent')
                    .attr('id', function (d) {
                        return `linkT${d.id}-${that.svgId}`;
                    })
                    .attr('d', function () {
                        var dxs = 0, dxt = 0;
                        if (d.out.position === 'top' || d.out.position === 'bottom') {
                            dxs = d.source.width / 2;
                        } else if (d.out.position === 'right') {
                            dxs = d.source.width;
                        }
                        if (d.in.position === 'top' || d.in.position === 'bottom') {
                            dxt = d.target.width / 2;
                        } else if (d.in.position === 'right') {
                            dxt = d.target.width;
                        }
                        if (d.source.x + dxs > d.target.x + dxt) {
                            pathStr = that.linkPathRadius(that.linkPathFilterDir(d.path), 'reverse');
                        }
                        return pathStr;
                    });

                // 转移锚点
                pathEnterG.selectAll('.d-point').data([d, d]).enter()
                    .append('circle').attr('r', 0);

                // 节点锚点show
                var node = that.getNodeByUid(d.data.from);
                var nodeAnchor = that.getNodeAnchor(node, d.out.position);
                nodeAnchor.dispatch('show', {detail: {show: true}});
                // 添加触发style
                link
                    .on('style', function () {
                        var link = d3.select(this);
                        link.selectAll('.v-line')
                            .attr('stroke', d.stroke)
                            .attr('stroke-width', d.strokeWidth)
                            .attr('stroke-dasharray', d.strokeDasharray)
                            .attr('stroke-dashoffset', d.strokeDashoffset)
                            .attr('marker-end', `url(#${that.getArrowColor(d.stroke)})`)
                            .attr('fill', d.fill);
                    })
                    .dispatch('style');
            });
        },
        linkPathRadius: function (path, dir) {
            var that = this;
            if (!path || !Array.isArray(path)) return false;
            path = path.filter(function (d) {
                return !((d.x0 === d.x1) && (d.y0 === d.y1));
            });
            var pathStr = '', radius = that.options.linkPathRadius;
            var pathArr = [];
            path.forEach(function (d, i, arr) {
                if (i === 0) {
                    // pathStr = `M${d.x0},${d.y0}`
                    pathArr.push({
                        type: 'M',
                        x: d.x0,
                        y: d.y0
                    });
                } else {
                    var prev = arr[i - 1];
                    if ((prev.x0 === prev.x1 && d.y0 === d.y1)) { // 与前比较 prev 垂直
                        var k2 = (prev.y0 > prev.y1) && (d.x0 < d.x1);
                        var k4 = (prev.y0 < prev.y1) && (d.x0 > d.x1);
                        var dy = d.y0 + (prev.y0 < prev.y1 ? -radius : radius);
                        var dx = d.x0 + (d.x0 < d.x1 ? radius : -radius);
                        // var flag = `0,0,${ k2 || k4 ? 1 : 0}`
                        // pathStr += `L${d.x0},${dy}A${radius},${radius},${flag},${dx},${d.y0}`
                        if (dir === 'reverse') {
                            pathArr.push({
                                type: 'A',
                                flag: `0,0,${ k2 || k4 ? 0 : 1}`,
                                x0: d.x0,
                                y0: d.y0,
                                x2: d.x0,
                                y2: dy,
                                x1: dx,
                                y1: d.y0,
                                r: radius
                            });
                        } else {
                            pathArr.push({
                                type: 'A',
                                flag: `0,0,${ k2 || k4 ? 1 : 0}`,
                                x0: d.x0,
                                y0: d.y0,
                                x1: d.x0,
                                y1: dy,
                                x2: dx,
                                y2: d.y0,
                                r: radius
                            });
                        }

                    } else if ((prev.y0 === prev.y1 && d.x0 === d.x1)) { // prev 水平
                        var k1 = (prev.x0 < prev.x1) && (d.y0 < d.y1);
                        var k3 = (prev.x0 > prev.x1) && (d.y0 > d.y1);
                        var dx = d.x0 + (prev.x0 < prev.x1 ? -radius : radius);
                        var dy = d.y0 + (d.y0 < d.y1 ? radius : -radius);
                        // var flag = `0,0,${ k1 || k3 ? 1 : 0}`
                        // pathStr += `L${dx},${d.y0}A${radius},${radius},${flag},${d.x0},${dy}`
                        if (dir === 'reverse') {
                            pathArr.push({
                                type: 'A',
                                flag: `0,0,${ k1 || k3 ? 0 : 1}`,
                                x0: d.x0,
                                y0: d.y0,
                                x2: dx,
                                y2: d.y0,
                                x1: d.x0,
                                y1: dy,
                                r: radius
                            });
                        } else {
                            pathArr.push({
                                type: 'A',
                                flag: `0,0,${ k1 || k3 ? 1 : 0}`,
                                x0: d.x0,
                                y0: d.y0,
                                x1: dx,
                                y1: d.y0,
                                x2: d.x0,
                                y2: dy,
                                r: radius
                            });
                        }

                    } else {
                        // pathStr += `L${d.x0},${d.y0}`
                        pathArr.push({
                            type: 'L',
                            x: d.x0,
                            y: d.y0
                        });
                    }
                }
                if (i === arr.length - 1) {
                    // pathStr += `L${d.x1},${d.y1}`
                    pathArr.push({
                        type: 'L',
                        x: d.x1,
                        y: d.y1
                    });
                }
            });
            // 处理圆角过大
            pathArr.forEach(function (d, i, arr) {
                if (d.type === 'A') {
                    var prev = arr[i - 1];
                    var next = arr[i + 1];
                    var rp = d.r, rn = d.r;
                    if (prev.type === 'A') {
                        if (prev.x0 !== d.x0 && Math.abs(prev.x0 - d.x0) < radius * 2) {
                            rp = Math.abs(prev.x0 - d.x0) / 2;
                        } else if (prev.y0 !== d.y0 && Math.abs(prev.y0 - d.y0) < radius * 2) {
                            rp = Math.abs(prev.y0 - d.y0) / 2;
                        }
                    }
                    if (next.type === 'A') {
                        if (next.x0 !== d.x0 && Math.abs(next.x0 - d.x0) < radius * 2) {
                            rn = Math.abs(next.x0 - d.x0) / 2;
                        } else if (next.y0 !== d.y0 && Math.abs(next.y0 - d.y0) < radius * 2) {
                            rn = Math.abs(next.y0 - d.y0) / 2;
                        }
                    }
                    if (rp !== d.r || rn !== d.r) {
                        var r = d3.min([rp, rn]);
                        if (d.x1 !== d.x0) d.x1 = d.x1 > d.x0 ? d.x0 + r : d.x0 - r;
                        if (d.y1 !== d.y0) d.y1 = d.y1 > d.y0 ? d.y0 + r : d.y0 - r;
                        if (d.x2 !== d.x0) d.x2 = d.x2 > d.x0 ? d.x0 + r : d.x0 - r;
                        if (d.y2 !== d.y0) d.y2 = d.y2 > d.y0 ? d.y0 + r : d.y0 - r;
                        d.r = r;
                    }
                }
            });
            if (dir === 'reverse') {
                pathArr.reverse();
                pathArr[0].type = 'M';
                pathArr[pathArr.length - 1].type = 'L';
            }
            pathArr.forEach(function (d) {
                if (d.type === 'A') {
                    pathStr += `L${d.x1},${d.y1}A${d.r},${d.r},${d.flag},${d.x2},${d.y2}`;
                } else pathStr += `${d.type}${d.x},${d.y}`;
            });
            return pathStr;
        },
        linkDragAnchor: function (linkG) {
            var that = this;
            if (!linkG) return false;
            linkG = this.selectAll(linkG);
            var pathData = linkG.datum().path;
            var firstData = pathData[0], lastData = pathData[pathData.length - 1], prevData, nextData;
            var dragable = [];

            pathData.forEach(function (d, i) {
                if (d === firstData || d === lastData) return false;
                prevData = pathData[i - 1];
                nextData = pathData[i + 1];
                if (d.x0 === d.x1) { // 垂直
                    if ((prevData.y0 === prevData.y1) && (nextData.y0 === nextData.y1)) {
                        if (Math.abs(d.y0 - d.y1) < that.options.linkDragAnchorHeight) return false;
                        else dragable.push(d);
                    }
                } else if (d.y0 === d.y1) { // 水平
                    if ((prevData.x0 === prevData.x1) && (nextData.x0 === nextData.x1)) {
                        if (Math.abs(d.x0 - d.x1) < that.options.linkDragAnchorHeight) return false;
                        else dragable.push(d);
                    }
                }
            });

            linkG.each(function (d) {
                var dragAnchorG = linkG.selectAll('.dragAnchorG').data([d]).classed('hide', false);
                var enterDragAnchor = dragAnchorG.enter().append('g').attr('class', 'dragAnchorG');
                dragAnchorG.selectAll('rect').remove();
                var rect = dragAnchorG.merge(enterDragAnchor).selectAll('rect').data(dragable);
                var rectenter = rect.enter().append('rect')
                    .attr('fill', '#9f9f9f')
                    .attr('rx', '3')
                    .attr('ry', '3')
                    .attr('fill-opacity', 0.6)
                    .call(that.linkDrag)
                    .on('mouseenter', function (d) {
                        if (d.x0 === d.x1) {
                            d3.select(this).attr('cursor', 'e-resize');
                        } else if (d.y0 === d.y1) {
                            d3.select(this).attr('cursor', 's-resize').attr('');
                        }
                    });
                var exit = rect.exit().remove();
                dragAnchorG.merge(enterDragAnchor)
                    .on('update', function () {
                        var anchors = d3.select(this);
                        rectenter
                            .attr('x', function (d) {
                                return -that.options.linkDragAnchorWidth / 2;
                            })
                            .attr('y', function (d) {
                                return -that.options.linkDragAnchorHeight / 2;
                            })
                            .attr('width', that.options.linkDragAnchorWidth)
                            .attr('height', that.options.linkDragAnchorHeight)
                            .attr('transform', function (d) {
                                if (d.x0 === d.x1) return `translate(${(d.x0 + d.x1) / 2},${(d.y0 + d.y1) / 2})rotate(90)`;
                                return `translate(${(d.x0 + d.x1) / 2},${(d.y0 + d.y1) / 2})`;
                            });
                    })
                    .dispatch('update');
            });

        },
        appendLinkLabel: function (linkG) {
            var that = this;
            if (!linkG) return false;
            linkG = this.selectAll(linkG);
            var btnWidth = 14;
            linkG.each(function (d) {
                var _this = this;
                var btn = [];
                var label = [];
                if (d.behavior && !that.options.readOnly) {
                    if (d.behavior.deleteable) {
                        btn.push('delete');
                    }
                    if (d.behavior.editable && d.onEdit) {
                        btn.push('edit');
                    }
                }
                if (d.label && d.label.text) label.push(d.label);

                var link = d3.select(this);
                // label
                var labelG = link.selectAll('.labelG').data([d]);
                var enterLabelG = labelG.enter().append('g').attr('class', 'labelG');

                labelG = labelG.merge(enterLabelG);
                labelG.html('');
                var foreignObject = labelG.selectAll('.label').data(label)
                    .enter().append('g').attr('class', 'label');

                // foreignObject.append('rect')
                //     .attr('class', 'labelBg')
                //     .attr('width', that.options.linkLabelWidth).attr('height', 16)
                //     .attr('stroke', 'white').attr('stroke-width', 3)
                //     .attr('x', -that.options.linkLabelWidth / 2)
                //     .attr('y', -that.options.linkLabelHeight / 2)
                //     .attr('rx', 16 / 2)
                //     .attr('ry', 16 / 2)
                //     .attr('fill-opacity', d.label.fillopacity || 1)
                //     .attr('fill', function () {
                //         return d.label.bgColor || 'rgb(24, 161, 95)';
                //     });
                //
                // foreignObject.append('text')
                //     .attr('class', 'labelText')
                //     .attr('title', d.label.text)
                //     .attr('text-anchor', 'middle')
                //     .attr('dy', '0.37em')
                //     .attr('font-size', 10)
                //     .attr('fill', function () {
                //         return d.label.textColor || '#ffffff';
                //     })
                //     .text(function () {
                //         var chartLen = that.getStringChartLen(d.label.text);
                //         if (chartLen > 8) {
                //             return that.getCutStr(d.label.text, 8);
                //         } else return d.label.text;
                //     });

                // textPath
                var textPath = labelG.append('text')
                    .attr('font-size', 10)
                    .text(d.label.text)
                    .attr('class', 'labelTextPath')
                    .attr('fill', d.label.bgColor || '#4e4646')

                    // .attr('x', linkTotalLen / 2)
                    // .attr('text-anchor', 'middle')
                    .attr('dy', '0.32em');
                var textBox = textPath.node().getBBox();
                d.labelTextLen = textBox.width || 0;

                textPath.text('')
                    .append('textPath')
                    .text(d.label.text)
                    .attr('xlink:href', `#linkT${d.id}-${that.svgId}`);

                // btn
                var Btn = labelG.selectAll('.btn').data(btn);
                var enterBtn = Btn.enter().append('g').attr('class', 'btn')
                enterBtn.append('circle')
                    .attr('r', 7)
                    .attr('cx', function (d) {
                        if (d === 'delete') return 7.5;
                        else if (d === 'edit') return 7 - btnWidth - 6;
                    })
                    .attr('cy', 7)
                    .attr('stroke', d.linkBtnStroke)
                    .attr('stroke-width', 1)
                    .attr('fill', d.linkBtnFill);

                enterBtn.append('use')
                    .attr('width', function (d) {
                        if (d === 'delete') return btnWidth + 1;
                        else if (d === 'edit') return btnWidth;
                    })
                    .attr('height', function (d) {
                        if (d === 'delete') return btnWidth + 1;
                        else if (d === 'edit') return btnWidth;
                    })
                    .attr('xlink:href', function (d) {
                        if (d === 'delete') return '#iconDelete' + that.svgId;
                        else if (d === 'edit') return '#iconEdit' + that.svgId;
                    })
                    .attr('x', function (d) {
                        if (d === 'delete') return 0;
                        else if (d === 'edit') return -btnWidth - 6;
                    })
                    .attr('y', function (d) {
                        if (d === 'delete') return -0.5;
                        else if (d === 'edit') return 0;
                    })
                    .attr('fill', 'white');

                // 添加事件
                enterBtn
                    .on('click', function (d1) {
                        d3.event.stopPropagation();
                        var oLink = that.getLinkObject(_this);
                        if (d1 === 'delete') {
                            link.dispatch('remove');
                            if (typeof d.onDelete === 'function') d.onDelete(oLink);
                        } else if (d1 === 'edit') {
                            if (typeof d.onEdit === 'function') d.onEdit(oLink);
                        }
                    })
                    .on('hide', function (d1) {
                        if (d.selected) return false;
                        var node = d3.select(this);
                        node.transition()
                            .attr('opacity', 1)
                            .duration(300)
                            .attr('opacity', 0)
                            .on('end', function () {
                                node.classed('hide', true);
                            });
                    })
                    .on('show', function () {
                        var node = d3.select(this);
                        node.transition()
                            .attr('opacity', 0)
                            .duration(300)
                            .attr('opacity', 1)
                            .on('start', function () {
                                node.classed('hide', false);
                            });
                    })
                    .classed('hide', true);
                enterLabelG
                    .on('click', function () {
                        // 阻止事件冒泡
                        d3.event.stopPropagation();
                    })
                    .on('update', function () {
                        var fp = that.linkPathFilterDir(d.path);
                        var linePath = that.filterLabelPosition(fp);
                        var cx = (linePath.x0 + linePath.x1) / 2;
                        var cy = (linePath.y0 + linePath.y1) / 2;
                        var labelName = labelG.selectAll('.label').attr('transform', function () {
                            return `translate(${cx},${cy})`;
                        });
                        labelG.selectAll('.btn').attr('transform', function () {
                            if (labelName.size() === 0) return `translate(${cx + 3},${cy - btnWidth - 3})`;
                            return `translate(${cx + 2},${cy - that.options.linkLabelHeight / 2 - btnWidth - 2})`;
                        });
                        var linkPath = link.selectAll('.v-line');
                        if (linkPath.size()) {
                            var linkTotalLen = linkPath.node().getTotalLength();
                            var padding = d.labelTextLen === 0 ? 0 : 3;
                            d.strokeDasharray = `${linkTotalLen / 2 - d.labelTextLen / 2 - padding} ${d.labelTextLen + padding * 2} ${linkTotalLen / 2 - d.labelTextLen / 2 - padding}`;
                            labelG.select('.labelTextPath').attr('dx', linkTotalLen / 2 - d.labelTextLen / 2);
                            linkPath.attr('stroke-dasharray', d.strokeDasharray)
                        }
                    });
                labelG.dispatch('update');
            });

            // function filterLabelPath(filterPath) {
            //
            // }
        },
        filterLabelPosition: function (filterPath) {
            var linePath;
            if (filterPath.length === 1) {
                linePath = filterPath[0];
            } else if (filterPath.length === 2) {
                var a = Math.max(Math.abs(filterPath[0].x0 - filterPath[0].x1), Math.abs(filterPath[0].y0 - filterPath[0].y1));
                var b = Math.max(Math.abs(filterPath[1].x0 - filterPath[1].x1), Math.abs(filterPath[1].y0 - filterPath[1].y1));
                if (a > b) {
                    linePath = filterPath[0];
                } else linePath = filterPath[1];
            } else if (filterPath.length === 3) {
                linePath = filterPath[1];
            } else if (filterPath.length === 4) {
                var a = Math.max(Math.abs(filterPath[1].x0 - filterPath[1].x1), Math.abs(filterPath[1].y0 - filterPath[1].y1));
                var b = Math.max(Math.abs(filterPath[2].x0 - filterPath[2].x1), Math.abs(filterPath[2].y0 - filterPath[2].y1));
                if (a > b) {
                    linePath = filterPath[1];
                } else linePath = filterPath[2];
            } else if (filterPath.length === 5) {
                linePath = filterPath[2];
            }
            return linePath;
        },
        linkPathFilterDir: function (path) {
            if (!Array.isArray(path)) return false;
            var rePath = [];
            var durPath = Object.assign({}, path[0]);
            rePath.push(durPath);
            // var fixedValue, originalValue;
            // var first = path[0], last = path[path.length - 1];
            // console.log(first, last)
            var isSame = path.every(function (d1) {
                if (durPath.x0 === durPath.x1) return durPath.x0 === d1.x0 && d1.x0 === d1.x1;
                if (durPath.y0 === durPath.y1) return durPath.y0 === d1.y0 && d1.y0 === d1.y1;
            });
            if (isSame) {
                var last = path[path.length - 1];
                rePath.push({x0: durPath.x1, y0: durPath.y1, x1: last.x0, y1: last.y0});
                rePath.push(last);
            } else {
                path.forEach(function (d) {
                    var isx = (durPath.x0 === d.x1);
                    var isy = (durPath.y0 === d.y1);
                    if (isx) {
                        // if (d.fixed) delete d.fixed;
                        // if (fixedValue) {
                        //     delete durPath.fixed
                        //     if (fixedValue[0] === 'x') {
                        //         durPath.x0 = durPath.x1 = originalValue
                        //     } else if (fixedValue[0] === 'y') {
                        //         durPath.y0 = durPath.y1 = originalValue
                        //     }
                        // }
                        // delete durPath.fixed;
                        durPath.y1 = d.y1;
                    } else if (isy) {
                        // if (d.fixed) delete d.fixed;
                        // if (fixedValue) {
                        //     delete durPath.fixed
                        //     if (fixedValue[0] === 'x') {
                        //         durPath.x0 = durPath.x1 = originalValue
                        //     } else if (fixedValue[0] === 'y') {
                        //         durPath.y0 = durPath.y1 = originalValue
                        //     }
                        // }
                        // delete durPath.fixed;
                        durPath.x1 = d.x1;
                    } else {
                        durPath = Object.assign({}, d);
                        // if (d.fixed) durPath.origin = d;
                        // if (d.fixed) {
                        //     if (d.fixed[0] === 'x') {
                        //         durPath.x0 = durPath.x1 = d.fixed[1]
                        //     } else if (d.fixed[0] === 'y') {
                        //         durPath.y0 = durPath.y1 = d.fixed[1]
                        //     }
                        //     originalValue = d.fixed[1]
                        //     fixedValue = d.fixed
                        // } else if (fixedValue) {
                        //     if (fixedValue[0] === 'x') {
                        //         durPath.x0 = durPath.x1 = fixedValue[1]
                        //     } else if (fixedValue[0] === 'y') {
                        //         durPath.y0 = durPath.y1 = fixedValue[1]
                        //     }
                        //     fixedValue = ''
                        // }
                        rePath.push(durPath);
                    }
                    // delete d.fixed
                });
            }

            // 处理fixed问题
            // this.linkPathDealFixed(rePath, path)
            return rePath;
        },
        linkPathDealFixed: function (repath, path) {
            // console.log(repath, path)
            // var fixed = '';
            var nextPath, prevPath;
            var first = repath[0];
            var last = repath[repath.length - 1];
            var start = path[0], end = path[path.length - 1];
            repath.forEach(function (d, i, arr) {
                if (d.fixed) {
                    prevPath = arr[i - 1];
                    nextPath = arr[i + 1];
                    var isFirst = prevPath === first;
                    var isLast = nextPath === last;
                    // console.log(isFirst, isLast)
                    if (d.fixed[0] === 'x') {
                        if (isFirst) {
                            if (start.dir === 2 && start.x1 > d.fixed[1]) {
                                // console.log(1111);
                                delete d.origin.fixed;
                                console.log(d);
                                return false;
                            }
                            else if (start.dir === -2 && start.x1 < d.fixed[1]) {
                                console.log(222);
                                delete d.origin.fixed;
                                return false;
                            }
                        }
                        if (isLast) {
                            if (end.dir === 2 && end.x0 > d.fixed[1]) {
                                console.log(333);
                                delete d.origin.fixed;
                                return false;
                            }
                            else if (end.dir === -2 && end.x0 < d.fixed[1]) {
                                console.log(444);
                                delete d.origin.fixed;
                                return false;
                            }
                        }
                        d.x0 = d.x1 = d.fixed[1];
                        nextPath.x0 = d.x0;
                    } else if (d.fixed[0] === 'y') {
                        if (isFirst) {
                            if (start.dir === 1 && start.y1 > d.fixed[1]) {
                                delete d.origin.fixed;
                                console.log(555);
                                return false;
                            }
                            else if (start.dir === -1 && start.y1 < d.fixed[1]) {
                                delete d.origin.fixed;
                                console.log(666);
                                return false;
                            }
                        }
                        if (isLast) {
                            if (end.dir === 1 && end.y0 > d.fixed[1]) {
                                console.log(777);
                                delete d.origin.fixed;
                                return false;
                            }
                            else if (end.dir === -1 && end.y0 < d.fixed[1]) {
                                console.log(888);
                                delete d.origin.fixed;
                                return false;
                            }
                        }
                        d.y0 = d.y1 = d.fixed[1];
                        nextPath.y0 = d.y0;
                    }
                    // else if (d.fixed[0] === 'x0') {
                    //     d.x0 = fixed[1]
                    // } else if (d.fixed[0] === 'y0') {
                    //     d.y0 = fixed[1]
                    // }
                    // fixed = d.fixed
                }
                // if (fixed) {
                //     if (fixed[0] === 'x') {
                //         d.x0 = fixed[1]
                //     } else if (fixed[0] === 'y') {
                //         d.y0 = fixed[1]
                //     }
                //     fixed = ''
                // }
            });
        },
        switchDirection: function (linkData) {
            if (!linkData) return false;
            var that = this;
            var offset = that.options.anchorPointOffset || 0;
            var link = linkData,
                source = linkData.source,
                target = linkData.target,
                pathMin = this.options.anchorPathMinLength,
                path = linkData.path,
                first, last;
            if (!Array.isArray(path)) return false;
            var anchorX = 0, anchorY = 0;
            // 起始描点线
            if (link.out.position === 'top') {
                anchorX = source.x + source.width / 2;
                anchorY = source.y - offset;
                first = {
                    x0: anchorX,
                    y0: anchorY,
                    x1: anchorX,
                    y1: anchorY - pathMin,
                    dir: -1
                };
            } else if (link.out.position === 'left') {
                anchorX = source.x - offset;
                anchorY = source.y + source.height / 2;
                first = {
                    x0: anchorX,
                    y0: anchorY,
                    x1: anchorX - pathMin,
                    y1: anchorY,
                    dir: -2
                };
            } else if (link.out.position === 'bottom') {
                anchorX = source.x + source.width / 2;
                anchorY = source.y + source.height + offset;
                first = {
                    x0: anchorX,
                    y0: anchorY,
                    x1: anchorX,
                    y1: anchorY + pathMin,
                    dir: 1
                };
            } else { // 右边和兼容老版本
                anchorX = source.x + source.width + offset;
                anchorY = source.y + source.height / 2;
                first = {
                    x0: anchorX,
                    y0: anchorY,
                    x1: anchorX + pathMin,
                    y1: anchorY,
                    dir: 2
                };
            }
            // 结束描点线
            if (link.in.position === 'top') {
                anchorX = target.x + target.width / 2;
                anchorY = target.y - offset;
                last = {
                    x1: anchorX,
                    y1: anchorY,
                    x0: anchorX,
                    y0: anchorY - pathMin,
                    dir: -1
                };
            } else if (link.in.position === 'left') {
                anchorX = target.x - offset;
                anchorY = target.y + target.height / 2;
                last = {
                    x1: anchorX,
                    y1: anchorY,
                    x0: anchorX - pathMin,
                    y0: anchorY,
                    dir: -2
                };
            } else if (link.in.position === 'bottom') {
                anchorX = target.x + target.width / 2;
                anchorY = target.y + target.height + offset;
                last = {
                    x1: anchorX,
                    y1: anchorY,
                    x0: anchorX,
                    y0: anchorY + pathMin,
                    dir: 1
                };
            } else { // 右边和兼容老版本
                anchorX = target.x + target.width + offset;
                anchorY = target.y + target.height / 2;
                last = {
                    x1: anchorX,
                    y1: anchorY,
                    x0: anchorX + pathMin,
                    y0: anchorY,
                    dir: 2
                };
            }
            // 中间点
            var center = {
                x: (first.x1 + last.x0) / 2,
                y: (first.y1 + last.y0) / 2
            };

            var midPointsArr = [];
            if (first.dir + last.dir === 0) { // 通过两向量夹角来判断折线路径
                var k1 = ((first.y1 < last.y0) && (first.x1 < last.x0)),
                    k3 = ((first.y1 > last.y0) && (first.x1 > last.x0)),
                    k2 = ((first.y1 < last.y0) && (first.x1 > last.x0)),
                    k4 = ((first.y1 > last.y0) && (first.x1 < last.x0));

                if (Math.abs(first.dir) === 1) {  // 垂直反向
                    if (first.dir > 0) {
                        if (k3 || k4) midPointsArr = that.pathMidPoints(first, center, last, 5);
                        else midPointsArr = that.pathMidPoints(first, center, last, 3);
                    } else {
                        if (k1 || k2) midPointsArr = that.pathMidPoints(first, center, last, 5);
                        else midPointsArr = that.pathMidPoints(first, center, last, 3);
                    }
                } else if (Math.abs(first.dir) === 2) {  // 水平反向
                    if (first.dir > 0) {
                        if (k2 || k3) midPointsArr = that.pathMidPoints(first, center, last, 5);
                        else midPointsArr = that.pathMidPoints(first, center, last, 3);
                    } else {
                        if (k1 || k4) midPointsArr = that.pathMidPoints(first, center, last, 5);
                        else midPointsArr = that.pathMidPoints(first, center, last, 3);
                    }
                }
            } else if (first.dir === last.dir) { // 同向
                midPointsArr = that.pathMidPoints(first, center, last, 3);
            } else { // 垂直和水平交叉
                var k1 = ((first.y1 < last.y0) && (first.x1 < last.x0)),
                    k3 = ((first.y1 > last.y0) && (first.x1 > last.x0)),
                    k2 = ((first.y1 < last.y0) && (first.x1 > last.x0)),
                    k4 = ((first.y1 > last.y0) && (first.x1 < last.x0));

                var is = ((first.dir === -1) && ((last.dir === -2 && k4) || (last.dir === 2 && k3)))
                    || ((first.dir === 1) && ((last.dir === -2 && k1) || (last.dir === 2 && k2)))
                    || ((first.dir === -2) && ((last.dir === -1 && k2) || (last.dir === 1 && k3)))
                    || ((first.dir === 2) && ((last.dir === -1 && k1) || (last.dir === 1 && k4)));
                if (is) midPointsArr = that.pathMidPoints(first, center, last, 2);
                else {
                    midPointsArr = that.pathMidPoints(first, center, last, 4, [k1, k2, k3, k4]);
                }

            }
            if (path.length === 0) {
                path[0] = first;
                path[1] = midPointsArr[0];
                path[2] = midPointsArr[1];
                path[3] = midPointsArr[2];
                path[4] = last;
            } else {
                // if (path[0]) {
                //     that.pathFixed(path[0], first)
                //     // Object.assign(, first)
                // }
                // if (path[1]) {
                //     that.pathFixed(path[1], midPointsArr[0])
                //     // Object.assign(, first)
                // }
                // if (path[2]) {
                //     that.pathFixed(path[2], midPointsArr[1])
                //     // Object.assign(, first)
                // }
                // if (path[3]) {
                //     that.pathFixed(path[3], midPointsArr[2])
                //     // Object.assign(, first)
                // }
                // if (path[4]) {
                //     that.pathFixed(path[4], last)
                //     // Object.assign(, first)
                // }
                // path.forEach(function (d) {
                //     console.log(d)
                // })
                // midPointsArr.unshift(first)
                // midPointsArr.push(last)
                if (path[0]) Object.assign(path[0], first);
                if (path[1]) Object.assign(path[1], midPointsArr[0]);
                if (path[2]) Object.assign(path[2], midPointsArr[1]);
                if (path[3]) Object.assign(path[3], midPointsArr[2]);
                if (path[4]) Object.assign(path[4], last);
                // console.log(path)
            }
        },
        pathFixed: function (path1, path2) {
            // if (path1.x0 === path2.x1 && (path1.y0 === path2.x1))
            if (path1.fixed === 'x' && path2.x0 === path2.x1) {
                path1.y0 = path2.y0;
                path1.y1 = path2.y1;
            } else if (path1.fixed === 'y' && path2.y0 === path2.y1) {
                path1.x0 = path2.x0;
                path1.x1 = path2.x1;
            } else if (path1.fixed) {

            }
            if (path1.fixed === 'x' && path2.x0 === path2.x1) {
                path1.y0 = path2.y0;
                path1.y1 = path2.y1;
            } else if (path1.fixed == 'y' && path2.y0 === path2.y1) {
                path1.x0 = path2.x0;
                path1.x1 = path2.x1;
            } else if (path1.fixed === 'x0' && path2.y0 === path2.y1) {
                path1.y0 = path2.y0;
                path1.y1 = path2.y1;
                path1.x1 = path2.x1;
            } else if (path1.fixed === 'x1' && path2.y0 === path2.y1) {
                path1.y0 = path2.y0;
                path1.y1 = path2.y1;
                path1.x0 = path2.x0;
            } else if (path1.fixed === 'y0' && path2.x0 === path2.x1) {
                path1.y1 = path2.y1;
                path1.x0 = path2.x0;
                path1.x1 = path2.x1;
            } else if (path1.fixed === 'y1' && path2.x0 === path2.x1) {
                path1.y0 = path2.y0;
                path1.x0 = path2.x0;
                path1.x1 = path2.x1;
                // console.log(path1, path2);
            } else {
                Object.assign(path1, path2);
            }
        },
        pathMidPoints: function (first, center, last, num, kArr) {
            var point1, point2, point3;
            switch (num) {
                case 2:
                    if (Math.abs(first.dir) === 1) {
                        point1 = {
                            x0: first.x1,
                            y0: first.y1,
                            x1: first.x1,
                            y1: last.y0,
                            dir: first.dir
                        };
                        point2 = {
                            x0: first.x1,
                            y0: last.y0,
                            x1: first.x1,
                            y1: last.y0,
                            dir: first.dir
                        };
                        point3 = {
                            x0: first.x1,
                            y0: last.y0,
                            x1: last.x0,
                            y1: last.y0,
                            dir: -first.dir
                        };
                    }
                    if (Math.abs(first.dir) === 2) {
                        point1 = {
                            x0: first.x1,
                            y0: first.y1,
                            x1: last.x0,
                            y1: first.y1,
                            dir: -last.dir
                        };
                        point2 = {
                            x0: last.x0,
                            y0: first.y1,
                            x1: last.x0,
                            y1: first.y1,
                            dir: first.dir
                        };
                        point3 = {
                            x0: last.x0,
                            y0: first.y1,
                            x1: last.x0,
                            y1: last.y0,
                            dir: last.dir
                        };
                    }

                    break;
                case 3:
                    if (first.dir === last.dir) {
                        var is = ((first.dir === -1) && (first.y1 > last.y0))
                            || ((first.dir === 1) && !(first.y1 > last.y0))
                            || ((first.dir === 2) && (first.x1 > last.x0))
                            || ((first.dir === -2) && !(first.x1 > last.x0));
                        if (is) {
                            point1 = {
                                x0: first.x1,
                                y0: first.y1,
                                x1: first.x1,
                                y1: last.y0,
                                dir: first.dir
                            };
                            point2 = {
                                x0: first.x1,
                                y0: last.y0,
                                x1: last.x0,
                                y1: last.y0,
                                dir: 3 - Math.abs(last.dir)
                            };
                            point3 = {
                                x0: last.x0,
                                y0: last.y0,
                                x1: last.x0,
                                y1: last.y0,
                                dir: last.dir
                            };
                        } else {
                            point1 = {
                                x0: first.x1,
                                y0: first.y1,
                                x1: first.x1,
                                y1: first.y1,
                                dir: first.dir
                            };
                            point2 = {
                                x0: first.x1,
                                y0: first.y1,
                                x1: last.x0,
                                y1: first.y1,
                                dir: 3 - Math.abs(first.dir)
                            };
                            point3 = {
                                x0: last.x0,
                                y0: first.y1,
                                x1: last.x0,
                                y1: last.y0,
                                dir: last.dir
                            };
                        }
                    } else if (Math.abs(first.dir) === 1) { //垂直方向
                        point1 = {
                            x0: first.x1,
                            y0: first.y1,
                            x1: first.x1,
                            y1: center.y,
                            dir: first.dir
                        };
                        point2 = {
                            x0: first.x1,
                            y0: center.y,
                            x1: last.x0,
                            y1: center.y,
                            dir: 3 - Math.abs(first.dir)
                        };
                        point3 = {
                            x0: last.x0,
                            y0: center.y,
                            x1: last.x0,
                            y1: last.y0,
                            dir: last.dir
                        };
                    } else if (Math.abs(first.dir) === 2) { // 水平方向
                        point1 = {
                            x0: first.x1,
                            y0: first.y1,
                            x1: center.x,
                            y1: first.y1,
                            dir: first.dir
                        };
                        point2 = {
                            x0: center.x,
                            y0: first.y1,
                            x1: center.x,
                            y1: last.y0,
                            dir: 3 - Math.abs(first.dir)
                        };
                        point3 = {
                            x0: center.x,
                            y0: last.y0,
                            x1: last.x0,
                            y1: last.y0,
                            dir: last.dir
                        };
                    }
                    break;
                case 4:
                    var k1 = kArr[0], k2 = kArr[1], k3 = kArr[2], k4 = kArr[3];
                    if (Math.abs(first.dir) === 1) {
                        var is1 = (first.dir === -1 && last.dir === -2 && k1)
                            || (first.dir === -1 && last.dir === 2 && k2)
                            || (first.dir === 1 && last.dir === 2 && k3)
                            || (first.dir === 1 && last.dir === -2 && k4);
                        var is2 = (first.dir === -1 && last.dir === -2 && k3)
                            || (first.dir === -1 && last.dir === 2 && k4)
                            || (first.dir === 1 && last.dir === 2 && k1)
                            || (first.dir === 1 && last.dir === -2 && k2);
                        if (is1) {
                            // console.log(11111111, 1)
                            point1 = {
                                x0: first.x1,
                                y0: first.y1,
                                x1: center.x,
                                y1: first.y1,
                                dir: 2
                            };
                            point2 = {
                                x0: center.x,
                                y0: first.y1,
                                x1: center.x,
                                y1: last.y0,
                                dir: 2
                            };
                            point3 = {
                                x0: center.x,
                                y0: last.y0,
                                x1: last.x0,
                                y1: last.y0,
                                dir: 2
                            };
                        } else if (is2) {
                            // console.log(2222222, 1)
                            point1 = {
                                x0: first.x1,
                                y0: first.y1,
                                x1: first.x1,
                                y1: center.y,
                                dir: 2
                            };
                            point2 = {
                                x0: first.x1,
                                y0: center.y,
                                x1: last.x0,
                                y1: center.y,
                                dir: 2
                            };
                            point3 = {
                                x0: last.x0,
                                y0: center.y,
                                x1: last.x0,
                                y1: last.y0,
                                dir: 2
                            };
                        } else {
                            // console.log(333333, 1)
                            point1 = {
                                x0: first.x1,
                                y0: first.y1,
                                x1: last.x0,
                                y1: first.y1,
                                dir: 2
                            };
                            point2 = {
                                x0: last.x0,
                                y0: first.y1,
                                x1: last.x0,
                                y1: first.y1,
                                dir: 0
                            };
                            point3 = {
                                x0: last.x0,
                                y0: first.y1,
                                x1: last.x0,
                                y1: last.y0,
                                dir: 1
                            };
                        }
                    } else if (Math.abs(first.dir) === 2) {
                        var is1 = (first.dir === -2 && last.dir === -1 && k1)
                            || (first.dir === 2 && last.dir === -1 && k2)
                            || (first.dir === 2 && last.dir === 1 && k3)
                            || (first.dir === -2 && last.dir === 1 && k4);
                        var is2 = (first.dir === -2 && last.dir === -1 && k3)
                            || (first.dir === 2 && last.dir === -1 && k4)
                            || (first.dir === 2 && last.dir === 1 && k1)
                            || (first.dir === -2 && last.dir === 1 && k2);
                        if (is2) {
                            // console.log(11111111111, 2)
                            point1 = {
                                x0: first.x1,
                                y0: first.y1,
                                x1: center.x,
                                y1: first.y1,
                                dir: 2
                            };
                            point2 = {
                                x0: center.x,
                                y0: first.y1,
                                x1: center.x,
                                y1: last.y0,
                                dir: 2
                            };
                            point3 = {
                                x0: center.x,
                                y0: last.y0,
                                x1: last.x0,
                                y1: last.y0,
                                dir: 2
                            };
                        } else if (is1) {
                            // console.log(2222222, 2)
                            point1 = {
                                x0: first.x1,
                                y0: first.y1,
                                x1: first.x1,
                                y1: center.y,
                                dir: 2
                            };
                            point2 = {
                                x0: first.x1,
                                y0: center.y,
                                x1: last.x0,
                                y1: center.y,
                                dir: 2
                            };
                            point3 = {
                                x0: last.x0,
                                y0: center.y,
                                x1: last.x0,
                                y1: last.y0,
                                dir: 2
                            };
                        } else {
                            // console.log(333333, 2)
                            point1 = {
                                x0: first.x1,
                                y0: first.y1,
                                x1: first.x1,
                                y1: last.y0,
                                dir: 2
                            };
                            point2 = {
                                x0: first.x1,
                                y0: last.y0,
                                x1: first.x1,
                                y1: last.y0,
                                dir: 2
                            };
                            point3 = {
                                x0: first.x1,
                                y0: last.y0,
                                x1: last.x0,
                                y1: last.y0,
                                dir: 2
                            };
                        }
                    }
                    break;
                case 5:
                    if (Math.abs(first.dir) === 1) { // 垂直反向
                        point1 = {
                            x0: first.x1,
                            y0: first.y1,
                            x1: center.x,
                            y1: first.y1,
                            dir: 3 - Math.abs(first.dir)
                        };
                        point2 = {
                            x0: center.x,
                            y0: first.y1,
                            x1: center.x,
                            y1: last.y0,
                            dir: first.dir
                        };
                        point3 = {
                            x0: center.x,
                            y0: last.y0,
                            x1: last.x0,
                            y1: last.y0,
                            dir: 3 - Math.abs(first.dir)
                        };
                    } else if (Math.abs(first.dir) === 2) { // 水平反向
                        point1 = {
                            x0: first.x1,
                            y0: first.y1,
                            x1: first.x1,
                            y1: center.y,
                            dir: 3 - Math.abs(first.dir)
                        };
                        point2 = {
                            x0: first.x1,
                            y0: center.y,
                            x1: last.x0,
                            y1: center.y,
                            dir: first.dir
                        };
                        point3 = {
                            x0: last.x0,
                            y0: center.y,
                            x1: last.x0,
                            y1: last.y0,
                            dir: 3 - Math.abs(first.dir)
                        };
                    }
                    break;
            }
            return [point1, point2, point3];
        },
        segmentsIntr: function (a, b, c, d, action) {
            // 三角形abc 面积的2倍
            var area_abc = (a.x - c.x) * (b.y - c.y) - (a.y - c.y) * (b.x - c.x);

            // 三角形abd 面积的2倍
            var area_abd = (a.x - d.x) * (b.y - d.y) - (a.y - d.y) * (b.x - d.x);

            // 面积符号相同则两点在线段同侧,不相交 (对点在线段上的情况,本例当作不相交处理);
            if (area_abc * area_abd >= 0) {
                return false;
            }

            // 三角形cda 面积的2倍
            var area_cda = (c.x - a.x) * (d.y - a.y) - (c.y - a.y) * (d.x - a.x);
            // 三角形cdb 面积的2倍
            // 注意: 这里有一个小优化.不需要再用公式计算面积,而是通过已知的三个面积加减得出.
            var area_cdb = area_cda + area_abc - area_abd;
            if (area_cda * area_cdb >= 0) {
                return false;
            }
            if (action === 'trueOrFalse') {
                return true;
            }
            //计算交点坐标
            var t = area_cda / (area_abd - area_abc);
            var dx = t * (b.x - a.x),
                dy = t * (b.y - a.y);
            return {x: a.x + dx, y: a.y + dy};
        },
        getLinkData: function (link) {
            if (!link) return false;
            var linkData;
            if (typeof link.node === 'function' && link.size()) linkData = link.datum();
            else if (link.data) linkData = link;
            else if (link.nodeType === 1) linkData = d3.select(link).datum();
            if (!linkData) return false;
            this.setLinkData(linkData);
            return linkData.data;
        },
        setLinkData: function (link) {
            if (!link) return false;
            var linkData;
            if (typeof link.node === 'function' && link.size()) linkData = link.datum();
            else if (link.data) linkData = link;
            else if (link.nodeType === 1) linkData = d3.select(link).datum();
            if (!linkData) return false;
            linkData.data['path'] = linkData.path;
            return linkData;
        },
        getLinksByFrom: function (uid) {
            if (typeof uid !== 'string') return false;
            var links = this.linksG.selectAll('.linkG').filter(function (d) {
                return d.data.from === uid;
            });
            return links;
        },
        getLinksByTo: function (uid) {
            if (typeof uid !== 'string') return false;
            var links = this.linksG.selectAll('.linkG').filter(function (d) {
                return d.data.to === uid;
            });
            return links;
        },
        getLinkByUid: function (uid) {
            if (this.consoleLog(uid, '不存在该uid节点')) return false;
            return this.linksG.selectAll('.linkG').filter(function (d) {
                return d.data.id === uid;
            });
        },
        getLinksByData: function (data) { // link data
            var that = this;
            if (!data) return false;
            if (!Array.isArray(data)) data = [data];
            return this.linksG.selectAll('.linkG').filter(function (d) {
                // return data.includes(d);
                return that.include(data, d);
            });
        },
        hideLink: function (link) {
            if (!link) return false;
            if (link.nodeType === 1) link = d3.select(link);
            if (link.classed('linkG')) {
                link.classed('hide', true);
            }
        },
        showLink: function (link) {
            if (!link) return false;
            if (link.nodeType === 1) link = d3.select(link);
            if (link.classed('linkG')) {
                link.classed('hide', false);
            }
        },
        isLinkSame: function (linkData) {
            if (!linkData) return false;
            var linksData = this.getAllLinkData();
            var is = linksData.filter(function (d) {
                return (d.id !== linkData.id) && (d.data.from === linkData.data.from) && (d.data.to === linkData.data.to);
            });
            return is;
        },
        isLinkSelf: function (linkData) {
            if (!linkData) return false;
            return linkData.data.from === linkData.data.to;
        },
        isLinkLoop: function (linkG) {
            var that = this;
            if (!linkG) return false;
            if (linkG.nodeType === 1) linkG = d3.selectAll(linkG);
            if (typeof linkG.node === 'function') linkG = linkG.data();
            else if (!Array.isArray(linkG)) linkG = [linkG];
            var is = false;
            var linkDataArr = [];
            var branch = [];
            linkG.forEach(function (d) {
                if (!d.data) return false;
                var linksData = that.getAllLinkData();
                var nextLink = that.getLinksByFrom(d.target.id).data();
                if (!that.include(linksData, d)) {
                    var same = linksData.find(function (d1) {
                        return d1.id === d.id;
                    });
                    var index = linksData.indexOf(same);
                    if (index !== -1) linksData.splice(index, 1);
                    linksData.push(d);
                }
                nextLink.forEach(function (d1) {
                    if (is) return false;
                    linkDataArr.length = 0;
                    branch.length = 0;
                    findNext(d1);
                });

                function findNext(linkData) {
                    if (that.include(linkDataArr, linkData) || is) {
                        is = true;
                        return false;
                    } else {
                        if (loopCondition(linkData)) return false;
                        var prev = linksData.filter(function (d1) {
                            return d1.source === linkData.target;
                        });
                        linkDataArr.push(linkData);
                        if (prev.length) {
                            prev.forEach(function (d, i) {
                                if (prev.length > 1) {
                                    branch.push(d);
                                }
                                findNext(d);
                                if (prev.length > 1 && !is) {
                                    var index = linkDataArr.indexOf(branch.pop());
                                    if (index !== -1) linkDataArr.splice(index);
                                }
                            });
                        } else {

                        }
                    }
                }
            });
            function loopCondition (data) {
                if (data.target.loop || data.source.loop) return true;
                return false;
            }
            return {
                is: is,
                arr: linkDataArr
            };
        },
    });

})(window);