package org.ovirt.test;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.ovirt.test.TransactionSupport.TransactionScope;

/**
 * This Proxy is used to intercept methods and invoke them using
 * TransactionSupport, if @Transactional annotation is present on them
 * and the scope value is not TransactionScope.NONE 
 */
public class TransactionProxy implements MethodInterceptor {
	
	private Object invocationTarget;
	public Object getInvocationTarget() {
		return invocationTarget;
	}



	public void setInvocationTarget(Object invocationTarget) {
		this.invocationTarget = invocationTarget;
	}



	public Object[] getParams() {
		return params;
	}



	public void setParams(Object[] params) {
		this.params = params;
	}



	public MethodProxy getInvocationProxy() {
		return invocationProxy;
	}



	public void setInvocationProxy(MethodProxy invocationProxy) {
		this.invocationProxy = invocationProxy;
	}


	private Object[] params;
	private MethodProxy invocationProxy;

	public Object intercept(Object obj, Method method, Object[] params,
			MethodProxy proxy) throws Throwable {
		setInvocationTarget(obj);
		setInvocationProxy(proxy);
		setParams(params);
		//The method at the super class is the one that is potentially annotated
		//with @Transactional - so it is important to retrieve it and not work on the 
		//provided method object
		Method originalMethod = obj.getClass().getSuperclass().getDeclaredMethod(method.getName());
		
		Object result = null;
		Transactional annotation = (originalMethod != null)?(Transactional)originalMethod.getAnnotation(Transactional.class):null;
		//If there is no @Transactional annotation, or its value is none - 
		//no need to invoke the method in a context of TransactionSupport
		if (annotation != null && annotation.scope() != TransactionScope.NONE) {
			result = TransactionSupport.runInScope(new TransactionMethod<Object>() {
				public Object run()  {
					try {
						return invokeSuper();
					} catch (Throwable ex) {
						throw new RuntimeException(ex);
					}
				}
			},annotation.scope());
		}
		else {
			result = invokeSuper();
		}
		return result;
	}	
	
	
	private Object invokeSuper() throws Throwable {
		return invocationProxy.invokeSuper(invocationTarget,params);
	}

	
	public static <T> T newInstance(Class<T> clazz) {
		Enhancer e = new Enhancer();
		e.setSuperclass(clazz);
		e.setCallback(new TransactionProxy());
		return (T) e.create();
	}
	
}
