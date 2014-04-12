import compiler.PascalProgramVisitor;
import compiler.register.PascalRegistry;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import parser.GrammarLexer;
import parser.GrammarParser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Irene Petrova
 */
public class Main {
    public static void main(String[] args) {
        try {
            InputStream is = new FileInputStream("input.pas");
            ANTLRInputStream ais = new ANTLRInputStream(is);
            GrammarLexer lex = new GrammarLexer(ais);
            GrammarParser pars = new GrammarParser(new CommonTokenStream(lex));
            GrammarParser.ProgramContext context = pars.program();

            PascalRegistry reg = new PascalRegistry();
            new PascalProgramVisitor(reg).visit(context);

            try (FileOutputStream fos = new FileOutputStream(reg.getProgramName() + ".class")) {
                fos.write(reg.getBytecode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
