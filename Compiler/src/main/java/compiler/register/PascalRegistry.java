package compiler.register;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V1_7;

/**
 * @author Irene Petrova
 */
public class PascalRegistry {
    private final ClassNode cn;
    private final Map<String, Type> globalVars = new HashMap<>();
    private final Map<String, Pair> localVars = new HashMap<>();
    private final Map<FunctionType, Type> functions = new HashMap<>();
    private MethodNode currentMN;
    private Type currentMNType;

    public PascalRegistry() {
        cn = new ClassNode();
        cn.version = V1_7;
        cn.access = ACC_PUBLIC;
        cn.superName = "java/lang/Object";
    }

    public void setProgramName(String name) {
        cn.name = name;
    }

    public String getProgramName() {
        return cn.name;
    }

    public boolean hasFunction(String name, Type... input) {
        return functions.containsKey(new FunctionType(name, input));
    }

    public Type getFunctionType(String name, Type... input) {
        Type output = functions.get(new FunctionType(name, input));
        return Type.getMethodType(output, input);
    }

    public Type getFunctionOutputType(String name, Type... input) {
        return functions.get(new FunctionType(name, input));
    }

    public void startFunctionDeclaration(String name, Type output, Type... input) {
        functions.put(new FunctionType(name, input), output);
        MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, Type.getMethodDescriptor(output, input), null, null);
        cn.methods.add(mn);
        currentMNType = output;
        currentMN = mn;
    }

    public void resetLocalVars() {
        localVars.clear();
    }

    public boolean hasGlobalVar(String name) {
        return globalVars.containsKey(name);
    }

    public Type getGlobalVarType(String name) {
        return globalVars.get(name);
    }

    public boolean hasLocalVar(String name) {
        return localVars.containsKey(name);
    }

    public Type getLocalVarType(String name) {
        return localVars.get(name).type;
    }

    public String getCurrentMethodName() {
        return currentMN.name;
    }

    public Type getCurrentMethodType() {
        return currentMNType;
    }
    public int getLocalVarIndex(String name) {
        return localVars.get(name).id;
    }

    public void addGlobalVar(String name, Type type) {
        globalVars.put(name, type);
        cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, type.getDescriptor(), null, null));
    }

    public void addLocalVar(String name, Type type) {
        localVars.put(name, new Pair(type, localVars.size()));
    }
    public void addMethod(MethodNode mn) {
        cn.methods.add(mn);
        currentMN = mn;
    }

    public byte[] getBytecode() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        return cw.toByteArray();
    }

    public void addInstruction(AbstractInsnNode instruction) {
        currentMN.instructions.add(instruction);
    }

    private class Pair {
        private Type type;
        private int id;

        public Pair(Type type, int id) {
            this.type = type;
            this.id = id;
        }
    }

    private class FunctionType {
        private final String name;
        private final Type[] input;

        private FunctionType(String name, Type[] input) {
            this.name = name;
            this.input = input;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FunctionType that = (FunctionType) o;

            return Arrays.equals(input, that.input) && name.equals(that.name);

        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (input != null ? Arrays.hashCode(input) : 0);
            return result;
        }
    }
}

