import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
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
import java.io.File;
import javax.swing.JRadioButton;
import javax.swing.JLabel;

public class SearchPage {
	private JFrame frame;
	private JTextField folder_path_txt;
	private JTextField search_txt;
	private QueryProcessor index;
	private JTextArea result_txt;
	private JScrollPane scrollPane;
	private JRadioButton boolean_rbtn;
	private JRadioButton default_ranked_rbtn;
	private JRadioButton tfidf_ranked_rbtn;
	private JRadioButton okapi_ranked_rbtn;
	private JRadioButton wacky_ranked_rbtn;
	private JLabel lbl_mode;
	private JLabel lbl_error;
	private boolean build_flag = false;
	private boolean query_flag = false;
	private int k = 0;
	Path folderPath;

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

		JButton btn_folder = new JButton("Folder");
		
		btn_folder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showOpenDialog(fc);
				folderPath = Paths.get("");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					folderPath = Paths.get(fc.getSelectedFile().toString());
					folder_path_txt.setText(folderPath.toString());
				}
				index = new QueryProcessor(folderPath);

				if (build_flag) {
					long time = System.currentTimeMillis();
					index.inverted_index.indexDirectory();
					index.inverted_index.calculateDocAvgToken();
					index.inverted_index.pos_hash_list.calculateWdt();
					System.out.println("Time taken to make inverted index is : "
							+ new SimpleDateFormat("mm,ss,sss").format(System.currentTimeMillis() - time));

					time = System.currentTimeMillis();
					index.disk_index_writer.buildIndex(index.inverted_index);
					System.out.println("Time taken to make disk inverted index is : "
							+ new SimpleDateFormat("mm,ss,sss").format(System.currentTimeMillis() - time));
				}

			}
		});
		btn_folder.setBounds(437, 10, 124, 23);
		frame.getContentPane().add(btn_folder);
		btn_folder.setEnabled(false);

		JButton btn_search = new JButton("Search");
		btn_search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result_txt.setText("");
				if (boolean_rbtn.isSelected())
					result_txt.setText(new QueryProcessor(folderPath).processQuery(search_txt.getText()));
				else
					result_txt.setText(new RankedQueryProcessor(folder_path_txt.getText().toString())
							.processQuery(search_txt.getText(), k, index));
			}
		});
		btn_search.setVisible(false);
		btn_search.setBounds(338, 62, 89, 23);
		frame.getContentPane().add(btn_search);

		search_txt = new JTextField();
		search_txt.setBounds(10, 63, 289, 20);
		frame.getContentPane().add(search_txt);
		search_txt.setColumns(10);
		search_txt.setVisible(false);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(437, 60, 807, 680);
		frame.getContentPane().add(scrollPane);

		result_txt = new JTextArea();
		scrollPane.setViewportView(result_txt);
		result_txt.setEditable(false);

		boolean_rbtn = new JRadioButton("Boolean");
		boolean_rbtn.setBounds(171, 117, 109, 23);
		frame.getContentPane().add(boolean_rbtn);
		boolean_rbtn.setVisible(false);

		default_ranked_rbtn = new JRadioButton("Default");
		default_ranked_rbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (default_ranked_rbtn.isSelected())
					k = 0;
			}
		});
		default_ranked_rbtn.setBounds(286, 117, 109, 23);
		frame.getContentPane().add(default_ranked_rbtn);
		default_ranked_rbtn.setVisible(false);

		tfidf_ranked_rbtn = new JRadioButton("Tf-Idf");
		tfidf_ranked_rbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tfidf_ranked_rbtn.isSelected())
					k = 1;
			}
		});
		tfidf_ranked_rbtn.setBounds(289, 162, 109, 23);
		frame.getContentPane().add(tfidf_ranked_rbtn);
		tfidf_ranked_rbtn.setVisible(false);

		okapi_ranked_rbtn = new JRadioButton("Okapi BM25");
		okapi_ranked_rbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (okapi_ranked_rbtn.isSelected())
					k = 2;
			}
		});
		okapi_ranked_rbtn.setBounds(289, 213, 109, 23);
		frame.getContentPane().add(okapi_ranked_rbtn);
		okapi_ranked_rbtn.setVisible(false);

		wacky_ranked_rbtn = new JRadioButton("Wacky");
		wacky_ranked_rbtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (wacky_ranked_rbtn.isSelected())
					k = 3;
			}
		});
		wacky_ranked_rbtn.setBounds(289, 259, 109, 23);
		frame.getContentPane().add(wacky_ranked_rbtn);
		wacky_ranked_rbtn.setVisible(false);

		ButtonGroup group = new ButtonGroup();
		group.add(boolean_rbtn);
		group.add(default_ranked_rbtn);
		group.add(tfidf_ranked_rbtn);
		group.add(okapi_ranked_rbtn);
		group.add(wacky_ranked_rbtn);
		
		JRadioButton[] buttons = new JRadioButton[] { boolean_rbtn, default_ranked_rbtn, tfidf_ranked_rbtn,
				okapi_ranked_rbtn, wacky_ranked_rbtn };

		JButton btn_build = new JButton("Build");
		btn_build.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btn_search.setVisible(false);
				search_txt.setVisible(false);
				lbl_error.setText("");
				lbl_mode.setText("Build Mode");
				build_flag = true;
				query_flag = false;
				btn_folder.setEnabled(true);
				for (JRadioButton btn : buttons) {
			         btn.setVisible(false);
			    }
			}
		});
		btn_build.setBounds(10, 10, 89, 23);
		frame.getContentPane().add(btn_build);

		JButton btn_query = new JButton("Query");
		btn_query.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				search_txt.setVisible(true);
				btn_search.setVisible(true);
				lbl_mode.setText("Query Mode");
				build_flag = false;
				query_flag = true;
				btn_folder.setEnabled(true);
				for (JRadioButton btn : buttons) {
			         btn.setVisible(true);
			    }
			}
		});
		btn_query.setBounds(166, 10, 89, 23);
		frame.getContentPane().add(btn_query);

		lbl_mode = new JLabel("Select A mode");
		lbl_mode.setBounds(289, 14, 138, 14);
		frame.getContentPane().add(lbl_mode);

		lbl_error = new JLabel("");
		lbl_error.setBounds(20, 94, 46, 14);
		frame.getContentPane().add(lbl_error);

		
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
