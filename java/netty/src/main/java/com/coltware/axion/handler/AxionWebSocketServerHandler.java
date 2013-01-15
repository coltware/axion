package com.coltware.axion.handler;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriTemplate;

import axion.Context;
import axion.channel.WSChannel;
import axion.channel.WSCommandChannel;
import axion.data.SystemMessage;
import axion.impl.data.SystemMessageImpl;
import axion.registory.WSChannelRegistory;
import axion.request.AbstractRequest;
import axion.request.DefaultEventRequest;
import axion.request.DefaultMessageRequest;
import axion.request.DefaultPostRequest;
import axion.request.DefaultPubSubRequest;
import axion.request.PubSubRequest;
import axion.request.PubSubRequest.Method;
import axion.request.Request;

import com.coltware.axion.AttrKey;
import com.coltware.axion.NettyConnection;
import com.coltware.axion.util.HttpResponseUtil;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AxionWebSocketServerHandler extends
		ChannelInboundMessageHandlerAdapter<Object> {

	private static final Logger log = LoggerFactory.getLogger(AxionWebSocketServerHandler.class);
	private HttpRequest httpRequest;
	private WebSocketServerHandshaker handshaker;
	
	private JsonFactory jsonFactory;
	private ObjectMapper objMapper;
	
	private Context context;
	private WSChannelRegistory channelRegistory;
	private NettyConnection nettyConnection = null;
	
	public AxionWebSocketServerHandler(){
		jsonFactory = new JsonFactory();
		objMapper = new ObjectMapper(jsonFactory);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.debug("socket open");
		context = ctx.channel().attr(AttrKey.CONTEXT).get();
		channelRegistory = context.getWSChannelRegistroy();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		log.debug("socket close");
		if(nettyConnection != null){
			nettyConnection.close();
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		
		if(msg instanceof HttpRequest){
			handleHttpRequest(ctx, (HttpRequest) msg);
		}
		else if(msg instanceof WebSocketFrame){
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}
	
	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception{
		if(!req.getDecoderResult().isSuccess()){
			HttpResponseUtil.send(ctx, req, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		if(req.getMethod() != HttpMethod.GET){
			HttpResponseUtil.send(ctx, req, HttpResponseStatus.FORBIDDEN);
			return;
		}
		String uri = req.getUri();
		UriTemplate template = ctx.channel().attr(AttrKey.PATH).get();
		if(template != null){
			if(template.matches(uri)){
				Map<String, String> params = template.match(uri);
				ctx.channel().attr(AttrKey.PARAMS).set(params);
			}
			else{
				HttpResponseUtil.send(ctx, req, HttpResponseStatus.NOT_FOUND);
				return;
			}
		}
		
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				getWebSocketLocation(req), null, false);
		handshaker = wsFactory.newHandshaker(req);
		if(handshaker == null){
			WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
		}
		else{
			try{
				handshaker.handshake(ctx.channel(), req);
				this.httpRequest = req;
				log.debug("ws handshake ok");
				
				//	WebSocketのHandshakeが出来た
				Context axionCtx = ctx.channel().attr(AttrKey.CONTEXT).get();
				if(axionCtx == null){
					throw new Exception("please set context object to channel attributes");
				}
				
				// TODO 
				// 接続認証処理が必要!!
				
				nettyConnection = new NettyConnection(ctx.channel());
				WSCommandChannel systemChannel = axionCtx.getSystemCommandChannel();
				
				SystemMessageImpl sysMsg = new SystemMessageImpl();
				sysMsg.setType(SystemMessage.CONNECTED);
				sysMsg.setValue(systemChannel.getPath());
				
				ctx.channel().write(sysMsg);
			}
			catch(Exception ex){
				log.error(ex.getMessage(),ex);
				WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
			}
			
		}
	}
	
	/**
	 * WebSocketでのフレームデータが来たときの処理
	 * 
	 * @param ctx
	 * @param frame
	 * @throws Exception
	 */
	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception{
		log.debug("websock frame..." + frame.getClass().getSimpleName());
		
		if(frame instanceof CloseWebSocketFrame){
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame);
			return;
		}
		if(frame instanceof PingWebSocketFrame){
			ctx.channel().write(new PongWebSocketFrame(frame.getBinaryData()));
			return;
		}
		if(!(frame instanceof TextWebSocketFrame)){
			throw new UnsupportedOperationException(
					String.format("%s frame types not supported", frame.getClass().getName()));
		}
		
		TextWebSocketFrame tframe = (TextWebSocketFrame)frame;
		
		if(log.isDebugEnabled()){
			log.debug("get text frame:" + tframe.getText());
		}
		
		
		ByteBufInputStream bbis = new ByteBufInputStream(tframe.getBinaryData());
		JsonParser jp = jsonFactory.createJsonParser(bbis);
		JsonToken token = jp.nextToken();
		if(token != JsonToken.START_OBJECT){
			throw new UnsupportedOperationException("wrong json format");
		}
		Request request = null;
		while((token = jp.nextToken()) != JsonToken.END_OBJECT){
			String name = jp.getCurrentName();
			if(name != null){
				name = name.toLowerCase();
				if(name.equals("post")){
					request = this.parsePostJson(jp);
				}
				else if(name.equals("pubsub")){
					request = this.parsePubSubJson(jp);
				}
				else if(name.equals("message")){
					request = this.parseMessageJson(jp);
				}
				else if(name.equals("event")){
					request = this.parseEventJson(jp);
				}
				else{
					log.warn("Not support json type:[" + name + "]");
				}
				break;
			}
		}
		if(request != null){
			if(log.isDebugEnabled()){
				log.debug("request accepted..." + request.getType());
			}
			((AbstractRequest)request).setConnection(this.nettyConnection);
			ctx.nextInboundMessageBuffer().add(request);
			ctx.fireInboundBufferUpdated();
		}
		else{
			if(log.isDebugEnabled()){
				log.debug("request is in-valid or NULL");
			}
		}
		
	}
	
	/**
	 * POST 処理
	 * 
	 * @param jp
	 * @return
	 * @throws JsonParseException
	 * @throws IOException
	 */
	private Request parsePostJson(JsonParser jp) throws JsonParseException,IOException{
		JsonToken token = jp.nextToken();
		DefaultPostRequest request = null;
		Class objClz = null;
		if(token == JsonToken.START_ARRAY){
			int num = 0;
			while((token = jp.nextToken()) != JsonToken.END_ARRAY){
				if(num == 0){
					if(token == JsonToken.VALUE_NUMBER_INT){
						request = new DefaultPostRequest();
						request.setHttpRequest(this.httpRequest);
						request.setSequence(jp.getIntValue());
					}
				}
				if(request == null){
					return null;
				}
				if(num == 1){
					if(token == JsonToken.VALUE_STRING){
						String path = jp.getValueAsString();
						WSChannel ch = this.findChannel(path);
						if(ch != null){
							request.setChannel(ch);
							objClz = ch.inputClass();
						}
						else{
							return null;
						}
						request.setTargetPath(path);
					}
				}
				else if(num == 2){
					log.debug("objClz:" + objClz);
					Object data = null;
					if(objClz == null){
						if(token == JsonToken.START_OBJECT){
							data = objMapper.readValue(jp,new TypeReference<Map<String,Object>>(){});
						}
						else if(token == JsonToken.VALUE_STRING){
							data = jp.getValueAsString();
						}
					}
					else{
						data = objMapper.readValue(jp, objClz);
					}
					request.setContent(data);
				}
				num++;
			}
		}
		return request;
	}
	
	private Request parsePubSubJson(JsonParser jp) throws JsonParseException, IOException{
		JsonToken token = jp.nextToken();
		DefaultPubSubRequest request = null;
		if(token == JsonToken.START_ARRAY){
			int num = 0;
			while((token = jp.nextToken()) != JsonToken.END_ARRAY){
				if(num == 0){
					if(token == JsonToken.VALUE_NUMBER_INT){
						request = new DefaultPubSubRequest();
						request.setHttpRequest(this.httpRequest);
						request.setSequence(jp.getIntValue());
					}
				}
				if(request == null){
					return null;
				}
				if(num == 1){
					if(token == JsonToken.VALUE_STRING){
						String path = jp.getValueAsString();
						request.setTargetPath(path);
						WSChannel channel = this.findChannel(path);
						request.setChannel(channel);
					}
				}
				else if(num == 2){
					if(token == JsonToken.VALUE_STRING){
						String method = jp.getValueAsString();
						Method m = PubSubRequest.Method.fromString(method);
						if(m != null){
							request.setMethod(m);
						}
						else{
							request = null;
						}
					}
				}
				num++;
			}
		}
		return request;
	}
	
	private Request parseMessageJson(JsonParser jp) throws JsonParseException, IOException{
		log.debug("parse message json");
		JsonToken token = jp.nextToken();
		DefaultMessageRequest request = null;
		Class objClz = null;
		if(token == JsonToken.START_ARRAY){
			int num = 0;
			while((token = jp.nextToken()) != JsonToken.END_ARRAY){
				if(num == 0){
					if(token == JsonToken.VALUE_NUMBER_INT){
						request = new DefaultMessageRequest();
						request.setHttpRequest(this.httpRequest);
						request.setSequence(jp.getIntValue());
					}
				}
				if(request == null){
					return null;
				}
				if(num == 1){
					if(token == JsonToken.VALUE_STRING){
						String path = jp.getValueAsString();
						WSChannel ch = this.findChannel(path);
						if(ch != null){
							request.setChannel(ch);
							objClz = ch.inputClass();
						}
						request.setTargetPath(path);
					}
				}
				else if(num == 2){
					if(token == JsonToken.VALUE_STRING){
						String val = jp.getValueAsString();
						request.setContent(val);
					}
					else{
						@SuppressWarnings("unchecked")
						Object data = objMapper.readValue(jp, objClz);
						request.setContent(data);
					}
				}
				log.debug("message ..." + num + ":" + token);
				num++;
			}
		}
		log.debug("end parse message :" + request);
		return request;
	}
	
	private Request parseEventJson(JsonParser jp) throws JsonParseException, IOException{
		log.debug("parse message json");
		JsonToken token = jp.nextToken();
		DefaultEventRequest request = null;
		if(token == JsonToken.START_ARRAY){
			int num = 0;
			while((token = jp.nextToken()) != JsonToken.END_ARRAY){
				if(num == 0){
					if(token == JsonToken.VALUE_NUMBER_INT){
						request = new DefaultEventRequest();
						request.setHttpRequest(this.httpRequest);
						request.setSequence(jp.getIntValue());
					}
				}
				if(request == null){
					return null;
				}
				if(num == 1){
					if(token == JsonToken.VALUE_STRING){
						String path = jp.getValueAsString();
						request.setTargetPath(path);
						WSChannel channel = this.findChannel(path);
						request.setChannel(channel);
					}
				}
				else if(num == 2){
					if(token == JsonToken.VALUE_STRING){
						String method = jp.getValueAsString();
						request.setMethod(method);
					}
				}
				else if(num == 3){
					if(token == JsonToken.VALUE_STRING){
						String name = jp.getValueAsString();
						request.setName(name);
					}
				}
				else if(num == 4){
					Object data = null;
					if(token == JsonToken.START_OBJECT){
						data = objMapper.readValue(jp,new TypeReference<Map<String,Object>>(){});
					}
					else if(token == JsonToken.VALUE_STRING){
						data = jp.getValueAsString();
					}
					request.setContent(data);
				}
				log.debug("message ..." + num + ":" + token);
				num++;
			}
		}
		log.debug("end parse message :" + request);
		return request;
	}
	
	private WSChannel findChannel(String path){
		WSChannel wsc = channelRegistory.find(path);
		if(wsc != null){
			log.debug("findChannelClass(" + path + "): true");
			return wsc;
		}
		else{
			log.debug("findChannelClass(" + path + "): false");
			return null;
		}
	}
	
	private static String getWebSocketLocation(HttpRequest req) {
        return "ws://" + req.getHeader(HOST) + req.getUri();
    }
}
