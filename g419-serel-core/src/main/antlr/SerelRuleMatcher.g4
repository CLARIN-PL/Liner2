grammar SerelRuleMatcher;

start  :
     expression  EOF
  ;


expression
  :
   semRelName? leftExpression? token rightExpression?
   ;


semRelName : id '::' ;

depRelValue : id ;

depRel : '(' depRelValue ')';
leftEdge : depRel?  '>'  ;
rightEdge : '<' depRel? ;

namedEntity : id;
role : id ;
text : id;
namedEntityToRole: namedEntity ':' role ;
element : text ('/' namedEntityToRole)? ;

xPosValue : id ;
xPos : '[' xPosValue ']' ;
token:  xPos? element;

leftExpression
   :
    leftExpression token leftEdge
    |
    token leftEdge
   ;

rightExpression
   :
    rightEdge token rightExpression
    |
    rightEdge token
   ;

id : STAR | (LEMMA)? IDENTIFIER ;


STAR : '*' ;
LEMMA :'^' ;
IDENTIFIER : [a-zA-Z0-9]+;

WS : [ \t\r\n]+ -> skip;
//NewLine : ('\r'?'\n'|'\r') -> skip;
