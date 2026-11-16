package hexcassettes.miyucomics.hexcassettes;

public final class CassetteCastFlags {
    private static final ThreadLocal<Boolean> CASTING = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final ThreadLocal<Boolean> SOUND_ALLOWED = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private CassetteCastFlags() {}

    public static void begin(boolean soundAllowed) {
        CASTING.set(Boolean.TRUE);
        SOUND_ALLOWED.set(soundAllowed);
    }

    public static void end() {
        CASTING.set(Boolean.FALSE);
        SOUND_ALLOWED.set(Boolean.FALSE);
    }

    public static boolean isCasting() {
        return CASTING.get();
    }

    public static boolean isSoundAllowed() {
        return SOUND_ALLOWED.get();
    }
}
