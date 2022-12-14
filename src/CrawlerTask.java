import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**Ридер сайтов считывает http ссылки в html документе*/
public class CrawlerTask implements Runnable {
    private final URLPool pool;
    private static final String MODULE_NAME = "CrawlerTask";
    private static final String URL_PREFIX = "http://";
    private static final String URL_HREF = "<a href=";
    private  Logger l;
    private static final int TIMEOUT = 10000;
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
            socket = new Socket(pair.getURL().getHost(), 80);
        } catch (UnknownHostException e) {
            l.log("Неизвестный хост: " + pair.getURL().getHost());
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
        System.out.println("GET " + pair.getURL().getPath() + " HTTP/1.1");
        System.out.println("Host: " + pair.getURL().getHost());
        System.out.println("Connection: close");
        System.out.println();
        out.println("GET " + pair.getURL().getPath() + " HTTP/1.1");
        out.println("Host: " + pair.getURL().getHost());
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
            System.out.println(hLine);
            int startIdx = hLine.indexOf(URL_PREFIX);
            int endIdx = hLine.indexOf("\"", startIdx + 1);
            if(startIdx == -1 || endIdx == -1 || !hLine.contains(URL_HREF))continue;
            link = hLine.substring(startIdx, endIdx);
            try {
                newPair = new URLDepthPair(link, pair.getDepth() + 1);
            } catch (MalformedURLException e){
                continue;
            }
            list.add(newPair);
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
