
viewer.Toolbar = Class.extend({

    init:function(elementId, view){
        this.html = $("#"+elementId);
        this.view = view;

        // register this class as event listener for the canvas
        // CommandStack. This is required to update the state of
        // the Undo/Redo Buttons.
        //
        view.getCommandStack().addEventListener(this);

        // Register a Selection listener for the state hnadling
        // of the Delete Button
        //
        view.addSelectionListener(this);


    },

    resetConnection : function(){
        draw2d.Connection.createConnection = function(sourcePort, targetPort){
            return app.createConnection(sourcePort, targetPort);
        };
    },

    /**
     * @method
     * Called if the selection in the cnavas has been changed. You must register this
     * class on the canvas to receive this event.
     *
     * @param {draw2d.Figure} figure
     */
    onSelectionChanged : function(figure){
        //this.deleteButton.attr( "disabled", figure===null );
    },

    /**
     * @method
     * Sent when an event occurs on the command stack. draw2d.command.CommandStackEvent.getDetail()
     * can be used to identify the type of event which has occurred.
     *
     * @template
     *
     * @param {draw2d.command.CommandStackEvent} event
     **/
    stackChanged:function(event)
    {
        /*
        this.undoButton.attr("disabled", !event.getStack().canUndo() );
        this.redoButton.attr("disabled", !event.getStack().canRedo() );

        this.saveButton.attr("disabled",   !event.getStack().canUndo() && !event.getStack().canRedo()  );
        */
    },
    /**
     * 放大缩小
     * @param t
     */
    setViewZoom: function(t){
        switch(t){
            case 1:
                app.view.setZoom(app.view.getZoom()*1.2,true);
                break;
            case 2:
                app.view.setZoom(1.0, true);
                break;
            case 3:
                app.view.setZoom(app.view.getZoom()*0.8, true);
                break;
        }
    }
});