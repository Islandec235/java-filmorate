package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend(int user1Id, int user2Id) {
        User user1 = userStorage.getUserById(user1Id);
        User user2 = userStorage.getUserById(user2Id);
        Set<Long> user1Friends = user1.getFriends();
        Set<Long> user2Friends = user2.getFriends();
        user1Friends.add((long) user2Id);
        user2Friends.add((long) user1Id);
        return user1;
    }

    public User deleteFriend(int user1Id, int user2Id) {
        User user1 = userStorage.getUserById(user1Id);
        User user2 = userStorage.getUserById(user2Id);
        Set<Long> user1Friends = user1.getFriends();
        Set<Long> user2Friends = user2.getFriends();
        user1Friends.remove((long) user2Id);
        user2Friends.remove((long) user1Id);
        return user1;
    }

    public Collection<User> getFriends(int id) {
        List<User> friends = new ArrayList<>();

        for (long friend : userStorage.getUserById(id).getFriends()) {
            friends.add(userStorage.getUserById((int) friend));
        }

        return friends;
    }

    public Collection<User> getCommonFriends(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        List<User> commonFriends = new ArrayList<>();

        for (long otherUserId : user.getFriends()) {
            if (friend.getFriends().contains(otherUserId)) {
                commonFriends.add(userStorage.getUserById((int) otherUserId));
            }
        }

        return commonFriends;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User delete(User user) {
        return userStorage.delete(user);
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }
}
