package compiler;

import compiler.register.PascalRegistry;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.objectweb.asm.Type;
import parser.GrammarParser;

/**
 * @author Irene Petrova
 */
public class PascalGlobalVarDeclarationVisitor implements PascalVarDeclarationVisitor {
    private PascalRegistry reg;

    public PascalGlobalVarDeclarationVisitor(PascalRegistry registry) {
        reg = registry;
    }

    @Override
    public void visit(GrammarParser.VarDeclarationContext context) {
        Type type = Utils.getType(context.TYPE().getText());
        for (TerminalNode id : context.IDENTIFIER()) {
            if (reg.hasGlobalVar(id.getText())) {
                throw new IllegalStateException("Dublicate global variable" + id.getText());
            }
            reg.addGlobalVar(id.getText(), type);
        }
    }
}
