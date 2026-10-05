import java.awt.*;
import java.net.URL;
import java.sql.ResultSet;
import javax.swing.*;

public class MainApp extends javax.swing.JFrame {

    private final Database db;
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuBrowse;
    private javax.swing.JMenu menuOperaciones;
    private final java.util.Set<String> empleadosActivos = new java.util.HashSet<>();
    private GestorAgregar gestorAgregar;
    private GestorActualizar gestorActualizar;
    private GestorEliminar gestorEliminar;
    private GestorOperaciones gestorOperaciones;
    private GestorReportes gestorReportes;



    public MainApp() {
        //Poner usuario y contraseña de la BD
        db = Database.getDatabase("santiago", "1906");

        //Iniciar los componentes, cargar tablas y ponerle formato a la ventana
        initComponents();
        this.setSize(900, 600);
        this.setTitle("RestaurantDB");
        this.setLocationRelativeTo(null);
        cargarTablas();
        ImageIcon icon = new ImageIcon(getClass().getResource("/img/icon.png"));
        this.setIconImage(icon.getImage());

        //Iniciar gestores para realizar las operaciones SQL
        gestorAgregar = new GestorAgregar(this, db);
        gestorActualizar = new GestorActualizar(this, db);
        gestorEliminar = new GestorEliminar(this, db);
        gestorOperaciones = new GestorOperaciones(this, db);
        gestorReportes = new GestorReportes(this, db);
    }

    class BackgroundDesktop extends JDesktopPane {

        private Image background;

        public BackgroundDesktop(URL imageUrl) {
            background = new ImageIcon(imageUrl).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }

    private String getNiceQuery(String tableName) {
        switch (tableName) {

            case "customer":
                return """
                SELECT 
                    customer_id AS ID,
                    customer_first_name AS Nombre,
                    customer_surname AS Apellido,
                    phone_number AS Telefono,
                    cellphone_number AS Celular,
                    email_address AS Email,
                    other_customer_details AS Detalles
                FROM customer
                ORDER BY Nombre
            """;

            case "staff":
                return """
                SELECT
                    s.staff_id AS ID,
                    s.staff_first_name AS Nombre,
                    s.staff_last_name AS Apellido,
                    s.staff_shift AS Turno,
                    r.staff_role_description AS Rol
                FROM staff s
                JOIN staff_role r ON s.staff_role_code = r.staff_role_code
                ORDER BY Nombre
            """;

            case "staff_role":
                return """
                SELECT
                    staff_role_code AS Codigo,
                    staff_role_description AS Rol
                FROM staff_role
                ORDER BY Codigo
            """;

            case "table_restaurant":
                return """
                SELECT 
                    table_number AS Mesa,
                    table_capacity AS Capacidad,
                    table_location AS Ubicacion,
                    table_type AS Tipo,
                    table_status AS Estado
                FROM table_restaurant
                ORDER BY Mesa
            """;

            case "booking":
                return """
                SELECT
                    b.booking_id AS ID,
                    b.table_number AS Mesa,
                    b.date_of_booking AS Fecha,
                    b.number_in_party AS Comensales,
                    CONCAT(c.customer_first_name,' ',c.customer_surname) AS Cliente
                FROM booking b
                LEFT JOIN customer c ON c.customer_id = b.customer_id
                ORDER BY b.date_of_booking DESC
            """;

            case "menu":
                return """
                SELECT
                    menu_id AS ID,
                    menu_date AS Fecha
                FROM menu
                ORDER BY menu_date DESC
            """;

            case "menu_item":
                return """
                SELECT 
                    mi.menu_item_id AS ID,
                    mi.menu_item_description AS Platillo,
                    mi.menu_item_price AS Precio,
                    mi.menu_id AS MenuID
                FROM menu_item mi
                ORDER BY Platillo
            """;

            case "menu_item_ingredient":
                return """
                SELECT
                    mii.menu_item_id AS PlatilloID,
                    mi.menu_item_description AS Platillo,
                    ing.ingredient_name AS Ingrediente,
                    mii.item_quantity AS Cantidad
                FROM menu_item_ingredient mii
                JOIN menu_item mi ON mi.menu_item_id = mii.menu_item_id
                JOIN ingredients ing ON ing.ingredient_id = mii.ingredient_id
                ORDER BY Platillo
            """;

            case "ingredients":
                return """
                SELECT
                    i.ingredient_id AS ID,
                    i.ingredient_name AS Ingrediente,
                    t.ingredient_type_description AS Tipo,
                    i.ingredient_expiration AS Expiracion,
                    i.ingredient_measure_unit AS Unidad
                FROM ingredients i
                JOIN ingredient_type t ON i.ingredient_type_code = t.ingredient_type_code
                ORDER BY Ingrediente
            """;

            case "ingredient_type":
                return """
                SELECT
                    ingredient_type_code AS Codigo,
                    ingredient_type_description AS Tipo
                FROM ingredient_type
                ORDER BY Tipo
            """;

            case "order_restaurant":
                return """
                SELECT 
                    o.order_id AS ID,
                    o.order_status AS Estado,
                    o.order_date_time AS Fecha,
                    b.table_number AS Mesa,
                    CONCAT(s.staff_first_name,' ',s.staff_last_name) AS Mesero
                FROM order_restaurant o
                LEFT JOIN booking b ON o.booking_id = b.booking_id
                LEFT JOIN staff s ON o.staff_id = s.staff_id
                ORDER BY o.order_date_time DESC
            """;

            case "order_menu_item":
                return """
                SELECT 
                    omi.order_menu_item_id AS ID,
                    omi.order_id AS Orden,
                    mi.menu_item_description AS Platillo,
                    omi.order_menu_item_quantity AS Cantidad,
                    omi.order_menu_item_comments AS Comentarios
                FROM order_menu_item omi
                JOIN menu_item mi ON omi.menu_item_id = mi.menu_item_id
                ORDER BY omi.order_id DESC
            """;

        }
        return "SELECT * FROM \"" + tableName + "\"";
    }

    private void initComponents() {
        desktop = new BackgroundDesktop(
                getClass().getResource("/img/fondo-main.jpg")
        );

        menuBar = new javax.swing.JMenuBar();

        //Menú para ver tablas (Select - R de CRUD)
        menuBrowse = new javax.swing.JMenu("Ver Tablas");
        menuBar.add(menuBrowse);

        //Menú para agregar a tablas (Insert - C de CRUD)
        JMenu menuAgregar = new JMenu("Agregar");

        JMenuItem addClientes = new JMenuItem("Cliente");
        addClientes.addActionListener(evt -> gestorAgregar.agregarCliente());
        menuAgregar.add(addClientes);

        JMenuItem addEmpleados = new JMenuItem("Empleado");
        addEmpleados.addActionListener(evt -> gestorAgregar.agregarEmpleado());
        menuAgregar.add(addEmpleados);

        JMenuItem addRoles = new JMenuItem("Rol del Personal");
        addRoles.addActionListener(evt -> gestorAgregar.agregarRol());
        menuAgregar.add(addRoles);

        JMenuItem addMesas = new JMenuItem("Mesa");
        addMesas.addActionListener(evt -> gestorAgregar.agregarMesa());
        menuAgregar.add(addMesas);

        JMenuItem addPlato = new JMenuItem("Platillo");
        addPlato.addActionListener(evt -> gestorAgregar.agregarPlato());
        menuAgregar.add(addPlato);

        JMenuItem addIngredientes = new JMenuItem("Ingrediente");
        addIngredientes.addActionListener(evt -> gestorAgregar.agregarIngrediente());
        menuAgregar.add(addIngredientes);

        JMenuItem addTiposIngredientes = new JMenuItem("Tipo de Ingrediente");
        addTiposIngredientes.addActionListener(evt -> gestorAgregar.agregarTipoIngrediente());
        menuAgregar.add(addTiposIngredientes);

        JMenuItem addIngredientePlato = new JMenuItem("Ingrediente de Platillo");
        addIngredientePlato.addActionListener(evt -> gestorAgregar.agregarIngredienteAPlatillo());
        menuAgregar.add(addIngredientePlato);

        menuBar.add(menuAgregar);

        //Menú para actualizar registros (Update - U de CRUD)
        JMenu menuActualizar = new JMenu("Actualizar");

        JMenuItem updateCliente = new JMenuItem("Cliente");
        updateCliente.addActionListener(evt -> gestorActualizar.actualizarCliente());
        menuActualizar.add(updateCliente);

        JMenuItem updateEmpleado = new JMenuItem("Empleado");
        updateEmpleado.addActionListener(evt -> gestorActualizar.actualizarEmpleado());
        menuActualizar.add(updateEmpleado);

        JMenuItem updateRol = new JMenuItem("Rol del Personal");
        updateRol.addActionListener(evt -> gestorActualizar.actualizarRol());
        menuActualizar.add(updateRol);

        JMenuItem updateMesa = new JMenuItem("Mesa");
        updateMesa.addActionListener(evt -> gestorActualizar.actualizarMesa());
        menuActualizar.add(updateMesa);

        JMenuItem updatePlato = new JMenuItem("Platillo");
        updatePlato.addActionListener(evt -> gestorActualizar.actualizarPlato());
        menuActualizar.add(updatePlato);

        JMenuItem updateIngrediente = new JMenuItem("Ingrediente");
        updateIngrediente.addActionListener(evt -> gestorActualizar.actualizarIngrediente());
        menuActualizar.add(updateIngrediente);

        JMenuItem updateTipoIngrediente = new JMenuItem("Tipo de Ingrediente");
        updateTipoIngrediente.addActionListener(evt -> gestorActualizar.actualizarTipoIngrediente());
        menuActualizar.add(updateTipoIngrediente);

        JMenuItem updateIngredientePlato = new JMenuItem("Ingrediente de Platillo");
        updateIngredientePlato.addActionListener(evt -> gestorActualizar.actualizarIngredienteDePlatillo());
        menuActualizar.add(updateIngredientePlato);

        menuBar.add(menuActualizar);

        //Menú para eliminar registros (Delete - D de CRUD)
        JMenu menuEliminar = new JMenu("Eliminar");

        JMenuItem elimCliente = new JMenuItem("Cliente");
        elimCliente.addActionListener(evt -> gestorEliminar.eliminarCliente());
        menuEliminar.add(elimCliente);

        JMenuItem elimEmpleado = new JMenuItem("Empleado");
        elimEmpleado.addActionListener(evt -> gestorEliminar.eliminarEmpleado());
        menuEliminar.add(elimEmpleado);

        JMenuItem elimMesa = new JMenuItem("Mesa");
        elimMesa.addActionListener(evt -> gestorEliminar.eliminarMesa());
        menuEliminar.add(elimMesa);

        menuEliminar.addSeparator();

        JMenuItem elimReserva = new JMenuItem("Reserva");
        elimReserva.addActionListener(evt -> gestorEliminar.eliminarReserva());
        menuEliminar.add(elimReserva);

        JMenuItem elimOrden = new JMenuItem("Orden");
        elimOrden.addActionListener(evt -> gestorEliminar.eliminarOrden());
        menuEliminar.add(elimOrden);

        menuEliminar.addSeparator();

        JMenuItem elimPlatillo = new JMenuItem("Platillo");
        elimPlatillo.addActionListener(evt -> gestorEliminar.eliminarPlatillo());
        menuEliminar.add(elimPlatillo);

        JMenuItem elimIngrediente = new JMenuItem("Ingrediente");
        elimIngrediente.addActionListener(evt -> gestorEliminar.eliminarIngrediente());
        menuEliminar.add(elimIngrediente);

        JMenuItem elimTipoIngrediente = new JMenuItem("Tipo de Ingrediente");
        elimTipoIngrediente.addActionListener(evt -> gestorEliminar.eliminarTipoIngrediente());
        menuEliminar.add(elimTipoIngrediente);

        JMenuItem elimIngredientePlato = new JMenuItem("Ingrediente de Platillo");
        elimIngredientePlato.addActionListener(evt -> gestorEliminar.eliminarIngredienteDePlatillo());
        menuEliminar.add(elimIngredientePlato);

        menuBar.add(menuEliminar);

        //Menú de operaciones extra de negocio
        JMenu menuOperaciones = new JMenu("Operaciones");
        JMenuItem itemTomar = new JMenuItem("Tomar Orden");
        itemTomar.addActionListener(evt -> gestorOperaciones.tomarOrden());
        menuOperaciones.add(itemTomar);

        JMenuItem itemEstado = new JMenuItem("Cambiar Estado de Orden");
        itemEstado.addActionListener(evt -> gestorOperaciones.cambiarEstadoOrden());
        menuOperaciones.add(itemEstado);

        JMenuItem reservarItem = new JMenuItem("Reservar Mesa");
        reservarItem.addActionListener(evt -> gestorOperaciones.reservarMesa());
        menuOperaciones.add(reservarItem);

        menuBar.add(menuOperaciones);

        // Menú de Reportes
        JMenu menuReportes = new JMenu("Reportes");

        JMenuItem reporteVentas = new JMenuItem("Reporte de Ventas por Período");
        reporteVentas.addActionListener(e -> gestorReportes.generarReporteVentas());
        menuReportes.add(reporteVentas);

        JMenuItem reportePlatillos = new JMenuItem("Platillos Más Vendidos");
        reportePlatillos.addActionListener(e -> gestorReportes.generarReportePlatillosMasVendidos());
        menuReportes.add(reportePlatillos);

        JMenuItem reporteInventario = new JMenuItem("Inventario de Ingredientes");
        reporteInventario.addActionListener(e -> gestorReportes.generarReporteInventario());
        menuReportes.add(reporteInventario);

        menuBar.add(menuReportes);

        setJMenuBar(menuBar);
        add(desktop);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }

    private void cargarTablas() {
        addBrowseItem("booking", "Reservas");
        addBrowseItem("customer", "Clientes");
        addBrowseItem("staff", "Empleados");
        addBrowseItem("staff_role", "Roles del Personal");
        addBrowseItem("table_restaurant", "Mesas");
        addBrowseItem("order_menu_item", "Platillos por Orden");
        addBrowseItem("order_restaurant", "Ordenes");
        addBrowseItem("menu_item", "Platillos");
        addBrowseItem("menu_item_ingredient", "Ingredientes por Platillo");
        addBrowseItem("ingredients", "Ingredientes");
        addBrowseItem("ingredient_type", "Tipos de Ingrediente");
    }

    private void addBrowseItem(String tableName, String displayName) {
        JMenuItem item = new JMenuItem(displayName);
        item.addActionListener(evt -> mostrarTabla(tableName));
        menuBrowse.add(item);
    }

    public void mostrarTabla(String tableName) {
        try {
            String sql = getNiceQuery(tableName);
            ResultSet rs = db.query(sql);
            JDBCTableAdapter model = new JDBCTableAdapter(rs);
            TableBrowser tb = new TableBrowser(tableName, model);
            desktop.add(tb);

            tb.pack();

            int x = (desktop.getWidth() - tb.getWidth()) / 2;
            int y = (desktop.getHeight() - tb.getHeight()) / 2;
            tb.setLocation(x, y);

            tb.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar tabla: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new MainApp().setVisible(true));
    }
}