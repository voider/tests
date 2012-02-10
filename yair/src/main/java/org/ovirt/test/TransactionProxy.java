package org.ovirt.test;

import java.lang.reflect.Method;

import org.ovirt.test.TransactionSupport.TransactionScope;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

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
		Method originalMethod = obj.getClass().getSuperclass().getMethod(method.getName());
		Object result = null;
		TransactionScope scope = TransactionScope.NONE;
		if (originalMethod.isAnnotationPresent(Transactional.class)) {
			scope = TransactionSupport.TransactionScope.REQUIRED;
		}
		result = TransactionSupport.runInScope(new TransactionMethod<Object>() {
			public Object run()  {
				try {
					return invokeSuper();
				} catch (Throwable ex) {
					throw new RuntimeException(ex);
				}
			}
		},scope);
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
