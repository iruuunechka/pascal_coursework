grammar Grammar;
program : 'PROGRAM' IDENTIFIER ';' varBlock (functionDeclaration)* block '.' ;

varBlock : ('VAR' varDeclaration (';' varDeclaration)* ';')? ;
varDeclaration : IDENTIFIER (',' IDENTIFIER)* ':' TYPE;

functionDeclaration : functionHeading ';' varBlock block ';' ;
functionHeading : 'FUNCTION' IDENTIFIER ('(' varDeclaration (';' varDeclaration)* ')')? ':' TYPE ;

block : 'BEGIN' (statement ';')* 'END' ;
statement : 'IF' expression 'THEN' statement ('ELSE' statement)?
            | 'FOR' assignmentStatement ('TO' | 'DOWNTO') expression 'DO' statement
            | 'WHILE' expression 'DO' statement 
            | assignmentStatement
            | block 
            | 'BREAK' 
            | 'CONTINUE'
            |'READ' '(' IDENTIFIER (',' IDENTIFIER)* ')'
            |'WRITE' '(' expressionList ')'
            |IDENTIFIER '(' expressionList ')' ;

assignmentStatement : IDENTIFIER ('[' expression ']')? ':=' expression ;
expression : applicativeExpr (COMPARATOR applicativeExpr)? ;
applicativeExpr : (SIGN)? multiplicativeExpr (SIGN  multiplicativeExpr)? ;

multiplicativeExpr : term (OPERATOR term)* ;

term : NUMBER 
      | STRING_EXPRESSION
      | CHAR_EXPRESSION
      | IDENTIFIER ('[' expression ']')?
      | IDENTIFIER '(' expressionList ')'
      | '(' expression ')';

expressionList : expression (',' expression)* ;

OPERATOR : '*' | '/' | 'DIV' | 'MOD' ;
TYPE : 'INTEGER' | 'STRING' | 'CHAR' ;
COMPARATOR :  '>=' | '<=' | '=' | '<>' | '>' | '<' ;
SIGN : '+' | '-' ;
IDENTIFIER : [a-zA-Z][a-zA-Z0-9]* ;
NUMBER : [0-9]+ ;
STRING_EXPRESSION : '\''.*?'\'' ;
CHAR_EXPRESSION : '\''.'\'' ;
WS :  [ \t\r\n]+ -> skip ;