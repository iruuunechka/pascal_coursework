grammar Grammar;
program : 'PROGRAM' IDENTIFIER ';' varBlock (functionDeclaration)* block '.' ;

varBlock : ('VAR' varDeclaration (';' varDeclaration)* ';')? ;
varDeclaration : IDENTIFIER (',' IDENTIFIER)* ':' TYPE;

functionDeclaration : functionHeading ';' varBlock block ';' ;
functionHeading : 'FUNCTION' IDENTIFIER ('(' varDeclaration (';' varDeclaration)* ')')? ':' TYPE ;

block : 'BEGIN' (statement ';')* 'END' ;

ifStatement : 'IF' expression 'THEN' statement ('ELSE' statement)? ;
forStatement : 'FOR' assignmentStatement ('TO' | 'DOWNTO') expression 'DO' statement ;
whileStatement : 'WHILE' expression 'DO' statement ;
breakStatement : 'BREAK' ;
continueStatement : 'CONTINUE' ;
readStatement : 'READ' '(' IDENTIFIER (',' IDENTIFIER)* ')' ;
writeStatement : 'WRITE' '(' expressionList ')' ;
callStatement : IDENTIFIER '(' expressionList ')' ;
statement : ifStatement
            | forStatement
            | assignmentStatement
            | block 
            | whileStatement
            | breakStatement
            | continueStatement
            | readStatement
            | writeStatement
            | callStatement;

assignmentStatement : name ':=' expression ;
expression : applicativeExpr (COMPARATOR applicativeExpr)? ;
applicativeExpr : (SIGN)? multiplicativeExpr (SIGN  multiplicativeExpr)? ;

multiplicativeExpr : term (OPERATOR term)* ;

term : NUMBER 
      | STRING_EXPRESSION
      | CHAR_EXPRESSION
      | name
      | callStatement
      | '(' expression ')';

name : IDENTIFIER ('[' expression ']')? ;

expressionList : expression (',' expression)* ;

OPERATOR : '*' | '/' | 'MOD' ;
TYPE : 'INTEGER' | 'STRING' | 'CHAR' ;
COMPARATOR :  '>=' | '<=' | '=' | '<>' | '>' | '<' ;
SIGN : '+' | '-' ;
IDENTIFIER : [a-zA-Z][a-zA-Z0-9]* ;
NUMBER : [0-9]+ ;
CHAR_EXPRESSION : '\''.'\'' ;
STRING_EXPRESSION : '"'.*?'"' ;
WS :  [ \t\r\n]+ -> skip ;