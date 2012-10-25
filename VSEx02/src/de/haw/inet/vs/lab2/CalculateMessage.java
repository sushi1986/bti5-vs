package de.haw.inet.vs.lab2;

import java.io.Serializable;

public class CalculateMessage implements Serializable {
	private static final long serialVersionUID = 840244832287440949L;
	private int b;
	private int a;

	public CalculateMessage(int a, int b) {
		this.b = b;
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public int getA() {
		return a;
	}
}