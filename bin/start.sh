#!/bin/sh

echo "start datalinkx-job..."
nohup java -jar -Xmx256m -Xms125m -Duser.timezone=Asia/Shanghai /datalinkx/datalinkx-job/target/datalinkx-job*.jar --spring.config.location=/datalinkx/datalinkx-job/config/  > /dev/null 2>&1 &

echo "start datalinkx-server..."
java -jar -Xmx256m -Xms125m -Duser.timezone=Asia/Shanghai /datalinkx/datalinkx-server/target/datalinkx-server*.jar --spring.config.location=/datalinkx/datalinkx-server/config/