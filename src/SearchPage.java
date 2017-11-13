import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.nio.file.Path; 
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;
import javax.swing.text.Utilities;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import javax.swing.JRadioButton;

public class SearchPage {
	private JFrame frame;
	private JTextField folder_path_txt;
	private JTextField search_txt;
	private QueryProcessor index;
	private JTextArea result_txt;
	private JScrollPane scrollPane;
	private JRadioButton disk_index_rbtn;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SearchPage window = new SearchPage();
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
	public SearchPage() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1270, 790);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		folder_path_txt = new JTextField();
		folder_path_txt.setEditable(false);
		folder_path_txt.setBounds(571, 11, 673, 20);
		frame.getContentPane().add(folder_path_txt);
		folder_path_txt.setColumns(10);

		JButton btnNewButton = new JButton("Folder");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(fc);
				Path folderPath = Paths.get("");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					folderPath = Paths.get(fc.getSelectedFile().toString());
					folder_path_txt.setText(folderPath.toString());
				}
				index = new QueryProcessor(folderPath);
				
				long time = System.currentTimeMillis();
				index.inverted_index.indexDirectory();
				System.out.println("Time taken to make inverted index is : "+new SimpleDateFormat("mm,ss,sss").format(System.currentTimeMillis()-time));
				
//				time = System.currentTimeMillis();
//				index.disk_index_writer.buildIndex(index.inverted_index);
//				System.out.println("Time taken to make disk inverted index is : "+new SimpleDateFormat("mm,ss,sss").format(System.currentTimeMillis()-time));
				
			}
		});
		btnNewButton.setBounds(437, 10, 124, 23);
		frame.getContentPane().add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Search");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result_txt.setText("");
				result_txt.setText(index.processQuery(search_txt.getText(), disk_index_rbtn.isSelected()? false: true));
			}
		});
		btnNewButton_1.setBounds(309, 10, 89, 23);
		frame.getContentPane().add(btnNewButton_1);

		search_txt = new JTextField();
		search_txt.setBounds(10, 11, 289, 20);
		frame.getContentPane().add(search_txt);
		search_txt.setColumns(10);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(437, 60, 807, 680);
		frame.getContentPane().add(scrollPane);	

		result_txt = new JTextArea();
		scrollPane.setViewportView(result_txt);
				result_txt.setEditable(false);
				
				disk_index_rbtn = new JRadioButton("Use DIsk Index");
				disk_index_rbtn.setBounds(22, 62, 109, 23);
				frame.getContentPane().add(disk_index_rbtn);
				
				result_txt.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						try {
							int pt = result_txt.viewToModel(e.getPoint());
							int spt = Utilities.getWordStart(result_txt, pt);
							int ept = Utilities.getWordEnd(result_txt, pt);
							result_txt.setSelectionStart(spt);
							result_txt.setSelectionEnd(ept);
							File file = new File(folder_path_txt.getText() + "\\" + result_txt.getSelectedText() + ".txt");
							if (file.exists()) {
								ProcessBuilder pb = new ProcessBuilder("Notepad.exe", file.toString());
								pb.start();
							}
						} catch (Exception ex) {
						}
					}
				});
	}
}
