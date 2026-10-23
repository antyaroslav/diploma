# Мобильный хоспис — UI-тестирование

Android-приложение и дипломный проект тестирования. Тесты используют Kotlin,
Espresso, Page Objects, JUnit4 и Allure Android.

## Документация

- [План проверки](Plan.md).
- Чек-лист: [Excel](Check.xlsx), [просмотр на GitHub](Check.md).
- Тест-кейсы: [Excel](Cases.xlsx), [просмотр на GitHub](Cases.md).
- [Отчёты о дефектах](Bugs.xlsx).
- [Результаты и сравнение времени](Result.md).

Статусы исходной интерактивной проверки UI и покрытие UI-тестами учитываются раздельно.

## Окружение

- JDK 17 и Android Studio.
- Python 3.10+ для `scripts/run_ui_tests.py`; Allure CLI для просмотра результатов.
- Android SDK Platform 36 и Platform Tools; Gradle Wrapper 8.13,
  Android Gradle Plugin 8.12.2 (версии заданы проектом).
- Выделенный эмулятор Android 16 / API 36, рекомендованы Pixel 6,
  x86_64, en-US, Europe/Moscow, корректная текущая дата.
- Интернет для загрузки зависимостей и доступа к учебному backend.

Открыть корень проекта в Android Studio, дождаться синхронизации и создать
эмулятор API 36 через Device Manager. Путь SDK задаётся Android Studio
в `local.properties` либо переменной `ANDROID_HOME`.

Команды сборки в PowerShell из корня репозитория:

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
```

На macOS/Linux использовать `./gradlew` с теми же задачами.

## Запуск и отчёт

Создать локальный файл `qa-local.properties` (он указан в `.gitignore`) с полями
`qaLogin` и `qaPassword`, заполнив их учебными данными из задания. Альтернатива —
переменные окружения `QA_LOGIN` и `QA_PASSWORD`.
Gradle передаёт значения runner через instrumentation arguments. Не добавлять
значения в команды, исходники, скриншоты и отчёт.

Запустить выделенный эмулятор, проверить его serial командой `adb devices`.
Единый запуск с извлечением и упаковкой результатов Allure:

```powershell
python scripts/run_ui_tests.py --serial emulator-5556
```

Непосредственный запуск instrumentation-тестов:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true
```

Runner: `io.qameta.allure.android.runners.AllureAndroidJUnitRunner`.
Флаг `leaveApksInstalledAfterRun` сохраняет APK и внутренний каталог Allure после
прогона. Основной Python runner задаёт этот флаг и извлекает результаты автоматически;
при прямом запуске Gradle выгрузка архива сама не выполняется.
Тесты разделены по функциям: `AuthTests`, `NewsTests`, `MissionTests`,
`NewsRegressionTests`. Все взаимодействия с элементами выполняются в Page Objects.
Связь с кейсами и частичное покрытие представлены в Cases.xlsx.

Восемь тестов проверяют вход, валидацию пустого входа, выход, создание новости,
неполный диапазон дат, восемь карточек цитат, раскрытие/сворачивание первой карточки
и дату создания собственной новости. Отдельного экрана «О приложении» в этой версии нет.
На общем backend изменяются только собственные новости с уникальным префиксом
`QA_DIPLOMA_`; очистка удаляет их после сценария.
Очистка работает с записями, доступными в списке панели. Если сервер сохранил новость,
но ответ потерян и запись не появилась в списке, автоматическое удаление не гарантируется.
После такого сбоя проверить собственные записи с префиксом `QA_DIPLOMA_` в панели.

`NewsRegressionTests.creationDateEqualsToday` сохраняет правильное ожидание даты:
при воспроизведении BUG-001 он падает. Это зарегистрированный дефект, а не повод
считать любое падение ожидаемым. Причину фактического сбоя проверять по Allure
и [Result.md](Result.md).

Сохранённый итоговый прогон: **7 passed, 1 failed (BUG-001)**. Из-за этого
Gradle и Python runner возвращают код `1`. Ошибки окружения и другие расхождения
требуют отдельного анализа; их нельзя автоматически относить к этому дефекту.

Скрипт использует `adb` из `ANDROID_HOME`, `ANDROID_SDK_ROOT` или `PATH`.
При единственном авторизованном устройстве API 36 параметр `--serial` можно опустить.
Результаты выгружаются и при падениях тестов: архив `allure-results.zip` в корне
содержит каталог `allure-results/`. Лог сборки/прогона находится в
`build/qa-run/gradle.log`, сводка — в `build/qa-run/summary.json`.
Ненулевой код возврата сохраняется при ошибках Gradle или извлечения отчёта.

Для просмотра распаковать архив и открыть каталог результатов через Allure CLI:

```powershell
allure serve allure-results
```
