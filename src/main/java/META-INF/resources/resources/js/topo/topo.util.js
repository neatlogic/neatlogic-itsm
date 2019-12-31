(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {})));
}(this, (function (exports) {
    'use strict';
    var opts = {
        type: 'util',
    };
    // 细碎逻辑处理
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
            this.intersect = dagreD3.intersect;
        },
        shape: function (data, offsetSize) {
            var that = this;
            var shapeCreator;
            offsetSize = offsetSize || 0;
            switch (data.shape) {
                case 'rect':
                    shapeCreator = d3.create('svg:rect')
                        .attr('width', data.width + offsetSize)
                        .attr('height', data.height + offsetSize)
                        .attr('x', -data.width / 2 - offsetSize / 2)
                        .attr('y', -data.height / 2 - offsetSize / 2)
                        .attr('rx', data.rx || 0)
                        .attr('ry', data.rx || 0);
                    break;
                case 'ellipse':
                    shapeCreator = d3.create('svg:ellipse')
                        .attr("x", -data.width / 2 - offsetSize / 2)
                        .attr("y", -data.height / 2 - offsetSize / 2)
                        .attr("rx", data.width / 2 + offsetSize)
                        .attr("ry", data.height / 2 + offsetSize);
                    break;
                case 'circle':
                    shapeCreator = d3.create('svg:circle')
                        .attr('r', (data.size || data.width / 2) + offsetSize)
                        .attr('cx', data.cx || 0)
                        .attr('cy', data.cy || 0);
                    break;
            }
            return shapeCreator;
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
    });

})));