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

   @Query(value = """
        SELECT EXISTS (
            SELECT 1 FROM friendships 
            WHERE (requester_id = :user1Id AND addressee_id = :user2Id) 
            OR (requester_id = :user2Id AND addressee_id = :user1Id)
        )
    """, nativeQuery = true)
    boolean existsFriendshipBetweenUsers(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);
}
