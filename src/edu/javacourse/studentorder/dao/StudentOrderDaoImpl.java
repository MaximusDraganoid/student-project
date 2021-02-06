package edu.javacourse.studentorder.dao;

import edu.javacourse.studentorder.config.Config;
import edu.javacourse.studentorder.domain.*;
import edu.javacourse.studentorder.exception.DaoException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class StudentOrderDaoImpl implements StudentOrderDao{

    private static final String INSERT_ORDER =
            "INSERT INTO jc_student_order(" +
                    " student_order_status, student_order_date, h_sur_name, " +
                    " h_given_name, h_patronymic, h_date_of_birth, h_passport_seria, " +
                    " h_passport_number, h_passport_date, h_passport_office_id, h_post_index, " +
                    " h_street_code, h_building, h_extension, h_apartment, h_university_id, h_student_number, " +
                    " w_sur_name, w_given_name, w_patronymic, w_date_of_birth, w_passport_seria, " +
                    " w_passport_number, w_passport_date, w_passport_office_id, w_post_index, " +
                    " w_street_code, w_building, w_extension, w_apartment, w_university_id, w_student_number,  " +
                    " certificate_id, register_office_id, marriage_date)" +
                    " VALUES (?, ?, ?, " +
                    " ?, ?, ?, ?, " +
                    " ?, ?, ?, ?, " +
                    " ?, ?, ?, ?, ?, ?, " +
                    " ?, ?, ?, ?, ?, " +
                    " ?, ?, ?, ?," +
                    " ?, ?, ?, ?, ?, ?," +
                    "?, ?, ?);";
    public static final String INSERT_CHILD =
            "INSERT INTO public.jc_student_child(" +
                    " student_order_id, c_sur_name, c_given_name," +
                    " c_patronymic, c_date_of_birth, c_certificate_number, c_certificate_date, " +
                    "c_register_office_id, c_post_index, c_street_code, c_building, c_extension, c_apartment)" +
                    "VALUES (?, ?, ?," +
                    " ?, ?, ?, ?, " +
                    "?, ?, ?, ?, " +
                    "?, ?);";
    public static final String SELECT_ORDERS = "SELECT * FROM jc_student_order " +
                                                    "WHERE student_order_status = 0 ORDER BY student_order_date";

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

            //начинаем транзакцию
            conn.setAutoCommit(false);//отключаем автоматическое управление транзакциями. До этого отключения, каждая операция
            //с бд выполнялась как самостоятельная отдельная транзакция

            //ест 2 основные операции работы с транзакцией. С момента отключения автокоммита, мы управляем
            //транзакциями исключительно вручную. Есть 2 операции - принятие изменений (conn.commit();) и откат до инструкции
            //conn.setAutoCommit(false);  - conn.rollback(); Для работы можно обернуть их в такой блок:
            try {
                //header
                statement.setInt(1, StudentOrderStatus.START.ordinal());
                statement.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
                //husband and wife
                setParamForAdult(statement, 3, so.getHusband());
                setParamForAdult(statement, 18, so.getWife());

                //order data
                statement.setString(33, so.getMarriageCertificateId());
                statement.setLong(34, so.getMarriageOffice().getOfficeId());
                statement.setDate(35, java.sql.Date.valueOf(so.getMarriageDate()));

                statement.executeUpdate();
                ResultSet gkRs = statement.getGeneratedKeys();
                if (gkRs.next()) {
                    result = gkRs.getLong(1);
                }
                gkRs.close();

                saveChildren(conn, so, result);

                conn.commit();//принятие изменений в бд
            } catch (SQLException ex) {
                conn.rollback(); //откатываем изменения бд в случае если что то пошло не так
                throw ex;//пробрасывае возникшее исключени на уровень выше, чтобы оно было обнаружено и обработано
            }

        } catch (SQLException ex) {
            throw new DaoException(ex);
        }

        return result;
    }

    @Override
    public List<StudentOrder> getStudentOrders() throws DaoException {
        List<StudentOrder> result = new LinkedList<>();

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(SELECT_ORDERS)) {
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                StudentOrder so = new StudentOrder();

                fillStudentOrder(rs, so);
                fillMarriage(rs, so);

                Adult husband = fillAdult(rs, "h_");
                Adult wife = fillAdult(rs, "w_");
                so.setHusband(husband);
                so.setWife(wife);

                result.add(so);

            }
            rs.close();
        } catch (SQLException ex) {
            throw new DaoException(ex);
        }

        return result;
    }

    private Adult fillAdult(ResultSet rs, String pref) throws SQLException {
        Adult adult = new Adult();

        adult.setSurName(rs.getString(pref + "sur_name"));
        adult.setGivenName(rs.getString(pref + "given_name"));
        adult.setPatronymic(rs.getString(pref + "patronymic"));
        adult.setDateOfBirth(rs.getDate(pref + "date_of_birth").toLocalDate());
        adult.setPassportSeria(rs.getString(pref + "passport_seria"));
        adult.setPassportNumber(rs.getString(pref + "passport_number"));
        adult.setIssueDate(rs.getDate(pref + "passport_date").toLocalDate());

        Long passportOfficeId = rs.getLong(pref + "passport_office_id");
        PassportOffice po = new PassportOffice(passportOfficeId, "", ""); // мы умышленно
        //осталвяем часть полей пустыми, т.к. на данный момент в них нет необходимости. Как только нам
        //понадобится вытащить более подробную информацию, мы сможем это сделать при помощи идентефикатора

        Address adr = new Address();
        adr.setPostCode(rs.getString(pref + "post_index"));
        adr.setBuilding(rs.getString(pref + "building"));
        adr.setExtension(rs.getString(pref + "extension"));
        adr.setApartment(rs.getString(pref + "apartment"));
        Street street = new Street(rs.getLong(pref + "street_code"), "");
        adr.setStreet(street);
        adult.setAddress(adr);

        University uni = new University(rs.getLong(pref + "university_id"), "");
        adult.setUnivesity(uni);
        adult.setStudentId(rs.getString(pref + "student_number"));

        return adult;
    }

    private void fillStudentOrder(ResultSet rs, StudentOrder so) throws SQLException{
        so.setStudentOrderId(rs.getLong("student_order_id"));
        so.setStudentOrderDate(rs.getTimestamp("student_order_date").toLocalDateTime());
        so.setStudentOrderStatus(StudentOrderStatus.fromValue(rs.getInt("student_order_status")));
    }

    private void fillMarriage(ResultSet rs, StudentOrder so) throws SQLException{
        so.setMarriageCertificateId(rs.getString("certificate_id"));
        so.setMarriageDate(rs.getDate("marriage_date").toLocalDate());
        Long id = rs.getLong("register_office_id");
        RegisterOffice ro = new RegisterOffice(id, "", "");
        so.setMarriageOffice(ro);
    }

    private void saveChildren(Connection conn, StudentOrder so, Long soId) throws SQLException{
        try(PreparedStatement statement = conn.prepareStatement(INSERT_CHILD)) {
            for (Child child : so.getChildren()) {
                statement.setLong(1, soId);
                setParamsForChild(child, statement);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void setParamForAdult(PreparedStatement statement, int start, Adult adult ) throws SQLException{
        setParamsForPerson(statement, start, adult);
        statement.setString(start + 4, adult.getPassportSeria());
        statement.setString(start + 5, adult.getPassportNumber());
        statement.setDate(start + 6, java.sql.Date.valueOf(adult.getIssueDate()));
        statement.setLong(start + 7, adult.getIssueDepartment().getOfficeId());
        setParamsForAddress(statement, start + 8, adult);
        statement.setLong(start + 13, adult.getUnivesity().getUniversityId());
        statement.setString(start + 14, adult.getStudentId());
    }

    private void setParamsForChild(Child child, PreparedStatement statement) throws SQLException {
        setParamsForPerson(statement, 2, child);
        statement.setString(6, child.getCertificateNumber());
        statement.setDate(7, java.sql.Date.valueOf(child.getIssueDate()));
        statement.setLong(8, child.getIssueDepartment().getOfficeId());
        setParamsForAddress(statement, 9, child );
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
