

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.log4j.Logger;

public class FormattingData {

	static Logger logger=Logger.getLogger(FormattingData.class);
	
	protected static boolean startWorking(){
		String activkey="";
		char[] carr=new char[200];
		String s="";
		/*try {
			s=InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			System.out.println("Error: "+e1.getMessage());
			logger.debug(e1.getMessage(), e1);
		}
		
		s.getBytes();*/
		
		FileInputStream fis=null;
		
		try{
			fis=new FileInputStream(new File("ActivationId"));
		} catch (FileNotFoundException e) {
			try{
				fis=new FileInputStream(new File("ActivationId.dat"));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				System.out.println("Registration Error 101 : Activation Key not found please mail RequestActivationId.txt from this folder to vendor for Activation Key");
				logger.debug("Registration Error 101 : Activation Key not found please mail RequestActivationId.txt from this folder to vendor for Activation Key");
				System.exit(0);
			}
		}
			
		try{
			
			if(fis!=null){
				InputStreamReader isr=new InputStreamReader(fis);
				
				if(isr!=null){
					if(isr.read(carr)>0){
						activkey=new String(carr).trim();
					}
					isr.close();
				}
				
				fis.close();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Registration Error 102 : "+e.getMessage());
			logger.debug("Registration Error 102 : "+e.getMessage());
			System.exit(0);
		}
		
		if("".equals(activkey)||activkey==null){
			return false;
		}
			
		String[] reqbytes=new String[50];
		boolean start=true;
		
		activkey=activkey.replace("Z", " ");
		
		
		String[] tmp=activkey.split(" ");
		String userid=tmp[1];
		activkey=tmp[0];
		
		if(!getUserid("C").equals((new StringBuffer(userid).reverse().toString())))
			return false;
		
		try {
			activkey=activkey.replace("A", " ");
			activkey=activkey.replace("B", " ");
			activkey=activkey.replace("C", " ");
			activkey=activkey.replace("D", " ");
			activkey=activkey.replace("E", " ");
			activkey=activkey.replace("F", " ");

			//System.out.println(activkey);

			String[] activkeyarr=activkey.split(" ");

			for(int i=0; i<activkeyarr.length; i++){
				reqbytes[i]=""+(Long.parseLong(activkeyarr[i])-512*(i+1));
			}
			
			Enumeration n=NetworkInterface.getNetworkInterfaces();
			NetworkInterface neti=null;
			
			byte[] b=null;
			byte[] u=null;
			while(n.hasMoreElements()){
				neti=(NetworkInterface) n.nextElement();

				if(neti.isLoopback())
					continue;

				start=true;
				
				b=neti.getHardwareAddress();
				
				if(b==null||b.length<3){
					start=false;
					continue;
				}

				int i=0;
				for(i=0; i<b.length; i++){
					//System.out.println(reqbytes[i]+ " "+b[i]);
					if(!reqbytes[i].equals(""+b[i])){
						start=false;
						break;
					}
				}
			
				if(start==true)
					break;
			}
			
		} catch (Exception e) {
			System.out.println("Registration Error 103 : "+e.getMessage());
			logger.debug("Registration Error 103 : "+e.getMessage());
			start=false;
		}
		
		return start;
	}

	public static String getUserid(String drive) {
		String result = "";
		try {
			File file = File.createTempFile("realhowto",".vbs");
			file.deleteOnExit();
			FileWriter fw = new java.io.FileWriter(file);

			String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
				+"Set colDrives = objFSO.Drives\n"
				+"Set objDrive = colDrives.item(\"" + drive + "\")\n"
				+"Wscript.Echo objDrive.SerialNumber";  // see note
			fw.write(vbs);
			fw.close();
			Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
			BufferedReader input =
				new BufferedReader
				(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				result += line;
			}
			input.close();
		}
		catch(Exception e){
			System.out.println("Registration Error 104 : "+e.getMessage());
			logger.debug("Registration Error 104 : "+e.getMessage(), e);
		}
		
		
		return result.trim();
	}
	
}
