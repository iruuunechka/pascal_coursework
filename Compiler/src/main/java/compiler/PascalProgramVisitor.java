package compiler;

import compiler.register.PascalRegistry;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import parser.GrammarParser;

import java.io.PrintStream;

/**
 * @author Irene Petrova
 */
public class PascalProgramVisitor {
    private final PascalRegistry reg;

    private final PascalVarDeclarationVisitor varVisitor;
    private final PascalFunctionDeclarationVisitor funVisitor = new PascalFunctionDeclarationVisitor();

    public PascalProgramVisitor(PascalRegistry reg) {
        this.reg = reg;
        varVisitor = new PascalGlobalVarDeclarationVisitor(this.reg);
    }

    public void visit(GrammarParser.ProgramContext context) {
        reg.setProgramName(context.IDENTIFIER().getText());

        for (GrammarParser.VarDeclarationContext vcontext : context.varBlock().varDeclaration())
            varVisitor.visit(vcontext);

        for (GrammarParser.FunctionDeclarationContext fcontext : context.functionDeclaration())
            funVisitor.visit(fcontext);

        MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mn.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, Type.getInternalName(System.class), "out", Type.getDescriptor(PrintStream.class)));
        mn.instructions.add(new LdcInsnNode("Hello kitty!"));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(PrintStream.class), "println", "(Ljava/lang/String;)V"));
        mn.instructions.add(new InsnNode(Opcodes.RETURN));
        reg.addMethod(mn);
    }
}