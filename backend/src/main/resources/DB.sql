CREATE TABLE member (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    member_id VARCHAR(255) UNIQUE,
    member_pw VARCHAR(255),
    member_info VARCHAR(255), -- 학생은 학번, 교수는 이름
    member_role ENUM('Student', 'Professor') NOT NULL -- Student 또는 Professor 만 허용
);

CREATE TABLE lesson (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    lesson_title VARCHAR(255),
    lesson_code VARCHAR(255),
    lesson_year INT,
    lesson_semester ENUM('Spring', 'Fall') NOT NULL, -- Spring 또는 Fall 만 허용
    professor_pk INT,
    FOREIGN KEY (professor_pk) REFERENCES member(ID)
);

CREATE TABLE enlisted (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    lesson_id INT,
    student_id INT,
    FOREIGN KEY (lesson_id) REFERENCES lesson(ID),
    FOREIGN KEY (student_id) REFERENCES member(ID)
);