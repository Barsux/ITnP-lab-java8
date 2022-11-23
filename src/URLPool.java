import java.util.LinkedList;

/** Класс пул для взаимодействия с LinkedList в многопоточном режиме */
public class URLPool {
    private final LinkedList<URLDepthPair> openedPairs = new LinkedList<>();
    private final LinkedList<URLDepthPair> closedPairs = new LinkedList<>();
    //Переменная, учитывающая сколько потоков ожидает ссылок в пуле
    private int numWaiters = 0;

    /** Функция для получения ссылки из пула необработанных */
    public synchronized URLDepthPair pop(){
        while(isEmpty()){
            numWaiters++;
            try {
                wait();
            }
            catch (InterruptedException e) {}
            numWaiters--;
        }
        return openedPairs.removeFirst();
    }

    public int getWaiters(){
        return numWaiters;
    }

    /** Функция для добавления ссылки в пул необработанных */
    public synchronized boolean push(URLDepthPair pair){
        if(!openedPairs.contains(pair) && !closedPairs.contains(pair)){
            openedPairs.add(pair);
            notify();
            return true;
        }
        return false;
    }

    /** Функция, проверяющая пуст ли пул необработанных ссылок */
    public boolean isEmpty(){
        return openedPairs.isEmpty();
    }

    /** Функция для добавления ссылки в пул обработанных */
    public synchronized void migrate(URLDepthPair pair){
        if(!closedPairs.contains(pair)){
            closedPairs.add(pair);
        }
    }

    public LinkedList<URLDepthPair> getOpenedPairs(){
        return openedPairs;
    }

    public LinkedList<URLDepthPair> getClosedPairs(){
        return closedPairs;
    }
}
