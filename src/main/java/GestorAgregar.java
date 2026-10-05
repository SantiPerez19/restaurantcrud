import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GestorAgregar {
    private MainApp mainApp;
    private Database db;

    //Metodo contructor
    public GestorAgregar(MainApp mainApp, Database db) {
        this.mainApp = mainApp;
        this.db = db;
    }

    //Metodo para agregar clientes
    public void agregarCliente() {
        JTextField nombreField = new JTextField();
        JTextField apellidoField = new JTextField();
        JTextField telefonoField = new JTextField();
        JTextField celularField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField detallesField = new JTextField();

        Object[] message = {
                "Nombre:", nombreField,
                "Apellido:", apellidoField,
                "Teléfono:", telefonoField,
                "Celular:", celularField,
                "Email:", emailField,
                "Detalles:", detallesField
        };

        int option = JOptionPane.showConfirmDialog(mainApp, message, "Agregar Cliente", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            String nombre = nombreField.getText();
            String apellido = apellidoField.getText();
            String telefono = telefonoField.getText().trim();
            String celular = celularField.getText().trim();
            String email = emailField.getText().trim();
            String detalles = detallesField.getText();

            if (nombre.isEmpty() || apellido.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Nombre y Apellido son obligatorios.");
                return;
            }

            if (!email.isEmpty() && !email.contains("@")) {
                JOptionPane.showMessageDialog(mainApp, "Email debe contener @");
                return;
            }

            db.executeTransaction(() -> {
                String sql = "INSERT INTO customer (customer_first_name, customer_surname, phone_number, " +
                        "cellphone_number, email_address, other_customer_details) VALUES (?, ?, ?, ?, ?, ?)";

                db.executeUpdate(sql,
                        nombre,
                        apellido,
                        telefono.isEmpty() ? null : telefono,
                        celular.isEmpty() ? null : celular,
                        email.isEmpty() ? null : email,
                        detalles.isEmpty() ? null : detalles
                );
            });

            JOptionPane.showMessageDialog(mainApp, "Cliente agregado exitosamente.");
            mainApp.mostrarTabla("customer");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //Metodo para agregar empleados
    public void agregarEmpleado() {
        JTextField nombreField = new JTextField();
        JTextField apellidoField = new JTextField();
        JComboBox<String> turnoCombo = new JComboBox<>(new String[]{"morning", "afternoon", "night"});
        JComboBox<String> rolCombo = new JComboBox<>();
        java.util.Map<String, String> rolesMap = new java.util.HashMap<>();

        try {
            ResultSet rsRoles = db.executeQuery(
                    "SELECT staff_role_code, staff_role_description FROM staff_role ORDER BY staff_role_description"
            );

            while (rsRoles.next()) {
                String code = rsRoles.getString("staff_role_code");
                String desc = rsRoles.getString("staff_role_description");
                rolesMap.put(desc, code);
                rolCombo.addItem(desc);
            }
            rsRoles.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainApp, "Error cargando roles: " + e.getMessage());
            return;
        }

        Object[] message = {
                "Nombre:", nombreField,
                "Apellido:", apellidoField,
                "Turno:", turnoCombo,
                "Rol:", rolCombo
        };

        int option = JOptionPane.showConfirmDialog(mainApp, message, "Agregar Empleado", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            String nombre = nombreField.getText();
            String apellido = apellidoField.getText();
            String turno = turnoCombo.getSelectedItem().toString();
            String rolDesc = rolCombo.getSelectedItem().toString();

            if (nombre.isEmpty() || apellido.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Nombre y Apellido son obligatorios.");
                return;
            }

            String rolCode = rolesMap.get(rolDesc);

            db.executeTransaction(() -> {
                String sql = "INSERT INTO staff (staff_first_name, staff_last_name, staff_shift, staff_role_code) " +
                        "VALUES (?, ?, ?, ?)";
                db.executeUpdate(sql, nombre, apellido, turno, rolCode);
            });

            JOptionPane.showMessageDialog(mainApp, "Empleado agregado exitosamente.");
            mainApp.mostrarTabla("staff");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //Metodo para agregar roles de personal
    public void agregarRol() {
        JTextField codigoField = new JTextField();
        JTextField descripcionField = new JTextField();

        Object[] message = {
                "Código:", codigoField,
                "Descripción:", descripcionField
        };

        int option = JOptionPane.showConfirmDialog(mainApp, message, "Agregar Rol", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            String codigo = codigoField.getText();
            String descripcion = descripcionField.getText();

            if (codigo.isEmpty() || descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Código y Descripción son obligatorios.");
                return;
            }

            db.executeTransaction(() -> {
                String sql = "INSERT INTO staff_role (staff_role_code, staff_role_description) VALUES (?, ?)";
                db.executeUpdate(sql, codigo, descripcion);
            });

            JOptionPane.showMessageDialog(mainApp, "Rol agregado exitosamente.");
            mainApp.mostrarTabla("staff_role");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //Metodo para agregar mesas
    public void agregarMesa() {
        JTextField numeroField = new JTextField();
        JSpinner capacidadSpinner = new JSpinner(new javax.swing.SpinnerNumberModel(4, 1, 20, 1));
        JTextField ubicacionField = new JTextField();
        JTextField tipoField = new JTextField();

        Object[] message = {
                "Número de Mesa:", numeroField,
                "Capacidad:", capacidadSpinner,
                "Ubicación:", ubicacionField,
                "Tipo:", tipoField
        };

        int option = JOptionPane.showConfirmDialog(mainApp, message, "Agregar Mesa", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            int numero = Integer.parseInt(numeroField.getText().trim());
            int capacidad = (int) capacidadSpinner.getValue();
            String ubicacion = ubicacionField.getText();
            String tipo = tipoField.getText().trim();

            db.executeTransaction(() -> {
                String sql = "INSERT INTO table_restaurant (table_number, table_capacity, table_location, table_type, table_status) " +
                        "VALUES (?, ?, ?, ?, 'available')";
                db.executeUpdate(sql,
                        numero,
                        capacidad,
                        ubicacion.isEmpty() ? null : ubicacion,
                        tipo.isEmpty() ? null : tipo
                );
            });

            JOptionPane.showMessageDialog(mainApp, "Mesa agregada exitosamente.");
            mainApp.mostrarTabla("table_restaurant");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainApp, "El número de mesa debe ser un número válido.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //Metodo para agregar platillos
    public void agregarPlato() {
        JTextField descripcionField = new JTextField();
        JTextField precioField = new JTextField();

        Object[] message = {
                "Descripción:", descripcionField,
                "Precio:", precioField
        };

        int option = JOptionPane.showConfirmDialog(mainApp, message, "Agregar Platillo", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            String descripcion = descripcionField.getText();
            String precioTexto = precioField.getText().trim();

            if (descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Ingrese una descripción.");
                return;
            }

            if (precioTexto.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Ingrese un precio.");
                return;
            }

            double precio = Double.parseDouble(precioTexto);

            if (precio <= 0) {
                JOptionPane.showMessageDialog(mainApp, "El precio debe ser mayor a 0.");
                return;
            }

            db.executeTransaction(() -> {
                String sql = "INSERT INTO Menu_Item (menu_item_description, menu_item_price) VALUES (?, ?)";
                db.executeUpdate(sql, descripcion, precio);
            });

            JOptionPane.showMessageDialog(mainApp, "Platillo agregado exitosamente.");
            mainApp.mostrarTabla("menu_item");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: El precio debe ser un número válido.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //Metodo para agregar ingredientes
    public void agregarIngrediente() {
        JTextField nombreField = new JTextField();
        JComboBox<String> tipoCombo = new JComboBox<>();
        JTextField expiracionField = new JTextField();
        JComboBox<String> unidadCombo = new JComboBox<>(new String[]{"kg", "g", "l", "ml", "unit", "pcs"});
        java.util.Map<String, String> tiposMap = new java.util.HashMap<>();

        try {
            ResultSet rsTypes = db.executeQuery(
                    "SELECT ingredient_type_code, ingredient_type_description FROM ingredient_type ORDER BY ingredient_type_description"
            );

            while (rsTypes.next()) {
                String code = rsTypes.getString("ingredient_type_code");
                String desc = rsTypes.getString("ingredient_type_description");
                tiposMap.put(desc, code);
                tipoCombo.addItem(desc);
            }
            rsTypes.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainApp, "Error cargando tipos: " + e.getMessage());
            return;
        }

        Object[] message = {
                "Nombre:", nombreField,
                "Tipo:", tipoCombo,
                "Expiración (YYYY-MM-DD):", expiracionField,
                "Unidad de Medida:", unidadCombo
        };

        int option = JOptionPane.showConfirmDialog(mainApp, message, "Agregar Ingrediente", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            String nombre = nombreField.getText();
            String tipoDesc = tipoCombo.getSelectedItem().toString();
            String expiracion = expiracionField.getText().trim();
            String unidad = unidadCombo.getSelectedItem().toString();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Nombre es obligatorio.");
                return;
            }

            String tipoCode = tiposMap.get(tipoDesc);

            db.executeTransaction(() -> {
                String sql = "INSERT INTO ingredients (ingredient_name, ingredient_type_code, ingredient_expiration, ingredient_measure_unit) " +
                        "VALUES (?, ?, ?, ?)";

                java.sql.Date sqlDate = null;
                if (!expiracion.isEmpty()) {
                    sqlDate = java.sql.Date.valueOf(expiracion);
                }

                db.executeUpdate(sql,
                        nombre,
                        tipoCode,
                        sqlDate,
                        unidad.isEmpty() ? null : unidad
                );
            });

            JOptionPane.showMessageDialog(mainApp, "Ingrediente agregado exitosamente.");
            mainApp.mostrarTabla("ingredients");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //Metodo para agregar tipos de ingredientes
    public void agregarTipoIngrediente() {
        JTextField codigoField = new JTextField();
        JTextField descripcionField = new JTextField();

        Object[] message = {
                "Código:", codigoField,
                "Descripción:", descripcionField
        };

        int option = JOptionPane.showConfirmDialog(mainApp, message, "Agregar Tipo de Ingrediente", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;

        try {
            String codigo = codigoField.getText().trim();
            String descripcion = descripcionField.getText();

            if (codigo.isEmpty() || descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Código y Descripción son obligatorios.");
                return;
            }

            db.executeTransaction(() -> {
                String sql = "INSERT INTO ingredient_type (ingredient_type_code, ingredient_type_description) VALUES (?, ?)";
                db.executeUpdate(sql, codigo, descripcion);
            });

            JOptionPane.showMessageDialog(mainApp, "Tipo de ingrediente agregado exitosamente.");
            mainApp.mostrarTabla("ingredient_type");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //Metodo para agregar ingredientes a los platillos
    public void agregarIngredienteAPlatillo() {
        JComboBox<String> platoCombo = new JComboBox<>();
        JComboBox<String> ingredienteCombo = new JComboBox<>();
        JTextField cantidadField = new JTextField("1.0");

        java.util.Map<String, Integer> platosMap = new java.util.HashMap<>();
        java.util.Map<String, Integer> ingredientesMap = new java.util.HashMap<>();

        try {
            // Cargar platillos
            ResultSet rsPlatos = db.executeQuery(
                    "SELECT menu_item_id, menu_item_description FROM menu_item ORDER BY menu_item_description"
            );

            while (rsPlatos.next()) {
                int id = rsPlatos.getInt("menu_item_id");
                String desc = rsPlatos.getString("menu_item_description");
                platosMap.put(desc, id);
                platoCombo.addItem(desc);
            }
            rsPlatos.close();

            if (platosMap.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay platillos disponibles.");
                return;
            }

            // Cargar ingredientes
            ResultSet rsIngredientes = db.executeQuery(
                    "SELECT ingredient_id, ingredient_name, ingredient_measure_unit FROM ingredients ORDER BY ingredient_name"
            );

            while (rsIngredientes.next()) {
                int id = rsIngredientes.getInt("ingredient_id");
                String name = rsIngredientes.getString("ingredient_name");
                String unit = rsIngredientes.getString("ingredient_measure_unit");
                String label = name + (unit != null ? " (" + unit + ")" : "");

                ingredientesMap.put(label, id);
                ingredienteCombo.addItem(label);
            }
            rsIngredientes.close();

            if (ingredientesMap.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay ingredientes disponibles.");
                return;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainApp, "Error cargando datos: " + e.getMessage());
            return;
        }

        Object[] message = {
                "Platillo:", platoCombo,
                "Ingrediente:", ingredienteCombo,
                "Cantidad:", cantidadField
        };

        int option = JOptionPane.showConfirmDialog(mainApp, message,
                "Agregar Ingrediente a Platillo", JOptionPane.OK_CANCEL_OPTION);

        if (option != JOptionPane.OK_OPTION) return;

        try {
            String platoDesc = platoCombo.getSelectedItem().toString();
            String ingredienteLabel = ingredienteCombo.getSelectedItem().toString();
            String cantidadStr = cantidadField.getText().trim();

            if (cantidadStr.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Cantidad es obligatoria.");
                return;
            }

            double cantidad = Double.parseDouble(cantidadStr);

            if (cantidad < 0.01 || cantidad > 999.99) {
                JOptionPane.showMessageDialog(mainApp, "La cantidad debe estar entre 0.01 y 999.99");
                return;
            }

            int menuItemId = platosMap.get(platoDesc);
            int ingredientId = ingredientesMap.get(ingredienteLabel);

            db.executeTransaction(() -> {
                String sql = "INSERT INTO menu_item_ingredient (menu_item_id, ingredient_id, item_quantity) " +
                        "VALUES (?, ?, ?)";
                db.executeUpdate(sql, menuItemId, ingredientId, cantidad);
            });

            JOptionPane.showMessageDialog(mainApp, "Ingrediente agregado al platillo exitosamente.");
            mainApp.mostrarTabla("menu_item_ingredient");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: La cantidad debe ser un número válido.");
        } catch (SQLException ex) {
            if (ex.getMessage().contains("duplicate key") || ex.getMessage().contains("already exists")) {
                JOptionPane.showMessageDialog(mainApp, "Este ingrediente ya está asignado a este platillo.");
            } else {
                JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            }
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}