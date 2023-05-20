
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DataHandlerFactory {
	private static DataHandler obj;
	private static Map<String, DataHandler> handler=new ConcurrentHashMap<String, DataHandler>();
	static{
		handler.put("BRANDMASTER", new BrandMasterDataHandler());
		handler.put("QUESTION",new QuestionDataHandler());
		handler.put("UNREGMOB", new UnRegMobDataHandler());
	}
	
	public static DataHandler createHandler(String key) throws InvalidOperationException{
		if(key==null){
			throw new InvalidOperationException("Invalid key");
		}
		obj=handler.get(key);
		return obj;
	}
	
}
