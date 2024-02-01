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

Linux ver.
```bash
# install Docker
curl -fsSL https://get.docker.com | sh
# install k8s
curl -sfL https://get.k3s.io | sh -s - --docker --disable=traefik --write-kubeconfig-mode=644
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
```bash
# Docker Desktop 용 ssh 연결 명령어
ssh 2024000001-test2024@localhost -p 2024
# 접속
password: test
# pod 교체 후 ssh 연결 오류 WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!
ssh-keygen -R [localhost]:2024
```