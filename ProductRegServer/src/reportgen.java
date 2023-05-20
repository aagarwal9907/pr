

public class reportgen {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length!=2){
			System.out.println("Generate Reports Usage: reportgen <yyyy-mm-dd> <yyyy-mm-dd>\n where the first date is for generate FROM and second date is for generate TO");
			System.exit(0);
		}
			
		ReportScheduler r=new ReportScheduler(args[0],args[1]);
		r.generatereports();
		System.exit(0);
	}

}
