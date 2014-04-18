PROGRAM test2;
VAR
 i, j : INTEGER;
 a , b: STRING;

BEGIN
   a := "ab";
   a := a + a;
   WRITE(a);
   a[2] := 'u';
   b := a + a;
   b[3] := a[2];
   WRITE(b);

END.