package burp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class IProxyList {
	private final int MODE_SERIAL = 0;
	private final int MODE_DISCRETE = 1;
	private int MODE;

	private int iSerialNum = -1;
	
	private JTable jTable;
	private DefaultTableModel jTableModel;
	
	private ArrayList<ProxyAddr> ProxyList = new ArrayList<ProxyAddr>();
	
	public IProxyList(JTable jt){
		jTable = jt;
		jTableModel = (DefaultTableModel) jt.getModel();
		
		setSerialMode();
	}
	
	public int getMODE() {
		return MODE;
	}

	public void setSerialMode() {
		MODE = MODE_SERIAL;
	}
	
	public void setDiscreteMode() {
		MODE = MODE_DISCRETE;
	}
	
	public int size(){
		return ProxyList.size();
	}
	
	public ProxyAddr getProxy(){
		if(MODE == MODE_SERIAL){
			// MODE_SERIAL
			iSerialNum = (iSerialNum + 1) % ProxyList.size();
			return ProxyList.get(iSerialNum);
		} 
		if(MODE == MODE_DISCRETE){
			// MODE_DISCRETE
			return ProxyList.get( (int)(Math.random() * ProxyList.size()) );
		}
		
		// default return
		return ProxyList.get( (int)(Math.random() * ProxyList.size()) );
	}

	public void DeleteAll(){
		ProxyList.clear();
		FlushTable();
	}
	
	public void FlushTable(){
		if (jTable.isEditing()) {
		    jTable.getCellEditor().stopCellEditing();
		}
		jTableModel.setRowCount(0);
		for(ProxyAddr p : ProxyList){
			jTableModel.addRow(new Object[]{p.ip, p.port});
		}
	}
	
	public void loadProxy(){
		JFileChooser dlg = new JFileChooser();
		dlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if(dlg.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			Reader reader;
			BufferedReader bReader;
			try {
				reader = new InputStreamReader(new FileInputStream(dlg.getSelectedFile()), "UTF-8");
				bReader = new BufferedReader(reader);
				String[] _strProxy;
				ProxyAddr _proxyaddr = null;
				while(true){
					String line = bReader.readLine();
					if(line == null)
						break;
					if(line.trim().length() == 0)
						continue;
					try{
						_strProxy = line.trim().split(":");						
						_proxyaddr = new ProxyAddr( _strProxy[0], Integer.parseInt(_strProxy[1]) );
						if(_proxyaddr != null ){							
							ProxyList.add(_proxyaddr);
						}
					}catch(Exception e){
						continue;
					}			
				}
				bReader.close();
				FlushTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void deleteRows(){
		int[] rmi = jTable.getSelectedRows();
		for(int i=rmi.length-1; i>=0; i--){
			ProxyList.remove(rmi[i]);
		}
		FlushTable();
	}
	
	public void saveProxies(){
		JFileChooser dlg = new JFileChooser();
		dlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if(dlg.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
			Writer w;
			try{
				w= new OutputStreamWriter(new FileOutputStream(dlg.getSelectedFile()), "UTF-8");
				for(ProxyAddr p: ProxyList){
					w.write(String.format("%s:%d\r\n", p.ip, p.port));
				}
				w.close();
				JOptionPane.showMessageDialog(null, "Saved done.", "Save",JOptionPane.INFORMATION_MESSAGE);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void add(String ip, int port){
		ProxyAddr p = null;
		try {
			p = new ProxyAddr(ip, port);
			if(p != null){			
				ProxyList.add(p);
			}
			FlushTable();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}

}
