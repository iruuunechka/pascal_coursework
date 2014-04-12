grammar Grammar;
program : 'PROGRAM' IDENTIFIER ';' var_block function_list block '.' ;

var_block : ('VAR' var_list ';')? ;
var_list : var_declaration (';' var_declaration)* ;
var_declaration : IDENTIFIER (',' IDENTIFIER)* ':' TYPE;

function_list : (function_declaration)* ;
function_declaration : function_heading ';' var_block block ';' ;
function_heading : 'FUNCTION' IDENTIFIER parameter_list ':' TYPE ;
parameter_list : ('(' var_declaration (';' var_declaration)* ')')? ;

block : 'BEGIN' (statement ';')* 'END' ;
statement : 'IF' expression 'THEN' statement ('ELSE' statement)?
            | 'FOR' assignment_statement ('TO' | 'DOWNTO') expression 'DO' statement 
            | 'WHILE' expression 'DO' statement 
            | assignment_statement
            | block 
            | 'BREAK' 
            | 'CONTINUE'
            |'READ' '(' IDENTIFIER (',' IDENTIFIER)* ')'
            |'WRITE' '(' expression_list ')'
            |IDENTIFIER '(' expression_list ')' ;

assignment_statement : IDENTIFIER ('[' expression ']')? ':=' expression ;
expression : applicative_expr (COMPARATOR applicative_expr)? ;
applicative_expr : (SIGN)? multiplicative_expr ((SIGN | 'OR') multiplicative_expr)? ;

multiplicative_expr : term (OPERATOR term)* ;

term : NUMBER 
      | STRING_EXPRESSION
      | CHAR_EXPRESSION
      | IDENTIFIER ('[' expression ']')?
      | IDENTIFIER '(' expression_list ')'
      | '(' expression ')'
      | 'NOT' term ;

expression_list : expression (',' expression)* ;

OPERATOR : '*' | '/' | 'DIV' | 'MOD' | 'AND' ;
TYPE : 'INTEGER' | 'STRING' | 'CHAR' ;
COMPARATOR :  '>=' | '<=' | '=' | '<>' | '>' | '<' ;
SIGN : '+' | '-' ;
IDENTIFIER : [a-zA-Z][a-zA-Z0-9]* ;
NUMBER : [0-9]+ ;
STRING_EXPRESSION : '\''.*?'\'' ;
CHAR_EXPRESSION : '\''.'\'' ;
WS :  [ \t\r\n]+ -> skip ;