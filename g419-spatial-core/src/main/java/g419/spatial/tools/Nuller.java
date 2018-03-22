package g419.spatial.tools;

import java.util.Optional;
import java.util.function.Supplier;

public final class Nuller {

    public static <T> Optional<T> resolve(Supplier<T> resolver) {
        try {
            T result = resolver.get();
            return Optional.ofNullable(result);
        }
        catch (NullPointerException e) {
            return Optional.empty();
        }
    }

}
