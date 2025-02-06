package Controller;

import Model.MongoConnection;
import Model.modelDiary;
import Model.modelFace;
import Model.modelSchedule;
import Vista.DiaryProgram;
import Vista.FaceProgram;
import Vista.Resume;
import Vista.scheduleOutings;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.List;
import javax.mail.internet.ParseException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;

import org.bson.Document;

public class controlDiary implements ActionListener {

    private modelDiary model;
    private Vista.DiaryProgram view;
    private MongoConnection conexion;

    private ImageIcon imagenSeleccionada;

    public controlDiary(modelDiary model, DiaryProgram view) {
        this.model = model;
        this.view = view;
        this.conexion = MongoConnection.getInstance();

        // Añadiendo ActionListeners a los botones
        this.view.btnAgregar.addActionListener(this);
        this.view.btnActualizar.addActionListener(this);
        this.view.btnEliminar.addActionListener(this);
        this.view.btnBuscar.addActionListener(this);
        this.view.btnMostrarPerfil.addActionListener(this);
        this.view.btnCerrarSesion.addActionListener(this);
        this.view.btnCargarFoto.addActionListener(this);
        this.view.btnCargarDatos.addActionListener(this);
        this.view.btnAgendarSalida.addActionListener(this);
        this.view.btnLimpiarDatos.addActionListener(this);
        this.view.btnDescargarCSV.addActionListener(this);
        this.view.btnDescargarJSON.addActionListener(this);

        // Añadiendo MouseListener para la tabla
        this.view.txtDatos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                seleccionarFilaTabla();  // Llamamos al método para cargar los datos
            }
        });
        mostrarAmigosEnTabla();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == view.btnAgregar) {
            agregarAmigo();
        } else if (e.getSource() == view.btnActualizar) {
            actualizarAmigo();
        } else if (e.getSource() == view.btnEliminar) {
            eliminarAmigo();
        } else if (e.getSource() == view.btnBuscar) {
            buscarAmigo();
        } else if (e.getSource() == view.btnMostrarPerfil) {
            mostrarPerfil();
        } else if (e.getSource() == view.btnCerrarSesion) {
            cerrarSesion();
        } else if (e.getSource() == view.btnCargarFoto) {
            cargarImagen();
        } else if (e.getSource() == view.btnCargarDatos) {
            cargarDatosEnTabla();
        } else if (e.getSource() == view.btnAgendarSalida) {
            agendarSalidita();
        } else if (e.getSource() == view.btnLimpiarDatos) {
            limpiarCampos();
        } else if (e.getSource() == view.btnDescargarCSV) {
            exportarDatosCSV();
        } else if (e.getSource() == view.btnDescargarJSON) {
            exportarDatosJSON();
        }
    }

    private void cargarDatosEnTabla() {
        DefaultTableModel modeloTabla = (DefaultTableModel) view.txtDatos.getModel();
        modeloTabla.setRowCount(0); // Limpiar la tabla antes de cargar los datos

        // Obtener todos los amigos de la base de datos y agregarlos a la tabla
        for (Document amigo : model.obtenerAmigos()) {
            modeloTabla.addRow(new Object[]{
                amigo.getString("genero"),
                amigo.getString("nombreApodo"),
                amigo.getString("fecha"),
                amigo.getString("numero"),
                amigo.getString("comida"),
                amigo.getInteger("edad"),
                amigo.getString("color"),
                amigo.getString("musica"),
                amigo.getString("importante")
            });
        }

        // Notificar al usuario si la tabla está vacía
        if (modeloTabla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "No hay datos para mostrar en la tabla.");
        } else {
            JOptionPane.showMessageDialog(view, "Datos cargados correctamente.");
            limpiarCampos();
        }
    }

    private void cargarImagen() {
        // Crear un JFileChooser para seleccionar la imagen
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Imagen");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos de imagen", "jpg", "png", "gif"));

        int resultado = fileChooser.showOpenDialog(view);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            // Obtén el archivo seleccionado
            File archivo = fileChooser.getSelectedFile();
            String pathImagen = archivo.getAbsolutePath();

            // Mostrar la imagen en el JLabel correspondiente
            ImageIcon icono = new ImageIcon(pathImagen);
            Image imagen = icono.getImage().getScaledInstance(150, 150, Image.SCALE_DEFAULT);  // Ajuste de tamaño si es necesario
            view.lblFoto.setIcon(new ImageIcon(imagen));

            // Guardar la imagen seleccionada
            imagenSeleccionada = icono;
        }
    }

    private void agregarAmigo() {
        if (validarCampos()) {
            String genero = null;

            if (view.rdFemenino.isSelected()) {
                genero = "Femenino";
            } else if (view.rdMasculino.isSelected()) {
                genero = "Masculino";
            }

            // Validación del género
            if (genero == null) {
                JOptionPane.showMessageDialog(view, "Por favor, selecciona un género.");
                return;
            }

            String nombreApodo = view.txtNombre.getText();
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
            String fecha = formatoFecha.format(view.jDFecha.getDate());
            String numero = view.txtNumeroTelefono.getText();
            String comida = view.txtComidaFavorita.getText();
            int edad = (int) view.spinEdad.getValue();
            String color = (String) view.cboxColor.getSelectedItem();
            String musica = view.listMusica.getSelectedValue();
            String importante = view.txtImportante.getText();

            // Verificación de edad válida
            if (edad < 10 || edad > 65) {
                JOptionPane.showMessageDialog(view, "La edad debe ser entre 10 y 65 años.");
                return;
            }

            // Verificar si ya existe un amigo con el mismo nombreApodo
            List<Document> amigosExistentes = model.buscarAmigo(new Document("nombreApodo", nombreApodo));
            if (!amigosExistentes.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Ya existe un amigo con el mismo nombre/apodo.");
                return;
            }

            model.agregarAmigo(genero, nombreApodo, fecha, numero, comida, edad, color, musica, importante);

            // Mostrar solo los datos del amigo recién agregado
            String mensaje = "Amigo guardado correctamente.\n\nDatos del amigo:\n"
                    + "Nombre/Apodo: " + nombreApodo + "\n"
                    + "Género: " + genero + "\n"
                    + "Fecha: " + fecha + "\n"
                    + "Número: " + numero + "\n"
                    + "Comida favorita: " + comida + "\n"
                    + "Edad: " + edad + "\n"
                    + "Color favorito: " + color + "\n"
                    + "Música favorita: " + musica + "\n"
                    + "Importante: " + importante;

            JOptionPane.showMessageDialog(view, mensaje, "Amigo Guardado", JOptionPane.INFORMATION_MESSAGE);

            model.obtenerDatos();
            limpiarCampos();
            mostrarAmigosEnTabla();
        }
    }

    private void actualizarAmigo() {
        int filaSeleccionada = view.txtDatos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreApodo = (String) view.txtDatos.getValueAt(filaSeleccionada, 1);

            if (validarCampos()) {
                String genero = view.rdFemenino.isSelected() ? "Femenino" : "Masculino";
                SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
                String fecha = formatoFecha.format(view.jDFecha.getDate());
                String numero = view.txtNumeroTelefono.getText();
                String comida = view.txtComidaFavorita.getText();
                int edad = (int) view.spinEdad.getValue();
                String color = (String) view.cboxColor.getSelectedItem();
                String musica = view.listMusica.getSelectedValue();
                String importante = view.txtImportante.getText();

                // Verificación de edad válida
                if (edad < 10 || edad > 65) {
                    JOptionPane.showMessageDialog(view, "La edad debe ser entre 10 y 65 años.");
                    return;
                }

                Document nuevosDatos = new Document("genero", genero)
                        .append("fecha", fecha)
                        .append("numero", numero)
                        .append("comida", comida)
                        .append("edad", edad)
                        .append("color", color)
                        .append("musica", musica)
                        .append("importante", importante);

                model.actualizarAmigo(nombreApodo, nuevosDatos);
                //JOptionPane.showMessageDialog(view, "Amigo actualizado correctamente.");
                limpiarCampos();
                mostrarAmigosEnTabla();
            }
        } else {
            JOptionPane.showMessageDialog(view, "Por favor, selecciona un amigo de la tabla para actualizar.");
        }
    }

    private void eliminarAmigo() {
        int filaSeleccionada = view.txtDatos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreApodo = (String) view.txtDatos.getValueAt(filaSeleccionada, 1); // Cambiado a índice 1
            model.eliminarAmigo(nombreApodo);
            mostrarAmigosEnTabla();
            limpiarCampos();
        } else {
            JOptionPane.showMessageDialog(view, "Por favor, selecciona un amigo de la tabla para eliminar.");
        }
    }

    
    
    
  private void buscarAmigo() {
    String criterio = view.txtBuscar.getText().trim(); // Obtenemos el texto del campo de búsqueda

    if (criterio.isEmpty()) {
        JOptionPane.showMessageDialog(view, "Por favor, ingresa un criterio de búsqueda.");
        return;
    }
    // Crear un filtro que busque por nombreApodo, genero o numero
    Document filtro = new Document("$or", List.of(
        new Document("nombreApodo", new Document("$regex", criterio).append("$options", "i")), // Búsqueda por nombreApodo
        new Document("genero", new Document("$regex", criterio).append("$options", "i")),       // Búsqueda por genero
        new Document("numero", new Document("$regex", criterio).append("$options", "i"))        // Búsqueda por numero
    ));
    DefaultTableModel modeloTabla = (DefaultTableModel) view.txtDatos.getModel();
    modeloTabla.setRowCount(0);

    // Buscar en la base de datos y mostrar los resultados en la tabla
    boolean encontrado = false; // Variable para verificar si se encontraron resultados
    for (Document amigo : model.buscarAmigo(filtro)) {
        modeloTabla.addRow(new Object[] {
            amigo.getString("genero"),
            amigo.getString("nombreApodo"),
            amigo.getString("fecha"),
            amigo.getString("numero"),
            amigo.getString("comida"),
            amigo.getInteger("edad"),
            amigo.getString("color"),
            amigo.getString("musica"),
            amigo.getString("importante")
        });
        encontrado = true; // Actualizar a verdadero si se añade un resultado
    }
    if (encontrado) {
        JOptionPane.showMessageDialog(view, "Amigo encontrado.");
    } else {
        JOptionPane.showMessageDialog(view, "No se encontraron amigos con ese criterio.");
    }
}
  
  
    private void mostrarPerfil() {
        int filaSeleccionada = view.txtDatos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            String nombreApodo = (String) view.txtDatos.getValueAt(filaSeleccionada, 1); // Cambiado a índice 1
            List<Document> amigos = model.buscarAmigo(new Document("nombreApodo", nombreApodo));

            if (amigos.isEmpty()) {
                JOptionPane.showMessageDialog(view, "No se encontró el amigo con el nombre especificado.");
                return;
            }

            Document amigo = amigos.get(0);

            Resume resumeView = new Resume();
            resumeView.lblNombreUsuario.setText(amigo.getString("nombreApodo"));
            resumeView.lblNacimiento.setText(amigo.getString("fecha"));
            resumeView.lblNumeroTelefono.setText(amigo.getString("numero"));
            resumeView.lblComidaFavorita.setText(amigo.getString("comida"));

            // Concatenar datos para mostrar en el JTextArea
            String datosTexto = "Género: " + amigo.getString("genero") + "\n"
                    + "Edad: " + amigo.getInteger("edad") + "\n"
                    + "Color favorito: " + amigo.getString("color") + "\n"
                    + "Género musical: " + amigo.getString("musica") + "\n"
                    + "Importante: " + amigo.getString("importante");  // Agregado el campo 'importante'
            resumeView.txtDatosArea.setText(datosTexto);

            // Si se ha seleccionado una imagen, mostrarla en el lblImagen
            if (imagenSeleccionada != null) {
                resumeView.lblImagen.setIcon(imagenSeleccionada);
            }

            // Mostrar la vista y cerrar la actual
            resumeView.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(view, "Por favor, selecciona un amigo de la tabla para ver su perfil.");
        }
    }

    private void cerrarSesion() {
        // Confirmar si realmente se desea cerrar la sesión
        int respuesta = JOptionPane.showConfirmDialog(view, "¿Estás seguro de que deseas cerrar sesión?", "Confirmar cierre de sesión", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            // Cerrar la ventana de DiaryProgram
            view.dispose();

            // Mostrar la ventana de FaceProgram
            FaceProgram vistaInicio = new FaceProgram();
            modelFace modeloInicio = new modelFace();
            controlFace controllerInicio = new controlFace(vistaInicio, modeloInicio);
            vistaInicio.setVisible(true);

            JOptionPane.showMessageDialog(vistaInicio, "Sesión cerrada.");
        }
    }

    
        public static boolean containsOnlyLetters(String fieldText) {
    return fieldText.matches("[a-zA-Z\\s]+");
}
        
        public static boolean containsLettersAndNumbers(String fieldText) {
            if(fieldText.matches("\\d+")){
                return false;
                
            }
            if(fieldText.matches("\"[a-zA-Z\\\\s]+\"")){
                return true;
                
            }
            if(fieldText.matches("[a-zA-Z0-9\\s]+")){
                return true;
                
            }
            return false;
}
    
    private boolean validarCampos() {
        boolean valido = true;
        String nombre = view.txtNombre.getText();
        // Validación del campo de nombre
        if (view.txtNombre.getText().isEmpty()) {
            view.lblErrorNombre.setText("El nombre es obligatorio.");
            valido = false;
        } else if(!containsLettersAndNumbers(nombre)){
            view.lblErrorNombre.setText("Solo letras o letras y numeros");
        }
         else   {
            view.lblErrorNombre.setText("");
        }
     // Validación del campo de fecha de nacimiento
    Date selectedDate = view.jDFecha.getDate();
    if (selectedDate == null) {
        view.lblErrorNacimiento.setText("La fecha de nacimiento es obligatoria.");
        valido = false;
    } else {
        LocalDate birthDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();

        // Validar que la fecha no sea futura
        if (birthDate.isAfter(today)) {
            view.lblErrorNacimiento.setText("La fecha de nacimiento no puede ser en el futuro.");
            valido = false;
        } else {
            // Validar la edad (entre 10 y 65 años, por ejemplo)
            int age = Period.between(birthDate, today).getYears();
            if (age < 10 || age > 65) {
                view.lblErrorNacimiento.setText("La edad debe ser entre 10 y 65 años.");
                valido = false;
            } else {
                view.lblErrorNacimiento.setText(""); // Limpia el mensaje de error si es válido
            }
        }
    }
        // Validación del número de teléfono
        String numeroTelefono = view.txtNumeroTelefono.getText();
        if (numeroTelefono.isEmpty()) {
            view.lblErrorNumero.setText("El número de teléfono es obligatorio.");
            valido = false;
        } else if (!numeroTelefono.matches("\\d{10}")) {
            view.lblErrorNumero.setText("El número de teléfono debe tener 10 dígitos.");
            valido = false;
        } else {
            view.lblErrorNumero.setText(""); // Limpia el mensaje de error si es válido
        }

            String comidaFavorita = view.txtComidaFavorita.getText();
        // Validación del campo de comida favorita
        if (view.txtComidaFavorita.getText().isEmpty()) {
            view.lblErrorComidaFavorita.setText("La comida favorita es obligatoria.");
            valido = false;
        } else if(!containsOnlyLetters(comidaFavorita)){
             view.lblErrorComidaFavorita.setText("Solo puede contener letras");
            valido = false;
                        }else{
                        view.lblErrorComidaFavorita.setText("");
                        } // Limpia el mensaje de error si es válido
        

        // Validación de la edad
        int edad = (int) view.spinEdad.getValue();
        if (edad < 10 || edad > 65) {
            view.lblErrorEdad.setText("La edad debe ser entre 10 y 65 años.");
            valido = false;
        } else {
            view.lblErrorEdad.setText(""); // Limpia el mensaje de error si es válido
        }

        // Validación del género musical (si no se seleccionó nada)
        if (view.listMusica.getSelectedIndex() == -1) {
            view.lblErrorGeneroMusical.setText("Debes seleccionar un género musical.");
            valido = false;
        } else {
            view.lblErrorGeneroMusical.setText(""); // Limpia el mensaje de error si es válido
        }

        // Validación del campo importante
    String importante = view.txtImportante.getText();
    if (importante.isEmpty()) {
        view.lblErrorAcontecimiento.setText("El campo 'Importante' es obligatorio.");
        valido = false;
    } else if (!containsLettersAndNumbers(importante)) {
        view.lblErrorAcontecimiento.setText("Debe contener letras y puede incluir números.");
        valido = false;
    } else {
        view.lblErrorAcontecimiento.setText(""); 
    }
        if (!view.rdFemenino.isSelected() && !view.rdMasculino.isSelected()) {
            view.lblGenero.setText("Por favor, selecciona un género.");
            valido = false;
        } else {
            view.lblGenero.setText("");
        }
        return valido; // Devuelve true si todos los campos son válidos
    }

    private void limpiarCampos() {
        view.txtNombre.setText("");
        view.jDFecha.setDate(null); // Usar null para limpiar el campo de fecha
        view.txtNumeroTelefono.setText("");
        view.txtComidaFavorita.setText("");
        view.spinEdad.setValue(0);
        view.cboxColor.setSelectedIndex(0);
        view.listMusica.clearSelection();
        view.txtImportante.setText("");
        view.buttonGroup1.clearSelection();
    }

    private void mostrarAmigosEnTabla() {
        DefaultTableModel modeloTabla = (DefaultTableModel) view.txtDatos.getModel();
        modeloTabla.setRowCount(0);

        for (Document amigo : model.obtenerAmigos()) {
            modeloTabla.addRow(new Object[]{
                amigo.getString("genero"),
                amigo.getString("nombreApodo"),
                amigo.getString("fecha"),
                amigo.getString("numero"),
                amigo.getString("comida"),
                amigo.getInteger("edad"),
                amigo.getString("color"),
                amigo.getString("musica"),
                amigo.getString("importante")
            });
        }
    }

    private void seleccionarFilaTabla() {
        int filaSeleccionada = view.txtDatos.getSelectedRow();
        if (filaSeleccionada >= 0) {
            // Obtener los datos de la fila seleccionada
            String genero = (String) view.txtDatos.getValueAt(filaSeleccionada, 0);
            String nombreApodo = (String) view.txtDatos.getValueAt(filaSeleccionada, 1);
            Document amigo = model.buscarAmigo(new Document("nombreApodo", nombreApodo)).get(0);

            // Cargar los datos del amigo en los campos de texto correspondientes
            view.txtNombre.setText(amigo.getString("nombreApodo"));
            try {
                Date fecha = new SimpleDateFormat("dd/MM/yyyy").parse(amigo.getString("fecha"));
                view.jDFecha.setDate(fecha);
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
            view.txtNumeroTelefono.setText(amigo.getString("numero"));
            view.txtComidaFavorita.setText(amigo.getString("comida"));
            view.spinEdad.setValue(amigo.getInteger("edad"));
            view.cboxColor.setSelectedItem(amigo.getString("color"));
            view.listMusica.setSelectedValue(amigo.getString("musica"), true);
            view.txtImportante.setText(amigo.getString("importante"));

            if ("Masculino".equals(genero)) {
                view.rdMasculino.setSelected(true);
            } else if ("Femenino".equals(genero)) {
                view.rdFemenino.setSelected(true);
            }
        }
    }

    private void agendarSalidita() {
        DefaultTableModel modeloTabla = (DefaultTableModel) view.txtDatos.getModel();
        int rowCount = modeloTabla.getRowCount();
        Object[][] datosAmigos = new Object[rowCount][modeloTabla.getColumnCount()];

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < modeloTabla.getColumnCount(); j++) {
                datosAmigos[i][j] = modeloTabla.getValueAt(i, j);
            }
        }

        // Crear la vista y el modelo de ScheduleOutings
        scheduleOutings vista = new scheduleOutings();
        modelSchedule modelo = new modelSchedule();
        controlSchedule controller = new controlSchedule(vista, modelo);

        controller.setDatosAmigos(datosAmigos);

        vista.setVisible(true);
    }

    // Método para exportar datos a un archivo CSV
    private void exportarDatosCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo CSV");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivo CSV", "csv"));

        int seleccion = fileChooser.showSaveDialog(view);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            if (!archivo.getName().endsWith(".csv")) {
                archivo = new File(archivo.getAbsolutePath() + ".csv");
            }

            try (FileWriter writer = new FileWriter(archivo)) {
                // Escribir encabezados del archivo
                writer.write("Género,Nombre/Apodo,Fecha,Número,Comida,Edad,Color,Música,Importante\n");

                // Escribir los datos de los amigos
                for (Document amigo : model.obtenerAmigos()) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s\n",
                            amigo.getString("genero"),
                            amigo.getString("nombreApodo"),
                            amigo.getString("fecha"),
                            amigo.getString("numero"),
                            amigo.getString("comida"),
                            amigo.getInteger("edad"),
                            amigo.getString("color"),
                            amigo.getString("musica"),
                            amigo.getString("importante")));
                }

                JOptionPane.showMessageDialog(view, "Datos exportados correctamente a CSV.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Error al exportar datos a CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Método para exportar datos a un archivo JSON
    private void exportarDatosJSON() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar archivo JSON");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivo JSON", "json"));

        int seleccion = fileChooser.showSaveDialog(view);
        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            if (!archivo.getName().endsWith(".json")) {
                archivo = new File(archivo.getAbsolutePath() + ".json");
            }

            try (FileWriter writer = new FileWriter(archivo)) {
                List<Document> amigos = model.obtenerAmigos();
                writer.write("[\n");

                for (int i = 0; i < amigos.size(); i++) {
                    writer.write(amigos.get(i).toJson());
                    if (i < amigos.size() - 1) {
                        writer.write(",\n");
                    }
                }

                writer.write("\n]");
                JOptionPane.showMessageDialog(view, "Datos exportados correctamente a JSON.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(view, "Error al exportar datos a JSON: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
