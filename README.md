Markdown
# Simulador de Tráfico Urbano Concurrente en Java

Simulador de tráfico urbano en tiempo real implementado en Java. Modela una red vial mediante un grafo dirigido donde cada nodo representa una intersección y cada arista una calle con propiedades dinámicas (velocidad límite, distancia y concurrida congelación por tráfico).

## Características Principales

* **Algoritmo de Dijkstra:** Cálculo optimizado de la ruta más rápida ($O((V + E) \log V)$) considerando los costos de tiempo en tiempo real.
* **Programación Concurrente:** Simulación multihilo utilizando `ExecutorService` (`FixedThreadPool`) para coordinar el movimiento de múltiples vehículos en paralelo.
* **Hilos Seguros (Thread-Safety):** Manejo de congestión e intersecciones mediante estructuras concurrentes (`ConcurrentHashMap`) y bloqueos reentrantes (`ReentrantLock`).

## Arquitectura del Proyecto

* `Calle`: Representa una arista con cálculo dinámico de costo (tiempo de tránsito) y sincronización con `ReentrantLock`.
* `GrafoUrbano`: Estructura principal que administra las conexiones y ejecuta el cálculo de rutas óptimas.
* `Vehiculo`: Tarea ejecutable (`Runnable`) que recorre las intersecciones simulando tiempos de desplazamiento mediante `Thread.sleep`.
* `SimuladorTraficoUrbano`: Clase principal de arranque y gestión del grupo de hilos.

## Requisitos y Ejecución

* **Java Development Kit (JDK) 8** o superior.

Para compilar y ejecutar desde la terminal:

```bash
javac SimuladorTraficoUrbano.java
java SimuladorTraficoUrbano
