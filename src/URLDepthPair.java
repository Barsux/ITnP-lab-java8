import java.net.*;

/** Класс для хранения пары URL и глубины */
public class URLDepthPair {
    private static final String MODULE_NAME = "URLDepthPair";
    //Создаём объект класса Logger с именем MODULE_NAME
    private static final Logger l = new Logger(MODULE_NAME);
    private final String url;
    public URL urlObject;
    private int depth;
    /** Конструктор класса, проверяет корректность URL */
    public URLDepthPair(String url, int depth){
        this.url = url;
        try{
            urlObject = new URL(url);
        } catch (MalformedURLException e) {
            l.log("Неправильный URL" + url);
            return;
        }
        this.depth = depth;
    }
    public String getURL(){
        return url;
    }
    public int getDepth(){
        return depth;
    }
    public String toString(){
        return url + " " + depth;
    }

    public String getHost(){
        return urlObject.getHost();
    }
    public String getPath(){
        return urlObject.getPath();
    }

    /** Функция, проверяющая валидность ссылки в объекте */
    boolean isValidURL(){
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /** Переопределяем методы для корректного сравнения внутри LinkedList */
    @Override
    public boolean equals(Object o){
        if(o instanceof URLDepthPair p){
            return url.equals(p.url);
        }
        return false;
    }
    @Override
    public int hashCode(){
        return url.hashCode();
    }
}
