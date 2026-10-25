package Test2;
import java.util.*;

class Task {
    int id;
    Task(int id) { this.id = id; }
}

class Processor implements Runnable {
    private final String name;
    private final LinkedList<Task> myQueue;
    private final LinkedList<Task> otherQueue;
    private final int myLimit;
    private final int otherLimit;
    private final Object lock;

    Processor(String name, LinkedList<Task> my, LinkedList<Task> other, int myLim, int otherLim, Object lock) {
        this.name = name;
        this.myQueue = my;
        this.otherQueue = other;
        this.myLimit = myLim;
        this.otherLimit = otherLim;
        this.lock = lock;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Task task = null;

                synchronized (lock) {
                    // Условие кражи: когда длина своей очереди не превышает заданного для этой очереди размера, а длина другой очереди превышает заданный для нее размер, процессор берет на обработку чужой процесс.
                    if (myQueue.size() <= myLimit && otherQueue.size() > otherLimit) {
                        task = otherQueue.poll();
                        if (task != null) System.out.println(name + " украл задачу " + task.id);
                    }
                    
                    if (task == null) {
                        task = myQueue.poll();
                        if (task != null) System.out.println(name + " взял свою задачу " + task.id);
                    }
                }

                if (task != null) {
                    Thread.sleep(50); // Thread.sleep(200); - украдет
                } else {
                    Thread.sleep(20);
                }
            }
        } catch (InterruptedException e) {
            // Поток остановлен
        }
    }
}

public class lab4 {
    public static void main(String[] args) {
        System.out.println("--- Программа запущена ---");

        int LIMIT_1 = 2; //0;1 - украдет
        int LIMIT_2 = 2; //0;1 - украдет
        int count_1 = 0;
        int count_2 = 0;
        int max_1 = 0;
        int max_2 = 0;
        Object lock = new Object(); // Общий замок для всех очередей

        LinkedList<Task> q1 = new LinkedList<>();
        LinkedList<Task> q2 = new LinkedList<>();

        Thread p1 = new Thread(new Processor("CPU-1", q1, q2, LIMIT_1, LIMIT_2, lock));
        Thread p2 = new Thread(new Processor("CPU-2", q2, q1, LIMIT_2, LIMIT_1, lock));

        p1.start();
        p2.start();

        // Цикл (30 раз) > Случайный выбор очереди > Безопасная запись (Lock) > Пауза > Ожидание завершения всех потоков.
        Random rand = new Random();
        try {
            for (int i = 1; i <= 30; i++) {
                synchronized (lock) {
                    if (rand.nextBoolean()) {
                        q1.add(new Task(i));
                        count_1 += 1;
                        max_1 = i;
                    } else {
                        q2.add(new Task(i));
                        count_2 += 1;
                        max_2 = i;
                    }
                }
                Thread.sleep(40); // Thread.sleep(10); -украдет
            }
            System.out.println("Все задачи добавлены, ждем завершения...");
            Thread.sleep(3000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        p1.interrupt();
        p2.interrupt();
        System.out.println("CPU-1 взял " + count_1 + " задач(и). Макимальная: " + max_1);
        System.out.println("CPU-1 взял " + count_2 + " задач(и). Макимальная: " + max_2);
        System.out.println("--- Работа завершена ---");
    }
}
