package org.ovirt.test;

import org.ovirt.test.TransactionSupport.TransactionScope;

public class Person2 {

	public void foo() {
		System.out.println("foo");
		bar();
	}

	public void bar() {
		TransactionSupport.runInScope(new TransactionMethod<Void>() {

			public Void run() {
				System.out.println("bar");
				return null;
			}
		}, TransactionScope.REQUIRED);
	}
	
	public static void main(String[] args) {
		Person2 p = new Person2();
		p.foo();
	}
}
