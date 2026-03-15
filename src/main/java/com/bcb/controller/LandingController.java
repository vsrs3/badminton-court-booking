package com.bcb.controller;

import com.bcb.dto.landing.LandingFacilityDTO;
import com.bcb.dto.landing.LandingReviewDTO;
import com.bcb.utils.DBContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for landing page
 * Loads a small set of recent reviews for display
 */
@WebServlet(name = "LandingController", urlPatterns = {"/landing", "/"})
public class LandingController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("contextPath", request.getContextPath());
        request.setAttribute("landingReviews", loadRecentReviews());
        request.setAttribute("landingFacilities", loadLandingFacilities());

        request.getRequestDispatcher("/jsp/landing/landing.jsp").forward(request, response);
    }

    private List<LandingReviewDTO> loadRecentReviews() {
        String sql = """
            SELECT TOP 6
                f.name AS facility_name,
                CONCAT(f.address, ', ', f.district, ', ', f.province) AS facility_address,
                a.full_name AS reviewer_name,
                r.rating,
                r.comment,
                r.created_at
            FROM Review r
            INNER JOIN Booking b ON b.booking_id = r.booking_id
            INNER JOIN Facility f ON f.facility_id = b.facility_id
            INNER JOIN Account a ON a.account_id = r.account_id
            ORDER BY r.created_at DESC
        """;

        List<LandingReviewDTO> reviews = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LandingReviewDTO dto = new LandingReviewDTO();
                    dto.setFacilityName(rs.getString("facility_name"));
                    dto.setFacilityAddress(rs.getString("facility_address"));
                    dto.setReviewerName(rs.getString("reviewer_name"));
                    dto.setRating(rs.getInt("rating"));
                    dto.setComment(rs.getString("comment"));
                    dto.setCreatedAt(rs.getTimestamp("created_at"));
                    reviews.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading landing reviews: " + e.getMessage());
        }

        return reviews;
    }

    private List<LandingFacilityDTO> loadLandingFacilities() {
        String sql = """
            SELECT TOP 8
                f.facility_id,
                f.name,
                fi.image_path
            FROM Facility f
            LEFT JOIN FacilityImage fi
                ON fi.facility_id = f.facility_id
               AND fi.is_thumbnail = 1
            ORDER BY f.facility_id DESC
        """;

        List<LandingFacilityDTO> facilities = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LandingFacilityDTO dto = new LandingFacilityDTO();
                dto.setName(rs.getString("name"));

                String imagePath = rs.getString("image_path");
                if (imagePath != null && !imagePath.isBlank()) {
                    dto.setImageUrl("uploads/" + imagePath);
                } else {
                    dto.setImageUrl("assets/images/facilities/default-facility.jpg");
                }

                facilities.add(dto);
            }
        } catch (SQLException e) {
            System.err.println("Error loading landing facilities: " + e.getMessage());
        }

        return facilities;
    }
}
