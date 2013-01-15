package com.coltware.axion;

import java.util.Map;

import org.springframework.web.util.UriTemplate;

import axion.Context;
import io.netty.util.AttributeKey;

public class AttrKey {
	
	public static final AttributeKey<Context> CONTEXT = new AttributeKey<Context>("axion.context");
	public static final AttributeKey<UriTemplate> PATH = new AttributeKey<UriTemplate>("axion.path");
	public static final AttributeKey<Map<String, String>> PARAMS = new AttributeKey<Map<String,String>>("axion.params");
	
	public static final AttributeKey<ClassLoader> CLASS_LOADER = new AttributeKey<ClassLoader>("axion.classloader");
}
