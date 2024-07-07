#!/bin/bash

: '
chmod +x easy_erase.sh
'

# 사용자로부터 키워드 입력 받기
read -p "삭제할 키워드를 입력하세요: " keyword

# 입력된 키워드를 포함하는 서비스 삭제
kubectl get svc | grep $keyword | awk '{print $1}' | xargs -r kubectl delete svc --force --grace-period=0

# 입력된 키워드를 포함하는 컨피그맵 삭제
kubectl get cm | grep $keyword | awk '{print $1}' | xargs -r kubectl delete cm --force --grace-period=0

# 입력된 키워드를 포함하는 디플로이먼트 삭제
kubectl get deploy | grep $keyword | awk '{print $1}' | xargs -r kubectl delete deploy --force --grace-period=0

# 입력된 키워드를 포함하는 파드 삭제
kubectl get pods | grep $keyword | awk '{print $1}' | xargs -r kubectl delete pod --force --grace-period=0
