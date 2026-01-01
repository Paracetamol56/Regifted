package com.regifted.app.search;

import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.regifted.app.user.User;
import com.regifted.app.item.Item;

@Repository
public interface SearchRepository extends JpaRepository<Search, String> {

    // Afficher la liste des recherches d'un utilisateur
    Page<Search> findByUser(User user, Pageable pageable);

    // Trouver les utilisateurs à notifier lors d'un nouvel item
   @Query("""
        SELECT DISTINCT s.user 
        FROM Search s 
        JOIN s.user u
        WHERE u.uuid != :#{#item.user.uuid}
        AND (
            LOWER(:#{#item.title}) LIKE LOWER(CONCAT('%', s.query, '%')) 
            OR LOWER(:#{#item.description}) LIKE LOWER(CONCAT('%', s.query, '%'))
            OR EXISTS (
                SELECT k FROM Item i 
                JOIN i.keywords k 
                WHERE i.uuid = :#{#item.uuid} 
                AND LOWER(k.name) = LOWER(s.query)
            )
        )
    """)
    Set<User> findUsersToNotify(@Param("item") Item item);
}