package com.minhtrung.social_app.seeder;

import com.github.javafaker.Faker;
import com.minhtrung.social_app.enums.FriendshipStatus;
import com.minhtrung.social_app.models.Friendship;
import com.minhtrung.social_app.models.User;
import com.minhtrung.social_app.repositories.FriendshipRepository;
import com.minhtrung.social_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Component
// @Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class FriendshipDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final Faker faker = new Faker(Locale.forLanguageTag("en"));

    @Override
    @Transactional
    public void run(String... args) {
        long userCount = userRepository.count();
        long friendshipCount = friendshipRepository.count();

        if (userCount >= 500 && friendshipCount >= 8000) {
            log.info("✅ Data already seeded. Skipping.");
            return;
        }

        log.info("🌱 Seeding 500 users and 8000 friendships (with cliques)...");

        // 1. Ensure 500 users
        List<User> existingUsers = userRepository.findAll();
        Set<String> existingEmails = existingUsers.stream()
                .map(User::getEmail)
                .collect(Collectors.toSet());

        int usersToCreate = (int) (500 - existingUsers.size());
        if (usersToCreate > 0) {
            List<User> newUsers = createUsers(usersToCreate, existingEmails);
            existingUsers.addAll(newUsers);
            log.info("✅ Created {} new users", newUsers.size());
        }

        // 2. Generate friendships
        long existingFriendships = friendshipRepository.count();
        int targetTotal = 8000;
        int toCreate = (int) (targetTotal - existingFriendships);
        if (toCreate > 0) {
            createFriendships(existingUsers, toCreate);
            log.info("✅ Created {} new friendships (cliques)", toCreate);
        }

        log.info("🎉 Seeding complete!");
    }

    private List<User> createUsers(int count, Set<String> existingEmails) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            String email = faker.internet().emailAddress();
            while (existingEmails.contains(email)) {
                email = faker.internet().emailAddress();
            }
            user.setEmail(email);
            existingEmails.add(email);
            user.setPassword(faker.internet().password(8, 16));
            user.setGender(faker.options().option("Male", "Female", "Other"));
            user.setBirthDate(faker.date().birthday(18, 60)
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            user.setProfilePicUrl(faker.internet().avatar());
            user.setCreatedDate(LocalDateTime.now());
            users.add(user);
        }
        return userRepository.saveAll(users);
    }

    private void createFriendships(List<User> users, int targetTotal) {
        Random random = new Random();
        List<Friendship> friendships = new ArrayList<>();
        Set<String> usedPairs = new HashSet<>();

        // Chia users thành các clique (nhóm) kích thước 15-25
        int cliqueSizeMin = 15;
        int cliqueSizeMax = 25;
        List<List<User>> cliques = new ArrayList<>();
        int currentIndex = 0;
        while (currentIndex < users.size()) {
            int size = cliqueSizeMin + random.nextInt(cliqueSizeMax - cliqueSizeMin + 1);
            if (currentIndex + size > users.size()) {
                size = users.size() - currentIndex;
            }
            cliques.add(users.subList(currentIndex, currentIndex + size));
            currentIndex += size;
        }

        log.info("Created {} cliques", cliques.size());

        // Bước 1: Trong mỗi clique, tạo friendships đầy đủ (ACCEPTED) – tạo ra nhiều bạn chung
        int cliqueFriendships = 0;
        for (List<User> clique : cliques) {
            for (int i = 0; i < clique.size(); i++) {
                for (int j = i + 1; j < clique.size(); j++) {
                    User u1 = clique.get(i);
                    User u2 = clique.get(j);
                    String key = u1.getUserId().toString().compareTo(u2.getUserId().toString()) < 0
                            ? u1.getUserId() + ":" + u2.getUserId()
                            : u2.getUserId() + ":" + u1.getUserId();
                    usedPairs.add(key);
                    Friendship f = new Friendship();
                    f.setRequester(random.nextBoolean() ? u1 : u2);
                    f.setAddressee(f.getRequester().equals(u1) ? u2 : u1);
                    f.setStatus(FriendshipStatus.ACCEPTED);
                    f.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
                    f.setUpdatedAt(f.getCreatedAt().plusMinutes(random.nextInt(60)));
                    friendships.add(f);
                    cliqueFriendships++;
                    if (friendships.size() >= targetTotal) break;
                }
                if (friendships.size() >= targetTotal) break;
            }
            if (friendships.size() >= targetTotal) break;
        }
        log.info("Generated {} friendships within cliques (ACCEPTED)", cliqueFriendships);

        // Bước 2: Thêm một số friendships ngẫu nhiên giữa các clique khác nhau (PENDING, REJECTED, UNFRIENDED)
        int remaining = targetTotal - friendships.size();
        if (remaining > 0) {
            log.info("Adding {} cross-clique friendships (PENDING/REJECTED/UNFRIENDED)...", remaining);
            List<FriendshipStatus> crossStatuses = Arrays.asList(
                    FriendshipStatus.PENDING,
                    FriendshipStatus.PENDING,
                    FriendshipStatus.REJECTED,
                    FriendshipStatus.UNFRIENDED
            );
            int attempts = 0;
            while (friendships.size() < targetTotal && attempts < 100000) {
                attempts++;
                int idx1 = random.nextInt(users.size());
                int idx2 = random.nextInt(users.size());
                if (idx1 == idx2) continue;
                User u1 = users.get(idx1);
                User u2 = users.get(idx2);
                String key = u1.getUserId().toString().compareTo(u2.getUserId().toString()) < 0
                        ? u1.getUserId() + ":" + u2.getUserId()
                        : u2.getUserId() + ":" + u1.getUserId();
                if (usedPairs.contains(key)) continue;
                usedPairs.add(key);
                Friendship f = new Friendship();
                f.setRequester(random.nextBoolean() ? u1 : u2);
                f.setAddressee(f.getRequester().equals(u1) ? u2 : u1);
                f.setStatus(crossStatuses.get(random.nextInt(crossStatuses.size())));
                f.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
                f.setUpdatedAt(f.getCreatedAt().plusMinutes(random.nextInt(60)));
                friendships.add(f);
            }
        }

        friendshipRepository.saveAll(friendships);
        log.info("Saved {} total friendships (ACCEPTED: {}, PENDING: {}, OTHER: {})",
                friendships.size(),
                friendships.stream().filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED).count(),
                friendships.stream().filter(f -> f.getStatus() == FriendshipStatus.PENDING).count(),
                friendships.size() - friendships.stream().filter(f -> f.getStatus() == FriendshipStatus.ACCEPTED || f.getStatus() == FriendshipStatus.PENDING).count());
    }
}