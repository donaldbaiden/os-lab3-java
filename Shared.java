import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

class Shared {
    int[] arr;
    int size;
    int markerCount;
    int aliveCount;
    int blockedCount;
    boolean started;
    ReentrantLock lock = new ReentrantLock();
    Condition condStart;
    Condition condMain;
    MarkerState[] markers;
}
