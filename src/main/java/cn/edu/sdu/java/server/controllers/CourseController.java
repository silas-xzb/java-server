package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/course")

public class CourseController {
    private final CourseService courseService;
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }
    @PostMapping("/getCourseList")
    public DataResponse getCourseList(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.getCourseList(dataRequest);
    }

    @PostMapping("/courseSave")
    public DataResponse courseSave(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.courseSave(dataRequest);
    }
    @PostMapping("/courseDelete")
    public DataResponse courseDelete(@Valid @RequestBody DataRequest dataRequest) {
        return courseService.courseDelete(dataRequest);
    }
}
