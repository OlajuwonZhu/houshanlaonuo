-- Ensure UTF-8 encoding for this script
SET client_encoding = 'UTF8';

BEGIN;

-- Add system admin flag to users table
ALTER TABLE IF EXISTS users
    ADD COLUMN IF NOT EXISTS is_admin BOOLEAN NOT NULL DEFAULT FALSE;

-- Ensure default system user text is correct (id=1)
UPDATE users
SET name = '系统用户', mountain_name = 'System', bio = '系统默认用户，用于处理未登录用户的操作'
WHERE id = 1 AND open_id = 'system';

-- Seed product categories (insert if missing)
INSERT INTO product_categories (name, description, sort_order)
SELECT '会服', '社团文化衫、徽章等定制用品', 1
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE name = '会服');

INSERT INTO product_categories (name, description, sort_order)
SELECT '装备', '户外装备、露营用品', 2
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE name = '装备');

INSERT INTO product_categories (name, description, sort_order)
SELECT '周边', '贴纸、水杯等周边产品', 3
WHERE NOT EXISTS (SELECT 1 FROM product_categories WHERE name = '周边');

-- Seed example products (insert if missing by name)
INSERT INTO products (category_id, name, description, price, stock_quantity, image_urls, specifications, is_active, created_at, updated_at)
SELECT
    (SELECT id FROM product_categories WHERE name='会服' LIMIT 1),
    'SENOL文化衫', '社团定制文化衫，100%纯棉', 89.00, 100, '[]', '{"sizes":["S","M","L","XL","XXL"],"colors":["白色","橙色","黑色"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SENOL文化衫');

INSERT INTO products (category_id, name, description, price, stock_quantity, image_urls, specifications, is_active, created_at, updated_at)
SELECT
    (SELECT id FROM product_categories WHERE name='会服' LIMIT 1),
    'SENOL徽章', '社团logo金属徽章，可调节大小', 45.00, 50, '[]', '{"colors":["橙色","深绿"]}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SENOL徽章');

INSERT INTO products (category_id, name, description, price, stock_quantity, image_urls, specifications, is_active, created_at, updated_at)
SELECT
    (SELECT id FROM product_categories WHERE name='周边' LIMIT 1),
    'SENOL贴纸', '社团周边贴纸', 25.00, 200, '[]', '{}', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'SENOL贴纸');

-- Seed association basic info (insert if missing by info_type)
INSERT INTO association_info (info_type, title, content, display_order, is_active, created_at, updated_at)
SELECT 'FLAG', '协会会旗', '协会会旗体现我们对自然的热爱与对登山精神的追求', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM association_info WHERE info_type = 'FLAG');

INSERT INTO association_info (info_type, title, content, display_order, is_active, created_at, updated_at)
SELECT 'FUND', '协会基金会', '协会基金会成立于2010年，用于支持科普与户外教育等活动', 4, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM association_info WHERE info_type = 'FUND');

INSERT INTO association_info (info_type, title, content, display_order, is_active, created_at, updated_at)
SELECT 'EMBLEM', '协会会徽', '会徽融合多种元素，体现协会主题', 2, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM association_info WHERE info_type = 'EMBLEM');

INSERT INTO association_info (info_type, title, content, display_order, is_active, created_at, updated_at)
SELECT 'SONG', '协会会歌', '协会会歌', 3, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM association_info WHERE info_type = 'SONG');

COMMIT;
