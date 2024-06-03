package com.devlatte.devroom.k8s.utils;

import com.devlatte.devroom.k8s.api.core.ServiceApi;
import com.devlatte.devroom.k8s.exception.*;
import org.springframework.beans.factory.annotation.Value;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
@Service
public class PortFind {
    private final ServiceApi serviceApi;

    private final int portStart;

    private final int portEnd;
    public PortFind(ServiceApi serviceApi,
                    @Value("${config.kubernetes.portStart}") int portStart,
                    @Value("${config.kubernetes.portEnd}") int portEnd
                    ) {
        this.serviceApi = serviceApi;
        this.portStart = portStart;
        this.portEnd = portEnd;
    }
    public String get(Boolean isRandom) throws NoAvailablePortException {
        String jsonData = serviceApi.getInfo("all", null);

        JsonArray jsonArray = JsonParser.parseString(jsonData).getAsJsonArray();
        List<String> portList = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            String port = element.getAsJsonObject().get("port").getAsString();
            portList.add(port);
        }

        List<Integer> availablePorts = new ArrayList<>();
        for (int port = portStart; port <= portEnd; port++) {
            if (!portList.contains(String.valueOf(port))) {
                availablePorts.add(port);
            }
        }

        if (availablePorts.isEmpty()) throw new NoAvailablePortException("사용 가능한 포트가 없습니다.");

        int Index = 0;

        if (isRandom){
            Random random = new Random();
            Index = random.nextInt(availablePorts.size());
        }

        return String.valueOf(availablePorts.get(Index));
    }

}
