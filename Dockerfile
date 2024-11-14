# 基于Java8镜像
FROM openjdk:21

 # 设置时区
ENV TZ="Asia/Shanghai"
#ENV JAVA_OPTS="-Xmx20g -Xms4g"

VOLUME /tmp
VOLUME /dbTemp
# 复制文件到容器
ADD /target/bm.jar app.jar

RUN bash -c 'touch /app.jar'
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

# 配置容器启动后执行的命令
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
