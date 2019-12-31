;(function( $ ){
    "use strict";
    function dragMove(){
        var self=this;
        this.x=0;
        this.y=0;
        this.target=false;
        this.clone=false;
        this.placeholder=false;
        this.cloneoffset={x:0,y:0};
        this.move=function(e){
            self.x=e.pageX;
            self.y=e.pageY;
            if (self.clone!=false && self.target!=false){
                self.clone.css({top:self.y-self.cloneoffset.y,left:self.x-self.cloneoffset.x});
                self.clone.attr('top',self.y-$('.canvas').offset().top-128*0.2);
                self.clone.attr('left',self.x-$('.canvas').offset().left-128*0.2);
            }else{

            }
        };
        $(window).on('mousemove touchmove',function(e){
            self.move(e);
        });

    }

 
    
    $.fn.panel=function(opts){
        var me,defaults,options;
        me=this;
        defaults={
            target: '>div',
            draggable:false,
            callback: false,
            containerClass: 'drag-container',
            childrenClass: 'drags-children',
            cloneClass: 'drags-children-clone',
            active: true
        };
        options=$.extend( {}, defaults, opts );

        $(this).each(function(){
            var mouse,target,holderClass,dragClass,active,callback,parent,childrenClass,jQclass,cloneClass;
            var parser = new DOMParser();
            //SET DAD AND STARTING STATE
            mouse=new dragMove();
            active=options.active;
            parent=$(this);
            if (!parent.hasClass('drag-active') && active==true) parent.addClass('drag-active');
            //GET SETTINGS
            childrenClass=options.childrenClass;
            cloneClass=options.cloneClass;
            jQclass='.'+childrenClass;
            parent.addClass(options.containerClass);
            target=parent.find(options.target);
            callback=options.callback;
            dragClass='drag-draggable-area';
            
            //add panel items
            
            me.addPanelNode = function(panelNode){
            	var lsize = panelNode.getLayoutSize()?panelNode.getLayoutSize():12;
//            	if(panelNode.getIcon().indexOf('<svg')==0){
//            		var doc =parser.parseFromString(panelNode.getIcon(), "image/svg+xml");
//            		//this.append(' <div class="col-md-4 col-xs-4 col-sm-4 drags-children drag-draggable-area" data-type="'+panelNode.getType()+'"><object class="topocomponent"></object></div>');
//            		this.append(' <div class="col-md-'+lsize+' col-xs-'+lsize+' col-sm-'+lsize+' drags-children drag-draggable-area" id='+panelNode.getId()+'data-shape="'+panelNode.getShape+'" data-type="'+panelNode.getType()+'">'+panelNode.getIcon()+'<span>'+panelNode.getName()+'</span></div>');
//            		//this.find('[data-type="'+panelNode.getType()+'"] object')[0].data="data:image/svg+xml,"+panelNode.getIcon();
//            	}
//            	else{
            			this.append(' <div class="col-md-'+lsize+' col-xs-'+lsize+' col-sm-'+lsize+' drags-children drag-draggable-area" id='+panelNode.getId()+' data-name="'+panelNode.getName()+'"  data-type="'+panelNode.getType()+'">'+'<i class="fa fa-lg '+panelNode.getIcon()+'"></i><span>'+panelNode.getName()+'</span></div>');
//            	}
            
            };
            me.savePanel = function(){
            	
            	console.log('saving');
            	
            };
            //DROPZONE FUNCTION
            me.addDropzone=function(selector,func){
                $(selector).on('mouseenter touchenter',function(){
                    if (mouse.target!=false) {
                        //mouse.target.css({display: 'none'});
                        $(this).addClass('active');
                    }
                }).on('mouseup touchend',function(){
                    if (mouse.target!=false) {
                        mouse.target.css({display: 'block'});
                        func(mouse.clone);
                    }
                    $(this).removeClass('active');
                }).on('mouseleave touchleave',function(){
                    if (mouse.target!=false){
                        mouse.target.css({display: 'block'});
                    }
                    $(this).removeClass('active');
                });
            };

            //GET POSITION FUNCTION
            me.getPosition=function(){
                var positionArray = [];
                $(this).find(jQclass).each(function(){
                    positionArray[$(this).attr('data-dad-id')]=parseInt($(this).attr('data-dad-position'));
                });
                return positionArray;
            };
            //DEFAULT DROPPING
            parent.on('DOMNodeInserted',function(e){
                var Target=$(e.target);
                if (!Target.hasClass(childrenClass) ){Target.addClass(childrenClass);};
            });
            $(document).on('mouseup touchend',function(){
            	  if (mouse.target!=false &&  mouse.clone!=false){
                      if (callback!=false){
                          callback(mouse.target);
                      }
                      var appear=mouse.target;
                      var desapear=mouse.clone;
                      var bLeft =0;Math.floor(parseFloat(parent.css('border-left-width')));
                      var bTop =0;Math.floor(parseFloat(parent.css('border-top-width')));
                      if ($.contains(parent[0],mouse.target[0])){
                      	 desapear.remove();
                      }else{
                      	desapear.remove();
                      }
                      mouse.clone=false;
                      mouse.target=false;
                  }
                  $("html,body").removeClass('drag-noSelect');
            });
            //ORDER ELEMENTS
            var order = 1;
            target.addClass(childrenClass).each(function(){
                if($(this).data('drag-id')==undefined){
                    $(this).attr('data-drag-id',order);
                }
                $(this).attr('data-drag-position',order);
                order++;
            });
            
            //GRABBING EVENT
            var jq=(options.draggable!=false)?options.draggable:jQclass;
            parent.find(jq).addClass(dragClass);
            parent.on('mousedown touchstart',jq,function(e){
                if (mouse.target==false && e.which==1 && active==true){
                    // GET TARGET
                    if (options.draggable!=false){
                        mouse.target=parent.find(jQclass).has(this);
                    }else{
                        mouse.target=$(this);
                    }
                    // ADD CLONE
                    mouse.clone=mouse.target.clone();
                    //mouse.target.css({visibility:'hidden'}).addClass('active');
                    mouse.clone.addClass(cloneClass);
                    parent.append(mouse.clone);


                    // GET OFFSET FOR CLONE
                    var difx,dify;
                    var bLeft =Math.floor(parseFloat(parent.css('border-left-width')));
                    var bTop =Math.floor(parseFloat(parent.css('border-top-width')));
                    difx=mouse.x-mouse.target.offset().left+parent.offset().left+bLeft;
                    dify=mouse.y-mouse.target.offset().top+parent.offset().top+bTop;
                    mouse.cloneoffset.x=difx;
                    mouse.cloneoffset.y=dify;

                    // REMOVE THE CHILDREN DAD CLASS AND SET THE POSITION ON SCREEN
                    mouse.clone.removeClass(childrenClass).css({
                        position:'absolute',
                        top:mouse.y-mouse.cloneoffset.y,
                        left:mouse.x-mouse.cloneoffset.x
                    });
                    // UNABLE THE TEXT SELECTION AND SET THE GRAB CURSOR
                    $("html,body").addClass('drag-noSelect');
                }
            });
          

        });

        return this;
    };
}( jQuery ));

function PanelNode() {
    this.icon =null;
    this.clickAction = null;
    this.contextMenu = null;
    this.type = null;
    this.name = null;
    this.info = null;
    this.ip = null;
    this.id = null;
    this.layoutSize = null;
}
PanelNode.prototype = {
    consturctor: PanelNode,
    setLayoutSize:function(layoutSize){
    	this.layoutSize = layoutSize;
    },
    getLayoutSize:function(){
    	return this.layoutSize;
    },
    setId: function(id){
    	this.id = id;
    },
    getId: function(){
    	return this.id;
    },
    setIp: function(ip){
    	this.ip = ip;
    },
    getIp: function(){
    	return this.ip;
    },
    setInfo: function(info){
    	this.info = info;
    },
    getInfo: function(){
    	return this.info;
    },
    setName: function(name){
    	this.name = name;
    },
    getName: function(){
    	return this.name;
    },

    setIcon: function(icon) {
    	 this.icon =icon;
    },
    getIcon:function(){
    	return this.icon;
    },
    setClickAction:function(clickAction){
    	this.clickAction = clickAction;
    },
    getClickAction:function(){
    	return this.clickAction;
    },
    setContextMenu:function(contextMenu){
    	this.contextMenu = contextMenu;
    },
    getContextMenu:function(){
    	return this.contextMenu;
    },
    setType: function(type){
    	this.type = type;
    },
    getType:function(){
    	return this.type;
    },
    
};

