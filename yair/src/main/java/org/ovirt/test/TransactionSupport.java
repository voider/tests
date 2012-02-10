package org.ovirt.test;

public class TransactionSupport {
	
	public static enum TransactionScope {REQUIRED,REQUIRES_NEW,SUPPORTS,NONE};
	public static <T> T runInScope(TransactionMethod<T> method,TransactionScope scope) {
		System.out.println("Running code in transaction  " + scope.name());
		return method.run();
	}
}
