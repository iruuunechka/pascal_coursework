package compiler;

import org.objectweb.asm.Type;

/**
 * @author Irene Petrova
 */
public class Utils {
    public static Type getType(String type) {
        switch (type) {
            case "CHAR" : return Type.CHAR_TYPE;
            case "INTEGER" : return Type.INT_TYPE;
            case "STRING" : return Type.getType(String.class);
            default : throw new IllegalArgumentException("Invalid type");
        }
    }

    public static void checkType (Type t1, Type t2) {
        if (!t1.equals(t2)) {
            throw new IllegalStateException("Type mismatch:" + t1 + " not equals " + t2);
        }
    }
}
