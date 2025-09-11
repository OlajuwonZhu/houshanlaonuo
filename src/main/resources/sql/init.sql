-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS senol_db;

-- 使用数据库
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
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 插入系统默认用户
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
    department_name VARCHAR(100) NOT NULL,
    department_code VARCHAR(20) NOT NULL UNIQUE,
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
CREATE INDEX IF NOT EXISTS idx_department_code ON department(department_code);
CREATE INDEX IF NOT EXISTS idx_department_display_order ON department(display_order);
CREATE INDEX IF NOT EXISTS idx_posts_user_id ON posts(user_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at);
CREATE INDEX IF NOT EXISTS idx_post_comments_post_id ON post_comments(post_id);
CREATE INDEX IF NOT EXISTS idx_post_likes_post_id ON post_likes(post_id);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- 插入默认系统用户
INSERT INTO users (open_id, name, mountain_name, avatar_url, enrollment_year, class_name, current_work, bio, created_at, updated_at, is_active) VALUES
('system', 'System', '系统', '', 1994, '系统', '系统管理', '系统默认用户，用于临时数据创建', NOW(), NOW(), true);

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

-- 插入一些示例数据
INSERT INTO users (open_id, name, mountain_name, enrollment_year, class_name, current_job, bio, avatar_url, is_active, created_at, updated_at) VALUES
('demo_openid_1', '张三', '青山', 2010, '计算机科学与技术1班', '软件工程师', '热爱户外运动，喜欢探险', '/images/default-avatar.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('demo_openid_2', '李四', '绿水', 2012, '环境工程2班', '环保工程师', '致力于环境保护事业', '/images/default-avatar.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('demo_openid_3', '王五', 'Q坤', 2013, '林学3班', '林业工程师', '2013后河新生队成员，热爱大自然', '/images/default-avatar.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('demo_openid_4', '赵六', '山风', 2014, '生态学1班', '生态保护专家', '专注生态保护研究', '/images/default-avatar.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('demo_openid_5', '孙七', '林涛', 2015, '环境科学2班', '环境监测师', '环境监测领域专家', '/images/default-avatar.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('demo_openid_xiaoxiang', '小象', '小象', 2016, '环境科学1班', '产品经理', '喜欢户外运动和摄影', '/images/default-avatar.png', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (open_id) DO NOTHING;

-- 插入会刊数据
INSERT INTO publications (title, description, file_path, file_name, file_size, publish_year, issue_number, uploaded_by, download_count, is_active, created_at, updated_at) VALUES
('第一期会刊', '山诺会第一期会刊，记录了协会成立初期的珍贵回忆', './uploads/publications/第一期.pdf', '第一期.pdf', 13935630, 1994, '第一期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第二期会刊', '山诺会第二期会刊，记录了1995年的精彩活动', './uploads/publications/第二期.pdf', '第二期.pdf', 24549126, 1995, '第二期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第三期会刊', '山诺会第三期会刊，记录了1996年的探险历程', './uploads/publications/第三期.pdf', '第三期.pdf', 63411895, 1996, '第三期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第四期会刊', '山诺会第四期会刊，记录了1997年的户外活动', './uploads/publications/第四期.pdf', '第四期.pdf', 63668541, 1997, '第四期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第五期会刊', '山诺会第五期会刊，记录了1998年的科学探险', './uploads/publications/第五期.pdf', '第五期.pdf', 67656035, 1998, '第五期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第六期会刊', '山诺会第六期会刊，记录了1999年的野外生存训练', './uploads/publications/第六期.pdf', '第六期.pdf', 83582409, 1999, '第六期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第七期会刊', '山诺会第七期会刊，记录了2000年的千禧年活动', './uploads/publications/第七期.pdf', '第七期.pdf', 119612662, 2000, '第七期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第八期会刊', '山诺会第八期会刊，记录了2001年的新世纪探险', './uploads/publications/第八期.pdf', '第八期.pdf', 121795526, 2001, '第八期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第九期会刊', '山诺会第九期会刊，记录了2002年的户外教育发展', './uploads/publications/第九期.pdf', '第九期.pdf', 79061212, 2002, '第九期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第十期会刊', '山诺会第十期会刊，记录了2003年的重要里程碑', './uploads/publications/第十期.pdf', '第十期.pdf', 93787134, 2003, '第十期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第十一期会刊', '山诺会第十一期会刊，记录了2004年的协会发展', './uploads/publications/第十一期.pdf', '第十一期.pdf', 53451915, 2004, '第十一期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第十二期会刊', '山诺会第十二期会刊，记录了2005年的基金会成立', './uploads/publications/第十二期.pdf', '第十二期.pdf', 111084677, 2005, '第十二期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第十三期会刊', '山诺会第十三期会刊，记录了2006年的重要活动', './uploads/publications/第十三期.pdf', '第十三期.pdf', 106077376, 2006, '第十三期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第十四期会刊', '山诺会第十四期会刊，记录了2007年的探险成果', './uploads/publications/第十四期.pdf', '第十四期.pdf', 85349621, 2007, '第十四期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('第十五期会刊', '山诺会第十五期会刊，记录了2008年的重要时刻', './uploads/publications/第十五期.pdf', '第十五期.pdf', 47326956, 2008, '第十五期', NULL, 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

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

-- 插入系统默认用户（确保ID=1存在）
INSERT INTO users (id, open_id, name, mountain_name, bio, is_active, created_at, updated_at) 
VALUES (1, 'demo_openid', '朱二坤', 'Q坤', '系统默认用户，用于处理未登录用户的操作', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO UPDATE SET 
  open_id = EXCLUDED.open_id,
  name = EXCLUDED.name,
  mountain_name = EXCLUDED.mountain_name,
  updated_at = CURRENT_TIMESTAMP;

-- 插入新生队示例数据
INSERT INTO freshmen_teams (year, title, description, team_leader, member_count, activities, achievements, image_urls, members, created_by, is_active, created_at, updated_at) VALUES
(2023, '2023年新生队', '2023年新生队，共有15名新成员加入协会', '张三', 15, '新生培训、户外体验、团队建设活动', '成功完成了新生培训计划，所有成员通过了基础技能考核', '[]', '[{"userId": 1, "name": "张三", "mountainName": "青山", "avatarUrl": "/images/default-avatar.png"}, {"userId": 2, "name": "李四", "mountainName": "绿水", "avatarUrl": "/images/default-avatar.png"}, {"userId": 3, "name": "王五", "mountainName": "Q坤", "avatarUrl": "/images/default-avatar.png"}]', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2022, '2022年新生队', '2022年新生队，疫情期间的特殊一届', '赵六', 12, '线上培训、小规模户外活动', '在疫情限制下仍然保持了良好的团队凝聚力', '[]', '[{"userId": 4, "name": "赵六", "mountainName": "山风", "avatarUrl": "/images/default-avatar.png"}, {"userId": 5, "name": "孙七", "mountainName": "林涛", "avatarUrl": "/images/default-avatar.png"}]', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2013, '2013后河新生队', '2013年后河新生队，经典的一届', 'Q坤', 18, '后河探险、野外生存训练、团队协作', '完成了后河地区的深度探险，发现了多个新的生态点', '[]', '[{"userId": 3, "name": "王五", "mountainName": "Q坤", "avatarUrl": "/images/default-avatar.png"}, {"userId": 1, "name": "张三", "mountainName": "青山", "avatarUrl": "/images/default-avatar.png"}]', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 插入暑期队示例数据
INSERT INTO summer_teams (year, title, description, destination, team_leader, member_count, duration_days, activities, achievements, image_urls, members, created_by, is_active, created_at, updated_at) VALUES
(2023, '2023年暑期科考队', '2023年夏季科学考察活动', '青海可可西里', '李四', 20, 15, '高原生态调查、野生动物观察、环境监测', '完成了可可西里地区的生态调查报告，拍摄了珍贵的野生动物影像资料', '[]', '[{"userId": 2, "name": "李四", "mountainName": "绿水", "avatarUrl": "/images/default-avatar.png"}, {"userId": 3, "name": "王五", "mountainName": "Q坤", "avatarUrl": "/images/default-avatar.png"}, {"userId": 4, "name": "赵六", "mountainName": "山风", "avatarUrl": "/images/default-avatar.png"}]', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2022, '2022年暑期探险队', '2022年夏季探险活动', '新疆天山', '孙七', 16, 12, '高山探险、地质勘探、摄影记录', '成功攀登了天山主峰，完成了地质样本采集', '[]', '[{"userId": 5, "name": "孙七", "mountainName": "林涛", "avatarUrl": "/images/default-avatar.png"}, {"userId": 1, "name": "张三", "mountainName": "青山", "avatarUrl": "/images/default-avatar.png"}]', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 插入秘书处示例数据
INSERT INTO secretariat (year, secretary_general, deputy_secretary, members, achievements, activities, description, image_urls, created_by, updated_by, is_active, created_at, updated_at) VALUES
(2023, '张三', '李四', '[{"userId": 1, "name": "张三", "mountainName": "青山", "avatarUrl": "/images/default-avatar.png"}, {"userId": 2, "name": "李四", "mountainName": "绿水", "avatarUrl": "/images/default-avatar.png"}, {"userId": 3, "name": "王五", "mountainName": "Q坤", "avatarUrl": "/images/default-avatar.png"}]', '成功组织了多次大型活动，提升了协会影响力', '组织年度大会、协调各部门工作、管理协会日常事务', '2023年秘书处在协会发展中发挥了重要作用，积极推进各项改革措施。', '[]', 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2022, '王五', '赵六', '[{"userId": 3, "name": "王五", "mountainName": "Q坤", "avatarUrl": "/images/default-avatar.png"}, {"userId": 4, "name": "赵六", "mountainName": "山风", "avatarUrl": "/images/default-avatar.png"}, {"userId": 5, "name": "孙七", "mountainName": "林涛", "avatarUrl": "/images/default-avatar.png"}]', '建立了完善的管理制度，规范了协会运作', '制定管理制度、组织培训活动、协调对外合作', '2022年秘书处致力于制度建设，为协会长远发展奠定了基础。', '[]', 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2021, '孙七', '周八', '[{"userId": 5, "name": "孙七", "mountainName": "林涛", "avatarUrl": "/images/default-avatar.png"}, {"userId": 1, "name": "张三", "mountainName": "青山", "avatarUrl": "/images/default-avatar.png"}]', '疫情期间创新工作方式，保持协会正常运转', '线上会议组织、数字化管理、疫情防控协调', '2021年面对疫情挑战，秘书处积极应对，确保协会工作不断线。', '[]', 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 插入攀岩队示例数据
INSERT INTO climbing_team (year, team_leader, vice_leader, members, climbing_routes, achievements, training_activities, competitions, equipment, description, image_urls, created_by, updated_by, is_active, created_at, updated_at) VALUES
(2023, '刘强', '陈明', '[{"userId": 1, "name": "刘强", "mountainName": "青山", "avatarUrl": "/images/default-avatar.png"}, {"userId": 2, "name": "陈明", "mountainName": "绿水", "avatarUrl": "/images/default-avatar.png"}, {"userId": 3, "name": "王五", "mountainName": "Q坤", "avatarUrl": "/images/default-avatar.png"}]', '完成了华山北峰、泰山天街等多条经典路线', '获得全国大学生攀岩比赛团体第三名', '每周三次室内训练，每月一次户外实践', '参加全国大学生攀岩锦标赛、北京市攀岩公开赛', '购置了全套攀岩装备，包括绳索、安全带、头盔等', '2023年攀岩队在技术和成绩上都有显著提升，队员们的攀岩水平不断进步。', '[]', 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2022, '黄伟', '林涛', '[{"userId": 4, "name": "赵六", "mountainName": "山风", "avatarUrl": "/images/default-avatar.png"}, {"userId": 5, "name": "孙七", "mountainName": "林涛", "avatarUrl": "/images/default-avatar.png"}]', '挑战了多条高难度攀岩路线', '队员个人最好成绩达到5.12级别', '加强基础体能训练，提升技术水平', '参加北京市大学生攀岩比赛', '维护和更新攀岩装备', '2022年攀岩队注重基础训练，为后续发展打下坚实基础。', '[]', 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- 插入部门示例数据
INSERT INTO department (department_name, department_code, description, responsibilities, current_head, members, contact_info, achievements, image_urls, display_order, created_by, updated_by, is_active, created_at, updated_at) VALUES
('办公室', 'OFFICE', '协会行政管理部门，负责日常事务协调和文档管理', '负责协会日常行政事务、文件管理、会议组织、对外联络等工作', '张主任', '["张主任", "李秘书", "王助理"]', '{"email": "office@senol.org", "phone": "010-12345678"}', '建立了完善的档案管理系统，提高了工作效率', '[]', 1, 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('野生部', 'WILDLIFE', '专注于野生动植物保护和生态研究的专业部门', '开展野生动植物调查、生态保护项目、环境监测、科普宣传等工作', '李部长', '["李部长", "王研究员", "赵调查员", "孙志愿者"]', '{"email": "wildlife@senol.org", "phone": "010-23456789"}', '完成了多项野生动物保护调查项目，发表了重要研究成果', '[]', 2, 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('环保部', 'ENVIRO', '致力于环境保护和可持续发展的行动部门', '组织环保活动、推广绿色理念、开展环境教育、参与环保项目等', '王部长', '["王部长", "赵干事", "孙宣传员"]', '{"email": "enviro@senol.org", "phone": "010-34567890"}', '成功组织了多次大型环保活动，影响了数千名学生', '[]', 3, 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('培训部', 'TRAINING', '负责协会成员技能培训和能力提升的教育部门', '制定培训计划、组织技能培训、开展野外生存训练、安全教育等', '赵部长', '["赵部长", "孙教练", "周助教"]', '{"email": "training@senol.org", "phone": "010-45678901"}', '培训了数百名学员，显著提升了协会整体技能水平', '[]', 4, 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('影像部', 'MEDIA', '负责协会活动记录和宣传材料制作的创意部门', '活动摄影摄像、宣传片制作、图片处理、媒体资料管理等工作', '孙部长', '["孙部长", "周摄影师", "吴剪辑师"]', '{"email": "media@senol.org", "phone": "010-56789012"}', '制作了大量优质的宣传材料，提升了协会形象', '[]', 5, 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('外联部', 'EXTERNAL', '负责对外合作和资源整合的联络部门', '寻找合作伙伴、申请项目资金、维护外部关系、组织交流活动等', '周部长', '["周部长", "吴联络员", "郑协调员"]', '{"email": "external@senol.org", "phone": "010-67890123"}', '建立了广泛的合作网络，为协会争取了丰富的资源', '[]', 6, 1, 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

