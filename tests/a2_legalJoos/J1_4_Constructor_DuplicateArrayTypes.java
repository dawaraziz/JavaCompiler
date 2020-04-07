// HIERARCHY
/**
 * Hierarchy:
 * - A class must not declare two constructors with the same parameter
 * types (8.8.2, simple constraint 5).
 */

public class J1_4_Constructor_DuplicateArrayTypes {
    public J1_4_Constructor_MatchAsSets a;

    public J1_4_Constructor_DuplicateArrayTypes() {
        J1_4_Constructor_MatchAsSets a = new J1_4_Constructor_MatchAsSets("a", "b");
    }
    
    public static int test() {
	return 123;
    }
}
