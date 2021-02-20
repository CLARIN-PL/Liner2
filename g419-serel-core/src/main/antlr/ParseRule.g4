grammar ParseRule;

start  :
   semRel?   expression  EOF
  ;
semRel : semRelName '::' ;

semRelName : id ;

expression
  :
   rootLeftExpression? rootNode rootRightExpression?
   ;

rootNode: node ;
rootLeftExpression : leftExpression ;
rootRightExpression : rightExpression ;

leftExpression
   :
    leftExpression node leftEdge
    |
    node leftEdge
   ;

rightExpression
   :
    rightEdge node rightExpression
    |
    rightEdge node
   ;


leftEdge : depRel?  '>'  ;
rightEdge : '<' depRel? ;
depRel : '(' depRelValue ')';
depRelValue : id ;

node:  xPos? element;
xPos : '[' xPosValue ']' ;
xPosValue : id ;
element : text ('/' namedEntityToRole)? ;
namedEntityToRole: namedEntity toRole? ;
toRole : ':' role ;
namedEntity : id;
role : id ;
text : id;
functionName: IDENTIFIER;










id : STAR | (LEMMA | (LEMMA functionName ':'))? IDENTIFIER+ ;


STAR : '*' ;
LEMMA :'^' ;
IDENTIFIER : [a-zA-Z0-9_ĄąĆćĘęŁłŃńÓóŚśŻżŹź]+;

WS : [ \t\r\n]+ -> skip;
//NewLine : ('\r'?'\n'|'\r') -> skip;
