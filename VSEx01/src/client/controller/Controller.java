package client.controller;

import client.view.*;

public class Controller {
	
	private View view;
	
	public Controller(View view) {
		super();
		this.view=view;
	}
	
	public void sendPressed(String msg) {
		// TODO send msg to server 
		System.out.println(msg);
		updateTextview("me: "+msg);
	}
	
	public void updateTextview(String msg){
		// TODO get msg from server and update view; change parameter?!
		view.getTxtrLeser().setText(view.getTxtrLeser().getText() + "\n" + msg);
	}
	
	public void receive() {
		System.out.println("Receive");

	}
	
	public void receiveAll() {
		// TODO Auto-generated method stub
		System.out.println("Receive all");

	}
}
