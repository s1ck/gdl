// Graph Definition Language
grammar GDL;

graph:
    (vertex NewLine)+
    EOF;

vertex
    : '(' vertexHeader ')'
    ;
vertexHeader
    : Variable? (Colon VertexLabel)?
    ;

VertexLabel
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

Colon
    : ':'
    ;

NewLine
    : '\r'? '\n'
    ;