import java.net.*;
import java.util.LinkedList;

/*
    Примеры сайтов
    http://courses.cms.caltech.edu/cs11/ -> 4
    http://info.cern.ch/ -> 2
 */



/** Класс для чтения сайта */
public class Crawler {
    private static final String MODULE_NAME = "Crawler";
    private static final String ERROR = "Usage: java Crawler <URL> <depth> <num_threads>";
    private static final Logger l = new Logger(MODULE_NAME);
    private static final int THREAD_SLEEP_TIME = 500;

    private static URLPool pool;

    /** Функция для проверки валидности URL в строке*/
    public static String parseURL(String url){
        try {
            new URL(url);
            return url;
        } catch (MalformedURLException e) {
            System.out.println("Wrong url structure!");
            return null;
        }
    }
    /**Функция для проверки корректности Int в строке*/
    public static int parseInt(String digit){
        try {
            return Integer.parseInt(digit);
        } catch (NumberFormatException e) {
            System.out.println("Invalid depth: " + digit);
            return -1;
        }
    }
    /**Точка входа в программу*/
    public static void main(String[] args){
        if(args.length != 3){
           System.out.println(ERROR);
            System.exit(1);
        }
        String url = parseURL(args[0]);
        int maxDepth = parseInt(args[1]);
        int numThreads = parseInt(args[2]);
        if(url == null || maxDepth < 0 || numThreads < 0){
            System.out.println(ERROR);
            System.exit(1);
        }
        l.log("Консольные аргументы верны.");
        //Создаём пул и добавляем первую ссылку в список необработанынх ссылок
        pool = new URLPool();
        pool.push(new URLDepthPair(url, 0));

        //Создаём массив потоков и запускаем их
        Thread[] threads = new Thread[numThreads];
        for(int i = 0; i < numThreads; i++){
            CrawlerTask task = new CrawlerTask(pool, maxDepth);
            threads[i] = new Thread(task);
            threads[i].start();
            l.log("Запущен поток " + threads[i].getName());
        }
        //Если все потоки ждут ссылку, то ссылки закончились и можно останавливать потоки
        while(pool.getWaiters() != numThreads){
            try {
                //Если не все потоки ждут ссылки то следущая проверка произойдёт через THREAD_SLEEP_TIME мс
                Thread.sleep(THREAD_SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Останавливаем потоки
        for(int i = 0; i < numThreads; i++){
            threads[i].stop();
        }
        //Выводим результат
        LinkedList<URLDepthPair> result = pool.getClosedPairs();
        l.log("\nРезультаты:");
        for(URLDepthPair pair : result){
            System.out.println(pair.toString());
        }
    }
}
