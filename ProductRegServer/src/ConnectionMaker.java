

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
public class ConnectionMaker {
	static Logger logger=Logger.getLogger(ConnectionMaker.class);
	
	public static Connection getConnection() throws Exception{
		Connection con=null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://192.168.22.7:3306/productdb", "appconnect", "Hosting@2023");

		} catch (ClassNotFoundException e) {
			logger.debug(e.getMessage());
			throw e;
		} catch (SQLException e) {
			logger.debug("Error connecting to database. Please check if mysqld is running or refer to operation manual for details "+e.getMessage());
			throw e;
		} 
		return con;
	}
	public static  void closeConnection(Connection con) throws SQLException {
		if(con!=null && !con.isClosed())
		con.close();
	}
	
	
}