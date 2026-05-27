#!/usr/bin/env bash

#### Set below DB configs to connect to your own database
# Comment out below exports to config your DB connection
# Supported DB_TYPE:  h2, mysql, postgres
export DB_TYPE=mysql
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=bi
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=123456
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=
export REDIS_DATABASE=0
export FILE_STORAGE_PATH=/uploads
export FILE_VISIT_PATH_PREFIX=http://localhost:8080/file
export OPENAI_API_BASE_URL=
export OPENAI_API_KEY=
export OPENAI_DEFAULT_MODEL=
export OPENAI_DEFAULT_MODEL_MAX_TOKENS=
export OPENAI_CODE_MODEL=
export OPENAI_CODE_MODEL_MAX_TOKENS=
export PROMPT_GENERATE_SQL_OUTPUT_MODEL=default
export JWT_SECRET=
export JWT_TTL_SECONDS=43200
export DB_INIT_MODE=never
export SM2_PRIVATE_KEY=