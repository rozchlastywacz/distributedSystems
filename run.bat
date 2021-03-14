@REM netstat -a
start cmd.exe @cmd /k "cd .\target\classes && java chat.App"
start cmd.exe @cmd /k "cd .\target\classes && java chat.SRClient"
start cmd.exe @cmd /k "cd .\target\classes && java chat.SRClient"
@echo -------------
@REM netstat -a