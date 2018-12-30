package tool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {
	private String DRIVER = "com.mysql.jdbc.Driver";
	private String CONNECT_STR = "jdbc:mysql://" + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_NAME
			+ "?user=" + Config.DB_USER + "&password=" + Config.DB_PASS
			+ "&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&failOverReadOnly=false";
	private Connection conn = null;
	private static ResultPair rp;
	private static MySQL sInstance;

	public static MySQL getInstance() {
		if (sInstance == null)
			sInstance = new MySQL();
		return sInstance;
	}

	private MySQL() {
		try {
			Class.forName(DRIVER);
			conn = DriverManager.getConnection(CONNECT_STR);
			if (conn.isClosed()) {
				System.out.println("Faild connect to database.");
			}
		} catch (ClassNotFoundException e) {
			System.out.println("mysql driver not found.");
		} catch (SQLException e) {
			System.out.println("failed to open database.");
		}

	}

	public ResultPair query(String sql) throws SQLException {
		ResultPair rp = new ResultPair();
		rp.statement = conn.createStatement();
		rp.resultSet = rp.statement.executeQuery(sql);
		return rp;
	}

	public void execute(String sql) throws SQLException {
		Statement statement = null;
		try {
			statement = conn.createStatement();
			statement.execute(sql);
		} finally {
			if (statement != null && !statement.isClosed())
				statement.close();
		}
	}

	public int insert(String table, String fields[], String values[]) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = getInsertSql(table, fields, values);
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			rs.next();
			return rs.getInt(1);
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null && !ps.isClosed())
				ps.close();
		}
	}

	public int replace(String table, String fields[], String values[]) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			String sql = getReplaceSql(table, fields, values);
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			rs.next();
			return rs.getInt(1);
		} finally {
			if (rs != null)
				rs.close();
			if (ps != null && !ps.isClosed())
				ps.close();
		}
	}

	public void update(String table, String fields[], String values[], String where) throws SQLException {
		PreparedStatement ps = null;
		try {
			String sql = getUpdateSql(table, fields, values, where);
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.executeUpdate();
		} finally {
			if (ps != null && !ps.isClosed())
				ps.close();
		}
	}

	public void delete(String table, String where) throws SQLException {
		PreparedStatement ps = null;
		try {
			String sql = "DELETE FROM `" + table + "` WHERE " + where + ";";
			ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.executeUpdate();
		} finally {
			if (ps != null && !ps.isClosed())
				ps.close();
		}
	}

	private String getInsertSql(String table, String fields[], String values[]) {
		String sql = "INSERT INTO `" + table + "` (";
		for (int i = 0; i < fields.length; i++) {
			sql += "`" + fields[i] + "`,";
		}
		sql = sql.substring(0, sql.length() - 1);
		sql += ") VALUES (";
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals("NaN"))
				values[i] = "0.0";
			sql += "'" + values[i] + "',";
		}
		sql = sql.substring(0, sql.length() - 1);
		sql += ");";
		return sql;
	}

	private String getReplaceSql(String table, String fields[], String values[]) {
		String sql = "REPLACE INTO `" + table + "` (";
		for (int i = 0; i < fields.length; i++) {
			sql += "`" + fields[i] + "`,";
		}
		sql = sql.substring(0, sql.length() - 1);
		sql += ") VALUES (";
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals("NaN"))
				values[i] = "0.0";
			sql += "'" + values[i] + "',";
		}
		sql = sql.substring(0, sql.length() - 1);
		sql += ");";
		return sql;
	}

	private String getUpdateSql(String table, String fields[], String values[], String where) {
		String sql = "UPDATE `" + table + "` SET";
		for (int i = 0; i < fields.length; i++) {
			sql += " `" + fields[i] + "` = '" + values[i] + "',";
		}
		sql = sql.substring(0, sql.length() - 1);
		sql += "WHERE " + where;
		sql += ";";
		return sql;
	}

	public static ResultPair getResultPair() {
		return rp;
	}

	public class ResultPair {
		public ResultSet resultSet;
		public Statement statement;

		public void close() {
			try {
				if (resultSet != null && !resultSet.isClosed())
					resultSet.close();
				if (statement != null && !statement.isClosed())
					statement.close();
			} catch (SQLException e) {
				System.out.println("failed to close database.");
			}
		}
	}
}
