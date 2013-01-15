this.axion = this.axion||{};

(function(){
var Channel = function(client,path){
	this.initialize(client,path);
}
var p = Channel.prototype;

	p.path = "";
	p.client = null;
	
	p.callback = null;
	p.eventFunctions = {};
	p.size = -1;
	
	p.initialize = function(client,path){
		this.client = client;
		this.path = path;
		this.eventFunctions = {};
	}
	
	p.post = function(msg,successCallback,failureCallback){
		this.client.post({
			path: this.path,
			data: msg,
			success: successCallback,
			failure: failureCallback
		});
		this.client.post(this.path,msg,callback);
	}
	
	p.message = function(message,callback){
		this.client.message({
			path: this.path,
			message: message,
			success: callback
		});
	}
	
	p.unsubscribe = function(successCallback,failureCallback){
		this.client.unsubscribe({
			path: this.path,
			success: successCallback,
			failure: failureCallback
		});
	}
	
	p.accept = function(callback){
		this.callback = callback;
	}
	
	p.bind = function(name,successCallback,failureCallback){
		this.eventFunctions[name] = successCallback;
		
		this.client.bind(name,{
			path: this.path,
			failure: failureCallback
		});
	}
	
	p.fire = function(name,data){
		this.client.fire(name,{
			path: this.path,
			message:data
		});
	}
	
	
	
	axion.Channel = Channel;
}());