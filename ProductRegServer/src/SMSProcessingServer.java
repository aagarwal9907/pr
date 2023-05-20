
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class SMSProcessingServer {
    // default ip
    public static String SERVERIP = "localhost";
    public static String REMOTESERVERIP = "localhost";
    private static int servertype = 0;
    // public static String PHONESERVERIP = "42.109.10.48";
    static int smscounter = 0;
    // designate a port
    /*public static final int SERVERPORT_RECV = 8079;
    public static final int SERVERPORT_SEND = 8078;*/

    private static String NOTSUCCESS = "";
    private static String BRANDINFO = "";
    private static String NOTAUTHORIZED = "";
    private static String NOPASSWD = "";
    private static int SMSLIMIT = 2;

    private static String adbfwd1 = "cmd /c adb forward tcp:8079 tcp:8079";
    private static String adbfwd2 = "cmd /c adb forward tcp:8078 tcp:8078";
    private static String adbfwd3 = "cmd /c adb forward tcp:8078 tcp:8078";

    static Logger logger = Logger.getLogger(SMSProcessingServer.class);

    private static void loadConstants() {
        NOTSUCCESS = "This is not a genuine product number check the number and sms again";
        BRANDINFO = "$remark2$, $brand$, $prod_code$, $remark3$, $remark4$, $remark5$, $remark6$, $remark7$, $ac_name$, $desp_name$";
        NOTAUTHORIZED = "You are not unauthorized to unregister the product. Please contact the vendor.";
        NOPASSWD = "Please SMS <16 digit code> <4 digit password>. Password located ";

        NOTSUCCESS = Utility.getMasterValueByName("NOTSUCCESSMSG");
        BRANDINFO = Utility.getMasterValueByName("BRANDINFOMSG");
        NOPASSWD = Utility.getMasterValueByName("NOPASSWD");
        SMSLIMIT = Integer.parseInt(Utility.getMasterValueByName("SMSLIMIT"));
    }

    public static void main(String[] args) {

        String adbdevices = "cmd /c adb devices";

        int devno = -1;
        String[] devices = new String[5];

        try {
            Process p = Runtime.getRuntime()
                .exec(adbdevices);
            InputStream in = p.getInputStream();
            BufferedReader b = new BufferedReader(new InputStreamReader(in));

            do {
                devno++;
                devices[devno] = b.readLine();
            } while (devices[devno] != null);

            if (devno < 2) {
                System.out.println("No devices found. Exiting.");
                logger.info("No devices found. Exiting.");
                // System.exit(0);
            }

            if (devno > 3) {
                devices[1] = devices[1].substring(0, devices[1].indexOf("device"))
                    .trim();
                devices[2] = devices[2].substring(0, devices[2].indexOf("device"))
                    .trim();
                System.out.println("Found Devices :" + devices[1] + "; " + devices[2]);
                logger.info("Found Devices :" + devices[1] + "; " + devices[2]);
            } else if (devno > 2) {
                devices[1] = devices[1].substring(0, devices[1].indexOf("device"))
                    .trim();
                System.out.println("Found Device :" + devices[1]);
                logger.info("Found Device :" + devices[1]);
            }

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            System.out.println("Error 1: " + e1.getMessage());
            logger.debug(e1.getMessage(), e1);
        }

        loadConstants();

        try {
            if (devno > 4) {
                // System.out.println(">4");
                adbfwd1 = "cmd /c adb -s " + devices[2] + " forward tcp:8079 tcp:8079";
                adbfwd2 = "cmd /c adb -s " + devices[1] + " forward tcp:8078 tcp:8078";
                adbfwd3 = "cmd /c adb -s " + devices[3] + " forward tcp:8078 tcp:8078";
            } else if (devno > 3) {
                // System.out.println(">3");
                adbfwd1 = "cmd /c adb -s " + devices[2] + " forward tcp:8079 tcp:8079";
                adbfwd2 = "cmd /c adb -s " + devices[1] + " forward tcp:8078 tcp:8078";
                adbfwd3 = "cmd /c adb -s " + devices[2] + " forward tcp:8078 tcp:8078";
            } else if (devno > 2) {
                // System.out.println(">2");
                adbfwd1 = "cmd /c adb -s " + devices[1] + " forward tcp:8079 tcp:8079";
                adbfwd2 = "cmd /c adb -s " + devices[1] + " forward tcp:8078 tcp:8078";
                adbfwd3 = "cmd /c adb -s " + devices[1] + " forward tcp:8078 tcp:8078";
            }

            Runtime.getRuntime()
                .exec(adbfwd1);
            Thread.sleep(1000);
            Runtime.getRuntime()
                .exec(adbfwd2);
            Thread.sleep(1000);
            Runtime.getRuntime()
                .exec(adbfwd3);
            Thread.sleep(2000);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.debug(e.getMessage() + "\nError in device communication. Please refer the installation guide or contact vendor.");
            System.out.println(e.getMessage() + "\nError in device communication. Please refer the installation guide or contact vendor.");
            // e.printStackTrace();
        }

        if (args.length == 1) {
            if (!args[0].equals("main")) {
                System.out.println("Invalid Arguments\n");
                System.out.println("Usage :\n");
                System.out.println("For main server : java SMSProcessingServer main");
                System.out.println("For all other locations : java SMSProcessingServer branch <IP>\n");
                System.out.println("Where <IP> is the IP address of machine hosting main server");
                System.exit(0);
            }
            // System.out.println("Product Registration Suite by Jeetronics. Mail to pai.gurudutt@gmail.com or jeetronics.mumbai@gmail.com for product support.");
            System.out.println("Welcome to Product Registration Suite.");
            LabelsData.getData();

            ServerThread t = new SMSProcessingServer().new ServerThread(8079, 8078);
            new Timer().scheduleAtFixedRate(t, 5000, 20000);

            servertype = 2;

            logger.debug("Calling scheduler starter");
            new SchedulerStarter();

            ServerThread t1 = new SMSProcessingServer().new ServerThread(true);
            t1.run();

        } else if (args.length == 2) {
            ServerThread t = new SMSProcessingServer().new ServerThread(8079, 8078);
            new Timer().scheduleAtFixedRate(t, 5000, 20000);

            if (!args[0].equals("branch")) {
                System.out.println("Invalid Arguments\n");
                System.out.println("Usage :\n");
                System.out.println("java SMSProcessingServer main");
                System.out.println("java SMSProcessingServer branch <IP>\n");
                System.out.println("For branch server <IP> is the IP address of machine hosting main server");
                System.exit(0);
            } else {
                REMOTESERVERIP = args[1];
                servertype = 3;
            }
        } else {
            System.out.println("Invalid number of arguments\n");
            System.out.println("Usage :\n");
            System.out.println("java SMSProcessingServer main");
            System.out.println("java SMSProcessingServer branch <IP>\n");
            System.out.println("For branch server <IP> is the IP address of machine hosting main server");
            System.exit(0);
        }
        // SERVERIP = getLocalIpAddress();
        // callSendSMSApp("Test","");
        // Thread fst = new Thread(new ServerThread());
    }

    public class ServerThread extends TimerTask {

        protected int SERVERPORT_RECV = 8079;
        protected int SERVERPORT_SEND = 8078;
        protected int RELAYLISTEN = 8077;
        boolean relayflag = false;

        ServerThread() {
            super();
        }

        ServerThread(int recvport, int sendport) {
            SERVERPORT_RECV = recvport;
            SERVERPORT_SEND = sendport;
        }

        ServerThread(boolean b) {
            relayflag = b;
        }

        public void run() {
            try {
                if (SERVERIP != null) {
                    // serverSocket = new ServerSocket(SERVERPORT_RECV);

                    do {
                        // listen for incoming SMS
                        // Socket client = serverSocket.accept();
                        Socket client = null;
                        ServerSocket server = null;
                        boolean valid = true;
                        boolean securitythreat = false;
                        try {// remove this try block
                            if (relayflag) {
                                server = new ServerSocket(RELAYLISTEN);
                                logger.debug("Waiting for relay SMS...");
                                client = server.accept();
                                logger.debug("Connected relay...");
                            } else {
                                System.out.println("Waiting for SMS...");
                                logger.debug("Waiting for SMS...");

                                client = new Socket("localhost", SERVERPORT_RECV);

                                logger.debug("Connected...");
                            }

                            try {
                                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                                String line = null;
                                while ((line = in.readLine()) != null) {
                                    securitythreat = false;

                                    logger.debug("Received SMS:" + line);

                                    logger.debug("Processing SMS" + line);

                                    String[] smsdetail = line.split("\\|");
                                    String password = null;

                                    String processedSMS = "Processed Your SMS. Thanks";

                                    String regcode[] = { "", "" };
                                    if (smsdetail[1] != null) {
                                        regcode = smsdetail[1].split(" ");
                                        logger.debug("Shortcode=" + regcode[0] + " Regcode=" + regcode[1]);

                                        if (!validatelength(smsdetail[1], smsdetail[0])) {
                                            valid = false;
                                            processedSMS = "Format of registration SMS is incorrect. Please check and try again or contact dealer.";
                                        }

                                        if (regcode[1] != null && regcode[1].length() == 21) {
                                            regcode = passwordspacer(regcode, smsdetail[1], smsdetail[0]);
                                        }

                                        if (regcode.length == 3)
                                            password = regcode[2];

                                        if (!("reg".equalsIgnoreCase(regcode[0]) || "relay".equalsIgnoreCase(regcode[0]))) {
                                            logger.debug("Shortcode=" + regcode[0] + " Regcode=" + regcode[1]);
                                            processedSMS = "Error in request. TYPE reg <prodcode>";
                                        } else {
                                            if (servertype == 2) {
                                                if (valid) {
                                                    if (securitycheck(smsdetail[0], "WRONGFORMAT_REQ_LIMIT", "FORMATERR")) {
                                                        String[] result = new String[2];
                                                        result[0] = "9";

                                                        if ("1".equals(Utility.getMasterValueByName("MOBILE_UNREG"))) {
                                                            if (securitycheck(smsdetail[0], "UNREG_REQ_LIMIT", "UNREG_REQ"))
                                                                result = unregister(regcode[1], smsdetail[0]);
                                                            else {
                                                                securitythreat = true;
                                                                processedSMS = "You have exceeded the allowable unregistration limit.";
                                                            }
                                                        }

                                                        if ("1".equals(result[0])) {
                                                            processedSMS = result[1];
                                                        } else {
                                                            result = verifyUniqueId(regcode[1], smsdetail[0], password);
                                                            processedSMS = result[1];
                                                        }
                                                    } else {
                                                        securitythreat = true;
                                                        processedSMS = "You have exceeded the allowable registration limit.";
                                                    }
                                                }
                                            } else if (servertype == 3) {
                                                if (password == null || password.trim()
                                                    .length() == 0)
                                                    processedSMS = relaySMSToMain("smsdetail[0]|relay" + " " + regcode[1]);
                                                else
                                                    processedSMS = relaySMSToMain("smsdetail[0]|relay" + " " + regcode[1] + " " + password);
                                            }
                                        }
                                    }

                                    logger.debug("Sending response : " + processedSMS + " to " + smsdetail[0]);

                                    if (servertype == 2 && "relay".equalsIgnoreCase(regcode[0])) {
                                        OutputStream os = client.getOutputStream();
                                        os.write(processedSMS.getBytes());
                                        os.close();
                                    } else if (securitythreat) {
                                        logger.debug("Security threat, not sending response");
                                        // donot respond or process if security threat
                                    } else if (!"relay".equalsIgnoreCase(regcode[0])) {
                                        // SMSSendApp Call Here
                                        callSendSMSApp(processedSMS, smsdetail[0]);
                                    }
                                }
                            } catch (Exception e) {
                                System.out.println("Error 2: " + e.getMessage());
                                logger.debug(e.getMessage(), e);
                            }
                        } catch (Exception ioe) {
                            System.out.println("Error 3: " + ioe.getMessage());
                            logger.debug(ioe.getMessage(), ioe);
                        }
                    } while (relayflag);
                } else {
                    logger.debug("Couldn't detect internet connection.");
                }

            } /*catch( Exception ioe){ uncomment this
              System.out.println("Could not connect to mobile device.\n Please check installation guide or contact vendor for details.");
              logger.debug("IOE no mobile connected.");
              System.exit(0);
              //ioe.printStackTrace();
              }*/catch (Exception e) {
                System.out.println("Error 4: " + e.getMessage());
                logger.debug(e.getMessage(), e);
            }
        }

        protected boolean securitycheck(String mobileno, String LIMIT, String TAG) {
            Connection con = null;
            boolean secure = true;
            String reqlimit = Utility.getMasterValueByName(LIMIT);

            int intreqlimit = 5;

            if (reqlimit != null) {
                try {
                    intreqlimit = Integer.parseInt(reqlimit);
                } catch (Exception e) {
                }
            }

            try {
                con = Utility.getConnection();

                PreparedStatement p = con.prepareStatement("SELECT count(*) from incoming_log where DATE_ADD(stamptime, INTERVAL 30 MINUTE ) >= NOW() and mobileno=? and status='" + TAG + "'");

                int paramidx = 0;

                p.setString(++paramidx, mobileno);

                ResultSet rs = p.executeQuery();

                if (rs != null && rs.next()) {
                    int count = rs.getInt(1);

                    if (count > intreqlimit)
                        secure = false;
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.debug(e.getMessage());
            } finally {
                try {
                    if (con != null)
                        con.close();
                } catch (Exception e) {
                }
            }

            return secure;
        }

        protected void callSendSMSApp(String message, String phoneno) {

            logger.debug("In callSendSMSApp");

            Socket echoSocket = null;
            PrintStream out = null;

            try {
                echoSocket = new Socket("localhost", SERVERPORT_SEND);
                out = new PrintStream(echoSocket.getOutputStream());
                out.println(message + "|" + phoneno);
                echoSocket.close();
                smscounter++;
                if (smscounter > SMSLIMIT) {
                    Runtime.getRuntime()
                        .exec(adbfwd3);
                    String swap = adbfwd3;
                    adbfwd3 = adbfwd2;
                    adbfwd2 = swap;
                    smscounter = 0;
                }
            } catch (UnknownHostException e) {
                logger.debug("Don't know about host: localhost." + e.getMessage());
                // e.printStackTrace();
                // System.exit(1);
            } catch (IOException e) {
                logger.debug("SMSProcessingServer Error 330:" + e.getMessage());
                // e.printStackTrace();
                /*System.err.println("Couldn't get I/O for "+ "the connection to: localhost.");*/
                // System.exit(1);
            }
            logger.debug("connected!!");

        }

        protected boolean validatelength(String smsstr, String mobileno) {

            if (smsstr == null)
                Utility.log_incoming(smsstr, mobileno, "FORMATERR");

            String[] regcode = smsstr.split(" ");
            String password = null;

            logger.debug("Shortcode=" + regcode[0] + " Regcode=" + regcode[1]);

            if (!(regcode.length == 2 || regcode.length == 3)) {
                Utility.log_incoming(smsstr, mobileno, "FORMATERR");
                return false;
            }

            if (regcode.length == 2) {
                if (regcode[1] == null || !(regcode[1].length() == 17 || regcode[1].length() == 21)) {
                    Utility.log_incoming(smsstr, mobileno, "FORMATERR");
                    return false;
                }

                return true;
            }

            if (regcode.length == 3) {
                password = regcode[2];

                if (password == null || password.length() != 4) {
                    Utility.log_incoming(smsstr, mobileno, "PASSWDERR");
                    return false;
                }
            }

            return true;
        }

        protected String[] passwordspacer(String[] regcode, String smsstr, String mobileno) throws Exception {
            String[] spacedregcode = new String[3];
            String predicttype = null;
            String codestr = regcode[1];

            if (codestr.indexOf('a') > -1) {
                predicttype = "a";
            } else if (codestr.indexOf('A') > -1) {
                predicttype = "A";
            } else if (codestr.indexOf('B') > -1) {
                predicttype = "B";
            } else if (codestr.indexOf('b') > -1) {
                predicttype = "b";
            } else {
                Utility.log_incoming(smsstr, mobileno, "FORMATERR");
                throw new Exception("FORMATERR");
            }

            String[] tempregcode = codestr.split(predicttype);

            if (tempregcode[0] == null || tempregcode[0].length() != 16) {
                Utility.log_incoming(smsstr, mobileno, "FORMATERR");
                throw new Exception("FORMATERR");
            }

            if (tempregcode[1] == null || tempregcode[1].length() != 4) {
                Utility.log_incoming(smsstr, mobileno, "FORMATERR");
                throw new Exception("FORMATERR");
            }

            spacedregcode[0] = regcode[0];
            spacedregcode[1] = tempregcode[0] + predicttype;
            spacedregcode[2] = tempregcode[1];

            return spacedregcode;
        }

        protected String relaySMSToMain(String prodcode) {
            Socket echoSocket = null;
            PrintStream out = null;
            InputStreamReader isr = null;
            BufferedReader bfr = null;
            String response = "Error processing SMS. Please try again later.";

            try {
                echoSocket = new Socket(REMOTESERVERIP, RELAYLISTEN);
                out = new PrintStream(echoSocket.getOutputStream());
                out.println(prodcode);
                logger.debug("Sent relay code");
                out.flush();
                isr = new InputStreamReader(echoSocket.getInputStream());
                bfr = new BufferedReader(isr);
                response = bfr.readLine();
                echoSocket.close();
                logger.debug("Relay response=" + response);
            } catch (UnknownHostException e) {
                logger.debug("Don't know about host: localhost.");
                // System.exit(1);
            } catch (IOException e) {
                logger.debug("SMSProcessingServer Error:" + e.getMessage());
                /*System.err.println("Couldn't get I/O for "+ "the connection to: localhost.");*/
                // System.exit(1);
            }
            logger.debug("connected!!..");
            return response;
        }

        // gets the ip address of your phone's network
        /*private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
                }
            }
        } catch (SocketException ex) {
            System.out.println("ServerActivity"+ex.toString());
        }
        return null;
        }*/

        String getOriginalNo(String uid, String type) {
            String originalno = "1234567898765431";

            return originalno;
        }

        String getSecFlags(String password, String type, String orderno, String seqno) {
            String usertype = "USER";
            String brand = "";
            String passwdloc = ".";
            String passwdsec = "N";

            // System.out.println("Here A="+usertype);

            Connection con = null;
            try {
                con = Utility.getConnection();

                String sql = "select brand, passwdsec, passwdloc from reg_dtl_datafile_pred where orderno=CONVERT(?,UNSIGNED) and CONVERT(start_seqno,UNSIGNED)<=CONVERT(?,UNSIGNED) and CONVERT(end_seqno,UNSIGNED)>=CONVERT(?,UNSIGNED)";

                if ("B".equalsIgnoreCase(type)) {
                    sql = "select brand, passwdsec,passwdloc from reg_dtl_datafile_unpred where orderno=CONVERT(?,UNSIGNED) and CONVERT(start_seqno,UNSIGNED)<=CONVERT(?,UNSIGNED) and CONVERT(end_seqno,UNSIGNED)>=CONVERT(?,UNSIGNED)";
                }

                PreparedStatement p = con.prepareStatement(sql);

                p.setString(1, orderno);
                p.setString(2, seqno);
                p.setString(3, seqno);

                ResultSet rs = p.executeQuery();

                if (rs.next()) {
                    brand = rs.getString("brand");
                    passwdsec = rs.getString("passwdsec");
                    passwdloc = rs.getString("passwdloc");
                }

                if (verifyAdminPasswd(password, brand)) {
                    usertype = "ADMIN";
                }

                if (passwdsec == null)
                    passwdsec = "N";
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                System.out.println("Error 5: " + e.getMessage());
                logger.debug(e.getMessage(), e);
            } finally {
                try {
                    if (con != null)
                        con.close();
                } catch (Exception e) {
                }
            }

            if (passwdsec == null)
                passwdsec = ".";

            if (passwdloc == null || "".equals(passwdloc))
                passwdloc = ".";

            // System.out.println("Here B="+usertype);

            return usertype + "|" + passwdsec + "|" + passwdloc;
        }

        // returns string[0]=1 if already registered, 2 if not valid number, 3 if success. Result message will be in result[1]
        String[] verifyUniqueId(String uid, String mobileno, String password) {
            String[] result = new String[2];
            String respmessage = "Congratulations for purchasing $remark4$ of the brand $remark2$ $remark3$ $remark5$ $remark6$";
            String type = "A"; // A=predictable, B=nonpredictable
            String usertype = "USER"; // USER, ADMIN
            String passwdsecure = "N";
            String passwdloc = "";

            if (uid == null || uid.length() < 17) {
                respmessage = NOTSUCCESS;
                result[0] = "2";
                result[1] = respmessage;
                return result;
            }

            type = uid.substring(uid.length() - 1, uid.length());
            uid = uid.substring(0, uid.length() - 1);

            String originalno = "";

            originalno = getOriginalNo(uid, type);

            logger.debug("originalno=" + originalno);

            String secFlags = getSecFlags(password, type, originalno.substring(0, 7), originalno.substring(7, 16));

            String[] tmp = secFlags.split("\\|");
            usertype = tmp[0];
            passwdsecure = tmp[1];
            passwdloc = tmp[2];

            if (password != null && password.trim()
                .length() != 0) {
                if ("ADMIN".equals(usertype))
                    respmessage = BRANDINFO;
            } else if ("Y".equalsIgnoreCase(passwdsecure)) {
                logger.debug("No Password entered");
                respmessage = NOPASSWD + passwdloc;
                result[0] = "2";
                result[1] = respmessage;
                return result;
            }

            Connection con = null;
            String sql = null;
            String brand = null;

            passwdloc = ".";
            boolean codecorrect = false;

            sql = "select * from reg_dtl_datafile_pred where orderno=CONVERT(?,UNSIGNED) and CONVERT(start_seqno,UNSIGNED)<=CONVERT(?,UNSIGNED) and CONVERT(end_seqno,UNSIGNED)>=CONVERT(?,UNSIGNED) ";

            if ("B".equalsIgnoreCase(type)) {
                sql = "select * from reg_dtl_datafile_unpred where orderno=CONVERT(?,UNSIGNED) and CONVERT(start_seqno,UNSIGNED)<=CONVERT(?,UNSIGNED) and CONVERT(end_seqno,UNSIGNED)>=CONVERT(?,UNSIGNED) ";
            }

            try {
                con = Utility.getConnection();

                PreparedStatement p = con.prepareStatement(sql);

                p.setString(1, originalno.substring(0, 7));
                p.setString(2, originalno.substring(7, 16));
                p.setString(3, originalno.substring(7, 16));

                ResultSet rs = p.executeQuery();

                if (rs.next()) {
                    passwdloc = rs.getString("passwdloc");
                    brand = rs.getString("brand");
                    codecorrect = true;
                }
            } catch (SQLException e) {
                logger.debug(e.getMessage());
            } finally {
                try {
                    if (con != null)
                        con.close();
                } catch (Exception e) {
                }
            }

            if (passwdloc == null || "".equals(passwdloc))
                passwdloc = ".";

            if ("USER".equals(usertype)) {

                try {
                    con = Utility.getConnection();

                    sql = "select * from registered_dtl where orderno=CONVERT(?,UNSIGNED) and seqno=CONVERT(?,UNSIGNED) and user_type='USER' and predicttype=?";

                    PreparedStatement p = con.prepareStatement(sql);

                    p.setString(1, originalno.substring(0, 7));
                    p.setString(2, originalno.substring(7, 16));
                    p.setString(3, type);

                    ResultSet rs = p.executeQuery();
                    System.out.println("Here 2");
                    RegistrationDetail rtmp = null;
                    boolean alreadyreg = false;
                    if (rs.next()) {
                        System.out.println("Here 3");
                        try {
                            rtmp = new RegistrationDetail();
                            alreadyreg = true;
                            rtmp.setBrand(rs.getString("brand"));
                            rtmp.setProdcode(rs.getString("prod_code"));
                            rtmp.setMobile(mobileno);
                            rtmp.setOrderno(originalno.substring(0, 7));
                            rtmp.setStartseqno(originalno.substring(7, 16));
                            rtmp.setPredicttype(type);

                        } catch (Exception e) {
                            System.out.println("Error 6: " + e.getMessage());
                            logger.debug(e.getMessage(), e);
                        }
                    }

                    if (alreadyreg) {
                        result[0] = "1";
                        result[1] = "This product has already been sold. Check out with the vendor as this product may NOT BE GENUINE";
                        logDuplicateProduct(rtmp);
                        return result;
                    }

                    sql = "select * from reg_dtl_bad_labels where seqno=CONVERT(?,UNSIGNED)";

                    p = con.prepareStatement(sql);
                    p.setString(1, uid + type);
                    rs = p.executeQuery();

                    rtmp = null;
                    alreadyreg = false;
                    if (rs.next()) {
                        try {
                            alreadyreg = true;
                        } catch (Exception e) {
                            System.out.println("Error 7: " + e.getMessage());
                            e.printStackTrace();
                            logger.debug(e.getMessage(), e);
                        }
                    }

                    if (alreadyreg) {
                        result[0] = "1";
                        result[1] = "Check out with the vendor as this product label may NOT BE GENUINE";
                        return result;
                    }

                    logger.debug(originalno.substring(0, 7) + " " + originalno.substring(7, 16));

                    if (brand == null)
                        logger.debug("Brand not found in uploaded data");

                    try {
                        if (brand != null) {
                            sql = "select success_msg from brand_dtl where brandname=?";

                            p = con.prepareStatement(sql);

                            p.setString(1, brand);

                            rs = p.executeQuery();

                            if (rs.next()) {
                                respmessage = rs.getString(1);
                            }
                        }
                    } catch (Exception e) {
                    }
                } catch (SQLException e) {
                    logger.debug(e.getMessage());
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                } finally {
                    try {
                        if (con != null)
                            con.close();
                    } catch (Exception e) {
                    }
                }
            }

            sql = "select * from reg_dtl_datafile_pred where orderno=CONVERT(?,UNSIGNED) and CONVERT(start_seqno,UNSIGNED)<=CONVERT(?,UNSIGNED) and CONVERT(end_seqno,UNSIGNED)>=CONVERT(?,UNSIGNED) ";

            if ("B".equalsIgnoreCase(type)) {
                sql = "select * from reg_dtl_datafile_unpred where orderno=CONVERT(?,UNSIGNED) and CONVERT(start_seqno,UNSIGNED)<=CONVERT(?,UNSIGNED) and CONVERT(end_seqno,UNSIGNED)>=CONVERT(?,UNSIGNED) ";
            }

            if ("Y".equalsIgnoreCase(passwdsecure)) {
                sql = sql + " and passwd=?";
            }

            try {
                String brandvalue = "";

                con = Utility.getConnection();

                PreparedStatement p = con.prepareStatement(sql);

                p.setString(1, originalno.substring(0, 7));
                p.setString(2, originalno.substring(7, 16));
                p.setString(3, originalno.substring(7, 16));
                if ("Y".equalsIgnoreCase(passwdsecure)) {
                    p.setString(4, password);
                }

                ResultSet rs = p.executeQuery();

                List tokens = getDollarTokens(respmessage);

                if (rs.next()) {

                    registeruser(originalno.substring(0, 7), originalno.substring(7, 16), type, mobileno, usertype);

                    if (tokens != null) {
                        Iterator i = tokens.iterator();

                        while (i.hasNext()) {
                            try {
                                String token = (String) i.next();
                                String tokenvalue = rs.getString(token);
                                // System.out.println("token="+token+" tokenvalue"+tokenvalue);
                                if (tokenvalue == null)
                                    tokenvalue = "";

                                if ("brand".equals(token))
                                    brandvalue = tokenvalue;

                                respmessage = respmessage.replaceAll("\\$" + token + "\\$", tokenvalue);
                            } catch (Exception e) {
                                logger.debug("Error in message creation, please check column name in brand message settings" + e.getMessage());
                            }
                        }
                        logger.debug("respmessage=" + respmessage);

                        if (password != null && password.trim()
                            .length() != 0 && "ADMIN".equals(usertype)) {
                            boolean passwordmatch = verifyAdminPasswd(password, brandvalue);

                            if (!passwordmatch) {
                                logger.debug("Password not matched.");
                                result[0] = "4";
                                result[1] = "Invalid password, please try again or contact the admin.";
                                return result;
                            }
                        }
                    }
                } else {
                    if (codecorrect) {
                        respmessage = NOPASSWD + passwdloc + ">";
                    } else {
                        respmessage = NOTSUCCESS;
                    }

                    result[0] = "2";
                    result[1] = respmessage;
                    return result;
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.debug(e.getMessage());
            } finally {
                try {
                    if (con != null)
                        con.close();
                } catch (Exception e) {
                }
            }

            result[0] = "3";
            result[1] = respmessage;

            return result;
        }

        String[] unregister(String uid, String storemobile) throws Exception {
            String[] result = new String[2];
            result[0] = "9";
            String respmessage = "Product has been sucessfully unregistered.";
            String type = "A"; // A=predictable, B=nonpredictable
            String usertype = "USER"; // USER, ADMIN

            logger.debug("In unregister()");

            if (uid == null || uid.length() < 17) {
                respmessage = NOTSUCCESS;
                result[0] = "2";
                result[1] = respmessage;
                return result;
            }

            Utility.log_incoming(uid, storemobile, "UNREG_REQ");

            type = uid.substring(uid.length() - 1, uid.length());
            uid = uid.substring(0, uid.length() - 1);

            String originalno = getOriginalNo(uid, type);

            Connection con = null;
            String sql = null;
            String brand = null;

            if (storemobile != null) {
                boolean authorized = false;
                try {
                    con = Utility.getConnection();

                    PreparedStatement p;
                    ResultSet rs;

                    try {
                        sql = "select * from unregister_auth where INSTR(?,storemobile)>0 and status='A' and authtype='U'";

                        p = con.prepareStatement(sql);

                        p.setString(1, storemobile);

                        rs = p.executeQuery();

                        authorized = false;
                        if (rs.next()) {
                            authorized = true;
                            logger.debug(storemobile + " authorized to unregister");
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    if (!authorized) {
                        result[0] = "4";
                        result[1] = NOTAUTHORIZED;
                        // logUnauthAccess();
                        return result;
                    }

                    try {
                        sql = "select * from registered_dtl where orderno=CONVERT(?,UNSIGNED) and seqno=CONVERT(?,UNSIGNED)";

                        p = con.prepareStatement(sql);

                        p.setString(1, originalno.substring(0, 7));
                        p.setString(2, originalno.substring(7, 16));

                        rs = p.executeQuery();

                        authorized = false;
                        if (rs.next()) {
                            authorized = true;
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    if (!authorized) {
                        result[0] = "2";
                        result[1] = NOTSUCCESS;
                        // logUnauthAccess();
                        return result;
                    }

                    try {
                        sql = "insert into unregistered_dtl ( orderno, seqno,  smsmobileno, brand, remark2, prod_code, qtty, user_type,"
                            + "mobile, ac_name, predicttype, remark3, remark4, remark5, remark6, remark7, desp_name, stamptime ) select * from registered_dtl where orderno=CONVERT(?,UNSIGNED) and seqno=CONVERT(?,UNSIGNED)";

                        p = con.prepareStatement(sql);

                        p.setString(1, originalno.substring(0, 7));
                        p.setString(2, originalno.substring(7, 16));

                        p.execute();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    try {
                        sql = "update unregistered_dtl set storemobile=CONVERT(?,UNSIGNED) where orderno=CONVERT(?,UNSIGNED) and seqno=CONVERT(?,UNSIGNED)";

                        p = con.prepareStatement(sql);

                        p.setString(1, storemobile);
                        p.setString(2, originalno.substring(0, 7));
                        p.setString(3, originalno.substring(7, 16));

                        p.execute();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    try {
                        sql = "delete from registered_dtl where orderno=CONVERT(?,UNSIGNED) and seqno=CONVERT(?,UNSIGNED)";

                        p = con.prepareStatement(sql);

                        p.setString(1, originalno.substring(0, 7));
                        p.setString(2, originalno.substring(7, 16));

                        p.execute();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    logger.debug("Unregistered " + originalno);
                } catch (Exception e) {
                    logger.debug(e.getMessage());
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                    throw new Exception(e);
                } finally {
                    try {
                        if (con != null)
                            con.close();
                    } catch (Exception e) {
                    }
                }
            }

            result[0] = "1";
            result[1] = respmessage;

            return result;
        }

        protected List getDollarTokens(String respmessage) {
            List l = new ArrayList();

            while (respmessage.indexOf("$") != -1) {
                int firstidx = respmessage.indexOf("$");

                int nextidx = respmessage.indexOf("$", firstidx + 1);

                String t = respmessage.substring(firstidx + 1, nextidx);

                l.add(t);

                respmessage = respmessage.replaceAll("\\$" + t + "\\$", "");
            }

            return l;
        }

        protected void registeruser(String orderno, String seqno, String type, String mobileno, String usertype) {
            Connection con = null;
            try {
                con = Utility.getConnection();

                String sql = "select * from reg_dtl_datafile_pred where orderno=CONVERT(?,UNSIGNED) and CONVERT(start_seqno,UNSIGNED)<=CONVERT(?,UNSIGNED) and CONVERT(end_seqno,UNSIGNED)>=CONVERT(?,UNSIGNED)";

                if ("B".equalsIgnoreCase(type)) {
                    sql = "select * from reg_dtl_datafile_unpred where orderno=CONVERT(?,UNSIGNED) and CONVERT(start_seqno,UNSIGNED)<=CONVERT(?,UNSIGNED) and CONVERT(end_seqno,UNSIGNED)>=CONVERT(?,UNSIGNED)";
                }

                PreparedStatement p = con.prepareStatement(sql);

                p.setString(1, orderno);
                p.setString(2, seqno);
                p.setString(3, seqno);

                ResultSet rs = p.executeQuery();

                RegistrationDetail r = new RegistrationDetail();

                if (rs.next()) {
                    r.setOrderno(orderno);
                    r.setStartseqno(seqno);
                    r.setBrand(rs.getString("brand"));
                    r.setMobile(rs.getString("mobile"));
                    r.setProdcode(rs.getString("prod_code"));
                    r.setQtty(rs.getString("qtty"));
                    r.setRemark2(rs.getString("remark2"));
                    r.setRemark3(rs.getString("remark3"));
                    r.setRemark4(rs.getString("remark4"));
                    r.setRemark5(rs.getString("remark5"));
                    r.setRemark6(rs.getString("remark6"));
                    r.setRemark7(rs.getString("remark7"));
                    r.setAc_name(rs.getString("ac_name"));
                    r.setDesp_name(rs.getString("desp_name"));
                }

                String entrytable = "registered_dtl";
                if ("ADMIN".equals(usertype))
                    entrytable = "brand_request_log";

                p = con.prepareStatement("insert into " + entrytable + " values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,now())");

                int paramidx = 0;

                p.setString(++paramidx, r.getOrderno());
                p.setString(++paramidx, r.getStartseqno());
                p.setString(++paramidx, mobileno);
                p.setString(++paramidx, r.getBrand());
                p.setString(++paramidx, r.getRemark2());
                p.setString(++paramidx, r.getProdcode());
                p.setString(++paramidx, "1"/*r.getQtty()*/);
                p.setString(++paramidx, usertype);// USER,ADMIN
                p.setString(++paramidx, r.getMobile());
                p.setString(++paramidx, r.getAc_name());
                p.setString(++paramidx, type.toUpperCase());
                p.setString(++paramidx, r.getRemark3());
                p.setString(++paramidx, r.getRemark4());
                p.setString(++paramidx, r.getRemark5());
                p.setString(++paramidx, r.getRemark6());
                p.setString(++paramidx, r.getRemark7());
                p.setString(++paramidx, r.getDesp_name());

                p.executeUpdate();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                System.out.println("Error 8: " + e.getMessage());
                logger.debug(e.getMessage(), e);
            } finally {
                try {
                    if (con != null)
                        con.close();
                } catch (Exception e) {
                }
            }
        }

        protected void logDuplicateProduct(RegistrationDetail r) {
            Connection con = null;
            try {
                con = Utility.getConnection();

                PreparedStatement p = con.prepareStatement("insert into duplicate_reg_log values(?,?,?,?,?,?, now())");

                int paramidx = 0;

                p.setString(++paramidx, r.getOrderno());
                p.setString(++paramidx, r.getStartseqno());
                p.setString(++paramidx, r.getPredicttype());
                p.setString(++paramidx, r.getMobile());
                p.setString(++paramidx, r.getProdcode());
                p.setString(++paramidx, r.getBrand());

                p.executeUpdate();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.debug(e.getMessage());
            } finally {
                try {
                    if (con != null)
                        con.close();
                } catch (Exception e) {
                }
            }
        }

        protected boolean verifyAdminPasswd(String password, String brand) {

            Connection con = Utility.getConnection();
            boolean passwordmatch = false;

            try {
                String sql = "select * from brand_dtl where password=? and brandname=?";

                PreparedStatement p = con.prepareStatement(sql);

                p.setString(1, password);
                p.setString(2, brand);

                ResultSet rs = p.executeQuery();

                if (rs.next()) {
                    System.out.println("Here 4");
                    passwordmatch = true;
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                logger.debug(e.getMessage());
            } finally {
                try {
                    if (con != null)
                        con.close();
                } catch (Exception e) {
                }
            }

            return passwordmatch;
        }
    }
}
