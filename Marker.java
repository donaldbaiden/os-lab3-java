import java.util.Random;

class Marker implements Runnable {
    private final int num;
    private final Shared g;

    Marker(int num, Shared g) {
        this.num = num;
        this.g = g;
    }

    @Override
    public void run() {
        MarkerState state = g.markers[num - 1];

        g.lock.lock();
        while (!g.started) {
            g.condStart.awaitUninterruptibly();
        }
        g.lock.unlock();

        Random rand = new Random(num);

        while (true) {
            int r = rand.nextInt(g.size);

            g.lock.lock();
            if (g.arr[r] == 0) {
                g.lock.unlock();
                try { Thread.sleep(5); } catch (InterruptedException e) {}
                g.lock.lock();
                g.arr[r] = num;
                state.markedCount++;
                g.lock.unlock();
                try { Thread.sleep(5); } catch (InterruptedException e) {}
                continue;
            }

            System.out.println("Marker " + num + ": marked " + state.markedCount
                    + ", blocked at index " + r);
            state.blocked = true;
            g.blockedCount++;
            g.condMain.signal();

            while (!state.shouldContinue && !state.shouldTerminate) {
                state.cond.awaitUninterruptibly();
            }

            if (state.shouldTerminate) {
                for (int i = 0; i < g.size; i++) {
                    if (g.arr[i] == num) g.arr[i] = 0;
                }
                state.alive = false;
                g.aliveCount--;
                g.blockedCount--;
                g.condMain.signal();
                g.lock.unlock();
                return;
            }

            state.shouldContinue = false;
            state.blocked = false;
            g.blockedCount--;
            g.lock.unlock();
        }
    }
}
