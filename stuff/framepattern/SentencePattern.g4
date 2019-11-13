grammar SentencePattern ;

patterns  : pattern (NEWLINE pattern)* ;

pattern  : element ;

element : annotation  # element_annotation
        | token       # element_token
        ;

annotation : (LABEL ':')? '#' NAME ('<' element '>')? ;

token : (LABEL ':')? '[' token_cond ']' ;

token_cond : 'pos' '=' STR  # token_cond_pos ;

NEWLINE : [\r\n]+ ;
SPACE   : [ ]+ ;
NAME    : [a-z0-9_]+ ;
LABEL   : [a-z0-9_]+ ;
STR     : '"' (~["])+ '"' ;