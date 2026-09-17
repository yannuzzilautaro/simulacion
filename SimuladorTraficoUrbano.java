import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

class Calle {
    String destino;
    double distanciaKm;
    double velocidadLimiteKmH;
    double factorCongestion;
    private final ReentrantLock lock = new ReentrantLock();

    public Calle(String destino, double distanciaKm, double velocidadLimiteKmH) {
        this.destino = destino;
        this.distanciaKm = distanciaKm;
        this.velocidadLimiteKmH = velocidadLimiteKmH;
        this.factorCongestion = 1.0;
    }


    public double getCostoActual() {
        lock.lock();
        try {
            double velocidadReal = velocidadLimiteKmH / factorCongestion;
            return distanciaKm / Math.max(velocidadReal, 5.0);
        } finally {
            lock.unlock();
        }
    }

    public void actualizarCongestion(double nuevoFactor) {
        lock.lock();
        try {
            this.factorCongestion = Math.max(1.0, nuevoFactor);
        } finally {
            lock.unlock();
        }
    }
}

class GrafoUrbano {
    private final Map<String, List<Calle>> adyacencia = new ConcurrentHashMap<>();

    public void agregarInterseccion(String interseccion) {
        adyacencia.putIfAbsent(interseccion, new ArrayList<>());
    }

    public void agregarCalle(String origen, String destino, double distancia, double velocidad) {
        agregarInterseccion(origen);
        agregarInterseccion(destino);
        adyacencia.get(origen).add(new Calle(destino, distancia, velocidad));
    }


    public List<String> calcularRutaMasRapida(String inicio, String fin) {
        Map<String, Double> tiempos = new HashMap<>();
        Map<String, String> previos = new HashMap<>();
        PriorityQueue<NodoDistancia> pq = new PriorityQueue<>(Comparator.comparingDouble(n -> n.tiempoHoras));

        for (String nodo : adyacencia.keySet()) {
            tiempos.put(nodo, Double.MAX_VALUE);
        }

        tiempos.put(inicio, 0.0);
        pq.add(new NodoDistancia(inicio, 0.0));

        while (!pq.isEmpty()) {
            NodoDistancia actual = pq.poll();

            if (actual.nodo.equals(fin)) break;
            if (actual.tiempoHoras > tiempos.get(actual.nodo)) continue;

            for (Calle calle : adyacencia.getOrDefault(actual.nodo, Collections.emptyList())) {
                double nuevoTiempo = tiempos.get(actual.nodo) + calle.getCostoActual();

                if (nuevoTiempo < tiempos.get(calle.destino)) {
                    tiempos.put(calle.destino, nuevoTiempo);
                    previos.put(calle.destino, actual.nodo);
                    pq.add(new NodoDistancia(calle.destino, nuevoTiempo));
                }
            }
        }


        LinkedList<String> ruta = new LinkedList<>();
        String curr = fin;
        if (!previos.containsKey(curr) && !curr.equals(inicio)) return ruta; // Sin ruta

        while (curr != null) {
            ruta.addFirst(curr);
            curr = previos.get(curr);
        }
        return ruta;
    }

    private static class NodoDistancia {
        String nodo;
        double tiempoHoras;
        public NodoDistancia(String nodo, double tiempoHoras) {
            this.nodo = nodo;
            this.tiempoHoras = tiempoHoras;
        }
    }
}


class Vehiculo implements Runnable {
    private final String id;
    private final String origen;
    private final String destino;
    private final GrafoUrbano ciudad;

    public Vehiculo(String id, String origen, String destino, GrafoUrbano ciudad) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.ciudad = ciudad;
    }

    @Override
    public void run() {
        System.out.println("[Vehículo " + id + "] Iniciando viaje de " + origen + " a " + destino);
        List<String> ruta = ciudad.calcularRutaMasRapida(origen, destino);

        if (ruta.isEmpty() || ruta.size() < 2) {
            System.out.println("[Vehículo " + id + "] No se encontró ruta disponible.");
            return;
        }

        for (int i = 0; i < ruta.size() - 1; i++) {
            String actual = ruta.get(i);
            String siguiente = ruta.get(i + 1);

            try {

                Thread.sleep(300); 
                System.out.println("[Vehículo " + id + "] Avanzó de " + actual + " a " + siguiente);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[Vehículo " + id + "] Viaje interrumpido.");
                return;
            }
        }

        System.out.println("[Vehículo " + id + "] ¡Llegó a su destino: " + destino + "!");
    }
}

public class SimuladorTraficoUrbano {
    public static void main(String[] args) throws InterruptedException {
        GrafoUrbano ciudad = new GrafoUrbano();


        ciudad.agregarCalle("A", "B", 2.0, 40.0);
        ciudad.agregarCalle("A", "C", 5.0, 60.0);
        ciudad.agregarCalle("B", "C", 1.5, 30.0);
        ciudad.agregarCalle("B", "D", 4.0, 50.0);
        ciudad.agregarCalle("C", "D", 2.0, 40.0);
        ciudad.agregarCalle("D", "E", 3.0, 50.0);


        ExecutorService executor = Executors.newFixedThreadPool(4);


        executor.submit(new Vehiculo("Auto-1", "A", "E", ciudad));
        executor.submit(new Vehiculo("Auto-2", "A", "D", ciudad));
        executor.submit(new Vehiculo("Moto-1", "C", "E", ciudad));
        executor.submit(new Vehiculo("Colectivo-1", "A", "E", ciudad));

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("\nSimulación de tráfico finalizada con éxito.");
    }
}
