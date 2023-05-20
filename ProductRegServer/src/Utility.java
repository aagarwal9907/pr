

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.apache.log4j.Logger;

public class Utility {
	static Logger logger=Logger.getLogger(Utility.class);
	
	static public Connection getConnection(){
		Connection con=null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql:///productdb","root", "");

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			logger.debug(e.getMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			logger.debug(e.getMessage());
		} 
		return con;
	}
	
	static public void log_incoming(String message, String mobileno, String status){
		Connection con=null;
		try {
			con=getConnection();

			PreparedStatement p=con.prepareStatement("insert into incoming_log values(?,?,?,now())");

			int paramidx=0;

			p.setString(++paramidx, message);
			p.setString(++paramidx, mobileno);
			p.setString(++paramidx, status);
			
			p.executeUpdate();				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.debug(e.getMessage());
		}finally{
			try{
				if(con!=null)
					con.close();
			}catch(Exception e){}
		}
	}
	
	public static String getMasterValueByName(String name){

		Connection con=null;
		String value=null;
		Statement s=null;

					
		try {
			con=ConnectionMaker.getConnection();
			
			if(con==null)
				return null;
			
			s=con.createStatement();
			s.execute("select value from generic_master where name='"+name+"'");
			ResultSet rs = s.getResultSet();

			if (rs != null){
				if ( rs.next() )
				{
					try{
						value=rs.getString(1);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						System.out.println("Error: "+e.getMessage());
						logger.debug(e.getMessage(), e);
					}
				}
			}

		} catch (Exception e) {
			logger.debug(e.getMessage());
			//e.printStackTrace();
		}finally{
			try {
				if(s!=null)
				s.close();
				
				if(con!=null)
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.debug(e.getMessage());
			} 
		}

		return value;
	}
	
	public static void updateMasterValue(String name, String value){

		Connection con=null;
		PreparedStatement s=null;

					
		try {
			con=ConnectionMaker.getConnection();
			
			if(con==null)
				return;
			
			s=con.prepareStatement("update generic_master set value=? where name=?");
			
			s.setString(1, value);
			s.setString(2, name);
			
			s.execute();
		} catch (Exception e) {
			logger.debug(e.getMessage());
			//e.printStackTrace();
		}finally{
			try {
				if(con!=null)
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.debug(e.getMessage());
			} 
		}

	}
	
	public static void copyFile(File sourceFile, File destFile) {  

		InputStream source = null;  
		OutputStream destination = null;  
		boolean success=true;
		
		logger.debug("source="+sourceFile+" destFile="+destFile);

		try { 
			if(!destFile.exists()) {   
				destFile.createNewFile();  
			}

			source = new FileInputStream(sourceFile);   
			destination = new FileOutputStream(destFile);   

			//destination.transferFrom(source, 0, source.size());  
			//sourceFile.renameTo(new File("C:/", destFile.getName()));

			byte[] buf = new byte[1024];
			int len;

			while((len = source.read(buf)) > 0){
				destination.write(buf, 0, len);
			}
			
			
		}catch(Exception e){
			success=false;
			System.out.println("Error: "+e.getMessage());
			logger.debug(e.getMessage(), e);
		}finally {   
			try {
				if(source != null) {    source.close();   }   
				if(destination != null) {    destination.close();   }
			} catch (IOException e) {
				logger.debug(e.getMessage());
			} 
			if(success){
				try {
					delete(sourceFile);
				} catch (IOException e) {
					System.out.println("Error: "+e.getMessage());
					logger.debug(e.getMessage(), e);
				}
			}
		}
	}
	
	public static boolean delete(File resource) throws IOException{ 

		if(resource.isDirectory()){

			File[] childFiles = resource.listFiles();

			for(File child : childFiles){

				delete(child);

			}

		}

		return resource.delete();

	}
	
}
