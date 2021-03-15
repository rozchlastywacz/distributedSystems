@REM netstat -a
start cmd.exe @cmd /k "cd .\target\classes && java chat.App"
start cmd.exe @cmd /k "cd .\target\classes && java chat.Client"
start cmd.exe @cmd /k "cd .\target\classes && java chat.Client"
@echo -------------
@REM netstat -a