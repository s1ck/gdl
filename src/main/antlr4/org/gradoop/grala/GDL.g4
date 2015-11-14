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
    : incomingEdge
    | outgoingEdge
    ;

incomingEdge
    : '<-' edgeBody? '-'
    ;

outgoingEdge
    : '-' edgeBody? '->'
    ;

edgeBody
    : '[' header properties? ']'
    ;

header
    : Variable? (COLON Label)?
    ;

properties
    : '{' (property (',' property)*)? '}'
    ;

property
    : Variable ':' Value
    ;

Value
    : StringValue
    | BooleanValue
    | NumberValue
    ;

StringValue
    : '\"' Character* '\"'
    ;

BooleanValue
    : 'true' | 'false'
    ;

NumberValue
    : Digit+
    ;

//properties
//    : '{' property* '}'
//    ;
//
//property
//    : PropertyKey ':' PropertyValue
//    ;
//
//PropertyKey
//    : (LowerCaseLetter | UpperCaseLetter) Character*
//    ;
//
//PropertyValue
//    : '\"' Character+ '\"'
//    ;

Label
    : UpperCaseLetter Character*
    ;

Variable
    : (UpperCaseLetter | LowerCaseLetter)+
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

COLON
    : ':'
    ;

SEMICOLON
    : ';'
    ;

WS
    :   [ \t\n\r]+ -> skip
    ;