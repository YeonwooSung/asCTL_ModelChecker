package modelChecker;

import java.util.Set;
import java.util.HashSet;

class CollectionHelper {
    public static <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> set = new HashSet<T>();

        for (T t : set1) {
            if (set2.contains(t)) {
                set.add(t);
            }
        }

        return set;
    }

    public static <T> Set<T> substraction(Set<T> set1, Set<T> set2) {
        Set<T> set = new HashSet<T>();
        set.addAll(set1);
        set.removeAll(set2);
        return set;
    }
}
