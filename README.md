# EnVivo

> **Plataforma web de gestión y descubrimiento de eventos**, desarrollada con Spring Boot y Thymeleaf. Permite a los usuarios explorar eventos, marcar sus favoritos y a los administradores gestionar el catálogo completo.

---

## Badges

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=flat-square&logo=thymeleaf&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
![Cloudinary](https://img.shields.io/badge/Cloudinary-SDK-3448C5?style=flat-square&logo=cloudinary&logoColor=white)
![Licencia](https://img.shields.io/badge/Licencia-MIT-yellow?style=flat-square)
![Versión](https://img.shields.io/badge/Versión-0.0.1--SNAPSHOT-blue?style=flat-square)
![Estado](https://img.shields.io/badge/Estado-En%20Desarrollo-orange?style=flat-square)

---

## Tabla de Contenidos

1. [Descripción del Proyecto](#-envivo)
2. [Tecnologías y Stack](#️-tecnologías-y-stack)
3. [Arquitectura y Estructura de Carpetas](#-arquitectura-y-estructura-de-carpetas)
4. [Prerrequisitos](#-prerrequisitos)
5. [Instalación y Configuración](#️-instalación-y-configuración)
6. [Uso y Ejecución](#-uso-y-ejecución)
7. [Endpoints Principales](#-endpoints-principales)
8. [Contribución](#-contribución)
9. [Autores](#-autores)

---

## Tecnologías y Stack

| Capa | Tecnología | Versión |
|---|---|---|
| **Lenguaje** | Java | 17 |
| **Framework Backend** | Spring Boot | 3.4.3 |
| **Seguridad** | Spring Security | 6.x |
| **Persistencia** | Spring Data JPA / Hibernate | 6.x |
| **Motor de Plantillas** | Thymeleaf + Thymeleaf Security Extras | 3.x |
| **Base de Datos** | PostgreSQL | 16 |
| **Gestor de Dependencias** | Apache Maven | 3.9 |
| **Almacenamiento de Imágenes** | Cloudinary SDK | 1.36.0 |
| **Utilidades** | Lombok | Latest |
| **Frontend** | HTML5 + CSS3 + JavaScript (Bootstrap) | — |

---

## Modelo C4

**Contexto:**

<img width="842" height="441" alt="Modelo C4-Contexto" src="https://github.com/user-attachments/assets/161275a8-8696-42d2-8b1a-dc1e235055ea" />

**Contenedor:**

<img width="867" height="1030" alt="Modelo C4-Contendo" src="https://github.com/user-attachments/assets/3bb32f82-b34b-48b3-8e19-3fa0b39092cb" />

**Componente:**

<img width="1462" height="1058" alt="Modelo C4-Componente" src="https://github.com/user-attachments/assets/1672b3e0-fafc-4b00-87b1-749be6c31c19" />

---

## Prerrequisitos

Antes de clonar y ejecutar el proyecto, asegurarse de tener instalado:

| Herramienta | Versión Mínima | Enlace |
|---|---|---|
| **JDK** | 17 | [adoptium.net](https://adoptium.net/) |
| **Apache Maven** | 3.9 | [maven.apache.org](https://maven.apache.org/) |
| **PostgreSQL** | 14 | [postgresql.org](https://www.postgresql.org/) |
| **Git** | 2.x | [git-scm.com](https://git-scm.com/) |
| **Cuenta Cloudinary** *(opcional para imágenes)* | — | [cloudinary.com](https://cloudinary.com/) |

---

## Instalación y Configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/EnVivo.git
cd EnVivo
```

### 2. Crear la base de datos en PostgreSQL

```sql
CREATE DATABASE events_db;
```

> Hibernate creará las tablas automáticamente al iniciar la aplicación gracias a `ddl-auto: update`.

### 3. Configurar Variables de Entorno

Por motivos de seguridad, las contraseñas y claves de API no están quemadas en el código fuente. El proyecto requiere la configuración de variables de entorno locales:

1. Ubica el archivo `.env.example` en la raíz del proyecto.
2. Crea una copia de este archivo y renómbralo a `.env`.
3. Completa el archivo `.env` con tus credenciales locales y de Cloudinary:

# Configuración de Base de Datos
DB_URL=jdbc:postgresql://localhost:5432/events_db
DB_USERNAME=postgres
DB_PASSWORD=tu_password_local

# Configuración de Cloudinary (Imágenes)
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret

### 4. Instalar dependencias

```bash
mvn clean install -DskipTests
```

---

## Uso y Ejecución

### Ejecutar en local

```bash
mvn spring-boot:run
```

O bien, compilar y ejecutar el `.jar`:

```bash
mvn clean package -DskipTests
java -jar target/EnVivo-0.0.1-SNAPSHOT.jar
```

### Acceder a la aplicación

| Rol | URL | Credenciales por defecto |
|---|---|---|
| **Administrador** | `http://localhost:8080/` | `admin` / `1234` |
| **Cliente** | `http://localhost:8080/` | Registrarse con `/register` |

> El administrador es un **usuario en memoria** definido directamente en `SecurityConfig.java`. Los clientes se registran a través de la base de datos.

---

## Logging y Observabilidad (Grafana, Loki, Promtail)

### Arquitectura de logs (breve)

- **Promtail**: recolecta logs desde los contenedores y archivos del host y los envía a **Loki**.
- **Loki**: almacena los logs de forma escalable y indexa etiquetas para búsquedas eficientes.
- **Grafana**: visualiza los logs consultando a Loki y construye paneles y alertas.

En este proyecto, la aplicación escribe logs en archivos locales (`./logs/app.log`) para que Promtail los lea desde el volumen o ruta del host.

### Ejecución con Docker Compose

El sistema de observabilidad (Grafana + Loki + Promtail) está preparado para levantarse junto con la aplicación mediante `docker compose up -d`.
Los servicios relevantes incluyen los contenedores para Grafana, Loki y Promtail. Al ejecutar `docker compose up -d`, todos los servicios configurados en `docker-compose.yml` deberán iniciarse automáticamente.

Importante: los contenedores están configurados con la política `restart: always`. Esto significa que los servicios se reiniciarán automáticamente si fallan o si el host Docker se reinicia.

### Comportamiento esperado

- Al arrancar el proyecto con Docker Compose, Grafana, Loki y Promtail deben estar en ejecución sin intervención manual.
- La aplicación escribe logs en `./logs/app.log` (ruta relativa al proyecto) y Promtail debe recolectarlos y enviarlos a Loki.

### ¿Qué hacer si los servicios NO se reinician automáticamente?

1. Verificar que el servicio Docker Engine esté activo en la máquina.
2. Ejecutar `docker ps -a` para revisar el estado de los contenedores y detectar errores de arranque.
3. Reiniciar los servicios con:

```bash
docker compose up -d
```

4. Si el problema persiste, recrear los contenedores:

```bash
docker compose up -d --force-recreate
```

5. Revisar logs del contenedor problemático para diagnosticar (ejemplo para Grafana):

```bash
docker logs grafana
```

### Comandos útiles

```bash
docker compose up -d
docker compose down
docker ps
docker logs grafana
```

Si Promtail no está enviando logs a Loki, verificar:

- Que la ruta del archivo (`./logs/app.log`) esté montada o accesible desde el contenedor de Promtail.
- Permisos de lectura en los archivos de logs.
- La configuración de `promtail-config.yaml` para asegurar que el `scrape_config` incluye la ruta correspondiente.

Esta configuración facilita la integración con Grafana/Loki y permite búsquedas y dashboards académicos para análisis de eventos y fallos.

## Endpoints Principales

### Públicos (sin autenticación)

| Método | URL | Descripción |
|---|---|---|
| `GET` | `/` | Página de login |
| `POST` | `/login` | Procesamiento del login (Spring Security) |
| `POST` | `/register` | Registro de nuevo usuario con rol `CLIENTE` |
| `GET` | `/main` | Listado público de eventos con paginación y búsqueda |
| `GET` | `/logout` | Cierre de sesión |

### Autenticados — Clientes (`ROLE_CLIENTE`)

| Método | URL | Descripción |
|---|---|---|
| `GET` | `/favorites` | Panel de eventos favoritos del usuario |
| `POST` | `/evento/{id}/interest` | Marcar / desmarcar un evento como "Me interesa" |

### Administrador (`ROLE_ADMIN`)

| Método | URL | Descripción |
|---|---|---|
| `GET` | `/admin` | Panel de administración con listado de eventos |
| `POST` | `/admin/guardar` | Crear un nuevo evento (con imagen) |
| `GET` | `/admin/editar/{id}` | Formulario de edición de un evento |
| `POST` | `/admin/editar/{id}` | Guardar cambios de un evento |
| `GET` | `/admin/eliminar/{id}` | Eliminar un evento |
| `GET` | `/categories` | Gestión de categorías |
| `POST` | `/categories/create` | Crear o editar una categoría |
| `GET` | `/categories/delete/{id}` | Eliminar una categoría |
| `GET` | `/reports` | Reporte Top 10 de eventos por interés |

> Los endpoints de interés (`/evento/{id}/interest`) devuelven **JSON** (`@ResponseBody`) con el nuevo estado y un mensaje de confirmación.

---

## Contribución

Para contribuir al proyecto, se deben seguir los siguientes pasos:

1. **Hacer un fork** del repositorio.
2. Crea una rama para la funcionalidad:
   ```bash
   git checkout -b nueva-funcionalidad
   ```
3. Realizar cambios y hacer commit:
   ```bash
   git commit -m "EV-X-Nueva-funcionalidad"
   ```
4. Subir la rama:
   ```bash
   git push origin nueva-funcionalidad
   ```
5. Abrir un **Pull Request** describiendo los cambios realizados.

---

## Autores

- Arian Selene Daza Ochoa 
- Wilson Alexander 
- Valeria Tocarruncho Mosquera
---

