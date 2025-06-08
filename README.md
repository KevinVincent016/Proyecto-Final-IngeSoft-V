# Documentación del Proyecto eCommerce Microservices

### Santiago Jose Belalcazar - A00381777

## 1. Introducción

### 1.1 Objetivo del Taller

Este taller tiene como propósito guiar el desarrollo de una arquitectura de microservicios robusta y escalable para un sistema de comercio electrónico, aplicando las mejores prácticas de ingeniería de software. El proyecto busca no solo implementar la solución técnica, sino también fomentar la comprensión profunda de los principios de microservicios, DevOps, automatización y calidad de software. Los objetivos principales son:

- **Diseñar una arquitectura desacoplada y escalable**, donde cada microservicio pueda evolucionar y desplegarse de forma independiente, facilitando la innovación y la resiliencia.
- **Automatizar el ciclo de vida del software** mediante pipelines CI/CD con Jenkins, asegurando entregas rápidas, seguras y repetibles.
- **Garantizar portabilidad y consistencia** usando Docker para la containerización de servicios, permitiendo que el entorno de desarrollo, pruebas y producción sean equivalentes.
- **Orquestar y gestionar servicios en producción** con Kubernetes, asegurando alta disponibilidad, autoescalado y despliegues controlados.
- **Implementar un enfoque integral de testing**, cubriendo desde pruebas unitarias hasta pruebas de rendimiento, para asegurar la calidad y confiabilidad del sistema.
- **Asegurar observabilidad y trazabilidad** con herramientas de monitoreo distribuido, facilitando la detección y resolución de problemas en entornos complejos.
- **Centralizar la gestión de configuración** para mantener la coherencia y flexibilidad en todos los entornos.

### 1.2 Microservicios Seleccionados y Justificación

La arquitectura está compuesta por **10 microservicios principales** y **3 servicios de infraestructura**, cada uno diseñado para cumplir una responsabilidad específica dentro del ecosistema de comercio electrónico:

#### Microservicios de Negocio

| Microservicio | Puerto | Justificación |
|---------------|--------|---------------|
| **user-service** | 8700 | Gestiona toda la lógica relacionada con usuarios (registro, autenticación, perfiles). Separado para permitir escalabilidad independiente y seguridad especializada. |
| **product-service** | 8500 | Maneja el catálogo de productos, inventario y búsquedas. Crucial para el rendimiento ya que es uno de los servicios más consultados. |
| **order-service** | 8300 | Procesa y gestiona pedidos. Requiere alta disponibilidad y consistencia de datos, por lo que se beneficia de estar aislado. |
| **payment-service** | 8400 | Procesa transacciones financieras. Separado por seguridad, cumplimiento PCI-DSS y para permitir integración con múltiples gateways de pago. |
| **shipping-service** | 8600 | Gestiona envíos y logística. Permite integración independiente con diferentes proveedores de transporte. |
| **favourite-service** | 8800 | Maneja listas de favoritos de usuarios. Microservicio ligero que puede escalar independientemente según el uso. |

#### Servicios de Infraestructura

| Servicio | Puerto | Justificación |
|----------|--------|---------------|
| **service-discovery** (Eureka) | 8761 | Registro y descubrimiento automático de servicios. Fundamental para la comunicación dinámica entre microservicios. |
| **cloud-config** | 9296 | Configuración centralizada externa. Permite cambios de configuración sin redespliegue y mantiene consistencia entre entornos. |
| **api-gateway** | - | Punto de entrada único para clientes. Proporciona routing, load balancing, autenticación y rate limiting. |

#### Servicios de Soporte

| Servicio | Puerto | Justificación |
|----------|--------|---------------|
| **proxy-client** | - | Cliente proxy para comunicación entre servicios. Abstrae la comunicación HTTP y maneja circuit breakers. |
| **zipkin** | 9411 | Trazabilidad distribuida. Esencial para debugging y monitoring de requests que atraviesan múltiples servicios. |

### 1.3 Herramientas y Tecnologías Utilizadas

#### 🔧 Desarrollo y Framework
- **Spring Boot**: Framework principal para el desarrollo de microservicios Java

- **Maven**: Gestión de dependencias y construcción de proyectos
- **Java 11**: Versión LTS para estabilidad y soporte a largo plazo

#### DevOps y CI/CD
- **Jenkins**: Orquestación de pipelines de CI/CD con stages automatizados
  - Construcción y empaquetado con Maven
  - Ejecución de pruebas automatizadas (unitarias, integración, E2E)
  - Construcción y push de imágenes Docker
  - Despliegue automatizado en Kubernetes
- **Git**: Control de versiones con branching strategy (dev, release, master)

####  Containerización y Orquestación
- **Docker**: Containerización de todos los servicios para garantizar portabilidad
- **Docker Hub**: Registro de imágenes para distribución
- **Kubernetes**: Orquestación de contenedores con manifests YAML
  - Deployments, Services, ConfigMaps
  - Health checks y rolling updates
  - Namespace isolation (`ecommerce`)

####  Testing y Calidad
- **JUnit**: Pruebas unitarias para lógica de negocio
- **Spring Boot Test**: Pruebas de integración con contexto de aplicación
- **Locust**: Herramienta de testing de performance y estrés
  - Pruebas de carga con 5 usuarios concurrentes
  - Pruebas de estrés con 10 usuarios concurrentes
  - Generación de reportes HTML con métricas detalladas
- **Testcontainers**: Testing de integración con contenedores reales



Esta arquitectura robusta permite escalabilidad horizontal, fault tolerance, y deployment independiente de cada servicio, cumpliendo con los principios fundamentales de microservicios y las mejores prácticas de la industria.

## 2. Arquitectura General

### 2.1 Diagrama de Arquitectura de Microservicios

El siguiente diagrama ilustra la arquitectura lógica del sistema, mostrando la interacción entre microservicios, servicios de infraestructura y componentes de soporte. Esta visión global facilita la comprensión de los flujos de datos, la resiliencia y la escalabilidad del sistema.

![Diagrama de Arquitectura](app-architecture.drawio.png)

### 2.2 Interacciones entre los Servicios

- **API Gateway:** Punto de entrada único, gestiona autenticación, balanceo de carga y enrutamiento inteligente.
- **Service Discovery (Eureka):** Permite el registro y descubrimiento dinámico de servicios, facilitando la escalabilidad y tolerancia a fallos.
- **Cloud Config:** Centraliza la configuración, permitiendo cambios sin redespliegue y manteniendo la coherencia entre entornos.
- **Proxy Client:** Abstrae la comunicación HTTP y gestiona circuit breakers, mejorando la resiliencia.
- **Zipkin:** Proporciona trazabilidad distribuida, esencial para el monitoreo y la depuración de flujos complejos.
- **Microservicios de negocio:** Interactúan mediante llamadas HTTP internas, gestionadas por el API Gateway y el Service Discovery, asegurando independencia y escalabilidad.

### 2.3 Ambientes Definidos

El ciclo de vida del software se gestiona a través de tres ambientes principales:

- **dev:** Espacio seguro para experimentación y desarrollo individual.
- **stage:** Entorno de integración y pruebas end-to-end, simula condiciones de producción.
- **master (producción):** Entorno estable y seguro, donde se ejecuta la versión aprobada del sistema.

Cada ambiente cuenta con configuración propia y pipelines de CI/CD dedicados, permitiendo despliegues independientes y controlados.

## 3. Configuración del Entorno

### 3.1 Jenkins (Ejecución Local en Windows con UI)

Jenkins es el motor de automatización que orquesta la construcción, pruebas y despliegue del sistema. Su instalación y configuración en Windows es sencilla y permite gestionar pipelines visualmente desde la interfaz web.

- **Instalación:** Descarga desde [jenkins.io/download](https://www.jenkins.io/download/), instalación asistida y acceso vía navegador.
- **Desbloqueo inicial:** Uso de la clave generada automáticamente para el primer acceso.
- **Plugins recomendados:** Docker Pipeline, Git, Blue Ocean (UI avanzada).
- **Gestión de pipelines:** Creación, monitoreo y parametrización de jobs desde la UI.
- **Pipeline declarativa:** El `Jenkinsfile` define el flujo CI/CD, asegurando reproducibilidad y control de versiones.
- **Despliegue automatizado:** Desde la compilación hasta el despliegue en Kubernetes, todo el ciclo está automatizado y versionado.

**Buenas prácticas:**
- Configurar credenciales y variables sensibles desde la UI de Jenkins.
- Asegurar permisos adecuados para la ejecución de Docker y comandos de despliegue.

### 3.2 Docker y los Dockerfiles de los Microservicios

Docker permite encapsular cada microservicio en una imagen ligera y portable, asegurando que el entorno de ejecución sea idéntico en desarrollo, pruebas y producción.

- **Dockerfile por microservicio:** Define la base, dependencias, variables de entorno y comando de arranque.
- **Imágenes optimizadas:** Uso de imágenes base ligeras y build eficientes.
- **Orquestación local:** Docker Compose permite levantar el sistema completo para pruebas integradas.
- **Automatización en pipeline:** Jenkins construye y publica las imágenes en cada ciclo, garantizando trazabilidad y control de versiones.

### 3.3 Kubernetes

Kubernetes es la plataforma de orquestación que gestiona el ciclo de vida de los contenedores en producción y pruebas.

- **Manifiestos YAML:** Definen deployments, servicios, ConfigMaps y otros recursos.
- **Namespace dedicado:** Aislamiento lógico para el sistema eCommerce.
- **Comandos clave:**
  - `kubectl apply -f k8s/` para desplegar todos los recursos.
  - `kubectl get pods -n ecommerce` y `kubectl get svc -n ecommerce` para monitoreo.
  - `kubectl logs <nombre-del-pod> -n ecommerce` para depuración.
- **Despliegue automatizado:** El pipeline aplica los manifiestos tras la construcción y publicación de imágenes.

### 3.4 Repositorio y Estructura de Carpetas

El repositorio está cuidadosamente organizado para reflejar la separación de responsabilidades y facilitar la navegación y el mantenimiento:

```
ecommerce-microservice-backend-app/
│
├── api-gateway/           # Microservicio API Gateway (con su Dockerfile)
├── cloud-config/          # Servicio de configuración centralizada (con su Dockerfile)
├── favourite-service/     # Microservicio de favoritos (con su Dockerfile)
├── order-service/         # Microservicio de pedidos (con su Dockerfile)
├── payment-service/       # Microservicio de pagos (con su Dockerfile)
├── product-service/       # Microservicio de productos (con su Dockerfile)
├── proxy-client/          # Cliente proxy para comunicación entre servicios (con su Dockerfile)
├── service-discovery/     # Servicio Eureka para descubrimiento (con su Dockerfile)
├── shipping-service/      # Microservicio de envíos (con su Dockerfile)
├── user-service/          # Microservicio de usuarios (con su Dockerfile)
├── zipkin/                # Servicio de trazabilidad distribuida (con su Dockerfile)
│
├── k8s/                   # Manifiestos de Kubernetes
├── locust/                # Pruebas de carga y rendimiento con Locust
├── e2e-tests/             # Pruebas de E2E
├── Jenkinsfile            # Pipeline de CI/CD
├── compose.yml            # Orquestación local con Docker Compose
├── DOCUMENTATION.md       # Documentación del proyecto
├── README.md              # Instrucciones rápidas
└── ...otros archivos
```

Esta estructura promueve la modularidad, la escalabilidad y la facilidad de mantenimiento, permitiendo que cada equipo o desarrollador trabaje de forma independiente en su microservicio, sin interferir con el resto del sistema.

---

## 4. Pipelines de Integración y Entrega Continua

La automatización del ciclo de vida del software es clave para la agilidad y la calidad. En este proyecto, una pipeline declarativa en Jenkins gestiona de forma inteligente los flujos de integración y despliegue para las ramas principales (`dev`, `stage`, `master`). Esto asegura que cada entorno reciba el nivel de validación y control adecuado, minimizando riesgos y acelerando la entrega de valor.

### 4.1 Pipeline para entorno de desarrollo (`dev`)

El entorno de desarrollo está orientado a la experimentación y validación rápida de cambios. Cada push o pull request a la rama `dev` dispara la pipeline, que ejecuta:

1. Compilación y análisis estático del código para detectar errores tempranos.
2. Pruebas unitarias y de integración para validar la lógica y la interacción interna.
3. Construcción de imágenes Docker etiquetadas como `dev`.
4. Publicación de imágenes en el registro.
5. Despliegue automático en el entorno de desarrollo de Kubernetes.

Esto permite a los desarrolladores recibir feedback inmediato y trabajar en un entorno lo más parecido posible a producción.


### 4.2 Pipeline para entorno de staging (`stage`)

El entorno de staging simula condiciones de producción y es el espacio para validaciones exhaustivas antes del despliegue final. Cada push a la rama `stage` ejecuta:

1. Compilación y pruebas (unitarias, integración y, opcionalmente, end-to-end).
2. Construcción y push de imágenes Docker etiquetadas como `stage`.
3. Despliegue en el entorno de staging de Kubernetes.
4. Ejecución de pruebas automatizadas post-despliegue (carga, estres, E2E, validación de endpoints).

Esto asegura que solo versiones estables y validadas lleguen a producción.

### 4.3 Pipeline de despliegue (`master` / Producción)

El entorno de producción es el más crítico y requiere validaciones adicionales. Cada push o merge a la rama `master` ejecuta:

1. Compilación y pruebas exhaustivas.
2. Construcción y push de imágenes Docker etiquetadas como `prod`.
3. Despliegue en el entorno de producción de Kubernetes.
4. Generación y publicación automática de release notes.
5. Notificación de despliegue exitoso.

Esto garantiza trazabilidad, control y calidad en cada entrega al usuario final.

### 4.4 Resumen del manejo de ramas y pipeline

La pipeline única, combinada con la directiva `when { branch 'nombre' }`, permite adaptar el flujo a cada entorno, asegurando que cada rama reciba el tratamiento adecuado y que los despliegues sean seguros, trazables y eficientes.

---

## 5. Diseño y Ejecución de Pruebas

La calidad del software es un pilar fundamental en sistemas distribuidos. Este proyecto implementa una estrategia de testing integral, cubriendo desde la lógica interna de cada microservicio hasta la experiencia real del usuario y el rendimiento bajo carga. Cada tipo de prueba aporta una capa de confianza y permite detectar errores en diferentes etapas del ciclo de vida.

### 5.1 Pruebas Unitarias

Las pruebas unitarias validan la lógica de negocio de cada componente de forma aislada, usando JUnit y Mockito. Permiten detectar errores en etapas tempranas y facilitan el refactorizado seguro. Cada microservicio cuenta con pruebas enfocadas en sus métodos críticos, asegurando que la funcionalidad básica sea confiable y mantenible.

Cada microservicio cuenta con su propio conjunto de pruebas unitarias, enfocadas en los métodos más críticos de su lógica interna. Por ejemplo, en `user-service` se prueban métodos como `findById`, `save` y `update`, asegurando que el servicio gestione correctamente los datos de usuario. En `product-service` se valida la obtención y manipulación de productos.

| Microservicio      | Clase de prueba                       | Ejemplo de método probado                |
|--------------------|---------------------------------------|------------------------------------------|
| user-service       | UserServiceImplTest                   | testFindById_ReturnsUserDto, testSave_ReturnsSavedUserDto |
| product-service    | ProductServiceImplTest | testFindById_ReturnsProductDto, testSave_ReturnsSavedProductDto, testFindById_ThrowsException_WhenNotFound |

**Ejemplo real de prueba unitaria (user-service):**
Esta prueba verifica que el método `testFindById_ReturnsUserDto` del servicio de usuarios retorna correctamente un usuario existente. Se simula el repositorio con Mockito para devolver un usuario de prueba y se comprueba que los datos retornados coinciden con los esperados.
```java
@Test
void testFindById_ReturnsUserDto() {
    Credential credential = Credential.builder().credentialId(1).username("testuser").password("pass").isEnabled(true).isAccountNonExpired(true).isAccountNonLocked(true).isCredentialsNonExpired(true).build();
    User user = User.builder().userId(1).firstName("John").lastName("Doe").email("john@doe.com").credential(credential).build();

    credential.setUser(user);

    when(userRepository.findById(1)).thenReturn(Optional.of(user));

    UserDto result = userService.findById(1);

    assertNotNull(result);
    assertEquals("John", result.getFirstName());
    assertEquals("testuser", result.getCredentialDto().getUsername());
}
```

**Ejemplo real de prueba unitaria (product-service):**
Aquí se valida que el método `testFindById_ReturnsProductDto` del servicio de productos retorna correctamente un DTO de producto cuando existe en el repositorio simulado. Se comprueban los campos clave del producto.
```java
@Test
void testFindById_ReturnsProductDto() {
    Category category = Category.builder().categoryId(1).categoryTitle("Test Category").build();
    Product product = Product.builder().productId(1).productTitle("Test Product").category(category).build();
    when(productRepository.findById(1)).thenReturn(Optional.of(product));

    ProductDto result = productService.findById(1);

    assertNotNull(result);
    assertEquals(1, result.getProductId());
    assertEquals("Test Product", result.getProductTitle());
    assertNotNull(result.getCategoryDto());
    assertEquals("Test Category", result.getCategoryDto().getCategoryTitle());
}
```

---

### 5.2 Pruebas de Integración

Las pruebas de integración validan la interacción entre componentes dentro de un microservicio, asegurando que los endpoints REST y la integración entre capas funcionen correctamente. Usan bases embebidas o contenedores para simular el entorno real y detectar problemas de configuración o comunicación.

Por ejemplo, tanto en el `user-service` como en `product-service` se prueba la creación de un usuario y producto respectivamente a través del endpoint REST, verificando que la respuesta sea la esperada y que los datos se almacenen correctamente.

**Ejemplo real de prueba de integración (user-service):**
Esta prueba simula una petición HTTP POST para crear un usuario, usando `TestRestTemplate`. Se verifica que la respuesta sea exitosa y que los datos retornados coincidan con los enviados.
```java
@Test
void testSaveAndFindUser() {
    Credential credential = Credential.builder().username("integrationUser").password("pass").isEnabled(true).isAccountNonExpired(true).isAccountNonLocked(true).isCredentialsNonExpired(true).build();
    User user = User.builder().firstName("Jane").lastName("Smith").email("jane@smith.com").credential(credential).build();

    credential.setUser(user);

    user = userRepository.save(user);

    Optional<User> found = userRepository.findById(user.getUserId());
    assertTrue(found.isPresent());
    assertEquals("Jane", found.get().getFirstName());
    assertEquals("integrationUser", found.get().getCredential().getUsername());
}
```

**Ejemplo real de prueba de integración (product-service):**
Esta prueba simula una petición HTTP POST para crear un producto, usando `TestRestTemplate`. Se verifica que la respuesta sea exitosa y que los datos retornados coincidan con los enviados.
```java
@Test
void testSaveAndFindProduct() {
    Category category = Category.builder().categoryTitle("Electronics").build();
    category = categoryRepository.save(category);

    Product product = Product.builder().productTitle("Laptop").category(category).build();
    product = productRepository.save(product);

    Optional<Product> found = productRepository.findById(product.getProductId());
    assertTrue(found.isPresent());
    assertEquals("Laptop", found.get().getProductTitle());
    assertEquals("Electronics", found.get().getCategory().getCategoryTitle());
}
```

---

### 5.3 Pruebas E2E (End-to-End)

Las pruebas E2E simulan flujos completos de usuario a través de varios microservicios, validando la integración real del sistema en condiciones similares a producción. Son clave para asegurar que los procesos críticos del negocio funcionen de extremo a extremo y que la experiencia del usuario sea consistente.

**Ejemplo real de prueba E2E (user-service):**
Esta prueba simula el registro completo de un usuario, enviando un payload complejo que incluye credenciales y dirección. Se valida que el endpoint acepte la petición y retorne un código de éxito.
```java
@Test
void shouldSaveUser() {
    Map<String, Object> credentialPayload = Map.of(
            "username", "e2euser",
            "password", "e2epassword",
            "roleBasedAuthority", "ROLE_USER",
            "isEnabled", true,
            "isAccountNonExpired", true,
            "isAccountNonLocked", true,
            "isCredentialsNonExpired", true
    );

    Map<String, Object> addressPayload = Map.of(
            "fullAddress", "123 E2E Test St",
            "postalCode", "12345",
            "city", "E2E City"
    );

    Map<String, Object> userPayload = Map.of(
            "firstName", "E2E",
            "lastName", "User",
            "imageUrl", "http://placeholder:200",
            "email", "e2euser@example.com",
            "phone", "1234567890",
            "addressDtos", List.of(addressPayload),
            "credential", credentialPayload
    );

    ResponseEntity<String> response = restFacade.post(
            userServiceUrl + "/user-service/api/users",
            userPayload,
            String.class
    );

    System.out.println("Response: " + response.getBody());
    System.out.println("Status Code: " + response.getStatusCode());
    assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());
}
```

**Ejemplo real de prueba E2E (order-service):**
Esta prueba comprueba que el endpoint de ordenes retorna correctamente la lista de ordenes del usuario autenticado, validando la integración entre autenticación, lógica de negocio y persistencia.
```java
@Test
void shouldGetAllOrders() {
    ResponseEntity<String> response = restFacade.get(orderServiceUrl + "/order-service/api/orders", String.class);
    System.out.println("Response: " + response.getBody());
    System.out.println("Status Code: " + response.getStatusCode());
    assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());
}
```

**Ejemplo real de prueba E2E (favourite-service):**
Esta prueba comprueba que el endpoint de favoritos retorna correctamente la lista de favoritos del usuario autenticado, validando la integración entre autenticación, lógica de negocio y persistencia.
```java
@Test
void shouldGetAllFavourites() {
    ResponseEntity<String> response = restFacade.get(favouriteServiceUrl + "/favourite-service/api/favourites",String.class);
    System.out.println("Response: " + response.getBody());
    System.out.println("Status Code: " + response.getStatusCode());
    assertTrue(response.getStatusCode().is2xxSuccessful(), "Unexpected status code: " + response.getStatusCode());
}
```

---

### 5.4 Pruebas de Rendimiento con Locust

Las pruebas de rendimiento, realizadas con Locust, permiten simular usuarios concurrentes y medir la capacidad, estabilidad y tiempos de respuesta del sistema bajo carga. Son esenciales para identificar cuellos de botella y validar la escalabilidad antes de exponer el sistema a usuarios reales.

Los scripts de Locust, ubicados en la carpeta `locust/`, definen escenarios de uso típicos, como navegación de productos y creación de órdenes. Se pueden ajustar el número de usuarios concurrentes y el tiempo de espera entre acciones para simular diferentes condiciones de carga.

**Ejemplo real de script Locust (locust/locustfile.py):**
Este script define dos tareas principales: consultar la lista de productos y crear una orden. Cada usuario virtual ejecuta estas tareas de forma aleatoria, permitiendo medir el rendimiento de los endpoints más críticos del sistema.
```python
from locust import HttpUser, task, between

class EcommerceUser(HttpUser):
    wait_time = between(1, 5)

    @task
    def view_products(self):
        self.client.get("/products")

    @task
    def create_order(self):
        self.client.post("/orders", json={"productId": 1, "quantity": 1})
```

Al ejecutar estas pruebas, Locust genera reportes detallados con métricas como tiempos de respuesta, throughput y tasa de errores, facilitando la toma de decisiones para optimización y dimensionamiento del sistema.

## 6. Proceso Paso a Paso del Taller: Implementación de Microservicios con CI/CD

Esta sección describe de manera detallada y secuencial cómo se implementó el proyecto de microservicios de eCommerce, incluyendo la configuración de Docker, el despliegue en Kubernetes con Minikube, la configuración de Jenkins para CI/CD, y las pruebas de rendimiento con Locust. Cada proceso se documenta tal como fue ejecutado durante el desarrollo real del proyecto.

### 6.1 Configuración del Entorno de Desarrollo Local

El desarrollo comenzó con la configuración de un entorno local que permitiera ejecutar todos los microservicios de manera integrada. Para esto se utilizó Docker Compose como herramienta principal de orquestación local. Los archivos `core.yml` y `compose.yml` fueron diseñados para levantar todos los servicios necesarios del ecosistema de microservicios de forma coordinada.

La configuración inicial requirió la creación de un archivo Core que definiera los microservicios de infraestructura y soporte: service-discovery (Eureka Server), cloud-config (Configuration Server) y un Docker Compose que definiera cada uno de los 8 microservicios del proyecto: api-gateway, product-service, user-service, order-service, payment-service, shipping-service, favourite-service y proxy-client. Cada servicio fue configurado con sus respectivos puertos, variables de entorno y dependencias específicas.

![alt text](images/image_core_yml.png)
![alt text](images/image_compose_yml.png)

La estructura del core.yml y compose.yml incluyen la definición de redes internas que permiten la comunicación entre servicios, volúmenes persistentes para datos que requieren permanencia, y la configuración de healthchecks para garantizar que los servicios dependientes no inicien hasta que sus dependencias estén completamente operativas.

### 6.2 Creación de Dockerfiles para cada Microservicio

Cada microservicio requirió la creación de un Dockerfile específico que definiera su entorno de ejecución. Durante el desarrollo se estableció un patrón estándar para todos los servicios Java Spring Boot, utilizando OpenJDK 11 como imagen base debido a la compatibilidad.

 El Dockerfile implementado utiliza un enfoque multi-stage que optimiza el tamaño final de la imagen, separando la fase de construcción de la fase de ejecución.

![alt text](images/image_dockerfile.png)



La configuración de cada Dockerfile consideró aspectos específicos como los puertos de exposición únicos para cada servicio, las variables de entorno necesarias para la conexión con otros servicios.

### 6.3 Configuración de Docker Hub

Para almacenar y distribuir las imágenes Docker de los microservicios, se configuró Docker Hub como registro central de contenedores. Este proceso requirió la creación de una cuenta en Docker Hub y la configuración del repositorio para almacenar todas las imágenes del proyecto.

Se creó la cuenta con el usuario `sanjodb` que se utilizó consistentemente a lo largo del proyecto para mantener todas las imágenes organizadas bajo un mismo namespace. Esta configuración facilitó la gestión centralizada de versiones y el acceso controlado a las imágenes desde diferentes entornos.

El proceso incluyó la configuración de repositorios públicos para cada uno de los 11 microservicios del proyecto. Cada servicio tiene su propio repositorio en Docker Hub siguiendo la convención de nombres: `sanjodb/service-name`, donde service-name corresponde a cada microservicio (api-gateway, user-service, product-service, etc.).

![alt text](images/image_dockerhub_general.png)

Para automatizar el proceso de subida de imágenes, se configuraron las credenciales de Docker Hub que posteriormente serían utilizadas por Jenkins durante el pipeline de CI/CD. Esto permite que el pipeline construya las imágenes localmente y las suba automáticamente al registro de Docker Hub con tags específicos según la rama y versión del código.

La estrategia de tagging implementada utiliza diferentes etiquetas según el entorno:
- **Tag `dev`**: Para imágenes construidas desde la rama de desarrollo
- **Tag `stage`**: Para imágenes del entorno de staging
- **Tag `prod`**: Para imágenes de producción desde la rama master

![alt text](images/image_dockerhub_especifico.png)

Esta configuración de Docker Hub es fundamental para el pipeline de CI/CD, ya que actúa como el puente entre la construcción de imágenes y el despliegue en Kubernetes, permitiendo que los diferentes entornos accedan a las versiones correctas de cada microservicio.

### 6.4 Instalación y Configuración de Minikube

Para el entorno de Kubernetes local se procedió con la instalación de Minikube, que permite ejecutar un clúster de Kubernetes en una máquina local para desarrollo y pruebas. El proceso comenzó con la descarga e instalación de Minikube desde el sitio oficial, seguido de la configuración del driver de virtualización adecuado para el sistema operativo utilizado.

Una vez instalado Minikube, se procedió con la inicialización del clúster local mediante el comando `minikube start driver=docker`. La configuración inicial incluyó la habilitación de addons necesarios como el ingress controller y el dashboard de Kubernetes.

![alt text](images/image_minikube_docker.png)

La verificación de la instalación se realizó mediante comandos como `kubectl cluster-info` y `minikube dashboard` para confirmar que el clúster estaba operativo y accesible. Posteriormente se configuró el contexto de kubectl para apuntar al clúster de Minikube, permitiendo la ejecución de comandos de Kubernetes desde la terminal local.

![alt text](images/image_kubernetes.png)

### 6.5 Configuración de Manifiestos de Kubernetes

La implementación en Kubernetes requirió la creación de manifiestos YAML que definieran cómo cada microservicio sería desplegado en el clúster. El proceso comenzó con la creación de la estructura de carpetas `k8s/` que contendría todos los archivos de configuración necesarios para el despliegue.

Se inició con la creación del namespace `ecommerce` para aislar todos los recursos del proyecto en un espacio de nombres específico. 

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: ecommerce
```

![alt text](images/image_k8s.png)

Para cada microservicio se crearon tres tipos de manifiestos: Deployment (que define cómo se ejecuta el servicio), Service (que expone el servicio internamente en el clúster), y opcionalmente Ingress (para servicios que requieren acceso externo como el Api-Gateway). Los Deployments fueron configurados con estrategias de rolling update, health checks, y resource limits apropiados para cada servicio.

El proceso incluyó la configuración específica de cada servicio con sus respectivas variables de entorno, como las URLs de connection para la base de datos, los endpoints de service discovery (Eureka), y las configuraciones específicas de cada microservicio. Los Services fueron configurados con ClusterIP para comunicación interna y LoadBalancer para servicios que requieren acceso externo como el API Gateway.

![alt text](images/image_deployment.png)

![alt text](images/image_service.png)

### 6.6 Configuración de Jenkins para CI/CD

La implementación del pipeline de CI/CD requirió la instalación y configuración de Jenkins como servidor de integración continua. El proceso comenzó con la instalación de Jenkins en el entorno local, seguido de la configuración de plugins necesarios para Docker, Kubernetes, y Git.

![alt text](images/image_panel_deafult.png)

Una vez instalado Jenkins, se procedió con la configuración de credenciales para acceder a Docker Hub donde se almacenarían las imágenes de los microservicios. Estas credenciales fueron configuradas con el usuario `sanjodb` que se utilizó consistentemente a lo largo del proyecto para el almacenamiento de todas las imágenes Docker. Tambien un access token de GitHub.

![alt text](images/image_credentials.png)

La configuración incluyó la instalación de plugins específicos como Docker Pipeline, Kubernetes CLI, y Pipeline Stage View para mejorar la funcionalidad del pipeline. También se configuró la integración con Git para permitir que Jenkins detecte cambios en el repositorio y ejecute el pipeline correspondiente.

![alt text](images/image_plugins.png)

Se estableció un pipeline multibranch que permite manejar diferentes ramas del repositorio de forma independiente, cada una con su propio ciclo de despliegue. Esta configuración es particularmente útil durante el desarrollo cuando se trabaja con features branches que requieren despliegues separados para pruebas.

![alt text](images/image_multibranch.png)

### 6.7 Desarrollo del Jenkinsfile y Stages del Pipeline

El corazón del pipeline de CI/CD es el Jenkinsfile que define todas las etapas del proceso de construcción, pruebas y despliegue. El archivo desarrollado contiene 438 líneas de código que implementa un pipeline sofisticado con lógica condicional basada en ramas y múltiples tipos de pruebas.

![alt text](images/image_jenkinsfile.png)

El Jenkinsfile implementa una estrategia de ramificación que utiliza bloques `when {}` para definir qué stages se ejecutan en cada rama específica. Esta configuración permite diferentes flujos de trabajo según el entorno de destino:

#### 6.7.1 Configuración de Ramas y Profiles

El stage **Init** define la configuración específica para cada rama del repositorio. Utiliza un mapeo que asocia cada rama con un perfil de Spring y un tag de imagen específico:

- **master**: Utiliza perfil `prod` con tag `prod` para despliegues en producción
- **stage**: Utiliza perfil `stage` con tag `stage` para el entorno de staging 
- **dev**: Utiliza perfil `dev` con tag `dev` para desarrollo local
- **feature/**: Ramas de características que heredan la configuración de dev

```groovy
def profileConfig = [
    master : ['prod', '-prod'],
    stage  : ['stage', '-stage']
]
def config = profileConfig[env.BRANCH_NAME] ?: ['dev', '-dev']
```

#### 6.7.2 Stages Principales del Pipeline

**Stage: Build & Package**
```groovy
when { anyOf { branch 'master'; branch 'stage'; branch 'dev'; } }
```
Este stage ejecuta `mvn clean package -DskipTests` y se activa en las ramas principales. Construye todos los microservicios del proyecto sin ejecutar pruebas para optimizar tiempo de construcción.

![alt text](images/image_build&package.png)

**Stage: Build & Push Docker Images** 
```groovy
when { anyOf { branch 'stage'; branch 'master' } }
```
Exclusivo para ramas `stage` y `master`, este stage construye y sube las imágenes Docker a DockerHub con el usuario `sanjodb`. Cada uno de los 11 microservicios es procesado individualmente con tags específicos según la rama.

![alt text](images/image_buildandpushdocker.png)

**Stage: Unit Tests**
```groovy
when {
    anyOf {
        branch 'dev'; branch 'stage';
        expression { env.BRANCH_NAME.startsWith('feature/') }
    }
}
```
Ejecuta pruebas unitarias en `user-service` y `product-service`. Se activa en ramas de desarrollo, staging y ramas de feature. Los resultados se publican usando el plugin JUnit de Jenkins.

![alt text](images/image_unit_test.png)

**Stage: Integration Tests**
```groovy
when {
    anyOf {
        branch 'dev'; branch 'stage';
        expression { env.BRANCH_NAME.startsWith('feature/') }
    }
}
```
Ejecuta pruebas de integración usando `mvn verify` en `user-service` y `product-service`. Valida la interacción entre componentes y la integración con bases de datos embebidas.

![alt text](images/image_integration.png)

**Stage: E2E Tests**
```groovy
when { anyOf { branch 'stage'; } }
```
Exclusivo para la rama `stage`, ejecuta las pruebas end-to-end que validan flujos completos de usuario a través de múltiples microservicios. Utiliza el módulo `e2e-tests` del proyecto.

![alt text](images/image_e2e.png)

### 6.8 Ejecución Específica por Rama

#### 6.8.1 Rama Development (dev)
En la rama `dev` se ejecutan los siguientes stages:
- Init (configuración dev)
- Ensure Namespace 
- Checkout
- Verify Tools
- Build & Package
- Unit Tests
- Integration Tests

![alt text](images/image_general_dev.png)
![alt text](images/image_pipeline_dev.png)

#### 6.8.2 Rama Staging (stage)  
La rama `stage` ejecuta el pipeline más completo incluyendo:
- Todos los stages de `dev`
- Build & Push Docker Images
- E2E Tests
- Levantar contenedores para pruebas
- Run Load Tests with Locust
- Run Stress Tests with Locust  
- Detener y eliminar contenedores

![alt text](images/image_stage_general.png)
![alt text](images/image_pipeline_stage.png) 
![alt text](images/image_pipeline_stage_2.png) 

#### 6.8.3 Rama Master (master)
La rama `master` se enfoca en despliegue a producción:
- Init (configuración prod) 
- Build & Package
- Build & Push Docker Images  
- Deploy Common Config
- Deploy Core Services
- Deploy Microservices
- Generate and Archive Release Notes 

![alt text](images/image_master_general.png)
![alt text](images/image_rama_master.png)

### 6.8 Implementación Completa de Pruebas

![alt text](images/image_test.png)

#### 6.8.1 Pruebas Unitarias
Las pruebas unitarias se ejecutan en las ramas `dev`, `stage` y `feature/*` usando Maven. El pipeline ejecuta `mvn test` específicamente en los servicios que contienen pruebas unitarias implementadas: `user-service`, `product-service` y `payment-service`. Los resultados se publican automáticamente en Jenkins a través del plugin JUnit.

![alt text](images/image_test_unitarios.png)
![alt text](images/image_unitarios_pipelineJ.png)

#### 6.8.2 Pruebas de Integración  
Las pruebas de integración utilizan `mvn verify` y se ejecutan en `user-service` y `product-service`. Estas pruebas validan la correcta integración entre las capas de la aplicación, incluyendo controladores REST, servicios y repositorios con bases de datos embebidas.

![alt text](images/image_test_integracion.png)
![alt text](images/image_test_integracionJ.png)

#### 6.8.3 Pruebas End-to-End (E2E)
Las pruebas E2E se ejecutan exclusivamente en la rama `stage` usando el módulo `e2e-tests`. Estas pruebas validan flujos completos de usuario que atraviesan múltiples microservicios, asegurando que la integración entre servicios funcione correctamente en un entorno similar a producción.

![alt text](images/image_test_e2e.png)
![alt text](images/image_test_e2eJ.png)

#### 6.8.4 Pruebas de Rendimiento con Locust

**Stage: Levantar contenedores para pruebas**
Antes de ejecutar las pruebas de Locust, el pipeline levanta un entorno de pruebas completo usando Docker. Este stage crea una red `ecommerce-test` y despliega secuencialmente:
- Zipkin (trazabilidad)
- Service Discovery (Eureka)  
- Cloud Config (configuración centralizada)
- Todos los microservicios de negocio

![alt text](images/image_test_containers.png)

Cada servicio incluye health checks para asegurar que esté completamente operativo antes de continuar.

**Stage: Run Load Tests with Locust**
Ejecuta pruebas de carga normal con 10 usuarios concurrentes (`-u 5 -r 2 -t 1m`) en tres servicios críticos:
- order-service (puerto 8300)
- payment-service (puerto 8400)  
- favourite-service (puerto 8800)

![alt text](images/image_load_test.png)

Cada prueba genera un reporte HTML específico que se almacena en `locust-reports/`.

**Stage: Run Stress Tests with Locust**  
Ejecuta pruebas de estrés con 50 usuarios concurrentes (`-u 10 -r 5 -t 1m`) en los mismos servicios. Estas pruebas evalúan el comportamiento del sistema bajo alta carga y permiten identificar puntos de quiebre y cuellos de botella.

![alt text](images/image_stress_test.png)

### 6.9 Resultados 

#### 6.9.1 Resultados de Pruebas de Rendimiento

**Pruebas de Load Testing**
Los resultados de las pruebas de carga con Locust muestran el comportamiento del sistema bajo condiciones normales de uso. Los reportes HTML generados incluyen métricas detalladas de:
- Tiempo de respuesta promedio y percentiles
- Throughput (requests por segundo)
- Tasa de errores
- Distribución de tiempos de respuesta

**Favourite Service**

En las pruebas de carga, el favourite-service mostró un rendimiento muy aceptable. El tiempo de respuesta promedio fue de 520.36 ms, con un percentil 95 del tiempo de respuesta en 110 ms, lo que indica que el 95% de las peticiones fueron atendidas en menos de ese tiempo. La tasa de solicitudes por segundo (RPS) se mantuvo entre 2.0 y 2.5, sin registrar errores. Esto demuestra que el servicio se comporta de manera estable y eficiente bajo condiciones normales de uso.

![alt text](images/image_favourite_load.png)
![alt text](images/total_requests_per_second_favourite_load.png)

**Order Service**

Este servicio presentó el mejor rendimiento en las pruebas de carga. Con un tiempo de respuesta promedio de apenas 21.37 ms y un percentil 95 de 12 ms, el order-service maneja las peticiones con gran rapidez. Su RPS fue alrededor de 2.5, también sin errores. Estas métricas reflejan una alta eficiencia y bajo consumo de recursos, posicionándolo como el servicio más optimizado de los tres en este escenario.

![alt text](images/image_order_load.png)
![alt text](images/total_requests_per_second_order_load.png)

**Payment Service**

El payment-service mostró tiempos de respuesta notablemente más altos comparado con los otros microservicios. El promedio fue de 330.83 ms, con un percentil 95 de 370 ms. Aunque la RPS fue alrededor de 2.5 y no se registraron errores, estos valores indican un rendimiento más limitado, posiblemente por operaciones complejas o mayor uso de recursos. Si bien es funcional bajo carga normal, no es tan eficiente como los otros.

![alt text](images/image_payment_load.png)
![alt text](images/total_requests_per_second_payment_load.png)

**Pruebas de Stress Testing**  

Las pruebas de estrés revelan el comportamiento del sistema bajo alta carga, identificando:
- Límites de capacidad de cada microservicio
- Degradación del rendimiento bajo presión
- Puntos de falla del sistema
- Comportamiento de recovery después de picos de carga

**Favourite Service**

Bajo condiciones de alta carga, el favourite-service mantuvo un rendimiento sobresaliente. Logró una tasa de 5 solicitudes por segundo, con un tiempo de respuesta promedio de 64.9 ms y percentil 95 de 76 ms, sin errores reportados. Esto demuestra que el servicio escala de forma excelente, siendo robusto y confiable incluso en escenarios exigentes.

![alt text](images/image_favourite_stress.png)
![alt text](images/total_requests_per_second_favourite_stress.png)

**Order Service**

El order-service también mostró una capacidad de escalabilidad excepcional en las pruebas de estrés. Alcanzó una tasa de 5.5 RPS con un tiempo de respuesta promedio bajísimo de 9.31 ms y percentil 95 de 7 ms. Estos valores confirman que el servicio es extremadamente rápido y eficiente incluso con un alto volumen de peticiones simultáneas, ideal para entornos de alta demanda.

![alt text](images/image_order_stress.png)
![alt text](images/total_requests_per_second_order_stress.png)

**Payment Service**

En contraste, el payment-service evidenció limitaciones importantes bajo estrés. Aunque alcanzó 5.0 RPS, su tiempo de respuesta promedio se disparó a 646.35 ms, con un percentil 95 de 5200 ms y un pico máximo de 1779 ms, sin errores. Aunque funcional, su rendimiento cae considerablemente bajo carga alta, lo que lo convierte en el principal candidato para optimización o escalamiento.

![alt text](images/image_payment_stress.png)
![alt text](images/total_requests_per_second_payment_stress.png)

#### 6.9.2 Despliegue Exitoso en Kubernetes

**Deploy Core Services**
En la rama `master`, el pipeline despliega los servicios fundamentales en el siguiente orden:
1. **Zipkin**: Sistema de trazabilidad distribuida
2. **Service Discovery**: Servidor Eureka para registro de servicios  
3. **Cloud Config**: Servidor de configuración centralizada

Cada despliegue incluye `kubectl rollout status` con timeout de 300 y 500 segundos para verificar que el servicio esté completamente operativo.

![alt text](images/image_deploy_core.png)


**Deploy Microservices**
Posteriormente se despliegan los microservicios de negocio:
- product-service
- user-service  
- order-service
- payment-service
- api-gateway

Cada microservicio se despliega con la imagen correspondiente del tag de la rama, configuración de variables de entorno específicas, y verificación de rollout exitoso.

![alt text](images/image_deploy_microservices.png)

![alt text](images/image_kubernetes_pods.png)

El proceso concluye con la generación automática de release notes usando `convco changelog` y el archivado de artefactos en Jenkins, proporcionando trazabilidad completa del despliegue y documentación automática de cambios para cada release en producción.

![alt text](images/image_release_notes.png)

![alt text](images/image_changelog_gen.png)

![alt text](images/image_release_notes_vista.png)
