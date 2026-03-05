package com.bcb.repository.impl;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import com.bcb.dto.ReviewDTO;
import com.bcb.model.Review;
import com.bcb.repository.ReviewRepository;
import com.bcb.utils.DBContext;

public class ReviewRepositoryImpl implements ReviewRepository{

	@Override
	public Optional<Review> viewReview(Integer bookingId, Integer accountId) {
		
		String sql = "select * from Review r Where r.booking_id = ? And r.account_id = ?";
		
		try (Connection conn = DBContext.getConnection(); 
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, bookingId);
			ps.setInt(2, accountId);
			
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				Review review = mapResultSetToReview(rs);
				return Optional.of(review);
			}
			
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
		
		return Optional.empty();
	}

	@Override
	public boolean editReview(Integer bookingId, Integer accountId, Integer rating, String comment) {
		
		String sql = "Update Review Set "
					+ "rating = ?, "
					+ "comment = ? "
					+ "Where booking_id = ? AND account_id = ? ";
		
		try (Connection conn = DBContext.getConnection();
			     PreparedStatement ps = conn.prepareStatement(sql)) {

			    ps.setInt(1, rating);
			    ps.setString(2, comment);
			    ps.setInt(3, bookingId);
			    ps.setInt(4, accountId);

			    int rs = ps.executeUpdate();
			    return rs > 0;

			} catch (Exception e) {
				System.out.print(e.getMessage());
				return false;
			    
			}
	}
	
	@Override
	public List<Review> listLocationReview(Integer facilityId) {
		
		String sql = "Select * from Review Where facility_id = ?";
		
		List<Review> listReview = new ArrayList<Review>();
		try(Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ps.setInt(1, facilityId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				listReview.add(mapResultSetToReview(rs));
			}
			return listReview;
			
		} catch (Exception e) {
			System.out.print(e.getMessage());
			return null;
		}
	}

	
	private Review mapResultSetToReview(ResultSet rs) throws SQLException{
		Review review = new Review();
		
		review.setBookingId(rs.getInt("booking_id"));
		review.setFacilityId(rs.getInt("facility_id"));
		review.setAccountId(rs.getInt("account_id"));
		review.setRating(rs.getInt("rating"));
		review.setComment(rs.getString("comment"));
		
		Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
        	review.setCreatedAt(createdAt.toLocalDateTime());
        }
		
		return review;
	}

}
