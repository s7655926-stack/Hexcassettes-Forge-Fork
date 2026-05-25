package hexcassettes.miyucomics.hexcassettes.patterns;

import at.petrak.hexcasting.api.casting.RenderedSpell;

import java.lang.reflect.Proxy;

public final class NoOpSpell {

    public static final RenderedSpell INSTANCE = (RenderedSpell) Proxy.newProxyInstance(
            RenderedSpell.class.getClassLoader(),
            new Class<?>[]{RenderedSpell.class},
            (proxy, method, args) -> {
                String name = method.getName();

                if (name.equals("toString")) {
                    return "Hexcassettes NoOpSpell";
                }

                if (name.equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }

                if (name.equals("equals")) {
                    return proxy == args[0];
                }

                Class<?> returnType = method.getReturnType();

                if (returnType == Void.TYPE) {
                    return null;
                }

                if (returnType == Boolean.TYPE) {
                    return false;
                }

                if (returnType == Byte.TYPE) {
                    return (byte) 0;
                }

                if (returnType == Short.TYPE) {
                    return (short) 0;
                }

                if (returnType == Integer.TYPE) {
                    return 0;
                }

                if (returnType == Long.TYPE) {
                    return 0L;
                }

                if (returnType == Float.TYPE) {
                    return 0.0F;
                }

                if (returnType == Double.TYPE) {
                    return 0.0D;
                }

                if (returnType == Character.TYPE) {
                    return '\0';
                }

                return null;
            }
    );

    private NoOpSpell() {

    }
}