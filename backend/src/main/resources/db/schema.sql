CREATE DATABASE IF NOT EXISTS trade_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE trade_db;

-- Seata AT undo log（Seata 2.x）
CREATE TABLE IF NOT EXISTS undo_log (
    branch_id     BIGINT       NOT NULL COMMENT 'branch transaction id',
    xid           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    context       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    rollback_info LONGBLOB     NOT NULL COMMENT 'rollback info',
    log_status    INT          NOT NULL COMMENT '0:normal status,1:defense status',
    log_created   DATETIME(6)  NOT NULL COMMENT 'create datetime',
    log_modified  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction mode undo table';

CREATE TABLE IF NOT EXISTS trade_product (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    name          VARCHAR(128) NOT NULL,
    description   VARCHAR(512),
    price         DECIMAL(12,2) NOT NULL,
    cover_url     VARCHAR(256),
    default_sku_id BIGINT,
    created_at    DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trade_sku (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id      BIGINT NOT NULL,
    sku_code        VARCHAR(64) NOT NULL,
    title           VARCHAR(128) NOT NULL,
    total_stock     INT NOT NULL DEFAULT 0,
    reserved_stock  INT NOT NULL DEFAULT 0,
    version         INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_sku_code (sku_code),
    KEY idx_sku_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trade_order (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no      VARCHAR(64) NOT NULL,
    user_id       BIGINT NOT NULL,
    status        VARCHAR(32) NOT NULL,
    total_amount  DECIMAL(12,2) NOT NULL,
    remark        VARCHAR(256),
    created_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_order_user (user_id),
    KEY idx_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trade_order_item (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id      BIGINT NOT NULL,
    sku_id       BIGINT NOT NULL,
    product_name  VARCHAR(128) NOT NULL,
    quantity      INT NOT NULL,
    unit_price    DECIMAL(12,2) NOT NULL,
    KEY idx_item_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trade_payment_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    biz_type        VARCHAR(16) NOT NULL COMMENT 'PAY / REFUND',
    order_no        VARCHAR(64) NOT NULL,
    channel_pay_id  VARCHAR(128) NOT NULL,
    amount          DECIMAL(12,2) NOT NULL,
    status          VARCHAR(16) NOT NULL DEFAULT 'SUCCESS',
    created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_payment_idem (biz_type, channel_pay_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trade_shipment (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no     VARCHAR(64) NOT NULL,
    carrier      VARCHAR(64) NOT NULL DEFAULT 'MOCK',
    tracking_no  VARCHAR(64) NOT NULL,
    status       VARCHAR(32) NOT NULL,
    created_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    KEY idx_ship_order (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO trade_product (id, name, description, price, cover_url, default_sku_id)
VALUES (1, '示例机械键盘', '教学用虚拟商品', 399.00, 'https://picsum.photos/seed/keyboard/400/300', 1),
       (2, '示例人体工学椅', '教学用虚拟商品', 1299.00, 'https://picsum.photos/seed/chair/400/300', 2)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO trade_sku (id, product_id, sku_code, title, total_stock, reserved_stock, version)
VALUES (1, 1, 'SKU-KB-001', '红轴 87 键', 500, 0, 0),
       (2, 2, 'SKU-CH-001', '黑色 标准版', 200, 0, 0)
ON DUPLICATE KEY UPDATE title = VALUES(title), total_stock = VALUES(total_stock);

UPDATE trade_product SET default_sku_id = 1 WHERE id = 1;
UPDATE trade_product SET default_sku_id = 2 WHERE id = 2;
