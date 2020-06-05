/*
 * Copyright 2017 The GDL Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    : Identifier? label*
    ;

properties
    : '{' (property (',' property)*)? '}'
    ;

property
    : Identifier Colon literal
    ;

label
    : Colon Identifier
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
     | timeFunc
     | temporalComparison
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
    | NaN
    | Null
    ;
    
    
//------------------------
// time-related 
//________________________

// temporal query constraints

temporalComparison
    : timePoint ComparisonOP timePoint
    ;

timeFunc
    : interval '.' intervalFunc             #intvF
    | timePoint '.' stampFunc                 #stmpF
    | Identifier '.asOf(' timePoint ')'   #asOf
    ;

// intervals

interval
    : intervalSelector
    | intervalFromStamps
    | complexInterval
    ;

intervalSelector
    : Identifier '.' IntervalConst
    | IntervalConst
    ;

intervalFromStamps
    : 'Interval(' timePoint ',' timePoint ')'
    ;

complexInterval
    : complexIntervalArgument '.merge(' complexIntervalArgument ')'
    | complexIntervalArgument '.join(' complexIntervalArgument ')'
    ;

complexIntervalArgument
    : intervalSelector
    | intervalFromStamps
    ;

// time points

timePoint
    : timeLiteral
    | timeSelector
    | complexTimePoint
    ;

timeLiteral
    : Datetime
    | Date
    | Time
    | IntegerLiteral
    | Now;

timeSelector
    : Identifier '.' TimeProp
    | TimeProp
    ;

complexTimePoint
    : 'MAX(' complexTimePointArgument (',' complexTimePointArgument)+ ')'
    | 'MIN(' complexTimePointArgument (',' complexTimePointArgument)+ ')'
    ;

complexTimePointArgument
    : timeLiteral
    | timeSelector
    ;


// interval functions

intervalFunc
            : overlapsIntervallOperator
            | fromToOperator
            | betweenOperator
            | precedesOperator
            | succeedsOperator
            | containsOperator
            | immediatelyPrecedesOperator
            | immediatelySucceedsOperator
            | equalsOperator
            | longerThanOperator
            | shorterThanOperator
            | lengthAtLeastOperator
            | lengthAtMostOperator
            ;

overlapsIntervallOperator
    : 'overlaps(' interval ')'
    ;

fromToOperator
    : 'fromTo(' timePoint ',' timePoint ')'
    ;

betweenOperator
    : 'between(' timePoint ',' timePoint ')'
    ;

precedesOperator
    : 'precedes(' interval ')'
    ;

succeedsOperator
    : 'succeeds(' interval ')'
    ;

containsOperator
    : 'contains(' interval ')'
    | 'contains(' timePoint ')'
    ;

immediatelyPrecedesOperator
    : 'immediatelyPrecedes(' interval ')'
    ;

immediatelySucceedsOperator
    : 'immediatelySucceeds(' interval ')'
    ;

equalsOperator
    : 'equals(' interval ')'
    ;

longerThanOperator
    : 'longerThan(' (interval | timeConstant) ')'
    ;

shorterThanOperator
    : 'shorterThan(' (interval | timeConstant) ')'
    ;

lengthAtLeastOperator
    : 'lengthAtLeast(' (interval | timeConstant) ')'
    ;

lengthAtMostOperator
    : 'lengthAtMost(' (interval | timeConstant) ')'
    ;

timeConstant
    : 'Millis(' IntegerLiteral ')'
    | 'Seconds(' IntegerLiteral ')'
    | 'Minutes(' IntegerLiteral ')'
    | 'Hours(' IntegerLiteral ')'
    | 'Days(' IntegerLiteral ')'
    ;


// time stamp/point functions

stampFunc
    : beforePointOperator
    | afterPointOperator
    | precedesOperator
    | succeedsOperator
    ;

beforePointOperator
    : 'before' '(' timePoint ')'
    ;

afterPointOperator
    : 'after' '(' timePoint ')'
    ;


//-------------------------------
// String Literal
//-------------------------------
StringLiteral
    : '"' ('\\"'|.)*? '"'
    | '\'' ('\\\''|.)*? '\''
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
    | '<>'
    | '>'
    | '<'
    | '>='
    | '<='
    ;

//_______________________________
// Time lexing 
//_______________________________
TimeProp
    : 'tx_from'
    | 'tx_to'
    | 'val_from'
    | 'val_to'
    ;

IntervalConst
    : 'tx'
    | 'val'
    ;

Datetime
    :  Digit Digit Digit Digit '-' Digit Digit '-' Digit Digit 'T'Time
    ;

Date
    : Digit Digit Digit Digit '-' Digit Digit('-' Digit Digit)?
    ;

Time
    : Digit Digit ':' Digit Digit (':' Digit Digit)?
    ;

Now
    : ('N'|'n')('O'|'o')('W'|'w')
    ;

//-------------------------------
// General fragments
//-------------------------------

NaN
    : 'NaN'
    ;

Null
    : 'NULL'
    ;
//-------------------------------
// Identifier
//-------------------------------

Identifier
    : (UnderScore | LowerCaseLetter | UpperCaseLetter) (UnderScore | Character)*   // e.g. _temp, _0, t_T, g0, alice, birthTown
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

