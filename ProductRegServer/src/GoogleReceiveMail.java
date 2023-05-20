
 
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.net.ssl.SSLContext;

import com.sun.mail.pop3.POP3SSLStore; 


public class GoogleReceiveMail {

	/**
	 * @param args
	 * @throws NoSuchAlgorithmException 
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException {
 
		Security.addProvider(SSLContext.getDefault().getProvider()); 
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory"; 
		// Get a Properties object 
		Properties props = System.getProperties(); 
		props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY); 
		props.setProperty("mail.pop3.socketFactory.fallback", "false"); 
		props.setProperty("mail.pop3.port", "995"); 
		props.setProperty("mail.pop3.socketFactory.port", "995"); 
		props.setProperty("mail.pop3.host", "pop.gmail.com"); 
		props.setProperty("mail.pop3.user", "talk420"); 
		props.setProperty("mail.pop3.passwd", "pssword"); 
		props.setProperty("mail.pop3.ssl", "true"); 
		Session session = Session.getInstance(props,null); 
		URLName urln = new URLName("pop3","pop.gmail.com",995,null,"talk420", "pssword"); 
		Store store = new POP3SSLStore(session, urln); 
		//session.setDebug(true); 
		try {
			store.connect("pop.gmail.com",args[0],args[1]); 
			Folder folder = store.getDefaultFolder(); 
			folder = folder.getFolder("INBOX"); 
			folder.open(Folder.READ_ONLY); 
			System.out.println("Message Count "+folder.getMessageCount()); 
			System.out.println("New Message Count "+folder.getNewMessageCount()); 
			System.out.println("========================================="); 
			Message[] messages = folder.getMessages(); 
			FetchProfile fp = new FetchProfile(); 
			fp.add(FetchProfile.Item.ENVELOPE); 
			folder.fetch(messages, fp); 

			for (int i = 0; i < 5; i++) { 
				System.out.println("From:"+ messages[i].getFrom()); 
			} 

			folder.close(true);
			store.close();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	} 
}
