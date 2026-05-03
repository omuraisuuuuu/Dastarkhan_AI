# Гайд по запуску Food Scanner

Да, все верно. Код приложения уже готов. Тебе нужно сделать 4 вещи:

1. Создать базу данных в Supabase
2. Получить API-ключи (Supabase + HuggingFace)
3. Вписать ключи в файл `local.properties`
4. Открыть проект в Android Studio и запустить

Ниже - пошаговая инструкция.

---

## Шаг 1. Создать проект в Supabase

1. Зайди на [https://supabase.com](https://supabase.com) и зарегистрируйся (или войди)
2. Нажми **New Project**
3. Выбери организацию, придумай имя проекта (например `food-scanner`) и пароль базы данных
4. Регион - выбери ближайший (например `Central EU (Frankfurt)`)
5. Нажми **Create new project** и подожди ~2 минуты пока создастся

---

## Шаг 2. Создать таблицы в базе данных

1. В левом меню Supabase нажми **SQL Editor**
2. Нажми **New query**
3. Скопируй и вставь весь SQL-код ниже:

```sql
CREATE TABLE profiles (
  id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
  weight REAL,
  height REAL,
  target_weight REAL,
  is_halal BOOLEAN DEFAULT FALSE,
  is_lactose_free BOOLEAN DEFAULT FALSE,
  is_vegan BOOLEAN DEFAULT FALSE,
  allergies TEXT[] DEFAULT '{}',
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE scan_history (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
  scan_type TEXT NOT NULL,
  product_name TEXT,
  calories REAL,
  protein REAL,
  fat REAL,
  carbs REAL,
  is_compatible BOOLEAN,
  raw_data JSONB,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE scan_history ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own profile"
  ON profiles FOR ALL USING (auth.uid() = id);
CREATE POLICY "Users can manage own history"
  ON scan_history FOR ALL USING (auth.uid() = user_id);
```

4. Нажми **Run** (или Ctrl+Enter)
5. Должно появиться сообщение `Success. No rows returned` - это нормально, таблицы созданы

---

## Шаг 3. Отключить подтверждение email (для разработки)

Без этого шага после регистрации нельзя будет войти - Supabase будет ждать подтверждения письма.

1. В левом меню нажми **Authentication**
2. Перейди в **Providers** (или **Settings > Auth**)
3. Найди провайдер **Email**
4. **Выключи** переключатель **Confirm email**
5. Нажми **Save**

---

## Шаг 4. Скопировать ключи Supabase

1. В левом меню нажми **Settings** (шестеренка внизу)
2. Перейди в **API**
3. Тебе нужны два значения:
   - **Project URL** - выглядит как `https://abcdefgh.supabase.co`
   - **anon public** ключ - длинная строка, начинается с `eyJhbGci...`
4. Скопируй оба значения, они понадобятся на шаге 6

---

## Шаг 5. Получить токен HuggingFace

1. Зайди на [https://huggingface.co](https://huggingface.co) и зарегистрируйся (или войди)
2. Перейди в **Settings** -> **Access Tokens**: [https://huggingface.co/settings/tokens](https://huggingface.co/settings/tokens)
3. Нажми **Create new token**
4. Имя - любое (например `food-scanner`)
5. Тип - **Read** (достаточно для Inference API)
6. Нажми **Create**
7. Скопируй токен (начинается с `hf_...`)

---

## Шаг 6. Вписать ключи в проект

Открой файл `local.properties` в корне проекта и замени значения:

```properties
SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...YOUR_KEY
HF_API_TOKEN=hf_YOUR_TOKEN
```

**Важно:** Не добавляй кавычки вокруг значений. Просто вставь строки как есть.

---

## Шаг 7. Открыть проект в Android Studio

1. Открой **Android Studio**
2. Нажми **File** -> **Open**
3. Выбери папку проекта
4. Нажми **OK**
5. Android Studio начнет синхронизировать Gradle - подожди пока завершится (может занять 2-5 минут при первом открытии)
6. Если появится предложение обновить Gradle или плагины - нажми **Update**

---

## Шаг 8. Запустить приложение

### На физическом телефоне (рекомендуется)

1. На телефоне включи **Режим разработчика**: Настройки -> О телефоне -> 7 раз нажми на "Номер сборки"
2. Включи **Отладку по USB**: Настройки -> Для разработчиков -> Отладка по USB
3. Подключи телефон к компьютеру по USB
4. Разреши отладку во всплывающем окне на телефоне
5. В Android Studio вверху выбери свое устройство
6. Нажми зеленую кнопку **Run** или `Shift+F10`

### На эмуляторе

1. В Android Studio: **Tools** -> **Device Manager**
2. Нажми **Create Device**
3. Выбери устройство (например Pixel 7)
4. Выбери образ системы **API 33+** (скачай если нужно)
5. Нажми **Finish**, потом запусти эмулятор
6. Нажми **Run**

**Примечание:** Камера на эмуляторе работает ограниченно. Для полноценного тестирования лучше физический телефон.
