-- 混合检索表结构：向量列 + tsvector 列（需手动执行）
-- 在已启用 pgvector 的库中执行，例如: psql $PG_CONNECTION -f langchain/03-rag/schema.sql

CREATE EXTENSION IF NOT EXISTS vector;

-- 表：内容、向量、全文检索列（content_tsv 由 content 自动生成）
-- EMBEDDING_DIM 需与 embedding 模型维度一致（DashScope text-embedding-v3 为 1024）
CREATE TABLE IF NOT EXISTS rag_hybrid (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  content    text NOT NULL,
  embedding  vector(1024) NOT NULL,
  content_tsv tsvector GENERATED ALWAYS AS (to_tsvector('simple', content)) STORED,
  metadata   jsonb DEFAULT '{}'
);

-- 向量相似度索引（余弦）
CREATE INDEX IF NOT EXISTS rag_hybrid_embedding_idx ON rag_hybrid
  USING hnsw (embedding vector_cosine_ops) WITH (m = 16, ef_construction = 64);

-- 全文检索 GIN 索引
CREATE INDEX IF NOT EXISTS rag_hybrid_content_tsv_idx ON rag_hybrid
  USING GIN (content_tsv);

-- RRF 融合用辅助函数（混合检索时合并向量排序与全文排序）
CREATE OR REPLACE FUNCTION rrf_score(rank bigint, rrf_k int DEFAULT 50)
RETURNS numeric
LANGUAGE SQL
IMMUTABLE PARALLEL SAFE
AS $$
  SELECT COALESCE(1.0 / ($1 + $2), 0.0);
$$;

COMMENT ON TABLE rag_hybrid IS 'RAG 混合检索：embedding 向量 + content_tsv 全文';
