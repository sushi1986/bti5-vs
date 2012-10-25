package de.haw.inet.vs.lab2;

import java.io.Serializable;

public class ResultMessage implements Serializable {

	private static final long serialVersionUID = -6065578273626197783L;

	public ResultMessage(int result) {

		this.result = result;
	}

	public int getResult() {
		return this.result;
	}

	private int result;
}