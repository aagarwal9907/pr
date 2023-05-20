

public class ReportValidateContext {
    
    String query;
    String params[];
    int paramsCount;
    
    public ReportValidateContext(){}
    
    public ReportValidateContext(String query, String ... params) {
        this.query = query;
        this.params = params;
        this.paramsCount=this.params.length;
    }

    public int getParamsCount() {
        return paramsCount;
    }
    public String getQuery() {
        return query;
    }
    public void setQuery(String query) {
        this.query = query;
    }
    public String[] getParams() {
        return params;
    }
    public void setParams(String... params) {
        this.params = params;
        this.paramsCount=this.params.length;
    }

}
