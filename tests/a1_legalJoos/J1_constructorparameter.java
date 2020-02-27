// PARSER_WEEDER
public class J1_constructorparameter {

    protected int i;

    public J1_constructorparameter(int i) {
	this.i = i;
    }

    public static int test(int i) {
        {
            int r = 7;
        }

        return new J1_constructorparameter(123).i;
    }

}
