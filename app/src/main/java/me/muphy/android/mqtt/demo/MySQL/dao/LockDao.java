package me.muphy.android.mqtt.demo.MySQL.dao;

import me.muphy.android.mqtt.demo.utils.JDBCUtils;


import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.SQLException;


public class LockDao {


    public boolean login(int id, String username, int state) {

        String sql = "REPLACE INTO locklist(id,username,state) VALUES (?,?,?)";

        Connection con = JDBCUtils.getConn();

        try {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, String.valueOf(id));

            pst.setString(2, username);

            pst.setString(3, String.valueOf(state));
            System.out.println(pst);

            if (pst.executeQuery().next()) {
                return false;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            JDBCUtils.close(con);
        }

        return true;
    }

    public boolean update(int id,  int state){
        String sql = "UPDATE locklist SET state = ? WHERE id = ? ";

        Connection con = JDBCUtils.getConn();

        try {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, String.valueOf(state));


            pst.setString(2, String.valueOf(id));

            System.out.println(pst);
            pst.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            JDBCUtils.close(con);
        }
        return true;
    }
}
/***
    public boolean register(Lock lock){

        String sql = "INSERT INTO `user` (`username`, `password`, `power`, `salt`, `timestamp`) VALUES (?,?,?, '', CURRENT_TIMESTAMP)";

        Connection  con = JDBCUtils.getConn();

        try {
            PreparedStatement pst=con.prepareStatement(sql);

            pst.setString(1,user.getUsername());
            pst.setString(2,user.getPassword());
            pst.setInt(3,user.getPower());


            int value = pst.executeUpdate();

            if(value>0){
                return true;
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con);
        }
        return false;
    }

    public User findUser(String username){

        String sql = "select * from user where username = ?";

        Connection  con = JDBCUtils.getConn();
        User user = null;
        try {
            PreparedStatement pst=con.prepareStatement(sql);

            pst.setString(1,username);

            ResultSet rs = pst.executeQuery();

            while (rs.next()){

                int id = rs.getInt(0);
                String usernamedb = rs.getString(1);
                String passworddb = rs.getString(2);
                int power  = rs.getInt(3);
                String salt = rs.getString(4);
                Timestamp timestamp = rs.getTimestamp(5);
                user = new User(id,usernamedb,passworddb,power,salt,timestamp);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JDBCUtils.close(con);
        }

        return user;
    }

***/
