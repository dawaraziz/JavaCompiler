// PARSER_WEEDER,CODE_GENERATION

import java.util.ArrayList;
import java.util.HashMap;

public class typeChanging {

    public ArrayList arr_list;

    public typeChanging(){}

    public static ArrayList test(){
        int[] posty = new TestClass[5];
        TestClass hi = (TestClass) object;
        posty[2] = 5;
        int hello = 15;
        return new ArrayList();
    }

    public int test2(ArrayList changed) {

        ArrayList the_list = new ArrayList();
        ArrayList.add();
        Callfunc();
        return 5;
    }

    public static void main(String[] args) {
        Point p = new Point();
        Element e = new Element();
        int y = 6;
        if (e instanceof Point) {											// compile-time error
            System.out.println("I get your point!");
            p = (Point)e;										// compile-time error
        }
        else if (y < 5){
            int u = 5;
        }
    }
}

