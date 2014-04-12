package compiler.register;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V1_7;

/**
 * @author Irene Petrova
 */
public class PascalRegistry {
    private final ClassNode cn;
    private final Map<String, Type> globalVars = new HashMap<>();
    private MethodNode currentMN;

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

    public boolean hasGlobalVar(String name) {
        return globalVars.containsKey(name);
    }

    public void addGlobalVar(String name, Type type) {
        globalVars.put(name, type);
        cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, name, type.getDescriptor(), null, null));
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

    public void addInstruction(InsnList instructions) {
        currentMN.instructions.add(instructions);
    }
}
