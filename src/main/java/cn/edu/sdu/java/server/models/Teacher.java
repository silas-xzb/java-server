package cn.edu.sdu.java.server.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(	name = "teacher",
        uniqueConstraints = {
        })
public class Teacher {
    @Id
    private Integer personId;

    @OneToOne
    @JoinColumn(name="personId")
    private Person person;

    @Size(max = 20)
    private String title;

    @Size(max = 10)
    private String degree;

}
