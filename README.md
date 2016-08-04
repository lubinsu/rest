# Rest
基于Spary的Scala REST服务接口样例

## 部署脚本
/appl/scripts/deploy.sh

\#!/bin/bash
source ~/.bash_profile

hosts=`echo "$1" | awk -F: '{print $1}'`
port=`echo "$1" | awk -F: '{print $2}'`
export SLAVES="$hosts"
export USER='hadoop'

for LINE in `echo $SLAVES | tr " " "\n"`
do
ssh "$USER@$LINE" "rm -rf /appl/scripts/e-business/rest/"
ssh "$USER@$LINE" "mkdir -p /appl/scripts/e-business/rest/"

scp -r /appl/scripts/e-business/rest/* "$USER@$LINE:/appl/scripts/e-business/rest/"

ssh "$USER@$LINE" "rm -rf /appl/conf/*"
scp -r /appl/conf/* "$USER@$LINE:/appl/conf/"
echo "finished $USER@$LINE"
done

将项目部署到各个服务器中
/appl/scripts/deploy.sh "bigdata1:22 bigdata2:22 bigdata3:22 bigdata4:22 bigdata6:22 hadoop.slave2:11017"

## 启动Web端
nohup java -Djava.ext.dirs=/appl/scripts/e-business/rest/target/lib -classpath /appl/scripts/e-business/rest/target/rest-1.0-SNAPSHOT.jar com.eweise.api.ApiBoot hadoop.slave2 9993 &

## 启动其他服务节点
nohup java -Djava.ext.dirs=/appl/scripts/e-business/rest/target/lib -classpath /appl/scripts/e-business/rest/target/rest-1.0-SNAPSHOT.jar com.eweise.service.BackendServiceBoot 4444 &

## 测试结果
curl "http://172.19.0.30:9993/userlabel?userId=5236317&labelCode=coach_spot_class"

{"userId":"5236317","labels":"10,13,6,1,3","errorCode":0}