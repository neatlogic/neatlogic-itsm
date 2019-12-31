(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {}), global));
}(this, (function (exports, global) {
    var Item = function (base) {
        this.type = 'item';
        this.base = base;
        this.init();
    }
    Item.prototype = {
        constructor: Item,
        init: function () {
            this.base.getVm('svg').container.classed('left-block', true);
            this.container = this.base.container.append('xhtml:div').attr('class', 'itempannel');
            this.pannel = this.container.append('xhtml:div').attr('class', 'pannel');
            this.container.html(function () {
                return `
                    <div class="pannel">
                        <div class="item" title="qqqq" style="cursor: pointer;">
                            <i class="icon iconfont icon-fit"></i><span>fdasdfasdfasdf</span>
                        </div>
                        <div class="item" title="qqqq">
                            <i class="icon iconfont icon-fit"></i><span>fdasdfasdfasdf</span>
                        </div>
                    </div>
                    
                    
                    <div>
                    
                        <!--<img draggable="false" src="https://gw.alipayobjects.com/zos/rmsportal/ZnPxbVjKYADMYxkTQXRi.svg"-->
                             <!--data-type="node" data-shape="flow-circle" data-size="72*72" data-color="#FA8C16" data-label="起止节点"-->
                             <!--class="getItem">-->
                        <!--<img draggable="false" src="https://gw.alipayobjects.com/zos/rmsportal/wHcJakkCXDrUUlNkNzSy.svg"-->
                             <!--data-type="node" data-shape="flow-rect" data-size="80*48"-->
                             <!--data-color="#1890FF" data-label="常规节点" class="getItem">-->
                        <!--<img draggable="false" src="https://gw.alipayobjects.com/zos/rmsportal/SnWIktArriZRWdGCnGfK.svg"-->
                             <!--class="getItem">-->
                        <!--<img draggable="false" src="https://gw.alipayobjects.com/zos/rmsportal/rQMUhHHSqwYsPwjXxcfP.svg"-->
                             <!--data-type="node" data-shape="flow-capsule" data-size="80*48" data-color="#722ED1" data-label="模型节点"-->
                             <!--class="getItem">-->
                    </div>
                `
            })
        },
        render: function (data) {
            console.log(data);
            var that = this;
            var item = this.pannel.selectAll('.item').data(data, function (d) {
                return d;
            });
            var itemEnter = item.enter();
            var itemExit = item.exit().remove();

            var cloneItem, dx = 0, dy = 0;


            itemEnter
                .append('xhtml:div')
                .attr('class', 'item')
                .style('cursor', 'pointer')
                .attr('title', function (d) {
                    return d.title;
                })
                .html(function (d) {
                    return `
                        <i class="icon iconfont icon-fit"></i><span>${d.name}</span>
                    `
                })
                .call(d3.drag()
                    .on('start', function () {
                        dx += 0;
                        dy += d3.event.y - d3.mouse(this)[1];
                        cloneItem = d3.select(this).clone(true)
                            .classed('drag', true)
                            .style('position', 'absolute')
                            .style('top', dy + 'px')
                            .style('left', dx + 'px')
                            .style('z-index', 100)
                    })
                    .on('drag', function () {
                        dx += d3.event.dx;
                        dy += d3.event.dy;
                        cloneItem
                            .style('top', dy + 'px')
                            .style('left', dx + 'px')
                    })
                    .on('end', function (d) {
                        dx = 0, dy = 0;
                        cloneItem.remove();

                        // 创建节点
                        that.base.append(that.dataFn(d));

                    }))

        },
        getData: function (fn) {
            var that = this;
            if (typeof fn === 'function') {
                fn(function (data) {
                    that.render(data)
                }, function (error) {
                    that.error(error)
                });
            }
        },
        dataFn: function (data) {
            // obj
            var obj = {

            };
            var prc = d3.mouse(this.base.getVm('svg').el.node());
            obj.x = prc[0];
            obj.y = prc[1];
            return Object.assign(obj, data)
        }
    }
    exports.item = Item;
})));