package edu.javacourse.studentorder.dao;

import edu.javacourse.studentorder.config.Config;
import edu.javacourse.studentorder.domain.*;
import edu.javacourse.studentorder.exception.DaoException;

import java.sql.*;
import java.time.LocalDateTime;

public class StudentOrderDaoImpl implements StudentOrderDao{

    private static final String INSERT_ORDER =
            "INSERT INTO jc_student_order(" +
                    " student_order_status, student_order_date, h_sur_name, " +
                    " h_given_name, h_patronymic, h_date_of_birth, h_passport_seria, " +
                    " h_passport_number, h_passport_date, h_passport_office_id, h_post_index, " +
                    " h_street_code, h_building, h_extension, h_apartment,  " +
                    " w_sur_name, w_given_name, w_patronymic, w_date_of_birth, w_passport_seria, " +
                    " w_passport_number, w_passport_date, w_passport_office_id, w_post_index, " +
                    " w_street_code, w_building, w_extension, w_apartment,  " +
                    " certificate_id, register_office_id, marriage_date)" +
                    " VALUES (?, ?, ?, " +
                    " ?, ?, ?, ?, " +
                    " ?, ?, ?, ?, " +
                    " ?, ?, ?, ?, ?, ?, " +
                    " ?, ?, ?, ?, ?, " +
                    " ?, ?, ?, ?, " +
                    " ?, ?, ?, ?, ?);";
    public static final String INSERT_CHILD =
            "INSERT INTO public.jc_student_child(" +
                    " student_order_id, c_sur_name, c_given_name," +
                    " c_patronymic, c_date_of_birth, c_certificate_number, c_certificate_date, " +
                    "c_register_office_id, c_post_index, c_street_code, c_building, c_extension, c_apartment)" +
                    "VALUES (?, ?, ?," +
                    " ?, ?, ?, ?, " +
                    "?, ?, ?, ?, " +
                    "?, ?);";
    //TODO: refactoring - make one method
    private Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(
                Config.getProperties(Config.DB_URL),
                Config.getProperties(Config.DB_LOGIN),
                Config.getProperties(Config.DB_PASSWORD));
        return conn;
    }

    @Override
    public Long saveStudentOrder(StudentOrder so) throws DaoException {
        Long result = -1L;

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(INSERT_ORDER, new String[]{"student_order_id"})) {
            //header
            statement.setInt(1, StudentOrderStatus.START.ordinal());
            statement.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            //husband and wife
            setParamForAdult(statement, 3, so.getHusband());
            setParamForAdult(statement, 16, so.getWife());

            //order data
            statement.setString(29, so.getMarriageCertificateId());
            statement.setLong(30, so.getMarriageOffice().getOfficeId());
            statement.setDate(31, java.sql.Date.valueOf(so.getMarriageDate()));

            statement.executeUpdate();
            ResultSet gkRs = statement.getGeneratedKeys();
            if (gkRs.next()){
                result = gkRs.getLong(1);
            }

            saveChildren(conn, so, result);

        } catch (SQLException ex) {
            throw new DaoException(ex);
        }

        return result;
    }

    private void saveChildren(Connection conn, StudentOrder so, Long soId) throws SQLException{
        try(PreparedStatement statement = conn.prepareStatement(INSERT_CHILD)) {
            for (Child child : so.getChildren()) {
                statement.setLong(1, soId);
                setParamsForChild(child, statement);
                statement.executeUpdate();
            }
        }
    }

    private void setParamForAdult(PreparedStatement statement, int start, Adult adult ) throws SQLException{
        setParamsForPerson(statement, start, adult);
        statement.setString(start + 4, adult.getPassportSeria());
        statement.setString(start + 5, adult.getPassportNumber());
        statement.setDate(start + 6, java.sql.Date.valueOf(adult.getIssueDate()));
        statement.setLong(start + 7, adult.getIssueDepartment().getOfficeId());
        setParamsForAddress(statement, start + 8, adult);
    }

    private void setParamsForChild(Child child, PreparedStatement statement) throws SQLException {
        setParamsForPerson(statement, 2, child);
        statement.setString(6, child.getCertificateNumber());
        statement.setDate(7, java.sql.Date.valueOf(child.getIssueDate()));
        statement.setLong(8, child.getIssueDepartment().getOfficeId());
        setParamsForAddress(statement, 9, child);
    }

    private void setParamsForPerson(PreparedStatement statement, int start, Person person) throws SQLException {
        statement.setString(start, person.getSurName());
        statement.setString(start + 1, person.getGivenName());
        statement.setString(start + 2, person.getPatronymic());
        statement.setDate(start + 3, java.sql.Date.valueOf(person.getDateOfBirth()));
    }

    private void setParamsForAddress (PreparedStatement statement, int start, Person person ) throws SQLException {
        Address address = person.getAddress();
        statement.setString(start, address.getPostCode());
        statement.setLong(start + 1, address.getStreet().getStreetCode());
        statement.setString(start + 2, address.getBuilding());
        statement.setString(start + 3, address.getExtension());
        statement.setString(start + 4, address.getApartment());
    }

}
