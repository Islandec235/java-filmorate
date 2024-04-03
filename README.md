# java-filmorate
Template repository for Filmorate project.

Диаграмма БД: ![Screenshot](https://github.com/Islandec235/java-filmorate/assets/141726749/abcf3a7a-6554-44ef-90a4-01bd23fd1680)

> [!NOTE]
> Status в таблице friends показывает состояние дружбы (Подтверждена/Неподтверждена).

* Вывод примеры вывода данных:

1. Выввод всех фильмов/пользователей:
*  фильмы
   ```
   SELECT *
   FROM films;
   ```
* пользователи
  ```
  SELECT *
  FROM users;   
  ```
2. Вывод топ n фильмов по лайкам:
   ```
   SELECT title,
          likes
   FROM films
   ORDER BY likes DESC
   LIMIT n;
   ```
