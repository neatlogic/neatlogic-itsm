//考虑是否通过标签绑定插件
;
(function($) {
	var ScrollPage = function($target, config) {
		var that = this;
		var defConfig = {
				scrollContent : null,//非必填   需要监听滚动的容器，默认为绑定的div 内容为选择器  或则 jquery对象 或则dom对象    非widow的滚动的对象需要设置高度
				initPage : 1,//非必填   第一次加载的currentPage的值
			    pageParam:null,//非必填   分页已经加载了之后在调插件 参数值 {currentPage:'',pageCount:''}
				pageFn : function(currentPage,isInit) {//必填   滚动到底的调用的方法 currentPage为调用接口的currentPage  isInit:true第一次填充数据	
					
				}
		   };
		that.isMore = true;//是否有下一页
		that.isScrolling=false;//是否正在滚动加载数据
		that.config = $.extend({}, defConfig, config);
		that.$target = $target;
		
		if(!that.config.scrollContent){//初始化的对象
			that.$scrollContent=that.$target;
		}else if(that.config.scrollContent instanceof jQuery){ //jQuery对象
			that.$scrollContent=that.config.scrollContent;
		}else {//选择器字符串  或则 dom对象
			that.$scrollContent=$(that.config.scrollContent);
		}
		that.$scrollContent.css({overflow:'auto'});
        if(that.config.pageParam==null){
            that.isScrolling=true;
            setTimeout(function(){that.config.pageFn(that.config.initPage,true)});
        }else{
        	if(typeof that.config.pageParam.currentPage == 'undefined' || typeof that.config.pageParam.pageCount == 'undefined'){
				that.setTip('error');
				return false;
			}
        	that.currentPage = that.config.pageParam.currentPage;
			that.pageCount = that.config.pageParam.pageCount;
        	if (that.pageCount <= that.currentPage) {
				that.isMore = false;
			} else {
				that.isMore = true;
				//如果监听滚动的容器没有出现滚动条时，需要加载下一页
				that.loadTimeout=setTimeout(function(){//使用settimeout是为了防止页面没有被渲染，导致高度计算问题
					that.loadPage();
					that.loadTimeout=null;
				},10);
			}
        }
        
        that.$scrollContent.on('scroll', function(event) {
        	if(that.isScrolling) return false;
        	var oContent = this==window?window.document.body:this; //加一个判断,如果滚动的是window对象则需要做一下处理
			if (that.$target[0].scrollHeight!==0 && (Math.ceil($(this).scrollTop()) + oContent.offsetHeight >= oContent.scrollHeight) && that.isMore) {// 达到滚动的条件和有下一页
			    that.setTip('running');
				that.isScrolling=true;
				that.config.pageFn((that.currentPage?that.currentPage+1:that.config.initPage),false);
			}
		});
		
	};

	ScrollPage.prototype = {
		constructor : ScrollPage,
		changeStatus : function(currentPage, pageCount) {// 改变滚动的状态
			var that = this;
			that.isScrolling=false;
			if(that.loadTimeout){
				clearTimeout(that.loadTimeout);
				that.loadTimeout=null;
			}
			
			if(that.$tip){ //消除数据加载动画
				that.$tip.remove();
				that.$tip=null;
			}
			
			if(typeof currentPage == 'undefined' || typeof pageCount == 'undefined'){
				that.setTip('error');
				return false;
			}
			
			that.currentPage = currentPage;
			that.pageCount = pageCount;
			
			if (pageCount <= currentPage) {
				that.isMore = false;
			} else {
				that.isMore = true;
				that.loadPage();
			}
		},
		setTip:function(status){
			var that = this;
			that.$tip &&  that.$tip.length>0 && that.$tip.remove();
			if(status=='running'){
				that.$tip=$('<div class="text-lighten text-center fz12" style="padding: 10px 0;" ><i class="ts-loading text-grey  font-mgr fz18"></i>正在加载中...</div>');
				that.$target.append(that.$tip);
			}else if(status=='waiting'){
				that.$tip=$('<div class="text-lighten text-center fz12" style="padding: 10px 0;cursor: pointer;">加载更多</div>');
				that.$target.append(that.$tip);
				that.$tip.on('click',function(){
					that.loadPage();
				});
			}else if(status=='error'){
				that.$tip=$('<div class="text-danger text-center fz12" style="padding: 10px 0;">数据加载出错</div>');
				that.$target.append(that.$tip);
			}else{
				that.$tip=null;
			}
		},
		loadPage:function(){
			var that = this;
			var oContent = that.$scrollContent[0]==window?window.document.body:that.$scrollContent[0]; //加一个判断,如果滚动的是window对象则需要做一下处理
			if (that.$target[0].scrollHeight!==0 && (Math.ceil(that.$scrollContent.scrollTop()) + oContent.offsetHeight >= oContent.scrollHeight) && that.isMore) {// 达到滚动的条件和有下一页
				that.setTip('running');
				that.isScrolling=true;
				that.config.pageFn((that.currentPage?that.currentPage+1:that.config.initPage),false);
			}else{
				that.setTip('waiting');
			}
		}
	};

	$.fn.scrollPage = function(config) {
		var $that = $(this);
		if (!$that.attr('bind-scrollpage')) {
			var scroll = new ScrollPage($that, config);
			$that.attr('bind-scrollpage', 'true');
			$that[0].scrollPage = scroll;
			return scroll;
		}
		return $that[0].scrollPage;
	};

})($);