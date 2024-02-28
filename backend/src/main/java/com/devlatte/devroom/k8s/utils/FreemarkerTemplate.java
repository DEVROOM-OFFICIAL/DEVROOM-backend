package com.devlatte.devroom.k8s.utils;

import java.io.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.util.HashMap;
import java.util.Map;
public class FreemarkerTemplate {
    public static String convert(String path, String fileName, Map<String, String> templates) throws IOException, TemplateException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setClassForTemplateLoading(FreemarkerTemplate.class, path);
        cfg.setDefaultEncoding("UTF-8");

        Template template = cfg.getTemplate(fileName);
        Map<String, Object> data = new HashMap<>();
        StringWriter stringWriter = new StringWriter();
        template.process(templates, stringWriter);
        return stringWriter.toString();
    }
}
