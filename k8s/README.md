## 📃 환경 설명

### 개발 환경

- Kubernetes v1.28.2 (Docker Desktop)  
- Windows10

### 배포 환경

- k3s
- Ubuntu 22.04  

## 🔨 환경 구축

### K8S 설치

windows ver.
- Docker Desktop 설치 후 k8s 설정 등록

Linux - container-d ver.
```bash
# install k8s
curl -sfL https://get.k3s.io | sh -s - --disable=traefik --write-kubeconfig-mode=644

# 헬름을 위해 k3s 설정 파일 추출하기
kubectl config view --raw > ~/.kube/config
chmod 600 ~/.kube/config
```

Linux - docker ver. (현재 오류 발생)
```bash
# install Docker
curl -fsSL https://get.docker.com | sh
# docker 권한 설정
sudo usermod -aG docker $USER
newgrp docker
sudo chown $USER:docker /var/run/docker.sock

# install k8s
curl -sfL https://get.k3s.io | sh -s - --docker --disable=traefik --write-kubeconfig-mode=644

# 헬름을 위해 k3s 설정 파일 추출하기
kubectl config view --raw > ~/.kube/config
chmod 600 ~/.kube/config
```

### Helm 설치

windows ver.
```powershell
# 초콜레티 설치
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
# 헬름 설치
choco install -y kubernetes-helm
# 헬름 버전 확인
helm version
```

Linux ver.
```bash
# 스크립트를 사용한 헬름 설치
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash
# 헬름 버전 확인
helm version
```

### 영구 볼륨을 위한 노드 레이블 배치 (정보 저장용)
```bash
# 클러스터의 특정 노드에 레이블 부여
kubectl label node $(kubectl get nodes -o jsonpath='{.items[0].metadata.name}') storage=dev-room-pv
# 레이블 셀렉터로 노드의 존재 확인
kubectl get nodes -l storage=dev-room-pv
```

### 토큰 생성 및 연결
```bash
# 기본 서비스 어카운트용 토큰을 보관할 시크릿을 생성한다.
kubectl apply -f - <<EOF
apiVersion: v1
kind: Secret
metadata:
  name: default-token
  annotations:
    kubernetes.io/service-account.name: default
type: kubernetes.io/service-account-token
EOF

# 토큰 컨트롤러가 해당 시크릿에 토큰을 기록할 때까지 기다린다.
while ! kubectl describe secret default-token | grep -E '^token' >/dev/null; do
  echo "waiting for token..." >&2
  sleep 1
done

# default 계정 권한 설정
kubectl create clusterrolebinding default-cluster-admin --clusterrole cluster-admin --serviceaccount default:default

# 토큰 값을 얻는다
TOKEN=$(kubectl get secret default-token -o jsonpath='{.data.token}' | base64 --decode)
echo $TOKEN > test_token

# linux 기준
curl -k -H "Authorization: Bearer $TOKEN" https://114.200.134.130:6443/api/

# windows 기준
set TOKEN=위의 파일 내용
curl -k -H "Authorization: Bearer %TOKEN%" https://114.200.134.130:6443/api/

# get pods 테스트
curl -v -k -X GET -H "Authorization: Bearer %TOKEN%" https://114.200.134.130:6443/api/v1/namespaces/default/pods
```

## 🚀 Helm 차트 실행
```bash
# 차트에 들어갈 파일의 유효성을 검증
helm lint dev-room-k8s
# 차트 디렉터리에서 릴리스를 설치
helm install dev-room-k8s dev-room-k8s/
# 설치된 릴리스의 상태를 확인
helm ls
```

### Helm 차트 제거
```bash
helm uninstall $(helm ls -q)
```

### 기타 명령어 목록

#### vscode-server 접속

브라우저로 다음 링크에 접속
[114.200.134.130:2024](114.200.134.130:2024)

#### ssh 접속 (현재는 vscode-server로 대체)
```bash
# pod 교체 후 ssh 연결 오류 WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!
ssh-keygen -R [localhost]:2024
ssh-keygen -R [114.200.134.130]:2024

# ssh 연결 명령어
ssh 2024000001-test2024@localhost -p 2024
ssh 2024000001-test2024@114.200.134.130 -p 2024

# 접속 비밀번호
password: test

# mini pc 테스트용
ssh hdyang@114.200.134.130 -p 2019
ssh hdyang@192.168.35.100 -p 2019
sudo shutdown -h now
```

#### 파드 직접 접속 & 제거
```bash
kubectl exec -it <pod 이름> -- /bin/bash
kubectl delete pod <pod 이름> --force --grace-period=0
```

#### k3s 제거
```bash
# k3s 제거
/usr/local/bin/k3s-uninstall.sh

# 파드 확인
kubectl get pods
```