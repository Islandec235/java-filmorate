package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UserService {
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend(long userId, long otherUserId) {
        return userStorage.addFriend(userId, otherUserId);
    }

    public User deleteFriend(long userId, long otherUserId) {
        return userStorage.deleteFriend(userId, otherUserId);
    }

    public Collection<User> getFriends(long id) {
        List<User> friends = new ArrayList<>();

        for (long friend : userStorage.getUserById(id).getFriends()) {
            friends.add(userStorage.getUserById(friend));
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
