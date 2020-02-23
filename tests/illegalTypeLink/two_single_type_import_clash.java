// PARSER_WEEDER
/**
 * This method is supposed to test whether access to the resulting
 * objects of method calls are parsed correctly.
 **/
import x.import_class_clash;
import y.import_class_clash;
import java.util.ArrayList;

public class two_single_type_import_clash{

    public int i;
    public ArrayList lis;

    public two_single_type_import_clash(int j){
        i = j;
    }

    public two_single_type_import_clash inc(){
        return new two_single_type_import_clash(i+1);
    }

    public static int test(){
        return new two_single_type_import_clash(120).inc().inc().inc().i;
    }

}
