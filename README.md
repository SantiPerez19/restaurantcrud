# RestaurantDB - CRUD para Restaurante

Proyecto de la materia **Base de Datos I**. Aplicación de escritorio que ofrece una interfaz gráfica para administrar la información de un restaurante mediante operaciones **CRUD** (Crear, Leer, Actualizar, Eliminar) sobre una base de datos **PostgreSQL**, además de operaciones de negocio y generación de **reportes en PDF**.

## Descripción general

La aplicación se conecta a la base de datos `restaurantDB` en PostgreSQL y presenta una ventana principal con una barra de menús desde la que se realizan todas las operaciones:

- **Ver Tablas**: exploración de las tablas del sistema con consultas que muestran los datos de forma legible (clientes, empleados, roles, mesas, reservas, menú, platillos, ingredientes, órdenes, etc.).
- **Agregar / Actualizar / Eliminar**: operaciones CRUD sobre clientes, empleados, roles del personal, mesas, platillos, ingredientes, tipos de ingrediente, reservas y órdenes.
- **Operaciones**: funciones de negocio como *Tomar Orden*, *Cambiar Estado de Orden* y *Reservar Mesa*.
- **Reportes**: generación de documentos PDF con:
  - Reporte de ventas por período.
  - Platillos más vendidos.
  - Inventario de ingredientes (resaltando los próximos a vencer en 30 días).

La conexión a la base de datos se implementa con el patrón **Singleton** (`Database`), e incluye soporte para transacciones (commit/rollback) y consultas con `PreparedStatement`.

## Tecnologías y lenguajes

| Área | Tecnología |
|------|------------|
| Lenguaje | **Java** (JDK 23) |
| Interfaz gráfica | **Java Swing / AWT** (`JFrame`, `JDesktopPane`, `JTable`, `JMenuBar`) |
| Base de datos | **PostgreSQL** (driver JDBC `org.postgresql:postgresql` 42.5.0) |
| Acceso a datos | **JDBC** (`java.sql`), `PreparedStatement` y transacciones |
| Reportes PDF | **iText** 5.5.13.3 |
| Utilidades PDF | **Apache PDFBox** 2.0.30 (dependencia declarada) |
| Pruebas | **JUnit 5** (5.10.0) |
| Build | **Gradle** (Kotlin DSL, wrapper Gradle 8.14) |

## Requerimientos

- JDK 17 o superior (probado con JDK 23).
- PostgreSQL en ejecución con la base de datos `restaurantDB`.
- Gradle (se incluye el wrapper `gradlew`).

## Configuración e inicio

1. Crear la base de datos `restaurantDB` en PostgreSQL con el esquema del restaurante (tablas `customer`, `staff`, `staff_role`, `table_restaurant`, `booking`, `menu`, `menu_item`, `menu_item_ingredient`, `ingredients`, `ingredient_type`, `order_restaurant`, `order_menu_item`).
2. Configurar las credenciales de la base de datos en `src/main/java/MainApp.java` (línea de `Database.getDatabase(...)`) y, si es necesario, la URL en `Database.java` (`DB_URL`).
3. Compilar el proyecto:
   ```bash
   ./gradlew build
   ```
4. Ejecutar la clase `MainApp` (por ejemplo desde el IDE).

## Estructura del proyecto

```
src/main/java/
├── MainApp.java             # Ventana principal, menús y navegación de tablas
├── Database.java            # Conexión Singleton a PostgreSQL y utilidades SQL
├── JDBCTableAdapter.java    # Adaptador de ResultSet a JTable
├── TableBrowser.java        # Ventana para visualizar tablas
├── GestorAgregar.java       # Operaciones INSERT
├── GestorActualizar.java    # Operaciones UPDATE
├── GestorEliminar.java      # Operaciones DELETE
├── GestorOperaciones.java   # Operaciones de negocio (órdenes, reservas)
└── GestorReportes.java      # Generación de reportes en PDF (iText)

src/main/resources/img/      # Recursos gráficos (ícono y fondo)
```
