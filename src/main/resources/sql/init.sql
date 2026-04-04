-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS senol_db;
\c senol_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    open_id VARCHAR(100) UNIQUE NOT NULL,
    union_id VARCHAR(100) UNIQUE,
    name VARCHAR(50),
    mountain_name VARCHAR(50),
    enrollment_year INTEGER,
    class_name VARCHAR(100),
    current_job VARCHAR(200),
    bio TEXT,
    avatar_url VARCHAR(500),
    phone_number VARCHAR(20),
    email VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_admin BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 系统默认用户
INSERT INTO users (id, open_id, name, mountain_name, bio, is_active)
VALUES (1, 'system', '系统用户', 'System', '系统默认用户，用于处理未登录用户的操作', true)
ON CONFLICT (id) DO NOTHING;

-- 创建会刊表
CREATE TABLE IF NOT EXISTS publications (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_size BIGINT,
    publish_year INTEGER,
    issue_number VARCHAR(50),
    cover_image_url VARCHAR(500),
    uploaded_by BIGINT REFERENCES users(id),
    download_count INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建历史事件表
CREATE TABLE IF NOT EXISTS history_events (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    event_date DATE NOT NULL,
    event_year INTEGER NOT NULL,
    event_month INTEGER,
    image_urls TEXT,
    event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('GENERAL', 'ACTIVITY', 'ACHIEVEMENT', 'MILESTONE', 'ANNIVERSARY')),
    created_by BIGINT REFERENCES users(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建新生队表
CREATE TABLE IF NOT EXISTS freshmen_teams (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    team_leader VARCHAR(100),
    member_count INTEGER,
    activities TEXT,
    achievements TEXT,
    image_urls TEXT,
    members TEXT,
    created_by BIGINT REFERENCES users(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建暑期队表
CREATE TABLE IF NOT EXISTS summer_teams (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    destination VARCHAR(200),
    team_leader VARCHAR(100),
    member_count INTEGER,
    duration_days INTEGER,
    activities TEXT,
    achievements TEXT,
    image_urls TEXT,
    members TEXT,
    created_by BIGINT REFERENCES users(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建秘书处表
CREATE TABLE IF NOT EXISTS secretariat (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    secretary_general VARCHAR(100),
    deputy_secretary VARCHAR(100),
    members TEXT,
    achievements TEXT,
    activities TEXT,
    description TEXT,
    image_urls TEXT,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建攀岩队表
CREATE TABLE IF NOT EXISTS climbing_team (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    team_leader VARCHAR(100),
    vice_leader VARCHAR(100),
    members TEXT,
    climbing_routes TEXT,
    achievements TEXT,
    training_activities TEXT,
    competitions TEXT,
    equipment TEXT,
    description TEXT,
    image_urls TEXT,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建部门表
CREATE TABLE IF NOT EXISTS department (
    id BIGSERIAL PRIMARY KEY,
    year INTEGER NOT NULL,
    department_name VARCHAR(100) NOT NULL,
    department_code VARCHAR(20),
    description TEXT,
    responsibilities TEXT,
    current_head VARCHAR(100),
    members TEXT,
    contact_info TEXT,
    achievements TEXT,
    image_urls TEXT,
    display_order INTEGER DEFAULT 0,
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建协会信息表
CREATE TABLE IF NOT EXISTS association_info (
    id BIGSERIAL PRIMARY KEY,
    info_type VARCHAR(20) NOT NULL CHECK (info_type IN ('FLAG', 'EMBLEM', 'SONG', 'FOUNDATION')),
    title VARCHAR(200) NOT NULL,
    content TEXT,
    image_url VARCHAR(500),
    file_url VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    updated_by BIGINT REFERENCES users(id),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
-- 社区帖子表
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    image_urls TEXT, -- JSON格式存储多张图片URL
    like_count INTEGER DEFAULT 0,
    comment_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 帖子评论表
CREATE TABLE post_comments (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    content TEXT NOT NULL,
    parent_comment_id BIGINT REFERENCES post_comments(id), -- 支持回复评论
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 帖子点赞表
CREATE TABLE post_likes (
    id BIGSERIAL PRIMARY KEY,
    post_id BIGINT NOT NULL REFERENCES posts(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(post_id, user_id) -- 防止重复点赞
);

-- 商品分类表
CREATE TABLE product_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 商品表
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES product_categories(id),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INTEGER DEFAULT 0,
    image_urls TEXT, -- JSON格式存储多张商品图片
    specifications TEXT, -- JSON格式存储规格信息(尺寸、颜色等)
    sales_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 订单表
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, PAID, SHIPPED, COMPLETED, CANCELLED
    payment_method VARCHAR(20) DEFAULT 'WECHAT_PAY',
    shipping_address TEXT,
    shipping_phone VARCHAR(20),
    shipping_name VARCHAR(100),
    remark TEXT,
    paid_at TIMESTAMP,
    shipped_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 订单商品详情表
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    product_name VARCHAR(200) NOT NULL,
    product_price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    specifications TEXT, -- 选择的规格信息
    subtotal DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_users_open_id ON users(open_id);
CREATE INDEX IF NOT EXISTS idx_users_enrollment_year ON users(enrollment_year);
CREATE INDEX IF NOT EXISTS idx_publications_year ON publications(publish_year);
CREATE INDEX IF NOT EXISTS idx_history_events_year ON history_events(event_year);
CREATE INDEX IF NOT EXISTS idx_history_events_type ON history_events(event_type);
CREATE INDEX IF NOT EXISTS idx_freshmen_teams_year ON freshmen_teams(year);
CREATE INDEX IF NOT EXISTS idx_summer_teams_year ON summer_teams(year);
CREATE INDEX IF NOT EXISTS idx_secretariat_year ON secretariat(year);
CREATE INDEX IF NOT EXISTS idx_climbing_team_year ON climbing_team(year);
CREATE INDEX IF NOT EXISTS idx_department_display_order ON department(display_order);
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at);
CREATE INDEX IF NOT EXISTS idx_post_comments_post_id ON post_comments(post_id);
CREATE INDEX IF NOT EXISTS idx_post_likes_post_id ON post_likes(post_id);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);


-- 插入商品分类初始数据
INSERT INTO product_categories (name, description, sort_order) VALUES
('会服', '协会文化衫、帽子等服装用品', 1),
('装备', '户外装备、登山用品', 2),
('纪念品', '协会徽章、贴纸等纪念品', 3);

-- 插入示例商品
INSERT INTO products (category_id, name, description, price, stock_quantity, image_urls, specifications, is_active, created_at, updated_at) VALUES
(1, 'SENOL文化衫', '协会经典款文化衫，100%纯棉材质', 89.00, 100, '[]', '{"sizes":["S","M","L","XL","XXL"],"colors":["白色","黑色","绿色"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'SENOL帽子', '协会logo刺绣帽子，可调节大小', 45.00, 50, '[]', '{"colors":["黑色","军绿色"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'SENOL徽章', '协会金属徽章，可别在背包上', 25.00, 200, '[]', '{}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


-- 插入协会基本信息
INSERT INTO association_info (info_type, title, content, display_order, is_active, created_at, updated_at) VALUES
('FLAG', '山诺会会旗', '山诺会的会旗象征着我们对自然的热爱和对探险精神的追求。', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FUND', '山诺基金会', '山诺基金会成立于2010年，致力于支持科学探险和野外生存教育事业的发展。基金会通过资助学术研究、组织培训活动、提供装备支持等方式，为广大科学探险爱好者提供帮助。多年来，基金会已资助了数百项科研项目和探险活动，培养了大批优秀的科学探险人才。', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('EMBLEM', '山诺会会徽', '会徽设计融合了山峰、指南针等元素，体现了科学探险的主题。', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('SONG', '山诺会会歌', '我们是新时代的学子，怀着满腔的热情
我们是绿色的守护者，有着对大山的承诺
背负着历史的嘱托，不畏那风雨险阻
雪山沙漠和高原，都留下我们的足迹
只希望黄河流碧水，赤地能变青山
世界充满绿色，地球不再有荒原
我们向希望放声歌唱，唱出心中的炽热
我们向万物尽情拥抱，将美好一切细心照料
保存着心中一团火，奔向那美好大自然
森林河畔和荒野，回荡着我们的欢歌笑语
只希望黄河流碧水，赤地能变青山
世界充满绿色，地球不再有荒原
朋友啊朋友，让我们携起手
开创未来，谱写绿色之歌', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('FOUNDATION', '山诺基金会', '"山诺会"是北京林业大学科学探险与野外生存协会的音译简称，成立于1994年4月27日，是丰富绿色专业学习、衔接课堂知识与科学考察活动、培训野外工作技能、锻造大学生热爱自然之情和致力环境保护之心的大学生社团。成立30年来，这个社团成为北京林业大学最具代表性的环保公益社团之一，以严谨细致的活动组织、锐意创新的活动形式、科学可持续的组织体系而赢得社会赞誉。近两万名大学生在这个社团经受锻炼、迅速成长，毕业后成为我国生态文明建设和环境保护事业的中坚。', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- =============================
-- Groups, Events, Signups, Feature Flags
-- =============================

-- 特性开关
CREATE TABLE IF NOT EXISTS feature_flags (
    id BIGSERIAL PRIMARY KEY,
    flag_key VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT true,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 小组表
CREATE TABLE IF NOT EXISTS groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    city VARCHAR(50),
    description TEXT,
    cover_image_url TEXT,
    join_policy VARCHAR(20) NOT NULL DEFAULT 'APPROVAL_REQUIRED' CHECK (join_policy IN ('OPEN','APPROVAL_REQUIRED','CLOSED')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','INACTIVE')),
    member_count INTEGER NOT NULL DEFAULT 0,
    max_members INTEGER,
    tags TEXT,
    primary_leader_user_id BIGINT REFERENCES users(id),
    created_by BIGINT REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 小组成员表
CREATE TABLE IF NOT EXISTS group_members (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' CHECK (role IN ('LEADER','MODERATOR','MEMBER')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','REJECTED','REMOVED')),
    applied_at TIMESTAMP,
    joined_at TIMESTAMP,
    rejected_at TIMESTAMP,
    remark TEXT,
    UNIQUE (group_id, user_id)
);

-- 小组活动表
CREATE TABLE IF NOT EXISTS group_events (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    location TEXT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    signup_start_time TIMESTAMP,
    signup_end_time TIMESTAMP,
    capacity INTEGER,
    waitlist_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    require_approval BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('DRAFT','PUBLISHED','CANCELED','ENDED')),
    organizer_user_id BIGINT REFERENCES users(id),
    cover_image_url TEXT,
    external_link TEXT,
    form_schema TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 活动报名表
CREATE TABLE IF NOT EXISTS event_signups (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES group_events(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED','WAITLISTED','CANCELED','REJECTED','CHECKED_IN')),
    signup_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancel_at TIMESTAMP,
    check_in_at TIMESTAMP,
    answers TEXT,
    note TEXT,
    source VARCHAR(50),
    UNIQUE (event_id, user_id)
);

-- 常用索引（Groups 模块）
CREATE INDEX IF NOT EXISTS idx_group_members_group_status ON group_members(group_id, status);
CREATE INDEX IF NOT EXISTS idx_group_events_group_status ON group_events(group_id, status);
CREATE INDEX IF NOT EXISTS idx_event_signups_event_status ON event_signups(event_id, status);
CREATE INDEX IF NOT EXISTS idx_event_signups_event_signupat ON event_signups(event_id, signup_at);

-- 初始化特性开关
INSERT INTO feature_flags (flag_key, enabled, description, updated_at) VALUES
('groups.enabled', true, '小组功能总开关', CURRENT_TIMESTAMP),
('events.enabled', true, '活动功能总开关', CURRENT_TIMESTAMP),
('signups.enabled', true, '报名功能总开关', CURRENT_TIMESTAMP)
ON CONFLICT (flag_key) DO NOTHING;
