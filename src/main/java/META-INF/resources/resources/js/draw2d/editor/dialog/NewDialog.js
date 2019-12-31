

editor.dialog.NewDialog = Class.extend({
    
    init:function(){
      
    },
    
    show: function(){
            this.container = $('#myModal');
            var container = this.container;
            var _this = this;
            
            container.modal();
            $("#myModalLabel").text("创建新拓扑图");
            $("#myModal").css("width","500px");
            // Form part
            //
            var template =
                        '<form>'+
                        '  <label>拓扑图名称：</label>'+
                        '  <input type="text" placeholder="请输入名称">'+
                        '</form>';
            var compiled = Hogan.compile(template);
            var output = $(compiled.render({}));
            container.find('.modal-body').html(output);
            var input = output.find("input");
            input.on("keyup", function(){
                container.find('.btn-primary').attr("disabled",input.val().length===0);
            });
            output.submit(function(e){
                return false;
            });
            
            // button bar
            //
            template = '<button class="btn" data-dismiss="modal" aria-hidden="true">取消</button><button class="btn btn-primary">保存</button>';
            compiled = Hogan.compile(template);
            output = compiled.render({});
            container.find('.modal-footer').html(output);
            container.find('.btn-primary').on('click', $.proxy(function(e) {
                e.preventDefault();
                var row = $(container.find("tr.success"));
                var viewName = input.val();
                _this._onOk(viewName);
            },this)).attr("disabled",true);
    },
    
    _onOk: function(viewName){
        this.container.modal('hide');
        app.createDefinition(viewName);
    }
});