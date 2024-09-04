
import edu.escuelaing.arem.ASE.app.Anotaciones.GetMapping;
import edu.escuelaing.arem.ASE.app.Anotaciones.RequestParam;
import edu.escuelaing.arem.ASE.app.Controladores.GreetingController;
import edu.escuelaing.arem.ASE.app.Controladores.HelloService;
import edu.escuelaing.arem.ASE.app.Controladores.SumController;
import edu.escuelaing.arem.ASE.app.SpringECI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ClientHandlerTest {

    private SpringECI.ClientHandler clientHandler;
    private Socket mockSocket;

    /**
     * Configura el entorno de prueba antes de cada test.
     * Se crea un Socket simulado y se inicializa el ClientHandler con este Socket.
     */
    @BeforeEach
    public void setUp() throws IOException {
        mockSocket = mock(Socket.class);
        clientHandler = new SpringECI.ClientHandler(mockSocket);
    }
    /**
     * Método auxiliar para probar un endpoint HTTP.
     * Simula una solicitud HTTP, ejecuta el método del ClientHandler, y verifica si la respuesta
     * coincide con el contenido y estado esperado.
     *
     * @param httpRequest la solicitud HTTP a simular
     * @param expectedContent el contenido esperado en la respuesta
     * @param expectedStatus el estado esperado en la respuesta
     */
    private void testEndpoint(String httpRequest, String expectedContent, String expectedStatus) throws Exception {
        // Mocking the InputStream and OutputStream
        InputStream mockInputStream = new ByteArrayInputStream(httpRequest.getBytes());
        ByteArrayOutputStream mockOutputStream = new ByteArrayOutputStream();

        when(mockSocket.getInputStream()).thenReturn(mockInputStream);
        when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);

        // Execute
        clientHandler.run();

        // Verify response
        String response = mockOutputStream.toString();
        String expectedResponse = expectedStatus + "\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + expectedContent.length() + "\r\n" +
                "\r\n" +
                expectedContent;
        assertEquals(expectedResponse, response);
    }

    /**
     * Prueba el servicio de saludo con un parámetro de nombre.
     * Configura el servicio REST para la ruta "/greeting" con un parámetro de nombre,
     * simula una solicitud y verifica si la respuesta es correcta.
     */
    @Test
    public void testGreetingWithNameParam() throws Exception {
        // Mocking the services map
        Method greetingMethod = GreetingController.class.getMethod("greeting", String.class);
        SpringECI.services.put("/greeting", greetingMethod);
        String httpRequest = "GET /api/greeting?name=CAROLA HTTP/1.1\r\n";
        String expectedContent = "Hello, CAROLA!";
        String expectedStatus = "HTTP/1.1 200 OK";

        testEndpoint(httpRequest, expectedContent, expectedStatus);
    }



    /**
     * Prueba el servicio de contenido estático para la ruta raíz "/".
     * Configura el servicio REST para la ruta "/", simula una solicitud y verifica si la respuesta
     * contiene el contenido esperado.
     */
    @Test
    public void testIndexStatic() throws Exception {
        // Mocking the services map
        Method indexMethod = HelloService.class.getMethod("index");
        SpringECI.services.put("/", indexMethod);

        String httpRequest = "GET /api/ HTTP/1.1\r\n";
        String expectedContent = "Greetings from Custom Framework!";
        String expectedStatus = "HTTP/1.1 200 OK";

        testEndpoint(httpRequest, expectedContent, expectedStatus);
    }
    /**
     * Prueba el servicio de contenido estático para la ruta "/hello".
     * Configura el servicio REST para la ruta "/hello", simula una solicitud y verifica si la respuesta
     * contiene el contenido esperado.
     */
    @Test
    public void testHelloStatic() throws Exception {
        // Mocking the services map
        Method indexMethod = HelloService.class.getMethod("hello");
        SpringECI.services.put("/hello", indexMethod);

        String httpRequest = "GET /api/hello HTTP/1.1\r\n";
        String expectedContent = "hello";
        String expectedStatus = "HTTP/1.1 200 OK";

        testEndpoint(httpRequest, expectedContent, expectedStatus);
    }
    /**
     * Prueba el servicio de suma con parámetros.
     * Configura el servicio REST para la ruta "/sum" con parámetros de suma,
     * simula una solicitud y verifica si la respuesta es el resultado correcto de la suma.
     */
    @Test
    public void testSumWithParams() throws Exception {
        // Mocking the services map
        Method sumMethod = SumController.class.getMethod("sum", String.class, String.class);
        SpringECI.services.put("/sum", sumMethod);

        String httpRequest = "GET /api/sum?a=7&b=3 HTTP/1.1\r\n";
        String expectedContent = "10";
        String expectedStatus = "HTTP/1.1 200 OK";

        testEndpoint(httpRequest, expectedContent, expectedStatus);
    }
    /**
     * Prueba el servicio de saludo con el parámetro de nombre por defecto.
     * Configura el servicio REST para la ruta "/greeting" sin parámetros,
     * simula una solicitud y verifica si la respuesta es el saludo por defecto.
     */
    @Test
    public void testGreetingWithDefaultParam() throws Exception {
        // Mocking the services map
        Method greetingMethod = GreetingController.class.getMethod("greeting", String.class);
        SpringECI.services.put("/greeting", greetingMethod);

        String httpRequest = "GET /api/greeting HTTP/1.1\r\n";
        String expectedContent = "Hello, World!";
        String expectedStatus = "HTTP/1.1 200 OK";

        testEndpoint(httpRequest, expectedContent, expectedStatus);
    }

}