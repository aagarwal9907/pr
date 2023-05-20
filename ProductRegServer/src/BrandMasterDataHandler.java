

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.vasanth.lab.bean.masterbean.BrandDetails;

public class BrandMasterDataHandler implements DataHandler {

    private static Logger logger = Logger.getLogger(BrandMasterDataHandler.class);

    public boolean updateDatabase(List<?> data, String key) {

        boolean writesuccess = true;
        if (data == null) {
            logger.error("No data was retrieved for insert. For " + key);

            return writesuccess;
        }

        Connection con = null;
        int count = 0;
        try {
            con = ConnectionMaker.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement("update brand_dtl set buser_name=?,buser_pwd=? where brandname=?");
            for (Iterator iterator1 = data.iterator(); iterator1.hasNext();) {
                BrandDetails brandDetails = (BrandDetails) iterator1.next();
                try {
                    preparedStatement.setString(1, brandDetails.getName());
                    preparedStatement.setString(7, brandDetails.getBusername());
                    preparedStatement.setString(8, brandDetails.getBuserpwd());
                    count = preparedStatement.executeUpdate();
                    if (count == 0) {
                        PreparedStatement statement = con.prepareStatement("INSERT INTO brand_dtl (brandname,success_msg,failure_msg,password,email,day,buser_name,buser_pwd) values(?,?,?,?,?,?,?,?)");

                        for (Iterator iterator = data.iterator(); iterator.hasNext();) {
                            BrandDetails brandDtl = (BrandDetails) iterator.next();
                            try {
                                statement.setString(1, brandDtl.getName());
                                statement.setString(2, brandDtl.getSuccessMessage());
                                statement.setString(3, brandDtl.getFailMessage());
                                statement.setString(4, brandDtl.getPassword());
                                statement.setString(5, brandDtl.getEmail());
                                if (brandDtl.getDay() != null && brandDtl.getDay()
                                    .matches("[0-9]+")) {
                                    statement.setInt(6, Integer.valueOf(brandDtl.getDay()));

                                } else {
                                    statement.setInt(6, 0);
                                }
                                statement.setString(7, brandDtl.getBusername());
                                statement.setString(8, brandDtl.getBuserpwd());
                                statement.executeUpdate();

                            } catch (Exception ex) {
                                logger.debug(String.format("Error Inserting DB BrandMaster Exception: %s", ex.getMessage()), ex);
                                writesuccess = false;
                            }
                        }

                    }
                } catch (Exception ex) {
                    logger.debug(String.format("Error Inserting DB BrandMaster Exception: %s", ex.getMessage()), ex);
                    writesuccess = false;
                }
            }
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
            writesuccess = false;
        } finally {
            try {
                ConnectionMaker.closeConnection(con);
            } catch (SQLException e) {
                logger.debug(e.getMessage(), e);
                writesuccess = false;
            }
        }

        return writesuccess;
    }

}
