#!/bin/bash

student_id=${student}
class_id=${class}
username="$student_id"-"$class_id"
student_folder_path="/home/$username"

# vscode-server 설치 및 실행
sudo -u $username curl -fsSL https://code-server.dev/install.sh | sh
mkdir -p $student_folder_path/.config/code-server
cat > $student_folder_path/.config/code-server/config.yaml <<EOF
bind-addr: 0.0.0.0:8080
auth: password
password: test
cert: false
app-name: DEV-ROOM
welcome-text: 초기 비밀번호는 test 입니다
disable-workspace-trust: true
EOF

sudo -u $username code-server --open $student_folder_path