# AquaControl API (backend)

Java 17 · Tomcat 11 (Jakarta Servlet 6.1) · PostgreSQL 17 · Maven

## Arquitectura (igual que el pizarrón)
BD → **pool de conexiones** (HikariCP) → **DAO** (CRUD con JDBC) → **Servlets** (API REST, JSON) → **HTTPS** → Front (Angular)

```
src/main/java/aquacontrol/
  config/      Config (lee propiedades), Conexion (pool), AppListener
  modelos/     12 POJOs (uno por tabla) + 3 enums de PostgreSQL
  dao/         BaseDao (CRUD genérico) + 12 DAOs
  servlets/    13 servlets (12 tablas + AuthServlet); cada uno implementa sus propios doGet/doPost/doPush/doPut/doPatch/doDelete
  seguridad/   HostFilter, CorsFilter, HttpsFilter, AuthFilter, Passwords (BCrypt), Tokens (JWT), TokensRevocados
  util/        Json, Respuesta, ApiException, HttpUtil (id de la ruta y manejo de errores)
src/main/resources/  db.properties, app.properties
src/main/webapp/WEB-INF/web.xml   (orden de filtros y rutas protegidas)
tomcat/      crear-certificado.bat y server-https-conector.xml (HTTPS en Tomcat)
```

## Ruta específica (no localhost)
La API usa el nombre de host **aquacontrol.local** (configurable en `app.properties` → `app.host`).
- `HostFilter` rechaza (421) cualquier petición que llegue por otro nombre: `localhost`, `127.0.0.1`, la IP, etc.
- La base se llama por `db.host` (db.properties), el front por `cors.origen`. No hay `localhost` en el código.
- Si no existe un DNS, agrega el nombre al archivo **hosts** de la PC (abrir el Bloc de notas como administrador):
  `C:\Windows\System32\drivers\etc\hosts`  →  `127.0.0.1   aquacontrol.local`
  (si Tomcat está en otra PC, usa la IP de esa PC en lugar de 127.0.0.1).
- Para usar otro nombre: cambia `app.host` (y `db.host`/`cors.origen`) o define `AQUACONTROL_APP_HOST`.

## Cifrado en tránsito (HTTPS) en venta y pago
`HttpsFilter` solo acepta por HTTPS las rutas de **ventas, detalle de venta, pendientes (pagos), usuarios y login**;
por HTTP responde 403. Con HTTPS agrega el encabezado HSTS. Para activarlo en Tomcat:
1. Ejecutar `tomcat\crear-certificado.bat` (usa keytool del JDK 17). Genera `aquacontrol.p12` y `aquacontrol.cer`.
2. Copiar `aquacontrol.p12` a `TOMCAT\conf\`.
3. Pegar el contenido de `tomcat\server-https-conector.xml` dentro de `<Service>` en `TOMCAT\conf\server.xml`.
4. Reiniciar Tomcat. La API queda en: `https://aquacontrol.local:8443/aquacontrol-api/api/...`
5. El certificado es autofirmado: en Thunder Client/Postman desactiva "SSL verification", o instala
   `aquacontrol.cer` en *Entidades de certificación raíz de confianza* (Windows).
Solo para pruebas sin certificado se puede poner `https.requerido=false` (no dejarlo así al entregar).
Importante: HTTPS cifra la comunicación; los valores de las columnas no se guardan cifrados en la base (excepto las contraseñas, con BCrypt).

## Puesta en marcha
1. **Base de datos**: crear `purificadora` y restaurar el respaldo (formato custom de pg_dump):
   `pg_restore -U postgres -d purificadora purificadora.sql`
2. Editar `db.properties` (contraseña) y `app.properties` (**cambiar `jwt.secret`**, revisar `rol.admin`),
   o usar `AQUACONTROL_DB_PASSWORD` y `AQUACONTROL_JWT_SECRET`.
3. Compilar: `mvn clean package` → `target/aquacontrol-api.war`
4. Copiar el `.war` a `TOMCAT_HOME/webapps/`, configurar HTTPS (arriba) y arrancar Tomcat 11 (JDK 17).
5. Probar: `https://aquacontrol.local:8443/aquacontrol-api/api/productos`

## Endpoints
Cada tabla: `GET /api/{tabla}`, `GET /api/{tabla}/{id}`, `POST`, `PUSH`, `PUT /{id}`, `PATCH /{id}`, `DELETE /{id}`.
PATCH y PUSH se detectan en `service()` y se mandan a `doPatch()` y `doPush()`. `doPush` es un método propio del proyecto
(no estándar de HTTP): registra un recurso nuevo con la misma lógica que POST. No es el Server Push de HTTP/2.
Tablas: `clientes`, `detalle-venta`, `movimientos-agua`, `movimientos-inventario`, `pendientes`, `pipas`,
`productos`, `registro-pipas`, `roles-permisos`, `tanques`, `usuarios`, `ventas`.
JSON en camelCase. Fechas ISO (`2026-10-01T14:40:00`).

### Ventas (`/api/ventas`, token + HTTPS)
- `POST` `{ "idCliente": 1, "detalles": [ { "idProducto": 1, "cantidad": 2 } ] }`: registra la venta en una transacción
  (cabecera + detalles + descuento de stock + movimientos). Los precios salen de la BD; el vendedor, del token.
- `PUT /{id}`: reemplaza cliente y productos. `PATCH /{id}`: solo `idCliente` y/o `detalles`.
  En ambos se devuelve el stock anterior y se aplica el nuevo en la misma transacción.
- `DELETE /{id}`: cancela la venta y devuelve el stock.

### Sesión (`/api/auth/*`, HTTPS)
| Método | Ruta | Acción |
|---|---|---|
| POST | `/api/auth/login` `{usuario, password}` | token JWT (público) |
| GET | `/api/auth/sesion` | datos del dueño del token |
| PUT / PATCH | `/api/auth/password` `{passwordActual, passwordNueva}` | cambia su contraseña |
| DELETE | `/api/auth/sesion` | cierra la sesión (el token deja de servir) |

## Seguridad (orden de los filtros en web.xml)
1. **Host**: solo el nombre configurado (no localhost). 2. **CORS**: solo `cors.origen`.
3. **HTTPS**: obligatorio en venta, pago, usuarios y login. 4. **Token JWT**: en información delicada
   (usuarios, roles, ventas, detalle de venta, movimientos); usuarios y roles solo para el rol administrador.
- Contraseñas con BCrypt; nunca se devuelven. Consultas con `PreparedStatement` (sin inyección SQL).
