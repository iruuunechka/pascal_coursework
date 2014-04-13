package compiler;

import compiler.register.PascalRegistry;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.objectweb.asm.Type;
import parser.GrammarParser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Irene Petrova
 */
public class PascalFunctionDeclarationVisitor {
    private final PascalRegistry reg;
    private final PascalVarDeclarationVisitor varVisitor;
    private final PascalStatementVisitor stVisitor;

    public PascalFunctionDeclarationVisitor(PascalRegistry registry, PascalStatementVisitor statementVisitor) {
        this.reg = registry;
        this.stVisitor = statementVisitor;
        varVisitor = new PascalLocalVarDeclarationVisitor(reg);
    }

    public void visit(GrammarParser.FunctionDeclarationContext context) {
        visit(context.functionHeading());
        for (GrammarParser.VarDeclarationContext vcontext : context.varBlock().varDeclaration()) {
            varVisitor.visit(vcontext);
        }
        stVisitor.visit(context.block());
    }

    public void visit(GrammarParser.FunctionHeadingContext context) {
        reg.resetLocalVars();
        List<Type> input = new ArrayList<>();
        for (GrammarParser.VarDeclarationContext vcontext : context.varDeclaration()) {
            Type type = Utils.getType(vcontext.TYPE().getText());
            for (TerminalNode id : vcontext.IDENTIFIER()) {
                input.add(type);
                reg.addLocalVar(id.getText(), type);
                Utils.checkLocalVarName(id.getText(), context.IDENTIFIER().getText());
            }
        }
        Type output = Utils.getType(context.TYPE().getText());
        reg.startFunctionDeclaration(context.IDENTIFIER().getText(), output, input.toArray(new Type[input.size()]));
    }
}
