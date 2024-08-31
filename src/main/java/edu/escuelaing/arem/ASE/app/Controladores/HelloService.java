package edu.escuelaing.arem.ASE.app.Controladores;

import edu.escuelaing.arem.ASE.app.Anotaciones.RESTcontroller;
import edu.escuelaing.arem.ASE.app.Anotaciones.GetMapping;
import edu.escuelaing.arem.ASE.app.Anotaciones.RequestMapping;

@RESTcontroller
public class HelloService {

    @RequestMapping("/")
    public static String index() {
        return "Greetings from Custom Framework!";
    }
    @GetMapping("/hello")
    public static  String hello(){
        return "hello";
    }
    @GetMapping("/sumade1mas2")
    public static Integer sumade1mas2(){
        int n1 = 1;
        int n2 = 2;
        return n1+n2;
    }
    @GetMapping("/adios")
        public static String adios(){
        return "Adios";
    }
    @GetMapping("/cedula")
    public static String cedula(){
        return "100858401";
    }

    @GetMapping("/pi")
    public static  String tremendo(){
        return String.valueOf(Math.PI);
    }



}
