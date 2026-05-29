-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Хост: 127.0.0.1
-- Время создания: Май 28 2026 г., 16:20
-- Версия сервера: 10.4.32-MariaDB
-- Версия PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- База данных: `clinic`
--

DROP DATABASE IF EXISTS `course_work`;
CREATE DATABASE `course_work` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `course_work`;

-- --------------------------------------------------------

--
-- Структура таблицы `appointments`
--

CREATE TABLE `appointments` (
  `id` int(10) UNSIGNED NOT NULL,
  `client_id` int(11) NOT NULL,
  `doctor_id` int(11) NOT NULL,
  `service_id` int(11) NOT NULL,
  `date_time` datetime NOT NULL,
  `status` enum('COMPLETED','CANCELLED','SCHEDULED') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `appointments`
--

INSERT INTO `appointments` (`id`, `client_id`, `doctor_id`, `service_id`, `date_time`, `status`) VALUES
(35, 9, 16, 15, '2026-05-29 16:00:00', 'COMPLETED'),
(36, 11, 16, 15, '2026-05-29 15:00:00', 'CANCELLED'),
(37, 11, 2, 16, '2026-05-28 16:00:00', 'SCHEDULED'),
(38, 11, 1, 27, '2026-05-29 12:00:00', 'CANCELLED'),
(39, 11, 16, 20, '2026-05-31 12:00:00', 'COMPLETED'),
(40, 11, 16, 16, '2026-06-05 15:00:00', 'COMPLETED'),
(41, 11, 2, 28, '2026-05-27 13:00:00', 'CANCELLED'),
(42, 11, 2, 19, '2026-05-27 13:00:00', 'SCHEDULED'),
(45, 11, 1, 21, '2026-05-29 11:00:00', 'SCHEDULED'),
(46, 11, 16, 26, '2026-05-29 15:00:00', 'CANCELLED'),
(47, 11, 16, 26, '2026-05-29 15:00:00', 'COMPLETED'),
(65, 11, 16, 25, '2026-05-29 15:00:00', 'SCHEDULED');

-- --------------------------------------------------------

--
-- Структура таблицы `breaks`
--

CREATE TABLE `breaks` (
  `id` int(10) UNSIGNED NOT NULL,
  `start_time` time DEFAULT NULL,
  `end_time` time DEFAULT NULL,
  `schedule_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `breaks`
--

INSERT INTO `breaks` (`id`, `start_time`, `end_time`, `schedule_id`) VALUES
(4, '14:00:00', '15:00:00', 4),
(5, '14:00:00', '15:00:00', 5),
(6, '14:00:00', '15:00:00', 6),
(7, '14:00:00', '15:00:00', 7),
(8, '12:00:00', '13:00:00', 8),
(9, '12:30:00', '13:30:00', 9),
(10, '12:30:00', '13:30:00', 10),
(11, '12:30:00', '13:30:00', 11),
(12, '11:00:00', '11:30:00', 12),
(94, '12:00:00', '13:00:00', 48),
(95, '14:00:00', '15:00:00', 48),
(106, '12:00:00', '13:00:00', 61),
(107, '15:00:00', '16:00:00', 61),
(108, '16:00:00', '17:00:00', 63);

-- --------------------------------------------------------

--
-- Структура таблицы `clients`
--

CREATE TABLE `clients` (
  `user_id` int(11) NOT NULL,
  `birth_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `clients`
--

INSERT INTO `clients` (`user_id`, `birth_date`) VALUES
(9, '2004-06-13'),
(11, '2005-06-15');

-- --------------------------------------------------------

--
-- Структура таблицы `doctors`
--

CREATE TABLE `doctors` (
  `user_id` int(11) NOT NULL,
  `bio` text DEFAULT NULL,
  `experience_years` int(11) NOT NULL,
  `image_path` varchar(255) NOT NULL,
  `license_path` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `doctors`
--

INSERT INTO `doctors` (`user_id`, `bio`, `experience_years`, `image_path`, `license_path`) VALUES
(1, 'Врач-дерматолог высшей категории. Окончил Первый МГМУ им. Сеченова по специальности \"Лечебное дело\", ординатура по специальности \"Дерматовенерология\". Регулярно повышает квалификацию в ведущих клиниках Европы и Израиля. Член Российского общества дерматологов и косметологов. Автор 15 научных публикаций. Основное направление работы — лечение акне, розацеа, гиперпигментации, возрастных изменений кожи. Использует современные методы лечения: фототерапию, лазерные технологии, химические пилинги. Индивидуальный подход к каждому пациенту, назначает только доказательные схемы лечения.', 13, 'https://i.ibb.co/XZJ0LKMr/1-jpg.jpg', 'https://i.ibb.co/CpKHjtrr/pdf.jpg'),
(2, 'Врач-косметолог, дерматовенеролог. Окончила РНИМУ им. Н.И. Пирогова по специальности \"Медицинская биохимия\", затем ординатуру по специальности \"Дерматовенерология\". Профессиональную переподготовку прошла в Институте пластической хирургии и косметологии. Эксперт по инъекционной косметологии и аппаратной эстетической медицине. Проводит более 500 инъекционных процедур ежегодно (ботулинотерапия, контурная пластика, мезотерапия, биоревитализация). Владеет методиками: SMAS-лифтинг, RF-лифтинг, лазерная эпиляция, фотоомоложение. Член European Society of Cosmetic Dermatology. Постоянный участник международных конгрессов по эстетической медицине.', 10, 'https://i.ibb.co/NnbRR5jQ/doctor3-jpg.jpg', 'https://i.ibb.co/HDrS3jr7/2-pdf.jpg'),
(16, 'Врач-дерматовеневролог высшей категории. Окончил Иркутский государственный медицинский университет по специальности «Лечебное дело», прошёл ординатуру по специальности «Дерматовенерология». Член Российского общества дерматологов и косметологов. Специализируется на лечении акне, розацеа, гиперпигментации, возрастных изменений кожи. Владеет современными методиками: фототерапия, лазерные технологии, химические пилинги. Назначает только доказательные схемы лечения.', 11, 'https://i.ibb.co/7tVDVvmj/doctor4-jpg.jpg', 'https://i.ibb.co/1GsrWsRn/2026-05-24-173803-png.png');

-- --------------------------------------------------------

--
-- Структура таблицы `doctorservice`
--

CREATE TABLE `doctorservice` (
  `doctor_id` int(11) NOT NULL,
  `service_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `doctorservice`
--

INSERT INTO `doctorservice` (`doctor_id`, `service_id`) VALUES
(1, 16),
(1, 21),
(1, 22),
(1, 24),
(1, 25),
(1, 27),
(1, 28),
(2, 15),
(2, 16),
(2, 17),
(2, 19),
(2, 24),
(2, 25),
(2, 26),
(2, 27),
(2, 31),
(2, 34),
(16, 15),
(16, 16),
(16, 20),
(16, 22),
(16, 25),
(16, 26);

-- --------------------------------------------------------

--
-- Структура таблицы `notifications`
--

CREATE TABLE `notifications` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `client_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `type` enum('INFO','SUCCESS','WARNING','APPOINTMENT_REMINDER','APPOINTMENT_CANCELLED','APPOINTMENT_COMPLETED') NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `notifications`
--

INSERT INTO `notifications` (`id`, `client_id`, `title`, `message`, `type`, `is_read`, `created_at`) VALUES
(1, 11, 'Запись отменена', 'Ваша запись на Увлажняющая маска с коллагеном к врачу Игнатьева Инна Дмитриевна на 2026-05-29 была отменена.', 'APPOINTMENT_CANCELLED', 1, '2026-05-26 19:12:13'),
(2, 11, 'Прием завершен', 'Прием по услуге Ультразвуковая чистка лица у врача Игнатьева Инна Дмитриевна успешно завершен.', 'APPOINTMENT_COMPLETED', 1, '2026-05-26 19:15:13'),
(3, 11, 'Прием завершен', 'Прием по услуге Филлеры (1 мл) у врача Игнатьева Инна Дмитриевна успешно завершен.', 'APPOINTMENT_COMPLETED', 1, '2026-05-27 15:01:23'),
(4, 11, 'Прием завершен', 'Прием по услуге Увлажняющая маска с коллагеном у врача Игнатьева Инна Дмитриевна успешно завершен.', 'APPOINTMENT_COMPLETED', 1, '2026-05-27 15:01:35'),
(12, 11, 'Запись отменена', 'Ваша запись на УЗИ мягких тканей лица к врачу Смирнова Елена Андреевна на 2026-05-27 была отменена.', 'APPOINTMENT_CANCELLED', 0, '2026-05-28 21:31:53');

-- --------------------------------------------------------

--
-- Структура таблицы `schedules`
--

CREATE TABLE `schedules` (
  `id` int(10) UNSIGNED NOT NULL,
  `day_of_week` int(11) NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `doctor_id` int(11) NOT NULL
) ;

--
-- Дамп данных таблицы `schedules`
--

INSERT INTO `schedules` (`id`, `day_of_week`, `start_time`, `end_time`, `doctor_id`) VALUES
(4, 1, '10:00:00', '19:00:00', 2),
(5, 2, '10:00:00', '19:00:00', 2),
(6, 3, '10:00:00', '19:00:00', 2),
(7, 4, '10:00:00', '19:00:00', 2),
(8, 6, '10:00:00', '15:00:00', 2),
(9, 2, '09:00:00', '17:00:00', 3),
(10, 3, '09:00:00', '17:00:00', 3),
(11, 4, '09:00:00', '17:00:00', 3),
(12, 6, '08:00:00', '14:00:00', 3),
(28, 2, '09:00:00', '18:00:00', 1),
(48, 1, '09:00:00', '18:00:00', 1),
(49, 5, '09:00:00', '15:00:00', 1),
(50, 7, '11:00:00', '15:00:00', 1),
(53, 7, '09:00:00', '14:00:00', 16),
(61, 1, '09:00:00', '18:00:00', 16),
(62, 3, '09:00:00', '18:00:00', 16),
(63, 5, '15:00:00', '18:00:00', 16);

-- --------------------------------------------------------

--
-- Структура таблицы `servicecategories`
--

CREATE TABLE `servicecategories` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `servicecategories`
--

INSERT INTO `servicecategories` (`id`, `name`) VALUES
(2, 'Аппаратная косметология'),
(5, 'Диагностика'),
(1, 'Инъекционная косметология'),
(8, 'Маникюр'),
(3, 'Массаж и SPA'),
(4, 'Уходовые процедуры');

-- --------------------------------------------------------

--
-- Структура таблицы `services`
--

CREATE TABLE `services` (
  `id` int(10) UNSIGNED NOT NULL,
  `name` varchar(255) NOT NULL,
  `price` int(11) NOT NULL,
  `duration` int(11) NOT NULL,
  `description` text DEFAULT NULL,
  `category_id` int(10) UNSIGNED DEFAULT NULL,
  `image_path` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `services`
--

INSERT INTO `services` (`id`, `name`, `price`, `duration`, `description`, `category_id`, `image_path`) VALUES
(15, 'Ботокс (1 зона)', 5000, 30, 'Инъекции ботулотоксина типа А для временного расслабления мимических мышц. Эффективно разглаживает межбровные морщины, \"гусиные лапки\" вокруг глаз и горизонтальные морщины на лбу. Результат сохраняется от 4 до 6 месяцев. Процедура занимает около 30 минут, эффект заметен через 7-14 дней.', 1, 'https://i.ibb.co/Ng8XyD7f/1-jpg.jpg'),
(16, 'Филлеры (1 мл)', 12000, 45, 'Инъекционные импланты на основе стабилизированной гиалуроновой кислоты для восстановления объема тканей, коррекции носослезной борозды, носогубных складок, увеличения губ и моделирования контуров лица. Результат виден сразу и сохраняется от 6 до 12 месяцев в зависимости от метаболизма и типа препарата.', 1, 'https://i.ibb.co/yct2Rktf/2-jpg.jpg'),
(17, 'Мезотерапия лица', 6000, 60, 'Метод введения микроинъекций с коктейлями из витаминов, минералов, аминокислот и гиалуроновой кислоты. Улучшает цвет лица, увлажняет кожу, стимулирует выработку коллагена и эластина. Курс рекомендуется из 4-6 процедур с интервалом 7-10 дней.', 1, 'https://i.ibb.co/RkXGhYpm/3-jpg.jpg'),
(19, 'RF-лифтинг лица', 7000, 60, 'Радиочастотный подтяжка кожи с использованием аппарата с частотой 448 кГц. Глубокий прогрев дермы стимулирует выработку коллагена и эластина, запускает процессы регенерации. Процедура эффективна для подтяжки овала лица, уменьшения второго подбородка, разглаживания морщин. Рекомендуемый курс 5-8 процедур.', 2, 'https://i.ibb.co/Ldp4zT8J/5-jpg.png'),
(20, 'Ультразвуковая чистка лица', 4000, 45, 'Глубокая очистка пор с помощью ультразвуковых волн. Бесконтактная методика удаляет комедоны, себум, ороговевшие клетки без травмирования кожи. Подходит для чувствительной и куперозной кожи. Процедура безболезненна, не вызывает покраснений. Рекомендуется проводить 1 раз в 3-4 недели.', 2, 'https://i.ibb.co/fYY51CnX/6-jpg.jpg'),
(21, 'Классический массаж спины', 3500, 60, 'Расслабляющий массаж мышц спины, шеи и плечевого пояса с использованием классических техник: поглаживание, растирание, разминание, вибрация. Снимает мышечное напряжение, улучшает кровообращение, снимает головные боли напряжения. Проводится на массажном столе с использованием массажного масла.', 3, 'https://i.ibb.co/7FBKnQb/7-jpg.jpg'),
(22, 'Лимфодренажный массаж лица', 3000, 45, 'Мягкая техника массажа, направленная на активизацию лимфотока. Уменьшает отечность лица и век, улучшает цвет лица, разглаживает мелкие морщины, моделирует овал лица. Выполняется легкими, поверхностными движениями по лимфатическим путям. Рекомендуется курсом 8-12 процедур.', 3, 'https://i.ibb.co/7c1byy7/8-jpg.jpg'),
(24, 'Атравматичная чистка лица', 5000, 60, 'Щадящая чистка лица без ручного выдавливания комедонов. Используются маски, кремы-сольвенты и ультразвук для размягчения и удаления сальных пробок. Подходит для тонкой, чувствительной и куперозной кожи. Не вызывает покраснений и шелушений, не требует реабилитации. Эффект \"гладкой и сияющей кожи\" сразу после процедуры.', 4, 'https://i.ibb.co/Xhmn35x/10-jpg.jpg'),
(25, 'Пилинг Джесснера', 4000, 15, 'Срединный химический пилинг на основе комбинации салициловой, молочной и резорциновой кислот. Эффективен при акне, постакне, гиперпигментации, фотостарении. Отшелушивает ороговевший слой, стимулирует обновление клеток. Требует реабилитации 5-7 дней (покраснение, шелушение). Курс 3-4 процедуры с интервалом 3-4 недели.', 4, 'https://i.ibb.co/qM7kGWY5/11-jpg.jpg'),
(26, 'Увлажняющая маска с коллагеном', 2500, 30, 'Интенсивное увлажнение и питание кожи с помощью альгинатной маски с гидролизованным коллагеном. Подходит для обезвоженной, тусклой и возрастной кожи. Восстанавливает гидролипидный баланс, повышает упругость, разглаживает текстуру кожи. Проводится после чистки или перед важным событием для экспресс-эффекта сияющей кожи.', 4, 'https://i.ibb.co/TDntcVJS/12-jpg.jpg'),
(27, 'Консультация косметолога', 1500, 30, 'Первичный прием врача-косметолога: сбор анамнеза, визуальный осмотр кожи, дерматоскопия, определение типа кожи и проблем. Врач составляет индивидуальный план лечения и ухода, дает рекомендации по домашней косметике. При необходимости назначает дополнительные обследования.', 5, 'https://i.ibb.co/MyJSYp1p/13-jpg.jpg'),
(31, 'Комбинированный маникюр', 2700, 30, 'Сочетает лучшие техники классического и аппаратного маникюра. Сначала кутикула обрабатывается аппаратными фрезами, а затем дорабатывается вручную. Позволяет добиться идеального результата даже на самых сложных ногтях.', 8, 'https://i.ibb.co/tMJCKnC5/M-height-jpg.jpg'),
(34, 'Антицеллюлитный массаж', 4500, 90, 'Интенсивный массаж проблемных зон (бедра, ягодицы, живот) с применением разминающих и выжимающих техник. Используется разогревающий крем и специальные банки для усиления эффекта. Расщепляет жировые отложения, разбивает фиброзные спайки, улучшает микроциркуляцию и лимфоток. Курс 10-15 процедур через день. Возможны умеренные болевые ощущения в первых процедурах.', 3, 'https://i.ibb.co/0RCzVZdm/9-jpg.jpg');

-- --------------------------------------------------------

--
-- Структура таблицы `users`
--

CREATE TABLE `users` (
  `id` int(10) UNSIGNED NOT NULL,
  `phone` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('CLIENT','ADMIN','DOCTOR') NOT NULL,
  `name` varchar(255) NOT NULL,
  `gender` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;

--
-- Дамп данных таблицы `users`
--

INSERT INTO `users` (`id`, `phone`, `email`, `password`, `role`, `name`, `gender`) VALUES
(1, '+79161234563', 'ivan.dermatolog@clinic.ru', '$2a$12$jTOqp9ByQT5t.kW0qQc76OcewJWo.XtTXxX.X2GTRopNsKpuZQHwi', 'DOCTOR', 'Алексеев Иван Петрович', 0),
(2, '+79162345678', 'elena.kosmetolog@clinic.ru', '$2a$12$O9rPlvQ3mwA5LX4Q02WIu.x5IJ7.8WtIrTO86kSN4pIU6oGAbg8BS', 'DOCTOR', 'Смирнова Елена Андреевна', 1),
(9, '89051218817', 'test@bk.ru', '$2a$10$6wjQ5d2ZJ5Dhb1KPfvDxJuEPgoF0u/oVXroV4VDitmuBySOYOyHci', 'CLIENT', 'Васильков Данил Викторович', 0),
(11, '89041218917', 'test2@bk.ru', '$2a$10$0VKYFdilULFBx2UtqGQavexAuIpUPN2.hpU0DKeZ/Y7lg8Aef.nZG', 'CLIENT', 'Иванова Виктория Светлановна', 1),
(12, '+79990000000', 'admin@clinic.ru', '$2a$12$diUC79EMllICpuAu2dlLseALul..7bQw4l6c5J5uZCNb4ErmtC5ru', 'ADMIN', 'Администратор', 1),
(16, '89041318888', 'test3@bk.ru', '$2a$10$woxXKjhM2hFvet6LeZkMo.1zNx1XhcefkaYCSeN.E1.e1A9if33h.', 'DOCTOR', 'Игнатьева Инна Валерьевна', 1);

--
-- Индексы сохранённых таблиц
--

--
-- Индексы таблицы `appointments`
--
ALTER TABLE `appointments`
  ADD PRIMARY KEY (`id`);

--
-- Индексы таблицы `breaks`
--
ALTER TABLE `breaks`
  ADD PRIMARY KEY (`id`);

--
-- Индексы таблицы `clients`
--
ALTER TABLE `clients`
  ADD PRIMARY KEY (`user_id`);

--
-- Индексы таблицы `doctors`
--
ALTER TABLE `doctors`
  ADD PRIMARY KEY (`user_id`);

--
-- Индексы таблицы `doctorservice`
--
ALTER TABLE `doctorservice`
  ADD PRIMARY KEY (`doctor_id`,`service_id`);

--
-- Индексы таблицы `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_client_id` (`client_id`),
  ADD KEY `idx_is_read` (`is_read`),
  ADD KEY `idx_created_at` (`created_at`),
  ADD KEY `idx_type` (`type`);

--
-- Индексы таблицы `schedules`
--
ALTER TABLE `schedules`
  ADD PRIMARY KEY (`id`);

--
-- Индексы таблицы `servicecategories`
--
ALTER TABLE `servicecategories`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`);

--
-- Индексы таблицы `services`
--
ALTER TABLE `services`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_Services_category_id_ServiceCategories` (`category_id`);

--
-- Индексы таблицы `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `phone` (`phone`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT для сохранённых таблиц
--

--
-- AUTO_INCREMENT для таблицы `appointments`
--
ALTER TABLE `appointments`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=66;

--
-- AUTO_INCREMENT для таблицы `breaks`
--
ALTER TABLE `breaks`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=109;

--
-- AUTO_INCREMENT для таблицы `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT для таблицы `schedules`
--
ALTER TABLE `schedules`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT для таблицы `servicecategories`
--
ALTER TABLE `servicecategories`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT для таблицы `services`
--
ALTER TABLE `services`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT для таблицы `users`
--
ALTER TABLE `users`
  MODIFY `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=37;

--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `doctorservice`
--
ALTER TABLE `doctorservice`
  ADD CONSTRAINT `doctorservice_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctors` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Ограничения внешнего ключа таблицы `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`client_id`) REFERENCES `clients` (`user_id`) ON DELETE CASCADE;

--
-- Ограничения внешнего ключа таблицы `services`
--
ALTER TABLE `services`
  ADD CONSTRAINT `fk_Services_category_id_ServiceCategories` FOREIGN KEY (`category_id`) REFERENCES `servicecategories` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
