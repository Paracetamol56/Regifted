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

@Entity
@Table(name = "bundle")
public class Bundle {

    @Id
    private String uuid = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_uuid", nullable = false)
    private User receiver;

    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL)
    private Set<Item> items = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private BundleStatus status;

    @CreationTimestamp
    private Instant createdAt;

    private Instant checkoutAt;
}
