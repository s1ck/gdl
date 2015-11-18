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
    : path
    | graph
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