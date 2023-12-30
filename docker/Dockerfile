FROM maven:3.8.7-eclipse-temurin-19

RUN apt update && apt install git

#切换目录，指定源码克隆后保存位置
WORKDIR /

#拉取源代码
RUN git clone https://gitee.com/atuptown/datalinkx.git /datalinkx \
    && cd /datalinkx \
    && cp docker/settings.xml /usr/share/maven/conf \
    && mvn clean install -U -Dmaven.test.skip=true

#切换jar目录
WORKDIR /datalinkx

#挂载配置映射路径
VOLUME /datalinkx/datalinkx-server/config
VOLUME /datalinkx/datalinkx-job/config

#声明项目所用端口
EXPOSE 12345

#启动java项目程序
CMD sh /datalinkx/bin/start.sh