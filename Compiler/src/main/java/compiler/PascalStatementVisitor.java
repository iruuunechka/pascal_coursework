package compiler;

import compiler.register.PascalRegistry;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import parser.GrammarParser;

import java.io.Console;
import java.io.PrintStream;
import java.util.Stack;

/**
 * @author Irene Petrova
 */
public class PascalStatementVisitor {
    private final PascalRegistry reg;
    private final PascalExpressionVisitor expVisitor;
    private final Stack<LabelNode> continueLabels = new Stack<>();
    private final Stack<LabelNode> breakLabels = new Stack<>();

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
            expVisitor.visit(context.callStatement());
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
        for (TerminalNode id : context.IDENTIFIER()) {
            reg.addInstruction(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(System.class), "console",
                    Type.getMethodDescriptor(Type.getType(Console.class))));
            reg.addInstruction(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Console.class), "readLine",
                    Type.getMethodDescriptor(Type.getType(String.class))));
            String varName = id.getText();
            Type varType = Utils.getVarType(varName, reg);
            if (varType.equals(Type.INT_TYPE)) {
                reg.addInstruction(new MethodInsnNode(Opcodes.INVOKESTATIC, Type.getInternalName(Integer.class), "parseInt",
                        Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(String.class))));
            } else if (varType.equals(Type.CHAR_TYPE)) {
                reg.addInstruction(new InsnNode(Opcodes.ICONST_0));
                reg.addInstruction(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(String.class), "charAt",
                        Type.getMethodDescriptor(Type.CHAR_TYPE, Type.INT_TYPE)));
            }
            storeVar(varName, varType);
        }
    }

    private void visit(GrammarParser.AssignmentStatementContext context) {
        String varName = context.name().IDENTIFIER().getText();
        if (varName.equals(reg.getCurrentMethodName())) {
            if (context.name().expression() != null) {
                throw new IllegalStateException("Illegal arguments to function name");
            }
            Utils.checkType(expVisitor.visit(context.expression()), reg.getCurrentMethodType());
            reg.addInstruction(new InsnNode(reg.getCurrentMethodType().equals(Type.getType(String.class)) ? Opcodes.ARETURN : Opcodes.IRETURN));
            return;
        }
        Type varType = Utils.getVarType(varName, reg);
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
        storeVar(varName, varType);
    }


    private void storeVar(String varName, Type varType) {
        if (reg.hasLocalVar(varName)){
            reg.addInstruction(new VarInsnNode(varType.equals(Type.getType(String.class)) ? Opcodes.ASTORE : Opcodes.ISTORE, reg.getLocalVarIndex(varName)));
        } else {
            reg.addInstruction(new FieldInsnNode(Opcodes.PUTSTATIC, reg.getProgramName(), varName, varType.getDescriptor()));
        }
    }

    private void visit(GrammarParser.WhileStatementContext context) {
        LabelNode continueL = new LabelNode();
        continueLabels.push(continueL);
        LabelNode breakL = new LabelNode();
        breakLabels.push(breakL);
        reg.addInstruction(continueL);
        expVisitor.visit(context.expression());
        reg.addInstruction(new JumpInsnNode(Opcodes.IFEQ, breakL));
        visit(context.statement());
        reg.addInstruction(new JumpInsnNode(Opcodes.GOTO, continueL));
        reg.addInstruction(breakL);
        breakLabels.pop();
        continueLabels.pop();
    }

    private void visit(GrammarParser.ForStatementContext context) {
        LabelNode breakL = new LabelNode();
        breakLabels.push(breakL);
        LabelNode continueL = new LabelNode();
        continueLabels.push(continueL);

        String forVarName = context.assignmentStatement().name().IDENTIFIER().getText();
        Type forVarType = Utils.getVarType(forVarName, reg);
        Utils.checkType(forVarType, Type.INT_TYPE);
        visit(context.assignmentStatement());
        LabelNode startL = new LabelNode();
        reg.addInstruction(startL);
        expVisitor.visit(context.expression());
        expVisitor.visit(context.assignmentStatement().name());

        boolean direction = "TO".equals(context.getChild(2).getText());
        reg.addInstruction(new JumpInsnNode(direction ? Opcodes.IF_ICMPLT : Opcodes.IF_ICMPGT, breakL));
        visit(context.statement());

        reg.addInstruction(continueL);

        expVisitor.visit(context.assignmentStatement().name());
        reg.addInstruction(new InsnNode(direction ? Opcodes.ICONST_1 : Opcodes.ICONST_M1));
        reg.addInstruction(new InsnNode(Opcodes.IADD));
        storeVar(forVarName, forVarType);

        reg.addInstruction(new JumpInsnNode(Opcodes.GOTO, startL));
        reg.addInstruction(breakL);

        breakLabels.pop();
        continueLabels.pop();
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

    private void visit(GrammarParser.BreakStatementContext context) {
        if (breakLabels.isEmpty())
            throw new IllegalStateException("Break is outside of loop");
        reg.addInstruction(new JumpInsnNode(Opcodes.GOTO, breakLabels.peek()));
    }

    private void visit(GrammarParser.ContinueStatementContext context) {
        if (continueLabels.isEmpty())
            throw new IllegalStateException("Continue is outside of loop");
        reg.addInstruction(new JumpInsnNode(Opcodes.GOTO, continueLabels.peek()));
    }

    public void visit(GrammarParser.BlockContext context) {
        for (GrammarParser.StatementContext scontext : context.statement())
            visit(scontext);
    }
}
