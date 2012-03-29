-- Table: lang

-- DROP TABLE lang;

CREATE TABLE lang (
  id serial NOT NULL,
  lang_short character varying,
  lang_medium character varying NOT NULL,
  CONSTRAINT lang_id_pkey PRIMARY KEY (id ),
  CONSTRAINT lang_lang_medium_key UNIQUE (lang_medium )
);

-- Table: article

-- DROP TABLE article;

CREATE TABLE article (
  id serial NOT NULL,
  lang_id integer NOT NULL,
  name character varying NOT NULL,
  portlet_id character varying NOT NULL,
  wikitext character varying,
  title character varying NOT NULL,
  CONSTRAINT article_id_pkey PRIMARY KEY (id ),
  CONSTRAINT wiki_text_lang_id_fkey FOREIGN KEY (lang_id)
      REFERENCES lang (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT article_name_key UNIQUE (name , portlet_id , lang_id )
);