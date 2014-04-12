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
public class PascalStatementVisitor {
    private final PascalRegistry reg;
    private final PascalExpressionVisitor expVisitor;

    public PascalStatementVisitor(PascalRegistry reg) {
        this.reg = reg;
        this.expVisitor = new PascalExpressionVisitor(reg);
    }

    public void visit (GrammarParser.StatementContext context) {
        if (context.block() != null) {
            visit(context.block());
        } else if (context.breakStatement() != null) {
            visit(context.breakStatement());
        } else if (context.continueStatement() != null) {
            visit(context.continueStatement());
        } else if (context.ifStatement() != null) {
            visit(context.ifStatement());
        } else if (context.forStatement() != null) {
            visit(context.forStatement());
        } else if (context.whileStatement() != null) {
            visit(context.whileStatement());
        } else if (context.callStatement() != null) {
            visit(context.callStatement());
        } else if (context.assignmentStatement() != null) {
            visit(context.assignmentStatement());
        } else if (context.readStatement() != null) {
            visit(context.readStatement());
        } else if (context.writeStatement() != null) {
            visit(context.writeStatement());
        }

    }

    private void visit(GrammarParser.WriteStatementContext context) {
        for (GrammarParser.ExpressionContext econtext : context.expressionList().expression()) {
            reg.addInstruction(new FieldInsnNode(Opcodes.GETSTATIC, Type.getInternalName(System.class), "out", Type.getDescriptor(PrintStream.class)));
            Type type = expVisitor.visit(econtext);
            reg.addInstruction(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(PrintStream.class), "print", Type.getMethodDescriptor(Type.VOID_TYPE, type)));
        }
    }

    private void visit(GrammarParser.ReadStatementContext context) {

    }

    private void visit(GrammarParser.AssignmentStatementContext context) {
        String varName = context.name().IDENTIFIER().getText();
        if (reg.hasGlobalVar(varName)) {
            Type varType = reg.getGlobalVarType(varName);
            if (context.name().expression() != null) {
                Utils.checkType(varType, Type.getType(String.class));
                reg.addInstruction(new FieldInsnNode(Opcodes.GETSTATIC, reg.getProgramName(), varName, varType.getDescriptor()));
                reg.addInstruction(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(String.class), "toCharArray", Type.getMethodDescriptor(Type.getType(char[].class))));
                reg.addInstruction(new InsnNode(Opcodes.DUP));
                Utils.checkType(expVisitor.visit(context.name().expression()), Type.INT_TYPE);
                Utils.checkType(expVisitor.visit(context.expression()), Type.CHAR_TYPE);
                reg.addInstruction(new InsnNode(Opcodes.CASTORE));
                reg.addInstruction(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(String.class), "valueOf", Type.getMethodDescriptor(Type.getType(String.class), Type.getType(char[].class))));
            } else {
                Utils.checkType(expVisitor.visit(context.expression()), varType);
            }
            reg.addInstruction(new FieldInsnNode(Opcodes.PUTSTATIC, reg.getProgramName(), varName, varType.getDescriptor()));
        }
    }

    private void visit(GrammarParser.CallStatementContext context) {

    }

    private void visit(GrammarParser.WhileStatementContext context) {
        LabelNode continueL = new LabelNode();
        reg.addInstruction(continueL);
        expVisitor.visit(context.expression());
        LabelNode breakL = new LabelNode();
        reg.addInstruction(new JumpInsnNode(Opcodes.IFEQ, breakL));
        visit(context.statement());
        reg.addInstruction(new JumpInsnNode(Opcodes.GOTO, continueL));
        reg.addInstruction(breakL);
    }

    private void visit(GrammarParser.ForStatementContext context) {
        String forVarName = context.assignmentStatement().name().IDENTIFIER().getText();
        Type forVarType = reg.getGlobalVarType(forVarName);
        Utils.checkType(forVarType, Type.INT_TYPE);
        visit(context.assignmentStatement());
        LabelNode startL = new LabelNode();
        reg.addInstruction(startL);
        expVisitor.visit(context.expression());
        expVisitor.visit(context.assignmentStatement().name());
        LabelNode breakL = new LabelNode();
        boolean direction = "TO".equals(context.getChild(2).getText());
        reg.addInstruction(new JumpInsnNode(direction ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPGT, breakL));
        visit(context.statement());
        LabelNode continueL = new LabelNode();
        reg.addInstruction(continueL);

        expVisitor.visit(context.assignmentStatement().name());
        reg.addInstruction(new InsnNode(direction ? Opcodes.ICONST_1 : Opcodes.ICONST_M1));
        reg.addInstruction(new InsnNode(Opcodes.IADD));
        reg.addInstruction(new FieldInsnNode(Opcodes.PUTSTATIC, reg.getProgramName(), forVarName, forVarType.getDescriptor()));

        reg.addInstruction(new JumpInsnNode(Opcodes.GOTO, startL));
        reg.addInstruction(breakL);

    }

    private void visit(GrammarParser.IfStatementContext context) {
        Utils.checkType(expVisitor.visit(context.expression()), Type.INT_TYPE);
        LabelNode end = new LabelNode();
        if (context.statement().size() == 2) {
            LabelNode elseL = new LabelNode();
            reg.addInstruction(new JumpInsnNode(Opcodes.IFEQ, elseL));
            visit(context.statement(0));
            reg.addInstruction(new JumpInsnNode(Opcodes.GOTO, end));
            reg.addInstruction(elseL);
            visit(context.statement(1));
            reg.addInstruction(end);
        } else {
            reg.addInstruction(new JumpInsnNode(Opcodes.IFEQ, end));
            visit(context.statement(0));
            reg.addInstruction(end);
        }

    }

    private void visit(GrammarParser.ContinueStatementContext context) {

    }

    private void visit(GrammarParser.BreakStatementContext context) {

    }

    public void visit(GrammarParser.BlockContext context) {
        for (GrammarParser.StatementContext scontext : context.statement())
            visit(scontext);
    }
}
