package edu.escuelaing.arem.ASE.app;

import edu.escuelaing.arem.ASE.app.Anotaciones.GetMapping;
import edu.escuelaing.arem.ASE.app.Anotaciones.RESTcontroller;
import edu.escuelaing.arem.ASE.app.Anotaciones.RequestMapping;
import edu.escuelaing.arem.ASE.app.Anotaciones.RequestParam;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.util.*;

public class SpringECI {

    private static final int PORT = 8080;
    private static final String WEB_ROOT = "src/webroot";
    public static final Map<String, Method> services = new HashMap<>();

    public static void main(String[] args) {
        loadServices();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor escuchando en el puerto " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static class ClassFinder {

        public static List<Class<?>> findClasses(String packageName) throws ClassNotFoundException {
            String packagePath = packageName.replace('.', '/');
            File directory = new File(Thread.currentThread().getContextClassLoader().getResource(packagePath).getFile());
            List<Class<?>> classes = new ArrayList<>();

            if (directory.exists()) {
                for (File file : directory.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        String className = packageName + '.' + file.getName().replace(".class", "");
                        classes.add(Class.forName(className));
                    }
                }
            }
            return classes;
        }
    }

    public static void loadServices() {
        Map<String, Method> services = new HashMap<>();

        try {
            String packageName = "edu.escuelaing.arem.ASE.app.Controladores";
            List<Class<?>> classes = ClassFinder.findClasses(packageName);

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(RESTcontroller.class)) {
                    for (Method method : clazz.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(GetMapping.class)) {
                            services.put(method.getAnnotation(GetMapping.class).value(), method);
                        }
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            services.put(method.getAnnotation(RequestMapping.class).value(), method);
                        }
                    }
                }
            }

            SpringECI.services.putAll(services);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 OutputStream out = clientSocket.getOutputStream()) {

                String requestLine = in.readLine();
                if (requestLine == null) return;

                String[] tokens = requestLine.split(" ");
                if (tokens.length < 3) return;

                String method = tokens[0];
                String requestedResource = tokens[1];
                String response;

                // Manejo de parámetros de consulta
                String[] pathAndParams = requestedResource.split("\\?");
                String path = pathAndParams[0];
                Map<String, String> params = new HashMap<>();

                if (pathAndParams.length > 1) {
                    String[] queryParams = pathAndParams[1].split("&");
                    for (String param : queryParams) {
                        String[] keyValue = param.split("=");
                        if (keyValue.length == 2) {
                            params.put(keyValue[0], keyValue[1]);
                        }
                    }
                }

                if (path.startsWith("/api")) {
                    String ruta = path.substring(4); // Eliminar "/api" del inicio
                    Method serviceMethod = services.get(ruta);
                    if (serviceMethod != null) {
                        Object[] methodParams = new Object[serviceMethod.getParameterCount()];
                        Annotation[][] parameterAnnotations = serviceMethod.getParameterAnnotations();

                        for (int i = 0; i < methodParams.length; i++) {
                            for (Annotation annotation : parameterAnnotations[i]) {
                                if (annotation instanceof RequestParam) {
                                    RequestParam requestParam = (RequestParam) annotation;
                                    //aca deberia parsear los tipos de dato
                                    String paramValue = params.getOrDefault(requestParam.value(), requestParam.defaultValue());
                                    methodParams[i] = paramValue;
                                }
                            }
                        }

                        Object result;
                        if (Modifier.isStatic(serviceMethod.getModifiers())) {
                            // Invocar el método estático con los parámetros
                            result = serviceMethod.invoke(null, methodParams);
                        } else {
                            // Invocar el método no estático (se necesita una instancia de la clase)
                            Class<?> declaringClass = serviceMethod.getDeclaringClass();
                            Object instance = declaringClass.getDeclaredConstructor().newInstance();
                            result = serviceMethod.invoke(instance, methodParams);
                        }

                        response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "Content-Length: " + result.toString().length() + "\r\n" +
                                "\r\n" +
                                result.toString();
                    } else {
                        response = "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Type: text/plain\r\n" +
                                "\r\n" +
                                "Not Found";
                    }
                    out.write(response.getBytes());
                    out.flush();
                } else {
                    serveStaticFile(requestedResource, out);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void serveStaticFile(String resource, OutputStream out) throws IOException {
            Path filePath = Paths.get(WEB_ROOT, resource);
            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                String contentType = Files.probeContentType(filePath);
                byte[] fileContent = Files.readAllBytes(filePath);

                String responseHeader = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Content-Length: " + fileContent.length + "\r\n" +
                        "\r\n";
                out.write(responseHeader.getBytes());
                out.write(fileContent);
            } else {
                send404(out);
            }
        }

        private void send404(OutputStream out) throws IOException {
            String response = "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Not Found";
            out.write(response.getBytes());
        }
    }
}
