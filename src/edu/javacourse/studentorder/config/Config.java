package edu.javacourse.studentorder.config;

import java.io.*;
import java.util.Properties;

public class Config {
    public static final String PATH_TO_CONFIG = "dao.properties";
    public static final String DB_URL = "db.url";
    public static final String DB_LOGIN = "db.login";
    public static final String DB_PASSWORD = "db.password";

    private static Properties properties = new Properties();

    public static String getProperties(String name) {
        if (properties.isEmpty()) {
            try(InputStream is = Config.class.getClassLoader()
                    .getResourceAsStream("dao.properties")) {
                properties.load(is);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex); //здесь можно бросить такое исключение,
                // т.к. запуск приложения без возможности достучаться
                //до бд бессмысленный
            }

        }
        return properties.getProperty(name);
    }

}
