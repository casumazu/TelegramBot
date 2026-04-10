# ☕ TelegramBot - Кофейня самообслуживания

Telegram-бот для кофейни самообслуживания с возможностью просмотра меню, оставления отзывов и просмотра отзывов других клиентов.

## 🚀 Возможности

- **Автоматическая регистрация** - пользователи автоматически добавляются в базу данных при первом взаимодействии
- **Просмотр меню** - актуальный список доступных напитков с ценами
- **Система отзывов** - клиенты могут оставлять отзывы о кофейне
- **Просмотр отзывов** - все отзывы доступны для просмотра другими клиентами
- **Приветствие** - персонализированное приветствие пользователя

## 🛠 Технологии

- **Java 17**
- **Spring Boot 3.1.1**
  - Spring Data JPA
  - Spring Validation
- **TelegramBots API 6.9.7.1**
- **PostgreSQL**
- **Lombok**
- **Maven**

## 📋 Предварительные требования

- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Telegram Bot Token (получить у [@BotFather](https://t.me/BotFather))

## ⚙️ Установка

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd TelegramBot
```

### 2. Настройка базы данных

Создайте базу данных в PostgreSQL:

```sql
CREATE DATABASE coffee_bot;
```

Выполните скрипт для создания таблиц (находится в `src/main/resources/schema.sql`):

```bash
psql -U postgres -d coffee_bot -f src/main/resources/schema.sql
```

### 3. Настройка переменных окружения

Создайте файл `.env` или установите переменные окружения:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/coffee_bot
DB_USERNAME=postgres
DB_PASSWORD=your_password

# Telegram Bot
BOT_NAME=YourBotName
BOT_TOKEN=your-telegram-bot-token
```

Или отредактируйте файл `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/coffee_bot
spring.datasource.username=postgres
spring.datasource.password=your_password
bot.name=YourBotName
bot.token=your-telegram-bot-token
```

### 4. Сборка проекта

```bash
mvn clean package
```

### 5. Запуск

```bash
java -jar target/SpringBot-0.0.1-SNAPSHOT.jar
```

Или через Maven:

```bash
mvn spring-boot:run
```

## 📖 Структура проекта

```
src/
├── main/
│   ├── java/com/example/SpringBot/
│   │   ├── config/              # Конфигурация бота
│   │   │   ├── BotConfig.java
│   │   │   └── BotInitializer.java
│   │   ├── model/               # Модели данных
│   │   │   ├── User.java
│   │   │   └── Comments.java
│   │   ├── repository/          # Репозитории
│   │   │   ├── UserRepository.java
│   │   │   └── CommentsRepository.java
│   │   ├── service/             # Сервисный слой
│   │   │   ├── TelegramBot.java
│   │   │   ├── UserService.java
│   │   │   └── CommentsService.java
│   │   └── SpringBotApplication.java
│   └── resources/
│       ├── application.properties
│       └── schema.sql
└── test/
```

## 🎮 Использование

После запуска бота в Telegram:

1. Отправьте `/start` для начала работы
2. Используйте кнопки меню для навигации:
   - **Напитки ☕** - просмотр меню
   - **Добавить отзыв 😊** - оставить отзыв
   - **Посмотреть отзывы** - просмотреть все отзывы

## 🏗 Архитектура

### Слои приложения

1. **Presentation Layer** (`TelegramBot`) - обработка сообщений от Telegram API
2. **Service Layer** (`UserService`, `CommentsService`) - бизнес-логика
3. **Repository Layer** (`UserRepository`, `CommentsRepository`) - работа с БД
4. **Model Layer** (`User`, `Comments`) - модели данных

### Ключевые улучшения

- **Валидация данных** - использование Bean Validation
- **Транзакционность** - корректное управление транзакциями
- **Логирование** - подробное логирование действий и ошибок
- **Безопасность** - использование параметризованных запросов через JPA
- **Производительность** - индексы в базе данных для оптимизации запросов
- **Конкурентность** - `ConcurrentHashMap` для потокобезопасного хранения состояний

## 🔧 Конфигурация

### Переменные окружения

| Переменная | Описание | По умолчанию |
|------------|----------|--------------|
| `DB_URL` | URL подключения к БД | `jdbc:postgresql://localhost:5432/coffee_bot` |
| `DB_USERNAME` | Имя пользователя БД | `postgres` |
| `DB_PASSWORD` | Пароль пользователя БД | `postgres` |
| `BOT_NAME` | Имя Telegram бота | `CoffeeBot` |
| `BOT_TOKEN` | Токен Telegram бота | - |

## 📝 Логирование

Логи настраиваются в `application.properties`. По умолчанию:
- Уровень логирования приложения: `DEBUG`
- Уровень логирования Telegram API: `INFO`

## 🧪 Тестирование

```bash
mvn test
```

## 🤝 Вклад в проект

Не стесняйтесь создавать Pull Request'ы и сообщать об ошибках.

## 📄 Лицензия

Этот проект распространяется под лицензией MIT.

## 📞 Поддержка

Если у вас возникли вопросы или проблемы, создайте Issue в репозитории.
