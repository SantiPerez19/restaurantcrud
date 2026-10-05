import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GestorOperaciones {
    private MainApp mainApp;
    private Database db;

    public GestorOperaciones(MainApp mainApp, Database db) {
        this.mainApp = mainApp;
        this.db = db;
    }

    private int[] showMultiSelectList(java.util.List<String> labels, String title) {
        //Crea una lista que permite seleccionar varios items
        //Esto sera para que se puedan ordenar varias comidas al mismo tiempo, y no una por una.
        JList<String> list = new JList<>(labels.toArray(new String[0]));
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //Se pone la lista en un ScrollPane para que se puedan revisar las comidas a ordenar y confirmar
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(400, 300));
        //Se confirma si se quiere realizar la orden o no
        int resp = JOptionPane.showConfirmDialog(mainApp, scroll, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resp != JOptionPane.OK_OPTION) return null;
        //Regresa los indices de lo que se selecciono
        return list.getSelectedIndices();

    }

    //Tomar orden, crea booking/reserva si hace falta, crea order_restaurant (pending) y order_menu_item rows
    public void tomarOrden() {
        try {
            //Cargar menu items
            ResultSet rsMenu = db.executeQuery(
                    "SELECT menu_item_id, menu_item_description, menu_item_price FROM menu_item ORDER BY menu_item_id"
            );

            java.util.List<Integer> menuIds = new java.util.ArrayList<>();
            java.util.List<String> menuLabels = new java.util.ArrayList<>();

            while (rsMenu.next()) {
                int id = rsMenu.getInt("menu_item_id");
                String desc = rsMenu.getString("menu_item_description");
                java.math.BigDecimal price = rsMenu.getBigDecimal("menu_item_price");
                menuIds.add(id);
                menuLabels.add(id + " - " + desc + " ($" + price + ")");
            }
            rsMenu.close();

            if (menuIds.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay platillos en el menú.");
                return;
            }

            //Selección múltiple de platillos
            int[] selectedIdx = showMultiSelectList(menuLabels, "Selecciona platillos (Ctrl+click para varios)");
            if (selectedIdx == null || selectedIdx.length == 0) return;

            //Pedir cantidades y comentarios
            class LineItem {
                int menuId;
                int qty;
                String comments;
            }

            java.util.List<LineItem> lineItems = new java.util.ArrayList<>();
            for (int idx : selectedIdx) {
                int menuId = menuIds.get(idx);
                String menuLabel = menuLabels.get(idx);

                String qtyStr = JOptionPane.showInputDialog(mainApp, "Cantidad para:\n" + menuLabel, "1");
                if (qtyStr == null) return;

                int qty;
                try {
                    qty = Integer.parseInt(qtyStr.trim());
                    if (qty < 1) qty = 1;
                } catch (Exception ex) {
                    qty = 1;
                }

                String comments = JOptionPane.showInputDialog(mainApp, "Comentario (opcional) para:\n" + menuLabel, "");

                LineItem li = new LineItem();
                li.menuId = menuId;
                li.qty = qty;
                li.comments = (comments == null ? "" : comments);
                lineItems.add(li);
            }

            //Elegir mesa
            ResultSet rsTables = db.executeQuery(
                    "SELECT table_number, table_capacity, table_location FROM table_restaurant ORDER BY table_number"
            );

            java.util.List<Integer> tableNums = new java.util.ArrayList<>();
            java.util.List<String> tableLabels = new java.util.ArrayList<>();

            while (rsTables.next()) {
                int tnum = rsTables.getInt("table_number");
                tableNums.add(tnum);
                tableLabels.add("Mesa " + tnum +
                        " (cap: " + rsTables.getInt("table_capacity") +
                        ") - " + rsTables.getString("table_location"));
            }
            rsTables.close();

            if (tableNums.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay mesas definidas.");
                return;
            }

            Object chosenTable = JOptionPane.showInputDialog(mainApp,
                    "Selecciona mesa:", "Mesa",
                    JOptionPane.QUESTION_MESSAGE, null,
                    tableLabels.toArray(), tableLabels.get(0));
            if (chosenTable == null) return;

            int tableIndex = tableLabels.indexOf(chosenTable);
            int tableNumber = tableNums.get(tableIndex);

            //Buscar reserva o crear nueva
            java.util.List<Integer> bookingIds = new java.util.ArrayList<>();
            java.util.List<String> bookingLabels = new java.util.ArrayList<>();

            ResultSet rsBooking = db.executeQuery(
                    "SELECT booking_id, customer_id, date_of_booking FROM booking " +
                            "WHERE table_number = ? ORDER BY date_of_booking DESC LIMIT 20",
                    tableNumber
            );

            while (rsBooking.next()) {
                int bid = rsBooking.getInt("booking_id");
                bookingIds.add(bid);
                bookingLabels.add("Booking " + bid +
                        " - " + rsBooking.getTimestamp("date_of_booking") +
                        " (customer:" + rsBooking.getString("customer_id") + ")");
            }
            rsBooking.close();

            bookingLabels.add(0, "Crear nueva reserva ahora (NOW)");

            Object chosenBooking = JOptionPane.showInputDialog(mainApp,
                    "Reserva para la mesa:", "Reserva",
                    JOptionPane.QUESTION_MESSAGE, null,
                    bookingLabels.toArray(), bookingLabels.get(0));
            if (chosenBooking == null) return;

            int chosenIdx = bookingLabels.indexOf(chosenBooking);
            Integer bookingId = null;

            if (chosenIdx == 0) {
                //Crear nueva reserva con transacción
                final int finalTableNumber = tableNumber;
                db.executeTransaction(() -> {
                    String sql = "INSERT INTO booking (table_number, customer_id, date_of_booking, number_in_party) " +
                            "VALUES (?, NULL, NOW(), 1)";
                    db.executeUpdate(sql, finalTableNumber);
                });

                //Obtener el ID de la reserva recién creada
                ResultSet rsNewBooking = db.executeQuery(
                        "SELECT booking_id FROM booking WHERE table_number = ? ORDER BY booking_id DESC LIMIT 1",
                        tableNumber
                );
                if (rsNewBooking.next()) {
                    bookingId = rsNewBooking.getInt("booking_id");
                }
                rsNewBooking.close();
            } else {
                bookingId = bookingIds.get(chosenIdx - 1);
            }

            if (bookingId == null) {
                JOptionPane.showMessageDialog(mainApp, "No se pudo crear/obtener reserva.");
                return;
            }

            //Elegir empleado que toma la orden
            java.util.List<Integer> staffIds = new java.util.ArrayList<>();
            java.util.List<String> staffLabels = new java.util.ArrayList<>();

            ResultSet rsStaff = db.executeQuery(
                    "SELECT staff_id, staff_first_name || ' ' || staff_last_name AS name FROM staff ORDER BY name"
            );

            staffLabels.add("Sin asignar");

            while (rsStaff.next()) {
                staffIds.add(rsStaff.getInt("staff_id"));
                staffLabels.add(rsStaff.getString("name"));
            }
            rsStaff.close();

            Object chosenStaff = JOptionPane.showInputDialog(mainApp,
                    "Empleado que toma la orden (opcional):", "Empleado",
                    JOptionPane.QUESTION_MESSAGE, null,
                    staffLabels.toArray(), staffLabels.get(0));
            if (chosenStaff == null) return;

            Integer staffId = null;
            int staffIdx = staffLabels.indexOf(chosenStaff);
            if (staffIdx > 0) staffId = staffIds.get(staffIdx - 1);

            //Se crea la orden usando transacción
            final Integer finalBookingId = bookingId;
            final Integer finalStaffId = staffId;
            final java.util.List<LineItem> finalLineItems = lineItems;

            final int[] orderIdHolder = new int[1]; // Para guardar el orderId

            db.executeTransaction(() -> {
                //Insertar orden
                String sqlOrder = "INSERT INTO order_restaurant (booking_id, staff_id, order_date_time, order_status) " +
                        "VALUES (?, ?, NOW(), 'pending')";
                ResultSet rsOrder = db.executeInsert(sqlOrder, finalBookingId, finalStaffId);

                if (!rsOrder.next()) {
                    throw new RuntimeException("Error al crear la orden");
                }

                int orderId = rsOrder.getInt(1);
                orderIdHolder[0] = orderId;
                rsOrder.close();

                //Insertar items de la orden
                for (LineItem li : finalLineItems) {
                    String sqlItem = "INSERT INTO order_menu_item " +
                            "(order_id, menu_item_id, order_menu_item_quantity, order_menu_item_comments) " +
                            "VALUES (?, ?, ?, ?)";
                    db.executeUpdate(sqlItem,
                            orderId,
                            li.menuId,
                            li.qty,
                            li.comments.isEmpty() ? null : li.comments
                    );
                }
            });

            JOptionPane.showMessageDialog(mainApp, "Orden creada con ID " + orderIdHolder[0]);
            mainApp.mostrarTabla("order_restaurant");
            mainApp.mostrarTabla("order_menu_item");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error al tomar orden: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    //Cambiar estado de una orden existente
    public void cambiarEstadoOrden() {
        try {
            String sql = "SELECT " +
                    "    o.order_id, " +
                    "    o.order_date_time, " +
                    "    o.order_status, " +
                    "    b.table_number, " +
                    "    COALESCE(( " +
                    "        SELECT SUM(omi.order_menu_item_quantity) " +
                    "        FROM order_menu_item omi " +
                    "        WHERE omi.order_id = o.order_id " +
                    "    ), 0) AS total_items " +
                    "FROM order_restaurant o " +
                    "JOIN booking b ON o.booking_id = b.booking_id " +
                    "ORDER BY o.order_date_time DESC " +
                    "LIMIT 50";

            ResultSet rs = db.query(sql);
            java.util.List<Integer> orderIds = new java.util.ArrayList<>();
            java.util.List<String> orderLabels = new java.util.ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("order_id");
                String status = rs.getString("order_status");
                int tableNum = rs.getInt("table_number");
                int totalItems = rs.getInt("total_items");

                orderIds.add(id);
                orderLabels.add("ID " + id +
                        " - Mesa " + tableNum +
                        " - " + rs.getTimestamp("order_date_time") +
                        " - Estado: " + status +
                        " (" + totalItems + " items)");
            }
            rs.close();

            if (orderIds.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay órdenes.");
                return;
            }

            Object chosen = JOptionPane.showInputDialog(mainApp,
                    "Selecciona orden:", "Cambiar estado",
                    JOptionPane.QUESTION_MESSAGE, null,
                    orderLabels.toArray(), orderLabels.get(0));
            if (chosen == null) return;

            int idx = orderLabels.indexOf(chosen);
            int orderId = orderIds.get(idx);

            String[] estados = new String[] {
                    "pending",
                    "preparing",
                    "served",
                    "cancelled",
                    "completed"
            };

            Object newEstado = JOptionPane.showInputDialog(mainApp,
                    "Selecciona nuevo estado:", "Estado",
                    JOptionPane.QUESTION_MESSAGE, null,
                    estados, estados[0]);
            if (newEstado == null) return;

            String estadoStr = newEstado.toString();

            // ACTUALIZAR CON TRANSACCIÓN
            final int finalOrderId = orderId;
            final String finalEstado = estadoStr;

            db.executeTransaction(() -> {
                String updateSql = "UPDATE order_restaurant SET order_status = ? WHERE order_id = ?";
                db.executeUpdate(updateSql, finalEstado, finalOrderId);
            });

            JOptionPane.showMessageDialog(mainApp,
                    "Estado actualizado exitosamente.\n\n" +
                            "Orden ID: " + orderId + "\n" +
                            "Nuevo estado: " + estadoStr);

            mainApp.mostrarTabla("order_restaurant");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(mainApp, "Error SQL: " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainApp, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //Reservar mesa
    public void reservarMesa() {
        try {
            //1. Seleccionar mesa
            ResultSet rsTables = db.executeQuery(
                    "SELECT table_number, table_capacity, table_location, table_status " +
                            "FROM table_restaurant ORDER BY table_number"
            );

            java.util.List<Integer> tableNums = new java.util.ArrayList<>();
            java.util.List<Integer> tableCapacities = new java.util.ArrayList<>();
            java.util.List<String> tableLabels = new java.util.ArrayList<>();

            while (rsTables.next()) {
                int tnum = rsTables.getInt("table_number");
                int capacity = rsTables.getInt("table_capacity");
                String location = rsTables.getString("table_location");
                String status = rsTables.getString("table_status");

                tableNums.add(tnum);
                tableCapacities.add(capacity);
                tableLabels.add("Mesa " + tnum +
                        " (Capacidad: " + capacity +
                        ", Ubicación: " + (location != null ? location : "N/A") +
                        ", Estado: " + status + ")");
            }
            rsTables.close();

            if (tableNums.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "No hay mesas disponibles para reservar.");
                return;
            }

            Object chosenTable = JOptionPane.showInputDialog(mainApp,
                    "Selecciona la mesa:",
                    "Paso 1/3: Seleccionar Mesa",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    tableLabels.toArray(),
                    tableLabels.get(0));

            if (chosenTable == null) return;

            int tableIndex = tableLabels.indexOf(chosenTable);
            int tableNumber = tableNums.get(tableIndex);
            int tableCapacity = tableCapacities.get(tableIndex);

            //2. Seleccionar cliente
            ResultSet rsCustomers = db.executeQuery(
                    "SELECT customer_id, customer_first_name, customer_surname, phone_number, cellphone_number " +
                            "FROM customer ORDER BY customer_first_name, customer_surname"
            );

            java.util.List<Integer> customerIds = new java.util.ArrayList<>();
            java.util.List<String> customerLabels = new java.util.ArrayList<>();

            customerLabels.add("Sin cliente asignado (Walk-in)");
            customerIds.add(null);

            while (rsCustomers.next()) {
                Integer custId = rsCustomers.getInt("customer_id");
                String firstName = rsCustomers.getString("customer_first_name");
                String surname = rsCustomers.getString("customer_surname");
                String phone = rsCustomers.getString("phone_number");
                String cellphone = rsCustomers.getString("cellphone_number");

                customerIds.add(custId);

                String contactInfo = "";
                if (cellphone != null && !cellphone.isEmpty()) {
                    contactInfo = " - Cel: " + cellphone;
                } else if (phone != null && !phone.isEmpty()) {
                    contactInfo = " - Tel: " + phone;
                }

                customerLabels.add("ID: " + custId + " - " + firstName + " " + surname + contactInfo);
            }
            rsCustomers.close();

            Object chosenCustomer = JOptionPane.showInputDialog(mainApp,
                    "Selecciona el cliente:",
                    "Paso 2/3: Seleccionar Cliente",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    customerLabels.toArray(),
                    customerLabels.get(0));

            if (chosenCustomer == null) return;

            int customerIndex = customerLabels.indexOf(chosenCustomer);
            Integer customerId = customerIds.get(customerIndex);

            //3. Seleccionar fecha, hora y comensales
            JPanel panel = new JPanel(new java.awt.GridLayout(4, 2, 5, 5));

            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            JTextField fechaField = new JTextField(dateFormat.format(new java.util.Date()));
            JTextField horaField = new JTextField("15:00");

            // Limitar el spinner a la capacidad de la mesa
            JSpinner numPersonasSpinner = new JSpinner(
                    new javax.swing.SpinnerNumberModel(
                            Math.min(1, tableCapacity),  // Valor inicial: mínimo entre 1 y capacidad
                            1,                            // Mínimo
                            tableCapacity,                // Máximo = capacidad de la mesa
                            1                             // Paso
                    )
            );

            panel.add(new JLabel("Fecha (YYYY-MM-DD):"));
            panel.add(fechaField);
            panel.add(new JLabel("Hora (HH:MM):"));
            panel.add(horaField);
            panel.add(new JLabel("Número de personas (máx. " + tableCapacity + "):"));
            panel.add(numPersonasSpinner);

            JLabel infoLabel = new JLabel("<html><b>Reserva para:</b><br>" +
                    "Mesa: " + tableNumber + " (Capacidad: " + tableCapacity + ")<br>" +
                    "Cliente: " + (customerId == null ? "Walk-in" :
                    customerLabels.get(customerIndex)) + "</html>");
            panel.add(infoLabel);

            int result = JOptionPane.showConfirmDialog(mainApp,
                    panel,
                    "Paso 3/3: Datos de la Reserva",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) return;

            String fecha = fechaField.getText().trim();
            String hora = horaField.getText().trim();
            int numPersonas = (int) numPersonasSpinner.getValue();

            // VALIDACIÓN: Verificar que no exceda la capacidad
            if (numPersonas > tableCapacity) {
                JOptionPane.showMessageDialog(mainApp,
                        "Error: El número de personas (" + numPersonas +
                                ") excede la capacidad de la mesa (" + tableCapacity + ").\n" +
                                "Por favor, seleccione una mesa con mayor capacidad.",
                        "Capacidad Excedida",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (fecha.isEmpty() || hora.isEmpty()) {
                JOptionPane.showMessageDialog(mainApp, "Fecha y hora son obligatorias.");
                return;
            }

            String fechaHora = fecha + " " + hora + ":00";

            try {
                java.text.SimpleDateFormat fullFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                fullFormat.setLenient(false);
                fullFormat.parse(fechaHora);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainApp,
                        "Formato de fecha/hora inválido.\nUse YYYY-MM-DD para fecha y HH:MM para hora.");
                return;
            }

            String confirmMsg = "¿Confirmar la siguiente reserva?\n\n" +
                    "Mesa: " + tableNumber + " (Capacidad: " + tableCapacity + ")\n" +
                    "Cliente: " + (customerId == null ? "Walk-in" : customerLabels.get(customerIndex)) + "\n" +
                    "Fecha y hora: " + fechaHora + "\n" +
                    "Número de personas: " + numPersonas;

            int confirm = JOptionPane.showConfirmDialog(mainApp,
                    confirmMsg,
                    "Confirmar Reserva",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm != JOptionPane.YES_OPTION) return;

            final Integer finalCustomerId = customerId;
            final int finalTableNumber = tableNumber;
            final String finalFechaHora = fechaHora;
            final int finalNumPersonas = numPersonas;

            db.executeTransaction(() -> {
                String sql = "INSERT INTO booking (table_number, customer_id, date_of_booking, number_in_party) " +
                        "VALUES (?, ?, ?::timestamp, ?)";

                db.executeUpdate(sql,
                        finalTableNumber,
                        finalCustomerId,
                        finalFechaHora,
                        finalNumPersonas
                );
            });

            JOptionPane.showMessageDialog(mainApp,
                    "Reserva creada exitosamente!\n\n" +
                            "Mesa: " + tableNumber + "\n" +
                            "Capacidad: " + tableCapacity + "\n" +
                            "Fecha: " + fechaHora + "\n" +
                            "Personas: " + numPersonas);

            mainApp.mostrarTabla("booking");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error al crear reserva: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
