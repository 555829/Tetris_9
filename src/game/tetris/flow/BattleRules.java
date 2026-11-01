package game.tetris.flow;

/** Local versus attack exchange, called after both players update. */
public final class BattleRules {
    private BattleRules() { }

    public static void exchangeAttacks(TetrisFlow first, TetrisFlow second) {
        if (!first.isRunning() || !second.isRunning()) return;
        int toSecond = first.consumeOutgoingAttack();
        int toFirst = second.consumeOutgoingAttack();
        second.receiveAttack(toSecond);
        first.receiveAttack(toFirst);
    }
}
