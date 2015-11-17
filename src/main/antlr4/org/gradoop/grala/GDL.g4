// Graph Definition Language
grammar GDL;

// starting point for parsing a GDL script
database
    : graph+ EOF
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
    : Variable? (Colon Label)?
    ;

properties
    : '{' (property (',' property)*)? '}'
    ;

property
    : Variable ':' literal
    ;

literal
    : StringLiteral
    | BooleanLiteral
    | IntegerLiteral
    ;

StringLiteral
    : '\"' Character* '\"'
    ;

BooleanLiteral
    : 'true'
    | 'false'
    ;

IntegerLiteral
    : '0'
    | NonZeroDigit Digit*
    ;

Label
    : UpperCaseLetter Character*
    ;

Variable
    : Character+
    ;

Character
    : UpperCaseLetter
    | LowerCaseLetter
    | Digit
    ;

UpperCaseLetter
    : [A-Z]
    ;

LowerCaseLetter
    : [a-z]
    ;

Digit
    : [0-9]
    ;

NonZeroDigit
    : [1-9]
    ;

Colon
    : ':'
    ;

WS
    : [ \t\n\r]+ -> skip
    ;