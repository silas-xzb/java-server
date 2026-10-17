package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.StudentStatistics;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.ScoreRepository;
import cn.edu.sdu.java.server.repositorys.StudentLeaveRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.repositorys.StudentStatisticsRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

@Service
public class StudentStatisticsService {
    private final StudentRepository studentRepository;
    private final ScoreRepository scoreRepository;
    private final StudentLeaveRepository studentLeaveRepository;
    private final StudentStatisticsRepository studentStatisticsRepository;
    public StudentStatisticsService(StudentRepository studentRepository, ScoreRepository scoreRepository, StudentLeaveRepository studentLeaveRepository, StudentStatisticsRepository studentStatisticsRepository) {
        this.studentRepository = studentRepository;
        this.scoreRepository = scoreRepository;
        this.studentLeaveRepository = studentLeaveRepository;
        this.studentStatisticsRepository = studentStatisticsRepository;
    }
    public DataResponse getStudentStatisticsList(DataRequest dataRequest) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        List<StudentStatistics> sList = studentStatisticsRepository.findAll();
        for (StudentStatistics ss : sList) {
            Map<String, Object> m = new HashMap<>();
            Person p = ss.getStudent().getPerson();
            m.put("studentNum", p.getNum());
            m.put("studentName", p.getName());
            m.put("courseCount", ss.getCourseCount()+"");
            m.put("avgScore", ss.getAvgScore());
            m.put("gpa", ss.getGpa());
            m.put("no", ss.getNo()+"");
            m.put("leaveCount", ss.getLeaveCount()+"");
            dataList.add(m);
        }
        return CommonMethod.getReturnData(dataList);
    };
    public DataResponse doStudentStatistics(DataRequest dataRequest) {
        String year = "2025";
        List<Student> sList = studentRepository.findAll();
        int i,j;
        Student s;
        Integer personId;
        Object[] as;
        Long l;
        int c;
        double creditMark;
        double creditSum;
        double sum;
        List<Integer> idList = new ArrayList<>();
        Map<Integer,StudentStatistics> sMap = new HashMap<>();
        StudentStatistics ss;
        for(i = 0; i < sList.size(); i++) {
            s= sList.get(i);
            ss = new StudentStatistics();
            ss.setStudent(s);
            ss.setYear(year);
            ss.setCourseCount(0);
            ss.setGpa(0d);
            ss.setAvgScore(0d);
            ss.setLeaveCount(0);
            personId = s.getPerson().getPersonId();
            sMap.put(personId, ss);
            idList.add(personId);
        }
        List<StudentStatistics> ssList = studentStatisticsRepository.findByYear(year, idList);
        if(ssList!= null && !ssList.isEmpty()) {
            for (i = 0; i < ssList.size();i++) {
                ss = ssList.get(i);
                personId = ss.getStudent().getPersonId();
                sMap.put(personId, ss);
            }
        }
        List<?> list = scoreRepository.getStudentStatisticsList(idList);
        if(list != null && !list.isEmpty()) {
            for(i = 0;i<list.size();i++) {
                as = (Object[]) list.get(i);
                personId = (Integer)as[0];
                ss = sMap.get(personId);
                if(ss == null)
                    continue;
                l = (Long)as[1];
                if(l != null)
                    c = l.intValue();
                else
                    c = 0;
                if(c == 0)
                    continue;
                sum = (Long)as[2];
                ss.setCourseCount(c);
                ss.setAvgScore(CommonMethod.getDouble2(sum/c));
                creditSum = (Long)as[3];
                creditMark = (Long)as[4];
                ss.setGpa(CommonMethod.getDouble2(creditMark / creditSum));
            }
        }
        list = studentLeaveRepository.getStudentStatisticsList(idList);
        if(list != null && !list.isEmpty()) {
            for(i = 0;i<list.size();i++) {
                as = (Object[]) list.get(i);
                personId = (Integer)as[0];
                ss = sMap.get(personId);
                if(ss == null)
                    continue;
                l = (Long)as[1];
                if(l != null)
                    c = l.intValue();
                else
                    c = 0;
                if(c == 0)
                    continue;
                ss.setLeaveCount(c);
            }
        }
        StudentStatistics[] ssArray = new StudentStatistics[sMap.size()];
        sMap.values().toArray(ssArray);
        Arrays.sort(ssArray);
        for(i= 0; i < ssArray.length; i++) {
            ss = ssArray[i];
            ss.setNo(i+1);
            studentStatisticsRepository.save(ss);
        }
        return CommonMethod.getReturnMessageOK();
    }
}
