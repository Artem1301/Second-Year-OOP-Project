package mvc.model;

public class CollisionOp {
    public enum Operation {
        ADD, REMOVE
    }

    private Movable mMovable;
    private Operation mOperation;

    public CollisionOp(Movable movable, Operation op) {
        mMovable = movable;
        mOperation = op;
    }


    public Movable getMovable() {
        return mMovable;
    }

    public Operation getOperation() {
        return mOperation;
    }

}
