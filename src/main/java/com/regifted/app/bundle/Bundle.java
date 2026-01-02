package com.regifted.app.bundle;


import com.regifted.app.item.Item;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.regifted.app.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor

@Entity @Table(name = "bundle")
public class Bundle {
    @Id
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_uuid", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_uuid", nullable = false)
    private User donor;
    @OneToMany(mappedBy = "bundle")
    @JsonManagedReference
    private Set<Item> items = new HashSet<>();

    @CreationTimestamp
    private Instant createdAt;

    @Column
    private Instant checkoutAt;
}