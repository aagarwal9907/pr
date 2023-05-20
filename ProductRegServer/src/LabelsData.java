
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LabelsData {
    static Logger logger = Logger.getLogger(LabelsData.class);

    public static void getData() {

    }

    public static String getUserid(String drive) {
	
	StringBuilder result =new StringBuilder();
	File file = null;
	try {
	    file = File.createTempFile("realhowto", ".vbs");
	    file.deleteOnExit();
	} catch (IOException e1) {
	    logger.log(Level.FATAL,e1.getStackTrace());
	}
	try (FileWriter fw = new java.io.FileWriter(file)) {

	    String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
		    + "Set colDrives = objFSO.Drives\n" + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
		    + "Wscript.Echo objDrive.SerialNumber"; // see note
	    fw.write(vbs);
	    if (file != null) {
		Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
		BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line="";
		while ((line = input.readLine()) != null) {
		  result.append(line);
		}
		input.close();
	    }
	} catch (Exception e) {
	    logger.debug(e.getMessage(), e);
	}
	return  result.toString().trim();
    }
}
