package com.company;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Main {
    public static ArrayList<DBObject> list = new ArrayList<>();
    public static String html = "<html><head><title>Отчёт по узлам</title></head><body>";
    public static HashMap<Integer, Integer> types = new HashMap<>();

    public static void main(String[] args) throws IOException {
	    if (args.length < 3) {
            System.out.println("Недостаточно аргументов! Должно быть 3: адрес БД, логин, пароль");
            return;
        }
        connectToDatabase(args[0], args[1], args[2]);
	    writeToHtml();
        System.out.println("Успешно!");
    }

    public static void connectToDatabase(String url, String login, String pass) throws IOException {
        try {
            Class.forName("org.postgresql.Driver");
            url = "jdbc:postgresql://" + url;
            Connection con = DriverManager.getConnection(url, login, pass);
            readObjectsFromDb(con);
            con.close();
        } catch (SQLException e) {
            System.out.println("Ошибка SQL");
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка");
        }
    }


    public static void readObjectsFromDb(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM objects");
        while (rs.next()) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = null;
            try {
                jsonObj = (JSONObject) parser.parse(rs.getString(4));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            list.add(new DBObject(rs.getInt(1), rs.getString(2), rs.getInt(3), jsonObj, rs.getInt(5)));
        }
        rs.close();
        stmt.close();
    }

    public static void writeToHtml() throws IOException {
        Iterator<DBObject> iterator = list.iterator();
        int treeCount = 0;
        while (iterator.hasNext()) {
            DBObject dbObj = iterator.next();
            if (dbObj.getParentObjectId() == 0) {
                html += dbObj.getName() + "<br>";
                if (!types.containsKey(dbObj.getObjectType())) types.put(dbObj.getObjectType(), 1);
                else types.put(dbObj.getObjectType(), types.get(dbObj.getObjectType()) + 1);
                getChildren(dbObj.getId(), treeCount);
            }
        }
        html += "<br>Статистика по типам:<br><br>";
        Iterator<Map.Entry<Integer, Integer>> entryIterator = types.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = entryIterator.next();
            html += "object_type " + entry.getKey() + ": " + entry.getValue() + " шт." + "<br>";
        }
        html += "</body></html>";
        BufferedWriter writer = new BufferedWriter(new FileWriter("result.html"));
        writer.write(html);
        writer.close();
    }

    public static void getChildren(int parentId, int treeCount) {
        treeCount++;
        Iterator<DBObject> iterator = list.iterator();
        while (iterator.hasNext()) {
            DBObject dbObj = iterator.next();
            if (dbObj.getParentObjectId() == parentId) {
                for (int i = 0; i < treeCount; i++) html += "&nbsp;&nbsp;&nbsp;&nbsp;";
                html += dbObj.getName() + "<br>";
                if (!types.containsKey(dbObj.getObjectType())) types.put(dbObj.getObjectType(), 1);
                else types.put(dbObj.getObjectType(), types.get(dbObj.getObjectType()) + 1);
                getChildren(dbObj.getId(), treeCount);
            }
        }
    }
}
