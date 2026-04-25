import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Размер массива: ");
        int n = sc.nextInt();
        System.out.print("Количество marker: ");
        int k = sc.nextInt();

        Shared g = new Shared();
        g.arr = new int[n];
        g.size = n;
        g.markerCount = k;
        g.aliveCount = k;
        g.blockedCount = 0;
        g.started = false;
        g.condStart = g.lock.newCondition();
        g.condMain = g.lock.newCondition();
        g.markers = new MarkerState[k];

        Thread[] threads = new Thread[k];
        for (int i = 0; i < k; i++) {
            MarkerState s = new MarkerState();
            s.num = i + 1;
            s.alive = true;
            s.cond = g.lock.newCondition();
            g.markers[i] = s;
            threads[i] = new Thread(new Marker(i + 1, g));
            threads[i].start();
        }

        g.lock.lock();
        g.started = true;
        g.condStart.signalAll();
        g.lock.unlock();

        while (true) {
            g.lock.lock();
            while (g.aliveCount > 0 && g.blockedCount < g.aliveCount) {
                g.condMain.awaitUninterruptibly();
            }
            if (g.aliveCount == 0) {
                g.lock.unlock();
                break;
            }

            System.out.print("Массив: ");
            for (int i = 0; i < g.size; i++) {
                System.out.print(g.arr[i] + " ");
            }
            System.out.println();

            g.lock.unlock();
            System.out.print("Какой marker завершить? ");
            int target = sc.nextInt();
            g.lock.lock();

            if (target < 1 || target > k || !g.markers[target - 1].alive) {
                System.out.println("Неверный номер");
                g.lock.unlock();
                continue;
            }

            g.markers[target - 1].shouldTerminate = true;
            g.markers[target - 1].cond.signal();

            while (g.markers[target - 1].alive) {
                g.condMain.awaitUninterruptibly();
            }

            System.out.print("Массив после завершения marker " + target + ": ");
            for (int i = 0; i < g.size; i++) {
                System.out.print(g.arr[i] + " ");
            }
            System.out.println();

            for (int i = 0; i < k; i++) {
                if (g.markers[i].alive && g.markers[i].blocked) {
                    g.markers[i].shouldContinue = true;
                    g.markers[i].cond.signal();
                }
            }
            g.lock.unlock();
        }

        for (int i = 0; i < k; i++) {
            threads[i].join();
        }

        System.out.println("Все потоки marker завершены.");
        System.out.print("Итоговый массив: ");
        for (int i = 0; i < g.size; i++) {
            System.out.print(g.arr[i] + " ");
        }
        System.out.println();
    }
}
