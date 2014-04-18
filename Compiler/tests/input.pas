PROGRAM test;
VAR
i, j : INTEGER;
s, s1 : STRING;
c : CHAR;
FUNCTION add(i : INTEGER; c : CHAR) : INTEGER;
BEGIN
  s[i] := c;
  add := j + i;
END;
BEGIN
  s := "1234567";
  s := "123" + "67890";
  READ(s1);
  s1[0] := s[4];
  WRITE('a');
  WRITE(s1[0]);
  j := 7;
  WRITE(add(6, 'b'), s);
END.