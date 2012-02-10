package org.ovirt.test;

import org.ovirt.test.TransactionSupport.TransactionScope;



public class Person {
	
	public void foo() {
		System.out.println("foo");
		bar();
	}
	
	@Transactional(scope=TransactionScope.REQUIRED)  
	public void bar() {
		System.out.println("bar");
	}



	public static void main(String[] args) {
		Person p = TransactionProxy.newInstance(Person.class);
		p.foo();
	}
}
