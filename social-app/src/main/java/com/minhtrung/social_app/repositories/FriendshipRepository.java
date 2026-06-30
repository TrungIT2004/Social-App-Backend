package com.minhtrung.social_app.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minhtrung.social_app.models.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
    @Query(value = """
        WITH friends_of_userId AS (
            SELECT CASE WHEN requester_id = :userId THEN addressee_id ELSE requester_id END AS friend_id
            FROM friendships 
            WHERE ( requester_id = :userId OR addressee_id = :userId ) AND status = 'ACCEPTED'
        ), friend_requests_of_userId AS (
            SELECT requester_id as sender_id, created_at, friendship_id
            FROM friendships
            WHERE addressee_id = :userId AND status = 'PENDING'
        ), 
        mutual_friend_count AS (
            SELECT COUNT(*) AS mutual_friend_count, f1.sender_id as sender_id 
            FROM friendships f2
            JOIN friend_requests_of_userId f1 ON ( f2.requester_id = f1.sender_id OR f2.addressee_id = f1.sender_id) AND f2.status = 'ACCEPTED'
            JOIN friends_of_userId f3 ON f3.friend_id = (CASE WHEN f2.requester_id = f1.sender_id THEN f2.addressee_id ELSE f2.requester_id END)
            GROUP BY f1.sender_id
        )
        
        SELECT u.user_id, CONCAT(u.first_name, ' ', u.last_name) as fullname, u.profile_pic_url, mfc.mutual_friend_count, f.created_at, f.friendship_id
        FROM friend_requests_of_userId f
        LEFT JOIN mutual_friend_count mfc ON f.sender_id = mfc.sender_id 
        JOIN users u ON f.sender_id = u.user_id
    """, nativeQuery = true) 
    List<Object[]> retrieveFriendRequests(@Param("userId") UUID userId);

    @Query(value = """
        WITH friends_of_userId AS (
            SELECT CASE WHEN requester_id = :userId THEN addressee_id ELSE requester_id END AS friend_id 
            FROM friendships 
            WHERE ( requester_id = :userId OR addressee_id = :userId ) AND status = 'ACCEPTED'
        ), friend_requests_of_userId AS (
            SELECT addressee_id as receiver_id, created_at, friendship_id
            FROM friendships
            WHERE requester_id = :userId AND status = 'PENDING'
        ), 
        mutual_friend_count AS (
            SELECT COUNT(*) AS mutual_friend_count, f1.receiver_id as receiver_id 
            FROM friendships f2
            JOIN friend_requests_of_userId f1 ON ( f2.requester_id = f1.receiver_id OR f2.addressee_id = f1.receiver_id) AND f2.status = 'ACCEPTED'
            JOIN friends_of_userId f3 ON f3.friend_id = (CASE WHEN f2.requester_id = f1.receiver_id THEN f2.addressee_id ELSE f2.requester_id END)
            GROUP BY f1.receiver_id 
        )
        
        SELECT u.user_id, CONCAT(u.first_name, ' ', u.last_name) as fullname, u.profile_pic_url, mfc.mutual_friend_count, f.created_at, f.friendship_id
        FROM friend_requests_of_userId f
        LEFT JOIN mutual_friend_count mfc ON f.receiver_id = mfc.receiver_id 
        JOIN users u ON f.receiver_id = u.user_id
    """, nativeQuery = true) 
    List<Object[]> retrieveSentFriendRequests(@Param("userId") UUID userId);

    @Query(value = """
        WITH friends_of_userId AS (
            SELECT CASE WHEN requester_id = :userId THEN addressee_id ELSE requester_id END AS friend_id, friendship_id
            FROM friendships 
            WHERE ( requester_id = :userId OR addressee_id = :userId ) AND status = 'ACCEPTED'
        ),
        mutual_friend_count AS (
            SELECT COUNT(*) AS mutual_friend_count, f1.friend_id as friend_id 
            FROM friendships f2
            JOIN friends_of_userId f1 ON ( f2.requester_id = f1.friend_id OR f2.addressee_id = f1.friend_id) AND f2.status = 'ACCEPTED'
            JOIN friends_of_userId f3 ON f3.friend_id = (CASE WHEN f2.requester_id = f1.friend_id THEN f2.addressee_id ELSE f2.requester_id END)
            GROUP BY f1.friend_id
        )
        
        SELECT u.user_id, CONCAT(u.first_name, ' ', u.last_name) as fullname, u.profile_pic_url, mfc.mutual_friend_count, f.friendship_id
        FROM friends_of_userId f
        LEFT JOIN mutual_friend_count mfc ON f.friend_id = mfc.friend_id 
        JOIN users u ON f.friend_id = u.user_id
    """, nativeQuery = true) 
    List<Object[]> retrieveFriendsInfo(@Param("userId") UUID userId);

    @Query(value =  """
        WITH f1 AS (
            SELECT CASE WHEN f.requester_id = :userId THEN addressee_id ELSE requester_id END AS friend_id
            FROM friendships f 
            WHERE ( f.requester_id = :userId OR f.addressee_id = :userId ) AND f.status = 'ACCEPTED'
        ),      
        f2 AS (
            SELECT DISTINCT CASE when f.requester_id = f1.friend_id THEN addressee_id ELSE requester_id END AS ff_id 
            FROM friendships f
            JOIN f1 ON ( f1.friend_id = f.requester_id OR f1.friend_id = f.addressee_id ) 
            WHERE
            f.requester_id != :userId AND
            f.addressee_id != :userId AND
            ( CASE when f.requester_id = f1.friend_id THEN addressee_id ELSE requester_id END NOT IN ( SELECT friend_id FROM f1 ))
            AND f.status = 'ACCEPTED'
        ),
                        
        f3 AS (
            SELECT f2.ff_id, CASE WHEN f.requester_id = f2.ff_id THEN addressee_id ELSE requester_id END AS fff_id
            FROM friendships f
            JOIN f2 ON ( f2.ff_id = f.requester_id OR f2.ff_id = f.addressee_id ) 
            WHERE f.status = 'ACCEPTED'
        ),
                        
        f3_friend_count AS (
            SELECT ff_id, COUNT(*) as friend_count
            from f3
            GROUP BY ff_id
        ),
                        
        mutual_friend_count AS (
            SELECT f3.ff_id, COUNT(*) as mutual_friend_count
            from f1
            JOIN f3 ON (f1.friend_id = fff_id)
            GROUP BY f3.ff_id
        )

        SELECT u.user_id, CONCAT(u.first_name, ' ', u.last_name) as fullname, u.profile_pic_url,
        ( (1.0 * mfc.mutual_friend_count ) / ( ( SELECT COUNT(*) FROM f1) + f3fc.friend_count - mfc.mutual_friend_count)) AS jaccard_point,
                mfc.mutual_friend_count
        FROM f2
        JOIN f3_friend_count f3fc ON f2.ff_id = f3fc.ff_id
        JOIN mutual_friend_count mfc ON f2.ff_id = mfc.ff_id
        JOIN users u ON u.user_id = f2.ff_id 
        ORDER BY jaccard_point DESC
        LIMIT :limit 
        OFFSET :offset
    """, nativeQuery = true)
    List<Object[]> friendRecommendationsJaccard(@Param("userId") UUID userId, @Param("limit") int limit, @Param("offset") int offset);


    @Query(value = """
        WITH f1 AS (
            SELECT CASE WHEN requester_id = :userId THEN addressee_id ELSE requester_id END AS friend_id
            FROM friendships
            WHERE ( requester_id = :userId OR addressee_id = :userId ) AND status = 'ACCEPTED'
        ),
                
        f2 AS (
            SELECT CASE WHEN f.requester_id = f1.friend_id THEN addressee_id ELSE requester_id END AS ff_id, f1.friend_id
            FROM friendships f
            JOIN f1 ON ( f.requester_id = f1.friend_id OR f.addressee_id = f1.friend_id ) 
            WHERE f.status = 'ACCEPTED'
        ),
                        
        user_degree AS (
            SELECT friend_id, COUNT(*) as friend_count
            FROM f2
            GROUP BY friend_id
        ), 
                        
        f3 AS (
            SELECT CASE WHEN f.requester_id = f1.friend_id THEN addressee_id ELSE requester_id END AS ff_id, f1.friend_id
            FROM friendships f
            JOIN f1 ON ( f.requester_id = f1.friend_id OR f.addressee_id = f1.friend_id ) 
            WHERE 
            f.requester_id != :userId AND
            f.addressee_id != :userId AND
            ( CASE when f.requester_id = f1.friend_id THEN addressee_id ELSE requester_id END NOT IN ( SELECT friend_id FROM f1 ))
            AND f.status = 'ACCEPTED'
        ),
                        
        mutual_friend_count AS (
            SELECT f3.ff_id, COUNT(*) as mutual_friend_count
            FROM f3
            JOIN f1 ON f3.friend_id = f1.friend_id
            GROUP BY f3.ff_id
        ),
                        
        caculate_AA_point AS (
            SELECT ff_id, SUM( 1.0 /ln(ud.friend_count)) AS AA_point
            FROM f3
            JOIN user_degree ud on f3.friend_id = ud.friend_id
            GROUP BY ff_id
        )
        
        SELECT u.user_id, CONCAT(u.first_name, ' ', u.last_name) AS fullname, u.profile_pic_url, caap.AA_point, mfc.mutual_friend_count
        FROM caculate_AA_point caap
        JOIN mutual_friend_count mfc on caap.ff_id = mfc.ff_id
        JOIN users u ON caap.ff_id = u.user_id
        ORDER BY AA_point DESC
        LIMIT :limit 
        OFFSET :offset
    """, nativeQuery = true)
    List<Object[]> friendRecommentdationsAdamicAdar(@Param("userId") UUID userId, @Param("limit") int limit, @Param("offset") int offset);

   @Query(value = """
        SELECT EXISTS (
            SELECT 1 FROM friendships 
            WHERE (requester_id = :user1Id AND addressee_id = :user2Id) 
            OR (requester_id = :user2Id AND addressee_id = :user1Id)
        )
    """, nativeQuery = true)
    boolean existsFriendshipBetweenUsers(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);
}
