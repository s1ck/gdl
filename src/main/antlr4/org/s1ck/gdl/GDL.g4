/*
 * This file is part of GDL.
 *
 * GDL is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GDL is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GDL.  If not, see <http://www.gnu.org/licenses/>.
 */

// Graph Definition Language
grammar GDL;

// starting point for parsing a GDL script
database
    : elementList EOF
    ;

elementList
    : (definition ','?)+ | query
    ;

definition
    : graph
    | path
    ;

graph
    : header properties? (('[' (path ','?)* ']'))
    ;

query
    : match where*
    ;

match
    : 'MATCH' (path ','?)+
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
    : '[' header properties? edgeLength?']'
    ;

edgeLength
    : '*' IntegerLiteral? ('..' IntegerLiteral)?
    ;

header
    : Identifier? label?
    ;

properties
    : '{' (property (',' property)*)? '}'
    ;

property
    : Identifier Colon literal
    ;

label
    : Colon (Characters | Identifier)
    ;

where
    : ('where' | 'WHERE') expression
    ;

expression : xorExpression ;

xorExpression: andExpression ( XOR andExpression )* ;

andExpression: orExpression ( AND orExpression )* ;

orExpression: notExpression ( OR notExpression )* ;

notExpression : ( NOT )* expression2 ;

expression2 : atom ;

atom : parenthesizedExpression
     | comparisonExpression
     ;

comparisonExpression
    : comparisonElement ComparisonOP comparisonElement
    ;

comparisonElement
    : Identifier
    | propertyLookup
    | literal
    ;

parenthesizedExpression : '(' expression ')' ;

propertyLookup
    : Identifier '.' Identifier
    ;

literal
    : StringLiteral
    | BooleanLiteral
    | IntegerLiteral
    | FloatingPointLiteral
    | Null
    ;

//-------------------------------
// String Literal
//-------------------------------
StringLiteral
    : '"' ('\\"'|.)*? '"'
    ;

//-------------------------------
// Boolean Literal
//-------------------------------
BooleanLiteral
    : 'true'
    | 'TRUE'
    | 'false'
    | 'FALSE'
    ;

//-------------------------------
// Integer Literal
//-------------------------------
IntegerLiteral
    : DecimalIntegerLiteral
    ;

fragment
DecimalIntegerLiteral
    : DecimalNumeral IntegerTypeSuffix?
    ;

fragment
DecimalNumeral
    : '0'
    | '-'? NonZeroDigit Digit*
    ;
fragment
IntegerTypeSuffix
    : [lL]
    ;

//-------------------------------
// Floating Point Literal
//-------------------------------
FloatingPointLiteral
    :   DecimalFloatingPointLiteral
    ;

fragment
DecimalFloatingPointLiteral
    :   (DecimalFloatingPointNumeral '.' Digits)
    |   (DecimalFloatingPointNumeral? '.' Digits)  FloatTypeSuffix?
    ;

fragment
DecimalFloatingPointNumeral
    : '0'
    | '-'? Digits
    ;

fragment
FloatTypeSuffix
    :   [fFdD]
    ;

//-------------------------------
// Identifier
//-------------------------------


Identifier
    : (UnderScore | LowerCaseLetter) (UnderScore | Character)*   // e.g. _temp, _0, t_T, g0, alice, birthTown
    ;

//-------------------------------
// Comparison
//-------------------------------


AND
    : ('a'|'A')('n'|'N')('d'|'D')
    ;
OR
    : ('o'|'O')('r'|'R')
    ;
XOR
    : ('x'|'X')('o'|'O')('r'|'R')
    ;

NOT
    : ('N'|'n')('o'|'O')('t'|'T')
    ;

ComparisonOP
    : '='
    | '!='
    | '>'
    | '<'
    | '>='
    | '<='
    ;


//-------------------------------
// General fragments
//-------------------------------

Null
    : 'NULL'
    ;

Characters
    : Character+
    ;

Character
    : UpperCaseLetter
    | LowerCaseLetter
    | Digit
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

fragment
Digits
    : Digit+
    ;

fragment
Digit
    : [0-9]
    ;

fragment
NonZeroDigit
    : [1-9]
    ;

fragment
UnderScore
    : '_'
    ;


Colon
    : ':'
    ;


PERIOD
    : '.'
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

