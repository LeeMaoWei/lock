package me.muphy.android.mqtt.demo.MySQL.dao;

import me.muphy.android.mqtt.demo.MySQL.enity.Lock;
import me.muphy.android.mqtt.demo.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class LockDao {


    public boolean login(int id, String username, int state) {

        String sql = "REPLACE INTO locklist(lockid,username,lcokstate) VALUES (?,?,?)";

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
        String sql = "UPDATE locklist SET lockstate = ? WHERE lockid = ? ";

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
    public Lock getinfo(String lockid) throws SQLException {

        Lock lock= new Lock();
        String sql="select * from locklist where lockid="+lockid ;

//        调用连接函数，传入数据库名的形参，获得conn对象，因为getConnection的返回类型就是Connection及conn
        try {
            Connection conn=JDBCUtils.getConn();
            Statement sta=conn.createStatement();
            ResultSet result=sta.executeQuery(sql);
            if (result==null){
                return null;
            }

            while (result.next()) {
                lock.setState(Integer.parseInt(result.getString("lockstate")));
                lock.setUsername(result.getString("username"));
//
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


//        由conn对象创建执行sql语句的对象（Statement类型),调用方法createStatement()


//        定义sql语句


//        调用Statement对象执行sql语句,返回结果result是ResultSet类型，就是结果集，具体百度


//        判断一下是否为空


//        条件是当结果集是否有下一行，这是一个相当于指针的东西，第一次调用时会把第一行设置为当前行，第二次回吧第二行设置为当前行，以此类推，直到没有下一行，循环结束
//            往map中填数据，map的数据类型相当于键值对
//            键是name，值是result.getString("empname"),意思是结果集指针所在行的字段名中的数据
//            lock.setId(Integer.parseInt(result.getString("lockid")));
//            lock.setLockname(result.getString("lockname"));

//        最后记得把list返回出去，不然拿不到这个list
        return lock;
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
