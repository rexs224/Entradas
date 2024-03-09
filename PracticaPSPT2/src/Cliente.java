import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @user Miguel
 */

/**
 * Clase Cliente que permite interactuar con el servidor para la venta de entradas.
 */
public class Cliente {

    public static int contador = 0;
    private JFrame frame;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    /**
     * Método principal que inicia la aplicación del cliente.
     *
     * @param args Argumentos de línea de comandos (no utilizado).
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Cliente window = new Cliente();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Constructor de la clase Cliente que inicializa la interfaz de usuario y se conecta al servidor.
     */
    public Cliente() {
        initialize();
        conectar();
    }

    /**
     * Inicializa los componentes de la interfaz de usuario.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JLabel lblNewLabel = new JLabel("Venta de entradas");
        lblNewLabel.setBounds(158, 27, 192, 13);
        frame.getContentPane().add(lblNewLabel);

        JButton EntradasDisponibles = new JButton("Entradas");
        EntradasDisponibles.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Enviar solicitud de consulta de entradas al servidor
                    writer.write("CONSULTAR_ENTRADAS");
                    writer.newLine();
                    writer.flush();

                    // Recibir y mostrar la respuesta del servidor
                    String respuesta = reader.readLine();
                    mensajeServidor("Entradas Disponibles", respuesta);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        EntradasDisponibles.setBounds(64, 50, 136, 21);
        frame.getContentPane().add(EntradasDisponibles);

        // Botones para comprar entradas
        JButton btnEntradaUno = new JButton("Comprar entrada 1");
        btnEntradaUno.setEnabled(false);
        btnEntradaUno.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                realizarCompra(1, 1);
            }
        });
        btnEntradaUno.setBounds(126, 143, 169, 21);
        frame.getContentPane().add(btnEntradaUno);

        JButton btnEntradaDos = new JButton("Comprar entrada 2"); // PRECIO = 50€
        btnEntradaDos.setEnabled(false);
        btnEntradaDos.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                realizarCompra(2, 1);
            }
        });
        btnEntradaDos.setBounds(126, 174, 169, 21);
        frame.getContentPane().add(btnEntradaDos);

        JButton btnEntradaTres = new JButton("Comprar entrada 3");// PRECIO = 100€
        btnEntradaTres.setEnabled(false);
        btnEntradaTres.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                realizarCompra(3, 1);
            }
        });
        btnEntradaTres.setBounds(126, 205, 169, 21);
        frame.getContentPane().add(btnEntradaTres);

        JButton btnReserva = new JButton("Reserva");
        btnReserva.setBounds(241, 50, 136, 21);
        frame.getContentPane().add(btnReserva);

        // Acción al hacer clic en el botón de reserva
        btnReserva.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tiempo(btnEntradaUno, btnEntradaDos, btnEntradaTres, btnReserva);
            }
        });
    }

    /**
     * Muestra un mensaje del servidor en un cuadro de diálogo.
     *
     * @param titulo  Título del cuadro de diálogo.
     * @param mensaje Mensaje a mostrar.
     */
    private void mensajeServidor(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(frame, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Establece la conexión con el servidor.
     */
    private void conectar() {
        try {
            socket = new Socket("localhost", 1234);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Realiza la compra de entradas según el tipo y la cantidad especificados.
     *
     * @param tipo     Tipo de entrada a comprar.
     * @param cantidad Cantidad de entradas a comprar.
     */
    private void realizarCompra(int tipo, int cantidad) {
        try {
            writer.write("COMPRAR_ENTRADA," + tipo + "," + cantidad);
            writer.newLine();
            writer.flush();

            String respuesta = reader.readLine();
            mensajeServidor("Resultado", respuesta);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Habilita y deshabilita los botones de compra y reserva después de un cierto tiempo.
     *
     * @param uno      Botón de compra de entrada 1.
     * @param dos      Botón de compra de entrada 2.
     * @param tres     Botón de compra de entrada 3.
     * @param reserva  Botón de reserva.
     */
    private static void tiempo(JButton uno, JButton dos, JButton tres, JButton reserva) {

        uno.setEnabled(true);
        dos.setEnabled(true);
        tres.setEnabled(true);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                uno.setEnabled(false);
                dos.setEnabled(false);
                tres.setEnabled(false);
                reserva.setEnabled(false);

            }
        }, 100 * 1000);
    }
}
