package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.services.StudentLeaveService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/studentLeave")
public class StudentLeaveController {
    private final StudentLeaveService studentLeaveService;
    public StudentLeaveController(StudentLeaveService studentLeaveService) {
        this.studentLeaveService = studentLeaveService;
    }
    @PostMapping("/getTeacherItemOptionList")
    @PreAuthorize("hasRole('STUDENT')")
    public OptionItemList getTeacherItemOptionList(@Valid @RequestBody DataRequest dataRequest) {
        return studentLeaveService.getTeacherItemOptionList(dataRequest);
    }
    @PostMapping("/getStudentLeaveList")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')  or hasRole('TEACHER')")
    public DataResponse getStudentLeaveList(@Valid @RequestBody DataRequest dataRequest) {
        return studentLeaveService.getStudentLeaveList(dataRequest);
    }
    @PostMapping("/studentLeaveSave")
    @PreAuthorize("hasRole('STUDENT') ")
    public DataResponse studentLeaveSave(@Valid @RequestBody DataRequest dataRequest) {
        return studentLeaveService.studentLeaveSave(dataRequest);
    }
    @PostMapping("/studentLeaveCheck")
    @PreAuthorize("hasRole('ADMIN')  or hasRole('TEACHER')")
    public DataResponse studentLeaveCheck(@Valid @RequestBody DataRequest dataRequest) {
        return studentLeaveService.studentLeaveCheck(dataRequest);
    }

}
