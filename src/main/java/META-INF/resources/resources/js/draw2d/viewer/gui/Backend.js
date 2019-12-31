

viewer.backend.Backend = Class.extend({
	
    definitions : [{
                    name:"DefName01",
                    id:"id01",
                    content:[{"type":"draw2d.shape.state.Start","id":"98fcbeb5-3c72-af82-41e4-7f9bd73660c5","x":63,"y":73,"width":50,"height":50,"userData":null},{"type":"draw2d.shape.state.End","id":"174353d9-f4c6-617d-29d0-aa10601b2d07","x":252,"y":175,"width":50,"height":50,"userData":null},{"type":"draw2d.shape.state.Connection","id":"0c1c81f8-6e35-2a94-f186-b964528fac13","userData":null,"cssClass":"stroke","stroke":2,"color":"#1B1B1B","source":{"node":"98fcbeb5-3c72-af82-41e4-7f9bd73660c5","port":"output0"},"target":{"node":"174353d9-f4c6-617d-29d0-aa10601b2d07","port":"input0"},"router":"draw2d.layout.connection.FanConnectionRouter","label":"label"}]
                 },
                 {
                     name:"DefName02",
                     id:"id02",
                     content:[]
                  }
                ],
    
	init:function(){
      
	},
	
	getActivities: function(successCallback){
		successCallback( [
	            { id:"ReloadDefinitions",
	              parameters:[]
	            }
	           ]);
	},
	
    getPrerequisitVariables: function(successCallback){
        successCallback( [ "currentUser" ]);
    },
    	
    /**
     * @method
     * Called if the selection in the canvas has been changed. You must register this
     * class on the canvas to receive this event.
     * 
     * @param {draw2d.Figure} figure
     */
    getDefinitions : function(successCallback, errorCallback){
        successCallback({ definitions:this.definitions });
    },

    save: function(definitionId,bg, content,updateType, successCallback, errorCallback){
        $.post("/asmconsole/flow/updateFlow.do",  {
            id: definitionId,
            bg: bg,
            updateType: updateType,
            content: JSON.stringify(content,null,2)
        }).done(function(data) {
                if(data!="err"){
                    successCallback(data);
                }else{
                    showPopMsg.error("保存失败!");
                }
            });
    },

    load:  function(definitionId, successCallback, errorCallback){
        if(definitionId!=null){
            $.getJSON( "/asmconsole/flow/loadFlowJsonByID.do?id="+definitionId , function(data){
                if(data!="err"){
                    successCallback(data);
                    jsonData = data["chartConf"];
                }else{
                    showPopMsg.error("数据加载失败!");
                }
            });
        }
    }
});

