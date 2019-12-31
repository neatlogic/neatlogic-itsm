/*****************************************
 *   Library is under GPL License (GPL)
 *   Copyright (c) 2013 Techsure
 ****************************************/
draw2d.ui.MyLabelEditor = Class.extend({

    /**
     * @constructor
     * Create an label editor with a dedicated callback listener
     *
     * @private
     */
    init: function(listener){

        // register some default listener and override this with the handover one
        this.listener = $.extend({onCommit:function(){}, onCancel:function(){}},listener);
    },

    /**
     * @method
     * Trigger the edit of the label text.
     *
     * @param {draw2d.shape.basic.Label} label the label to edit
     */
    start: function( label){
    	
    	var listener = this.listener ; 
    	createModalDialog({
    		msgcontent : "节点名称：<input type=\"text\" id=\"_txtPointValue\" value='"+label.getText()+"'></input>" ,
    		successFuc : function(){
    			var newText = $("#_txtPointValue").val();
    			
    	        if(newText){
    	            label.setText(newText);
    	            listener.onCommit(label.getText());
    	        }
    	        else{
    	        	listener.onCancel();
    	        }
    		}
    	});
    	
    	
        /*var newText = prompt("节点名称: ", label.getText());
        if(newText){
            label.setText(newText);
            this.listener.onCommit(label.getText());
        }
        else{
            this.listener.onCancel();
        }*/
    }

});