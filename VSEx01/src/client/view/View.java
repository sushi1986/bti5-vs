package client.view;

import client.controller.*;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JButton;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class View {

	private JFrame frame;
	private Controller cont;
	private JTextArea txtrLeser;

	public JTextArea getTxtrLeser() {
		return txtrLeser;
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					View window = new View();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public View() {
		cont = new Controller(this);
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 470, 606);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		final JTextPane txtpnRedakteurwrite = new JTextPane();
		txtpnRedakteurwrite.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() == '\n') {
					String text = txtpnRedakteurwrite.getText();
					text=text.substring(0, text.length()-1);
					cont.sendPressed(text);
					txtpnRedakteurwrite.setText("");
				}
			}
		});
		txtpnRedakteurwrite.setBounds(6, 511, 341, 29);
		frame.getContentPane().add(txtpnRedakteurwrite);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 42, 341, 457);
		frame.getContentPane().add(scrollPane);

		
		txtrLeser = new JTextArea();
		scrollPane.setColumnHeaderView(txtrLeser);
		txtrLeser.setEditable(false);

		JButton btnSend = new JButton("send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cont.sendPressed(txtpnRedakteurwrite.getText());
				txtpnRedakteurwrite.setText("");
			}
		});
		btnSend.setBounds(179, 550, 173, 29);
		frame.getContentPane().add(btnSend);
		
		JButton btnNewButton = new JButton("Receive");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cont.receive();
			}
		});
		btnNewButton.setBounds(364, 83, 98, 138);
		frame.getContentPane().add(btnNewButton);
		
		JButton btnReceiveAll = new JButton("Receive all");
		btnReceiveAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cont.receiveAll();
			}
		});
		btnReceiveAll.setBounds(364, 233, 98, 138);
		frame.getContentPane().add(btnReceiveAll);
		
		final JTextPane txtpnServerAddress = new JTextPane();
		txtpnServerAddress.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				if (arg0.getKeyChar() == '\n') {
					String text = txtpnServerAddress.getText();
					text=text.substring(0, text.length()-1);
					cont.connect(text);
					txtpnServerAddress.setText(text);
				}
			}
		});
		txtpnServerAddress.setText("Server Address");
		txtpnServerAddress.setBounds(6, 6, 341, 29);
		frame.getContentPane().add(txtpnServerAddress);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cont.connect(txtpnServerAddress.getText());

			}
		});
		btnConnect.setBounds(366, 6, 98, 29);
		frame.getContentPane().add(btnConnect);
		
		JButton btnNewButton_1 = new JButton("clear chatwindow");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getTxtrLeser().setText("");
			}
		});
		btnNewButton_1.setBounds(6, 550, 173, 29);
		frame.getContentPane().add(btnNewButton_1);
	}
}
