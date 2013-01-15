package com.coltware.axion.handler;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import axion.data.EventMessage;
import axion.data.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class AxionMessageEncoder extends MessageToMessageEncoder<Message, WebSocketFrame> {

	private static JsonFactory jsonFactory = new JsonFactory();
	private static ObjectMapper objMapper = new ObjectMapper();
	
	@Override
	public boolean isEncodable(Object msg) throws Exception {
		if(msg instanceof Message){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public WebSocketFrame encode(ChannelHandlerContext ctx, Message msg)
			throws Exception {
		ByteBuf buff = Unpooled.buffer();
		
		ByteBufOutputStream bbos = new ByteBufOutputStream(buff);
		
		JsonGenerator jg = jsonFactory.createGenerator(bbos, JsonEncoding.UTF8);
		jg.writeStartObject();
		
		if(msg instanceof EventMessage){
			jg.writeFieldName("event");
			
			jg.writeStartObject();
			jg.writeStringField("path", msg.getPath());
			EventMessage em = (EventMessage)msg;
			jg.writeStringField("type", em.getName());
			
			jg.writeFieldName("body");
			objMapper.writeValue(jg, em.getBody());
		}
		else{
			jg.writeFieldName("msg");
			
			jg.writeStartObject();
			jg.writeStringField("path", msg.getPath());
			jg.writeObjectField("body", msg.getBody());
		}
		jg.writeEndObject();
		jg.writeEndObject();
		
		jg.close();
		TextWebSocketFrame frame = new TextWebSocketFrame();
		frame.setBinaryData(buff);
		
		return frame;
	}

}
