package com.example.administrator.cloudhook.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {

    public static String getFieldSignature(Class clz, String fieldName) {
        String result = "";
        try {
            Field[] fields = clz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                String name = field.getName();
                if (name.equals(fieldName)) {
                    Method sigMethod = field.getClass().getDeclaredMethod("getSignatureAttribute", new Class[]{});
                    sigMethod.setAccessible(true);
                    String signatureStr = (String)sigMethod.invoke(field, new Object[]{});
                    result = signatureStr;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public static String getMethodSignature(Class clz, String methodName) {
        String result = "";
        try {
            Method[] methods = clz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                String name = method.getName();
                if (name.equals(methodName)) {
                    Method getSigMethod = method.getClass().getDeclaredMethod("getSignatureAttribute", new Class[]{});
                    getSigMethod.setAccessible(true);
                    String signatureStr = (String) getSigMethod.invoke(method, new Object[]{});
                    result = signatureStr;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
