

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.jxls.report.ReportManager;
import net.sf.jxls.report.ReportManagerImpl;
import net.sf.jxls.transformer.XLSTransformer;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ReportScheduler extends Timer {
    static Logger logger = Logger.getLogger(ReportScheduler.class);
    private String fromdate = null;
    private String todate = null;

    public ReportScheduler() {
        try {
            logger.debug("In Report Scheduler");
            String startafter = Utility.getMasterValueByName("RPT_SCHEDSTARTAFTER");
            String repeatevery = Utility.getMasterValueByName("RPT_SCHEDREPEATEVERY");

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

    public ReportScheduler(String fromdate, String todate) {
        this.fromdate = fromdate;
        this.todate = todate;
    }

    protected class SchedulerThread extends TimerTask {

        @Override
        public void run() {
            logger.debug("Generating Reports...");
            generatereports();
        }

    }

    protected void generatereports() {
        Connection con = null;
        boolean update_mstdate = false;
        CheckReportToGenerate check = new CheckReportToGenerate();

        try {
            Calendar cal = Calendar.getInstance();
            String date = (cal.get(Calendar.MONTH) + 1) + "_" + cal.get(Calendar.DAY_OF_MONTH) + "_" + cal.get(Calendar.YEAR) + "_" + cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE);
            String path = "";
            // path=path.substring(1, path.indexOf("classes")+7);
            // path=path.replace("%20", " ");

            if (fromdate == null) {
                fromdate = Utility.getMasterValueByName("RPT_CREATED_UPTO");
                update_mstdate = true;
            }

            if (todate == null)
                todate = "2200-01-01";

            con = ConnectionMaker.getConnection();

            PreparedStatement ps = con.prepareStatement("select distinct brand from registered_dtl order by brand");

            ResultSet rs = ps.executeQuery();

            Map beans = new HashMap();

            while (rs.next()) {

                ReportManager rm = new ReportManagerImpl(con, beans);
                beans.put("rm", rm);
                String brand = rs.getString(1);
                beans.put("brandparam", brand);
                beans.put("formatstr", "%d-%m-%Y %I:%i:%S %p");
                beans.put("fromdatestr", fromdate);
                beans.put("todatestr", todate);
                ReportValidateContext context = ValidationContextFactory.getContextForRegisteredDtl(fromdate, todate, brand);
                if (check.isToGenerateReport(context)) {
                    FileInputStream inputStream = new FileInputStream(getClass().getResource("brandwise_registered_products.xls")
                        .getPath()
                        .replaceAll("%20", " "));
                    InputStream is = new BufferedInputStream(inputStream);
                    XLSTransformer transformer = new XLSTransformer();
                    HSSFWorkbook resultWorkbook = (HSSFWorkbook) transformer.transformXLS(is, beans);
                    FileOutputStream fos = new FileOutputStream(new File("reports/reg_brand_" + brand + "_" + date + ".xls"));
                    resultWorkbook.write(fos);
                    fos.close();
                }
            }

            beans.clear();
            ReportManager rm = new ReportManagerImpl(con, beans);
            beans.put("rm", rm);
            beans.put("formatstr", "%d-%m-%Y %I:%i:%S %p");
            beans.put("fromdatestr", fromdate);
            beans.put("todatestr", todate);
            ReportValidateContext context = ValidationContextFactory.getContextForBrandRequestLog(fromdate, todate);
            if (check.isToGenerateReport(context)) {
                InputStream is = new BufferedInputStream(new FileInputStream(getClass().getResource("brandowner_request_log.xls")
                    .getPath()
                    .replaceAll("%20", " ")));
                XLSTransformer transformer = new XLSTransformer();
                HSSFWorkbook resultWorkbook = (HSSFWorkbook) transformer.transformXLS(is, beans);
                FileOutputStream fos = new FileOutputStream(new File("reports/brandowner_request" + "_" + date + ".xls"));
                resultWorkbook.write(fos);
                fos.close();
            }
            beans = new HashMap();
            rm = new ReportManagerImpl(con, beans);
            beans.put("rm", rm);
            beans.put("formatstr", "%d-%m-%Y %I:%i:%S %p");
            beans.put("fromdatestr", fromdate);
            beans.put("todatestr", todate);
            context = ValidationContextFactory.getContextForDuplicateRequestLog(fromdate, todate);
            if (check.isToGenerateReport(context)) {
                InputStream is = new BufferedInputStream(new FileInputStream(getClass().getResource("duplicate_request_log.xls")
                    .getPath()
                    .replaceAll("%20", " ")));
                XLSTransformer transformer = new XLSTransformer();
                HSSFWorkbook resultWorkbook = (HSSFWorkbook) transformer.transformXLS(is, beans);
                FileOutputStream fos = new FileOutputStream(new File("reports/duplicate_request" + "_" + date + ".xls"));
                resultWorkbook.write(fos);

                fos.close();
            }
            if (update_mstdate) {
                Utility.updateMasterValue("RPT_CREATED_UPTO", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance()
                    .getTime()));
            }

        } catch (ConnectException e) {
            logger.debug(e.getMessage());
            System.out.println("Please ensure if the MySQL Server is running and try again.");
            System.exit(0);
        } catch (Exception e) {

            System.out.println("Error: " + e.getMessage());
            logger.debug(e.getMessage(), e);
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (SQLException e) {

                System.out.println("Error: " + e.getMessage());
                logger.debug(e.getMessage(), e);
            }
        }
        System.out.println("Done.");
    }

}
