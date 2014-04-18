package compiler;

import compiler.register.PascalRegistry;
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
            throw new IllegalStateException("Type mismatch:" + t1.getClassName() + " not equals " + t2.getClassName());
        }
    }

    public static void checkLocalVarName(String varName, String name) {
        if (varName.equals(name)) {
            throw new IllegalStateException("Variable has the same name with function name");
        }
    }

    public static Type getVarType(String varName, PascalRegistry reg) {
        Type varType;
        if (reg.hasLocalVar(varName)) {
            varType = reg.getLocalVarType(varName);
        } else if (reg.hasGlobalVar(varName)) {
            varType = reg.getGlobalVarType(varName);
        } else {
            throw new IllegalStateException("Variable " + varName + " does not exists");
        }
        return varType;
    }

}
