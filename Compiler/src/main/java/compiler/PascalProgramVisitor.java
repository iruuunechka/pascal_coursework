package compiler;

import compiler.register.PascalRegistry;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnNode;
import parser.GrammarParser;

/**
 * @author Irene Petrova
 */
public class PascalProgramVisitor {
    private final PascalRegistry reg;

    private final PascalVarDeclarationVisitor varVisitor;
    private final PascalFunctionDeclarationVisitor funVisitor;
    private final PascalStatementVisitor stVisitor;

    public PascalProgramVisitor(PascalRegistry reg) {
        this.reg = reg;
        varVisitor = new PascalGlobalVarDeclarationVisitor(this.reg);
        stVisitor = new PascalStatementVisitor(this.reg);
        funVisitor = new PascalFunctionDeclarationVisitor(this.reg, stVisitor);
    }

    public void visit(GrammarParser.ProgramContext context) {
        reg.setProgramName(context.IDENTIFIER().getText());

        for (GrammarParser.VarDeclarationContext vcontext : context.varBlock().varDeclaration())
            varVisitor.visit(vcontext);

        for (GrammarParser.FunctionDeclarationContext fcontext : context.functionDeclaration())
            funVisitor.visit(fcontext);

        reg.startFunctionDeclaration("main", Type.VOID_TYPE, Type.getType(String[].class));
        stVisitor.visit(context.block());
        reg.addInstruction(new InsnNode(Opcodes.RETURN));
    }
}
