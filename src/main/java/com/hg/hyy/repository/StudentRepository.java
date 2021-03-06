package com.hg.hyy.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
// import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

import com.hg.hyy.entity.Student;

// @RepositoryRestResource(collectionResourceRel = "student", path = "student")
public interface StudentRepository extends PagingAndSortingRepository<Student, Long> {

    List<Student> findByStudentName(@Param("studentName") String studentName);

}
