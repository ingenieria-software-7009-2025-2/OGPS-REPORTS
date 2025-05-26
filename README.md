Link del video probando la primera iteración:

https://drive.google.com/file/d/1MdzKFTAWG7vnHq8-UmL0VfJ4bJDv71XH/view

## Ejecución del proyecto con Docker Compose
Este proyecto puede ejecutarse fácilmente usando contenedores con Docker y Docker Compose, lo cual levanta automáticamente:

- Una base de datos PostgreSQL inicializada con los scripts SQL proporcionados.

- El backend (API en Kotlin).

- El frontend (React).

### Estructura esperada
Asegúrate de posicionar el archivo docker-compose.yml al mismo nivel que los dos repositorios. Por ejemplo:
```
.
├── OGP-REPORTS/
├── ogp-reports-frontend/
└── docker-compose.yml
```
Nota: Este archivo debe colocarse fuera de las carpetas que contienen la api y el frontend.

### Requisitos previos
Antes de comenzar, asegúrate de tener instalado:

- Docker
- Docker Compose (ya viene incluido en Docker Desktop)

Para verificar las versiones:
```
docker --version
docker-compose --version
```

### Pasos para ejecutar el proyecto
1. Clona ambos repositorios (front y back) en el mismo directorio.
2. Coloca el archivo docker-compose.yml junto a ambos repositorios, como se muestra arriba.
3. Desde el mismo nivel donde está docker-compose.yml, ejecuta:
```
docker-compose up --build
```
Esto realizará las siguientes acciones:
  1. Inicializará una base de datos PostgreSQL llamada mojarras.
  2. Ejecutará automáticamente los scripts de inicialización.
  3. Levantará el backend en http://localhost:8080
  4. Levantará el frontend en http://localhost:5173

4. Accede a los servicios en las rutas del paso anterior

### Detener los contenedores
Para detener y limpiar los contenedores:
```
docker-compose down -v
```