

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class CheckReportToGenerate {
    private static Logger logger = Logger.getLogger(CheckReportToGenerate.class);

    public boolean isToGenerateReport(ReportValidateContext context) {
        boolean count = false;
        Connection con = null;
        if (context != null && context.getParamsCount() >= 2) {
            try {
                con = ConnectionMaker.getConnection();
                PreparedStatement statement = con.prepareStatement(context.getQuery());
                for (int index = 0, param = 1; index < context.getParamsCount(); index++,param++) {
                    statement.setString(param, context.getParams()[index]);
                }
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    count = (resultSet.getInt(1) >= 1) ? true : false;
                }

            } catch (Exception sql) {
                logger.debug(String.format("Error Inserting DB BrandMaster Exception: %s", sql.getMessage()), sql);
            } finally {
                try {
                    ConnectionMaker.closeConnection(con);
                } catch (SQLException e) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        return count;
    }

}
