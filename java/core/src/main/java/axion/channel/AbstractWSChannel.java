package axion.channel;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javassist.ClassPool;
import javassist.CtClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import axion.ns.Path;
import axion.responder.Responder;

public abstract class AbstractWSChannel implements WSChannel{
	
	private static final Logger log = LoggerFactory.getLogger(AbstractWSChannel.class);
	private static final ClassPool pool = ClassPool.getDefault();
	private ClassLoader classLoader = null;
	
	
	protected String path;
	
	protected Class responderClz;
	
	@SuppressWarnings("rawtypes")
	protected Class inClz;
	
	@SuppressWarnings("rawtypes")
	protected Class outClz;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public Class inputClass() {
		return this.inClz;
	}

	@Override
	public Class outputClass() {
		return this.outClz;
	}
	
	public void setClassLoader(ClassLoader loader){
		this.classLoader = loader;
	}
	
	public void setResponder(String clzname) throws Exception{
		log.debug("responder :" + clzname);
		CtClass ct = pool.get(clzname);
		Class clz = ct.toClass();
		log.debug("clz:" + clz);
		this._setResponder(clz);
		
		Object obj = clz.newInstance();
		if(obj instanceof Responder){
			this.responderClz = clz;
		}
	}
	
	private void _setResponder(Class clz){
		Type[] t = clz.getGenericInterfaces();
		if(t != null && t.length > 0){
			log.debug("responser " + t[0]);
			if(t[0] instanceof ParameterizedType){
				ParameterizedType pt = (ParameterizedType)t[0];
				Type[] io = pt.getActualTypeArguments();
				if(io != null && io.length == 2){
					Type in 	= io[0];
					Type out 	= io[1];
					if(in instanceof Class && out instanceof Class){
						this.inClz = (Class)in;
						this.outClz = (Class)out;
					}
				}
			}
		}
		if(this.path == null){
			Method[] m = clz.getMethods();
			for(int i = 0; i<m.length; i++){
				Method mtd = m[i];
				if(mtd.getName().equals("response")){
					Path _path = mtd.getAnnotation(Path.class);
					if(_path != null){
						String _path_str = _path.value();
						this.setPath(_path_str);
					}
				}
			}
		}
	}

	@Override
	public Object request(Object in) throws Exception{
		if(this.responderClz != null){
			Responder responder = this.createResponder();
			if(responder != null){
				return responder.response(in,null);
			}
		}
		return null;
	}
	
	protected Responder createResponder() throws Exception{
		if(this.classLoader != null){
			Class clz = this.classLoader.loadClass(responderClz.getName());
			if(clz != null){
				Responder res = (Responder)clz.newInstance();
				return res;
			}
		}
		else{
			return (Responder)responderClz.newInstance();
		}
		return null;
	}
}