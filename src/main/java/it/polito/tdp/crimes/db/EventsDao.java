package it.polito.tdp.crimes.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.polito.tdp.crimes.model.Event;

public class EventsDao {
	
	public List<Event> listAllEvents(){
		String sql = "SELECT * FROM events" ;
		try {
			Connection conn = DBConnect.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			List<Event> list = new ArrayList<>() ;
			
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				try {
					list.add(new Event(res.getLong("incident_id"),
							res.getInt("offense_code"),
							res.getInt("offense_code_extension"), 
							res.getString("offense_type_id"), 
							res.getString("offense_category_id"),
							res.getTimestamp("reported_date").toLocalDateTime(),
							res.getString("incident_address"),
							res.getDouble("geo_lon"),
							res.getDouble("geo_lat"),
							res.getInt("district_id"),
							res.getInt("precinct_id"), 
							res.getString("neighborhood_id"),
							res.getInt("is_crime"),
							res.getInt("is_traffic")));
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(res.getInt("id"));
				}
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}

	public List<Integer> getAnni() {
		String sql = "SELECT DISTINCT YEAR(reported_date) AS anno FROM events";
		List<Integer> anni = new ArrayList<>();
		
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			
			while(res.next()) {
				anni.add(res.getInt("anno"));
			}
			conn.close();
			Collections.sort(anni);
			return anni;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
		
	}
	
	public List<Integer> getMesi() {
		String sql = "SELECT DISTINCT MONTH(reported_date) AS anno FROM events";
		List<Integer> anni = new ArrayList<>();
		
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			
			while(res.next()) {
				anni.add(res.getInt("anno"));
			}
			conn.close();
			Collections.sort(anni);
			return anni;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
		
	}
	
	public List<Integer> getGiorni() {
		String sql = "SELECT DISTINCT DAY(reported_date) AS anno FROM events";
		List<Integer> anni = new ArrayList<>();
		
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			
			while(res.next()) {
				anni.add(res.getInt("anno"));
			}
			conn.close();
			Collections.sort(anni);
			return anni;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
		
	}

	public List<Integer> getDistretti() {
		String sql = "SELECT DISTINCT district_id FROM events";
		List<Integer> distretti = new ArrayList<>();
		
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res = st.executeQuery();
			
			while(res.next()) {
				distretti.add(res.getInt("district_id"));
			}
			conn.close();
			return distretti;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
		
	}

	public Double getLatMedia(Integer anno, Integer v1) {
		String sql = "SELECT AVG(geo_lat) AS lat FROM events WHERE YEAR(reported_date) = ? AND district_id = ?";
		
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, anno);
			st.setInt(2, v1);
			ResultSet res = st.executeQuery();
			
			if(res.next()) {
				conn.close();
				return res.getDouble("lat");
			}
			else {
				conn.close();
				return null;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}

	public Double getLonMedia(Integer anno, Integer v1) {
		String sql = "SELECT AVG(geo_lon) AS lon FROM events WHERE YEAR(reported_date) = ? AND district_id = ?";
		
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, anno);
			st.setInt(2, v1);
			ResultSet res = st.executeQuery();
			
			if(res.next()) {
				conn.close();
				return res.getDouble("lon");
			}
			else {
				conn.close();
				return null;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}

	public Integer ditrettoMin(Integer anno) {
		String sql = "SELECT district_id " + 
				"FROM events\n" + 
				"WHERE YEAR(reported_date) = ? " + 
				"GROUP BY district_id " + 
				"ORDER BY COUNT(*) ASC " + 
				"LIMIT 1";
		
		Connection conn = DBConnect.getConnection();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, anno);
			
			ResultSet res = st.executeQuery();
			if(res.next()) {
				conn.close();
				return res.getInt("district_id");
			}
			else {
				conn.close();
				return null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}
	
	public List<Event> listAllEventsByDate(Integer anno, Integer mese, Integer giorno){
		String sql = "SELECT * FROM events WHERE YEAR(reported_date) = ? "
				+ "AND MONTH(reported_date) = ? AND DAY(reported_date) = ?" ;
		try {
			Connection conn = DBConnect.getConnection() ;
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, anno);
			st.setInt(2, mese);
			st.setInt(3, giorno);
			
			List<Event> list = new ArrayList<>() ;
			ResultSet res = st.executeQuery() ;
			
			while(res.next()) {
				try {
					list.add(new Event(res.getLong("incident_id"),
							res.getInt("offense_code"),
							res.getInt("offense_code_extension"), 
							res.getString("offense_type_id"), 
							res.getString("offense_category_id"),
							res.getTimestamp("reported_date").toLocalDateTime(),
							res.getString("incident_address"),
							res.getDouble("geo_lon"),
							res.getDouble("geo_lat"),
							res.getInt("district_id"),
							res.getInt("precinct_id"), 
							res.getString("neighborhood_id"),
							res.getInt("is_crime"),
							res.getInt("is_traffic")));
				} catch (Throwable t) {
					t.printStackTrace();
					System.out.println(res.getInt("id"));
				}
			}
			
			conn.close();
			return list ;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}

}
