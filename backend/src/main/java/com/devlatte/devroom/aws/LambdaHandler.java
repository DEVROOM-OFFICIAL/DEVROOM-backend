package com.devlatte.devroom.aws;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.devlatte.devroom.CoreApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class LambdaHandler implements RequestStreamHandler{
    private static SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

    public LambdaHandler(){
        try{
            handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(CoreApplication.class);
        } catch (ContainerInitializationException e){
            throw new RuntimeException("Failed to start Spring Application", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
        throws IOException{
        handler.proxyStream(inputStream, outputStream, context);
    }

    /*
    //RequestHandler<AwsProxyRequest, AwsProxyResponse>를 LambdaHandler가 override할 때 이 코드를 사용
    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest req, Context context){
        return handler.proxy(req, context);
    }
    */
}
