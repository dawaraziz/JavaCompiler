// JOOS1:ENVIRONMENTS,DUPLICATE_VARIABLE
// JOOS2:ENVIRONMENTS,DUPLICATE_VARIABLE
// JAVAC:UNKNOWN
// 
/**
 * Environments:
 * - Check that no two local variables with overlapping scope have the
 * same name.
 */

public class Je_2_Locals_Overlapping_InsideDoubleBlock {

    public Je_2_Locals_Overlapping_InsideDoubleBlock() {}

    public static int test() {
	int y = 6;
	int r = 0;
	{
	    {
		int r = 123;
		r = 5;
		return r;
	    }
	}
    }
}
