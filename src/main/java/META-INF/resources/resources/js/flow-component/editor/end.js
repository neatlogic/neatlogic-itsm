draw2d.shape.node.FlowEnd = draw2d.shape.node.FlowBaseCircle.extend({
	NAME : "draw2d.shape.node.FlowEnd",
	CNNAME :'结束',
	COLOR: '#FF0000',
	PORTTYPE: 'input',
	allowBack : false,
	allowRefire : false,
	allowAssign : false,
	allowEoa : false,
	allowGrade : false,
	stateFigure : null,
	allowCallout : true,
	isValid : function() {
		for(var c = 0; c < this.getChildren().getSize(); c++){
			if(this.getChildren().get(c).NAME == 'draw2d.shape.basic.Image'){
				this.removeFigure(this.stateFigure);
			}
		}
		return true;
	}
});