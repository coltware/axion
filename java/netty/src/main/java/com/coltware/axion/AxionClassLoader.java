package com.coltware.axion;

import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ClassPool;
import javassist.CtClass;

public class AxionClassLoader extends ClassLoader {

	private static final Logger log = LoggerFactory.getLogger(AxionClassLoader.class);
	
	private ClassLoader parentLoader;
	private ClassPool classPool;
	private Map<String,Class> definedClasses;
	private Map<String,ProtectionDomain> domainMap;

	public AxionClassLoader(ClassLoader parent) {
		this.parentLoader = parent;
		classPool = ClassPool.getDefault();
		this.definedClasses = new HashMap<String, Class>();
		this.domainMap = new HashMap<String, ProtectionDomain>();
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return this.findClass(name);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		
		try{
			if(name.startsWith("axion.sample")){
				log.debug("find class:" + name + " by " + this);
				if(definedClasses.containsKey(name)){
					return definedClasses.get(name);
				}
				
				CtClass ctclz = classPool.get(name);
				ProtectionDomain domain = this.getProtectionDomain(name);
				
				byte bytes[] = ctclz.toBytecode();
				log.debug("byte length :" + bytes.length);
				Class clz = this.defineClass(name, bytes, 0, bytes.length, domain);
				this.resolveClass(clz);
				
				this.definedClasses.put(name, clz);
				return clz;
			}
			else{
				return this.parentLoader.loadClass(name);
			}
		}
		catch(Exception ex){
			return this.parentLoader.loadClass(name);
		}
	}
	
	protected ProtectionDomain getProtectionDomain(String name) throws Exception{
		CtClass ctClz = classPool.get(name);
		if(ctClz != null){
			String filename = ctClz.getURL().getFile();
			log.debug("filename:" + filename);
			int pos = filename.indexOf("classes");
			if(pos > 0){
				String dir = filename.substring(0,pos);
				if(this.domainMap.containsKey(dir)){
					return this.domainMap.get(dir);
				}
				URL url = new URL("file:" + dir);
				CodeSource cs = new CodeSource(url, (Certificate[])null);
				Permissions p = new Permissions();
				p.add(new AllPermission());
				ProtectionDomain domain = new ProtectionDomain(cs, p);
				this.domainMap.put(dir, domain);
				return domain;
			}
		}
		return null;
	}
	
	
}
