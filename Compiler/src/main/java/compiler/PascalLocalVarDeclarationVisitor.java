package compiler;

import compiler.register.PascalRegistry;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.objectweb.asm.Type;
import parser.GrammarParser;

/**
 * @author Irene Petrova
 */
public class PascalLocalVarDeclarationVisitor implements PascalVarDeclarationVisitor{
    private PascalRegistry reg;

    public PascalLocalVarDeclarationVisitor(PascalRegistry registry) {
        reg = registry;
    }

    @Override
    public void visit(GrammarParser.VarDeclarationContext context) {
        Type type = Utils.getType(context.TYPE().getText());
        for (TerminalNode id : context.IDENTIFIER()) {
            if (reg.hasLocalVar(id.getText())) {
                throw new IllegalStateException("Dublicate local variable" + id.getText());
            }
            Utils.checkLocalVarName(id.getText(), reg.getCurrentMethodName());
            reg.addLocalVar(id.getText(), type);
        }
    }
}
