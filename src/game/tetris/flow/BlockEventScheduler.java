package game.tetris.flow;

import java.util.Random;

// 새 블록마다 방해 이벤트를 하나 뽑아 두고, 블록이 정해진 줄에 오면 그 이벤트를 꺼내 줌
final class BlockEventScheduler
{
    // 이벤트 확률은 레벨이 오를수록 커짐: 레벨 1에서 10%, 레벨마다 +5%, 최대 40%
    private static final double CHANCE_BASE = 0.10;
    private static final double CHANCE_PER_LEVEL = 0.05;
    private static final double CHANCE_MAX = 0.40;

    // 이벤트가 일어나는 줄: 3~7번째 줄 중 하나
    private static final int MIN_ROW = 3;
    private static final int ROW_RANGE = 5;

    private static final BlockEvent KICK = new KickEvent();
    private static final BlockEvent MORPH = new MorphEvent();

    private final Random random;
    private BlockEvent pending;     // null이면 이번 블록에는 이벤트 없음
    private int triggerRow;

    BlockEventScheduler(Random random)
    {
        this.random = random;
    }

    // 새 블록이 나올 때 호출. 차기를 먼저 뽑고, 안 뽑혔을 때만 모양 바꾸기를 뽑음 (한 블록에 하나만)
    void schedule(int level)
    {
        double chance = chanceFor(level);
        pending = null;
        if (random.nextDouble() < chance)
        {
            setPending(KICK);
            return;
        }
        if (random.nextDouble() < chance)
            setPending(MORPH);
    }

    // 블록이 정해진 줄에 왔으면 이벤트를 꺼내 줌. 한 번 꺼내면 다시 나오지 않음
    BlockEvent poll(int pieceY)
    {
        if (pending == null || pieceY < triggerRow)
            return null;

        BlockEvent event = pending;
        pending = null;
        return event;
    }

    void clear()
    {
        pending = null;
    }

    private void setPending(BlockEvent event)
    {
        pending = event;
        triggerRow = MIN_ROW + random.nextInt(ROW_RANGE);
    }

    private static double chanceFor(int level)
    {
        return Math.min(CHANCE_MAX, CHANCE_BASE + (level - 1) * CHANCE_PER_LEVEL);
    }
}