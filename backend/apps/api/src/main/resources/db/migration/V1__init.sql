CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS citext;

-- ПОЛЬЗОВАТЕЛИ
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email CITEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- ПРОФИЛИ (1:1 с users)
CREATE TABLE profiles (
    user_id UUID PRIMARY KEY,
    type TEXT NOT NULL, -- PERSON / COMPANY

    full_name TEXT,
    company_name TEXT,

    country TEXT,
    city TEXT,
    district TEXT,

    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,

    rating_avg DOUBLE PRECISION DEFAULT 0.0,
    reviews_count INTEGER DEFAULT 0,
    orders_done_count INTEGER DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now(),

    CONSTRAINT fk_profiles_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- СПРАВОЧНИК СПЕЦИАЛИЗАЦИЙ
CREATE TABLE specializations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    description TEXT
);


-- ПРОФИЛЬ ↔ СПЕЦИАЛИЗАЦИЯ
CREATE TABLE profile_specializations (
    profile_id UUID NOT NULL,
    specialization_id UUID NOT NULL,
    level INTEGER NOT NULL DEFAULT 1,
    verification_status TEXT NOT NULL DEFAULT 'PENDING',
    rating_avg DOUBLE PRECISION DEFAULT 0.0,
    reviews_count INTEGER DEFAULT 0,

    PRIMARY KEY (profile_id, specialization_id),
    FOREIGN KEY (profile_id) REFERENCES profiles(user_id) ON DELETE CASCADE,
    FOREIGN KEY (specialization_id) REFERENCES specializations(id) ON DELETE CASCADE
);

