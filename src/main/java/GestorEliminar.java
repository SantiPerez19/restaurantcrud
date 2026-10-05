import javax.swing.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class GestorEliminar {
    private MainApp mainApp;
    private Database db;

    public GestorEliminar(MainApp mainApp, Database db) {
        this.mainApp = mainApp;
        this.db = db;
    }

    // ==================== ELIMINAR CLIENTE ====================
    public void eliminarCliente() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT customer_id, customer_first_name, customer_surname, email_address FROM customer ORDER BY customer_first_name"
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
                JOptionPane.showMessageDialog(mainApp, "No hay clientes para eliminar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp,
                    "Selecciona el cliente a eliminar:", "Eliminar Cliente",
                    JOptionPane.WARNING_MESSAGE, null, labels.toArray(), labels.get(0));
            if (selected == null) return;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    "¿Estás seguro de eliminar este cliente?\n\n" + selected +
                            "\n\nADVERTENCIA: También se eliminarán sus reservas.",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            int customerId = ids.get(labels.indexOf(selected));

            db.executeTransaction(() -> {
                String sql = "DELETE FROM customer WHERE customer_id = ?";
                db.executeUpdate(sql, customerId);
            });

            JOptionPane.showMessageDialog(mainApp, "Cliente eliminado exitosamente.");
            mainApp.mostrarTabla("customer");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ELIMINAR EMPLEADO ====================
    public void eliminarEmpleado() {
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
                JOptionPane.showMessageDialog(mainApp, "No hay empleados para eliminar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp,
                    "Selecciona el empleado a eliminar:", "Eliminar Empleado",
                    JOptionPane.WARNING_MESSAGE, null, labels.toArray(), labels.get(0));
            if (selected == null) return;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    "¿Estás seguro de eliminar este empleado?\n\n" + selected,
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            int staffId = ids.get(labels.indexOf(selected));

            db.executeTransaction(() -> {
                String sql = "DELETE FROM staff WHERE staff_id = ?";
                db.executeUpdate(sql, staffId);
            });

            JOptionPane.showMessageDialog(mainApp, "Empleado eliminado exitosamente.");
            mainApp.mostrarTabla("staff");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ELIMINAR ROL ====================
    public void eliminarRol() {
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
                JOptionPane.showMessageDialog(mainApp, "No hay roles para eliminar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp,
                    "Selecciona el rol a eliminar:", "Eliminar Rol",
                    JOptionPane.WARNING_MESSAGE, null, labels.toArray(), labels.get(0));
            if (selected == null) return;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    "¿Estás seguro de eliminar este rol?\n\n" + selected +
                            "\n\nADVERTENCIA: Esto afectará a los empleados con este rol.",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            String roleCode = codes.get(labels.indexOf(selected));

            db.executeTransaction(() -> {
                String sql = "DELETE FROM staff_role WHERE staff_role_code = ?";
                db.executeUpdate(sql, roleCode);
            });

            JOptionPane.showMessageDialog(mainApp, "Rol eliminado exitosamente.");
            mainApp.mostrarTabla("staff_role");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ELIMINAR MESA ====================
    public void eliminarMesa() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT table_number, table_capacity, table_location FROM table_restaurant ORDER BY table_number"
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
                JOptionPane.showMessageDialog(mainApp, "No hay mesas para eliminar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp,
                    "Selecciona la mesa a eliminar:", "Eliminar Mesa",
                    JOptionPane.WARNING_MESSAGE, null, labels.toArray(), labels.get(0));
            if (selected == null) return;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    "¿Estás seguro de eliminar esta mesa?\n\n" + selected +
                            "\n\nADVERTENCIA: También se eliminarán las reservas.",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            int tableNum = tableNums.get(labels.indexOf(selected));

            db.executeTransaction(() -> {
                String sql = "DELETE FROM table_restaurant WHERE table_number = ?";
                db.executeUpdate(sql, tableNum);
            });

            JOptionPane.showMessageDialog(mainApp, "Mesa eliminada exitosamente.");
            mainApp.mostrarTabla("table_restaurant");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ELIMINAR RESERVA ====================
    public void eliminarReserva() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT b.booking_id, b.table_number, b.date_of_booking FROM booking b ORDER BY b.date_of_booking DESC LIMIT 50"
            );

            List<Integer> ids = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("booking_id"));
                labels.add("ID: " + rs.getInt("booking_id") +
                        " - Mesa " + rs.getInt("table_number") +
                        " - " + rs.getTimestamp("date_of_booking"));
            }
            rs.close();

            if (ids.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay reservas para eliminar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp,
                    "Selecciona la reserva a eliminar:", "Eliminar Reserva",
                    JOptionPane.WARNING_MESSAGE, null, labels.toArray(), labels.get(0));
            if (selected == null) return;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    "¿Estás seguro de eliminar esta reserva?\n\n" + selected +
                            "\n\nADVERTENCIA: También se eliminarán las órdenes asociadas.",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            int bookingId = ids.get(labels.indexOf(selected));

            db.executeTransaction(() -> {
                String sql = "DELETE FROM booking WHERE booking_id = ?";
                db.executeUpdate(sql, bookingId);
            });

            JOptionPane.showMessageDialog(mainApp, "Reserva eliminada exitosamente.");
            mainApp.mostrarTabla("booking");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ELIMINAR ORDEN ====================
    public void eliminarOrden() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT o.order_id, o.order_date_time, o.order_status FROM order_restaurant o ORDER BY o.order_date_time DESC LIMIT 50"
            );

            List<Integer> ids = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("order_id"));
                labels.add("ID: " + rs.getInt("order_id") +
                        " - " + rs.getTimestamp("order_date_time") +
                        " - " + rs.getString("order_status"));
            }
            rs.close();

            if (ids.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay órdenes para eliminar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp,
                    "Selecciona la orden a eliminar:", "Eliminar Orden",
                    JOptionPane.WARNING_MESSAGE, null, labels.toArray(), labels.get(0));
            if (selected == null) return;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    "¿Estás seguro de eliminar esta orden?\n\n" + selected +
                            "\n\nADVERTENCIA: También se eliminarán los platillos.",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            int orderId = ids.get(labels.indexOf(selected));

            db.executeTransaction(() -> {
                String sql = "DELETE FROM order_restaurant WHERE order_id = ?";
                db.executeUpdate(sql, orderId);
            });

            JOptionPane.showMessageDialog(mainApp, "Orden eliminada exitosamente.");
            mainApp.mostrarTabla("order_restaurant");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ELIMINAR PLATILLO ====================
    public void eliminarPlatillo() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT menu_item_id, menu_item_description, menu_item_price FROM menu_item ORDER BY menu_item_description"
            );

            List<Integer> ids = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("menu_item_id"));
                labels.add("ID: " + rs.getInt("menu_item_id") +
                        " - " + rs.getString("menu_item_description") +
                        " - $" + rs.getBigDecimal("menu_item_price"));
            }
            rs.close();

            if (ids.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay platillos para eliminar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp,
                    "Selecciona el platillo a eliminar:", "Eliminar Platillo",
                    JOptionPane.WARNING_MESSAGE, null, labels.toArray(), labels.get(0));
            if (selected == null) return;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    "¿Estás seguro de eliminar este platillo?\n\n" + selected +
                            "\n\nADVERTENCIA: También se eliminarán sus ingredientes asociados.",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            int itemId = ids.get(labels.indexOf(selected));

            db.executeTransaction(() -> {
                String sql = "DELETE FROM menu_item WHERE menu_item_id = ?";
                db.executeUpdate(sql, itemId);
            });

            JOptionPane.showMessageDialog(mainApp, "Platillo eliminado exitosamente.");
            mainApp.mostrarTabla("menu_item");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ELIMINAR INGREDIENTE ====================
    public void eliminarIngrediente() {
        try {
            ResultSet rs = db.executeQuery(
                    "SELECT ingredient_id, ingredient_name FROM ingredients ORDER BY ingredient_name"
            );

            List<Integer> ids = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (rs.next()) {
                ids.add(rs.getInt("ingredient_id"));
                labels.add("ID: " + rs.getInt("ingredient_id") +
                        " - " + rs.getString("ingredient_name"));
            }
            rs.close();

            if (ids.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay ingredientes para eliminar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp,
                    "Selecciona el ingrediente a eliminar:", "Eliminar Ingrediente",
                    JOptionPane.WARNING_MESSAGE, null, labels.toArray(), labels.get(0));
            if (selected == null) return;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    "¿Estás seguro de eliminar este ingrediente?\n\n" + selected +
                            "\n\nADVERTENCIA: Se eliminará de todos los platillos.",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            int ingId = ids.get(labels.indexOf(selected));

            db.executeTransaction(() -> {
                String sql = "DELETE FROM ingredients WHERE ingredient_id = ?";
                db.executeUpdate(sql, ingId);
            });

            JOptionPane.showMessageDialog(mainApp, "Ingrediente eliminado exitosamente.");
            mainApp.mostrarTabla("ingredients");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ELIMINAR TIPO DE INGREDIENTE ====================
    public void eliminarTipoIngrediente() {
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
                JOptionPane.showMessageDialog(mainApp, "No hay tipos para eliminar.");
                return;
            }

            Object selected = JOptionPane.showInputDialog(mainApp,
                    "Selecciona el tipo a eliminar:", "Eliminar Tipo de Ingrediente",
                    JOptionPane.WARNING_MESSAGE, null, labels.toArray(), labels.get(0));
            if (selected == null) return;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    "¿Estás seguro de eliminar este tipo?\n\n" + selected +
                            "\n\nADVERTENCIA: Esto afectará a los ingredientes con este tipo.",
                    "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

            String typeCode = codes.get(labels.indexOf(selected));

            db.executeTransaction(() -> {
                String sql = "DELETE FROM ingredient_type WHERE ingredient_type_code = ?";
                db.executeUpdate(sql, typeCode);
            });

            JOptionPane.showMessageDialog(mainApp, "Tipo de ingrediente eliminado exitosamente.");
            mainApp.mostrarTabla("ingredient_type");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== ELIMINAR INGREDIENTE DE PLATILLO ====================
    public void eliminarIngredienteDePlatillo() {
        try {
            // LISTAR RELACIONES EXISTENTES
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

                labels.add(platoDesc + " → " + ingName + " (" + qty + " " +
                        (unit != null ? unit : "unidad") + ")");
            }
            rs.close();

            if (labels.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay ingredientes asignados a platillos para eliminar.");
                return;
            }

            Object chosen = JOptionPane.showInputDialog(mainApp,
                    "Selecciona el ingrediente a eliminar del platillo:",
                    "Eliminar Ingrediente de Platillo",
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    labels.toArray(),
                    labels.get(0));

            if (chosen == null) return;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    "¿Estás seguro de eliminar este ingrediente del platillo?\n\n" + chosen,
                    "Confirmar Eliminación",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) return;

            int index = labels.indexOf(chosen);
            int menuItemId = menuItemIds.get(index);
            int ingredientId = ingredientIds.get(index);

            // ELIMINAR CON TRANSACCIÓN
            final int finalMenuItemId = menuItemId;
            final int finalIngredientId = ingredientId;

            db.executeTransaction(() -> {
                String sql = "DELETE FROM menu_item_ingredient " +
                        "WHERE menu_item_id = ? AND ingredient_id = ?";
                db.executeUpdate(sql, finalMenuItemId, finalIngredientId);
            });

            JOptionPane.showMessageDialog(mainApp, "Ingrediente eliminado del platillo exitosamente.");
            mainApp.mostrarTabla("menu_item_ingredient");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}