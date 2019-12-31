

editor.PropertyPane = Class.extend({
	
	init:function(elementId, view){
	    this.selectedFigure = null;
        this.html = $("#"+elementId);
        this.view = view;
        this.pane = null;
        this.view.addSelectionListener(this);
        
        // register as listener to update the property pane if anything has been changed in the model
        //
        view.getCommandStack().addEventListener($.proxy(function(event){
            if(event.isPostChangeEvent()){
                this.onSelectionChanged(this.selectedFigure);
            }
        },this));
	},
	
    /**
     * @method
     * Called if the selection in the canvas has been changed. You must register this
     * class on the canvas to receive this event.
     * 
     * @param {draw2d.Figure} figure
     */
    onSelectionChanged : function(figure){
        this.selectedFigure = figure;
        
        if(this.pane!==null){
            this.pane.onHide();
        }
        
        this.html.html("");

        if(figure===null){
            return;
        }else{
            this.pane = new editor.propertypane.PropertyPaneNode(figure);
        }
        /*
        this.pane = null;
        switch(figure.NAME)
        {
            case "draw2d.shape.node.DeviceNode":
                this.pane = new editor.propertypane.PropertyPaneState(figure);
                break;
            case "draw2d.shape.node.ApplicationNode":
                this.pane = new editor.propertypane.PropertyPaneStart(figure);
                break;
            case "draw2d.shape.node.BizNode":
                this.pane = new editor.propertypane.PropertyPaneEnd(figure);
                break;
            default:
                break;
        }
       
        if(this.pane!==null){
            this.pane.injectPropertyView(this.html);
        } */
    },
    
    onResize: function()
    {
        if(this.pane!==null){
            this.pane.onResize();
        }
    }
    
});

