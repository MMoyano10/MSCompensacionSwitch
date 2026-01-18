# 🏦 Microservicio de Compensación y Liquidación (Settlement G4)

### Módulo G4: Motor de Clearing Automatizado, Continuidad Contable y Monitor Operativo.

## 📌 Descripción General
El Microservicio de Compensación actúa como la autoridad contable del Switch Transaccional. Su función es gestionar el ciclo de vida de la liquidación de fondos (Settlement) entre las instituciones participantes.

A diferencia de los sistemas batch tradicionales, este microservicio implementa un Motor de Continuidad en Tiempo Real, diseñado para operar en ventanas de tiempo configurables (minutos/segundos) garantizando que el cierre de un ciclo provoque atómicamente la apertura del siguiente, manteniendo la integridad de los saldos.

## 🚀 Características Clave (Implementación G4)

### 1. ⏱️ Automatización de Ciclos (Scheduler)
El sistema ya no depende de una ejecución manual.
- **Auto-Arranque:** Al iniciar el sistema, si no existen datos, el DataInitializer crea automáticamente el Ciclo #1.
- **Cierre por Tiempo:** Un cronómetro interno monitorea la antigüedad del ciclo abierto. Si supera el umbral configurado (defecto: 5 minutos), ejecuta el cierre automáticamente.

### 2. 🔄 Continuidad Contable (Rolling Balances)
Implementación del principio de "Libro Mayor Continuo".
- Al cerrar el Ciclo N, el sistema calcula los saldos netos.
- Inmediatamente abre el Ciclo N+1.
- **Arrastre de Saldos:** El saldo final del ciclo anterior se inyecta como Saldo Inicial del nuevo ciclo. Esto garantiza trazabilidad ininterrumpida.

### 3. 🔐 Firma Digital JWS (Validez Legal)
Cumplimiento del requisito RNF-SEC-04.
- Los archivos de liquidación (XML ISO 20022) se firman criptográficamente.
- Se utiliza el estándar JWS (JSON Web Signature) con algoritmo RS256 mediante la librería nimbus-jose-jwt.

### 4. 📊 Dashboard Monitor
Exposición de métricas en tiempo real para el tablero de control operativo:
- Estado del Semáforo (Verde/Rojo).
- Identificación del Ciclo Activo.
- Hora de inicio para cálculo de SLA.

## 🛠️ Tecnologías
- **Java 21 & Spring Boot 3.x**
- **Spring Scheduler:** Automatización de tareas.
- **PostgreSQL:** Persistencia relacional estricta.
- **Nimbus JOSE+JWT:** Criptografía y firmas digitales.
- **Lombok & Swagger:** Reducción de código y documentación.

## 🔌 API Reference (V1)

### 🟢 Dashboard & Monitoreo
Endpoints públicos para el Frontend de control.

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/dashboard/monitor` | Semáforo: Retorna estado (OPERATIVO/CERRADO), color (VERDE/ROJO) y ciclo activo. |
| GET | `/api/v1/compensacion/ciclos` | Historial completo de ciclos operativos. |
| GET | `/api/v1/compensacion/ciclos/{id}/posiciones` | Detalle de saldos netos por banco en un ciclo específico. |

### ⚡ Operaciones Core (Switch Interno)
Endpoints utilizados por el MS-Nucleo para registrar movimientos.

| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/compensacion/acumular` | **Auto-Detect:** Registra un débito/crédito en el ciclo ABIERTO actual automáticamente. |
| POST | `/api/v1/compensacion/ciclos/{id}/cierre` | **Settlement Trigger:** Fuerza el cierre, firma el XML y activa la continuidad. (Usado por el Scheduler). |

## ⚙️ Configuración y Ejecución

### Requisitos Previos
- Docker y Docker Compose instalados.
- Puerto 8084 disponible (por defecto).

### Pasos de Despliegue

#### Limpieza (Recomendado para ver la Inicialización):
Si desea ver el Ciclo 1 crearse solo, limpie la base de datos antes de iniciar.

#### Construcción y Arranque:
```bash
./mvnw clean package -DskipTests
docker-compose up -d --build ms-compensacion
```

#### Verificación:
Revise los logs para confirmar la firma JWS y la creación de ciclos:
```bash
docker logs -f ms-compensacion
```
**Salida esperada:** `>>> INICIALIZADOR: Ciclo 1 Creado Automáticamente.`

## 🧪 Escenario de Prueba (Demo)
1. **Inicio:** El sistema levanta y crea el Ciclo 1.
2. **Operación:** Se envían transacciones desde el Switch (`/acumular`).
3. **Corte Automático:** Al pasar 5 minutos, el Scheduler:
   - Valida suma cero.
   - Genera el XML firmado.
   - Cierra el Ciclo 1.
   - Abre el Ciclo 2 arrastrando los saldos netos.
4. **Resultado:** El Dashboard muestra inmediatamente "Ciclo 2" y el semáforo en VERDE.

