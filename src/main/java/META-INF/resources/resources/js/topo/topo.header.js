(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {}), global));
}(this, (function (exports, global) {
    var Header = function (base) {
        this.base = base;
        console.log(this.base);
        this.init();
    };
    Header.prototype = {
        constructor: Header,
        init: function () {
            this.container = this.base.content.append('xhtml:div').attr('class', 'header').lower();
            var iconUndo = this.container.append('xhtml:i').attr('class', 'command iconfont icon-undo')
                .attr('titled', '撤销')
                .on('click', function () {

                });
            var iconRedo = this.container.append('xhtml:i').attr('class', 'command iconfont icon-redo')
                .attr('titled', '重做')
                .on('click', function () {

                });
            // 分割线
            this.container.append('xhtml:span').attr('class', 'separator');
            var iconRedo = this.container.append('xhtml:i').attr('class', 'command iconfont icon-delete-o disable')
                .attr('titled', '删除')
                .on('click', function () {

                });

            this.container.append('xhtml:span').attr('class', 'separator');
            var iconRedo = this.container.append('xhtml:i').attr('class', 'command iconfont icon-zoom-in-o')
                .attr('titled', '放大')
                .on('click', function () {

                });
            var iconRedo = this.container.append('xhtml:i').attr('class', 'command iconfont icon-zoom-out-o disable')
                .attr('titled', '缩小')
                .on('click', function () {

                });
            var iconRedo = this.container.append('xhtml:i').attr('class', 'command iconfont icon-fit')
                .attr('titled', '适应画布')
                .on('click', function () {

                });
            var iconRedo = this.container.append('xhtml:i').attr('class', 'command iconfont icon-actual-size-o')
                .attr('titled', '实际尺寸')
                .on('click', function () {

                });
            this.container.append('xhtml:span').attr('class', 'separator');
            var iconRedo = this.container.append('xhtml:i').attr('class', 'command iconfont icon-group')
                .attr('title', '成组')
                .on('click', function () {

                });
            return;
            this.container.html(function () {
                return `
                    <i data-command="undo" class="command iconfont icon-undo" title="撤销"></i>
                    <i data-command="redo" class="command iconfont icon-redo" title="重做"></i>
                    <span class="separator"></span>
                    <i data-command="copy" class="command iconfont icon-copy-o disable" title="复制"></i>
                    <i data-command="paste" class="command iconfont icon-paster-o disable" title="粘贴"></i>
                    <i data-command="delete" class="command iconfont icon-delete-o disable" title="删除"></i>
                    <span class="separator"></span>
                    <i data-command="zoomIn" class="command iconfont icon-zoom-in-o" title="放大"></i>
                    <i data-command="zoomOut" class="command iconfont icon-zoom-out-o disable" title="缩小"></i>
                    <i data-command="autoZoom" class="command iconfont icon-fit" title="适应画布"></i>
                    <i data-command="resetZoom" class="command iconfont icon-actual-size-o" title="实际尺寸"></i>
                    <span class="separator"></span>
                    <i data-command="toBack" class="command iconfont icon-to-back disable" title="层级后置"></i>
                    <i data-command="toFront" class="command iconfont icon-to-front disable" title="层级前置"></i>
                    <span class="separator"></span>
                    <!--<i data-command="multiSelect" class="command iconfont icon-select" title="多选"></i>-->
                    <i data-command="addGroup" class="command iconfont icon-group" title="添加组"></i>
                    <!--<i data-command="unGroup" class="command iconfont icon-ungroup disable" title="解组"></i>-->
                `;
            })
        },

    }

    exports.header = Header;
})));