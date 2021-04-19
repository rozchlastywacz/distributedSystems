
start cmd.exe @cmd /k "cd D:\Kody\semestr_6\SystemyRozproszone\lab3_zookeeper\apache-zookeeper-3.6.1-bin\bin & zkServer2.cmd \zoo1.cfg"
start cmd.exe @cmd /k "cd D:\Kody\semestr_6\SystemyRozproszone\lab3_zookeeper\apache-zookeeper-3.6.1-bin\bin & zkServer2.cmd \zoo2.cfg"
start cmd.exe @cmd /k "cd D:\Kody\semestr_6\SystemyRozproszone\lab3_zookeeper\apache-zookeeper-3.6.1-bin\bin & zkServer2.cmd \zoo3.cfg"
start cmd.exe @cmd /k "cd D:\Kody\semestr_6\SystemyRozproszone\lab3_zookeeper\apache-zookeeper-3.6.1-bin\bin & zkCli.cmd -server 127.0.0.1:2182"