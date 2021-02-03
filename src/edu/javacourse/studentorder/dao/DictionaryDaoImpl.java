package edu.javacourse.studentorder.dao;

import edu.javacourse.studentorder.config.Config;
import edu.javacourse.studentorder.domain.PassportOffice;
import edu.javacourse.studentorder.domain.RegisterOffice;
import edu.javacourse.studentorder.domain.Street;
import edu.javacourse.studentorder.exception.DaoException;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class DictionaryDaoImpl implements DictionaryDao{

    private static final String GET_STREET = "SELECT  street_code, street_name FROM jc_street WHERE UPPER(street_name) LIKE UPPER(?)";
    private static final String GET_PASSPORT = "SELECT * FROM jc_passport_office WHERE p_office_area_id = ?";
    private static final String GET_REGISTER = "SELECT * FROM jc_register_office WHERE r_office_area_id = ?";
    private Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                Config.getProperties(Config.DB_URL),
                Config.getProperties(Config.DB_LOGIN),
                Config.getProperties(Config.DB_PASSWORD));
        return conn;
    }

    public List<Street> findStreets(String pattern) throws DaoException {
        List<Street> result = new LinkedList<>();

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(GET_STREET)) {
            statement.setString(1, "%" + pattern + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Street street = new Street(resultSet.getLong("street_code"), resultSet.getString("street_name"));
                result.add(street);
            }
        } catch (SQLException ex) {
            throw new DaoException(ex);
        }
        return result;
    }

    @Override
    public List<PassportOffice> findPassportOffices(String areaId) throws DaoException {
        List<PassportOffice> result = new LinkedList<>();

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(GET_PASSPORT)) {
            statement.setString(1, areaId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                PassportOffice street = new PassportOffice (
                        resultSet.getLong("p_office_id"),
                        resultSet.getString("p_office_area_id"),
                        resultSet.getString("p_office_name"));
                result.add(street);
            }
        } catch (SQLException ex) {
            throw new DaoException(ex);
        }
        return result;
    }

    @Override
    public List<RegisterOffice> findRegisterOffices(String areaId) throws DaoException {
        List<RegisterOffice> result = new LinkedList<>();

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(GET_REGISTER)) {
            statement.setString(1, areaId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                RegisterOffice street = new RegisterOffice (
                        resultSet.getLong("r_office_id"),
                        resultSet.getString("r_office_area_id"),
                        resultSet.getString("r_office_name"));
                result.add(street);
            }
        } catch (SQLException ex) {
            throw new DaoException(ex);
        }
        return result;
    }
}
