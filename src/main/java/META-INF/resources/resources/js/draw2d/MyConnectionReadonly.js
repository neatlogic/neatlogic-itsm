/*****************************************
 *   Library is under GPL License (GPL)
 *   Copyright (c) 2013 Techsure.com
 ****************************************/
/**
 * @class MyConnection
 * 
 * A simple Connection with a label wehich sticks in the middle of the connection..
 *
 * @author wangtc
 * @extend draw2d.Connection
 */
var MyConnection= draw2d.Connection.extend({
    NAME: "MyConnection",
    init:function(route , arrow )
    {
    	this._super();
        this.label = new draw2d.shape.basic.Label("");
		this.label.setStroke(0);
		var color = new draw2d.util.Color('#666666');
		this.label.setFontColor(color);
		this.addFigure(this.label, new draw2d.layout.locator.ManhattanMidpointLocator(this));
    },

    resetColor: function(){
        if(this.originalColor != undefined){
            this.setColor( this.originalColor );
        }
    },
    setOriginalColor: function(color){
        this.originalColor = color;
    },
    getPersistentAttributes : function()
    {
        var memento = this._super();
        if(typeof this.dasharray === "string"){
            memento.dasharray = this.dasharray;
        }

        if( this.dynamicLine == 1){
            memento.dynamicLine = this.dynamicLine;
        }
        if( this.sourceDecorator != null ){
            memento.source.sourceDecorator = this.sourceDecorator.NAME;
        }
        if( this.targetDecorator != null ){
            memento.target.targetDecorator = this.targetDecorator.NAME;
        }
        memento.labels = this.label.getPersistentAttributes();
        return memento;
    },
    setPersistentAttributes : function(memento)
    {
        this._super(memento);

        if(typeof memento.dasharray !=="undefined"){
            this.setDashArray( memento.dasharray );
        }

        if(typeof memento.dynamicLine !=="undefined"){
            this.setDynamic( memento.dynamicLine );
        }

        if(typeof memento.color !=="undefined"){
            this.setOriginalColor( memento.color );
        }

        if(typeof memento.source.sourceDecorator !=="undefined"){
            this.setSourceDecorator( eval("new " + memento.source.sourceDecorator + "(10,7)") );
        }
        if(typeof memento.target.targetDecorator !=="undefined"){
            this.setTargetDecorator( eval("new " + memento.target.targetDecorator + "(10,7)") );
        }
        if(typeof memento.labels.text !=="undefined"){
            this.setLabelText( memento.labels.text );
        }
    },
    setDynamic: function(){
        this.dynamicLine = 1;

        //虚线动态效果

        this.dasharray = '-';
        this.setLineAnimation();

    },
    setStatic: function(){
        this.setDashArray("");
        if(this.dynamicLineInterval!=null){
            clearInterval(this.dynamicLineInterval);
        }
    },
    setData: function(d){
    	this.setUserData(d);
    	if(d != null && d != ''){
    		if(d.rule != null && d.rule != ''){
    			this.setDashArray('-');
    			this.repaint();
    		}else{
    			this.setDashArray('');
                this.repaint();
    		}
    		if(d.name != null && d.name != ''){
    			this.setLabelText(d.name);
    		}
    	}else{
    		 this.setDashArray('');
             this.repaint();
    	}
    },
    setLabelText: function(txt){
        this.labelText = txt;
        this.label.setText(this.labelText);
    }
});
