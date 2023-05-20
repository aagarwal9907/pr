

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;


import com.vasanth.lab.bean.masterbean.QuestionDetails;

public class QuestionDataHandler implements DataHandler {
	private static Logger logger = Logger.getLogger(QuestionDataHandler.class);

	public boolean updateDatabase(List<?> data, String key) {

		boolean writesuccess = true;
		if (data == null) {
			logger.error("No data was retrieved for insert. For " + key);

			return writesuccess;
		}

		Connection con = null;
		try {
			con = ConnectionMaker.getConnection();
			PreparedStatement preparedStatement = con.prepareStatement("DELETE FROM survey_master");
			preparedStatement.executeUpdate();

			PreparedStatement statement = con.prepareStatement("INSERT INTO survey_master (qn_no,qn_en) values(?,?)");

			for (Iterator iterator = data.iterator(); iterator.hasNext();) {
				QuestionDetails question = (QuestionDetails) iterator.next();
				try {
					if ( question.getQuestionen()!=null && question.getQuestionen()!=null) {
						statement.setString(1, question.getQuestionno());
						statement.setString(2, question.getQuestionen());
						statement.executeUpdate();
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
