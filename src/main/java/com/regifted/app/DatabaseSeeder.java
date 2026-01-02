package com.regifted.app;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.regifted.app.item.EState;
import com.regifted.app.item.Item;
import com.regifted.app.item.ItemRepository;
import com.regifted.app.keyword.Keyword;
import com.regifted.app.keyword.KeywordRepository;
import com.regifted.app.user.User;
import com.regifted.app.user.UserRepository;

@Configuration
public class DatabaseSeeder {

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Bean
  CommandLineRunner seedDatabase(
      UserRepository userRepository,
      KeywordRepository keywordRepository,
      ItemRepository itemRepository) {

    return args -> {

      // Prevent duplicating seeds
      if (itemRepository.count() > 0) {
        System.out.println("Database already contains data — skipping seeding.");
        return;
      }

      System.out.println("Seeding database...");

      // --- USERS ------------------------------------------------------
      User user1 = new User();
      user1.setName("alice");
      user1.setEmail("alice@regifted.com");
      user1.setPassword(this.passwordEncoder.encode("password"));
      userRepository.save(user1);

      User user2 = new User();
      user2.setName("bob");
      user2.setEmail("bob@regifted.com");
      user2.setPassword(this.passwordEncoder.encode("password"));
      userRepository.save(user2);

      User user3 = new User();
      user3.setName("charlie");
      user3.setEmail("charlie@regifted.com");
      user3.setPassword(this.passwordEncoder.encode("password"));
      userRepository.save(user3);

      // --- KEYWORDS ---------------------------------------------------
      Keyword k1 = new Keyword();
      k1.setName("cardiovascular");

      Keyword k2 = new Keyword();
      k2.setName("respiratory");

      Keyword k3 = new Keyword();
      k3.setName("digestive");

      Keyword k4 = new Keyword();
      k4.setName("pediatric");

      Keyword k5 = new Keyword();
      k5.setName("transplant");

      Keyword k6 = new Keyword();
      k6.setName("vital");

      Keyword k7 = new Keyword();
      k7.setName("sensory");

      Keyword k8 = new Keyword();
      k8.setName("endocrine");

      // --- ITEMS ------------------------------------------------------
      Item i1 = new Item();
      i1.setUser(user1);
      i1.setTitle("Healthy kidney");
      i1.setDescription("Type O blood compatible, excellent filtration rate");
      i1.setLatitude(48.8566f);
      i1.setLongitude(2.3522f);
      i1.setKeywords(Set.of(k5, k6));
      i1.setState(EState.USED);
      i1.setCreatedAt(Instant.now());
      i1.setUpdatedAt(Instant.now());

      Item i2 = new Item();
      i2.setUser(user2);
      i2.setTitle("Left lung");
      i2.setDescription("Non-smoker, excellent capacity");
      i2.setLatitude(45.7640f);
      i2.setLongitude(4.8357f);
      i2.setKeywords(Set.of(k2, k5));
      i2.setState(EState.EXPERIMENTED);
      i2.setCreatedAt(Instant.now());
      i2.setUpdatedAt(Instant.now());

      Item i3 = new Item();
      i3.setUser(user3);
      i3.setTitle("Cornea pair");
      i3.setDescription("Perfect condition, crystal clear");
      i3.setLatitude(43.6045f);
      i3.setLongitude(1.4442f);
      i3.setState(EState.NEVER_USED);
      i3.setKeywords(Set.of(k7));
      i3.setCreatedAt(Instant.now());
      i3.setUpdatedAt(Instant.now());

      Item i4 = new Item();
      i4.setUser(user1);
      i4.setTitle("Pancreas");
      i4.setDescription("Insulin-producing, healthy tissue");
      i4.setLatitude(50.6292f);
      i4.setLongitude(3.0573f);
      i4.setKeywords(Set.of(k8, k3));
      i4.setState(EState.USED);
      i4.setCreatedAt(Instant.now());
      i4.setUpdatedAt(Instant.now());

      Item i5 = new Item();
      i5.setUser(user2);
      i5.setTitle("Liver lobe");
      i5.setDescription("Right lobe, regenerative capacity intact");
      i5.setLatitude(47.2184f);
      i5.setLongitude(-1.5536f);
      i5.setKeywords(Set.of(k3, k5));
      i5.setState(EState.EXPERIMENTED);
      i5.setCreatedAt(Instant.now());
      i5.setUpdatedAt(Instant.now());

      Item i6 = new Item();
      i6.setUser(user3);
      i6.setTitle("Heart valve");
      i6.setDescription("Mitral valve, still functional, minor wear");
      i6.setLatitude(48.1173f);
      i6.setLongitude(-1.6778f);
      i6.setKeywords(Set.of(k1, k5));
      i6.setState(EState.USED);
      i6.setCreatedAt(Instant.now());
      i6.setUpdatedAt(Instant.now());

      Item i7 = new Item();
      i7.setUser(user1);
      i7.setTitle("Small intestine section");
      i7.setDescription("Healthy tissue, good absorption capacity");
      i7.setLatitude(43.2965f);
      i7.setLongitude(5.3698f);
      i7.setKeywords(Set.of(k3, k5));
      i7.setState(EState.EXPERIMENTED);
      i7.setCreatedAt(Instant.now());
      i7.setUpdatedAt(Instant.now());

      Item i8 = new Item();
      i8.setUser(user2);
      i8.setTitle("Skin graft");
      i8.setDescription("Large area, 200x140 mm, minimal scarring");
      i8.setLatitude(49.2583f);
      i8.setLongitude(4.0317f);
      i8.setKeywords(Set.of(k5));
      i8.setState(EState.USED);
      i8.setCreatedAt(Instant.now());
      i8.setUpdatedAt(Instant.now());

      Item i9 = new Item();
      i9.setUser(user3);
      i9.setTitle("Spleen");
      i9.setDescription("Works perfectly, slight enlargement");
      i9.setLatitude(44.8378f);
      i9.setLongitude(-0.5792f);
      i9.setKeywords(Set.of(k3, k6));
      i9.setState(EState.EXPERIMENTED);
      i9.setCreatedAt(Instant.now());
      i9.setUpdatedAt(Instant.now());

      Item i10 = new Item();
      i10.setUser(user1);
      i10.setTitle("Bone marrow sample");
      i10.setDescription("Red marrow, healthy stem cells included");
      i10.setLatitude(48.5734f);
      i10.setLongitude(7.7521f);
      i10.setKeywords(Set.of(k5));
      i10.setState(EState.USED);
      i10.setCreatedAt(Instant.now());
      i10.setUpdatedAt(Instant.now());

      Item i11 = new Item();
      i11.setUser(user2);
      i11.setTitle("Thyroid gland");
      i11.setDescription("Size large, hormone production normal");
      i11.setLatitude(45.1885f);
      i11.setLongitude(5.7245f);
      i11.setKeywords(Set.of(k8));
      i11.setState(EState.USED);
      i11.setCreatedAt(Instant.now());
      i11.setUpdatedAt(Instant.now());

      Item i12 = new Item();
      i12.setUser(user3);
      i12.setTitle("Vertebral discs");
      i12.setDescription("L4-L5, very good condition");
      i12.setLatitude(48.3904f);
      i12.setLongitude(-4.4861f);
      i12.setKeywords(Set.of(k5));
      i12.setState(EState.EXPERIMENTED);
      i12.setCreatedAt(Instant.now());
      i12.setUpdatedAt(Instant.now());

      Item i13 = new Item();
      i13.setUser(user1);
      i13.setTitle("Baby teeth collection");
      i13.setDescription("Around 20 pieces");
      i13.setLatitude(43.7102f);
      i13.setLongitude(7.2620f);
      i13.setKeywords(Set.of(k4));
      i13.setState(EState.USED);
      i13.setCreatedAt(Instant.now());
      i13.setUpdatedAt(Instant.now());

      Item i14 = new Item();
      i14.setUser(user2);
      i14.setTitle("Gallbladder");
      i14.setDescription("Small, 3-chamber model");
      i14.setLatitude(49.4944f);
      i14.setLongitude(0.1079f);
      i14.setKeywords(Set.of(k3));
      i14.setState(EState.EXPERIMENTED);
      i14.setCreatedAt(Instant.now());
      i14.setUpdatedAt(Instant.now());

      Item i15 = new Item();
      i15.setUser(user3);
      i15.setTitle("Eardrum");
      i15.setDescription("Round membrane, soft tissue");
      i15.setLatitude(50.7239f);
      i15.setLongitude(1.6133f);
      i15.setKeywords(Set.of(k7));
      i15.setState(EState.USED);
      i15.setCreatedAt(Instant.now());
      i15.setUpdatedAt(Instant.now());

      Item i16 = new Item();
      i16.setUser(user1);
      i16.setTitle("Rib bone");
      i16.setDescription("White, 60 mm length");
      i16.setLatitude(46.2044f);
      i16.setLongitude(6.1432f);
      i16.setKeywords(Set.of(k5));
      i16.setState(EState.NEVER_USED);
      i16.setCreatedAt(Instant.now());
      i16.setUpdatedAt(Instant.now());

      Item i17 = new Item();
      i17.setUser(user2);
      i17.setTitle("Set of heart chambers");
      i17.setDescription("Atria + ventricles, good condition");
      i17.setLatitude(49.1193f);
      i17.setLongitude(6.1757f);
      i17.setKeywords(Set.of(k1, k6));
      i17.setState(EState.EXPERIMENTED);
      i17.setCreatedAt(Instant.now());
      i17.setUpdatedAt(Instant.now());

      Item i18 = new Item();
      i18.setUser(user3);
      i18.setTitle("Appendix");
      i18.setDescription("Beige tissue, works well");
      i18.setLatitude(47.3220f);
      i18.setLongitude(5.0415f);
      i18.setKeywords(Set.of(k3));
      i18.setState(EState.USED);
      i18.setCreatedAt(Instant.now());
      i18.setUpdatedAt(Instant.now());

      Item i19 = new Item();
      i19.setUser(user1);
      i19.setTitle("Retina");
      i19.setDescription("Vintage, still usable");
      i19.setLatitude(45.8992f);
      i19.setLongitude(6.1294f);
      i19.setKeywords(Set.of(k7));
      i19.setState(EState.EXPERIMENTED);
      i19.setCreatedAt(Instant.now());
      i19.setUpdatedAt(Instant.now());

      Item i20 = new Item();
      i20.setUser(user2);
      i20.setTitle("Small pediatric heart");
      i20.setDescription("Young donor, blue-tinged tissue");
      i20.setLatitude(44.9334f);
      i20.setLongitude(4.8924f);
      i20.setKeywords(Set.of(k1, k4, k6));
      i20.setState(EState.USED);
      i20.setCreatedAt(Instant.now());
      i20.setUpdatedAt(Instant.now());

      itemRepository.saveAll(List.of(
          i1, i2, i3, i4, i5, i6, i7, i8, i9, i10,
          i11, i12, i13, i14, i15, i16, i17, i18, i19, i20));

      System.out.println("Seeding completed.");
    };
  }
}
