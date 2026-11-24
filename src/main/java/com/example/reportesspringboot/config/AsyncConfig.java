package com.example.reportesspringboot.config;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration // 1. Le dice a Spring que esta clase configura componentes al iniciar la app
@EnableAsync   // 2. Activa el soporte para usar @Async en los Services
public class AsyncConfig {

    @Bean(name = "taskExecutor") // 3. Registra este pool con un nombre para llamarlo desde @Async("taskExecutor")
    public Executor taskExecutor() {
        
        // Creamos la fábrica administrada de hilos (reemplaza hacer "new Thread()" a mano)
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Hilos mínimos siempre activos y listos para trabajar
        executor.setCorePoolSize(4);
        
        // Hilos máximos si llegan muchas tareas al mismo tiempo
        executor.setMaxPoolSize(8);
        
        // Cola de espera: si los 8 hilos están ocupados, hasta 100 tareas esperan turno
        executor.setQueueCapacity(100);
        
        // Prefijo del nombre: en la consola vas a ver [HiloVentas-1], [HiloVentas-2], etc.
        executor.setThreadNamePrefix("HiloVentas-");
        
        // Inicializamos el pool antes de entregarlo a Spring
        executor.initialize();
        
        return executor;
    }
}
