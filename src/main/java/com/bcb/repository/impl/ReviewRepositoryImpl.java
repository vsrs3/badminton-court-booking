package com.bcb.repository.impl;

import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.time.LocalDateTime;

import com.bcb.dto.ReviewDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.model.Review;
import com.bcb.repository.ReviewRepository;
import com.bcb.utils.DBContext;

public class ReviewRepositoryImpl implements ReviewRepository {

	@Override
	public Review viewReview(Integer bookingId, Integer accountId) {

		String sql = "select * from Review r Where r.booking_id = ? And r.account_id = ?";

		Review review = new Review();
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, bookingId);
			ps.setInt(2, accountId);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				review = mapResultSetToReview(rs);
			}
			return review;

		} catch (Exception e) {
			throw new DataAccessException("Failed to view review", e);
		}
	}

	@Override
	public boolean editReview(Integer bookingId, Integer accountId, Integer rating, String comment) {

		String sql = "Update Review Set " + "rating = ?, " + "comment = ? "
				+ "Where booking_id = ? AND account_id = ? ";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, rating);
			ps.setString(2, comment);
			ps.setInt(3, bookingId);
			ps.setInt(4, accountId);

			int rs = ps.executeUpdate();
			return rs > 0;

		} catch (Exception e) {
			throw new DataAccessException("Failed to edit review", e);

		}
	}

	@Override
	public List<Review> listLocationReview(Integer facilityId) {

		String sql = "Select * from Review Where facility_id = ?";

		List<Review> listReview = new ArrayList<Review>();
		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, facilityId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				listReview.add(mapResultSetToReview(rs));
			}
			return listReview;

		} catch (Exception e) {
			throw new DataAccessException("Failed to view the list of reviewing location", e);
		}
	}

	@Override
	public boolean addReview(Integer bookingId, Integer facilityId, Integer accountId, Integer rating, String comment) {

		String sql = "Insert Into Review " + "(booking_id, facility_id, account_id, rating, comment, created_at) "
				+ "Values " + "(?, ?, ?, ?, ?, GETDATE())";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, bookingId);
			ps.setInt(2, facilityId);
			ps.setInt(3, accountId);
			ps.setInt(4, rating);
			ps.setString(5, comment);

			int rs = ps.executeUpdate();
			return rs > 0;

		} catch (Exception e) {
			throw new DataAccessException("Failed to create review", e);
		}
	}

	@Override
	public Set<Integer> getReviewedBookingIds(Integer accountId) {

		String sql = "SELECT booking_id FROM Review WHERE account_id = ?";

		Set<Integer> bookingIdList = new HashSet<Integer>();

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, accountId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				bookingIdList.add(rs.getInt("booking_id"));
			}
			return bookingIdList;

		} catch (Exception e) {
			throw new DataAccessException("Can not get the booking Id list in review table", e);
		}
	}

	@Override
	public Optional<Integer> getFacilityId(Integer accountId, Integer bookingId) {
		String sql = "Select r.facility_id From Booking r " + "Where account_id = ? And booking_id = ? ";

		try (Connection conn = DBContext.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, accountId);
			ps.setInt(2, bookingId);

			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return Optional.of(rs.getInt("facility_id"));
			}

		} catch (Exception e) {
			throw new DataAccessException("Failed to find facility ID", e);
		}
		return Optional.empty();
	}

	private Review mapResultSetToReview(ResultSet rs) throws SQLException {
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

	@Override
	public boolean deleteReview(Integer bookingId, Integer accountId) {
		String sql = "Delete From Review Where booking_id = ? And account_id = ?";
		
		try (Connection conn = DBContext.getConnection(); 
		PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, bookingId);
			ps.setInt(2, accountId);
			
			int rs = ps.executeUpdate();
			return rs > 0;
			
		} catch (Exception e) {
			throw new DataAccessException("Failed to delete review", e);
		}
	}

}
