/*define(['util'], function(util){
	*/
    $.ajaxSetup({
        error:function(x,e){
        	//showPopMsg.error("request failed.");
        	removeMask();
            return false;
        }
    });
	
	var restGetAsync = function(url){
		var rst = {};
		$.ajax({
		  url: url,
		  async: false,
          contentType: 'application/json',
          dataType: 'json',
          success: function(data){
        	  rst = data;
          },
          error: function(data){
              //showPopMsg.error("request failed.");
              console.error(data);
          },
          type: 'GET'
		});
		return rst;
	};
	
	var restGet = function(url, callback){
		$.ajax({
		  url: url,
		  async: false,
          contentType: 'application/json',
          dataType: 'json',
          success: function(data){
        	  if(callback){
        		  callback(data);
        	  }
          },
          error: function(data){
              //showPopMsg.error("request failed.");
              console.error(data);
          },
          type: 'GET'
		});
	};
	
	
	var restPut = function(url, param, callback, isAsync ){
		var sync = true;
		if(isAsync != null){
			sync = isAsync;
		}
		$.ajax({
		  url: url,
		  async: sync,
          contentType: 'application/json',
          dataType: 'json',
          success: function(data){
        	  if(callback){
        		  callback(data);
        	  }
          },
          error: function(data){
              //showPopMsg.error("request failed.");
              console.error(data);
          },
          type: 'PUT',
          processData: false,
          data: JSON.stringify(param)
		});
	};

	var restPost = function(url, param, callback, isAsync){
		var sync = true;
		if(isAsync != null){
			sync = isAsync;
		}
		$.ajax({
			  url: url,
			  async: sync,
	          contentType: 'application/json',
	          dataType: 'json',
	          success: function(data){
	        	  if(callback){
	        		  callback(data);
	        	  }
	          },
	          error: function(data){
	              //showPopMsg.error("request failed.");
	              console.error(data);
	          },
	          type: 'POST',
	          processData: false,
	          data: JSON.stringify(param)
			});
	};
	
	var restDelete = function(url, param, callback, isAsync){
		var sync = true;
		if(isAsync != null){
			sync = isAsync;
		}
		$.ajax({
			  url: url,
			  async: sync,
	          contentType: 'application/json',
	          dataType: 'json',
	          success: function(data){
	        	  if(callback){
	        		  callback(data);
	        	  }
	          },
	          error: function(data){
	              //showPopMsg.error("request failed.");
	              console.error(data);
	          },
	          type: 'DELETE',
	          processData: false,
	          data: JSON.stringify(param)
			});
	};

  /*  return {
    	restGetAsync: restGetAsync,
    	restGet: restGet,
    	restPut: restPut,
    	restPost: restPost,
    	restDelete: restDelete
    };*/
//});
