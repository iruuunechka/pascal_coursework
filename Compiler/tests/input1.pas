PROGRAM test1;
VAR
i : INTEGER;
s : STRING;
BEGIN
  s := "asd";
  s[2] := 'b';
  i := 3;
  IF i > 5 THEN
    i := -5 * 3 + (-4 + 8) / 2
  ELSE
    i := 7;
  WRITE("i=", i);

  FOR i := 1 TO 5 DO
  BEGIN
    IF i = 4 THEN
      CONTINUE;
    s := s + "a";
  END;
  i := 1;
  WHILE i < 5 DO
  BEGIN
    s := s + "b";
    i := i + 1;
    IF i > 3 THEN
      BREAK;
  END;
  WRITE(s, i);
END.