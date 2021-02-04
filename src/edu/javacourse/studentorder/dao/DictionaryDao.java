package edu.javacourse.studentorder.dao;

import edu.javacourse.studentorder.domain.CountryArea;
import edu.javacourse.studentorder.domain.PassportOffice;
import edu.javacourse.studentorder.domain.RegisterOffice;
import edu.javacourse.studentorder.domain.Street;
import edu.javacourse.studentorder.exception.DaoException;

import java.util.List;

public interface DictionaryDao {
    public List<Street> findStreets(String pattern) throws DaoException;
    public List<PassportOffice> findPassportOffices(String areaId) throws DaoException;
    public List<RegisterOffice> findRegisterOffices(String areaId) throws DaoException;
    public List<CountryArea> findAreas(String areaId) throws DaoException;
}
