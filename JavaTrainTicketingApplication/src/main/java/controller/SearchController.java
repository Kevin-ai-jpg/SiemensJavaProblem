package controller;

import service.JourneyDto;
import service.TrainService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final TrainService trainService;

    public SearchController(TrainService trainService) { this.trainService = trainService; }

    @GetMapping("/direct")
    public List<JourneyDto> direct(@RequestParam String from, @RequestParam String to) {
        return trainService.findDirect(from, to);
    }

    @GetMapping("/transfer")
    public List<JourneyDto> transfer(@RequestParam String from, @RequestParam String to,
                                     @RequestParam(defaultValue = "5") int minTransferMinutes) {
        return trainService.findSingleTransfer(from, to, minTransferMinutes);
    }
}
