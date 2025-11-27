package com.employee.entity;

import java.sql.Date;
import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sce_skill_test_result", schema = "sce_employee")
public class SkillTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_test_result_id")
    private int skillTestResultId;

    @ManyToOne
    @JoinColumn(name = "skill_test_detl_id", nullable = false)
    private SkillTestDetails skillTestDetlId;

    @Column(name = "emp_name", nullable = false, length = 100)
    private String empName;

    @Column(name = "exam_date", nullable = false)
    private Date examDate;

    @Column(name = "no_of_question", nullable = false)
    private int noOfQuestion;

    @Column(name = "no_of_ques_attempt", nullable = false)
    private int noOfQuesAttempt;

    @Column(name = "no_of_ques_unattempt", nullable = false)
    private int noOfQuesUnattempt;

    @Column(name = "no_of_ques_correct", nullable = false)
    private int noOfQuesCorrect;

    @Column(name = "no_of_ques_wrong", nullable = false)
    private int noOfQuesWrong;

    @Column(name = "total_marks", nullable = false)
    private int totalMarks;

    @Column(name = "result", nullable = false, length = 30)
    private String result;

    @Column(name = "is_active", nullable = false)
    private int isActive = 1;


}