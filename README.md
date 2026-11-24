# Reportes Spring Boot

API REST para importar ventas desde CSV y consultar reportes para un dashboard. El proyecto usa Java 17, Spring Boot 3.4.13, Maven, Spring Web, Spring Data JPA, Hibernate y MySQL 8.

## Arquitectura por capas

```text
controller  ->  service  ->  repository  ->  MySQL
                    |                         |
                   DTO                    model/entity
```

- **`controller`** expone los endpoints HTTP y convierte las solicitudes en llamadas a los servicios.
- **`service`** contiene los flujos del negocio: importación asíncrona y coordinación de reportes.
- **`repository`** usa Spring Data JPA para persistencia. `VentaRepository` también contiene las consultas agregadas del dashboard.
- **`model`** define las entidades `Venta` e `Importacion` que Hibernate mapea a las tablas `ventas` e `importaciones`.
- **`dto`** define la respuesta agregada del dashboard, separada de las entidades persistidas.
- **`config`** configura el pool de hilos y activa el soporte de `@Async`.

## Procesamiento asíncrono

`AsyncConfig` registra un `ThreadPoolTaskExecutor` llamado `taskExecutor`, compartido por la importación y los reportes. Tiene 4 hilos base, un máximo de 8, una cola de hasta 100 tareas y el prefijo `HiloVentas-` para identificar los hilos en los logs. El pool reutiliza hilos y limita la cantidad de trabajo concurrente; evita crear un hilo nuevo sin control por cada solicitud.

### Importación de ventas

1. `ImportacionController` lee el archivo CSV en UTF-8 y crea un ticket en estado `PENDIENTE`.
2. Llama a `ImportacionService.procesarArchivoAsync`, que Spring ejecuta en un hilo del pool, y responde `202 Accepted` con el ticket.
3. El servicio actualiza el estado a `PROCESANDO`, valida las filas y guarda las ventas en lotes de 500. Al finalizar deja el ticket en `COMPLETADO`; ante una fila inválida lo marca `ERROR`.
4. El endpoint de estado consulta el ticket en MySQL para mostrar el estado y los registros procesados.

El CSV debe tener estas seis columnas, en este orden. Se acepta un encabezado opcional; las fechas deben usar `yyyy-MM-dd`. La configuración actual permite archivos de hasta 10 MB. Si una fila inválida aparece después de guardar uno o más lotes, esos lotes ya guardados permanecen en la base y el ticket queda en `ERROR`.

```csv
cliente,producto,categoria,cantidad,precioUnitario,fechaVenta
Ana,Notebook Pro,Computacion,1,120000,2026-11-01
Bruno,Mouse inalambrico,Accesorios,2,4500,2026-11-02
```

El servidor lee el contenido del archivo antes de devolver el ticket y pasa las líneas al servicio asíncrono. El procesamiento y la escritura en la base corren en segundo plano.

### Dashboard en paralelo

`ReporteService` dispara tres métodos de `ReporteAsyncWorker`. Cada método está anotado con `@Async("taskExecutor")` y devuelve un `CompletableFuture` con su resultado:

1. Facturación total por categoría.
2. Segmentación de clientes: `VIP` desde 100.000, `FRECUENTE` desde 40.000 y `REGULAR` por debajo de 40.000.
3. Los cinco productos con más unidades vendidas.

El coordinador usa `CompletableFuture.allOf(...).join()` para esperar a que terminen las tres consultas, mide el tiempo total y arma `ResumenDashboardDTO`. `CompletableFuture` permite componer las tareas y esperar al conjunto sin ejecutarlas una detrás de otra. Las tres consultas comparten el pool; el prefijo de los hilos sirve para observarlos en los logs.

## Requisitos

- JDK 17 o superior.
- Docker con Docker Compose.

## Iniciar MySQL y la aplicación

Desde la raíz del proyecto, iniciar MySQL:

```bash
docker compose up -d
```

Compose ejecuta MySQL 8 y crea la base `ventas_async_db`. El puerto del host `3307` se conecta al puerto `3306` del contenedor. La configuración local de `application.properties` usa `localhost:3307`, usuario `root` y contraseña `root`. El volumen `mysql_data` conserva los datos entre reinicios.

Iniciar Spring Boot en otra terminal:

```bash
# Windows PowerShell
.\mvnw.cmd spring-boot:run

# macOS / Linux
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8081`. JPA está configurado con `ddl-auto=update`, por lo que Hibernate crea o actualiza las tablas al iniciar la aplicación.

Para detener MySQL sin borrar los datos:

```bash
docker compose down
```

## API REST

### Subir un CSV

`POST /api/importaciones/subir` recibe un formulario `multipart/form-data` con un campo de archivo llamado `file`. Devuelve `202 Accepted` con el ticket creado.

```powershell
curl.exe -X POST "http://localhost:8081/api/importaciones/subir" `
  -F "file=@C:\Users\facun\OneDrive\Desktop\ventas_prueba.csv"
```

Respuesta aproximada:

```json
{
  "id": 1,
  "nombreArchivo": "ventas_prueba.csv",
  "estado": "PENDIENTE",
  "registrosProcesados": 0,
  "fechaCreacion": "2026-11-24T12:00:00"
}
```

En Postman, elegí `POST`, la URL `http://localhost:8081/api/importaciones/subir`, luego **Body → form-data**. Agregá una clave `file`, cambiá su tipo de **Text** a **File** y seleccioná el CSV. Postman define el `Content-Type` multipart automáticamente.

### Consultar el estado de una importación

`GET /api/importaciones/{id}` devuelve el ticket actualizado. Si el ID no existe, responde `404 Not Found`.

```powershell
curl.exe "http://localhost:8081/api/importaciones/1"
```

Consultá el mismo endpoint hasta que `estado` sea `COMPLETADO` o `ERROR`.

### Consultar el dashboard

`GET /api/reportes/dashboard` devuelve las tres métricas y el tiempo de ejecución.

```powershell
curl.exe "http://localhost:8081/api/reportes/dashboard"
```

En Postman, elegí `GET` y esa misma URL. No requiere body ni autenticación.

La respuesta tiene esta forma:

```json
{
  "tiempoEjecucionMs": 42,
  "facturacionPorCategoria": [
    { "categoria": "Computacion", "total": 176000.0 }
  ],
  "segmentacionClientes": [
    { "cliente": "Ana", "totalGastado": 133500.0, "segmento": "VIP" }
  ],
  "topProductos": [
    { "producto": "Mouse inalambrico", "unidades": 5 }
  ]
}
```

Los valores anteriores son ilustrativos; el resultado depende de las ventas guardadas en MySQL.
