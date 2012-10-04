package view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JButton;

import controller.Controller;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class View {

	private JFrame frame;
	private Controller cont;

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
		cont = new Controller();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 759, 563);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JTextArea txtrRedaktion = new JTextArea();
		txtrRedaktion.setEditable(false);
		txtrRedaktion.setText("redaktion");
		txtrRedaktion.setBounds(6, 6, 346, 472);
		frame.getContentPane().add(txtrRedaktion);

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
		txtpnRedakteurwrite.setText("redakteur_write");
		txtpnRedakteurwrite.setBounds(6, 490, 284, 29);
		frame.getContentPane().add(txtpnRedakteurwrite);

		JTextArea txtrLeser = new JTextArea();
		txtrLeser.setEditable(false);
		txtrLeser.setText("leser");
		txtrLeser.setBounds(407, 6, 346, 472);
		frame.getContentPane().add(txtrLeser);

		JButton btnSend = new JButton("send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cont.sendPressed(txtpnRedakteurwrite.getText());
				txtpnRedakteurwrite.setText("");
			}
		});
		btnSend.setBounds(290, 490, 67, 29);
		frame.getContentPane().add(btnSend);
	}
}
