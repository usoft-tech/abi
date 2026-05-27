-- agentscope.agentscope_sessions definition

CREATE TABLE IF NOT EXISTS `agentscope_sessions` (
  `session_id` varchar(255) NOT NULL,
  `state_key` varchar(255) NOT NULL,
  `item_index` int NOT NULL DEFAULT '0',
  `state_data` longtext NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`session_id`,`state_key`,`item_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;