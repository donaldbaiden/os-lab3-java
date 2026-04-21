import java.util.concurrent.locks.Condition;

class MarkerState {
    int num;
    boolean blocked;
    boolean shouldContinue;
    boolean shouldTerminate;
    boolean alive;
    int markedCount;
    Condition cond;
}
