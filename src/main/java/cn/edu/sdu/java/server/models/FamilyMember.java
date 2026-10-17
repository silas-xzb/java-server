package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
@Table(	name = "family_member",
        uniqueConstraints = {
        })
public class FamilyMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer memberId;

    @ManyToOne
    @JoinColumn(name="personId")
    private Student student;
    @Size(max=10)
    private String relation;
    @Size(max=30)
    private String name;
    @Size(max=10)
    private String gender;
    private Integer age;
    @Size(max=50)
    private String unit;

}
