//Importan archivos externos y librerias
import java.text.DecimalFormat;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;

//Clase principal - Conversor de monedas
public class ConversorDeMonedas {

    private static String apiKey;
    private static String apiUrl;

    public static void main(String[] args) {
        cargarConfiguracion();
        mostrarMenu();
    }

    //Metodo para cargar la clave de la API
    private static void cargarConfiguracion() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("src/api_config.properties")) {
            prop.load(input);
            apiKey = prop.getProperty("api.key");
            apiUrl = prop.getProperty("api.url");
        } catch (IOException ex) {
            System.err.println("Error al cargar el archivo de configuración: " + ex.getMessage());
            // Aquí puedes registrar el error si tienes un sistema de logging
        }
    }



    //metodo para mostrar el menú de opciones al usuario
    private static void mostrarMenu() {
        Scanner scanner = new Scanner(System.in);
        String monedaOrigen; // Sin inicialización redundante
        String monedaDestino;
        double valor;
        String opcion;

        System.out.println("Bienvenido al Conversor de Monedas");

        // Bucle para mostrar el menú
        do {
            // Pedir moneda de origen
            while (true) {
                System.out.print("Ingrese el código de la moneda de origen (Ejemplo: COP, USD, EUR, BRL): ");
                monedaOrigen = scanner.nextLine().toUpperCase();
                if (esCodigoMonedaValido(monedaOrigen)) {
                    break; // Salir del bucle si es válido
                } else {
                    System.out.println("El código de moneda de origen es inválido. Debe ser de tres letras mayúsculas.");
                }
            }

            // Pedir moneda de destino
            while (true) {
                System.out.print("Ingrese el código de la moneda de destino (Ejemplo: COP, USD, EUR, BRL): ");
                monedaDestino = scanner.nextLine().toUpperCase();
                if (esCodigoMonedaValido(monedaDestino)) {
                    break; // Salir del bucle si es válido
                } else {
                    System.out.println("El código de moneda de destino es inválido. Debe ser de tres letras mayúsculas.");
                }
            }

            // Pedir valor a convertir
            while (true) {
                System.out.print("Ingrese el valor a convertir: ");
                if (scanner.hasNextDouble()) {
                    valor = scanner.nextDouble();
                    scanner.nextLine(); // Consumir la nueva línea
                    break; // Salir del bucle si la entrada es válida
                } else {
                    System.out.println("Por favor, ingrese un valor numérico válido.");
                    scanner.nextLine(); // Limpiar el buffer
                }
            }

            // Lógica para hacer la conversión
            String resultado = convertirMoneda(monedaOrigen, monedaDestino, valor);
            System.out.println("El resultado es: " + resultado);

            // Pregunta si el usuario desea convertir otra moneda o desea salir
            System.out.print("¿Desea realizar otra conversión? (s/n): ");
            opcion = scanner.nextLine().toLowerCase();

        } while (opcion.equals("s")); // Si la respuesta es 's', el menú se repite

        System.out.println("Gracias por usar el Conversor de Monedas. ¡Hasta luego!");
    }





    // Metodo para validar el código ingresado de moneda de origen.
    private static boolean esCodigoMonedaValido(String codigo) {
        return codigo.matches("^[A-Z]{3}$"); // Verifica que el código sea de tres letras mayúsculas
    }



    private static String convertirMoneda(String origen, String destino, double valor) {
        try {
            String url_str = apiUrl + "/latest/USD?apiKey=" + apiKey;
            URL url = new URL(url_str);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parsear la respuesta JSON
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            JsonObject conversionRates = jsonResponse.getAsJsonObject("conversion_rates");

            // Verificar si los códigos de moneda son válidos
            if (!conversionRates.has(origen) || !conversionRates.has(destino)) {
                return "Uno de los códigos de moneda es incorrecto.";
            }

            // Obtener las tasas de conversión
            double tasaOrigen = conversionRates.get(origen).getAsDouble();
            double tasaDestino = conversionRates.get(destino).getAsDouble();

            // Calcular el resultado
            double resultado = (valor / tasaOrigen) * tasaDestino;

            // Formatear y concatenar el código de la moneda
            DecimalFormat df = new DecimalFormat("#,##0.00");
            return df.format(resultado) + " " + destino; // Retornar el valor formateado como String

            //inicia bloque de captura de error por si no funciona el código anterior
        } catch (java.io.IOException e) {
            System.err.println("Error de entrada/salida: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.err.println("Error en el formato de JSON: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ocurrió un error inesperado: " + e.getMessage());
        }
        return "Error en la conversión"; // Retornar un String en caso de error
    }


}
