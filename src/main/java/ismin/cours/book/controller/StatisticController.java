package ismin.cours.book.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ismin.cours.book.dto.Score;
import ismin.cours.book.dto.Statistic;
import ismin.cours.book.service.StatisticService;

@RestController
@RequestMapping("/api/books")
public class StatisticController {

    private final StatisticService statisticService;

    public StatisticController(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    @GetMapping("/{isbn}/statistics")
    public Statistic getBookStatistics(@PathVariable long isbn) {
        return statisticService.computeStatistic(isbn);
    }

    @GetMapping("/ranking")
    public List<Score> getBookRanking() {
        return statisticService.computeRanking();
    }
}
