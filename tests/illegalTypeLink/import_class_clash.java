// PARSER_WEEDER
/**
 * This method is supposed to test whether access to the resulting
 * objects of method calls are parsed correctly.
 **/
import x.import_class_clash;
import y.test;

public class import_class_clash{

    public int i;

    public import_class_clash(int j){
        i = j;
    }

    public import_class_clash inc(){
        return new import_class_clash(i+1);
    }

    public static int test(){
        return new import_class_clash(120).inc().inc().inc().i;
    }

}
