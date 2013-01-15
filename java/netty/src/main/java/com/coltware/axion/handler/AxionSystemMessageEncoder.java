package com.coltware.axion.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import axion.data.SystemMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class AxionSystemMessageEncoder extends MessageToMessageEncoder<SystemMessage, TextWebSocketFrame> {

	private static final Logger log = LoggerFactory.getLogger(AxionSystemMessageEncoder.class);
	
	private JsonFactory jsonFactory;
	
	public AxionSystemMessageEncoder(){
		jsonFactory = new JsonFactory();
	}
	
	@Override
	public boolean isEncodable(Object msg) throws Exception {
		if(msg instanceof SystemMessage){
			return true;
		}
		else{
			return false;
		}
	}

	@Override
	public TextWebSocketFrame encode(ChannelHandlerContext ctx,
			SystemMessage msg) throws Exception {
		
		ByteBuf buf = Unpooled.buffer();
		ByteBufOutputStream bbos = new ByteBufOutputStream(buf);
		
		JsonGenerator jg = jsonFactory.createGenerator(bbos, JsonEncoding.UTF8);
		jg.writeStartObject();
		jg.writeFieldName("system");
		
		jg.writeStartObject();
		jg.writeStringField("type", msg.getType());
		jg.writeObjectField("value", msg.getValue());
		
		jg.writeEndObject();
		
		jg.writeEndObject();
		jg.close();
		
		TextWebSocketFrame frame = new TextWebSocketFrame(buf);
		bbos.close();
		
		return frame;
	}

}
