
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.xml.sax.SAXException;

import com.vasanth.lab.bean.masterbean.Brands;
import com.vasanth.lab.bean.masterbean.Questions;
import com.vasanth.lab.bean.masterbean.UnRegMob;

import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.ReaderConfig;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;

public class DataScheduler extends Timer {
    static Logger logger = Logger.getLogger(DataScheduler.class);

    public DataScheduler() {
        try {

            logger.debug("In Data Scheduler");
            String startafter = Utility.getMasterValueByName("DATA_SCHEDSTARTAFTER");
            String repeatevery = Utility.getMasterValueByName("DATA_SCHEDREPEATEVERY");

            if (startafter == null || startafter.equals("") || repeatevery == null || repeatevery.equals("")) {
                logger.debug("Configuration not completed from Settings screen.");
                System.out.println("Configuration not completed from Settings screen. Please refer operations manual for details.");
                System.exit(0);
            }

            this.scheduleAtFixedRate(new SchedulerThread(), (int) (Float.parseFloat(startafter) * 60 * 1000), (int) (Float.parseFloat(repeatevery) * 60 * 1000));
            logger.debug("Done....");
        } catch (Exception e) {

            logger.debug(e.getMessage());
        }
    }

    protected class SchedulerThread extends TimerTask {

        @Override
        public void run() {
            logger.debug("Calling updateDatabase");
            updateDataList("DATAFILE_PRED");
            updateDataList("DATAFILE_UNPRED");
            updateDataList("BAD_LABELS");
            updateDataList("UNREG_AUTH");
            updateDataList("BRANDMASTER");
            updateDataList("QUESTION");
            updateDataList("UNREGMOB");
            logger.debug("After updateDatabase");
        }

    }

    protected void updateDataList(String key) {
        logger.debug("Updating Database..." + key);

        String datapath = Utility.getMasterValueByName(key);

        if (datapath == null) {
            logger.debug("Name of datafile couldnot be retrieved. Please check settings.");
            return;
        }

        File destFiles = null;
        try {
            if (!new File(datapath).isDirectory()) {
                logger.debug("Source is not a folder.");
                return;
            }
            destFiles = new File(datapath);
        } catch (Exception e1) {
            logger.debug("Destination Folder: " + e1.getMessage());
            return;
        }
        String destpath = datapath + "/processed";
        List data = null;
        File[] childFiles = destFiles.listFiles();
        for (File datafile : childFiles) {

            String filename = datafile.getName();       
            logger.debug("filepath=" + datafile.getAbsolutePath());            
            InputStream xlfile = null;
            if (datafile.isDirectory())
                continue;
            try {
                xlfile = new FileInputStream(datafile);
            } catch (FileNotFoundException e) {
               logger.debug(e.getMessage(), e);
            }

            if (xlfile == null) {
                logger.debug("Datafile not found. Please check settings and if the datafile exists in the location for key=" + key);
                continue;
            }

            boolean readsuccess = false;
            try {
                data = processXL(xlfile, filename, key);
                readsuccess = true;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                logger.debug(e.getMessage(), e);
            }

            if (readsuccess) {
                boolean writesuccess = false;
                try {
                    DataHandler handler = DataHandlerFactory.createHandler(key);
                    writesuccess = handler.updateDatabase(data, key);
                } catch (InvalidOperationException ex) {
                    writesuccess = updateDatabase(data, key);
                }
                if (writesuccess) {
                    Utility.copyFile(datafile, new File(destpath + "/" + filename));
                }
            }
        }

    }

    protected boolean updateDatabase(List data, String key) {

        boolean writesuccess = true;
        if (data == null) {
            System.out.println("No data was retrieved for update. For " + key);
            logger.debug("No data was retrieved for update. For " + key);

            return writesuccess;
        }

        Iterator iterator = data.iterator();
        Connection con = null;

        try {
            con = ConnectionMaker.getConnection();
            PreparedStatement s = con.prepareStatement("update reg_dtl_" + key.toLowerCase()
                + " set brand=?, remark2=?, remark3=?, remark4=?, remark5=?, remark6=?, remark7=?, prod_code=?, qtty=?, mobile=?, ac_name=?, desp_name=?, image_name=?, passwd=?, passwdsec=?, passwdloc=?, feedback=?, brandwebsite=? where CONVERT(orderno,UNSIGNED)=CONVERT(?,UNSIGNED) and CONVERT(start_seqno,UNSIGNED)=CONVERT(?,UNSIGNED) and CONVERT(end_seqno,UNSIGNED)=CONVERT(?,UNSIGNED)");
            PreparedStatement ins = con.prepareStatement("insert into reg_dtl_" + key.toLowerCase() + " values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");

            if ("bad_labels".equalsIgnoreCase(key)) {
                updateRegDtlBadLableDatabase(key,iterator,con,s,ins,writesuccess);
            } else if ("UNREG_AUTH".equalsIgnoreCase(key)) {
                updateUnRegAuthDatabase(iterator,con,s,ins,writesuccess);
            } else {

                int paramidx = 0;

                while (iterator.hasNext()) {
                    paramidx = 0;
                    RegistrationDetail e = (RegistrationDetail) iterator.next();
                    int count = 0;

                    try {
                        if (e != null && e.getOrderno() != null) {
                            s.setString(++paramidx, e.getBrand());
                            s.setString(++paramidx, e.getRemark2());
                            s.setString(++paramidx, e.getRemark3());
                            s.setString(++paramidx, e.getRemark4());
                            s.setString(++paramidx, e.getRemark5());
                            s.setString(++paramidx, e.getRemark6());
                            s.setString(++paramidx, e.getRemark7());
                            s.setString(++paramidx, e.getProdcode());
                            s.setString(++paramidx, e.getQtty());
                            s.setString(++paramidx, e.getMobile());
                            s.setString(++paramidx, e.getAc_name());
                            s.setString(++paramidx, e.getDesp_name());
                            s.setString(++paramidx, e.getPredicttype());// image name field
                            s.setString(++paramidx, e.getPasswd());
                            s.setString(++paramidx, e.getPasswdsec());
                            s.setString(++paramidx, e.getPasswdloc());
                            s.setString(++paramidx, e.getSurveyqns());
                            s.setString(++paramidx, e.getBrandwebsite());
                            s.setString(++paramidx, e.getOrderno());
                            s.setString(++paramidx, e.getStartseqno());
                            s.setString(++paramidx, e.getEndseqno());

                            count = s.executeUpdate();

                            if (count == 0) {
                                paramidx = 0;
                                ins.setString(++paramidx, e.getOrderno());
                                ins.setString(++paramidx, e.getStartseqno());
                                ins.setString(++paramidx, e.getEndseqno());
                                ins.setString(++paramidx, e.getBrand());
                                ins.setString(++paramidx, e.getRemark2());
                                ins.setString(++paramidx, e.getProdcode());
                                ins.setString(++paramidx, e.getQtty());
                                ins.setString(++paramidx, e.getMobile());
                                ins.setString(++paramidx, e.getAc_name());
                                ins.setString(++paramidx, e.getRemark3());
                                ins.setString(++paramidx, e.getRemark4());
                                ins.setString(++paramidx, e.getRemark5());
                                ins.setString(++paramidx, e.getRemark6());
                                ins.setString(++paramidx, e.getRemark7());
                                ins.setString(++paramidx, e.getDesp_name());
                                ins.setString(++paramidx, e.getPredicttype());// image name field
                                ins.setString(++paramidx, e.getPasswd());
                                ins.setString(++paramidx, e.getPasswdsec());
                                ins.setString(++paramidx, e.getPasswdloc());
                                ins.setString(++paramidx, e.getSurveyqns());
                                ins.setString(++paramidx, e.getBrandwebsite());

                                ins.executeUpdate();
                            }
                        }
                    } catch (Exception e1) {
                        logger.info("Error updating database orderno=" + e.getOrderno() + " and seqno=" + e.getStartseqno() + " Exception:" + e1.getMessage());

                        logger.info("Error: " + e1.getMessage());
                        logger.debug(e1.getMessage(), e1);
                        writesuccess = false;
                    }
                }
            }
           
        } catch (Exception e) {

            logger.info("Error: " + e.getMessage());
            logger.debug(e.getMessage(), e);
            writesuccess = false;
        } finally {
            try {
                ConnectionMaker.closeConnection(con);
            } catch (SQLException e) {

                logger.info("Error: " + e.getMessage());
                logger.debug(e.getMessage(), e);
                writesuccess = false;
            }
        }

        return writesuccess;
    }
    private void updateRegDtlBadLableDatabase(String key,Iterator iterator, Connection con, PreparedStatement s, PreparedStatement ins, boolean writesuccess) throws SQLException{
        s = con.prepareStatement("update reg_dtl_" + key.toLowerCase() + " set seqno=? where seqno=?");
        ins = con.prepareStatement("insert into reg_dtl_" + key.toLowerCase() + " values ( ? ) ");

        int paramidx = 0;

        while (iterator.hasNext()) {
            paramidx = 0;
            RegistrationDetail e = (RegistrationDetail) iterator.next();
            int count = 0;
            if (e != null && e.getOrderno() != null) {
                s.setString(++paramidx, e.getOrderno());
                s.setString(++paramidx, e.getOrderno());

                try {
                    count = s.executeUpdate();

                    if (count == 0) {
                        paramidx = 0;
                        ins.setString(++paramidx, e.getOrderno());

                        ins.executeUpdate();
                    }
                } catch (Exception e1) {

                    System.out.println("Error Updating DB bad_labels seqno=" + e.getOrderno() + " Exception:" + e1.getMessage());
                    logger.debug(e1.getMessage(), e1);
                    writesuccess = false;
                }
            }
        }
    }
    private void updateUnRegAuthDatabase(Iterator iterator, Connection con, PreparedStatement s, PreparedStatement ins, boolean writesuccess) throws SQLException {
        s = con.prepareStatement("update unregister_auth set storeimei=? where storemobile=?");
        ins = con.prepareStatement("insert into unregister_auth (ac_code, ac_name, storemobile, storeimei, authtype, status) values ( ?, ?, ?, ?, ?, 'A' ) ");

        int paramidx = 0;

        while (iterator.hasNext()) {
            paramidx = 0;
            GenericBean e = (GenericBean) iterator.next();
            int count = 0;
            if (e != null && e.getV3() != null) {
                s.setString(++paramidx, e.getV4());
                s.setString(++paramidx, e.getV3());

                try {
                    count = s.executeUpdate();

                    if (count == 0) {
                        paramidx = 0;
                        ins.setString(++paramidx, e.getV1());
                        ins.setString(++paramidx, e.getV2());
                        ins.setString(++paramidx, e.getV3());
                        ins.setString(++paramidx, e.getV4());
                        ins.setString(++paramidx, e.getV5());

                        ins.executeUpdate();
                    }
                } catch (Exception e1) {

                    System.out.println("Error Updating DB unregister_auth Exception:" + e1.getMessage());
                    logger.debug(e1.getMessage(), e1);
                    writesuccess = false;
                }
            }
        }
    }
    
    protected List processXL(InputStream xlfile, String filename, String key) throws Exception {

        logger.debug("Uploading Applications");

        XLSReader mainReader = null;

        Map<String, Object> beans = null;
        System.out.println("xlfile=" + xlfile);
        logger.debug("xlfile=" + xlfile);
        try {
            String inputXML = ReadXMLFile.updateDOMTree(filename.substring(0, filename.indexOf(".")), key);

            InputStream is = new ByteArrayInputStream(inputXML.getBytes());

            mainReader = ReaderBuilder.buildFromXML(is);

            beans = new HashMap<String, Object>();
            ReaderConfig.getInstance()
                .setSkipErrors(true);
            XLSReadStatus readStatus = null;

            if (key != null && key.equalsIgnoreCase("unreg_auth")) {
                GenericBeanList regs = new GenericBeanList();
                beans.put("genericbeanlist", regs);
                mainReader.read(xlfile, beans);
                logger.debug("size=" + regs.getAppls()
                    .size());
                List i = regs.getAppls();

                return i;
            } else if (key != null && key.equalsIgnoreCase("BRANDMASTER")) {
                Brands brandObj = new Brands();
                beans.put("brandObj", brandObj);
                mainReader.read(xlfile, beans);
                logger.debug("size=" + brandObj.getBrand()
                    .size());
                List i = brandObj.getBrand();

                return i;
            } else if (key != null && key.equalsIgnoreCase("QUESTION")) {
                Questions questiondetailsobj = new Questions();
                beans.put("questiondetailsobj", questiondetailsobj);
                mainReader.read(xlfile, beans);
                logger.debug("size=" + questiondetailsobj.getQdetailsLs()
                    .size());
                List i = questiondetailsobj.getQdetailsLs();

                return i;
            } else if (key != null && key.equalsIgnoreCase("UNREGMOB")) {
                UnRegMob unRegMobobj = new UnRegMob();
                beans.put("unRegMobobj", unRegMobobj);
                mainReader.read(xlfile, beans);
                logger.debug("size=" + unRegMobobj.getUnRegModDetails()
                    .size());
                List i = unRegMobobj.getUnRegModDetails();
                return i;
            } else {
                Registrations regs = new Registrations();
                beans.put("registrations", regs);
                mainReader.read(xlfile, beans);
                logger.debug("size=" + regs.getAppls()
                    .size());
                List i = regs.getAppls();

                return i;
            }

        } catch (InvalidFormatException e1) {

            System.out.println("Error: " + e1.getMessage());
            logger.debug(e1.getMessage(), e1);
            throw e1;
        } catch (IOException e1) {

            System.out.println("Error: " + e1.getMessage());
            logger.debug(e1.getMessage(), e1);
            throw e1;
        } catch (SAXException e1) {

            System.out.println("Error: " + e1.getMessage());
            logger.debug(e1.getMessage(), e1);
            throw e1;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            logger.debug(e.getMessage(), e);
            throw e;
        }

    }
}
