import java.text.SimpleDateFormat;
import java.util.Date;

/** Класс для логирования*/
public class Logger {
    //Опциональная переменная, сейчас смысла в ней нет
    private static final boolean DEBUG = true;
    private static final int PADDING = 10;
    private String threadName = null;
    private String pad;
    private final String moduleName;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("[yyyy-MM-dd][HH:mm:ss:SSSS]");
    /** Конструктор класса, в каждом модуле вызывается со своим названием*/
    public Logger(String moduleName){
        this.moduleName = moduleName;
        pad = "";
        for(int i = 0; i < PADDING - moduleName.length(); i++){
            pad += " ";
        }
    }

    /** Перегрузка конструктора для логирования в потоках*/
    public Logger(String moduleName, String name){
        this(moduleName);
        threadName = name;
    }

    /** Выводит сообщение в консоль с timestamp вида [yyyy-mm-dd][hh:mm:ss:4ms][modulename][threadname?]: message */
    public void log(Object object){
        String message;
        if(object instanceof String) message = (String)object;
        else message = object.toString();
        if(DEBUG && message.length() != 0){
            String time = dateFormat.format(new Date());
            if(threadName == null)System.out.println(time + "[" + moduleName + "]" + pad + ": " + message);
            else System.out.println(time + "[" + moduleName + "][" + threadName + "]" + pad + ": " + message);
        }
    }
}
