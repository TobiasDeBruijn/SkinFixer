package dev.array21.skinfixer.util;

public class Pair<A, B> {

	private A a;
	private B b;
	
	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}
	
	public A getA() {
		return this.a;
	}
	
	public B getB() {
		return this.b;
	}
}
