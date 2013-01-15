package com.coltware.axion.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import axion.Connection;
import axion.channel.AbstractWSChannel;
import axion.channel.WSChannel;
import axion.channel.WSCommandChannel;
import axion.channel.WSP2PChannel;
import axion.channel.WSPubSubChannel;
import axion.channel.WSTemplateChannel;
import axion.data.DefaultEventMessage;
import axion.data.DefaultMessage;
import axion.impl.ResponseImpl;
import axion.request.EventRequest;
import axion.request.MessageRequest;
import axion.request.PostRequest;
import axion.request.PubSubRequest;
import axion.request.PubSubRequest.Method;
import axion.request.Request;

import com.coltware.axion.AttrKey;

public class AxionRequestHandler extends ChannelInboundMessageHandlerAdapter<Request> {

	private static final Logger log = LoggerFactory.getLogger(AxionRequestHandler.class);
	
	@Override
	public boolean isSupported(Object msg) throws Exception {
		if(msg instanceof Request){
			return true;
		}
		else{
			return false;
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		log.error(cause.getMessage(),cause);
		ResponseImpl response = new ResponseImpl();
		response.setCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.getCode());
		response.setMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		response.setSequence(0);
		Channel ch = ctx.channel();
		if(ch.isActive()){
			ctx.channel().write(response);
		}
		//super.exceptionCaught(ctx, cause);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, Request request)
			throws Exception {
		
		String path = request.getPath();
		log.debug("request:(" + request.getType() + "):" + request.getPath());
		
		
		Channel ch = ctx.channel();
		WSChannel wsc = request.getChannel();
		
		if(wsc != null){
			Map<String, String> map = ctx.channel().attr(AttrKey.PARAMS).get();
			if(!wsc.accept(request.getConnection(), map)){
				//
				ResponseImpl response = new ResponseImpl();
				response.setCode(HttpResponseStatus.FORBIDDEN.getCode());
				response.setMessage(HttpResponseStatus.FORBIDDEN.getReasonPhrase());
				response.setSequence(request.getSequence());
				ctx.channel().write(response);
				return;
			}
			try{
				
				if(wsc instanceof AbstractWSChannel){
					ClassLoader loader = ch.attr(AttrKey.CLASS_LOADER).get();
					if(loader != null){
						((AbstractWSChannel)wsc).setClassLoader(loader);
					}
				}
				
				if(wsc instanceof WSPubSubChannel){
					log.debug("pubsub channel:[" + path + "]");
					WSPubSubChannel psChannel = (WSPubSubChannel)wsc;
					if(request instanceof PubSubRequest){
						this.pubsubReceived(ctx, (WSPubSubChannel) wsc, (PubSubRequest)request);
					}
					else if(request instanceof MessageRequest){
						this.pubsubMessageRecieved(ctx, (WSPubSubChannel)wsc, (MessageRequest)request);
					}
					else if(request instanceof EventRequest){
						this.pubsubEventReceived(ctx, psChannel, (EventRequest)request);
					}
					else{
						log.warn("unsupport request:" + request.getClass());
					}
				}
				else if(wsc instanceof WSCommandChannel){
					log.debug("command channel:[" + path + "]");
					if(request instanceof PostRequest){
						this.commandPostReceived(ctx, (WSCommandChannel)wsc, (PostRequest)request);
					}
					else{
						
					}
				}
				else if(wsc instanceof WSP2PChannel){
					log.debug("p2p channel:[" + path + "]");
					// 	TODO
				}
			}
			catch(Exception ex){
				
				ResponseImpl response = new ResponseImpl();
				if(ex instanceof UnsupportedOperationException){
					response.setCode(HttpResponseStatus.METHOD_NOT_ALLOWED.getCode());
					response.setMessage(HttpResponseStatus.METHOD_NOT_ALLOWED.getReasonPhrase());
				}
				else{
					log.error(ex.getMessage(),ex);
					response.setCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.getCode());
					response.setMessage(HttpResponseStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
				}
				response.setContent(ex.getMessage());
				response.setSequence(request.getSequence());
				ch.write(response);
			}
		}
		else{
			log.debug("channel is NULL");
			ResponseImpl response = new ResponseImpl();
			response.setCode(HttpResponseStatus.NOT_FOUND.getCode());
			response.setMessage(HttpResponseStatus.NOT_FOUND.getReasonPhrase());
			response.setSequence(request.getSequence());
			ch.write(response);
		}
	}
	
	/**
	 * PubSub リクエスト処理
	 * 
	 * @param ctx
	 * @param channel
	 * @param request
	 * @throws Exception
	 */
	protected void pubsubReceived(ChannelHandlerContext ctx, WSPubSubChannel channel,PubSubRequest request) 
			throws Exception{
		Connection conn = request.getConnection();
		ResponseImpl res = new ResponseImpl();
		res.setSequence(request.getSequence());
		Method m = request.getMethod();
		if(m.equals(Method.SUB)){
			if(channel.subscribe(conn)){
				// OK
				log.debug(channel.getPath() + ";subscribe "+ channel.size());	
				res.setCode(HttpResponseStatus.ACCEPTED.getCode());
				res.setContent(channel);
				if(channel.hasEvent("sub")){
					int size = channel.size();
					DefaultEventMessage msg = new DefaultEventMessage();
					msg.setPath(request.getPath());
					msg.setName("sub");
					Map<String, Object> body = new HashMap<String, Object>();
					body.put("size", Integer.valueOf(size));
					msg.setBody(body);
					log.info("fire event..... " + msg);
					channel.fireEvent(msg);
				}
			}
			else{
				// NG
				res.setCode(HttpResponseStatus.FORBIDDEN.getCode());
			}
		}
		else if(m.equals(Method.UNSUB)){
			if(channel.unsubscribe(conn)){
				log.debug(channel.getPath() + ";subscribe "+ channel.size());	
				res.setCode(HttpResponseStatus.ACCEPTED.getCode());
				res.setContent(channel);
				
				if(channel.hasEvent("unsub")){
					int size = channel.size();
					DefaultEventMessage msg = new DefaultEventMessage();
					msg.setPath(request.getPath());
					msg.setName("unsub");
					Map<String, Object> body = new HashMap<String, Object>();
					body.put("size", Integer.valueOf(size));
					msg.setBody(body);
					log.info("fire event..... " + msg);
					channel.fireEvent(msg);
				}
			}
			else{
				res.setCode(HttpResponseStatus.FORBIDDEN.getCode());
			}
		}
		conn.write(res);
	}
	
	protected void pubsubMessageRecieved(ChannelHandlerContext ctx,WSPubSubChannel channel, MessageRequest request)
		throws Exception{
		Connection conn = request.getConnection();
		log.debug("pubsub message");
		
		Object out = null;
		if(channel instanceof WSTemplateChannel){
			WSTemplateChannel wst = (WSTemplateChannel)channel;
			Map<String, String> args = wst.match(request.getPath());
			out = ((WSTemplateChannel) channel).request(request.getContent(), args);
		}
		else{
			out = channel.request(request.getContent());
		}
		if(out != null){
			DefaultMessage message = new DefaultMessage();
			message.setPath(request.getPath());
			message.setBody(out);
			
			for(Connection c : channel.connections()){
				if(c != conn){
					c.write(message);
				}
			}
		}
	}
	
	protected void pubsubEventReceived(ChannelHandlerContext ctx,WSPubSubChannel channel, EventRequest request)
		throws Exception{
		log.debug("event request [" + request.getMethod() + "]");
		Connection conn = request.getConnection();
		String method = request.getMethod();
		if(method.equals("bind")){
			String name = request.getName();
			boolean ret = channel.bind(name, conn);
			if(ret){
				log.debug("bind ok [" + name + "]");
			}
		}
		else if(method.equals("fire")){
			log.debug("fire event");
			log.debug("fire:" + request.getContent());
			
			if(channel.hasEvent(request.getName())){
				DefaultEventMessage msg = new DefaultEventMessage();
				msg.setPath(request.getPath());
				msg.setName(request.getName());
				msg.setBody(request.getContent());
				channel.fireEvent(msg);
			}
			
		}
		
		
	}
	
	protected void commandPostReceived(ChannelHandlerContext ctx,WSCommandChannel channel, PostRequest request) 
			throws Exception{
		Connection conn = request.getConnection();
		Object out = null;
		if(channel instanceof WSTemplateChannel){
			log.debug("template channel " + channel);
			WSTemplateChannel wst = (WSTemplateChannel)channel;
			Map<String, String> args = wst.match(request.getPath());
			Object content = request.getContent();
			log.debug("input object is:" + content);
			out = ((WSTemplateChannel) channel).request(content, args);
		}
		else{
			log.debug("plain channel");
			out = channel.request(request.getContent());
		}
		if(out != null){
			ResponseImpl response = new ResponseImpl();
			response.setCode(HttpResponseStatus.OK.getCode());
			response.setSequence(request.getSequence());
			response.setContent(out);
			conn.write(response);
		}
		else{
			ResponseImpl response = new ResponseImpl();
			response.setCode(HttpResponseStatus.NOT_FOUND.getCode());
			response.setSequence(request.getSequence());
			conn.write(response);
		}
	}
}
