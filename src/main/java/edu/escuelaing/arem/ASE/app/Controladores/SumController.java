package edu.escuelaing.arem.ASE.app.Controladores;

import edu.escuelaing.arem.ASE.app.Anotaciones.RESTcontroller;
import edu.escuelaing.arem.ASE.app.Anotaciones.GetMapping;
import edu.escuelaing.arem.ASE.app.Anotaciones.RequestParam;

@RESTcontroller
public class SumController {

    @GetMapping("/sum")
    public String sum(@RequestParam(value = "a", defaultValue = "0") String aStr,
                      @RequestParam(value = "b", defaultValue = "0") String bStr) {
        try {
            int a = Integer.parseInt(aStr);
            int b = Integer.parseInt(bStr);
            int result = a + b;
            return Integer.toString(result);
        } catch (NumberFormatException e) {
            return "Entrada incorrecta. Por favor ingresa enteros";
        }
    }

}