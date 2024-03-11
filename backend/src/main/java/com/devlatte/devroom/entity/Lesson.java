package com.devlatte.devroom.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Getter
@Setter
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    private String lesson_title;
    private String lesson_code;
    private int lesson_year;
    private Semester lesson_semester;

    @ManyToOne
    @JoinColumn(name="professor_pk")
    private Member member;

    public enum Semester{
        SPRING, FALL;
    }

}


