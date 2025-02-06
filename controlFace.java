package Controller;

import Model.MongoConnection;
import Model.modelDiary;
import Model.modelFace;
import Vista.FaceProgram;
import javax.swing.JOptionPane;
import Vista.DiaryProgram;
import com.mongodb.client.MongoCollection;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;
import org.bson.Document;

public class controlFace {

    private FaceProgram vista;
    private modelFace modelo;
    private static final String ERROR_USUARIO_OBLIGATORIO = "El usuario es obligatorio.";
    private static final String ERROR_CLAVE_OBLIGATORIA = "La clave es obligatoria.";

    public controlFace(FaceProgram vista, modelFace modelo) {
        this.vista = vista;
        this.modelo = modelo;
        this.vista.btnIngresar.addActionListener(e -> ingresar());
        this.vista.btnRegistrar.addActionListener(e -> registrarUsuario());
        this.vista.btnRecordar.addActionListener(e -> recordarUsuario());
        this.vista.btnRecuperar.addActionListener(e -> recuperarContraseña());
        this.vista.btnSalir.addActionListener(e -> salir());
    }

    private void ingresar() {
        limpiarErrores();
        String usuario = vista.txtUsuario.getText();
        String clave = vista.txtClave.getText();

        boolean hayErrores = false;

        if (usuario.isEmpty()) {
            vista.lblErrorUsuario.setText(ERROR_USUARIO_OBLIGATORIO);
            vista.lblErrorUsuario.setVisible(true);
            hayErrores = true;
        }
        if (clave.isEmpty()) {
            vista.lblErrorClave.setText(ERROR_CLAVE_OBLIGATORIA);
            vista.lblErrorClave.setVisible(true);
            hayErrores = true;
        }

        if (!hayErrores) {
            System.out.println("Intentando iniciar sesión con usuario: " + usuario + " y clave: " + clave);
            if (modelo.verificarUsuario(usuario, clave)) {
                // Mostrar mensaje de inicio de sesión exitoso
                JOptionPane.showMessageDialog(vista, "Inicio de sesión exitoso");

                // Cerrar la ventana de FaceProgram
                vista.dispose();

                // Crear una instancia de DiaryProgram y mostrarla
                DiaryProgram view = new DiaryProgram();  // Asegúrate de que DiaryProgram esté bien inicializada
                modelDiary model = new modelDiary();
                controlDiary controller = new controlDiary(model, view);
                view.setVisible(true); // Mostrar la nueva ventana
            } else {
                JOptionPane.showMessageDialog(vista, "Usuario o contraseña incorrectos");
            }
        }
    }

    // Método para el evento de Registrar
    private void registrarUsuario() {
        // Limpiar errores previos
        limpiarErrores();

        String nombre = vista.txtNombre.getText().trim();
        String apellido = vista.txtApellido.getText().trim();
        String correo = vista.txtCorreoR.getText().trim();
        String usuario = vista.txtUsuarioR.getText().trim();
        String clave = vista.txtClaveR.getText().trim();

        boolean hayErrores = false;

        // Validación del nombre
        if (nombre.isEmpty()) {
            vista.lblErrorNombre.setText("El nombre es obligatorio.");
            vista.lblErrorNombre.setVisible(true);
            hayErrores = true;
        } else if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            vista.lblErrorNombre.setText("El nombre solo puede contener letras.");
            vista.lblErrorNombre.setVisible(true);
            hayErrores = true;
        } else {
            vista.lblErrorNombre.setText(""); // Limpia el mensaje de error si es válido
        }

        // Validación del apellido
        if (apellido.isEmpty()) {
            vista.lblErrorApellido.setText("El apellido es obligatorio.");
            vista.lblErrorApellido.setVisible(true);
            hayErrores = true;
        } else if (!apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$")) {
            vista.lblErrorApellido.setText("El apellido solo puede contener letras.");
            vista.lblErrorApellido.setVisible(true);
            hayErrores = true;
        } else {
            vista.lblErrorApellido.setText(""); // Limpia el mensaje de error si es válido
        }

        // Validación del correo
        if (correo.isEmpty()) {
            vista.lblErrorCorreo.setText("El correo es obligatorio.");
            vista.lblErrorCorreo.setVisible(true);
            hayErrores = true;
        } else if (!isEmailValid(correo)) {
            vista.lblErrorCorreo.setText("El formato del correo es inválido.");
            vista.lblErrorCorreo.setVisible(true);
            hayErrores = true;
        } else {
            vista.lblErrorCorreo.setText(""); // Limpia el mensaje de error si es válido
        }

// Validación del usuario
        if (usuario.isEmpty()) {
            vista.lblErrorUsuarioRegistro.setText("El usuario es obligatorio.");
            vista.lblErrorUsuarioRegistro.setVisible(true);
            hayErrores = true;
        } else if (!usuario.matches(".*[a-zA-Z].*") || usuario.matches("^[0-9]+$")) {
            // Verifica que el usuario contenga al menos una letra y no sea solo números
            vista.lblErrorUsuarioRegistro.setText("El usuario debe contener al menos una letra y no solo números.");
            vista.lblErrorUsuarioRegistro.setVisible(true);
            hayErrores = true;
        } else {
            vista.lblErrorUsuarioRegistro.setText(""); // Limpia el mensaje de error si es válido
        }

        // Validación de la clave
        if (clave.isEmpty()) {
            vista.lblErorClaveRegistro.setText("La clave es obligatoria.");
            vista.lblErorClaveRegistro.setVisible(true);
            hayErrores = true;
        } else {
            vista.lblErorClaveRegistro.setText(""); // Limpia el mensaje de error si es válido
        }

        // Si no hay errores, intentar registrar al usuario
        if (!hayErrores) {
            if (modelo.registrarUsuario(nombre, apellido, correo, usuario, clave)) {
                JOptionPane.showMessageDialog(vista, "Registro exitoso");
            } else {
                JOptionPane.showMessageDialog(vista, "Error al registrar usuario");
            }
        }
    }

    private void recordarUsuario() {
        // Limpiar errores previos
        limpiarErrores();

        String correoIngresado = JOptionPane.showInputDialog(vista, "Ingresa tu correo para recordar el usuario:");

        if (correoIngresado != null && !correoIngresado.isEmpty()) {
            MongoCollection<Document> collection = MongoConnection.getInstance().getCollection();

            Document usuarioDoc = collection.find(new Document("correo", correoIngresado)).first();

            if (usuarioDoc != null) {
                String usuario = usuarioDoc.getString("usuario");
                String clave = usuarioDoc.getString("clave");

                vista.txtUsuario.setText(usuario);
                vista.txtClave.setText(clave);

                JOptionPane.showMessageDialog(vista, "Usuario encontrado. Los campos han sido completados.", "Confirmación", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(vista, "No se encontró un usuario con ese correo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(vista, "Por favor, ingresa un correo.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recuperarContraseña() {
        limpiarErrores();

        String usuarioGuardado = vista.txtUsuario.getText().trim();

        if (usuarioGuardado != null && !usuarioGuardado.isEmpty()) {
            MongoCollection<Document> collection = MongoConnection.getInstance().getCollection();

            Document usuarioDoc = collection.find(new Document("usuario", usuarioGuardado)).first();

            if (usuarioDoc != null) {
                String clave = usuarioDoc.getString("clave");

                vista.txtClave.setText(clave);

                JOptionPane.showMessageDialog(vista, "Usuario encontrado. Los campos han sido completados.", "Confirmación", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(vista, "Usuario no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(vista, "Por favor, ingresa un nombre de usuario.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para limpiar los mensajes de error
    private void limpiarErrores() {
        vista.lblErrorUsuario.setVisible(false);
        vista.lblErrorClave.setVisible(false);
        vista.lblErrorApellido.setVisible(false);
        vista.lblErrorNombre.setVisible(false);
        vista.lblErrorCorreo.setVisible(false);
        vista.lblErrorUsuarioRegistro.setVisible(false);
        vista.lblErorClaveRegistro.setVisible(false);
    }

    // Método para validar el formato del correo electrónico
    private boolean isEmailValid(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private void salir() {
        // Confirmar si realmente se desea salir de la aplicación
        int respuesta = JOptionPane.showConfirmDialog(vista, "¿Estás seguro de que deseas salir?", "Confirmar salida", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            // Cerrar la ventana de la interfaz FaceProgram
            vista.dispose();
            System.exit(0);
        }
    }
}
