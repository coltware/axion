this.axion = this.axion||{};

(function(){
var Client = function(){
	this.initialize();
}	
var p = Client.prototype;

var num = 0;

	p.wsock = null;
	p.connectHandler = null;
	
	p.responseFunctions = {};
	p.failureFunctinos = {};
	p.eventFunctions = {};
	
	p.targetPath = "";
	
	p.openCallback 	= null;
	p.closeCallback = null;
	
	p.channels = {};
	
	p.system = null;
	
	function debug(msg){
		if(console.log){
			console.log(msg);
		}
	}
	
	function _close(event,client){
		debug("axion disconnected");
		if(client.closeCallback){
			client.closeCallback(event);
		}
	}
	
	function _message(event,client){
		if(event.data instanceof Blob){
			
		}
		else if(event.data instanceof ArrayBuffer){
			
		}
		else if(typeof event.data == 'string'){
			debug(event.data);
			var json = JSON.parse(event.data);
			for(key in json){
				if(key == 'system'){
					_message_system(event, json[key], client);
				}
				else if(key == 'response'){
					_message_response(event,json[key], client);
				}
				else if(key == 'msg'){
					_message_message(event,json[key],client);
				}
				else if(key == 'event'){
					_message_event(event,json[key],client);
				}
				else{
					debug("not support:" + key);
				}
			}
		}
	}
	
	function _message_system(event,json,client){
		var type = json.type;
		var value = json.value;
		
		if(type == 'connected'){
			client.targetPath = value;
			
			client.system = new axion.Channel(client,value);
			client.channels[value] = client.system;
			
			if(client.openCallback){
				client.openCallback(json);
			}
		}
	}
	
	function _message_response(event,json,client){
		var code = json.code;
		var seq  = json.seq;
		
		var stat = Math.floor(code / 100);
		console.log("status[" + stat + "]");
		if(stat == 2){
			var res = _parse_response_json(client,json);
			if(client.responseFunctions[seq]){
				if(res.channel){
					var path = res.channel.path;
					client.channels[path] = res.channel;
					client.responseFunctions[seq](event,res.channel);
				}
				else{
					client.responseFunctions[seq](event,res);
				}
			}
		}
		else{
			if(client.failureFunctinos[seq]){
				debug("invoked failureFunction ");
				client.failureFunctinos[seq](event,json);
			}
			else{
				debug("not found failureFunction ");
			}
		}
		
		if(client.responseFunctions[seq]){
			delete client.responseFunctions[seq];
		}
		if(client.failureFunctinos[seq]){
			delete client.failureFunctinos[seq];
		}
	}
	
	function _message_message(event,json,client){
		var body = json.body;
		var path = json.path;
		debug("message :" + path);
		if(client.channels[path]){
			var channel = client.channels[path];
			if(channel.callback){
				debug("callback found");
				channel.callback(body);
			}
			else{
				debug("not found channel message callback");
			}
		}
		else{
			debug("not found channel");
		}
	}
	
	function _message_event(event,json,client){
		var body = json.body;
		var path = json.path;
		if(client.channels[path]){
			var channel = client.channels[path];
			if(channel){
				var type = json.type;
				if(channel.eventFunctions[type]){
					channel.eventFunctions[type](json.body);
				}
				else{
					debug("can't find event handler [" + type + "]");
				}
			}
		}
		else{
			debug("can't find channel");
		}
	}
	
	function _parse_response_json(client,json){
		var ret = {};
		if(json.channel){
			var path = json.channel.path;
			var channel = new axion.Channel(client,path);
			channel.size = json.channel.size;
			ret.channel = channel;
		}
		else if(json.content){
			ret.content = json.content;
		}
		return ret;
	}

	p.initialize = function(){
		if(!window.WebSocket){
			window.WebSocket = window.MozWebSocket;
		}
		this.wsock = null;
		this.responseFunctions = {};
		this.failureFunctinos = {};
		
		this.targetPath = "";
		
		this.openCallback 	= null;
		this.closeCallback 	= null;
		
		this.commandChannel = null;
	}
	
	p.isSupport = function(){
		if(!window.WebSocket){
			return false;
		}
		return true;
	}
	
	p.connect = function(params){
		var url = params.url;
		
		this.wsock = new WebSocket(url);
		if(params.opened){
			if((typeof params.opened) == 'function'){
				this.openCallback = params.opened;
			}
		}
		if(params.closed){
			if((typeof params.closed) == 'function'){
				this.closeCallback = params.closed;
			}
		}
		var self = this;
		var ws = this.wsock;
		this.wsock.onopen = function(event){
			
		}
		this.wsock.onclose = function(event){
			_close(event,self);
		}
		
		this.wsock.onmessage = function(event){
			_message(event,self);
		}
		
		this.wsock.onerror = function(event){
			debug(event);
		}
	}
	
	p.disconnect = function(){
		debug("axion disconnecting....");
		this.wsock.close();
	}
	
	p.post = function(opts){
		num++;
		var req = {
			post: [
			num,
			opts.path,
			opts.data
			]
		};
		
		if(opts.success){
			this.responseFunctions[num] = opts.success;
		}
		if(opts.failure){
			debug("hook failure :" + num);
			this.failureFunctinos[num] = opts.failure;
		}
		
		var str = JSON.stringify(req);
		this.wsock.send(str);
	}
	
	p.message = function(opts){
		num++;
		var req = {
			message: [
			   num,
			   opts.path,
			   opts.message
			]
		};
		if(opts.success){
			this.responseFunctions[num] = opts.success;
		}
		var str = JSON.stringify(req);
		this.wsock.send(str);
	}
	
	p.subscribe = function(opts){
		num++;
		var req = {
			pubsub: [
			  num,
			  opts.path,
			  'sub',
			]
		};
		if(opts.success){
			this.responseFunctions[num] = opts.success;
		}
		if(opts.failure){
			this.failureFunctinos[num] = opts.failure;
		}
		var str = JSON.stringify(req);
		this.wsock.send(str);
	}
	
	p.unsubscribe = function(opts){
		num++;
		var req = {
			pubsub: [
			   num,
			   opts.path,
			   'unsub'
			]
		};
		if(opts.success){
			this.responseFunctions[num] = opts.success;
		}
		if(opts.failure){
			this.failureFunctinos[num] = opts.failure;
		}
		var str = JSON.stringify(req);
		this.wsock.send(str);
	}
	
	p.bind = function(name,opts){
		num++;
		var req = {
			event:[
				num,
				opts.path,
				'bind',
				name,
			]
		};
		if(opts.success){
			
		}
		if(opts.failure){
			this.failureFunctinos[num] = opts.failure;
		}
		var str = JSON.stringify(req);
		this.wsock.send(str);
	}
	
	p.fire = function(name,opts){
		num++;
		var req = {
			event:[
			       num,
			       opts.path,
			       'fire',
			       name,
			       opts.message
			]
		};
		var str = JSON.stringify(req);
		this.wsock.send(str);
	}
	
	

	axion.Client = Client;
}());