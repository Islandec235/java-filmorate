package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> getUsers();

    User create(User user);

    User update(User user);

    User delete(User user);

    User getUserById(long id);

    User addFriend(long userId, long friendId);

    User deleteFriend(long userId, long friendId);
}
