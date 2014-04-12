package compiler;

import parser.GrammarParser;

/**
 * @author Irene Petrova
 */
public interface PascalVarDeclarationVisitor {
    void visit(GrammarParser.VarDeclarationContext context);
}
