@REM netstat -a
start cmd.exe @cmd /k "java -cp lib\\*;target\\classes rabbit.Z1_Consumer"
start cmd.exe @cmd /k "java -cp lib\\*;target\\classes rabbit.Z1_Producer"
@echo -------------
@REM netstat -a