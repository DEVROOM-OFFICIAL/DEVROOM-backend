package com.devlatte.devroom.k8s.api.user;

import com.devlatte.devroom.k8s.api.core.*;
import com.devlatte.devroom.k8s.exception.NoAvailablePortException;
import com.devlatte.devroom.k8s.utils.FreemarkerTemplate;
import com.devlatte.devroom.k8s.utils.PortFind;
import com.google.gson.*;
import freemarker.template.TemplateException;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class ClassApi extends K8sApiBase {
    private final DeployApi deployApi;
    private final PodApi podApi;
    private final ConfigMapApi configMapApi;
    private final ServiceApi serviceApi;
    private final ExecApi execApi;
    private final PortFind portFind;
    private final String volumeHostPath;
    private final String volumeStudentPath;
    private final String cmdServerLabel;
    private final String volumeClassPath;

    public ClassApi(DeployApi deployApi,
                    ExecApi execApi,
                    ConfigMapApi configMapApi,
                    ServiceApi serviceApi,
                    PortFind portFind,
                    PodApi podApi,
                    @Value("${config.kubernetes.volumeHostPath}") String volumeHostPath,
                    @Value("${config.kubernetes.volumeStudentPath}") String volumeStudentPath,
                    @Value("${config.kubernetes.volumeClassPath}") String volumeClassPath,
                    @Value("${config.kubernetes.cmdServerLabel}") String cmdServerLabel,
                    @Value("${config.kubernetes.url}") String apiServer,
                    @Value("${config.kubernetes.token}") String apiToken
    ) {
        super(apiServer, apiToken);
        this.execApi = execApi;
        this.deployApi = deployApi;
        this.configMapApi = configMapApi;
        this.serviceApi = serviceApi;
        this.portFind = portFind;
        this.volumeHostPath = volumeHostPath;
        this.volumeStudentPath = volumeStudentPath;
        this.volumeClassPath = volumeClassPath;
        this.cmdServerLabel = cmdServerLabel;
        this.podApi = podApi;
    }

    public String create(String className, String professorId, List<String> studentIds, Map<String, String> options, String[] command, String customScript) {
        Map<String, List<String>> successMap = new HashMap<>();
        List<String> successId = new ArrayList<>();
        HashMap<String, HashMap<String, String>> errorMap = new HashMap<>();
        customScript = "#!/bin/bash\n\n"+customScript;

        HashMap<String, String> errorList = new HashMap<>();
            // 관리자용 id 추가
            // studentIds.addLast("ta");

            for (String studentId : studentIds) {
                String idName = "id-"+studentId+"-"+className;
                String port = "";

                // 포트 자동 할당. false일 경우 순차, true일 경우 랜덤
                try {
                    port = portFind.get(false);
                } catch (NoAvailablePortException e) {
                    errorList.put(idName, e.getMessage());
                    errorMap.put("error", errorList);
                    return gson.toJson(errorMap);
                }

                Map<String, String> labels = new HashMap<>();
                labels.put("class_id", "id-"+className);
                labels.put("student_id", "id-"+studentId);
                labels.put("professor_id", "id-"+professorId);
                labels.put("port", "port-"+port);


                // 영구볼륨 경로 확인. 없을 시 생성. class는 별개의 폴더에 저장.
//                try {
//                    String class_path = "/host/"+volumeHostPath+"/"+volumeClassPath+"/"+className;
//                    execApi.run("app", cmdServerLabel, new String[]{"/bin/sh", "-c", "mkdir "+class_path});
//                    execApi.run("app", cmdServerLabel, new String[]{"/bin/sh", "-c", "chmod -R 777 "+class_path});
//
//                    String student_path = "/host/"+volumeHostPath+"/"+volumeStudentPath+"/"+studentId;
//                    execApi.run("app", cmdServerLabel, new String[]{"/bin/sh", "-c", "mkdir "+student_path});
//                    execApi.run("app", cmdServerLabel, new String[]{"/bin/sh", "-c", "chmod -R 777 "+student_path});
//
//                } catch (KubernetesClientException | IOException | InterruptedException e) {
//                    errorList.put(idName+"-execApi", e.getMessage());
//                }

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

        successMap.put("created", successId);
        return gson.toJson(successMap);
    }
    private void createService(String className, String studentId, String port, Map<String, String> options, Map<String, String> labels) {

        String exPort = port;
        String inPort = "80";
        // vscode server 용 포트개방
        if (options.containsKey("vscode") && options.get("vscode").equals("yes")){
            inPort = "8080";
            labels.put("connection", "vscode");
        }

        // ssh 용 포트개방
        if (options.containsKey("ssh") && options.get("ssh").equals("yes")){
            inPort = "22";
            labels.put("connection", "ssh");
        }

        if (!labels.containsKey("connection")){
            labels.put("connection", "none");
        }

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
        data.put("ubuntu_init.sh",FreemarkerTemplate.convert("/scripts/", "ubuntu_init.sh", template).replaceAll("\\r", ""));

        // ssh 설치용 스크립트
        if (options.containsKey("ssh") && options.get("ssh").equals("yes"))
            data.put("01_install_ssh.sh",FreemarkerTemplate.convert("/scripts/", "01_install_ssh.sh", template).replaceAll("\\r", ""));

        // vscode 설치용 스크립트
        if (options.containsKey("vscode") && options.get("vscode").equals("yes"))
            data.put("02_install_vscode.sh",FreemarkerTemplate.convert("/scripts/", "02_install_vscode.sh", template).replaceAll("\\r", ""));

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

        // 커스텀 command 생성
        Map<String, String> template = new HashMap<>();
        String cmd = FreemarkerTemplate.convert("/scripts/", "ubuntu_command.sh", template).replaceAll("\\r", "");

        String[] defaultCmd = (command != null && command.length > 0) ? command : new String[]{
                "/bin/sh",
                "-c",
                cmd
        };

        // 볼륨 마운트용 Map 생성
        Map<String, Map<String, String>> volumes = new HashMap<>();

        if (Objects.equals(studentId, "ta")){
            // ta용 수정가능 ta폴더
            Map<String, String> publicVolume = new HashMap<>();
            publicVolume.put("hostPath", volumeHostPath+"/"+volumeClassPath+"/"+className);
            publicVolume.put("mountPath", "/devroom_mnt/"+className);
            publicVolume.put("isReadOnly", "false");
            volumes.put("class-data", publicVolume);
        }
        else {
            // student용 수정 불가능 ta폴더
            Map<String, String> publicVolume = new HashMap<>();
            publicVolume.put("hostPath", volumeHostPath+"/"+volumeClassPath+"/"+className);
            publicVolume.put("mountPath", "/devroom_mnt/"+className);
            publicVolume.put("isReadOnly", "true");
            volumes.put("class-data", publicVolume);

            // student용 수정 가능 student폴더
            Map<String, String> privateVolume = new HashMap<>();
            privateVolume.put("hostPath", volumeHostPath+"/"+volumeStudentPath+"/"+studentId);
            privateVolume.put("mountPath", "/devroom_mnt/"+studentId);
            privateVolume.put("isReadOnly", "false");
            volumes.put("student-data", privateVolume);
        }

        String result = deployApi.createDeploy(
                "id-"+studentId+"-"+className,
                "skku-devroom",
                options.getOrDefault("image", "ubuntu:latest"),
                options.getOrDefault("selector", "id-"+studentId+"-"+className),
                options.getOrDefault("cpuReq", "0.5"),
                options.getOrDefault("cpuLimit", "1"),
                options.getOrDefault("memReq", "512Mi"),
                options.getOrDefault("memLimit", "1Gi"),
                labels,
                volumes,
                defaultCmd
        );

        JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
        if (jsonObject.has("error")) {
            throw new KubernetesClientException(String.valueOf(jsonObject.get("error")));
        }
    }

    public String delete(String className, String studentId, String professorId) {
        Map<String, List<String>> successMap = new HashMap<>();
        List<String> successId = new ArrayList<>();
        List<String> studentIds = new ArrayList<>();
        List<String> classIds = new ArrayList<>();
        HashMap<String, HashMap<String, String>> errorMap = new HashMap<>();
        HashMap<String, String> errorList = new HashMap<>();

        String professorPodList = podApi.getInfo("professor_id", "id-"+professorId);
        Gson gson = new Gson();
        JsonArray classArray = gson.fromJson(professorPodList, JsonArray.class);

        for (JsonElement element : classArray) {
            JsonObject jsonObject = element.getAsJsonObject();
            JsonObject labels = jsonObject.getAsJsonObject("labels");
            String tmpClassId = labels.get("class_id").getAsString().substring(3);
            classIds.addLast(tmpClassId);
        }

        try {
            if (!classIds.contains(className)) {
                throw new IllegalArgumentException(professorId+" Professor permission denied: "+className);
            }
        } catch (IllegalArgumentException e) {
            errorList.put("error", e.getMessage());
            return gson.toJson(errorList);
        }

        if (Objects.equals(studentId, "all")){
            String podList = podApi.getInfo("class_id", "id-"+className);
            JsonArray jsonArray = gson.fromJson(podList, JsonArray.class);

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                JsonObject labels = jsonObject.getAsJsonObject("labels");
                String tmpId = labels.get("student_id").getAsString();
                studentIds.addLast(tmpId);
            }

        }
        else{
            studentIds.addLast("id-"+studentId);
        }

        for (String id : studentIds) {

            String idName = id+"-"+className;
            // 서비스 제거
            try {
                serviceApi.deleteService(idName);
            } catch (KubernetesClientException e) {
                errorList.put(idName+"-service", e.getMessage());
            }
            // 컨피그맵 제거
            try {
                configMapApi.deleteConfigMap(idName+"-config");
            } catch (KubernetesClientException e) {
                errorList.put(idName+"-confingMap", e.getMessage());
            }
            // 디플로이 제거
            try {
                deployApi.deleteDeploy(idName);
            } catch (KubernetesClientException e) {
                errorList.put(idName+"-deploy", e.getMessage());
            }
            successId.addLast(id.substring(3));
        }

        if (!errorList.isEmpty()) {
            errorMap.put("error", errorList);
            return gson.toJson(errorMap);
        }

        successMap.put("deleted", successId);
        return gson.toJson(successMap);
    }
}
