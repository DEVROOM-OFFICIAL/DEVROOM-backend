package com.devlatte.devroom.k8s.api;

import com.devlatte.devroom.k8s.api.basic.*;
import com.devlatte.devroom.k8s.utils.FreemarkerTemplate;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import freemarker.template.TemplateException;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class ClassApi extends K8sApiBase {
    private final DeployApi deployApi;
    private final ConfigMapApi configMapApi;
    private final ServiceApi serviceApi;
    private final ExecApi execApi;
    private final String pvHostPath;
    private final String pvStudentPath;
    private final String cmdServerLabel;
    private final String pvTaPath;

    public ClassApi(DeployApi deployApi,
                    ExecApi execApi,
                    ConfigMapApi configMapApi,
                    ServiceApi serviceApi,
                    @Value("${config.kubernetes.pvHostPath}") String pvHostPath,
                    @Value("${config.kubernetes.pvStudentPath}") String pvStudentPath,
                    @Value("${config.kubernetes.pvTaPath}") String pvTaPath,
                    @Value("${config.kubernetes.cmdServerLabel}") String cmdServerLabel,
                    @Value("${config.kubernetes.url}") String apiServer,
                    @Value("${config.kubernetes.token}") String apiToken
    ) {
        super(apiServer, apiToken);
        this.execApi = execApi;
        this.deployApi = deployApi;
        this.configMapApi = configMapApi;
        this.serviceApi = serviceApi;
        this.pvHostPath = pvHostPath;
        this.pvStudentPath = pvStudentPath;
        this.pvTaPath = pvTaPath;
        this.cmdServerLabel = cmdServerLabel;
    }

    public String create(String className, Map<String, String> studentId2Port, Map<String, String> options, String[] command, String customScript) {
        Map<String, List<String>> successMap = new HashMap<>();
        List<String> successId = new ArrayList<>();
        HashMap<String, HashMap<String, String>> errorMap = new HashMap<>();
        HashMap<String, String> errorList = new HashMap<>();
            // 관리자용 추가
            // studentIds.addLast("ta");

            for (Map.Entry<String, String> entry : studentId2Port.entrySet()) {

                String studentId = entry.getKey();
                String port = entry.getValue();
                String idName = "id-"+studentId+"-"+className;

                Map<String, String> labels = new HashMap<>();
                labels.put("class_id", "id-"+className);
                labels.put("student_id", "id-"+studentId);

                // 영구볼륨 경로 확인. 없을 시 생성. ta는 별개의 폴더에 저장.
                try {
                    String path = Objects.equals(studentId, "ta") ?
                            "/host/"+pvHostPath+"/"+pvTaPath+"/"+className :
                            "/host/"+pvHostPath+"/"+pvStudentPath+"/"+studentId;
                    execApi.run("app", cmdServerLabel, new String[]{"/bin/sh", "-c", "mkdir "+path});
                } catch (KubernetesClientException | IOException | InterruptedException e) {
                    errorList.put(idName+"-execApi", e.getMessage());
                }
                // 컨피그맵 생성
                try {
                    createConfingMap(className, studentId, options, labels, customScript);
                } catch (KubernetesClientException | IOException | TemplateException e) {
                    errorList.put(idName+"-confingMap", e.getMessage());
                }
                // 서비스 생성
                try {
                    createService(className, studentId, port, options, labels);
                } catch (KubernetesClientException e) {
                    errorList.put(idName+"-service", e.getMessage());
                }
                // 디플로이 생성
                try {
                    createDeploy(className, studentId, options, labels, command);
                } catch (KubernetesClientException | IOException | TemplateException e) {
                    errorList.put(idName+"-deploy", e.getMessage());
                }
                successId.addLast(studentId);
            }

        if (!errorList.isEmpty()) {
            errorMap.put("error", errorList);
            return gson.toJson(errorMap);
        }

        successMap.put("success", successId);
        return gson.toJson(successMap);
    }
    private void createService(String className, String studentId, String port, Map<String, String> options, Map<String, String> labels) {

        String exPort = port;
        String inPort = "80";
        // vscode server 용 포트개방
        if (options.containsKey("vscode") && options.get("vscode").equals("yes"))
            inPort = "8080";
        // ssh 용 포트개방
        if (options.containsKey("ssh") && options.get("ssh").equals("yes"))
            inPort = "22";

        String result = serviceApi.createService(
                "id-"+studentId+"-"+className,
                options.getOrDefault("selector", "id-"+studentId+"-"+className),
                exPort,
                inPort,
                labels
        );

        JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
        if (jsonObject.has("error")) {
            throw new KubernetesClientException(String.valueOf(jsonObject.get("error")));
        }
    }
    private void createConfingMap(String className, String studentId, Map<String, String> options, Map<String, String> labels, String customScript) throws TemplateException, IOException {

        Map<String, String> data = new HashMap<>();
        Map<String, String> template = new HashMap<>();

        template.put("student", studentId);
        template.put("class", className);

        // 기본 유저 생성 스크립트 넣기
        data.put("ubuntu_init.sh",FreemarkerTemplate.convert("/scripts/", "ubuntu_init.sh", template));

        // ssh 설치용 스크립트
        if (options.containsKey("ssh") && options.get("ssh").equals("yes"))
            data.put("01_install_ssh.sh",FreemarkerTemplate.convert("/scripts/", "01_install_ssh.sh", template));

        // vscode 설치용 스크립트
        if (options.containsKey("vscode") && options.get("vscode").equals("yes"))
            data.put("02_install_vscode.sh",FreemarkerTemplate.convert("/scripts/", "02_install_vscode.sh", template));

        // 커스텀 스크립트
        if (!customScript.isEmpty())
            data.put("00_custom_script.sh",customScript);

        String result = configMapApi.createConfigMap(
                "id-"+studentId+"-"+className+"-config",
                labels,
                data
        );

        JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
        if (jsonObject.has("error")) {
            throw new KubernetesClientException(String.valueOf(jsonObject.get("error")));
        }

    }
    private void createDeploy(String className, String studentId, Map<String, String> options, Map<String, String> labels, String[] command) throws TemplateException, IOException {

        Map<String, String> template = new HashMap<>();
        String cmd = FreemarkerTemplate.convert("/scripts/", "ubuntu_command.sh", template);

        String[] defaultCmd = (command != null && command.length > 0) ? command : new String[]{
                "/bin/sh",
                "-c",
                cmd
        };

        String result = deployApi.createDeploy(
                "id-"+studentId+"-"+className,
                "skku-devroom",
                options.getOrDefault("image", "ubuntu:latest"),
                options.getOrDefault("pvName", "dev-room-pv"),
                Objects.equals(studentId, "ta") ?
                        pvHostPath+"/"+pvTaPath+"/"+studentId :
                        pvHostPath+"/"+pvStudentPath+"/"+studentId,
                options.getOrDefault("mountPath", "/home/"+studentId+"-"+className+"/"+studentId),
                options.getOrDefault("selector", "id-"+studentId+"-"+className),
                defaultCmd,
                options.getOrDefault("cpuReq", "0.5"),
                options.getOrDefault("cpuLimit", "1"),
                labels
        );

        JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
        if (jsonObject.has("error")) {
            throw new KubernetesClientException(String.valueOf(jsonObject.get("error")));
        }
    }

    public String delete(String className, List<String> studentIds) {
        Map<String, List<String>> successMap = new HashMap<>();
        List<String> successId = new ArrayList<>();
        HashMap<String, HashMap<String, String>> errorMap = new HashMap<>();
        HashMap<String, String> errorList = new HashMap<>();

        for (String studentId : studentIds) {

            String idName = "id-"+studentId+"-"+className;
            Map<String, String> labels = new HashMap<>();
            labels.put("class_id", "id-"+className);
            labels.put("student_id", "id-"+studentId);


            // 컨피그맵 제거
            try {
                configMapApi.deleteConfigMap(idName+"-config");
            } catch (KubernetesClientException e) {
                errorList.put(idName+"-confingMap", e.getMessage());
            }
            // 서비스 제거
            try {
                serviceApi.deleteService(idName);
            } catch (KubernetesClientException e) {
                errorList.put(idName+"-service", e.getMessage());
            }
            // 디플로이 제거
            try {
                deployApi.deleteDeploy(idName);
            } catch (KubernetesClientException e) {
                errorList.put(idName+"-deploy", e.getMessage());
            }
            successId.addLast(studentId);
        }

        if (!errorList.isEmpty()) {
            errorMap.put("error", errorList);
            return gson.toJson(errorMap);
        }

        successMap.put("success", successId);
        return gson.toJson(successMap);
    }
}
