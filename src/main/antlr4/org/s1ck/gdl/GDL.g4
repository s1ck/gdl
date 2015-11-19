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
    : (element SEMICOLON?)*
    ;

element
    : graph
    | path
    ;

graph
    : header properties? '[' (path SEMICOLON?)+ ']'
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
    : Identifier? Label?
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
    | FloatLiteral
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

FloatLiteral
    : IntegerLiteral? '.' DIGIT*     // match 0.1, 1.0, 1., 3.14 etc.
    ;

Label
    : COLON UpperCaseLetter (LowerCaseLetter | UpperCaseLetter)*  // graph and vertex label (e.g. Person, BlogPost)
    | COLON LowerCaseLetter (LowerCaseLetter | UpperCaseLetter)*  // edge label (e.g. knows, hasInterest)
    ;

Identifier
    : LowerCaseLetter Characters?   // e.g. g0, alice, birthTown
    ;

fragment
Characters
    : Character+
    ;

fragment
Character
    : UpperCaseLetter
    | LowerCaseLetter
    | DIGIT
    ;

fragment
UpperCaseLetters
    : UpperCaseLetter+
    ;

fragment
UpperCaseLetter
    : [A-Z]
    ;

fragment
LowerCaseLetters
    : LowerCaseLetter+
    ;

fragment
LowerCaseLetter
    : [a-z]
    ;

fragment
DIGIT
    : [0-9]
    ;

fragment
NONZERODIGIT
    : [1-9]
    ;

fragment
UNDERSCORE
    : '_'
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

WS
    : [ \t\n\r]+ -> skip
    ;

COMMENT
    : '/*' .*? '*/' -> skip
    ;

LINE_COMMENT
    : '//' ~[\r\n]* -> skip
    ;