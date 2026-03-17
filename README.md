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

## Arquitectura y Estructura de Carpetas

El proyecto sigue una arquitectura en capas:

```
EnVivo/
├── src/
│   ├── main/
│   │   ├── java/com/edu/uptc/EnVivo/
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java        # Configuración de Spring Security
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java        # Registro de usuarios
│   │   │   │   ├── CategoryController.java    # CRUD de categorías
│   │   │   │   └── EventController.java       # CRUD de eventos, favoritos, reportes
│   │   │   ├── dto/
│   │   │   │   ├── CreateCategoryDTO.java
│   │   │   │   ├── CreateEventDTO.java
│   │   │   │   ├── EventReporteDTO.java
│   │   │   │   └── RegisterDTO.java
│   │   │   ├── entity/
│   │   │   │   ├── Category.java
│   │   │   │   ├── Event.java
│   │   │   │   ├── Role.java
│   │   │   │   └── User.java
│   │   │   ├── repository/
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── EventRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   └── service/
│   │   │       ├── CategoryService.java
│   │   │       ├── CloudinaryService.java
│   │   │       ├── EventService.java
│   │   │       └── UserService.java
│   │   └── resources/
│   │       ├── application.yaml               # Configuración de la aplicación
│   │       └── templates/
│   │           ├── index.html                 # Página de login / inicio
│   │           ├── main.html                  # Vista principal de eventos
│   │           ├── admin.html                 # Panel de administración
│   │           ├── categories.html            # Gestión de categorías
│   │           ├── favorites.html             # Favoritos del usuario
│   │           └── reports.html              # Reporte Top 10 eventos
│   └── test/
│       └── java/com/edu/uptc/EnVivo/
│           └── EnVivoApplicationTests.java
├── pom.xml
└── README.md
```

### Modelo de Datos (Entidades principales)

<img width="1216" height="243" alt="modelo" src="https://github.com/user-attachments/assets/5d46e3ad-82f6-4327-9120-1ac6474fd7e5" />


- **`usuarios`**: Almacena los usuarios registrados (usuario + contraseña cifrada).
- **`roles`**: `ADMIN` o `CLIENTE`.
- **`eventos`**: Catálogo de eventos con nombre, fecha, precio, descripción, imagen y categoría.
- **`categorias`**: Clasificación de los eventos.
- **`favoritos`**: Tabla pivote que registra los eventos marcados como *"Me interesa"*.
- **`usuarios_roles`**: Tabla relacional entre eventos y usuarios

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

Ubica el archivo .env.example en la raíz del proyecto.

Crea una copia de este archivo y renómbralo a .env.

Completa el archivo .env con tus credenciales locales y de Cloudinary:

# Configuración de Base de Datos
DB_URL=jdbc:postgresql://localhost:5432/events_db
DB_USERNAME=postgres
DB_PASSWORD=tu_password_local

# Configuración de Cloudinary (Imágenes)
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret

Nota: El archivo .env ya se encuentra en el .gitignore. Nunca subas tus claves reales al repositorio. Si usas Visual Studio Code, asegúrate de configurar "envFile": "${workspaceFolder}/.env" en tu archivo launch.json.

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

