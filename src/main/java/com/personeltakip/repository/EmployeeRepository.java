package com.personeltakip.repository;

import com.personeltakip.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmployeeNumberAndNationalId(String employeeNumber, String nationalId);
    Optional<Employee> findByEmployeeNumber(String employeeNumber);
}
