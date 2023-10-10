use crate::database::profile::BinProfile;
use serde::de::DeserializeOwned;
use serde::Serialize;
use sqlx::mysql::MySqlConnectOptions;
use sqlx::postgres::PgConnectOptions;
use sqlx::sqlite::SqliteConnectOptions;
use sqlx::{MySqlPool, PgPool, SqlitePool};
use std::path::{Path, PathBuf};
use thiserror::Error;
use tokio::fs;
use tokio::io::{AsyncReadExt, AsyncWriteExt};

mod profile;
pub use profile::*;

#[derive(Debug, Error)]
pub enum DatabaseError {
    #[error("{0}")]
    Sql(#[from] sqlx::Error),
    #[error("{0}")]
    Bin(#[from] BinError),
}

#[derive(Debug)]
pub enum Driver {
    Mysql(MySqlPool),
    Postgres(PgPool),
    Sqlite(SqlitePool),
    Bin(PathBuf),
}

pub enum DriverType<'a> {
    Mysql(DatabaseOptions<'a>),
    Postgres(DatabaseOptions<'a>),
    Sqlite(PathBuf),
    Bin(PathBuf),
}

pub struct DatabaseOptions<'a> {
    pub name: &'a str,
    pub user: &'a str,
    pub passw: &'a str,
    pub host: &'a str,
    pub port: u16,
}

impl Driver {
    pub async fn new(driver_type: DriverType<'_>) -> Result<Self, DatabaseError> {
        let driver = match driver_type {
            DriverType::Mysql(c) => Self::new_mysql(c).await?,
            DriverType::Postgres(c) => Self::new_postgres(c).await?,
            DriverType::Sqlite(c) => Self::new_sqlite(&c).await?,
            DriverType::Bin(c) => Self::new_bin(c).await?,
        };

        Ok(driver)
    }

    async fn new_mysql(options: DatabaseOptions<'_>) -> Result<Self, sqlx::Error> {
        let opts = MySqlConnectOptions::new()
            .database(options.name)
            .username(options.user)
            .password(options.passw)
            .host(options.host)
            .port(options.port);

        let pool = MySqlPool::connect_with(opts).await?;

        sqlx::query(include_str!("skins.sql"))
            .execute(&mut *pool.acquire().await?)
            .await?;

        Ok(Self::Mysql(pool))
    }

    async fn new_postgres(options: DatabaseOptions<'_>) -> Result<Self, sqlx::Error> {
        let opts = PgConnectOptions::new()
            .database(options.name)
            .username(options.user)
            .password(options.passw)
            .host(options.host)
            .port(options.port);

        let pool = PgPool::connect_with(opts).await?;
        sqlx::query(include_str!("skins.sql"))
            .execute(&mut *pool.acquire().await?)
            .await?;


        Ok(Self::Postgres(pool))
    }

    async fn new_sqlite(path: &Path) -> Result<Self, sqlx::Error> {
        let opts = SqliteConnectOptions::new().filename(path);

        let pool = SqlitePool::connect_with(opts).await?;
        sqlx::query(include_str!("skins.sql"))
            .execute(&mut *pool.acquire().await?)
            .await?;


        Ok(Self::Sqlite(pool))
    }

    async fn new_bin(path: PathBuf) -> Result<Self, BinError> {
        if !path.exists() {
            if let Some(parent) = path.parent() {
                fs::create_dir_all(parent).await?;
            }

            BinFile::write::<Vec<BinProfile>>(&path, &vec![]).await?;
        }

        Ok(Self::Bin(path))
    }
}

#[derive(Debug, Error)]
pub enum BinError {
    #[error("IO Error: {0}")]
    Io(#[from] std::io::Error),
    #[error("Binary (de)serialization error: {0}")]
    Bin(#[from] bincode::Error),
}

pub struct BinFile;

impl BinFile {
    async fn read<T: DeserializeOwned>(path: &Path) -> Result<T, BinError> {
        let mut file = fs::File::open(path).await?;
        let mut buf = Vec::new();
        file.read_to_end(&mut buf).await?;

        let deserialized: T = bincode::deserialize(&buf)?;

        Ok(deserialized)
    }

    async fn write<T: Serialize>(path: &Path, content: &T) -> Result<(), BinError> {
        let mut file = fs::File::create(path).await?;
        let buf = bincode::serialize(content)?;

        file.write_all(&buf).await?;

        Ok(())
    }
}
