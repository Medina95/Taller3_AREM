package edu.escuelaing.arem.ASE.app;

import edu.escuelaing.arem.ASE.app.Anotaciones.GetMapping;
import edu.escuelaing.arem.ASE.app.Anotaciones.RESTcontroller;
import edu.escuelaing.arem.ASE.app.Anotaciones.RequestMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {


    public static void main(String[] args) throws ClassNotFoundException, MalformedURLException, InvocationTargetException, IllegalAccessException {
        Class c = Class.forName(args[0]);
        Map<String, Method> services = new HashMap<>();

        if (c.isAnnotationPresent(RESTcontroller.class)) {
            Method[] method = c.getDeclaredMethods();
            for (Method m : method) {
                if (m.isAnnotationPresent(GetMapping.class)) {

                    String key = m.getAnnotation(GetMapping.class).value();

                    services.put(key, m);
                }
                if (m.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = m.getAnnotation(RequestMapping.class);
                    String key = requestMapping.value();
                    if (m.getReturnType().equals(String.class)) {
                        services.put(key, m);
                    }
                }


                String[] valores = {"pi", "hello", "adios", "sumade1mas2", "cedula"};


                for (String valor : valores) {
                    URL serviceurl = new URL("http://localhost:8080/App/" + valor);
                    String path = serviceurl.getPath();
                    System.out.println("path" + path);
                    String servicename = path.substring(4);
                    System.out.println("Service name" + servicename);
                    Method ms = services.get(servicename);
                    System.out.println("respuesta" + ms.invoke(null));

                }
            }
        }
    }
}