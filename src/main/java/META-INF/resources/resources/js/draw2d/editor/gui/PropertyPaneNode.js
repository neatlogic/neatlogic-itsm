/**
 * 属性绑定面板
 * @type {*}
 */
editor.propertypane.PropertyPaneNode = Class.extend({

    init:function(figure){
        this.figure = figure;
    },

    injectPropertyView: function( domId)
    {

        var nodeType = this.figure.NAME;

        var fid = this.figure.getId();
        var userData = this.figure.getUserData();
        var ctName,ctId,ciName,ciId,mtName,mtId ;
        //console.info( userData );
        if(userData != null){
            ctName = deleteLastChar( userData.CT.text ,",");
            //ctId = userData.CT.id;
            ciName = deleteLastChar( userData.CI.text ,",");

            //ciId = userData.CI.id;
            mtName = deleteLastChar( userData.MT.text ,",");
            //mtId = userData.MT.id;
        }
        if(ciName == null ){
            ciName = '未绑定';
        }
        if(mtName == null ){
            mtName = '未绑定';
        }
        var bindType,labelName,labelTxt;
        switch(nodeType){
            case "MyConnection":
                return;
            case "draw2d.shape.node.DeviceNode":
                bindType = "bindDevice";
                labelName = "配置项";
                labelTxt = ciName;
                break;
            case "draw2d.shape.node.ApplicationNode":
                bindType = "bindApp";
                labelName = "应用配置项";
                labelTxt = ciName;
                break;
            case "draw2d.shape.node.BizNode":
                bindType = "bindBiz";
                labelName = "业务指标";
                labelTxt = mtName;
                break;

        }


        var view = $("<form class='form-horizontal'>"+
            "<div class='control-group'>"+
            "   <div><span>" + labelName + "：</span><span id='spLabelTxt'>" +  labelTxt + "</span>" +
            //"       <span><button id='btnModifyBind' onclick=\"app.propertyPane.pane.updateBind('" + bindType + "','" + fid + "')\">修改绑定</button></span>"+
            "       <span id='spBtn'><button id='btnModifyBind'>修改绑定</button></span>"+
            "       </div>"+
            "   </div>" +
            "</form>");
        /*
        var input = view.find("#ciProperty");
        var handler =$.proxy(function(e){
            e.preventDefault();
            // provide undo/redo for the label field
            app.executeCommand(new editor.command.CommandSetLabel(this.figure, input.val()));
        },this);
        input.change(handler);
        */

        view.submit(function(e){
            return false;
        });

        domId.append(view);
        var handler = $.proxy(function(e){
            e.preventDefault();
            this.updateBind( bindType,fid );
        },this);
        $("#btnModifyBind").click(handler);
    },

    updateBind: function(opt,id){
        app.bindCMDB( opt, id,function(data){
            //var f = this.figure;
            var f = app.view.getFigure(id);
            //console.info(f);
            if(f!=undefined){
                if(opt == "bindBiz"){
                    $('#spLabelTxt').html(f.getBizData()).css("success");
                } else{
                    $('#spLabelTxt').html(f.getLabelText() ).css("success");
                }
                $('#spBtn').html("");
            }
        });
        //var f = app.view.getFigure(id);

        /*
        app.bindCMDB( opt , $.proxy(function(data){
            var d = eval('(' + data + ')');
            var txt = deleteLastChar( d.CI.text ,",");
            console.info(d);
            var f = app.view.getFigure(id);
            console.warn(id );
            if(f!=undefined){
                f.setUserData( d );
                f.setLabelTxt( txt );
                console.info(f.getId() + "," + f.labelTxt);

                $('#ciName').html( txt );
                $('#ciProperty').val(d.CI.id);
                $('#btnModifyBind').attr("disabled",true);
                if(app != null){
                    app.toolbar.saveButton.attr("disabled",false);
                }
                showPopMsg("操作完成！","ok", 2);
            }else{
                showPopMsg("操作失败！","error", 4);
            }
        },this));
        */
    },

    /**
     * @method
     * called by the framework if the pane has been resized. This is a good moment to adjust the layout if
     * required.
     *
     */
    onResize: function()
    {
    },


    /**
     * @method
     * called by the framework before the pane will be removed from the DOM tree
     *
     */
    onHide: function()
    {
    },

    /**
     * 删除字符串最后的字符
     * @param str
     * @param c
     * @returns {*}
     */
    deleteLastChar: function(s,c){
        //判断当前字符串是否以str结束
        if (typeof String.prototype.endsWith != 'function') {
            String.prototype.endsWith = function (str) {
                return this.slice(-str.length) == str;
            };
        }

        if (s!=null && s.endsWith(c)) {
            return s.substr(0, s.length - c.length);
        }
    }


});



