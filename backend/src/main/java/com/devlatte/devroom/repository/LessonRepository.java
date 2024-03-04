package com.devlatte.devroom.repository;

import com.devlatte.devroom.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

}
