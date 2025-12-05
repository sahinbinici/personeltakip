package com.personeltakip.automation.repository;

import com.personeltakip.automation.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findBySicilNo(Integer sicilNo);

    Optional<Person> findByTcKimlikNo(Long tcKimlikNo);

    Optional<Person> findBySicilNoAndTcKimlikNo(Integer sicilNo, Long tcKimlikNo);

    @Query(value = "SELECT p.peradi as adi, p.soyadi as soyadi, p.tckiml as tcKimlikNo, p.psicno as sicilNo, b.BRKODU as birimKodu, b.BRKDAC as birimAck, u.unvkod as unvanKodu, u.unvack as unvanAck, t.telefo as cepTel " +
                   "FROM person p " +
                   "JOIN unvkod u ON p.unvkod = u.unvkod " +
                   "JOIN brkodu b ON p.brkodu = b.BRKODU " +
                   "LEFT JOIN telefo t ON p.esicno = t.esicno " +
                   "WHERE t.teltur='GSM' AND p.tckiml = :tckiml AND p.psicno = :psicno", nativeQuery = true)
    Optional<Map<String, Object>> findDetailedPersonInfoByTckimlAndBrkodu(@Param("tckiml") Long tckiml, @Param("psicno") Integer psicno);

    @Query(value = "SELECT p.peradi as adi, p.soyadi as soyadi, p.tckiml as tcKimlikNo, p.psicno as sicilNo, b.BRKODU as birimKodu, b.BRKDAC as birimAck, u.unvkod as unvanKodu, u.unvack as unvanAck, t.telefo as cepTel " +
                   "FROM person p " +
                   "JOIN unvkod u ON p.unvkod = u.unvkod " +
                   "JOIN brkodu b ON p.brkodu = b.BRKODU " +
                   "LEFT JOIN telefo t ON p.esicno = t.esicno " +
                   "WHERE p.tckiml = :tckiml", nativeQuery = true)
    Optional<Map<String, Object>> findDetailedPersonInfoByTckiml(@Param("tckiml") Long tckiml);
}
