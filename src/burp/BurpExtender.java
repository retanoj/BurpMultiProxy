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
	JTable jTable;
	DefaultTableModel jTableModel;
	
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
		
		Box Vertical_in_h1 = Box.createVerticalBox();
		Box Horizonal1 = Box.createHorizontalBox();
		Box Horizonal2 = Box.createHorizontalBox();
		Box Vertical = Box.createVerticalBox();
		
		String[] columnNames = {"IP", "Port"};
		Object[][] o = {{"",""}};
		jTable = new JTable(new DefaultTableModel(o, columnNames));
		jTable.setPreferredScrollableViewportSize(new Dimension(300,200));
		jTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		jTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		jTable.setBackground(Color.LIGHT_GRAY);
		jTableModel = (DefaultTableModel) jTable.getModel();
		
		JScrollPane jScrollPane = new JScrollPane(jTable);
		Horizonal1.add(jScrollPane);
		
		JButton load = new JButton(" Load ");
		load.addActionListener(new loadButton(jTable));
		Vertical_in_h1.add(load);
		
		Vertical_in_h1.add(Box.createVerticalStrut(20));
		
		JButton delete = new JButton("Delete");
		delete.addActionListener(new deleteButton(jTable));
		Vertical_in_h1.add(delete);
		
		Vertical_in_h1.add(Box.createVerticalStrut(20));
		
		JButton save = new JButton(" Save ");
		save.addActionListener(new saveButton(jTable));
		Vertical_in_h1.add(save);
		
		Vertical_in_h1.add(Box.createVerticalStrut(20));
		
		JButton clean = new JButton(" Clean");
		clean.addActionListener(new cleanButton(jTable));
		Vertical_in_h1.add(clean);
		
		Horizonal1.add(Box.createHorizontalStrut(15));
		Horizonal1.add(Vertical_in_h1);
		
		JTextField jIpText = new JTextField();
		Horizonal2.add(new JLabel("IP:"));
		Horizonal2.add(Box.createHorizontalStrut(5));
		Horizonal2.add(jIpText);
		
		Horizonal2.add(Box.createHorizontalStrut(5));
		
		JTextField jPortText = new JTextField();	
		Horizonal2.add(new JLabel("Port:"));
		Horizonal2.add(Box.createHorizontalStrut(5));
		Horizonal2.add(jPortText);
		
		Horizonal2.add(Box.createHorizontalStrut(15));
		
		JButton add = new JButton("  Add  ");
		add.addActionListener(new addButton(jIpText, jPortText, jTable));
		Horizonal2.add(add);
		
		Vertical.add(Box.createVerticalStrut(30));
		Vertical.add(Horizonal1);
		Vertical.add(Box.createVerticalStrut(10));
		Vertical.add(Horizonal2);
		jPanel.add(Vertical);
		
	}
	
	class cleanButton implements ActionListener{
		JTable jTable;
		
		cleanButton(JTable jTable){
			this.jTable = jTable;
		}
		
		@Override
		public void actionPerformed(ActionEvent event) {
			if (jTable.isEditing()) 
			    jTable.getCellEditor().stopCellEditing();
			jTableModel.setRowCount(0);
		}
		
	}
	
	class addButton implements ActionListener{
		JTable jTable;
		JTextField jIpText;
		JTextField jPortText;
		
		addButton(JTextField jIpText, JTextField jPortText, JTable jTable){
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
	
	class deleteButton implements ActionListener{
		JTable jTable;
		
		deleteButton(JTable jTable){
			this.jTable = jTable;
		}
		@Override
		public void actionPerformed(ActionEvent event) {
			int[] rmi = jTable.getSelectedRows();
			for (int i=rmi.length-1; i>=0; i--){
				jTableModel.removeRow(rmi[i]);
			}
		}
	}
	class saveButton implements ActionListener{
		JTable jTable;
		
		saveButton(JTable jTable){
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
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}
	class loadButton implements ActionListener{
		JTable jTable;
		
		loadButton(JTable jTable){
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
			int num = (int)(Math.random() * jTableModel.getRowCount());
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
