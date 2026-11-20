# AquaControl API (backend)

Java 17 · Tomcat 11 (Jakarta Servlet 6.1) · PostgreSQL 17 · Maven

## Arquitectura (igual que el pizarrón)
BD → **pool de conexiones** (HikariCP) → **DAO** (CRUD con JDBC) → **Servlets** (API REST, JSON) → HTTP/HTTPS → Front (Angular)

```
src/main/java/aquacontrol/
  config/      Config (lee propiedades), Conexion (pool), AppListener
  modelos/     12 POJOs (uno por tabla) + 3 enums de PostgreSQL
  dao/         BaseDao (CRUD genérico) + 12 DAOs
  servlets/    13 servlets (12 tablas + AuthServlet); cada uno implementa sus propios doGet/doPost/doPut/doPatch/doDelete
  seguridad/   Passwords (BCrypt), Tokens (JWT), AuthFilter, CorsFilter
  util/        Json, Respuesta, ApiException, HttpUtil (id de la ruta y manejo de errores)
src/main/resources/  db.properties, app.properties
src/main/webapp/WEB-INF/web.xml
```

## Puesta en marcha
1. **Base de datos**: crear `purificadora` y restaurar el respaldo (formato custom de pg_dump):
   `pg_restore -U postgres -d purificadora purificadora.sql`
2. Editar `src/main/resources/db.properties` (usuario/contraseña) y **cambiar `jwt.secret`** en `app.properties`
   (o usar las variables de entorno `AQUACONTROL_DB_PASSWORD` y `AQUACONTROL_JWT_SECRET`).
3. Compilar: `mvn clean package` → genera `target/aquacontrol-api.war`
4. Copiar el `.war` a `TOMCAT_HOME/webapps/` y arrancar Tomcat 11 (JDK 17).
5. Probar: `http://localhost:8080/aquacontrol-api/api/productos`

## Endpoints (todas las tablas)
| Método | Ruta | Acción |
|---|---|---|
| GET | `/api/{tabla}` | listar |
| GET | `/api/{tabla}/{id}` | uno |
| POST | `/api/{tabla}` | crear (201) |
| PUT | `/api/{tabla}/{id}` | reemplazar completo |
| PATCH | `/api/{tabla}/{id}` | cambio parcial |
| DELETE | `/api/{tabla}/{id}` | eliminar (204) |

Tablas: `clientes`, `detalle-venta`, `movimientos-agua`, `movimientos-inventario`, `pendientes`, `pipas`,
`productos`, `registro-pipas`, `roles-permisos`, `tanques`, `usuarios`, `ventas`.
JSON en camelCase (`idCliente`, `precioUnitario`…). Fechas ISO (`2026-10-01T14:40:00`).

### Casos especiales
- `POST /api/auth/login` `{ "usuario": "...", "password": "..." }` → `{ token, usuario{...} }`
- `POST /api/ventas` `{ "idCliente": 1, "detalles": [ { "idProducto": 1, "cantidad": 2 } ] }`
  una sola transacción: venta + detalles + descuento de stock + movimientos de inventario.
  Los precios salen de la BD. `PUT/PATCH` de ventas no existen; `DELETE` cancela y devuelve el stock.

## Seguridad
- **JWT solo en información delicada** (`web.xml`): usuarios, roles-permisos, ventas, detalle-venta,
  movimientos-agua y movimientos-inventario. Enviar `Authorization: Bearer <token>`.
  Usuarios y roles-permisos solo para el rol administrador (`rol.admin` en `app.properties`).
- **Contraseñas**: se guardan con BCrypt; nunca se devuelven en ninguna respuesta. Una contraseña vieja en
  texto plano se convierte a hash en el primer login correcto.
- Consultas con `PreparedStatement` (sin inyección SQL). CORS limitado al origen de Angular (`cors.origen`).
- HTTPS: se activa en Tomcat con un certificado (conector 8443 en `server.xml`); el código no cambia.
