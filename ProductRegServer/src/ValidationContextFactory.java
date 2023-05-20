

public final class ValidationContextFactory {

    private ValidationContextFactory(){}
    
    public static ReportValidateContext getContextForDuplicateRequestLog(String start, String end){
        ReportValidateContext context=new ReportValidateContext();
        context.setQuery("SELECT count(*) FROM duplicate_reg_log r where date_format(stamptime,  '%Y-%m-%d' ) >= ? and date_format(stamptime,  '%Y-%m-%d' ) <= ? ");
        context.setParams(start,end);
        return context;
    }
    
    
    public static  ReportValidateContext getContextForRegisteredDtl(String start, String end,String brand){
        ReportValidateContext context=new ReportValidateContext();
        context.setQuery("SELECT count(*) FROM registered_dtl r where brand=? and date_format(stamptime,  '%Y-%m-%d' ) >= ? and date_format(stamptime,  '%Y-%m-%d' ) <= ? order by orderno, seqno");
        context.setParams(brand,start,end);
        return context;
    }
    
    public static ReportValidateContext getContextForBrandRequestLog(String start, String end){
        ReportValidateContext context=new ReportValidateContext();
        context.setQuery("SELECT  count(*) FROM brand_request_log r where date_format(stamptime,  '%Y-%m-%d' ) >= ? and date_format(stamptime,  '%Y-%m-%d' ) <= ? order by orderno, seqno");
        context.setParams(start,end);
        return context;
    }
    
}
