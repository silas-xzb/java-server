package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.StudentStatisticsService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/studentStatistics")

public class StudentStatisticsController {
    private final StudentStatisticsService studentStatisticsService;

    public StudentStatisticsController(StudentStatisticsService studentStatisticsService) {
        this.studentStatisticsService = studentStatisticsService;
    }

    @PostMapping("/getStudentStatisticsList")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse getStudentStatisticsList(@Valid @RequestBody DataRequest dataRequest) {
        return studentStatisticsService.getStudentStatisticsList(dataRequest);
    }
    @PostMapping("/doStudentStatistics")
    @PreAuthorize("hasRole('ADMIN') ")
    public DataResponse doStudentStatistics(@Valid @RequestBody DataRequest dataRequest) {
        return studentStatisticsService.doStudentStatistics(dataRequest);
    }

}
