package weakling.segunda.mano.utils;

public class RbQuery {
    private String text = null;

    public RbQuery setQuery(String query){
        text = query;
        return this;
    }

    public String getQuery(){
        return text;
    }
}
