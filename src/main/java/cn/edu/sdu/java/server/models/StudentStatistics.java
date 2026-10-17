package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
@Table(	name = "student_statistics",
        uniqueConstraints = {
        })
public class StudentStatistics implements Comparable<StudentStatistics> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer statisticsId;

    @ManyToOne
    @JoinColumn(name="personId")
    private  Student student;
    @Size(max=4)
    private String year;
    private Integer courseCount;
    private Double avgScore;
    private Double gpa;
    private Integer leaveCount;
    private Integer no;

    @Override
    public int compareTo(StudentStatistics o) {
        return o.gpa.compareTo(gpa);
    }
}
