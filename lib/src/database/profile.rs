use crate::database::{BinError, BinFile, DatabaseError, Driver};
use serde::{Deserialize, Serialize};
use sqlx::{Error, FromRow, MySqlPool, PgPool, SqlitePool};
use std::path::Path;

#[derive(Debug, FromRow)]
pub struct Profile {
    pub value: String,
    pub signature: String,
    pub uuid: String,
}

#[derive(Deserialize, Serialize)]
pub struct BinProfile {
    uuid: String,
    value: String,
    signature: String,
}

impl Profile {
    pub async fn get_by_uuid(driver: &Driver, uuid: &str) -> Result<Option<Self>, DatabaseError> {
        let item = match driver {
            Driver::Mysql(e) => Self::get_by_uuid_mysql(e, uuid).await?,
            Driver::Postgres(e) => Self::get_by_uuid_postgres(e, uuid).await?,
            Driver::Sqlite(e) => Self::get_by_uuid_sqlite(e, uuid).await?,
            Driver::Bin(p) => Self::get_by_uuid_bin(p, uuid).await?,
        };

        Ok(item)
    }

    async fn get_by_uuid_mysql(driver: &MySqlPool, uuid: &str) -> Result<Option<Self>, Error> {
        sqlx::query_as("SELECT * FROM skins WHERE uuid = ?")
            .bind(uuid)
            .fetch_optional(driver)
            .await
    }

    async fn get_by_uuid_postgres(driver: &PgPool, uuid: &str) -> Result<Option<Self>, Error> {
        sqlx::query_as("SELECT * FROM skins WHERE uuid = $1")
            .bind(uuid)
            .fetch_optional(driver)
            .await
    }

    async fn get_by_uuid_sqlite(driver: &SqlitePool, uuid: &str) -> Result<Option<Self>, Error> {
        sqlx::query_as("SELECT * FROM skins WHERE uuid = ?")
            .bind(uuid)
            .fetch_optional(driver)
            .await
    }

    async fn get_by_uuid_bin(path: &Path, uuid: &str) -> Result<Option<Self>, BinError> {
        let contents: Vec<BinProfile> = BinFile::read(path).await?;
        Ok(contents
            .into_iter()
            .find(|f| f.uuid.eq(uuid))
            .map(|f| Self {
                uuid: f.uuid,
                signature: f.signature,
                value: f.value,
            }))
    }

    pub async fn set_skin_profile(
        driver: &Driver,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), DatabaseError> {
        if Self::get_by_uuid(driver, uuid).await?.is_some() {
            Self::update_skin_profile(driver, uuid, value, signature).await?;
        } else {
            Self::insert_skin_profile(driver, uuid, value, signature).await?;
        }

        Ok(())
    }

    async fn insert_skin_profile(
        driver: &Driver,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), DatabaseError> {
        match driver {
            Driver::Mysql(e) => Self::insert_skin_profile_mysql(e, uuid, value, signature).await?,
            Driver::Postgres(e) => {
                Self::insert_skin_profile_postgres(e, uuid, value, signature).await?
            }
            Driver::Sqlite(e) => {
                Self::insert_skin_profile_sqlite(e, uuid, value, signature).await?
            }
            Driver::Bin(p) => Self::insert_skin_profile_bin(p, uuid, value, signature).await?,
        }

        Ok(())
    }

    async fn insert_skin_profile_mysql(
        driver: &MySqlPool,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), Error> {
        sqlx::query("INSERT INTO skins (uuid, value, signature) VALUES (?, ?, ?)")
            .bind(uuid)
            .bind(value)
            .bind(signature)
            .execute(driver)
            .await?;
        Ok(())
    }

    async fn insert_skin_profile_postgres(
        driver: &PgPool,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), Error> {
        sqlx::query("INSERT INTO skins (uuid, value, signature) VALUES ($1, $2, $3)")
            .bind(uuid)
            .bind(value)
            .bind(signature)
            .execute(driver)
            .await?;
        Ok(())
    }

    async fn insert_skin_profile_sqlite(
        driver: &SqlitePool,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), Error> {
        sqlx::query("INSERT INTO skins (uuid, value, signature) VALUES (?, ?, ?)")
            .bind(uuid)
            .bind(value)
            .bind(signature)
            .execute(driver)
            .await?;
        Ok(())
    }

    async fn insert_skin_profile_bin(
        path: &Path,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), BinError> {
        let mut contents: Vec<BinProfile> = BinFile::read(path).await?;
        contents.push(BinProfile {
            uuid: uuid.to_string(),
            value: value.to_string(),
            signature: signature.to_string(),
        });

        BinFile::write(path, &contents).await?;

        Ok(())
    }

    async fn update_skin_profile(
        driver: &Driver,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), DatabaseError> {
        match driver {
            Driver::Mysql(e) => Self::update_skin_profile_mysql(e, uuid, value, signature).await?,
            Driver::Postgres(e) => {
                Self::update_skin_profile_postgres(e, uuid, value, signature).await?
            }
            Driver::Sqlite(e) => {
                Self::update_skin_profile_sqlite(e, uuid, value, signature).await?
            }
            Driver::Bin(p) => Self::update_skin_profile_bin(p, uuid, value, signature).await?,
        }

        Ok(())
    }

    async fn update_skin_profile_mysql(
        driver: &MySqlPool,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), Error> {
        sqlx::query("UPDATE skins SET value = ?, signature = ? WHERE uuid = ?")
            .bind(value)
            .bind(signature)
            .bind(uuid)
            .execute(driver)
            .await?;
        Ok(())
    }

    async fn update_skin_profile_postgres(
        driver: &PgPool,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), Error> {
        sqlx::query("UPDATE skins SET value = $1, signature = $2 WHERE uuid = $3")
            .bind(value)
            .bind(signature)
            .bind(uuid)
            .execute(driver)
            .await?;
        Ok(())
    }

    async fn update_skin_profile_sqlite(
        driver: &SqlitePool,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), Error> {
        sqlx::query("UPDATE skins SET value = ?, signature = ? WHERE uuid = ?")
            .bind(value)
            .bind(signature)
            .bind(uuid)
            .execute(driver)
            .await?;
        Ok(())
    }

    async fn update_skin_profile_bin(
        path: &Path,
        uuid: &str,
        value: &str,
        signature: &str,
    ) -> Result<(), BinError> {
        let contents: Vec<BinProfile> = BinFile::read(path).await?;
        let contents = contents
            .into_iter()
            .map(|mut f| {
                if f.uuid.eq(uuid) {
                    f.value = value.to_string();
                    f.signature = signature.to_string();
                }

                f
            })
            .collect::<Vec<_>>();

        BinFile::write(path, &contents).await?;

        Ok(())
    }

    pub async fn delete(driver: &Driver, uuid: &str) -> Result<(), DatabaseError> {
        match driver {
            Driver::Mysql(e) => Self::delete_mysql(e, uuid).await?,
            Driver::Postgres(e) => Self::delete_postgres(e, uuid).await?,
            Driver::Sqlite(e) => Self::delete_sqlite(e, uuid).await?,
            Driver::Bin(p) => Self::delete_bin(p, uuid).await?,
        }

        Ok(())
    }

    async fn delete_mysql(driver: &MySqlPool, uuid: &str) -> Result<(), Error> {
        sqlx::query("DELETE FROM skins WHERE uuid = ?")
            .bind(uuid)
            .execute(driver)
            .await?;
        Ok(())
    }

    async fn delete_postgres(driver: &PgPool, uuid: &str) -> Result<(), Error> {
        sqlx::query("DELETE FROM skins WHERE uuid = $1")
            .bind(uuid)
            .execute(driver)
            .await?;
        Ok(())
    }

    async fn delete_sqlite(driver: &SqlitePool, uuid: &str) -> Result<(), Error> {
        sqlx::query("DELETE FROM skins WHERE uuid = ?")
            .bind(uuid)
            .execute(driver)
            .await?;
        Ok(())
    }

    async fn delete_bin(path: &Path, uuid: &str) -> Result<(), BinError> {
        let contents: Vec<BinProfile> = BinFile::read(path).await?;
        let contents = contents
            .into_iter()
            .filter(|f| f.uuid.ne(uuid))
            .collect::<Vec<_>>();

        BinFile::write(path, &contents).await?;

        Ok(())
    }
}
