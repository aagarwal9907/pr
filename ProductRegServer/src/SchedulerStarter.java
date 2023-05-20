


import java.util.Timer;

import org.apache.log4j.Logger;

/**
 * Application Lifecycle Listener implementation class SchedulerStarter
 *
 */
public class SchedulerStarter {
	static Logger logger=Logger.getLogger(SchedulerStarter.class);
	
	Timer schedulerTimer=null;

    /**
     * Default constructor. 
     */
    public SchedulerStarter() {
    	logger.debug("In SchedulerStarter");
    	new DataScheduler();
    	new ReportScheduler();
    }

    @Override
    protected void finalize() throws Throwable {
     
    	super.finalize();
        if(schedulerTimer!=null)
        	schedulerTimer.cancel();
    }
 
}
