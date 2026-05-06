package controller;

import model.*;
import repo.StationRepository;
import service.TrainService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// add these imports
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import java.time.LocalTime;
import java.util.List;

@SpringBootApplication
// ensure Spring scans all your top-level packages
@ComponentScan(basePackages = {"controller", "service", "repo", "model", "sqlite"})
// ensure JPA repositories are picked up
@EnableJpaRepositories(basePackages = {"repo"})
// ensure JPA entity classes are discovered
@EntityScan(basePackages = {"model"})
public class TrainApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrainApplication.class, args);
    }

    // Seed sample stations and trains for quick demo
    @Bean
    CommandLineRunner runner(TrainService trainService, StationRepository stationRepo) {
        return args -> {
            var sta = stationRepo.findByCode("STA").orElseGet(() -> stationRepo.save(new Station("STA", "Station A")));
            var stb = stationRepo.findByCode("STB").orElseGet(() -> stationRepo.save(new Station("STB", "Station B")));
            var stc = stationRepo.findByCode("STC").orElseGet(() -> stationRepo.save(new Station("STC", "Station C")));
            var std = stationRepo.findByCode("STD").orElseGet(() -> stationRepo.save(new Station("STD", "Station D")));
            var ste = stationRepo.findByCode("STE").orElseGet(() -> stationRepo.save(new Station("STE", "Station E")));
            var stf = stationRepo.findByCode("STF").orElseGet(() -> stationRepo.save(new Station("STF", "Station F")));

            if (trainService.findByCode("T1").isEmpty()) {
                Train t1 = new Train("T1", "InterCity 1", 100);
                List<StopTime> stops1 = List.of(
                        new StopTime(t1, sta, 0, null, LocalTime.of(8, 0)),
                        new StopTime(t1, stb, 1, LocalTime.of(8, 30), LocalTime.of(8, 35)),
                        new StopTime(t1, stc, 2, LocalTime.of(9, 15), LocalTime.of(9, 20)),
                        new StopTime(t1, std, 3, LocalTime.of(10, 0), null)
                );
                trainService.createTrain(t1.getTrainCode(), t1.getName(), t1.getCapacity(), stops1);
            }

            if (trainService.findByCode("T2").isEmpty()) {
                Train t2 = new Train("T2", "Regional 2", 50);
                List<StopTime> stops2 = List.of(
                        new StopTime(t2, stc, 0, null, LocalTime.of(9, 50)),
                        new StopTime(t2, ste, 1, LocalTime.of(10, 20), LocalTime.of(10, 25)),
                        new StopTime(t2, stf, 2, LocalTime.of(11, 0), null)
                );
                trainService.createTrain(t2.getTrainCode(), t2.getName(), t2.getCapacity(), stops2);
            }

            if (trainService.findByCode("T3").isEmpty()) {
                Train t3 = new Train("T3", "Direct 3", 60);
                List<StopTime> stops3 = List.of(
                        new StopTime(t3, sta, 0, null, LocalTime.of(7, 45)),
                        new StopTime(t3, ste, 1, LocalTime.of(8, 30), LocalTime.of(8, 35)),
                        new StopTime(t3, stf, 2, LocalTime.of(9, 10), null)
                );
                trainService.createTrain(t3.getTrainCode(), t3.getName(), t3.getCapacity(), stops3);
            }
        };
    }
}
