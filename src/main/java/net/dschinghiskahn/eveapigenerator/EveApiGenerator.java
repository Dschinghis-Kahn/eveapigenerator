package net.dschinghiskahn.eveapigenerator;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import net.dschinghiskahn.eveapigenerator.bean.ApiBean;

public class EveApiGenerator {

    private static final String OUTPUT_DIRECTORY = "src/main/api";

    public static void main(String[] args) throws Exception {
        File outDir = new File(OUTPUT_DIRECTORY + "/net");
        if (outDir.isDirectory()) {
            delete(outDir);
        }
        Properties properties = new Properties();
        File baseDir = new File(EveApiGenerator.class.getClassLoader().getResource("").getPath() + "/");
        for (File apiType : baseDir.listFiles()) {
            if (apiType.isDirectory()) {
                for (File apiContent : apiType.listFiles()) {
                    if (apiContent.isDirectory()) {
                        for (File file : apiContent.listFiles()) {
                            if (file.getName().endsWith(".properties")) {
                                properties.clear();
                                properties.load(new FileInputStream(file));
                                properties.put("name", file.getName().replaceAll(".properties", ""));
                                System.out.println("Generatring classes for " + apiType.getName() + "/" + apiContent.getName() + "/" + file.getName());
                                process(properties, apiType.getName() + "/" + apiContent.getName());
                            }
                        }
                    }
                }
            }
        }
    }

    private static void delete(File file) {
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            for (File content : file.listFiles()) {
                delete(content);
            }
            file.delete();
        }
    }

    public static void process(Properties properties, String url) throws Exception {
        File file = new File(OUTPUT_DIRECTORY + "/net/dschinghiskahn/eveapi/" + url + "/" + properties.getProperty("name") + ".java");
        file.mkdirs();
        file.delete();
        DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
        try {
            ApiBean bean = new ApiBean(properties.getProperty("name"), "net.dschinghiskahn.eveapi." + url.replaceAll("/", "."),
                    url.endsWith(properties.getProperty("name").toLowerCase()));
            if (properties.getProperty("fieldLong") != null && properties.getProperty("fieldLong").length() > 0) {
                for (String field : properties.getProperty("fieldLong").split(",")) {
                    bean.addField(field, Long.class.getName());
                }
            }
            if (properties.getProperty("fieldString") != null && properties.getProperty("fieldString").length() > 0) {
                for (String field : properties.getProperty("fieldString").split(",")) {
                    bean.addField(field, String.class.getName());
                }
            }
            if (properties.getProperty("fieldDouble") != null && properties.getProperty("fieldDouble").length() > 0) {
                for (String field : properties.getProperty("fieldDouble").split(",")) {
                    bean.addField(field, Double.class.getName());
                }
            }
            if (properties.getProperty("fieldDate") != null && properties.getProperty("fieldDate").length() > 0) {
                for (String field : properties.getProperty("fieldDate").split(",")) {
                    bean.addField(field, Date.class.getName());
                }
            }
            if (properties.getProperty("fieldList") != null && properties.getProperty("fieldList").length() > 0) {
                int position = 1;
                for (String field : properties.getProperty("fieldList").split(",")) {
                    bean.addField(field, List.class.getName(), position++);
                }
            }
            if (properties.getProperty("fieldObject") != null && properties.getProperty("fieldObject").length() > 0) {
                for (String field : properties.getProperty("fieldObject").split(",")) {
                    bean.addField(field, "net.dschinghiskahn.eveapi.xxx." + field.replaceAll("\\*", ""));
                }
            }
            output.write(bean.toString().getBytes());
        } finally {
            output.close();
        }

    }
}
