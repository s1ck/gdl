// Graph Definition Language
grammar GDL;

// starting point for parsing a GDL script
database
    : elementList EOF
    ;

elementList
    : (element SEMICOLON?)*
    ;

element
    : path
    | graph
    ;

graph
    : header properties? '[' (path)+ ']'
    ;

path
    : vertex (edge vertex)*
    ;

vertex
    : '(' header properties? ')'
    ;

edge
    : '<-' edgeBody? '-'    #incomingEdge
    | '-' edgeBody? '->'    #outgoingEdge
    ;

edgeBody
    : '[' header properties? ']'
    ;

header
    : Identifier? (COLON Label)?
    ;

properties
    : '{' (property (',' property)*)? '}'
    ;

property
    : Identifier EQUALS literal
    ;

literal
    : StringLiteral
    | BooleanLiteral
    | IntegerLiteral
    ;

StringLiteral
    : '"' ('\\"'|.)*? '"'
    ;

BooleanLiteral
    : 'true'
    | 'false'
    ;

IntegerLiteral
    : '0'
    | '-'? NONZERODIGIT DIGIT*
    ;

Label
    : UpperCaseLetter LowerCaseLetters?                 // graph and vertex label
    | UpperCaseLetter (UpperCaseLetter | UNDERSCORE)*   // edge label
    ;

Identifier
    : Characters
    ;

Characters
    : Character+
    ;

Character
    : UpperCaseLetter
    | LowerCaseLetter
    | DIGIT
    ;

UpperCaseLetters
    : UpperCaseLetter+
    ;

UpperCaseLetter
    : [A-Z]
    ;

LowerCaseLetters
    : LowerCaseLetter+
    ;

LowerCaseLetter
    : [a-z]
    ;

DIGIT
    : [0-9]
    ;

NONZERODIGIT
    : [1-9]
    ;

COLON
    : ':'
    ;

SEMICOLON
    : ';'
    ;

EQUALS
    : '='
    ;

UNDERSCORE
    : '_'
    ;

WS
    : [ \t\n\r]+ -> skip
    ;

COMMENT
    : '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;