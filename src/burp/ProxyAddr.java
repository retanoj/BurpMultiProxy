package burp;

public class ProxyAddr {
	String ip;
	int port;
	
	public ProxyAddr(String ip, int port) throws Exception{
		if(ip.trim().length() != 0){
			this.ip = ip.trim();
		}else{
			throw new Exception("Invalid IP addr");
		}
		
		if(1<=port && port<=65535){
			this.port = port;
		}else{
			throw new Exception("Invalid Port");
		}
		
	}
}
