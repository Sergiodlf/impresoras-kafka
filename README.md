# Impresoras Kafka

Proyecto académico que implementa un **sistema de mensajería con Apache Kafka y Java** para simular una cola de impresión empresarial distribuida.

El objetivo es aprender el uso de **topics, consumer groups y particiones**, así como la puesta en marcha y mantenimiento de un sistema Kafka en un entorno local de desarrollo.

---

## 1. Descripción general

Los empleados envían documentos para imprimir en formato JSON.  
El sistema procesa estos documentos de la siguiente forma:

1. Recibe los documentos originales.
2. Guarda los documentos originales sin modificar.
3. Transforma los documentos dividiéndolos en páginas de un máximo de 400 caracteres.
4. Envía las páginas a colas de impresión separadas según el tipo:
   - Blanco y Negro
   - Color
5. Simula la impresión guardando las páginas en carpetas del sistema de archivos.

El guardado y la transformación se realizan **en paralelo** mediante distintos *consumer groups*.

---

## 2. Arquitectura de Kafka

### 2.1 Topics utilizados

| Topic | Particiones | Descripción |
|------|------------|------------|
| print-requests | 1 | Entrada de documentos originales |
| print-queue-bw | 3 | Cola de impresión Blanco y Negro |
| print-queue-color | 2 | Cola de impresión Color |

Las particiones de los topics de impresión representan **impresoras lógicas**.

---

### 2.2 Consumer Groups

| Consumer Group | Función |
|---------------|--------|
| archiver-group | Guarda los documentos originales |
| transformer-group | Transforma documentos en páginas |
| printer-bw-group | Simula 3 impresoras B/N |
| printer-color-group | Simula 2 impresoras Color |

El topic `print-requests` es consumido simultáneamente por `archiver-group` y `transformer-group`, permitiendo el procesamiento en paralelo.

---

### 2.3 Enrutamiento a impresoras

El campo `sender` se utiliza como **clave (KEY)** del mensaje Kafka.

Kafka usa esta clave para decidir la partición destino, lo que provoca que:
- Un mismo empleado envíe sus trabajos siempre a la misma impresora.
- La selección de impresora sea automática.
- No sea necesario implementar lógica adicional de reparto.

---

## 3. Estructura del proyecto

```
src/main/java/
├── producer/
│   └── EmployeeProducer.java
├── consumer/
│   ├── ArchiverConsumer.java
│   ├── TransformerConsumer.java
│   └── PrinterConsumer.java
├── model/
│   ├── PrintRequest.java
│   └── PrintPage.java
├── util/
│   ├── JsonUtil.java
│   └── FileUtil.java
└── config/
    └── AppConfig.java
```

Estructura generada durante la ejecución:

```
data/
├── archive/
│   └── <sender>/
│       └── *.json
└── printed/
    ├── bw/
    │   └── printer-<id>/
    └── color/
        └── printer-<id>/
```

---

## 4. Instalación y puesta en marcha (Entorno local de desarrollo)

### 4.1 Requisitos

- Java JDK 17 o superior
- Maven
- Apache Kafka (modo standalone / KRaft)
- Windows

---

### 4.2 Arranque de Kafka

```
.\bin\windows\kafka-storage.bat random-uuid
.\bin\windows\kafka-storage.bat format -t <UUID_GENERADO> -c config\kraft\server.properties
.\bin\windows\kafka-server-start.bat config\kraft\server.properties
```

---

### 4.3 Creación de topics

```
.\bin\windows\kafka-topics.bat --create --topic print-requests --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
.\bin\windows\kafka-topics.bat --create --topic print-queue-bw --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
.\bin\windows\kafka-topics.bat --create --topic print-queue-color --bootstrap-server localhost:9092 --partitions 2 --replication-factor 1
```

---

### 4.4 Ejecución del sistema

1. Ubicate en tu workspace de Eclipse para clonar esta carpeta.
2. Clona el repositorio: 
    ```bash
    git clone https://github.com/Sergiodlf/SimulacionCine.git
    ```

Orden recomendado:

1. ArchiverConsumer
2. TransformerConsumer
3. PrinterConsumer bw 1
4. PrinterConsumer bw 2
5. PrinterConsumer bw 3
6. PrinterConsumer color 1
7. PrinterConsumer color 2

---

## 5. Mantenimiento del sistema

### 5.1 Reinicio de la aplicación

- Detener los procesos Java.
- Volver a ejecutarlos en el orden recomendado.

---

### 5.2 Limpieza de datos generados

Eliminar manualmente las carpetas:

```
data/archive/
data/printed/
```

---

### 5.3 Limpieza y recreación de topics

```
.\bin\windows\kafka-topics.bat --delete --topic print-requests --bootstrap-server localhost:9092
.\bin\windows\kafka-topics.bat --delete --topic print-queue-bw --bootstrap-server localhost:9092
.\bin\windows\kafka-topics.bat --delete --topic print-queue-color --bootstrap-server localhost:9092
```

Después, volver a crear los topics siguiendo el apartado 4.3.

---

## 6. Notas finales

Este proyecto es una **simulación académica** destinada al aprendizaje de Apache Kafka y sistemas de mensajería distribuidos.
