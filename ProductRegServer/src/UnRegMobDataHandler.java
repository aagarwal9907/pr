

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.vasanth.lab.bean.masterbean.UnRegModDetails;

public class UnRegMobDataHandler implements DataHandler {

    private static Logger logger = Logger.getLogger(UnRegMobDataHandler.class);

    @Override
    public boolean updateDatabase(List<?> data, String key) {
        boolean writesuccess = true;
        if (data == null) {
            logger.error("No data was retrieved for insert. For " + key);

            return writesuccess;
        }

        Connection con = null;
        try {
            con = ConnectionMaker.getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO unregister_auth (storemobile,storeimei,authtype,status,ac_code,ac_name,rreg_yn,reg_sr) values(?,?,?,?,?,?,?,?)");

            for (Iterator iterator = data.iterator(); iterator.hasNext();) {
                UnRegModDetails unregMod = (UnRegModDetails) iterator.next();
                try {
                    if (unregMod != null && unregMod.getBinspMob() != null) {
                        statement.setString(1, unregMod.getBinspMob());
                        statement.setString(2, unregMod.getImeiMob());
                        statement.setString(3, unregMod.getUrbandMark());
                        statement.setString(4, null);
                        statement.setString(5, unregMod.getAcCode());
                        statement.setString(6, unregMod.getAcName());
                        statement.setString(7, unregMod.getRregYN());
                        statement.setString(8, unregMod.getRegSr());
                        statement.executeUpdate();
                    }

                } catch (Exception ex) {
                    logger.debug(String.format("Error Inserting DB UnRegisterMobile Exception: %s", ex.getMessage()), ex);
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
