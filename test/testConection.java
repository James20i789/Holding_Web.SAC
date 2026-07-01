/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import Dao.IMPL.ProductoImpl;
import Dao.IMPL.UsuarioImpl;
import Modelos.Producto;
import Modelos.Usuario;
import Util.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
/**
 *
 * @author ElBatan
 */
public class testConection {
 public static void main(String[] args) {
        System.out.println("=== Iniciando prueba de conexión ===");
        
        // 1. Obtener la conexión usando tu patrón Singleton
        Connection cn = ConexionBD.getInstancia().getConexion();
        
        // 2. Verificar el estado
        try {
            if (cn != null && !cn.isClosed()) {
                System.out.println("¡Felicidades, James! Conexión EXITOSA a 'Holding_web.SAC'.");
            } else {
                System.err.println("La conexión es nula o está cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Ocurrió un error durante la verificación: " + e.getMessage());
        } finally {
            // 3. Cerramos la prueba (opcional)
            ConexionBD.getInstancia().cerrarConexion();
            System.out.println("=== Prueba finalizada ===");
        }
        
        
        
        
        ProductoImpl dao = new ProductoImpl();
        System.out.println("====== INICIANDO TEST DE PRODUCTO ======\n");

     
//        System.out.println("--- 1. Insertando un producto de prueba ---");
//        Producto nuevo = new Producto();
//        nuevo.setNombre("1/8 de pollo a la braza");
//        nuevo.setDescripcion("Rico pollo para el cancer");
//        nuevo.setPrecio(12.50);
//        nuevo.setStock(15);
//        nuevo.setImagen("pollo.jpg");
//        nuevo.setIdCategoria(1); 
//        
//        boolean insertado = dao.insertar(nuevo);
//        System.out.println("¿Insertado con éxito?: " + insertado);
//        System.out.println("-------------------------------------------\n");


        // =====================================================================
        // TEST 2: LISTAR TODOS Y BUSCAR POR NOMBRE
        // =====================================================================
//        System.out.println("--- 2. Listando todos los productos activos ---");
//        List<Producto> todos = dao.listarTodos();
//        for (Producto p : todos) {
//            System.out.println("[" + p.getIdProducto() + "] " + p.getNombre() + " - Stock: " + p.getStock() + " | Cat: " + p.getNombreCategoria());
//        }
//        System.out.println("-------------------------------------------\n");
//
//        System.out.println("--- 3. Buscando productos con la palabra 'Test' ---");
//        List<Producto> encontrados = dao.buscarPorNombre("Test");
//        if (!encontrados.isEmpty()) {
//            // Guardamos el ID del primer producto que encontramos para los siguientes tests
//            int idDePrueba = encontrados.get(0).getIdProducto();
//            
//            System.out.println("Se encontró el producto con ID: " + idDePrueba);


            // =====================================================================
            // TEST 3: BUSCAR POR ID Y ACTUALIZAR
            // =====================================================================
//            System.out.println("\n--- 4. Buscando producto específico por ID (" + idDePrueba + ") ---");
//            Producto pId = dao.buscarPorId(idDePrueba);
//            if (pId != null) {
//                System.out.println("Encontrado: " + pId.getNombre() + " ($" + pId.getPrecio() + ")");
//                
//                System.out.println("\n--- 5. Modificando precio del producto encontrado ---");
//                pId.setPrecio(1350.00); // Cambiamos el precio original
//                boolean actualizado = dao.actualizar(pId);
//                System.out.println("¿Actualizado con éxito?: " + actualizado);
//            }
//
//
//            // =====================================================================
//            // TEST 4: ACTUALIZAR STOCK
//            // =====================================================================
//            System.out.println("\n--- 6. Simulando venta de 2 unidades (Reducir Stock) ---");
//            boolean stockReducido = dao.actualizarStock(idDePrueba, 2);
//            System.out.println("¿Stock actualizado con éxito?: " + stockReducido);
//
//
//            // =====================================================================
//            // TEST 5: ELIMINAR (Opcional)
//            // =====================================================================
//            /* System.out.println("\n--- 7. Eliminando el producto de prueba creado ---");
//            boolean eliminado = dao.eliminar(idDePrueba);
//            System.out.println("¿Eliminado con éxito?: " + eliminado);
//            */
//            
//        } else {
//            System.out.println("No se encontraron productos con ese criterio para continuar el flujo.");
//        }
//        System.out.println("-------------------------------------------\n");
//
//
//        // =====================================================================
//        // TEST 6: QUERIES AVANZADOS (Filtros y Métricas)
//        // =====================================================================
//        System.out.println("--- 8. Productos por Categoría (ID: 1) ---");
//        List<Producto> porCat = dao.listarPorCategoria(1);
//        System.out.println("Total encontrados en categoría 1: " + porCat.size());
//        
//        System.out.println("\n--- 9. Alerta de Stock Bajo (Umbral: 5 unidades) ---");
//        List<Producto> stockBajo = dao.listarStockBajo(5);
//        for (Producto p : stockBajo) {
//            System.out.println("⚠️ ALERTA: " + p.getNombre() + " solo tiene " + p.getStock() + " unidades.");
//        }
//
//        System.out.println("\n--- 10. Top 3 de Productos Más Vendidos ---");
//        List<Producto> topVendidos = dao.listarMasVendidos(3);
//        for (Producto p : topVendidos) {
//            System.out.println("⭐ " + p.getNombre());
//        }
//
//        System.out.println("\n====== FIN DE TODOS LOS TESTS ======");
   //}
         UsuarioImpl usuarioDao = new UsuarioImpl();
        Usuario usuario = new Usuario();
        usuario.setNombre("Priscila");
        usuario.setCorreo("priss.14rs@gmail.com");
        usuario.setPassword("199426");
        usuario.setDireccion("Av Martinez de Compañong C/S Huayco");
        usuario.setIdRol(2); // verifica que exista este rol en tu tabla rol

        boolean registrado = usuarioDao.registrar(usuario);

        if (registrado) {
            System.out.println(" Usuario registrado correctamente");
        } else {
            System.out.println(" Error al registrar usuario");
        }
    }
    }
 
   
 
 

 
 
