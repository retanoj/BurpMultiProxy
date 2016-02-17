package burp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class BurpExtender implements ITab, IBurpExtender, IHttpListener {
	/*
	 * BurpSuite 插件
	 * 可以为HTTP请求自动添加HTTP代理
	 * 代理列表文件格式为每行ip:port 
	 */
	IExtensionHelpers helpers;
	IBurpExtenderCallbacks callbacks;
	JPanel jPanel;
	JTable jTable;
	
	IArrayList proxies;
	
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
		
		/* 总体水平布局 */
		Box HorizonalTop = Box.createHorizontalBox();
		/* 左垂直 */
		Box VerticalLeft = Box.createVerticalBox();
		/* 右垂直 */
		Box VerticalRight = Box.createVerticalBox();
		/* 左侧第二层 */
		Box HorizonalLayer2 = Box.createHorizontalBox();
		/* 左侧第三层 */
		Box HorizonalLayer3 = Box.createHorizontalBox();		
		
		/* 左侧Table（第一层） */
		String[] columnNames = {"IP", "Port"};
		Object[][] o = {{"",""}};
		jTable = new JTable(new DefaultTableModel(o, columnNames));
		jTable.setPreferredScrollableViewportSize(new Dimension(300,200));
		jTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		jTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		jTable.setBackground(Color.LIGHT_GRAY);
		JScrollPane jScrollPane = new JScrollPane(jTable);
		
		proxies = new IArrayList(jTable);
		
		VerticalLeft.add(Box.createVerticalStrut(30));
		VerticalLeft.add(jScrollPane);
		
		/* 右侧添加按钮 */
		JButton load = new JButton(" Load ");
		load.setBounds(0,0,300,50);
		load.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){	proxies.loadProxy(); }
		});		
		
		JButton delete = new JButton("Delete");
		delete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { proxies.deleteRows(); }
		});

		JButton save = new JButton(" Save ");
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { proxies.saveProxies(); }
		});
				
		JButton clean = new JButton(" Clean");
		clean.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){ proxies.DeleteAll(); }
		});
		
		VerticalRight.add(Box.createVerticalStrut(45));
		VerticalRight.add(load);
		VerticalRight.add(Box.createVerticalStrut(20));
		VerticalRight.add(delete);
		VerticalRight.add(Box.createVerticalStrut(20));
		VerticalRight.add(save);
		VerticalRight.add(Box.createVerticalStrut(20));
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
		JButton addButton = new JButton("  Add  ");
		addButton.addActionListener(new addButtonListener(jIpText, jPortText));
		VerticalRight.add(Box.createVerticalStrut(40));
		VerticalRight.add(addButton);
		VerticalRight.add(Box.createVerticalGlue());
		
		/* 左侧第三层添加MODE单选框 */
		JRadioButton jRadioButton0 = new JRadioButton("顺序模式", true);
		jRadioButton0.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){ proxies.setSerialMode(); }
		});
		JRadioButton jRadioButton1 = new JRadioButton("随机模式");
		jRadioButton1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){ proxies.setDiscreteMode(); }
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
	
	class addButtonListener implements ActionListener{
		JTextField jIpText;
		JTextField jPortText;
		
		addButtonListener(JTextField jIpText, JTextField jPortText){
			this.jIpText = jIpText;
			this.jPortText = jPortText;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			proxies.add(jIpText.getText(), Integer.parseInt(jPortText.getText()));
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
	public void processHttpMessage(
		int toolFlag, 
		boolean messageIsRequest,
		IHttpRequestResponse messageInfo) {
		
		if(messageIsRequest && proxies.size() > 0){
			ProxyAddr p = proxies.getProxy();
			messageInfo.setHttpService(
				helpers.buildHttpService(p.ip, p.port, messageInfo.getHttpService().getProtocol() ) 
			);
	
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


