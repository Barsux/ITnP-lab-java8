import java.net.*;

/** Класс для хранения пары URL и глубины */
public class URLDepthPair {
    private static final String MODULE_NAME = "URLDepthPair";
    //Создаём объект класса Logger с именем MODULE_NAME
    private static final Logger l = new Logger(MODULE_NAME);
    private URL urlObject;
    private int depth;
    /** Конструктор класса, проверяет корректность URL */
    public URLDepthPair(String url, int depth) throws MalformedURLException {
        urlObject = new URL(url);
        this.depth = depth;
    }
    public URL getURL(){
        return urlObject;
    }
    public int getDepth(){
        return depth;
    }
    public String toString(){
        return urlObject.toString() + " " + depth;
    }


    /** Переопределяем методы для корректного сравнения внутри LinkedList */
    @Override
    public boolean equals(Object o){
        if(o instanceof URLDepthPair p){
            return urlObject.equals(p.getURL());
        }
        return false;
    }
    @Override
    public int hashCode(){
        return urlObject.hashCode();
    }
}
