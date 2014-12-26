package burp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BurpExtender implements ITab, IBurpExtender, IHttpListener {
	
	IExtensionHelpers helpers;
	IBurpExtenderCallbacks callbacks;
	JPanel jPanel;
	JTableX jTable;
	DefaultTableModel jTableModel;
	
	int MODE = 0;
	
	public static void main(String[] args) {
		BurpExtender b = new BurpExtender();
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		b.createPanel();
		frame.getContentPane().add(BorderLayout.CENTER, b.jPanel);
		frame.setSize(800,600);
		frame.setVisible(true);
	}
	
	void createPanel(){
		jPanel = new JPanel();
		
		Box VerticalLeft = Box.createVerticalBox();
		Box VerticalRight = Box.createVerticalBox();
		Box HorizonalLayer2 = Box.createHorizontalBox();
		Box HorizonalLayer3 = Box.createHorizontalBox();		
		Box HorizonalTop = Box.createHorizontalBox();
		
		/* 左侧添加Table */
		String[] columnNames = {"IP", "Port"};
		Object[][] o = {{"",""}};
		jTable = new JTableX(new DefaultTableModel(o, columnNames));
		jTable.setPreferredScrollableViewportSize(new Dimension(300,200));
		jTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		jTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		jTable.setBackground(Color.LIGHT_GRAY);
		jTableModel = (DefaultTableModel) jTable.getModel();
		
		
		JScrollPane jScrollPane = new JScrollPane(jTable);
		VerticalLeft.add(Box.createVerticalStrut(30));
		VerticalLeft.add(jScrollPane);
		
		/* 右侧添加按钮 */
		JButton load = new JButton(" Load ");
		load.addActionListener(new loadButton(jTable));
		VerticalRight.add(Box.createVerticalStrut(45));
		VerticalRight.add(load);
		
		load.setVerticalTextPosition(SwingConstants.TOP);
		
		VerticalRight.add(Box.createVerticalStrut(20));
		
		JButton delete = new JButton("Delete");
		delete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int[] rmi = jTable.getSelectedRows();
				for (int i=rmi.length-1; i>=0; i--){
					jTableModel.removeRow(rmi[i]);
				}
			}
		});
		VerticalRight.add(delete);
		
		VerticalRight.add(Box.createVerticalStrut(20));
		
		JButton save = new JButton(" Save ");
		save.addActionListener(new saveButton(jTable));
		VerticalRight.add(save);
		
		VerticalRight.add(Box.createVerticalStrut(20));
		
		JButton clean = new JButton(" Clean");
		clean.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (jTable.isEditing()) 
				    jTable.getCellEditor().stopCellEditing();
				//jTableModel.setRowCount(0);
				if(jTable.getRowCount() > 0){
					System.out.println(jTable.getIndex(MODE));
				}
			}
		});
		VerticalRight.add(clean);
		
		/* 左侧第二层添加ip-port输入框 */
		JTextField jIpText = new JTextField();
		HorizonalLayer2.add(new JLabel("IP:"));
		HorizonalLayer2.add(Box.createHorizontalStrut(5));
		HorizonalLayer2.add(jIpText);
		
		HorizonalLayer2.add(Box.createHorizontalStrut(5));
		
		JTextField jPortText = new JTextField();	
		HorizonalLayer2.add(new JLabel("Port:"));
		HorizonalLayer2.add(Box.createHorizontalStrut(5));
		HorizonalLayer2.add(jPortText);
		
		VerticalLeft.add(Box.createVerticalStrut(5));
		VerticalLeft.add(HorizonalLayer2);
		
		/* 右侧添加输入框对应的Add按钮 */
		JButton add = new JButton("  Add  ");
		add.addActionListener(new addButton(jIpText, jPortText, jTable));
		VerticalRight.add(Box.createVerticalStrut(40));
		VerticalRight.add(add);
		VerticalRight.add(Box.createVerticalGlue());
		
		/* 左侧第三层添加MODE单选框 */
		JRadioButton jRadioButton0 = new JRadioButton("顺序模式", true);
		jRadioButton0.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){ MODE = 0; }
		});
		JRadioButton jRadioButton1 = new JRadioButton("随机模式");
		jRadioButton1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){ MODE = 1; }
		});
		ButtonGroup jBtnGroup=new ButtonGroup();
		jBtnGroup.add(jRadioButton0);
		jBtnGroup.add(jRadioButton1);
		
		HorizonalLayer3.add(jRadioButton0);
		HorizonalLayer3.add(Box.createHorizontalStrut(40));
		HorizonalLayer3.add(jRadioButton1);
		HorizonalLayer3.add(Box.createHorizontalGlue());
		
		VerticalLeft.add(Box.createVerticalStrut(5));
		VerticalLeft.add(HorizonalLayer3);
		
		/* 总体布局 */
		HorizonalTop.add(VerticalLeft);
		HorizonalTop.add(Box.createHorizontalStrut(30));
		HorizonalTop.add(VerticalRight);
		
		jPanel.add(HorizonalTop);
		
	}
	
	class addButton implements ActionListener{
		JTableX jTable;
		JTextField jIpText;
		JTextField jPortText;
		
		addButton(JTextField jIpText, JTextField jPortText, JTableX jTable){
			this.jTable = jTable;
			this.jIpText = jIpText;
			this.jPortText = jPortText;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String[] _ip = {"",""};
			_ip[0] = jIpText.getText();
			_ip[1] = jPortText.getText();
			if(_ip[0].trim().length() != 0 && _ip[1].trim().length() != 0)
				jTableModel.addRow(_ip);
		}
		
	}
	
	class saveButton implements ActionListener{
		JTableX jTable;
		
		saveButton(JTableX jTable){
			this.jTable = jTable;
		}
		@Override
		public void actionPerformed(ActionEvent event) {
			JFileChooser dlg = new JFileChooser();
			dlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if(dlg.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				int rowCount = jTableModel.getRowCount();
				Writer w;
				try{
					w= new OutputStreamWriter(new FileOutputStream(dlg.getSelectedFile()), "UTF-8");
					for(int i=0; i<rowCount; i++){
						String s = String.format("%s:%s\n", jTableModel.getValueAt(i,0), jTableModel.getValueAt(i,1));
						if(!s.trim().equals(":"))
							w.write(s);
					}
					w.close();
					JOptionPane.showMessageDialog(null, "Saved done.", "Save",JOptionPane.INFORMATION_MESSAGE);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	class loadButton implements ActionListener{
		JTableX jTable;
		
		loadButton(JTableX jTable){
			this.jTable = jTable;
		}
		
		@Override
		public void actionPerformed(ActionEvent event){
			JFileChooser dlg = new JFileChooser();
			dlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if(dlg.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
				Reader reader;
				BufferedReader bReader;
				try {
					reader = new InputStreamReader(new FileInputStream(dlg.getSelectedFile()), "UTF-8");
					bReader = new BufferedReader(reader);
					String[] _ip;
					while(true){
						String line = bReader.readLine();
						if(line == null)
							break;
						if(line.trim().length() == 0)
							continue;
						_ip = line.split(":");
						jTableModel.addRow(_ip);
					}
					bReader.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		this.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		
		callbacks.setExtensionName("MultiProxy");
		
		createPanel();
		
		callbacks.registerHttpListener(this);
		callbacks.addSuiteTab(this);
	}

	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest,
			IHttpRequestResponse messageInfo) {
		if(messageIsRequest && jTableModel.getRowCount() > 0){
			int num = jTable.getIndex(MODE);
			if(jTableModel.getValueAt(num, 0).toString().trim().length() != 0 &&
				jTableModel.getValueAt(num, 1).toString().trim().length() != 0){
					messageInfo.setHttpService(
							helpers.buildHttpService(jTableModel.getValueAt(num, 0).toString(),
									Integer.parseInt(jTableModel.getValueAt(num, 1).toString()),
									messageInfo.getHttpService().getProtocol() ) );
			}
		}
	}

	@Override
	public String getTabCaption() {
		return "MultiProxy";
	}

	@Override
	public Component getUiComponent() {
		return jPanel;
	}
}


class JTableX extends JTable{
	private int MODE_SERIAL = 0;
	private int MODE_DISCRETE = 1;
	
	private int serial = -1;
	
	public JTableX(DefaultTableModel defaultTableModel) {
		super(defaultTableModel);
	}
	
	int getIndex(int MODE){
		if(MODE == MODE_SERIAL){
			// MODE_SERIAL
			return serial = (serial + 1) % this.getRowCount();
		} else{
			// MODE_DISCRETE
			return (int)(Math.random() * this.getRowCount());
		}
	}
	
}

