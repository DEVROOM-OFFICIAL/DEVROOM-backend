package com.devlatte.devroom.aws;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.devlatte.devroom.CoreApplication;

/*
<RequestHandler, RequestStreamHandler>의 차이
https://github.com/aws/serverless-java-container/issues/245 - RequestHandler를 써라

RequestStreamHandler는 그야말로 Stream을 받아서 내가 직접 커스터마이징한 직렬/역직렬 과정을 거쳐야 한다.
형식화된 Req를 받으려면 RequestHandler를 써서 AwsProxyRequest와 AwsProxyResponse를 사용해야 한다.
*/

public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {

    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    public LambdaHandler(){
        try{
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(CoreApplication.class);
        } catch (ContainerInitializationException e){
            throw new RuntimeException("Failed to start Spring Application", e);
        }
    }

    //RequestHandler<AwsProxyRequest, AwsProxyResponse>를 LambdaHandler가 override할 때 이 코드를 사용
    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest req, Context context){
        LambdaLogger lambdaLogger = context.getLogger();
        lambdaLogger.log("Request Received : " + req);

        // 로그 출력
        lambdaLogger.log("HTTP Method: " + req.getHttpMethod());
        lambdaLogger.log("Path: " + req.getPath());
        lambdaLogger.log("Body: " + req.getBody());

        return handler.proxy(req, context);
    }




    /*
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
        throws IOException{
        handler.proxyStream(inputStream, outputStream, context);
    }

    */
}
