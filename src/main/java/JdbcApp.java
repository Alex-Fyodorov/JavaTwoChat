package lesson10;

import java.sql.*;
import java.util.Random;

public class JdbcApp {
    private static Connection connection;
    private static Statement statement;
    private static final Random random = new Random();

    public static void main(String[] args) {
        try {
            connect();
            createTable();
            long start = System.currentTimeMillis();
            insertStudents();
            System.out.println("insert stmt " +
                    (System.currentTimeMillis() - start) + "ms.");
            start = System.currentTimeMillis();
            insertStudentsBatch();
            System.out.println("insert batch " +
                    (System.currentTimeMillis() - start) + "ms.");

            insertOneStudent("' drop database; '", "35A");
            readData();
            dropTable();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            disconnect();
        }
    }

    private static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:java.db");
        statement = connection.createStatement();
    }

    private static void disconnect() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void createTable() throws SQLException {
        statement.executeUpdate("create table if not exists students (\n" +
                " id integer primary key autoincrement not null,\n" +
                " name text not null,\n" +
                " group_name text,\n" +
                " score integer\n" +
                ");");
    }

    public static void insertStudents() throws SQLException {
        for (int i = 0; i < 10; i++) {
            statement.executeUpdate(
                    "insert into students (name, group_name, score) " +
                            "values ('Bob" + i + "', '22', 3)");
        }
    }

    private static void insertStudentsBatch() {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into students (name, group_name, score) " +
                        "values (?, ?, ?)")) {
            for (int i = 0; i < 10; i++) {
                ps.setString(1, "Jack" + i);
                ps.setString(2, "group" + (10 - i));
                ps.setInt(3, random.nextInt(6));
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void insertOneStudent(String name, String group) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "insert into students (name, group_name, score) " +
                        "values (?, ?, 3) ")) {
            ps.setString(1, name);
            ps.setString(2, group);
            ps.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void readData() {
        try (ResultSet rs = statement.executeQuery("select * from students")) {
            while (rs.next()) {
                System.out.println(rs.getInt(1) + " " +
                        rs.getString("name") + " " +
                        rs.getString("group_name") + " " +
                        rs.getInt("score"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void dropTable() throws SQLException {
        statement.executeUpdate("drop table students");
    }
}
