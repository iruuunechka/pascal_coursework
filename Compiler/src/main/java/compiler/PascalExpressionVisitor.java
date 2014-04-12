package compiler;

import compiler.register.PascalRegistry;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import parser.GrammarParser;

/**
 * @author Irene Petrova
 */
public class PascalExpressionVisitor {
    private final PascalRegistry reg;

    public PascalExpressionVisitor(PascalRegistry reg) {
        this.reg = reg;
    }

    public Type visit(GrammarParser.ExpressionContext context) {
        if (context.COMPARATOR() == null) {
            return visit(context.applicativeExpr(0));
        }
        Utils.checkType(visit(context.applicativeExpr(0)), Type.INT_TYPE);
        Utils.checkType(visit(context.applicativeExpr(1)), Type.INT_TYPE);
        LabelNode ln = new LabelNode();
        switch (context.COMPARATOR().getText()) {
            case ("<") :
                reg.addInstruction(new JumpInsnNode(Opcodes.IF_ICMPGE, ln));
                break;
            case (">") :
                reg.addInstruction(new JumpInsnNode(Opcodes.IF_ICMPLE, ln));
                break;
            case ("<=") :
                reg.addInstruction(new JumpInsnNode(Opcodes.IF_ICMPGT, ln));
                break;
            case (">=") :
                reg.addInstruction(new JumpInsnNode(Opcodes.IF_ICMPLT, ln));
                break;
            case ("=") :
                reg.addInstruction(new JumpInsnNode(Opcodes.IF_ICMPNE, ln));
                break;
            case ("<>") :
                reg.addInstruction(new JumpInsnNode(Opcodes.IF_ICMPEQ, ln));
                break;
        }
        LabelNode end = new LabelNode();
        reg.addInstruction(new InsnNode(Opcodes.ICONST_1));
        reg.addInstruction(new JumpInsnNode(Opcodes.GOTO, end));
        reg.addInstruction(ln);
        reg.addInstruction(new InsnNode(Opcodes.ICONST_0));
        reg.addInstruction(end);
        return Type.INT_TYPE;
    }

    public Type visit(GrammarParser.ApplicativeExprContext context) {
        if (context.SIGN().isEmpty()) {
            return visit(context.multiplicativeExpr(0));
        }
        Type type;
        int i = 0;
        if (context.SIGN().size() == context.multiplicativeExpr().size()) {
            reg.addInstruction(new InsnNode(Opcodes.ICONST_0));
            type = Type.INT_TYPE;
        } else {
            type = visit(context.multiplicativeExpr(0));
            i++;
        }
        for (TerminalNode sign : context.SIGN()) {
            Type secondType = visit(context.multiplicativeExpr(i++));
            if (secondType.equals(Type.CHAR_TYPE)) {
                throw new IllegalStateException("Wrong type for operation");
            }
            switch (sign.getText()) {
                case ("+") :
                    Utils.checkType(secondType, type);
                    if (type.equals(Type.INT_TYPE)) {
                        reg.addInstruction(new InsnNode(Opcodes.IADD));
                    } else {
                        reg.addInstruction(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(String.class), "concat", Type.getMethodDescriptor(Type.getType(String.class), Type.getType(String.class))));
                    }
                    break;
                case ("-") :
                    Utils.checkType(type, Type.INT_TYPE);
                    Utils.checkType(secondType, type);
                    reg.addInstruction(new InsnNode(Opcodes.ISUB));
                    break;
            }
        }
        return type;
    }

    public Type visit(GrammarParser.MultiplicativeExprContext context) {
        if (!context.OPERATOR().isEmpty()) {
            GrammarParser.TermContext tc = context.term(0);
            Utils.checkType(visit(tc), Type.INT_TYPE);
            for (int i = 1; i < context.term().size(); ++i) {
                Utils.checkType(visit(context.term(i)), Type.INT_TYPE);
                switch(context.OPERATOR(i - 1).getText()) {
                    case ("*") :
                        reg.addInstruction(new InsnNode(Opcodes.IMUL));
                        break;
                    case ("/") :
                        reg.addInstruction(new InsnNode(Opcodes.IDIV));
                        break;
                    case ("MOD") :
                        reg.addInstruction(new InsnNode(Opcodes.IREM));
                        break;
                }
            }
            return Type.INT_TYPE;
        } else {
            return visit(context.term(0));
        }
    }

    public Type visit(GrammarParser.TermContext context) {
        if (context.expression() != null) {
            return visit(context.expression());
        } else if (context.STRING_EXPRESSION() != null) {
            String str = context.STRING_EXPRESSION().getText();
            reg.addInstruction(new LdcInsnNode(str.substring(1, str.length() - 1)));
            return Type.getType(String.class);
        } else if (context.CHAR_EXPRESSION() != null) {
            reg.addInstruction(new LdcInsnNode(context.CHAR_EXPRESSION().getText().charAt(1)));
            return Type.CHAR_TYPE;
        } else if (context.NUMBER() != null) {
            reg.addInstruction(new LdcInsnNode(Integer.valueOf(context.NUMBER().getText())));
            return Type.INT_TYPE;
        } else if (context.name() != null) {
            return visit(context.name());
        } else if (context.callStatement() != null) {
            //TODO
        }
        return Type.VOID_TYPE;
    }

    public Type visit(GrammarParser.NameContext context) {
        String varName = context.IDENTIFIER().getText();
        if (!reg.hasGlobalVar(varName)) {
            throw new IllegalStateException("Undefined variable" + varName);
        }
        Type varType = reg.getGlobalVarType(varName);
        if (context.expression() != null) {
            Utils.checkType(varType, Type.getType(String.class));
            Utils.checkType(visit(context.expression()), Type.INT_TYPE);
            reg.addInstruction(new FieldInsnNode(Opcodes.GETSTATIC, reg.getProgramName(), varName, varType.getDescriptor()));
            reg.addInstruction(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(String.class), "charAt", Type.getMethodDescriptor(Type.CHAR_TYPE, Type.INT_TYPE)));
            return Type.CHAR_TYPE;
        } else {
            reg.addInstruction(new FieldInsnNode(Opcodes.GETSTATIC, reg.getProgramName(), varName, varType.getDescriptor()));
        }
        return varType;
    }
}
