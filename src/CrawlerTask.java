import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**Ридер сайтов считывает http ссылки в html документе*/
public class CrawlerTask implements Runnable {
    private final URLPool pool;
    private static final String MODULE_NAME = "CrawlerTask";
    private static final Pattern urlPattern = Pattern.compile(
            "http://{1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+",
            Pattern.CASE_INSENSITIVE);
    private  Logger l;
    private static final int TIMEOUT = 1000;
    private int maxDepth = 0;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public CrawlerTask(URLPool pool, int maxDepth){
        this.pool = pool;
        this.maxDepth = maxDepth;
    }

    /** Функция для создания сокета/подключения к сайту */
    private boolean connect(URLDepthPair pair){
        try{
            socket = new Socket(pair.getHost(), 80);
        } catch (UnknownHostException e) {
            l.log("Неизвестный хост: " + pair.getHost());
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /** Функция для установке таймаута/времени после которого сокет перестанет пытаться считывать/получать информацию */
    private boolean setTimeout(){
        try{
            socket.setSoTimeout(TIMEOUT);
        } catch (SocketException e) {
            l.log("Ошибка ввода-вывода при установке таймаута: " + e.getMessage());
            return false;
        }
        return true;
    }

    /** Функция для получения имени текущего потока */
    private String getName(){
        return Thread.currentThread().getName();
    }

    /** Функция для получения потоков ввода ввывода */
    private boolean openStreams(){
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            l.log("Ошибка ввода-вывода при открытии потоков: " + e.getMessage());
            return false;
        }
        return true;
    }

    /** Функция для отправки запроса на сервер и чтения html документа */
    private LinkedList<String> readHTML(URLDepthPair pair){
        LinkedList<String> lines = new LinkedList<>();
        out.println("GET " + pair.getPath() + " HTTP/1.1");
        out.println("Host: " + pair.getHost());
        out.println("Connection: close");
        out.println();
        String line;
        try {
            while((line = in.readLine()) != null){
                lines.add(line);
            }
        } catch (IOException e) {
            l.log("Ошибка ввода-вывода: " + e.getMessage());
            return null;
        }
        return lines;
    }

    /** Функция для закрытия сокета */
    private boolean closeConnection(){
        try{
            socket.close();
        } catch (IOException e) {
            l.log("Ошибка ввода-вывода при закрытии сокета: " + e.getMessage());
            return false;
        }
        return true;
    }

    /** Функция для получения ссылок из html документа */
    public LinkedList<URLDepthPair> read(URLDepthPair pair){
        LinkedList<URLDepthPair> list = new LinkedList<>();
        if (!connect(pair)) {
            return null;
        }
        if (!setTimeout()) {
            return null;
        }
        if (!openStreams()) {
            return null;
        }
        LinkedList<String> lines = readHTML(pair);
        if (lines == null) {
            return null;
        }
        if (!closeConnection()) {
            return null;
        }
        String link;
        URLDepthPair newPair;
        for (String hLine : lines) {
            Matcher matcher = urlPattern.matcher(hLine);
            while (matcher.find()) {
                newPair = new URLDepthPair(matcher.group(), pair.getDepth() + 1);
                if(!newPair.isValidURL())continue;
                list.add(newPair);
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            l.log("Ошибка ввода-вывода: " + e.getMessage());
            return null;
        }
        return list;
    }

    /** Основная функция, переопределенная из интерфейса Runnable */
    @Override
    public void run() {
        l = new Logger(MODULE_NAME, this.getName());
        while (true) {
            l.log("Жду пары");
            URLDepthPair pair = pool.pop();
            l.log("Получил пару");
            if (pair.getDepth() > maxDepth) continue;
            LinkedList<URLDepthPair> list = read(pair);
            if (list == null) continue;
            for(URLDepthPair elem: list)pool.push(elem);
            pool.migrate(pair);
            l.log("Обработал пару.");
        }
    }
}
