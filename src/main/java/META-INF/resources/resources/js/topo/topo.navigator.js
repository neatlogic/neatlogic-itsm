(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? factory(exports) :
        typeof define === 'function' && define.amd ? define(['exports'], factory) :
            (factory((global.TsTopo = global.TsTopo || {}), global));
}(this, (function (exports, global) {
    var Navigator = function (base) {
        this.base = base;
        this.init();
    }
    Navigator.prototype = {
        constructor: Navigator,
        init: function () {
            this.container = this.base.container.append('xhtml:div').attr('class', 'navigator')
            this.container.html(function () {
                return `
                    <div class="pannel-title">导航器</div>
                `
            })
        }
    }
    exports.navigator = Navigator;
})));