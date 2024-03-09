import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;


/**
 * @user Miguel
 */

/**
 * Clase ServidorCentral que gestiona las operaciones de venta de entradas.
 */
public class ServidorCentral {

    private JFrame frame;

    private static int entradas1 = 100;
    private static int entradas2 = 60;
    private static int entradas3 = 30;
    private static int Suma = 0;

    private static int cantidadTotal = 0;

    private JLabel contador1;
    private JLabel contador2;
    private JLabel contador3;
    private JLabel Suma_Label;

    private static String pdfTipo1 = "";
    private static String pdfTipo2 = "";
    private static String pdfTipo3 = "";
    private static int pdfPrecio1 = 0;
    private static int pdfPrecio2 = 0;
    private static int pdfPrecio3 = 0;

    /**
     * Método principal para iniciar el servidor y la interfaz de usuario.
     *
     * @param args Argumentos de línea de comandos (no utilizado).
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ServidorCentral window = new ServidorCentral();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        ServidorCentral servidor = new ServidorCentral();
        servidor.iniciarServidor();
    }

    /**
     * Constructor de la clase ServidorCentral que inicializa la interfaz de usuario.
     */
    public ServidorCentral() {

        initialize();
    }

    /**
     * Inicia el servidor para aceptar conexiones entrantes de clientes.
     */
    private void iniciarServidor() {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

                Thread cliente = new Thread(() -> manejarConexion(clienteSocket));
                cliente.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualiza los campos de la interfaz de usuario con las cantidades actuales de entradas y la suma total.
     */
    private synchronized void actualizarCampos() {
        contador1.setText(Integer.toString(entradas1));
        contador2.setText(Integer.toString(entradas2));
        contador3.setText(Integer.toString(entradas3));
        Suma_Label.setText(Integer.toString(Suma));
    }

    /**
     * Actualiza la cantidad de entradas disponibles según el tipo y la cantidad especificados.
     *
     * @param tipo     Tipo de entrada.
     * @param cantidad Cantidad de entradas a actualizar.
     */
    private synchronized void actualizarEntradas(int tipo, int cantidad) {
        switch (tipo) {
            case 1:
                entradas1 -= cantidad;
                break;
            case 2:
                entradas2 -= cantidad;
                break;
            case 3:
                entradas3 -= cantidad;
                break;
        }
    }

    /**
     * Actualiza la suma total de la facturación.
     *
     * @param cantidad Cantidad a agregar a la facturación.
     */
    private synchronized void actualizarFacturacion(int cantidad) {
        Suma += cantidad;
    }
    /**
     * Maneja la conexión con un cliente, procesando sus solicitudes.
     *
     * @param clienteSocket Socket del cliente conectado.
     */
    private void manejarConexion(Socket clienteSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clienteSocket.getOutputStream()));

            String mensajeCliente;

            while ((mensajeCliente = reader.readLine()) != null) {
                System.out.println("Mensaje recibido: " + mensajeCliente);

                if (mensajeCliente.contains("COMPRAR_ENTRADA")) {
                    String[] partes = mensajeCliente.split(",");
                    int tipo = Integer.parseInt(partes[1]);
                    int cantidad = Integer.parseInt(partes[2]);
                    tipoEntrada(tipo);

                    if (cantidadTotal >= 3) {
                        writer.write("ERROR: Ya has seleccionado 3 entradas.");
                        writer.newLine();
                        writer.flush();
                    } else if (verificarDisponibilidadEntradas(tipo, cantidad)) {
                        actualizarEntradas(tipo, cantidad);
                        actualizarFacturacion(cantidad * obtenerPrecioEntrada(tipo));

                        writer.write("RESERVA_REALIZADA");
                        writer.newLine();
                        writer.flush();

                        actualizarCampos();
                    } else {
                        writer.write("ERROR: Entradas insuficientes");
                        writer.newLine();
                        writer.flush();
                    }
                    cantidadTotal++;
                } else if (mensajeCliente.equals("CONSULTAR_ENTRADAS")) {
                    InfoEntradas(writer);
                }
            }
        } catch (SocketException e) {
            System.out.println("Cliente desconectado");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clienteSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Verifica la disponibilidad de entradas para un tipo y cantidad específicos.
     *
     * @param tipo     Tipo de entrada.
     * @param cantidad Cantidad de entradas a comprar.
     * @return true si hay suficientes entradas disponibles, false de lo contrario.
     */
    private boolean verificarDisponibilidadEntradas(int tipo, int cantidad) {
        switch (tipo) {
            case 1:
                return entradas1 >= cantidad;
            case 2:
                return entradas2 >= cantidad;
            case 3:
                return entradas3 >= cantidad;
            default:
                return false;
        }
    }

    /**
     * Obtiene el precio de una entrada según su tipo.
     *
     * @param tipo Tipo de entrada.
     * @return Precio de la entrada.
     */
    private int obtenerPrecioEntrada(int tipo) {
        switch (tipo) {
            case 1:
                return 30;
            case 2:
                return 50;
            case 3:
                return 100;
            default:
                return 0;
        }
    }

    /**
     * Envía información sobre las entradas disponibles al cliente.
     *
     * @param writer BufferedWriter utilizado para enviar datos al cliente.
     */
    private void InfoEntradas(BufferedWriter writer) {
        try {
            writer.write("Entradas tipo 1 disponibles: " + entradas1);
            writer.newLine();
            writer.write("Entradas tipo 2 disponibles: " + entradas2);
            writer.newLine();
            writer.write("Entradas tipo 3 disponibles: " + entradas3);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestiona el tipo de entrada y asigna los valores correspondientes a los PDF.
     *
     * @param tipo Tipo de entrada.
     */
    private void tipoEntrada(int tipo) {
        switch (tipo) {
            case 1:
                if (pdfTipo1.equals("")) {
                    pdfTipo1 = "Entrada Normal";
                    pdfPrecio1 = obtenerPrecioEntrada(tipo);
                } else if (pdfTipo2.equals("")) {
                    pdfTipo2 = "Entrada Normal";
                    pdfPrecio2 = obtenerPrecioEntrada(tipo);
                } else if (pdfTipo3.equals("")) {
                    pdfTipo3 = "Entrada Normal";
                    pdfPrecio3 = obtenerPrecioEntrada(tipo);
                } else {
                    System.out.println("No");
                }
                break;
            case 2:
                if (pdfTipo1.equals("")) {
                    pdfTipo1 = "Entrada Premium";
                    pdfPrecio1 = obtenerPrecioEntrada(tipo);
                } else if (pdfTipo2.equals("")) {
                    pdfTipo2 = "Entrada Premium";
                    pdfPrecio2 = obtenerPrecioEntrada(tipo);
                } else if (pdfTipo3.equals("")) {
                    pdfTipo3 = "Entrada Premium";
                    pdfPrecio3 = obtenerPrecioEntrada(tipo);
                } else {
                    System.out.println("No");
                }
                break;
            case 3:
                if (pdfTipo1.equals("")) {
                    pdfTipo1 = "Entrada VIP";
                    pdfPrecio1 = obtenerPrecioEntrada(tipo);
                } else if (pdfTipo2.equals("")) {
                    pdfTipo2 = "Entrada VIP";
                    pdfPrecio2 = obtenerPrecioEntrada(tipo);
                } else if (pdfTipo3.equals("")) {
                    pdfTipo3 = "Entrada VIP";
                    pdfPrecio3 = obtenerPrecioEntrada(tipo);
                } else {
                    System.out.println("No");
                }
                break;
            default:
                System.out.println("¿Que?");
                break;
        }
    }

    /**
     * Inicializa los componentes de la interfaz de usuario.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel lblNewLabel = new JLabel("Taquilla");
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
        lblNewLabel.setBounds(169, 25, 145, 21);
        frame.getContentPane().add(lblNewLabel);

        contador1 = new JLabel(Integer.toString(entradas1));
        contador1.setBounds(198, 63, 45, 13);
        frame.getContentPane().add(contador1);

        contador2 = new JLabel(Integer.toString(entradas2));
        contador2.setBounds(198, 104, 45, 13);
        frame.getContentPane().add(contador2);

        contador3 = new JLabel(Integer.toString(entradas3));
        contador3.setBounds(198, 146, 45, 13);
        frame.getContentPane().add(contador3);

        Suma_Label = new JLabel(Integer.toString(Suma));
        Suma_Label.setBounds(381, 212, 45, 13);
        frame.getContentPane().add(Suma_Label);

        JButton btnNewButton = new JButton("Confirmar Compra");
        btnNewButton.setBounds(122, 184, 169, 21);
        frame.getContentPane().add(btnNewButton);

        JLabel lblEntrada = new JLabel("Entrada 1:");
        lblEntrada.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lblEntrada.setBounds(122, 61, 76, 13);
        frame.getContentPane().add(lblEntrada);

        JLabel lblEntrada_3 = new JLabel("Entrada 2:");
        lblEntrada_3.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lblEntrada_3.setBounds(122, 102, 76, 13);
        frame.getContentPane().add(lblEntrada_3);

        JLabel lblEntrada_1 = new JLabel("Entrada 3:");
        lblEntrada_1.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lblEntrada_1.setBounds(122, 144, 76, 13);
        frame.getContentPane().add(lblEntrada_1);

        JLabel lblRecaudado = new JLabel("Recaudado:");
        lblRecaudado.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lblRecaudado.setBounds(294, 210, 87, 13);
        frame.getContentPane().add(lblRecaudado);

        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                actualizarCampos();
                cantidadTotal = 0;

                Map<String, Object> parametro = new HashMap<>();

                parametro.put("Tipo1", pdfTipo1);
                parametro.put("Tipo2", pdfTipo2);
                parametro.put("Tipo3", pdfTipo3);
                parametro.put("Precio1", Integer.toString(pdfPrecio1) + "€");
                parametro.put("Precio2", Integer.toString(pdfPrecio2) + "€");
                parametro.put("Precio3", Integer.toString(pdfPrecio3) + "€");
                parametro.put("Suma", Integer.toString(pdfPrecio1 + pdfPrecio2 + pdfPrecio3) + "€");

                try {
                    JasperReport jasperReport = JasperCompileManager.compileReport("C:\\Users\\user\\JaspersoftWorkspace\\MyReports\\Blank_Letter.jrxml");
                    JasperPrint informerPrint = JasperFillManager.fillReport(jasperReport, parametro, new JREmptyDataSource());
                    JasperViewer.viewReport(informerPrint);
                } catch (JRException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}