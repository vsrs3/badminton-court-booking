package com.bcb.repository;

import com.bcb.model.Facility;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.bcb.model.Inventory;


/**
 * Repository interface for Facility data access.
 */
public interface FacilityRepository {

    List<Facility> findAll(int limit, int offset);

    int count();

    List<Facility> findByKeyword(String keyword, int limit, int offset);

    int countByKeyword(String keyword);

    List<Facility> findAllWithPagination(int offset, int limit);

    List<Facility> findForHome(int offset, int limit, String keyword, String province, String district,
                               Integer favoriteAccountId);

    Optional<Facility> findById(int facilityId);

    Optional<Facility> findById(Integer facilityId);

    int insert(Facility facility);

    int getTotalCount();

    int countForHome(String keyword, String province, String district, Integer favoriteAccountId);

    int update(Facility facility);

    String findThumbnailPath(Integer facilityId);

    int softDelete(int facilityId);

    Double getAverageRating(Integer facilityId);

    Map<Integer, String> findThumbnailPaths(List<Integer> facilityIds);

    Map<Integer, Double> findAverageRatings(List<Integer> facilityIds);

    Map<Integer, String> findPriceRanges(List<Integer> facilityIds);

    boolean addFavorite(int accountId, int facilityId);

    boolean removeFavorite(int accountId, int facilityId);

    boolean isFavorite(int accountId, int facilityId);

    List<Integer> getFavoriteFacilityIds(int accountId);
    List<Facility> findAllActive();


}
