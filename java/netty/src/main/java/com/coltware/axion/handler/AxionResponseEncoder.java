package com.coltware.axion.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import axion.Response;
import axion.channel.WSChannel;
import axion.channel.WSPubSubChannel;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class AxionResponseEncoder extends MessageToMessageEncoder<Response, WebSocketFrame> {

	private static final Logger log = LoggerFactory.getLogger(AxionResponseEncoder.class);
	private JsonFactory jsonFactory;
	
	public AxionResponseEncoder(){
		jsonFactory = new JsonFactory();
	}
	
	@Override
	public boolean isEncodable(Object msg) throws Exception {
		log.debug("isEncodable :" + msg.getClass());
		if(msg instanceof Response){
			return true;
		}
		else{
			log.debug("return false :" + msg.getClass());
			return false;
		}
	}

	@Override
	public WebSocketFrame encode(ChannelHandlerContext ctx, Response response)
			throws Exception {
		log.debug("encode from:" + response);
		
		ByteBuf buff = Unpooled.buffer();
		ByteBufOutputStream bbos = new ByteBufOutputStream(buff);
		
		JsonGenerator jg = jsonFactory.createGenerator(bbos, JsonEncoding.UTF8);
		
		jg.writeStartObject();
		jg.writeFieldName("response");
		
		jg.writeStartObject();
		jg.writeNumberField("code", response.getCode());
		jg.writeNumberField("seq", response.getSequence());
		jg.writeStringField("message", response.getMessage());
		
		Object content = response.getContent();
		if(content instanceof WSChannel){
			WSChannel channel = (WSChannel)content;
			
			jg.writeFieldName("channel");
			jg.writeStartObject();
			
			if(channel instanceof WSPubSubChannel){
				int size =  ((WSPubSubChannel) channel).size();
				if(size >= 0){
					jg.writeNumberField("size", size);
				}
			}
			jg.writeObjectField("path", channel.getPath());
			jg.writeEndObject();
		}
		else{
			jg.writeObjectField("content", response.getContent());
		}
		jg.writeEndObject();
		
		jg.writeEndObject();
		
		jg.close();
		
		TextWebSocketFrame frame = new TextWebSocketFrame();
		frame.setBinaryData(buff);
		
		log.debug("encode to:" + buff.toString(Charset.defaultCharset()));
		
		return frame;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		log.error(cause.getMessage(),cause);
		//super.exceptionCaught(ctx, cause);
	}
	
	

}
