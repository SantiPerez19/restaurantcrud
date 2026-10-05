import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorActualizar {
    private MainApp mainApp;
    private Database db;

    public GestorActualizar(MainApp mainApp, Database db) {
        this.mainApp = mainApp;
        this.db = db;
    }

    // ==================== ACTUALIZAR CLIENTE ====================
    public void actualizarCliente() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT customer_id, customer_first_name, customer_surname FROM customer ORDER BY customer_first_name"
            );

            List<Integer> ids = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("customer_id"));
                labels.add("ID: " + rs.getInt("customer_id") + " - " +
                        rs.getString("customer_first_name") + " " +
                        rs.getString("customer_surname"));
            }
            rs.close();

            if (ids.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay clientes para actualizar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp, "Selecciona el cliente:",
                    "Actualizar Cliente", JOptionPane.QUESTION_MESSAGE, null,
                    labels.toArray(), labels.get(0));
            if (selected == null) return;

            int customerId = ids.get(labels.indexOf(selected));
            ResultSet rsCliente = db.executeQuery(
                    "SELECT * FROM customer WHERE customer_id = ?", customerId
            );

            if (!rsCliente.next()) {
                rsCliente.close();
                return;
            }

            JTextField nombreField = new JTextField(rsCliente.getString("customer_first_name"));
            JTextField apellidoField = new JTextField(rsCliente.getString("customer_surname"));
            JTextField telefonoField = new JTextField(
                    rsCliente.getString("phone_number") == null ? "" : rsCliente.getString("phone_number")
            );
            JTextField celularField = new JTextField(
                    rsCliente.getString("cellphone_number") == null ? "" : rsCliente.getString("cellphone_number")
            );
            JTextField emailField = new JTextField(
                    rsCliente.getString("email_address") == null ? "" : rsCliente.getString("email_address")
            );
            rsCliente.close();

            Object[] message = {
                    "Nombre:", nombreField,
                    "Apellido:", apellidoField,
                    "Teléfono:", telefonoField,
                    "Celular:", celularField,
                    "Email:", emailField
            };

            int option = JOptionPane.showConfirmDialog(mainApp, message,
                    "Actualizar Cliente", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;

            String nombre = nombreField.getText().trim();
            String apellido = apellidoField.getText().trim();
            String telefono = telefonoField.getText().trim();
            String celular = celularField.getText().trim();
            String email = emailField.getText().trim();

            if (nombre.isEmpty() || apellido.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Nombre y Apellido son obligatorios.");
                return;
            }

            if (!email.isEmpty() && !email.contains("@")) {
                JOptionPane.showMessageDialog(mainApp, "Email debe contener @");
                return;
            }

            db.executeTransaction(() -> {
                String sql = "UPDATE customer SET customer_first_name = ?, customer_surname = ?, " +
                        "phone_number = ?, cellphone_number = ?, email_address = ? " +
                        "WHERE customer_id = ?";
                db.executeUpdate(sql,
                        nombre,
                        apellido,
                        telefono.isEmpty() ? null : telefono,
                        celular.isEmpty() ? null : celular,
                        email.isEmpty() ? null : email,
                        customerId
                );
            });

            JOptionPane.showMessageDialog(mainApp, "Cliente actualizado exitosamente.");
            mainApp.mostrarTabla("customer");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ACTUALIZAR EMPLEADO ====================
    public void actualizarEmpleado() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT staff_id, staff_first_name, staff_last_name FROM staff ORDER BY staff_first_name"
            );

            List<Integer> ids = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("staff_id"));
                labels.add("ID: " + rs.getInt("staff_id") + " - " +
                        rs.getString("staff_first_name") + " " +
                        rs.getString("staff_last_name"));
            }
            rs.close();

            if (ids.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay empleados para actualizar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp, "Selecciona el empleado:",
                    "Actualizar Empleado", JOptionPane.QUESTION_MESSAGE, null,
                    labels.toArray(), labels.get(0));
            if (selected == null) return;

            int staffId = ids.get(labels.indexOf(selected));
            ResultSet rsStaff = db.executeQuery(
                    "SELECT * FROM staff WHERE staff_id = ?", staffId
            );

            if (!rsStaff.next()) {
                rsStaff.close();
                return;
            }

            // Cargar roles
            ResultSet rsRoles = db.executeQuery(
                    "SELECT staff_role_code, staff_role_description FROM staff_role ORDER BY staff_role_description"
            );

            Map<String, String> rolesMap = new HashMap<>();
            List<String> roleLabels = new ArrayList<>();

            while (rsRoles.next()) {
                String code = rsRoles.getString("staff_role_code");
                String desc = rsRoles.getString("staff_role_description");
                rolesMap.put(desc, code);
                roleLabels.add(desc);
            }
            rsRoles.close();

            JTextField nombreField = new JTextField(rsStaff.getString("staff_first_name"));
            JTextField apellidoField = new JTextField(rsStaff.getString("staff_last_name"));
            JComboBox<String> turnoCombo = new JComboBox<>(new String[]{"morning", "afternoon", "night"});
            turnoCombo.setSelectedItem(rsStaff.getString("staff_shift"));
            JComboBox<String> rolCombo = new JComboBox<>(roleLabels.toArray(new String[0]));

            String currentRoleCode = rsStaff.getString("staff_role_code");
            rsStaff.close();

            // Seleccionar el rol actual
            for (Map.Entry<String, String> entry : rolesMap.entrySet()) {
                if (entry.getValue().equals(currentRoleCode)) {
                    rolCombo.setSelectedItem(entry.getKey());
                    break;
                }
            }

            Object[] message = {
                    "Nombre:", nombreField,
                    "Apellido:", apellidoField,
                    "Turno:", turnoCombo,
                    "Rol:", rolCombo
            };

            int option = JOptionPane.showConfirmDialog(mainApp, message,
                    "Actualizar Empleado", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;

            String nombre = nombreField.getText().trim();
            String apellido = apellidoField.getText().trim();
            String turno = turnoCombo.getSelectedItem().toString();
            String rolDesc = rolCombo.getSelectedItem().toString();
            String selectedRoleCode = rolesMap.get(rolDesc);

            if (nombre.isEmpty() || apellido.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Nombre y Apellido son obligatorios.");
                return;
            }

            db.executeTransaction(() -> {
                String sql = "UPDATE staff SET staff_first_name = ?, staff_last_name = ?, " +
                        "staff_shift = ?, staff_role_code = ? WHERE staff_id = ?";
                db.executeUpdate(sql, nombre, apellido, turno, selectedRoleCode, staffId);
            });

            JOptionPane.showMessageDialog(mainApp, "Empleado actualizado exitosamente.");
            mainApp.mostrarTabla("staff");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ACTUALIZAR ROL ====================
    public void actualizarRol() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT staff_role_code, staff_role_description FROM staff_role ORDER BY staff_role_description"
            );

            List<String> codes = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                codes.add(rs.getString("staff_role_code"));
                labels.add(rs.getString("staff_role_code") + " - " +
                        rs.getString("staff_role_description"));
            }
            rs.close();

            if (codes.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay roles para actualizar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp, "Selecciona el rol:",
                    "Actualizar Rol", JOptionPane.QUESTION_MESSAGE, null,
                    labels.toArray(), labels.get(0));
            if (selected == null) return;

            String roleCode = codes.get(labels.indexOf(selected));
            ResultSet rsRole = db.executeQuery(
                    "SELECT * FROM staff_role WHERE staff_role_code = ?", roleCode
            );

            if (!rsRole.next()) {
                rsRole.close();
                return;
            }

            JTextField descripcionField = new JTextField(rsRole.getString("staff_role_description"));
            rsRole.close();

            Object[] message = {
                    "Código: " + roleCode + " (no modificable)",
                    "Descripción:", descripcionField
            };

            int option = JOptionPane.showConfirmDialog(mainApp, message,
                    "Actualizar Rol", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;

            String descripcion = descripcionField.getText().trim();
            if (descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Descripción es obligatoria.");
                return;
            }

            db.executeTransaction(() -> {
                String sql = "UPDATE staff_role SET staff_role_description = ? WHERE staff_role_code = ?";
                db.executeUpdate(sql, descripcion, roleCode);
            });

            JOptionPane.showMessageDialog(mainApp, "Rol actualizado exitosamente.");
            mainApp.mostrarTabla("staff_role");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ACTUALIZAR MESA ====================
    public void actualizarMesa() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT table_number, table_capacity FROM table_restaurant ORDER BY table_number"
            );

            List<Integer> tableNums = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                tableNums.add(rs.getInt("table_number"));
                labels.add("Mesa " + rs.getInt("table_number") +
                        " - Cap: " + rs.getInt("table_capacity"));
            }
            rs.close();

            if (tableNums.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay mesas para actualizar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp, "Selecciona la mesa:",
                    "Actualizar Mesa", JOptionPane.QUESTION_MESSAGE, null,
                    labels.toArray(), labels.get(0));
            if (selected == null) return;

            int tableNum = tableNums.get(labels.indexOf(selected));
            ResultSet rsMesa = db.executeQuery(
                    "SELECT * FROM table_restaurant WHERE table_number = ?", tableNum
            );

            if (!rsMesa.next()) {
                rsMesa.close();
                return;
            }

            JSpinner capacidadSpinner = new JSpinner(
                    new javax.swing.SpinnerNumberModel(rsMesa.getInt("table_capacity"), 1, 20, 1)
            );
            JTextField ubicacionField = new JTextField(
                    rsMesa.getString("table_location") == null ? "" : rsMesa.getString("table_location")
            );
            JTextField tipoField = new JTextField(
                    rsMesa.getString("table_type") == null ? "" : rsMesa.getString("table_type")
            );
            JComboBox<String> estadoCombo = new JComboBox<>(
                    new String[]{"available", "occupied", "reserved", "maintenance"}
            );
            estadoCombo.setSelectedItem(rsMesa.getString("table_status"));
            rsMesa.close();

            Object[] message = {
                    "Mesa Nº: " + tableNum + " (no modificable)",
                    "Capacidad:", capacidadSpinner,
                    "Ubicación:", ubicacionField,
                    "Tipo:", tipoField,
                    "Estado:", estadoCombo
            };

            int option = JOptionPane.showConfirmDialog(mainApp, message,
                    "Actualizar Mesa", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;

            int capacidad = (int) capacidadSpinner.getValue();
            String ubicacion = ubicacionField.getText().trim();
            String tipo = tipoField.getText().trim();
            String estado = estadoCombo.getSelectedItem().toString();

            db.executeTransaction(() -> {
                String sql = "UPDATE table_restaurant SET table_capacity = ?, table_location = ?, " +
                        "table_type = ?, table_status = ? WHERE table_number = ?";
                db.executeUpdate(sql,
                        capacidad,
                        ubicacion.isEmpty() ? null : ubicacion,
                        tipo.isEmpty() ? null : tipo,
                        estado,
                        tableNum
                );
            });

            JOptionPane.showMessageDialog(mainApp, "Mesa actualizada exitosamente.");
            mainApp.mostrarTabla("table_restaurant");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ACTUALIZAR PLATILLO ====================
    public void actualizarPlato() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT menu_item_id, menu_item_description, menu_item_price FROM menu_item ORDER BY menu_item_id"
            );

            List<Integer> ids = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("menu_item_id"));
                labels.add("ID: " + rs.getInt("menu_item_id") + " - " +
                        rs.getString("menu_item_description") +
                        " ($" + rs.getDouble("menu_item_price") + ")");
            }
            rs.close();

            if (ids.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay platillos para actualizar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp, "Selecciona el platillo:",
                    "Actualizar Platillo", JOptionPane.QUESTION_MESSAGE, null,
                    labels.toArray(), labels.get(0));
            if (selected == null) return;

            int menuItemId = ids.get(labels.indexOf(selected));
            ResultSet rsPlato = db.executeQuery(
                    "SELECT * FROM menu_item WHERE menu_item_id = ?", menuItemId
            );

            if (!rsPlato.next()) {
                rsPlato.close();
                return;
            }

            JTextField descripcionField = new JTextField(rsPlato.getString("menu_item_description"));
            JTextField precioField = new JTextField(String.valueOf(rsPlato.getDouble("menu_item_price")));
            rsPlato.close();

            Object[] message = {
                    "Descripción:", descripcionField,
                    "Precio:", precioField
            };

            int option = JOptionPane.showConfirmDialog(mainApp, message,
                    "Actualizar Platillo", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;

            String descripcion = descripcionField.getText().trim();
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
                String sql = "UPDATE menu_item SET menu_item_description = ?, menu_item_price = ? " +
                        "WHERE menu_item_id = ?";
                db.executeUpdate(sql, descripcion, precio, menuItemId);
            });

            JOptionPane.showMessageDialog(mainApp, "Platillo actualizado exitosamente.");
            mainApp.mostrarTabla("menu_item");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: El precio debe ser un número válido.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ACTUALIZAR INGREDIENTE ====================
    public void actualizarIngrediente() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT ingredient_id, ingredient_name FROM ingredients ORDER BY ingredient_name"
            );

            List<Integer> ids = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("ingredient_id"));
                labels.add("ID: " + rs.getInt("ingredient_id") + " - " +
                        rs.getString("ingredient_name"));
            }
            rs.close();

            if (ids.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay ingredientes para actualizar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp, "Selecciona el ingrediente:",
                    "Actualizar Ingrediente", JOptionPane.QUESTION_MESSAGE, null,
                    labels.toArray(), labels.get(0));
            if (selected == null) return;

            int ingId = ids.get(labels.indexOf(selected));
            ResultSet rsIng = db.executeQuery(
                    "SELECT * FROM ingredients WHERE ingredient_id = ?", ingId
            );

            if (!rsIng.next()) {
                rsIng.close();
                return;
            }

            // Cargar tipos
            ResultSet rsTypes = db.executeQuery(
                    "SELECT ingredient_type_code, ingredient_type_description FROM ingredient_type ORDER BY ingredient_type_description"
            );

            Map<String, String> tiposMap = new HashMap<>();
            List<String> typeLabels = new ArrayList<>();

            while (rsTypes.next()) {
                String code = rsTypes.getString("ingredient_type_code");
                String desc = rsTypes.getString("ingredient_type_description");
                tiposMap.put(desc, code);
                typeLabels.add(desc);
            }
            rsTypes.close();

            JTextField nombreField = new JTextField(rsIng.getString("ingredient_name"));
            JComboBox<String> tipoCombo = new JComboBox<>(typeLabels.toArray(new String[0]));

            String currentTypeCode = rsIng.getString("ingredient_type_code");

            // Seleccionar el tipo actual
            for (Map.Entry<String, String> entry : tiposMap.entrySet()) {
                if (entry.getValue().equals(currentTypeCode)) {
                    tipoCombo.setSelectedItem(entry.getKey());
                    break;
                }
            }

            JTextField expiracionField = new JTextField(
                    rsIng.getString("ingredient_expiration") == null ? "" : rsIng.getString("ingredient_expiration")
            );

            String[] unidades = {"kg", "g", "l", "ml", "unit", "pcs"};
            JComboBox<String> unidadCombo = new JComboBox<>(unidades);

            String currentUnit = rsIng.getString("ingredient_measure_unit");
            if (currentUnit != null && !currentUnit.isEmpty()) {
                unidadCombo.setSelectedItem(currentUnit);
            }

            rsIng.close();

            Object[] message = {
                    "Nombre:", nombreField,
                    "Tipo:", tipoCombo,
                    "Expiración (YYYY-MM-DD):", expiracionField,
                    "Unidad:", unidadCombo
            };

            int option = JOptionPane.showConfirmDialog(mainApp, message,
                    "Actualizar Ingrediente", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;

            String nombre = nombreField.getText().trim();
            String tipoDesc = tipoCombo.getSelectedItem().toString();
            String selectedTypeCode = tiposMap.get(tipoDesc);
            String expiracion = expiracionField.getText().trim();
            String unidad = unidadCombo.getSelectedItem().toString();

            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Nombre es obligatorio.");
                return;
            }

            db.executeTransaction(() -> {
                String sql = "UPDATE ingredients SET ingredient_name = ?, ingredient_type_code = ?, " +
                        "ingredient_expiration = ?, ingredient_measure_unit = ? WHERE ingredient_id = ?";

                java.sql.Date sqlDate = null;
                if (!expiracion.isEmpty()) {
                    sqlDate = java.sql.Date.valueOf(expiracion);
                }

                db.executeUpdate(sql,
                        nombre,
                        selectedTypeCode,
                        sqlDate,
                        unidad,
                        ingId
                );
            });

            JOptionPane.showMessageDialog(mainApp, "Ingrediente actualizado exitosamente.");
            mainApp.mostrarTabla("ingredients");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ACTUALIZAR TIPO DE INGREDIENTE ====================
    public void actualizarTipoIngrediente() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT ingredient_type_code, ingredient_type_description FROM ingredient_type ORDER BY ingredient_type_description"
            );

            List<String> codes = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                codes.add(rs.getString("ingredient_type_code"));
                labels.add(rs.getString("ingredient_type_code") + " - " +
                        rs.getString("ingredient_type_description"));
            }
            rs.close();

            if (codes.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay tipos para actualizar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp, "Selecciona el tipo:",
                    "Actualizar Tipo", JOptionPane.QUESTION_MESSAGE, null,
                    labels.toArray(), labels.get(0));
            if (selected == null) return;

            String typeCode = codes.get(labels.indexOf(selected));
            ResultSet rsType = db.executeQuery(
                    "SELECT * FROM ingredient_type WHERE ingredient_type_code = ?", typeCode
            );

            if (!rsType.next()) {
                rsType.close();
                return;
            }

            JTextField descripcionField = new JTextField(rsType.getString("ingredient_type_description"));
            rsType.close();

            Object[] message = {
                    "Código: " + typeCode + " (no modificable)",
                    "Descripción:", descripcionField
            };

            int option = JOptionPane.showConfirmDialog(mainApp, message,
                    "Actualizar Tipo", JOptionPane.OK_CANCEL_OPTION);
            if (option != JOptionPane.OK_OPTION) return;

            String descripcion = descripcionField.getText().trim();
            if (descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Descripción es obligatoria.");
                return;
            }

            db.executeTransaction(() -> {
                String sql = "UPDATE ingredient_type SET ingredient_type_description = ? " +
                        "WHERE ingredient_type_code = ?";
                db.executeUpdate(sql, descripcion, typeCode);
            });

            JOptionPane.showMessageDialog(mainApp, "Tipo actualizado exitosamente.");
            mainApp.mostrarTabla("ingredient_type");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ACTUALIZAR INGREDIENTE DE PLATILLO ====================
    public void actualizarIngredienteDePlatillo() {
        try {
            // 1. LISTAR RELACIONES EXISTENTES
            ResultSet rs = db.executeQuery(
                    "SELECT " +
                            "    mii.menu_item_id, " +
                            "    mii.ingredient_id, " +
                            "    mii.item_quantity, " +
                            "    mi.menu_item_description, " +
                            "    i.ingredient_name, " +
                            "    i.ingredient_measure_unit " +
                            "FROM menu_item_ingredient mii " +
                            "JOIN menu_item mi ON mii.menu_item_id = mi.menu_item_id " +
                            "JOIN ingredients i ON mii.ingredient_id = i.ingredient_id " +
                            "ORDER BY mi.menu_item_description, i.ingredient_name"
            );

            java.util.List<Integer> menuItemIds = new java.util.ArrayList<>();
            java.util.List<Integer> ingredientIds = new java.util.ArrayList<>();
            java.util.List<Double> cantidades = new java.util.ArrayList<>();
            java.util.List<String> labels = new java.util.ArrayList<>();

            while (rs.next()) {
                int menuId = rs.getInt("menu_item_id");
                int ingId = rs.getInt("ingredient_id");
                double qty = rs.getDouble("item_quantity");
                String platoDesc = rs.getString("menu_item_description");
                String ingName = rs.getString("ingredient_name");
                String unit = rs.getString("ingredient_measure_unit");

                menuItemIds.add(menuId);
                ingredientIds.add(ingId);
                cantidades.add(qty);

                labels.add(platoDesc + " → " + ingName + " (" + qty + " " +
                        (unit != null ? unit : "unidad") + ")");
            }
            rs.close();

            if (labels.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay ingredientes asignados a platillos.");
                return;
            }

            Object chosen = JOptionPane.showInputDialog(mainApp,
                    "Selecciona la relación a actualizar:",
                    "Actualizar Ingrediente de Platillo",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    labels.toArray(),
                    labels.get(0));

            if (chosen == null) return;

            int index = labels.indexOf(chosen);
            int oldMenuItemId = menuItemIds.get(index);
            int oldIngredientId = ingredientIds.get(index);
            double cantidadActual = cantidades.get(index);

            // 2. CARGAR DATOS PARA COMBOS
            JComboBox<String> platoCombo = new JComboBox<>();
            JComboBox<String> ingredienteCombo = new JComboBox<>();
            JTextField cantidadField = new JTextField(String.valueOf(cantidadActual));

            java.util.Map<String, Integer> platosMap = new java.util.HashMap<>();
            java.util.Map<String, Integer> ingredientesMap = new java.util.HashMap<>();

            // Cargar platillos
            ResultSet rsPlatos = db.executeQuery(
                    "SELECT menu_item_id, menu_item_description FROM menu_item ORDER BY menu_item_description"
            );

            String platoSeleccionado = null;
            while (rsPlatos.next()) {
                int id = rsPlatos.getInt("menu_item_id");
                String desc = rsPlatos.getString("menu_item_description");
                platosMap.put(desc, id);
                platoCombo.addItem(desc);

                if (id == oldMenuItemId) {
                    platoSeleccionado = desc;
                }
            }
            rsPlatos.close();

            if (platoSeleccionado != null) {
                platoCombo.setSelectedItem(platoSeleccionado);
            }

            // Cargar ingredientes
            ResultSet rsIngredientes = db.executeQuery(
                    "SELECT ingredient_id, ingredient_name, ingredient_measure_unit FROM ingredients ORDER BY ingredient_name"
            );

            String ingredienteSeleccionado = null;
            while (rsIngredientes.next()) {
                int id = rsIngredientes.getInt("ingredient_id");
                String name = rsIngredientes.getString("ingredient_name");
                String unit = rsIngredientes.getString("ingredient_measure_unit");
                String label = name + (unit != null ? " (" + unit + ")" : "");

                ingredientesMap.put(label, id);
                ingredienteCombo.addItem(label);

                if (id == oldIngredientId) {
                    ingredienteSeleccionado = label;
                }
            }
            rsIngredientes.close();

            if (ingredienteSeleccionado != null) {
                ingredienteCombo.setSelectedItem(ingredienteSeleccionado);
            }

            Object[] message = {
                    "Platillo:", platoCombo,
                    "Ingrediente:", ingredienteCombo,
                    "Cantidad:", cantidadField
            };

            int result = JOptionPane.showConfirmDialog(mainApp,
                    message,
                    "Actualizar Relación",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) return;

            String platoDesc = platoCombo.getSelectedItem().toString();
            String ingredienteLabel = ingredienteCombo.getSelectedItem().toString();
            String cantidadStr = cantidadField.getText().trim();

            if (cantidadStr.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Cantidad es obligatoria.");
                return;
            }

            double nuevaCantidad = Double.parseDouble(cantidadStr);

            if (nuevaCantidad < 0.01 || nuevaCantidad > 999.99) {
                JOptionPane.showMessageDialog(mainApp, "La cantidad debe estar entre 0.01 y 999.99");
                return;
            }

            int newMenuItemId = platosMap.get(platoDesc);
            int newIngredientId = ingredientesMap.get(ingredienteLabel);

            // 3. ACTUALIZAR CON TRANSACCIÓN
            final int finalOldMenuId = oldMenuItemId;
            final int finalOldIngId = oldIngredientId;
            final int finalNewMenuId = newMenuItemId;
            final int finalNewIngId = newIngredientId;
            final double finalCantidad = nuevaCantidad;

            db.executeTransaction(() -> {
                // Si cambió el platillo o ingrediente, eliminar el viejo e insertar el nuevo
                if (finalOldMenuId != finalNewMenuId || finalOldIngId != finalNewIngId) {
                    // Verificar que la nueva combinación no exista
                    ResultSet rsCheck = db.executeQuery(
                            "SELECT 1 FROM menu_item_ingredient WHERE menu_item_id = ? AND ingredient_id = ?",
                            finalNewMenuId, finalNewIngId
                    );

                    if (rsCheck.next()) {
                        rsCheck.close();
                        throw new SQLException("La nueva combinación de platillo e ingrediente ya existe.");
                    }
                    rsCheck.close();

                    // Eliminar el registro viejo
                    String sqlDelete = "DELETE FROM menu_item_ingredient " +
                            "WHERE menu_item_id = ? AND ingredient_id = ?";
                    db.executeUpdate(sqlDelete, finalOldMenuId, finalOldIngId);

                    // Insertar el nuevo
                    String sqlInsert = "INSERT INTO menu_item_ingredient (menu_item_id, ingredient_id, item_quantity) " +
                            "VALUES (?, ?, ?)";
                    db.executeUpdate(sqlInsert, finalNewMenuId, finalNewIngId, finalCantidad);
                } else {
                    // Solo cambió la cantidad, hacer UPDATE simple
                    String sqlUpdate = "UPDATE menu_item_ingredient SET item_quantity = ? " +
                            "WHERE menu_item_id = ? AND ingredient_id = ?";
                    db.executeUpdate(sqlUpdate, finalCantidad, finalNewMenuId, finalNewIngId);
                }
            });

            JOptionPane.showMessageDialog(mainApp, "Relación actualizada exitosamente!");
            mainApp.mostrarTabla("menu_item_ingredient");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: La cantidad debe ser un número válido.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}