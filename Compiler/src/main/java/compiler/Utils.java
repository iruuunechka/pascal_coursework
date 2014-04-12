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

}
